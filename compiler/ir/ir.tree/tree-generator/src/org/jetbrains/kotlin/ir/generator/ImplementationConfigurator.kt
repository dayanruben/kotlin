/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.generator

import org.jetbrains.kotlin.generators.tree.ArbitraryImportable
import org.jetbrains.kotlin.generators.tree.ImplementationKind
import org.jetbrains.kotlin.generators.tree.ClassRef
import org.jetbrains.kotlin.generators.tree.ImportCollector
import org.jetbrains.kotlin.generators.tree.StandardTypes
import org.jetbrains.kotlin.generators.tree.Visibility
import org.jetbrains.kotlin.generators.tree.printer.FunctionParameter
import org.jetbrains.kotlin.generators.tree.printer.VariableKind
import org.jetbrains.kotlin.generators.tree.printer.printFunctionWithBlockBody
import org.jetbrains.kotlin.generators.tree.printer.printPropertyDeclaration
import org.jetbrains.kotlin.ir.generator.config.AbstractIrTreeImplementationConfigurator
import org.jetbrains.kotlin.ir.generator.model.Element
import org.jetbrains.kotlin.ir.generator.model.Implementation
import org.jetbrains.kotlin.ir.generator.model.ListField
import org.jetbrains.kotlin.utils.SmartPrinter

object ImplementationConfigurator : AbstractIrTreeImplementationConfigurator() {
    override fun configure(model: Model): Unit = with(IrTree) {
        impl(anonymousInitializer) {
            isLateinit("body")
        }

        impl(simpleFunction, "IrFunctionImpl") {
            defaultEmptyList("valueParameters")
            defaultNull("dispatchReceiverParameter", "extensionReceiverParameter", "body", "correspondingPropertySymbol")
            default("contextReceiverParametersCount", "0")
            isLateinit("returnType")
        }
        impl(functionWithLateBinding) {
            defaultEmptyList("valueParameters")
            defaultNull("dispatchReceiverParameter", "extensionReceiverParameter", "body", "correspondingPropertySymbol")
            default("contextReceiverParametersCount", "0")
            isLateinit("returnType")
            defaultNull("containerSource", withGetter = true)
            configureDeclarationWithLateBindinig(simpleFunctionSymbolType)
        }

        impl(constructor) {
            defaultEmptyList("valueParameters")
            defaultNull("dispatchReceiverParameter", "extensionReceiverParameter", "body")
            default("contextReceiverParametersCount", "0")
            isLateinit("returnType")
        }

        impl(field) {
            defaultNull("initializer", "correspondingPropertySymbol")
        }

        impl(property) {
            defaultNull("backingField", "getter", "setter")
        }
        impl(propertyWithLateBinding) {
            defaultNull("backingField", "getter", "setter")
            defaultNull("containerSource", withGetter = true)
            configureDeclarationWithLateBindinig(propertySymbolType)
        }

        impl(localDelegatedProperty) {
            isLateinit("delegate", "getter")
            defaultNull("setter")
        }

        impl(typeParameter) {
            defaultEmptyList("superTypes")
        }

        impl(valueParameter) {
            defaultNull("defaultValue")
        }

        impl(variable) {
            implementation.putImplementationOptInInConstructor = false
            implementation.constructorParameterOrderOverride =
                listOf("startOffset", "endOffset", "origin", "symbol", "name", "type", "isVar", "isConst", "isLateinit")
            defaultNull("initializer")
            default("factory") {
                value = "error(\"Create IrVariableImpl directly\")"
                withGetter = true
            }
        }

        impl(`class`) {
            kind = ImplementationKind.OpenClass
            defaultNull("thisReceiver", "valueClassRepresentation")
            defaultEmptyList("superTypes", "sealedSubclasses")
            defaultFalse("isExternal", "isCompanion", "isInner", "isData", "isValue", "isExpect", "isFun", "hasEnumEntries")
        }

        impl(enumEntry) {
            defaultNull("correspondingClass", "initializerExpression")
        }

        impl(script) {
            implementation.putImplementationOptInInConstructor = false
            implementation.constructorParameterOrderOverride = listOf("symbol", "name", "factory", "startOffset", "endOffset")
            defaultNull(
                "thisReceiver", "baseClass", "resultProperty", "earlierScriptsParameter",
                "importedScripts", "earlierScripts", "targetClass", "constructor"
            )
            isLateinit("explicitCallParameters", "implicitReceiversParameters", "providedProperties", "providedPropertiesParameters")
            default("origin", "SCRIPT_ORIGIN")
        }

        impl(moduleFragment) {
            implementation.putImplementationOptInInConstructor = false
            additionalImports(ArbitraryImportable(Packages.tree, "UNDEFINED_OFFSET"))
            default("startOffset", "UNDEFINED_OFFSET", withGetter = true)
            default("endOffset", "UNDEFINED_OFFSET", withGetter = true)
            default("name", "descriptor.name", withGetter = true)
        }

        impl(errorDeclaration) {
            implementation.doPrint = false
        }

        impl(externalPackageFragment) {
            implementation.doPrint = false
        }

        impl(file) {
            implementation.doPrint = false
        }
    }

    private fun ImplementationContext.configureDeclarationWithLateBindinig(symbolType: ClassRef<*>) {
        default("isBound") {
            value = "_symbol != null"
            withGetter = true
        }
        default("symbol") {
            value = "_symbol ?: error(\"\$this has not acquired a symbol yet\")"
            withGetter = true
        }
        additionalImports(ArbitraryImportable("org.jetbrains.kotlin.ir.descriptors", "toIrBasedDescriptor"))
        default("descriptor") {
            value = "_symbol?.descriptor ?: this.toIrBasedDescriptor()"
            withGetter = true
        }
        implementation.generationCallback = {
            println()
            printPropertyDeclaration(
                "_symbol",
                symbolType.copy(nullable = true),
                VariableKind.VAR,
                visibility = Visibility.PRIVATE,
                initializer = "null"
            )
            println()
            println()
            printFunctionWithBlockBody(
                "acquireSymbol",
                listOf(FunctionParameter("symbol", symbolType)),
                implementation.element,
                override = true,
            ) {
                println("assert(_symbol == null) { \"\$this already has symbol _symbol\" }")
                println("_symbol = symbol")
                println("symbol.bind(this)")
                println("return this")
            }
        }
    }

    override fun configureAllImplementations(model: Model) {
        configureFieldInAllImplementations("parent") {
            isLateinit("parent")
            isMutable("parent")
        }

        configureFieldInAllImplementations("attributeOwnerId") {
            default(it, "this")
        }
        configureFieldInAllImplementations("originalBeforeInline") {
            defaultNull(it)
        }

        configureFieldInAllImplementations("metadata") {
            defaultNull(it)
        }

        configureFieldInAllImplementations("annotations") {
            defaultEmptyList(it)
        }

        configureFieldInAllImplementations("overriddenSymbols") {
            defaultEmptyList(it)
        }

        configureFieldInAllImplementations("typeParameters") {
            defaultEmptyList(it)
        }

        configureFieldInAllImplementations("statements") {
            default(it, "ArrayList(2)")
        }

        configureFieldInAllImplementations("descriptor", { impl -> impl.allFields.any { it.name == "symbol" } }) {
            default(it, "symbol.descriptor", withGetter = true)
        }

        configureFieldInAllImplementations(
            fieldName = null,
            fieldPredicate = { it is ListField && it.isChild && it.listType == StandardTypes.mutableList }
        ) {
            default(it, "ArrayList()")
        }

        // Generation of implementation classes of IrExpression are left out for subsequent MR, as a part of KT-65773.
        for (element in model.elements) {
            if (element.category == Element.Category.Expression) {
                for (implementation in element.implementations) {
                    implementation.doPrint = false
                }
            }
        }
    }
}