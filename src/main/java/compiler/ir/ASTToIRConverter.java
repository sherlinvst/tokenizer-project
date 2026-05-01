/*package main.java.compiler.ir;

import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.expression.*;
import main.java.compiler.parser.statement.*;
import main.java.compiler.parser.declaration.*;
import java.util.List;

public class ASTToIRConverter {
    private IRGenerator irGen;

    public ASTToIRConverter(IRGenerator irGen) {
        this.irGen = irGen;
    }

    public void convert(List<ASTNode> nodes) {
        for (ASTNode node : nodes) convertNode(node);
    }

    public String convertNode(ASTNode node) {
        if (node == null) return null;

        // 1. Terminals (Leaves)
        if (node instanceof LiteralExp) {
            return ((LiteralExp) node).value.getLexeme();
        }
        if (node instanceof IdentifierExp) {
            return ((IdentifierExp) node).name.getLexeme();
        }

        // 2. Binary Expressions (a + b)
        if (node instanceof BinaryExp) {
            BinaryExp bin = (BinaryExp) node;
            String left = convertNode(bin.left);
            String right = convertNode(bin.right);
            String target = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.BINARY_OP, bin.operator.getLexeme(), left, right, target));
            return target;
        }

        // 3. Assignments (x = value)
        if (node instanceof AssignExp) {
            AssignExp assign = (AssignExp) node;
            String val = convertNode(assign.value);
            if (assign.target instanceof IdentifierExp) {
                String varName = ((IdentifierExp) assign.target).name.getLexeme();
                irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN, null, val, varName));
            }
            return null;
        }

        // 4. Statements
        if (node instanceof ExprStmt) {
            convertNode(((ExprStmt) node).exp);
        } else if (node instanceof VarDecl) {
            VarDecl decl = (VarDecl) node;
            if (decl.init != null) {
                String val = convertNode(decl.init);
                irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN, null, val, decl.name.getLexeme()));
            }
        } else if (node instanceof BlockStmt) {
            for (ASTNode s : ((BlockStmt) node).statements) convertNode(s);
        }

        return null;
    }
}*/
package main.java.compiler.ir;

import java.util.List;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.declaration.*;
import main.java.compiler.parser.expression.*;
import main.java.compiler.parser.statement.*;

public class ASTToIRConverter {
    private IRGenerator irGen;

    public ASTToIRConverter(IRGenerator irGen) {
        this.irGen = irGen;
    }

    public void convert(List<ASTNode> nodes) {
        for (ASTNode node : nodes) convertNode(node);
    }

    public String convertNode(ASTNode node) {
        if (node == null) return null;

        // ----------------------------------------------------------------
        // 1. Terminals (Leaves)
        // ----------------------------------------------------------------
        if (node instanceof LiteralExp) {
            return ((LiteralExp) node).value.getLexeme();
        }
        if (node instanceof IdentifierExp) {
            return ((IdentifierExp) node).name.getLexeme();
        }
        if (node instanceof ThisExp) {
            return "this";
        }
        if (node instanceof SuperExp) {
            return "super";
        }

        // ----------------------------------------------------------------
        // 2. Binary Expressions (a + b)
        // ----------------------------------------------------------------
        if (node instanceof BinaryExp) {
            BinaryExp bin = (BinaryExp) node;
            String left  = convertNode(bin.left);
            String right = convertNode(bin.right);
            String target = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.BINARY_OP,
                bin.operator.getLexeme(), left, right, target));
            return target;
        }

        // ----------------------------------------------------------------
        // 3. Unary Expressions (!x, -x)
        // ----------------------------------------------------------------
        if (node instanceof UnaryExp) {
            UnaryExp unary = (UnaryExp) node;
            String operand = convertNode(unary.operand);
            String target  = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.UNARY_OP,
                unary.operator.getLexeme(), operand, target));
            return target;
        }

        // ----------------------------------------------------------------
        // 4. Prefix (++x, --x)
        // ----------------------------------------------------------------
        if (node instanceof PreFixExp) {
            PreFixExp pre = (PreFixExp) node;
            String operand = convertNode(pre.operand);
            String one     = "1";
            String op      = pre.operator.getLexeme().equals("++") ? "+" : "-";
            String target  = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.BINARY_OP, op, operand, one, target));
            irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN, null, target, operand));
            return target;
        }

        // ----------------------------------------------------------------
        // 5. Postfix (x++, x--)
        // ----------------------------------------------------------------
        if (node instanceof PostFixExp) {
            PostFixExp post = (PostFixExp) node;
            String operand = convertNode(post.operand);
            String saved   = irGen.nextTemp(); // save original value
            irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN, null, operand, saved));
            String one  = "1";
            String op   = post.operator.getLexeme().equals("++") ? "+" : "-";
            String temp = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.BINARY_OP, op, operand, one, temp));
            irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN, null, temp, operand));
            return saved; // expression value is the original
        }

        // ----------------------------------------------------------------
        // 6. Assignment (x = value)
        // ----------------------------------------------------------------
        if (node instanceof AssignExp) {
            AssignExp assign = (AssignExp) node;
            String val = convertNode(assign.value);
            if (assign.target instanceof IdentifierExp) {
                String varName = ((IdentifierExp) assign.target).name.getLexeme();
                irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN, null, val, varName));
            } else if (assign.target instanceof FieldAccessExp) {
                // e.g. this.x = val
                FieldAccessExp fa = (FieldAccessExp) assign.target;
                String obj = convertNode(fa.target);
                String fieldName = obj + "." + fa.field.getLexeme();
                irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN, null, val, fieldName));
            } else if (assign.target instanceof ArrAccessExp) {
                // e.g. arr[i] = val
                ArrAccessExp aa = (ArrAccessExp) assign.target;
                String arr   = convertNode(aa.array);
                String index = convertNode(aa.index);
                String target = arr + "[" + index + "]";
                irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN, null, val, target));
            }
            return val;
        }

        // ----------------------------------------------------------------
        // 7. Compound Assignment (x += value, x -= value, etc.)
        // ----------------------------------------------------------------
        if (node instanceof CompoundAssignExp) {
            CompoundAssignExp ca = (CompoundAssignExp) node;
            String target = convertNode(ca.target);
            String val    = convertNode(ca.value);
            // Strip the '=' from operator: "+=" → "+"
            String op = ca.operator.getLexeme().replace("=", "");
            String temp = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.BINARY_OP, op, target, val, temp));
            irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN, null, temp, target));
            return temp;
        }

        // ----------------------------------------------------------------
        // 8. Field Access (object.field)
        // ----------------------------------------------------------------
        if (node instanceof FieldAccessExp) {
            FieldAccessExp fa = (FieldAccessExp) node;
            String obj    = convertNode(fa.target);
            String target = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.FIELD_ACCESS,
                fa.field.getLexeme(), obj, target));
            return target;
        }

        // ----------------------------------------------------------------
        // 9. Method Call (object.method(args) or method(args))
        // ----------------------------------------------------------------
        if (node instanceof MethodCallExp) {
            MethodCallExp call = (MethodCallExp) node;

            // Convert all arguments first
            String[] argTemps = new String[call.arguments.size()];
            for (int i = 0; i < call.arguments.size(); i++) {
                argTemps[i] = convertNode(call.arguments.get(i));
            }

            // Build argument string  e.g. "arg1, arg2, arg3"
            String argList = String.join(", ", argTemps);

            // Build full method name  e.g. "System.out.println" or "myMethod"
            String methodName;
            if (call.object != null) {
                String obj = convertNode(call.object);
                methodName = obj + "." + call.method.getLexeme();
            } else {
                methodName = call.method.getLexeme();
            }

            String target = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.CALL,
                methodName, argList, target));
            return target;
        }

        // ----------------------------------------------------------------
        // 10. New Object (new MyClass(args))
        // ----------------------------------------------------------------
        if (node instanceof NewObjExp) {
            NewObjExp newObj = (NewObjExp) node;
            String[] argTemps = new String[newObj.args.size()];
            for (int i = 0; i < newObj.args.size(); i++) {
                argTemps[i] = convertNode(newObj.args.get(i));
            }
            String argList = String.join(", ", argTemps);
            String target  = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.NEW_OBJ,
                newObj.type.baseName, argList, target));
            return target;
        }

        // ----------------------------------------------------------------
        // 11. New Array (new int[size])
        // ----------------------------------------------------------------
        if (node instanceof NewArrExp) {
            NewArrExp newArr = (NewArrExp) node;
            String size   = newArr.size != null ? convertNode(newArr.size) : "0";
            String target = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.NEW_ARR,
                newArr.elementType.baseName, size, target));
            return target;
        }

        // ----------------------------------------------------------------
        // 12. Array Initializer ({1, 2, 3})
        // ----------------------------------------------------------------
        if (node instanceof ArrInitExp) {
            ArrInitExp arrInit = (ArrInitExp) node;
            // Convert each element and build a comma-separated value string
            // e.g. {1, 2, 3} → ARR_INIT "1, 2, 3" → target
            StringBuilder elements = new StringBuilder();
            for (int i = 0; i < arrInit.elements.size(); i++) {
                if (i > 0) elements.append(", ");
                elements.append(convertNode(arrInit.elements.get(i)));
            }
            String target = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.ARR_INIT,
                null, elements.toString(), target));
            return target;
        }

        // ----------------------------------------------------------------
        // 13. Array Access (arr[index])
        // ----------------------------------------------------------------
        if (node instanceof ArrAccessExp) {
            ArrAccessExp aa = (ArrAccessExp) node;
            String arr    = convertNode(aa.array);
            String index  = convertNode(aa.index);
            String target = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.ARR_ACCESS,
                index, arr, target));
            return target;
        }

        // ----------------------------------------------------------------
        // 13. Cast ((int) x)
        // ----------------------------------------------------------------
        if (node instanceof CastExp) {
            CastExp cast = (CastExp) node;
            String operand = convertNode(cast.operand);
            String target  = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.CAST,
                cast.targetType.baseName, operand, target));
            return target;
        }

        // ----------------------------------------------------------------
        // 14. Ternary (condition ? then : else)
        // ----------------------------------------------------------------
        if (node instanceof TernaryExp) {
            TernaryExp ternary = (TernaryExp) node;
            String cond   = convertNode(ternary.condition);
            String result = irGen.nextTemp();
            String elseLabel = irGen.nextLabel();
            String endLabel  = irGen.nextLabel();

            irGen.emit(new IRInstruction(IRInstruction.Type.CONDITIONAL_JUMP, cond, elseLabel));
            String thenVal = convertNode(ternary.thenBranch);
            irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN, null, thenVal, result));
            irGen.emit(new IRInstruction(IRInstruction.Type.JUMP, endLabel));
            irGen.emit(new IRInstruction(IRInstruction.Type.LABEL, elseLabel));
            String elseVal = convertNode(ternary.elseBranch);
            irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN, null, elseVal, result));
            irGen.emit(new IRInstruction(IRInstruction.Type.LABEL, endLabel));
            return result;
        }

        // ----------------------------------------------------------------
        // 15. Variable Declaration (int x = 5)
        // ----------------------------------------------------------------
        if (node instanceof VarDecl) {
            VarDecl decl = (VarDecl) node;
            if (decl.init != null) {
                String val = convertNode(decl.init);
                irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN,
                    null, val, decl.name.getLexeme()));
            }
            return null;
        }

        // ----------------------------------------------------------------
        // 16. Block Statement ({ ... })
        // ----------------------------------------------------------------
        if (node instanceof BlockStmt) {
            for (ASTNode s : ((BlockStmt) node).statements) convertNode(s);
            return null;
        }

        // ----------------------------------------------------------------
        // 17. Expression Statement (expression used as a statement)
        // ----------------------------------------------------------------
        if (node instanceof ExprStmt) {
            convertNode(((ExprStmt) node).exp);
            return null;
        }

        // ----------------------------------------------------------------
        // 18. If Statement
        // ----------------------------------------------------------------
        if (node instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) node;
            String cond      = convertNode(ifStmt.condition);
            String elseLabel = irGen.nextLabel();
            String endLabel  = irGen.nextLabel();

            // IF_FALSE cond GOTO elseLabel
            irGen.emit(new IRInstruction(IRInstruction.Type.CONDITIONAL_JUMP, cond, elseLabel));

            // then branch
            convertNode(ifStmt.thenBranch);
            irGen.emit(new IRInstruction(IRInstruction.Type.JUMP, endLabel));

            // else branch
            irGen.emit(new IRInstruction(IRInstruction.Type.LABEL, elseLabel));
            if (ifStmt.elseBranch != null) {
                convertNode(ifStmt.elseBranch);
            }
            irGen.emit(new IRInstruction(IRInstruction.Type.LABEL, endLabel));
            return null;
        }

        // ----------------------------------------------------------------
        // 19. While Statement
        // ----------------------------------------------------------------
        if (node instanceof WhileStmt) {
            WhileStmt whileStmt = (WhileStmt) node;
            String startLabel = irGen.nextLabel();
            String endLabel   = irGen.nextLabel();

            irGen.emit(new IRInstruction(IRInstruction.Type.LABEL, startLabel));
            String cond = convertNode(whileStmt.condition);
            irGen.emit(new IRInstruction(IRInstruction.Type.CONDITIONAL_JUMP, cond, endLabel));
            convertNode(whileStmt.body);
            irGen.emit(new IRInstruction(IRInstruction.Type.JUMP, startLabel));
            irGen.emit(new IRInstruction(IRInstruction.Type.LABEL, endLabel));
            return null;
        }

        // ----------------------------------------------------------------
        // 20. Do-While Statement
        // ----------------------------------------------------------------
        if (node instanceof DoWhileStmt) {
            DoWhileStmt doWhile = (DoWhileStmt) node;
            String startLabel = irGen.nextLabel();
            String endLabel   = irGen.nextLabel();

            irGen.emit(new IRInstruction(IRInstruction.Type.LABEL, startLabel));
            convertNode(doWhile.body);
            String cond = convertNode(doWhile.condition);
            // Jump back to start if condition is still true
            irGen.emit(new IRInstruction(IRInstruction.Type.CONDITIONAL_JUMP, cond, endLabel));
            irGen.emit(new IRInstruction(IRInstruction.Type.JUMP, startLabel));
            irGen.emit(new IRInstruction(IRInstruction.Type.LABEL, endLabel));
            return null;
        }

        // ----------------------------------------------------------------
        // 21. For Statement
        // ----------------------------------------------------------------
        if (node instanceof ForStmt) {
            ForStmt forStmt = (ForStmt) node;
            String startLabel = irGen.nextLabel();
            String endLabel   = irGen.nextLabel();

            // init
            if (forStmt.init != null) convertNode(forStmt.init);

            irGen.emit(new IRInstruction(IRInstruction.Type.LABEL, startLabel));

            // condition
            if (forStmt.condition != null) {
                String cond = convertNode(forStmt.condition);
                irGen.emit(new IRInstruction(IRInstruction.Type.CONDITIONAL_JUMP, cond, endLabel));
            }

            // body
            convertNode(forStmt.body);

            // update
            if (forStmt.update != null) convertNode(forStmt.update);

            irGen.emit(new IRInstruction(IRInstruction.Type.JUMP, startLabel));
            irGen.emit(new IRInstruction(IRInstruction.Type.LABEL, endLabel));
            return null;
        }

        // ----------------------------------------------------------------
        // 22. Return Statement
        // ----------------------------------------------------------------
        if (node instanceof ReturnStmt) {
            ReturnStmt ret = (ReturnStmt) node;
            if (ret.value != null) {
                String val = convertNode(ret.value);
                irGen.emit(new IRInstruction(IRInstruction.Type.RETURN, null, val, null));
            } else {
                irGen.emit(new IRInstruction(IRInstruction.Type.RETURN, null, null, null));
            }
            return null;
        }

        // ----------------------------------------------------------------
        // 23. Method Declaration
        // ----------------------------------------------------------------
        if (node instanceof MethodDecl) {
            MethodDecl method = (MethodDecl) node;

            // Build param string e.g. "int a, String b"
            StringBuilder params = new StringBuilder();
            for (int i = 0; i < method.params.size(); i++) {
                VarDecl param = method.params.get(i);
                if (i > 0) params.append(", ");
                String paramType = param.type.baseName;
                for (int d = 0; d < param.type.dimension; d++) paramType += "[]";
                params.append(paramType).append(" ").append(param.name.getLexeme());
            }

            // Build modifier string e.g. "public static"
            StringBuilder mods = new StringBuilder();
            for (int i = 0; i < method.modifiers.size(); i++) {
                if (i > 0) mods.append(" ");
                mods.append(method.modifiers.get(i).getLexeme());
            }

            String returnType = method.type != null ? method.type.baseName : "void";
            String methodName = method.name.getLexeme();

            irGen.emit(new IRInstruction(IRInstruction.Type.METHOD_START,
                mods.toString(), returnType + " " + methodName, params.toString(), null));

            if (method.body != null) convertNode(method.body);

            irGen.emit(new IRInstruction(IRInstruction.Type.METHOD_END, methodName));
            return null;
        }

        // ----------------------------------------------------------------
        // 24. Constructor Declaration
        // ----------------------------------------------------------------
        if (node instanceof ConstructorDecl) {
            ConstructorDecl ctor = (ConstructorDecl) node;

            StringBuilder params = new StringBuilder();
            for (int i = 0; i < ctor.params.size(); i++) {
                VarDecl param = ctor.params.get(i);
                if (i > 0) params.append(", ");
                String paramType = param.type.baseName;
                for (int d = 0; d < param.type.dimension; d++) paramType += "[]";
                params.append(paramType).append(" ").append(param.name.getLexeme());
            }

            StringBuilder mods = new StringBuilder();
            for (int i = 0; i < ctor.modifiers.size(); i++) {
                if (i > 0) mods.append(" ");
                mods.append(ctor.modifiers.get(i).getLexeme());
            }

            irGen.emit(new IRInstruction(IRInstruction.Type.METHOD_START,
                mods.toString(), ctor.name.getLexeme(), params.toString(), null));

            if (ctor.body != null) convertNode(ctor.body);

            irGen.emit(new IRInstruction(IRInstruction.Type.METHOD_END, ctor.name.getLexeme()));
            return null;
        }

        // ----------------------------------------------------------------
        // 25. Class Declaration
        // ----------------------------------------------------------------
        if (node instanceof ClassDecl) {
            ClassDecl cls = (ClassDecl) node;

            StringBuilder mods = new StringBuilder();
            for (int i = 0; i < cls.modifiers.size(); i++) {
                if (i > 0) mods.append(" ");
                mods.append(cls.modifiers.get(i).getLexeme());
            }

            String superClass = cls.superClass != null ? cls.superClass.baseName : null;

            irGen.emit(new IRInstruction(IRInstruction.Type.CLASS_START,
                mods.toString(), cls.name.getLexeme(), superClass));

            for (ASTNode member : cls.members) convertNode(member);

            irGen.emit(new IRInstruction(IRInstruction.Type.CLASS_END, cls.name.getLexeme()));
            return null;
        }

        // ----------------------------------------------------------------
        // 26. Break / Continue
        // ----------------------------------------------------------------
        if (node instanceof BreakStmt) {
            irGen.emit(new IRInstruction(IRInstruction.Type.BREAK, "break"));
            return null;
        }
        if (node instanceof ContinueStmt) {
            irGen.emit(new IRInstruction(IRInstruction.Type.CONTINUE, "continue"));
            return null;
        }

        // ----------------------------------------------------------------
        // 27. Throw Statement
        // ----------------------------------------------------------------
        if (node instanceof ThrowStmt) {
            ThrowStmt throwStmt = (ThrowStmt) node;
            String val = convertNode(throwStmt.value);
            irGen.emit(new IRInstruction(IRInstruction.Type.THROW, null, val, null));
            return null;
        }

        return null;
    }
}