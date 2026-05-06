package main.java.compiler.semantic;

import java.util.ArrayList;

public class SymbolTable {
    public static class Symbol {
        public final String name;
        public final String kind;       
        public final String typeName;   
        public final int arrayDims;     
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

    private final ArrayList<ArrayList<Symbol>> scopes = new ArrayList<>();
    private String currentClassName = null;
    private String currentReturnType = null;

    public void setCurrentClassName(String name) { this.currentClassName  = name; }
    public void setCurrentReturnType(String type) { this.currentReturnType = type; }
    public String getCurrentClassName() { return currentClassName; }
    public String getCurrentReturnType() { return currentReturnType; }

    public void enterScope() {
        scopes.add(new ArrayList<>());
    }

    public void exitScope() {
        if (!scopes.isEmpty()) {
            scopes.remove(scopes.size() - 1);
        }
    }

    public int scopeDepth() { return scopes.size(); }

    public String declare(Symbol symbol) {
        if (scopes.isEmpty()) return "No active scope";

        ArrayList<Symbol> current = scopes.get(scopes.size() - 1);
        for (Symbol s : current) {
            if (s.name.equals(symbol.name)) {
                return "'" + symbol.name + "' is already declared in this scope "
                       + "(previously declared at line " + s.line + ")";
            }
        }

        current.add(symbol);
        return null;
    }

    public Symbol resolve(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            for (Symbol s : scopes.get(i)) {
                if (s.name.equals(name)) return s;
            }
        }
        return null;
    }

    public Symbol resolveLocal(String name) {
        if (scopes.isEmpty()) return null;
        ArrayList<Symbol> current = scopes.get(scopes.size() - 1);
        for (Symbol s : current) {
            if (s.name.equals(name)) return s;
        }
        return null;
    }

    public Symbol resolveClass(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            for (Symbol s : scopes.get(i)) {
                if (s.name.equals(name) && s.kind.equals("class")) return s;
            }
        }
        return null;
    }
}
