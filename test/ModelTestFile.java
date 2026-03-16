package test;

import main.java.model.Keywords;
import main.java.model.Operators;
import main.java.model.Separators;

public class ModelTestFile {
    public static void main(String[] args) {
        Keywords keywords = new Keywords();
        System.out.println(keywords.getToken("if"));
        Operators operator = new Operators();
        System.out.println(operator.getToken("="));
        Separators separator = new Separators();
        System.out.println(separator.getToken(";"));
    }
}
