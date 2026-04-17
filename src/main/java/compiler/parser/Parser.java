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
        errors.add(new ParseError("SYNTAX ERROR: " +message + " at line " + line + ", column " + col, line, col));
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
        // check if current token matches expected type
        return peek().getTokenType().equals(tokenType);
    }

    private boolean checkLexeme(String lexeme) {
        // check if current lexeme matches for keyword or operator
        return peek().getLexeme().equals(lexeme);
    }

    private Token expectedToken(String tokenType, String errorMessage) {
        if (!checkToken(tokenType)) {
            reportError(errorMessage + tokenType + "', got '" + peek().getLexeme() + "'", peek().getLineNumber(), peek().getColumnNumber());
            if (!isEnd()) next();
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
        // check if matches then consume and return true, else return false
        if (checkLexeme(lexeme)) {
            next();
            return true;
        }
        return false;
    }

    private boolean isEnd(){
        // check if reached end of tokens
        return peek().getTokenType().equals("EOF");
    }

    // checks if the next thing is a declaration or a plain statement
    private ASTNode declOrStmt() {
        ArrayList<Token> modifiers = new ArrayList<>();
        while (isAccessModifier(peek())) {
            modifiers.add(next());
        }

        if (checkLexeme("class")) {
            if (context == ParseContext.TOP_LEVEL) {
                return classDecl(modifiers);
            } else {
                // Allow class declarations in other contexts if needed
                return classDecl(modifiers);
            }
        }

        if (isTypeToken(peek()) && isIdentifier(checkNext())) {
            return varOrMethodDecl(modifiers);
        }

        // modifier without declaration is error
        if (!modifiers.isEmpty()) {
            reportError("Expected declaration after modifier", peek().getLineNumber(), peek().getColumnNumber());
            return new ErrorNode(peek().getLineNumber(), peek().getColumnNumber());
        }

        return stmt();
    }

    private void recover() {
        next(); // consume bad token

        while (!isEnd() && !isRecoveryToken(peek())) {
            next();
        }
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

    private boolean isCompoundAssign() {
        String lex = peek().getLexeme();
        return lex.equals("+=") || lex.equals("-=") ||
              lex.equals("*=") || lex.equals("/=") || lex.equals("%=");
    }

    // check type of token
    private boolean isTypeToken(Token token) {
        String lexeme = token.getLexeme();
        return lexeme.equals("int")    || lexeme.equals("float")   || lexeme.equals("double") ||
              lexeme.equals("boolean")|| lexeme.equals("char")    || lexeme.equals("String") ||
              lexeme.equals("void")   || token.getTokenType().equals("Identifier"); // for user defined
    }

    // check if identifier
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

    private boolean isRecoveryToken(Token t) {
        String lex = t.getLexeme();

        return lex.equals(";") ||
              lex.equals("}") ||
              lex.equals("{") ||
              lex.equals("class") ||
              lex.equals("if") ||
              lex.equals("for") ||
              lex.equals("while") ||
              lex.equals("return");
    }


    // check if variable or method declaration
    private ASTNode varOrMethodDecl(ArrayList<Token> modifiers) {
        TypeNode type = type();
        Token name = next();

        if (checkLexeme("(")) {
            // Methods only allowed at class body or top level
            if (context == ParseContext.METHOD_BODY) {
                reportError("Method declarations not allowed inside a method", name.getLineNumber(), name.getColumnNumber());
            }
            return methodDecl(modifiers, type, name);
        }

        if (context == ParseContext.METHOD_BODY) {
            for (Token mod : modifiers) {
                if (mod.getLexeme().equals("static") || mod.getLexeme().equals("public") ||
                    mod.getLexeme().equals("private") || mod.getLexeme().equals("protected")) {
                    reportError("Modifier '" + mod.getLexeme() + "' not allowed on local variable", mod.getLineNumber(), mod.getColumnNumber());
                }
            }
        }

        ASTNode init = null;
        if (matchLexeme("=")) init = expression();
        expectedLexeme(";");
        return new VarDecl(modifiers, type, name, init);
    }

    private MethodDecl methodDecl(ArrayList<Token> modifiers, TypeNode returnType, Token name) {
        expectedLexeme("(");
        ArrayList<VarDecl> params = new ArrayList<>();

        if (!checkLexeme(")")) {
            while (true){
                TypeNode paramType = type();
                Token paramName = next();
                params.add(new VarDecl(new ArrayList<>(), paramType, paramName, null));
                if (!matchLexeme(",")) break;
            }
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
            do {
                interfaces.add(baseType());
            } while (matchLexeme(","));
        }

        expectedLexeme("{");
        ArrayList<ASTNode> members = new ArrayList<>();
        context = ParseContext.CLASS_BODY;
        while (!checkLexeme("}") && !isEnd()) {
            try {
                members.add(declOrStmt());
            } catch (ParseError e) {
                errors.add(e);
                recover();;
            }
        }
        context = ParseContext.TOP_LEVEL;

        expectedLexeme("}");
        return new ClassDecl(modifiers, name, superClass, interfaces, members);
    }

    // for statements
    private ASTNode stmt() {
        try {
            if (checkLexeme("{")) return block();
            if (checkLexeme("if")) return ifStmt();
            if (checkLexeme("while")) return whileStmt();
            if (checkLexeme("for")) return forStmt();
            if (checkLexeme("return")) return returnStmt();

            ASTNode expr = expression();

            if (expr instanceof ErrorNode) {
                recover();
                return new ErrorNode(expr.lineNumber, expr.columnNumber);
            }

            expectedLexeme(";");
            return new ExprStmt(expr, expr.lineNumber, expr.columnNumber);

        } catch (ParseError e) {
            errors.add(e);
            recover();
            return new ErrorNode(peek().getLineNumber(), peek().getColumnNumber());
        }
    }

    private BlockStmt block() {
        int line = peek().getLineNumber(), col = peek().getColumnNumber();
        expectedLexeme("{");
        ArrayList<ASTNode> stmts = new ArrayList<>();
        while (!checkLexeme("}") && !isEnd()) {
            try {
                ASTNode node = declOrStmt();
                if (node instanceof ErrorNode){
                    reportError("Invalid statement", node.lineNumber, node.columnNumber);
                } else {
                    stmts.add(node);
                }
            } catch (ParseError e) {
                errors.add(e);
                recover();
            }
        }
        expectedLexeme("}");
        return new BlockStmt(stmts, line, col);
    }

    private IfStmt ifStmt() {
        int line = peek().getLineNumber();
        int col = peek().getColumnNumber();
        expectedLexeme("if");
        expectedLexeme("(");

        ASTNode condition = expression();
        expectedLexeme(")");

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
        int col = peek().getColumnNumber();
        expectedLexeme("while");
        expectedLexeme("(");
        ASTNode condition = expression();
        expectedLexeme(")");
        ASTNode body = stmt();
        return new WhileStmt(condition, body, line, col);
    }

    private ForStmt forStmt() {
        int line = peek().getLineNumber();
        int col = peek().getColumnNumber();
        expectedLexeme("for");
        expectedLexeme("(");

        // init (var decl, expression, or empty)
        ASTNode init = null;
        if (!checkLexeme(";")) {
            // collect any modifiers (e.g. final int i = 0)
            ArrayList<Token> modifiers = new ArrayList<>();
            while (isAccessModifier(peek())) {
                modifiers.add(next());
            }

            if (isTypeToken(peek()) && isIdentifier(checkNext())) {
                init = varOrMethodDecl(modifiers); // already consumes the semicolon
            } else {
                // no modifiers expected before a plain expression
                if (!modifiers.isEmpty()) {
                  reportError("Unexpected modifier in for-init", peek().getLineNumber(), peek().getColumnNumber());
                }
                init = expression();
                expectedLexeme(";");
            }
        } else {
            next(); // consume the ';'
        }

        // condition (may be empty)
        ASTNode condition = null;
        if (!checkLexeme(";")) condition = expression();
        expectedLexeme(";");

        // update (may be empty) and allows i++, i--, i += 1, i = i + 1
        ASTNode update = null;
        if (!checkLexeme(")")) update = expression();
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

    // expression chain
    private ASTNode expression() {
        try {
          return assignment();
      } catch (Exception e) {
          reportError("Invalid expression", peek().getLineNumber(), peek().getColumnNumber());
          recoverExpression();
          return new ErrorNode(peek().getLineNumber(), peek().getColumnNumber());
      }
        
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
                return new AssignExp(left, value);  // store the whole lvalue, not just name
            }
            return new CompoundAssignExp(left, op, value);
        }

        

        return left;
    }

    private ASTNode ternary() {
        ASTNode condition = orStmt();
        if (checkLexeme("?")) {
            next(); // consume '?'
            ASTNode thenBranch = expression(); // full expression in the middle
            expectedLexeme(":");
            ASTNode elseBranch = ternary(); // right-associative
            return new TernaryExp(condition, thenBranch, elseBranch);
        }
        return condition;
      }

    private ASTNode orStmt() {
        ASTNode left = andStmt();
        while (checkLexeme("||")) {
            Token op = next();
            left = new BinaryExp(op, left, andStmt());
        }
        return left;
    }

    private ASTNode andStmt() {
        ASTNode left = equality();
        while (checkLexeme("&&")) {
            Token op = next();
            left = new BinaryExp(op, left, equality());
        }
        return left;
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
        // increment and decrement
        if (checkLexeme("++") || checkLexeme("--")) {
            Token op = next();
            ASTNode operand = unary();
            return new PreFixExp(op, operand);
        }
        if (checkLexeme("!") || checkLexeme("-")) {
            Token op = next();
            return new UnaryExp(op, unary()); // right recursive
        }
        return postFix();
    }

    // method call and member access
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
        // consume array dimensions if present
        int dims = 0;
        while (checkLexeme("[") && nextIs("]")) {
            next(); next(); // consume []
            dims++;
        }
        return new TypeNode(base.baseName, base.typeArgs, dims, base.lineNumber, base.columnNumber);
    }

    private TypeNode baseType() {
        int line = peek().getLineNumber(), col = peek().getColumnNumber();

        Token t = peek();

        if (!isTypeToken(t)) {
            reportError("Expected type name, got '" + t.getLexeme() + "'", t.getLineNumber(), t.getColumnNumber());
            return new TypeNode("error", new ArrayList<>(), 0, t.getLineNumber(), t.getColumnNumber());
        }

        next(); // consume the type

        StringBuilder name = new StringBuilder(t.getLexeme());
        while (checkLexeme(".") && checkNext().getTokenType().equals("Identifier")) {
            next();
            name.append(".").append(next().getLexeme());
        }

        ArrayList<TypeNode> typeArgs = new ArrayList<>();
  
        return new TypeNode(name.toString(), typeArgs, 0, line, col);
    }

    // helper: peek at token after next without consuming
    private boolean nextIs(String lexeme) {
        return current + 1 < tokens.size() && tokens.get(current + 1).getLexeme().equals(lexeme);
    }

    // bottom of chain
    private ASTNode primary() {
        Token t = peek();

        if (t.getTokenType().equals("Numeric Literal") ||
            t.getTokenType().equals("Float Literal")   ||
            t.getTokenType().equals("String Literal")  ||
            t.getTokenType().equals("Char Literal")    ||
            t.getTokenType().equals("Boolean Literal") ||
            t.getTokenType().equals("Special Literal")) {
            return new LiteralExp(next());
        }

        if (checkLexeme("{")) {
            int line = t.getLineNumber(), col = t.getColumnNumber();
            next();

            ArrayList<ASTNode> elements = new ArrayList<>();
            if (!checkLexeme("}")) {
                do {
                    elements.add(expression());
                } while (matchLexeme(","));
            }

            expectedLexeme("}");
            return new ArrInitExp(elements, line, col);
        }

        if (checkLexeme("new")) {
            next();
            TypeNode type = baseType();

            if (checkLexeme("[")) {
                next();

                if (checkLexeme("]")) {
                    next();
                    expectedLexeme("{");

                    ArrayList<ASTNode> elements = new ArrayList<>();
                    if (!checkLexeme("}")) {
                        do {
                            elements.add(expression());
                        } while (matchLexeme(","));
                    }

                    expectedLexeme("}");
                    return new NewArrExp(type, null, elements);
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

        // -------------------------
        // GROUPING OR CAST
        // -------------------------
        if (checkLexeme("(")) {
            next();
            ASTNode expr = expression();
            expectedLexeme(")");
            return expr;
        }

        if (checkLexeme("(") && isTypeToken(checkNext())) {
            int saved = current;

            next(); // (
            TypeNode tNode = type();

            if (checkLexeme(")")) {
                next();

                if (isCastFollower()) {
                    return new CastExp(tNode, unary());
                }
            }

            current = saved; // rollback
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