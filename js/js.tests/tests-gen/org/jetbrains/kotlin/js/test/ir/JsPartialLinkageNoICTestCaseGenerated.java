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
@TestMetadata("compiler/testData/klib/partial-linkage")
@TestDataPath("$PROJECT_ROOT")
@Tag("legacy-frontend")
public class JsPartialLinkageNoICTestCaseGenerated extends AbstractJsPartialLinkageNoICTestCase {
  @Test
  @TestMetadata("addEnumEntry")
  public void testAddEnumEntry() {
    runTest("compiler/testData/klib/partial-linkage/addEnumEntry/");
  }

  @Test
  @TestMetadata("addSealedSubclass")
  public void testAddSealedSubclass() {
    runTest("compiler/testData/klib/partial-linkage/addSealedSubclass/");
  }

  @Test
  public void testAllFilesPresentInPartial_linkage() {
    KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/testData/klib/partial-linkage"), Pattern.compile("^([^_](.+))$"), null, TargetBackend.JS_IR, false);
  }

  @Test
  @TestMetadata("changeClassVisibility")
  public void testChangeClassVisibility() {
    runTest("compiler/testData/klib/partial-linkage/changeClassVisibility/");
  }

  @Test
  @TestMetadata("changeFunctionVisibility")
  public void testChangeFunctionVisibility() {
    runTest("compiler/testData/klib/partial-linkage/changeFunctionVisibility/");
  }

  @Test
  @TestMetadata("changePropertyVisibility")
  public void testChangePropertyVisibility() {
    runTest("compiler/testData/klib/partial-linkage/changePropertyVisibility/");
  }

  @Test
  @TestMetadata("classTransformations")
  public void testClassTransformations() {
    runTest("compiler/testData/klib/partial-linkage/classTransformations/");
  }

  @Test
  @TestMetadata("externalDeclarations")
  public void testExternalDeclarations() {
    runTest("compiler/testData/klib/partial-linkage/externalDeclarations/");
  }

  @Test
  @TestMetadata("externalDeclarationsKJS")
  public void testExternalDeclarationsKJS() {
    runTest("compiler/testData/klib/partial-linkage/externalDeclarationsKJS/");
  }

  @Test
  @TestMetadata("functionTransformations")
  public void testFunctionTransformations() {
    runTest("compiler/testData/klib/partial-linkage/functionTransformations/");
  }

  @Test
  @TestMetadata("inheritanceIssues")
  public void testInheritanceIssues() {
    runTest("compiler/testData/klib/partial-linkage/inheritanceIssues/");
  }

  @Test
  @TestMetadata("noNonImplementedCallableFalsePositives")
  public void testNoNonImplementedCallableFalsePositives() {
    runTest("compiler/testData/klib/partial-linkage/noNonImplementedCallableFalsePositives/");
  }

  @Test
  @TestMetadata("nonAbstractCallableBecomesAbstract")
  public void testNonAbstractCallableBecomesAbstract() {
    runTest("compiler/testData/klib/partial-linkage/nonAbstractCallableBecomesAbstract/");
  }

  @Test
  @TestMetadata("nonExhaustivenessOfWhenClause")
  public void testNonExhaustivenessOfWhenClause() {
    runTest("compiler/testData/klib/partial-linkage/nonExhaustivenessOfWhenClause/");
  }

  @Test
  @TestMetadata("propertyTransformations")
  public void testPropertyTransformations() {
    runTest("compiler/testData/klib/partial-linkage/propertyTransformations/");
  }

  @Test
  @TestMetadata("referencingUnusableDeclarations")
  public void testReferencingUnusableDeclarations() {
    runTest("compiler/testData/klib/partial-linkage/referencingUnusableDeclarations/");
  }

  @Test
  @TestMetadata("removeAbstractCallableFromAbstractClassOrInterface")
  public void testRemoveAbstractCallableFromAbstractClassOrInterface() {
    runTest("compiler/testData/klib/partial-linkage/removeAbstractCallableFromAbstractClassOrInterface/");
  }

  @Test
  @TestMetadata("removeCallable")
  public void testRemoveCallable() {
    runTest("compiler/testData/klib/partial-linkage/removeCallable/");
  }

  @Test
  @TestMetadata("removeClass")
  public void testRemoveClass() {
    runTest("compiler/testData/klib/partial-linkage/removeClass/");
  }

  @Test
  @TestMetadata("removeEnumEntry")
  public void testRemoveEnumEntry() {
    runTest("compiler/testData/klib/partial-linkage/removeEnumEntry/");
  }

  @Test
  @TestMetadata("removeSealedSubclass")
  public void testRemoveSealedSubclass() {
    runTest("compiler/testData/klib/partial-linkage/removeSealedSubclass/");
  }

  @Test
  @TestMetadata("replaceCallableReturnType")
  public void testReplaceCallableReturnType() {
    runTest("compiler/testData/klib/partial-linkage/replaceCallableReturnType/");
  }

  @Test
  @TestMetadata("severalInheritedImplementations")
  public void testSeveralInheritedImplementations() {
    runTest("compiler/testData/klib/partial-linkage/severalInheritedImplementations/");
  }

  @Test
  @TestMetadata("typeAliasChanges")
  public void testTypeAliasChanges() {
    runTest("compiler/testData/klib/partial-linkage/typeAliasChanges/");
  }
}
