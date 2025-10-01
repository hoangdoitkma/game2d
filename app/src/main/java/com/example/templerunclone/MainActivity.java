package com.example.templerunclone;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Create and set the game view
        gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Don't start game here, let it start when surface is ready
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.startGame(); // Start game on resume
            gameView.resumeGame();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pauseGame();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gameView != null) {
            gameView.stopGame();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null) {
            gameView.cleanup();
        }
    }
}
