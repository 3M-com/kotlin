/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.linkage

import org.jetbrains.kotlin.ir.IrProvider
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.name.Name

interface IrDeserializer : IrProvider {
    enum class TopLevelSymbolKind {
        FUNCTION_SYMBOL,
        CLASS_SYMBOL,
        PROPERTY_SYMBOL,
        TYPEALIAS_SYMBOL;
    }

    fun init(moduleFragment: IrModuleFragment?) {}
    fun resolveBySignatureInModule(signature: IdSignature, kind: TopLevelSymbolKind, moduleName: Name): IrSymbol

    /**
     * [postProcess] has two usages with different expectations:
     * - IR plugin API: actualize expects/actuals, generate fake overrides
     * - Linker(s): the same + run partial linkage
     *
     * In the future, this function should be split into several functions with different semantics for more precise use.
     */
    @Deprecated(
        "Use postProcess(inOrAfterLinkageStep) instead",
        ReplaceWith("postProcess(inOrAfterLinkageStep = true)"),
        DeprecationLevel.ERROR
    )
    fun postProcess() = postProcess(inOrAfterLinkageStep = true)
    fun postProcess(inOrAfterLinkageStep: Boolean)
}