package com.example.templerunclone.managers;

/**
 * Handles input from touch events
 */
public class InputManager {
    private boolean isShooting = false;
    private float touchX = 0f;
    private float touchY = 0f;
    
    public void handleTouch(float x, float y, boolean isDown) {
        touchX = x;
        touchY = y;
        isShooting = isDown;
    }
    
    public boolean isShooting() {
        return isShooting;
    }
    
    public float getTouchX() {
        return touchX;
    }
    
    public float getTouchY() {
        return touchY;
    }
}