package com.example.templerunclone.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Utility for optimized bitmap loading
 */
public class BitmapUtils {
    
    public static Bitmap loadOptimizedBitmap(Context context, int resourceId, int reqWidth, int reqHeight) {
        // Guard against zero or negative target sizes
        if (reqWidth <= 0 || reqHeight <= 0) {
            // Try to fetch original bounds to set a sane default
            final BitmapFactory.Options boundsOpts = new BitmapFactory.Options();
            boundsOpts.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(context.getResources(), resourceId, boundsOpts);
            int fallbackW = boundsOpts.outWidth > 0 ? boundsOpts.outWidth : 64;
            int fallbackH = boundsOpts.outHeight > 0 ? boundsOpts.outHeight : 64;
            android.util.Log.w("BitmapUtils", "Requested size invalid (" + reqWidth + "x" + reqHeight + "), using fallback " + fallbackW + "x" + fallbackH);
            reqWidth = fallbackW;
            reqHeight = fallbackH;
        }
        android.util.Log.d("BitmapUtils", "=== BITMAP LOADING DEBUG ===");
        android.util.Log.d("BitmapUtils", "Resource ID: " + resourceId);
        android.util.Log.d("BitmapUtils", "Requested size: " + reqWidth + "x" + reqHeight);
        
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        
        android.util.Log.d("BitmapUtils", "Original size: " + options.outWidth + "x" + options.outHeight);
        android.util.Log.d("BitmapUtils", "MIME type: " + options.outMimeType);
        
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        android.util.Log.d("BitmapUtils", "Sample size: " + options.inSampleSize);
        
        // Use RGB_565 format to save memory (except for images with transparency)
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true; // Allow system to reclaim memory if needed
        options.inInputShareable = true;
        
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        
        if (bitmap == null) {
            android.util.Log.e("BitmapUtils", "Failed to decode resource " + resourceId);
            return createFallbackBitmap(reqWidth, reqHeight);
        }
        
        android.util.Log.d("BitmapUtils", "Decoded bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
        
        // Scale to exact size if needed (only if difference is significant)
        if (Math.abs(bitmap.getWidth() - reqWidth) > 5 || Math.abs(bitmap.getHeight() - reqHeight) > 5) {
            android.util.Log.d("BitmapUtils", "Scaling bitmap from " + bitmap.getWidth() + "x" + bitmap.getHeight() + " to " + reqWidth + "x" + reqHeight);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false); // Use false for better performance
            if (scaledBitmap != bitmap) {
                bitmap.recycle(); // Free original bitmap memory
            }
            android.util.Log.d("BitmapUtils", "Final bitmap size: " + scaledBitmap.getWidth() + "x" + scaledBitmap.getHeight());
            return scaledBitmap;
        }
        
        android.util.Log.d("BitmapUtils", "No scaling needed, returning original decoded bitmap");
        return bitmap;
    }
    
    private static Bitmap createFallbackBitmap(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColor(android.graphics.Color.GRAY);
        canvas.drawRect(0, 0, width, height, paint);
        return bitmap;
    }
    
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
}