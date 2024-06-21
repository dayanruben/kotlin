/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.resolver

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.test.framework.projectStructure.KtTestModule
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.psi.KtDeclarationModifierList
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFileAnnotationList
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfTypeInPreorder
import org.jetbrains.kotlin.test.services.TestServices

abstract class AbstractResolveByElementTest : AbstractResolveTest<KtElement>() {
    final override fun generateResolveOutput(
        context: ResolveTestCaseContext<KtElement>,
        mainFile: KtFile,
        mainModule: KtTestModule,
        testServices: TestServices,
    ): String = generateResolveOutput(context.element, testServices)

    abstract fun generateResolveOutput(mainElement: KtElement, testServices: TestServices): String

    override fun collectElementsToResolve(
        mainFile: KtFile,
        mainModule: KtTestModule,
        testServices: TestServices,
    ): Collection<ResolveTestCaseContext<KtElement>> {
        val carets = testServices.expressionMarkerProvider.getAllCarets(mainFile)
        if (carets.size > 1) {
            return carets.map {
                val element = testServices.expressionMarkerProvider.getElementOfTypeAtCaret<KtElement>(mainFile, it.tag)
                ResolveKtElementTestCaseContext(element = element, marker = it.fullTag)
            }
        }

        val expression = testServices.expressionMarkerProvider.getElementOfTypeAtCaretOrNull<KtExpression>(mainFile)
            ?: testServices.expressionMarkerProvider.getSelectedElementOfTypeByDirective(
                ktFile = mainFile,
                module = mainModule,
                defaultType = KtElement::class,
            ) as KtElement

        val elementToResolve = expression.elementToResolve
        return listOf(ResolveKtElementTestCaseContext(element = elementToResolve, marker = null))
    }

    class ResolveKtElementTestCaseContext(
        override val element: KtElement,
        override val marker: String?,
    ) : ResolveTestCaseContext<KtElement> {
        override val context: KtElement? get() = element
    }

    protected val KtElement.elementToResolve: KtElement
        get() = when (this) {
            is KtValueArgument -> getArgumentExpression()!!
            is KtDeclarationModifierList -> annotationEntries.singleOrNull() ?: error("Only single annotation entry is supported for now")
            is KtFileAnnotationList -> annotationEntries.singleOrNull() ?: error("Only single annotation entry is supported for now")
            else -> this
        }

    protected fun collectAllKtElements(file: KtFile): Collection<ResolveKtElementTestCaseContext> = buildSet {
        file.forEachDescendantOfTypeInPreorder<PsiElement> { element ->
            if (element is KtElement) {
                add(ResolveKtElementTestCaseContext(element = element, marker = null))
            }
        }
    }
}