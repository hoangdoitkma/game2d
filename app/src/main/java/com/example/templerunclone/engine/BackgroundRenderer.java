package com.example.templerunclone.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Handles scrolling background rendering
 */
public class BackgroundRenderer {
    private Bitmap backgroundBitmap;
    private float bgY1, bgY2;
    private float bgSpeed = 12f;
    private int screenWidth, screenHeight;
    
    public BackgroundRenderer(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.bgY1 = 0f;
        this.bgY2 = -screenHeight;
    }
    
    public void update(float deltaTime, float speedMultiplier) {
        float actualSpeed = bgSpeed * speedMultiplier * deltaTime / 1000f;
        
        bgY1 += actualSpeed;
        bgY2 += actualSpeed;
        
        // Reset positions when off screen
        if (bgY1 >= screenHeight) {
            bgY1 = bgY2 - screenHeight;
        }
        if (bgY2 >= screenHeight) {
            bgY2 = bgY1 - screenHeight;
        }
    }
    
    public void draw(Canvas canvas, Paint paint) {
        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, 0, bgY1, paint);
            canvas.drawBitmap(backgroundBitmap, 0, bgY2, paint);
        }
    }
    
    public void setBackground(Bitmap backgroundBitmap) {
        this.backgroundBitmap = backgroundBitmap;
    }
}