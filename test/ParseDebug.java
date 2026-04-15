package test;

import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.statement.*;
import main.java.compiler.parser.expression.*;
import main.java.compiler.parser.declaration.*;

public class ParseDebug {
    private int indent = 0;

    public void print(ASTNode node) {
        if (node == null) { line("null"); return; }

        if (node instanceof ClassDecl)  printClass((ClassDecl) node);
        else if (node instanceof MethodDecl) printMethod((MethodDecl) node);
        else if (node instanceof VarDecl)    printVarDecl((VarDecl) node);
        else if (node instanceof BlockStmt)      printBlock((BlockStmt) node);
        else if (node instanceof IfStmt)         printIf((IfStmt) node);
        else if (node instanceof WhileStmt)      printWhile((WhileStmt) node);
        else if (node instanceof ReturnStmt)     printReturn((ReturnStmt) node);
        else if (node instanceof ExprStmt)       { line("ExprStmt"); indent++; print(((ExprStmt)node).exp); indent--; }
        else if (node instanceof BinaryExp)     printBinary((BinaryExp) node);
        else if (node instanceof UnaryExp)      printUnary((UnaryExp) node);
        else if (node instanceof AssignExp)     printAssign((AssignExp) node);
        else if (node instanceof LiteralExp)    line("Literal: " + ((LiteralExp)node).value.getLexeme());
        else if (node instanceof IdentifierExp) line("Identifier: " + ((IdentifierExp)node).name.getLexeme());
        else if (node instanceof MethodCallExp) printCall((MethodCallExp) node);
        else line("Unknown node: " + node.getClass().getSimpleName());
    }

    private void printBinary(BinaryExp n) {
        line("BinaryExpr: " + n.operator.getLexeme());
        indent++;
        print(n.left);
        print(n.right);
        indent--;
    }

    private void printBlock(BlockStmt n) {
        line("Block");
        indent++;
        for (ASTNode s : n.statements) print(s);
        indent--;
    }

    private void printIf(IfStmt n) {
        line("IfStmt");
        indent++;
        line("condition:"); indent++; print(n.condition); indent--;
        line("then:"); indent++; print(n.thenBranch); indent--;
        if (n.elseBranch != null) { line("else:"); indent++; print(n.elseBranch); indent--; }
        indent--;
    }

    private void printWhile(WhileStmt n) {
        line("WhileStmt");
        indent++;
        line("condition:"); indent++; print(n.condition); indent--;
        line("body:"); indent++; print(n.body); indent--;
        indent--;
    }

    private void printReturn(ReturnStmt n) {
        line("ReturnStmt");
        if (n.value != null) {
            indent++;
            print(n.value);
            indent--;
        }
    }

    private void printVarDecl(VarDecl n) {
        line("VarDecl: " + n.name.getLexeme());
        if (n.init != null) {
            indent++;
            line("initializer:");
            indent++;
            print(n.init);
            indent -= 2;
        }
    } 

    private void printMethod(MethodDecl n) {
        line("MethodDecl: " + n.name.getLexeme());
        indent++;
        if (!n.params.isEmpty()) {
            line("parameters:");
            indent++;
            for (VarDecl param : n.params) printVarDecl(param);
            indent--;
        }
        line("body:");
        indent++;
        print(n.body);
        indent -= 2;
    }

    private void printCall(MethodCallExp n) {
        line("MethodCall: " + n.method.getLexeme());
        indent++;
        if (n.object != null) {
            line("receiver:");
            indent++;
            print(n.object);
            indent--;
        }
        if (!n.arguments.isEmpty()) {
            line("arguments:");
            indent++;
            for (ASTNode arg : n.arguments) print(arg);
            indent--;
        }
        indent--;
    }

    private void printClass(ClassDecl n) {
        line("ClassDecl: " + n.name.getLexeme());
        indent++;
        if (!n.members.isEmpty()) {
            line("members:");
            indent++;
            for (ASTNode member : n.members) print(member);
            indent--;
        }
        indent--;
    }

    private void printUnary(UnaryExp n) {
        line("UnaryExpr: " + n.operator.getLexeme());
        indent++;
        print(n.operand);
        indent--;
    }

    private void printAssign(AssignExp n) {
        line("AssignExpr: " + n.name.getLexeme() + " =");
        indent++;
        print(n.value);
        indent--;
    }

    private void line(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent * 2; i++) sb.append(' ');
        sb.append(text);
        System.out.println(sb.toString());
    }
}