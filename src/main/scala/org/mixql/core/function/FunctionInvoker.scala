package org.mixql.core.function

import org.mixql.core.context.Context
import org.mixql.core.context.gtype._
import java.lang.reflect.Method
import scala.collection.mutable.ListBuffer

import scala.reflect.runtime.{universe => ru}

object FunctionInvoker {
  def invoke(
    functions: Map[String, Any],
    funcName: String,
    context: Context,
    params: Seq[Any] = Nil,
    paramsMap: Map[String, Object] = Map.empty
  ): Any = {
    functions.get(funcName.toLowerCase()) match {
      case Some(func) =>
        func match {
          case l: List[_] =>
            for (f <- l) {
              val applyMethods =
                f.getClass.getMethods.filter(x =>
                  // x.getParameters.length != 0 &&
                  x.getParameters.exists(y =>
                    y.getType.getName != "java.lang.Object"
                  ) && x.getName == "apply"
                )

              if (compareFunctionTypes(applyMethods(0), params)) {
                return invokeFunc(
                  f.asInstanceOf[Object],
                  context,
                  params.map(a => a.asInstanceOf[Object]),
                  paramsMap,
                  funcName
                )
              }
            }
            throw new RuntimeException(
              s"Can't find function `$funcName` in $l [${l.length}] params=$params"
            )
          case _ =>
            invokeFunc(
              func.asInstanceOf[Object],
              context,
              params.map(a => a.asInstanceOf[Object]),
              paramsMap,
              funcName
            )
        }
      case None =>
        unpack(
          context.currentEngine
            .executeFunc(funcName.toLowerCase, params.map(pack): _*)
        )
    }
  }

  private def compareFunctionTypes(a: Method, paramsSeq: Seq[_]): Boolean = {
    val params = paramsSeq.toArray
    if (a.getParameters.length != params.length) {
      if (
        a.getParameters.last.getType.isAssignableFrom(
          Class.forName("scala.collection.immutable.Seq")
        )
      ) return true
      else return false
    }

    var i = 0
    for (p <- a.getParameters) {
      if (!isTypeEquals(p.getType, params(i).getClass)) return false
      i += 1
    }

    true
  }

  private def isTypeEquals(left: Class[_], right: Class[_]): Boolean = {
    if (left.isPrimitive) {
      left.getName match {
        case "int" =>
          return right.getName == "java.lang.Integer"
        case "double" =>
          return right.getName == "java.lang.Double"
      }
    }
    right.isAssignableFrom(left)
  }

  private def invokeFunc(
    obj: Object,
    context: Context,
    params: Seq[Object] = Nil,
    paramsMap: Map[String, Object] = Map.empty,
    funcName: String
  ): Any = {
    if (obj.isInstanceOf[SqlLambda])
      return obj.asInstanceOf[SqlLambda].apply(params: _*)
    val a = obj.getClass.getMethods.find(_.getName == "apply")
    a match {
      case Some(apply) =>
        val pc = apply.getParameters.length
        if (pc == params.length)
          apply.invoke(obj, params: _*)
        else {
          val lb = ListBuffer(params.toIndexedSeq: _*)
          // add default values
          for (i <- params.length + 1 to pc) {
            val m = ru.runtimeMirror(this.getClass.getClassLoader)
            val cl = m.classSymbol(obj.getClass).toType
            val ap = cl.member(ru.TermName("apply")).asTerm.alternatives.head
            val p = ap.asMethod.paramLists.head(i - 1).asTerm.typeSignature

            val paramName = apply.getParameters.apply(i - 1).getName
            if (p == ru.typeOf[Context])
              lb += context
            else
              lb += paramsMap.getOrElse(paramName, getDefParamsFor(obj, i))
          }
          if (lb.length > pc) {
            val head = lb.take(pc - 1)
            val tail = lb.drop(pc - 1)
            val bundle = head ++ Seq(tail.toSeq)
            apply.invoke(obj, bundle.toArray: _*)
          } else
            apply.invoke(obj, lb.toArray: _*)
        }
      case None =>
        throw new RuntimeException(
          s"Can't find method `apply` in function $funcName"
        )
    }
  }

  private def getDefParamsFor(obj: Object, i: Int): Object = {
    val paramName = s"apply$$default$$$i"
    obj.getClass.getMethod(paramName).invoke(obj)
  }
}
