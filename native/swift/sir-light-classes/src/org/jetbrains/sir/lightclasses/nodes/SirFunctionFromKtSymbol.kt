/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.sir.lightclasses.nodes

import org.jetbrains.kotlin.analysis.api.symbols.KtFunctionLikeSymbol
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.sir.*
import org.jetbrains.kotlin.sir.providers.SirSession
import org.jetbrains.kotlin.sir.providers.source.KotlinSource
import org.jetbrains.sir.lightclasses.SirFromKtSymbol
import org.jetbrains.sir.lightclasses.extensions.documentation
import org.jetbrains.sir.lightclasses.extensions.lazyWithSessions
import org.jetbrains.sir.lightclasses.extensions.sirCallableKind
import org.jetbrains.sir.lightclasses.extensions.withSessions

internal class SirFunctionFromKtSymbol(
    override val ktSymbol: KtFunctionLikeSymbol,
    override val ktModule: KtModule,
    override val sirSession: SirSession,
) : SirFunction(), SirFromKtSymbol {

    override val visibility: SirVisibility = SirVisibility.PUBLIC
    override val origin: SirOrigin by lazy {
        KotlinSource(ktSymbol)
    }
    override val kind: SirCallableKind by lazy {
        ktSymbol.sirCallableKind
    }
    override val name: String by lazyWithSessions {
        ktSymbol.sirDeclarationName()
    }
    override val parameters: MutableList<SirParameter> by lazyWithSessions {
        mutableListOf<SirParameter>().apply {
            ktSymbol.valueParameters.mapTo(this) {
                SirParameter(argumentName = it.name.asString(), type = it.returnType.translateType(analysisSession))
            }
        }
    }
    override val returnType: SirType by lazyWithSessions {
        ktSymbol.returnType.translateType(analysisSession)
    }
    override val documentation: String? by lazyWithSessions {
        ktSymbol.documentation()
    }

    override var parent: SirDeclarationParent
        get() = withSessions {
            ktSymbol.getSirParent(analysisSession)
        }
        set(_) = Unit

    override var body: SirFunctionBody? = null
}
