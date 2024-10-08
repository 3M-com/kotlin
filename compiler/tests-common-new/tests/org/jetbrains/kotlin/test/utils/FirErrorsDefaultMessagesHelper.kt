/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.utils

import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.junit.Assert
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

fun KtDiagnosticFactoryToRendererMap.verifyMessages(objectWithErrors: Any) {
    val errors = mutableListOf<String>()
    for (property in objectWithErrors::class.memberProperties) {
        when (val factory = property.getter.call(objectWithErrors)) {
            is AbstractKtDiagnosticFactory -> {
                errors += verifyMessageForFactory(factory, property)
            }
            is KtDiagnosticFactoryForDeprecation<*> -> {
                errors += verifyMessageForFactory(factory.warningFactory, property)
                errors += verifyMessageForFactory(factory.errorFactory, property)
            }
            else -> {}
        }
    }

    if (errors.isNotEmpty()) {
        Assert.fail(errors.joinToString("\n", prefix = "\n"))
    }
}

private val messageParameterRegex = """\{\d.*?}""".toRegex()

private val lastCharRegex = """[.}\d]""".toRegex()

private val lastCharExclusions = setOf(
    FirErrors.DATA_CLASS_COPY_VISIBILITY_WILL_BE_CHANGED.name,
    FirErrors.ERROR_SUPPRESSION.name,
    FirErrors.NOT_A_MULTIPLATFORM_COMPILATION.name,
    FirErrors.SAFE_CALL_WILL_CHANGE_NULLABILITY.name,
)

fun KtDiagnosticFactoryToRendererMap.verifyMessageForFactory(factory: AbstractKtDiagnosticFactory, property: KProperty<*>) = buildList {
    if (!containsKey(factory)) {
        add("No default diagnostic renderer is provided for ${property.name}")
        return@buildList
    }

    val renderer = get(factory)!!

    val parameterCount = when (renderer) {
        is KtDiagnosticWithParameters4Renderer<*, *, *, *> -> 4
        is KtDiagnosticWithParameters3Renderer<*, *, *> -> 3
        is KtDiagnosticWithParameters2Renderer<*, *> -> 2
        is KtDiagnosticWithParameters1Renderer<*> -> 1
        else -> 0
    }

    for (parameter in messageParameterRegex.findAll(renderer.message)) {
        val index = parameter.value.substring(1, 2).toInt()
        if (index >= parameterCount) {
            add("Message for ${property.name} references wrong parameter {$index}")
        }
    }

    if (parameterCount > 0 && renderer.message.contains("(?<!')'(?!')".toRegex())) {
        add("Renderer for ${property.name} has parameters and contains a single quote. Text inside single quotes is not formatted in MessageFormat. Use double quotes instead.")
    }

    if (parameterCount == 0 && renderer.message.contains("(?<!')''(?!')".toRegex())) {
        add("Renderer for ${property.name} has no parameters and contains double quote. Single quotes should be used.")
    }

    if (property.name !in lastCharExclusions && !renderer.message.last().toString().matches(lastCharRegex)) {
        add("Renderer for ${property.name} should end with a full stop. If this error is a false positive, add the name of the diagnostic to the list of exclusions.")
    }
}