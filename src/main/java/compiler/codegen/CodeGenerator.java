package main.java.compiler.codegen;

import main.java.compiler.ir.ASTToIRConverter;
import main.java.compiler.ir.IRGenerator;
import main.java.compiler.ir.IRInstruction;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.semantic.SemanticAnalyzer;
import main.java.compiler.semantic.SemanticError;

import java.util.ArrayList;
import java.util.List;

/**
 * CodeGenerator — Pipeline Coordinator
 *
 * This is the entry point for the code generation stage.
 * It connects the full pipeline and returns the final Java source code.
 *
 * Pipeline:
 *   List<ASTNode>  (from Parser)
 *       ↓
 *   SemanticAnalyzer  (validates the AST — stops if errors found)
 *       ↓
 *   ASTToIRConverter  (produces IRInstructions)
 *       ↓
 *   IRGenerator  (holds the List<IRInstruction>)
 *       ↓
 *   JavaEmitter  (translates IR → Java source string)
 *       ↓
 *   String  (valid Java code, ready for display or file output)
 */
public class CodeGenerator {

    private final IRGenerator irGen;
    private final ASTToIRConverter converter;
    private final JavaEmitter emitter;
    private final SemanticAnalyzer semantic;

    // Stores the last generated Java source code
    private String generatedCode = "";

    // Stores semantic errors from the last run
    private ArrayList<SemanticError> semanticErrors = new ArrayList<>();

    public CodeGenerator() {
        this.irGen     = new IRGenerator();
        this.converter = new ASTToIRConverter(irGen);
        this.emitter   = new JavaEmitter();
        this.semantic  = new SemanticAnalyzer();
    }

    /**
     * Main entry point.
     * Pass in the AST root (list of nodes from Parser.parse()).
     * Returns the generated Java source code as a String,
     * or an empty string if semantic errors were found.
     */
    public String generate(List<ASTNode> astNodes) {
        // Step 1: Reset state so previous runs don't bleed in
        irGen.reset();
        generatedCode = "";
        semanticErrors.clear();

        // Step 2: Run semantic analysis — validate the AST
        semantic.analyze(new ArrayList<>(astNodes));
        semanticErrors = semantic.getErrors();

        // Step 3: Stop here if semantic errors exist — don't generate bad code
        if (semantic.hasErrors()) {
            return "";
        }

        // Step 4: Pre-pass — collect variable types from VarDecl nodes
        // so JavaEmitter knows whether to write "int x" or "String x" etc.
        emitter.collectTypes(astNodes);

        // Step 5: Walk the AST and emit IR instructions
        converter.convert(astNodes);

        // Step 6: Get the instruction list and translate to Java source
        List<IRInstruction> instructions = irGen.getInstructions();
        generatedCode = emitter.emit(instructions);

        return generatedCode;
    }

    /**
     * Returns semantic errors from the last generate() call.
     * Check this if generate() returned an empty string.
     */
    public ArrayList<SemanticError> getSemanticErrors() {
        return semanticErrors;
    }

    /**
     * Returns true if the last generate() call had semantic errors.
     */
    public boolean hasSemanticErrors() {
        return !semanticErrors.isEmpty();
    }

    /**
     * Returns the last generated code without re-running the pipeline.
     */
    public String getGeneratedCode() {
        return generatedCode;
    }

    /**
     * Returns the IRGenerator — useful if the caller wants to print
     * or inspect the IR instructions alongside the generated code.
     */
    public IRGenerator getIRGenerator() {
        return irGen;
    }
}