package com.example.templerunclone.entities;

import android.graphics.Bitmap;

/**
 * Enemy with zigzag/sinusoidal horizontal movement, used for Level 2.
 */
public class EnemyZigZag extends Enemy {
    private float time;
    private float amplitude; // horizontal swing range
    private float frequency; // oscillation speed
    private float originX;

    public EnemyZigZag(float x, float y, Bitmap bitmap) {
        // Slightly smaller than basic to keep count higher without clutter
        super(x, y, 80f, 80f, bitmap, 220f, 2);
        this.scoreValue = 15;
        this.time = 0f;
        this.amplitude = 120f;  // pixels
        this.frequency = 2.0f;  // oscillations per second
        this.originX = x;
    }

    @Override
    public void update(float deltaTime) {
        float dt = deltaTime / 1000f;
        time += dt;

        // Vertical descent
        y += speed * dt;

        // Sinusoidal horizontal movement centered around originX
        float omega = (float) (2 * Math.PI * frequency);
        x = originX + (float) Math.sin(time * omega) * amplitude;

        // Keep inside screen bounds if available
        if (screenWidth > 0) {
            if (x < 0) { x = 0; originX = Math.min(originX, x); }
            if (x > screenWidth - width) { x = screenWidth - width; originX = Math.max(originX, x); }
        }

        // Deactivate if far off screen
        if (y > 2000) {
            active = false;
        }
    }
}
