/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.testNew;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link GenerateNewCompilerTests.kt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("js/js.translator/testData/webDemoExamples")
@TestDataPath("$PROJECT_ROOT")
public class WebDemoExamplesTestGenerated extends AbstractWebDemoExamplesTest {
    @Test
    public void testAllFilesPresentInWebDemoExamples() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("js/js.translator/testData/webDemoExamples"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JS, true);
    }

    @Test
    @TestMetadata("bottles.kt")
    public void testBottles() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/bottles.kt");
    }

    @Test
    @TestMetadata("builder.kt")
    public void testBuilder() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/builder.kt");
    }

    @Test
    @TestMetadata("forLoop1.kt")
    public void testForLoop1() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/forLoop1.kt");
    }

    @Test
    @TestMetadata("forLoop2.kt")
    public void testForLoop2() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/forLoop2.kt");
    }

    @Test
    @TestMetadata("ifAsExpression.kt")
    public void testIfAsExpression() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/ifAsExpression.kt");
    }

    @Test
    @TestMetadata("isCheck.kt")
    public void testIsCheck() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/isCheck.kt");
    }

    @Test
    @TestMetadata("life.kt")
    public void testLife() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/life.kt");
    }

    @Test
    @TestMetadata("maze.kt")
    public void testMaze() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/maze.kt");
    }

    @Test
    @TestMetadata("multiLanguageHello.kt")
    public void testMultiLanguageHello() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/multiLanguageHello.kt");
    }

    @Test
    @TestMetadata("nullChecks1.kt")
    public void testNullChecks1() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/nullChecks1.kt");
    }

    @Test
    @TestMetadata("nullChecks2.kt")
    public void testNullChecks2() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/nullChecks2.kt");
    }

    @Test
    @TestMetadata("objectOrientedHello.kt")
    public void testObjectOrientedHello() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/objectOrientedHello.kt");
    }

    @Test
    @TestMetadata("patternMatching.kt")
    public void testPatternMatching() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/patternMatching.kt");
    }

    @Test
    @TestMetadata("printArg.kt")
    public void testPrintArg() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/printArg.kt");
    }

    @Test
    @TestMetadata("ranges1.kt")
    public void testRanges1() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/ranges1.kt");
    }

    @Test
    @TestMetadata("ranges2.kt")
    public void testRanges2() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/ranges2.kt");
    }

    @Test
    @TestMetadata("whileLoop.kt")
    public void testWhileLoop() throws Exception {
        runTest("js/js.translator/testData/webDemoExamples/whileLoop.kt");
    }
}
