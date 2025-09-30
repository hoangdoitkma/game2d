package com.example.templerunclone;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Explosion animation controlled by an array of frames (Bitmap[]).
 * Constructor signature matches how GameView instantiates it:
 *   new Explosion(explosionFrames, x, y)
 */
public class Explosion {
    private final Bitmap[] frames;
    private final int x;
    private final int y;

    private int frameIndex = 0;
    private int frameTicker = 0;
    private int frameDelay = 4; // số vòng update trước khi chuyển frame (tăng để làm chậm)
    private boolean finished = false;

    /**
     * @param frames mảng các frame (Bitmap) của vụ nổ (đã load/scale ở GameView)
     * @param x toạ độ x (pixel) để vẽ vụ nổ (thường là vị trí enemy/ship)
     * @param y toạ độ y (pixel)
     */
    public Explosion(Bitmap[] frames, int x, int y) {
        if (frames == null || frames.length == 0) {
            throw new IllegalArgumentException("frames must not be null/empty");
        }
        this.frames = frames;
        this.x = x;
        this.y = y;
        this.frameIndex = 0;
        this.frameTicker = 0;
    }

    /**
     * Gọi mỗi vòng update để tiến animation.
     */
    public void update() {
        if (finished) return;

        frameTicker++;
        if (frameTicker >= frameDelay) {
            frameTicker = 0;
            frameIndex++;
            if (frameIndex >= frames.length) {
                finished = true;
            }
        }
    }

    /**
     * Vẽ frame hiện tại lên canvas (không dùng Paint).
     */
    public void draw(Canvas canvas) {
        if (finished) return;
        Bitmap bmp = frames[Math.min(frameIndex, frames.length - 1)];
        if (bmp != null && !bmp.isRecycled()) {
            canvas.drawBitmap(bmp, x, y, null);
        }
    }

    /**
     * Vẽ frame hiện tại lên canvas bằng Paint (nếu bạn muốn truyền Paint từ GameView).
     */
    public void draw(Canvas canvas, Paint paint) {
        if (finished) return;
        Bitmap bmp = frames[Math.min(frameIndex, frames.length - 1)];
        if (bmp != null && !bmp.isRecycled()) {
            canvas.drawBitmap(bmp, x, y, paint);
        }
    }

    public boolean isFinished() {
        return finished;
    }
}
