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
        if (backgroundBitmap != null && !backgroundBitmap.isRecycled()) {
            canvas.drawBitmap(backgroundBitmap, 0, bgY1, paint);
            canvas.drawBitmap(backgroundBitmap, 0, bgY2, paint);
        } else {
            // Draw a visible fallback background instead of dark blue
            if (paint == null) {
                paint = new Paint();
            }
            
            // Create a bright, obvious fallback pattern
            paint.setColor(android.graphics.Color.MAGENTA); // Bright magenta base
            canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
            
            // Add white stripes to make it obvious this is fallback
            paint.setColor(android.graphics.Color.WHITE);
            paint.setStrokeWidth(10);
            for (int i = 0; i < screenWidth; i += 50) {
                canvas.drawLine(i, 0, i, screenHeight, paint);
            }
            
            android.util.Log.w("BackgroundRenderer", "Drawing BRIGHT FALLBACK background - no bitmap available");
        }
    }
    
    public void setBackground(Bitmap backgroundBitmap) {
        this.backgroundBitmap = backgroundBitmap;
        if (backgroundBitmap != null && !backgroundBitmap.isRecycled()) {
            android.util.Log.d("BackgroundRenderer", "Background set: " + backgroundBitmap.getWidth() + "x" + backgroundBitmap.getHeight());
            // Reset scroll positions to ensure bitmap is visible immediately after swap
            this.bgY1 = 0f;
            this.bgY2 = -screenHeight;
        } else {
            android.util.Log.w("BackgroundRenderer", "Background set to null or recycled bitmap");
        }
    }
}