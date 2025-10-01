package com.example.templerunclone.managers;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.example.templerunclone.entities.PowerUp;

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
    
    private int screenWidth, screenHeight;
    
    public PowerUpManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.powerUps = new ArrayList<>();
        this.random = new Random();
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
        
        // Randomly choose power-up type
        PowerUp.PowerUpType[] types = PowerUp.PowerUpType.values();
        PowerUp.PowerUpType type = types[random.nextInt(types.length)];
        
        PowerUp powerUp;
        long duration = 5000; // 5 seconds default
        
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
                powerUp = new PowerUp(x, y, resourceManager.getPowerUpEnergyShieldBitmap(), type, duration * 2); // 10 seconds
                break;
            case FORCE_FIELD:
                powerUp = new PowerUp(x, y, resourceManager.getPowerUpForceFieldBitmap(), type, duration * 3); // 15 seconds
                break;
            default:
                powerUp = new PowerUp(x, y, resourceManager.getPowerUpShieldBitmap(), type, duration);
                break;
        }
        
        powerUps.add(powerUp);
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
}