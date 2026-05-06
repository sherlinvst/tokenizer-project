package main.java.compiler.parser;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.type.TypeNode;
import main.java.compiler.parser.declaration.*;
import main.java.compiler.parser.error.ErrorNode;
import main.java.compiler.parser.statement.*;
import main.java.compiler.parser.expression.*;
import main.java.model.Token;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private enum ParseContext { TOP_LEVEL, CLASS_BODY, METHOD_BODY }
    private ParseContext context = ParseContext.TOP_LEVEL;
    private List<Token> tokens;
    private ArrayList<ParseError> errors = new ArrayList<>();
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    public ArrayList<ParseError> getErrors() { return errors; }

    private void reportError(String message, int line, int col) {
        errors.add(new ParseError("ERROR: " +message + " at line " + line + ", column " + col, line, col));
    }

    private static final Token EOF_TOKEN = new Token("EOF", "EOF", -1, -1);

    private Token peek() {
        if (current >= tokens.size()) return EOF_TOKEN;
        return tokens.get(current);
    }

    private Token checkNext() {
        int nextIndex = current + 1;
        if (nextIndex >= tokens.size()) return EOF_TOKEN;
        return tokens.get(nextIndex);
    }

    private Token next() {
        if (current >= tokens.size()) return EOF_TOKEN;
        return tokens.get(current++);
    }

    private boolean checkToken(String tokenType) {
        return peek().getTokenType().equals(tokenType);
    }

    private boolean checkLexeme(String lexeme) {
        return peek().getLexeme().equals(lexeme);
    }

    private Token expectedToken(String tokenType, String errorMessage) {
        if (!checkToken(tokenType)) {
            if (isEnd()) return EOF_TOKEN; 
            reportError(errorMessage + ", got '" + peek().getLexeme() + "'",
                        peek().getLineNumber(), peek().getColumnNumber());
            next();
            return EOF_TOKEN;
        }
        return next();
    }

    private Token expectedLexeme(String lexeme) {
        if (!checkLexeme(lexeme)) {

        if (isEnd()) return EOF_TOKEN;

        reportError("Expected '" + lexeme + "', got '" + peek().getLexeme() + "'",
                    peek().getLineNumber(), peek().getColumnNumber());

        next();
        return EOF_TOKEN;
        }
        return next();
    }

    private boolean matchLexeme (String lexeme) {
        if (checkLexeme(lexeme)) {
            next();
            return true;
        }
        return false;
    }

    private boolean isEnd(){
        return peek().getTokenType().equals("EOF");
    }

    private ASTNode declOrStmt() {
        ArrayList<Token> modifiers = new ArrayList<>();
        while (isAccessModifier(peek())) {
            modifiers.add(next());
        }

        if (modifiers.isEmpty() && isIdentifier(peek()) && !isEnd()) {
            Token suspect = peek();
            Token after   = checkNext();
            boolean looksLikeModifier =
                after.getLexeme().equals("class")       ||
                (isTypeToken(after) && !isStatementStarter()) ||
                isAccessModifier(after);

            if (looksLikeModifier) {
                reportError("Unknown modifier '" + suspect.getLexeme() + "'",
                            suspect.getLineNumber(), suspect.getColumnNumber());
                next();
                while (isAccessModifier(peek())) modifiers.add(next());
            }
        }

        if (checkLexeme("class")) return classDecl(modifiers);

        if (context == ParseContext.CLASS_BODY
                && isIdentifier(peek())
                && Character.isUpperCase(peek().getLexeme().charAt(0))
                && checkNext().getLexeme().equals("(")) {
            return constructorDecl(modifiers);
        }

        if (isTypeToken(peek()) && isDeclarationStart(checkNext())) {
            return varOrMethodDecl(modifiers);
        }

        if (!modifiers.isEmpty()) {
            reportError("Expected declaration after modifier",
                        peek().getLineNumber(), peek().getColumnNumber());
            recover();
            return new ErrorNode(peek().getLineNumber(), peek().getColumnNumber());
        }

        return stmt();
    }

    private ConstructorDecl constructorDecl(ArrayList<Token> modifiers) {
        Token name = next();
        expectedLexeme("(");

        ArrayList<VarDecl> params = new ArrayList<>();
        if (!checkLexeme(")")) {
            do {
                TypeNode pType = type();
                if (!isIdentifier(peek())) {
                    reportError("Expected parameter name", peek().getLineNumber(), peek().getColumnNumber());
                    break;
                }
                Token pName = next();
                params.add(new VarDecl(new ArrayList<>(), pType, pName, null));
            } while (matchLexeme(","));
        }
        expectedLexeme(")");

        ArrayList<TypeNode> throwsClause = new ArrayList<>();
        if (matchLexeme("throws")) {
            do { throwsClause.add(baseType()); } while (matchLexeme(","));
        }

        ParseContext saved = context;
        context = ParseContext.METHOD_BODY;
        BlockStmt body = block();
        context = saved;

        return new ConstructorDecl(modifiers, name, params, throwsClause, body);
    }

    private void recover() {
        while (!isEnd()) {
            String lex = peek().getLexeme();
            if (lex.equals(";"))  { next(); return; }
            if (lex.equals("}") || lex.equals("{"))   return;
            if (isStatementStarter() || isAccessModifier(peek())) return;
            next();
        }
    }

    private void recoverClassMember() {
        while (!isEnd()) {
            if (checkLexeme("}")) return;
            if (isAccessModifier(peek()) || isTypeToken(peek()) || checkLexeme("class")) return;
            if (checkLexeme(";")) { next(); return; }
            next();
        }
    }

    private boolean isStatementStarter() {
        String lex = peek().getLexeme();
        return lex.equals("if")     || lex.equals("while")  || lex.equals("for") ||
              lex.equals("return") || lex.equals("{")       || lex.equals("do") ||
              lex.equals("break")  || lex.equals("continue")|| lex.equals("try")||
              lex.equals("throw")  || lex.equals("switch");
    }

    private void recoverExpression() {
        while (!isEnd()) {
            String lex = peek().getLexeme();
            if (lex.equals(";") || lex.equals(")") || lex.equals("]") ||
                lex.equals(",") || lex.equals("}")) {
                break;
            }
            next();
        }
    }

    private boolean isDeclarationStart(Token token) {
        return token.getTokenType().equals("Identifier") ||
              token.getLexeme().equals("[");
    }

    private boolean isCompoundAssign() {
        String lex = peek().getLexeme();
        return lex.equals("+=") || lex.equals("-=") ||
              lex.equals("*=") || lex.equals("/=") || lex.equals("%=");
    }

    private boolean isTypeToken(Token token) {
        String lexeme = token.getLexeme();
        return lexeme.equals("int")    || lexeme.equals("float")   || lexeme.equals("double") ||
              lexeme.equals("boolean")|| lexeme.equals("char")    || lexeme.equals("String") ||
              lexeme.equals("void")   || token.getTokenType().equals("Identifier");
    }

    private boolean isIdentifier(Token token) {
        return token.getTokenType().equals("Identifier");
    }

    private boolean isAccessModifier(Token token) {
        String lexeme = token.getLexeme();
        return lexeme.equals("public")    || lexeme.equals("private") ||
              lexeme.equals("protected") || lexeme.equals("static")  ||
              lexeme.equals("final");
    }

    private boolean isCastFollower() {
        String lex = peek().getLexeme();
        String type = peek().getTokenType();
        return type.equals("Identifier")       ||
              type.equals("Numeric Literal")  ||
              type.equals("Boolean Literal")  ||
              type.equals("String Literal")   ||
              lex.equals("(")  || lex.equals("!")  ||
              lex.equals("-")  || lex.equals("++") ||
              lex.equals("--") || lex.equals("new");
    }

    private ASTNode varOrMethodDecl(ArrayList<Token> modifiers) {
        TypeNode type = type();

        if (!isIdentifier(peek())) {
            reportError("Expected variable or method name after type '" + type.baseName + "', got '" + peek().getLexeme() + "'",
                        peek().getLineNumber(), peek().getColumnNumber());
            recover();
            return new ErrorNode(peek().getLineNumber(), peek().getColumnNumber());
        }

        Token name = next();

        if (checkLexeme("(")) {
            if (context == ParseContext.METHOD_BODY) {
                reportError("Method declarations not allowed inside a method body",
                            name.getLineNumber(), name.getColumnNumber());
            }
            return methodDecl(modifiers, type, name);
        }

        if (context == ParseContext.METHOD_BODY) {
            for (Token mod : modifiers) {
                String lex = mod.getLexeme();
                if (lex.equals("static") || lex.equals("public") ||
                    lex.equals("private") || lex.equals("protected")) {
                    reportError("Modifier '" + lex + "' not allowed on local variable",
                                mod.getLineNumber(), mod.getColumnNumber());
                }
            }
        }

        ASTNode init = null;
        if (matchLexeme("=")) {
            init = checkLexeme("{") ? arrInitExp() : expression();
        }
        expectedLexeme(";");
        return new VarDecl(modifiers, type, name, init);
    }

    private MethodDecl methodDecl(ArrayList<Token> modifiers, TypeNode returnType, Token name) {
        expectedLexeme("(");
        ArrayList<VarDecl> params = new ArrayList<>();

        if (!checkLexeme(")")) {
            do {
                TypeNode paramType = type();

                if (!isIdentifier(peek())) {
                    reportError("Expected parameter name, got '" + peek().getLexeme() + "'",
                                peek().getLineNumber(), peek().getColumnNumber());
                    recover();
                    break;
                }

                Token paramName = next();
                params.add(new VarDecl(new ArrayList<>(), paramType, paramName, null));
            } while (matchLexeme(","));
        }

        expectedLexeme(")");

        ArrayList<TypeNode> throwsClause = new ArrayList<>();
        if (matchLexeme("throws")) {
            do {
                throwsClause.add(baseType());
            } while (matchLexeme(","));
        }

        ParseContext savedContext = context;
        context = ParseContext.METHOD_BODY;
        BlockStmt body = block();
        context = savedContext;
        return new MethodDecl(modifiers, returnType, name, throwsClause, params, body);
    }

    private ASTNode classDecl(ArrayList<Token> modifiers) {
      expectedLexeme("class");
      Token name = expectedToken("Identifier", "Expected class name");

      TypeNode superClass = null;
      if (matchLexeme("extends")) superClass = baseType();

      ArrayList<TypeNode> interfaces = new ArrayList<>();
      if (matchLexeme("implements")) {
          do { interfaces.add(baseType()); } while (matchLexeme(","));
      }

      int openLine = peek().getLineNumber();
      int openCol  = peek().getColumnNumber();
      expectedLexeme("{");

      ParseContext saved = context;
      context = ParseContext.CLASS_BODY;

      ArrayList<ASTNode> members = new ArrayList<>();
      while (!checkLexeme("}") && !isEnd()) {
          try {
              ASTNode member = declOrStmt();
              if (!(member instanceof ErrorNode)) {
                  members.add(member);
              }
          } catch (ParseError e) {
              errors.add(e);
              recoverClassMember();
          }
      }

      context = saved;

      if (isEnd()) {
          reportUnclosedBlock("class '" + name.getLexeme() + "'", openLine, openCol);
      } else {
          next();
      }

      return new ClassDecl(modifiers, name, superClass, interfaces, members);
  }

  private void expectClosingDelimiter(String delimiter, String context, int openLine, int openCol) {
      if (isEnd()) {
          reportError("Unclosed " + context +
                      " — missing '" + delimiter + "' (opened at line " +
                      openLine + ", column " + openCol + ")",
                      openLine, openCol);
          return;
      }
      if (!checkLexeme(delimiter)) {
          reportError("Expected '" + delimiter + "', got '" + peek().getLexeme() + "'",
                      peek().getLineNumber(), peek().getColumnNumber());
          while (!isEnd() && !checkLexeme(delimiter) &&
                !checkLexeme(";") && !checkLexeme("}")) {
              next();
          }
          matchLexeme(delimiter);
          return;
      }
      next();
  }

    private ASTNode stmt() {
        try {
            if (checkLexeme("{"))        return block();
            if (checkLexeme("if"))       return ifStmt();
            if (checkLexeme("while"))    return whileStmt();
            if (checkLexeme("for"))      return forStmt();
            if (checkLexeme("return"))   return returnStmt();
            if (checkLexeme("break"))    return breakStmt();
            if (checkLexeme("continue")) return continueStmt();
            if (checkLexeme("do"))       return doWhileStmt();
            if (checkLexeme("throw"))    return throwStmt();

            ASTNode expr = expression();
            if (expr instanceof ErrorNode) return expr;
            expectedLexeme(";");
            return new ExprStmt(expr, expr.lineNumber, expr.columnNumber);

        } catch (ParseError e) {
            errors.add(e);
            recover();
            return new ErrorNode(peek().getLineNumber(), peek().getColumnNumber());
        }
    }

    private BreakStmt breakStmt() {
        int line = peek().getLineNumber(), col = peek().getColumnNumber();
        expectedLexeme("break");
        Token label = isIdentifier(peek()) ? next() : null;
        expectedLexeme(";");
        return new BreakStmt(label, line, col);
    }

    private ContinueStmt continueStmt() {
        int line = peek().getLineNumber(), col = peek().getColumnNumber();
        expectedLexeme("continue");
        Token label = isIdentifier(peek()) ? next() : null;
        expectedLexeme(";");
        return new ContinueStmt(label, line, col);
    }

    private DoWhileStmt doWhileStmt() {
        int line = peek().getLineNumber(), col = peek().getColumnNumber();
        expectedLexeme("do");
        ASTNode body = stmt();
        expectedLexeme("while");
        expectedLexeme("(");
        ASTNode condition = expression();
        expectedLexeme(")");
        expectedLexeme(";");
        return new DoWhileStmt(body, condition, line, col);
    }

    private ThrowStmt throwStmt() {
        int line = peek().getLineNumber(), col = peek().getColumnNumber();
        expectedLexeme("throw");
        ASTNode value = expression();
        expectedLexeme(";");
        return new ThrowStmt(value, line, col);
    }

    private BlockStmt block() {
      int line = peek().getLineNumber();
      int col  = peek().getColumnNumber();
      expectedLexeme("{");

      ArrayList<ASTNode> stmts = new ArrayList<>();
      while (!checkLexeme("}") && !isEnd()) {
          try {
              ASTNode node = declOrStmt();
              if (!(node instanceof ErrorNode)) {
                  stmts.add(node);
              }
          } catch (ParseError e) {
              errors.add(e);
              recover();
          }
      }

      if (isEnd()) {
          reportUnclosedBlock("block", line, col);
      } else {
          next();
      }

      return new BlockStmt(stmts, line, col);
  }

    private void reportUnclosedBlock(String blockType, int openLine, int openCol) {
        reportError("Unclosed " + blockType +
                    " — missing '}' (opened at line " + openLine +
                    ", column " + openCol + ")",
                    openLine, openCol);
    }

    private IfStmt ifStmt() {
      int line = peek().getLineNumber();
      int col  = peek().getColumnNumber();
      expectedLexeme("if");

      int parenLine = peek().getLineNumber();
      int parenCol  = peek().getColumnNumber();
      expectedLexeme("(");

      ASTNode condition = expression();
      expectClosingDelimiter(")", "if condition", parenLine, parenCol);

      ASTNode thenBranch = stmt();
      ASTNode elseBranch = null;
      if (checkLexeme("else")) {
          next();
          elseBranch = stmt();
      }
      return new IfStmt(condition, thenBranch, elseBranch, line, col);
  }

    private WhileStmt whileStmt() {
      int line = peek().getLineNumber();
      int col  = peek().getColumnNumber();
      expectedLexeme("while");

      int parenLine = peek().getLineNumber();
      int parenCol  = peek().getColumnNumber();
      expectedLexeme("(");

      ASTNode condition = expression();
      expectClosingDelimiter(")", "while condition", parenLine, parenCol);

      ASTNode body = stmt();
      return new WhileStmt(condition, body, line, col);
  }

    private ForStmt forStmt() {
        int line = peek().getLineNumber();
        int col  = peek().getColumnNumber();
        expectedLexeme("for");
        expectedLexeme("(");

        ASTNode init = null;
        if (!checkLexeme(";")) {
            ArrayList<Token> modifiers = new ArrayList<>();
            while (isAccessModifier(peek())) modifiers.add(next());

            if (isTypeToken(peek()) && isDeclarationStart(checkNext())) {
                TypeNode varType = type();

                if (!isIdentifier(peek())) {
                    throw new ParseError(
                        "Expected variable name in for-init, got '" + peek().getLexeme() + "'",
                        peek().getLineNumber(), peek().getColumnNumber());
                }

                Token varName = next();

                if (checkLexeme("(")) {
                    throw new ParseError("Method declaration not allowed in for-init",
                        varName.getLineNumber(), varName.getColumnNumber());
                }

                ASTNode varInit = null;
                if (matchLexeme("=")) {
                    varInit = checkLexeme("{") ? arrInitExp() : expression();
                }

                expectedLexeme(";");
                init = new VarDecl(modifiers, varType, varName, varInit);

            } else {
                if (!modifiers.isEmpty()) {
                    throw new ParseError("Unexpected modifier in for-init",
                        peek().getLineNumber(), peek().getColumnNumber());
                }
                init = expression();
                expectedLexeme(";");
            }
        } else {
            next();
        }

        ASTNode condition = null;
        if (!checkLexeme(";")) {
            try {
                condition = expression();
            } catch (ParseError e) {
                errors.add(e);
                while (!isEnd() && !checkLexeme(";") && !checkLexeme(")")) next();
            }
        }

        if (!checkLexeme(";")) {
            reportError("Expected ';' after for-condition, got '" + peek().getLexeme() + "'",
                        peek().getLineNumber(), peek().getColumnNumber());
            while (!isEnd() && !checkLexeme(")") && !checkLexeme("{")) next();
        } else {
            next();
        }

        ASTNode update = null;
        if (!checkLexeme(")")) {
            try {
                update = expression();
            } catch (ParseError e) {
                errors.add(e);
                while (!isEnd() && !checkLexeme(")")) next();
            }
        }
        expectedLexeme(")");

        ASTNode body = stmt();
        return new ForStmt(init, condition, update, body, line, col);
    }

    private ReturnStmt returnStmt() {
        int line = peek().getLineNumber();
        int col = peek().getColumnNumber();
        expectedLexeme("return");
        ASTNode value = null;
        if (!checkLexeme(";")) value = expression();
        expectedLexeme(";");
        return new ReturnStmt(value, line, col);
    }

    private ASTNode expression() {
        try {
            return assignment();
        } catch (ParseError e) {
            errors.add(e);
            recoverExpression();
            return new ErrorNode(peek().getLineNumber(), peek().getColumnNumber());
        }
    }

    private ArrInitExp arrInitExp() {
        int line = peek().getLineNumber(), col = peek().getColumnNumber();
        expectedLexeme("{");
        ArrayList<ASTNode> elements = new ArrayList<>();
        if (!checkLexeme("}")) {
            do {
                if (checkLexeme("{")) {
                    elements.add(arrInitExp());
                } else {
                    elements.add(expression());
                }
            } while (matchLexeme(","));
            matchLexeme(",");
        }
        expectedLexeme("}");
        return new ArrInitExp(elements, line, col);
    }

    private ASTNode assignment() {
        ASTNode left = ternary();

        if (checkLexeme("=") || isCompoundAssign()) {
            Token op = next();
            ASTNode value = assignment();

            if (!(left instanceof IdentifierExp || left instanceof ArrAccessExp || left instanceof FieldAccessExp)) {
                reportError("Invalid assignment target", op.getLineNumber(), op.getColumnNumber());
            }

            if (op.getLexeme().equals("=")) {
                return new AssignExp(left, value);
            }
            return new CompoundAssignExp(left, op, value);
        }

        return left;
    }

    private ASTNode orExpr() {
        ASTNode left = andExpr();
        while (checkLexeme("||")) {
            Token op = next();
            left = new BinaryExp(op, left, andExpr());
        }
        return left;
    }

    private ASTNode andExpr() {
        ASTNode left = bitwiseOrExpr();
        while (checkLexeme("&&")) {
            Token op = next();
            left = new BinaryExp(op, left, bitwiseOrExpr());
        }
        return left;
    }

    private ASTNode bitwiseOrExpr() {
        ASTNode left = bitwiseXorExpr();
        while (checkLexeme("|")) {
            Token op = next();
            left = new BinaryExp(op, left, bitwiseXorExpr());
        }
        return left;
    }

    private ASTNode bitwiseAndExpr() {
        ASTNode left = equality();
        while (checkLexeme("&")) {
            Token op = next();
            left = new BinaryExp(op, left, equality());
        }
        return left;
    }

    private ASTNode bitwiseXorExpr() {
        ASTNode left = bitwiseAndExpr();
        while (checkLexeme("^")) {
            Token op = next();
            left = new BinaryExp(op, left, bitwiseAndExpr());
        }
        return left;
    }

    private ASTNode ternary() {
        ASTNode condition = orExpr();
        if (checkLexeme("?")) {
            ASTNode thenBranch = expression();
            expectedLexeme(":");
            ASTNode elseBranch = ternary();
            return new TernaryExp(condition, thenBranch, elseBranch);
        }
        return condition;
    }

    private ASTNode equality() {
        ASTNode left = comparison();
        while (checkLexeme("==") || checkLexeme("!=")) {
            Token op = next();
            left = new BinaryExp(op, left, comparison());
        }
        return left;
    }

    private ASTNode comparison() {
        ASTNode left = addition();
        while (checkLexeme("<") || checkLexeme(">") || checkLexeme("<=") || checkLexeme(">=")) {
            Token op = next();
            left = new BinaryExp(op, left, addition());
        }
        return left;
    }

    private ASTNode addition() {
        ASTNode left = multiplication();
        while (checkLexeme("+") || checkLexeme("-")) {
            Token op = next();
            left = new BinaryExp(op, left, multiplication());
        }
        return left;
    }

    private ASTNode multiplication() {
        ASTNode left = unary();
        while (checkLexeme("*") || checkLexeme("/") || checkLexeme("%")) {
            Token op = next();
            left = new BinaryExp(op, left, unary());
        }
        return left;
    }

    private ASTNode unary() {
        if (checkLexeme("++") || checkLexeme("--")) {
            Token op = next();
            ASTNode operand = unary();
            return new PreFixExp(op, operand);
        }
        if (checkLexeme("!") || checkLexeme("-")) {
            Token op = next();
            return new UnaryExp(op, unary());
        }
        return postFix();
    }

    private ASTNode postFix() {
        ASTNode expr = primary();

        while (checkLexeme("[") || checkLexeme("++") || checkLexeme("--")
               || checkLexeme(".")   || checkLexeme("(")) {
            if (checkLexeme("[")) {
                next();
                ASTNode index = expression();
                expectedLexeme("]");
                expr = new ArrAccessExp(expr, index);
            } else if (checkLexeme("++") || checkLexeme("--")) {
                Token op = next();
                expr = new PostFixExp(expr, op);
            } else if (checkLexeme(".")) {
                next();
                Token memberName = expectedToken("Identifier", "Expected method or field name after '.'");
                if (checkLexeme("(")) {
                    ArrayList<ASTNode> args = argList();
                    expr = new MethodCallExp(memberName, expr, args);
                } else {
                    expr = new FieldAccessExp(expr, memberName);
                }
            } else if (checkLexeme("(") && expr instanceof IdentifierExp) {
                ArrayList<ASTNode> args = argList();
                expr = new MethodCallExp(((IdentifierExp) expr).name, null, args);
            } else {
                break;
            }
        }

        return expr;
    }

    private ArrayList<ASTNode> argList() {
        expectedLexeme("(");
        ArrayList<ASTNode> args = new ArrayList<>();
        if (!checkLexeme(")")) {
            do {
                args.add(expression());
            } while (matchLexeme(","));
        }
        expectedLexeme(")");
        return args;
    }

    private TypeNode type() {
        TypeNode base = baseType();
        int dims = 0;
        while (checkLexeme("[") && tokenAt(1, "]")) {
            next();
            next();
            dims++;
        }
        if (dims == 0) return base;
        return new TypeNode(base.baseName, base.typeArgs, dims,
                            base.lineNumber, base.columnNumber);
    }

    private TypeNode baseType() {
        int line = peek().getLineNumber(), col = peek().getColumnNumber();
        Token t = peek();

        if (!isTypeToken(t)) {
            reportError("Expected type name, got '" + t.getLexeme() + "'",
                        t.getLineNumber(), t.getColumnNumber());
            return new TypeNode("error", new ArrayList<>(), 0,
                                t.getLineNumber(), t.getColumnNumber());
        }

        next();
        StringBuilder name = new StringBuilder(t.getLexeme());

        while (checkLexeme(".") && checkNext().getTokenType().equals("Identifier")) {
            next();
            name.append(".").append(next().getLexeme());
        }

        ArrayList<TypeNode> typeArgs = new ArrayList<>();
        if (checkLexeme("<")) {
            next();
            if (!checkLexeme(">")) {
                do {
                    if (checkLexeme("?")) {
                        int wLine = peek().getLineNumber(), wCol = peek().getColumnNumber();
                        next();
                        if (checkLexeme("extends") || checkLexeme("super")) {
                            String bound = next().getLexeme();
                            TypeNode boundType = baseType();
                            typeArgs.add(new TypeNode("? " + bound + " " + boundType.baseName,
                                                      new ArrayList<>(), 0, wLine, wCol));
                        } else {
                            typeArgs.add(new TypeNode("?", new ArrayList<>(), 0, wLine, wCol));
                        }
                    } else {
                        typeArgs.add(type());
                    }
                } while (matchLexeme(","));
            }

            if (checkLexeme(">>")) {
                Token gt = tokens.get(current);
                tokens.set(current,
                    new Token(">", "Operator", gt.getLineNumber(), gt.getColumnNumber()));
                tokens.add(current + 1,
                    new Token(">", "Operator", gt.getLineNumber(), gt.getColumnNumber() + 1));
            }
            expectedLexeme(">");
        }

        return new TypeNode(name.toString(), typeArgs, 0, line, col);
    }

    private boolean tokenAt(int offset, String lexeme) {
        int idx = current + offset;
        return idx < tokens.size() && tokens.get(idx).getLexeme().equals(lexeme);
    }

    private ASTNode primary() {
        Token t = peek();

        if (t.getTokenType().equals("Numeric Literal") ||
            t.getTokenType().equals("Float Literal")   ||
            t.getTokenType().equals("String Literal")  ||
            t.getTokenType().equals("Char Literal")    ||
            t.getTokenType().equals("Boolean Literal") ||
            t.getTokenType().equals("Special Literal")) {

                Token literalToken = next();

                if (literalToken.getTokenType().equals("Char Literal")) {
                    String raw = literalToken.getLexeme();

                    if (raw.length() >= 2) {
                        String value = raw.substring(1, raw.length() - 1);

                        literalToken = new Token(
                            value,
                            literalToken.getTokenType(),
                            literalToken.getLineNumber(),
                            literalToken.getColumnNumber()
                        );
                    }
                }

            return new LiteralExp(literalToken);
        }

        if (checkLexeme("{")) return arrInitExp();

        if (checkLexeme("new")) {
            next();
            TypeNode type = baseType();

            if (checkLexeme("[")) {
                next();

                if (checkLexeme("]")) {
                    next();
                    return new NewArrExp(type, arrInitExp(), null);
                } else {
                    ASTNode size = expression();
                    expectedLexeme("]");
                    return new NewArrExp(type, size, null);
                }
            } else {
                ArrayList<ASTNode> args = argList();
                return new NewObjExp(type, args);
            }
        }

        if (checkLexeme("this")) return new ThisExp(next());
        if (checkLexeme("super")) return new SuperExp(next());
        if (checkLexeme("null")) return new LiteralExp(next());

        if (t.getTokenType().equals("Identifier")) {
            return new IdentifierExp(next());
        }

        if (checkLexeme("(")) {
            int saved = current;
            int parenLine = peek().getLineNumber();
            int parenCol  = peek().getColumnNumber();
            next();

            if (isTypeToken(peek())) {
                try {
                    TypeNode castType = type();
                    if (checkLexeme(")")) {
                        next();
                        if (isCastFollower()) {
                            return new CastExp(castType, unary());
                        }
                    }
                } catch (ParseError ignored) {}
            }

            current = saved;
            next();

            ASTNode inner = expression();

            if (!checkLexeme(")")) {
                reportError("Unclosed '(' — expected ')', got '" + peek().getLexeme() + "'",
                            parenLine, parenCol);
                while (!isEnd() && !checkLexeme(")") &&
                      !checkLexeme(";") && !checkLexeme("}")) {
                    next();
                }
                matchLexeme(")");
                return inner;
            }

            next();
            return inner;
        }

        reportError("Expected expression", t.getLineNumber(), t.getColumnNumber());
        next();
        return new ErrorNode(t.getLineNumber(), t.getColumnNumber());
    }
    
    public ArrayList<ASTNode> parse() {
        ArrayList<ASTNode> astNodes = new ArrayList<>();
        while (!isEnd()) {
            try {
                ASTNode node = declOrStmt();
                if (!(node instanceof ErrorNode)) astNodes.add(node);
            } catch (ParseError e) {
                errors.add(e);
                recover();
            }
        }
        return astNodes;
    }
}