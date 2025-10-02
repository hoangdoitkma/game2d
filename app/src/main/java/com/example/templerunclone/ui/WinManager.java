package com.example.templerunclone.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Win Manager để xử lý UI khi game thắng
 */
public class WinManager {
    private Paint textPaint;
    private Paint buttonPaint;
    private Paint buttonTextPaint;
    private Paint backgroundPaint;
    
    private Bitmap congratulationsBitmap;
    
    // Button bitmaps từ res/
    private Bitmap replayButtonBitmap;
    private Bitmap menuButtonBitmap;
    private Bitmap highScoresButtonBitmap;
    
    // Button rectangles
    private Rect replayButton;
    private Rect homeButton;
    private Rect highScoresButton;
    
    private int screenWidth, screenHeight;
    
    public WinManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        initializePaints();
        setupButtons();
    }
    
    private void initializePaints() {
        // Background paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.argb(180, 0, 0, 50)); // Semi-transparent dark green
        
        // Text paint
        textPaint = new Paint();
        textPaint.setColor(Color.rgb(255, 215, 0)); // Gold color
        textPaint.setTextSize(80);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);
        
        // Button paint
        buttonPaint = new Paint();
        buttonPaint.setColor(Color.argb(200, 255, 215, 0)); // Gold background
        buttonPaint.setAntiAlias(true);
        
        // Button text paint
        buttonTextPaint = new Paint();
        buttonTextPaint.setColor(Color.BLACK);
        buttonTextPaint.setTextSize(50);
        buttonTextPaint.setAntiAlias(true);
        buttonTextPaint.setFakeBoldText(true);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
    }
    
    private void setupButtons() {
        int buttonWidth = 200;
        int buttonHeight = 80;
        int buttonSpacing = 50;
        
        int totalWidth = 3 * buttonWidth + 2 * buttonSpacing;
        int startX = (screenWidth - totalWidth) / 2;
        int buttonY = screenHeight / 2 + 200;
        
        // Replay button
        replayButton = new Rect(startX, buttonY, startX + buttonWidth, buttonY + buttonHeight);
        
        // Home button
        homeButton = new Rect(startX + buttonWidth + buttonSpacing, buttonY, 
                             startX + 2 * buttonWidth + buttonSpacing, buttonY + buttonHeight);
        
        // High Scores button
        highScoresButton = new Rect(startX + 2 * (buttonWidth + buttonSpacing), buttonY, 
                                   startX + 3 * buttonWidth + 2 * buttonSpacing, buttonY + buttonHeight);
    }
    
    public void setButtonBitmaps(Bitmap replayBitmap, Bitmap menuBitmap, Bitmap highScoresBitmap) {
        this.replayButtonBitmap = replayBitmap;
        this.menuButtonBitmap = menuBitmap;
        this.highScoresButtonBitmap = highScoresBitmap;
    }
    
    public void setCongratulationsBitmap(Bitmap congratulationsBitmap) {
        this.congratulationsBitmap = congratulationsBitmap;
        if (congratulationsBitmap != null) {
            android.util.Log.d("WinManager", "Congratulations bitmap set successfully, size: " + 
                congratulationsBitmap.getWidth() + "x" + congratulationsBitmap.getHeight());
        } else {
            android.util.Log.w("WinManager", "Congratulations bitmap is null, will use text fallback");
        }
    }
    
    public void draw(Canvas canvas, int score, int level) {
        // Draw semi-transparent background
        canvas.drawRect(0, 0, screenWidth, screenHeight, backgroundPaint);

        // Draw congratulations image only
        if (congratulationsBitmap != null && !congratulationsBitmap.isRecycled()) {
            float imageX = (screenWidth - congratulationsBitmap.getWidth()) / 2f;
            float imageY = screenHeight / 2f - 200;
            canvas.drawBitmap(congratulationsBitmap, imageX, imageY, null);
        }
        
        // Draw "YOU WIN!" text
        textPaint.setColor(Color.GREEN);
        textPaint.setTextSize(60);
        String winText = "YOU WIN!";
        float winTextWidth = textPaint.measureText(winText);
        float winTextX = (screenWidth - winTextWidth) / 2f;
        float winTextY = screenHeight / 2f - 20;
        canvas.drawText(winText, winTextX, winTextY, textPaint);
        
        // Draw final score
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50);
        String scoreText = "Final Score: " + score;
        String levelText = "Level Reached: " + level;
        
        float scoreTextWidth = textPaint.measureText(scoreText);
        float scoreTextX = (screenWidth - scoreTextWidth) / 2f;
        canvas.drawText(scoreText, scoreTextX, screenHeight / 2f + 40, textPaint);
        
        float levelTextWidth = textPaint.measureText(levelText);
        float levelTextX = (screenWidth - levelTextWidth) / 2f;
        canvas.drawText(levelText, levelTextX, screenHeight / 2f + 100, textPaint);
        
        // Draw buttons
        drawButtons(canvas);
    }
    
    private void drawButtons(Canvas canvas) {
        // Draw replay button
        if (replayButtonBitmap != null) {
            canvas.drawBitmap(replayButtonBitmap, null, replayButton, null);
        } else {
            canvas.drawRect(replayButton, buttonPaint);
            canvas.drawText("REPLAY", replayButton.centerX(), 
                           replayButton.centerY() + buttonTextPaint.getTextSize() / 3, buttonTextPaint);
        }
        
        // Draw home/menu button
        if (menuButtonBitmap != null) {
            canvas.drawBitmap(menuButtonBitmap, null, homeButton, null);
        } else {
            canvas.drawRect(homeButton, buttonPaint);
            canvas.drawText("MENU", homeButton.centerX(), 
                           homeButton.centerY() + buttonTextPaint.getTextSize() / 3, buttonTextPaint);
        }
        
        // Draw high scores button
        if (highScoresButtonBitmap != null) {
            canvas.drawBitmap(highScoresButtonBitmap, null, highScoresButton, null);
        } else {
            canvas.drawRect(highScoresButton, buttonPaint);
            canvas.drawText("HIGH SCORES", highScoresButton.centerX(), 
                           highScoresButton.centerY() + buttonTextPaint.getTextSize() / 3, buttonTextPaint);
        }
    }
    
    public String handleTouch(float x, float y) {
        if (replayButton.contains((int)x, (int)y)) {
            return "REPLAY";
        } else if (homeButton.contains((int)x, (int)y)) {
            return "MENU";
        } else if (highScoresButton.contains((int)x, (int)y)) {
            return "HIGH_SCORES";
        }
        return null;
    }
    
    public void cleanup() {
        if (congratulationsBitmap != null && !congratulationsBitmap.isRecycled()) {
            congratulationsBitmap.recycle();
        }
        if (replayButtonBitmap != null && !replayButtonBitmap.isRecycled()) {
            replayButtonBitmap.recycle();
        }
        if (menuButtonBitmap != null && !menuButtonBitmap.isRecycled()) {
            menuButtonBitmap.recycle();
        }
        if (highScoresButtonBitmap != null && !highScoresButtonBitmap.isRecycled()) {
            highScoresButtonBitmap.recycle();
        }
    }
}