package com.imagine.story.common.widget;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.bumptech.glide.request.RequestOptions;
import com.imagine.story.R;
import com.imagine.story.common.glide.BlurTransformation;
import com.imagine.story.common.glide.GlideApp;
import com.imagine.story.common.widget.popup.BasePopupWindow;

/**
 * Created by conquer on 2018/3/1.
 *
 */

public class MatchPopupWindow extends BasePopupWindow {
    private static final String TAG = MatchPopupWindow.class.getSimpleName();
    private ImageView matchLoading;
    private String bgUrl;
    private String avatarUrl;

    public MatchPopupWindow(Context context) {
        super(context);
    }

    @Override
    protected void initAttributes() {
        setContentView(R.layout.layout_match, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusAndOutsideEnable(false);
        setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // dismiss的时候，停止动画
                if (matchLoading != null) {
                    Log.d(TAG, "end animation");
                    matchLoading.clearAnimation();
                }
            }
        });
    }

    @Override
    protected void initViews(View view) {
        ImageView matchBg = view.findViewById(R.id.matchBg);
        ImageView avatar = view.findViewById(R.id.avatar);
        matchLoading = view.findViewById(R.id.matchLoading);

        GlideApp.with(getContext())
                .load(bgUrl)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(20, 12)))
                .into(matchBg);

        GlideApp.with(getContext())
                .load(avatarUrl)
                .circleCrop()
                .into(avatar);

        Animation circle_anim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_rotate);
        LinearInterpolator interpolator = new LinearInterpolator();
        circle_anim.setInterpolator(interpolator);
        matchLoading.startAnimation(circle_anim);
    }

    public void setBgUrl(String bgUrl) {
        this.bgUrl = bgUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

}
