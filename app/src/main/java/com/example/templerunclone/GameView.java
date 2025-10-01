package com.example.templerunclone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.templerunclone.engine.GameEngine;
import com.example.templerunclone.managers.ResourceManager;
import com.example.templerunclone.managers.SoundManager;

/**
 * Main GameView using the restructured architecture
 */
public class GameView extends SurfaceView implements Runnable {
    
    private static final String TAG = "GameView";
    private static final int TARGET_FPS = 60;
    private static final long FRAME_TIME = 1000 / TARGET_FPS;
    
    // Core components
    private Thread gameThread;
    private volatile boolean isPlaying = false;
    private SurfaceHolder holder;
    private Paint paint;
    
    // Game engine
    private GameEngine gameEngine;
    private ResourceManager resourceManager;
    private SoundManager soundManager;
    
    // Screen dimensions
    private int screenWidth = 0, screenHeight = 0;
    
    // Surface state
    private volatile boolean surfaceReady = false;
    
    public GameView(Context context) {
        super(context);
        initialize(context);
    }
    
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }
    
    private void initialize(Context context) {
        holder = getHolder();
        paint = new Paint();
        paint.setAntiAlias(true);
        
        // Set up surface callbacks
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                surfaceReady = true;
                Log.d(TAG, "Surface created");
                
                if (isPlaying && (gameThread == null || !gameThread.isAlive())) {
                    startGameThread();
                }
            }
            
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
                screenWidth = width;
                screenHeight = height;
                Log.d(TAG, "Surface changed: " + width + "x" + height);
                
                // Initialize game engine with screen dimensions
                initializeGameEngine();
            }
            
            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                surfaceReady = false;
                Log.d(TAG, "Surface destroyed");
                
                stopGame();
            }
        });
    }
    
    private void initializeGameEngine() {
        if (gameEngine == null && screenWidth > 0 && screenHeight > 0) {
            Log.d(TAG, "Initializing game engine with dimensions: " + screenWidth + "x" + screenHeight);
            
            gameEngine = new GameEngine(screenWidth, screenHeight);
            
            // Initialize resource manager
            resourceManager = new ResourceManager(screenWidth, screenHeight);
            resourceManager.loadResources(getContext());
            gameEngine.setResourceManager(resourceManager);
            
            // Initialize sound manager
            soundManager = new SoundManager();
            soundManager.initialize(getContext());
            gameEngine.setSoundManager(soundManager);
            
            Log.d(TAG, "Game engine initialized successfully");
        }
    }
    
    public void startGame() {
        Log.d(TAG, "startGame() called - surfaceReady: " + surfaceReady + ", isPlaying: " + isPlaying);
        
        if (!isPlaying) {
            isPlaying = true;
            
            if (surfaceReady) {
                startGameThread();
            } else {
                Log.d(TAG, "Surface not ready yet, will start when surface is created");
            }
            
            // Start background music
            if (soundManager != null) {
                soundManager.startMusic();
            }
        }
    }
    
    public void stopGame() {
        isPlaying = false;
        
        if (gameThread != null) {
            try {
                gameThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Game thread interrupted during stop", e);
            }
            gameThread = null;
        }
        
        if (gameEngine != null) {
            gameEngine.cleanup();
        }
        
        if (soundManager != null) {
            soundManager.cleanup();
        }
    }
    
    public void cleanup() {
        // Call stopGame to ensure proper cleanup
        stopGame();
    }
    
    public void pauseGame() {
        if (gameEngine != null) {
            gameEngine.pause();
        }
        if (soundManager != null) {
            soundManager.pauseMusic();
        }
    }

    public void resumeGame() {
        if (gameEngine != null) {
            gameEngine.resume();
        }
        if (soundManager != null) {
            soundManager.resumeMusic();
        }
    }    private void startGameThread() {
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }
    
    @Override
    public void run() {
        Log.d(TAG, "Game thread started");
        
        long lastTime = System.currentTimeMillis();
        int frameCount = 0;
        
        while (isPlaying && surfaceReady) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastTime;
            
            // Update game logic
            if (gameEngine != null) {
                gameEngine.update();
                
                // Check for pending actions from game over screen
                String pendingAction = gameEngine.getGameState().getPendingAction();
                if (pendingAction != null) {
                    handleGameAction(pendingAction);
                }
            } else {
                Log.w(TAG, "Game engine is null, trying to initialize...");
                initializeGameEngine();
            }
            
            // Render game
            render();
            
            // Log every 60 frames (1 second at 60 FPS)
            frameCount++;
            if (frameCount % 60 == 0) {
                Log.d(TAG, "Game running - Frame: " + frameCount + ", GameEngine: " + (gameEngine != null ? "OK" : "NULL"));
            }
            
            // Control frame rate
            long frameTime = System.currentTimeMillis() - currentTime;
            if (frameTime < FRAME_TIME) {
                try {
                    Thread.sleep(FRAME_TIME - frameTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            lastTime = currentTime;
        }
        
        Log.d(TAG, "Game thread ended");
    }
    
    private void handleGameAction(String action) {
        switch (action) {
            case "HOME":
                // Return to main menu
                getContext().startActivity(new Intent(getContext(), MainMenuActivity.class));
                if (getContext() instanceof MainActivity) {
                    ((MainActivity) getContext()).finish();
                }
                break;
            case "HIGH_SCORES":
                // TODO: Implement high scores screen
                Log.d(TAG, "High scores requested");
                break;
        }
    }
    
    private void render() {
        if (!surfaceReady || holder == null) {
            Log.w(TAG, "Cannot render - surfaceReady: " + surfaceReady + ", holder: " + (holder != null ? "OK" : "NULL"));
            return;
        }
        
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                if (gameEngine != null) {
                    // Clear canvas
                    canvas.drawRGB(0, 0, 0);
                    
                    // Render game
                    gameEngine.render(canvas, paint);
                } else {
                    // Show loading screen if game engine not ready
                    canvas.drawRGB(50, 50, 50); // Dark gray background
                    paint.setColor(android.graphics.Color.WHITE);
                    paint.setTextSize(60);
                    canvas.drawText("Loading...", canvas.getWidth() / 4f, canvas.getHeight() / 2f, paint);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during rendering", e);
        } finally {
            if (canvas != null) {
                try {
                    holder.unlockCanvasAndPost(canvas);
                } catch (Exception e) {
                    Log.e(TAG, "Error unlocking canvas", e);
                }
            }
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameEngine == null) return false;
        
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                gameEngine.handleTouch(x, y, true);
                return true;
                
            case MotionEvent.ACTION_MOVE:
                gameEngine.handleTouch(x, y, true);
                return true;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                gameEngine.handleTouch(x, y, false);
                return true;
        }
        
        return super.onTouchEvent(event);
    }
    
    // Public API
    public boolean isGameOver() {
        return gameEngine != null && gameEngine.isGameOver();
    }
    
    public int getScore() {
        return gameEngine != null ? gameEngine.getGameState().getScore() : 0;
    }
    
    public int getLevel() {
        return gameEngine != null ? gameEngine.getGameState().getLevel() : 1;
    }
    
    public void resetGame() {
        if (gameEngine != null) {
            gameEngine.getGameState().reset();
        }
    }
}