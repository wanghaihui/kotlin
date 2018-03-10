package com.imagine.story.feed;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.imagine.story.R;
import com.imagine.story.common.base.BaseAdapter;
import com.imagine.story.common.base.BaseViewHolder;
import com.imagine.story.viewholder.RecyclerViewHolder;

/**
 * Created by conquer on 2018/2/3.
 *
 */

public abstract class FeedViewHolderBase extends RecyclerViewHolder<BaseAdapter, BaseViewHolder, Feed> {

    // basic
    protected View view;
    protected Context context;

    // data
    protected Feed feed;

    protected FrameLayout contentContainer;

    // 返回具体消息类型内容展示区域的layout res id
    protected abstract int getContentResId();

    // 在该接口中根据layout对各控件成员变量赋值
    protected abstract void inflateContentView();

    protected abstract void bindContentView(int position);

    public FeedViewHolderBase(BaseAdapter adapter) {
        super(adapter);
    }

    @Override
    public void convert(BaseViewHolder holder, Feed data, int position, boolean isScrolling) {
        view = holder.getConvertView();
        context = holder.getContext();
        feed = data;

        inflate();
        refresh(position);
    }

    protected final void inflate() {
        contentContainer = findViewById(R.id.feed_item_content);

        // 这里只要inflate出来后加入一次即可
        if (contentContainer.getChildCount() == 0) {
            View.inflate(view.getContext(), getContentResId(), contentContainer);
        }
        inflateContentView();
    }

    protected final void refresh(int position) {
        // 绑定内容View
        bindContentView(position);
    }

    // 根据layout id查找对应的控件
    @SuppressWarnings("unchecked")
    protected <T extends View> T findViewById(int id) {
        return (T) view.findViewById(id);
    }

}
