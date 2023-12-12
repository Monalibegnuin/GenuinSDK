package com.begenuin.library.common.customViews.draggableview;

import static com.google.android.material.animation.AnimationUtils.LINEAR_INTERPOLATOR;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.FloatProperty;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.begenuin.begenuin.ui.customview.draggableview.DraggableBaseCustomView;
import com.begenuin.library.common.ImageUtils;
import com.begenuin.library.common.Utility;
import com.begenuin.library.core.enums.LayerType;
import com.begenuin.library.core.interfaces.OnMatrixChangeListener;

import java.util.ArrayList;

public class Layer implements OnMatrixChangeListener {
    Matrix matrix = new Matrix();
    Matrix inverse = new Matrix();
    public RectF bounds;
    DraggableLayers parent;
    public Bitmap bitmap;
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private ArrayList<Integer> mDelays = new ArrayList<>();
    Context context;
    MatrixGestureDetector mgd;
    float touchX = 0f, touchY = 0f;
    public boolean isOverlapped = false;
    public int viewId;
    public DraggableBaseCustomView draggableTextView;
    public float[] translatedXY = {0, 0};
    public double scaleFactor = 1;
    public double angle;
    private boolean isClickHandled = false;
    public boolean isVisible = true;
    public boolean isDeleteAnim = false;
    private float mScale = 1.0f;
    private float deleteCenterX, deleteCenterY;
    private final int videoContainerLeft;
    private final int videoContainerTop;
    public final LayerType layerType;
    public float originalHeight, originalWidth;


    public Layer(Activity context, DraggableLayers p, Bitmap b, int viewId, DraggableBaseCustomView draggableTextView, int videoContainerLeft, int videoContainerTop, LayerType layerType) {
        this.layerType = layerType;
        parent = p;
        bitmap = b;
        this.viewId = viewId;
        this.context = context;
        this.draggableTextView = draggableTextView;
        this.videoContainerLeft = videoContainerLeft;
        this.videoContainerTop = videoContainerTop;
        bounds = new RectF(0, 0, b.getWidth(), b.getHeight());
        if (layerType == LayerType.IMAGE) {
            int[] screen = new int[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                screen = Utility.getScreenWidthHeight(context);
            }
            int width = screen[0] - videoContainerLeft;
            int height = screen[1] - videoContainerTop - getStatusBarHeight();
            float dx = (float) ((width - b.getWidth()) / 2);
            float dy = (float) ((height - b.getHeight()) / 2);
            matrix.postTranslate(dx, dy);
        }
        mgd = new MatrixGestureDetector(context, matrix, this);
    }

    public Layer(Activity context, DraggableLayers p, ArrayList<Bitmap> b, ArrayList<Integer> mDelays, int viewId, DraggableBaseCustomView draggableTextView, int videoContainerLeft, int videoContainerTop) {
        layerType = LayerType.GIF;
        parent = p;
        bitmap = b.get(0);
        bitmaps = b;
        this.mDelays = mDelays;
        this.viewId = viewId;
        this.context = context;
        this.draggableTextView = draggableTextView;
        this.videoContainerLeft = videoContainerLeft;
        this.videoContainerTop = videoContainerTop;
        int[] screen = new int[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            screen = Utility.getScreenWidthHeight(context);
        }
        int width = screen[0] - videoContainerLeft;
        int height = screen[1] - videoContainerTop - getStatusBarHeight();
        bounds = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        float dx = (float) ((width - bitmap.getWidth()) / 2);
        float dy = (float) ((height - bitmap.getHeight()) / 2);
        matrix.postTranslate(dx, dy);
        mgd = new MatrixGestureDetector(context, matrix, this);
        playGif();
    }

    public void layerTranslateToBelowQuestion(Activity context) {
        int[] screen = new int[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            screen = Utility.getScreenWidthHeight(context);
        }
        int width = screen[0] - videoContainerLeft;
        int height = screen[1] - videoContainerTop;
        float dx = (float) ((width - bitmap.getWidth()) / 2);
        float dy = (float) ((height + (height * 0.33) - getStatusBarHeight() - bitmap.getHeight()) / 2 + Utility.dpToPx(16, context));
        matrix = new Matrix();
        matrix.postTranslate(dx, dy);
        mgd = new MatrixGestureDetector(context, matrix, this);
    }

    public void layerTranslateToTranscribe(Activity context, int width, int y) {
        int[] screen = new int[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            screen = Utility.getScreenWidthHeight(context);
        }
        int diff = (int) (screen[0] - width - videoContainerLeft - Utility.dpToPx(8, context));
        float dx = (float) ((width - bitmap.getWidth()) / 2) + diff;
        float dy = y + videoContainerTop;
        matrix = new Matrix();
        matrix.postTranslate(dx, dy);
        mgd = new MatrixGestureDetector(context, matrix, this);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void saveScreenshot() {
        float resizeFactor = Math.max(parent.widthFactor, parent.heightFactor);
        Bitmap bMapImage = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bMapImage,
                (int) (bMapImage.getWidth() * resizeFactor),
                (int) (bMapImage.getHeight() * resizeFactor),
                true);
        String path = ImageUtils.saveStickerAsImage(context, resizedBitmap, viewId);
        bMapImage.recycle();
        resizedBitmap.recycle();
        if (parent.draggableLayerInterface != null) {
            parent.draggableLayerInterface.onCaptureImage(path, this);
        }
    }

    public boolean contains(MotionEvent event) {
        matrix.invert(inverse);
        float[] pts = {event.getX(), event.getY()};
        inverse.mapPoints(pts);
//        if (!bounds.contains(pts[0], pts[1])) {
//            return false;
//        }
//        return Color.alpha(bitmap.getPixel((int) pts[0], (int) pts[1])) != 0;
        return bounds.contains(pts[0], pts[1]);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event != null) {
            touchX = event.getRawX();
            touchY = event.getRawY();
            mgd.onTouchEvent(event);
        }
        return true;
    }

    @Override
    public void onChange(Matrix matrix) {
        parent.invalidate();
        if (parent.draggableLayerInterface != null) {
            parent.draggableLayerInterface.onTouchMove(touchX, touchY, this);
        }
    }

    @Override
    public void onTouchDown() {
        isClickHandled = false;
        if (parent.draggableLayerInterface != null) {
            parent.draggableLayerInterface.onTouchDown();
        }
    }

    @Override
    public void onTouchRelease() {
        if (!isClickHandled) {
            Utility.showLog("Touch", "Release");
            if (parent.draggableLayerInterface != null) {
                parent.draggableLayerInterface.onTouchRelease(touchX, touchY, this);
            }
            convertToImage();
        }
    }

    @Override
    public void onTouchClick() {
        isClickHandled = true;
        if (parent.draggableLayerInterface != null) {
            parent.draggableLayerInterface.onTouchClick(this);
        }
    }

    private Bitmap mTmpBitmap;

    public void draw(Canvas canvas) {
        if (isVisible) {
            if (isDeleteAnim) {
                canvas.scale(mScale, mScale, deleteCenterX, deleteCenterY);
            }
            if (isOverlapped) {
                Paint paint = new Paint();
                paint.setAlpha(125);
                if (layerType == LayerType.GIF) {
                    if (mTmpBitmap != null && !mTmpBitmap.isRecycled()) {
                        canvas.drawBitmap(mTmpBitmap, matrix, paint);
                    }
                } else {
                    canvas.drawBitmap(bitmap, matrix, paint);
                }
            } else {
                if (layerType == LayerType.GIF) {
                    if (mTmpBitmap != null && !mTmpBitmap.isRecycled()) {
                        canvas.drawBitmap(mTmpBitmap, matrix, null);
                    }
                } else {
                    canvas.drawBitmap(bitmap, matrix, null);
                }
            }
        }
    }

    final Handler mHandler = new Handler(Looper.getMainLooper());

    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            if (mTmpBitmap != null && !mTmpBitmap.isRecycled()) {
                reDraw();
            }
        }
    };

    private void playGif() {
        new Thread(() -> {
            final int n = bitmaps.size();
            final int ntimes = Integer.MAX_VALUE;
            int repetitionCounter = 0;
            do {
                for (int i = 0; i < n; i++) {
                    mTmpBitmap = bitmaps.get(i);
                    int t = mDelays.get(i);
                    mHandler.post(mUpdateResults);
                    try {
                        Thread.sleep(t);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                repetitionCounter++;
            } while (repetitionCounter <= ntimes);
        }).start();
    }

    public void setDeleteCenter(float deleteCenterX, float deleteCenterY) {
        this.deleteCenterX = deleteCenterX;
        this.deleteCenterY = deleteCenterY;
    }

    private static final float ORIGIN_SCALE = 1.0f;
    private static final float DEFAULT_SCALE_DOWN = 0.1f;

    @SuppressLint("RestrictedApi")
    public void doScaleAnimation() {
        AnimatorSet mDownScaleAnimatorSet = new AnimatorSet();
        final ObjectAnimator scale = ObjectAnimator.ofFloat(this, SCALE, ORIGIN_SCALE, DEFAULT_SCALE_DOWN);
        scale.setAutoCancel(true);
        mDownScaleAnimatorSet.setInterpolator(LINEAR_INTERPOLATOR);
        mDownScaleAnimatorSet.setDuration(300);
        mDownScaleAnimatorSet.play(scale);
        mDownScaleAnimatorSet.start();
        mDownScaleAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isDeleteAnim = false;
                if (parent.draggableLayerInterface != null) {
                    parent.draggableLayerInterface.onDeleteSticker(Layer.this);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private static final FloatProperty<Layer> SCALE = new FloatProperty<Layer>("scaleY") {
        @Override
        public void setValue(Layer object, float value) {
            object.mScale = value;
            object.reDraw();
        }

        @Override
        public Float get(Layer object) {
            return object.mScale;
        }
    };

    public void reDraw() {
        parent.invalidate();
    }

    public void convertToImage() {
        setTranslatedXY();
        saveScreenshot();
    }

    public void setTranslatedXY() {
        float[] values = new float[9];
        matrix.getValues(values);
//        double bottomLeftX = values[Matrix.MTRANS_X] - parent.videoContainerLeft;
//        double bottomLeftY = values[Matrix.MTRANS_Y] - parent.videoContainerTop;

        double bottomLeftX = values[Matrix.MTRANS_X];
        double bottomLeftY = values[Matrix.MTRANS_Y];

        scaleFactor = Math.sqrt((Math.pow(values[Matrix.MSCALE_X], 2) + Math.pow(values[Matrix.MSKEW_Y], 2)));

        originalHeight = (float) (scaleFactor * bounds.height() * parent.heightFactor);
        originalWidth = (float) (scaleFactor * bounds.width() * parent.widthFactor);

        float width = (float) (scaleFactor * bounds.width());
        float height = (float) (scaleFactor * bounds.height());

        // calculate the degree of rotation
        float rAngle = Math.round(Math.atan2(values[Matrix.MSKEW_X], values[Matrix.MSCALE_X]) * (180 / Math.PI));
        angle = Math.toRadians(-rAngle);

        double centerX = bottomLeftX + (width / 2) * Math.cos(angle) - (height / 2) * Math.sin(angle);
        double centerY = bottomLeftY + ((width / 2) * Math.sin(angle)) + ((height / 2) * Math.cos(angle));

        double bottomRightX = centerX + ((width / 2) * Math.cos(angle)) + ((height / 2) * Math.sin(angle));
        double bottomRightY = centerY + ((width / 2) * Math.sin(angle)) - ((height / 2) * Math.cos(angle));

        double topLeftX = centerX - ((width / 2) * Math.cos(angle)) - ((height / 2) * Math.sin(angle));
        double topLeftY = centerY - ((width / 2) * Math.sin(angle)) + ((height / 2) * Math.cos(angle));

        double topRightX = centerX + ((width / 2) * Math.cos(angle)) - ((height / 2) * Math.sin(angle));
        double topRightY = centerY + ((width / 2) * Math.sin(angle)) + ((height / 2) * Math.cos(angle));

        double myLeft = Math.min(bottomLeftX, bottomRightX);
        myLeft = Math.min(myLeft, topLeftX);
        myLeft = Math.min(myLeft, topRightX);

        double myTop = Math.min(bottomLeftY, bottomRightY);
        myTop = Math.min(myTop, topLeftY);
        myTop = Math.min(myTop, topRightY);

        translatedXY[0] = (float) (myLeft * parent.widthFactor);
        translatedXY[1] = (float) (myTop * parent.heightFactor);
    }
}

class MatrixGestureDetector {
    private int ptpIdx = 0;
    private final Matrix mTempMatrix = new Matrix();
    private final Matrix mMatrix;
    private final OnMatrixChangeListener mListener;
    private final float[] mSrc = new float[4];
    private final float[] mDst = new float[4];
    private int mCount;
    private final GestureDetector tapGestureDetector;

    public MatrixGestureDetector(Context context, Matrix matrix, OnMatrixChangeListener listener) {
        this.mMatrix = matrix;
        this.mListener = listener;
        tapGestureDetector = new GestureDetector(context, new TapListener());
    }

    public void onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 2) {
            return;
        }

        tapGestureDetector.onTouchEvent(event);

        int action = event.getActionMasked();
        int index = event.getActionIndex();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                int idx = index * 2;
                mSrc[idx] = event.getX(index);
                mSrc[idx + 1] = event.getY(index);
                mCount++;
                ptpIdx = 0;
                if (mListener != null) {
                    mListener.onTouchDown();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < mCount; i++) {
                    idx = ptpIdx + i * 2;
                    mDst[idx] = event.getX(i);
                    mDst[idx + 1] = event.getY(i);
                }
                mTempMatrix.setPolyToPoly(mSrc, ptpIdx, mDst, ptpIdx, mCount);
                mMatrix.postConcat(mTempMatrix);
                if (mListener != null) {
                    mListener.onChange(mMatrix);
                }
                System.arraycopy(mDst, 0, mSrc, 0, mDst.length);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerId(index) == 0) ptpIdx = 2;
                mCount--;
                if (event.getPointerCount() == 1) {
                    if (mListener != null) {
                        mListener.onTouchRelease();
                    }
                }
                break;
        }
    }

    public class TapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Utility.showLog("Tap", "Confirmed");
            if (mListener != null) {
                mListener.onTouchClick();
            }
            return true;
        }
    }
}
