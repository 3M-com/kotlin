/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.plugin

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.scripting.configuration.ScriptingConfigurationKeys
import java.io.File

class ScriptingCommandLineProcessor : CommandLineProcessor {
    companion object {
        val DISABLE_SCRIPTING_PLUGIN_OPTION = CliOption(
            "disable", "true/false", "Disable scripting plugin",
            required = false, allowMultipleOccurrences = false
        )
        val SCRIPT_DEFINITIONS_OPTION = CliOption(
            "script-definitions", "<fully qualified class name[,]>", "Script definition classes",
            required = false, allowMultipleOccurrences = true
        )
        val SCRIPT_DEFINITIONS_CLASSPATH_OPTION = CliOption(
            "script-definitions-classpath", "<classpath entry[:]>", "Additional classpath for the script definitions",
            required = false, allowMultipleOccurrences = true
        )
        val DISABLE_STANDARD_SCRIPT_DEFINITION_OPTION = CliOption(
            "disable-standard-script", "true/false", "Disable standard kotlin script support",
            required = false, allowMultipleOccurrences = false
        )
        val DISABLE_SCRIPT_DEFINITIONS_FROM_CLSSPATH_OPTION = CliOption(
            "disable-script-definitions-from-classpath", "true/false", "Do not extract script definitions from the compilation classpath",
            required = false, allowMultipleOccurrences = false
        )
        val DISABLE_SCRIPT_DEFINITIONS_AUTOLOADING_OPTION = CliOption(
            "disable-script-definitions-autoloading",
            "true/false",
            "Do not automatically load compiler-supplied script definitions, like main-kts",
            required = false, allowMultipleOccurrences = false
        )
        val ENABLE_SCRIPT_EXPLANATION_OPTION = CliOption(
            "enable-script-explanation",
            "true/false",
            "Enable additional IR generation which contains script expressions evaluation info  (works via power-assert plugin)",
            required = false, allowMultipleOccurrences = false
        )
        val LEGACY_SCRIPT_TEMPLATES_OPTION = CliOption(
            "script-templates", "<fully qualified class name[,]>", "Script definition template classes",
            required = false, allowMultipleOccurrences = true
        )
        val LEGACY_SCRIPT_RESOLVER_ENVIRONMENT_OPTION = CliOption(
            "script-resolver-environment", "<key=value[,]>",
            "Script resolver environment in key-value pairs (the value could be quoted and escaped)",
            required = false, allowMultipleOccurrences = true
        )
    }

    override val pluginId = KOTLIN_SCRIPTING_PLUGIN_ID
    override val pluginOptions =
        listOf(
            DISABLE_SCRIPTING_PLUGIN_OPTION,
            SCRIPT_DEFINITIONS_OPTION,
            SCRIPT_DEFINITIONS_CLASSPATH_OPTION,
            DISABLE_STANDARD_SCRIPT_DEFINITION_OPTION,
            DISABLE_SCRIPT_DEFINITIONS_FROM_CLSSPATH_OPTION,
            DISABLE_SCRIPT_DEFINITIONS_AUTOLOADING_OPTION,
            LEGACY_SCRIPT_TEMPLATES_OPTION,
            LEGACY_SCRIPT_RESOLVER_ENVIRONMENT_OPTION,
            ENABLE_SCRIPT_EXPLANATION_OPTION
        )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) = when (option) {
        DISABLE_SCRIPTING_PLUGIN_OPTION -> {
            configuration.put(
                ScriptingConfigurationKeys.DISABLE_SCRIPTING_PLUGIN_OPTION,
                value.takeUnless { it.isBlank() }?.toBoolean() ?: true
            )
        }

        SCRIPT_DEFINITIONS_OPTION, LEGACY_SCRIPT_TEMPLATES_OPTION -> {
            val currentDefs = configuration.getList(ScriptingConfigurationKeys.SCRIPT_DEFINITIONS_CLASSES).toMutableList()
            currentDefs.addAll(value.split(','))
            configuration.put(ScriptingConfigurationKeys.SCRIPT_DEFINITIONS_CLASSES, currentDefs)
        }
        SCRIPT_DEFINITIONS_CLASSPATH_OPTION -> {
            val currentCP = configuration.getList(ScriptingConfigurationKeys.SCRIPT_DEFINITIONS_CLASSPATH).toMutableList()
            currentCP.addAll(value.split(File.pathSeparatorChar).map(::File))
            configuration.put(ScriptingConfigurationKeys.SCRIPT_DEFINITIONS_CLASSPATH, currentCP)
        }
        DISABLE_STANDARD_SCRIPT_DEFINITION_OPTION -> {
            configuration.put(
                JVMConfigurationKeys.DISABLE_STANDARD_SCRIPT_DEFINITION,
                value.takeUnless { it.isBlank() }?.toBoolean() ?: true
            )
        }
        DISABLE_SCRIPT_DEFINITIONS_FROM_CLSSPATH_OPTION -> {
            configuration.put(
                ScriptingConfigurationKeys.DISABLE_SCRIPT_DEFINITIONS_FROM_CLASSPATH_OPTION,
                value.takeUnless { it.isBlank() }?.toBoolean() ?: true
            )
        }
        DISABLE_SCRIPT_DEFINITIONS_AUTOLOADING_OPTION -> {
            configuration.put(
                ScriptingConfigurationKeys.DISABLE_SCRIPT_DEFINITIONS_AUTOLOADING_OPTION,
                value.takeUnless { it.isBlank() }?.toBoolean() ?: true
            )
        }
        ENABLE_SCRIPT_EXPLANATION_OPTION -> {
            configuration.put(
                ScriptingConfigurationKeys.ENABLE_SCRIPT_EXPLANATION_OPTION,
                value.takeUnless { it.isBlank() }?.toBoolean() ?: false
            )
        }
        LEGACY_SCRIPT_RESOLVER_ENVIRONMENT_OPTION -> {
            val currentEnv = configuration.getMap(ScriptingConfigurationKeys.LEGACY_SCRIPT_RESOLVER_ENVIRONMENT_OPTION).toMutableMap()
            // parses key/value pairs in the form <key>=<value>, where
            //   <key> - is a single word (\w+ pattern)
            //   <value> - optionally quoted string with allowed escaped chars (only double-quote, comma and backslash chars are supported)
            // TODO: implement generic unescaping
            // TODO: consider switching to simple parser - current approach is too complicated already and doesn't handle quoted commas (unless they are escaped)
            val envParseRe = """(\w+)=(?:"([^"\\]*(\\.[^"\\]*)*)"|([^\s]*))""".toRegex()
            val unescapeRe = """\\(["\\,])""".toRegex()
            val splitRe = """(?:\\.|[^,\\]++)*""".toRegex()
            val splitMatches = splitRe.findAll(value)
            for (envParam in splitMatches.map { it.value }.filter { it.isNotBlank() }) {
                val match = envParseRe.matchEntire(envParam)
                if (match == null || match.groupValues.size < 4 || match.groupValues[1].isBlank()) {
                    throw CliOptionProcessingException("Unable to parse script-resolver-environment argument $envParam")
                }
                currentEnv[match.groupValues[1]] =
                    match.groupValues.drop(2).firstOrNull { it.isNotEmpty() }?.let { unescapeRe.replace(it, "\$1") }
            }
            configuration.put(ScriptingConfigurationKeys.LEGACY_SCRIPT_RESOLVER_ENVIRONMENT_OPTION, currentEnv)
        }
        else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
    }
}
