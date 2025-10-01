package com.example.templerunclone.engine;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.example.templerunclone.entities.*;
import com.example.templerunclone.entities.bullets.*;
import com.example.templerunclone.managers.*;
import com.example.templerunclone.ui.HUDManager;
import com.example.templerunclone.ui.GameOverManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main game engine that coordinates all game systems
 */
public class GameEngine {
    
    // Managers
    private InputManager inputManager;
    private CollisionManager collisionManager;
    private SoundManager soundManager;
    private ResourceManager resourceManager;
    private EnemyManager enemyManager;
    private PowerUpManager powerUpManager;
    
    // UI Managers
    private HUDManager hudManager;
    private GameOverManager gameOverManager;
    
    // Game state
    private GameState gameState;
    private Player player;
    private List<Bullet> bullets;
    private List<Explosion> explosions;
    
    // Timing
    private long lastUpdateTime;
    private long lastShotTime;
    private long shootInterval = 250; // ms
    
    // Screen dimensions
    private int screenWidth, screenHeight;
    
    // Background
    private BackgroundRenderer backgroundRenderer;
    
    public GameEngine(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        initialize();
    }
    
    private void initialize() {
        // Initialize managers
        inputManager = new InputManager();
        collisionManager = new CollisionManager();
        soundManager = new SoundManager();
        resourceManager = new ResourceManager(screenWidth, screenHeight);
        enemyManager = new EnemyManager(screenWidth, screenHeight);
        powerUpManager = new PowerUpManager(screenWidth, screenHeight);
        
        // Initialize game state
        gameState = new GameState();
        
        // Initialize UI managers
        hudManager = new HUDManager(screenWidth, screenHeight);
        gameOverManager = new GameOverManager(screenWidth, screenHeight);
        
        // Set button bitmaps after gameOverManager is created
        gameOverManager.setButtonBitmaps(
            resourceManager.getReplayButtonBitmap(),
            resourceManager.getMenuButtonBitmap(), 
            resourceManager.getSettingButtonBitmap()
        );
        
        // Initialize collections
        bullets = new ArrayList<>();
        explosions = new ArrayList<>();
        
        // Initialize background
        backgroundRenderer = new BackgroundRenderer(screenWidth, screenHeight);
        
        // Initialize player
        float playerX = screenWidth / 2f - 50;
        float playerY = screenHeight - 200;
        player = new Player(playerX, playerY, 100, 100, null);
        
        lastUpdateTime = System.currentTimeMillis();
    }
    
    public void update() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;
        
        if (gameState.isGameOver()) {
            return;
        }
        
        // Update player
        player.update(deltaTime);
        
        // Handle shooting
        if (inputManager.isShooting() && currentTime - lastShotTime > shootInterval) {
            shoot();
            lastShotTime = currentTime;
        }
        
        // Update bullets
        updateBullets(deltaTime);
        
        // Update enemies với level parameter
        enemyManager.update(deltaTime, gameState.getSpeedMultiplier(), gameState.getLevel());
        
        // Update power-ups
        powerUpManager.update(deltaTime);
        
        // Update explosions
        updateExplosions(deltaTime);
        
        // Update background
        backgroundRenderer.update(deltaTime, gameState.getSpeedMultiplier());
        
        // Check collisions
        checkCollisions();
        
        // Update game state
        gameState.update(deltaTime);
        
        // Check if player is dead
        if (player.isDead()) {
            gameState.setGameOver(true);
            hudManager.stopTimer(); // Dừng đồng hồ khi game over
        }
    }
    
    private void shoot() {
        float bulletX = player.getX() + player.getWidth() / 2 - 4;
        float bulletY = player.getY() - 16;
        
        Bullet bullet;
        
        // Chọn loại đạn dựa trên PowerUp active - chỉ 3 loại
        if (gameState.isLaserBeamActive()) {
            bullet = new LaserBullet(bulletX, bulletY, 1000f);
        } else {
            bullet = new Bullet(bulletX, bulletY, 800f); // Default bullet
        }
        
        bullets.add(bullet);
        
        // Multi-shot
        if (gameState.isMultiShotActive()) {
            // Tạo thêm đạn bên trái và phải
            Bullet leftBullet, rightBullet;
            float offset = 30f;
            
            if (gameState.isLaserBeamActive()) {
                leftBullet = new LaserBullet(bulletX - offset, bulletY, 1000f);
                rightBullet = new LaserBullet(bulletX + offset, bulletY, 1000f);
            } else {
                leftBullet = new Bullet(bulletX - offset, bulletY, 800f);
                rightBullet = new Bullet(bulletX + offset, bulletY, 800f);
            }
            
            bullets.add(leftBullet);
            bullets.add(rightBullet);
        }
        
        soundManager.playShoot();
    }
    
    private void updateBullets(float deltaTime) {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update(deltaTime);
            
            if (!bullet.isActive()) {
                bulletIterator.remove();
            }
        }
    }
    
    private void updateExplosions(float deltaTime) {
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.update(deltaTime);
            
            if (!explosion.isActive()) {
                explosionIterator.remove();
            }
        }
    }
    
    private void checkCollisions() {
        // Bullet vs Enemy collisions
        for (Bullet bullet : bullets) {
            List<Enemy> hitEnemies = collisionManager.checkBulletCollisions(bullet, enemyManager.getEnemies());
            for (Enemy enemy : hitEnemies) {
                enemy.takeDamage(bullet.getDamage());
                
                // Xử lý đạn laser (xuyên qua)
                if (!(bullet instanceof LaserBullet)) {
                    bullet.setActive(false);
                }
                
                if (enemy.isDead()) {
                    // Create explosion
                    explosions.add(new Explosion(enemy.getX(), enemy.getY(), 
                        resourceManager.getExplosionBitmap(), 500));
                    
                    // Add score
                    gameState.addScore(enemy.getScoreValue());
                    
                    // Play sound
                    soundManager.playEnemyExplode();
                    
                    // Chance to drop power-up - HIGH RATE for testing at level 1
                    float dropChance;
                    if (gameState.getLevel() == 1) {
                        dropChance = 0.8f; // 80% chance at level 1 for testing
                    } else {
                        dropChance = 0.15f + (gameState.getLevel() * 0.02f); // Normal progression
                    }
                    if (Math.random() < Math.min(dropChance, 0.8f)) {
                        powerUpManager.spawnPowerUp(enemy.getX(), enemy.getY());
                    }
                }
            }
        }
        
        // Player vs Enemy collisions
        List<Enemy> collidingEnemies = collisionManager.checkPlayerCollisions(player, enemyManager.getEnemies());
        if (!collidingEnemies.isEmpty() && !player.isInvincible()) {
            player.takeDamage(1, gameState);
            
            // Create explosion at player position
            explosions.add(new Explosion(player.getX(), player.getY(), 
                resourceManager.getExplosionBitmap(), 500));
            
            soundManager.playPlayerHit();
        }
        
        // Player vs PowerUp collisions
        List<PowerUp> collectedPowerUps = collisionManager.checkPowerUpCollisions(player, powerUpManager.getPowerUps());
        for (PowerUp powerUp : collectedPowerUps) {
            gameState.applyPowerUp(powerUp);
            powerUp.setActive(false);
            soundManager.playPowerUp();
        }
        
        // Player vs Border collisions
        if (collisionManager.checkBorderCollisions(player, screenWidth, screenHeight)) {
            player.takeDamage(1, gameState);
            explosions.add(new Explosion(player.getX(), player.getY(), 
                resourceManager.getExplosionBitmap(), 500));
            soundManager.playBorderHit();
        }
    }
    
    public void render(Canvas canvas, Paint paint) {
        // Draw background
        backgroundRenderer.draw(canvas, paint);
        
        // Draw player with shield effects
        player.drawWithShield(canvas, paint, gameState);
        
        // Draw bullets
        for (Bullet bullet : bullets) {
            bullet.draw(canvas, paint);
        }
        
        // Draw enemies
        enemyManager.draw(canvas, paint);
        
        // Draw power-ups
        powerUpManager.draw(canvas, paint);
        
        // Draw explosions
        for (Explosion explosion : explosions) {
            explosion.draw(canvas, paint);
        }
        
        // Draw HUD
        hudManager.draw(canvas, gameState, player);
        
        // Draw Game Over screen if game is over
        if (gameState.isGameOver()) {
            long playTime = hudManager.getPlayTime(); // Use HUD manager's frozen time
            gameOverManager.draw(canvas, gameState.getScore(), gameState.getLevel(), playTime);
        }
    }
    
    private void handleExplosiveDamage(float explosionX, float explosionY, float radius, int damage) {
        for (Enemy enemy : enemyManager.getEnemies()) {
            float distance = (float) Math.sqrt(
                Math.pow(enemy.getX() - explosionX, 2) + 
                Math.pow(enemy.getY() - explosionY, 2)
            );
            
            if (distance <= radius && !enemy.isDead()) {
                enemy.takeDamage(damage);
                if (enemy.isDead()) {
                    explosions.add(new Explosion(enemy.getX(), enemy.getY(), 
                        resourceManager.getExplosionBitmap(), 300));
                    gameState.addScore(enemy.getScoreValue() / 2); // Bonus score giảm
                }
            }
        }
    }
    
    public void handleTouch(float x, float y, boolean isDown) {
        if (gameState.isGameOver() && isDown) {
            // Handle game over touch events
            String action = gameOverManager.handleTouch(x, y);
            if ("REPLAY".equals(action)) {
                resetGame();
            } else if ("HOME".equals(action)) {
                // This will be handled by GameView to return to main menu
                gameState.setAction("HOME");
            } else if ("MENU".equals(action)) {
                // This will be handled by GameView to show main menu
                gameState.setAction("MENU");
            }
        } else if (!gameState.isGameOver()) {
            // Normal game touch handling
            inputManager.handleTouch(x, y, isDown);
            
            if (isDown) {
                player.moveTo(x - player.getWidth() / 2, y - player.getHeight() / 2);
                
                // Test cheat: Touch top-left corner to spawn all PowerUps
                if (x < 100 && y < 100 && gameState.getLevel() == 1) {
                    powerUpManager.spawnAllPowerUpsForTesting(200f, 200f);
                }
            }
        }
    }
    
    private void resetGame() {
        // Reset game state
        gameState.reset();
        
        // Reset HUD
        hudManager.reset();
        
        // Clear game objects
        bullets.clear();
        explosions.clear();
        enemyManager.clear();
        powerUpManager.clear();
        
        // Reset player
        float playerX = screenWidth / 2f - 50;
        float playerY = screenHeight - 200;
        player = new Player(playerX, playerY, 100, 100, resourceManager.getPlayerBitmap());
        
        lastUpdateTime = System.currentTimeMillis();
    }
    
    public void pause() {
        soundManager.pauseMusic();
    }
    
    public void resume() {
        soundManager.resumeMusic();
        lastUpdateTime = System.currentTimeMillis();
    }
    
    public void cleanup() {
        soundManager.cleanup();
        
        // Clean up managers
        if (enemyManager != null) {
            enemyManager.clear();
        }
        if (powerUpManager != null) {
            powerUpManager.clear();
        }
        
        // Clear lists
        if (bullets != null) {
            bullets.clear();
        }
        if (explosions != null) {
            explosions.clear();
        }
        
        // Clean up resource manager
        if (resourceManager != null) {
            resourceManager.cleanup();
        }
    }
    
    // Getters
    public GameState getGameState() { return gameState; }
    public Player getPlayer() { return player; }
    public boolean isGameOver() { return gameState.isGameOver(); }
    
    // Resource management
    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        backgroundRenderer.setBackground(resourceManager.getBackgroundBitmap());
        player.setBitmap(resourceManager.getPlayerBitmap());
        enemyManager.setResourceManager(resourceManager);
        powerUpManager.setResourceManager(resourceManager);
        
        // Set game over images
        gameOverManager.setGameOverBitmap(resourceManager.getGameOverBitmap());
        gameOverManager.setYouLoseBitmap(resourceManager.getYouLoseBitmap());
    }
    
    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }
}