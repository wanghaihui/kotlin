package com.imagine.story.fragment;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.imagine.story.R;
import com.imagine.story.bean.TestUrl;
import com.imagine.story.common.glide.GlideApp;
import com.imagine.story.common.widget.FlingLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerListener;
import tcking.github.com.giraffeplayer2.VideoInfo;
import tcking.github.com.giraffeplayer2.VideoView;
import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * Created by conquer on 2018/1/19.
 */

public class GirlFragment extends Fragment implements FlingLayout.OnScrollListener {

    private Unbinder unbinder;

    @BindView(R.id.girl_video)
    VideoView girlVideo;

    @BindView(R.id.girl_image)
    ImageView girlImage;

    @BindView(R.id.girl_layout)
    FlingLayout girlLayout;

    private List<TestUrl> mDataList = new ArrayList<>();
    private int position = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_girl, container, false);
        unbinder = ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        girlLayout.setOnScrollListener(this);
        initData();

        initVideoInfo();
        loadImage(mDataList.get(position).imageUrl);
        loadVideo(mDataList.get(position).videoUrl);
    }



    @Override
    public void onPause() {
        super.onPause();
        if (girlVideo.getPlayer().canPause()) {
            girlVideo.getPlayer().pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void initData() {
        mDataList.add(new TestUrl("http://p2rgaxopf.bkt.clouddn.com/image/story1.jpg",
                "http://image.51meijian.com/upload/cms_video/1801181503225a6046baac428.mp4"));
        mDataList.add(new TestUrl("http://p2rgaxopf.bkt.clouddn.com/image/story2.jpeg",
                "http://image.51meijian.com/upload/cms_video/1801171226285a5ed074de8c9.mp4"));
        mDataList.add(new TestUrl("http://p2rgaxopf.bkt.clouddn.com/image/story3.jpeg",
                "http://image.51meijian.com/upload/cms_video/1801101832025a55eba241242.mp4"));
    }

    private void initVideoInfo()  {
        girlVideo.getVideoInfo()
                .setAspectRatio(VideoInfo.AR_MATCH_PARENT)
                .setShowTopBar(false)
                .setPortraitWhenFullScreen(true);
    }

    private void loadImage(String url) {
        girlImage.setVisibility(View.VISIBLE);

        GlideApp.with(this)
                .load(url)
                .centerCrop()
                .into(girlImage);
    }

    private void loadVideo(String url) {
        girlVideo.setVideoPath(url)
                .setPlayerListener(new PlayerListener() {
                    @Override
                    public void onPrepared(GiraffePlayer giraffePlayer) {
                        ObjectAnimator animator = ObjectAnimator.ofFloat(girlImage, "alpha", 1f, 0.7f);
                        animator.setDuration(500);
                        animator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (isAdded()) {
                                    girlImage.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }
                        });
                        animator.start();
                    }

                    @Override
                    public void onBufferingUpdate(GiraffePlayer giraffePlayer, int percent) {

                    }

                    @Override
                    public boolean onInfo(GiraffePlayer giraffePlayer, int what, int extra) {
                        return false;
                    }

                    @Override
                    public void onCompletion(GiraffePlayer giraffePlayer) {
                        giraffePlayer.start();
                    }

                    @Override
                    public void onSeekComplete(GiraffePlayer giraffePlayer) {

                    }

                    @Override
                    public boolean onError(GiraffePlayer giraffePlayer, int what, int extra) {
                        return false;
                    }

                    @Override
                    public void onPause(GiraffePlayer giraffePlayer) {

                    }

                    @Override
                    public void onRelease(GiraffePlayer giraffePlayer) {

                    }

                    @Override
                    public void onStart(GiraffePlayer giraffePlayer) {

                    }

                    @Override
                    public void onTargetStateChange(int oldState, int newState) {

                    }

                    @Override
                    public void onCurrentStateChange(int oldState, int newState) {

                    }

                    @Override
                    public void onDisplayModelChange(int oldModel, int newModel) {

                    }

                    @Override
                    public void onPreparing(GiraffePlayer giraffePlayer) {

                    }

                    @Override
                    public void onTimedText(GiraffePlayer giraffePlayer, IjkTimedText text) {

                    }
                })
                .getPlayer()
                .start();
    }

    @Override
    public void onScrollUp() {
        position--;
        loadImage(mDataList.get(position).imageUrl);
        loadVideo(mDataList.get(position).videoUrl);
    }

    @Override
    public void onScrollDown() {
        position++;
        loadImage(mDataList.get(position).imageUrl);
        loadVideo(mDataList.get(position).videoUrl);

    }
}
