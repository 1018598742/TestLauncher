package com.android.launcher3;

import android.view.MotionEvent;
import android.view.View;

import com.android.launcher3.StylusEventHelper.StylusButtonListener;

/**
 * Simple listener that performs a long click on the view after a stylus button press.
 * 在按下触控笔按钮后对视图执行长按的简单侦听器。
 */
public class SimpleOnStylusPressListener implements StylusButtonListener {
    private View mView;

    public SimpleOnStylusPressListener(View view) {
        mView = view;
    }

    public boolean onPressed(MotionEvent event) {
        //performLongClick 调用此视图的OnLongClickListener（如果已定义）。如果OnLongClickListener没有使用该事件，则调用*上下文菜单。
        return mView.isLongClickable() && mView.performLongClick();
    }

    public boolean onReleased(MotionEvent event) {
        return false;
    }
}