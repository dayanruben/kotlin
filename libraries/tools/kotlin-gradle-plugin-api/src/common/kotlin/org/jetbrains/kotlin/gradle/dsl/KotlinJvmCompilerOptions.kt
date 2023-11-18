// DO NOT EDIT MANUALLY!
// Generated by org/jetbrains/kotlin/generators/arguments/GenerateGradleOptions.kt
// To regenerate run 'generateGradleOptions' task
@file:Suppress("RemoveRedundantQualifierName", "Deprecation", "DuplicatedCode")

package org.jetbrains.kotlin.gradle.dsl

interface KotlinJvmCompilerOptions : org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions {

    /**
     * Generate metadata for Java 1.8 reflection on method parameters.
     * Default value: false
     */
    @get:org.gradle.api.tasks.Input
    val javaParameters: org.gradle.api.provider.Property<kotlin.Boolean>

    /**
     * The target version of the generated JVM bytecode (1.8, 9, 10, ..., 21), with 1.8 as the default.
     * Possible values: "1.8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"
     * Default value: JvmTarget.DEFAULT
     */
    @get:org.gradle.api.tasks.Input
    val jvmTarget: org.gradle.api.provider.Property<org.jetbrains.kotlin.gradle.dsl.JvmTarget>

    /**
     * Name of the generated '.kotlin_module' file.
     * Default value: null
     */
    @get:org.gradle.api.tasks.Optional
    @get:org.gradle.api.tasks.Input
    val moduleName: org.gradle.api.provider.Property<kotlin.String>

    /**
     * Don't automatically include the Java runtime in the classpath.
     * Default value: false
     */
    @get:org.gradle.api.tasks.Input
    val noJdk: org.gradle.api.provider.Property<kotlin.Boolean>
}
