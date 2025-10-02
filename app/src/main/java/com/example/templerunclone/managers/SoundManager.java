package com.example.templerunclone.managers;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import com.example.templerunclone.R;

/**
 * Manages all sound effects and background music
 */
public class SoundManager {
    private SoundPool soundPool;
    private MediaPlayer bgMusic;
    
    private int soundShoot = 0;
    private int soundEnemyExplode = 0;
    private int soundPlayerHit = 0;
    private int soundBorderHit = 0;
    private int soundPowerUp = 0;
    private int soundCongratulations = 0;
    
    private boolean soundEffectsEnabled = true;
    private boolean musicEnabled = true;
    
    public void initialize(Context context) {
        try {
            // Initialize SoundPool for sound effects
            soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .build();
                
            // Load sound effects from raw resources
            soundShoot = soundPool.load(context, R.raw.shoot, 1);
            soundEnemyExplode = soundPool.load(context, R.raw.enemy_explode, 1);
            soundPlayerHit = soundPool.load(context, R.raw.player_explode, 1);
            soundBorderHit = soundPool.load(context, R.raw.border, 1);
            soundPowerUp = soundPool.load(context, R.raw.warning, 1); // Using warning sound for power-up
            
            // Try to load congratulations sound
            try {
                soundCongratulations = soundPool.load(context, R.raw.congratulation, 1);
            } catch (Exception e) {
                Log.w("SoundManager", "Congratulations sound not found, will use default sound");
                soundCongratulations = soundPowerUp; // Fallback to power-up sound
            }
            
            // Initialize background music
            bgMusic = MediaPlayer.create(context, R.raw.bg_music);
            if (bgMusic != null) {
                bgMusic.setLooping(true);
                bgMusic.setVolume(0.5f, 0.5f); // Set volume to 50%
            }
            
            Log.d("SoundManager", "All sounds loaded successfully");
            
        } catch (Exception e) {
            Log.e("SoundManager", "Error loading sounds", e);
        }
    }
    
    public void playShoot() {
        if (soundEffectsEnabled && soundShoot != 0) {
            soundPool.play(soundShoot, 1f, 1f, 1, 0, 1f);
        }
    }
    
    public void playEnemyExplode() {
        if (soundEffectsEnabled && soundEnemyExplode != 0) {
            soundPool.play(soundEnemyExplode, 1f, 1f, 1, 0, 1f);
        }
    }
    
    public void playPlayerHit() {
        if (soundEffectsEnabled && soundPlayerHit != 0) {
            soundPool.play(soundPlayerHit, 1f, 1f, 1, 0, 1f);
        }
    }
    
    public void playBorderHit() {
        if (soundEffectsEnabled && soundBorderHit != 0) {
            soundPool.play(soundBorderHit, 1f, 1f, 1, 0, 1f);
        }
    }
    
    public void playPowerUp() {
        if (soundEffectsEnabled && soundPowerUp != 0) {
            soundPool.play(soundPowerUp, 1f, 1f, 1, 0, 1f);
        }
    }
    
    public void playCongratulations() {
        if (soundEffectsEnabled && soundCongratulations != 0) {
            soundPool.play(soundCongratulations, 1f, 1f, 1, 0, 1f);
        }
    }
    
    public void startMusic() {
        if (musicEnabled && bgMusic != null && !bgMusic.isPlaying()) {
            bgMusic.start();
        }
    }
    
    public void pauseMusic() {
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }
    
    public void resumeMusic() {
        if (musicEnabled && bgMusic != null) {
            bgMusic.start();
        }
    }
    
    public void stopMusic() {
        if (bgMusic != null) {
            bgMusic.stop();
        }
    }
    
    public void cleanup() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        
        if (bgMusic != null) {
            bgMusic.release();
            bgMusic = null;
        }
    }
    
    // Getters and setters
    public boolean isSoundEffectsEnabled() { return soundEffectsEnabled; }
    public boolean isMusicEnabled() { return musicEnabled; }
    
    public void setSoundEffectsEnabled(boolean enabled) { this.soundEffectsEnabled = enabled; }
    public void setMusicEnabled(boolean enabled) { 
        this.musicEnabled = enabled;
        if (!enabled) {
            pauseMusic();
        } else {
            resumeMusic();
        }
    }
}