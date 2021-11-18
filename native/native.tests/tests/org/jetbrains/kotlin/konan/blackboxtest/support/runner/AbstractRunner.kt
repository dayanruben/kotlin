/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.runner

import org.jetbrains.kotlin.konan.blackboxtest.support.LoggedData
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertEquals
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertTrue
import java.lang.AssertionError

internal abstract class AbstractRunner<R> {
    protected abstract fun buildRun(): AbstractRun
    protected abstract fun buildResultHandler(runResult: RunResult): ResultHandler
    protected abstract fun handleUnexpectedFailure(t: Throwable): Nothing

    fun run(): R = try {
        val run = buildRun()
        val runResult = run.run()
        val resultHandler = buildResultHandler(runResult)
        resultHandler.handle()
    } catch (t: Throwable) {
        if (t is AssertionError)
            throw t
        else {
            // An unexpected failure.
            handleUnexpectedFailure(t)
        }
    }

    fun interface AbstractRun {
        fun run(): RunResult
    }

    data class RunResult(val exitCode: Int, val durationMillis: Long, val stdOut: String, val stdErr: String)

    abstract inner class ResultHandler(protected val runResult: RunResult) {
        abstract fun getLoggedRun(): LoggedData
        abstract fun handle(): R

        val exitCode get() = runResult.exitCode
        val durationMillis get() = runResult.durationMillis
        val stdOut get() = runResult.stdOut
        val stdErr get() = runResult.stdErr

        protected inline fun <T> verifyExpectation(expected: T, actual: T, crossinline errorMessageHeader: () -> String) {
            assertEquals(expected, actual) { formatErrorMessage(errorMessageHeader) }
        }

        protected inline fun verifyExpectation(shouldBeTrue: Boolean, crossinline errorMessageHeader: () -> String) {
            assertTrue(shouldBeTrue) { formatErrorMessage(errorMessageHeader) }
        }

        private inline fun formatErrorMessage(errorMessageHeader: () -> String) = buildString {
            appendLine(errorMessageHeader())
            appendLine()
            appendLine(getLoggedRun())
        }
    }
}
