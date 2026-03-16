package main.java.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Separators extends Tokens{
    private final File tokenFile = new File("src/main/java/database/separators.txt");
    private final ArrayList<String> separators = new ArrayList<>();

    public Separators(){
        retrieveTokens();
    }

    @Override
    public void retrieveTokens() {
        if (tokenFile.exists()) {
            try (Scanner scanner = new Scanner(tokenFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    separators.add(line);
                }
            } catch (Exception e) {
                System.err.println("An error occurred while reading the separators file: " + e.getMessage());
            }
        } else {
            throw new Error("File not found.");
        }
    }

    @Override
    public String getToken(String lexeme) {
        for (String separator : separators) {
            String[] op = separator.split(",");
            if (op[0].equals(lexeme)) {
                return op[1];
            }
        }

        return null;
    }
}
