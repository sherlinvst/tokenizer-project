package test;

import main.java.compiler.codegen.CodeGenerator;
import main.java.compiler.parser.Parser;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.semantic.SemanticError;
import main.java.model.Token;
import main.java.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;

public class CodeGenTest {

    // ----------------------------------------------------------------
    // Helper — runs the full pipeline on a list of source lines
    // and prints the IR and generated Java code
    // ----------------------------------------------------------------
    static void runTest(String testName, ArrayList<String> lines) {
        System.out.println("\n#########################################");
        System.out.println("### TEST: " + testName);
        System.out.println("#########################################");

        System.out.println("\n--- SIMULATED SOURCE CODE ---");
        for (String line : lines) System.out.println(line);

        // Step 1: Tokenize
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenizeLines(lines, 1);
        ArrayList<Token> tokens = tokenizer.getTokens();

        // Step 2: Parse
        Parser parser = new Parser(tokens);
        List<ASTNode> root = parser.parse();

        if (!parser.getErrors().isEmpty()) {
            System.err.println("PARSER ERRORS:");
            parser.getErrors().forEach(err -> System.err.println("  " + err));
            return;
        }
        System.out.println("\nParser: OK");

        // Step 3: Full pipeline
        CodeGenerator codeGen = new CodeGenerator();
        try {
            String javaCode = codeGen.generate(root);

            if (codeGen.hasSemanticErrors()) {
                System.err.println("SEMANTIC ERRORS:");
                for (SemanticError err : codeGen.getSemanticErrors()) {
                    System.err.println("  " + err.getMessage());
                }
                return;
            }
            System.out.println("Semantic Analysis: OK");

            System.out.println("\n--- GENERATED IR ---");
            codeGen.getIRGenerator().printIR();

            System.out.println("\n--- GENERATED JAVA SOURCE CODE ---");
            System.out.println(javaCode);

        } catch (Exception e) {
            System.err.println("Error during code generation:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        // ----------------------------------------------------------------
        // Test 1: Hello World
        // ----------------------------------------------------------------
        ArrayList<String> test1 = new ArrayList<>();
        test1.add("public class Main {");
        test1.add("    public static void main(String[] args) {");
        test1.add("        System.out.println(\"Hello, World!\");");
        test1.add("    }");
        test1.add("}");
        runTest("Hello World", test1);

        // ----------------------------------------------------------------
        // Test 2: If/Else Statement
        // ----------------------------------------------------------------
        ArrayList<String> test2 = new ArrayList<>();
        test2.add("public class Main {");
        test2.add("    public static void main(String[] args) {");
        test2.add("        int x = 10;");
        test2.add("        if (x > 5) {");
        test2.add("            System.out.println(\"big\");");
        test2.add("        } else {");
        test2.add("            System.out.println(\"small\");");
        test2.add("        }");
        test2.add("    }");
        test2.add("}");
        runTest("If/Else Statement", test2);

        // ----------------------------------------------------------------
        // Test 3: For Loop
        // ----------------------------------------------------------------
        ArrayList<String> test3 = new ArrayList<>();
        test3.add("public class Main {");
        test3.add("    public static void main(String[] args) {");
        test3.add("        for (int i = 0; i < 5; i++) {");
        test3.add("            System.out.println(i);");
        test3.add("        }");
        test3.add("    }");
        test3.add("}");
        runTest("For Loop", test3);

        System.out.println("\n#########################################");
        System.out.println("### ALL TESTS DONE");
        System.out.println("#########################################");
    }
}

/*
EXPECTED OUTPUT FOR EACH TEST:

=== Test 1: Hello World ===
public class Main {
    public static void main(String[] args) {
        var t1 = System.out;
        t1.println("Hello, World!");
    }
}

=== Test 2: If/Else ===
public class Main {
    public static void main(String[] args) {
        int x = 10;
        // conditional jump logic
        var t1 = System.out;
        t1.println("big");
        // else branch
        var t2 = System.out;
        t2.println("small");
    }
}

=== Test 3: For Loop ===
public class Main {
    public static void main(String[] args) {
        int i = 0;
        // loop with condition i < 5
        var t1 = System.out;
        t1.println(i);
        // i++
    }
}
*/