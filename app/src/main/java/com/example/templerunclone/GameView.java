package com.example.templerunclone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private Thread gameThread;
    private volatile boolean isPlaying = false;
    private volatile boolean surfaceReady = false;

    private SurfaceHolder holder;

    private Bitmap backgroundOrig, shipOrig;
    private Bitmap background, ship;

    private int surfaceWidth = 0;
    private int surfaceHeight = 0;

    private float bgY1, bgY2;
    private float bgSpeed = 6f; // giảm tốc độ cho mượt ban đầu

    private float playerX, playerY;

    // Constructors
    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        holder = getHolder();
        holder.addCallback(this);

        // load originals (chưa scale). We'll scale them once surface size is known.
        backgroundOrig = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        shipOrig = BitmapFactory.decodeResource(getResources(), R.drawable.ship);
    }

    // SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // get actual surface dimensions
        surfaceWidth = getWidth();
        surfaceHeight = getHeight();

        if (surfaceWidth <= 0 || surfaceHeight <= 0) {
            // fallback to display metrics (rare)
            surfaceWidth = getResources().getDisplayMetrics().widthPixels;
            surfaceHeight = getResources().getDisplayMetrics().heightPixels;
        }

        // Scale background to surface size (guarantee coverage)
        if (backgroundOrig != null) {
            background = Bitmap.createScaledBitmap(backgroundOrig, surfaceWidth, surfaceHeight, true);
        }

        // Scale ship relatively (1/8 screen width)
        if (shipOrig != null) {
            int shipW = Math.max(1, surfaceWidth / 8);
            int shipH = shipOrig.getHeight() * shipW / Math.max(1, shipOrig.getWidth());
            ship = Bitmap.createScaledBitmap(shipOrig, shipW, shipH, true);
        }

        // Initial positions: make sure screen is already filled
        bgY1 = 0;
        bgY2 = -surfaceHeight; // second tile sits immediately above first (no gap)

        // initial player pos: bottom-center
        playerX = surfaceWidth / 2f - (ship != null ? ship.getWidth() / 2f : 0);
        playerY = surfaceHeight - (ship != null ? ship.getHeight() : 0) - 50;

        surfaceReady = true;

        // if game was requested to run (resume called earlier), start thread now
        if (isPlaying && (gameThread == null || !gameThread.isAlive())) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // not used here
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceReady = false;
        // stop game thread safely
        pause();
    }

    // Game loop
    @Override
    public void run() {
        while (isPlaying && surfaceReady) {
            long start = System.currentTimeMillis();

            update();
            draw();

            long took = System.currentTimeMillis() - start;
            long sleep = Math.max(2, 16 - took); // aim ~60fps, but keep min sleep small
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ignored) {}
        }
    }

    private void update() {
        // update background positions
        bgY1 += bgSpeed;
        bgY2 += bgSpeed;

        // reset logic: when a tile moves entirely below screen, place it above the other
        if (bgY1 >= surfaceHeight) {
            bgY1 = bgY2 - surfaceHeight;
        }
        if (bgY2 >= surfaceHeight) {
            bgY2 = bgY1 - surfaceHeight;
        }
    }

    private void draw() {
        if (!surfaceReady) return;

        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            if (canvas == null) return;

            // draw two background tiles to always cover canvas
            if (background != null) {
                canvas.drawBitmap(background, 0, bgY1, null);
                canvas.drawBitmap(background, 0, bgY2, null);
            }

            // draw ship
            if (ship != null) {
                canvas.drawBitmap(ship, playerX, playerY, null);
            }
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    // Touch: ship follows finger (centered)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!surfaceReady) return true;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                if (ship != null) {
                    playerX = x - ship.getWidth() / 2f;
                    playerY = y - ship.getHeight() / 2f;
                } else {
                    playerX = x;
                    playerY = y;
                }
                // clamp
                playerX = Math.max(0, Math.min(playerX, surfaceWidth - (ship != null ? ship.getWidth() : 0)));
                playerY = Math.max(0, Math.min(playerY, surfaceHeight - (ship != null ? ship.getHeight() : 0)));
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // no-op: keep ship where released
                break;
        }
        return true;
    }

    // Lifecycle helpers
    public void resume() {
        if (isPlaying) return;
        isPlaying = true;
        // only start thread if surface already ready
        if (surfaceReady) {
            gameThread = new Thread(this);
            gameThread.start();
        }
        // if not ready, thread will start in surfaceCreated()
    }

    public void pause() {
        if (!isPlaying) return;
        isPlaying = false;
        // join thread
        try {
            if (gameThread != null) {
                gameThread.join();
            }
        } catch (InterruptedException ignored) {}
        gameThread = null;
    }
}
