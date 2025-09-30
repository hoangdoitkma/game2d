package com.example.templerunclone;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class GameActivity extends Activity {
    private static final String TAG = "GameActivity";
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView);

        // Optionally if PreloadManager already loaded, you can pass resources or let GameView read them itself.
        if (PreloadManager.getInstance().isLoaded()) {
            Log.d(TAG, "Preloaded resources available on GameActivity start.");
            // If your GameView expects preloaded resources, it can fetch them in its init() method.
        } else {
            Log.d(TAG, "Preloaded not ready yet; GameView will show placeholder while loading.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameView.destroy();
    }
}