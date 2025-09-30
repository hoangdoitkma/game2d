package com.example.templerunclone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

public class PreloadManager {
    private static final String TAG = "PreloadManager";
    private static final PreloadManager INSTANCE = new PreloadManager();

    public interface Listener {
        void onPreloadFinished();
    }

    private PreloadManager() {}

    public static PreloadManager getInstance() {
        return INSTANCE;
    }

    // ==== Resource đã load ====
    private volatile boolean loaded = false;

    private Bitmap background;
    private Bitmap player;
    private Bitmap enemy1, enemy2, enemy3;
    private Bitmap musicOn, musicOff;
    private Bitmap soundOn, soundOff;
    private Bitmap explosion;

    private SoundPool soundPool;
    private int soundShoot, soundBorder, soundWarning, soundPlayerExplode, soundEnemyExplode;
    private MediaPlayer bgMusic;

    // ==== Getter ====
    public boolean isLoaded() { return loaded; }
    public Bitmap getBackground() { return background; }
    public Bitmap getPlayer() { return player; }
    public Bitmap getEnemy1() { return enemy1; }
    public Bitmap getEnemy2() { return enemy2; }
    public Bitmap getEnemy3() { return enemy3; }
    public Bitmap getMusicOn() { return musicOn; }
    public Bitmap getMusicOff() { return musicOff; }
    public Bitmap getSoundOn() { return soundOn; }
    public Bitmap getSoundOff() { return soundOff; }
    public Bitmap getExplosion() { return explosion; }

    public SoundPool getSoundPool() { return soundPool; }
    public int getSoundShoot() { return soundShoot; }
    public int getSoundBorder() { return soundBorder; }
    public int getSoundWarning() { return soundWarning; }
    public int getSoundPlayerExplode() { return soundPlayerExplode; }
    public int getSoundEnemyExplode() { return soundEnemyExplode; }
    public MediaPlayer getBgMusic() { return bgMusic; }

    // ==== Helpers ====
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Preload resources on background thread. Uses screenW/screenH to downsample images.
     * Listener is invoked on main thread.
     */
    public void preload(Context context, int screenW, int screenH, Listener listener) {
        // If already loaded, immediately invoke listener on main thread
        if (loaded) {
            if (listener != null) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onPreloadFinished());
            }
            return;
        }

        final Context appCtx = context.getApplicationContext();

        // Run background loading
        new Thread(() -> {
            boolean success = true;
            try {
                // Desired sizes for bitmaps (you can tune these)
                final int bgW = Math.max(1, screenW);
                final int bgH = Math.max(1, screenH);
                final int iconSize = Math.max(64, Math.min(screenW, screenH) / 12);
                final int basePlayerW = Math.max(1, screenW / 12);

                // Helper: decode scaled
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.background, opts);
                opts.inSampleSize = calculateInSampleSize(opts, bgW, bgH);
                opts.inJustDecodeBounds = false;
                opts.inPreferredConfig = Bitmap.Config.RGB_565; // save memory (no alpha needed for many sprites)
                background = BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.background, opts);
                if (background != null && (background.getWidth() != bgW || background.getHeight() != bgH)) {
                    background = Bitmap.createScaledBitmap(background, bgW, bgH, true);
                }

                // player
                opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.ship, opts);
                opts.inSampleSize = calculateInSampleSize(opts, basePlayerW, basePlayerW);
                opts.inJustDecodeBounds = false;
                player = BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.ship, opts);

                // enemies
                opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.enemy1, opts);
                opts.inSampleSize = calculateInSampleSize(opts, basePlayerW, basePlayerW);
                opts.inJustDecodeBounds = false;
                enemy1 = BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.enemy1, opts);
                enemy2 = BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.enemy2, opts);
                enemy3 = BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.enemy3, opts);

                // UI icons / explosion
                opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.music_turnon, opts);
                opts.inSampleSize = calculateInSampleSize(opts, iconSize, iconSize);
                opts.inJustDecodeBounds = false;
                musicOn = BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.music_turnon, opts);
                musicOff = BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.music_turnoff, opts);
                soundOn = BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.sound_on, opts);
                soundOff = BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.sound_off, opts);

                opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.explosion1, opts);
                opts.inSampleSize = calculateInSampleSize(opts, basePlayerW, basePlayerW);
                opts.inJustDecodeBounds = false;
                explosion = BitmapFactory.decodeResource(appCtx.getResources(), R.drawable.explosion1, opts);

                // Prepare SoundPool
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();

                soundPool = new SoundPool.Builder()
                        .setMaxStreams(6)
                        .setAudioAttributes(audioAttributes)
                        .build();

                // load sound effects and wait for load completion
                final int expectedSounds = 5; // shoot, border, warning, player explode, enemy explode
                AtomicInteger remaining = new AtomicInteger(expectedSounds);

                soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
                    if (status != 0) {
                        Log.w(TAG, "Sound load failed id=" + sampleId);
                    }
                    if (remaining.decrementAndGet() <= 0) {
                        // All sounds loaded: mark loaded true and notify on main thread (unless already set)
                        loaded = true;
                        if (bgMusic != null) {
                            // nothing
                        }
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (listener != null) listener.onPreloadFinished();
                        });
                    }
                });

                soundShoot = soundPool.load(appCtx, R.raw.shoot, 1);
                soundBorder = soundPool.load(appCtx, R.raw.border, 1);
                soundWarning = soundPool.load(appCtx, R.raw.warning, 1);

                // Optional additional sounds — add these resources if present in res/raw
                // If your project doesn't have these files, remove or comment next two lines
                soundPlayerExplode = soundPool.load(appCtx, R.raw.player_explode, 1);
                soundEnemyExplode = soundPool.load(appCtx, R.raw.enemy_explode, 1);

                // Background music — use application context and prepare (create returns prepared MediaPlayer)
                try {
                    bgMusic = MediaPlayer.create(appCtx, R.raw.bg_music);
                    if (bgMusic != null) bgMusic.setLooping(true);
                } catch (Exception e) {
                    Log.w(TAG, "bgMusic create failed", e);
                }

                // Note: loaded will be set true in OnLoadComplete after all sounds are loaded.
                // However if sound load isn't critical, you may set loaded=true here instead.
                // For safety, if there are zero sounds to load (edge-case), post listener:
                if (expectedSounds == 0) {
                    loaded = true;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (listener != null) listener.onPreloadFinished();
                    });
                }

            } catch (Throwable t) {
                Log.e(TAG, "Preload error", t);
                success = false;
            }

            // If something failed before sound load completes, ensure listener still gets notified on main thread
            if (!success) {
                loaded = false;
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (listener != null) listener.onPreloadFinished();
                });
            }

        }, "PreloadManager-Thread").start();
    }
}