package com.example.templerunclone.managers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.example.templerunclone.entities.PowerUp;
import com.example.templerunclone.levels.LevelManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Manages power-up spawning, updating and rendering
 */
public class PowerUpManager {
    private List<PowerUp> powerUps;
    private Random random;
    private ResourceManager resourceManager;
    private LevelManager levelManager;
    
    private int screenWidth, screenHeight;
    
    // Level-specific settings
    private float levelPowerUpSpawnRate = 0.3f; // 30% chance on enemy death
    private String[] levelAvailablePowerUps = {"health", "speed", "shield"};
    
    public PowerUpManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.powerUps = new ArrayList<>();
        this.random = new Random();
    }
    
    /**
     * Configure level-specific power-up settings
     */
    public void configureLevelSettings(float spawnRate, String[] availablePowerUps) {
        this.levelPowerUpSpawnRate = spawnRate;
        this.levelAvailablePowerUps = availablePowerUps.clone();
        
        android.util.Log.d("PowerUpManager", "Level settings configured: SpawnRate=" + spawnRate + 
                          ", AvailablePowerUps=" + java.util.Arrays.toString(availablePowerUps));
    }
    
    public void update(float deltaTime) {
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            powerUp.update(deltaTime);
            
            if (!powerUp.isActive()) {
                powerUpIterator.remove();
            }
        }
    }
    
    public void spawnPowerUp(float x, float y) {
        if (resourceManager == null) return;
        
        // Check spawn rate
        if (random.nextFloat() > levelPowerUpSpawnRate) {
            return; // Don't spawn based on level spawn rate
        }
        
        // Choose from level-available power-ups
        if (levelAvailablePowerUps.length == 0) {
            return; // No power-ups available for this level
        }
        
        String powerUpName = levelAvailablePowerUps[random.nextInt(levelAvailablePowerUps.length)];
        PowerUp.PowerUpType type = getPowerUpTypeFromName(powerUpName);
        
        PowerUp powerUp;
        long duration = 5000; // 5 seconds default
        
        // Get level-specific, type-specific bitmap
        Bitmap powerUpBitmap;
        if (levelManager != null && levelManager.getCurrentLevelConfig() != null) {
            powerUpBitmap = resourceManager.createLevelPowerUpByType(levelManager.getCurrentLevelConfig(), type);
        } else {
            // Fallback to generic powerup if level manager not available
            powerUpBitmap = resourceManager.getCurrentLevelPowerUp();
        }
        
        switch (type) {
            case HEALTH:
                powerUp = new PowerUp(x, y, powerUpBitmap, type, 0); // Instant effect
                break;
            case SHIELD:
                powerUp = new PowerUp(x, y, powerUpBitmap, type, duration);
                break;
            case RAPID_FIRE:
                powerUp = new PowerUp(x, y, powerUpBitmap, type, duration);
                break;
            case MULTI_SHOT:
                powerUp = new PowerUp(x, y, powerUpBitmap, type, duration);
                break;
            case LASER_BEAM:
                powerUp = new PowerUp(x, y, powerUpBitmap, type, duration);
                break;
            case ENERGY_SHIELD:
                powerUp = new PowerUp(x, y, powerUpBitmap, type, duration * 2);
                break;
            case FORCE_FIELD:
                powerUp = new PowerUp(x, y, powerUpBitmap, type, duration * 3);
                break;
            default:
                powerUp = new PowerUp(x, y, powerUpBitmap, type, duration);
                break;
        }
        
        powerUps.add(powerUp);
        android.util.Log.d("PowerUpManager", "Spawned power-up: " + powerUpName);
    }
    
    /**
     * Convert string name to PowerUpType enum
     */
    private PowerUp.PowerUpType getPowerUpTypeFromName(String name) {
        switch (name.toLowerCase()) {
            case "health":
                return PowerUp.PowerUpType.HEALTH;
            case "speed":
                return PowerUp.PowerUpType.RAPID_FIRE; // Map speed to rapid fire for now
            case "shield":
                return PowerUp.PowerUpType.SHIELD;
            case "multishot":
                return PowerUp.PowerUpType.MULTI_SHOT;
            case "rapidfire":
                return PowerUp.PowerUpType.RAPID_FIRE;
            case "laser":
                return PowerUp.PowerUpType.LASER_BEAM;
            case "freeze":
                return PowerUp.PowerUpType.FORCE_FIELD; // Map freeze to force field for now
            default:
                return PowerUp.PowerUpType.SHIELD;
        }
    }
    
    // Test method to spawn all PowerUp types at level 1
    public void spawnAllPowerUpsForTesting(float startX, float startY) {
        if (resourceManager == null) return;
        
        PowerUp.PowerUpType[] allTypes = PowerUp.PowerUpType.values();
        float spacing = 80f; // Space between powerups
        
        for (int i = 0; i < allTypes.length; i++) {
            PowerUp.PowerUpType type = allTypes[i];
            float x = startX + (i % 3) * spacing; // 3 columns
            float y = startY + (i / 3) * spacing; // Multiple rows
            
            PowerUp powerUp;
            long duration = 8000; // 8 seconds for testing
            
            switch (type) {
                case SHIELD:
                    powerUp = new PowerUp(x, y, resourceManager.getPowerUpShieldBitmap(), type, duration);
                    break;
                case RAPID_FIRE:
                    powerUp = new PowerUp(x, y, resourceManager.getPowerUpRapidFireBitmap(), type, duration);
                    break;
                case MULTI_SHOT:
                    powerUp = new PowerUp(x, y, resourceManager.getPowerUpMultiShotBitmap(), type, duration);
                    break;
                case LASER_BEAM:
                    powerUp = new PowerUp(x, y, resourceManager.getPowerUpLaserBitmap(), type, duration);
                    break;
                case ENERGY_SHIELD:
                    powerUp = new PowerUp(x, y, resourceManager.getPowerUpEnergyShieldBitmap(), type, duration * 2);
                    break;
                case FORCE_FIELD:
                    powerUp = new PowerUp(x, y, resourceManager.getPowerUpForceFieldBitmap(), type, duration * 3);
                    break;
                default:
                    powerUp = new PowerUp(x, y, resourceManager.getPowerUpShieldBitmap(), type, duration);
                    break;
            }
            
            powerUps.add(powerUp);
        }
    }
    
    public void draw(Canvas canvas, Paint paint) {
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(canvas, paint);
        }
    }
    
    public void clear() {
        powerUps.clear();
    }
    
    // Getters
    public List<PowerUp> getPowerUps() {
        return powerUps;
    }
    
    // Setters
    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
    
    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }
}