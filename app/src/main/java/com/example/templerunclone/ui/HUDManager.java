package com.example.templerunclone.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import com.example.templerunclone.engine.GameState;
import com.example.templerunclone.entities.Player;

/**
 * HUD Manager để hiển thị thông tin game trên màn hình
 */
public class HUDManager {
    private Paint textPaint;
    private Paint backgroundPaint;
    private Paint healthBarPaint;
    private Paint healthBackgroundPaint;
    
    private int screenWidth, screenHeight;
    private long gameStartTime;
    private long gameEndTime = -1; // Thời điểm game over
    private boolean isGameRunning = true;
    
    public HUDManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.gameStartTime = System.currentTimeMillis();
        
        initializePaints();
    }
    
    private void initializePaints() {
        // Text paint
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50);
        textPaint.setAntiAlias(true);
        
        // Background paint for HUD elements
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.argb(120, 0, 0, 0)); // Semi-transparent black
        
        // Health bar paint
        healthBarPaint = new Paint();
        healthBarPaint.setColor(Color.GREEN);
        
        // Health background paint
        healthBackgroundPaint = new Paint();
        healthBackgroundPaint.setColor(Color.RED);
    }
    
    public void draw(Canvas canvas, GameState gameState, Player player) {
        drawTopHUD(canvas, gameState, player);
        drawBottomHUD(canvas, gameState);
        drawLeftHUD(canvas, gameState); // Chuyển từ right sang left
    }
    
    private void drawTopHUD(Canvas canvas, GameState gameState, Player player) {
        // Background for top HUD
        canvas.drawRect(0, 0, screenWidth, 120, backgroundPaint);
        
        // Score
        textPaint.setTextSize(60);
        textPaint.setColor(Color.YELLOW);
        canvas.drawText("Score: " + gameState.getScore(), 30, 50, textPaint);
        
        // Level
        textPaint.setColor(Color.CYAN);
        canvas.drawText("Level: " + gameState.getLevel(), 30, 100, textPaint);
        
        // Health Bar
        drawHealthBar(canvas, player, screenWidth - 250, 20);
    }
    
    private void drawBottomHUD(Canvas canvas, GameState gameState) {
        // Background for bottom HUD
        int hudHeight = 100;
        canvas.drawRect(0, screenHeight - hudHeight, screenWidth, screenHeight, backgroundPaint);
        
        // Game Time - dừng khi game over
        long playTime;
        if (isGameRunning) {
            playTime = (System.currentTimeMillis() - gameStartTime) / 1000;
        } else {
            playTime = (gameEndTime - gameStartTime) / 1000;
        }
        String timeText = String.format("Time: %02d:%02d", playTime / 60, playTime % 60);
        textPaint.setTextSize(45);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(timeText, 30, screenHeight - 30, textPaint);
        
        // Speed Multiplier
        String speedText = String.format("Speed: %.1fx", gameState.getSpeedMultiplier());
        textPaint.setColor(Color.rgb(255, 165, 0)); // Orange color
        canvas.drawText(speedText, screenWidth - 200, screenHeight - 30, textPaint);
    }
    
    private void drawLeftHUD(Canvas canvas, GameState gameState) {
        // Active Power-ups display - hiển thị ở lề trái
        int yOffset = 200;
        textPaint.setTextSize(40);
        
        if (gameState.isRapidFireActive()) {
            textPaint.setColor(Color.YELLOW);
            canvas.drawText("⚡ RAPID", 20, yOffset, textPaint);
            yOffset += 50;
        }
        
        if (gameState.isMultiShotActive()) {
            textPaint.setColor(Color.CYAN);
            canvas.drawText("🔥 MULTI", 20, yOffset, textPaint);
            yOffset += 50;
        }
        
        if (gameState.isLaserBeamActive()) {
            textPaint.setColor(Color.CYAN);
            canvas.drawText("🔵 LASER", 20, yOffset, textPaint);
            yOffset += 50;
        }
        
        if (gameState.isShieldActive()) {
            textPaint.setColor(Color.BLUE);
            canvas.drawText("🛡 SHIELD", 20, yOffset, textPaint);
            yOffset += 50;
        }
        
        if (gameState.isEnergyShieldActive()) {
            textPaint.setColor(Color.rgb(255, 215, 0)); // Gold
            canvas.drawText("⚡ ENERGY", 20, yOffset, textPaint);
            yOffset += 50;
        }
        
        if (gameState.isForceFieldActive()) {
            textPaint.setColor(Color.rgb(148, 0, 211)); // Dark violet
            canvas.drawText("🔮 FORCE", 20, yOffset, textPaint);
            yOffset += 50;
        }
    }
    
    private void drawHealthBar(Canvas canvas, Player player, float x, float y) {
        float barWidth = 200;
        float barHeight = 20;
        
        // Draw health background (red)
        canvas.drawRect(x, y, x + barWidth, y + barHeight, healthBackgroundPaint);
        
        // Draw current health (green)
        float healthPercent = (float) player.getHealth() / player.getMaxHealth();
        canvas.drawRect(x, y, x + (barWidth * healthPercent), y + barHeight, healthBarPaint);
        
        // Draw health text
        textPaint.setTextSize(35);
        textPaint.setColor(Color.WHITE);
        String healthText = player.getHealth() + "/" + player.getMaxHealth();
        canvas.drawText(healthText, x + 50, y + 15, textPaint);
        
        // Health label
        textPaint.setTextSize(30);
        canvas.drawText("HP:", x - 40, y + 15, textPaint);
    }
    
    public void reset() {
        gameStartTime = System.currentTimeMillis();
        gameEndTime = -1;
        isGameRunning = true;
    }
    
    public void stopTimer() {
        if (isGameRunning) {
            gameEndTime = System.currentTimeMillis();
            isGameRunning = false;
        }
    }
    
    public long getPlayTime() {
        if (isGameRunning) {
            return (System.currentTimeMillis() - gameStartTime) / 1000;
        } else {
            return (gameEndTime - gameStartTime) / 1000;
        }
    }
}