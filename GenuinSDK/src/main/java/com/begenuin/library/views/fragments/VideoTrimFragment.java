//package com.begenuin.library.views.fragments;
//
//import android.annotation.SuppressLint;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.res.Resources;
//import android.graphics.Bitmap;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.Drawable;
//import android.media.MediaMetadataRetriever;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.SystemClock;
//import android.util.Size;
//import android.util.TypedValue;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.view.animation.TranslateAnimation;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.SeekBar;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.cardview.widget.CardView;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.begenuin.library.R;
//import com.begenuin.library.common.Utility;
//import com.begenuin.library.common.customViews.CustomTextView;
//import com.begenuin.library.common.customViews.VideoMultiTrimmerView;
//import com.begenuin.library.common.customViews.VideoTrimmerView;
//import com.begenuin.library.core.interfaces.SeekBarRangeChanged;
//import com.begenuin.library.data.model.PlayerHelperModel;
//import com.begenuin.library.data.model.VideoFileModel;
//import com.begenuin.library.data.model.VideoModel;
//import com.begenuin.library.views.activities.CameraNewActivity;
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.bumptech.glide.request.target.CustomTarget;
//import com.bumptech.glide.request.transition.Transition;
//import com.google.android.exoplayer2.C;
//import com.google.android.exoplayer2.ExoPlayer;
//import com.google.android.exoplayer2.MediaItem;
//import com.google.android.exoplayer2.Player;
//import com.google.android.exoplayer2.SeekParameters;
//import com.google.android.exoplayer2.ui.StyledPlayerView;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Locale;
//import java.util.Timer;
//import java.util.TimerTask;
//
///**
// * A simple {@link Fragment} subclass.
// */
//public class VideoTrimFragment extends Fragment implements View.OnClickListener, SeekBarRangeChanged, OnSelectedRangeChangedListener {
//
//    private CameraNewActivity context;
//
//    private CustomTextView tvVideoTimerSelected;
//    private TextView tvDuration, tvFullDuration, tvDurationSingle, tvFullDurationSingle;
//    private LinearLayout llVideoTrimmerMultiple, llVideoTrimmerSingle, llRetake;
//    private RecyclerView recyclerViewVideoList;
//    private VideoMultiTrimmerView videoTrimmerView;
//    private VideoTrimmerView videoTrimmerViewSingle;
//    private ExoPlayer player;
//    private LinearLayout llSingleTrimBack;
//    private Dialog mDeleteClipDialog, mBackConfirmDialog;
//
//    //private int previewWidth, previewHeight;
//    public float screenWidth, screenHeight;
//    private long mLastClickTime = 0;
//
//    VideoListAdapter videoListAdapter;
//
//    private int selectedPos = 0;
//    private int prevSelectedPos = 0;
//
//    private long startTrimMillis = 0, endTrimMillis = 0;
//    private int singleStartPos;
//
//    float px10Value;
//    public int THUMB_WIDTH = 50;
//    private int THUMB_HEIGHT;
//    private int frameVideoPos = 0;
//    private int frameCount = 0;
//    private final int TOTAL_THUMB_COUNT = 15;
//    private long TOTAL_DURATION = 0;
//    private boolean isShowSection = false;
//    private boolean isPaused = false;
//    private boolean isTrimmed = false;
//    private boolean isDeleted = false;
//    private StyledPlayerView videoView;
//    private int playStartPos = -1, playEndPos = -1;
//    private final double MIN_DURATION = 4.0;
//    long previousEventTime;
//    private boolean isSeeking = false;
//    private String taskFilePath, multiTaskFilePath;
//    private final ArrayList<VideoModel> tempVideoList = new ArrayList<>();
//    private ArrayList<PlayerHelperModel> playerHelper = new ArrayList<>();
//    private CardView llVideoView;
//
//    public VideoTrimFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        context = (CameraNewActivity) getActivity();
//        assert context != null;
//        THUMB_HEIGHT = (int) Utility.dpToPx(48f, context);
//    }
//
//    @Override
//    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        Bundle bundle = getArguments();
//        if (bundle != null) {
//            previousEventTime = bundle.getLong("previousEventTime", 0);
//        }
//        return inflater.inflate(R.layout.fragment_video_trim, container, false);
//    }
//
//    private final Player.Listener playListener = new Player.Listener() {
//        @Override
//        public void onMediaItemTransition(@Nullable @org.jetbrains.annotations.Nullable MediaItem mediaItem, int reason) {
//            getRelativeFrontBack();
//            setDashLine();
//        }
//
//        @Override
//        public void onRenderedFirstFrame() {
//            if (getRelativeFrontBack()) {
//                videoView.getVideoSurfaceView().setScaleX(-1);
//            } else {
//                videoView.getVideoSurfaceView().setScaleX(1);
//            }
//            // onRenderedFirstFrame is called before onMediaItemTransition sometimes so we need to have below code.
//            new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                if (player != null) {
//                    if (getRelativeFrontBack()) {
//                        videoView.getVideoSurfaceView().setScaleX(-1);
//                    } else {
//                        videoView.getVideoSurfaceView().setScaleX(1);
//                    }
//                }
//            }, 100);
//        }
//
//        @Override
//        public void onPlaybackStateChanged(int playbackState) {
//            Player.Listener.super.onPlaybackStateChanged(playbackState);
//            if (playbackState == Player.STATE_ENDED) {
//                if (player != null) {
//                    player.seekTo(0, 0L);
//                    player.setPlayWhenReady(true);
//                }
//            }
//        }
//    };
//
//    private final Player.Listener singlePlayListener = new Player.Listener() {
//        @Override
//        public void onRenderedFirstFrame() {
//            if (getSingleRelativeFrontBack()) {
//                videoView.getVideoSurfaceView().setScaleX(-1);
//            } else {
//                videoView.getVideoSurfaceView().setScaleX(1);
//            }
//            // onRenderedFirstFrame is called before onMediaItemTransition sometimes so we need to have below code.
//            new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                if (player != null) {
//                    if (getSingleRelativeFrontBack()) {
//                        videoView.getVideoSurfaceView().setScaleX(-1);
//                    } else {
//                        videoView.getVideoSurfaceView().setScaleX(1);
//                    }
//                }
//            }, 100);
//        }
//
//        @Override
//        public void onPlaybackStateChanged(int playbackState) {
//            Player.Listener.super.onPlaybackStateChanged(playbackState);
//            if (playbackState == Player.STATE_ENDED) {
//                if (player != null) {
//                    player.seekTo(0, 0L);
//                    player.setPlayWhenReady(true);
//                }
//            }
//        }
//    };
//
//    private boolean getRelativeFrontBack() {
//        int currentPlayPos = player.getCurrentPeriodIndex();
//        selectedPos = playerHelper.get(currentPlayPos).selectedPos;
//        return playerHelper.get(currentPlayPos).isFront;
//    }
//
//    private boolean getSingleRelativeFrontBack() {
//        int relativePos = singleStartPos + player.getCurrentPeriodIndex();
//        VideoModel video = context.videoList.get(selectedPos);
//        boolean isRetake = video.videoRetakeFileList.size() > 0;
//        if (isRetake) {
//            return video.videoRetakeFileList.get(relativePos).isFront;
//        } else {
//            return video.videoFileList.get(relativePos).isFront;
//        }
//    }
//
//    public void refreshTrimmerView() {
//        frameVideoPos = 0;
//        frameCount = 0;
//        calculationForFrames();
//        setVideoFrames();
//        if (videoListAdapter != null) {
//            videoListAdapter.notifyDataSetChanged();
//        }
//        if (context.isFullTrim) {
//            updateVideoProgress();
//        } else {
//            setVideoPlay();
//        }
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        player = new ExoPlayer.Builder(context).build();
//
//        int[] ints = new int[0];
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
//            ints = Utility.getScreenWidthHeight(context);
//        }
//        screenWidth = ints[0];
//        screenHeight = ints[1];
//
//        float dip = 10f;
//        Resources r = context.getResources();
//        px10Value = TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_DIP,
//                dip,
//                r.getDisplayMetrics()
//        );
//
//        initViews(view);
//        setVideoData();
//        if (context.videoList.size() > 0) {
//            new Handler(Looper.getMainLooper()).postDelayed(() -> {
//               /* previewHeight = llVideoView.getHeight();
//                previewWidth = previewHeight * 9 / 16;
////                previewWidth = (int) ((int) (previewHeight * screenWidth) / screenHeight);
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(previewWidth, previewHeight);
//                videoView.setLayoutParams(params);*/
//                refreshTrimmerView();
//            }, 200);
//        }
//    }
//
//    public void refreshSectionView() {
//        setSingleVideoPlay(true);
//        getVideoTrimView(context.videoList.get(selectedPos));
//    }
//
//    public void refreshSectionViewAfterClose() {
//        setSingleVideoPlay(true);
//        getVideoTrimView(context.videoList.get(selectedPos));
//    }
//
//    public void resetSeekBar() {
//        stopPlaying();
//        videoTrimmerView.mLeftProgressPos = 0;
//        videoTrimmerView.mRedProgressBarPos = 0;
//        resetSelection();
//        if (videoListAdapter != null) {
//            videoListAdapter.notifyDataSetChanged();
//        }
//        if (videoTrimmerView.mVideoThumbAdapter != null) {
//            videoTrimmerView.mVideoThumbAdapter.clear();
//        }
//    }
//
//    private void calculationForFrames() {
//        TOTAL_DURATION = 0;
//        long tempDuration = 0;
//        boolean isStartSelected = false, isEndSelected = false;
//        long startPositionMillis = 0, endPositionMillis = 0;
//        int startPos = -1;
//        for (int i = 0; i < context.videoList.size(); i++) {
//            VideoModel video = context.videoList.get(i);
//            boolean isTrimmed = isTrimmed(video);
//            if (isTrimmed) {
//                TOTAL_DURATION += (context.videoList.get(i).trimDuration * 1000);
//            } else {
//                TOTAL_DURATION += (context.videoList.get(i).actualDuration * 1000);
//            }
//            if (video.isFullTrim) {
//                if (!isStartSelected) {
//                    isStartSelected = true;
//                    startPos = i;
//                    selectedPos = i;
//                    if (i != 0) {
//                        context.videoList.get(0).isSelected = false;
//                        context.videoList.get(i).isSelected = true;
//                    }
//                    if (isTrimmed) {
//                        startPositionMillis = video.fullTrimStartMillis + TOTAL_DURATION - (long) (context.videoList.get(i).trimDuration * 1000);
//                    } else {
//                        startPositionMillis = video.fullTrimStartMillis + TOTAL_DURATION - (long) (context.videoList.get(i).actualDuration * 1000);
//                    }
//                    tempDuration = TOTAL_DURATION;
//                } else if (isStartSelected && !isEndSelected) {
//                    isEndSelected = true;
//                    if (isTrimmed) {
//                        endPositionMillis = video.fullTrimEndMillis + TOTAL_DURATION - (long) (context.videoList.get(i).trimDuration * 1000);
//                    } else {
//                        endPositionMillis = video.fullTrimEndMillis + TOTAL_DURATION - (long) (context.videoList.get(i).actualDuration * 1000);
//                    }
//                }
//            }
//        }
//        if (startPos != -1 && !isEndSelected) {
//            VideoModel video = context.videoList.get(startPos);
//            if (isTrimmed(video)) {
//                endPositionMillis = video.fullTrimEndMillis + tempDuration - (long) (context.videoList.get(startPos).trimDuration * 1000);
//            } else {
//                endPositionMillis = video.fullTrimEndMillis + tempDuration - (long) (context.videoList.get(startPos).actualDuration * 1000);
//            }
//        }
//        for (int i = 0; i < context.videoList.size(); i++) {
//            VideoModel video = context.videoList.get(i);
//            double countValue;
//            if (isTrimmed(video)) {
//                countValue = ((TOTAL_THUMB_COUNT * context.videoList.get(i).trimDuration * 1000) / TOTAL_DURATION);
//            } else {
//                countValue = ((TOTAL_THUMB_COUNT * context.videoList.get(i).actualDuration * 1000) / TOTAL_DURATION);
//            }
//            countValue = Double.parseDouble(String.format(Locale.ENGLISH, "%.2f", countValue));
//            context.videoList.get(i).thumbCount = (int) Math.ceil(countValue);
//            context.videoList.get(i).thumbFloatCount = countValue;
//        }
//
//        THUMB_WIDTH = (int) ((DeviceUtil.getDeviceWidth() - Utility.dpToPx(30f, context)) / TOTAL_THUMB_COUNT);
//        videoTrimmerView.mLeftProgressPos = 0;
//        videoTrimmerView.mRightProgressPos = TOTAL_DURATION;
//        videoTrimmerView.initRangeSeekBarView();
//        videoTrimmerView.videoSeekBar.setMax((int) TOTAL_DURATION);
//        tvFullDuration.setText(String.format("  /  %s", Utility.milliToStringTrim(TOTAL_DURATION)));
//        videoTrimmerView.videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
//                if (b) {
//                    Utility.showLog("Tag", progress + "");
//                    if (progress < videoTrimmerView.mLeftProgressPos) {
//                        videoTrimmerView.videoSeekBar.setProgress((int) videoTrimmerView.mLeftProgressPos);
//                    } else if (progress > videoTrimmerView.mRightProgressPos) {
//                        videoTrimmerView.videoSeekBar.setProgress((int) videoTrimmerView.mRightProgressPos);
//                    }
//
//                    if (SystemClock.elapsedRealtime() - mLastClickTime < 50) {
//                        return;
//                    }
//                    mLastClickTime = SystemClock.elapsedRealtime();
//                    int[] seekParams = getActualSeekProgress(seekBar.getProgress());
//                    player.seekTo(seekParams[0], seekParams[1]);
//                    tvDuration.setText(Utility.milliToStringTrim(seekBar.getProgress()));
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                isSeeking = true;
//                player.setSeekParameters(SeekParameters.NEXT_SYNC);
//                player.setPlayWhenReady(false);
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                isSeeking = false;
//                player.setSeekParameters(SeekParameters.EXACT);
//                if (videoView != null) {
//                    int[] seekParams = getActualSeekProgress(seekBar.getProgress());
//                    player.seekTo(seekParams[0], seekParams[1]);
//                    player.setPlayWhenReady(true);
//                }
//            }
//        });
//        videoTrimmerView.setRangeListener(this);
//        if (context.isFullTrim) {
//            videoTrimmerView.mLeftProgressPos = startPositionMillis;
//            videoTrimmerView.mRightProgressPos = endPositionMillis;
//            videoTrimmerView.mRangeSeekBarView.setStartEndTime(startPositionMillis, endPositionMillis);
//            videoTrimmerView.mRedProgressBarPos = startPositionMillis;
//            videoTrimmerView.mRangeSeekBarView.setSelectedMinValue(startPositionMillis);
//            videoTrimmerView.mRangeSeekBarView.setSelectedMaxValue(endPositionMillis);
//            videoTrimmerView.mRangeSeekBarView.invalidate();
//        }
//        setTimerText(videoTrimmerView.mLeftProgressPos, videoTrimmerView.mRightProgressPos);
//    }
//
//    private boolean isTrimmed(VideoModel video) {
//        return video.trimDuration > 0;
//    }
//
//    private void setVideoFrames() {
//        final VideoModel video = context.videoList.get(frameVideoPos);
//        videoThumbInBackground(context, video,
//                (bitmap, interval) -> {
//                    if (bitmap != null) {
//                        if (!isShowSection) {
//                            UiThreadExecutor.runTask("1", () -> {
//                                videoTrimmerView.mVideoThumbAdapter.addBitmaps(bitmap);
//                                frameCount++;
//                                if (frameCount == video.thumbCount) {
//                                    frameCount = 0;
//                                    frameVideoPos++;
//                                    if (frameVideoPos < context.videoList.size()) {
//                                        setVideoFrames();
//                                    }
//                                }
//                            }, 0L);
//                        }
//                    }
//                });
//    }
//
//    private void setVideoFramesForSingle(VideoModel video) {
//        videoThumbInBackgroundForSingle(context, video,
//                (bitmap, interval) -> {
//                    if (bitmap != null) {
//                        if (isShowSection) {
//                            UiThreadExecutor.runTask("2", () -> {
//                                videoTrimmerViewSingle.mVideoThumbAdapter.addBitmaps(bitmap);
//                            }, 0L);
//                        }
//                    }
//                });
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        int count = context.getSupportFragmentManager().getBackStackEntryCount();
//        if (count == 3 && isPaused) {
//            isPaused = false;
//            if (isShowSection) {
//                setSingleVideoPlay(true);
//                getVideoTrimView(context.videoList.get(selectedPos));
//            } else {
//                playStartPos = -1;
//                playEndPos = -1;
//                resetFullTrim();
//                resetSeekBar();
//                refreshTrimmerView();
//            }
//        }
//    }
//
//    private void initViews(View view) {
//        tvVideoTimerSelected = view.findViewById(R.id.tvVideoTimerSelected);
//        LinearLayout llVideoTrimSave = view.findViewById(R.id.llVideoTrimSave);
//        LinearLayout llVideoTrimCancel = view.findViewById(R.id.llVideoTrimCancel);
//        llSingleTrimBack = view.findViewById(R.id.llSingleTrimBack);
//        recyclerViewVideoList = view.findViewById(R.id.recyclerViewVideoList);
//        videoTrimmerView = view.findViewById(R.id.videoTrimmerView);
//        videoTrimmerViewSingle = view.findViewById(R.id.videoTrimmerViewSingle);
//        llVideoView = view.findViewById(R.id.llVideoView);
//        videoView = view.findViewById(R.id.videoView);
//        llRetake = view.findViewById(R.id.llRetake);
//        LinearLayout llVideoDelete = view.findViewById(R.id.llVideoDelete);
//        llVideoTrimmerMultiple = view.findViewById(R.id.llVideoTrimmerMultiple);
//        llVideoTrimmerSingle = view.findViewById(R.id.llVideoTrimmerSingle);
//        tvDuration = view.findViewById(R.id.tvDuration);
//        tvFullDuration = view.findViewById(R.id.tvFullDuration);
//        tvDurationSingle = view.findViewById(R.id.tvDurationSingle);
//        tvFullDurationSingle = view.findViewById(R.id.tvFullDurationSingle);
//        tempVideoList.clear();
//        for (int i = 0; i < context.videoList.size(); i++) {
//            tempVideoList.add(new VideoModel(context.videoList.get(i)));
//        }
//        llVideoTrimSave.setOnClickListener(this);
//        llRetake.setOnClickListener(this);
//        llVideoDelete.setOnClickListener(this);
//        llVideoTrimCancel.setOnClickListener(this);
//        llSingleTrimBack.setOnClickListener(this);
//    }
//
//    private void setVideoData() {
//        if (context.videoList.size() > 0) {
//            resetSelection();
//            int fourDp = (int) Utility.dpToPx(4, context);
//            recyclerViewVideoList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
//            recyclerViewVideoList.addItemDecoration(new EqualSpacingItemDecoration(fourDp, EqualSpacingItemDecoration.HORIZONTAL));
//            videoListAdapter = new VideoListAdapter();
//            recyclerViewVideoList.setAdapter(videoListAdapter);
//        }
//    }
//
//    private void resetSelection() {
//        for (int i = 0; i < context.videoList.size(); i++) {
//            VideoModel video = context.videoList.get(i);
//            video.isSelected = false;
////            try {
////                video.actualDuration = getDuration(video.videoFileList.get(0).filePath);
////            } catch (Exception e) {
////                video.actualDuration = video.duration;
////            }
////            video.actualDurationWithoutSpeed = video.actualDuration;
////            video.actualDuration = Double.parseDouble(String.format(Locale.ENGLISH, "%.1f", video.actualDuration / video.videoFileList.get(0).videoSpeed));
//        }
//        selectedPos = 0;
//        prevSelectedPos = 0;
//        context.videoList.get(0).isSelected = true;
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        isPaused = true;
//        stopPlaying();
//    }
//
//   /* public Bitmap createFlippedBitmap(Bitmap source, boolean xFlip, boolean yFlip) {
//        Matrix matrix = new Matrix();
//        matrix.postScale(xFlip ? -1 : 1, yFlip ? -1 : 1, source.getWidth() / 2f, source.getHeight() / 2f);
//        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
//    }*/
//
//    @Override
//    public void onDetach() {
//        cancelTimerCounter();
//        stopPlaying();
//        player.release();
//        super.onDetach();
//    }
//
//    @Override
//    public void onDestroy() {
//        cancelTimerCounter();
//        super.onDestroy();
//    }
//
//    public void videoThumbInBackground(final Context context, final VideoModel video, final SingleCallback<Bitmap, Integer> callback) {
//        multiTaskFilePath = video.videoFileList.get(0).filePath;
//        BackgroundExecutor.execute(new BackgroundExecutor.Task("1", 0L, "M_" + video.videoFileList.get(0).filePath) {
//            @Override
//            public void execute() {
//                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//                try {
//                    boolean isRetake = video.videoRetakeFileList.size() > 0;
//                    if (isRetake) {
//                        mediaMetadataRetriever.setDataSource(context, Uri.parse(video.videoRetakeFileList.get(0).filePath));
//                    } else {
//                        mediaMetadataRetriever.setDataSource(context, Uri.parse(video.videoFileList.get(0).filePath));
//                    }
//                    // Retrieve media data use microsecond
//                    int count = 0;
//                    long minusDuration = 0;
//                    boolean isTrimmed = isTrimmed(video);
//                    long interval;
//                    if (isTrimmed) {
//                        interval = (long) ((video.trimDuration * 1000) / (video.thumbCount - 1));
//                        if (isRetake) {
//                            for (int j = 0; j < video.videoRetakeFileList.size(); j++) {
//                                VideoFileModel videoFile = video.videoRetakeFileList.get(j);
//                                if (video.trimStartMillis > videoFile.trimEndMillis + minusDuration) {
//                                    count++;
//                                    minusDuration += video.videoRetakeFileList.get(count - 1).trimEndMillis;
//                                }
//                            }
//                            mediaMetadataRetriever.setDataSource(context, Uri.parse(video.videoRetakeFileList.get(count).filePath));
//                        } else {
//                            for (int j = 0; j < video.videoFileList.size(); j++) {
//                                VideoFileModel videoFile = video.videoFileList.get(j);
//                                if (video.trimStartMillis > videoFile.trimEndMillis + minusDuration) {
//                                    count++;
//                                    minusDuration += video.videoFileList.get(count - 1).trimEndMillis;
//                                }
//                            }
//                            mediaMetadataRetriever.setDataSource(context, Uri.parse(video.videoFileList.get(count).filePath));
//                        }
//
//                    } else {
//                        interval = (long) ((video.actualDurationWithoutSpeed * 1000) / (video.thumbCount - 1));
//                    }
//                    for (long i = 0; i < video.thumbCount; ++i) {
//                        if (!multiTaskFilePath.equalsIgnoreCase(video.videoFileList.get(0).filePath)) {
//                            break;
//                        }
//                        long frameTime;
//                        if (isTrimmed) {
//                            frameTime = interval * i + video.trimStartMillis - minusDuration;
//                        } else {
//                            frameTime = interval * i - minusDuration;
//                        }
//
//                        if (isRetake) {
//                            if (count < video.videoRetakeFileList.size() && frameTime > video.videoRetakeFileList.get(count).trimEndMillis) {
//                                count++;
//                                if (video.videoRetakeFileList.size() > count) {
//                                    mediaMetadataRetriever.setDataSource(context, Uri.parse(video.videoRetakeFileList.get(count).filePath));
//                                    minusDuration += video.videoRetakeFileList.get(count - 1).trimEndMillis;
//                                    frameTime = 0;
//                                }
//                            }
//                        } else {
//                            if (count < video.videoFileList.size() && frameTime > video.videoFileList.get(count).trimEndMillis) {
//                                count++;
//                                if (video.videoFileList.size() > count) {
//                                    mediaMetadataRetriever.setDataSource(context, Uri.parse(video.videoFileList.get(count).filePath));
//                                    minusDuration += video.videoFileList.get(count - 1).trimEndMillis;
//                                    frameTime = 0;
//                                }
//                            }
//                        }
//                        Utility.showLog("Tag", frameTime + "");
//                        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(frameTime * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//                        if (bitmap == null) continue;
//                        try {
//                            if (i == video.thumbCount - 1) {
//                                double width = video.thumbFloatCount + 1 - video.thumbCount;
//                                int bitmapWidth = (int) (THUMB_WIDTH * width);
//                                if (bitmapWidth < 1) {
//                                    bitmapWidth = 1;
//                                }
//                                bitmap = Bitmap.createScaledBitmap(bitmap, bitmapWidth, THUMB_HEIGHT, false);
//                            } else {
//                                bitmap = Bitmap.createScaledBitmap(bitmap, THUMB_WIDTH, THUMB_HEIGHT, false);
//                            }
//                            boolean isFront = false;
//                            if (isRetake && count < video.videoRetakeFileList.size()) {
//                                isFront = video.videoRetakeFileList.get(count).isFront;
//                            } else if (count < video.videoFileList.size()) {
//                                isFront = video.videoFileList.get(count).isFront;
//                            }
//                            if (isFront) {
//                                bitmap = Utility.createFlippedBitmap(bitmap, true, false);
//                            }
//                        } catch (final Throwable t) {
//                            t.printStackTrace();
//                        }
//                        if (multiTaskFilePath.equalsIgnoreCase(video.videoFileList.get(0).filePath)) {
//                            callback.onSingleCallback(bitmap, (int) interval);
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    try {
//                        mediaMetadataRetriever.release();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }
//
//    public void videoThumbInBackgroundForSingle(final Context context, final VideoModel video, final SingleCallback<Bitmap, Integer> callback) {
//        taskFilePath = video.videoFileList.get(0).filePath;
//        BackgroundExecutor.execute(new BackgroundExecutor.Task("2", 0L, video.videoFileList.get(0).filePath) {
//            @Override
//            public void execute() {
//                try {
//                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//                    boolean isRetake = video.videoRetakeFileList.size() > 0;
//                    if (isRetake) {
//                        mediaMetadataRetriever.setDataSource(context, Uri.parse(video.videoRetakeFileList.get(0).filePath));
//                    } else {
//                        mediaMetadataRetriever.setDataSource(context, Uri.parse(video.videoFileList.get(0).filePath));
//                    }
//                    // Retrieve media data use microsecond
//                    int count = 0;
//                    long minusDuration = 0;
//                    long interval = (long) ((video.actualDurationWithoutSpeed * 1000) / (TOTAL_THUMB_COUNT - 1));
//                    for (long i = 0; i < TOTAL_THUMB_COUNT; ++i) {
//                        if (!taskFilePath.equalsIgnoreCase(video.videoFileList.get(0).filePath)) {
//                            break;
//                        }
//                        long frameTime = interval * i - minusDuration;
//
//                        if (isRetake) {
//                            if (count < video.videoRetakeFileList.size() && frameTime > video.videoRetakeFileList.get(count).trimEndMillis) {
//                                count++;
//                                if (video.videoRetakeFileList.size() > count) {
//                                    mediaMetadataRetriever.setDataSource(context, Uri.parse(video.videoRetakeFileList.get(count).filePath));
//                                    minusDuration += video.videoRetakeFileList.get(count - 1).trimEndMillis;
//                                    frameTime = 0;
//                                }
//                            }
//                        } else {
//                            if (count < video.videoFileList.size() && frameTime > video.videoFileList.get(count).trimEndMillis) {
//                                count++;
//                                if (video.videoFileList.size() > count) {
//                                    mediaMetadataRetriever.setDataSource(context, Uri.parse(video.videoFileList.get(count).filePath));
//                                    minusDuration += video.videoFileList.get(count - 1).trimEndMillis;
//                                    frameTime = 0;
//                                }
//                            }
//                        }
//
//                        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(frameTime * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//                        if (bitmap == null) continue;
//                        try {
//                            bitmap = Bitmap.createScaledBitmap(bitmap, THUMB_WIDTH, THUMB_HEIGHT, false);
//                            boolean isFront = false;
//                            if (isRetake && count < video.videoRetakeFileList.size()) {
//                                isFront = video.videoRetakeFileList.get(count).isFront;
//                            } else if (count < video.videoFileList.size()) {
//                                isFront = video.videoFileList.get(count).isFront;
//                            }
//                            if (isFront) {
//                                bitmap = Utility.createFlippedBitmap(bitmap, true, false);
//                            }
//                        } catch (final Throwable t) {
//                            t.printStackTrace();
//                        }
//
//                        if (taskFilePath.equalsIgnoreCase(video.videoFileList.get(0).filePath)) {
//                            callback.onSingleCallback(bitmap, (int) interval);
//                        }
//                    }
//                    mediaMetadataRetriever.release();
//                } catch (final Throwable e) {
//                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
//                }
//            }
//        });
//    }
//
//    private void setVideoPlay() {
//        stopPlaying();
//        ArrayList<MediaItem> mediaItems = new ArrayList<>();
//        playerHelper.clear();
//        for (int i = 0; i < context.videoList.size(); i++) {
//            final VideoModel video = context.videoList.get(i);
//            boolean isTrimmed = isTrimmed(video);
//            boolean isFirst = true;
//            long lastEndMillis = 0;
//            boolean isRetake = video.videoRetakeFileList.size() > 0;
//            int size = isRetake ? video.videoRetakeFileList.size() : video.videoFileList.size();
//            for (int j = 0; j < size; j++) {
//                VideoFileModel videoFile = isRetake ? video.videoRetakeFileList.get(j) : video.videoFileList.get(j);
//                long startPositionMs, endPositionMs;
//                if (isTrimmed) {
//                    if (lastEndMillis > video.trimEndMillis) {
//                        break;
//                    }
//                    lastEndMillis += videoFile.trimEndMillis;
//                    if (video.trimStartMillis > lastEndMillis) {
//                        continue;
//                    }
//                    if (isFirst) {
//                        isFirst = false;
//                        startPositionMs = video.trimStartMillis - (lastEndMillis - videoFile.trimEndMillis);
//                    } else {
//                        startPositionMs = videoFile.trimStartMillis;
//                    }
//
//                    if (lastEndMillis > video.trimEndMillis) {
//                        endPositionMs = videoFile.trimEndMillis - (lastEndMillis - video.trimEndMillis);
//                    } else {
//                        endPositionMs = videoFile.trimEndMillis;
//                    }
//
//                    MediaItem.ClippingConfiguration clippingConfiguration = new MediaItem.ClippingConfiguration.Builder()
//                            .setStartPositionMs(startPositionMs)
//                            .setEndPositionMs(endPositionMs)
//                            .setStartsAtKeyFrame(true)
//                            .build();
//                    MediaItem builder = new MediaItem.Builder()
//                            .setUri(videoFile.filePath)
//                            .setClippingConfiguration(clippingConfiguration)
//                            .build();
//                    mediaItems.add(builder);
//                    PlayerHelperModel playerHelperModel = new PlayerHelperModel();
//                    playerHelperModel.isFront = videoFile.isFront;
//                    playerHelperModel.selectedPos = i;
//                    playerHelper.add(playerHelperModel);
//                } else {
//                    startPositionMs = videoFile.trimStartMillis;
//                    endPositionMs = videoFile.trimEndMillis;
//                    MediaItem.ClippingConfiguration clippingConfiguration = new MediaItem.ClippingConfiguration.Builder()
//                            .setStartPositionMs(startPositionMs)
//                            .setEndPositionMs(endPositionMs)
//                            .setStartsAtKeyFrame(true)
//                            .build();
//                    MediaItem builder = new MediaItem.Builder()
//                            .setUri(videoFile.filePath)
//                            .setClippingConfiguration(clippingConfiguration)
//                            .build();
//                    mediaItems.add(builder);
//                    PlayerHelperModel playerHelperModel = new PlayerHelperModel();
//                    playerHelperModel.isFront = videoFile.isFront;
//                    playerHelperModel.selectedPos = i;
//                    playerHelper.add(playerHelperModel);
//                }
//            }
//        }
//
//        player.addMediaItems(mediaItems);
//
//        player.prepare();
//        player.addListener(playListener);
//        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
////        player.setRepeatMode(Player.REPEAT_MODE_ALL);
//        videoView.setPlayer(player);
//        if (playStartPos == -1) {
//            selectedPos = 0;
//        } else {
//            selectedPos = playStartPos;
//        }
//        setAspectRatioAdjustment();
//        player.setPlayWhenReady(true);
//        setDashLine();
//        allTimerCounter();
//    }
//
//    private String getFilePath(VideoModel video) {
//        if (video.videoRetakeFileList.size() == 0) {
//            return video.videoFileList.get(0).filePath;
//        } else {
//            return video.videoRetakeFileList.get(0).filePath;
//        }
//    }
//
//    private void setDashLine() {
//        VideoModel video = context.videoList.get(selectedPos);
//        video.isSelected = true;
//        recyclerViewVideoList.smoothScrollToPosition(selectedPos);
//
//        if (prevSelectedPos != selectedPos) {
//            context.videoList.get(prevSelectedPos).isSelected = false;
//            prevSelectedPos = selectedPos;
//        }
//
//        videoListAdapter.notifyDataSetChanged();
//    }
//
//    private void setSingleVideoPlay(boolean isFirstTime) {
//        stopPlaying();
//        ArrayList<MediaItem> mediaItems = new ArrayList<>();
//        final VideoModel video = context.videoList.get(selectedPos);
//        if (isFirstTime) {
//            singleStartPos = 0;
//            if (video.videoRetakeFileList.size() > 0) {
//                isTrimmed = true;
//                startTrimMillis = 0L;
//                endTrimMillis = (long) (video.actualDuration * 1000);
//            } else if (video.trimStartMillis == 0 && video.trimEndMillis == 0) {
//                startTrimMillis = 0L;
//                endTrimMillis = (long) (video.actualDuration * 1000);
//            } else {
//                startTrimMillis = video.trimStartMillis;
//                endTrimMillis = video.trimEndMillis;
//            }
//        }
//        boolean isFirst = true;
//        long lastEndMillis = 0;
//        boolean isRetake = video.videoRetakeFileList.size() > 0;
//        int size = isRetake ? video.videoRetakeFileList.size() : video.videoFileList.size();
//        for (int i = 0; i < size; i++) {
//            long startPositionMs, endPositionMs;
//            VideoFileModel videoFile = isRetake ? video.videoRetakeFileList.get(i) : video.videoFileList.get(i);
//            if (lastEndMillis > endTrimMillis) {
//                break;
//            }
//            lastEndMillis += videoFile.trimEndMillis;
//            if (startTrimMillis > lastEndMillis) {
//                continue;
//            }
//            if (isFirst) {
//                singleStartPos = i;
//                isFirst = false;
//                startPositionMs = startTrimMillis - (lastEndMillis - videoFile.trimEndMillis);
//            } else {
//                startPositionMs = videoFile.trimStartMillis;
//            }
//
//            if (lastEndMillis > endTrimMillis) {
//                endPositionMs = videoFile.trimEndMillis - (lastEndMillis - endTrimMillis);
//            } else {
//                endPositionMs = videoFile.trimEndMillis;
//            }
//            MediaItem builder;
//            MediaItem.ClippingConfiguration clippingConfiguration = new MediaItem.ClippingConfiguration.Builder()
//                    .setStartPositionMs(startPositionMs)
//                    .setEndPositionMs(endPositionMs)
//                    .setStartsAtKeyFrame(true).build();
//            builder = new MediaItem.Builder()
//                    .setUri(videoFile.filePath)
//                    .setClippingConfiguration(clippingConfiguration)
//                    .build();
//            mediaItems.add(builder);
//        }
//        player.addMediaItems(mediaItems);
//        player.prepare();
//        player.seekTo(0L);
////        player.setRepeatMode(Player.REPEAT_MODE_ALL);
//        player.addListener(singlePlayListener);
//        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
//        videoView.setPlayer(player);
//        player.setPlayWhenReady(true);
//        tvVideoTimerSelected.setText("");
////        tvVideoSectionTimerSelected.setText(String.format("%ss Selected", getDuration()));
//    }
//
//    private Timer timer;
//
//    private void timerCounter() {
//        cancelTimerCounter();
//        timer = new Timer();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                context.runOnUiThread(() -> {
//                    if (isAdded() && !isSeeking) {
//                        int progress = getProgress();
//                        videoTrimmerViewSingle.videoSeekBar.setProgress(progress);
//                        tvDurationSingle.setText(Utility.milliToStringTrim(progress));
//                    }
//                });
//            }
//        };
//        timer.schedule(task, 0, 50);
//    }
//
//    private int[] getActualSeekProgress(int progress) {
//        int actualDuration = 0;
//        int mediaItemIndex = 0;
//        int seekProgress = 0;
//        int leftHandlePos = -1;
//
//        long leftProgressPos = videoTrimmerView.mLeftProgressPos;
//
//        for (int i = 0; i < player.getMediaItemCount(); i++) {
//            seekProgress = progress - actualDuration;
//            actualDuration += (player.getMediaItemAt(i).clippingConfiguration.endPositionMs - player.getMediaItemAt(i).clippingConfiguration.startPositionMs);
//
//            if (leftHandlePos == -1 && leftProgressPos < actualDuration) {
//                leftHandlePos = i;
//            }
//            if (progress <= actualDuration) {
//                mediaItemIndex = i;
//                if (leftHandlePos == mediaItemIndex) {
//                    seekProgress = progress - (int) leftProgressPos;
//                }
//                if (leftHandlePos != -1) {
//                    mediaItemIndex = mediaItemIndex - leftHandlePos;
//                }
//                break;
//            }
//        }
//        Utility.showLog("Tag", mediaItemIndex + " : " + seekProgress);
//        return new int[]{mediaItemIndex, seekProgress};
//    }
//
//    private int[] getActualSingleSeekProgress(int progress) {
//        int actualDuration = 0;
//        int mediaItemIndex = 0;
//        int seekProgress = 0;
//        int leftHandlePos = -1;
//
//        long leftProgressPos = videoTrimmerViewSingle.mLeftProgressPos;
//        VideoModel video = context.videoList.get(selectedPos);
//        boolean isRetake = video.videoRetakeFileList.size() > 0;
//        int size = isRetake ? video.videoRetakeFileList.size() : video.videoFileList.size();
//        for (int i = 0; i < size; i++) {
//            VideoFileModel videoFile = isRetake ? video.videoRetakeFileList.get(i) : video.videoFileList.get(i);
//            seekProgress = progress - actualDuration;
//            actualDuration += videoFile.trimEndMillis;
//
//            if (leftHandlePos == -1 && leftProgressPos < actualDuration) {
//                leftHandlePos = i;
//            }
//            if (progress <= actualDuration) {
//                mediaItemIndex = i;
//                if (leftHandlePos == mediaItemIndex) {
//                    seekProgress = progress - (int) leftProgressPos;
//                }
//                if (leftHandlePos != -1) {
//                    mediaItemIndex = mediaItemIndex - leftHandlePos;
//                }
//                break;
//            }
//        }
//        Utility.showLog("Tag", mediaItemIndex + " : " + seekProgress);
//        return new int[]{mediaItemIndex, seekProgress};
//    }
//
//    private int getProgress() {
//        int totalProgress;
//        int cumulativePos = 0;
//        for (int i = 0; i < player.getCurrentPeriodIndex(); i++) {
//            cumulativePos += (player.getMediaItemAt(i).clippingConfiguration.endPositionMs - player.getMediaItemAt(i).clippingConfiguration.startPositionMs);
//        }
//        if (isShowSection) {
//            totalProgress = (int) (startTrimMillis + cumulativePos + player.getCurrentPosition());
//        } else {
//            totalProgress = (int) (cumulativePos + player.getCurrentPosition());
//        }
//        return totalProgress;
//    }
//
//    private void allTimerCounter() {
//        cancelTimerCounter();
//        timer = new Timer();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                context.runOnUiThread(() -> {
//                    if (isAdded() && !isSeeking) {
//                        int progress = getProgress();
//                        videoTrimmerView.videoSeekBar.setProgress(progress);
//                        tvDuration.setText(Utility.milliToStringTrim(progress));
//                    }
//                });
//            }
//        };
//        timer.schedule(task, 0, 50);
//    }
//
//    private void cancelTimerCounter() {
//        if (timer != null) {
//            timer.cancel();
//        }
//    }
//
//    boolean isRangeSelectionConsider = false;
//
//    private void getVideoTrimView(VideoModel videoTrim) {
//        if (videoTrimmerViewSingle.mVideoThumbAdapter != null) {
//            videoTrimmerViewSingle.mVideoThumbAdapter.clear();
//        }
//        cancelTimerCounter();
//        isRangeSelectionConsider = false;
//        if (videoTrim.isFromGallery) {
//            llRetake.setVisibility(View.GONE);
//        } else {
//            llRetake.setVisibility(View.VISIBLE);
//        }
//        THUMB_WIDTH = (int) ((DeviceUtil.getDeviceWidth() - Utility.dpToPx(50f, context)) / TOTAL_THUMB_COUNT);
//        videoTrimmerViewSingle.mLeftProgressPos = 0;
//        videoTrimmerViewSingle.mRightProgressPos = (long) (videoTrim.actualDuration * 1000);
//        videoTrimmerViewSingle.initRangeSeekBarView(false);
//        videoTrimmerViewSingle.videoSeekBar.setMax((int) (videoTrim.actualDuration * 1000));
//        tvFullDurationSingle.setText(String.format("  /  %s", Utility.milliToStringTrim((int) (videoTrim.actualDuration * 1000))));
//        videoTrimmerViewSingle.videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
//                if (b) {
//                    if (progress < videoTrimmerViewSingle.mLeftProgressPos) {
//                        videoTrimmerViewSingle.videoSeekBar.setProgress((int) videoTrimmerViewSingle.mLeftProgressPos);
//                    } else if (progress > videoTrimmerViewSingle.mRightProgressPos) {
//                        videoTrimmerViewSingle.videoSeekBar.setProgress((int) videoTrimmerViewSingle.mRightProgressPos);
//                    }
//
//                    if (SystemClock.elapsedRealtime() - mLastClickTime < 50) {
//                        return;
//                    }
//                    mLastClickTime = SystemClock.elapsedRealtime();
//                    int[] seekParams = getActualSingleSeekProgress((int) (seekBar.getProgress()));
//                    player.seekTo(seekParams[0], seekParams[1]);
//                    tvDurationSingle.setText(Utility.milliToStringTrim(seekBar.getProgress()));
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                isSeeking = true;
//                player.setSeekParameters(SeekParameters.NEXT_SYNC);
//                player.setPlayWhenReady(false);
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                isSeeking = false;
//                if (videoView != null) {
//                    player.setSeekParameters(SeekParameters.EXACT);
//                    int[] seekParams = getActualSingleSeekProgress((int) (seekBar.getProgress()));
//                    player.seekTo(seekParams[0], seekParams[1]);
//                    player.setPlayWhenReady(true);
//                }
//            }
//        });
//
//        if (isTrimmed(videoTrim)) {
//            videoTrimmerViewSingle.mLeftProgressPos = videoTrim.trimStartMillis;
//            videoTrimmerViewSingle.mRightProgressPos = videoTrim.trimEndMillis;
//            videoTrimmerViewSingle.mRangeSeekBarView.setStartEndTime(videoTrim.trimStartMillis, videoTrim.trimEndMillis);
//            videoTrimmerViewSingle.mRedProgressBarPos = videoTrim.trimStartMillis;
//            videoTrimmerViewSingle.mRangeSeekBarView.setSelectedMinValue(videoTrim.trimStartMillis);
//            videoTrimmerViewSingle.mRangeSeekBarView.setSelectedMaxValue(videoTrim.trimEndMillis);
//            videoTrimmerViewSingle.mRangeSeekBarView.invalidate();
//        }
//
//        //Single video trimming
//        videoTrimmerViewSingle.setRangeListener(new SeekBarRangeChanged() {
//            @Override
//            public void onRangeChanged(long start, long end, boolean isLeft) {
//                Utility.printErrorLog("rangeSeekbar: VideoTrimFragment on onRangeChanged");
//                startTrimMillis = start;
//                endTrimMillis = end;
//                isTrimmed = true;
//                cancelTimerCounter();
//                setSingleVideoPlay(false);
//                timerCounter();
//            }
//
//            @Override
//            public void onRangeSelection(long start, long end, boolean isLeft) {
//                Utility.printErrorLog("rangeSeekbar: VideoTrimFragment on action move, start: " + start + " end: " + end);
//                tvDurationSingle.setText(Utility.milliToStringTrim(start));
//                if (isLeft) {
//                    player.seekTo((int) start);
//                    videoTrimmerViewSingle.videoSeekBar.setProgress((int) start);
//                } else {
//                    player.seekTo(end);
//                    videoTrimmerViewSingle.videoSeekBar.setProgress((int) end);
//                }
//                //
////                tvVideoSectionTimerSelected.setText(String.format("%ss Selected", getDuration(start, end)));
//            }
//
//            @Override
//            public void onRangeStart(boolean isLeft) {
//                cancelTimerCounter();
//                pausePlay();
//            }
//
//            @Override
//            public void onRangeEnd() {
//
//            }
//        });
//
//        setVideoFramesForSingle(videoTrim);
//        timerCounter();
//        new Handler(Looper.getMainLooper()).postDelayed(() -> isRangeSelectionConsider = true, 500);
//    }
//
//    private double getDuration(String filePath) {
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(String.valueOf(Uri.parse(filePath)));
//        long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
//        try {
//            retriever.release();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        double sec = duration / 1000f;
//        return Double.parseDouble(String.format(Locale.ENGLISH, "%.1f", sec));
//    }
//
//    @Override
//    public void onSelectRange(long l, long l1) {
//        if (isRangeSelectionConsider) {
//            startTrimMillis = l;
//            endTrimMillis = l1;
////            tvVideoSectionTimerSelected.setText(String.format(Locale.ENGLISH, "%ss Selected", getDuration()));
//        }
//        Utility.showLog("Tag-SelectRange", "start : " + l + " end : " + l1);
//    }
//
//    @Override
//    public void onSelectRangeEnd(long l, long l1) {
//        Utility.showLog("Tag-SelectRangeEnd", "start : " + l + " end : " + l1);
//        if (isRangeSelectionConsider) {
//            startTrimMillis = l;
//            endTrimMillis = l1;
//            cancelTimerCounter();
//            setSingleVideoPlay(false);
//            timerCounter();
//        }
//    }
//
//    @Override
//    public void onSelectRangeStart() {
//        player.pause();
//    }
//
//    private void updateVideoProgress() {
//        long duration = 0;
//        playStartPos = -1;
//        playEndPos = -1;
//        for (int i = 0; i < context.videoList.size(); i++) {
//            VideoModel video = context.videoList.get(i);
//            boolean isTrimmed = isTrimmed(video);
//            if (isTrimmed) {
//                duration += (context.videoList.get(i).trimDuration * 1000);
//            } else {
//                duration += (context.videoList.get(i).actualDuration * 1000);
//            }
//            if (videoTrimmerView.mLeftProgressPos <= duration && playStartPos == -1) {
//                selectedPos = i;
//                playStartPos = i;
//                if (isTrimmed) {
//                    video.fullTrimStartMillis = videoTrimmerView.mLeftProgressPos + (long) (context.videoList.get(i).trimDuration * 1000) - duration;
//                    video.fullTrimEndMillis = (long) (context.videoList.get(i).trimDuration * 1000);
//                } else {
//                    video.fullTrimStartMillis = videoTrimmerView.mLeftProgressPos + (long) (context.videoList.get(i).actualDuration * 1000) - duration;
//                    video.fullTrimEndMillis = (long) (context.videoList.get(i).actualDuration * 1000);
//                }
//            }
//
//            if (playStartPos >= 0 && videoTrimmerView.mRightProgressPos <= duration) {
//                playEndPos = i;
//                if (playStartPos != playEndPos) {
//                    video.fullTrimStartMillis = 0L;
//                }
//                if (isTrimmed) {
//                    video.fullTrimEndMillis = videoTrimmerView.mRightProgressPos + (long) (context.videoList.get(i).trimDuration * 1000) - duration;
//                } else {
//                    video.fullTrimEndMillis = videoTrimmerView.mRightProgressPos + (long) (context.videoList.get(i).actualDuration * 1000) - duration;
//                }
//                break;
//            }
//
//            if (playStartPos != i) {
//                video.fullTrimStartMillis = 0L;
//                if (isTrimmed) {
//                    video.fullTrimEndMillis = (long) (context.videoList.get(i).trimDuration * 1000);
//                } else {
//                    video.fullTrimEndMillis = (long) (context.videoList.get(i).actualDuration * 1000);
//                }
//            }
//        }
//        setVideoPlay();
//        setTimerText(videoTrimmerView.mLeftProgressPos, videoTrimmerView.mRightProgressPos);
//    }
//
//    private void setTimerText(long start, long end) {
//        double duration = Utility.getDuration(start, end);
//        Utility.showLog("logs1", String.valueOf(TOTAL_DURATION));
//        Utility.showLog("logs", String.valueOf((long) (duration * 1000)));
//        tvVideoTimerSelected.setText(String.format("%ss Selected", duration));
//    }
//
//    private void storeMultiTrimData(VideoModel video, long startTrimMillis, long endTrimMillis) {
//        video.fullTrimStartMillis = startTrimMillis;
//        video.fullTrimEndMillis = endTrimMillis;
//        video.fullTrimDuration = Utility.getDuration(startTrimMillis, endTrimMillis);
//        video.isFullTrim = true;
//        context.isFullTrim = true;
//    }
//
//    private void storeTrimData() {
//        VideoModel video = context.videoList.get(selectedPos);
//        video.trimStartMillis = startTrimMillis;
//        video.trimEndMillis = endTrimMillis;
//        video.trimDuration = getDuration();
//        video.isFullTrim = false;
//        context.isFullTrim = false;
//    }
//
//    private double getDuration() {
//        double sec = (endTrimMillis - startTrimMillis) / 1000f;
//        return Double.parseDouble(String.format(Locale.ENGLISH, "%.1f", sec));
//    }
//
//    long saveFStartTrimMillis = 0, saveFEndTrimMillis = 0;
//    long saveEStartTrimMillis = 0, saveEEndTrimMillis = 0;
//    int saveStartPos = 0, saveEndPos = 0;
//
//    private void save() {
//        if (isShowSection) {
//            VideoModel video = context.videoList.get(selectedPos);
//            video.isSkipMemory = true;
//            storeTrimData();
//            resetFullTrim();
//            calculationForFrames();
//        }
//
//        double currentDuration = Utility.getDuration(videoTrimmerView.mLeftProgressPos, videoTrimmerView.mRightProgressPos);
//        if (currentDuration < MIN_DURATION) {
//            if (context.videoOptions == CameraNewActivity.VideoOptions.PUBLIC || context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
//                Utility.showToast(context, context.getResources().getString(R.string.video_trim_validation));
//            } else {
//                Utility.showToast(context, context.getResources().getString(R.string.video_trim_validation_send));
//            }
//            return;
//        }
//        stopPlaying();
//        long duration = 0;
//        boolean isStartPosSelected = false, isEndPosSelected = false;
//        for (int i = 0; i < context.videoList.size(); i++) {
//            VideoModel video = context.videoList.get(i);
//            if (video.videoRetakeFileList.size() > 0) {
//                video.videoFileList.clear();
//                for (int j = 0; j < video.videoRetakeFileList.size(); j++) {
//                    video.videoFileList.add(new VideoFileModel(video.videoRetakeFileList.get(j)));
//                }
//                video.videoRetakeFileList.clear();
//            }
//            video.isFullTrim = false;
//            boolean isTrimmed = isTrimmed(video);
//            if (isTrimmed) {
//                duration += (context.videoList.get(i).trimDuration * 1000);
//            } else {
//                duration += (context.videoList.get(i).actualDuration * 1000);
//            }
//            if (!isStartPosSelected) {
//                if (videoTrimmerView.mLeftProgressPos <= duration) {
//                    saveStartPos = i;
//                    isStartPosSelected = true;
//                    if (isTrimmed) {
//                        saveFStartTrimMillis = videoTrimmerView.mLeftProgressPos + (long) (context.videoList.get(i).trimDuration * 1000) - duration;
//                        saveFEndTrimMillis = (long) (context.videoList.get(i).trimDuration * 1000);
//                    } else {
//                        saveFStartTrimMillis = videoTrimmerView.mLeftProgressPos + (long) (context.videoList.get(i).actualDuration * 1000) - duration;
//                        saveFEndTrimMillis = (long) (context.videoList.get(i).actualDuration * 1000);
//                    }
//                }
//            }
//            if (isStartPosSelected && !isEndPosSelected) {
//                if (videoTrimmerView.mRightProgressPos <= duration) {
//                    saveEndPos = i;
//                    if (isTrimmed) {
//                        saveEStartTrimMillis = context.videoList.get(i).trimStartMillis;
//                        saveEEndTrimMillis = videoTrimmerView.mRightProgressPos + (long) (context.videoList.get(i).trimDuration * 1000) - duration;
//                    } else {
//                        saveEStartTrimMillis = 0;
//                        saveEEndTrimMillis = videoTrimmerView.mRightProgressPos + (long) (context.videoList.get(i).actualDuration * 1000) - duration;
//                    }
//                    isEndPosSelected = true;
//                }
//            }
//        }
//
//        if (saveStartPos == saveEndPos) {
//            saveFEndTrimMillis = saveEEndTrimMillis;
//
//            VideoModel video = context.videoList.get(saveStartPos);
//            storeMultiTrimData(video, saveFStartTrimMillis, saveFEndTrimMillis);
//        } else {
//            VideoModel video = context.videoList.get(saveStartPos);
//            storeMultiTrimData(video, saveFStartTrimMillis, saveFEndTrimMillis);
//
//            VideoModel video1 = context.videoList.get(saveEndPos);
//            storeMultiTrimData(video1, saveEStartTrimMillis, saveEEndTrimMillis);
//        }
//        context.getSupportFragmentManager().popBackStack();
//        if (context.mergeAndPlayFragment != null) {
//            context.progressArray = new int[2];
//            context.videoProgress = 0;
//            context.mergeAndPlayFragment.isFirstTimeCoverImageGenerated = false;
//            context.mergeAndPlayFragment.startVideoPlaying(true);
//        }
//    }
//
//    private void resetFullTrim() {
//        context.isFullTrim = false;
//        for (int i = 0; i < context.videoList.size(); i++) {
//            VideoModel video = context.videoList.get(i);
//            video.fullTrimStartMillis = 0;
//            video.fullTrimEndMillis = 0;
//            video.isFullTrim = false;
//        }
//    }
//
//    @SuppressLint("NonConstantResourceId")
//    @Override
//    public void onClick(View view) {
//
//        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
//            return;
//        }
//        mLastClickTime = SystemClock.elapsedRealtime();
//
//        int id = view.getId();
//        if (id == R.id.llVideoTrimSave) {
//            save();
//        } else if (id == R.id.llRetake) {
//            stopPlaying();
//            context.isRetake = true;
//            context.retakeFileName = context.videoList.get(selectedPos).videoFileList.get(0).fileName;
//            context.retakePos = selectedPos;
//            context.videoList.get(selectedPos).trimDuration = 0L;
////                isRetakeFront = context.videoList.get(selectedPos).isFront;
////                isRetakeSpeed = context.videoList.get(selectedPos).videoFileList.get(0).videoSpeed;
////                isRetakeScale = context.videoList.get(selectedPos).videoZoomLevel;
//            context.goToRetakeFragment(selectedPos);
//        } else if (id == R.id.llVideoDelete) {
//            showDeleteClipAlert();
//                /*case R.id.btnNextVideoList:
//                double duration = getDuration(videoTrimmerView.mLeftProgressPos, videoTrimmerView.mRightProgressPos);
//                if (duration >= MIN_DURATION) {
//                    stopPlaying();
//                    HashMap<String, Object> map = new HashMap<String, Object>() {{
//                        put("user_id", context.userId);
//                        put("device_id", context.deviceId);
//                        put("event_id", context.uuid);
//                        if (previousEventTime != 0) {
//                            put("duration", (System.currentTimeMillis() - previousEventTime) / 1000);
//                        } else {
//                            put("duration", 0);
//                        }
//                        put("title", Constants.RECORD_NEXT_CLICKED);
//                    }};
//                    GenuInApplication.getInstance().createDataDogLogs(Constants.RECORD_NEXT_CLICKED, map);
//                    context.prevEventTime = System.currentTimeMillis();
//                    save();
//                } else {
//                    boolean isToastShown = SharedPrefUtils.getBoolPreference(context, Constants.PREF_IS_SHOWN_VIDEO_TRIM_VALIDATION);
//                    if (!isToastShown) {
//                        SharedPrefUtils.setBoolPreference(context, Constants.PREF_IS_SHOWN_VIDEO_TRIM_VALIDATION, true);
//                        if (context.videoOptions == CameraNewActivity.VideoOptions.PUBLIC || context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
//                            Utility.showToast(context, context.getResources().getString(R.string.video_trim_validation));
//                        } else {
//                            Utility.showToast(context, context.getResources().getString(R.string.video_trim_validation_send));
//                        }
//                    }
//                }
//                break;*/
//        } else if (id == R.id.llSingleTrimBack) {
//            stopPlaying();
//            playStartPos = -1;
//            playEndPos = -1;
//            VideoModel video = context.videoList.get(selectedPos);
//            video.isSkipMemory = true;
//            storeTrimData();
//            resetFullTrim();
//            manageViews(false);
//            resetSeekBar();
//            refreshTrimmerView();
//            sendEditClipsSingleClipClosed();
//        } else if (id == R.id.llVideoTrimCancel) {
//            if (isTrimmed || isDeleted) {
//                showBackConfirmAlert();
//            } else {
//                cancelTimerCounter();
//                stopPlaying();
//                resetData();
//                context.getSupportFragmentManager().popBackStack();
//                if (context.mergeAndPlayFragment != null) {
//                    context.mergeAndPlayFragment.startVideoPlaying(false);
//                }
//            }
//        }
//    }
//
//    private void sendEditClipsClosedClicked(boolean isChanged) {
//        Properties properties = new Properties();
//        properties.put(Constants.KEY_EVENT_RECORD_SCREEN, Constants.SCREEN_PREVIEW);
//        properties.put(Constants.KEY_EVENT_TARGET_SCREEN, Constants.SCREEN_EDIT_CLIPS);
//        properties.put(Constants.KEY_CLIP_EDITED, isChanged ? "yes" : "no");
//        GenuInApplication.getInstance().sendEventLogs(Constants.EDIT_CLIPS_CLOSED, properties);
//    }
//
//    private void sendEditClipsSingleClipOpened() {
//        Properties properties = new Properties();
//        properties.put(Constants.KEY_EVENT_RECORD_SCREEN, Constants.SCREEN_EDIT_CLIPS);
//        properties.put(Constants.KEY_EVENT_TARGET_SCREEN, Constants.NONE);
//        GenuInApplication.getInstance().sendEventLogs(Constants.EDIT_CLIPS_SINGLE_CLIP_OPENED, properties);
//    }
//
//    private void sendEditClipsSingleClipClosed() {
//        Properties properties = new Properties();
//        properties.put(Constants.KEY_EVENT_RECORD_SCREEN, Constants.SCREEN_EDIT_CLIPS);
//        properties.put(Constants.KEY_EVENT_TARGET_SCREEN, Constants.NONE);
//        GenuInApplication.getInstance().sendEventLogs(Constants.EDIT_CLIPS_SINGLE_CLIP_CLOSED, properties);
//    }
//
//    private void sendEditClipsDiscardDialogDeleteClicked() {
//        Properties properties = new Properties();
//        properties.put(Constants.KEY_EVENT_RECORD_SCREEN, Constants.SCREEN_EDIT_CLIPS);
//        properties.put(Constants.KEY_EVENT_TARGET_SCREEN, Constants.NONE);
//        GenuInApplication.getInstance().sendEventLogs(Constants.EDIT_CLIPS_DISCARD_DIALOG_DELETE_CLICKED, properties);
//    }
//
//    private void sendEditClipsDiscardDialogCancelClicked() {
//        Properties properties = new Properties();
//        properties.put(Constants.KEY_EVENT_RECORD_SCREEN, Constants.SCREEN_EDIT_CLIPS);
//        properties.put(Constants.KEY_EVENT_TARGET_SCREEN, Constants.NONE);
//        GenuInApplication.getInstance().sendEventLogs(Constants.EDIT_CLIPS_DISCARD_DIALOG_CANCEL_CLICKED, properties);
//    }
//
//    public void backManage() {
//        if (isShowSection) {
//            llSingleTrimBack.performClick();
//        } else {
//            if (isTrimmed || isDeleted) {
//                showBackConfirmAlert();
//            } else {
//                cancelTimerCounter();
//                stopPlaying();
//                resetData();
//                context.getSupportFragmentManager().popBackStack();
//                if (context.mergeAndPlayFragment != null) {
//                    context.mergeAndPlayFragment.startVideoPlaying(false);
//                }
//            }
//        }
//    }
//
//    private void resetData() {
//        context.isFullTrim = false;
//        context.videoList.clear();
//        for (int i = 0; i < tempVideoList.size(); i++) {
//            tempVideoList.get(i).videoRetakeFileList.clear();
//            context.videoList.add(new VideoModel(tempVideoList.get(i)));
//        }
//    }
//
//    private void showBackConfirmAlert() {
//        mBackConfirmDialog = new Dialog(context);
//        mBackConfirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        mBackConfirmDialog.setContentView(R.layout.common_simple_dialog_new);
//        mBackConfirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        mBackConfirmDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        mBackConfirmDialog.show();
//
//        CustomTextView tvTitle = mBackConfirmDialog.findViewById(R.id.dialog_title);
//        CustomTextView tvMsg = mBackConfirmDialog.findViewById(R.id.dialog_message);
//        CustomTextView btnCancel = mBackConfirmDialog.findViewById(R.id.dialog_btn_cancel);
//        CustomTextView btnYes = mBackConfirmDialog.findViewById(R.id.dialog_btn_yes);
//
//        btnYes.setText(getResources().getString(R.string.go_back));
//        tvTitle.setText(getResources().getString(R.string.diiscard_all_edits));
//        tvTitle.setVisibility(View.VISIBLE);
//        tvMsg.setText(getResources().getString(R.string.txt_back_confirm_sub));
//        tvMsg.setVisibility(View.GONE);
//        btnCancel.setOnClickListener(v -> mBackConfirmDialog.dismiss());
//        btnYes.setOnClickListener(v -> {
//            mBackConfirmDialog.dismiss();
//            cancelTimerCounter();
//            stopPlaying();
//            resetData();
//            context.getSupportFragmentManager().popBackStack();
//            if (context.mergeAndPlayFragment != null) {
//                context.mergeAndPlayFragment.startVideoPlaying(false);
//            }
//        });
//    }
//
//    private void stopPlaying() {
//        if (player != null) {
//            player.removeListener(playListener);
//            player.removeListener(singlePlayListener);
//            player.setPlayWhenReady(false);
//            player.stop();
//            player.clearMediaItems();
//        }
//    }
//
//    private void showDeleteClipAlert() {
//        mDeleteClipDialog = new Dialog(context);
//        mDeleteClipDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        mDeleteClipDialog.setContentView(R.layout.common_simple_dialog_new);
//        mDeleteClipDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        mDeleteClipDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        mDeleteClipDialog.show();
//
//        CustomTextView tvTitle = mDeleteClipDialog.findViewById(R.id.dialog_title);
//        CustomTextView tvMsg = mDeleteClipDialog.findViewById(R.id.dialog_message);
//        CustomTextView btnCancel = mDeleteClipDialog.findViewById(R.id.dialog_btn_cancel);
//        CustomTextView btnYes = mDeleteClipDialog.findViewById(R.id.dialog_btn_yes);
//
//        btnYes.setText(getResources().getString(R.string.txt_delete));
//        tvTitle.setText(getResources().getString(R.string.delete_this_clip));
//        tvTitle.setVisibility(View.VISIBLE);
//        tvMsg.setText(getResources().getString(R.string.txt_delete_clip_sub));
//        btnCancel.setOnClickListener(v -> {
//            mDeleteClipDialog.dismiss();
//            sendEditClipsDiscardDialogCancelClicked();
//        });
//        btnYes.setOnClickListener(v -> {
//            cancelTimerCounter();
//            mDeleteClipDialog.dismiss();
//            stopPlaying();
//            playStartPos = -1;
//            playEndPos = -1;
//            deleteSection();
//            sendEditClipsDiscardDialogDeleteClicked();
//        });
//    }
//
//    private void deleteSection() {
//        stopPlaying();
//        playStartPos = -1;
//        playEndPos = -1;
//        isDeleted = true;
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            context.videoList.remove(selectedPos);
//            if (context.videoList.size() == 0) {
//                context.isFullTrim = false;
//                context.getSupportFragmentManager().popBackStack();
//                context.getSupportFragmentManager().popBackStack();
//                if (context.mCameraFragment != null) {
//                    context.timeStamp = "";
//                    context.mCameraFragment.closeAndReopenCamera();
//                    context.mCameraFragment.setProgressAndVideo();
//                }
//            } else {
//                prevSelectedPos = 0;
//                resetFullTrim();
//                manageViews(false);
//                resetSeekBar();
//                refreshTrimmerView();
//            }
//        }, 100);
//    }
//
//    public void manageViews(boolean isShowSection) {
//        if (isShowSection) {
//            this.isShowSection = true;
//            llVideoTrimmerMultiple.setVisibility(View.GONE);
//            llVideoTrimmerSingle.setVisibility(View.VISIBLE);
//            videoTrimmerViewSingle.setVisibility(View.VISIBLE);
//            videoTrimmerView.setVisibility(View.GONE);
//        } else {
//            this.isShowSection = false;
//            llVideoTrimmerMultiple.setVisibility(View.VISIBLE);
//            llVideoTrimmerSingle.setVisibility(View.GONE);
//            videoTrimmerViewSingle.setVisibility(View.GONE);
//            videoTrimmerView.setVisibility(View.VISIBLE);
//        }
//        switchSingleAndMultiple(isShowSection);
//    }
//
//    private void switchSingleAndMultiple(boolean isShowSection) {
//        if (isShowSection) {
//            TranslateAnimation multiViewAnim = new TranslateAnimation(0, -(screenWidth + 200), 0, 0);
//            multiViewAnim.setDuration(300);
//            multiViewAnim.setFillAfter(false);
//
//            TranslateAnimation singleViewAnim = new TranslateAnimation(screenWidth + 200, 0, 0, 0);
//            singleViewAnim.setDuration(300);
//            singleViewAnim.setFillAfter(true);
//
//            llVideoTrimmerMultiple.startAnimation(multiViewAnim);
//            llVideoTrimmerSingle.startAnimation(singleViewAnim);
//        } else {
//            TranslateAnimation multiViewAnim = new TranslateAnimation(-(screenWidth + 200), 0, 0, 0);
//            multiViewAnim.setDuration(300);
//            multiViewAnim.setFillAfter(true);
//
//            TranslateAnimation singleViewAnim = new TranslateAnimation(0, screenWidth + 200, 0, 0);
//            singleViewAnim.setDuration(300);
//            singleViewAnim.setFillAfter(false);
//
//            llVideoTrimmerMultiple.startAnimation(multiViewAnim);
//            llVideoTrimmerSingle.startAnimation(singleViewAnim);
//        }
//    }
//
//    @Override
//    public void onRangeChanged(long start, long end, boolean isLeft) {
//        if (!isShowSection) {
//            updateVideoProgress();
//        }
//    }
//
//    @Override
//    public void onRangeSelection(long start, long end, boolean isLeft) {
//        if (!isShowSection) {
//            setTimerText(start, end);
//        }
//    }
//
//    @Override
//    public void onRangeStart(boolean isLeft) {
//        if (!isShowSection) {
//            cancelTimerCounter();
//            pausePlay();
//        }
//    }
//
//    @Override
//    public void onRangeEnd() {
//
//    }
//
//    private void pausePlay() {
//        if (videoView != null) {
//            player.pause();
//        }
//    }
//
//    private class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoListAdapterViewHolder> {
//
//        VideoListAdapter() {
//
//        }
//
//        class VideoListAdapterViewHolder extends RecyclerView.ViewHolder {
//            ImageView ivVideoThumbnail;
//            CustomTextView tvVideoDuration;
//            CardView viewVideoSelected;
//
//            VideoListAdapterViewHolder(View itemView) {
//                super(itemView);
//                ivVideoThumbnail = itemView.findViewById(R.id.ivVideoThumbnail);
//                tvVideoDuration = itemView.findViewById(R.id.tvVideoDuration);
//                viewVideoSelected = itemView.findViewById(R.id.viewVideoSelected);
//            }
//        }
//
//        @NotNull
//        @Override
//        public VideoListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_video_list, parent, false);
//            return new VideoListAdapterViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(final VideoListAdapterViewHolder viewHolder, final int position) {
//            VideoModel videoTrim = context.videoList.get(position);
//            viewHolder.ivVideoThumbnail.setTag(position);
//            boolean isRetake = videoTrim.videoRetakeFileList.size() > 0;
//            final boolean isFront = isRetake ? videoTrim.videoRetakeFileList.get(0).isFront : videoTrim.videoFileList.get(0).isFront;
//            Bitmap bmp = isRetake ? videoTrim.videoRetakeFileList.get(0).bmp : videoTrim.videoFileList.get(0).bmp;
//            if (bmp == null || videoTrim.isSkipMemory) {
//                videoTrim.isSkipMemory = false;
//                Glide.with(context.getApplicationContext()).asBitmap().load(getFilePath(videoTrim)).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(new CustomTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                        Bitmap bmp = resource;
//                        if (isFront) {
//                            bmp = Utility.createFlippedBitmap(resource, true, false);
//                        }
//                        viewHolder.ivVideoThumbnail.setImageBitmap(bmp);
//                        if (isRetake) {
//                            videoTrim.videoRetakeFileList.get(0).bmp = bmp;
//                        } else {
//                            videoTrim.videoFileList.get(0).bmp = bmp;
//                        }
//                    }
//
//                    @Override
//                    public void onLoadCleared(@Nullable Drawable placeholder) {
//
//                    }
//                });
//            } else {
//                viewHolder.ivVideoThumbnail.setImageBitmap(bmp);
//            }
//
////            if (videoTrim.trimDuration == 0) {
////                viewHolder.tvVideoDuration.setText(String.format("%ss", videoTrim.actualDuration));
////            } else {
////                viewHolder.tvVideoDuration.setText(String.format("%ss", videoTrim.trimDuration));
////            }
//
//            if (videoTrim.isSelected) {
//                viewHolder.viewVideoSelected.setVisibility(View.VISIBLE);
//            } else {
//                viewHolder.viewVideoSelected.setVisibility(View.INVISIBLE);
//            }
//
//            viewHolder.ivVideoThumbnail.setOnClickListener(v -> {
//                videoTrimmerView.cancelThreads();
//                videoTrimmerViewSingle.cancelThreads();
//                stopPlaying();
//                selectedPos = (int) v.getTag();
//                context.videoList.get(selectedPos).isSelected = true;
//                if (prevSelectedPos != selectedPos) {
//                    context.videoList.get(prevSelectedPos).isSelected = false;
//                    notifyDataSetChanged();
//                    prevSelectedPos = selectedPos;
//                }
//                manageViews(true);
//                setSingleVideoPlay(true);
//                getVideoTrimView(context.videoList.get(selectedPos));
//                sendEditClipsSingleClipOpened();
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return context.videoList.size();
//        }
//    }
//
//    private void setAspectRatioAdjustment() {
//        VideoModel video = context.videoList.get(selectedPos);
//        Size size = video.previewSize;
//
//        Utility.printErrorLog("~~~~ Ratio: video Width: " + size.getWidth() + " Height: " + size.getHeight());
//
//        float previewHeight = llVideoView.getHeight();
//        float previewWidth = (previewHeight * size.getHeight()) / size.getWidth();
//
//        Utility.printErrorLog("~~~~ Ratio: previewHeight Width: " + previewWidth + " Height: " + previewHeight);
//
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) llVideoView.getLayoutParams();
//        params.height = (int) previewHeight;
//        params.width = (int) previewWidth;
//        llVideoView.setLayoutParams(params);
//    }
//}