/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.wasm.semantics;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("compiler/testData/codegen/boxWasmJsInterop")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class IrCodegenWasmJsInteropWasmTestGenerated extends AbstractIrCodegenWasmJsInteropWasmTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest0(this::doTest, TargetBackend.WASM, testDataFilePath);
    }

    public void testAllFilesPresentInBoxWasmJsInterop() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/codegen/boxWasmJsInterop"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.WASM, true);
    }

    @TestMetadata("externalTypeOperators.kt")
    public void testExternalTypeOperators() throws Exception {
        runTest("compiler/testData/codegen/boxWasmJsInterop/externalTypeOperators.kt");
    }

    @TestMetadata("externals.kt")
    public void testExternals() throws Exception {
        runTest("compiler/testData/codegen/boxWasmJsInterop/externals.kt");
    }

    @TestMetadata("jsExport.kt")
    public void testJsExport() throws Exception {
        runTest("compiler/testData/codegen/boxWasmJsInterop/jsExport.kt");
    }

    @TestMetadata("nullableExternRefs.kt")
    public void testNullableExternRefs() throws Exception {
        runTest("compiler/testData/codegen/boxWasmJsInterop/nullableExternRefs.kt");
    }

    @TestMetadata("types.kt")
    public void testTypes() throws Exception {
        runTest("compiler/testData/codegen/boxWasmJsInterop/types.kt");
    }
}
