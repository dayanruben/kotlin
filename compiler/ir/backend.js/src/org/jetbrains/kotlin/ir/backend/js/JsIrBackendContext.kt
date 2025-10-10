/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js

import org.jetbrains.kotlin.backend.common.compilationException
import org.jetbrains.kotlin.backend.common.ir.KlibSharedVariablesManager
import org.jetbrains.kotlin.backend.common.linkage.partial.createPartialLinkageSupportForLowerings
import org.jetbrains.kotlin.backend.common.linkage.partial.partialLinkageConfig
import org.jetbrains.kotlin.backend.common.lower.InnerClassesSupport
import org.jetbrains.kotlin.backend.common.reportWarning
import org.jetbrains.kotlin.backend.common.serialization.IrInterningService
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.config.phaseConfig
import org.jetbrains.kotlin.config.phaser.PhaseConfig
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.lower.JsInnerClassesSupport
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.JsPolyfills
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.translateJsCodeIntoStatementList
import org.jetbrains.kotlin.ir.backend.js.utils.JsInlineClassesUtils
import org.jetbrains.kotlin.ir.backend.js.utils.Keeper
import org.jetbrains.kotlin.ir.backend.js.utils.MinimizedNameGenerator
import org.jetbrains.kotlin.ir.backend.js.utils.OperatorNames
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFileSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.DescriptorlessExternalPackageFragmentSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrDynamicTypeImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.js.backend.ast.JsExpressionStatement
import org.jetbrains.kotlin.js.backend.ast.JsFunction
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.config.JsGenerationGranularity
import org.jetbrains.kotlin.js.config.RuntimeDiagnostic
import org.jetbrains.kotlin.js.config.compileLongAsBigint
import org.jetbrains.kotlin.js.parser.sourcemaps.*
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import org.jetbrains.kotlin.utils.filterIsInstanceMapNotNull
import java.util.*

@OptIn(ObsoleteDescriptorBasedAPI::class)
class JsIrBackendContext(
    val module: ModuleDescriptor,
    override val irBuiltIns: IrBuiltIns,
    override val symbolTable: SymbolTable,
    val additionalExportedDeclarationNames: Set<FqName>,
    keep: Set<String>,
    override val configuration: CompilerConfiguration, // TODO: remove configuration from backend context
    val mainCallArguments: List<String>?,
    val dceRuntimeDiagnostic: RuntimeDiagnostic? = null,
    val safeExternalBoolean: Boolean = false,
    val safeExternalBooleanDiagnostic: RuntimeDiagnostic? = null,
    val granularity: JsGenerationGranularity = JsGenerationGranularity.WHOLE_PROGRAM,
    val incrementalCacheEnabled: Boolean = false,
) : JsCommonBackendContext {
    val phaseConfig = configuration.phaseConfig ?: PhaseConfig()

    val polyfills = JsPolyfills()
    val globalIrInterner = IrInterningService()

    val localClassNames = WeakHashMap<IrClass, String>()
    val classToItsId = WeakHashMap<IrClass, String>()

    val minimizedNameGenerator: MinimizedNameGenerator =
        MinimizedNameGenerator()

    val keeper: Keeper =
        Keeper(keep)

    val fieldDataCache = WeakHashMap<IrClass, Map<IrField, String>>()

    override val typeSystem: IrTypeSystemContext = IrTypeSystemContextImpl(irBuiltIns)

    override val irFactory: IrFactory = symbolTable.irFactory

    override var inVerbosePhase: Boolean = false

    val devMode = configuration[JSConfigurationKeys.DEVELOPER_MODE] ?: false
    override val es6mode = configuration[JSConfigurationKeys.USE_ES6_CLASSES] ?: false
    val platformArgumentsProviderJsExpression = configuration[JSConfigurationKeys.DEFINE_PLATFORM_MAIN_FUNCTION_ARGUMENTS]

    override val externalPackageFragment = mutableMapOf<IrFileSymbol, IrFile>()

    override val additionalExportedDeclarations = hashSetOf<IrDeclaration>()

    override val bodilessBuiltInsPackageFragment: IrPackageFragment = IrExternalPackageFragmentImpl(
        DescriptorlessExternalPackageFragmentSymbol(),
        FqName("kotlin")
    )

    val packageLevelJsModules = hashSetOf<IrFile>()
    val declarationLevelJsModules = mutableListOf<IrDeclarationWithName>()

    val testFunsPerFile = hashMapOf<IrFile, IrSimpleFunction>()

    override val inlineClassesUtils = JsInlineClassesUtils(this)

    override val innerClassesSupport: InnerClassesSupport = JsInnerClassesSupport(irFactory)

    val dynamicType: IrDynamicType = IrDynamicTypeImpl(emptyList(), Variance.INVARIANT)

    override val reflectionSymbols: ReflectionSymbols get() = symbols.reflectionSymbols

    override val catchAllThrowableType: IrType
        get() = dynamicType

    override val internalPackageFqn = JsStandardClassIds.BASE_JS_PACKAGE

    private val operatorMap = referenceOperators()

    fun getOperatorByName(name: Name, lhsType: IrSimpleType, rhsType: IrSimpleType?) =
        operatorMap[name]?.get(lhsType.classifier)?.let { candidates ->
            if (rhsType == null)
                candidates.singleOrNull()
            else
                candidates.singleOrNull { candidate ->
                    candidate.owner.parameters.first { it.kind == IrParameterKind.Regular }.type.classifierOrNull == rhsType.classifier
                }
        }

    override val jsPromiseSymbol: IrClassSymbol
        get() = symbols.promiseClassSymbol

    override val symbols = JsSymbols(irBuiltIns, irFactory.stageController, configuration.compileLongAsBigint)

    override val propertyLazyInitialization: PropertyLazyInitialization = PropertyLazyInitialization(
        enabled = configuration.get(JSConfigurationKeys.PROPERTY_LAZY_INITIALIZATION, true),
        eagerInitialization = symbols.eagerInitialization
    )

    override val sharedVariablesManager = KlibSharedVariablesManager(symbols)

    override val shouldGenerateHandlerParameterForDefaultBodyFun: Boolean
        get() = true

    // Top-level functions forced to be loaded
    val throwableConstructors by lazy(LazyThreadSafetyMode.NONE) {
        symbols.throwableClass.owner.declarations.filterIsInstance<IrConstructor>().map { it.symbol }
    }
    val defaultThrowableCtor by lazy(LazyThreadSafetyMode.NONE) {
        throwableConstructors.single { !it.owner.isPrimary && it.owner.parameters.isEmpty() }
    }
    val throwableConstructorWithMessageOnly by lazy(LazyThreadSafetyMode.NONE) {
        throwableConstructors.single { it.owner.parameters.size == 1 && it.owner.parameters[0].type.isNullableString() }
    }
    val throwableConstructorWithBothMessageAndCause by lazy(LazyThreadSafetyMode.NONE) {
        throwableConstructors.single { it.owner.parameters.size == 2 }
    }

    private fun referenceOperators(): Map<Name, Map<IrClassSymbol, Collection<IrSimpleFunctionSymbol>>> {
        val primitiveIrSymbols = irBuiltIns.primitiveIrTypes.map { it.classifierOrFail as IrClassSymbol }
        return OperatorNames.ALL.associateWith { name ->
            primitiveIrSymbols.associateWith { classSymbol ->
                classSymbol.owner.declarations
                    .filterIsInstanceMapNotNull<IrSimpleFunction, IrSimpleFunctionSymbol> { function ->
                        function.symbol.takeIf { function.name == name }
                    }
            }
        }
    }

    private fun parseJsFromAnnotation(declaration: IrDeclaration, annotationClassId: ClassId): Pair<IrConstructorCall, JsFunction>? {
        val annotation = declaration.getAnnotation(annotationClassId.asSingleFqName())
            ?: return null
        val jsCode = annotation.arguments[0]
            ?: compilationException("@${annotationClassId.shortClassName} annotation must contain the JS code argument", annotation)
        val statements = translateJsCodeIntoStatementList(jsCode, declaration)
            ?: compilationException("Could not parse JS code", annotation)
        val parsedJsFunction = statements.singleOrNull()
            ?.safeAs<JsExpressionStatement>()
            ?.expression
            ?.safeAs<JsFunction>()
            ?: compilationException("Provided JS code is not a js function", annotation)
        return annotation to parsedJsFunction
    }

    private val outlinedJsCodeFunctions = WeakHashMap<IrFunctionSymbol, JsFunction>()
    fun getJsCodeForFunction(symbol: IrFunctionSymbol): JsFunction? {
        val jsFunction = outlinedJsCodeFunctions[symbol]
        if (jsFunction != null) return jsFunction

        parseJsFromAnnotation(symbol.owner, JsStandardClassIds.Annotations.JsOutlinedFunction)
            ?.let { (annotation, parsedJsFunction) ->
                val sourceMap = (annotation.arguments[1] as? IrConst)?.value as? String
                val parsedSourceMap = sourceMap?.let { parseSourceMap(it, symbol.owner.fileOrNull, annotation) }
                if (parsedSourceMap != null) {
                    val remapper = SourceMapLocationRemapper(parsedSourceMap)
                    remapper.remap(parsedJsFunction)
                }
                outlinedJsCodeFunctions[symbol] = parsedJsFunction
                return parsedJsFunction
            }

        parseJsFromAnnotation(symbol.owner, JsStandardClassIds.Annotations.JsFun)
            ?.let { (_, parsedJsFunction) ->
                outlinedJsCodeFunctions[symbol] = parsedJsFunction
                return parsedJsFunction
            }
        return null
    }

    private fun parseSourceMap(sourceMap: String, file: IrFile?, annotation: IrConstructorCall): SourceMap? {
        if (sourceMap.isEmpty()) return null
        return when (val result = SourceMapParser.parse(sourceMap)) {
            is SourceMapSuccess -> result.value
            is SourceMapError -> {
                reportWarning(
                    """
                    Invalid source map in annotation:
                    ${annotation.dumpKotlinLike()}
                    ${result.message.replaceFirstChar(Char::uppercase)}.
                    Some debug information may be unavailable.
                    If you believe this is not your fault, please create an issue: https://kotl.in/issue
                    """.trimIndent(),
                    file,
                    annotation,
                )
                null
            }
        }
    }

    override val partialLinkageSupport = createPartialLinkageSupportForLowerings(
        configuration.partialLinkageConfig,
        irBuiltIns,
        configuration.messageCollector
    )

    internal var nextAssociatedObjectKey = 0
}
