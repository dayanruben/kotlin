/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.sir.providers.source

import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.sir.SirDeclaration
import org.jetbrains.kotlin.sir.SirOrigin
import org.jetbrains.kotlin.sir.SirParameter

public open class KotlinSource(
    public val symbol: KaSymbol,
) : SirOrigin.Foreign.SourceCode {
    override fun toString(): String {
        val fqName = when (symbol) {
            is KaClassLikeSymbol -> symbol.classId?.asSingleFqName()
            is KaCallableSymbol -> symbol.callableId?.asSingleFqName()
            else -> null
        }
        return "KotlinSource(symbol=${symbol::class.simpleName ?: "?"}(fqName=${fqName ?: symbol.name ?: "?"}))"
    }
}

public class KotlinPropertyAccessorOrigin(
    symbol: KaPropertyAccessorSymbol,
    public val propertySymbol: KaPropertySymbol,
) : KotlinSource(symbol)

public class KotlinRuntimeElement : SirOrigin.Foreign.SourceCode

public class KotlinMarkerProtocol(symbol: KaNamedClassSymbol) : KotlinSource(symbol)

public sealed class KotlinParameterOrigin : SirParameter.Origin {
    public class ValueParameter(public val parameter: KaValueParameterSymbol) : KotlinParameterOrigin()
    public class ReceiverParameter(public val parameter: KaReceiverParameterSymbol) : KotlinParameterOrigin()
}

/**
 * Convenience accessor which provides direct access to [KaSymbol].
 */
public inline fun <reified T : KaSymbol> SirDeclaration.kaSymbolOrNull(): T? {
    val kotlinOrigin = origin as? KotlinSource
        ?: return null
    return kotlinOrigin.symbol as? T
}