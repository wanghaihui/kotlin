package com.imagine.story.common.widget;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by conquer on 2018/1/17.
 * Read the fucking code.
 *
 * ViewDragHelper is a utility class(工具类) for writing custom ViewGroups(写自定义ViewGroups). It offers a number
 * of useful operations and state tracking(状态追踪) for allowing a user to drag(拖动) and reposition(改变位置)
 * views within their parent ViewGroup.
 *
 */

public class ViewDragHelper {

    /**
     * A null/invalid pointer ID.
     */
    public static final int INVALID_POINTER = -1;

    public static final int STATE_IDLE = 0;

    public static final int STATE_DRAGGING = 1;

    public static final int STATE_SETTLING = 2;

    public abstract static class Callback {

        public void onViewDragStateChanged(int state) {

        }

        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {

        }

        public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {

        }

        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {

        }

        public void onEdgeTouched(int edgeFlags, int pointerId) {

        }

        public boolean onEdgeLock(int edgeFlags) {
            return false;
        }

        public void onEdgeDragStarted(int edgeFlags, int pointerId) {

        }

        public int getOrderedChildIndex(int index) {
            return index;
        }

        public int getViewHorizontalDragRange(@NonNull View child) {
            return 0;
        }

        public int getViewVerticalDragRange(@NonNull View child) {
            return 0;
        }

        public abstract boolean tryCaptureView(@NonNull View child, int pointerId);

        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            return 0;
        }

        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            return 0;
        }
    }
}