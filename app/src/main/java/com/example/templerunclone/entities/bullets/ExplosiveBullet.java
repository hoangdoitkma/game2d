package com.example.templerunclone.entities.bullets;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.example.templerunclone.entities.Bullet;

/**
 * Explosive Bullet - creates explosion on impact
 */
public class ExplosiveBullet extends Bullet {
    private float explosionRadius = 60f;
    
    public ExplosiveBullet(float x, float y, float speed) {
        super(x, y, speed);
        this.width = 10;
        this.height = 18;
        this.damage = 2;
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        // Draw main bullet body
        paint.setColor(Color.RED);
        canvas.drawRect(x, y, x + width, y + height, paint);
        
        // Draw explosive tip
        paint.setColor(Color.YELLOW);
        canvas.drawRect(x + 2, y, x + width - 2, y + 4, paint);
        
        // Add glow effect
        paint.setColor(Color.argb(80, 255, 165, 0));
        canvas.drawRect(x - 1, y, x + width + 1, y + height, paint);
    }
    
    public float getExplosionRadius() {
        return explosionRadius;
    }
}