package main.java.compiler.semantic;

import java.util.ArrayList;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.declaration.*;
import main.java.compiler.parser.error.ErrorNode;
import main.java.compiler.parser.expression.*;
import main.java.compiler.parser.statement.*;

public class SemanticAnalyzer {

    private final SymbolTable symbolTable = new SymbolTable();
    private final ArrayList<SemanticError> errors = new ArrayList<>();

    public ArrayList<SemanticError> getErrors() { return errors; }
    public boolean hasErrors()                  { return !errors.isEmpty(); }

    private void reportError(String message, int line, int col) {
        errors.add(new SemanticError(message, line, col));
    }

    // Entry point — call this with the list returned by parser.parse()
    public void analyze(ArrayList<ASTNode> nodes) {
        // Global scope — holds class names
        symbolTable.enterScope();
        for (ASTNode node : nodes) {
            analyze(node);
        }
        symbolTable.exitScope();
    }

    // Central dispatcher — routes each node to the right handler
    private void analyze(ASTNode node) {
        if (node == null || node instanceof ErrorNode) return;

        if (node instanceof ClassDecl)       analyzeClass((ClassDecl) node);
        else if (node instanceof MethodDecl) analyzeMethod((MethodDecl) node);
        else if (node instanceof ConstructorDecl) analyzeConstructor((ConstructorDecl) node);
        else if (node instanceof VarDecl)    analyzeVarDecl((VarDecl) node);
        else if (node instanceof BlockStmt)  analyzeBlock((BlockStmt) node);
        else if (node instanceof IfStmt)     analyzeIf((IfStmt) node);
        else if (node instanceof WhileStmt)  analyzeWhile((WhileStmt) node);
        else if (node instanceof ForStmt)    analyzeFor((ForStmt) node);
        else if (node instanceof ReturnStmt) analyzeReturn((ReturnStmt) node);
        else if (node instanceof ExprStmt)   analyzeExpr(((ExprStmt) node).exp);
        else if (node instanceof BreakStmt)  { /* valid anywhere in loop — track loop depth if needed */ }
        else if (node instanceof ContinueStmt) { /* same */ }
        else if (node instanceof DoWhileStmt) analyzeDoWhile((DoWhileStmt) node);
        else if (node instanceof ThrowStmt)  analyzeThrow((ThrowStmt) node);
        // expressions used as statements
        else analyzeExpr(node);
    }
    
    private void analyzeClass(ClassDecl node) {
        SymbolTable.Symbol classSym = new SymbolTable.Symbol(
            node.name.getLexeme(), "class", node.name.getLexeme(),
            0, node.name.getLineNumber(), node.name.getColumnNumber());

        String err = symbolTable.declare(classSym);
        if (err != null) {
            reportError("Duplicate class name: " + err,
                        node.name.getLineNumber(), node.name.getColumnNumber());
        }

        if (node.superClass != null) {
            SymbolTable.Symbol superSym = symbolTable.resolveClass(node.superClass.baseName);
            if (superSym == null) {
                reportError("Superclass '" + node.superClass.baseName + "' is not defined",
                            node.lineNumber, node.columnNumber);
            }
        }

        symbolTable.enterScope();
        String previousClass = symbolTable.getCurrentClassName();
        symbolTable.setCurrentClassName(node.name.getLexeme());

        // First pass — register names so bodies can reference anything in the class
        for (ASTNode member : node.members) {
            if (member instanceof VarDecl)             registerField((VarDecl) member);
            else if (member instanceof MethodDecl)     registerMethod((MethodDecl) member);
            else if (member instanceof ConstructorDecl) registerConstructor((ConstructorDecl) member);
        }

        // Second pass — analyze bodies WITHOUT re-declaring fields
        for (ASTNode member : node.members) {
            if (member instanceof VarDecl) {
                analyzeFieldInitializerOnly((VarDecl) member); // NEW — skip re-declaration
            } else {
                analyze(member); // methods and constructors go through normal path
            }
        }

        symbolTable.setCurrentClassName(previousClass);
        symbolTable.exitScope();
    }

    // Only type-checks the initializer — does NOT declare into symbol table
    private void analyzeFieldInitializerOnly(VarDecl node) {
        if (node.init == null) return;

        String initType = analyzeExpr(node.init);

        if (node.type != null && !initType.equals("error")) {
            if (!isTypeCompatible(node.type.baseName, initType)) {
                reportError("Type mismatch in field '" + node.name.getLexeme() +
                            "': cannot assign '" + initType +
                            "' to '" + node.type.baseName + "'",
                            node.name.getLineNumber(), node.name.getColumnNumber());
            }
        }
    }

    // Register field without analyzing initializer yet
    private void registerField(VarDecl node) {
        SymbolTable.Symbol sym = new SymbolTable.Symbol(
            node.name.getLexeme(), "variable",
            node.type != null ? node.type.baseName : "error",
            node.type != null ? node.type.dimension : 0,
            node.name.getLineNumber(), node.name.getColumnNumber());

        String err = symbolTable.declare(sym);
        if (err != null) {
            reportError("Duplicate field declaration: " + err,
                        node.name.getLineNumber(), node.name.getColumnNumber());
        }
    }

    // Register method signature only — body analyzed separately
    private void registerMethod(MethodDecl node) {
        // FIX: was node.type — should be node.returnType
        String returnTypeName = node.type != null ? node.type.baseName : "void";

        SymbolTable.Symbol sym = new SymbolTable.Symbol(
            node.name.getLexeme(), "method",
            returnTypeName, 0,
            node.name.getLineNumber(), node.name.getColumnNumber());

        String err = symbolTable.declare(sym);
        if (err != null) {
            reportError("Duplicate method declaration: " + err,
                        node.name.getLineNumber(), node.name.getColumnNumber());
        }
    }

    private void registerConstructor(ConstructorDecl node) {
        SymbolTable.Symbol sym = new SymbolTable.Symbol(
            node.name.getLexeme(), "constructor",
            node.name.getLexeme(), // return type is the class itself
            0,
            node.name.getLineNumber(), node.name.getColumnNumber());
        symbolTable.declare(sym); // duplicate constructors only matter if params are identical — skip for mini compiler
    }

    private void analyzeMethod(MethodDecl node) {
        // FIX: was node.type — should be node.returnType
        String returnTypeName = node.type != null ? node.type.baseName : "void";
        String previousReturn = symbolTable.getCurrentReturnType();
        symbolTable.setCurrentReturnType(returnTypeName);

        symbolTable.enterScope();

        for (VarDecl param : node.params) {
            SymbolTable.Symbol sym = new SymbolTable.Symbol(
                param.name.getLexeme(), "parameter",
                param.type != null ? param.type.baseName : "error",
                param.type != null ? param.type.dimension : 0,
                param.name.getLineNumber(), param.name.getColumnNumber());

            String err = symbolTable.declare(sym);
            if (err != null) {
                reportError("Duplicate parameter name: " + err,
                            param.name.getLineNumber(), param.name.getColumnNumber());
            }
        }

        if (node.body != null) {
            analyzeBlockContents(node.body); // correct — params and body share this scope
        }

        symbolTable.exitScope();
        symbolTable.setCurrentReturnType(previousReturn);
    }

    private void analyzeConstructor(ConstructorDecl node) {
        // Constructor name must match class name
        if (symbolTable.getCurrentClassName() != null &&
            !node.name.getLexeme().equals(symbolTable.getCurrentClassName())) {
            reportError("Constructor name '" + node.name.getLexeme() +
                        "' does not match class name '" +
                        symbolTable.getCurrentClassName() + "'",
                        node.name.getLineNumber(), node.name.getColumnNumber());
        }

        String previousReturn = symbolTable.getCurrentReturnType();
        symbolTable.setCurrentReturnType("void"); // constructors have no return type

        symbolTable.enterScope();

        for (VarDecl param : node.params) {
            SymbolTable.Symbol sym = new SymbolTable.Symbol(
                param.name.getLexeme(), "parameter",
                param.type != null ? param.type.baseName : "error",
                param.type != null ? param.type.dimension : 0,
                param.name.getLineNumber(), param.name.getColumnNumber());

            String err = symbolTable.declare(sym);
            if (err != null) {
                reportError("Duplicate parameter name: " + err,
                            param.name.getLineNumber(), param.name.getColumnNumber());
            }
        }

        if (node.body != null) analyzeBlockContents(node.body);

        symbolTable.exitScope();
        symbolTable.setCurrentReturnType(previousReturn);
    }

    private void analyzeVarDecl(VarDecl node) {
        // Validate non-primitive type is known
        if (node.type != null && !isPrimitiveType(node.type.baseName)
                && !node.type.baseName.equals("String")
                && !node.type.baseName.startsWith("java.")) {
            SymbolTable.Symbol typeSym = symbolTable.resolveClass(node.type.baseName);
            if (typeSym == null) {
                reportError("Unknown type '" + node.type.baseName + "'",
                            node.lineNumber, node.columnNumber);
            }
        }

        // Analyze initializer BEFORE declaring to prevent self-reference
        String initType = null;
        if (node.init != null) {
            initType = analyzeExpr(node.init);
        }

        // Type-check initializer
        if (node.type != null && initType != null && !initType.equals("error")) {
            if (!isTypeCompatible(node.type.baseName, initType)) {
                reportError("Type mismatch: cannot assign '" + initType +
                            "' to '" + node.type.baseName + "' variable '" +
                            node.name.getLexeme() + "'",
                            node.name.getLineNumber(), node.name.getColumnNumber());
            }
        }

        // Declare into current scope
        SymbolTable.Symbol sym = new SymbolTable.Symbol(
            node.name.getLexeme(), "variable",
            node.type != null ? node.type.baseName : "error",
            node.type != null ? node.type.dimension : 0,
            node.name.getLineNumber(), node.name.getColumnNumber());

        String err = symbolTable.declare(sym);
        if (err != null) {
            reportError("Duplicate variable declaration: " + err,
                        node.name.getLineNumber(), node.name.getColumnNumber());
        }
    }

    private boolean isPrimitiveType(String type) {
        return type.equals("int")     || type.equals("float")  ||
              type.equals("double")  || type.equals("boolean") ||
              type.equals("char")    || type.equals("long")    ||
              type.equals("short")   || type.equals("byte")    ||
              type.equals("void");
    }

    private void analyzeBlock(BlockStmt node) {
        symbolTable.enterScope();
        analyzeBlockContents(node);
        symbolTable.exitScope();
    }

    // Analyze block contents WITHOUT opening a new scope
    // Used by method/constructor which open the scope themselves to include params
    private void analyzeBlockContents(BlockStmt node) {
        for (ASTNode stmt : node.statements) {
            analyze(stmt);
        }
    }

    private void analyzeIf(IfStmt node) {
        String condType = analyzeExpr(node.condition);
        if (condType != null && !condType.equals("boolean") && !condType.equals("error")) {
            reportError("if condition must be boolean, got '" + condType + "'",
                        node.lineNumber, node.columnNumber);
        }
        analyze(node.thenBranch);
        if (node.elseBranch != null) analyze(node.elseBranch);
    }

    private void analyzeWhile(WhileStmt node) {
        String condType = analyzeExpr(node.condition);
        if (condType != null && !condType.equals("boolean") && !condType.equals("error")) {
            reportError("while condition must be boolean, got '" + condType + "'",
                        node.lineNumber, node.columnNumber);
        }
        analyze(node.body);
    }

    private void analyzeDoWhile(DoWhileStmt node) {
        analyze(node.body);
        String condType = analyzeExpr(node.condition);
        if (condType != null && !condType.equals("boolean") && !condType.equals("error")) {
            reportError("do-while condition must be boolean, got '" + condType + "'",
                        node.lineNumber, node.columnNumber);
        }
    }

    private void analyzeFor(ForStmt node) {
        symbolTable.enterScope(); // MUST be first

        // init declares the loop variable into this scope
        if (node.init != null) {
            analyze(node.init); // for VarDecl this calls analyzeVarDecl which declares 'i'
        }

        // condition — 'i' is now visible
        if (node.condition != null) {
            String condType = analyzeExpr(node.condition);
            if (condType != null && !condType.equals("boolean") && !condType.equals("error")) {
                reportError("for condition must be boolean, got '" + condType + "'",
                            node.lineNumber, node.columnNumber);
            }
        }

        // update — 'i' still visible
        if (node.update != null) analyzeExpr(node.update);

        // body — analyzeBlock opens its own inner scope on top
        if (node.body != null) analyze(node.body);

        symbolTable.exitScope();
    }
    private void analyzeReturn(ReturnStmt node) {
        String expected = symbolTable.getCurrentReturnType();

        if (node.value == null) {
            // return; — valid only in void methods
            if (expected != null && !expected.equals("void")) {
                reportError("Missing return value — method expects '" + expected + "'",
                            node.lineNumber, node.columnNumber);
            }
            return;
        }

        String actual = analyzeExpr(node.value);

        if (expected == null) return; // outside any method — parser should have caught this

        if (expected.equals("void")) {
            reportError("Cannot return a value from a void method",
                        node.lineNumber, node.columnNumber);
            return;
        }

        if (actual != null && !actual.equals("error") && !isTypeCompatible(expected, actual)) {
            reportError("Return type mismatch: expected '" + expected +
                        "', got '" + actual + "'",
                        node.lineNumber, node.columnNumber);
        }
    }

    private void analyzeThrow(ThrowStmt node) {
        analyzeExpr(node.value); // just resolve — type checking for throws is advanced
    }
    
    // Returns the inferred type string, or "error" if unresolvable
    // Returns null only if node is null
    private String analyzeExpr(ASTNode node) {
        if (node == null || node instanceof ErrorNode) return "error";

        if (node instanceof LiteralExp)        return analyzeLiteral((LiteralExp) node);
        if (node instanceof IdentifierExp)     return analyzeIdentifier((IdentifierExp) node);
        if (node instanceof BinaryExp)         return analyzeBinary((BinaryExp) node);
        if (node instanceof UnaryExp)          return analyzeUnary((UnaryExp) node);
        if (node instanceof PreFixExp)         return analyzePreFix((PreFixExp) node);
        if (node instanceof PostFixExp)        return analyzePostFix((PostFixExp) node);
        if (node instanceof AssignExp)         return analyzeAssign((AssignExp) node);
        if (node instanceof CompoundAssignExp) return analyzeCompoundAssign((CompoundAssignExp) node);
        if (node instanceof MethodCallExp)     return analyzeMethodCall((MethodCallExp) node);
        if (node instanceof FieldAccessExp)    return analyzeFieldAccess((FieldAccessExp) node);
        if (node instanceof ArrAccessExp)      return analyzeArrayAccess((ArrAccessExp) node);
        if (node instanceof NewObjExp)         return analyzeNewObj((NewObjExp) node);
        if (node instanceof NewArrExp)         return analyzeNewArr((NewArrExp) node);
        if (node instanceof ArrInitExp)        return "array";
        if (node instanceof TernaryExp)        return analyzeTernary((TernaryExp) node);
        if (node instanceof CastExp)           return analyzeCast((CastExp) node);
        if (node instanceof ThisExp)           return symbolTable.getCurrentClassName();
        if (node instanceof SuperExp)          return symbolTable.getCurrentClassName();

        return "error";
    }

    private String analyzeLiteral(LiteralExp node) {
        String type = node.value.getTokenType();
        switch (type) {
            case "Numeric Literal":  return "int";
            case "Float Literal":    return "double";
            case "String Literal":   return "String";
            case "Char Literal":     return "char";
            case "Boolean Literal":  return "boolean";
            case "Special Literal":  return "null"; // null literal
            default:                 return "error";
        }
    }

    private String analyzeIdentifier(IdentifierExp node) {
        String name = node.name.getLexeme();

        if (isKnownBuiltinClass(name)) return name; //test lang

        SymbolTable.Symbol sym = symbolTable.resolve(name);
        if (sym == null) {
            reportError("Undeclared variable '" + name + "'",
                        node.name.getLineNumber(), node.name.getColumnNumber());
            return "error";
        }
        return sym.typeName;
    }

    private boolean isKnownBuiltinClass(String name) {
    return name.equals("System") || name.equals("Math")   ||
           name.equals("String") || name.equals("Integer")||
           name.equals("Double") || name.equals("Boolean")||
           name.equals("Float")  || name.equals("Long")   ||
           name.equals("Object") || name.equals("Arrays") ||
           name.equals("Collections") || name.equals("Scanner");
    } //test lang 

    private String analyzeBinary(BinaryExp node) {
        String left  = analyzeExpr(node.left);
        String right = analyzeExpr(node.right);
        String op    = node.operator.getLexeme();

        if (left.equals("error") || right.equals("error")) return "error";

        // Comparison operators always produce boolean
        if (op.equals("==") || op.equals("!=") || op.equals("<") ||
            op.equals(">")  || op.equals("<=") || op.equals(">=")) {
            return "boolean";
        }

        // Logical operators
        if (op.equals("&&") || op.equals("||")) {
            if (!left.equals("boolean") || !right.equals("boolean")) {
                reportError("Logical operator '" + op + "' requires boolean operands, got '" +
                            left + "' and '" + right + "'",
                            node.operator.getLineNumber(), node.operator.getColumnNumber());
            }
            return "boolean";
        }

        // String concatenation
        if (op.equals("+") && (left.equals("String") || right.equals("String"))) {
            return "String";
        }

        // Numeric operators: +, -, *, /, %
        if (isNumericType(left) && isNumericType(right)) {
            return numericResultType(left, right);
        }

        reportError("Operator '" + op + "' cannot be applied to '" +
                    left + "' and '" + right + "'",
                    node.operator.getLineNumber(), node.operator.getColumnNumber());
        return "error";
    }

    private String analyzeUnary(UnaryExp node) {
        String operand = analyzeExpr(node.operand);
        String op = node.operator.getLexeme();

        if (op.equals("!")) {
            if (!operand.equals("boolean")) {
                reportError("Operator '!' requires boolean, got '" + operand + "'",
                            node.operator.getLineNumber(), node.operator.getColumnNumber());
            }
            return "boolean";
        }

        if (op.equals("-") || op.equals("+")) {
            if (!isNumericType(operand)) {
                reportError("Unary '" + op + "' requires numeric type, got '" + operand + "'",
                            node.operator.getLineNumber(), node.operator.getColumnNumber());
                return "error";
            }
            return operand;
        }

        return operand;
    }

    private String analyzePreFix(PreFixExp node) {
        String operand = analyzeExpr(node.operand);
        if (!isNumericType(operand) && !operand.equals("error")) {
            reportError("Prefix '" + node.operator.getLexeme() +
                        "' requires numeric type, got '" + operand + "'",
                        node.operator.getLineNumber(), node.operator.getColumnNumber());
            return "error";
        }
        return operand;
    }

    private String analyzePostFix(PostFixExp node) {
        String operand = analyzeExpr(node.operand);
        if (!isNumericType(operand) && !operand.equals("error")) {
            reportError("Postfix '" + node.operator.getLexeme() +
                        "' requires numeric type, got '" + operand + "'",
                        node.operator.getLineNumber(), node.operator.getColumnNumber());
            return "error";
        }
        return operand;
    }

    private String analyzeAssign(AssignExp node) {
        String targetType = analyzeExpr(node.target);
        String valueType  = analyzeExpr(node.value);

        if (!targetType.equals("error") && !valueType.equals("error")) {
            if (!isTypeCompatible(targetType, valueType)) {
                reportError("Cannot assign '" + valueType + "' to '" + targetType + "'",
                            node.lineNumber, node.columnNumber);
            }
        }
        return targetType;
    }

    private String analyzeCompoundAssign(CompoundAssignExp node) {
        String targetType = analyzeExpr(node.target);
        String valueType  = analyzeExpr(node.value); // FIX: was missing entirely

        String op = node.operator.getLexeme();

        if (op.equals("+=") && targetType.equals("String")) return "String";

        if (!targetType.equals("error") && !isNumericType(targetType)) {
            reportError("Operator '" + op + "' requires numeric target, got '" + targetType + "'",
                        node.operator.getLineNumber(), node.operator.getColumnNumber());
        }

        if (!targetType.equals("error") && !valueType.equals("error")) {
            if (!isTypeCompatible(targetType, valueType) && !isNumericType(valueType)) {
                reportError("Operator '" + op + "' cannot apply '" + valueType +
                            "' to '" + targetType + "'",
                            node.operator.getLineNumber(), node.operator.getColumnNumber());
            }
        }

        return targetType;
    }

    private String analyzeMethodCall(MethodCallExp node) {
        // Analyze the object the method is called on (if any)
        if (node.object != null) analyzeExpr(node.object);

        // Resolve method name — for a mini compiler, just check it exists
        String methodName = node.method.getLexeme();
        SymbolTable.Symbol sym = symbolTable.resolve(methodName);

        // Analyze all arguments regardless of whether method was found
        for (ASTNode arg : node.arguments) analyzeExpr(arg);

        if (sym == null) {
            // Common built-in calls: System.out.println etc — skip for mini compiler
            if (!isKnownBuiltin(methodName)) {
                reportError("Undeclared method '" + methodName + "'",
                            node.method.getLineNumber(), node.method.getColumnNumber());
            }
            return "error";
        }

        return sym.typeName; // return type of the method
    }

    private String analyzeFieldAccess(FieldAccessExp node) {
        analyzeExpr(node.target); // still walk the object for errors
        // Full field resolution requires a type table — return "error" for unknowns
        return "error"; // semantic analyzer does not resolve field types in a mini compiler
    }

    private String analyzeArrayAccess(ArrAccessExp node) {
        String arrayType = analyzeExpr(node.array);
        String indexType = analyzeExpr(node.index);

        if (!indexType.equals("int") && !indexType.equals("error")) {
            reportError("Array index must be int, got '" + indexType + "'",
                        node.lineNumber, node.columnNumber);
        }

        // Strip one dimension from the type
        return arrayType.equals("error") ? "error" : arrayType;
    }

    private String analyzeNewObj(NewObjExp node) {
        // Check type is known
        if (!isPrimitiveType(node.type.baseName) &&
            !node.type.baseName.equals("String") &&
            !node.type.baseName.startsWith("java.")) {
            SymbolTable.Symbol sym = symbolTable.resolveClass(node.type.baseName);
            if (sym == null) {
                reportError("Unknown class '" + node.type.baseName + "'",
                            node.lineNumber, node.columnNumber);
            }
        }
        for (ASTNode arg : node.args) analyzeExpr(arg);
        return node.type.baseName;
    }

    private String analyzeNewArr(NewArrExp node) {
        // FIX: was node.elementType — check your NewArrExp, likely node.type
        if (node.size != null) {
            String sizeType = analyzeExpr(node.size);
            if (!sizeType.equals("int") && !sizeType.equals("error")) {
                reportError("Array size must be int, got '" + sizeType + "'",
                            node.lineNumber, node.columnNumber);
            }
        }
        return node.elementType.baseName; // element type — fix field name to match your AST node
    }

    private String analyzeTernary(TernaryExp node) {
        String condType = analyzeExpr(node.condition);
        String thenType = analyzeExpr(node.thenBranch);
        String elseType = analyzeExpr(node.elseBranch);

        if (!condType.equals("boolean") && !condType.equals("error")) {
            reportError("Ternary condition must be boolean, got '" + condType + "'",
                        node.lineNumber, node.columnNumber);
        }

        if (!thenType.equals("error") && !elseType.equals("error") &&
            !isTypeCompatible(thenType, elseType)) {
            reportError("Ternary branches have incompatible types: '" +
                        thenType + "' and '" + elseType + "'",
                        node.lineNumber, node.columnNumber);
        }

        return thenType.equals("error") ? elseType : thenType;
    }

    private String analyzeCast(CastExp node) {
        analyzeExpr(node.operand);
        return node.targetType.baseName;
    }

    private boolean isNumericType(String type) {
    return type.equals("int")    || type.equals("float") ||
           type.equals("double") || type.equals("long")  ||
           type.equals("short")  || type.equals("byte")  ||
           type.equals("char");
}

    // Numeric promotion rules — widening only
    private String numericResultType(String a, String b) {
        if (a.equals("double") || b.equals("double")) return "double";
        if (a.equals("float")  || b.equals("float"))  return "float";
        if (a.equals("long")   || b.equals("long"))   return "long";
        return "int"; // int, short, byte, char all promote to int
    }

    // Checks whether valueType can be assigned to targetType
    private boolean isTypeCompatible(String target, String value) {
        if (target.equals(value))       return true;
        if (value.equals("null"))       return !isPrimitiveType(target); // null ok for objects
        if (target.equals("double") && isNumericType(value)) return true; // widening
        if (target.equals("float")  && (value.equals("int") || value.equals("long"))) return true;
        if (target.equals("long")   && (value.equals("int") || value.equals("short") || value.equals("byte"))) return true;
        return false;
    }

    // Known Java built-in method calls to suppress false "undeclared method" errors
    private boolean isKnownBuiltin(String name) {
        return name.equals("println") || name.equals("print")  ||
              name.equals("printf")  || name.equals("length") ||
              name.equals("size")    || name.equals("get")    ||
              name.equals("add")     || name.equals("remove") ||
              name.equals("toString")|| name.equals("equals") ||
              name.equals("charAt")  || name.equals("substring");
    }
}
