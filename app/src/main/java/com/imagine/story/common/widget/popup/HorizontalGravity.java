package com.imagine.story.common.widget.popup;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by conquer on 2018/3/1.
 *
 */
@IntDef({
    HorizontalGravity.CENTER,
    HorizontalGravity.LEFT,
    HorizontalGravity.RIGHT,
    HorizontalGravity.ALIGN_LEFT,
    HorizontalGravity.ALIGN_RIGHT
})
@Retention(RetentionPolicy.SOURCE)
public @interface HorizontalGravity {
    int CENTER = 0;
    int LEFT = 1;
    int RIGHT = 2;
    int ALIGN_LEFT = 3;
    int ALIGN_RIGHT = 4;
}
