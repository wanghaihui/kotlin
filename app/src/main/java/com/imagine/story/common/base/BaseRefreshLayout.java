package com.imagine.story.common.base;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;

import com.imagine.story.R;

/**
 * Created by conquer on 2018/3/10.
 *
 */

public class BaseRefreshLayout extends SwipeRefreshLayout implements SwipeRefreshLayout.OnRefreshListener {

    private BaseRecyclerView.ILoadListener mLoadListener;
    private BaseRecyclerView mBaseRecyclerView;

    public BaseRefreshLayout(Context context) {
        super(context);
    }

    public BaseRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        View.inflate(context, R.layout.view_base_recycler_view, this);
        mBaseRecyclerView = findViewById(R.id.recycler_view);
        setOnRefreshListener(this);

        setColorSchemeColors(getResources().getColor(R.color.orange),
                getResources().getColor(R.color.green),
                getResources().getColor(R.color.blue));
    }

    @Override
    public void onRefresh() {
        if (mLoadListener != null) {
            mLoadListener.onRefresh();
        }
    }

    public void setLoadListener(BaseRecyclerView.ILoadListener loadListener) {
        mLoadListener = loadListener;
        mBaseRecyclerView.setLoadListener(mLoadListener);
    }
}
