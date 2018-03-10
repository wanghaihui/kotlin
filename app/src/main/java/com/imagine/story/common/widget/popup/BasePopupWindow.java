package com.imagine.story.common.widget.popup;

import android.content.Context;
import android.view.View;

/**
 * Created by conquer on 2018/3/1.
 * 基类 for 自定义
 */

public abstract class BasePopupWindow extends EasyPopupWindow {

    protected BasePopupWindow(Context context) {
        super(context);
    }

    @Override
    public void onPopupWindowCreated() {
        super.onPopupWindowCreated();
        initAttributes();
    }

    @Override
    public void onPopupWindowViewCreated(View contentView) {
        initViews(contentView);
    }

    @Override
    public void onPopupWindowDismiss() {

    }

    /**
     * 可以在此方法中设置PopupWindow需要的属性
     */
    protected abstract void initAttributes();

    /**
     * 初始化view {@see getView()}
     */
    protected abstract void initViews(View view);
}
