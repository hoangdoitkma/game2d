package com.example.templerunclone.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Utility for optimized bitmap loading
 */
public class BitmapUtils {
    
    public static Bitmap loadOptimizedBitmap(Context context, int resourceId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        
        // Scale to exact size if needed
        if (bitmap.getWidth() != reqWidth || bitmap.getHeight() != reqHeight) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
            bitmap.recycle();
            return scaledBitmap;
        }
        
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