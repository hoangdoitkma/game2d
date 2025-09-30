package com.example.templerunclone; // Thay đổi thành package của bạn

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * GameView updated to prefer resources from PreloadManager (started in MainMenu).
 * Collision handling implemented: bullets vs enemies and player vs enemies.
 */
public class GameView extends SurfaceView implements Runnable {

    private static final String TAG = "GameView";

    private Thread gameThread;
    private volatile boolean isPlaying = false;
    private final SurfaceHolder holder;
    private Paint paint;

    // Background / original bitmaps (may come from PreloadManager)
    private Bitmap backgroundOrig, playerOrig;
    private Bitmap enemy1Orig, enemy2Orig, enemy3Orig;
    private Bitmap musicOnOrig, musicOffOrig, soundOnOrig, soundOffOrig;
    private Bitmap explosionBitmapOrig; // Original explosion bitmap

    // Scaled bitmaps for drawing (derived from originals)
    private Bitmap background, player;
    private Bitmap musicOnBitmap, musicOffBitmap, soundOnBitmap, soundOffBitmap;
    private Bitmap explosionBitmap; // Scaled explosion bitmap for enemies/player

    // Background scrolling
    private float bgY1 = 0f, bgY2 = 0f;
    private final float bgSpeed = 12f; // Tốc độ cuộn nền

    // Screen dimensions
    private int screenWidth = 0, screenHeight = 0;

    // Player properties
    private float playerX = 0f, playerY = 0f;
    private float targetX = 0f, targetY = 0f; // For smooth movement
    private final float playerSpeed = 25f; // Tốc độ di chuyển của player
    private int playerHealth = 3;
    private boolean playerExploding = false;

    // Player scale (1.0 = default).
    private float playerScale = 1.5f;

    // Flag: resources applied (from PreloadManager or fallback)
    private volatile boolean resourcesLoaded = false;

    // Invincibility after hit
    private boolean playerInvincible = false;
    private long playerInvincibleStart = 0;
    private final long playerInvincibleDuration = 800; // ms

    // Game objects
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();

    // Sound
    private SoundPool soundPool;
    private int soundShoot = 0, soundBorder = 0, soundWarning = 0, soundPlayerExplode = 0, soundEnemyExplode = 0;
    private boolean soundEffectsOn = true;
    private MediaPlayer bgMusic;
    private boolean musicOn = true;

    // UI Button positions
    private float musicBtnX = 0f, musicBtnY = 0f;
    private float soundBtnX = 0f, soundBtnY = 0f;
    private Rect musicButtonRect;
    private Rect soundButtonRect;

    // Game state & logic
    private boolean isTouching = false; // Player is touching screen (for shooting)
    private long lastShotTime = 0;
    private final long shootInterval = 250; // Milliseconds between shots

    private boolean isGameOver = false;
    private final Random random = new Random();
    private int score = 0;

    // Enemy spawning logic
    private long lastEnemySpawnTime = 0;
    private long enemySpawnInterval = 1800; // Spawn new enemy every 1.8 seconds
    private final Set<Enemy> warnedEnemies = new HashSet<>(); // Avoid repeated warning sounds

    // Track whether surface is ready
    private volatile boolean surfaceReady = false;

    public GameView(Context context) {
        super(context);
        holder = getHolder();
        paint = new Paint();
        paint.setAntiAlias(true);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        paint = new Paint();
        paint.setAntiAlias(true);
        init(context);
    }

    private void init(final Context ctx) {
        // Surface lifecycle
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                surfaceReady = true;
                Log.d(TAG, "surfaceCreated");
                if (isPlaying && (gameThread == null || !gameThread.isAlive())) {
                    startGameThread();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) { }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                surfaceReady = false;
                Log.d(TAG, "surfaceDestroyed");
                isPlaying = false;
                if (gameThread != null) {
                    try { gameThread.join(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
                gameThread = null;
            }
        });

        // First try: apply preloaded assets if already available
        tryApplyPreloadedOrPoll();
    }

    // Try to apply preloaded assets; if not ready, poll a few times then give up (fallback to placeholders)
    private void tryApplyPreloadedOrPoll() {
        final PreloadManager pm = PreloadManager.getInstance();
        if (pm.isLoaded()) {
            applyPreloaded(pm);
            Log.d(TAG, "Applied preloaded resources immediately in GameView.init()");
            return;
        }

        // Poller: check every 120ms for up to ~80 tries (~9.6s). Adjust as needed.
        final int maxTries = 80;
        final int[] tries = {0};
        final Runnable checker = new Runnable() {
            @Override
            public void run() {
                tries[0]++;
                if (pm.isLoaded()) {
                    applyPreloaded(pm);
                    Log.d(TAG, "Applied preloaded resources after " + tries[0] + " checks");
                    return;
                }
                if (tries[0] < maxTries) {
                    postDelayed(this, 120);
                } else {
                    Log.w(TAG, "PreloadManager not ready after timeout; GameView will use placeholders and/or fallback loading");
                    // resourcesLoaded remains false -> GameView will draw placeholder until maybe a later check
                }
            }
        };
        post(checker);
    }

    // Pull assets from PreloadManager into GameView fields and scale/position them.
    private void applyPreloaded(PreloadManager pm) {
        // Assign originals from PreloadManager (they may already be sampled/decoded to reasonable sizes)
        backgroundOrig = pm.getBackground();
        playerOrig = pm.getPlayer();
        enemy1Orig = pm.getEnemy1();
        enemy2Orig = pm.getEnemy2();
        enemy3Orig = pm.getEnemy3();
        musicOnOrig = pm.getMusicOn();
        musicOffOrig = pm.getMusicOff();
        soundOnOrig = pm.getSoundOn();
        soundOffOrig = pm.getSoundOff();
        explosionBitmapOrig = pm.getExplosion();

        // Sound & music
        soundPool = pm.getSoundPool();
        soundShoot = pm.getSoundShoot();
        soundBorder = pm.getSoundBorder();
        soundWarning = pm.getSoundWarning();
        soundPlayerExplode = pm.getSoundPlayerExplode();
        soundEnemyExplode = pm.getSoundEnemyExplode();
        bgMusic = pm.getBgMusic();

        // Scale to current screen size if available (applyScaling will be safe if bitmaps already match)
        if (screenWidth > 0 && screenHeight > 0) {
            applyScalingForCurrentSize();
            positionPlayerBottomCenter();
        }

        resourcesLoaded = true;
        postInvalidate();

        // Safe approach: reset game so enemies are recreated with correct sizes (preserves consistent collision)
        resetGame();
    }

    // Prepare MediaPlayer asynchronously (fallback in case PreloadManager didn't prepare music)
    private void prepareBgMusicAsync(Context ctx) {
        try {
            if (bgMusic != null) return; // already prepared by PreloadManager
            MediaPlayer mp = new MediaPlayer();
            AssetFileDescriptor afd = ctx.getResources().openRawResourceFd(R.raw.bg_music);
            if (afd == null) {
                Log.w(TAG, "bg music afd null");
                return;
            }
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.setLooping(true);
            mp.setOnPreparedListener(mediaPlayer -> {
                bgMusic = mediaPlayer;
                if (musicOn && !isGameOver) bgMusic.start();
            });
            mp.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "prepareBgMusicAsync error", e);
        }
    }

    // Apply scaling of loaded original bitmaps to match current screen size & playerScale
    private void applyScalingForCurrentSize() {
        if (backgroundOrig != null) {
            // If backgroundOrig size differs from screen, scale it; otherwise reuse
            if (backgroundOrig.getWidth() == screenWidth && backgroundOrig.getHeight() == screenHeight) {
                background = backgroundOrig;
            } else {
                background = Bitmap.createScaledBitmap(backgroundOrig, screenWidth, screenHeight, true);
            }
        }
        // player
        if (playerOrig != null) {
            int basePlayerW = Math.max(1, screenWidth / 12);
            int playerW = Math.max(1, (int) (basePlayerW * playerScale));
            int playerH = playerOrig.getHeight() * playerW / Math.max(1, playerOrig.getWidth());
            player = Bitmap.createScaledBitmap(playerOrig, playerW, playerH, true);
        }
        // explosion scaled to player size if available
        if (explosionBitmapOrig != null && player != null) {
            explosionBitmap = Bitmap.createScaledBitmap(explosionBitmapOrig, player.getWidth(), player.getHeight(), true);
        } else if (explosionBitmapOrig != null) {
            int defaultExplosionSize = Math.max(1, screenWidth / 10);
            explosionBitmap = Bitmap.createScaledBitmap(explosionBitmapOrig, defaultExplosionSize, defaultExplosionSize, true);
        }

        // UI icons
        int iconSize = Math.max(64, Math.min(screenWidth, screenHeight) / 12);
        if (musicOnOrig != null) musicOnBitmap = Bitmap.createScaledBitmap(musicOnOrig, iconSize, iconSize, true);
        if (musicOffOrig != null) musicOffBitmap = Bitmap.createScaledBitmap(musicOffOrig, iconSize, iconSize, true);
        if (soundOnOrig != null) soundOnBitmap = Bitmap.createScaledBitmap(soundOnOrig, iconSize, iconSize, true);
        if (soundOffOrig != null) soundOffBitmap = Bitmap.createScaledBitmap(soundOffOrig, iconSize, iconSize, true);

        // Position UI rects
        float UIMargin = screenWidth / 30f;
        musicBtnX = UIMargin;
        musicBtnY = UIMargin;
        soundBtnX = screenWidth - (soundOnBitmap != null ? soundOnBitmap.getWidth() : iconSize) - UIMargin;
        soundBtnY = UIMargin;

        if (musicOnBitmap != null) musicButtonRect = new Rect((int) musicBtnX, (int) musicBtnY,
                (int) (musicBtnX + musicOnBitmap.getWidth()), (int) (musicBtnY + musicOnBitmap.getHeight()));
        if (soundOnBitmap != null) soundButtonRect = new Rect((int) soundBtnX, (int) soundBtnY,
                (int) (soundBtnX + soundOnBitmap.getWidth()), (int) (soundBtnY + soundOnBitmap.getHeight()));
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!holder.getSurface().isValid()) {
                try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                continue;
            }

            long frameStartTime = System.currentTimeMillis();

            if (!isGameOver) updateGame();
            drawGame();

            long timeThisFrame = System.currentTimeMillis() - frameStartTime;
            long sleepTime = 16 - timeThisFrame;
            if (sleepTime > 0) {
                try { Thread.sleep(sleepTime); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void updateGame() {
        if (playerInvincible && System.currentTimeMillis() - playerInvincibleStart > playerInvincibleDuration) {
            playerInvincible = false;
        }

        // Scroll background
        bgY1 += bgSpeed;
        bgY2 += bgSpeed;
        if (bgY1 >= screenHeight) bgY1 = bgY2 - screenHeight;
        if (bgY2 >= screenHeight) bgY2 = bgY1 - screenHeight;

        // Player movement
        if (!playerExploding && player != null) {
            float dx = targetX - playerX;
            float dy = targetY - playerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance > playerSpeed) {
                playerX += (dx / distance) * playerSpeed;
                playerY += (dy / distance) * playerSpeed;
            } else if (distance > 0) {
                playerX = targetX;
                playerY = targetY;
            }

            // clamp
            if (playerX < 0) playerX = 0;
            if (playerY < 0) playerY = 0;
            if (playerX > screenWidth - player.getWidth()) playerX = screenWidth - player.getWidth();
            if (playerY > screenHeight - player.getHeight()) playerY = screenHeight - player.getHeight();
        }

        // shooting
        if (isTouching && !playerExploding && player != null && System.currentTimeMillis() - lastShotTime > shootInterval) {
            shootBullet();
            lastShotTime = System.currentTimeMillis();
        }

        // bullets update
        Iterator<Bullet> bit = bullets.iterator();
        while (bit.hasNext()) {
            Bullet b = bit.next();
            b.update();
            if (b.isOutOfScreen(screenHeight)) {
                bit.remove();
                continue;
            }
        }

        // enemies update
        Iterator<Enemy> eit = enemies.iterator();
        while (eit.hasNext()) {
            Enemy e = eit.next();
            e.update();

            // Remove enemies going far below screen
            if (e.getY() > screenHeight + e.getHeight()) {
                warnedEnemies.remove(e);
                eit.remove();
                continue;
            }

            // Warning sound when enemy approaches bottom area (once)
            if (!warnedEnemies.contains(e) && e.getY() > screenHeight * 0.6f) {
                warnedEnemies.add(e);
                if (soundEffectsOn && soundWarning != 0 && soundPool != null) {
                    soundPool.play(soundWarning, 0.6f, 0.6f, 1, 0, 1f);
                }
            }
        }

        // --- COLLISIONS: bullets <-> enemies ---
        // For each bullet, check against enemies. Remove bullet after hit, apply damage to enemy.
        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet bullet = bulletIter.next();
            Iterator<Enemy> enemyIter = enemies.iterator();
            while (enemyIter.hasNext()) {
                Enemy enemy = enemyIter.next();

                if (Rect.intersects(bullet.getRect(),
                        new Rect((int) enemy.getX(), (int) enemy.getY(),
                                (int) enemy.getX() + enemy.getWidth(),
                                (int) enemy.getY() + enemy.getHeight()))) {

                    bulletIter.remove(); // xóa đạn

                    // gọi takeDamage để trừ máu, trả về true nếu enemy chết
                    boolean killed = enemy.takeDamage(1);

                    if (killed) {
                        score += 10; // 🎯 cộng điểm ở đây
                        // chơi sound explosion
                        if (soundPool != null && soundEffectsOn) {
                            soundPool.play(soundEnemyExplode, 1, 1, 1, 0, 1f);
                        }
                    }

                    break; // ra khỏi vòng lặp enemy cho viên đạn này
                }
            }
        }


        // --- COLLISIONS: player <-> enemies ---
        if (!playerExploding && !playerInvincible && player != null) {
            Rect pRect = new Rect((int)playerX, (int)playerY, (int)(playerX + player.getWidth()), (int)(playerY + player.getHeight()));
            for (Enemy e : new ArrayList<>(enemies)) {
                if (e.isFinished() || e.isExploding()) continue;
                Rect eRect = new Rect((int)e.getX(), (int)e.getY(), (int)(e.getX() + e.getWidth()), (int)(e.getY() + e.getHeight()));
                if (Rect.intersects(pRect, eRect)) {
                    // Collision: damage player and explode enemy
                    playerHealth = Math.max(0, playerHealth - 1);
                    playerInvincible = true;
                    playerInvincibleStart = System.currentTimeMillis();
                    e.explode();
                    if (soundEffectsOn && soundPlayerExplode != 0 && soundPool != null) {
                        soundPool.play(soundPlayerExplode, 0.9f, 0.9f, 1, 0, 1f);
                    }
                    if (playerHealth <= 0) {
                        playerExploding = true;
                        isGameOver = true;
                        // stop music optionally
                        if (bgMusic != null && bgMusic.isPlaying()) bgMusic.pause();
                    }
                    break;
                }
            }
        }

        // cleanup finished enemies
        enemies.removeIf(Enemy::isFinished);
        warnedEnemies.removeIf(enemy -> enemy.isFinished() || !enemies.contains(enemy));

        // spawn logic
        if (System.currentTimeMillis() - lastEnemySpawnTime > enemySpawnInterval || enemies.size() < 2) {
            if (enemies.size() < 6) {
                spawnNewEnemy();
                lastEnemySpawnTime = System.currentTimeMillis();
            }
        }

        if (!playerExploding && player != null && soundEffectsOn && soundBorder != 0 && soundPool != null) {
            if (playerX <= 0 || playerX >= screenWidth - player.getWidth()) {
                soundPool.play(soundBorder, 0.5f, 0.5f, 1, 0, 1f);
            }
        }
    }

    private void drawGame() {
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) return;

        try {
            // If resources not yet loaded, draw a simple loading screen and placeholder player
            if (!resourcesLoaded) {
                canvas.drawColor(0xFF000000);
                paint.setColor(0xFFFFFFFF);
                paint.setTextAlign(Paint.Align.CENTER);
                float textSize = Math.max(24, Math.min(48, Math.min(screenWidth, screenHeight) / 20f));
                paint.setTextSize(textSize);
                canvas.drawText("Loading...", Math.max(1, screenWidth) / 2f, Math.max(1, screenHeight) / 2f, paint);

                if (screenWidth > 0 && screenHeight > 0) {
                    paint.setColor(0xFFCCCCCC);
                    int basePlayerW = Math.max(1, screenWidth / 12);
                    int w = Math.max(1, (int) (basePlayerW * playerScale));
                    int h = Math.max(1, w * 3 / 4);
                    float px = screenWidth / 2f - w / 2f;
                    float py = screenHeight - h - (screenWidth / 30f) * 2;
                    canvas.drawRect(px, py, px + w, py + h, paint);
                }
                return;
            }

            // Draw background
            if (background != null) {
                canvas.drawBitmap(background, 0f, bgY1, null);
                canvas.drawBitmap(background, 0f, bgY2, null);
            } else {
                canvas.drawColor(0xFF000000);
            }

            // Draw player
            if (player != null) {
                if (playerExploding && explosionBitmap != null) {
                    canvas.drawBitmap(explosionBitmap, playerX, playerY, null);
                } else {
                    canvas.drawBitmap(player, playerX, playerY, null);
                }
            }

            // Draw bullets
            paint.setColor(0xFFFFFF00);
            for (Bullet b : bullets) b.draw(canvas, paint);

            // Draw enemies
            for (Enemy e : enemies) e.draw(canvas);

            // UI icons
            if (musicOnBitmap != null && musicOffBitmap != null) canvas.drawBitmap(musicOn ? musicOnBitmap : musicOffBitmap, musicBtnX, musicBtnY, null);
            if (soundOnBitmap != null && soundOffBitmap != null) canvas.drawBitmap(soundEffectsOn ? soundOnBitmap : soundOffBitmap, soundBtnX, soundBtnY, null);

            // HUD
            paint.setColor(0xFFFFFFFF);
            float ts = Math.min(screenWidth, screenHeight) / 22f;
            paint.setTextSize(ts);
            paint.setTextAlign(Paint.Align.LEFT);
            float margin = Math.min(screenWidth, screenHeight) / 30f;
            canvas.drawText("Score: " + score, margin, margin + ts, paint);
            String healthText = "Health: " + Math.max(0, playerHealth);
            if (playerInvincible) healthText += " *";
            canvas.drawText(healthText, margin, margin * 2 + ts * 2, paint);

            if (isGameOver) {
                paint.setColor(0xFFFF3333);
                float goSize = Math.min(screenWidth, screenHeight) / 8f;
                paint.setTextSize(goSize);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f - goSize / 2f, paint);

                paint.setColor(0xFFFFFFFF);
                float restartSize = Math.min(screenWidth, screenHeight) / 18f;
                paint.setTextSize(restartSize);
                canvas.drawText("Tap to Restart", screenWidth / 2f, screenHeight / 2f + restartSize, paint);
            }
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void shootBullet() {
        if (player == null || screenHeight == 0) return;
        int bx = (int) (playerX + player.getWidth() / 2f);
        int by = (int) playerY;
        bullets.add(new Bullet(getContext(), bx, by, screenHeight));
        if (soundEffectsOn && soundShoot != 0 && soundPool != null) soundPool.play(soundShoot, 0.8f, 0.8f, 1, 0, 1f);
    }

    private void spawnNewEnemy() {
        Bitmap chosen = enemy1Orig != null ? enemy1Orig : (enemy2Orig != null ? enemy2Orig : enemy3Orig);
        if (chosen == null || screenWidth <= 0) return;
        int ew = chosen.getWidth();
        int sx = random.nextInt(Math.max(1, screenWidth - ew));
        int sy = -random.nextInt(200) - chosen.getHeight();
        Enemy e = new Enemy(getContext(), chosen, sx, sy, screenWidth, screenHeight);
        e.setExplosionBitmap(explosionBitmap);
        enemies.add(e);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;

        if (resourcesLoaded) {
            applyScalingForCurrentSize();
        } else {
            // placeholder position
            float base = Math.max(1, screenWidth / 12f);
            playerX = screenWidth / 2f - base * playerScale / 2f;
            playerY = screenHeight - base * playerScale - (screenWidth / 30f) * 2;
            targetX = playerX; targetY = playerY;
        }

        float UIMargin = screenWidth / 30f;
        musicBtnX = UIMargin; musicBtnY = UIMargin;
        soundBtnX = screenWidth - (soundOnBitmap != null ? soundOnBitmap.getWidth() : Math.max(64, Math.min(screenWidth, screenHeight) / 12)) - UIMargin;
        soundBtnY = UIMargin;
        if (musicOnBitmap != null) musicButtonRect = new Rect((int)musicBtnX, (int)musicBtnY, (int)(musicBtnX + musicOnBitmap.getWidth()), (int)(musicBtnY + musicOnBitmap.getHeight()));
        if (soundOnBitmap != null) soundButtonRect = new Rect((int)soundBtnX, (int)soundBtnY, (int)(soundBtnX + soundOnBitmap.getWidth()), (int)(soundBtnY + soundOnBitmap.getHeight()));

        if (oldw != 0 || oldh != 0) resetGame();
    }

    private void resetGame() {
        playerHealth = 3; score = 0; playerExploding = false; isGameOver = false;
        bullets.clear(); enemies.clear(); warnedEnemies.clear();
        playerInvincible = false; playerInvincibleStart = 0;
        if (player != null) positionPlayerBottomCenter();
        lastEnemySpawnTime = System.currentTimeMillis();
        for (int i = 0; i < 2; i++) if (enemies.size() < 6) spawnNewEnemy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (player == null && !isGameOver) return false;
        float tx = event.getX(); float ty = event.getY();
        int action = event.getActionMasked();
        if (isGameOver) {
            if (action == MotionEvent.ACTION_DOWN) resetGame();
            return true;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (musicButtonRect != null && musicButtonRect.contains((int)tx, (int)ty)) { toggleMusic(); isTouching = false; return true; }
                if (soundButtonRect != null && soundButtonRect.contains((int)tx, (int)ty)) { toggleSound(); isTouching = false; return true; }
                if (player != null) { targetX = tx - player.getWidth() / 2f; targetY = ty - player.getHeight() / 2f; }
                isTouching = true;
                break;
            case MotionEvent.ACTION_MOVE:
                boolean onMusicButton = musicButtonRect != null && musicButtonRect.contains((int)tx, (int)ty);
                boolean onSoundButton = soundButtonRect != null && soundButtonRect.contains((int)tx, (int)ty);
                if (!onMusicButton && !onSoundButton && player != null) {
                    targetX = tx - player.getWidth() / 2f; targetY = ty - player.getHeight() / 2f; isTouching = true;
                } else isTouching = false;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouching = false; break;
        }
        return true;
    }

    private void toggleMusic() {
        musicOn = !musicOn;
        if (musicOn) {
            if (bgMusic != null && !bgMusic.isPlaying() && !isGameOver) bgMusic.start();
        } else {
            if (bgMusic != null && bgMusic.isPlaying()) bgMusic.pause();
        }
    }

    private void toggleSound() { soundEffectsOn = !soundEffectsOn; }

    public void setPlayerScale(float scale) {
        if (scale <= 0) return;
        float clamped = Math.max(0.2f, Math.min(scale, 3.0f));
        playerScale = clamped;
        Log.d(TAG, "setPlayerScale called: " + playerScale + " screenWidth=" + screenWidth);
        if (playerOrig != null && screenWidth > 0) {
            int basePlayerW = Math.max(1, screenWidth / 12);
            int newW = Math.max(1, (int) (basePlayerW * playerScale));
            int newH = playerOrig.getHeight() * newW / Math.max(1, playerOrig.getWidth());
            player = Bitmap.createScaledBitmap(playerOrig, newW, newH, true);
            if (explosionBitmapOrig != null) explosionBitmap = Bitmap.createScaledBitmap(explosionBitmapOrig, player.getWidth(), player.getHeight(), true);
            playerX = Math.max(0f, Math.min(playerX, Math.max(0, screenWidth - player.getWidth())));
            playerY = Math.max(0f, Math.min(playerY, Math.max(0, screenHeight - player.getHeight())));
            requestLayout(); postInvalidate();
            Log.d(TAG, "player resized to " + player.getWidth() + "x" + player.getHeight());
        } else {
            Log.d(TAG, "scale saved, will apply when resources loaded and size known");
        }
    }

    public float getPlayerScale() { return playerScale; }

    private void positionPlayerBottomCenter() {
        if (player == null || screenWidth <= 0 || screenHeight <= 0) return;
        float UIMargin = Math.max(1, screenWidth / 30f);
        playerX = screenWidth / 2f - player.getWidth() / 2f;
        playerY = screenHeight - player.getHeight() - UIMargin * 2;
        targetX = playerX; targetY = playerY;
    }

    // Start the game loop thread - internal helper
    private void startGameThread() {
        if (gameThread != null && gameThread.isAlive()) return;
        gameThread = new Thread(this);
        isPlaying = true;
        gameThread.start();
        Log.d(TAG, "Game thread started");
    }

    // Call from Activity lifecycle
    public void resume() {
        isPlaying = true;
        if (surfaceReady) startGameThread();
        if (bgMusic != null && musicOn && !bgMusic.isPlaying() && !isGameOver) bgMusic.start();
    }

    public void pause() {
        isPlaying = false;
        if (bgMusic != null && bgMusic.isPlaying()) bgMusic.pause();
        if (gameThread != null) {
            try { gameThread.join(500); } catch (InterruptedException ignored) {}
        }
        gameThread = null;
    }

    public void destroy() {
        isPlaying = false;
        if (bgMusic != null) {
            if (bgMusic.isPlaying()) bgMusic.stop();
            bgMusic.release(); bgMusic = null;
        }
        if (soundPool != null) {
            soundPool.release(); soundPool = null;
        }
        // Let GC collect bitmaps
        backgroundOrig = null; playerOrig = null; enemy1Orig = null; enemy2Orig = null; enemy3Orig = null;
        explosionBitmapOrig = null; background = null; player = null; explosionBitmap = null;
        musicOnBitmap = null; musicOffBitmap = null; soundOnBitmap = null; soundOffBitmap = null;
    }
}