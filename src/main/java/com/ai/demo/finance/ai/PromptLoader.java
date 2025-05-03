package com.ai.demo.finance.ai;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PromptLoader {
    private static final String PROMPT_FOLDER = "ai/prompts";

    private static final Map<Prompts, String> PROMPTS = loadPrompts();

    private PromptLoader() {
    }

    private static Map<Prompts, String> loadPrompts() {
        try {
            var uri = Objects.requireNonNull(
                    PromptLoader.class.getClassLoader().getResource(PROMPT_FOLDER),
                    "Prompt folder not found in classpath").toURI();

            Path folderPath;
            if (uri.getScheme().equals("jar")) {
                // For reading from JAR file in production
                try (FileSystem fs = FileSystems.newFileSystem(uri, Map.of())) {
                    folderPath = fs.getPath(PROMPT_FOLDER);
                }
            } else {
                // Local development
                folderPath = Paths.get(uri);
            }

            try (var stream = Files.list(folderPath)) {
                return stream
                        .filter(path -> path.toString().endsWith(".txt"))
                        .collect(Collectors.toUnmodifiableMap(
                                path -> stripExtension(path.getFileName().toString()),
                                PromptLoader::readFile));
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load prompt files", e);
        }
    }

    private static Prompts stripExtension(String filename) {
        return Prompts.valueOf(filename.replaceFirst("\\.txt$", "").toUpperCase());
    }

    private static String readFile(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read prompt file: " + path, e);
        }
    }

    public static String get(Prompts key) {

        if (PROMPTS.containsKey(key)) {
            return PROMPTS.get(key);
        }

        throw new IllegalArgumentException("Prompt not found: " + key);
    }

    public enum Prompts {
        RECOMMENDATIONS,
        BUDGETS
    }
}
