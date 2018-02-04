package com.imagine.story.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.imagine.story.R;

import static java.lang.Math.abs;
import static java.lang.Math.floor;

/**
 * Created by meijian on 2018/1/23.
 */

public class ScrollingImageView extends AppCompatImageView {

    private final int speed;
    private final Bitmap bitmap;

    private Rect clipBounds = new Rect();
    private int offset = 0;

    private boolean isStarted;

    public ScrollingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ScrollingImageView, 0, 0);
        try {
            speed = ta.getDimensionPixelSize(R.styleable.ScrollingImageView_speed, 10);
            bitmap = BitmapFactory.decodeResource(getResources(), ta.getResourceId(R.styleable.ScrollingImageView_src, 0));
        } finally {
            ta.recycle();
        }
        start();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) {
            return;
        }

        // 裁剪的边界--画布的宽度和高度
        canvas.getClipBounds(clipBounds);

        int normalizedOffset = offset;
        int layerWidth = bitmap.getWidth();
        if (offset < -layerWidth) {
            offset += (int) (floor(abs(normalizedOffset) / (float) layerWidth) * layerWidth);
        }

        int left = offset;
        while (left < clipBounds.width()) {
            canvas.drawBitmap(bitmap, getBitmapLeft(layerWidth, left), 0, null);
            left += layerWidth;
        }

        if (isStarted) {
            offset -= speed;
            postInvalidateOnAnimation();
        }
    }

    private float getBitmapLeft(int layerWidth, int left) {
        float bitmapLeft = left;
        if (speed < 0) {
            bitmapLeft = clipBounds.width() - layerWidth - left;
        }
        return bitmapLeft;
    }

    /**
     * Start the animation
     */
    public void start() {
        if (!isStarted) {
            isStarted = true;
            postInvalidateOnAnimation();
        }
    }

    /**
     * Stop the animation
     */
    public void stop() {
        if (isStarted) {
            isStarted = false;
            invalidate();
        }
    }
}
