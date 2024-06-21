/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.components

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaIdeApi
import org.jetbrains.kotlin.analysis.api.lifetime.KaLifetimeOwner
import org.jetbrains.kotlin.analysis.api.lifetime.KaLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.scopes.KaScope
import org.jetbrains.kotlin.analysis.api.scopes.KaTypeScope
import org.jetbrains.kotlin.analysis.api.symbols.KaFileSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPackageSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KaDeclarationContainerSymbol
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import java.util.Objects

public interface KaScopeProvider {
    /**
     * Returns a [KaScope] containing *non-static* callable members (functions, properties, and constructors) and all classifier members
     * (classes and objects) of the given [KaDeclarationContainerSymbol]. The scope includes members inherited from the symbol's supertypes, in
     * addition to members which are declared explicitly inside the symbol's body.
     *
     * The member scope doesn't include synthetic Java properties. To get such properties, use [syntheticJavaPropertiesScope].
     *
     * @see staticMemberScope
     */
    public val KaDeclarationContainerSymbol.memberScope: KaScope

    /**
     * Returns a [KaScope] containing all members from [memberScope] and [staticMemberScope].
     */
    public val KaDeclarationContainerSymbol.combinedMemberScope: KaScope
        get() = withValidityAssertion {
            return listOf(memberScope, staticMemberScope).asCompositeScope()
        }

    /**
     * Returns a [KaScope] containing the *static* members of the given [KaDeclarationContainerSymbol].
     *
     * The behavior of the scope differs based on whether the given [KaDeclarationContainerSymbol] is a Kotlin or Java class:
     *
     * - **Kotlin class:** The scope contains static callables (functions and properties) and classifiers (classes and objects) declared
     *   directly in the [KaDeclarationContainerSymbol]. Hence, the static member scope for Kotlin classes is equivalent to [declaredMemberScope].
     * - **Java class:** The scope contains static callables (functions and properties) declared in the [KaDeclarationContainerSymbol] or any of its
     *   superclasses (excluding static callables from super-interfaces), and classes declared directly in the [KaDeclarationContainerSymbol]. This
     *   follows Kotlin's rules about static inheritance in Java classes, where static callables are propagated from superclasses, but
     *   nested classes are not.
     *
     * #### Kotlin Example
     *
     * ```kotlin
     * abstract class A {
     *     class C1
     *     inner class D1
     *     object O1
     *
     *     // There is no way to declare a static callable in an abstract class, as only enum classes define additional static callables.
     * }
     *
     * class B : A() {
     *     class C2
     *     inner class D2
     *     object O2
     *     companion object {
     *         val baz: String = ""
     *     }
     * }
     * ```
     *
     * The static member scope of `B` contains the following symbols:
     *
     * ```
     * class C2
     * inner class D2
     * object O2
     * companion object
     * ```
     *
     * #### Java Example
     *
     * ```java
     * // SuperInterface.java
     * public interface SuperInterface {
     *     public static void fromSuperInterface() { }
     * }
     *
     * // SuperClass.java
     * public abstract class SuperClass implements SuperInterface {
     *     static class NestedSuperClass { }
     *     class InnerSuperClass { }
     *     public static void fromSuperClass() { }
     * }
     *
     * // FILE: JavaClass.java
     * public class JavaClass extends SuperClass {
     *     static class NestedClass { }
     *     class InnerClass { }
     *     public static void fromJavaClass() { }
     * }
     * ```
     *
     * The static member scope of `JavaClass` contains the following symbols:
     *
     * ```
     * public static void fromSuperClass()
     * public static void fromJavaClass()
     * static class NestedClass
     * class InnerClass
     * ```
     *
     * @see memberScope
     */
    public val KaDeclarationContainerSymbol.staticMemberScope: KaScope

    /**
     * Returns a [KaScope] containing the *non-static* callables (functions, properties, and constructors) and inner classes explicitly
     * declared in the given [KaDeclarationContainerSymbol].
     *
     * The declared member scope does not contain classifiers (including the companion object) except for inner classes. To retrieve the
     * classifiers declared in this [KaDeclarationContainerSymbol], please use the *static* declared member scope provided by
     * [staticDeclaredMemberScope].
     *
     * @see staticDeclaredMemberScope
     */
    public val KaDeclarationContainerSymbol.declaredMemberScope: KaScope

    /**
     * Returns a [KaScope] containing the *static* callables (functions and properties) and all classifiers (classes and objects) explicitly
     * declared in the given [KaDeclarationContainerSymbol].
     *
     * It is worth noting that, while Java classes may contain declarations of static callables freely, in Kotlin only enum classes define
     * static callables. Hence, for non-enum Kotlin classes, it is not expected that the static declared member scope will contain any
     * callables.
     *
     * @see declaredMemberScope
     */
    public val KaDeclarationContainerSymbol.staticDeclaredMemberScope: KaScope

    /**
     * Returns a [KaScope] containing *all* members explicitly declared in the given [KaDeclarationContainerSymbol].
     *
     * In contrast to [declaredMemberScope] and [staticDeclaredMemberScope], this scope contains both static and non-static members.
     */
    public val KaDeclarationContainerSymbol.combinedDeclaredMemberScope: KaScope

    public val KaDeclarationContainerSymbol.delegatedMemberScope: KaScope

    public val KaFileSymbol.fileScope: KaScope

    public val KaPackageSymbol.packageScope: KaScope

    public fun List<KaScope>.asCompositeScope(): KaScope

    /**
     * Return a [KaTypeScope] for a given [KaType].
     * The type scope will include all members which are declared and callable on a given type.
     *
     * Comparing to the [KaScope], in the [KaTypeScope] all use-site type parameters are substituted.
     *
     * Consider the following code
     * ```
     * fun foo(list: List<String>) {
     *      list // get KtTypeScope for it
     * }
     *```
     *
     * Inside the `LIST_KT_ELEMENT.getKaType().getTypeScope()` would contain the `get(i: Int): String` method with substituted type `T = String`
     *
     * @return type scope for the given type if given `KaType` is not error type, `null` otherwise.
     * Returned [KaTypeScope] includes synthetic Java properties.
     *
     * @see KaTypeScope
     * @see KaTypeProviderMixIn.getKaType
     */
    @KaExperimentalApi
    public val KaType.scope: KaTypeScope?

    @KaExperimentalApi
    public val KaTypeScope.declarationScope: KaScope

    /**
     * Returns a [KaTypeScope] with synthetic Java properties created for a given [KaType].
     */
    @KaExperimentalApi
    public val KaType.syntheticJavaPropertiesScope: KaTypeScope?

    /**
     * For each scope in [KaScopeContext] an index is calculated. The indexes are relative to position, and they are only known for
     * scopes obtained with [scopeContext].
     *
     * Scopes with [KaScopeKind.TypeScope] include synthetic Java properties.
     */
    public fun KtFile.scopeContext(position: KtElement): KaScopeContext


    @Deprecated("Use 'scopeContext()' instead", replaceWith = ReplaceWith("scopeContext(positionInFakeFile)"))
    public fun KtFile.getScopeContextForPosition(positionInFakeFile: KtElement): KaScopeContext {
        return scopeContext(positionInFakeFile)
    }

    /**
     * Returns a [KaScopeContext] formed by all imports in the [KtFile].
     *
     * By default, this will also include default importing scopes, which can be filtered by [KaScopeKind]
     */
    public val KtFile.importingScopeContext: KaScopeContext

    /**
     * Returns single scope, containing declarations from all scopes that satisfy [filter]. The order of declarations corresponds to the
     * order of their containing scopes, which are sorted according to their indexes in scope tower.
     */
    public fun KaScopeContext.compositeScope(filter: (KaScopeKind) -> Boolean = { true }): KaScope = withValidityAssertion {
        val subScopes = scopes.filter { filter(it.kind) }.map { it.scope }
        subScopes.asCompositeScope()
    }

    @Deprecated("Use 'compositeScope()' instead.", replaceWith = ReplaceWith("compositeScope(filter)"))
    public fun KaScopeContext.getCompositeScope(filter: (KaScopeKind) -> Boolean = { true }): KaScope {
        return compositeScope(filter)
    }
}

public interface KaScopeContext : KaLifetimeOwner {
    public val implicitReceivers: List<KaImplicitReceiver>

    /**
     * Scopes for position, sorted according to their indexes in scope tower, i.e. the first scope is the closest one to position.
     */
    public val scopes: List<KaScopeWithKind>
}

@Deprecated("Use 'KaScopeContext' instead.", replaceWith = ReplaceWith("KaScopeContext"))
public typealias KtScopeContext = KaScopeContext

public interface KaImplicitReceiver : KaLifetimeOwner {
    public val ownerSymbol: KaSymbol
    public val type: KaType
    public val scopeIndexInTower: Int
}

@Deprecated("Use 'KaImplicitReceiver' instead.", replaceWith = ReplaceWith("KaImplicitReceiver"))
public typealias KtImplicitReceiver = KaImplicitReceiver

public sealed interface KaScopeKind {
    /**
     * Index in scope tower. For example:
     * ```
     * fun f(a: A, b: B) {      // local scope:       indexInTower = 2
     *     with(a) {            // type scope for A:  indexInTower = 1
     *         with(b) {        // type scope for B:  indexInTower = 0
     *             <caret>
     *         }
     *     }
     * }
     * ```
     */
    public val indexInTower: Int

    public interface LocalScope : KaScopeKind

    /**
     * Represents [KaScope] for type, which include synthetic Java properties of corresponding type.
     */
    public interface TypeScope : KaScopeKind

    public sealed interface NonLocalScope : KaScopeKind

    /**
     * Represents [KaScope] containing type parameters.
     */
    public interface TypeParameterScope : NonLocalScope

    /**
     * Represents [KaScope] containing declarations from package.
     */
    public interface PackageMemberScope : NonLocalScope

    /**
     * Represents [KaScope] containing declarations from imports.
     */
    public sealed interface ImportingScope : NonLocalScope

    /**
     * Represents [KaScope] containing declarations from explicit non-star imports.
     */
    public interface ExplicitSimpleImportingScope : ImportingScope

    /**
     * Represents [KaScope] containing declarations from explicit star imports.
     */
    public interface ExplicitStarImportingScope : ImportingScope

    /**
     * Represents [KaScope] containing declarations from non-star imports which are not declared explicitly and are added by default.
     */
    public interface DefaultSimpleImportingScope : ImportingScope

    /**
     * Represents [KaScope] containing declarations from star imports which are not declared explicitly and are added by default.
     */
    public interface DefaultStarImportingScope : ImportingScope

    /**
     * Represents [KaScope] containing static members of a classifier.
     */
    public interface StaticMemberScope : NonLocalScope

    /**
     * Represents [KaScope] containing members of a script.
     */
    public interface ScriptMemberScope : NonLocalScope
}

@KaIdeApi
public object KaScopeKinds {
    public class LocalScope(override val indexInTower: Int) : KaScopeKind.LocalScope

    public class TypeScope(override val indexInTower: Int) : KaScopeKind.TypeScope

    public class TypeParameterScope(override val indexInTower: Int) : KaScopeKind.TypeParameterScope

    public class PackageMemberScope(override val indexInTower: Int) : KaScopeKind.PackageMemberScope

    public class ExplicitSimpleImportingScope(override val indexInTower: Int) : KaScopeKind.ExplicitSimpleImportingScope

    public class ExplicitStarImportingScope(override val indexInTower: Int) : KaScopeKind.ExplicitStarImportingScope

    public class DefaultSimpleImportingScope(override val indexInTower: Int) : KaScopeKind.DefaultSimpleImportingScope

    public class DefaultStarImportingScope(override val indexInTower: Int) : KaScopeKind.DefaultStarImportingScope

    public class StaticMemberScope(override val indexInTower: Int) : KaScopeKind.StaticMemberScope

    public class ScriptMemberScope(override val indexInTower: Int) : KaScopeKind.ScriptMemberScope
}

@Deprecated("Use KaScopeKind' instead.", replaceWith = ReplaceWith("KaScopeKind"))
public typealias KtScopeKind = KaScopeKind

public interface KaScopeWithKind : KaLifetimeOwner {
    public val scope: KaScope
    public val kind: KaScopeKind
}

@KaIdeApi
public class KaScopeWithKindImpl(
    private val backingScope: KaScope,
    private val backingKind: KaScopeKind,
) : KaScopeWithKind {
    override val token: KaLifetimeToken get() = backingScope.token

    override val scope: KaScope get() = withValidityAssertion { backingScope }
    override val kind: KaScopeKind get() = withValidityAssertion { backingKind }

    override fun equals(other: Any?): Boolean {
        return this === other ||
                other is KaScopeWithKindImpl &&
                other.backingScope == backingScope &&
                other.backingKind == backingKind
    }

    override fun hashCode(): Int = Objects.hash(backingScope, backingKind)
}

@Deprecated("Use 'KaScopeWithKind' instead.", replaceWith = ReplaceWith("KaScopeWithKind"))
public typealias KtScopeWithKind = KaScopeWithKind
