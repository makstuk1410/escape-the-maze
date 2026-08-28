package management;

import entities.MazeObjects.Levels;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScoreManager {

    
    private static final String FILE_NAME = "score.txt";
    private static final Map<String, Integer> scores = new LinkedHashMap<>();

    static {
        loadScores();
    }

    private static void loadScores() {
        for (int i = 0; i < Levels.size(); i++) {
            scores.put(Levels.getLevel(i).getName(), 0);
        }

        Path file = Path.of(FILE_NAME);
        if (!Files.exists(file)) {
            saveScores();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length != 2) {
                    continue;
                }

                try {
                    int score = Integer.parseInt(parts[1].trim());
                    if (scores.containsKey(parts[0].trim()) && score >= 0) {
                        scores.put(parts[0].trim(), score);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load scores: " + e.getMessage());
        }
    }

    public static int getScore(String level) {
        return scores.getOrDefault(level, 0);
    }

    public static void updateScore(String level, int newScore) {
        int current = scores.getOrDefault(level, 0);
        if (newScore > current) {
            scores.put(level, newScore);
            saveScores();
        }
    }

    private static void saveScores() {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Path.of(FILE_NAME),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not save scores: " + e.getMessage());
        }
    }
}
