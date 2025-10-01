package com.example.templerunclone.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Explosion effect entity
 */
public class Explosion extends GameObject {
    private Bitmap bitmap;
    private long startTime;
    private long duration;
    
    public Explosion(float x, float y, Bitmap bitmap, long duration) {
        super(x, y, bitmap.getWidth(), bitmap.getHeight());
        this.bitmap = bitmap;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }
    
    @Override
    public void update(float deltaTime) {
        if (System.currentTimeMillis() - startTime > duration) {
            active = false;
        }
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (bitmap != null) {
            // Tạo hiệu ứng nổ màu vàng
            Paint explosionPaint = new Paint();
            explosionPaint.setColorFilter(new android.graphics.PorterDuffColorFilter(
                Color.YELLOW, android.graphics.PorterDuff.Mode.MULTIPLY));
            canvas.drawBitmap(bitmap, x, y, explosionPaint);
        } else {
            // Vẽ vòng tròn vàng nếu không có bitmap
            paint.setColor(Color.YELLOW);
            canvas.drawCircle(x + width/2, y + height/2, width/3, paint);
        }
    }
    
    public boolean isFinished() {
        return !active;
    }
}