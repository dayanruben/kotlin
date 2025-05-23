/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.library.metadata

import org.jetbrains.kotlin.builtins.BuiltInsPackageFragment
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.metadataVersion
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.MetadataVersion
import org.jetbrains.kotlin.metadata.deserialization.NameResolverImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.DeserializationComponents
import org.jetbrains.kotlin.serialization.deserialization.DeserializedPackageFragment
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedPackageMemberScope
import org.jetbrains.kotlin.serialization.deserialization.getClassId
import org.jetbrains.kotlin.serialization.deserialization.getName
import org.jetbrains.kotlin.storage.StorageManager
import java.lang.ref.SoftReference

open class KlibMetadataDeserializedPackageFragment(
    fqName: FqName,
    private val library: KotlinLibrary,
    private val packageAccessHandler: PackageAccessHandler?,
    storageManager: StorageManager,
    module: ModuleDescriptor,
    private val partName: String,
    containerSource: KlibDeserializedContainerSource
) : KlibMetadataPackageFragment(fqName, storageManager, module, containerSource) {

    // The proto field is lazy so that we can load only needed
    // packages from the library.
    override val protoForNames: ProtoBuf.PackageFragment get() = ensureStorage()

    private var protoForNamesStorage: SoftReference<ProtoBuf.PackageFragment> = SoftReference(null)

    private fun ensureStorage(): ProtoBuf.PackageFragment {
        var tmp = protoForNamesStorage.get()
        if (tmp == null) {
            tmp = (packageAccessHandler ?: SimplePackageAccessHandler).loadPackageFragment(library, fqName.asString(), partName)
            protoForNamesStorage = SoftReference(tmp)
        }
        return tmp
    }

    override val proto: ProtoBuf.PackageFragment
        get() {
            packageAccessHandler?.markNeededForLink(library, fqName.asString())
            return protoForNames
        }
}

class BuiltInKlibMetadataDeserializedPackageFragment(
    fqName: FqName,
    library: KotlinLibrary,
    packageAccessHandler: PackageAccessHandler?,
    storageManager: StorageManager,
    module: ModuleDescriptor,
    partName: String,
    containerSource: KlibDeserializedContainerSource
) : KlibMetadataDeserializedPackageFragment(fqName, library, packageAccessHandler, storageManager, module, partName, containerSource),
    BuiltInsPackageFragment {

    override val isFallback: Boolean
        get() = false
}

class KlibMetadataCachedPackageFragment(
    byteArray: ByteArray,
    storageManager: StorageManager,
    module: ModuleDescriptor,
    override val protoForNames: ProtoBuf.PackageFragment = parsePackageFragment(byteArray),
    fqName: FqName = FqName(protoForNames.getExtension(KlibMetadataProtoBuf.fqName))
) : KlibMetadataPackageFragment(fqName, storageManager, module, containerSource = null)

abstract class KlibMetadataPackageFragment(
    fqName: FqName,
    storageManager: StorageManager,
    module: ModuleDescriptor,
    protected val containerSource: KlibDeserializedContainerSource?
) : DeserializedPackageFragment(fqName, storageManager, module) {

    lateinit var components: DeserializationComponents

    override fun initialize(components: DeserializationComponents) {
        this.components = components
    }

    // The proto field is lazy so that we can load only needed
    // packages from the library.
    abstract val protoForNames: ProtoBuf.PackageFragment

    open val proto: ProtoBuf.PackageFragment
        get() = protoForNames

    private val nameResolver by lazy {
        NameResolverImpl(protoForNames.strings, protoForNames.qualifiedNames)
    }

    override val classDataFinder by lazy {
        KlibMetadataClassDataFinder(protoForNames, nameResolver, containerSource)
    }

    override fun getSource(): SourceElement = containerSource ?: super.source

    private val _memberScope by lazy {
        DeserializedPackageMemberScope(
            this,
            proto.getPackage(),
            nameResolver,
            containerSource?.klib?.metadataVersion ?: MetadataVersion.INVALID_VERSION,
            /* containerSource = */ containerSource,
            components,
            "scope for $this"
        ) { loadClassNames() }
    }

    override fun getMemberScope(): DeserializedPackageMemberScope = _memberScope

    private val classifierNames: Set<Name> by lazy {
        val result = mutableSetOf<Name>()
        result.addAll(loadClassNames())
        protoForNames.getPackage().typeAliasList.mapTo(result) { nameResolver.getName(it.name) }
        result
    }

    fun hasTopLevelClassifier(name: Name): Boolean = name in classifierNames

    private fun loadClassNames(): Collection<Name> {

        val classNameList = protoForNames.getExtension(KlibMetadataProtoBuf.className).orEmpty()

        val names = classNameList.mapNotNull {
            val classId = nameResolver.getClassId(it)
            val shortName = classId.shortClassName
            if (!classId.isNestedClass) shortName else null
        }

        return names
    }
}
