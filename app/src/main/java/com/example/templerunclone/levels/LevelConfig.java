package com.example.templerunclone.levels;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.example.templerunclone.entities.Enemy;

/**
 * Level configuration data for different game levels
 */
public class LevelConfig {
    private int levelNumber;
    private String levelName;
    private String description;
    
    // Graphics assets paths
    private String backgroundImagePath;
    private String playerImagePath;
    private String enemyImagePath;
    private String bulletImagePath;
    private String powerUpImagePath;
    
    // Gameplay mechanics
    private float playerSpeed;
    private float bulletSpeed;
    private int bulletDamage;
    private float enemySpawnRate;
    private float enemySpeed;
    private int enemyHealth;
    private float powerUpSpawnRate;
    
    // Level progression
    private int scoreToNextLevel;
    private int maxEnemies;
    private boolean hasSpecialWeapons;
    private String[] availablePowerUps;
    
    // Visual effects
    private int backgroundColor;
    private float particleEffectIntensity;
    private String musicPath;
    private String[] soundEffectPaths;
    
    public LevelConfig(int levelNumber) {
        this.levelNumber = levelNumber;
        initializeLevel();
    }
    
    private void initializeLevel() {
        switch (levelNumber) {
            case 1:
                setupLevel1();
                break;
            case 2:
                setupLevel2();
                break;
            case 3:
                setupLevel3();
                break;
            default:
                setupDefaultLevel();
                break;
        }
    }
    
    private void setupLevel1() {
        levelName = "Forest Temple";
        description = "Begin your journey through the ancient forest temple";
        
        // Graphics paths - using actual drawable resources (now in main drawable folder)
        backgroundImagePath = "background_level_1";
        playerImagePath = "player_level_1";
        enemyImagePath = "enemy_level_1";
        bulletImagePath = "bullet_level_1";
        powerUpImagePath = "powerup_level_1"; // Will fallback to color bitmap if not found

        // Gameplay settings - Easy level
        playerSpeed = 300f;
        bulletSpeed = 600f;
        bulletDamage = 1;
        enemySpawnRate = 2.0f; // seconds between spawns
        enemySpeed = 150f;
        enemyHealth = 1;
        powerUpSpawnRate = 0.3f; // 30% chance on enemy death
        
        // Progression
        scoreToNextLevel = 150;
        maxEnemies = 3;
        hasSpecialWeapons = false;
        availablePowerUps = new String[]{"health", "speed", "shield"};
        
        // Visual
        backgroundColor = 0xFF2E5D31; // Dark forest green
        particleEffectIntensity = 0.5f;
        musicPath = "res/raw/level1_forest_music.ogg";
        soundEffectPaths = new String[]{
            "res/raw/level1_arrow_shoot.wav",
            "res/raw/level1_enemy_death.wav"
        };
    }
    
    private void setupLevel2() {
        levelName = "Desert Ruins";
        description = "Navigate through the scorching desert ruins";
        
        // Graphics paths - using actual drawable resources (now in main drawable folder)
        backgroundImagePath = "background_level_2";
        playerImagePath = "player_level_2";
        enemyImagePath = "enemy_level_2";
        bulletImagePath = "bullet_level_2";
        powerUpImagePath = "powerup_level_2"; // Will fallback to color bitmap if not found

        // Gameplay settings - Medium difficulty
        playerSpeed = 350f;
        bulletSpeed = 800f;
        bulletDamage = 2;
        enemySpawnRate = 1.5f;
        enemySpeed = 200f;
        enemyHealth = 2;
        powerUpSpawnRate = 0.25f;
        
        // Progression
        scoreToNextLevel = 300;
        maxEnemies = 5;
        hasSpecialWeapons = true;
        availablePowerUps = new String[]{"health", "speed", "shield", "multishot", "rapidfire"};
        
        // Visual
        backgroundColor = 0xFFD2691E; // Sandy brown
        particleEffectIntensity = 0.8f;
        musicPath = "res/raw/level2_desert_music.ogg";
        soundEffectPaths = new String[]{
            "res/raw/level2_fire_shoot.wav",
            "res/raw/level2_enemy_death.wav",
            "res/raw/level2_sandstorm.wav"
        };
    }
    
    private void setupLevel3() {
        levelName = "Ice Cavern";
        description = "Brave the frozen depths of the ice cavern";
        
        // Graphics paths - using actual drawable resources (now in main drawable folder)
        backgroundImagePath = "background_level_3";
        playerImagePath = "player_level_3";
        enemyImagePath = "enemy_level_3";
        bulletImagePath = "bullet_level_3";
        powerUpImagePath = "powerup_level_3"; // Will fallback to color bitmap if not found
        
        // Gameplay settings - Hard difficulty
        playerSpeed = 400f;
        bulletSpeed = 1000f;
        bulletDamage = 3;
        enemySpawnRate = 1.0f;
        enemySpeed = 250f;
        enemyHealth = 3;
        powerUpSpawnRate = 0.2f;
        
        // Progression
        scoreToNextLevel = 500;
        maxEnemies = 7;
        hasSpecialWeapons = true;
        availablePowerUps = new String[]{"health", "speed", "shield", "multishot", "rapidfire", "laser", "freeze"};
        
        // Visual
        backgroundColor = 0xFF87CEEB; // Sky blue
        particleEffectIntensity = 1.0f;
        musicPath = "res/raw/level3_ice_music.ogg";
        soundEffectPaths = new String[]{
            "res/raw/level3_ice_shoot.wav",
            "res/raw/level3_enemy_death.wav",
            "res/raw/level3_freeze_effect.wav"
        };
    }
    
    private void setupDefaultLevel() {
        // Fallback configuration
        setupLevel1();
    }
    
    // Getters
    public int getLevelNumber() { return levelNumber; }
    public String getLevelName() { return levelName; }
    public String getDescription() { return description; }
    public String getBackgroundImagePath() { return backgroundImagePath; }
    public String getPlayerImagePath() { return playerImagePath; }
    public String getEnemyImagePath() { return enemyImagePath; }
    public String getBulletImagePath() { return bulletImagePath; }
    public String getPowerUpImagePath() { return powerUpImagePath; }
    public float getPlayerSpeed() { return playerSpeed; }
    public float getBulletSpeed() { return bulletSpeed; }
    public int getBulletDamage() { return bulletDamage; }
    public float getEnemySpawnRate() { return enemySpawnRate; }
    public float getEnemySpeed() { return enemySpeed; }
    public int getEnemyHealth() { return enemyHealth; }
    public float getPowerUpSpawnRate() { return powerUpSpawnRate; }
    public int getScoreToNextLevel() { return scoreToNextLevel; }
    public int getMaxEnemies() { return maxEnemies; }
    public boolean hasSpecialWeapons() { return hasSpecialWeapons; }
    public String[] getAvailablePowerUps() { return availablePowerUps; }
    public int getBackgroundColor() { return backgroundColor; }
    public float getParticleEffectIntensity() { return particleEffectIntensity; }
    public String getMusicPath() { return musicPath; }
    public String[] getSoundEffectPaths() { return soundEffectPaths; }
}