package com.example.templerunclone.entities;

import android.graphics.Bitmap;
import java.util.Random;

/**
 * Basic enemy type - fast but weak
 */
public class EnemyBasic extends Enemy {
    
    public EnemyBasic(float x, float y, Bitmap bitmap) {
        // Random size between 60-100
        super(x, y, getRandomSize(60, 100), getRandomSize(60, 100), bitmap, 200f, 1);
        this.scoreValue = 10;
    }
    
    private static float getRandomSize(int min, int max) {
        Random random = new Random();
        return min + random.nextFloat() * (max - min);
    }
}