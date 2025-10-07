package com.example.templerunclone.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * PowerUp entity
 */
public class PowerUp extends GameObject {
    public enum PowerUpType {
        HEALTH,         // Health restore
        SHIELD,         // Basic shield (blue)
        RAPID_FIRE,     // Faster shooting
        MULTI_SHOT,     // Multiple bullets
        LASER_BEAM,     // Penetrating laser bullets
        ENERGY_SHIELD,  // Energy shield with yellow highlight
        FORCE_FIELD     // Force field with purple highlight
    }
    
    private Bitmap bitmap;
    private PowerUpType type;
    private float speed;
    private long duration; // How long the effect lasts
    
    public PowerUp(float x, float y, Bitmap bitmap, PowerUpType type, long duration) {
        super(x, y, 40, 40);
        this.bitmap = bitmap;
        this.type = type;
        this.speed = 100f;
        this.duration = duration;
    }
    
    @Override
    public void update(float deltaTime) {
        y += speed * deltaTime / 1000f;
        
        // Deactivate if off screen
        if (y > 2000) {
            active = false;
        }
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, x, y, paint);
        }
    }
    
    public PowerUpType getType() {
        return type;
    }
    
    public long getDuration() {
        return duration;
    }
}