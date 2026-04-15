package main.java.compiler.parser;

public class ParseError extends RuntimeException {
    private final int lineNumber;
    private final int columnNumber;

    public ParseError(String message, int lineNumber, int columnNumber) {
        super(message);
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

}
