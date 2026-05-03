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
        // check if current token matches expected type
        return peek().getTokenType().equals(tokenType);
    }

    private boolean checkLexeme(String lexeme) {
        // check if current lexeme matches for keyword or operator
        return peek().getLexeme().equals(lexeme);
    }

    private Token expectedToken(String tokenType, String errorMessage) {
        if (!checkToken(tokenType)) {
            if (isEnd()) return EOF_TOKEN; // add this guard
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

        // Detect typo-ed modifier: lowercase identifier followed by 'class' or a type+identifier
        // e.g. "pubic class Foo" or "pubic int x"
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
                next(); // consume the bad modifier and continue
                // retry modifiers in case more follow
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
        Token name = next(); // consume class name
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
        // Do NOT consume if already at a safe boundary
        while (!isEnd()) {
            String lex = peek().getLexeme();
            if (lex.equals(";"))  { next(); return; } // consume semicolon, stop
            if (lex.equals("}") || lex.equals("{"))   return; // stop, do not consume
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

    // check if variable or method declaration
    private ASTNode varOrMethodDecl(ArrayList<Token> modifiers) {
        TypeNode type = type();

        // validate name is actually an identifier
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

                // validate param name
                if (!isIdentifier(peek())) {
                    reportError("Expected parameter name, got '" + peek().getLexeme() + "'",
                                peek().getLineNumber(), peek().getColumnNumber());
                    recover();
                    break;
                }

                Token paramName = next();
                params.add(new VarDecl(new ArrayList<>(), paramType, paramName, null));
            } while (matchLexeme(","));      // changed from while(true)+break to do-while
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

        expectedLexeme("{");

        ParseContext saved = context; // save whatever context we came from
        context = ParseContext.CLASS_BODY;

        ArrayList<ASTNode> members = new ArrayList<>();
        while (!checkLexeme("}") && !isEnd()) {
            try {
                int errorsBefore = errors.size();
                ASTNode member = declOrStmt();
                // only add if it parsed cleanly AND is not an ErrorNode
                if (!(member instanceof ErrorNode) && errors.size() == errorsBefore) {
                    members.add(member);
                } else if (!(member instanceof ErrorNode) && errors.size() > errorsBefore) {
                    // parsed with errors — still add so downstream analysis can try
                    // but flag it so semantic analyzer knows it may be incomplete
                    members.add(member);
                }
            } catch (ParseError e) {
                errors.add(e);
                recoverClassMember();
            }
        }

        context = saved; // restore — not hardcoded TOP_LEVEL
        expectedLexeme("}");
        return new ClassDecl(modifiers, name, superClass, interfaces, members);
    }
    // for statements
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
        int line = peek().getLineNumber(), col = peek().getColumnNumber();
        expectedLexeme("{");
        ArrayList<ASTNode> stmts = new ArrayList<>();
        while (!checkLexeme("}") && !isEnd()) {
            try {
                ASTNode node = declOrStmt();
                // ErrorNode already had its error recorded — just skip adding to tree
                if (!(node instanceof ErrorNode)) {
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
                init = new VarDecl(modifiers, varType, varName, varInit); // always assign — no if condition here

            } else {
                if (!modifiers.isEmpty()) {
                    throw new ParseError("Unexpected modifier in for-init",
                        peek().getLineNumber(), peek().getColumnNumber());
                }
                init = expression();
                expectedLexeme(";");
            }
        } else {
            next(); // consume ';'
        }

        // condition
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
            next(); // consume ';'
        }

        // update
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

    // expression chain
    private ASTNode expression() {
        try {
            return assignment();
        } catch (ParseError e) { // only catch known parse errors, not all exceptions
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
                // nested initializer for multi-dim arrays
                if (checkLexeme("{")) {
                    elements.add(arrInitExp());
                } else {
                    elements.add(expression());
                }
            } while (matchLexeme(","));
            matchLexeme(","); // trailing comma allowed
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
                return new AssignExp(left, value);  // store the whole lvalue, not just name
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
        while (checkLexeme("|")) { // tokenizer makes || a separate token, so this is safe
            Token op = next();
            left = new BinaryExp(op, left, bitwiseXorExpr());
        }
        return left;
    }

    private ASTNode bitwiseAndExpr() {
        ASTNode left = equality();
        while (checkLexeme("&")) { // same — && is a separate token
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
        ASTNode condition = orExpr(); // correct
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
        int dims = 0;
        // current is now pointing at whatever follows the base type
        // for "int[] arr", current points at '[', current+1 points at ']'
        while (checkLexeme("[") && tokenAt(1, "]")) {
            next(); // consume '['
            next(); // consume ']'
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

        next(); // consume base type name
        StringBuilder name = new StringBuilder(t.getLexeme());

        // qualified name: java.util.List
        while (checkLexeme(".") && checkNext().getTokenType().equals("Identifier")) {
            next();
            name.append(".").append(next().getLexeme());
        }

        // generic type arguments: <String, Integer>
        ArrayList<TypeNode> typeArgs = new ArrayList<>();
        if (checkLexeme("<")) {
            next(); // consume '<'
            if (!checkLexeme(">")) {
                do {
                    if (checkLexeme("?")) {
                        // wildcard: ?, ? extends T, ? super T
                        int wLine = peek().getLineNumber(), wCol = peek().getColumnNumber();
                        next(); // consume '?'
                        if (checkLexeme("extends") || checkLexeme("super")) {
                            String bound = next().getLexeme();
                            TypeNode boundType = baseType();
                            typeArgs.add(new TypeNode("? " + bound + " " + boundType.baseName,
                                                      new ArrayList<>(), 0, wLine, wCol));
                        } else {
                            typeArgs.add(new TypeNode("?", new ArrayList<>(), 0, wLine, wCol));
                        }
                    } else {
                        typeArgs.add(type()); // recursive for nested generics
                    }
                } while (matchLexeme(","));
            }

            // handle ">>" split for nested generics like Map<String, List<Integer>>
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

    // when called from type(), current is pointing at '[', so current+1 is ']'
    private boolean tokenAt(int offset, String lexeme) {
        int idx = current + offset;
        return idx < tokens.size() && tokens.get(idx).getLexeme().equals(lexeme);
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

        if (checkLexeme("{")) return arrInitExp();

        if (checkLexeme("new")) {
            next();
            TypeNode type = baseType();

            if (checkLexeme("[")) {
                next();

                if (checkLexeme("]")) {
                    next(); // consume ']'
                    return new NewArrExp(type, arrInitExp(), null); // delegate
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
            int saved = current;
            int parenLine = peek().getLineNumber();
            int parenCol  = peek().getColumnNumber();
            next(); // consume '('

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

            // Backtrack and parse as grouped expression
            current = saved;
            next(); // consume '(' again

            ASTNode inner = expression();

            // If ')' is missing, report once and do NOT cascade
            if (!checkLexeme(")")) {
                reportError("Unclosed '(' — expected ')', got '" + peek().getLexeme() + "'",
                            parenLine, parenCol); // report at the OPENING paren position
                // skip to a safe recovery point without consuming block tokens
                while (!isEnd() && !checkLexeme(")") &&
                      !checkLexeme(";") && !checkLexeme("}")) {
                    next();
                }
                matchLexeme(")"); // consume ')' if we found one
                return inner;     // return what we have
            }

            next(); // consume ')'
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