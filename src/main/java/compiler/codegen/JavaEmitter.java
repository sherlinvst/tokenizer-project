package main.java.compiler.codegen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import main.java.compiler.ir.IRInstruction;
import main.java.compiler.ir.IRInstruction.Type;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.declaration.VarDecl;
import main.java.compiler.parser.statement.BlockStmt;

/**
 * JavaEmitter — Code Generator
 *
 * Translates List<IRInstruction> into valid Java source code.
 */
public class JavaEmitter {

    // Maps variable/temp name → Java type string  e.g. "x" → "int"
    private final Map<String, String> varTypes = new HashMap<>();

    // Tracks which names have already been declared in the output
    private final Set<String> declared = new HashSet<>();

    // Maps field access temps to their full path e.g. "t1" → "System.out"
    // Used to collapse "t1 = System.out; t1.println()" into "System.out.println()"
    private final Map<String, String> fieldAccessTemps = new HashMap<>();
    private final Map<String, String> tempExpressions   = new HashMap<>();
    private final Set<String>         condJumpTemps      = new HashSet<>();
    private final Map<String, Integer> useCount = new HashMap<>();
    private final Set<String> pendingBlockClose = new HashSet<>();


    // Current indentation level
    private int indentLevel = 0;
    private static final String INDENT_UNIT = "    "; // 4 spaces

    // -------------------------------------------------------------------------
    // Pre-pass: collect VarDecl type info from AST
    // -------------------------------------------------------------------------
    public void collectTypes(List<ASTNode> nodes) {
        for (ASTNode node : nodes) collectTypesFromNode(node);
    }

    private void collectTypesFromNode(ASTNode node) {
        if (node == null) return;
        if (node instanceof VarDecl) {
            VarDecl decl = (VarDecl) node;
            String typeName = buildTypeString(decl.type.baseName, decl.type.dimension);
            varTypes.put(decl.name.getLexeme(), typeName);
        }
        if (node instanceof BlockStmt) {
            for (ASTNode child : ((BlockStmt) node).statements)
                collectTypesFromNode(child);
        }
        if (node instanceof main.java.compiler.parser.declaration.MethodDecl) {
            main.java.compiler.parser.declaration.MethodDecl m =
                (main.java.compiler.parser.declaration.MethodDecl) node;
            if (m.body != null) collectTypesFromNode(m.body);
        }
        if (node instanceof main.java.compiler.parser.declaration.ClassDecl) {
            main.java.compiler.parser.declaration.ClassDecl c =
                (main.java.compiler.parser.declaration.ClassDecl) node;
            for (ASTNode member : c.members) collectTypesFromNode(member);
        }
        if (node instanceof main.java.compiler.parser.declaration.ConstructorDecl) {
            main.java.compiler.parser.declaration.ConstructorDecl ctor =
                (main.java.compiler.parser.declaration.ConstructorDecl) node;
            if (ctor.body != null) collectTypesFromNode(ctor.body);
        }
    }

    public void registerType(String name, String javaType) {
        varTypes.put(name, javaType);
    }

    // -------------------------------------------------------------------------
    // Main emit — translates IR instructions to Java source
    // -------------------------------------------------------------------------

    public String emit(List<IRInstruction> instructions) {
        StringBuilder sb = new StringBuilder();

        Set<String> jumpTargets = new HashSet<>();
        for (int j = 0; j < instructions.size(); j++) {
        IRInstruction instr = instructions.get(j);
        if (instr.getType() == Type.CONDITIONAL_JUMP) {
            condJumpTemps.add(instr.getArg1());
            jumpTargets.add(instr.getLabel());
        }
        if (instr.getType() == Type.JUMP) {
            String target = instr.getLabel();
            for (int k = 0; k < j; k++) {
                if (instructions.get(k).getType() == Type.LABEL &&
                    target.equals(instructions.get(k).getLabel())) {
                    jumpTargets.add(target);
                    break;
                }
            }
        }
        if (instr.getArg1() != null) useCount.merge(instr.getArg1(), 1, Integer::sum);
        if (instr.getArg2() != null) useCount.merge(instr.getArg2(), 1, Integer::sum);
        if (instr.getOp()   != null) useCount.merge(instr.getOp(),   1, Integer::sum);
    }

        for (int i = 0; i < instructions.size(); i++) {
            IRInstruction instr = instructions.get(i);

            switch (instr.getType()) {

                case CLASS_START:
                    emitClassStart(sb, instr);
                    break;

                case CLASS_END:
                    indentLevel--;
                    sb.append(indent()).append("}\n");
                    break;

                case METHOD_START:
                    emitMethodStart(sb, instr);
                    break;

                case METHOD_END:
                    indentLevel--;
                    sb.append(indent()).append("}\n\n");
                    break;

                case BINARY_OP:
                    emitBinaryOp(sb, instr);
                    break;

                case ASSIGN:
                    emitAssign(sb, instr);
                    break;

                case UNARY_OP:
                    emitUnaryOp(sb, instr);
                    break;

                case CALL:
                    emitCall(sb, instr);
                    break;

                case FIELD_ACCESS:
                    // Peek ahead — if next instruction is a CALL using this temp,
                    // skip emitting this and let CALL handle the full path
                    if (i + 1 < instructions.size()) {
                        IRInstruction next = instructions.get(i + 1);
                        if (next.getType() == Type.CALL &&
                            next.getOp() != null &&
                            next.getOp().startsWith(instr.getResult() + ".")) {
                            // Store full path e.g. t1 → "System.out"
                            fieldAccessTemps.put(instr.getResult(),
                                instr.getArg1() + "." + instr.getOp());
                            break; // skip emitting — CALL will handle it
                        }
                    }
                    emitFieldAccess(sb, instr);
                    break;

                case NEW_OBJ:
                    emitNewObj(sb, instr);
                    break;

                case NEW_ARR:
                    emitNewArr(sb, instr);
                    break;

                case ARR_INIT:
                    emitArrInit(sb, instr);
                    break;

                case ARR_ACCESS:
                    emitArrAccess(sb, instr);
                    break;

                case CAST:
                    emitCast(sb, instr);
                    break;

                case RETURN:
                    emitReturn(sb, instr);
                    break;

                case LABEL:
                    if (pendingBlockClose.contains(instr.getLabel())) {
                        indentLevel--;
                        sb.append(indent()).append("}\n");
                        pendingBlockClose.remove(instr.getLabel());
                        // Don't re-emit the label — the block close replaces it
                        break;  // ← add this, remove the fall-through to sb.append below
                    }
                    if (jumpTargets.contains(instr.getLabel())) {
                        sb.append(instr.getLabel()).append(":\n");
                    }
                    break;

                case JUMP:
                    //sb.append(indent()).append("// GOTO ").append(instr.getLabel()).append("\n");
                    break;

                case CONDITIONAL_JUMP: {
                    String cond      = instr.getArg1();
                    String expr      = tempExpressions.getOrDefault(cond, cond);
                    String flipped   = flipCondition(expr);
                    String jumpLabel = instr.getLabel();
                    sb.append(indent()).append(jumpLabel).append(": {\n");
                    indentLevel++;
                    sb.append(indent()).append("if (").append(flipped)
                    .append(") break ").append(jumpLabel).append(";\n");
                    // Find the matching LABEL and close the block there
                    pendingBlockClose.add(jumpLabel);
                    break;
                }

                case BREAK:
                    sb.append(indent()).append("break;\n");
                    break;

                case CONTINUE:
                    sb.append(indent()).append("continue;\n");
                    break;

                case THROW:
                    sb.append(indent()).append("throw ").append(instr.getArg1()).append(";\n");
                    break;

                default:
                    sb.append(indent()).append("// [unknown IR: ").append(instr).append("]\n");
                    break;
            }
        }

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Emitters for each instruction type
    // -------------------------------------------------------------------------

    private void emitClassStart(StringBuilder sb, IRInstruction instr) {
        sb.append(indent());
        if (instr.getOp() != null && !instr.getOp().isEmpty()) {
            sb.append(instr.getOp()).append(" ");
        }
        sb.append("class ").append(instr.getArg1());
        if (instr.getArg2() != null) {
            sb.append(" extends ").append(instr.getArg2());
        }
        sb.append(" {\n");
        indentLevel++;
    }

    private void emitMethodStart(StringBuilder sb, IRInstruction instr) {
        sb.append(indent());
        if (instr.getOp() != null && !instr.getOp().isEmpty()) {
            sb.append(instr.getOp()).append(" ");
        }
        sb.append(instr.getArg1()).append("(");
        if (instr.getArg2() != null && !instr.getArg2().isEmpty()) {
            sb.append(instr.getArg2());
        }
        sb.append(") {\n");
        indentLevel++;
    }

    private void emitBinaryOp(StringBuilder sb, IRInstruction instr) {
        String result     = instr.getResult();
        String expression = instr.getArg1() + " " + instr.getOp() + " " + instr.getArg2();
        tempExpressions.put(result, expression);  // <-- add
        if (condJumpTemps.contains(result)) return; // <-- add: skip emitting, used inline
        sb.append(indent());
        if (!declared.contains(result)) {
            String type = resolveType(result, instr.getArg1(), instr.getArg2(), instr.getOp());
            sb.append(type).append(" ");
            declared.add(result);
        }
        sb.append(result).append(" = ").append(expression).append(";\n");
    }

    private void emitAssign(StringBuilder sb, IRInstruction instr) {
        String result = instr.getResult();
        String value  = instr.getArg1();
        if (result == null) return;

        if(result.matches("t\\d+") && useCount.getOrDefault(result, 0) == 0) return;

        sb.append(indent());
        if (!declared.contains(result)) {
            String type = varTypes.getOrDefault(result, inferLiteralType(value));
            sb.append(type).append(" ");
            declared.add(result);
        }
        sb.append(result).append(" = ").append(value).append(";\n");
    }

    private void emitUnaryOp(StringBuilder sb, IRInstruction instr) {
        String result     = instr.getResult();
        String expression = instr.getOp() + instr.getArg1();
        sb.append(indent());
        if (!declared.contains(result)) {
            String type = varTypes.getOrDefault(result, "var");
            sb.append(type).append(" ");
            declared.add(result);
        }
        sb.append(result).append(" = ").append(expression).append(";\n");
    }

    private void emitCall(StringBuilder sb, IRInstruction instr) {
        String methodName = instr.getOp();
        String args       = instr.getArg1() != null ? instr.getArg1() : "";
        String result     = instr.getResult();

        // Replace temp with full field path
        // e.g. "t1.println" where t1 = "System.out" → "System.out.println"
        if (methodName != null && methodName.contains(".")) {
            String tempPart = methodName.substring(0, methodName.indexOf("."));
            if (fieldAccessTemps.containsKey(tempPart)) {
                String fullPath = fieldAccessTemps.get(tempPart);
                String methodPart = methodName.substring(tempPart.length()); // ".println"
                methodName = fullPath + methodPart; // "System.out.println"
            }
        }

        boolean isVoid = isVoidCall(methodName);

        sb.append(indent());
        if (!isVoid && result != null && !declared.contains(result)) {
            String type = varTypes.getOrDefault(result, "var");
            sb.append(type).append(" ").append(result).append(" = ");
            declared.add(result);
        } else if (!isVoid && result != null && declared.contains(result)) {
            sb.append(result).append(" = ");
        }
        sb.append(methodName).append("(").append(args).append(");\n");
    }

    private void emitFieldAccess(StringBuilder sb, IRInstruction instr) {
        String result = instr.getResult();
        String obj    = instr.getArg1();
        String field  = instr.getOp();
        sb.append(indent());
        if (!declared.contains(result)) {
            String type = varTypes.getOrDefault(result, "var");
            sb.append(type).append(" ");
            declared.add(result);
        }
        sb.append(result).append(" = ").append(obj).append(".").append(field).append(";\n");
    }

    private void emitNewObj(StringBuilder sb, IRInstruction instr) {
        String result    = instr.getResult();
        String className = instr.getOp();
        String args      = instr.getArg1() != null ? instr.getArg1() : "";
        sb.append(indent());
        if (!declared.contains(result)) {
            sb.append(className).append(" ");
            declared.add(result);
        }
        sb.append(result).append(" = new ").append(className)
          .append("(").append(args).append(");\n");
    }

    private void emitNewArr(StringBuilder sb, IRInstruction instr) {
        String result      = instr.getResult();
        String elementType = instr.getOp();
        String size        = instr.getArg1();
        sb.append(indent());
        if (!declared.contains(result)) {
            sb.append(elementType).append("[] ");
            declared.add(result);
        }
        sb.append(result).append(" = new ").append(elementType)
          .append("[").append(size).append("];\n");
    }

    private void emitArrInit(StringBuilder sb, IRInstruction instr) {
        String result   = instr.getResult();
        String elements = instr.getArg1();
        sb.append(indent());
        if (!declared.contains(result)) {
            String type = varTypes.getOrDefault(result, "var");
            sb.append(type).append("[] ");
            declared.add(result);
        }
        sb.append(result).append(" = {").append(elements).append("};\n");
    }

    private void emitArrAccess(StringBuilder sb, IRInstruction instr) {
        String result = instr.getResult();
        String array  = instr.getArg1();
        String index  = instr.getOp();
        sb.append(indent());
        if (!declared.contains(result)) {
            String type = varTypes.getOrDefault(result, "var");
            sb.append(type).append(" ");
            declared.add(result);
        }
        sb.append(result).append(" = ").append(array).append("[").append(index).append("];\n");
    }

    private void emitCast(StringBuilder sb, IRInstruction instr) {
        String result     = instr.getResult();
        String targetType = instr.getOp();
        String operand    = instr.getArg1();
        sb.append(indent());
        if (!declared.contains(result)) {
            sb.append(targetType).append(" ");
            declared.add(result);
        }
        sb.append(result).append(" = (").append(targetType).append(") ")
          .append(operand).append(";\n");
    }

    private void emitReturn(StringBuilder sb, IRInstruction instr) {
        sb.append(indent()).append("return");
        if (instr.getArg1() != null) {
            sb.append(" ").append(instr.getArg1());
        }
        sb.append(";\n");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private String flipCondition(String expr) {
        String[][] flips = {
            {">=", "<"}, {"<=", ">"}, {">", "<="}, {"<", ">="}, {"==", "!="}, {"!=", "=="}
        };
        for (String[] pair : flips) {
            int idx = expr.indexOf(" " + pair[0] + " ");
            if (idx >= 0) {
                return expr.substring(0, idx + 1)
                    + pair[1]
                    + expr.substring(idx + pair[0].length() + 1);
            }
        }
        return "!(" + expr + ")"; // fallback for plain boolean temps
    }

    private String indent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) sb.append(INDENT_UNIT);
        return sb.toString();
    }

    private String resolveType(String result, String arg1, String arg2, String op) {
        if (varTypes.containsKey(result)) return varTypes.get(result);
        String t1 = varTypes.getOrDefault(arg1, inferLiteralType(arg1));
        String t2 = varTypes.getOrDefault(arg2, inferLiteralType(arg2));
        if ("String".equals(t1) || "String".equals(t2)) return "String";
        if ("double".equals(t1) || "double".equals(t2)) return "double";
        if ("float".equals(t1)  || "float".equals(t2))  return "float";
        if ("long".equals(t1)   || "long".equals(t2))   return "long";
        if (isComparisonOp(op) || isLogicalOp(op)) return "boolean";
        if (isArithmeticOp(op)) return "int";
        return "var";
    }

    private String inferLiteralType(String value) {
        if (value == null) return "var";
        if (value.equals("true") || value.equals("false")) return "boolean";
        if (value.startsWith("\"")) return "String";
        if (value.startsWith("'"))  return "char";
        if (value.endsWith("L") || value.endsWith("l")) return "long";
        if (value.endsWith("f") || value.endsWith("F")) return "float";
        if (value.endsWith("d") || value.endsWith("D")) return "double";
        if (value.contains("."))    return "double";
        try { Integer.parseInt(value); return "int"; } catch (NumberFormatException ignored) {}
        return "var";
    }

    private boolean isArithmeticOp(String op) {
        return op != null && (op.equals("+") || op.equals("-") || op.equals("*")
                           || op.equals("/") || op.equals("%"));
    }

    private boolean isComparisonOp(String op) {
        return op != null && (op.equals("==") || op.equals("!=") || op.equals("<")
                           || op.equals(">")  || op.equals("<=") || op.equals(">="));
    }

    private boolean isLogicalOp(String op) {
        return op != null && (op.equals("&&") || op.equals("||") || op.equals("!"));
    }

    private boolean isVoidCall(String methodName) {
        if (methodName == null) return true;
        return methodName.contains("println") || methodName.contains("print")
            || methodName.contains("printf")  || methodName.contains("append")
            || methodName.contains("add")     || methodName.contains("remove")
            || methodName.contains("set")     || methodName.contains("clear")
            || methodName.contains("close")   || methodName.contains("flush")
            || methodName.contains("write")   || methodName.contains("sort");
    }

    private String buildTypeString(String baseName, int dims) {
        if (dims == 0) return baseName;
        StringBuilder sb = new StringBuilder(baseName);
        for (int i = 0; i < dims; i++) sb.append("[]");
        return sb.toString();
    }
}