package com.imagine.story.feed;

import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;

import com.imagine.story.R;
import com.imagine.story.common.base.BaseMultiItemAdapter;
import com.imagine.story.common.base.BaseViewHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by conquer on 2018/2/4.
 *
 */

public class FeedAdapter extends BaseMultiItemAdapter<Feed, BaseViewHolder> {

    private Map<Class<? extends FeedViewHolderBase>, Integer> holderViewType;

    private SparseIntArray heightCache = new SparseIntArray();

    public FeedAdapter(RecyclerView recyclerView, List<Feed> data) {
        super(recyclerView, data);

        // view type, view holder
        holderViewType = new HashMap<>();
        List<Class<? extends FeedViewHolderBase>> holders = FeedViewHolderFactory.getAllViewHolders();
        int viewType = 0;
        for (Class<? extends FeedViewHolderBase> holder : holders) {
            viewType++;
            addItemType(viewType, R.layout.feed_item, holder);
            holderViewType.put(holder, viewType);
        }
    }

    @Override
    protected int getViewType(Feed feed) {
        return holderViewType.get(FeedViewHolderFactory.getViewHolderByType(feed));
    }

    @Override
    protected String getItemKey(Feed feed) {
        return feed.getFeedId();
    }

    public SparseIntArray getHeightCache() {
        return heightCache;
    }
}
