/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/fir/tree/tree-generator/Readme.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.fir.expressions

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.FirVisitor

/**
 * Generated from: [org.jetbrains.kotlin.fir.tree.generator.FirTree.typeOperatorCall]
 */
abstract class FirTypeOperatorCall : FirExpression(), FirCall {
    abstract override val source: KtSourceElement?
    @UnresolvedExpressionTypeAccess
    abstract override val coneTypeOrNull: ConeKotlinType?
    abstract override val annotations: List<FirAnnotation>
    abstract override val argumentList: FirArgumentList
    abstract val operation: FirOperation
    abstract val conversionTypeRef: FirTypeRef
    abstract val argFromStubType: Boolean

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R =
        visitor.visitTypeOperatorCall(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformTypeOperatorCall(this, data) as E

    abstract override fun replaceConeTypeOrNull(newConeTypeOrNull: ConeKotlinType?)

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun replaceArgumentList(newArgumentList: FirArgumentList)

    abstract fun replaceConversionTypeRef(newConversionTypeRef: FirTypeRef)

    abstract fun replaceArgFromStubType(newArgFromStubType: Boolean)

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirTypeOperatorCall

    abstract fun <D> transformConversionTypeRef(transformer: FirTransformer<D>, data: D): FirTypeOperatorCall

    abstract fun <D> transformOtherChildren(transformer: FirTransformer<D>, data: D): FirTypeOperatorCall
}
