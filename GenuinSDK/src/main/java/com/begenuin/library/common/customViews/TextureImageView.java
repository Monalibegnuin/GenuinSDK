package com.begenuin.library.common.customViews;


import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.begenuin.library.common.Utility;

public class TextureImageView extends AppCompatImageView {

    public int imageWidth = 0;
    public int imageHeight = 0;
    private boolean isLandScape = false;

    public TextureImageView(@NonNull Context context) {
        super(context);
    }

    public TextureImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextureImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
//        recomputeImgMatrix();
        reassignScaleType();
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
//        recomputeImgMatrix();
        reassignScaleType();
        return super.setFrame(l, t, r, b);
    }

    private void init() {
//        setScaleType(ScaleType.MATRIX);
        setScaleType(ScaleType.CENTER_CROP);
    }

    public void setImageHeightWidth(int width, int height) {
        this.imageHeight = height;
        this.imageWidth = width;
    }

    public void setLandScapeMode(boolean isLandScape) {
        this.isLandScape = isLandScape;
    }

    private void reassignScaleType() {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        final int drawableWidth;
        final int drawableHeight;

        if (imageWidth > 0 && imageHeight > 0) {
            drawableWidth = imageWidth;
            drawableHeight = imageHeight;
        } else {
            drawableWidth = drawable.getMinimumWidth();
            drawableHeight = drawable.getMinimumHeight();
        }

        if (isLandScape) {
            setScaleType(ScaleType.FIT_CENTER);
//            if (drawableHeight > drawableWidth) {
//                setScaleType(ScaleType.FIT_CENTER);
//            } else {
//                setScaleType(ScaleType.CENTER_CROP);
//            }
        } else {
            if (drawableWidth > drawableHeight) {
                setScaleType(ScaleType.FIT_CENTER);
            } else {
                setScaleType(ScaleType.CENTER_CROP);
            }
        }
    }

    private void recomputeImgMatrix() {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        final int drawableWidth;
        final int drawableHeight;

        if (imageWidth > 0 && imageHeight > 0) {
            drawableWidth = imageWidth;
            drawableHeight = imageHeight;
        } else {
            drawableWidth = drawable.getIntrinsicWidth();
            drawableHeight = drawable.getIntrinsicHeight();
        }
        if (drawableWidth > drawableHeight) {
            adjustAspectRatioOriginal(drawableWidth, drawableHeight);
        } else {
            adjustAspectRatio(drawableWidth, drawableHeight);
        }
    }

    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight < (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Utility.showLog("TAG", "Image=" + videoWidth + "x" + videoHeight +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff);

        Matrix txform = getImageMatrix();
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoff, yoff);
        setImageMatrix(txform);
    }

    private void adjustAspectRatioOriginal(int videoWidth, int videoHeight) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Utility.showLog("TAG", "video=" + videoWidth + "x" + videoHeight +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff);


        Matrix txform = getImageMatrix();
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoff, yoff);
        setImageMatrix(txform);
    }
}
