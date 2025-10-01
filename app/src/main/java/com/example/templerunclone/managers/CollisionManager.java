package com.example.templerunclone.managers;

import com.example.templerunclone.entities.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles collision detection between game objects
 */
public class CollisionManager {
    
    public List<Enemy> checkBulletCollisions(Bullet bullet, List<Enemy> enemies) {
        List<Enemy> hitEnemies = new ArrayList<>();
        
        for (Enemy enemy : enemies) {
            if (enemy.isActive() && bullet.isActive() && bullet.intersects(enemy)) {
                hitEnemies.add(enemy);
            }
        }
        
        return hitEnemies;
    }
    
    public List<Enemy> checkPlayerCollisions(Player player, List<Enemy> enemies) {
        List<Enemy> collidingEnemies = new ArrayList<>();
        
        for (Enemy enemy : enemies) {
            if (enemy.isActive() && player.intersects(enemy)) {
                collidingEnemies.add(enemy);
            }
        }
        
        return collidingEnemies;
    }
    
    public List<PowerUp> checkPowerUpCollisions(Player player, List<PowerUp> powerUps) {
        List<PowerUp> collectedPowerUps = new ArrayList<>();
        
        for (PowerUp powerUp : powerUps) {
            if (powerUp.isActive() && player.intersects(powerUp)) {
                collectedPowerUps.add(powerUp);
            }
        }
        
        return collectedPowerUps;
    }
    
    public boolean checkBorderCollisions(Player player, int screenWidth, int screenHeight) {
        float px = player.getX();
        float py = player.getY();
        float pw = player.getWidth();
        float ph = player.getHeight();
        
        // Check if player is outside screen bounds
        return px < 0 || py < 0 || px + pw > screenWidth || py + ph > screenHeight;
    }
}