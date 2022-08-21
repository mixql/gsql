package org.grenki.gsql.test

import org.scalatest.funsuite.AnyFunSuite
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.grenki.gsql.{sqlLexer, sqlParser}
import collection.JavaConverters._

class ParserTest extends AnyFunSuite {
  
  test("Test `some code to run;` is any_coma rule") {
    val code = """
                |some code to run;
                """.stripMargin
    val lexer = new sqlLexer(CharStreams.fromString(code))
    val parser = new sqlParser(new CommonTokenStream(lexer))
    val stmts = parser.program().statment
    assert(stmts.size == 1)
    val any_coma = stmts.get(0).any_comma
    assert(any_coma != null)
    val any_coma_text = any_coma.getText 
    assert(any_coma_text == "somecodetorun;")
  }

  test("Test parsing `assigment` statment") {
    val code = """
                |set x = 10;
                |set y = $x + 12;
                """.stripMargin
    val lexer = new sqlLexer(CharStreams.fromString(code))
    val parser = new sqlParser(new CommonTokenStream(lexer))
    val stmts = parser.program().statment()
    assert(stmts.size == 2)
    val assign_stmt1 = stmts.get(0).assigment_stmt
    assert(assign_stmt1 != null)
    assert(assign_stmt1.expr != null)
    val assign_stmt2 = stmts.get(1).assigment_stmt
    assert(assign_stmt2 != null)
    assert(assign_stmt2.expr != null)
  }  

  test("Test parsing `if` statment") {
    val code = """
                |if $a < 11 then
                |  some code to run 1;
                |ELSIF $a > 12 then
                |  some code to run 2;
                |else
                |  some code to run 3;
                |end if
                """.stripMargin
    val lexer = new sqlLexer(CharStreams.fromString(code))
    val parser = new sqlParser(new CommonTokenStream(lexer))
    val stmts = parser.program().statment
    assert(stmts.size == 1)
    val if_stmt = stmts.get(0).if_stmt
    assert(if_stmt != null)
    assert(if_stmt.block != null)
    assert(if_stmt.elseif_block != null)
    assert(if_stmt.elseif_block.size == 1)
    assert(if_stmt.else_block != null)
  }

  test("Test parsing `while` statment") {
    val code = """
                |while $x < 5 do
                |  print($x);
                |  set x = $x + 1;
                |end
                """.stripMargin
    val lexer = new sqlLexer(CharStreams.fromString(code))
    val parser = new sqlParser(new CommonTokenStream(lexer))
    val stmts = parser.program().statment
    assert(stmts.size == 1)
    val while_stmt = stmts.get(0).while_stmt
    assert(while_stmt != null)
    assert(while_stmt.expr != null)
    assert(while_stmt.block != null)
  }

  test("Test parsing `for range` statment") {
    val code = """
                |for i in REVERSE 1..20 step 2 loop
                |  print($i);
                |end loop
                """.stripMargin
    val lexer = new sqlLexer(CharStreams.fromString(code))
    val parser = new sqlParser(new CommonTokenStream(lexer))
    val stmts = parser.program().statment
    assert(stmts.size == 1)
    val for_stmt = stmts.get(0).for_range_stmt
    assert(for_stmt != null)
    assert(for_stmt.from != null)
    assert(for_stmt.to != null)
    assert(for_stmt.step != null)
    assert(for_stmt.ident != null)
    assert(for_stmt.block != null)
  }

  test("Test parsing `expression` statment") {
    {
      val code = """
                  |$a;
                  |null;
                  |'string';
                  |10;
                  |9.9;
                  |true;
                  |false;
                  |current_date;
                  |current_timestamp;
                  """.stripMargin
      val lexer = new sqlLexer(CharStreams.fromString(code))
      val parser = new sqlParser(new CommonTokenStream(lexer))
      val stmts = parser.program().statment
      assert(stmts.size == 9)
      var expr_stmt = stmts.get(0).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_varContext])
      
      expr_stmt = stmts.get(1).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_literalContext])
      assert(expr_stmt.asInstanceOf[sqlParser.Expr_literalContext].literal.isInstanceOf[sqlParser.Literal_nullContext])

      expr_stmt = stmts.get(2).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_literalContext])
      assert(expr_stmt.asInstanceOf[sqlParser.Expr_literalContext].literal.isInstanceOf[sqlParser.Literal_stringContext])

      expr_stmt = stmts.get(3).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_literalContext])
      assert(expr_stmt.asInstanceOf[sqlParser.Expr_literalContext].literal.isInstanceOf[sqlParser.Literal_intContext])

      expr_stmt = stmts.get(4).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_literalContext])
      assert(expr_stmt.asInstanceOf[sqlParser.Expr_literalContext].literal.isInstanceOf[sqlParser.Literal_doubleContext])

      expr_stmt = stmts.get(5).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_literalContext])
      assert(expr_stmt.asInstanceOf[sqlParser.Expr_literalContext].literal.isInstanceOf[sqlParser.Literal_boolContext])

      expr_stmt = stmts.get(6).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_literalContext])
      assert(expr_stmt.asInstanceOf[sqlParser.Expr_literalContext].literal.isInstanceOf[sqlParser.Literal_boolContext])

      expr_stmt = stmts.get(7).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_literalContext])
      assert(expr_stmt.asInstanceOf[sqlParser.Expr_literalContext].literal.isInstanceOf[sqlParser.Literal_current_dateContext])

      expr_stmt = stmts.get(8).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_literalContext])
      assert(expr_stmt.asInstanceOf[sqlParser.Expr_literalContext].literal.isInstanceOf[sqlParser.Literal_current_timestampContext])
    }

    {
      val code = """
                  |$a * 10;
                  |$a / 10;
                  |$a + 10;
                  |$a - 10;
                  """.stripMargin
      val lexer = new sqlLexer(CharStreams.fromString(code))
      val parser = new sqlParser(new CommonTokenStream(lexer))
      val stmts = parser.program().statment
      assert(stmts.size == 4)
      var expr_stmt = stmts.get(0).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_arithmetic_p1Context])
      assert(expr_stmt.asInstanceOf[sqlParser.Expr_arithmetic_p1Context].T_MUL != null)
      
      expr_stmt = stmts.get(1).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_arithmetic_p1Context])
      assert(expr_stmt.asInstanceOf[sqlParser.Expr_arithmetic_p1Context].T_DIV != null)

      expr_stmt = stmts.get(2).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_arithmetic_p2Context])
      assert(expr_stmt.asInstanceOf[sqlParser.Expr_arithmetic_p2Context].T_ADD != null)

      expr_stmt = stmts.get(3).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_arithmetic_p2Context])
      assert(expr_stmt.asInstanceOf[sqlParser.Expr_arithmetic_p2Context].T_SUB != null)
    }

    {
      val code = """
                  |$a > 10;
                  |$a < 10;
                  |$a >= 10;
                  |$a <= 10;
                  |$a and true;
                  |$a or true;
                  |not $a;
                  """.stripMargin
      val lexer = new sqlLexer(CharStreams.fromString(code))
      val parser = new sqlParser(new CommonTokenStream(lexer))
      val stmts = parser.program().statment
      assert(stmts.size == 7)
      var expr_stmt = stmts.get(0).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_compareContext])
      
      expr_stmt = stmts.get(1).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_compareContext])

      expr_stmt = stmts.get(2).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_compareContext])

      expr_stmt = stmts.get(3).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_compareContext])

      expr_stmt = stmts.get(4).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_logicalContext])

      expr_stmt = stmts.get(5).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_logicalContext])

      expr_stmt = stmts.get(6).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_notContext])
    }

    {
      val code = """
                  |$a > 10;
                  |$a < 10;
                  |$a >= 10;
                  |$a <= 10;
                  |$a and true;
                  |$a or true;
                  |not $a;
                  |$a || "some str";
                  """.stripMargin
      val lexer = new sqlLexer(CharStreams.fromString(code))
      val parser = new sqlParser(new CommonTokenStream(lexer))
      val stmts = parser.program().statment
      assert(stmts.size == 8)
      var expr_stmt = stmts.get(0).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_compareContext])
      
      expr_stmt = stmts.get(1).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_compareContext])

      expr_stmt = stmts.get(2).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_compareContext])

      expr_stmt = stmts.get(3).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_compareContext])

      expr_stmt = stmts.get(4).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_logicalContext])

      expr_stmt = stmts.get(5).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_logicalContext])

      expr_stmt = stmts.get(6).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_notContext])

      expr_stmt = stmts.get(7).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_concatContext])
    }

    {
      val code = """
                  |case when 1 > 2 then 12 when 1 < 2 then 13 else '12g' end;
                  """.stripMargin
      val lexer = new sqlLexer(CharStreams.fromString(code))
      val parser = new sqlParser(new CommonTokenStream(lexer))
      val stmts = parser.program().statment
      assert(stmts.size == 1)
      var expr_stmt = stmts.get(0).expr
      assert(expr_stmt != null)
      assert(expr_stmt.isInstanceOf[sqlParser.Expr_caseContext])
      val case_stmt = expr_stmt.asInstanceOf[sqlParser.Expr_caseContext].case_r
      assert(case_stmt.case_when_then != null)
      assert(case_stmt.case_when_then.size == 2)
      assert(case_stmt.ex_else != null)
    }
  }
}