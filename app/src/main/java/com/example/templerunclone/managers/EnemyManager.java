package com.example.templerunclone.managers;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.example.templerunclone.entities.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Manages enemy spawning, updating and rendering
 */
public class EnemyManager {
    private List<Enemy> enemies;
    private Random random;
    private ResourceManager resourceManager;
    
    private int screenWidth, screenHeight;
    private long lastSpawnTime;
    private long spawnInterval = 1500; // ms
    private long minSpawnInterval = 800; // Minimum spawn interval
    
    // Level-specific settings
    private float levelEnemySpawnRate = 2.0f; // seconds between spawns
    private float levelEnemySpeed = 150f;
    private int levelEnemyHealth = 1;
    private int levelMaxEnemies = 3;
    // Boss management for level 3
    private boolean bossSpawned = false;
    
    public EnemyManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.enemies = new ArrayList<>();
        this.random = new Random();
        this.lastSpawnTime = System.currentTimeMillis();
    }
    
    /**
     * Configure level-specific enemy settings
     */
    public void configureLevelSettings(float spawnRate, float speed, int health, int maxEnemies) {
        this.levelEnemySpawnRate = spawnRate;
        this.levelEnemySpeed = speed;
        this.levelEnemyHealth = health;
        this.levelMaxEnemies = maxEnemies;
        this.spawnInterval = (long) (levelEnemySpawnRate * 1000); // Convert to milliseconds
        
        android.util.Log.d("EnemyManager", "Level settings configured: SpawnRate=" + spawnRate + 
                          "s, Speed=" + speed + ", Health=" + health + ", MaxEnemies=" + maxEnemies);
    }
    
    public void update(float deltaTime, float speedMultiplier, int level) {
        // Update existing enemies
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.update(deltaTime);
            
            if (!enemy.isActive()) {
                enemyIterator.remove();
            }
        }
        
        // Spawn new enemies based on level configuration
        long currentTime = System.currentTimeMillis();
        
        if (level == 3) {
            // Ensure exactly one boss exists
            if (!bossSpawned) {
                spawnBoss();
                bossSpawned = true;
            }
            // Let boss spawn minions through its own logic; we still allow small random spawns
            if (currentTime - lastSpawnTime > spawnInterval && enemies.size() < levelMaxEnemies + 2) {
                spawnMinionNearTop();
                lastSpawnTime = currentTime;
            }
        } else if (currentTime - lastSpawnTime > spawnInterval && enemies.size() < levelMaxEnemies) {
            spawnEnemyForLevel(level);
            lastSpawnTime = currentTime;
        }
    }
    
    /**
     * Spawn enemy based on current level configuration
     */
    private void spawnEnemyForLevel(int level) {
        if (resourceManager == null) return;
        
        float x = random.nextFloat() * (screenWidth - 100);
        float y = -100; // Start above screen
        
        // Level 2: ZigZag movement enemies, Level 1: Basic
        Enemy enemy;
        if (level == 2) {
            enemy = new EnemyZigZag(x, y, resourceManager.getCurrentLevelEnemy());
        } else {
            enemy = new EnemyBasic(x, y, resourceManager.getCurrentLevelEnemy());
        }
        enemy.setHealth(levelEnemyHealth);
        enemy.setSpeed(levelEnemySpeed);
        enemy.setScreenWidth(screenWidth);
        
        enemies.add(enemy);
        
        android.util.Log.d("EnemyManager", "Spawned enemy with health=" + levelEnemyHealth + 
                          ", speed=" + levelEnemySpeed);
    }

    private void spawnBoss() {
        if (resourceManager == null) return;
    float y = -220f;
        // Boss uses current level enemy bitmap scaled up 3x relative to regular enemy size
        android.graphics.Bitmap base = resourceManager.getCurrentLevelEnemy();
        android.graphics.Bitmap bossBitmap = base;
        if (base != null && !base.isRecycled()) {
            int bw = Math.max(1, base.getWidth());
            int bh = Math.max(1, base.getHeight());
            int targetW = Math.min(screenWidth, bw * 3);
            int targetH = Math.min(screenHeight, bh * 3);
            try {
                bossBitmap = android.graphics.Bitmap.createScaledBitmap(base, targetW, targetH, true);
            } catch (Exception e) {
                android.util.Log.w("EnemyManager", "Failed to scale boss bitmap, using base size: " + e.getMessage());
                bossBitmap = base;
            }
        }
        EnemyBoss.MinionSpawner spawner = (mx, my) -> {
            // Spawn small minion below boss
            Enemy minion = new EnemyBasic(Math.max(0, Math.min(mx, screenWidth - 60)), my, resourceManager.getCurrentLevelEnemy());
            minion.setHealth(Math.max(1, levelEnemyHealth - 1));
            minion.setSpeed(levelEnemySpeed + 40);
            minion.setScreenWidth(screenWidth);
            enemies.add(minion);
        };
    float bossWidth = (bossBitmap != null) ? bossBitmap.getWidth() : 200f;
    float x = screenWidth / 2f - bossWidth / 2f;
    Enemy boss = new EnemyBoss(x, y, bossBitmap, screenWidth, screenHeight, spawner);
        boss.setHealth(Math.max(10, levelEnemyHealth * 10)); // much higher HP
        enemies.add(boss);
        android.util.Log.d("EnemyManager", "Boss spawned at Level 3");
    }

    private void spawnMinionNearTop() {
        if (resourceManager == null) return;
        float x = random.nextFloat() * (screenWidth - 80);
        float y = -80;
        Enemy minion = new EnemyBasic(x, y, resourceManager.getCurrentLevelEnemy());
        minion.setHealth(Math.max(1, levelEnemyHealth - 1));
        minion.setSpeed(levelEnemySpeed + 30);
        minion.setScreenWidth(screenWidth);
        enemies.add(minion);
    }
    
    public void draw(Canvas canvas, Paint paint) {
        for (Enemy enemy : enemies) {
            enemy.draw(canvas, paint);
        }
    }
    
    public void clear() {
        enemies.clear();
        bossSpawned = false;
    }
    
    // Getters
    public List<Enemy> getEnemies() {
        return enemies;
    }
    
    public int getEnemyCount() {
        return enemies.size();
    }
    
    // Setters
    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
    
    public void setSpawnInterval(long spawnInterval) {
        this.spawnInterval = spawnInterval;
    }
}