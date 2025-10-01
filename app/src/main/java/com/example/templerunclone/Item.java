package com.example.templerunclone;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Item {
    public enum Type {
        HEALTH, SHIELD, COIN, SPEED, WEAPON
    }

    private final Bitmap bitmap;
    private final int x;
    private int y;
    private final int speed;
    private final Type type;
    private boolean active = true;

    public Item(Bitmap bitmap, int x, int y, int speed, Type type) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.type = type;
    }

    public void update() {
        y += speed;
        if (y > 2000) active = false;
    }

    public void draw(Canvas canvas, Paint paint) {
        if (active) canvas.drawBitmap(bitmap, x, y, paint);
    }

    public Rect getRect() {
        return new Rect(x, y, x + bitmap.getWidth(), y + bitmap.getHeight());
    }

    public Type getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    public void collect() {
        active = false;
    }
}
