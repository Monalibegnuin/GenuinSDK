package com.begenuin.library.common.customViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.view.ViewCompat;

import com.begenuin.library.common.Utility;
import com.begenuin.library.common.customViews.CustomLeftRightSwipeGesture;
import com.google.android.material.tabs.TabLayout;

public class CenteringTabLayout extends TabLayout {
    private CustomLeftRightSwipeGesture gesture;

    public CenteringTabLayout(Context context) {
        super(context);
    }

    public CenteringTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CenteringTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        View firstTab = ((ViewGroup) getChildAt(0)).getChildAt(0);
        View lastTab = ((ViewGroup) getChildAt(0)).getChildAt(((ViewGroup) getChildAt(0)).getChildCount() - 1);
        ViewCompat.setPaddingRelative(getChildAt(0), (getWidth() / 2) - (firstTab.getWidth() / 2), 0, (getWidth() / 2) - (lastTab.getWidth() / 2), 0);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Utility.printErrorLog("CustomLeftRightSwipeGesture onTouch called");
        gesture.onTouch(this, ev);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(gesture.onTouch(this, event)) {
            return false;
        }
        return super.onInterceptTouchEvent(event);
    }

    public void setGesture(CustomLeftRightSwipeGesture gesture) {
        this.gesture = gesture;
    }
}