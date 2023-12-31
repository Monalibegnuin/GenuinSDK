package com.begenuin.library.common.customViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.begenuin.library.R;
import com.begenuin.library.common.Utility;

import java.text.DecimalFormat;

public class RangeSeekBarView extends View {
    private static final String TAG = RangeSeekBarView.class.getSimpleName();
    public static final int INVALID_POINTER_ID = 255;
    public static final int ACTION_POINTER_INDEX_MASK = 0x0000ff00, ACTION_POINTER_INDEX_SHIFT = 8;
    private static final int TextPositionY = 7;
    private static final int paddingTop =10;
    private int mActivePointerId = INVALID_POINTER_ID;

    private long mMinShootTime = 60000L;
    private long mMaxShootTime = 70000L;
    private double absoluteMinValuePrim, absoluteMaxValuePrim;
    private double normalizedMinValue = 0d;//点坐标占总长度的比例值，范围从0-1
    private double normalizedMaxValue = 1d;//点坐标占总长度的比例值，范围从0-1
    private double normalizedMinValueTime = 0d;
    private double normalizedMaxValueTime = 1d;// normalized：规格化的--点坐标占总长度的比例值，范围从0-1
    private int mScaledTouchSlop;
    private Bitmap thumbImageLeft;
    private Bitmap thumbImageRight;
    private Bitmap thumbPressedImage;
    private Paint paint;
    private Paint rectPaint;
    private final Paint mVideoTrimTimePaintL = new Paint();
    private final Paint mVideoTrimTimePaintR = new Paint();
    private final Paint mShadow = new Paint();
    private int thumbWidth;
    private float thumbHalfWidth;
    private final float padding = 0;
    private long mStartPosition = 0;
    private long mEndPosition = 0;
    private float thumbPaddingTop = 2;
    private boolean isTouchDown;
    private float mDownMotionX;
    private boolean mIsDragging;
    private Thumb pressedThumb;
    private boolean isMin, isMax;
    private double min_width = 1, max_width;//最小裁剪距离
    private boolean notifyWhileDragging = false;
    private OnRangeSeekBarChangeListener mRangeSeekBarChangeListener;
    private int whiteColorRes;
    private boolean isEnableDragging;
    private boolean isBlueThumb;

    public enum Thumb {
        MIN, MAX //MIn = Left, MAX = Right
    }

    public RangeSeekBarView(Context context) {
        super(context);
    }

    public RangeSeekBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RangeSeekBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RangeSeekBarView(Context context, long absoluteMinValuePrim, long absoluteMaxValuePrim, boolean isEnableDragging, boolean isBlueThumb) {
        super(context);
        this.absoluteMinValuePrim = absoluteMinValuePrim;
        this.absoluteMaxValuePrim = absoluteMaxValuePrim;
        this.isBlueThumb = isBlueThumb;
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.isEnableDragging = isEnableDragging;
        if (isEnableDragging) {
            if(isBlueThumb) {
                whiteColorRes = getContext().getResources().getColor(R.color.colorPrimary, null);
            }else{
                whiteColorRes = getContext().getResources().getColor(R.color.trim_border, null);
            }
        } else {
            whiteColorRes = getContext().getResources().getColor(R.color.transparent, null);
        }
        init();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void init() {
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        Drawable d, d1;
        if(isBlueThumb){
            d = ResourcesCompat.getDrawable(getResources(), R.drawable.video_trimmer_blue_left_bar, null);
            d1 = ResourcesCompat.getDrawable(getResources(), R.drawable.video_trimmer_blue_right_bar, null);
        }else {
            d = ResourcesCompat.getDrawable(getResources(), R.drawable.video_trimmer_left_bar, null);
            d1 = ResourcesCompat.getDrawable(getResources(), R.drawable.video_trimmer_right_bar, null);
        }
        thumbImageLeft = drawableToBitmap(d);
        thumbImageRight = drawableToBitmap(d1);
        int width = thumbImageLeft.getWidth();
        int height = thumbImageLeft.getHeight();
        //int newWidth = UnitConverter.dpToPx(14);
        //int newHeight = UnitConverter.dpToPx(48);
        int newWidth =14;
        int newHeight = 48;
        float scaleWidth = newWidth * 1.0f / width;
        float scaleHeight = newHeight * 1.0f / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        thumbImageLeft = Bitmap.createBitmap(thumbImageLeft, 0, 0, width, height, matrix, true);
        thumbImageRight = Bitmap.createBitmap(thumbImageRight, 0, 0, width, height, matrix, true);
        thumbPressedImage = thumbImageLeft;
        thumbWidth = newWidth;
//    thumbHalfWidth = thumbWidth / 2;
//        thumbHalfWidth = thumbWidth * 2;
        thumbHalfWidth = thumbWidth;
        int shadowColor;
        if (isEnableDragging) {
            shadowColor = getContext().getResources().getColor(R.color.shadow_color, null);
        } else {
            shadowColor = getContext().getResources().getColor(R.color.color_353535, null);
        }
        mShadow.setAntiAlias(true);
        mShadow.setColor(shadowColor);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setColor(whiteColorRes);

        mVideoTrimTimePaintL.setStrokeWidth(3);
        mVideoTrimTimePaintL.setARGB(255, 51, 51, 51);
        mVideoTrimTimePaintL.setTextSize(28);
        mVideoTrimTimePaintL.setAntiAlias(true);
        mVideoTrimTimePaintL.setColor(whiteColorRes);
        mVideoTrimTimePaintL.setTextAlign(Paint.Align.LEFT);

        mVideoTrimTimePaintR.setStrokeWidth(3);
        mVideoTrimTimePaintR.setARGB(255, 51, 51, 51);
        mVideoTrimTimePaintR.setTextSize(28);
        mVideoTrimTimePaintR.setAntiAlias(true);
        mVideoTrimTimePaintR.setColor(whiteColorRes);
        mVideoTrimTimePaintR.setTextAlign(Paint.Align.RIGHT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 300;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int height = 120;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float bg_middle_left = 0;
        float bg_middle_right = getWidth() - getPaddingRight();
        float rangeL = normalizedToScreen(normalizedMinValue);
        float rangeR = normalizedToScreen(normalizedMaxValue);
//        Rect leftRect = new Rect((int) bg_middle_left + UnitConverter.dpToPx(14), getHeight(), (int) rangeL, paddingTop);
//        Rect rightRect = new Rect((int) rangeR, getHeight(), (int) bg_middle_right - UnitConverter.dpToPx(14), paddingTop);

//        Rect leftRect = new Rect((int) bg_middle_left + UnitConverter.dpToPx(14), getHeight() - UnitConverter.dpToPx(4), (int) rangeL + UnitConverter.dpToPx(14), paddingTop + (int) thumbPaddingTop);
//        Rect rightRect = new Rect((int) rangeR - UnitConverter.dpToPx(14), getHeight() - UnitConverter.dpToPx(4), (int) bg_middle_right - UnitConverter.dpToPx(14), paddingTop + (int) thumbPaddingTop);
//        canvas.drawRect(leftRect, mShadow);
//        canvas.drawRect(rightRect, mShadow);
//
//        canvas.drawRect(rangeL + UnitConverter.dpToPx(14), thumbPaddingTop + paddingTop, rangeR - UnitConverter.dpToPx(14), thumbPaddingTop + UnitConverter.dpToPx(2) + paddingTop, rectPaint);
//        canvas.drawRect(rangeL + UnitConverter.dpToPx(14), getHeight() - UnitConverter.dpToPx(6), rangeR - UnitConverter.dpToPx(14), getHeight() - UnitConverter.dpToPx(4), rectPaint);
//
//        if (isEnableDragging) {
//            drawThumb(normalizedToScreen(normalizedMinValue), false, canvas, true);
//            drawThumb(normalizedToScreen(normalizedMaxValue), false, canvas, false);
//            drawVideoTrimTimeText(canvas);
//        }
    }

    private void drawThumb(float screenCoord, boolean pressed, Canvas canvas, boolean isLeft) {
        canvas.drawBitmap(pressed ? thumbPressedImage : (isLeft ? thumbImageLeft : thumbImageRight), screenCoord - (isLeft ? 0 : thumbWidth), paddingTop + thumbPaddingTop,
                paint);
    }

    private void drawVideoTrimTimeText(Canvas canvas) {
//        String leftThumbsTime = DateUtil.convertSecondsToTime(mStartPosition);
//        String rightThumbsTime = DateUtil.convertSecondsToTime(mEndPosition);
//        canvas.drawText(leftThumbsTime, normalizedToScreen(normalizedMinValue), TextPositionY, mVideoTrimTimePaintL);
//        canvas.drawText(rightThumbsTime, normalizedToScreen(normalizedMaxValue), TextPositionY, mVideoTrimTimePaintR);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isTouchDown) {
            return super.onTouchEvent(event);
        }
        if (event.getPointerCount() > 1) {
            return super.onTouchEvent(event);
        }

        if (!isEnabled()) return false;
        if (absoluteMaxValuePrim <= mMinShootTime) {
            return super.onTouchEvent(event);
        }
        int pointerIndex;// record the index of the click point
        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Utility.printErrorLog("rangeSeekbar action down ");
                //Remember the coordinate x of the point where the last finger tapped the screen，mDownMotionX
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(pointerIndex);
                Utility.printErrorLog("touchPoint: " + mDownMotionX);
                // Determine whether the touch is the maximum value thumb or the minimum value thumb
                pressedThumb = evalPressedThumb(mDownMotionX);
                if (pressedThumb == null) return super.onTouchEvent(event);
                setPressed(true);// Set the control to be pressed
                onStartTrackingTouch();// Set mIsDragging to true and start tracking touch events
                trackTouchEvent(event);
                attemptClaimDrag();
                if (mRangeSeekBarChangeListener != null) {
                    Utility.printErrorLog("rangeSeekbar mRangeSeekBarChangeListener not null (Down) ");
                    mRangeSeekBarChangeListener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue(),
                            MotionEvent.ACTION_DOWN, isMin,
                            pressedThumb);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Utility.printErrorLog("rangeSeekbar action move ");
                if (pressedThumb != null) {
                    Utility.printErrorLog("rangeSeekbar pressedThumb ");
                    if (mIsDragging) {
                        Utility.printErrorLog("rangeSeekbar mIsDragging ");
                        trackTouchEvent(event);
                    } else {
                        Utility.printErrorLog("rangeSeekbar else case (Move) ");
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        final float x = event.getX(pointerIndex);// The X coordinate of the finger on the control
                        Utility.printErrorLog("rangeSeekbar: X: " + x);
                        // The finger is not on the min and max, and there is a swipe event on the control
                        float value = Math.abs(x - mDownMotionX);
                        if (value > mScaledTouchSlop) {
                            Utility.printErrorLog("rangeSeekbar Math.abs(x - mDownMotionX) > mScaledTouchSlop statisfied ");
                            setPressed(true);
                            Utility.printErrorLog("rangeSeekbar: Not dragging the maximum and minimum");// 一直不会执行？
                            invalidate();
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
                    }
                    if (notifyWhileDragging && mRangeSeekBarChangeListener != null) {
                        mRangeSeekBarChangeListener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue(), MotionEvent.ACTION_MOVE,
                                isMin, pressedThumb);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                Utility.printErrorLog("rangeSeekbar action up ");
                if (mIsDragging) {
                    Utility.printErrorLog("rangeSeekbar dragging.. ");
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    Utility.printErrorLog("rangeSeekbar no dragging.. ");
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }

                invalidate();
                if (mRangeSeekBarChangeListener != null) {
                    mRangeSeekBarChangeListener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue(), MotionEvent.ACTION_UP, isMin,
                            pressedThumb);
                }
                pressedThumb = null;// When the finger is lifted, the touched thumb is set to null
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                final int index = event.getPointerCount() - 1;
                // final int index = ev.getActionIndex();
                mDownMotionX = event.getX(index);
                mActivePointerId = event.getPointerId(index);
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate(); // see above explanation
                break;
            default:
                break;
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mDownMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private void trackTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1) return;
        Utility.printErrorLog("rangeSeekbar: trackTouchEvent: " + event.getAction() + " x: " + event.getX());
        final int pointerIndex = event.findPointerIndex(mActivePointerId);// get the index of the pressed point
        float x = 0;
        try {
            x = event.getX(pointerIndex);
            Utility.printErrorLog("rangeSeekbar: trackTouchEvent: index of the pressed point: " + x);
        } catch (Exception e) {
            return;
        }
        if (Thumb.MIN.equals(pressedThumb)) {
            // screenToNormalized(x)-->得到规格化的0-1的值
            setNormalizedMinValue(screenToNormalized(x, 0));
        } else if (Thumb.MAX.equals(pressedThumb)) {
            setNormalizedMaxValue(screenToNormalized(x, 1));
        }
    }

    private double screenToNormalized(float screenCoord, int position) {
        int width = getWidth();
        if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            return 0d;
        } else {
            isMin = false;
            isMax = false;
            double current_width = screenCoord;
            float rangeL = normalizedToScreen(normalizedMinValue);
            float rangeR = normalizedToScreen(normalizedMaxValue);
            double min = mMinShootTime / (absoluteMaxValuePrim - absoluteMinValuePrim) * (width - thumbWidth * 2);
            double max = mMaxShootTime / (absoluteMaxValuePrim - absoluteMinValuePrim) * (width);
            if (absoluteMaxValuePrim > 5 * 60 * 1000) {//Exact four decimal places greater than 5 minutes
                DecimalFormat df = new DecimalFormat("0.0000");
                min_width = Double.parseDouble(df.format(min));
            } else {
                min_width = Math.round(min + 0.5d);
            }
            max_width = Math.round(max + 0.5d);
            if (position == 0) {
                if (isInThumbRangeLeft(screenCoord, normalizedMinValue, 0.1)) {
                    return normalizedMinValue;
                }

                float rightPosition = (getWidth() - rangeR) >= 0 ? (getWidth() - rangeR) : 0;
                double left_length = getValueLength() - (rightPosition + min_width);

                if (current_width > rangeL) {
                    current_width = rangeL + (current_width - rangeL);
                } else if (current_width <= rangeL) {
                    current_width = rangeL - (rangeL - current_width);
                }

                if (current_width > left_length) {
                    isMin = true;
                    current_width = left_length;
                }

                if (current_width < thumbWidth * 2f / 3f) {
                    current_width = 0;
                }

                // Calculation for max limit -- vishal
                if((rangeR - current_width) > max_width){
                    isMax = true;
                    current_width = rangeR - max_width;
                }

                double resultTime = (current_width - padding) / (width - 2 * thumbWidth);
                normalizedMinValueTime = Math.min(1d, Math.max(0d, resultTime));
                double result = (current_width - padding) / (width - 2 * padding);
                return Math.min(1d, Math.max(0d, result));// The value is guaranteed to be between 0 and 1, based on min and max
            } else {
                if (isInThumbRange(screenCoord, normalizedMaxValue, 0.1)) {
                    return normalizedMaxValue;
                }
                double right_length = getValueLength() - (rangeL + min_width);
                if (current_width > rangeR) {
                    current_width = rangeR + (current_width - rangeR);
                } else if (current_width <= rangeR) {
                    current_width = rangeR - (rangeR - current_width);
                }

                double paddingRight = getWidth() - current_width;

                if (paddingRight > right_length) {
                    isMin = true;
                    current_width = getWidth() - right_length;
                    paddingRight = right_length;
                }

                if (paddingRight < thumbWidth * 2f / 3f) {
                    current_width = getWidth();
                    paddingRight = 0;
                }

                //Calculation for max limit -- vishal
                if((current_width - rangeL) > max_width){
                    isMax = true;
                    current_width = max_width + rangeL;
                }

                double resultTime = (paddingRight - padding) / (width - 2 * thumbWidth);
                resultTime = 1 - resultTime;
                normalizedMaxValueTime = Math.min(1d, Math.max(0d, resultTime));
                double result = (current_width - padding) / (width - 2 * padding);
                return Math.min(1d, Math.max(0d, result));// The value is guaranteed to be between 0 and 1, based on min and max
            }
        }
    }

    private int getValueLength() {
        return (getWidth() - 2 * thumbWidth);
    }

    /**
     * Calculate in which Thumb
     *
     * @param touchX touchX
     * @return is touched is empty or maximum or minimum
     */
    private Thumb evalPressedThumb(float touchX) {
        Utility.printErrorLog("evalPressedThumb: " + touchX);
        Thumb result = null;
        boolean minThumbPressed = isInThumbRange(touchX, normalizedMinValue, 2);// Whether the touch point is within the minimum image range
        boolean maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue, 2);
        if (minThumbPressed && maxThumbPressed) {
            /* If two thumbs overlap, you can't judge which one to drag, do the following
             If the touch point is on the right side of the screen, it is judged that the touch has reached the minimum value thumb,
            otherwise it is judged that the touch has reached the maximum value thumb
             */
            result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
        } else if (minThumbPressed) {
            result = Thumb.MIN;
        } else if (maxThumbPressed) {
            result = Thumb.MAX;
        }
        return result;
    }

    private boolean isInThumbRange(float touchX, double normalizedThumbValue, double scale) {
        // The difference between the X coordinate of the current touch point and the X coordinate of the center point of the minimum image on the screen <= the width of the minimum image is generally
        // That is, to determine whether the touch point is within the circle with the minimum image center as the origin and half the width as the radius.
        if (isEnableDragging) {
            return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth * scale;
        } else {
            return false;
        }
    }

    private boolean isInThumbRangeLeft(float touchX, double normalizedThumbValue, double scale) {
        // The difference between the X coordinate of the current touch point and the X coordinate of the center point of the minimum image on
        // the screen <= the width of the minimum image is generally
        // That is, to determine whether the touch point is within the circle with the minimum image center as the origin and half the width as the radius.
        if (isEnableDragging) {
            return Math.abs(touchX - normalizedToScreen(normalizedThumbValue) - thumbWidth) <= thumbHalfWidth * scale;
        } else {
            return false;
        }
    }

    /**
     * Trying to tell the parent view not to intercept the drag of the child control
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    void onStartTrackingTouch() {
        mIsDragging = true;
    }

    void onStopTrackingTouch() {
        mIsDragging = false;
    }

    public void setMinShootTime(long min_cut_time) {
        this.mMinShootTime = min_cut_time;
    }

    public void setMaxShootTime(long max_cut_time) {
        this.mMaxShootTime = max_cut_time;
    }

    private float normalizedToScreen(double normalizedCoord) {
        return (float) (getPaddingLeft() + normalizedCoord * (getWidth() - getPaddingLeft() - getPaddingRight()));
    }

    private double valueToNormalized(long value) {
        if (0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            return 0d;
        }
        return (value - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim);
    }

    public void setStartEndTime(long start, long end) {
        this.mStartPosition = start / 1000;
        this.mEndPosition = end / 1000;
    }

    public void setSelectedMinValue(long value) {
        if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMinValue(0d);
        } else {
            setNormalizedMinValue(valueToNormalized(value));
        }
    }

    public void setSelectedMaxValue(long value) {
        if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMaxValue(1d);
        } else {
            setNormalizedMaxValue(valueToNormalized(value));
        }
    }

    public void setNormalizedMinValue(double value) {
        normalizedMinValue = Math.max(0d, Math.min(1d, Math.min(value, normalizedMaxValue)));
        normalizedMinValueTime = Math.max(0d, Math.min(1d, Math.min(value, normalizedMaxValueTime)));
        invalidate();// 重新绘制此view
    }

    public void setNormalizedMaxValue(double value) {
        normalizedMaxValue = Math.max(0d, Math.min(1d, Math.max(value, normalizedMinValue)));
        normalizedMaxValueTime = Math.max(0d, Math.min(1d, Math.max(value, normalizedMinValueTime)));
        invalidate();// 重新绘制此view
    }

    public long getSelectedMinValue() {
        return normalizedToValue(normalizedMinValueTime);
    }

    public long getSelectedMaxValue() {
        return normalizedToValue(normalizedMaxValueTime);
    }

    private long normalizedToValue(double normalized) {
        return (long) (absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim));
    }

    /**
     * 供外部activity调用，控制是都在拖动的时候打印log信息，默认是false不打印
     */
    public boolean isNotifyWhileDragging() {
        return notifyWhileDragging;
    }

    public void setNotifyWhileDragging(boolean flag) {
        this.notifyWhileDragging = flag;
    }

    public void setTouchDown(boolean touchDown) {
        isTouchDown = touchDown;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable("SUPER", super.onSaveInstanceState());
        bundle.putDouble("MIN", normalizedMinValue);
        bundle.putDouble("MAX", normalizedMaxValue);
        bundle.putDouble("MIN_TIME", normalizedMinValueTime);
        bundle.putDouble("MAX_TIME", normalizedMaxValueTime);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcel) {
        final Bundle bundle = (Bundle) parcel;
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"));
        normalizedMinValue = bundle.getDouble("MIN");
        normalizedMaxValue = bundle.getDouble("MAX");
        normalizedMinValueTime = bundle.getDouble("MIN_TIME");
        normalizedMaxValueTime = bundle.getDouble("MAX_TIME");
    }

    public interface OnRangeSeekBarChangeListener {
        void onRangeSeekBarValuesChanged(RangeSeekBarView bar, long minValue, long maxValue, int action, boolean isMin, Thumb pressedThumb);
    }

    public void setOnRangeSeekBarChangeListener(OnRangeSeekBarChangeListener listener) {
        this.mRangeSeekBarChangeListener = listener;
    }
}
