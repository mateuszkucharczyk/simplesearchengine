package com.protonmail.mateuszkucharczyk.simplesearchengine;

import java.util.List;
import java.util.Scanner;

import static java.util.Arrays.asList;

public class Application {
    public static void main(String[] args) {
        new Application().run(args);
    }

    private static final String ADD_COMMAND = "add";
    private static final String SEARCH_COMMAND = "search";
    private static final String QUIT_COMMAND = "quit";

    private SimpleSearchEngine searchEngine = new SimpleSearchEngine(asList(
            new Document("the brown fox jumped over the brown dog"),
            new Document("the lazy brown dog sat in the corner"),
            new Document("the red fox bit the lazy dog")
    ));

    private void run(String[] args) {
        printHelp();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print('>');
            String line = scanner.nextLine();
            String[] commandAndParameters = line.split("\\s", 2);
            String command = commandAndParameters[0];
            switch (command) {
                case ADD_COMMAND:
                    searchEngine.addDocument(documentFrom(commandAndParameters));
                    break;
                case SEARCH_COMMAND:
                    List<Document> documents = searchEngine.search(termsFrom(commandAndParameters));
                    documents.forEach(System.out::println);
                    break;
                case QUIT_COMMAND:
                    return;
                default:
                    printHelp();
            }
        }
    }

    private static Document documentFrom(String[] commandAndParameters) {
        if (commandAndParameters.length < 2) {
            return new Document("");
        }
        String content = commandAndParameters[1];
        return new Document(content);
    }

    private static String[] termsFrom(String[] commandAndParameters) {
        if (commandAndParameters.length < 2) {
            return new String[0];
        }
        String parameters = commandAndParameters[1];
        return parameters.split("\\s");
    }

    private static void printHelp() {

        System.out.println("usages:");
        System.out.println('\t' + ADD_COMMAND + " the noisy chicken jumped over the quick cat");
        System.out.println('\t' + SEARCH_COMMAND + " brown dog");
        System.out.println('\t' + QUIT_COMMAND);
    }
}
