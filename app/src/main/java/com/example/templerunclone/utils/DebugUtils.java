package com.example.templerunclone.utils;

import android.util.Log;

/**
 * Debug utility for logging game state
 */
public class DebugUtils {
    private static final String TAG = "GameDebug";
    
    public static void logGameState(String component, String message) {
        Log.d(TAG, "[" + component + "] " + message);
    }
    
    public static void logError(String component, String message, Throwable throwable) {
        Log.e(TAG, "[" + component + "] " + message, throwable);
    }
    
    public static void logWarning(String component, String message) {
        Log.w(TAG, "[" + component + "] " + message);
    }
}