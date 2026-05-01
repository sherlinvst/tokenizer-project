package main.java.compiler;

import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.declaration.*;
import main.java.compiler.parser.expression.*;
import main.java.compiler.parser.statement.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Interpreter — AST Walker
 * Executes the logic defined in the AST nodes directly.
 */
public class Interpreter {

    // Stack of scopes for variables (maps name to current value)
    private final Stack<Map<String, Object>> scopes = new Stack<>();

    public Interpreter() {
        // Global scope
        scopes.push(new HashMap<>());
    }

    public void interpret(List<ASTNode> nodes) {
        for (ASTNode node : nodes) {
            execute(node);
        }
    }

    private Object execute(ASTNode node) {
        if (node == null) return null;

        if (node instanceof ClassDecl) {
            for (ASTNode member : ((ClassDecl) node).members) {
                if (member instanceof MethodDecl && ((MethodDecl) member).name.getLexeme().equals("main")) {
                    execute(((MethodDecl) member).body);
                }
            }
        } else if (node instanceof BlockStmt) {
            enterScope();
            for (ASTNode stmt : ((BlockStmt) node).statements) {
                execute(stmt);
            }
            exitScope();
        } else if (node instanceof VarDecl) {
            VarDecl decl = (VarDecl) node;
            Object val = (decl.init != null) ? evaluate(decl.init) : null;
            scopes.peek().put(decl.name.getLexeme(), val);
        } else if (node instanceof ExprStmt) {
            evaluate(((ExprStmt) node).exp);
        } else if (node instanceof IfStmt) {
            IfStmt stmt = (IfStmt) node;
            if ((Boolean) evaluate(stmt.condition)) {
                execute(stmt.thenBranch);
            } else if (stmt.elseBranch != null) {
                execute(stmt.elseBranch);
            }
        } else if (node instanceof ForStmt) {
            ForStmt stmt = (ForStmt) node;
            enterScope();
            execute(stmt.init);
            while ((Boolean) evaluate(stmt.condition)) {
                execute(stmt.body);
                evaluate(stmt.update);
            }
            exitScope();
        }
        return null;
    }

    private Object evaluate(ASTNode expr) {
        if (expr instanceof LiteralExp) {
            String lexeme = ((LiteralExp) expr).value.getLexeme();
            String type = ((LiteralExp) expr).value.getTokenType();
            if (type.equals("Numeric Literal")) return Integer.parseInt(lexeme);
            if (type.equals("String Literal")) return lexeme.replace("\"", "");
            if (type.equals("Boolean Literal")) return Boolean.parseBoolean(lexeme);
            return lexeme;
        }

        if (expr instanceof IdentifierExp) {
            String name = ((IdentifierExp) expr).name.getLexeme();
            return resolveVariable(name);
        }

        if (expr instanceof BinaryExp) {
            BinaryExp b = (BinaryExp) expr;
            Object left = evaluate(b.left);
            Object right = evaluate(b.right);
            String op = b.operator.getLexeme();

            switch (op) {
                case "+":
                    if (left instanceof String || right instanceof String) return left.toString() + right.toString();
                    return (Integer) left + (Integer) right;
                case "-": return (Integer) left - (Integer) right;
                case "*": return (Integer) left * (Integer) right;
                case "/": return (Integer) left / (Integer) right;
                case ">": return (Integer) left > (Integer) right;
                case "<": return (Integer) left < (Integer) right;
                case "==": return left.equals(right);
            }
        }

        if (expr instanceof AssignExp) {
            AssignExp ae = (AssignExp) expr;
            if (ae.target instanceof IdentifierExp) {
                String name = ((IdentifierExp) ae.target).name.getLexeme();
                Object val = evaluate(ae.value);
                updateVariable(name, val);
                return val;
            }
        }
        
        if (expr instanceof PostFixExp) {
            PostFixExp p = (PostFixExp) expr;
            if (p.operand instanceof IdentifierExp) {
                String name = ((IdentifierExp) p.operand).name.getLexeme();
                int val = (Integer) resolveVariable(name);
                if (p.operator.getLexeme().equals("++")) updateVariable(name, val + 1);
                if (p.operator.getLexeme().equals("--")) updateVariable(name, val - 1);
                return val;
            }
        }

        if (expr instanceof MethodCallExp) {
            MethodCallExp call = (MethodCallExp) expr;
            // Mocking System.out.println
            if (call.method.getLexeme().equals("println")) {
                Object arg = evaluate(call.arguments.get(0));
                System.out.println(arg);
                return null;
            }
        }

        return null;
    }

    private void enterScope() {
        scopes.push(new HashMap<>());
    }

    private void exitScope() {
        scopes.pop();
    }

    private Object resolveVariable(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name)) return scopes.get(i).get(name);
        }
        throw new RuntimeException("Runtime Error: Undefined variable " + name);
    }

    private void updateVariable(String name, Object val) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name)) {
                scopes.get(i).put(name, val);
                return;
            }
        }
    }
}