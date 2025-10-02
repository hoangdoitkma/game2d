package com.example.templerunclone.managers;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Manager để quản lý high scores
 */
public class HighScoreManager {
    private static final String PREFS_NAME = "HighScores";
    private static final String SCORES_KEY = "scores";
    private static final int MAX_SCORES = 8;
    
    private Context context;
    private List<HighScore> highScores;
    
    public HighScoreManager(Context context) {
        this.context = context;
        this.highScores = new ArrayList<>();
        loadHighScores();
    }
    
    public static class HighScore {
        public int score;
        public int level;
        public long timestamp;
        
        public HighScore(int score, int level, long timestamp) {
            this.score = score;
            this.level = level;
            this.timestamp = timestamp;
        }
        
        public HighScore(String data) {
            String[] parts = data.split(",");
            if (parts.length >= 3) {
                this.score = Integer.parseInt(parts[0]);
                this.level = Integer.parseInt(parts[1]);
                this.timestamp = Long.parseLong(parts[2]);
            }
        }
        
        @Override
        public String toString() {
            return score + "," + level + "," + timestamp;
        }
    }
    
    private void loadHighScores() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String scoresData = prefs.getString(SCORES_KEY, "");
        
        highScores.clear();
        
        if (!scoresData.isEmpty()) {
            String[] scores = scoresData.split(";");
            for (String scoreData : scores) {
                if (!scoreData.trim().isEmpty()) {
                    try {
                        highScores.add(new HighScore(scoreData));
                    } catch (Exception e) {
                        // Skip invalid entries
                    }
                }
            }
        }
        
        // Sort by score descending
        Collections.sort(highScores, new Comparator<HighScore>() {
            @Override
            public int compare(HighScore a, HighScore b) {
                return Integer.compare(b.score, a.score);
            }
        });
    }
    
    private void saveHighScores() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < highScores.size(); i++) {
            if (i > 0) sb.append(";");
            sb.append(highScores.get(i).toString());
        }
        
        editor.putString(SCORES_KEY, sb.toString());
        editor.apply();
    }
    
    public boolean addScore(int score, int level) {
        HighScore newScore = new HighScore(score, level, System.currentTimeMillis());
        
        // Add the new score
        highScores.add(newScore);
        
        // Sort by score descending
        Collections.sort(highScores, new Comparator<HighScore>() {
            @Override
            public int compare(HighScore a, HighScore b) {
                return Integer.compare(b.score, a.score);
            }
        });
        
        // Keep only top MAX_SCORES
        boolean isHighScore = false;
        if (highScores.size() > MAX_SCORES) {
            // Check if the new score made it to top 8
            for (int i = 0; i < MAX_SCORES; i++) {
                if (highScores.get(i) == newScore) {
                    isHighScore = true;
                    break;
                }
            }
            // Remove excess scores
            while (highScores.size() > MAX_SCORES) {
                highScores.remove(highScores.size() - 1);
            }
        } else {
            isHighScore = true;
        }
        
        saveHighScores();
        return isHighScore;
    }
    
    public List<HighScore> getHighScores() {
        return new ArrayList<>(highScores);
    }
    
    public void clearHighScores() {
        highScores.clear();
        saveHighScores();
    }
    
    public int getHighestScore() {
        if (highScores.isEmpty()) {
            return 0;
        }
        return highScores.get(0).score;
    }
    
    public boolean isHighScore(int score) {
        if (highScores.size() < MAX_SCORES) {
            return true;
        }
        return score > highScores.get(MAX_SCORES - 1).score;
    }
}