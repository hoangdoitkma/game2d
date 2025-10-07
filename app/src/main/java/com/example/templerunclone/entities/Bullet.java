package com.example.templerunclone.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;

/**
 * Bullet entity
 */
public class Bullet extends GameObject {
    private float speed;
    protected int damage; // Changed to protected for subclass access
    private Bitmap bitmap; // Add bitmap support
    
    public Bullet(float x, float y, float speed) {
        super(x, y, 8, 16); // Standard bullet size
        this.speed = speed;
        this.damage = 1;
        this.bitmap = null;
    }
    
    @Override
    public void update(float deltaTime) {
        y -= speed * deltaTime / 1000f;
        
        // Deactivate if off screen
        if (y < -height) {
            active = false;
        }
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (bitmap != null && !bitmap.isRecycled()) {
            // Draw bitmap if available
            canvas.drawBitmap(bitmap, x, y, paint);
        } else {
            // Fallback to colored rectangle
            paint.setColor(Color.YELLOW);
            canvas.drawRect(x, y, x + width, y + height, paint);
        }
    }
    
    public int getDamage() {
        return damage;
    }
    
    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        // Optionally resize bullet to match bitmap
        if (bitmap != null) {
            this.width = bitmap.getWidth();
            this.height = bitmap.getHeight();
        }
    }
}