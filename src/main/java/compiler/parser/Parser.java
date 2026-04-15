// package main.java.compiler.parser;
// import main.java.model.Token;
// import java.util.List;

// public class Parser {
//     private List<Token> tokens;
//     private int current = 0;

//     public Parser(List<Token> tokens) {
//         this.tokens = tokens;
//     }

//     private Token peek() {
//         // check current token
//         return tokens.get(current);
//     }

//     private Token next(){
//         // move to next token
//         return tokens.get(current++);
//     }

//     private boolean check (String type) {
//         // check if current token matches type
//         return false;
//     }

//     private boolean match(String... types) {
//         // if matches then consume
//         return false;
//     }

//     private Token expect (String type) {
//         // if matches then consume else throw error
//         return null;
//     }

//     public List<ASTNode> parse() {
//         // parse tokens into AST
//         return null;
//     }

//     private ASTNode parseExpression() {
//         // parse expression
//         return null;
//     }

//     private ASTNode parseStatement() {
//         // parse primary expression
//         return null;
//     }

//     private ASTNode parsePrimary() {
//         // parse statement
//         return null;
//     }

//     private ClassDecl parseClassDecl() {
//         // parse class declaration
//         return null;
//     }
// }
