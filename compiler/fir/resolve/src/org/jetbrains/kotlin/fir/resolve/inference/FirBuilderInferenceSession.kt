/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.inference

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.languageVersionSettings
import org.jetbrains.kotlin.fir.resolve.calls.Candidate
import org.jetbrains.kotlin.fir.resolve.calls.ImplicitExtensionReceiverValue
import org.jetbrains.kotlin.fir.resolve.calls.ResolutionContext
import org.jetbrains.kotlin.fir.resolve.calls.candidate
import org.jetbrains.kotlin.fir.resolve.inference.model.ConeBuilderInferenceSubstitutionConstraintPosition
import org.jetbrains.kotlin.fir.resolve.substitution.AbstractConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.ChainedSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.replaceStubsAndTypeVariablesToErrors
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirDefaultTransformer
import org.jetbrains.kotlin.fir.visitors.transformSingle
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemBuilder
import org.jetbrains.kotlin.resolve.calls.inference.buildAbstractResultingSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.components.ConstraintSystemCompletionMode
import org.jetbrains.kotlin.resolve.calls.inference.model.*
import org.jetbrains.kotlin.resolve.calls.inference.registerTypeVariableIfNotPresent
import org.jetbrains.kotlin.resolve.descriptorUtil.BUILDER_INFERENCE_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.types.model.TypeConstructorMarker
import org.jetbrains.kotlin.types.model.TypeSubstitutorMarker
import org.jetbrains.kotlin.types.model.safeSubstitute

/**
 * General documentation for builder inference algorithm is located at `/docs/fir/builder_inference.md`
 */
class FirBuilderInferenceSession(
    private val lambda: FirAnonymousFunction,
    resolutionContext: ResolutionContext,
    private val stubsForPostponedVariables: Map<ConeTypeVariable, ConeStubType>,
) : FirInferenceSessionForChainedResolve(resolutionContext) {
    private val session = resolutionContext.session
    private val commonCalls: MutableList<Pair<FirStatement, Candidate>> = mutableListOf()
    private var lambdaImplicitReceivers: MutableList<ImplicitExtensionReceiverValue> = mutableListOf()

    override fun <T> shouldRunCompletion(call: T): Boolean where T : FirResolvable, T : FirStatement {
        val candidate = call.candidate
        val system = candidate.system

        if (system.hasContradiction) return true
        if (!candidate.isSuitableForBuilderInference()) return true


        val storage = system.getBuilder().currentStorage()

        if (call.hasPostponed()) return true

        return storage.notFixedTypeVariables.keys.all {
            val variable = storage.allTypeVariables[it]
            val isPostponed = variable != null && variable in storage.postponedTypeVariables
            isPostponed || components.callCompleter.completer.variableFixationFinder.isTypeVariableHasProperConstraint(system, it)
        }
    }

    private fun Candidate.isSuitableForBuilderInference(): Boolean {
        val extensionReceiver = chosenExtensionReceiver
        val dispatchReceiver = dispatchReceiver
        return when {
            extensionReceiver == null && dispatchReceiver == null -> false
            dispatchReceiver?.resolvedType?.containsStubType() == true -> true
            extensionReceiver?.resolvedType?.containsStubType() == true -> symbol.fir.hasBuilderInferenceAnnotation(session)
            else -> false
        }
    }

    private fun ConeKotlinType.containsStubType(): Boolean {
        return this.contains {
            it is ConeStubTypeForChainInference
        }
    }

    private fun FirStatement.hasPostponed(): Boolean {
        var result = false
        processAllContainingCallCandidates(processBlocks = false) {
            result = result || it.hasPostponed()
        }
        return result
    }

    private fun Candidate.hasPostponed(): Boolean {
        return postponedAtoms.any { !it.analyzed }
    }

    fun addLambdaImplicitReceiver(receiver: ImplicitExtensionReceiverValue) {
        lambdaImplicitReceivers += receiver
    }

    override fun <T> addCompletedCall(call: T, candidate: Candidate) where T : FirResolvable, T : FirStatement {
        if (skipCall(call)) return
        commonCalls += call to candidate
    }

    @Suppress("UNUSED_PARAMETER")
    private fun <T> skipCall(call: T): Boolean where T : FirResolvable, T : FirStatement {
        // TODO: what is FIR analog?
        // if (descriptor is FakeCallableDescriptorForObject) return true
        // if (!DescriptorUtils.isObject(descriptor) && isInLHSOfDoubleColonExpression(callInfo)) return true

        return false
    }

    override fun inferPostponedVariables(
        lambda: ResolvedLambdaAtom,
        constraintSystemBuilder: ConstraintSystemBuilder,
        completionMode: ConstraintSystemCompletionMode,
        candidate: Candidate
    ): Map<ConeTypeVariableTypeConstructor, ConeKotlinType>? {
        val (commonSystem, effectivelyEmptyConstraintSystem) = buildCommonSystem(constraintSystemBuilder.currentStorage())
        val resultingSubstitutor by lazy { getResultingSubstitutor(commonSystem) }

        if (effectivelyEmptyConstraintSystem) {
            updateCalls(resultingSubstitutor)
            return null
        }

        val context = commonSystem.asConstraintSystemCompleterContext()
        components.callCompleter.completer.complete(
            context,
            completionMode,
            partiallyResolvedCalls.map { it.first as FirStatement },
            components.session.builtinTypes.unitType.type, resolutionContext,
            collectVariablesFromContext = true
        ) {
            error("Shouldn't be called in complete constraint system mode")
        }

        if (completionMode == ConstraintSystemCompletionMode.FULL) {
            constraintSystemBuilder.substituteFixedVariables(resultingSubstitutor)
        }

        if (!session.languageVersionSettings.supportsFeature(LanguageFeature.NoAdditionalErrorsInK1DiagnosticReporter)) {
            for (error in commonSystem.errors) {
                candidate.system.addError(error)
            }
        }
        updateCalls(resultingSubstitutor)

        @Suppress("UNCHECKED_CAST")
        return commonSystem.fixedTypeVariables as Map<ConeTypeVariableTypeConstructor, ConeKotlinType>
    }

    private fun buildCommonSystem(initialStorage: ConstraintStorage): Pair<NewConstraintSystemImpl, Boolean> {
        // TODO(KT-64034): Missing handling of parent builder inference sessions
        //  - See [org.jetbrains.kotlin.resolve.calls.inference.BuilderInferenceSession.initializeCommonSystem]

        val commonSystem = components.session.inferenceComponents.createConstraintSystem()
        val nonFixedToVariablesSubstitutor = createNonFixedTypeToVariableSubstitutor()

        var effectivelyEmptyCommonSystem =
            !integrateConstraints(commonSystem, initialStorage, nonFixedToVariablesSubstitutor, false)

        for ((_, candidate) in commonCalls) {
            val hasConstraints =
                integrateConstraints(commonSystem, candidate.system.asReadOnlyStorage(), nonFixedToVariablesSubstitutor, false)
            if (hasConstraints) effectivelyEmptyCommonSystem = false
        }
        for ((_, candidate) in partiallyResolvedCalls) {
            val hasConstraints =
                integrateConstraints(commonSystem, candidate.system.asReadOnlyStorage(), nonFixedToVariablesSubstitutor, true)
            if (hasConstraints) effectivelyEmptyCommonSystem = false
        }

        return commonSystem to effectivelyEmptyCommonSystem
    }

    private fun createNonFixedTypeToVariableSubstitutor(): ConeSubstitutor {
        val typeContext = components.session.typeContext

        val bindings = mutableMapOf<TypeConstructorMarker, ConeKotlinType>()
        for ((variable, nonFixedType) in stubsForPostponedVariables) {
            bindings[nonFixedType.constructor] = variable.defaultType
        }

        return typeContext.typeSubstitutorByTypeConstructor(bindings)
    }

    private fun integrateConstraints(
        commonSystem: NewConstraintSystemImpl,
        storage: ConstraintStorage,
        nonFixedToVariablesSubstitutor: ConeSubstitutor,
        shouldIntegrateAllConstraints: Boolean
    ): Boolean {
        storage.notFixedTypeVariables.values.forEach { commonSystem.registerTypeVariableIfNotPresent(it.typeVariable) }

        /*
        * storage can contain the following substitutions:
        *  TypeVariable(A) -> ProperType
        *  TypeVariable(B) -> Special-Non-Fixed-Type
        *
        * while substitutor from parameter map non-fixed types to the original type variable
        * */
        val callSubstitutor =
            storage.buildAbstractResultingSubstitutor(commonSystem, transformTypeVariablesToErrorTypes = false) as ConeSubstitutor

        var introducedConstraint = false

        for (initialConstraint in storage.initialConstraints) {
            if (initialConstraint.position is BuilderInferencePosition) continue
            if (integrateConstraintToSystem(
                    commonSystem, initialConstraint, callSubstitutor, nonFixedToVariablesSubstitutor
                )
            ) {
                introducedConstraint = true
            }
        }

        if (shouldIntegrateAllConstraints) {
            for ((variableConstructor, type) in storage.fixedTypeVariables) {
                val typeVariable = storage.allTypeVariables.getValue(variableConstructor)
                commonSystem.registerTypeVariableIfNotPresent(typeVariable)
                commonSystem.addEqualityConstraint((typeVariable as ConeTypeVariable).defaultType, type, BuilderInferencePosition)
                introducedConstraint = true
            }
        }

        return introducedConstraint
    }

    private fun extractCommonCapturedTypes(lower: ConeKotlinType, upper: ConeKotlinType): List<ConeCapturedType> {
        val extractedCapturedTypes = mutableSetOf<ConeCapturedType>().also { extractCapturedTypesTo(lower, it) }
        return extractedCapturedTypes.filter { capturedType ->
            upper.contains { it is ConeCapturedType && it.constructor === capturedType.constructor }
        }
    }

    private fun extractCapturedTypesTo(type: ConeKotlinType, to: MutableSet<ConeCapturedType>) {
        if (type is ConeCapturedType) {
            to.add(type)
        }
        for (typeArgument in type.typeArguments) {
            if (typeArgument !is ConeKotlinTypeProjection) continue
            extractCapturedTypesTo(typeArgument.type, to)
        }
    }

    private fun getResultingSubstitutor(commonSystem: NewConstraintSystemImpl): ConeSubstitutor {
        val nonFixedToVariablesSubstitutor = createNonFixedTypeToVariableSubstitutor()
        val commonSystemSubstitutor = commonSystem.buildCurrentSubstitutor() as ConeSubstitutor
        return ChainedSubstitutor(nonFixedToVariablesSubstitutor, commonSystemSubstitutor)
            .replaceStubsAndTypeVariablesToErrors(resolutionContext.typeContext, stubsForPostponedVariables.values.map { it.constructor })
    }

    private fun updateCalls(substitutor: ConeSubstitutor) {
        val stubTypeSubstitutor = FirStubTypeTransformer(substitutor)
        lambda.transformSingle(stubTypeSubstitutor, null)

        // TODO: Builder inference should not modify implicit receivers. KT-54708
        for (receiver in lambdaImplicitReceivers) {
            @Suppress("DEPRECATION_ERROR")
            receiver.updateTypeInBuilderInference(substitutor.substituteOrSelf(receiver.type))
        }

        val completionResultsWriter = components.callCompleter.createCompletionResultsWriter(substitutor)
        for ((call, _) in partiallyResolvedCalls) {
            call.transformSingle(completionResultsWriter, null)
        }
    }

    private fun integrateConstraintToSystem(
        commonSystem: NewConstraintSystemImpl,
        initialConstraint: InitialConstraint,
        callSubstitutor: ConeSubstitutor,
        nonFixedToVariablesSubstitutor: ConeSubstitutor,
    ): Boolean {
        val substitutedConstraintWith =
            initialConstraint.substitute(callSubstitutor).substituteNonFixedToVariables(nonFixedToVariablesSubstitutor)

        val substitutedA = substitutedConstraintWith.a
        val substitutedB = substitutedConstraintWith.b

        if (commonSystem.isProperType(substitutedA) && (substitutedA == substitutedB || commonSystem.isProperType(substitutedB))) return false

        val position = substitutedConstraintWith.position
        when (initialConstraint.constraintKind) {
            ConstraintKind.LOWER -> error("LOWER constraint shouldn't be used, please use UPPER")

            ConstraintKind.UPPER -> commonSystem.addSubtypeConstraint(substitutedA, substitutedB, position)

            ConstraintKind.EQUALITY ->
                with(commonSystem) {
                    addSubtypeConstraint(substitutedA, substitutedB, position)
                    addSubtypeConstraint(substitutedB, substitutedA, position)
                }
        }
        return true
    }

    private fun InitialConstraint.substitute(substitutor: TypeSubstitutorMarker): InitialConstraint {
        val substitutedA = substitutor.safeSubstitute(resolutionContext.typeContext, this.a)
        val substitutedB = substitutor.safeSubstitute(resolutionContext.typeContext, this.b)

        if (substitutedA == a && substitutedB == b) return this

        // TODO(KT-64033): Missing check for ForbidInferringPostponedTypeVariableIntoDeclaredUpperBound language feature

        return InitialConstraint(
            substitutedA,
            substitutedB,
            this.constraintKind,
            ConeBuilderInferenceSubstitutionConstraintPosition(this)
        )
    }

    /**
     * This function substitutes stub types in constraint with the corresponding type variable
     * Please make sure to pass correct {Stub(Tv) => Tv} substitutor
     *
     * E.g.:
     * ```
     * nonFixedToVariablesSubstitutor = {Stub(Tv) => Tv, Stub(Ov) => Ov}
     * constraint: A<Stub(Tv)> <: B<Stub(Ov)> -> A<Tv> <: B<Ov>
     * ```
     *
     * The main reason for this function's existence is the custom handling of captured types.
     * See KT-64027
     *
     * @return Constraint, substituted according to the [nonFixedToVariablesSubstitutor]
     */
    private fun InitialConstraint.substituteNonFixedToVariables(
        nonFixedToVariablesSubstitutor: ConeSubstitutor
    ): InitialConstraint {
        require(constraintKind != ConstraintKind.LOWER)

        // TODO(KT-63996, KT-64027): This function assumes types passed in lower, upper order which isn't true for equality constraints
        val commonCapTypes = extractCommonCapturedTypes(lower = a as ConeKotlinType, upper = b as ConeKotlinType)

        // TODO(KT-64027): This logic tries to work-around the problem with type substitution consistency in captured types
        //  In order to preserve consistency we collect captured types from both a and b and substitute them collectively
        //  E.g:
        //  substitutor = { B => W }
        //  a = C<CapturedType(out B)_0>, b = D<CapturedType(out B)_0>
        //  commonCapTypes = [CapturedType(out B)_0]
        //  capTypesSubstitutor = { CapturedTypeConstructor_0 => CapturedType(out W)_1 }
        //  substitutedA = C<CapturedType(out W)_1>
        //  substitutedB = D<CapturedType(out W)_1>
        val substitutedCommonCapType = commonCapTypes.associate {
            it.constructor to nonFixedToVariablesSubstitutor.substituteOrSelf(it)
        }

        val capTypesSubstitutor = object : AbstractConeSubstitutor(resolutionContext.typeContext) {
            override fun substituteType(type: ConeKotlinType): ConeKotlinType? {
                if (type !is ConeCapturedType) return null
                return substitutedCommonCapType[type.constructor]
            }
        }

        val substitutedA = nonFixedToVariablesSubstitutor.safeSubstitute(
            resolutionContext.typeContext,
            capTypesSubstitutor.safeSubstitute(resolutionContext.typeContext, this.a)
        )
        val substitutedB = nonFixedToVariablesSubstitutor.safeSubstitute(
            resolutionContext.typeContext,
            capTypesSubstitutor.safeSubstitute(resolutionContext.typeContext, this.b)
        )

        return InitialConstraint(
            substitutedA,
            substitutedB,
            constraintKind,
            position
        )
    }
}

class FirStubTypeTransformer(private val substitutor: ConeSubstitutor) : FirDefaultTransformer<Nothing?>() {

    override fun <E : FirElement> transformElement(element: E, data: Nothing?): E {
        // All resolvable nodes should be implemented separately to cover substitution of receivers in the candidate
        if (element is FirResolvable) {
            element.candidate()?.let { processCandidate(it) }
        }

        // Since FirExpressions don't have typeRefs, they need to be updated separately.
        // FirAnonymousFunctionExpression doesn't support replacing the type
        // since it delegates the getter to the underlying FirAnonymousFunction.
        if (element is FirExpression && element !is FirAnonymousFunctionExpression) {
            // TODO Check why some expressions have unresolved type in builder inference session KT-61835
            @OptIn(UnresolvedExpressionTypeAccess::class)
            element.coneTypeOrNull
                ?.let(substitutor::substituteOrNull)
                ?.let { element.replaceConeTypeOrNull(it) }
        }

        @Suppress("UNCHECKED_CAST")
        return element.transformChildren(this, data = null) as E
    }

    override fun transformTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall, data: Nothing?): FirStatement {
        if (typeOperatorCall.argument.resolvedType is ConeStubType) {
            typeOperatorCall.replaceArgFromStubType(true)
        }
        return super.transformTypeOperatorCall(typeOperatorCall, data)
    }

    override fun transformResolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef, data: Nothing?): FirTypeRef =
        substitutor.substituteOrNull(resolvedTypeRef.type)?.let {
            resolvedTypeRef.withReplacedConeType(it)
        } ?: resolvedTypeRef

    /*
     * We should manually update all receivers in the all not completed candidates, because not all calls with candidates
     *   contained in partiallyResolvedCalls and candidate stores not receiver values, which are updated, (TODO: remove this comment after removal of updating values)
     *   and receivers of candidates are not direct FIR children of calls, so they won't be visited during regular transformChildren
     */
    private fun processCandidate(candidate: Candidate) {
        candidate.dispatchReceiver = candidate.dispatchReceiver?.transform(this, data = null)
        candidate.chosenExtensionReceiver = candidate.chosenExtensionReceiver?.transform(this, data = null)
        candidate.contextReceiverArguments = candidate.contextReceiverArguments?.map { it.transform(this, data = null) }
    }
}

private val BUILDER_INFERENCE_ANNOTATION_CLASS_ID: ClassId = ClassId.topLevel(BUILDER_INFERENCE_ANNOTATION_FQ_NAME)

fun FirDeclaration.hasBuilderInferenceAnnotation(session: FirSession): Boolean =
    hasAnnotation(BUILDER_INFERENCE_ANNOTATION_CLASS_ID, session)
