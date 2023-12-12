package com.begenuin.library.common.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.begenuin.library.common.Utility;

public class PullBackLayout extends FrameLayout {

    private final ViewDragHelper dragger;

    private final int minimumFlingVelocity;

    View mVdhView;
    int mVdhXOffset;
    int mVdhYOffset;

    @Nullable
    private Callback callback;

    private boolean isXDragEnable;
    private boolean isDragEnable = true;
    private boolean isReverseDragEnable = false;

    public PullBackLayout(Context context) {
        this(context, null);
    }

    public PullBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullBackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        dragger = ViewDragHelper.create(this, 1f / 8f, new ViewDragCallback());
        minimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
//        initializeTransformer();
    }

    public void setCallback(@Nullable Callback callback) {
        this.callback = callback;
    }

    public void setXDragEnable(boolean isXDragEnable){
        this.isXDragEnable = isXDragEnable;
    }

    public void setReverseDragEnable(boolean isReverseDragEnable){
        this.isReverseDragEnable = isReverseDragEnable;
    }

    public void setIsDragEnable(boolean isDragEnable){
        this.isDragEnable = isDragEnable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragger.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        dragger.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (dragger.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }else {
            if(mVdhView != null) {
                Utility.showLog("Test_top", mVdhView.getTop() + "  " + mVdhView.getBottom());
                if(!isReverseDragEnable) {
                    mVdhXOffset = mVdhView.getLeft();
                    mVdhYOffset = mVdhView.getTop();
                }else{
                    mVdhXOffset = mVdhView.getRight() - getWidth();
                    mVdhYOffset = mVdhView.getBottom() - getHeight();
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(mVdhView != null) {
            // Reapply VDH offsets
            Utility.showLog("Test_top", mVdhYOffset + "");
            mVdhView.offsetLeftAndRight(mVdhXOffset);
            mVdhView.offsetTopAndBottom(mVdhYOffset);
        }
    }

    public interface Callback {

        void onPullStart();

        void onPull(float progress);

        void onPullCancel();

        void onPullComplete();

    }

    /**
     * Initialize Transformer with a scalable or change width/height implementation.
     */
//    private void initializeTransformer() {
//        ResizeTransformer resizeTransformer = new ResizeTransformer(getRootView(), this);
//        resizeTransformer.setViewHeight(getHeight());
//        resizeTransformer.setXScaleFactor(0.5f);
//        resizeTransformer.setYScaleFactor(0.6f);
////        resizeTransformer.setMarginRight(marginRight);
////        resizeTransformer.setMarginBottom(marginBottom);
//    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            mVdhView = child;
            return isDragEnable;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
//            return 0;
            if(isReverseDragEnable){
                return left;
            }else {
                if (isXDragEnable) {
                    return Math.max(0, left);
                } else {
                    return 0;
                }
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if(isReverseDragEnable){
                return top;
            }else {
                return Math.max(0, top);
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
//            return 0;
            if(isXDragEnable) {
                return getWidth();
            }else{
                return 0;
            }
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return getHeight();
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            if (callback != null) {
                callback.onPullStart();
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mVdhXOffset = left;
            mVdhYOffset = top;
            Utility.showLog("Test_top", mVdhYOffset + "");
            if (callback != null) {
                callback.onPull((float) top / (float) getHeight());
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            int slop = yvel > minimumFlingVelocity ? getHeight() / 6 : getHeight() / 3;
            if(isReverseDragEnable){
                if (getHeight() - releasedChild.getBottom() > slop) {
                    if (callback != null) {
                        callback.onPullComplete();
                    }
                }else if (releasedChild.getTop() > slop) {
                    if (callback != null) {
                        callback.onPullComplete();
                    }
                }else {
                    if (callback != null) {
                        callback.onPullCancel();
                    }

                    dragger.settleCapturedViewAt(0, 0);
                    invalidate();
                }
            }else {
                if (releasedChild.getTop() > slop) {
                    if (callback != null) {
                        callback.onPullComplete();
                    }
                } else {
                    if (callback != null) {
                        callback.onPullCancel();
                    }

                    dragger.settleCapturedViewAt(0, 0);
                    invalidate();
                }
            }
        }

    }

}