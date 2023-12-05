/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.transformers

import org.jetbrains.kotlin.analysis.low.level.api.fir.api.targets.LLFirResolveTarget
import org.jetbrains.kotlin.analysis.low.level.api.fir.file.builder.LLFirLockProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.checkExpectForActualIsResolved
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.fir.FirElementWithResolveState
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.languageVersionSettings
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.FirResolveContextCollector
import org.jetbrains.kotlin.fir.resolve.transformers.mpp.FirExpectActualMatcherTransformer
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase

internal object LLFirExpectActualMatcherLazyResolver : LLFirLazyResolver(FirResolvePhase.EXPECT_ACTUAL_MATCHING) {
    override fun resolve(
        target: LLFirResolveTarget,
        lockProvider: LLFirLockProvider,
        session: FirSession,
        scopeSession: ScopeSession,
        towerDataContextCollector: FirResolveContextCollector?,
    ) {
        val resolver = LLFirExpectActualMatchingTargetResolver(target, lockProvider, session, scopeSession)
        resolver.resolveDesignation()
    }

    override fun phaseSpecificCheckIsResolved(target: FirElementWithResolveState) {
        if (target.moduleData.session.languageVersionSettings.supportsFeature(LanguageFeature.MultiPlatformProjects) &&
            target is FirMemberDeclaration &&
            target.canHaveExpectCounterPart()
        ) {
            checkExpectForActualIsResolved(target)
        }
    }
}

private class LLFirExpectActualMatchingTargetResolver(
    target: LLFirResolveTarget,
    lockProvider: LLFirLockProvider,
    session: FirSession,
    scopeSession: ScopeSession,
) : LLFirTargetResolver(target, lockProvider, FirResolvePhase.EXPECT_ACTUAL_MATCHING) {
    private val enabled = session.languageVersionSettings.supportsFeature(LanguageFeature.MultiPlatformProjects)

    @Deprecated("Should never be called directly, only for override purposes, please use withRegularClass", level = DeprecationLevel.ERROR)
    override fun withRegularClassImpl(firClass: FirRegularClass, action: () -> Unit) {
        if (enabled) {
            // Resolve outer classes before resolving inner declarations. It's the requirement of FirExpectActualResolver
            firClass.lazyResolveToPhase(resolverPhase.previous)
            performResolve(firClass)
        }
        action()
    }

    private val transformer = object : FirExpectActualMatcherTransformer(session, scopeSession) {
        override fun transformRegularClass(regularClass: FirRegularClass, data: Nothing?): FirStatement {
            transformMemberDeclaration(regularClass)
            return regularClass
        }
    }

    override fun doLazyResolveUnderLock(target: FirElementWithResolveState) {
        if (enabled && target is FirMemberDeclaration && target.canHaveExpectCounterPart()) {
            transformer.transformMemberDeclaration(target)
        }
    }
}

private fun FirMemberDeclaration.canHaveExpectCounterPart(): Boolean = when (this) {
    is FirEnumEntry -> true
    is FirProperty -> true
    is FirConstructor -> true
    is FirSimpleFunction -> true
    is FirRegularClass -> true
    is FirTypeAlias -> true
    else -> false
}
