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
import com.example.templerunclone.ui.LoadingScreen;

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
    private LoadingScreen loadingScreen;
    
    // Screen dimensions
    private int screenWidth = 0, screenHeight = 0;
    
    // Surface state
    private volatile boolean surfaceReady = false;
    private volatile boolean resourcesLoaded = false;
    
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
                
                // Initialize loading screen
                loadingScreen = new LoadingScreen(screenWidth, screenHeight);
                
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
            
            // Initialize game engine first (lightweight)
            gameEngine = new GameEngine(screenWidth, screenHeight);
            gameEngine.setContext(getContext());
            
            // Load resources in background thread to avoid blocking UI
            new Thread(() -> {
                try {
                    Log.d(TAG, "Loading resources in background...");
                    
                    // Initialize resource manager
                    resourceManager = new ResourceManager(screenWidth, screenHeight);
                    resourceManager.loadResources(getContext());
                    
                    // Initialize sound manager
                    soundManager = new SoundManager();
                    soundManager.initialize(getContext());
                    
                    // Set managers on main thread
                    post(() -> {
                        if (gameEngine != null) {
                            gameEngine.setResourceManager(resourceManager);
                            gameEngine.setSoundManager(soundManager);
                            resourcesLoaded = true;
                            Log.d(TAG, "Game engine initialized successfully");
                        }
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error loading resources", e);
                    post(() -> {
                        // Fallback: create minimal resources
                        if (gameEngine != null && resourceManager == null) {
                            resourceManager = new ResourceManager(screenWidth, screenHeight);
                            soundManager = new SoundManager();
                            gameEngine.setResourceManager(resourceManager);
                            gameEngine.setSoundManager(soundManager);
                            resourcesLoaded = true;
                        }
                    });
                }
            }).start();
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
        long lastFpsTime = System.currentTimeMillis();
        
        while (isPlaying && surfaceReady) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastTime;
            
            // Skip frames if too much time has passed (prevent spiral of death)
            if (deltaTime > 100) { // If more than 100ms passed, skip heavy operations
                deltaTime = 16; // Cap to 60 FPS equivalent
            }
            
            // Update game logic only if resources are loaded
            if (gameEngine != null && resourcesLoaded) {
                gameEngine.update();
                
                // Check for pending actions from game over screen
                String pendingAction = gameEngine.getGameState().getPendingAction();
                if (pendingAction != null) {
                    handleGameAction(pendingAction);
                }
            } else if (gameEngine == null) {
                // Only try to initialize if game engine is null
                initializeGameEngine();
            }
            
            // Render game
            render();
            
            // Reduced logging frequency to every 3 seconds
            frameCount++;
            if (frameCount % 180 == 0) {
                long currentFpsTime = System.currentTimeMillis();
                float fps = 180000f / (currentFpsTime - lastFpsTime);
                Log.d(TAG, "FPS: " + String.format("%.1f", fps) + ", GameEngine: " + (gameEngine != null ? "OK" : "NULL"));
                lastFpsTime = currentFpsTime;
            }
            
            // Control frame rate with better timing
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
                // Launch high scores activity
                Intent highScoreIntent = new Intent(getContext(), com.example.templerunclone.HighScoreActivity.class);
                getContext().startActivity(highScoreIntent);
                break;
        }
    }
    
    private void render() {
        if (!surfaceReady || holder == null) {
            return; // Reduce logging spam
        }
        
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                // Clear canvas with optimized method
                canvas.drawColor(android.graphics.Color.BLACK);
                
                if (gameEngine != null && resourcesLoaded) {
                    // Only render if all components are ready
                    gameEngine.render(canvas, paint);
                } else if (loadingScreen != null) {
                    // Show loading screen
                    loadingScreen.draw(canvas);
                } else {
                    // Fallback loading text
                    paint.setColor(android.graphics.Color.WHITE);
                    paint.setTextSize(60);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("Loading...", canvas.getWidth() / 2f, canvas.getHeight() / 2f, paint);
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