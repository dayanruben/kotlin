/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.ir;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.junit.jupiter.api.Tag;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.GenerateJsTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("compiler/testData/klib/versionCompatibility")
@TestDataPath("$PROJECT_ROOT")
@Tag("legacy-frontend")
public class JsKlibCompatibilityNoICTestCaseGenerated extends AbstractJsKlibCompatibilityNoICTestCase {
  @Test
  @TestMetadata("addEnumEntry")
  public void testAddEnumEntry() {
    runTest("compiler/testData/klib/versionCompatibility/addEnumEntry/");
  }

  @Test
  public void testAllFilesPresentInVersionCompatibility() {
    KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/klib/versionCompatibility"), Pattern.compile("^([^_](.+))$"), null, TargetBackend.JS_IR, false);
  }

  @Test
  @TestMetadata("kt51302")
  public void testKt51302() {
    runTest("compiler/testData/klib/versionCompatibility/kt51302/");
  }

  @Test
  @TestMetadata("varargUsage")
  public void testVarargUsage() {
    runTest("compiler/testData/klib/versionCompatibility/varargUsage/");
  }
}
