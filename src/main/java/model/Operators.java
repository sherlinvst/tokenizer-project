package main.java.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Operators extends Tokens {
    private final File tokenFile = new File("src/main/java/database/operators.txt");
    private final ArrayList<String> operators = new ArrayList<>();

    public Operators(){
        retrieveTokens();
    }

    @Override
    public void retrieveTokens() {
        if (tokenFile.exists()) {
            try (Scanner scanner = new Scanner(tokenFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    operators.add(line);
                }
            } catch (Exception e) {
                System.err.println("An error occurred while reading the operators file: " + e.getMessage());
            }
        } else {
            throw new Error("File not found.");
        }
    }

    @Override
    public String getToken(String lexeme) {
        for (String operator : operators) {
            String[] op = operator.split(",");
            if (op[0].equals(lexeme)) {
                return op[1];
            }
        }

        return null;
    }
}
