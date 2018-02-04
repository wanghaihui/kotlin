package com.imagine.story.common.widget;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.imagine.story.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by conquer on 2018/1/16.
 * Read the fucking code.
 */

public class DrawerLayout extends ViewGroup {

    private static final String TAG = "DrawerLayout";

    private static final int[] THEME_ATTRS = {
            R.attr.colorPrimaryDark
    };

    /**
     * Indicates that any drawers are in an idle(空闲), settled state(安定的状态).
     * No animation is in progress(没有动画在进行中).
     */
    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

    /**
     * Indicates that a drawer is currently being dragged by the user.
     */
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

    /**
     * Indicates that a drawer is in the process of settling to a final position(正在安置到最终状态).
     */
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;

    // 用IntDef和StringDef注解代替Enum枚举类型
    // RetentionPolicy.SOURCE--这种类型的Annotations只在源代码级别保留,编译时就会被忽略
    @IntDef({STATE_IDLE, STATE_DRAGGING, STATE_SETTLING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {}

    /**
     * The drawer is unlocked(未锁定).
     */
    public static final int LOCK_MODE_UNLOCKED = 0;

    /**
     * The drawer is locked closed(锁定关闭状态). The user may not open it, though
     * the app may open it programmatically(已编程方式).
     */
    public static final int LOCK_MODE_LOCKED_CLOSED = 1;

    /**
     * The drawer is locked open(锁定打开状态). The user may not close it, though the app
     * may close it programmatically.
     */
    public static final int LOCK_MODE_LOCKED_OPEN = 2;

    /**
     * The drawer's lock state is reset to default(重置到缺省状态--未定义状态).
     */
    public static final int LOCK_MODE_UNDEFINED = 3;

    @IntDef({LOCK_MODE_UNLOCKED, LOCK_MODE_LOCKED_CLOSED, LOCK_MODE_LOCKED_OPEN, LOCK_MODE_UNDEFINED})
    @Retention(RetentionPolicy.SOURCE)
    private @interface LockMode {}

    @IntDef(value = {Gravity.LEFT, Gravity.RIGHT, GravityCompat.START, GravityCompat.END}, flag = true)
    @Retention(RetentionPolicy.SOURCE)
    private @interface EdgeGravity {}

    private static final int MIN_DRAWER_MARGIN = 64; // dp
    // 高出海平面的高度
    private static final int DRAWER_ELEVATION = 10; //dp

    // 缺省的遮罩颜色
    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

    /**
     * Minimum(最小的) velocity(速度) that will be detected(被检测) as a fling(手指触动屏幕后，稍微滑动后立即松开)
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second

    private int mMinDrawerMargin;  // px

    private final ViewDragCallback mLeftCallback;
    private final ViewDragCallback mRightCallback;

    public DrawerLayout(@NonNull Context context) {
        this(context, null);
    }

    public DrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // Drawer只有当其子类控件不需要获取焦点时才获取焦点
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        // dp=dip
        // densityDpi--dpi--单位:像素/英寸--每英寸(1英寸=2.54厘米)距离中有多少个像素点
        // density--其实是 dpi/(160像素/英寸） 后得到的值--density其实是没单位的，就是一个比例值
        final float density = getResources().getDisplayMetrics().density;
        // 0.5f--变量不为0
        // px = dp * density
        mMinDrawerMargin = (int) (MIN_DRAWER_MARGIN * density + 0.5f);

        final float minVel = MIN_FLING_VELOCITY * density;

        // Drag--拖动
        // 初始化拖动帮助类
        mLeftCallback = new ViewDragCallback(Gravity.LEFT);
        mRightCallback = new ViewDragCallback(Gravity.RIGHT);



    }

    // 必须重写这个函数
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }


    private class ViewDragCallback extends ViewDragHelper.Callback {
        private final int mAbsGravity;

        ViewDragCallback(int gravity) {
            mAbsGravity = gravity;
        }

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

        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return true;
        }

        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            return 0;
        }

        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            return 0;
        }
    }
}
