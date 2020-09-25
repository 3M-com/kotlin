/*
 * Copyright (C) 2020 Brian Norman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bnorm.power

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.asSimpleLambda
import org.jetbrains.kotlin.backend.common.ir.inline
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.at
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallOp
import org.jetbrains.kotlin.ir.builders.irFalse
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeArgument
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isBoolean
import org.jetbrains.kotlin.ir.types.isSubtypeOf
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.isFunctionOrKFunction
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.util.OperatorNameConventions
import java.io.File

fun FileLoweringPass.runOnFileInOrder(irFile: IrFile) {
  irFile.acceptVoid(object : IrElementVisitorVoid {
    override fun visitElement(element: IrElement) {
      element.acceptChildrenVoid(this)
    }

    override fun visitFile(declaration: IrFile) {
      lower(declaration)
      super.visitFile(declaration)
    }
  })
}

class PowerAssertCallTransformer(
  private val context: IrPluginContext,
  private val functions: Set<FqName>
) : IrElementTransformerVoidWithContext(), FileLoweringPass {
  private lateinit var file: IrFile
  private lateinit var fileSource: String

  override fun lower(irFile: IrFile) {
    file = irFile
    fileSource = File(irFile.path).readText().replace("\r\n", "\n")

    irFile.transformChildrenVoid()
  }

  override fun visitCall(expression: IrCall): IrExpression {
    // TODO expression.symbol.owner.fqNameSafe includes the wrapping ClassKt while descriptor doesn't
    val fqName = expression.symbol.descriptor.fqNameSafe
    if (functions.none { fqName == it })
      return super.visitCall(expression)

    val delegate = findDelegate(fqName) ?: run {
      // Find a valid delegate function or do not translate
      // TODO log a warning
      return super.visitCall(expression)
    }

    val function = expression.symbol.owner
    val assertionArgument = expression.getValueArgument(0)!!
    val messageArgument = if (function.valueParameters.size == 2) expression.getValueArgument(1) else null

    // If the tree does not contain any children, the expression is not transformable
    val tree = buildAssertTree(assertionArgument)
    val root = tree.children.singleOrNull() ?: return super.visitCall(expression)

    val symbol = currentScope!!.scope.scopeOwnerSymbol
    DeclarationIrBuilder(context, symbol).run {
      at(expression)

      val generator = object : PowerAssertGenerator() {
        override fun IrBuilderWithScope.buildAssertThrow(subStack: List<IrStackVariable>): IrExpression {

          val lambda = messageArgument?.asSimpleLambda()
          val title = when {
            messageArgument is IrConst<*> -> messageArgument
            messageArgument is IrStringConcatenation -> messageArgument
            lambda != null -> lambda.inline(parent)
            messageArgument != null -> {
              val invoke = messageArgument.type.getClass()!!.functions.single { it.name == OperatorNameConventions.INVOKE }
              irCallOp(invoke.symbol, invoke.returnType, messageArgument)
            }
            // TODO what should the default message be?
            else -> irString("Assertion failed")
          }

          return delegate.buildCall(this, buildMessage(file, fileSource, title, expression, subStack))
        }
      }

//      println(assertionArgument.dump())
//      println(tree.dump())

      return generator.buildAssert(this, root)
//        .also { println(it.dump())}
    }
  }

  private interface FunctionDelegate {
    fun buildCall(builder: IrBuilderWithScope, message: IrExpression): IrExpression
  }

  private fun findDelegate(fqName: FqName): FunctionDelegate? {
    return context.findOverloads(fqName)
      .mapNotNull { overload ->
        // TODO allow other signatures than (Boolean, String) and (Boolean, () -> String))
        val parameters = overload.owner.valueParameters
        if (parameters.size != 2) return@mapNotNull null
        if (!parameters[0].type.isBoolean()) return@mapNotNull null

        return@mapNotNull when {
          isStringSupertype(parameters[1].type) -> {
            object : FunctionDelegate {
              override fun buildCall(builder: IrBuilderWithScope, message: IrExpression): IrExpression = with(builder) {
                irCall(overload, type = overload.owner.returnType).apply {
                  putValueArgument(0, irFalse())
                  putValueArgument(1, message)
                }
              }
            }
          }
          isStringFunction(parameters[1].type) -> {
            object : FunctionDelegate {
              override fun buildCall(builder: IrBuilderWithScope, message: IrExpression): IrExpression = with(builder) {
                val scope = this
                val lambda = buildFun {
                  name = Name.special("<anonymous>")
                  returnType = context.irBuiltIns.stringType
                  visibility = Visibilities.LOCAL
                  origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
                }.apply {
                  val bodyBuilder = DeclarationIrBuilder(this@PowerAssertCallTransformer.context, symbol)
                  body = bodyBuilder.irBlockBody {
                    +irReturn(message)
                  }
                  parent = scope.parent
                }
                val expression = IrFunctionExpressionImpl(-1, -1, context.irBuiltIns.stringType, lambda, IrStatementOrigin.LAMBDA)
                irCall(overload, type = overload.owner.returnType).apply {
                  putValueArgument(0, irFalse())
                  putValueArgument(1, expression)
                }
              }
            }
          }
          else -> {
            null
          }
        }
      }
      .singleOrNull()
  }

  private fun isStringFunction(type: IrType): Boolean =
    type.isFunctionOrKFunction() && type is IrSimpleType && (type.arguments.size == 1 && isStringSupertype(type.arguments.first()))

  private fun isStringSupertype(argument: IrTypeArgument): Boolean =
    argument is IrTypeProjection && isStringSupertype(argument.type)

  private fun isStringSupertype(type: IrType): Boolean =
    context.irBuiltIns.stringType.isSubtypeOf(type, context.irBuiltIns)
}

// TODO is this the best way to find overload functions?
private fun IrPluginContext.findOverloads(fqName: FqName): List<IrFunctionSymbol> {
  return referenceFunctions(fqName).toList()
}
