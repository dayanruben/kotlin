/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kmp.lexer

import fleet.com.intellij.platform.syntax.util.lexer.FlexAdapter

class KotlinLexer : FlexAdapter(KotlinFlexLexer())