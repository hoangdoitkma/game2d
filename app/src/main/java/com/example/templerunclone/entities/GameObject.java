package com.example.templerunclone.entities;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Base class for all game objects
 */
public abstract class GameObject {
    protected float x, y;
    protected float width, height;
    protected boolean active = true;
    
    public GameObject(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public abstract void update(float deltaTime);
    public abstract void draw(Canvas canvas, Paint paint);
    
    public Rect getRect() {
        return new Rect((int)x, (int)y, (int)(x + width), (int)(y + height));
    }
    
    public boolean intersects(GameObject other) {
        return Rect.intersects(this.getRect(), other.getRect());
    }
    
    // Getters and setters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public boolean isActive() { return active; }
    
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setActive(boolean active) { this.active = active; }
}