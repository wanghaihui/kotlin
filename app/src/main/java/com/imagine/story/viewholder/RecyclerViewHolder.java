package com.imagine.story.viewholder;

import android.support.v7.widget.RecyclerView;

/**
 * Created by conquer on 2018/2/4.
 *
 */

public abstract class RecyclerViewHolder<T extends RecyclerView.Adapter, V extends BaseViewHolder, K> {
    private final T adapter;

    public RecyclerViewHolder(T adapter) {
        this.adapter = adapter;
    }

    public T getAdapter() {
        return adapter;
    }

    public abstract void convert(V holder, K data, int position, boolean isScrolling);

}
