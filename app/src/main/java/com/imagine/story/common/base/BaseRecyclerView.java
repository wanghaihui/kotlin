package com.imagine.story.common.base;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

/**
 * Created by conquer on 2018/3/10.
 *
 */

public class BaseRecyclerView extends RecyclerView {

    private LAYOUT_MANAGER_TYPE mLayoutManagerType;
    public ILoadListener mLoadListener;

    private int lastVisibleItemPosition;
    private int[] lastPositions;

    public BaseRecyclerView(Context context) {
        super(context);
    }

    public BaseRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);

        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (mLayoutManagerType == null) {
            if (layoutManager instanceof GridLayoutManager) {
                mLayoutManagerType = LAYOUT_MANAGER_TYPE.GRID;
            } else if (layoutManager instanceof LinearLayoutManager) {
                mLayoutManagerType = LAYOUT_MANAGER_TYPE.LINEAR;
            }  else if (layoutManager instanceof StaggeredGridLayoutManager) {
                mLayoutManagerType = LAYOUT_MANAGER_TYPE.STAGGERED_GRID;
            }
        }

        if (mLayoutManagerType != null) {
            switch (mLayoutManagerType) {
                case LINEAR:
                    lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    break;
                case GRID:
                    lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                    break;
                case STAGGERED_GRID:
                    StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                    if (lastPositions == null) {
                        lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                    }
                    staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                    lastVisibleItemPosition = findMax(lastPositions);
                    break;
            }
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);

        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        // -1是为了提前预加载，优化体验
        if (visibleItemCount > 0 && state == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItemPosition >= totalItemCount - 1) {
            if (mLoadListener != null) {
                mLoadListener.onLoadMore();
            }
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public void setLoadListener(ILoadListener loadListener) {
        this.mLoadListener = loadListener;
    }

    private enum LAYOUT_MANAGER_TYPE {
        LINEAR,
        GRID,
        STAGGERED_GRID
    }

    public interface ILoadListener {
        void onRefresh();
        void onLoadMore();
    }
}
