package com.imagine.story.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.imagine.story.R;
import com.imagine.story.common.base.BaseViewHolder;
import com.imagine.story.feed.Feed;
import com.imagine.story.viewholder.SearchViewHolder;

import java.util.List;

/**
 * Created by conquer on 2018/3/11.
 *
 */

public class HeaderAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private static final int TYPE_HEADER_SEARCH = 1;
    private static final int TYPE_ITEM = 2;

    List<Feed> mData;

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    HeaderAdapter(List<Feed> mData) {
        this.mData = mData;
    }

    public List<Feed> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER_SEARCH;
        }
        return TYPE_ITEM;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        mLayoutInflater = LayoutInflater.from(mContext);

        if (viewType == TYPE_HEADER_SEARCH) {
            return new SearchViewHolder(mLayoutInflater.inflate(R.layout.item_search, parent, false));
        }

        return new SearchViewHolder(mLayoutInflater.inflate(R.layout.item_search, parent, false));
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        int viewType = holder.getItemViewType();
        switch (viewType) {
            // bind view
            default:
                break;
        }
    }
}
