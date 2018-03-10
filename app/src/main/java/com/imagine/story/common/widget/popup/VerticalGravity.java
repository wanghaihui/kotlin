package com.imagine.story.common.widget.popup;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by conquer on 2018/3/1.
 *
 */
@IntDef({
    VerticalGravity.CENTER,
    VerticalGravity.ABOVE,
    VerticalGravity.BELOW,
    VerticalGravity.ALIGN_TOP,
    VerticalGravity.ALIGN_BOTTOM
})
@Retention(RetentionPolicy.SOURCE)
public @interface VerticalGravity {
    int CENTER = 0;
    int ABOVE = 1;
    int BELOW = 2;
    int ALIGN_TOP = 3;
    int ALIGN_BOTTOM = 4;
}
