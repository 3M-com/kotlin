/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.codegen.state.JvmBackendConfig
import org.jetbrains.kotlin.config.JvmAnalysisFlags
import org.jetbrains.kotlin.load.java.JvmAnnotationNames
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader
import org.jetbrains.org.objectweb.asm.AnnotationVisitor

fun writeKotlinMetadata(
    cb: ClassBuilder,
    config: JvmBackendConfig,
    kind: KotlinClassHeader.Kind,
    publicAbi: Boolean,
    extraFlags: Int,
    action: (AnnotationVisitor) -> Unit
) {
    val av = cb.newAnnotation(JvmAnnotationNames.METADATA_DESC, true)
    av.visit(JvmAnnotationNames.METADATA_VERSION_FIELD_NAME, config.metadataVersion.toArray())
    av.visit(JvmAnnotationNames.KIND_FIELD_NAME, kind.id)
    var flags = extraFlags
    if (config.languageVersionSettings.isPreRelease()) {
        flags = flags or JvmAnnotationNames.METADATA_PRE_RELEASE_FLAG
    }
    if (config.languageVersionSettings.getFlag(JvmAnalysisFlags.strictMetadataVersionSemantics)) {
        flags = flags or JvmAnnotationNames.METADATA_STRICT_VERSION_SEMANTICS_FLAG
    }
    if (publicAbi) {
        flags = flags or JvmAnnotationNames.METADATA_PUBLIC_ABI_FLAG
    }
    if (flags != 0) {
        av.visit(JvmAnnotationNames.METADATA_EXTRA_INT_FIELD_NAME, flags)
    }
    action(av)
    av.visitEnd()
}
