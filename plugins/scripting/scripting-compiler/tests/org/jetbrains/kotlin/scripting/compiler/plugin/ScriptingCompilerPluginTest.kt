/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DEPRECATION")

package org.jetbrains.kotlin.scripting.compiler.plugin

import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.cliArgument
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageCollectorImpl
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.SCRIPT_BASE_COMPILER_ARGUMENTS_PROPERTY
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.reporter
import org.jetbrains.kotlin.scripting.configuration.ScriptingConfigurationKeys
import org.jetbrains.kotlin.scripting.definitions.SCRIPT_DEFINITION_MARKERS_PATH
import org.jetbrains.kotlin.scripting.definitions.discoverScriptTemplatesInClasspath
import org.jetbrains.kotlin.scripting.definitions.loadScriptTemplatesFromClasspath
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.TestJdkKind
import org.jetbrains.kotlin.utils.KotlinPaths
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class ScriptingCompilerPluginTest {

    companion object {
        const val TEST_DATA_DIR = "plugins/scripting/scripting-compiler/testData"
    }

    init {
        setIdeaIoUseFallback()
    }

    private val kotlinPaths: KotlinPaths by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val paths = PathUtil.kotlinPathsForDistDirectory
        assertTrue(paths.libPath.absoluteFile.isDirectory, "Lib directory doesn't exist. Run 'ant dist'")
        paths
    }

    val runtimeClasspath = listOf( kotlinPaths.stdlibPath, kotlinPaths.scriptRuntimePath, kotlinPaths.reflectPath)
    val scriptingClasspath = listOf("kotlin-scripting-common.jar").map { File(kotlinPaths.libPath, it) }

    private fun createEnvironment(
        sources: List<String>,
        destDir: File,
        messageCollector: MessageCollector,
        disposable: Disposable,
        confBody: CompilerConfiguration.() -> Unit
    ): KotlinCoreEnvironment {
        val configuration = KotlinTestUtils.newConfiguration(ConfigurationKind.NO_KOTLIN_REFLECT, TestJdkKind.FULL_JDK).apply {
            updateWithBaseCompilerArguments()
            this.messageCollector = messageCollector
            addKotlinSourceRoots(sources)
            put(JVMConfigurationKeys.OUTPUT_DIRECTORY, destDir)
            confBody()
        }
        configuration.add(ComponentRegistrar.PLUGIN_COMPONENT_REGISTRARS, ScriptingCompilerConfigurationComponentRegistrar())
        configuration.add(CompilerPluginRegistrar.COMPILER_PLUGIN_REGISTRARS, ScriptingK2CompilerPluginRegistrar())

        return KotlinCoreEnvironment.createForTests(disposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
    }

    @Test
    fun testScriptResolverEnvironmentArgsParsing() {

        val longStr = (1..100).joinToString("\\,") { """\" $it aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \\""" }
        val unescapeRe = """\\(["\\,])""".toRegex()
        val cmdlineProcessor = ScriptingCommandLineProcessor()
        val configuration = CompilerConfiguration()

        cmdlineProcessor.processOption(
            ScriptingCommandLineProcessor.LEGACY_SCRIPT_RESOLVER_ENVIRONMENT_OPTION as AbstractCliOption,
            """abc=def,11="ab cd \\ \"",long="$longStr"""",
            configuration
        )

        val res = configuration.getMap(ScriptingConfigurationKeys.LEGACY_SCRIPT_RESOLVER_ENVIRONMENT_OPTION)

        assertEquals(
            hashMapOf("abc" to "def", "11" to "ab cd \\ \"", "long" to unescapeRe.replace(longStr, "\$1")),
            res
        )
    }

    @Test
    fun testLazyScriptDefinitionDiscovery() {

        withTempDir { tmpdir ->
            withDisposable { disposable ->
                // Three tests in one function: the direct loading, the discovery code separately, and as a part of regular compilation
                // tests are combined to avoid multiple compilation of script definition modules

                val defsOut = File(tmpdir, "testLazyScriptDefinition/out/defs")
                val defsSrc = File(TEST_DATA_DIR, "lazyDefinitions/definitions")
                val scriptsOut = File(tmpdir, "testLazyScriptDefinition/out/scripts")
                val scriptsSrc = File(TEST_DATA_DIR, "lazyDefinitions/scripts")
                val scriptsOut2 = File(tmpdir, "testLazyScriptDefinition/out/scripts2")
                val defClasses = listOf("TestScriptWithReceivers", "TestScriptWithSimpleEnvVars")

                val messageCollector = MessageCollectorImpl()

                val definitionsCompileResult = KotlinToJVMBytecodeCompiler.compileBunchOfSources(
                    createEnvironment(defClasses.map { File(defsSrc, "$it.kt").canonicalPath }, defsOut, messageCollector, disposable) {
                        addJvmClasspathRoots(runtimeClasspath)
                        addJvmClasspathRoots(scriptingClasspath)
                    }
                )

                assertTrue(definitionsCompileResult) {
                    "Compilation of script definitions failed: $messageCollector"
                }

                messageCollector.clear()

                loadScriptTemplatesFromClasspath(
                    listOf("TestScriptWithReceivers", "TestScriptWithSimpleEnvVars"),
                    listOf(defsOut),
                    emptyList(),
                    this::class.java.classLoader,
                    defaultJvmScriptingHostConfiguration,
                    messageCollector.reporter
                ).toList()

                for (def in defClasses) {
                    assertTrue(messageCollector.messages.any { it.message.contains("Configure scripting: Added template $def") }) {
                        "Missing messages from loading sequence (should contain \"Added template $def\"):\n$messageCollector"
                    }
                    assertTrue(messageCollector.messages.none { it.message.contains("Configure scripting: loading script definition class $def") }) {
                        "Unexpected messages from loading sequence (should not contain \"loading script definition class $def\"):\n$messageCollector"
                    }
                }

                messageCollector.clear()

                // chacking lazy discovery

                val templatesDir = File(defsOut, SCRIPT_DEFINITION_MARKERS_PATH).also { it.mkdirs() }
                for (def in defClasses) {
                    File(templatesDir, def).createNewFile()
                }

                val lazyDefsSeq =
                    discoverScriptTemplatesInClasspath(
                        listOf(defsOut),
                        this::class.java.classLoader,
                        defaultJvmScriptingHostConfiguration,
                        messageCollector.reporter
                    )

                assertTrue(messageCollector.messages.isEmpty()) {
                    "Unexpected messages from discovery sequence (should be empty):\n$messageCollector"
                }

                val lazyDefs = lazyDefsSeq.toList()

                for (def in defClasses) {
                    assertTrue(messageCollector.messages.any { it.message.contains("Configure scripting: Added template $def") }) {
                        "Missing messages from discovery sequence (should contain \"Added template $def\"):\n$messageCollector"
                    }
                    assertTrue(messageCollector.messages.none { it.message.contains("Configure scripting: loading script definition class $def") }) {
                        "Unexpected messages from discovery sequence (should not contain \"loading script definition class $def\"):\n$messageCollector"
                    }
                }

                messageCollector.clear()

                val scriptFiles = scriptsSrc.listFiles { file: File -> file.extension == "kts" }.map { it.canonicalPath }

                val scriptsCompileEnv = createEnvironment(scriptFiles, scriptsOut, messageCollector, disposable) {
                    addJvmClasspathRoots(runtimeClasspath)
                    addJvmClasspathRoots(scriptingClasspath)
                    addJvmClasspathRoot(defsOut)
                    addAll(ScriptingConfigurationKeys.SCRIPT_DEFINITIONS, lazyDefs)
                }

                val res = KotlinToJVMBytecodeCompiler.compileBunchOfSources(scriptsCompileEnv)

                assertTrue(res) {
                    "Failed to compile scripts:\n$messageCollector"
                }

                val isK2 = System.getProperty(SCRIPT_BASE_COMPILER_ARGUMENTS_PROPERTY)?.contains("${CommonCompilerArguments::languageVersion.cliArgument} 1.9") != true &&
                        System.getProperty(SCRIPT_TEST_BASE_COMPILER_ARGUMENTS_PROPERTY)?.contains("${CommonCompilerArguments::languageVersion.cliArgument} 1.9") != true

                val cp = (runtimeClasspath + scriptingClasspath + defsOut).joinToString(File.pathSeparator)
                val exitCode = K2JVMCompiler().exec(
                    System.err,
                    K2JVMCompilerArguments::classpath.cliArgument,
                    cp,
                    *(scriptFiles.toTypedArray()),
                    K2JVMCompilerArguments::destination.cliArgument,
                    scriptsOut2.canonicalPath,
                    K2JVMCompilerArguments::allowAnyScriptsInSourceRoots.cliArgument,
                    K2JVMCompilerArguments::useFirLT.cliArgument("false"),
                    CommonCompilerArguments::languageVersion.cliArgument,
                    if (isK2) "2.0" else "1.9",
                    CommonCompilerArguments::suppressVersionWarnings.cliArgument,
                )

                assertEquals(ExitCode.OK, exitCode)
            }
        }
    }

    @Test
    fun testLazyScriptDefinitionOtherAnnotation() {

        withTempDir { tmpdir ->
            withDisposable { disposable ->
                val defsOut = File(tmpdir, "testLazyScriptDefinition/out/otherAnn")
                val defsSrc = File(TEST_DATA_DIR, "lazyDefinitions/definitions")
                val defClasses = listOf("TestScriptWithOtherAnnotation")

                val messageCollector = MessageCollectorImpl()

                val definitionsCompileResult = KotlinToJVMBytecodeCompiler.compileBunchOfSources(
                    createEnvironment(defClasses.map { File(defsSrc, "$it.kt").canonicalPath }, defsOut, messageCollector, disposable) {
                        addJvmClasspathRoots(runtimeClasspath)
                        addJvmClasspathRoots(scriptingClasspath)
                    }
                )

                assertTrue(definitionsCompileResult) {
                    "Compilation of script definitions failed: $messageCollector"
                }

                val templatesDir = File(defsOut, SCRIPT_DEFINITION_MARKERS_PATH).also { it.mkdirs() }
                for (def in defClasses) {
                    File(templatesDir, def).createNewFile()
                }

                messageCollector.clear()

                discoverScriptTemplatesInClasspath(
                    listOf(defsOut),
                    this::class.java.classLoader,
                    defaultJvmScriptingHostConfiguration,
                    messageCollector.reporter
                ).toList()

                assertTrue(
                    messageCollector.messages.isNotEmpty()
                            && messageCollector.messages.all { it.message.contains("s not marked with any known kotlin script annotation") }
                ) {
                    "Unexpected messages from discovery sequence:\n$messageCollector"
                }
            }
        }
    }
}

fun MessageCollectorImpl.assertHasMessage(msg: String, desiredSeverity: CompilerMessageSeverity? = null) {
    assert(messages.any { it.message.contains(msg) && (desiredSeverity == null || it.severity == desiredSeverity) }) {
        "Expecting message \"$msg\" with severity ${desiredSeverity?.toString() ?: "Any"}, actual:\n" +
                messages.joinToString("\n") { it.severity.toString() + ": " + it.message }
    }
}

fun assertTrue(exp: Boolean, msg: () -> String) {
    if (!exp) {
        fail(msg())
    }
}
