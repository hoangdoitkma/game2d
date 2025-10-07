package com.example.templerunclone.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Simple loading screen to show while resources are loading
 */
public class LoadingScreen {
    private Paint textPaint;
    private Paint backgroundPaint;
    private Paint progressPaint;
    private int screenWidth, screenHeight;
    private float progress = 0f;
    private long startTime;
    
    public LoadingScreen(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.startTime = System.currentTimeMillis();
        
        // Initialize paints
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.argb(200, 0, 0, 0)); // Semi-transparent black
        
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        progressPaint = new Paint();
        progressPaint.setColor(Color.GREEN);
        progressPaint.setAntiAlias(true);
    }
    
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
    }
    
    public void draw(Canvas canvas) {
        // Draw background overlay
        canvas.drawRect(0, 0, screenWidth, screenHeight, backgroundPaint);
        
        // Draw loading text
        canvas.drawText("Loading Game...", screenWidth / 2f, screenHeight / 2f - 50, textPaint);
        
        // Draw progress bar
        float barWidth = screenWidth * 0.6f;
        float barHeight = 20f;
        float barX = (screenWidth - barWidth) / 2f;
        float barY = screenHeight / 2f + 50;
        
        // Progress bar background
        progressPaint.setColor(Color.GRAY);
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, progressPaint);
        
        // Progress bar fill
        progressPaint.setColor(Color.GREEN);
        canvas.drawRect(barX, barY, barX + (barWidth * progress), barY + barHeight, progressPaint);
        
        // Draw percentage
        textPaint.setTextSize(40);
        String percentText = (int)(progress * 100) + "%";
        canvas.drawText(percentText, screenWidth / 2f, barY + barHeight + 50, textPaint);
        
        // Auto-animate progress if not set
        if (progress == 0f) {
            long elapsed = System.currentTimeMillis() - startTime;
            float autoProgress = Math.min(1f, elapsed / 3000f); // 3 second animation
            setProgress(autoProgress);
        }
    }
}