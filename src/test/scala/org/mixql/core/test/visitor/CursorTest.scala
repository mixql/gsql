package org.mixql.core.test.visitor

import org.mixql.core.context.gtype.{array, bool, gInt, nothing}
import org.mixql.core.test.MainVisitorBaseTest

class CursorTest extends MainVisitorBaseTest {
  test("Test cursor is array, fetch, open, close") {
    val code =
      """
        |let d_cursor = cursor is [TRUE, [TRUE, "gg", 12], 12]; --does not execute when defined,
        |-- only when open is triggered
        |
        |open d_cursor; --executes expression here
        |
        |let res1 = fetch d_cursor;
        |print("first element is $res1");
        |
        |let res2 = fetch d_cursor;
        |print("second element is $res2");
        |
        |let res3 = fetch d_cursor;
        |print("third element is $res3");
        |
        |let res4 = fetch d_cursor;
        |print("fourth element is $res4");
        |
        |let res5 = fetch d_cursor;
        |print("fifth element is $res5");
        |
        |close d_cursor;
        |
              """.stripMargin
    val context = runMainVisitor(code)

    val res1 = context.getVar("res1")
    assert(res1.isInstanceOf[bool])
    assert(res1.asInstanceOf[bool].getValue)

    val res2 = context.getVar("res2")
    assert(res2.isInstanceOf[array])
    assert(res2.asInstanceOf[array].toString == "[true, \"gg\", 12]")

    val res3 = context.getVar("res3")
    assert(res3.isInstanceOf[gInt])
    assert(res3.asInstanceOf[gInt].getValue == 12)

    val res4 = context.getVar("res4")
    assert(res4.isInstanceOf[nothing])
    val res5 = context.getVar("res5")
    assert(res5.isInstanceOf[nothing])
  }
}
