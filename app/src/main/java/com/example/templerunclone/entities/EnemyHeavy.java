package com.example.templerunclone.entities;

import android.graphics.Bitmap;
import java.util.Random;

/**
 * Heavy enemy type - slow but strong
 */
public class EnemyHeavy extends Enemy {
    
    public EnemyHeavy(float x, float y, Bitmap bitmap) {
        // Random size between 100-150
        super(x, y, getRandomSize(100, 150), getRandomSize(100, 150), bitmap, 100f, 3);
        this.scoreValue = 30;
    }
    
    private static float getRandomSize(int min, int max) {
        Random random = new Random();
        return min + random.nextFloat() * (max - min);
    }
}