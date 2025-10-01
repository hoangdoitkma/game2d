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
    
    public EnemyManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.enemies = new ArrayList<>();
        this.random = new Random();
        this.lastSpawnTime = System.currentTimeMillis();
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
        
        // Spawn new enemies với tần suất tăng theo level
        long currentTime = System.currentTimeMillis();
        long adjustedSpawnInterval = calculateSpawnInterval(level);
        
        if (currentTime - lastSpawnTime > adjustedSpawnInterval) {
            spawnEnemies(level); // Spawn nhiều enemy ở level cao
            lastSpawnTime = currentTime;
        }
    }
    
    private long calculateSpawnInterval(int level) {
        // Giảm spawn interval để tăng số lượng enemy từ level 2
        if (level < 2) {
            return spawnInterval; // 1500ms cho level 1
        } else {
            // Level >= 2: giảm interval để spawn nhanh hơn
            long reducedInterval = spawnInterval - (level - 1) * 150; // Giảm 150ms mỗi level từ level 2
            return Math.max(250, reducedInterval); // Tối thiểu 250ms
        }
    }
    
    private void spawnEnemies(int level) {
        if (resourceManager == null) return;
        
        int enemiesToSpawn = 1; // Mặc định spawn 1 enemy
        
        // Level >= 2: có chance spawn nhiều enemy cùng lúc
        if (level >= 2) {
            if (random.nextFloat() < 0.4f) { // 40% chance từ level 2
                enemiesToSpawn = 2;
            }
            if (level >= 4 && random.nextFloat() < 0.2f) { // 20% chance ở level 4+
                enemiesToSpawn = 3;
            }
            if (level >= 6 && random.nextFloat() < 0.1f) { // 10% chance ở level 6+
                enemiesToSpawn = 4;
            }
        }
        
        for (int i = 0; i < enemiesToSpawn; i++) {
            spawnSingleEnemy();
        }
    }
    
    private void spawnSingleEnemy() {
        if (resourceManager == null) return;
        
        float x = random.nextFloat() * (screenWidth - 150); // Account for largest enemy size
        float y = -150; // Start above screen
        
        // Randomly choose enemy type
        int enemyType = random.nextInt(3);
        Enemy enemy;
        
        switch (enemyType) {
            case 0:
                enemy = new EnemyBasic(x, y, resourceManager.getEnemyBasicBitmap());
                break;
            case 1:
                enemy = new EnemyMedium(x, y, resourceManager.getEnemyMediumBitmap());
                break;
            case 2:
                enemy = new EnemyHeavy(x, y, resourceManager.getEnemyHeavyBitmap());
                break;
            default:
                enemy = new EnemyBasic(x, y, resourceManager.getEnemyBasicBitmap());
                break;
        }
        
        // Set screen width for boundary checking
        enemy.setScreenWidth(screenWidth);
        
        enemies.add(enemy);
    }
    
    public void draw(Canvas canvas, Paint paint) {
        for (Enemy enemy : enemies) {
            enemy.draw(canvas, paint);
        }
    }
    
    public void clear() {
        enemies.clear();
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