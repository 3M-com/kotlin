/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.optimizations

import org.jetbrains.kotlin.utils.forEachBit
import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.backend.common.push
import org.jetbrains.kotlin.backend.konan.DirectedGraph
import org.jetbrains.kotlin.backend.konan.DirectedGraphNode
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class CallGraphNode(val graph: CallGraph, val symbol: DataFlowIR.FunctionSymbol.Declared)
    : DirectedGraphNode<DataFlowIR.FunctionSymbol.Declared> {

    override val key get() = symbol

    override val directEdges: List<DataFlowIR.FunctionSymbol.Declared> by lazy {
        graph.directEdges[symbol]!!.callSites
                .filter { !it.isVirtual }
                .map { it.actualCallee }
                .filterIsInstance<DataFlowIR.FunctionSymbol.Declared>()
                .filter { graph.directEdges.containsKey(it) }
    }

    override val reversedEdges: List<DataFlowIR.FunctionSymbol.Declared> by lazy {
        graph.reversedEdges[symbol]!!
    }

    class CallSite(val call: DataFlowIR.Node.Call, val node: DataFlowIR.Node, val isVirtual: Boolean, val actualCallee: DataFlowIR.FunctionSymbol)

    val callSites = mutableListOf<CallSite>()
}

internal class CallGraph(val directEdges: Map<DataFlowIR.FunctionSymbol.Declared, CallGraphNode>,
                         val reversedEdges: Map<DataFlowIR.FunctionSymbol.Declared, MutableList<DataFlowIR.FunctionSymbol.Declared>>,
                         val rootExternalFunctions: List<DataFlowIR.FunctionSymbol>,
                         val rootSet: Set<DataFlowIR.FunctionSymbol.Declared>)
    : DirectedGraph<DataFlowIR.FunctionSymbol.Declared, CallGraphNode> {

    override val nodes get() = directEdges.values

    override fun get(key: DataFlowIR.FunctionSymbol.Declared) = directEdges[key]!!

    fun addEdge(caller: DataFlowIR.FunctionSymbol.Declared, callSite: CallGraphNode.CallSite) {
        directEdges[caller]!!.callSites += callSite
    }

    fun addReversedEdge(caller: DataFlowIR.FunctionSymbol.Declared, callee: DataFlowIR.FunctionSymbol.Declared) {
        reversedEdges[callee]!!.add(caller)
    }
}

internal class CallGraphBuilder(
        val context: Context,
        val irModule: IrModuleFragment,
        val moduleDFG: ModuleDFG,
        val devirtualizedCallSitesUnfoldFactor: Int,
        val nonDevirtualizedCallSitesUnfoldFactor: Int
) {
    private val directEdges = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, CallGraphNode>()
    private val reversedEdges = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, MutableList<DataFlowIR.FunctionSymbol.Declared>>()
    private val externalRootFunctions = mutableListOf<DataFlowIR.FunctionSymbol>()
    private val wholeRootSet = mutableSetOf<DataFlowIR.FunctionSymbol.Declared>()
    private val callGraph = CallGraph(directEdges, reversedEdges, externalRootFunctions, wholeRootSet)

    private data class HandleFunctionParams(val caller: DataFlowIR.FunctionSymbol.Declared?,
                                            val calleeFunction: DataFlowIR.Function)
    private val functionStack = mutableListOf<HandleFunctionParams>()

    fun build(): CallGraph {
        val rootSet = DevirtualizationAnalysis.computeRootSet(context, irModule, moduleDFG)
        rootSet.forEach { handleRoot(it) }

        while (functionStack.isNotEmpty()) {
            val (caller, calleeFunction) = functionStack.pop()
            val callee = calleeFunction.symbol as DataFlowIR.FunctionSymbol.Declared
            val newFunction = !directEdges.containsKey(callee)
            if (newFunction)
                addNode(callee)
            if (caller != null)
                callGraph.addReversedEdge(caller, callee)
            if (newFunction)
                handleFunction(callee, calleeFunction)
        }
        return callGraph
    }

    private fun addNode(symbol: DataFlowIR.FunctionSymbol.Declared) {
        directEdges[symbol] = CallGraphNode(callGraph, symbol)
        reversedEdges[symbol] = mutableListOf()
    }

    private inline fun DataFlowIR.FunctionBody.forEachCallSite(block: (DataFlowIR.Node.Call, DataFlowIR.Node) -> Unit): Unit =
            forEachNonScopeNode { node ->
                when (node) {
                    is DataFlowIR.Node.Call -> block(node, node)

                    is DataFlowIR.Node.Singleton ->
                        node.constructor?.let { constructor ->
                            val arguments = buildList {
                                add(DataFlowIR.Edge(node, null)) // this.
                                node.arguments?.let { addAll(it) }
                            }
                            block(DataFlowIR.Node.Call(constructor, arguments, node.type, null), node)
                        }

                    is DataFlowIR.Node.ArrayRead ->
                        block(DataFlowIR.Node.Call(
                                callee = node.callee,
                                arguments = listOf(node.array, node.index),
                                returnType = node.type,
                                irCallSite = null),
                                node
                        )

                    is DataFlowIR.Node.ArrayWrite ->
                        block(DataFlowIR.Node.Call(
                                callee = node.callee,
                                arguments = listOf(node.array, node.index, node.value),
                                returnType = moduleDFG.symbolTable.mapType(context.irBuiltIns.unitType),
                                irCallSite = null),
                                node
                        )

                    else -> { }
                }
            }

    private fun staticCall(caller: DataFlowIR.FunctionSymbol.Declared, call: DataFlowIR.Node.Call, node: DataFlowIR.Node, callee: DataFlowIR.FunctionSymbol) {
        val callSite = CallGraphNode.CallSite(call, node, false, callee)
        val function = moduleDFG.functions[callee]
        callGraph.addEdge(caller, callSite)
        if (function != null)
            functionStack.push(HandleFunctionParams(caller, function))
    }

    private fun handleRoot(symbol: DataFlowIR.FunctionSymbol) {
        val function = moduleDFG.functions[symbol]
        if (function == null)
            externalRootFunctions.add(symbol)
        else {
            wholeRootSet.add(symbol as DataFlowIR.FunctionSymbol.Declared)
            functionStack.push(HandleFunctionParams(null, function))
        }
    }

    private fun handleFunction(symbol: DataFlowIR.FunctionSymbol.Declared, function: DataFlowIR.Function) {
        val body = function.body
        body.forEachCallSite { call, node ->
            val devirtualizedCallSite = (call as? DataFlowIR.Node.VirtualCall)?.irCallSite?.devirtualizedCallSite
            when {
                call !is DataFlowIR.Node.VirtualCall -> staticCall(symbol, call, node, call.callee)

                devirtualizedCallSite != null -> {
                    if (devirtualizedCallSite.possibleCallees.size <= devirtualizedCallSitesUnfoldFactor)
                        devirtualizedCallSite.possibleCallees.forEach {
                            staticCall(symbol, call, node, it.callee)
                        }
                    else {
                        val callSite = CallGraphNode.CallSite(call, node, true, call.callee)
                        callGraph.addEdge(symbol, callSite)

                        devirtualizedCallSite.possibleCallees.forEach { handleRoot(it.callee) }
                    }

                }

                call.receiverType == DataFlowIR.Type.Virtual -> {
                    // Skip callsite. This can only be for invocations Any's methods on instances of ObjC classes.
                }

                else -> {
                    // Callsite has not been devirtualized - conservatively assume the worst:
                    // any inheritor of the receiver type is possible here.
                    val typeHierarchy = moduleDFG.symbolTable.typeHierarchy
                    val allPossibleCallees = mutableListOf<DataFlowIR.FunctionSymbol>()
                    typeHierarchy.inheritorsOf(call.receiverType).forEachBit {
                        val receiverType = typeHierarchy.allTypes[it]
                        if (receiverType.isAbstract) return@forEachBit
                        // TODO: Unconservative way - when we can use it?
                        //.filter { devirtualizationAnalysisResult.instantiatingClasses.contains(it) }
                        val actualCallee = when (call) {
                            is DataFlowIR.Node.VtableCall ->
                                receiverType.vtable[call.calleeVtableIndex]

                            is DataFlowIR.Node.ItableCall ->
                                receiverType.itable[call.interfaceId]!![call.calleeItableIndex]

                            else -> error("Unreachable")
                        }
                        allPossibleCallees.add(actualCallee)
                    }
                    if (allPossibleCallees.size <= nonDevirtualizedCallSitesUnfoldFactor)
                        allPossibleCallees.forEach { staticCall(symbol, call, node, it) }
                    else {
                        val callSite = CallGraphNode.CallSite(call, node, true, call.callee)
                        callGraph.addEdge(symbol, callSite)

                        allPossibleCallees.forEach { handleRoot(it) }
                    }
                }
            }
        }
    }
}