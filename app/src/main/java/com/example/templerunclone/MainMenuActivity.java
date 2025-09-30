package com.example.templerunclone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainMenuActivity extends Activity {

    private PreloadManager pm;
    private ProgressBar progressBar;
    private View menuLayout;
    private boolean musicOn = true;
    private boolean soundOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        progressBar = findViewById(R.id.progressBar);
        menuLayout = findViewById(R.id.menuLayout);
        menuLayout.setVisibility(View.GONE);

        pm = PreloadManager.getInstance();

        // Bắt đầu preload resource
        pm.preload(this, getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels,
                new PreloadManager.Listener() {
                    @Override
                    public void onPreloadFinished() {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            menuLayout.setVisibility(View.VISIBLE);
                            setupMenu();
                        });
                    }
                });
    }

    private void setupMenu() {
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnExit = findViewById(R.id.btnExit);
        ImageButton btnMusic = findViewById(R.id.btnMusic);
        ImageButton btnSound = findViewById(R.id.btnSound);

        // nút Play
        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
            startActivity(intent);
        });

        // nút Exit
        btnExit.setOnClickListener(v -> finish());

        // nút Music
        btnMusic.setOnClickListener(v -> {
            musicOn = !musicOn;
            btnMusic.setImageBitmap(musicOn ? pm.getMusicOn() : pm.getMusicOff());
            Toast.makeText(this, musicOn ? "Music On" : "Music Off", Toast.LENGTH_SHORT).show();
            if (musicOn && pm.getBgMusic() != null) {
                pm.getBgMusic().start();
            } else if (pm.getBgMusic() != null) {
                pm.getBgMusic().pause();
            }
        });

        // nút Sound
        btnSound.setOnClickListener(v -> {
            soundOn = !soundOn;
            btnSound.setImageBitmap(soundOn ? pm.getSoundOn() : pm.getSoundOff());
            Toast.makeText(this, soundOn ? "Sound On" : "Sound Off", Toast.LENGTH_SHORT).show();
        });
    }
}
