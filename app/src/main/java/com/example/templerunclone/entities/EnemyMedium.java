package com.example.templerunclone.entities;

import android.graphics.Bitmap;
import java.util.Random;

/**
 * Medium enemy type - balanced speed and health
 */
public class EnemyMedium extends Enemy {
    
    public EnemyMedium(float x, float y, Bitmap bitmap) {
        // Random size between 80-120
        super(x, y, getRandomSize(80, 120), getRandomSize(80, 120), bitmap, 150f, 2);
        this.scoreValue = 20;
    }
    
    private static float getRandomSize(int min, int max) {
        Random random = new Random();
        return min + random.nextFloat() * (max - min);
    }
}