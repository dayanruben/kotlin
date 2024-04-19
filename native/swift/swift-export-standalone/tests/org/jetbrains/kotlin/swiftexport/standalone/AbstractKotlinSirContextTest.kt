/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.swiftexport.standalone

import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.konan.target.Distribution
import org.jetbrains.kotlin.konan.test.blackbox.AbstractNativeSimpleTest
import org.jetbrains.kotlin.konan.test.blackbox.support.compilation.callCompilerWithoutOutputInterceptor
import org.jetbrains.kotlin.konan.test.blackbox.support.settings.KotlinNativeClassLoader
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.util.KtTestUtil
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.streams.asSequence

enum class InputModuleKind {
    Source, Binary
}

abstract class AbstractSourceBasedSwiftRunnerTest : AbstractSwiftRunnerTestBase(
    renderDocComments = true,
    inputModuleKind = InputModuleKind.Source,
)

abstract class AbstractKlibBasedSwiftRunnerTest : AbstractSwiftRunnerTestBase(
    renderDocComments = false,
    inputModuleKind = InputModuleKind.Binary,
)

abstract class AbstractSwiftRunnerTestBase(
    private val renderDocComments: Boolean,
    private val inputModuleKind: InputModuleKind,
) : AbstractNativeSimpleTest() {

    private val tmpdir = FileUtil.createTempDirectory("SwiftExportIntegrationTests", null, false)

    fun runTest(testPathString: String) {
        val path = Path(testPathString)
        val expectedFiles = path / "golden_result/"
        val moduleRoot = path / "input_root/"
        assert(expectedFiles.isDirectory() && moduleRoot.isDirectory()) { "setup for $path is incorrect" }

        val expectedSwift = if (!renderDocComments && (expectedFiles / "result.no_comments.swift").exists()) {
            expectedFiles / "result.no_comments.swift"
        } else {
            expectedFiles / "result.swift"
        }
        val expectedCHeader = expectedFiles / "result.h"
        val expectedKotlinBridge = expectedFiles / "result.kt"

        val output = SwiftExportOutput(
            swiftApi = tmpdir.resolve("result.swift").toPath(),
            kotlinBridges = tmpdir.resolve("result.kt").toPath(),
            cHeaderBridges = tmpdir.resolve("result.c").toPath()
        )

        val inputModule = when (inputModuleKind) {
            InputModuleKind.Source -> {
                InputModule.Source(
                    path = moduleRoot,
                    name = "main"
                )
            }
            InputModuleKind.Binary -> {
                InputModule.Binary(
                    path = compileToNativeKLib(moduleRoot),
                    name = "main"
                )
            }
        }

        runSwiftExport(
            input = inputModule,
            output = output,
            config = SwiftExportConfig(
                settings = mapOf(
                    SwiftExportConfig.STABLE_DECLARATIONS_ORDER to "true",
                    SwiftExportConfig.RENDER_DOC_COMMENTS to (if (renderDocComments) "true" else "false"),
                    SwiftExportConfig.BRIDGE_MODULE_NAME to SwiftExportConfig.DEFAULT_BRIDGE_MODULE_NAME,
                ),
                logger = createDummyLogger(),
                distribution = Distribution(KonanHome.konanHomePath),
            )
        )

        KotlinTestUtils.assertEqualsToFile(expectedSwift, output.swiftApi.readText())
        KotlinTestUtils.assertEqualsToFile(expectedCHeader, output.cHeaderBridges.readText())
        KotlinTestUtils.assertEqualsToFile(expectedKotlinBridge, output.kotlinBridges.readText())
    }
}

internal fun AbstractNativeSimpleTest.compileToNativeKLib(kLibSourcesRoot: Path): Path {
    val ktFiles = Files.walk(kLibSourcesRoot).asSequence().filter { it.extension == "kt" }.toList()
    val testKlib = KtTestUtil.tmpDir("testLibrary").resolve("library.klib").toPath()

    val arguments = buildList {
        ktFiles.mapTo(this) { it.absolutePathString() }
        addAll(listOf("-produce", "library"))
        addAll(listOf("-output", testKlib.absolutePathString()))
    }

    // Avoid creating excessive number of classloaders
    val classLoader = testRunSettings.get<KotlinNativeClassLoader>().classLoader
    val compileResult = callCompilerWithoutOutputInterceptor(arguments.toTypedArray(), classLoader)

    check(compileResult.exitCode == ExitCode.OK) {
        "Compilation error: $compileResult"
    }

    return testKlib
}

private object KonanHome {
    private const val KONAN_HOME_PROPERTY_KEY = "kotlin.internal.native.test.nativeHome"

    val konanHomePath: String
        get() = System.getProperty(KONAN_HOME_PROPERTY_KEY)
            ?: error("Missing System property: '$KONAN_HOME_PROPERTY_KEY'")
}
