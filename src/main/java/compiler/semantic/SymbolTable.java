package main.java.compiler.semantic;

import java.util.ArrayList;

public class SymbolTable {
    public static class Symbol {
        public final String name;
        public final String kind;       // "variable", "method", "class", "parameter"
        public final String typeName;   // "int", "String", "void", "MyClass", etc.
        public final int arrayDims;     // 0 = not array, 1 = int[], 2 = int[][]
        public final int line, col;

        public Symbol(String name, String kind, String typeName, int arrayDims, int line, int col) {
            this.name = name;
            this.kind = kind;
            this.typeName = typeName;
            this.arrayDims = arrayDims;
            this.line = line;
            this.col = col;
        }

        @Override
        public String toString() {
            return kind + " " + typeName + (arrayDims > 0 ? "[]".repeat(arrayDims) : "") + " " + name;
        }
    }

    // Stack of scopes — each scope is a flat list of symbols
    // Using ArrayList<ArrayList<Symbol>>
    private final ArrayList<ArrayList<Symbol>> scopes = new ArrayList<>();

    // Name of the class currently being analyzed — used for constructor checks
    private String currentClassName = null;

    // Return type of the method currently being analyzed — used for return checks
    private String currentReturnType = null;

    public void setCurrentClassName(String name) { this.currentClassName  = name; }
    public void setCurrentReturnType(String type) { this.currentReturnType = type; }
    public String getCurrentClassName() { return currentClassName; }
    public String getCurrentReturnType() { return currentReturnType; }

    // --- Scope management ---

    public void enterScope() {
        scopes.add(new ArrayList<>());
    }

    public void exitScope() {
        if (!scopes.isEmpty()) {
            scopes.remove(scopes.size() - 1);
        }
    }

    public int scopeDepth() { return scopes.size(); }

    // --- Declaration ---

    // Returns null if declared successfully, or an error message if duplicate
    public String declare(Symbol symbol) {
        if (scopes.isEmpty()) return "No active scope";

        ArrayList<Symbol> current = scopes.get(scopes.size() - 1);

        // Check for duplicate in the CURRENT scope only
        // (shadowing an outer scope is allowed — same scope is not)
        for (Symbol s : current) {
            if (s.name.equals(symbol.name)) {
                return "'" + symbol.name + "' is already declared in this scope "
                       + "(previously declared at line " + s.line + ")";
            }
        }

        current.add(symbol);
        return null; // success
    }

    // --- Resolution ---

    // Search from innermost scope outward — returns null if not found
    public Symbol resolve(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            for (Symbol s : scopes.get(i)) {
                if (s.name.equals(name)) return s;
            }
        }
        return null;
    }

    // Resolve only in the current (innermost) scope
    public Symbol resolveLocal(String name) {
        if (scopes.isEmpty()) return null;
        ArrayList<Symbol> current = scopes.get(scopes.size() - 1);
        for (Symbol s : current) {
            if (s.name.equals(name)) return s;
        }
        return null;
    }

    // Check if a class name is declared anywhere
    public Symbol resolveClass(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            for (Symbol s : scopes.get(i)) {
                if (s.name.equals(name) && s.kind.equals("class")) return s;
            }
        }
        return null;
    }
}
