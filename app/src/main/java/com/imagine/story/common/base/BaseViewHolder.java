package com.imagine.story.common.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by conquer on 2018/2/3.
 *
 */

public class BaseViewHolder extends RecyclerView.ViewHolder {

    public View convertView;

    public BaseViewHolder(View view) {
        super(view);
        convertView = view;
    }

    public View getConvertView() {
        return convertView;
    }

    public Context getContext() {
        if (convertView == null) {
            return null;
        }

        return convertView.getContext();
    }

}
