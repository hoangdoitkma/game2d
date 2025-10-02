package com.example.templerunclone.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Game Over Manager để xử lý UI khi game kết thúc
 */
public class GameOverManager {
    private Paint textPaint;
    private Paint buttonPaint;
    private Paint buttonTextPaint;
    private Paint backgroundPaint;
    
    private Bitmap gameOverBitmap;
    private Bitmap youLoseBitmap;
    
    // Button bitmaps từ res/
    private Bitmap replayButtonBitmap;
    private Bitmap menuButtonBitmap;
    private Bitmap settingButtonBitmap;
    
    // Button rectangles
    private Rect replayButton;
    private Rect homeButton;
    private Rect menuButton; // Thay thế scores button
    
    private int screenWidth, screenHeight;
    private boolean useGameOverImage = true; // Toggle between "Game Over" and "YOU LOSE"
    
    public GameOverManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        initializePaints();
        setupButtons();
    }
    
    private void initializePaints() {
        // Background paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.argb(180, 0, 0, 0)); // Semi-transparent black
        
        // Text paint
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        // Button background paint
        buttonPaint = new Paint();
        buttonPaint.setColor(Color.argb(200, 50, 50, 50)); // Dark gray
        buttonPaint.setAntiAlias(true);
        
        // Button text paint
        buttonTextPaint = new Paint();
        buttonTextPaint.setColor(Color.WHITE);
        buttonTextPaint.setTextSize(50);
        buttonTextPaint.setAntiAlias(true);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
    }
    
    private void setupButtons() {
        int buttonWidth = 250;
        int buttonHeight = 80;
        int buttonSpacing = 30;
        int centerX = screenWidth / 2;
        int startY = screenHeight / 2 + 100;
        
        // Replay Button
        replayButton = new Rect(
            centerX - buttonWidth / 2,
            startY,
            centerX + buttonWidth / 2,
            startY + buttonHeight
        );
        
        // Home Button
        homeButton = new Rect(
            centerX - buttonWidth / 2,
            startY + buttonHeight + buttonSpacing,
            centerX + buttonWidth / 2,
            startY + 2 * buttonHeight + buttonSpacing
        );
        
        // Menu Button (thay thế High Score)
        menuButton = new Rect(
            centerX - buttonWidth / 2,
            startY + 2 * (buttonHeight + buttonSpacing),
            centerX + buttonWidth / 2,
            startY + 3 * buttonHeight + 2 * buttonSpacing
        );
    }
    
    public void setButtonBitmaps(Bitmap replayBitmap, Bitmap menuBitmap, Bitmap settingBitmap) {
        this.replayButtonBitmap = replayBitmap;
        this.menuButtonBitmap = menuBitmap;
        this.settingButtonBitmap = settingBitmap;
    }
    
    public void draw(Canvas canvas, int finalScore, int level, long playTime) {
        // Draw semi-transparent background
        canvas.drawRect(0, 0, screenWidth, screenHeight, backgroundPaint);
        
        // Draw Game Over image or text
        drawGameOverTitle(canvas);
        
        // Draw final stats
        drawFinalStats(canvas, finalScore, level, playTime);
        
        // Draw buttons
        drawButtons(canvas);
    }
    
    private void drawGameOverTitle(Canvas canvas) {
        int centerX = screenWidth / 2;
        int titleY = screenHeight / 3;
        
        if (gameOverBitmap != null && useGameOverImage) {
            // Draw GameOver.png if available
            int imageWidth = 400;
            int imageHeight = 150;
            Rect destRect = new Rect(
                centerX - imageWidth / 2,
                titleY - imageHeight / 2,
                centerX + imageWidth / 2,
                titleY + imageHeight / 2
            );
            canvas.drawBitmap(gameOverBitmap, null, destRect, null);
        } else if (youLoseBitmap != null && !useGameOverImage) {
            // Draw YOU LOSE.png if available
            int imageWidth = 400;
            int imageHeight = 150;
            Rect destRect = new Rect(
                centerX - imageWidth / 2,
                titleY - imageHeight / 2,
                centerX + imageWidth / 2,
                titleY + imageHeight / 2
            );
            canvas.drawBitmap(youLoseBitmap, null, destRect, null);
        } else {
            // Fallback to text
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(80);
            canvas.drawText("GAME OVER", centerX, titleY, textPaint);
        }
    }
    
    private void drawFinalStats(Canvas canvas, int finalScore, int level, long playTime) {
        int centerX = screenWidth / 2;
        int statsY = screenHeight / 2 - 50;
        
        textPaint.setColor(Color.YELLOW);
        textPaint.setTextSize(50);
        
        // Final Score
        canvas.drawText("Final Score: " + finalScore, centerX, statsY, textPaint);
        
        // Level Reached
        textPaint.setColor(Color.CYAN);
        canvas.drawText("Level Reached: " + level, centerX, statsY + 60, textPaint);
        
        // Play Time
        long minutes = playTime / 60;
        long seconds = playTime % 60;
        String timeText = String.format("Time Played: %02d:%02d", minutes, seconds);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(timeText, centerX, statsY + 120, textPaint);
    }
    
    private void drawButtons(Canvas canvas) {
        // Draw buttons using bitmaps từ res/
        if (replayButtonBitmap != null) {
            canvas.drawBitmap(replayButtonBitmap, null, replayButton, null);
        } else {
            drawButton(canvas, replayButton, "🔄 REPLAY", Color.GREEN);
        }
        
        if (menuButtonBitmap != null) {
            canvas.drawBitmap(menuButtonBitmap, null, homeButton, null);
        } else {
            drawButton(canvas, homeButton, "🏠 HOME", Color.BLUE);
        }
        
        if (menuButtonBitmap != null) {
            canvas.drawBitmap(menuButtonBitmap, null, menuButton, null);
        } else {
            drawButton(canvas, menuButton, "📋 MENU", Color.MAGENTA);
        }
    }
    
    private void drawButton(Canvas canvas, Rect buttonRect, String text, int highlightColor) {
        // Draw button background
        buttonPaint.setColor(Color.argb(200, 50, 50, 50));
        canvas.drawRoundRect(buttonRect.left, buttonRect.top, buttonRect.right, buttonRect.bottom, 10, 10, buttonPaint);
        
        // Draw button border
        Paint borderPaint = new Paint();
        borderPaint.setColor(highlightColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setAntiAlias(true);
        canvas.drawRoundRect(buttonRect.left, buttonRect.top, buttonRect.right, buttonRect.bottom, 10, 10, borderPaint);
        
        // Draw button text
        int textX = buttonRect.centerX();
        int textY = buttonRect.centerY() + 15; // Offset for text centering
        buttonTextPaint.setColor(Color.WHITE);
        canvas.drawText(text, textX, textY, buttonTextPaint);
    }
    
    // Handle touch events
    public String handleTouch(float x, float y) {
        if (replayButton.contains((int)x, (int)y)) {
            return "REPLAY";
        } else if (homeButton.contains((int)x, (int)y)) {
            return "HOME";
        } else if (menuButton.contains((int)x, (int)y)) {
            return "MENU";
        }
        return null;
    }
    
    // Setters for game over images
    public void setGameOverBitmap(Bitmap gameOverBitmap) {
        this.gameOverBitmap = gameOverBitmap;
    }
    
    public void setYouLoseBitmap(Bitmap youLoseBitmap) {
        this.youLoseBitmap = youLoseBitmap;
    }
    
    public void setUseGameOverImage(boolean useGameOverImage) {
        this.useGameOverImage = useGameOverImage;
    }
    
    public void cleanup() {
        // Recycle bitmaps to free memory
        if (gameOverBitmap != null && !gameOverBitmap.isRecycled()) {
            gameOverBitmap.recycle();
        }
        if (youLoseBitmap != null && !youLoseBitmap.isRecycled()) {
            youLoseBitmap.recycle();
        }
        if (replayButtonBitmap != null && !replayButtonBitmap.isRecycled()) {
            replayButtonBitmap.recycle();
        }
        if (menuButtonBitmap != null && !menuButtonBitmap.isRecycled()) {
            menuButtonBitmap.recycle();
        }
        if (settingButtonBitmap != null && !settingButtonBitmap.isRecycled()) {
            settingButtonBitmap.recycle();
        }
    }
}