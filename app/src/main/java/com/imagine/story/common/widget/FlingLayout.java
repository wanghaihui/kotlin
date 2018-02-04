package com.imagine.story.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by conquer on 2018/1/19.
 */

public class FlingLayout extends FrameLayout {

    private static final float FLIP_DISTANCE = 96; // dp

    private float downY;

    private OnScrollListener mScrollListener;

    public FlingLayout(Context context) {
        super(context);
    }

    public FlingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            // 当手指按下的时候
            downY = event.getY();
            performClick();
        }

        if(event.getAction() == MotionEvent.ACTION_UP) {
            // 当手指离开的时候
            float upY = event.getY();
            if(downY - upY > FLIP_DISTANCE) {
                // 上滑
                if (mScrollListener != null) {
                    mScrollListener.onScrollUp();
                }
            } else if(upY - downY > FLIP_DISTANCE) {
                // 下滑
                if (mScrollListener != null) {
                    mScrollListener.onScrollDown();
                }
            }
        }

        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mScrollListener = onScrollListener;
    }

    public interface OnScrollListener {
        void onScrollUp();
        void onScrollDown();
    }

}
