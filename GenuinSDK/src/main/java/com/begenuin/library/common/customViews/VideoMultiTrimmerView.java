//package com.begenuin.library.common.customViews;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.util.AttributeSet;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.widget.AppCompatSeekBar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.begenuin.library.R;
//import com.begenuin.library.common.Utility;
//import com.begenuin.library.core.interfaces.IVideoTrimmerView;
//import com.begenuin.library.core.interfaces.SeekBarRangeChanged;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class VideoMultiTrimmerView extends FrameLayout implements IVideoTrimmerView {
//
//    private static final String TAG = VideoMultiTrimmerView.class.getSimpleName();
//
//    private int mMaxWidth;
//    private Context mContext;
//    public RangeSeekBarView mRangeSeekBarView;
//    private LinearLayout mSeekBarLayout;
//    public AppCompatSeekBar videoSeekBar;
//    public float averagePxMs;
//    public VideoTrimmerAdapter mVideoThumbAdapter;
//    private boolean isFromRestore = false;
//    public long mLeftProgressPos = 0L, mRightProgressPos = 10000L;
//    public long mRedProgressBarPos = 0;
//    public SeekBarRangeChanged rangeListener;
//
//    public VideoMultiTrimmerView(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public VideoMultiTrimmerView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init(context);
//    }
//
//    private void init(Context context) {
//        this.mContext = context;
////        LayoutInflater.from(context).inflate(R.layout.video_multi_trimmer_view, this, true);
////        mSeekBarLayout = findViewById(R.id.seekBarLayout);
////        videoSeekBar = findViewById(R.id.videoSeekBar);
////        RecyclerView mVideoThumbRecyclerView = findViewById(R.id.video_frames_recyclerView);
////        mVideoThumbRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
////        mVideoThumbAdapter = new VideoTrimmerAdapter(mContext);
////        mVideoThumbRecyclerView.setAdapter(mVideoThumbAdapter);
////        mMaxWidth = DeviceUtil.getDeviceWidth() - (int) Utility.dpToPx(30f, context);
//    }
//
//    public void initRangeSeekBarView() {
//        if (mRangeSeekBarView != null) {
//            mSeekBarLayout.removeAllViews();
//            mRangeSeekBarView = null;
//        }
//        mRangeSeekBarView = new RangeSeekBarView(mContext, mLeftProgressPos, mRightProgressPos, false, false);
//        mRangeSeekBarView.setSelectedMinValue(mLeftProgressPos);
//        mRangeSeekBarView.setSelectedMaxValue(mRightProgressPos);
//        mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
//        mRangeSeekBarView.setMinShootTime(1000L);
//        mRangeSeekBarView.setNotifyWhileDragging(true);
//        mRangeSeekBarView.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener);
//        mSeekBarLayout.addView(mRangeSeekBarView);
//        averagePxMs = (mMaxWidth * 1.0f / (mRightProgressPos - mLeftProgressPos));
//    }
//
//    private boolean getRestoreState() {
//        return isFromRestore;
//    }
//
//    public void setRestoreState(boolean fromRestore) {
//        isFromRestore = fromRestore;
//    }
//
//    public void setRangeListener(SeekBarRangeChanged rangeListener) {
//        this.rangeListener = rangeListener;
//    }
//
//    private final RangeSeekBarView.OnRangeSeekBarChangeListener mOnRangeSeekBarChangeListener = new RangeSeekBarView.OnRangeSeekBarChangeListener() {
//        @Override
//        public void onRangeSeekBarValuesChanged(RangeSeekBarView bar, long minValue, long maxValue, int action, boolean isMin,
//                                                RangeSeekBarView.Thumb pressedThumb) {
//            Utility.showLog(TAG, "-----minValue----->>>>>>" + minValue);
//            Utility.showLog(TAG, "-----maxValue----->>>>>>" + maxValue);
//            long scrollPos = 0;
//            mLeftProgressPos = minValue + scrollPos;
//            mRedProgressBarPos = mLeftProgressPos;
//            mRightProgressPos = maxValue + scrollPos;
//            videoSeekBar.setProgress((int)mRedProgressBarPos);
//            boolean isLeft = pressedThumb == RangeSeekBarView.Thumb.MIN;
//            Utility.showLog(TAG, "-----mLeftProgressPos----->>>>>>" + mLeftProgressPos);
//            Utility.showLog(TAG, "-----mRightProgressPos----->>>>>>" + mRightProgressPos);
//            switch (action) {
//                case MotionEvent.ACTION_DOWN:
//                    if (rangeListener != null) {
//                        rangeListener.onRangeStart(isLeft);
//                    }
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    Utility.printErrorLog("rangeSeekbar VideoTrimmerView passing to VideoTrimmerFragment. on action move");
//                    if (rangeListener != null) {
//                        rangeListener.onRangeSelection(mLeftProgressPos, mRightProgressPos,isLeft);
//                    }
//                    break;
//                case MotionEvent.ACTION_UP:
//                    if (rangeListener != null) {
//                        rangeListener.onRangeChanged(mLeftProgressPos, mRightProgressPos,isLeft);
//                    }
//                    break;
//                default:
//                    break;
//            }
//
//            mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
//        }
//    };
//
//    public static class VideoTrimmerAdapter extends RecyclerView.Adapter<VideoTrimmerAdapter.TrimmerViewHolder> {
//        private final List<Bitmap> mBitmaps = new ArrayList<>();
//        private final LayoutInflater mInflater;
//
//        public VideoTrimmerAdapter(Context context) {
//            this.mInflater = LayoutInflater.from(context);
//        }
//
//        @NonNull
//        @Override
//        public TrimmerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            return new TrimmerViewHolder(mInflater.inflate(R.layout.video_thumb_item_layout, parent, false));
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull TrimmerViewHolder holder, int position) {
//            holder.thumbImageView.setImageBitmap(mBitmaps.get(position));
//        }
//
//        @Override
//        public int getItemCount() {
//            return mBitmaps.size();
//        }
//
//        public void addBitmaps(Bitmap bitmap) {
//            mBitmaps.add(bitmap);
//            notifyDataSetChanged();
//        }
//
//        public void clear() {
//            mBitmaps.clear();
//            notifyDataSetChanged();
//        }
//
//        private static final class TrimmerViewHolder extends RecyclerView.ViewHolder {
//            ImageView thumbImageView;
//
//            TrimmerViewHolder(View itemView) {
//                super(itemView);
//                thumbImageView = itemView.findViewById(R.id.thumb);
//            }
//        }
//    }
//
//    public void cancelThreads(){
//        BackgroundExecutor.cancelAll("1", true);
//        UiThreadExecutor.cancelAll("1");
//    }
//
//    /**
//     * Cancel trim thread execute action when finish
//     */
//    @Override
//    public void onDestroy() {
//       cancelThreads();
//    }
//}
