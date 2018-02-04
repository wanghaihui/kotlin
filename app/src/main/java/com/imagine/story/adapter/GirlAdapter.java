package com.imagine.story.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.imagine.story.R;
import com.imagine.story.bean.TestUrl;
import com.imagine.story.common.glide.GlideApp;

import java.util.ArrayList;
import java.util.List;

import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerListener;
import tcking.github.com.giraffeplayer2.VideoView;
import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * Created by conquer on 2018/1/19.
 *
 */

public class GirlAdapter extends RecyclerView.Adapter<GirlAdapter.GirlViewHolder> {

    private Context mContext;
    private List<TestUrl> mDataList = new ArrayList<>();

    public GirlAdapter(Context context) {
        mContext = context;

        // init
        mDataList.add(new TestUrl("http://p2rgaxopf.bkt.clouddn.com/image/story1.jpg",
                "http://image.51meijian.com/upload/cms_video/1801181503225a6046baac428.mp4"));
        mDataList.add(new TestUrl("http://p2rgaxopf.bkt.clouddn.com/image/story2.jpeg",
                "http://image.51meijian.com/upload/cms_video/1801171226285a5ed074de8c9.mp4"));
        mDataList.add(new TestUrl("http://p2rgaxopf.bkt.clouddn.com/image/story3.jpeg",
                "http://image.51meijian.com/upload/cms_video/1801101832025a55eba241242.mp4"));

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public GirlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GirlViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_girl, parent,
                false));
    }

    @Override
    public void onBindViewHolder(GirlViewHolder holder, int position) {
        GlideApp.with(mContext)
                .load(mDataList.get(position).imageUrl)
                .centerCrop()
                .into(holder.girlImage);

        holder.girlVideo
                .setVideoPath(mDataList.get(position).videoUrl)
                .setPlayerListener(new PlayerListener() {
                    @Override
                    public void onPrepared(GiraffePlayer giraffePlayer) {
                        holder.girlImage.setVisibility(View.INVISIBLE);
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

    class GirlViewHolder extends RecyclerView.ViewHolder {

        ImageView girlImage;
        VideoView girlVideo;

        private GirlViewHolder(View view) {
            super(view);
            girlImage = view.findViewById(R.id.girl_image);
            girlVideo = view.findViewById(R.id.girl_video);
        }
    }
}
