package com.example.templerunclone.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.example.templerunclone.engine.GameState;

/**
 * Player entity with health and movement
 */
public class Player extends GameObject {
    private Bitmap bitmap;
    private int health;
    private int maxHealth;
    private float targetX, targetY;
    private float speed;
    private boolean invincible = false;
    private long invincibleStartTime = 0;
    private long invincibleDuration = 800; // ms
    
    public Player(float x, float y, float width, float height, Bitmap bitmap) {
        super(x, y, width, height);
        this.bitmap = bitmap;
        this.maxHealth = 3;
        this.health = maxHealth;
        this.speed = 25f;
        this.targetX = x;
        this.targetY = y;
    }
    
    @Override
    public void update(float deltaTime) {
        // Smooth movement towards target
        float dx = targetX - x;
        float dy = targetY - y;
        
        if (Math.abs(dx) > 1) {
            x += dx * speed * deltaTime / 1000f;
        } else {
            x = targetX;
        }
        
        if (Math.abs(dy) > 1) {
            y += dy * speed * deltaTime / 1000f;
        } else {
            y = targetY;
        }
        
        // Update invincibility
        if (invincible && System.currentTimeMillis() - invincibleStartTime > invincibleDuration) {
            invincible = false;
        }
    }
    
    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (bitmap != null) {
            // Flash effect during invincibility
            if (invincible) {
                long elapsed = System.currentTimeMillis() - invincibleStartTime;
                if ((elapsed / 100) % 2 == 0) { // Flash every 100ms
                    canvas.drawBitmap(bitmap, x, y, paint);
                }
            } else {
                canvas.drawBitmap(bitmap, x, y, paint);
            }
        }
    }
    
    public void drawWithShield(Canvas canvas, Paint paint, GameState gameState) {
        // Draw player bitmap first - giữ nguyên độ sáng
        if (bitmap != null) {
            // Flash effect during invincibility
            if (invincible) {
                long elapsed = System.currentTimeMillis() - invincibleStartTime;
                if ((elapsed / 100) % 2 == 0) { // Flash every 100ms
                    canvas.drawBitmap(bitmap, x, y, paint);
                }
            } else {
                canvas.drawBitmap(bitmap, x, y, paint);
            }
        }
        
        // Draw shield effects - sử dụng paint riêng để không ảnh hưởng player
        Paint shieldPaint = new Paint();
        shieldPaint.setAntiAlias(true);
        
        if (gameState.isShieldActive()) {
            // Blue shield
            shieldPaint.setColor(Color.argb(100, 0, 0, 255));
            canvas.drawCircle(x + width/2, y + height/2, width/2 + 10, shieldPaint);
        }
        
        if (gameState.isEnergyShieldActive()) {
            // Yellow/Gold energy shield
            shieldPaint.setColor(Color.argb(120, 255, 215, 0));
            canvas.drawCircle(x + width/2, y + height/2, width/2 + 15, shieldPaint);
        }
        
        if (gameState.isForceFieldActive()) {
            // Purple force field
            shieldPaint.setColor(Color.argb(100, 148, 0, 211));
            canvas.drawCircle(x + width/2, y + height/2, width/2 + 20, shieldPaint);
        }
    }
    
    public void moveTo(float x, float y) {
        this.targetX = x;
        this.targetY = y;
    }
    
    public void takeDamage(int damage, GameState gameState) {
        // Check for shield protection
        if (gameState != null && (gameState.isShieldActive() || 
                                  gameState.isEnergyShieldActive() || 
                                  gameState.isForceFieldActive())) {
            return; // No damage taken while shields are active
        }
        
        if (!invincible) {
            health -= damage;
            if (health < 0) health = 0;
            
            // Start invincibility period
            invincible = true;
            invincibleStartTime = System.currentTimeMillis();
        }
    }
    
    // Legacy method for compatibility
    public void takeDamage(int damage) {
        takeDamage(damage, null);
    }
    
    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }
    
    public boolean isDead() {
        return health <= 0;
    }
    
    // Getters
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isInvincible() { return invincible; }
    public Bitmap getBitmap() { return bitmap; }
    
    // Setters
    public void setBitmap(Bitmap bitmap) { this.bitmap = bitmap; }
    public void setSpeed(float speed) { this.speed = speed; }
}