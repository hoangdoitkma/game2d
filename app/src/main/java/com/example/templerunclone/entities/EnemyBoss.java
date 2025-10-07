package com.example.templerunclone.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Big boss for Level 3: moves down to 1/3 screen height, then hovers and spawns minions.
 */
public class EnemyBoss extends Enemy {
    private int screenHeight;
    private float targetY;
    private long lastSpawnTime;
    private long spawnCooldownMs = 1200; // spawn minions every 1.2s

    public interface MinionSpawner {
        void spawnMinion(float x, float y);
    }

    private MinionSpawner spawner;

    public EnemyBoss(float x, float y, Bitmap bitmap, int screenWidth, int screenHeight, MinionSpawner spawner) {
        // Pass computed size directly to super to keep it the first constructor statement
        super(
            x,
            y,
            (bitmap != null ? bitmap.getWidth() : 200f),
            (bitmap != null ? bitmap.getHeight() : 200f),
            bitmap,
            140f,
            30
        ); // Large size, high health
        this.scoreValue = 200;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.targetY = screenHeight / 3f; // stop at 1/3 of the screen
        this.spawner = spawner;
        this.lastSpawnTime = System.currentTimeMillis();
    }

    @Override
    public void update(float deltaTime) {
        float dt = deltaTime / 1000f;

        // Move down until reaching targetY, then hover (small bobbing)
        if (y < targetY) {
            y += speed * dt;
            if (y > targetY) y = targetY;
        } else {
            // subtle horizontal sway while hovering
            x += Math.sin(System.currentTimeMillis() / 400.0) * 0.8f;
        }

        // Keep boss on screen horizontally
        if (x < 0) x = 0;
        if (x > screenWidth - width) x = screenWidth - width;

        // Spawn minions periodically once boss is in position
        if (y >= targetY && spawner != null) {
            long now = System.currentTimeMillis();
            if (now - lastSpawnTime >= spawnCooldownMs) {
                // spawn 2 minions around the boss
                float midX = x + width / 2f;
                spawner.spawnMinion(midX - 60, y + height - 10);
                spawner.spawnMinion(midX + 60, y + height - 10);
                lastSpawnTime = now;
            }
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        // Draw the boss
        super.draw(canvas, paint);

        // Optionally draw a health bar on top of boss
        float barWidth = width;
        float barHeight = 10f;
        float ratio = Math.max(0f, Math.min(1f, health / (float) maxHealth));
        int bgColor = 0xAA000000; // semi-transparent black
        int hpColor = 0xFFFF4444; // red

        paint.setColor(bgColor);
        canvas.drawRect(x, y - 14, x + barWidth, y - 14 + barHeight, paint);
        paint.setColor(hpColor);
        canvas.drawRect(x, y - 14, x + barWidth * ratio, y - 14 + barHeight, paint);
    }
}
