package com.example.templerunclone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Player {
    private Bitmap bitmap;
    private int x, y;
    private int width, height;

    public Player(Context context, int startX, int startY) {
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ship);
        width = bitmap.getWidth() / 8;   // thu nhỏ 8 lần
        height = bitmap.getHeight() / 8;
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        this.x = startX - width / 2;
        this.y = startY - height / 2;
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(bitmap, x, y, paint);
    }

    public Rect getRect() {
        return new Rect((int)x, (int)y, (int)(x + width), (int)(y + height));
        // Assuming player has x, y, width, height properties
    }

    // Getter - Setter
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
