package com.example.templerunclone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Very simple bullet that goes upward. Provides getRect() for collision detection.
 */
public class Bullet {
    private float x, y;
    private final int speed = 18;
    private final int size = 8;
    private final int screenH;

    public Bullet(Context ctx, int startX, int startY, int screenH) {
        this.x = startX - size / 2f;
        this.y = startY - size;
        this.screenH = screenH;
    }

    public void update() {
        y -= speed;
    }

    public void draw(Canvas c, Paint p) {
        c.drawRect(x, y, x + size, y + size, p);
    }

    public boolean isOutOfScreen(int screenHeight) {
        return y + size < 0 || y > screenHeight;
    }

    public Rect getRect() {
        return new Rect((int)x, (int)y, (int)(x + size), (int)(y + size));
    }
}