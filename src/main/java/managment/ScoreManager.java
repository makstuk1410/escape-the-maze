package managment;

import entities.MazeObjects.Levels;
import java.io.*;
import java.util.*;

public class ScoreManager {

    private static int currentScore = 0;
    
    private static final String FILE_NAME = "score.txt";
    private static final Map<String, Integer> scores = new HashMap<>();

    static {
        loadScores();
    }

    private static void loadScores() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            // створити файл з нульовими значеннями
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (int i = 0; i < Levels.size(); i++) {
                    String name = Levels.getLevel(i).getName();
                    writer.println(name + "=0");
                    scores.put(name, 0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        scores.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getCurrentScore() {
        return currentScore;
    }

    public static void setCurrentScore(int curScore) {
        currentScore = curScore;
    }
    
    public static void addScore(int plusScore){
        currentScore += plusScore;
    }
}
