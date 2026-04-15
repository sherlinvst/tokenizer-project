package main.java.compiler.parser;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.declaration.*;
import main.java.compiler.parser.statement.*;
import main.java.compiler.parser.expression.*;
import main.java.model.Token;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() {
        // check current token
        return tokens.get(current);
    }

    private Token checkNext(){
        // check next token without consuming
        if (current + 1 < tokens.size()) return tokens.get(current + 1);
        return tokens.get(tokens.size() - 1);// no more tokens
    }

    private Token next () {
        // consume current token and move to next one
        Token token = tokens.get(current);
        current++;
        return token;
    }

    private boolean checkToken(String tokenType) {
        // check if current token matches expected type
        return peek().getTokenType().equals(tokenType);
    }

    private boolean checkLexeme(String lexeme) {
        // check if current lexeme matches for keyword or operator
        return peek().getLexeme().equals(lexeme);
    }

    private Token expectedToken (String tokenType, String errorMessage){
        // check if matches then throw error if not
        if (!checkToken(tokenType)) {
            throw new ParseError(errorMessage + ", got '" + peek().getLexeme() + "'", peek().getLineNumber(), peek().getColumnNumber());
        }
        return next();
    }

    private Token expectedLexeme (String lexeme){
        // check if matches then throw error if not
        if (!checkLexeme(lexeme)) {
            throw new ParseError("Expected '" + lexeme + "', got '" + peek().getLexeme() + "'", peek().getLineNumber(), peek().getColumnNumber());
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
        // for class declaration
        if (checkLexeme("class")) return classDecl();

        // check for method or variable declaration
        if (isTypeToken(peek()) && isIdentifier(checkNext())) {
            return varOrMethodDecl();
        }

        return stmt();
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

    // check if variable or method declaration
    private ASTNode varOrMethodDecl() {
        Token type = next();   // consume type
        Token name = next();   // cnnsume identifier

        // if next is '(' then method
        if (checkLexeme("(")) return methodDecl(type, name);

        // else var
        ASTNode init = null;
        if (matchLexeme("=")) {
            init = expression();
        }
        expectedLexeme(";");
        return new VarDecl(type, name, init);
    }

    private MethodDecl methodDecl(Token returnType, Token name) {
        expectedLexeme("(");
        ArrayList<VarDecl> params = new ArrayList<>();

        if (!checkLexeme(")")) {
            while (true){
                Token paramType = next();
                Token paramName = next();
                params.add(new VarDecl(paramType, paramName, null));
                if (!matchLexeme(",")) break;
            }
        }

        expectedLexeme(")");
        BlockStmt body = block();
        return new MethodDecl(returnType, name, params, body);
    }

    private ClassDecl classDecl() {
        expectedLexeme("class");
        Token name = expectedToken("Identifier", "Expected class name");
        expectedLexeme("{");

        ArrayList<ASTNode> members = new ArrayList<>();
        while (!checkLexeme("}") && !isEnd()) {
            members.add(declOrStmt());
        }

        expectedLexeme("}");
        return new ClassDecl(name, members);
    }

    // for statements
    private ASTNode stmt() {
        if (checkLexeme("{"))      return block();
        if (checkLexeme("if"))     return ifStmt();
        if (checkLexeme("while"))  return whileStmt();
        if (checkLexeme("for"))    return forStmt();
        if (checkLexeme("return")) return returnStmt();

        // else expression statement
        ASTNode expr = expression();
        expectedLexeme(";");
        return new ExprStmt(expr, expr.lineNumber, expr.columnNumber);
    }

    private BlockStmt block() {
        int line = peek().getLineNumber();
        int col = peek().getColumnNumber();

        expectedLexeme("{");
        ArrayList<ASTNode> stmts = new ArrayList<>();
        while (!checkLexeme("}") && !isEnd()) {
            stmts.add(declOrStmt());
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
            if (isTypeToken(peek()) && isIdentifier(checkNext())) {
                init = varOrMethodDecl(); // already consumes the semicolon
            } else {
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

        // update (may be empty)
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
        return assignment();
    }

    private ASTNode assignment() {
        ASTNode left = orStmt();

        if (checkLexeme("=")) {
            Token equals = next();
            ASTNode value = assignment(); // right-associative: recurse on itself
            if (left instanceof IdentifierExp) {
                return new AssignExp(((IdentifierExp) left).name, value);
            }
            throw new ParseError("Invalid assignment target", equals.getLineNumber(), equals.getColumnNumber());
        }

        return left;
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
        if (checkLexeme("!") || checkLexeme("-")) {
            Token op = next();
            return new UnaryExp(op, unary()); // right recursive
        }
        return postFix();
    }

    // method call and member access
    private ASTNode postFix() {
        ASTNode expr = primary();

        while (checkLexeme(".") || checkLexeme("(")) {
            if (checkLexeme(".")) {
                next(); // consume '.'
                Token memberName = expectedToken("Identifier", "Expected method or field name after '.'");
                if (checkLexeme("(")) {
                    ArrayList<ASTNode> args = argList();
                    expr = new MethodCallExp(memberName, expr, args);
                } else {
                    expr = new IdentifierExp(memberName); // field access (simplified)
                }
            } else if (checkLexeme("(") && expr instanceof IdentifierExp) {
                // plain function call: foo(...)
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

    // bottom of chain
    private ASTNode primary() {
        Token t = peek();

        // Literals
        if (t.getTokenType().equals("Numeric Literal") ||
            t.getTokenType().equals("Float Literal")   ||
            t.getTokenType().equals("String Literal")  ||
            t.getTokenType().equals("Char Literal")    ||
            t.getTokenType().equals("Boolean Literal") ||
            t.getTokenType().equals("Special Literal")) {
            return new LiteralExp(next());
        }

        // Identifier (variable reference)
        if (t.getTokenType().equals("Identifier")) {
            return new IdentifierExp(next());
        }

        // Grouped expression: (expr)
        if (t.getLexeme().equals("(")) {
            next();
            ASTNode inner = expression();
            expectedLexeme(")");
            return inner;
        }

        throw new ParseError("Unexpected token '" + t.getLexeme() + "'", t.getLineNumber(), t.getColumnNumber());
    }
    
    public ArrayList<ASTNode> parse(){
        ArrayList<ASTNode> astNodes = new ArrayList<>();

        while (!isEnd()) {
            astNodes.add(declOrStmt());
        }

        return astNodes;
    }
}
