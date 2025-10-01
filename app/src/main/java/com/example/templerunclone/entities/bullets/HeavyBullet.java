package com.example.templerunclone.entities.bullets;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.example.templerunclone.entities.Bullet;

/**
 * Heavy Bullet - slower but more damage
 */
public class HeavyBullet extends Bullet {
    
    public HeavyBullet(float x, float y, float speed) {
        super(x, y, speed * 0.7f); // 30% slower
        this.width = 12;
        this.height = 24;
        this.damage = 3;
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        // Draw main bullet body
        paint.setColor(Color.rgb(255, 215, 0)); // Gold color
        canvas.drawRect(x, y, x + width, y + height, paint);
        
        // Draw dark outline
        paint.setColor(Color.rgb(139, 69, 19)); // Brown outline
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(x, y, x + width, y + height, paint);
        paint.setStyle(Paint.Style.FILL);
        
        // Add metallic shine effect
        paint.setColor(Color.WHITE);
        canvas.drawRect(x + 2, y + 2, x + 4, y + height - 2, paint);
    }
}