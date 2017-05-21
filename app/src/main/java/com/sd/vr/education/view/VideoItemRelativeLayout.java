package com.sd.vr.education.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.sd.vr.R;

/**
 * 每个item的布局文件
 * Created by HL on 2017/5/8.
 */

public class VideoItemRelativeLayout extends RelativeLayout {


    public VideoItemRelativeLayout(Context context) {
        super(context);
    }

    public VideoItemRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoItemRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            this.setBackgroundResource(R.drawable.vr_09);
        } else {
            this.setBackgroundResource(R.drawable.vr_10);
        }
    }
}
