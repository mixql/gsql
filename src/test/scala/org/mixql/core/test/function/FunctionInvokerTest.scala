package org.mixql.core.test.function

import org.mixql.core.function.FunctionInvoker
import org.scalatest.Ignore
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.Seq

class FunctionInvokerTest extends AnyFunSuite {

  val length: Any = new (String => Int) {
    def apply(str: String): Int = str.length
  }

  val defArgFunc = new Object {
    def apply(str: String = "abc"): String = str
  }

  val variableArgFunc = new Object {
    def apply(strings: String*): Int = strings.length
  }

  val firstDefArgAndSecondVariableArgFunc = new Object {
    def apply(str: String, ints: Int*): String = str + ints.toList.sum.toString
  }

  val firstAndSecondDefArgAndThirdVariableArgFunc = new Object {
    def apply(str1: String, str2: String, ints: Int*): String =
      str1 + str2 + ints.toList.sum.toString
  }

  val twoDefaultArgFunc = new Object {
    def apply(a: String = "abc", b: String = "1234"): String = {
      a + b
    }
  }

  val defaultSecondArgFunc = new Object {
    def apply(a: String, b: String = "1234"): String = {
      a + b
    }
  }

  val substr = new ((String, Int, Int) => String) {
    def apply(str: String, pos: Int, len: Int = Int.MaxValue): String = {
      if len == Int.MaxValue then
        str.substring(pos)
      else
        str.substring(pos, len)
    }
  }

  val decInt = new (Int => Int) {
    def apply(num: Int): Int = num - 1
  }

  val decListOfInt = new Object {
    def apply(ints: Int*): String = {
      ints.map(_ - 1).mkString(" ")
    }
  }

  val decListOfString = new Object {
    def apply(str: String*): String = {
      str.map(_.dropRight(1)).mkString(" ")
    }
  }

  val decListOfListOfString = new Object {
    def apply(str: Seq[String]*): String = {
      str.flatMap(x => x.map(y => y.dropRight(1))).mkString(" ")
    }
  }

  val decSting = new (String => String) {
    def apply(str: String): String = str.dropRight(1)
  }

  val decDouble = new (Double => Double) {
    def apply(f: Double): Double = f - 1
  }

  val functions: Map[String, Any] = Map.apply(
    "length" -> length,
    "def_arg_func" -> defArgFunc,
    "two_def_arg_func" -> twoDefaultArgFunc,
    "default_second_arg_func" -> defaultSecondArgFunc,
    "substr" -> substr,
    "variable_number_of_args" -> variableArgFunc,
    "first_def_arg_and_second_variable_args" -> firstDefArgAndSecondVariableArgFunc,
    "first_and_second_def_arg_and_third_variable_args" -> firstAndSecondDefArgAndThirdVariableArgFunc,
    "dec" -> List(decInt, decListOfInt, decListOfString, decSting, decDouble)
  )

  test("Invoke anonymous function") {
    val res = FunctionInvoker.invoke(functions, "length", List("123"))
    assert(res == 3)
  }

  test("Invoke default argument function") {
    val res = FunctionInvoker.invoke(functions, "def_arg_func", Nil)
    assert(res == "abc")
  }

  test("Invoke two default argument function") {
    val res = FunctionInvoker.invoke(functions, "two_def_arg_func", Nil)
    assert(res == "abc1234")
  }

  test("Invoke function with default second argument") {
    val res =
      FunctionInvoker.invoke(functions, "default_second_arg_func", List("abc"))
    assert(res == "abc1234")
  }

  test("Invoke function with arguments was passed by name") {
    val res = FunctionInvoker.invoke(
      functions,
      "two_def_arg_func",
      Nil,
      Map("a" -> "qw", "b" -> "erty")
    )

    assert(res == "qwerty")
  }

  test("substr") {
    val res = FunctionInvoker.invoke(functions, "substr", List("123545", 3, 5))

    assert(res == "123545".substring(3, 5))
  }

  test("substr2") {
    val res = FunctionInvoker.invoke(functions, "substr", List("123545", 3))

    assert(res == "123545".substring(3))
  }

  test("variable_number_of_args") {
    val res = FunctionInvoker.invoke(
      functions,
      "variable_number_of_args",
      List("1", "2", "3", "4")
    )

    assert(res == 4)
  }

  test("first_def_arg_and_second_variable_args") {
    val res = FunctionInvoker.invoke(
      functions,
      "first_def_arg_and_second_variable_args",
      List("ABC", 1, 2, 3)
    )

    assert(res == "ABC6")
  }

  test("first_and_second_def_arg_and_third_variable_args") {
    val res = FunctionInvoker.invoke(
      functions,
      "first_and_second_def_arg_and_third_variable_args",
      List("ABC", "DE", 1, 2, 3, 4)
    )

    assert(res == "ABCDE10")
  }

  test("Invoke undefined function") {
    val thrown = intercept[RuntimeException] {
      FunctionInvoker.invoke(functions, "foo", List("bar"))
    }

    assert(thrown.getMessage == "Can't find function `foo`")
  }

  test("Invoke overloading function[Int]") {
    val res = FunctionInvoker.invoke(functions, "dec", List(5))
    assert(res == 4)
  }

  test("Invoke overloading function[String]") {
    val res =
      FunctionInvoker.invoke(functions, "dec", List("abc")).asInstanceOf[String]

    assert(res == "ab")
  }

  test("Invoke overloading function[Double]") {
    val res =
      FunctionInvoker.invoke(functions, "dec", List(1.1)).asInstanceOf[Double]

    assert((res - 0.1) < 0.0000000001)
  }

  test("Invoke overloading function[List[Int]]") {
    val res =
      FunctionInvoker
        .invoke(functions, "dec", List(1, 2, 3))
        .asInstanceOf[String]

    assert(res == "0 1 2")
  }

  ignore("Invoke overloading function[List[String]]") {
    val res =
      FunctionInvoker
        .invoke(functions, "dec", List("ab", "cd", "ef"))
        .asInstanceOf[String]

    assert(res == "a c e")
  }

  ignore("Invoke overloading function[List[List[String]]]") {
    val res =
      FunctionInvoker
        .invoke(functions, "dec", List(List("ab", "cd"), List("ef")))
        .asInstanceOf[String]

    assert(res == "a c e")
  }
}
