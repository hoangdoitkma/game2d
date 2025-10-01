package com.example.templerunclone.entities.bullets;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.example.templerunclone.entities.Bullet;

/**
 * Laser Bullet - penetrates through enemies
 */
public class LaserBullet extends Bullet {
    private boolean hasPenetrated = false;
    private int penetrationCount = 0;
    private final int maxPenetration = 3;
    
    public LaserBullet(float x, float y, float speed) {
        super(x, y, speed);
        this.width = 6;
        this.height = 20;
        this.damage = 1;
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.CYAN);
        canvas.drawRect(x, y, x + width, y + height, paint);
        
        // Add laser glow effect
        paint.setColor(Color.argb(100, 0, 255, 255));
        canvas.drawRect(x - 2, y, x + width + 2, y + height, paint);
    }
    
    public boolean canPenetrate() {
        return penetrationCount < maxPenetration;
    }
    
    public void penetrate() {
        penetrationCount++;
        hasPenetrated = true;
    }
    
    public boolean hasPenetrated() {
        return hasPenetrated;
    }
}