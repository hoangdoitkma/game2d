package com.example.templerunclone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Simple Enemy class with basic movement, drawing, hp and explosion state.
 * takeDamage now returns boolean: true if this hit killed the enemy (so caller can award score immediately).
 */
public class Enemy {
    private Bitmap bitmap;
    private Bitmap explosionBitmap;
    private float x, y;
    private int screenW, screenH;
    private float vx = 0f, vy = 6f;
    private int width, height;
    private int hp = 1;
    private boolean exploding = false;
    private long explodeStart = 0;
    private final long explodeDuration = 400; // ms
    private boolean finished = false;

    public Enemy(Context ctx, Bitmap bmp, int startX, int startY, int screenW, int screenH) {
        this.bitmap = bmp;
        this.x = startX;
        this.y = startY;
        this.screenW = screenW;
        this.screenH = screenH;
        this.width = (bitmap != null) ? bitmap.getWidth() : 48;
        this.height = (bitmap != null) ? bitmap.getHeight() : 48;
        // random horizontal drift
        this.vx = (float)((Math.random() - 0.5) * 4.0);
    }

    public void setExplosionBitmap(Bitmap bmp) { this.explosionBitmap = bmp; }

    public void update() {
        if (finished) return;
        if (exploding) {
            if (System.currentTimeMillis() - explodeStart > explodeDuration) {
                finished = true;
            }
            return;
        }
        x += vx;
        y += vy;
        // bounce horizontally inside screen
        if (x < 0) { x = 0; vx = -vx; }
        if (x > screenW - width) { x = screenW - width; vx = -vx; }
    }

    public void draw(Canvas c) {
        if (finished) return;
        if (exploding && explosionBitmap != null) {
            c.drawBitmap(explosionBitmap, x, y, null);
        } else if (bitmap != null) {
            c.drawBitmap(bitmap, x, y, null);
        } else {
            Paint p = new Paint();
            p.setColor(0xFFFF0000);
            c.drawRect(x, y, x + width, y + height, p);
        }
    }

    /**
     * Apply damage. Returns true if this call caused the enemy to die (so caller can award score).
     */
    public boolean takeDamage(int dmg) {
        if (exploding || finished) return false;
        hp -= dmg;
        if (hp <= 0) {
            explode();
            return true; // died now
        }
        return false;
    }

    public void explode() {
        if (exploding || finished) return;
        exploding = true;
        explodeStart = System.currentTimeMillis();
    }

    public boolean isExploding() { return exploding; }
    public boolean isFinished() { return finished; }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}