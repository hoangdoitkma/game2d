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
    
    private int screenWidth, screenHeight;
    
    public ResourceManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    
    public void loadResources(Context context) {
        try {
            // Load actual game assets with optimized loading
            backgroundBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.background, screenWidth, screenHeight);
            playerBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.ship, 100, 100);
            enemyBasicBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.enemy1, 80, 80);
            enemyMediumBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.enemy2, 100, 100);
            enemyHeavyBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.enemy3, 120, 120);
            explosionBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.explosion1, 120, 120);
            
            // Load power-up bitmaps
            powerUpHealthBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.item_health, 40, 40);
            powerUpShieldBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.iitem_shield, 40, 40);
            powerUpRapidFireBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.missile, 40, 40);
            powerUpMultiShotBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.bomb, 40, 40);
            
            // Load game over images
            gameOverBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.game_over, 400, 150);
            youLoseBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.you_lose, 400, 150);
            
            // Load button images từ res/drawable
            replayButtonBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.replay, 200, 80);
            menuButtonBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.menu, 200, 80);
            settingButtonBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.setting, 120, 60);
            
            // Try to load congratulations image and high scores button
            try {
                // Try to load congratulations image with direct BitmapFactory first
                congratulationsBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.congratulations);
                if (congratulationsBitmap != null) {
                    int origW = congratulationsBitmap.getWidth();
                    int origH = congratulationsBitmap.getHeight();
                    // Target ~60% of screen width, preserve aspect ratio
                    int targetW = Math.max(200, Math.min((int) (screenWidth * 0.6f), origW));
                    int targetH = (int) (origH * (targetW / (float) origW));
                    if (targetW != origW || targetH != origH) {
                        congratulationsBitmap = Bitmap.createScaledBitmap(congratulationsBitmap, targetW, targetH, true);
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
                highScoresButtonBitmap = BitmapUtils.loadOptimizedBitmap(context, R.drawable.high_scores, 200, 80);
                Log.d("ResourceManager", "High scores button image loaded successfully");
            } catch (Exception e) {
                Log.w("ResourceManager", "High scores button image not found, creating programmatic button", e);
                highScoresButtonBitmap = createHighScoresButton(200, 80);
            }
            
            // Create additional power-up placeholder bitmaps - chỉ 3 loại cần thiết
            powerUpLaserBitmap = createPlaceholderBitmap(40, 40, Color.rgb(100, 255, 255));
            powerUpEnergyShieldBitmap = createPlaceholderBitmap(40, 40, Color.rgb(255, 215, 0)); // Gold
            powerUpForceFieldBitmap = createPlaceholderBitmap(40, 40, Color.rgb(148, 0, 211)); // Dark violet
            
            Log.d("ResourceManager", "All assets loaded successfully");
            
        } catch (Exception e) {
            Log.e("ResourceManager", "Error loading assets, using placeholders", e);
            
            // Fallback to placeholder bitmaps if resource loading fails
            backgroundBitmap = createPlaceholderBitmap(screenWidth, screenHeight, Color.BLUE);
            playerBitmap = createPlaceholderBitmap(100, 100, Color.GREEN);
            enemyBasicBitmap = createPlaceholderBitmap(80, 80, Color.RED);
            enemyMediumBitmap = createPlaceholderBitmap(100, 100, Color.MAGENTA);
            enemyHeavyBitmap = createPlaceholderBitmap(120, 120, Color.DKGRAY);
            explosionBitmap = createPlaceholderBitmap(120, 120, Color.YELLOW);
            
            powerUpHealthBitmap = createPlaceholderBitmap(40, 40, Color.GREEN);
            powerUpShieldBitmap = createPlaceholderBitmap(40, 40, Color.BLUE);
            powerUpRapidFireBitmap = createPlaceholderBitmap(40, 40, Color.YELLOW);
            powerUpMultiShotBitmap = createPlaceholderBitmap(40, 40, Color.CYAN);
        }
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
    }
    
    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
}