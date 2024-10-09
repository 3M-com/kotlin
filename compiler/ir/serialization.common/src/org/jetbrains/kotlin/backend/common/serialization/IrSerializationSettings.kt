/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization

import org.jetbrains.kotlin.config.LanguageVersionSettings

class IrSerializationSettings(
    val compatibilityMode: CompatibilityMode = CompatibilityMode.CURRENT,
    val languageVersionSettings: LanguageVersionSettings
)
