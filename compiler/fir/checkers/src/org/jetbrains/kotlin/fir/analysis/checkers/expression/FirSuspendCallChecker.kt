/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.isExplicit
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.analysis.getChild
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.FirResolvedCallableReference
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.references.resolved
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import org.jetbrains.kotlin.util.getChildren

object FirSuspendCallChecker : FirQualifiedAccessExpressionChecker(MppCheckerKind.Common) {
    private val BUILTIN_SUSPEND_NAME = StandardClassIds.Callables.suspend.callableName

    internal val KOTLIN_SUSPEND_BUILT_IN_FUNCTION_CALLABLE_ID = CallableId(StandardClassIds.BASE_KOTLIN_PACKAGE, BUILTIN_SUSPEND_NAME)

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirQualifiedAccessExpression) {
        val reference = expression.calleeReference.resolved ?: return
        val symbol = reference.resolvedSymbol as? FirCallableSymbol ?: return
        if (reference.name == BUILTIN_SUSPEND_NAME ||
            symbol is FirNamedFunctionSymbol && symbol.name == BUILTIN_SUSPEND_NAME
        ) {
            checkSuspendModifierForm(expression, reference, symbol)
        }

        if (reference is FirResolvedCallableReference) {
            checkCallableReference(expression, symbol)
            return
        }

        when (symbol) {
            is FirNamedFunctionSymbol -> if (!symbol.isSuspend) return
            is FirPropertySymbol -> if (symbol.callableId != StandardClassIds.Callables.coroutineContext) return
            else -> return
        }
        val enclosingSuspendFunction = findEnclosingSuspendFunction()
        if (enclosingSuspendFunction == null) {
            when (symbol) {
                is FirNamedFunctionSymbol -> reporter.reportOn(expression.source, FirErrors.ILLEGAL_SUSPEND_FUNCTION_CALL, symbol)
                is FirPropertySymbol -> reporter.reportOn(expression.source, FirErrors.ILLEGAL_SUSPEND_PROPERTY_ACCESS, symbol)
                else -> {
                }
            }
        } else {
            if (!checkNonLocalReturnUsage(enclosingSuspendFunction)) {
                reporter.reportOn(expression.source, FirErrors.NON_LOCAL_SUSPENSION_POINT)
            }
            if (isInScopeForDefaultParameterValues(enclosingSuspendFunction)) {
                reporter.reportOn(
                    expression.source,
                    FirErrors.UNSUPPORTED,
                    "Suspend function call in default parameter value is unsupported."
                )
            }
            if (!checkRestrictsSuspension(expression, enclosingSuspendFunction, symbol)) {
                reporter.reportOn(expression.source, FirErrors.ILLEGAL_RESTRICTED_SUSPENDING_FUNCTION_CALL)
            }
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkSuspendModifierForm(
        expression: FirQualifiedAccessExpression,
        reference: FirResolvedNamedReference,
        symbol: FirCallableSymbol<*>,
    ) {
        if (symbol.callableId == KOTLIN_SUSPEND_BUILT_IN_FUNCTION_CALLABLE_ID) {
            if (reference.name != BUILTIN_SUSPEND_NAME ||
                expression.explicitReceiver != null ||
                expression.formOfSuspendModifierForLambdaOrFun() == null
            ) {
                reporter.reportOn(expression.source, FirErrors.NON_MODIFIER_FORM_FOR_BUILT_IN_SUSPEND)
            }
        } else if (reference.name == BUILTIN_SUSPEND_NAME) {
            when (expression.formOfSuspendModifierForLambdaOrFun()) {
                SuspendCallArgumentKind.FUN -> {
                    reporter.reportOn(expression.source, FirErrors.MODIFIER_FORM_FOR_NON_BUILT_IN_SUSPEND_FUN)
                }
                SuspendCallArgumentKind.LAMBDA -> {
                    reporter.reportOn(expression.source, FirErrors.MODIFIER_FORM_FOR_NON_BUILT_IN_SUSPEND)
                }
                null -> {
                    // Nothing to do
                }
            }
        }
    }

    private fun FirQualifiedAccessExpression.formOfSuspendModifierForLambdaOrFun(): SuspendCallArgumentKind? {
        if (this !is FirFunctionCall) return null
        val reference = this.calleeReference
        if (reference is FirResolvedCallableReference) return null
        if (typeArguments.any { it.isExplicit }) return null
        if (arguments.singleOrNull().let { it is FirAnonymousFunctionExpression && it.isTrailingLambda }) {
            // No brackets should be in a selector call
            val callExpressionSource =
                if (explicitReceiver == null) source
                else source?.getChild(KtNodeTypes.CALL_EXPRESSION, index = 1, depth = 1)
            if (callExpressionSource?.getChild(KtNodeTypes.VALUE_ARGUMENT_LIST, depth = 1) == null) {
                return SuspendCallArgumentKind.LAMBDA
            }
        }
        if (origin == FirFunctionCallOrigin.Infix) {
            val lastArgument = arguments.lastOrNull()
            if (lastArgument is FirAnonymousFunctionExpression && source?.getChild(KtNodeTypes.PARENTHESIZED, depth = 1) == null) {
                return if (lastArgument.source?.lighterASTNode?.tokenType == KtStubElementTypes.FUNCTION) {
                    SuspendCallArgumentKind.FUN
                } else {
                    SuspendCallArgumentKind.LAMBDA
                }
            }
        }
        return null
    }

    context(context: CheckerContext)
    private fun findEnclosingSuspendFunction(): FirFunctionSymbol<*>? {
        return context.containingDeclarations.lastOrNull {
            when (it) {
                is FirAnonymousFunctionSymbol ->
                    if (it.isLambda) it.resolvedTypeRef.coneType.isSuspendOrKSuspendFunctionType(context.session) else it.isSuspend
                is FirNamedFunctionSymbol ->
                    it.isSuspend
                else ->
                    false
            }
        } as? FirFunctionSymbol
    }

    context(context: CheckerContext)
    private fun isInScopeForDefaultParameterValues(
        enclosingSuspendFunction: FirFunctionSymbol<*>
    ): Boolean {
        val valueParameters = enclosingSuspendFunction.valueParameterSymbols
        for (declaration in context.containingDeclarations.asReversed()) {
            when {
                declaration is FirValueParameterSymbol && declaration in valueParameters && declaration.hasDefaultValue -> return true
                declaration is FirAnonymousFunctionSymbol && declaration.inlineStatus == InlineStatus.Inline -> continue
                declaration is FirFunctionSymbol && !declaration.isInline -> return false
            }
        }
        return false
    }

    context(context: CheckerContext)
    private fun checkNonLocalReturnUsage(enclosingSuspendFunction: FirFunctionSymbol<*>): Boolean {
        for (declaration in context.containingDeclarations.asReversed()) {
            // If we found the nearest suspend function, we're finished.
            if (declaration == enclosingSuspendFunction) return true
            // Local variables are okay.
            if (declaration is FirPropertySymbol && declaration.isLocal) continue
            // Inline lambdas are okay.
            if (declaration is FirAnonymousFunctionSymbol && declaration.inlineStatus.returnAllowed) continue
            // We already report UNSUPPORTED on suspend calls in value parameters default values, so they are okay for our purposes.
            if (declaration is FirValueParameterSymbol) continue
            // Everything else (local classes, init blocks, non-inline lambdas, etc.F) is not okay.
            return false
        }

        return false
    }

    context(context: CheckerContext)
    private fun checkRestrictsSuspension(
        expression: FirQualifiedAccessExpression,
        enclosingSuspendFunction: FirFunctionSymbol<*>,
        calledDeclarationSymbol: FirCallableSymbol<*>,
    ): Boolean {
        if (expression is FirFunctionCall && isCaseMissedByK1(expression)) {
            return true
        }

        val session = context.session

        val enclosingSuspendFunctionDispatchReceiverOwnerSymbol =
            enclosingSuspendFunction.dispatchReceiverType?.classLikeLookupTagIfAny?.toRegularClassSymbol(session)
        val enclosingSuspendFunctionExtensionReceiverSymbol = enclosingSuspendFunction.receiverParameterSymbol

        val (dispatchReceiverExpression, extensionReceiverExpression, extensionReceiverParameterType) =
            expression.computeReceiversInfo(session, calledDeclarationSymbol)

        for (receiverExpression in listOfNotNull(dispatchReceiverExpression, extensionReceiverExpression)) {
            if (!receiverExpression.resolvedType.isRestrictSuspensionReceiver(session)) continue
            if (sameInstanceOfReceiver(receiverExpression, enclosingSuspendFunctionDispatchReceiverOwnerSymbol)) continue
            if (sameInstanceOfReceiver(receiverExpression, enclosingSuspendFunctionExtensionReceiverSymbol)) continue

            return false
        }

        if (enclosingSuspendFunctionExtensionReceiverSymbol?.resolvedType?.isRestrictSuspensionReceiver(session) != true) {
            return true
        }

        if (sameInstanceOfReceiver(dispatchReceiverExpression, enclosingSuspendFunctionExtensionReceiverSymbol)) {
            return true
        }

        if (sameInstanceOfReceiver(extensionReceiverExpression, enclosingSuspendFunctionExtensionReceiverSymbol)) {
            if (extensionReceiverParameterType?.isRestrictSuspensionReceiver(session) == true) {
                return true
            }
        }
        return false
    }

    /**
     * This function exists because of KT-65272:
     *
     * ```
     * @RestrictsSuspension
     * object TestScope
     *
     * val testLambda: suspend TestScope.() -> Unit
     *     get() = TODO()
     *
     * suspend fun test() {
     *     TestScope.testLambda()        // ❌️K1 ❌️K2
     *     testLambda(TestScope)         // ✅️K1 ❌️K2  <-- Working K1 code now fails to compile
     *     testLambda.invoke(TestScope)  // ✅️K1 ✅️K2
     * }
     * ```
     *
     * It was decided to replicate K1 behavior for now, so function
     * returns `true` when given an expression like `testLambda(TestScope)`:
     * an implicit invoke call on a receiver of an extension function type
     * such that the receiver argument is passed as a value argument.
     */
    private fun isCaseMissedByK1(expression: FirFunctionCall): Boolean {
        val isInvokeFromExtensionFunctionType = expression is FirImplicitInvokeCall
                && expression.explicitReceiver?.resolvedType?.isExtensionFunctionType == true

        if (!isInvokeFromExtensionFunctionType) {
            return false
        }

        val source = expression.source
            ?: return false

        val visualValueArgumentsCount = source
            .getChild(KtNodeTypes.VALUE_ARGUMENT_LIST, depth = 1)
            ?.lighterASTNode?.getChildren(source.treeStructure)
            ?.filter { it.tokenType == KtNodeTypes.VALUE_ARGUMENT }
            ?.size
            ?: return false

        return visualValueArgumentsCount != expression.arguments.count() - 1
    }

    private fun ConeKotlinType.isRestrictSuspensionReceiver(session: FirSession): Boolean {
        when (this) {
            is ConeClassLikeType -> {
                val regularClassSymbol = fullyExpandedType(session).lookupTag.toRegularClassSymbol(session) ?: return false
                if (regularClassSymbol.hasAnnotationWithClassId(StandardClassIds.Annotations.RestrictsSuspension, session)) {
                    return true
                }
                return regularClassSymbol.resolvedSuperTypes.any { it.isRestrictSuspensionReceiver(session) }
            }
            is ConeTypeParameterType -> {
                return lookupTag.typeParameterSymbol.resolvedBounds.any { it.coneType.isRestrictSuspensionReceiver(session) }
            }
            else -> return false
        }
    }

    private fun sameInstanceOfReceiver(
        useSiteReceiverExpression: FirExpression?,
        declarationSiteReceiverOwnerSymbol: FirBasedSymbol<*>?
    ): Boolean {
        if (declarationSiteReceiverOwnerSymbol == null || useSiteReceiverExpression == null) return false
        if (useSiteReceiverExpression is FirThisReceiverExpression) {
            return useSiteReceiverExpression.calleeReference.boundSymbol == declarationSiteReceiverOwnerSymbol
        }
        return false
    }

    // Triple<DispatchReceiverValue, ExtensionReceiverValue, ExtensionReceiverParameterType>
    private fun FirQualifiedAccessExpression.computeReceiversInfo(
        session: FirSession,
        calledDeclarationSymbol: FirCallableSymbol<*>
    ): Triple<FirExpression?, FirExpression?, ConeKotlinType?> {
        val dispatchReceiver = dispatchReceiver
        if (this is FirImplicitInvokeCall &&
            dispatchReceiver != null && dispatchReceiver.resolvedType.isSuspendOrKSuspendFunctionType(session)
        ) {
            val variableForInvoke = dispatchReceiver
            val variableForInvokeType = variableForInvoke.resolvedType
            if (!variableForInvokeType.isExtensionFunctionType) return Triple(null, null, null)

            // `a.foo()` is resolved to invokeExtension, so it's been desugared to `foo.invoke(a)`
            // And we use the first argument (`a`) as an extension receiver
            return Triple(
                null,
                argumentList.arguments.getOrNull(0),
                variableForInvokeType.typeArguments.getOrNull(0) as? ConeKotlinType
            )
        }

        return Triple(
            dispatchReceiver,
            extensionReceiver,
            calledDeclarationSymbol.resolvedReceiverType,
        )
    }

    context(reporter: DiagnosticReporter, context: CheckerContext)
    private fun checkCallableReference(
        expression: FirQualifiedAccessExpression,
        symbol: FirCallableSymbol<*>,
    ) {
        if (symbol.callableId == StandardClassIds.Callables.coroutineContext) {
            reporter.reportOn(
                expression.calleeReference.source,
                FirErrors.UNSUPPORTED,
                "Callable reference to suspend property is unsupported."
            )
        }
    }
}

private enum class SuspendCallArgumentKind {
    FUN, LAMBDA
}
