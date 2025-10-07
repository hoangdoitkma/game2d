package com.example.templerunclone.levels;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.example.templerunclone.managers.ResourceManager;
import com.example.templerunclone.entities.Player;
import com.example.templerunclone.managers.EnemyManager;
import com.example.templerunclone.managers.PowerUpManager;

/**
 * Manages level progression, transitions, and state persistence
 */
public class LevelManager {
    private static final String TAG = "LevelManager";
    
    private int currentLevel;
    private LevelConfig currentLevelConfig;
    private LevelConfig nextLevelConfig;
    
    // Player state persistence
    private PlayerState savedPlayerState;
    
    // Managers that need level-specific updates
    private ResourceManager resourceManager;
    private EnemyManager enemyManager;
    private PowerUpManager powerUpManager;
    
    // Screen dimensions
    private int screenWidth, screenHeight;
    
    // Level transition state
    private boolean isTransitioning = false;
    private float transitionProgress = 0f;
    private String transitionType = ""; // "fade", "slide", etc.
    // Flag to signal completion in the next update tick
    private boolean transitionJustCompleted = false;
    
    public LevelManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.currentLevel = 1;
        this.currentLevelConfig = new LevelConfig(1);
        this.savedPlayerState = new PlayerState();
    }
    
    public void setManagers(ResourceManager resourceManager, EnemyManager enemyManager, PowerUpManager powerUpManager) {
        this.resourceManager = resourceManager;
        this.enemyManager = enemyManager;
        this.powerUpManager = powerUpManager;
        
        // Set level manager reference in powerup manager
        if (powerUpManager != null) {
            powerUpManager.setLevelManager(this);
        }
    }
    
    /**
     * Initialize the current level
     */
    public void initializeLevel(Context context) {
        Log.d(TAG, "Initializing level " + currentLevel + ": " + currentLevelConfig.getLevelName());
        
        // Load level-specific resources
        if (resourceManager != null) {
            resourceManager.loadLevelResources(context, currentLevelConfig);
            Log.d(TAG, "Level resources loaded for " + currentLevelConfig.getLevelName());
        }
        
        // Configure enemy manager for this level
        if (enemyManager != null) {
            enemyManager.configureLevelSettings(
                currentLevelConfig.getEnemySpawnRate(),
                currentLevelConfig.getEnemySpeed(),
                currentLevelConfig.getEnemyHealth(),
                currentLevelConfig.getMaxEnemies()
            );
        }
        
        // Configure power-up manager for this level
        if (powerUpManager != null) {
            powerUpManager.configureLevelSettings(
                currentLevelConfig.getPowerUpSpawnRate(),
                currentLevelConfig.getAvailablePowerUps()
            );
        }
        
        Log.d(TAG, "Level " + currentLevel + " initialized successfully");
    }
    
    /**
     * Check if player should advance to next level
     */
    public boolean shouldAdvanceLevel(int currentScore) {
        return currentScore >= currentLevelConfig.getScoreToNextLevel() && !isTransitioning;
    }
    
    /**
     * Start transition to next level
     */
    public void startLevelTransition(Player player) {
        if (currentLevel >= 3) {
            Log.d(TAG, "Max level reached, cannot advance further");
            return;
        }
        
        Log.d(TAG, "Starting transition from level " + currentLevel + " to " + (currentLevel + 1));
        
        // Save current player state
        savePlayerState(player);
        
        // Prepare next level
        nextLevelConfig = new LevelConfig(currentLevel + 1);
        
        // Start transition animation
        isTransitioning = true;
        transitionProgress = 0f;
        transitionType = "fade";
    }
    
    /**
     * Update transition animation
     */
    public void updateTransition(float deltaTime) {
        if (!isTransitioning) return;
        
        // Update transition progress (2 second transition)
        transitionProgress += deltaTime / 2000f;
        
        if (transitionProgress >= 1f) {
            // Transition complete
            completeTransition();
        }
    }
    
    /**
     * Complete the level transition
     */
    private void completeTransition() {
        currentLevel++;
        currentLevelConfig = nextLevelConfig;
        nextLevelConfig = null;
        
        isTransitioning = false;
        transitionProgress = 0f;
        transitionJustCompleted = true; // signal completion for consumers
        
        Log.d(TAG, "Transition completed. Now at level " + currentLevel);
        
        // Initialize the new level
        // Note: initializeLevel(context) should be called from GameEngine after this
    }

    /**
     * Returns true exactly once after a transition completes, then resets the flag.
     */
    public boolean consumeTransitionJustCompleted() {
        if (transitionJustCompleted) {
            transitionJustCompleted = false;
            return true;
        }
        return false;
    }
    
    /**
     * Restore player state from saved data
     */
    public void restorePlayerState(Player player) {
        if (savedPlayerState != null) {
            player.setHealth(savedPlayerState.health);
            player.setMaxHealth(savedPlayerState.maxHealth);
            player.setSpeed(savedPlayerState.speed);
            // Position will be reset to level start position
            
            Log.d(TAG, "Player state restored: Health=" + savedPlayerState.health + 
                      ", MaxHealth=" + savedPlayerState.maxHealth + ", Speed=" + savedPlayerState.speed);
        }
    }
    
    /**
     * Save current player state
     */
    private void savePlayerState(Player player) {
        savedPlayerState.health = player.getHealth();
        savedPlayerState.maxHealth = player.getMaxHealth();
        savedPlayerState.speed = player.getSpeed();
        
        Log.d(TAG, "Player state saved: Health=" + savedPlayerState.health + 
                  ", MaxHealth=" + savedPlayerState.maxHealth + ", Speed=" + savedPlayerState.speed);
    }
    
    /**
     * Draw level transition effect
     */
    public void drawTransition(Canvas canvas, Paint paint) {
        if (!isTransitioning) return;
        
        switch (transitionType) {
            case "fade":
                drawFadeTransition(canvas, paint);
                break;
            // Add more transition types as needed
        }
    }
    
    private void drawFadeTransition(Canvas canvas, Paint paint) {
        // Fade out effect
        int alpha = (int) (255 * Math.sin(transitionProgress * Math.PI));
        paint.setColor(android.graphics.Color.argb(alpha, 0, 0, 0));
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
        
        // Draw level name in center during transition
        if (transitionProgress > 0.3f && transitionProgress < 0.7f) {
            paint.setColor(android.graphics.Color.WHITE);
            paint.setTextSize(60);
            paint.setTextAlign(Paint.Align.CENTER);
            
            String levelText = "Level " + currentLevel;
            String nameText = nextLevelConfig != null ? nextLevelConfig.getLevelName() : "";
            
            canvas.drawText(levelText, screenWidth / 2f, screenHeight / 2f - 30, paint);
            canvas.drawText(nameText, screenWidth / 2f, screenHeight / 2f + 30, paint);
        }
    }
    
    /**
     * Reset to level 1
     */
    public void resetToLevel1() {
        currentLevel = 1;
        currentLevelConfig = new LevelConfig(1);
        isTransitioning = false;
        transitionProgress = 0f;
        savedPlayerState = new PlayerState();
        
        Log.d(TAG, "Reset to level 1");
    }
    
    /**
     * Get level-specific bullet configuration
     */
    public BulletConfig getBulletConfig() {
        return new BulletConfig(
            currentLevelConfig.getBulletSpeed(),
            currentLevelConfig.getBulletDamage(),
            currentLevelConfig.getBulletImagePath()
        );
    }
    
    // Getters
    public int getCurrentLevel() { return currentLevel; }
    public LevelConfig getCurrentLevelConfig() { return currentLevelConfig; }
    public boolean isTransitioning() { return isTransitioning; }
    public float getTransitionProgress() { return transitionProgress; }
    
    /**
     * Inner class to store player state between levels
     */
    private static class PlayerState {
        int health = 3;
        int maxHealth = 3;
        float speed = 300f;
    }
    
    /**
     * Inner class for bullet configuration
     */
    public static class BulletConfig {
        public final float speed;
        public final int damage;
        public final String imagePath;
        
        public BulletConfig(float speed, int damage, String imagePath) {
            this.speed = speed;
            this.damage = damage;
            this.imagePath = imagePath;
        }
    }
}