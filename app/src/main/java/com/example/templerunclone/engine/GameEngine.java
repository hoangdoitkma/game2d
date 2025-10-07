package com.example.templerunclone.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.example.templerunclone.entities.*;
import com.example.templerunclone.entities.bullets.*;
import com.example.templerunclone.managers.*;
import com.example.templerunclone.levels.LevelManager;
import com.example.templerunclone.levels.LevelConfig;
import com.example.templerunclone.ui.HUDManager;
import com.example.templerunclone.ui.GameOverManager;
import com.example.templerunclone.ui.WinManager;

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
    private WinManager winManager;
    private HighScoreManager highScoreManager;
    
    // Level Management
    private LevelManager levelManager;
    
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
    
    // Context for managers that need it
    private android.content.Context context;
    
    // Background
    private BackgroundRenderer backgroundRenderer;
    
    public GameEngine(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        initialize();
    }
    
    public void setContext(android.content.Context context) {
        this.context = context;
        if (highScoreManager == null && context != null) {
            highScoreManager = new HighScoreManager(context);
        }
        
        // Load initial assets when context is set
        if (context != null && resourceManager != null && levelManager != null) {
            android.util.Log.d("GameEngine", "Loading initial assets after setContext");
            
            // Initialize level 1 and load its assets
            levelManager.initializeLevel(context);
            updateAssetsForCurrentLevel();
            
            android.util.Log.d("GameEngine", "Initial assets loaded");
        }
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
        winManager = new WinManager(screenWidth, screenHeight);
        
        // Initialize level manager
        levelManager = new LevelManager(screenWidth, screenHeight);
        levelManager.setManagers(resourceManager, enemyManager, powerUpManager);
        
        // Set button bitmaps after managers are created
        gameOverManager.setButtonBitmaps(
            resourceManager.getReplayButtonBitmap(),
            resourceManager.getMenuButtonBitmap(), 
            resourceManager.getSettingButtonBitmap()
        );
        
        winManager.setButtonBitmaps(
            resourceManager.getReplayButtonBitmap(),
            resourceManager.getMenuButtonBitmap(),
            resourceManager.getHighScoresButtonBitmap()
        );
        
        winManager.setCongratulationsBitmap(resourceManager.getCongratulationsBitmap());
        
        // Initialize collections
        bullets = new ArrayList<>();
        explosions = new ArrayList<>();
        
        // Initialize background
        backgroundRenderer = new BackgroundRenderer(screenWidth, screenHeight);
        
        // Set initial background immediately to prevent null
        if (resourceManager != null) {
            Bitmap initialBackground = resourceManager.getCurrentLevelBackground();
            if (initialBackground != null) {
                backgroundRenderer.setBackground(initialBackground);
                android.util.Log.d("GameEngine", "Initial background set in constructor");
            } else {
                android.util.Log.w("GameEngine", "Initial background is null in constructor");
            }
        }
        
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
        
        if (gameState.isGameOver() || gameState.isGameWon()) {
            return;
        }
        
        // Update player
        if (player != null) {
            player.update(deltaTime);
        }
        
        // Handle shooting with reduced frequency checks
        if (inputManager.isShooting() && currentTime - lastShotTime > shootInterval) {
            shoot();
            lastShotTime = currentTime;
        }
        
        // Update bullets (limit processing)
        updateBullets(deltaTime);
        
        // Update enemies with level parameter (reduce spawn frequency if too many objects)
        if (enemyManager.getEnemies().size() < 15) { // Limit max enemies to prevent lag
            enemyManager.update(deltaTime, gameState.getSpeedMultiplier(), gameState.getLevel());
        }
        
        // Update power-ups (limit active power-ups)
        if (powerUpManager.getPowerUps().size() < 8) {
            powerUpManager.update(deltaTime);
        }
        
        // Update explosions
        updateExplosions(deltaTime);
        
        // Update background
        backgroundRenderer.update(deltaTime, gameState.getSpeedMultiplier());
        
        // Check collisions (only if we have active objects)
        if (!bullets.isEmpty() || !enemyManager.getEnemies().isEmpty()) {
            checkCollisions();
        }
        
        // Update game state
        boolean wasGameWon = gameState.isGameWon();
        gameState.update(deltaTime);
        
        // Additional win condition check based on level progress
        checkLevelWinConditions();
        
        // Update level manager and handle transitions
        levelManager.updateTransition(deltaTime);
        
        // Check for level advancement
        if (levelManager.shouldAdvanceLevel(gameState.getScore()) && !levelManager.isTransitioning()) {
            levelManager.startLevelTransition(player);
        }
        
        // Check if level transition completed (use single-fire flag to avoid timing issues)
        if (levelManager.consumeTransitionJustCompleted()) {
            // Level transition completed, initialize new level
            if (context != null) {
                levelManager.initializeLevel(context);
                
                // Force update all visual assets for new level
                updateAssetsForCurrentLevel();
                
                // Double-check background is set after level change
                Bitmap newBackground = resourceManager.getCurrentLevelBackground();
                if (newBackground != null && !newBackground.isRecycled()) {
                    backgroundRenderer.setBackground(newBackground);
                    android.util.Log.d("GameEngine", "Background force-updated after level transition");
                } else {
                    android.util.Log.e("GameEngine", "Background is null/recycled after level transition!");
                }
                
                // Clear existing entities so new ones use fresh level assets
                if (enemyManager != null) {
                    enemyManager.clear();
                }
                if (powerUpManager != null) {
                    powerUpManager.clear();
                }
                if (bullets != null) {
                    bullets.clear();
                }
                
                // Update player with saved state
                levelManager.restorePlayerState(player);
                // Reset player position for new level
                float playerX = screenWidth / 2f - 50;
                float playerY = screenHeight - 200;
                player.setPosition(playerX, playerY);
                
                android.util.Log.d("GameEngine", "Level transition completed to level " + levelManager.getCurrentLevel());
            }
        }
        
        // Check if player just won the game
        if (!wasGameWon && gameState.isGameWon()) {
            soundManager.playCongratulations();
            // Save high score
            if (highScoreManager != null) {
                highScoreManager.addScore(gameState.getScore(), gameState.getLevel());
            }
        }
        
        // Check if player is dead
        if (player.isDead()) {
            gameState.setGameOver(true);
            hudManager.stopTimer(); // Dừng đồng hồ khi game over
            // Save high score for game over too
            if (highScoreManager != null) {
                highScoreManager.addScore(gameState.getScore(), gameState.getLevel());
            }
        }
    }
    
    private void shoot() {
        float bulletX = player.getX() + player.getWidth() / 2 - 4;
        float bulletY = player.getY() - 16;
        
        // Get level-specific bullet configuration
        LevelManager.BulletConfig bulletConfig = levelManager.getBulletConfig();
        
        Bullet bullet;
        
        // Chọn loại đạn dựa trên PowerUp active và level config
        if (gameState.isLaserBeamActive()) {
            bullet = new LaserBullet(bulletX, bulletY, bulletConfig.speed);
        } else {
            bullet = new Bullet(bulletX, bulletY, bulletConfig.speed);
            bullet.setDamage(bulletConfig.damage);
        }
        
        // Set level-specific bullet bitmap
        Bitmap bulletBitmap = resourceManager.getCurrentLevelBullet();
        if (bulletBitmap != null) {
            bullet.setBitmap(bulletBitmap);
            android.util.Log.d("GameEngine", "Set bullet bitmap for level " + levelManager.getCurrentLevel());
        } else {
            android.util.Log.w("GameEngine", "No bullet bitmap available for level " + levelManager.getCurrentLevel());
        }
        
        bullets.add(bullet);
        
        // Multi-shot
        if (gameState.isMultiShotActive()) {
            // Tạo thêm đạn bên trái và phải
            Bullet leftBullet, rightBullet;
            float offset = 30f;
            
            if (gameState.isLaserBeamActive()) {
                leftBullet = new LaserBullet(bulletX - offset, bulletY, bulletConfig.speed);
                rightBullet = new LaserBullet(bulletX + offset, bulletY, bulletConfig.speed);
            } else {
                leftBullet = new Bullet(bulletX - offset, bulletY, bulletConfig.speed);
                rightBullet = new Bullet(bulletX + offset, bulletY, bulletConfig.speed);
                leftBullet.setDamage(bulletConfig.damage);
                rightBullet.setDamage(bulletConfig.damage);
            }
            
            // Set bitmap for multi-shot bullets too
            if (bulletBitmap != null) {
                leftBullet.setBitmap(bulletBitmap);
                rightBullet.setBitmap(bulletBitmap);
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
            // Handle health power-up specially
            if (powerUp.getType() == PowerUp.PowerUpType.HEALTH) {
                player.setHealth(player.getHealth() + 1); // Heal 1 HP
            } else {
                gameState.applyPowerUp(powerUp);
            }
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
        if (canvas == null) return;
        
        // Draw level-specific background or transition effect
        if (levelManager != null && levelManager.isTransitioning()) {
            // Draw current background
            backgroundRenderer.draw(canvas, paint);
            // Draw transition effect on top
            levelManager.drawTransition(canvas, paint);
            
            // Draw level info during transition
            paint.setColor(android.graphics.Color.WHITE);
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);
            String levelText = "Level " + levelManager.getCurrentLevel() + ": " + 
                             levelManager.getCurrentLevelConfig().getLevelName();
            canvas.drawText(levelText, screenWidth / 2f, 100, paint);
            return; // Skip other rendering during transition
        } 
        
        // Draw normal background
        backgroundRenderer.draw(canvas, paint);
        
        // Draw game objects only if not transitioning
        if (player != null) {
            player.drawWithShield(canvas, paint, gameState);
        }
        
        // Draw bullets (limit rendering if too many)
        int bulletCount = 0;
        for (Bullet bullet : bullets) {
            if (bulletCount++ < 50) { // Limit bullet rendering
                bullet.draw(canvas, paint);
            }
        }
        
        // Draw enemies
        enemyManager.draw(canvas, paint);
        
        // Draw power-ups
        powerUpManager.draw(canvas, paint);
        
        // Draw explosions (limit explosion rendering)
        int explosionCount = 0;
        for (Explosion explosion : explosions) {
            if (explosionCount++ < 10) { // Limit explosion rendering
                explosion.draw(canvas, paint);
            }
        }
        
        // Always draw HUD
        if (hudManager != null) {
            hudManager.draw(canvas, gameState, player);
        }
        
        // Draw Game Over screen if game is over
        if (gameState.isGameOver() && gameOverManager != null) {
            long playTime = hudManager != null ? hudManager.getPlayTime() : 0;
            gameOverManager.draw(canvas, gameState.getScore(), gameState.getLevel(), playTime);
        }
        
        // Draw Win screen if game is won
        if (gameState.isGameWon() && winManager != null) {
            winManager.draw(canvas, gameState.getScore(), gameState.getLevel());
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
        } else if (gameState.isGameWon() && isDown) {
            // Handle win touch events
            String action = winManager.handleTouch(x, y);
            if ("REPLAY".equals(action)) {
                resetGame();
            } else if ("MENU".equals(action)) {
                // This will be handled by GameView to return to main menu
                gameState.setAction("MENU");
            } else if ("HIGH_SCORES".equals(action)) {
                // This will be handled by GameView to show high scores
                gameState.setAction("HIGH_SCORES");
            }
        } else if (!gameState.isGameOver() && !gameState.isGameWon()) {
            // Normal game touch handling
            inputManager.handleTouch(x, y, isDown);
            
            if (isDown) {
                player.moveTo(x - player.getWidth() / 2, y - player.getHeight() / 2);
                
                // Cheat codes
                checkCheatCodes(x, y);
            }
        }
    }
    
    /**
     * Update all visual assets to match current level
     */
    private void updateAssetsForCurrentLevel() {
        if (resourceManager == null || levelManager == null) return;
        
        // FIRST: Load new assets for current level
        LevelConfig currentLevelConfig = levelManager.getCurrentLevelConfig();
        if (currentLevelConfig != null) {
            android.util.Log.d("GameEngine", "Loading new assets for level " + currentLevelConfig.getLevelNumber() + ": " + currentLevelConfig.getLevelName());
            resourceManager.loadLevelResources(context, currentLevelConfig);
        }
        
        // THEN: Update background
        Bitmap newBackground = resourceManager.getCurrentLevelBackground();
        backgroundRenderer.setBackground(newBackground);
        android.util.Log.d("GameEngine", "Updated background for level " + levelManager.getCurrentLevel());
        
        // Update player
        Bitmap newPlayer = resourceManager.getCurrentLevelPlayer();
        if (newPlayer != null) {
            player.setBitmap(newPlayer);
            android.util.Log.d("GameEngine", "Updated player bitmap for level " + levelManager.getCurrentLevel());
        }
        
        // Refresh enemy manager settings (spawn rate, speed etc already set in initializeLevel)
        if (enemyManager != null && levelManager.getCurrentLevelConfig() != null) {
            enemyManager.configureLevelSettings(
                levelManager.getCurrentLevelConfig().getEnemySpawnRate(),
                levelManager.getCurrentLevelConfig().getEnemySpeed(),
                levelManager.getCurrentLevelConfig().getEnemyHealth(),
                levelManager.getCurrentLevelConfig().getMaxEnemies()
            );
        }
        
        // Update enemy manager to use new assets
        if (enemyManager != null) {
            enemyManager.setResourceManager(resourceManager);
            android.util.Log.d("GameEngine", "Updated enemy manager assets for level " + levelManager.getCurrentLevel());
        }
        
        // Update power-up manager
        if (powerUpManager != null) {
            powerUpManager.setResourceManager(resourceManager);
            android.util.Log.d("GameEngine", "Updated powerup manager assets for level " + levelManager.getCurrentLevel());
        }
        
        // Debug log asset status
        logAssetStatus();
    }
    
    private void logAssetStatus() {
        android.util.Log.d("GameEngine", "=== ASSET STATUS DEBUG ===");
        Bitmap bg = resourceManager.getCurrentLevelBackground();
        Bitmap player = resourceManager.getCurrentLevelPlayer();
        Bitmap bullet = resourceManager.getCurrentLevelBullet();
        
        android.util.Log.d("GameEngine", "Background: " + (bg != null ? bg.getWidth() + "x" + bg.getHeight() : "NULL"));
        android.util.Log.d("GameEngine", "Player: " + (player != null ? player.getWidth() + "x" + player.getHeight() : "NULL"));
        android.util.Log.d("GameEngine", "Bullet: " + (bullet != null ? bullet.getWidth() + "x" + bullet.getHeight() : "NULL"));
        android.util.Log.d("GameEngine", "Current Level: " + levelManager.getCurrentLevel());
        android.util.Log.d("GameEngine", "=========================");
    }

    /**
     * Check for cheat codes based on touch position
     */
    private void checkCheatCodes(float x, float y) {
        float cornerSize = 150f; // Size of corner zones
        
        // Cheat: Win - Touch top-right corner
        if (x > screenWidth - cornerSize && y < cornerSize) {
            gameState.setGameWon(true);
            android.util.Log.d("GameEngine", "CHEAT: Win activated by touching top-right corner");
            soundManager.playCongratulations();
            if (highScoreManager != null) {
                highScoreManager.addScore(gameState.getScore(), gameState.getLevel());
            }
        }
        
        // Cheat: Lose - Touch bottom-left corner
        else if (x < cornerSize && y > screenHeight - cornerSize) {
            gameState.setGameOver(true);
            android.util.Log.d("GameEngine", "CHEAT: Game Over activated by touching bottom-left corner");
            soundManager.playPlayerHit(); // Use available sound method
        }
        
        // Original PowerUp cheat: Touch top-left corner (only in level 1)
        else if (x < cornerSize && y < cornerSize && gameState.getLevel() == 1) {
            powerUpManager.spawnAllPowerUpsForTesting(200f, 200f);
            android.util.Log.d("GameEngine", "CHEAT: All PowerUps spawned");
        }
        
        // Debug cheat: Touch bottom-right corner to advance level
        else if (x > screenWidth - cornerSize && y > screenHeight - cornerSize) {
            if (!levelManager.isTransitioning()) {
                levelManager.startLevelTransition(player);
                android.util.Log.d("GameEngine", "CHEAT: Level advancement activated");
            }
        }
    }

    private void resetGame() {
        // Reset game state
        gameState.reset();
        
        // Reset level manager
        levelManager.resetToLevel1();
        
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
        player = new Player(playerX, playerY, 100, 100, resourceManager.getCurrentLevelPlayer());
        
        // Initialize level 1
        if (context != null) {
            levelManager.initializeLevel(context);
            
            // Load fresh assets for level 1
            android.util.Log.d("GameEngine", "Loading fresh assets for game reset");
            updateAssetsForCurrentLevel();
        }
        
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
        
        // Clean up UI managers
        if (gameOverManager != null) {
            gameOverManager.cleanup();
        }
        if (winManager != null) {
            winManager.cleanup();
        }
        
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
    
    /**
     * Check win conditions based on current level and score targets
     */
    private void checkLevelWinConditions() {
        if (gameState.isGameWon() || gameState.isGameOver()) return;
        
        if (levelManager != null && levelManager.getCurrentLevelConfig() != null) {
            LevelConfig currentLevel = levelManager.getCurrentLevelConfig();
            int currentScore = gameState.getScore();
            
            // Check if player reached the score target for current level
            if (currentScore >= currentLevel.getScoreToNextLevel()) {
                // If we're on the final level (level 3), this means win
                if (currentLevel.getLevelNumber() >= 3) {
                    gameState.setGameWon(true);
                    android.util.Log.d("GameEngine", "Player won by reaching score target on final level: " + currentScore);
                }
                // For other levels, advance to next level (handled by LevelManager)
            }
            
            // Additional high score win conditions for each level
            switch (currentLevel.getLevelNumber()) {
                case 1:
                    if (currentScore >= 750) { // High score win for Level 1
                        gameState.setGameWon(true);
                        android.util.Log.d("GameEngine", "Player won Level 1 with high score: " + currentScore);
                    }
                    break;
                case 2:
                    if (currentScore >= 1200) { // High score win for Level 2
                        gameState.setGameWon(true);
                        android.util.Log.d("GameEngine", "Player won Level 2 with high score: " + currentScore);
                    }
                    break;
                case 3:
                    if (currentScore >= 2000) { // High score win for Level 3
                        gameState.setGameWon(true);
                        android.util.Log.d("GameEngine", "Player won Level 3 with high score: " + currentScore);
                    }
                    break;
            }
        }
    }

    // Resource management
    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        
        // Wire managers
        enemyManager.setResourceManager(resourceManager);
        powerUpManager.setResourceManager(resourceManager);
        // Update level manager reference
        levelManager.setManagers(resourceManager, enemyManager, powerUpManager);
        
        // Important: initialize level resources first, then update visual assets
        if (context != null) {
            levelManager.initializeLevel(context);
            // Now that resources are loaded for the current level, update all visuals
            updateAssetsForCurrentLevel();
        } else {
            // If no context yet, at least attempt to update with whatever is available
            updateAssetsForCurrentLevel();
        }
        
        // Set game over images
        gameOverManager.setGameOverBitmap(resourceManager.getGameOverBitmap());
        gameOverManager.setYouLoseBitmap(resourceManager.getYouLoseBitmap());

        // Update Win screen assets after resources are loaded
        if (winManager != null) {
            winManager.setButtonBitmaps(
                resourceManager.getReplayButtonBitmap(),
                resourceManager.getMenuButtonBitmap(),
                resourceManager.getHighScoresButtonBitmap()
            );
            winManager.setCongratulationsBitmap(resourceManager.getCongratulationsBitmap());
        }
    }
    
    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }
}