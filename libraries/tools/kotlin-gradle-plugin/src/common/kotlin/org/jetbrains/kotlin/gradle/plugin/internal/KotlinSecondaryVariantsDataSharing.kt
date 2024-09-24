/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.internal

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.utils.JsonUtils
import org.jetbrains.kotlin.gradle.utils.LazyResolvedConfiguration
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.gradle.utils.projectStoredProperty

internal val Project.kotlinSecondaryVariantsDataSharing: KotlinSecondaryVariantsDataSharing by projectStoredProperty {
    KotlinSecondaryVariantsDataSharing(project)
}

/**
 * Marker interface of classes that shares data between Gradle Projects using [KotlinSecondaryVariantsDataSharing]
 * Implementations should be serializable via [JsonUtils]
 */
internal interface KotlinShareableDataAsSecondaryVariant

/**
 * Service to share configuration state between Kotlin Projects as Configuration Secondary Variants
 */
internal class KotlinSecondaryVariantsDataSharing(
    private val project: Project
) {
    fun <T: KotlinShareableDataAsSecondaryVariant> shareDataFromProvider(
        key: String,
        outgoingConfiguration: Configuration,
        dataProvider: Provider<T>
    ) {
        val taskName = lowerCamelCaseName("export", key, "for", outgoingConfiguration.name)
        val task = project.tasks.register(taskName, ExportKotlinProjectDataTask::class.java) { task ->
            val fileName = "${key}_${outgoingConfiguration.name}.json"

            @Suppress("UNCHECKED_CAST")
            val taskOutputData = task.outputData as Property<T>
            taskOutputData.set(dataProvider)

            task.outputFile.set(project.layout.buildDirectory.file("kotlin/kotlin-project-shared-data/$fileName"))
        }

        val fileFromTask = task.flatMap { it.outputFile }
        outgoingConfiguration.outgoing.variants.create(key) { variant ->
            variant.artifact(fileFromTask) { artifact ->
                artifact.type = key
            }
            variant.attributes.configureAttributes(key)
        }
    }

    fun <T : KotlinShareableDataAsSecondaryVariant> consume(
        key: String,
        incomingConfiguration: Configuration,
        clazz: Class<T>,
    ): KotlinProjectSharedDataProvider<T> {
        val lazyResolvedConfiguration = LazyResolvedConfiguration(incomingConfiguration) { attributes ->
            attributes.configureAttributes(key)
        }
        return KotlinProjectSharedDataProvider(key, lazyResolvedConfiguration, clazz)
    }

    private fun AttributeContainer.configureAttributes(key: String) {
        val usageValue = project.objects.named(Usage::class.java, "kotlin-project-shared-data")
        attributeProvider(Usage.USAGE_ATTRIBUTE, project.provider { usageValue })
        attributeProvider(kotlinProjectSharedDataAttribute, project.provider { key })
    }
}

private val kotlinProjectSharedDataAttribute = Attribute.of("org.jetbrains.kotlin.project-shared-data", String::class.java)

/**
 * This class is Configuration Cache safe. It can be stored in a Task field.
 */
internal class KotlinProjectSharedDataProvider<T : KotlinShareableDataAsSecondaryVariant>(
    private val key: String,
    private val lazyResolvedConfiguration: LazyResolvedConfiguration,
    private val clazz: Class<T>,
) {
    val rootComponent: ResolvedComponentResult get() = lazyResolvedConfiguration.root

    val allResolvedDependencies: Set<ResolvedDependencyResult> get() = lazyResolvedConfiguration.allResolvedDependencies

    val files: FileCollection = lazyResolvedConfiguration.files

    fun getProjectDataFromDependencyOrNull(resolvedDependency: ResolvedDependencyResult): T? {
        val artifact = lazyResolvedConfiguration.getArtifacts(resolvedDependency).singleOrNull() ?: return null
        return artifact.parse()
    }

    private fun ResolvedArtifactResult.parse(): T? {
        // In rare cases, for example when provided attributes and requested attributes didn't match at all.
        // Gradle will resolve into that variant.
        // It can happen in Android Gradle Plugin for example, as they have a lot of secondary variants with few attributes
        val keyFromResolvedArtifact = variant.attributes.getAttribute(kotlinProjectSharedDataAttribute) ?: return null
        if (key != keyFromResolvedArtifact) return null

        val content = file.readText()
        return runCatching { JsonUtils.gson.fromJson(content, clazz) }.getOrNull()
    }
}

@DisableCachingByDefault(because = "Trivial operation")
internal abstract class ExportKotlinProjectDataTask : DefaultTask() {
    @get:Nested
    abstract val outputData: Property<KotlinShareableDataAsSecondaryVariant>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun action() {
        val data = outputData.get()
        val json = JsonUtils.gson.toJson(data)
        outputFile.get().asFile.writeText(json)
    }
}