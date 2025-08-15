/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.jsexport

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.serialization.js.ModuleKind

sealed class ExportedDeclaration

data class ExportedModule(
    val name: String,
    val moduleKind: ModuleKind,
    val declarations: List<ExportedDeclaration>
)

class ExportedNamespace(
    val name: String,
    val declarations: List<ExportedDeclaration>,
) : ExportedDeclaration()

data class ExportedFunction(
    val name: String,
    val isMember: Boolean = false,
    val isStatic: Boolean = false,
    val isAbstract: Boolean = false,
    val ir: IrSimpleFunction
) : ExportedDeclaration()

data class ExportedProperty(
    val name: String,
    val mutable: Boolean = true,
    val isMember: Boolean = false,
    val isStatic: Boolean = false,
    val isAbstract: Boolean = false,
    val isField: Boolean = false,
    val irGetter: IrFunction? = null,
    val irSetter: IrFunction? = null,
    val isOptional: Boolean = false,
    val isQualified: Boolean = false,
    val isObjectGetter: Boolean = false,
) : ExportedDeclaration()

sealed class ExportedClass : ExportedDeclaration() {
    abstract val name: String
    abstract val ir: IrClass
    abstract val members: List<ExportedDeclaration>
    abstract val nestedClasses: List<ExportedClass>
}

data class ExportedRegularClass(
    override val name: String,
    val isInterface: Boolean = false,
    val isAbstract: Boolean = false,
    val requireMetadata: Boolean = !isInterface,
    override val members: List<ExportedDeclaration>,
    override val nestedClasses: List<ExportedClass>,
    override val ir: IrClass,
) : ExportedClass()

data class ExportedObject(
    override val name: String,
    override val members: List<ExportedDeclaration>,
    override val nestedClasses: List<ExportedClass>,
    override val ir: IrClass,
    val irGetter: IrSimpleFunction? = null,
) : ExportedClass()
