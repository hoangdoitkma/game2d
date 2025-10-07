package com.example.templerunclone.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.example.templerunclone.R;
import com.example.templerunclone.utils.BitmapUtils;
import com.example.templerunclone.levels.LevelConfig;

/**
 * Manages loading and scaling of game resources (bitmaps, sounds)
 */
public class ResourceManager {
    private Bitmap backgroundBitmap;
    private Bitmap playerBitmap;
    private Bitmap enemyBasicBitmap;
    private Bitmap enemyMediumBitmap;
    private Bitmap enemyHeavyBitmap;
    private Bitmap explosionBitmap;
    private Bitmap powerUpHealthBitmap;
    private Bitmap powerUpShieldBitmap;
    private Bitmap powerUpRapidFireBitmap;
    private Bitmap powerUpMultiShotBitmap;
    
    // Game Over images
    private Bitmap gameOverBitmap;
    private Bitmap youLoseBitmap;
    
    // Button images
    private Bitmap replayButtonBitmap;
    private Bitmap menuButtonBitmap;
    private Bitmap settingButtonBitmap;
    private Bitmap highScoresButtonBitmap;
    
    // Win state images
    private Bitmap congratulationsBitmap;
    
    // Additional PowerUp bitmaps
    private Bitmap powerUpLaserBitmap;
    private Bitmap powerUpEnergyShieldBitmap;
    private Bitmap powerUpForceFieldBitmap;
    
    // Level-specific assets
    private Bitmap currentLevelBackground;
    private Bitmap currentLevelPlayer;
    private Bitmap currentLevelEnemy;
    private Bitmap currentLevelBullet;
    private Bitmap currentLevelPowerUp;
    
    // Cache for level-specific powerup bitmaps by type
    private java.util.Map<String, Bitmap> levelPowerUpCache = new java.util.HashMap<>();
    
    private int screenWidth, screenHeight;
    
    public ResourceManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    
    public void loadResources(Context context) {
        Log.d("ResourceManager", "Starting resource loading...");
        long startTime = System.currentTimeMillis();
        
        try {
            // Load critical assets first (smaller, essential ones)
            loadCriticalAssets(context);
            
            // Load secondary assets
            loadSecondaryAssets(context);
            
            // Load level-specific assets
            loadInitialLevelAssets(context);
            
            long loadTime = System.currentTimeMillis() - startTime;
            Log.d("ResourceManager", "All assets loaded successfully in " + loadTime + "ms");
            
        } catch (Exception e) {
            Log.e("ResourceManager", "Error loading assets, using placeholders", e);
            createFallbackAssets();
        }
    }
    
    private void loadCriticalAssets(Context context) {
        // Load only essential assets for immediate gameplay
    playerBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.player_level_1, 100, 100);
        enemyBasicBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.enemy1, 80, 80);
        
        // Simple background first
    backgroundBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.background_level_1, 
                                                          Math.min(screenWidth, 1920), 
                                                          Math.min(screenHeight, 1080));
    }
    
    private void loadSecondaryAssets(Context context) {
        // Load additional enemies and effects
        enemyMediumBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.enemy2, 100, 100);
        enemyHeavyBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.enemy3, 120, 120);
        explosionBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.explosion1, 120, 120);
        
        // Load power-up bitmaps with smaller sizes to save memory
        powerUpHealthBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.item_health, 35, 35);
        powerUpShieldBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.iitem_shield, 35, 35);
        powerUpRapidFireBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.missile, 35, 35);
        powerUpMultiShotBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.bomb, 35, 35);
        
        // Load game over images with reduced size
        gameOverBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.game_over, 300, 120);
        youLoseBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.you_lose, 300, 120);
        
        // Load button images
        replayButtonBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.replay, 160, 64);
        menuButtonBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.menu, 160, 64);
        settingButtonBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.setting, 100, 50);
    }
    
    private void loadInitialLevelAssets(Context context) {
        // Load congratulations image and high scores button
        try {
            // Load congratulations image with optimized size
            congratulationsBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.congratulations);
            if (congratulationsBitmap != null) {
                int origW = congratulationsBitmap.getWidth();
                int origH = congratulationsBitmap.getHeight();
                // Target ~50% of screen width to reduce memory usage
                int targetW = Math.max(200, Math.min((int) (screenWidth * 0.5f), origW));
                int targetH = (int) (origH * (targetW / (float) origW));
                if (targetW != origW || targetH != origH) {
                    Bitmap oldBitmap = congratulationsBitmap;
                    congratulationsBitmap = Bitmap.createScaledBitmap(congratulationsBitmap, targetW, targetH, false);
                    oldBitmap.recycle(); // Free original bitmap memory
                }
                Log.d("ResourceManager", "Congratulations image loaded: " + origW + "x" + origH + " -> " + targetW + "x" + targetH);
            } else {
                Log.w("ResourceManager", "Failed to decode congratulations image (null bitmap)");
            }
        } catch (Exception e) {
            Log.w("ResourceManager", "Congratulations image not found, will use text instead", e);
            congratulationsBitmap = null;
        }
        
        try {
            highScoresButtonBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.high_scores, 160, 64);
            Log.d("ResourceManager", "High scores button image loaded successfully");
        } catch (Exception e) {
            Log.w("ResourceManager", "High scores button image not found, creating programmatic button", e);
            highScoresButtonBitmap = createHighScoresButton(160, 64);
        }
        
        // Create additional power-up placeholder bitmaps with smaller sizes
        powerUpLaserBitmap = createPlaceholderBitmap(35, 35, Color.rgb(100, 255, 255));
        powerUpEnergyShieldBitmap = createPlaceholderBitmap(35, 35, Color.rgb(255, 215, 0));
        powerUpForceFieldBitmap = createPlaceholderBitmap(35, 35, Color.rgb(148, 0, 211));
    }
    
    private void createFallbackAssets() {
        // Create minimal placeholder assets if loading fails
        backgroundBitmap = createPlaceholderBitmap(screenWidth, screenHeight, Color.BLUE);
        playerBitmap = createPlaceholderBitmap(100, 100, Color.GREEN);
        enemyBasicBitmap = createPlaceholderBitmap(80, 80, Color.RED);
        enemyMediumBitmap = createPlaceholderBitmap(100, 100, Color.MAGENTA);
        enemyHeavyBitmap = createPlaceholderBitmap(120, 120, Color.DKGRAY);
        explosionBitmap = createPlaceholderBitmap(120, 120, Color.YELLOW);
        
        powerUpHealthBitmap = createPlaceholderBitmap(35, 35, Color.GREEN);
        powerUpShieldBitmap = createPlaceholderBitmap(35, 35, Color.BLUE);
        powerUpRapidFireBitmap = createPlaceholderBitmap(35, 35, Color.YELLOW);
        powerUpMultiShotBitmap = createPlaceholderBitmap(35, 35, Color.CYAN);
        
        replayButtonBitmap = createPlaceholderBitmap(160, 64, Color.GREEN);
        menuButtonBitmap = createPlaceholderBitmap(160, 64, Color.BLUE);
        settingButtonBitmap = createPlaceholderBitmap(100, 50, Color.GRAY);
        highScoresButtonBitmap = createHighScoresButton(160, 64);
    }
    
    private Bitmap createPlaceholderBitmap(int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0, 0, width, height, paint);
        return bitmap;
    }
    
    private Bitmap createHighScoresButton(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Create a gradient background
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        // Background
        paint.setColor(Color.argb(200, 255, 215, 0)); // Gold
        canvas.drawRoundRect(0, 0, width, height, 10, 10, paint);
        
        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(2, 2, width-2, height-2, 10, 10, paint);
        
        // Text
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText("HIGH", width/2, height/2 - 6, paint);
        canvas.drawText("SCORES", width/2, height/2 + 8, paint);
        
        return bitmap;
    }
    
    private Bitmap loadAndScaleBitmap(Context context, int resourceId, int width, int height) {
        Bitmap original = BitmapFactory.decodeResource(context.getResources(), resourceId);
        return Bitmap.createScaledBitmap(original, width, height, true);
    }
    
    // Getters
    public Bitmap getBackgroundBitmap() { return backgroundBitmap; }
    public Bitmap getPlayerBitmap() { return playerBitmap; }
    public Bitmap getEnemyBasicBitmap() { return enemyBasicBitmap; }
    public Bitmap getEnemyMediumBitmap() { return enemyMediumBitmap; }
    public Bitmap getEnemyHeavyBitmap() { return enemyHeavyBitmap; }
    public Bitmap getExplosionBitmap() { return explosionBitmap; }
    public Bitmap getPowerUpHealthBitmap() { return powerUpHealthBitmap; }
    public Bitmap getPowerUpShieldBitmap() { return powerUpShieldBitmap; }
    public Bitmap getPowerUpRapidFireBitmap() { return powerUpRapidFireBitmap; }
    public Bitmap getPowerUpMultiShotBitmap() { return powerUpMultiShotBitmap; }
    public Bitmap getGameOverBitmap() { return gameOverBitmap; }
    public Bitmap getYouLoseBitmap() { return youLoseBitmap; }
    
    // Button getters
    public Bitmap getReplayButtonBitmap() { return replayButtonBitmap; }
    public Bitmap getMenuButtonBitmap() { return menuButtonBitmap; }
    public Bitmap getSettingButtonBitmap() { return settingButtonBitmap; }
    public Bitmap getHighScoresButtonBitmap() { return highScoresButtonBitmap; }
    
    // Win state getters
    public Bitmap getCongratulationsBitmap() { return congratulationsBitmap; }
    
    // PowerUp getters - chỉ 6 loại
    public Bitmap getPowerUpLaserBitmap() { return powerUpLaserBitmap; }
    public Bitmap getPowerUpEnergyShieldBitmap() { return powerUpEnergyShieldBitmap; }
    public Bitmap getPowerUpForceFieldBitmap() { return powerUpForceFieldBitmap; }
    
    // Setters for external resource loading
    public void setBackgroundBitmap(Bitmap bitmap) { this.backgroundBitmap = bitmap; }
    public void setPlayerBitmap(Bitmap bitmap) { this.playerBitmap = bitmap; }
    public void setEnemyBasicBitmap(Bitmap bitmap) { this.enemyBasicBitmap = bitmap; }
    public void setEnemyMediumBitmap(Bitmap bitmap) { this.enemyMediumBitmap = bitmap; }
    public void setEnemyHeavyBitmap(Bitmap bitmap) { this.enemyHeavyBitmap = bitmap; }
    public void setExplosionBitmap(Bitmap bitmap) { this.explosionBitmap = bitmap; }
    
    public void cleanup() {
        // Recycle all bitmaps to free memory
        recycleBitmap(backgroundBitmap);
        recycleBitmap(playerBitmap);
        recycleBitmap(enemyBasicBitmap);
        recycleBitmap(enemyMediumBitmap);
        recycleBitmap(enemyHeavyBitmap);
        recycleBitmap(explosionBitmap);
        recycleBitmap(powerUpHealthBitmap);
        recycleBitmap(powerUpShieldBitmap);
        recycleBitmap(powerUpRapidFireBitmap);
        recycleBitmap(powerUpMultiShotBitmap);
        recycleBitmap(gameOverBitmap);
        recycleBitmap(youLoseBitmap);
        recycleBitmap(replayButtonBitmap);
        recycleBitmap(menuButtonBitmap);
        recycleBitmap(settingButtonBitmap);
        recycleBitmap(highScoresButtonBitmap);
        recycleBitmap(congratulationsBitmap);
        recycleBitmap(powerUpLaserBitmap);
        recycleBitmap(powerUpEnergyShieldBitmap);
        recycleBitmap(powerUpForceFieldBitmap);
        recycleBitmap(currentLevelBackground);
        recycleBitmap(currentLevelPlayer);
        recycleBitmap(currentLevelEnemy);
        recycleBitmap(currentLevelBullet);
        recycleBitmap(currentLevelPowerUp);
        
        // Clear powerup cache
        clearPowerUpCache();
    }
    
    /**
     * Load level-specific resources based on level configuration
     */
    public void loadLevelResources(Context context, LevelConfig levelConfig) {
        Log.d("ResourceManager", "Loading resources for level " + levelConfig.getLevelNumber() + ": " + levelConfig.getLevelName());
        
        try {
            // Clean up previous level resources safely
            safeRecycleBitmap(currentLevelBackground);
            safeRecycleBitmap(currentLevelEnemy);
            safeRecycleBitmap(currentLevelBullet);
            safeRecycleBitmap(currentLevelPowerUp);
            
            // Don't recycle currentLevelPlayer immediately to avoid race condition
            // It will be replaced below, and the old one will be garbage collected
            
            // Clear powerup cache for level change
            clearPowerUpCache();
            
            // Load level-specific assets - ensure background is NEVER null
            currentLevelBackground = loadLevelBackground(context, levelConfig);
            if (currentLevelBackground == null || currentLevelBackground.isRecycled()) {
                Log.w("ResourceManager", "Background is null/recycled, creating emergency fallback");
                currentLevelBackground = createEmergencyBackground();
            }
            
            currentLevelPlayer = loadLevelPlayer(context, levelConfig);
            currentLevelEnemy = loadLevelEnemy(context, levelConfig);
            currentLevelBullet = loadLevelBullet(context, levelConfig);
            currentLevelPowerUp = createLevelPowerUp(levelConfig); // Still using colored powerups
            
            Log.d("ResourceManager", "Level " + levelConfig.getLevelNumber() + " resources loaded successfully");
            if (currentLevelBackground != null) {
                Log.d("ResourceManager", "BG size: " + currentLevelBackground.getWidth() + "x" + currentLevelBackground.getHeight());
            } else {
                Log.w("ResourceManager", "BG is NULL after load");
            }
            if (currentLevelPlayer != null) {
                Log.d("ResourceManager", "Player size: " + currentLevelPlayer.getWidth() + "x" + currentLevelPlayer.getHeight());
            }
            if (currentLevelEnemy != null) {
                Log.d("ResourceManager", "Enemy size: " + currentLevelEnemy.getWidth() + "x" + currentLevelEnemy.getHeight());
            }
            if (currentLevelBullet != null) {
                Log.d("ResourceManager", "Bullet size: " + currentLevelBullet.getWidth() + "x" + currentLevelBullet.getHeight());
            }
            
        } catch (Exception e) {
            Log.e("ResourceManager", "Error loading level resources", e);
            // Fallback to default assets
            currentLevelBackground = backgroundBitmap;
            currentLevelPlayer = playerBitmap;
            currentLevelEnemy = enemyBasicBitmap;
            currentLevelBullet = createPlaceholderBitmap(20, 40, Color.YELLOW);
            currentLevelPowerUp = powerUpHealthBitmap;
        }
    }
    
    /**
     * Load level-specific background image with scaling
     */
    private Bitmap loadLevelBackground(Context context, LevelConfig levelConfig) {
        try {
            String imagePath = levelConfig.getBackgroundImagePath();
            Log.d("ResourceManager", "=== BACKGROUND LOADING DEBUG ===");
            Log.d("ResourceManager", "Level: " + levelConfig.getLevelNumber() + " - " + levelConfig.getLevelName());
            Log.d("ResourceManager", "Background path from config: " + imagePath);
            
            int resId = getDrawableResourceId(context, imagePath);
            Log.d("ResourceManager", "Resource ID found: " + resId);
            
            if (resId != 0) {
                Log.d("ResourceManager", "Loading background image: " + imagePath);
                Bitmap loadedBitmap = BitmapUtils.loadOptimizedBitmap(context, resId, screenWidth, screenHeight);
                if (loadedBitmap != null) {
                    Log.d("ResourceManager", "Background loaded successfully: " + loadedBitmap.getWidth() + "x" + loadedBitmap.getHeight());
                    return loadedBitmap;
                } else {
                    Log.w("ResourceManager", "Background bitmap is null after loading");
                }
            }
        } catch (Exception e) {
            Log.w("ResourceManager", "Failed to load background image: " + e.getMessage());
        }
        
        // Create a visible test background instead of fallback
        Log.d("ResourceManager", "Creating test colored background for debugging");
        Bitmap testBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(testBitmap);
        
        // Create a recognizable pattern
        Paint paint = new Paint();
        paint.setColor(Color.CYAN);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
        
        // Add some diagonal stripes to make it obvious
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(20);
        for (int i = 0; i < screenWidth + screenHeight; i += 100) {
            canvas.drawLine(i, 0, i - screenHeight, screenHeight, paint);
        }
        
        Log.d("ResourceManager", "Created test striped background: " + screenWidth + "x" + screenHeight);
        return testBitmap;
    }
    
    /**
     * Load level-specific player image with scaling
     */
    private Bitmap loadLevelPlayer(Context context, LevelConfig levelConfig) {
        try {
            String imagePath = levelConfig.getPlayerImagePath();
            int resId = getDrawableResourceId(context, imagePath);
            
            if (resId != 0) {
                Log.d("ResourceManager", "Loading player image: " + imagePath);
                // Load and scale to a consistent size
                return BitmapUtils.loadOptimizedBitmap(context, resId, 100, 100);
            }
        } catch (Exception e) {
            Log.w("ResourceManager", "Failed to load player image: " + e.getMessage());
        }
        
        // Fallback to colored player
        return createLevelPlayer(levelConfig);
    }
    
    /**
     * Load level-specific enemy image with scaling
     */
    private Bitmap loadLevelEnemy(Context context, LevelConfig levelConfig) {
        try {
            String imagePath = levelConfig.getEnemyImagePath();
            int resId = getDrawableResourceId(context, imagePath);
            
            if (resId != 0) {
                Log.d("ResourceManager", "Loading enemy image: " + imagePath);
                return BitmapUtils.loadOptimizedBitmap(context, resId, 60, 120); // Double height: 60x120
            }
        } catch (Exception e) {
            Log.w("ResourceManager", "Failed to load enemy image: " + e.getMessage());
        }
        
        // Fallback to colored enemy
        return createLevelEnemy(levelConfig);
    }
    
    /**
     * Load level-specific bullet image with scaling
     */
    private Bitmap loadLevelBullet(Context context, LevelConfig levelConfig) {
        try {
            String imagePath = levelConfig.getBulletImagePath();
            Log.d("ResourceManager", "=== BULLET LOADING DEBUG ===");
            Log.d("ResourceManager", "Level: " + levelConfig.getLevelNumber() + " - " + levelConfig.getLevelName());
            Log.d("ResourceManager", "Bullet path from config: " + imagePath);
            
            int resId = getDrawableResourceId(context, imagePath);
            Log.d("ResourceManager", "Bullet resource ID found: " + resId);
            
            if (resId != 0) {
                Log.d("ResourceManager", "Loading bullet image: " + imagePath);
                Bitmap loadedBullet = BitmapUtils.loadOptimizedBitmap(context, resId, 20, 40);
                if (loadedBullet != null) {
                    Log.d("ResourceManager", "Bullet loaded successfully");
                    return loadedBullet;
                }
            }
        } catch (Exception e) {
            Log.w("ResourceManager", "Failed to load bullet image: " + e.getMessage());
        }
        
        // Create a visible test bullet
        Log.d("ResourceManager", "Creating test colored bullet");
        Bitmap testBullet = Bitmap.createBitmap(20, 40, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(testBullet);
        Paint paint = new Paint();
        
        // Make bullet very bright and distinctive
        paint.setColor(Color.YELLOW);
        canvas.drawRect(0, 0, 20, 40, paint);
        
        // Add red border
        paint.setColor(Color.RED);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(1, 1, 19, 39, paint);
        
        Log.d("ResourceManager", "Created bright test bullet");
        return testBullet;
    }
    
    /**
     * Helper method to get drawable resource ID from path
     */
    private int getDrawableResourceId(Context context, String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            Log.e("ResourceManager", "Image path is null or empty");
            return 0;
        }
        
        // Remove any file extensions
        String resourceName = imagePath.replace(".png", "").replace(".jpg", "");
        
        Log.d("ResourceManager", "Looking for drawable resource: " + resourceName);
        Log.d("ResourceManager", "Package name: " + context.getPackageName());
        
        // Get resource ID
        int resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        Log.d("ResourceManager", "Resource ID for " + resourceName + ": " + resId);
        
        // Additional verification - try to get the resource directly
        if (resId != 0) {
            try {
                context.getResources().getDrawable(resId);
                Log.d("ResourceManager", "Resource " + resourceName + " exists and accessible");
                
                // Test direct R.drawable access for comparison
                try {
                    java.lang.reflect.Field field = com.example.templerunclone.R.drawable.class.getField(resourceName);
                    int directResId = field.getInt(null);
                    Log.d("ResourceManager", "Direct R.drawable." + resourceName + " = " + directResId);
                    
                    if (resId != directResId) {
                        Log.w("ResourceManager", "Resource ID mismatch! Using direct access.");
                        return directResId;
                    }
                } catch (Exception e) {
                    Log.w("ResourceManager", "Could not access R.drawable." + resourceName + " directly: " + e.getMessage());
                }
                
            } catch (Exception e) {
                Log.e("ResourceManager", "Resource " + resourceName + " exists but not accessible: " + e.getMessage());
                return 0;
            }
        } else {
            Log.e("ResourceManager", "Resource " + resourceName + " not found!");
            
            // List available resources for debugging
            try {
                java.lang.reflect.Field[] fields = com.example.templerunclone.R.drawable.class.getFields();
                Log.d("ResourceManager", "Available drawable resources:");
                for (java.lang.reflect.Field field : fields) {
                    if (field.getName().startsWith("background")) {
                        Log.d("ResourceManager", "  - " + field.getName());
                    }
                }
            } catch (Exception e) {
                Log.w("ResourceManager", "Could not list drawable resources");
            }
        }
        
        return resId;
    }
    
    private Bitmap createEmergencyBackground() {
        Log.w("ResourceManager", "Creating emergency background to prevent null");
        Bitmap emergency = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(emergency);
        Paint paint = new Paint();
        
        // Create a very obvious emergency pattern
        paint.setColor(Color.RED);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
        
        paint.setColor(Color.YELLOW);
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("EMERGENCY BG", screenWidth/2, screenHeight/2, paint);
        
        Log.d("ResourceManager", "Emergency background created: " + screenWidth + "x" + screenHeight);
        return emergency;
    }

    private Bitmap createLevelBackground(LevelConfig levelConfig) {
        // TODO: Load actual asset from levelConfig.getBackgroundImagePath()
        // For now, create colored background based on level
        Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        
        // Set color based on level theme
        paint.setColor(levelConfig.getBackgroundColor());
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
        
        // Add some level-specific visual elements
        paint.setColor(Color.argb(100, 255, 255, 255));
        for (int i = 0; i < 20; i++) {
            float x = (float) (Math.random() * screenWidth);
            float y = (float) (Math.random() * screenHeight);
            canvas.drawCircle(x, y, 5, paint);
        }
        
        return bitmap;
    }
    
    private Bitmap createLevelPlayer(LevelConfig levelConfig) {
        // TODO: Load actual asset from levelConfig.getPlayerImagePath()
        // For now, create a colored placeholder based on level
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        // Different player appearance per level
        switch (levelConfig.getLevelNumber()) {
            case 1:
                paint.setColor(Color.GREEN); // Forest theme
                break;
            case 2:
                paint.setColor(Color.YELLOW); // Desert theme
                break;
            case 3:
                paint.setColor(Color.CYAN); // Ice theme
                break;
            default:
                paint.setColor(Color.WHITE);
                break;
        }
        
        canvas.drawRect(20, 20, 80, 80, paint);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(20, 20, 80, 80, paint);
        
        return bitmap;
    }
    
    private Bitmap createLevelEnemy(LevelConfig levelConfig) {
        // TODO: Load actual asset from levelConfig.getEnemyImagePath()
        Bitmap bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        // Different enemy appearance per level
        switch (levelConfig.getLevelNumber()) {
            case 1:
                paint.setColor(Color.rgb(139, 69, 19)); // Brown for forest creatures
                break;
            case 2:
                paint.setColor(Color.rgb(255, 140, 0)); // Orange for desert enemies
                break;
            case 3:
                paint.setColor(Color.rgb(70, 130, 180)); // Steel blue for ice enemies
                break;
            default:
                paint.setColor(Color.RED);
                break;
        }
        
        canvas.drawOval(10, 10, 70, 70, paint);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawOval(10, 10, 70, 70, paint);
        
        return bitmap;
    }
    
    private Bitmap createLevelBullet(LevelConfig levelConfig) {
        // TODO: Load actual asset from levelConfig.getBulletImagePath()
        Bitmap bitmap = Bitmap.createBitmap(20, 40, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        // Different bullet appearance per level
        switch (levelConfig.getLevelNumber()) {
            case 1:
                paint.setColor(Color.rgb(34, 139, 34)); // Forest green arrow
                break;
            case 2:
                paint.setColor(Color.rgb(255, 69, 0)); // Red-orange fire bullet
                break;
            case 3:
                paint.setColor(Color.rgb(135, 206, 235)); // Light blue ice shard
                break;
            default:
                paint.setColor(Color.YELLOW);
                break;
        }
        
        canvas.drawRect(5, 0, 15, 40, paint);
        
        return bitmap;
    }
    
    private Bitmap createLevelPowerUp(LevelConfig levelConfig) {
        // Create a generic powerup bitmap - will be replaced by type-specific ones
        return createLevelPowerUpByType(levelConfig, com.example.templerunclone.entities.PowerUp.PowerUpType.HEALTH);
    }
    
    /**
     * Create level-specific color-coded powerup bitmap based on type (with caching)
     */
    public Bitmap createLevelPowerUpByType(LevelConfig levelConfig, com.example.templerunclone.entities.PowerUp.PowerUpType powerUpType) {
        // Create cache key
        String cacheKey = levelConfig.getLevelNumber() + "_" + powerUpType.name();
        
        // Check if already cached
        Bitmap cachedBitmap = levelPowerUpCache.get(cacheKey);
        if (cachedBitmap != null && !cachedBitmap.isRecycled()) {
            return cachedBitmap;
        }
        
        Bitmap bitmap = Bitmap.createBitmap(40, 40, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        // Base colors per level
        int baseColor, accentColor;
        switch (levelConfig.getLevelNumber()) {
            case 1: // Forest Temple - Green theme
                baseColor = Color.rgb(34, 139, 34);
                accentColor = Color.rgb(50, 205, 50);
                break;
            case 2: // Desert Ruins - Orange/Gold theme
                baseColor = Color.rgb(255, 140, 0);
                accentColor = Color.rgb(255, 215, 0);
                break;
            case 3: // Ice Cavern - Blue/Cyan theme
                baseColor = Color.rgb(0, 100, 200);
                accentColor = Color.rgb(0, 191, 255);
                break;
            default:
                baseColor = Color.GRAY;
                accentColor = Color.WHITE;
                break;
        }
        
        // Modify colors based on powerup type
        switch (powerUpType) {
            case HEALTH:
                // Red cross symbol
                paint.setColor(baseColor);
                canvas.drawCircle(20, 20, 18, paint);
                paint.setColor(Color.rgb(220, 20, 60)); // Crimson
                paint.setStrokeWidth(4);
                canvas.drawLine(20, 10, 20, 30, paint);
                canvas.drawLine(10, 20, 30, 20, paint);
                break;
                
            case SHIELD:
                // Shield shape with base color
                paint.setColor(baseColor);
                canvas.drawCircle(20, 20, 18, paint);
                paint.setColor(accentColor);
                paint.setStrokeWidth(3);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(20, 20, 15, paint);
                canvas.drawCircle(20, 20, 10, paint);
                break;
                
            case RAPID_FIRE:
                // Lightning bolt symbol
                paint.setColor(baseColor);
                canvas.drawCircle(20, 20, 18, paint);
                paint.setColor(Color.YELLOW);
                paint.setStrokeWidth(3);
                // Simple zigzag lightning
                canvas.drawLine(15, 8, 12, 15, paint);
                canvas.drawLine(12, 15, 25, 18, paint);
                canvas.drawLine(25, 18, 22, 25, paint);
                canvas.drawLine(22, 25, 28, 32, paint);
                break;
                
            case MULTI_SHOT:
                // Multiple arrows
                paint.setColor(baseColor);
                canvas.drawCircle(20, 20, 18, paint);
                paint.setColor(accentColor);
                paint.setStrokeWidth(2);
                // Three small arrows
                canvas.drawLine(12, 12, 18, 15, paint);
                canvas.drawLine(12, 20, 18, 20, paint);
                canvas.drawLine(12, 28, 18, 25, paint);
                canvas.drawLine(18, 15, 28, 12, paint);
                canvas.drawLine(18, 20, 28, 20, paint);
                canvas.drawLine(18, 25, 28, 28, paint);
                break;
                
            case LASER_BEAM:
                // Laser beam lines
                paint.setColor(baseColor);
                canvas.drawCircle(20, 20, 18, paint);
                paint.setColor(Color.rgb(255, 0, 255)); // Magenta laser
                paint.setStrokeWidth(2);
                for (int i = 0; i < 5; i++) {
                    canvas.drawLine(8, 12 + i * 3, 32, 12 + i * 3, paint);
                }
                break;
                
            case ENERGY_SHIELD:
                // Energy field effect
                paint.setColor(baseColor);
                canvas.drawCircle(20, 20, 18, paint);
                paint.setColor(Color.rgb(255, 215, 0)); // Gold energy
                paint.setStrokeWidth(2);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(20, 20, 16, paint);
                canvas.drawCircle(20, 20, 12, paint);
                canvas.drawCircle(20, 20, 8, paint);
                break;
                
            case FORCE_FIELD:
                // Force field hexagon
                paint.setColor(baseColor);
                canvas.drawCircle(20, 20, 18, paint);
                paint.setColor(Color.rgb(148, 0, 211)); // Dark violet
                paint.setStrokeWidth(3);
                paint.setStyle(Paint.Style.STROKE);
                // Hexagon shape
                float[] hexagon = {
                    20, 8,  // top
                    30, 14, // top right
                    30, 26, // bottom right
                    20, 32, // bottom
                    10, 26, // bottom left
                    10, 14  // top left
                };
                for (int i = 0; i < hexagon.length - 2; i += 2) {
                    canvas.drawLine(hexagon[i], hexagon[i + 1], 
                                    hexagon[(i + 2) % hexagon.length], 
                                    hexagon[(i + 3) % hexagon.length], paint);
                }
                break;
                
            default:
                // Default circle
                paint.setColor(baseColor);
                canvas.drawCircle(20, 20, 18, paint);
                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(2);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(20, 20, 15, paint);
                break;
        }
        
        // Cache the bitmap
        levelPowerUpCache.put(cacheKey, bitmap);
        
        return bitmap;
    }
    
    // Getters for level-specific assets
    public Bitmap getCurrentLevelBackground() { 
        Bitmap bg = currentLevelBackground != null ? currentLevelBackground : backgroundBitmap;
        android.util.Log.d("ResourceManager", "getCurrentLevelBackground called - returning: " + 
            (bg != null ? bg.getWidth() + "x" + bg.getHeight() : "null"));
        return bg;
    }
    public Bitmap getCurrentLevelPlayer() { return currentLevelPlayer != null ? currentLevelPlayer : playerBitmap; }
    public Bitmap getCurrentLevelEnemy() { return currentLevelEnemy != null ? currentLevelEnemy : enemyBasicBitmap; }
    public Bitmap getCurrentLevelBullet() { return currentLevelBullet; }
    public Bitmap getCurrentLevelPowerUp() { return currentLevelPowerUp != null ? currentLevelPowerUp : powerUpHealthBitmap; }
    
    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
    
    private void safeRecycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            try {
                bitmap.recycle();
            } catch (Exception e) {
                android.util.Log.w("ResourceManager", "Error recycling bitmap: " + e.getMessage());
            }
        }
    }
    
    private void clearPowerUpCache() {
        for (Bitmap bitmap : levelPowerUpCache.values()) {
            safeRecycleBitmap(bitmap);
        }
        levelPowerUpCache.clear();
    }
}