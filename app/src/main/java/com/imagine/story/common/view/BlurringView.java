package com.imagine.story.common.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.View;

import com.imagine.story.R;


/**
 * Created by conquer on 2018/1/16.
 *
 * A custom view for presenting a dynamically blurred version of another view's content.
 * <p/>
 * Use {@link #setBlurredView(android.view.View)} to set up the reference to the view to be blurred.
 * After that, call {@link #invalidate()} to trigger blurring whenever necessary.
 */

public class BlurringView extends View {

    public BlurringView(Context context) {
        this(context, null);
    }

    public BlurringView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 8个单位的缩放采样以及15个单位的模糊半径就能很好地呈现我们想要的效果
        final Resources resources = getResources();
        // 缺省的模糊半径
        final int defaultBlurRadius = resources.getInteger(R.integer.default_blur_radius);
        // 缺省的降采样因子
        final int defaultDownSampleFactor = resources.getInteger(R.integer.default_down_sample_factor);
        // 缺省的覆盖颜色
        final int defaultOverlayColor = ActivityCompat.getColor(context, R.color.default_overlay_color);

        initializeRenderScript(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurringView);
        setBlurRadius(a.getInt(R.styleable.BlurringView_blurRadius, defaultBlurRadius));
        setDownSampleFactor(a.getInt(R.styleable.BlurringView_downSampleFactor, defaultDownSampleFactor));
        setOverlayColor(a.getColor(R.styleable.BlurringView_overlayColor, defaultOverlayColor));
        a.recycle();
    }

    /**
     * 模糊视图拥有一份被模糊视图的引用
     * @param blurredView
     */
    public void setBlurredView(View blurredView) {
        mBlurredView = blurredView;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBlurredView != null) {
            if (prepare()) {
                // If the background of the blurred view is a color drawable, we use it to clear
                // the blurring canvas, which ensures that edges of the child views are blurred
                // as well; otherwise we clear the blurring canvas with a transparent color.
                if (mBlurredView.getBackground() != null && mBlurredView.getBackground() instanceof ColorDrawable) {
                    mBitmapToBlur.eraseColor(((ColorDrawable) mBlurredView.getBackground()).getColor());
                } else {
                    mBitmapToBlur.eraseColor(Color.TRANSPARENT);
                }

                // 让被模糊视图的draw()方法在私有的画布上绘制
                mBlurredView.draw(mBlurringCanvas);

                // 模糊私有画布的位图并传递给mBlurredBitmap
                blur();

                canvas.save();
                canvas.translate(mBlurredView.getX() - getX(), mBlurredView.getY() - getY());
                canvas.scale(mDownSampleFactor, mDownSampleFactor);
                canvas.drawBitmap(mBlurredBitmap, 0, 0, null);
                canvas.restore();

                canvas.drawColor(mOverlayColor);
            }
        }
    }

    private void initializeRenderScript(Context context) {
        mRenderScript = RenderScript.create(context);
        mBlurScript = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
    }

    public void setBlurRadius(int radius) {
        mBlurScript.setRadius(radius);
    }

    public void setDownSampleFactor(int factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Down sample factor must be greater than 0.");
        }

        if (mDownSampleFactor != factor) {
            mDownSampleFactor = factor;
            mDownSampleFactorChanged = true;
        }
    }

    public void setOverlayColor(int color) {
        mOverlayColor = color;
    }

    protected boolean prepare() {
        final int width = mBlurredView.getWidth();
        final int height = mBlurredView.getHeight();

        if (mBlurringCanvas == null || mDownSampleFactorChanged ||
                mBlurredViewWidth != width || mBlurredViewHeight != height) {
            mDownSampleFactorChanged = false;

            mBlurredViewWidth = width;
            mBlurredViewHeight = height;

            int scaledWidth = width / mDownSampleFactor;
            int scaledHeight = height / mDownSampleFactor;

            // 在模糊位图的边缘处我们遇到了一些RenderScript的历史遗留问题,为了应对这个问题,我们对宽度和高度缩放到近似4倍
            // The following manipulation is to avoid some RenderScript artifacts at the edge.
            scaledWidth = scaledWidth - scaledWidth % 4 + 4;
            scaledHeight = scaledHeight - scaledHeight % 4 + 4;

            if (mBlurredBitmap == null || mBlurredBitmap.getWidth() != scaledWidth ||
                    mBlurredBitmap.getHeight() != scaledHeight) {
                mBitmapToBlur = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
                if (mBitmapToBlur == null) {
                    return false;
                }

                mBlurredBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
                if (mBlurredBitmap == null) {
                    return false;
                }

                mBlurringCanvas = new Canvas(mBitmapToBlur);
                mBlurringCanvas.scale(1f / mDownSampleFactor, 1f / mDownSampleFactor);

                mBlurInput = Allocation.createFromBitmap(mRenderScript, mBitmapToBlur,
                        Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
                mBlurOutput = Allocation.createTyped(mRenderScript, mBlurInput.getType());
            }
        }

        return true;
    }

    protected void blur() {
        mBlurInput.copyFrom(mBitmapToBlur);
        mBlurScript.setInput(mBlurInput);
        mBlurScript.forEach(mBlurOutput);
        mBlurOutput.copyTo(mBlurredBitmap);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mRenderScript != null) {
            mRenderScript.destroy();
        }
    }

    private int mDownSampleFactor;
    private int mOverlayColor;

    private View mBlurredView;
    private int mBlurredViewWidth, mBlurredViewHeight;

    private boolean mDownSampleFactorChanged;

    // 模糊画布
    private Canvas mBlurringCanvas;
    private Bitmap mBitmapToBlur;
    private Bitmap mBlurredBitmap;

    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mBlurScript;
    private Allocation mBlurInput;
    private Allocation mBlurOutput;
}
