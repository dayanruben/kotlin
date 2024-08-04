/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.util

import org.gradle.testkit.runner.BuildResult
import org.jetbrains.kotlin.gradle.testbase.*
import java.nio.file.Files
import kotlin.test.assertTrue

private const val RESOLVE_ALL_CONFIGURATIONS_TASK_NAME = "resolveAllConfigurations"
private const val UNRESOLVED_MARKER = "<<!>>UNRESOLVED:"
private val unresolvedConfigurationRegex = "${Regex.escape(UNRESOLVED_MARKER)}(.*)".toRegex()

fun TestProject.testResolveAllConfigurations(
    subproject: String? = null,
    withUnresolvedConfigurationNames: TestProject.(unresolvedConfigurations: List<String>, buildResult: BuildResult) -> Unit = { conf, _ ->
        assertTrue("Unresolved configurations: $conf") { conf.isEmpty() }
    },
) {
    val targetProject = subproject?.let { subProject(it) } ?: this
    when {
        Files.exists(targetProject.buildGradle) -> targetProject.buildGradle
            .append("\n${generateResolveAllConfigurationsTask()}")
        Files.exists(targetProject.buildGradleKts) -> targetProject.buildGradleKts
            .append("\n${generateResolveAllConfigurationsTaskKts()}")
        else -> error("Build script does not exist under $projectPath")
    }

    build(
        RESOLVE_ALL_CONFIGURATIONS_TASK_NAME,
        buildOptions = buildOptions.copy(
            // The configuration resolution happens during execution, so we have to disable CC
            configurationCache = BuildOptions.ConfigurationCacheValue.DISABLED,
        )
    ) {
        assertTasksExecuted(":${subproject?.let { "$it:" }.orEmpty()}$RESOLVE_ALL_CONFIGURATIONS_TASK_NAME")
        val unresolvedConfigurations = unresolvedConfigurationRegex.findAll(output).map { it.groupValues[1] }.toList()
        withUnresolvedConfigurationNames(unresolvedConfigurations, this)
    }
}

private fun generateResolveAllConfigurationsTask() =
    """
        tasks.register("$RESOLVE_ALL_CONFIGURATIONS_TASK_NAME") {
            if ("commonizeNativeDistribution" in rootProject.tasks.names) {
                dependsOn(":commonizeNativeDistribution")
            }
            doFirst {
                def excludeConfigs = ["default", "archives"]
                ${computeExcludeConfigurations()}

                project.configurations
                    .matching { it.canBeResolved }
                    .matching { !excludeConfigs.contains(it.name) }
                    .each { configuration ->
                        def configurationPath = (project.path == ":") ? ":" + configuration.name : project.path + ":" + configuration.name
                        try {                            
                            println "Resolving " + configurationPath
                            configuration.files.each { println '>> ' + configurationPath + ' --> ' + it.name }
                            println "OK, resolved " + configurationPath + "\n"
                        } catch (e) {
                            def ex = e
                            while (ex != null) {
                                println ex.message
                                ex = ex.cause
                            }
                            println '$UNRESOLVED_MARKER' + configurationPath + "\n"
                        }
                    }
            }
        }
    """.trimIndent()

private fun generateResolveAllConfigurationsTaskKts() =
    """
        tasks.register("$RESOLVE_ALL_CONFIGURATIONS_TASK_NAME") {
            if ("commonizeNativeDistribution" in rootProject.tasks.names) {
                dependsOn(":commonizeNativeDistribution")
            }
            doFirst {
                val excludeConfigs = mutableListOf("default", "archives")

                project.configurations
                    .filter { it.isCanBeResolved }
                    .filterNot { excludeConfigs.contains(it.name) }
                    .forEach { configuration ->
                        val configurationPath = 
                            if (project.path == ":") ":" + configuration.name
                            else project.path + ":" + configuration.name
                        try {
                            println("Resolving ${'$'}configurationPath")
                            configuration.files.forEach { println(">> ${'$'}configurationPath --> ${'$'}{it.name}") }
                            println("OK, resolved ${'$'}configurationPath\n")
                        } catch (e: Throwable) {
                            var ex = e as Throwable?
                            while (ex != null) {
                                println(ex.message)
                                ex = ex.cause
                            }
                            println("$UNRESOLVED_MARKER ${'$'}configurationPath\n")
                        }
                    }
            }
        }
    """.trimIndent()

private fun computeExcludeConfigurations(): String {
    val excludingConfigurations = listOf("compile", "runtime", "compileOnly", "runtimeOnly")
    return """
        kotlin.sourceSets.forEach { sourceSet ->
            "${excludingConfigurations.joinToString()}".split(", ").toList().forEach {
                excludeConfigs.add(sourceSet.name + it.capitalize())
            }
        }

        "${excludingConfigurations.joinToString()}".split(", ").toList().forEach {
            excludeConfigs.add(it)
            excludeConfigs.add("test" + it.capitalize())
        }
    """.trimIndent()
}
