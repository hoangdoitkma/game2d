package com.example.templerunclone.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.Random;

/**
 * Base Enemy class
 */
public abstract class Enemy extends GameObject {
    protected Bitmap bitmap;
    protected float speed;
    protected int health;
    protected int maxHealth;
    protected int scoreValue;
    protected boolean hasWarned = false;
    
    // Random movement properties
    protected float velocityX = 0;
    protected float velocityY;
    protected int screenWidth = 0;
    protected Random random = new Random();
    
    public Enemy(float x, float y, float width, float height, Bitmap bitmap, float speed, int health) {
        super(x, y, width, height);
        this.bitmap = bitmap;
        this.speed = speed;
        this.health = health;
        this.maxHealth = health;
        this.scoreValue = 10;
        this.velocityY = speed;
        
        // Random horizontal movement
        this.velocityX = (random.nextFloat() - 0.5f) * speed * 0.3f; // 30% of vertical speed
    }
    
    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }
    
    @Override
    public void update(float deltaTime) {
        float dt = deltaTime / 1000f;
        
        // Move with velocity
        x += velocityX * dt;
        y += velocityY * dt;
        
        // Bounce off screen edges horizontally
        if (screenWidth > 0) {
            if (x <= 0 || x >= screenWidth - width) {
                velocityX = -velocityX; // Reverse horizontal direction
                x = Math.max(0, Math.min(x, screenWidth - width)); // Keep in bounds
            }
        }
        
        // Deactivate if off screen
        if (y > 2000) { // Screen height buffer
            active = false;
        }
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, x, y, paint);
        }
    }
    
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            active = false;
        }
    }
    
    public boolean isDead() {
        return health <= 0;
    }
    
    public boolean shouldWarn(int screenHeight) {
        return !hasWarned && y > screenHeight * 0.7f;
    }
    
    public void setWarned() {
        hasWarned = true;
    }
    
    // Getters
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getScoreValue() { return scoreValue; }
    public Bitmap getBitmap() { return bitmap; }
    public float getSpeed() { return speed; }
    
    // Setters
    public void setBitmap(Bitmap bitmap) { this.bitmap = bitmap; }
    public void setSpeed(float speed) { 
        this.speed = speed; 
        this.velocityY = speed;
    }
    public void setHealth(int health) { 
        this.health = health; 
        this.maxHealth = health;
    }
}