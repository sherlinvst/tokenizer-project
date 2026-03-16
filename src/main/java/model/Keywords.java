package main.java.model;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;

public class Keywords extends Tokens{
    private final File tokenFile = new File("src/main/java/database/keywords.txt");
    private final ArrayList<String> keywords = new ArrayList<>();
    private final HashMap <String,Integer> alphabetIndex = new HashMap<>();

    public Keywords(){
        retrieveTokens();
    }

    @Override
    public void retrieveTokens() {
        if (tokenFile.exists()) {
            try (Scanner scanner = new Scanner(tokenFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String firstChar = line.substring(0, 1);

                    if (!alphabetIndex.containsKey(firstChar)) {
                        alphabetIndex.put(firstChar, keywords.size() - 1);
                    }

                    keywords.add(line);
                }
            } catch (Exception e) {
                System.err.println("An error occurred while reading the keywords file: " + e.getMessage());
            }
        } else {
            throw new Error("File not found.");
        }
    }

    private int getIndex (int startIndex, String lexeme){
        for (int i = startIndex; i < keywords.size(); i++){
            String[] word = keywords.get(i).split(",");
            if (word[0].equals(lexeme)){
                return i;
            }
        }
        return -1;
    }

    @Override
    public String getToken(String lexeme) {
        String firstChar = lexeme.substring(0, 1);
        Integer startIndex = alphabetIndex.get(firstChar);
        if (startIndex == null) return null;

        int index = getIndex(startIndex, lexeme);
        if (index != -1) {
            String[] keyword = keywords.get(index).split(",");
            return keyword[1];
        }

        return null;
    }
}
