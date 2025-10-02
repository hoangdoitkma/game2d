package com.example.templerunclone.engine;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.example.templerunclone.entities.PowerUp;

/**
 * Manages game state including score, level, power-ups effects
 */
public class GameState {
    private int score = 0;
    private int level = 1;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private boolean paused = false;
    
    // Power-up effects - chỉ giữ 6 loại
    private boolean rapidFireActive = false;
    private boolean multiShotActive = false;
    private boolean shieldActive = false;
    private boolean laserBeamActive = false;
    private boolean energyShieldActive = false;
    private boolean forceFieldActive = false;
    
    private long rapidFireEndTime = 0;
    private long multiShotEndTime = 0;
    private long shieldEndTime = 0;
    private long laserBeamEndTime = 0;
    private long energyShieldEndTime = 0;
    private long forceFieldEndTime = 0;
    
    // Game progression
    private float speedMultiplier = 1.0f;
    private long gameStartTime;
    private String pendingAction = null; // For handling UI actions
    
    public GameState() {
        gameStartTime = System.currentTimeMillis();
    }
    
    public long getGameStartTime() {
        return gameStartTime;
    }
    
    public void update(float deltaTime) {
        if (gameOver || gameWon || paused) return;
        
        // Update level based on score - không thay đổi speed multiplier
        int newLevel = (score / 100) + 1;
        if (newLevel != level) {
            level = newLevel;
            // speedMultiplier giữ nguyên, không tăng
            
            // Check for WIN condition - reach level 2
            if (level >= 2) {
                gameWon = true;
                return;
            }
        }
        
        // Update power-up effects - Bật lại để tắt khi hết thời gian
        long currentTime = System.currentTimeMillis();
        
        if (rapidFireActive && currentTime > rapidFireEndTime) {
            rapidFireActive = false;
        }
        
        if (multiShotActive && currentTime > multiShotEndTime) {
            multiShotActive = false;
        }
        
        if (shieldActive && currentTime > shieldEndTime) {
            shieldActive = false;
        }
        
        if (laserBeamActive && currentTime > laserBeamEndTime) {
            laserBeamActive = false;
        }
        
        if (energyShieldActive && currentTime > energyShieldEndTime) {
            energyShieldActive = false;
        }
        
        if (forceFieldActive && currentTime > forceFieldEndTime) {
            forceFieldActive = false;
        }
    }
    
    public void applyPowerUp(PowerUp powerUp) {
        long currentTime = System.currentTimeMillis();
        
        switch (powerUp.getType()) {
            case SHIELD:
                shieldActive = true;
                shieldEndTime = currentTime + powerUp.getDuration();
                break;
                
            case RAPID_FIRE:
                rapidFireActive = true;
                rapidFireEndTime = currentTime + powerUp.getDuration();
                break;
                
            case MULTI_SHOT:
                multiShotActive = true;
                multiShotEndTime = currentTime + powerUp.getDuration();
                break;
                
            case LASER_BEAM:
                laserBeamActive = true;
                laserBeamEndTime = currentTime + powerUp.getDuration();
                break;
                
            case ENERGY_SHIELD:
                energyShieldActive = true;
                energyShieldEndTime = currentTime + powerUp.getDuration();
                break;
                
            case FORCE_FIELD:
                forceFieldActive = true;
                forceFieldEndTime = currentTime + powerUp.getDuration();
                break;
        }
    }
    
    public void addScore(int points) {
        score += points;
    }
    
    public void drawUI(Canvas canvas, Paint paint, int screenWidth, int screenHeight) {
        // Draw score
        paint.setColor(Color.WHITE);
        paint.setTextSize(60);
        canvas.drawText("Score: " + score, 50, 100, paint);
        
        // Draw level
        canvas.drawText("Level: " + level, 50, 180, paint);
        
        // Draw active power-ups
        int yOffset = 260;
        paint.setTextSize(40);
        
        if (rapidFireActive) {
            paint.setColor(Color.YELLOW);
            canvas.drawText("RAPID FIRE", 50, yOffset, paint);
            yOffset += 50;
        }
        
        if (multiShotActive) {
            paint.setColor(Color.CYAN);
            canvas.drawText("MULTI SHOT", 50, yOffset, paint);
            yOffset += 50;
        }
        
        if (shieldActive) {
            paint.setColor(Color.BLUE);
            canvas.drawText("SHIELD", 50, yOffset, paint);
        }
        
        // Draw game over screen
        if (gameOver) {
            paint.setColor(Color.RED);
            paint.setTextSize(80);
            canvas.drawText("GAME OVER", screenWidth / 2f - 200, screenHeight / 2f, paint);
            
            paint.setTextSize(50);
            canvas.drawText("Final Score: " + score, screenWidth / 2f - 150, screenHeight / 2f + 80, paint);
        }
        
        // Draw game won screen
        if (gameWon) {
            paint.setColor(Color.rgb(255, 215, 0)); // Gold color
            paint.setTextSize(80);
            canvas.drawText("CONGRATULATIONS!", screenWidth / 2f - 300, screenHeight / 2f - 50, paint);
            
            paint.setColor(Color.GREEN);
            paint.setTextSize(60);
            canvas.drawText("YOU WIN!", screenWidth / 2f - 120, screenHeight / 2f + 20, paint);
            
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            canvas.drawText("Final Score: " + score, screenWidth / 2f - 150, screenHeight / 2f + 100, paint);
        }
    }
    
    public void reset() {
        score = 0;
        level = 1;
        gameOver = false;
        gameWon = false;
        paused = false;
        speedMultiplier = 1.0f;
        
        // Reset power-ups - chỉ 6 loại
        rapidFireActive = false;
        multiShotActive = false;
        shieldActive = false;
        laserBeamActive = false;
        energyShieldActive = false;
        forceFieldActive = false;
        energyShieldActive = false;
        forceFieldActive = false;
        
        gameStartTime = System.currentTimeMillis();
    }
    
    // Getters
    public int getScore() { return score; }
    public int getLevel() { return level; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
    public boolean isPaused() { return paused; }
    public float getSpeedMultiplier() { return speedMultiplier; }
    
    public boolean isRapidFireActive() { return rapidFireActive; }
    public boolean isMultiShotActive() { return multiShotActive; }
    public boolean isShieldActive() { return shieldActive; }
    public boolean isLaserBeamActive() { return laserBeamActive; }
    public boolean isEnergyShieldActive() { return energyShieldActive; }
    public boolean isForceFieldActive() { return forceFieldActive; }
    
    // Setters
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public void setGameWon(boolean gameWon) { this.gameWon = gameWon; }
    public void setPaused(boolean paused) { this.paused = paused; }
    public void setAction(String action) { this.pendingAction = action; }
    
    public String getPendingAction() {
        String action = pendingAction;
        pendingAction = null; // Clear after getting
        return action;
    }
}