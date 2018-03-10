package com.imagine.story.common.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by conquer on 2018/2/3.
 *
 */

public abstract class BaseAdapter<T, K extends BaseViewHolder> extends RecyclerView.Adapter<K> {

    List<T> mData;

    private Context mContext;
    private int mLayoutResId;
    private LayoutInflater mLayoutInflater;

    private boolean isScrolling = false;

    BaseAdapter(RecyclerView recyclerView, int layoutResId, List<T> data) {
        mData = data;

        if (layoutResId != 0) {
            this.mLayoutResId = layoutResId;
        }
    }

    /**
     * Get the data of list
     */
    public List<T> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getDefaultItemViewType(position);
    }

    @Override
    public void onBindViewHolder(K holder, int position) {
        int viewType = holder.getItemViewType();
        switch (viewType) {
            default:
                convert(holder, mData.get(position), position, isScrolling);
                break;
        }
    }

    @Override
    public K onCreateViewHolder(ViewGroup parent, int viewType) {
        K baseViewHolder;
        mContext = parent.getContext();
        mLayoutInflater = LayoutInflater.from(mContext);
        baseViewHolder = onCreateDefaultViewHolder(parent, viewType);
        return baseViewHolder;
    }

    protected int getDefaultItemViewType(int position) {
        return super.getItemViewType(position);
    }

    protected K onCreateDefaultViewHolder(ViewGroup parent, int viewType) {
        return createBaseViewHolder(parent, mLayoutResId);
    }

    protected K createBaseViewHolder(ViewGroup parent, int layoutResId) {
        return createBaseViewHolder(getItemView(layoutResId, parent));
    }

    @SuppressWarnings("unchecked")
    private K createBaseViewHolder(View view) {
        return (K) new BaseViewHolder(view);
    }

    private View getItemView(int layoutResId, ViewGroup parent) {
        return mLayoutInflater.inflate(layoutResId, parent, false);
    }


    protected abstract void convert(K holder, T item, int position, boolean isScrolling);
}
