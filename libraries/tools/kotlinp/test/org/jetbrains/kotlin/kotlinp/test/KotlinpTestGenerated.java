/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kotlinp.test;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("libraries/tools/kotlinp/testData")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class KotlinpTestGenerated extends AbstractKotlinpTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
    }

    public void testAllFilesPresentInTestData() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("libraries/tools/kotlinp/testData"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @TestMetadata("Annotations.kt")
    public void testAnnotations() throws Exception {
        runTest("libraries/tools/kotlinp/testData/Annotations.kt");
    }

    @TestMetadata("Contracts.kt")
    public void testContracts() throws Exception {
        runTest("libraries/tools/kotlinp/testData/Contracts.kt");
    }

    @TestMetadata("FunInterface.kt")
    public void testFunInterface() throws Exception {
        runTest("libraries/tools/kotlinp/testData/FunInterface.kt");
    }

    @TestMetadata("IntersectionTypeInLambdaLiteralAndDelegatedProperty.kt")
    public void testIntersectionTypeInLambdaLiteralAndDelegatedProperty() throws Exception {
        runTest("libraries/tools/kotlinp/testData/IntersectionTypeInLambdaLiteralAndDelegatedProperty.kt");
    }

    @TestMetadata("Lambda.kt")
    public void testLambda() throws Exception {
        runTest("libraries/tools/kotlinp/testData/Lambda.kt");
    }

    @TestMetadata("LocalClass.kt")
    public void testLocalClass() throws Exception {
        runTest("libraries/tools/kotlinp/testData/LocalClass.kt");
    }

    @TestMetadata("LocalDelegatedProperties.kt")
    public void testLocalDelegatedProperties() throws Exception {
        runTest("libraries/tools/kotlinp/testData/LocalDelegatedProperties.kt");
    }

    @TestMetadata("MultiFileClass.kt")
    public void testMultiFileClass() throws Exception {
        runTest("libraries/tools/kotlinp/testData/MultiFileClass.kt");
    }

    @TestMetadata("NestedClasses.kt")
    public void testNestedClasses() throws Exception {
        runTest("libraries/tools/kotlinp/testData/NestedClasses.kt");
    }

    @TestMetadata("OptionalAnnotation.kt")
    public void testOptionalAnnotation() throws Exception {
        runTest("libraries/tools/kotlinp/testData/OptionalAnnotation.kt");
    }

    @TestMetadata("PlatformType.kt")
    public void testPlatformType() throws Exception {
        runTest("libraries/tools/kotlinp/testData/PlatformType.kt");
    }

    @TestMetadata("Properties.kt")
    public void testProperties() throws Exception {
        runTest("libraries/tools/kotlinp/testData/Properties.kt");
    }

    @TestMetadata("SimpleClass.kt")
    public void testSimpleClass() throws Exception {
        runTest("libraries/tools/kotlinp/testData/SimpleClass.kt");
    }

    @TestMetadata("SimplePackage.kt")
    public void testSimplePackage() throws Exception {
        runTest("libraries/tools/kotlinp/testData/SimplePackage.kt");
    }

    @TestMetadata("SyntheticClass.kt")
    public void testSyntheticClass() throws Exception {
        runTest("libraries/tools/kotlinp/testData/SyntheticClass.kt");
    }

    @TestMetadata("TypeParameters.kt")
    public void testTypeParameters() throws Exception {
        runTest("libraries/tools/kotlinp/testData/TypeParameters.kt");
    }

    @TestMetadata("ValueClass.kt")
    public void testValueClass() throws Exception {
        runTest("libraries/tools/kotlinp/testData/ValueClass.kt");
    }

    @TestMetadata("VersionRequirement.kt")
    public void testVersionRequirement() throws Exception {
        runTest("libraries/tools/kotlinp/testData/VersionRequirement.kt");
    }

    @TestMetadata("libraries/tools/kotlinp/testData/jvmDefault")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class JvmDefault extends AbstractKotlinpTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @TestMetadata("All.kt")
        public void testAll() throws Exception {
            runTest("libraries/tools/kotlinp/testData/jvmDefault/All.kt");
        }

        @TestMetadata("AllCompatibility.kt")
        public void testAllCompatibility() throws Exception {
            runTest("libraries/tools/kotlinp/testData/jvmDefault/AllCompatibility.kt");
        }

        public void testAllFilesPresentInJvmDefault() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("libraries/tools/kotlinp/testData/jvmDefault"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("withCompatibility.kt")
        public void testWithCompatibility() throws Exception {
            runTest("libraries/tools/kotlinp/testData/jvmDefault/withCompatibility.kt");
        }

        @TestMetadata("withoutCompatibility.kt")
        public void testWithoutCompatibility() throws Exception {
            runTest("libraries/tools/kotlinp/testData/jvmDefault/withoutCompatibility.kt");
        }
    }
}
