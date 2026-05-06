package main.java.compiler.codegen;

import main.java.compiler.ir.ASTToIRConverter;
import main.java.compiler.ir.IRGenerator;
import main.java.compiler.ir.IRInstruction;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.semantic.SemanticAnalyzer;
import main.java.compiler.semantic.SemanticError;

import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {

    private final IRGenerator irGen;
    private final ASTToIRConverter converter;
    private final JavaEmitter emitter;
    private final SemanticAnalyzer semantic;
    private String generatedCode = "";
    private ArrayList<SemanticError> semanticErrors = new ArrayList<>();

    public CodeGenerator() {
        this.irGen     = new IRGenerator();
        this.converter = new ASTToIRConverter(irGen);
        this.emitter   = new JavaEmitter();
        this.semantic  = new SemanticAnalyzer();
    }

    public String generate(List<ASTNode> astNodes) {
        irGen.reset();
        generatedCode = "";
        semanticErrors.clear();
        semantic.analyze(new ArrayList<>(astNodes));
        semanticErrors = semantic.getErrors();
        if (semantic.hasErrors()) {
            return "";
        }

        emitter.collectTypes(astNodes);
        converter.convert(astNodes);

        List<IRInstruction> instructions = irGen.getInstructions();
        generatedCode = emitter.emit(instructions);

        return generatedCode;
    }

    public ArrayList<SemanticError> getSemanticErrors() {
        return semanticErrors;
    }

    public boolean hasSemanticErrors() {
        return !semanticErrors.isEmpty();
    }

    public String getGeneratedCode() {
        return generatedCode;
    }

    public IRGenerator getIRGenerator() {
        return irGen;
    }
}