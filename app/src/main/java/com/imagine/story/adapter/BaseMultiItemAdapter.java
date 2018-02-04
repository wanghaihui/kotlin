package com.imagine.story.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ViewGroup;

import com.imagine.story.viewholder.BaseViewHolder;
import com.imagine.story.viewholder.RecyclerViewHolder;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by conquer on 2018/2/4.
 *
 */

public abstract class BaseMultiItemAdapter<T, K extends BaseViewHolder> extends BaseAdapter<T, K> {

    /**
     * viewType->layoutResId
     */
    private SparseIntArray layouts;

    /**
     * viewType->view holder class
     */
    private SparseArray<Class<? extends RecyclerViewHolder>> holderClasses;

    /**
     * viewType->view holder instance
     */
    private Map<Integer, Map<String, RecyclerViewHolder>> multiTypeViewHolders;

    /**
     * get view type from data item
     *
     * @param item
     */
    protected abstract int getViewType(T item);

    /**
     * get view holder unique key from data item
     *
     * @param item
     */
    protected abstract String getItemKey(T item);

    /**
     * add viewType->layoutResId, viewType->ViewHolder.class
     *
     * @param type            view type
     * @param layoutResId
     * @param viewHolderClass
     */
    @SuppressLint("UseSparseArrays")
    protected void addItemType(int type, @LayoutRes int layoutResId, Class<? extends RecyclerViewHolder> viewHolderClass) {
        // layouts
        if (layouts == null) {
            layouts = new SparseIntArray();
        }
        layouts.put(type, layoutResId);

        // view holder class
        if (holderClasses == null) {
            holderClasses = new SparseArray<>();
        }
        holderClasses.put(type, viewHolderClass);

        // view holder
        if (multiTypeViewHolders == null) {
            multiTypeViewHolders = new HashMap<>();
        }
        multiTypeViewHolders.put(type, new HashMap<>());
    }

    public BaseMultiItemAdapter(RecyclerView recyclerView, List<T> data) {
        super(recyclerView, 0, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void convert(final K baseHolder, final T item, final int position, boolean isScrolling) {
        final String key = getItemKey(item);
        final int viewType = baseHolder.getItemViewType();

        RecyclerViewHolder h = multiTypeViewHolders.get(viewType).get(key);
        if (h == null) {
            try {
                Class<? extends RecyclerViewHolder> cls = holderClasses.get(viewType);
                // 第一个显式的构造函数
                Constructor c = cls.getDeclaredConstructors()[0];
                c.setAccessible(true);

                h = (RecyclerViewHolder) c.newInstance(this);
                multiTypeViewHolders.get(viewType).put(key, h);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // convert
        if (h != null) {
            h.convert(baseHolder, item, position, isScrolling);
        }
    }

    private int getLayoutId(int viewType) {
        return layouts.get(viewType);
    }

    @Override
    protected int getDefaultItemViewType(int position) {
        return getViewType(mData.get(position));
    }

    @Override
    protected K onCreateDefaultViewHolder(ViewGroup parent, int viewType) {
        return createBaseViewHolder(parent, getLayoutId(viewType));
    }
}
