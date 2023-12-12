package com.begenuin.library.views.fragments;

import static com.begenuin.library.common.Utility.showLogException;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraCharacteristics;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.begenuin.begenuin.data.model.EditorColorsModel;
import com.begenuin.begenuin.ui.customview.draggableview.DraggableBaseCustomView;
import com.begenuin.begenuin.ui.customview.draggableview.DraggableImageViewFull;
import com.begenuin.library.common.GiphyGenuinManager;
import com.begenuin.library.common.ImageUtils;
import com.begenuin.library.common.VideoQuestionEditorDialog;
import com.begenuin.library.common.customViews.draggableview.DraggableImageView;
import com.begenuin.library.common.customViews.draggableview.DraggableTextView;
import com.begenuin.library.common.customViews.draggableview.IDraggableLayerInterface;
import com.begenuin.library.common.customViews.draggableview.draggablequestion.DraggableQuestionView;
import com.begenuin.library.R;
import com.begenuin.library.SDKInitiate;
import com.begenuin.library.common.Constants;
import com.begenuin.library.common.Utility;
import com.begenuin.library.common.VideoTextEditorDialog;
import com.begenuin.library.common.customViews.CustomEditText;
import com.begenuin.library.common.customViews.CustomIcon;
import com.begenuin.library.common.customViews.CustomTextView;
import com.begenuin.library.common.customViews.draggableview.DraggableLayers;
import com.begenuin.library.common.customViews.draggableview.Layer;
import com.begenuin.library.core.enums.LayerType;
import com.begenuin.library.core.enums.MediaType;
import com.begenuin.library.core.enums.PrivacyOptionsType;
import com.begenuin.library.core.enums.VideoConvType;
import com.begenuin.library.core.interfaces.ICustomDialogInterface;
import com.begenuin.library.core.interfaces.IQuestionCustomDialogInterface;
import com.begenuin.library.core.interfaces.ResponseListener;
import com.begenuin.library.data.model.EditorFontModel;
import com.begenuin.library.data.model.ImageStickerModel;
import com.begenuin.library.data.model.PlayerHelperModel;
import com.begenuin.library.data.model.QuestionModel;
import com.begenuin.library.data.model.VideoFileModel;
import com.begenuin.library.data.model.VideoModel;
import com.begenuin.library.data.model.VideoParamsModel;
import com.begenuin.library.data.remote.BaseAPIService;
import com.begenuin.library.views.activities.CameraNewActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.gifdecoder.StandardGifDecoder;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.material.button.MaterialButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */

public class VideoMergeAndPlayFragment extends Fragment implements View.OnClickListener, ICustomDialogInterface,
        IDraggableLayerInterface,
        IQuestionCustomDialogInterface {

    private CameraNewActivity context;
    private DraggableLayers draggableLayers;
    private String destinationPath, firstVideoPath;
    ExoPlayer player;
    private boolean isFront = false;
    private int cameraFacing;
    private long firstVideoTrimMillis;
    private ProgressBar progressTimer;
    private ImageView imgCloseFailure, ivPrivacyType;
    private LinearLayout ivVideoMergeDownload, ivVideoMergeBack;
    private RelativeLayout relativeTutorialSend;
    private LinearLayout llAddLinkInfo, llCloseLink;
    private CustomIcon llAddLink, llEditClips, llAddSticker, llGiphySticker;
    private CustomEditText etAddLink;
    private MaterialButton btnPublish;
    public RelativeLayout reactionBlurLayout;
    private MaterialButton btnGotItFailure;
    private TextView tvReactionFailure, tvDeleteSticker, tvUserName, tvGenuinLogo, tvFromUserName, tvFromCameraRoll;
    private TextView tvUserNameWaterMark, tvFullNameWaterMark, tvBioWaterMark;
    private CustomTextView txtInvalidDomain;
    int startPos = -1, endPos = -1;
    private LinearLayout llPrivateVideoMsg;
    private CustomTextView tvPrivateMsg;
    private CircleImageView ivWaterMarkProfile;
    private ImageView ivAudioOnly;
    private TextView tvTranscribedText;
    private LottieAnimationView lottieAudioProgress;
    private RelativeLayout rlHeaderMain;
    private int audioPos = 0;

    private boolean isPaused = false;
    public StyledPlayerView videoView;
    private String module = "";
    private long startMills, endMillis;
    private boolean isNeedToPerformAction = false;
    private String link = "";
    private boolean isValidatedUrl;
    private long totalDuration;
    private final long NEAR_FUTURE_TIME = 20 * 1000 * 60;
    private final double MIN_DURATION = 4.0;
    private ActivityResultLauncher<Intent> loginActivityResultLauncher;
    private LinearLayout rlHeader;
    private RelativeLayout rlStickers, rlAudioImage;
    private LinearLayout llDeleteSticker;
    private LinearLayout llAudioHeader;
    private TextView tvAudioUserName, tvAudioBio;
    private ArrayList<ImageStickerModel> draggableViewsList;
    private DraggableBaseCustomView currentDraggableView = null;
    private DraggableQuestionView currentDraggableQuestionView = null;
    private Layer currentLayer = null;

    private EditorFontModel selectedFontModel = null;
    private EditorColorsModel selectedColorModel = null;
    private MaterialButton btnSendTutorial;
    private CustomTextView txtSendLabel;
    private ArrayList<PlayerHelperModel> playerHelper;
    private String textToUpdate = "";
    private boolean isVibrateShouldPlay = false;
    private final HashMap<Integer, EditorColorsModel> colorsModelHashMap = new HashMap<>();
    private final HashMap<Integer, EditorFontModel> fontModelHashMap = new HashMap<>();
    private Timer timer;

    private VideoTextEditorDialog dialog;
    private boolean isValidURLNeedToCall = true;
    private boolean isDownloadClick = false;

    private QuestionModel questionModel;
    private float maxQuestionFontSize = Constants.QUESTION_FONT_MAX_DEFAULT_SIZE;
    private float currentFontSize = Constants.QUESTION_FONT_MAX_DEFAULT_SIZE;
    private float minFontSize = Constants.QUESTION_FONT_MIN_DEFAULT_SIZE;
    private CardView cardCoverPhoto;
    private ImageView ivCoverPhoto;
    private View layoutSuccessPage;
    public boolean isFirstTimeCoverImageGenerated = false;
    private boolean isCoverCommandExecuted = false;
    private boolean isPublishClicked = false;
    private boolean isPublished = false;
    //private WorkManager workManager;
    private final float THRESHOLD_PER = 0.7f;
    private RelativeLayout rlMain;
    private LinearLayout llProgressBar;
    private CardView cardView;
    private boolean isAllFileGenerated;
    private String fullTranscribedText = "";
    private int transcribeMaxWidth, transcribeY;
    private Dialog mStartOverDialog;
    private boolean shouldAskStartOver = true;

    public VideoMergeAndPlayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (CameraNewActivity) getActivity();
        if (context != null) {
            context.getWindow().setBackgroundDrawableResource(R.color.colorWhite);
        }
        draggableViewsList = new ArrayList();
        playerHelper = new ArrayList();
        questionModel = context.selectedQuestion;
        //EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_merge_and_play, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initControls(view);
        setListeners();
        registerActivityCallBack();
        setPublishButton();
        rlMain.post(() -> {
            setAspectRationAdjustment();
            cardView.invalidate();
            cardView.post(() -> {
                startVideoPlaying((questionModel == null && context.selectedTopic == null));
                if (context.isTextReply()) {
                    rlStickers.performClick();
                }
            });
        });

        new Handler().postDelayed(() -> {
//            if (context.isPhotoReply()) {
//                addImage();
//                addQuestion();
//                addTopic();
//            }
//            if (context.isAudioReply()) {
//                addAudio();
//                addQuestion();
//                addTopic();
//            }
            addQuestion();
            addTopic();
        }, 300);
    }

//    @Subscribe
//    public void onCompressionCompletedPreview(CompressionCompletedPreviewEvent compressionCompletedPreview) {
//        boolean isCompleted = compressionCompletedPreview.isCompleted;
//        context.runOnUiThread(() -> {
//            if (isCompleted) {
//                String path = compressionCompletedPreview.path;
//                String whichSession = compressionCompletedPreview.whichSession;
//                if (path.equalsIgnoreCase(context.coverPhotoPath)) {
//                    isCoverCommandExecuted = true;
//                    if (ivCoverPhoto != null) {
//                        Glide.with(context).asDrawable().load(context.coverPhotoPath).diskCacheStrategy(DiskCacheStrategy.NONE)
//                                .skipMemoryCache(true).into(ivCoverPhoto);
//                    }
//                    if (isPublishClicked) {
//                        isPublishClicked = false;
//                        if (!context.from.equalsIgnoreCase(Constants.FROM_REACTION)) {
//                            BaseAPIService.dismissProgressDialog();
//                        }
//                        // Internet condition removed so user can proceed with next step without internet also
//                        publishClickManage();
//                    } else if (isDownloadClick) {
//                        isDownloadClick = false;
//                        startDownload();
//                    }
//                } else if (!TextUtils.isEmpty(destinationPath) && path.equalsIgnoreCase(destinationPath)) {
//                    context.isCompressionDone = true;
//                    if (isDownloadClick) {
//                        isDownloadClick = false;
//                        startDownload();
//                    }
//                    Utility.showLog("TAG", "Compression Done received");
//                } else if (whichSession.equalsIgnoreCase(Constants.SESSION_DOWNLOAD)) {
//                    BaseAPIService.dismissProgressDialog();
//                    if (player != null) {
//                        player.setPlayWhenReady(true);
//                    }
//                    downloadVideo(context.downloadedVideoPath);
//                }
//            } else {
//                if (BaseAPIService.isShowingProgressDialog()) {
//                    BaseAPIService.dismissProgressDialog();
//                }
//                if (player != null) {
//                    player.setPlayWhenReady(true);
//                }
//            }
//        });
//    }

    private void initControls(View view) {
        //workManager = WorkManager.getInstance(context);
        rlHeaderMain = view.findViewById(R.id.rlHeaderMain);
        videoView = view.findViewById(R.id.videoView);
        progressTimer = view.findViewById(R.id.progressTimer);
        ivVideoMergeBack = view.findViewById(R.id.ivVideoMergeBack);
        ivVideoMergeDownload = view.findViewById(R.id.ivVideoMergeDownload);
        btnPublish = view.findViewById(R.id.btnPublish);
        llAddLinkInfo = view.findViewById(R.id.llAddLinkInfo);
        llAddLink = view.findViewById(R.id.llAddLink);
        etAddLink = view.findViewById(R.id.etAddLink);
        llCloseLink = view.findViewById(R.id.llCloseLink);

        llEditClips = view.findViewById(R.id.llEditClips);

        reactionBlurLayout = view.findViewById(R.id.reactionBlurLayout);
        btnGotItFailure = view.findViewById(R.id.btnGotItFailure);
        tvReactionFailure = view.findViewById(R.id.tvReactionFailure);
        imgCloseFailure = view.findViewById(R.id.imgCloseFailure);

        txtInvalidDomain = view.findViewById(R.id.txt_invalid_domain);
        txtInvalidDomain.setVisibility(View.GONE);

        relativeTutorialSend = view.findViewById(R.id.relative_tutorial_send);
        relativeTutorialSend.setVisibility(View.GONE);
        btnSendTutorial = view.findViewById(R.id.btnSendTutorial);
        txtSendLabel = view.findViewById(R.id.txt_send_label);
        llPrivateVideoMsg = view.findViewById(R.id.llPrivateVideoMsg);
        tvPrivateMsg = view.findViewById(R.id.tvPrivateMsg);

        rlStickers = view.findViewById(R.id.rlStickers);
        llDeleteSticker = view.findViewById(R.id.llDeleteSticker);
        llAddSticker = view.findViewById(R.id.llAddSticker);
        llGiphySticker = view.findViewById(R.id.llGiphySticker);
        rlHeader = view.findViewById(R.id.llHeader);
        tvDeleteSticker = view.findViewById(R.id.tvDeleteSticker);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserNameWaterMark = view.findViewById(R.id.tvUserNameWaterMark);
        tvFullNameWaterMark = view.findViewById(R.id.tvFullNameWaterMark);
        tvBioWaterMark = view.findViewById(R.id.tvBioWaterMark);
        tvGenuinLogo = view.findViewById(R.id.tvGenuinLogo);
        ivWaterMarkProfile = view.findViewById(R.id.ivWaterMarkProfile);
        tvFromUserName = view.findViewById(R.id.tvFromUserName);
        tvFromCameraRoll = view.findViewById(R.id.tvFromCameraRoll);
        cardCoverPhoto = view.findViewById(R.id.cardCoverPhoto);
        ivCoverPhoto = view.findViewById(R.id.ivCoverPhoto);
        layoutSuccessPage = view.findViewById(R.id.layoutSuccessPage);

        draggableLayers = new DraggableLayers(context);
        draggableLayers.setListener(this);

        rlMain = view.findViewById(R.id.rlMain);
        llProgressBar = view.findViewById(R.id.llProgressBar);
        cardView = view.findViewById(R.id.cardView);
        ivPrivacyType = view.findViewById(R.id.ivPrivacyType);

        ImageView ivPhotoOnly = view.findViewById(R.id.ivPhotoOnly);
        RelativeLayout rlAudioOnly = view.findViewById(R.id.rlAudioOnly);
        ivAudioOnly = view.findViewById(R.id.ivAudioOnly);
        tvTranscribedText = view.findViewById(R.id.tvTranscribedText);
        lottieAudioProgress = view.findViewById(R.id.lottieAudioProgress);
        llAudioHeader = view.findViewById(R.id.llAudioHeader);
        ImageView ivAudioGif = view.findViewById(R.id.ivAudioGif);
        CircleImageView ivAudioProfile = view.findViewById(R.id.ivAudioProfile);
        LottieAnimationView animationAudio = view.findViewById(R.id.animationAudio);
        tvAudioUserName = view.findViewById(R.id.tvAudioUserName);
        tvAudioBio = view.findViewById(R.id.tvAudioBio);
        rlAudioImage = view.findViewById(R.id.rlAudioImage);

        if (context.isReplyReactionWithoutVideo()) {
            cardCoverPhoto.setVisibility(View.GONE);
            llEditClips.setVisibility(View.GONE);
        }

        if (context.isPhotoReply()) {
            ivPhotoOnly.setVisibility(View.VISIBLE);
        } else if (context.isAudioReply()) {
            rlAudioOnly.setVisibility(View.VISIBLE);
            llAudioHeader.setVisibility(View.VISIBLE);
            Glide.with(context).asGif().load(R.drawable.audio_gif).into(ivAudioGif);
          //  MembersModel user = Utility.getCurrentUserObject(context, "");
            String userName = "test";
            String bio = "Monali";
            if (!TextUtils.isEmpty(userName)) {
                tvAudioUserName.setText(String.format("@%s", userName));
            } else {
                tvAudioUserName.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(bio)) {
                tvAudioBio.setText(bio);
            } else {
                tvAudioBio.setVisibility(View.GONE);
            }

//            try {
//                if (user.isAvatar()) {
//                    int res = context.getResources().getIdentifier(user.getProfileImage(),
//                            "raw", context.getPackageName());
//                    Drawable color = new ColorDrawable(context.getResources().getColor(LottieAnimModel.getMapData().get(res), null));
//                    ivAudioProfile.setImageDrawable(color);
//                    animationAudio.setVisibility(View.VISIBLE);
//                    animationAudio.setAnimation(res);
//                    animationAudio.playAnimation();
//                } else {
//                    animationAudio.setVisibility(View.GONE);
//                    if (!TextUtils.isEmpty(user.getProfileImageM())) {
//                        Utility.displayProfileImage(context, user.getProfileImageM(), ivAudioProfile);
//                    } else {
//                        Utility.displayProfileImage(context, user.getProfileImage(), ivAudioProfile);
//                    }
//                }
//            } catch (Exception e) {
//                showLogException(e);
//            }
//        }

            tvBioWaterMark.post(() -> {
                RelativeLayout.LayoutParams userNameLayoutParams = (RelativeLayout.LayoutParams) tvUserNameWaterMark.getLayoutParams();
                RelativeLayout.LayoutParams fullNameLayoutParams = (RelativeLayout.LayoutParams) tvFullNameWaterMark.getLayoutParams();
                RelativeLayout.LayoutParams bioLayoutParams = (RelativeLayout.LayoutParams) tvBioWaterMark.getLayoutParams();
                userNameLayoutParams.width = 420;
                fullNameLayoutParams.width = 420;
                bioLayoutParams.width = 420;
                tvBioWaterMark.invalidate();
                if (context.isAudioReply()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        transcribeMaxWidth = (int) (Utility.getScreenWidthHeight(context)[0] - Utility.dpToPx(84, context));
                    }
                    transcribeY = (int) (llAudioHeader.getHeight() + Utility.dpToPx(16, context));
                }
                //saveFilesIfNotExist();
            });
        }
    }

    private void setListeners() {
        ivVideoMergeBack.setOnClickListener(this);
        ivVideoMergeDownload.setOnClickListener(this);
        btnPublish.setOnClickListener(this);
        btnSendTutorial.setOnClickListener(this);
        imgCloseFailure.setOnClickListener(this);
        btnGotItFailure.setOnClickListener(this);
        relativeTutorialSend.setOnClickListener(this);
        llAddLink.setOnClickListener(this);
        llCloseLink.setOnClickListener(this);
        llEditClips.setOnClickListener(this);
        llAddSticker.setOnClickListener(this);
        rlStickers.setOnClickListener(this);
        cardCoverPhoto.setOnClickListener(this);
        llGiphySticker.setOnClickListener(this);
        addLinkListeners();
    }

    private final Player.Listener playerListener = new Player.Listener() {

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            if (playbackState == Player.STATE_ENDED) {
                if (player != null) {
                    player.seekTo(0, 0L);
                    player.setPlayWhenReady(true);
                }
            }
        }

        @Override
        public void onRenderedFirstFrame() {
            try {
                boolean isFront = getRelativeFrontBack();
                Utility.showLog("isFront", isFront + " ");
                if (videoView.getVideoSurfaceView() != null) {
                    if (isFront) {
                        videoView.getVideoSurfaceView().setScaleX(-1);
                    } else {
                        videoView.getVideoSurfaceView().setScaleX(1);
                    }
                    // onRenderedFirstFrame is called before onMediaItemTransition sometimes so we need to have below code.
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (player != null) {
                            boolean isFront1 = getRelativeFrontBack();
                            if (isFront1) {
                                videoView.getVideoSurfaceView().setScaleX(-1);
                            } else {
                                videoView.getVideoSurfaceView().setScaleX(1);
                            }
                        }
                    }, 100);
                }
            } catch (Exception e) {
                showLogException(e);
            }
        }
    };

    private boolean getRelativeFrontBack() {
        int currentPlayPos = player.getCurrentPeriodIndex();
        return playerHelper.get(currentPlayPos).isFront;
    }

    private void addLinkListeners() {
        etAddLink.setKeyImeChangeListener((keyCode, event) -> {
            Utility.hideKeyboard(context, etAddLink);
            callForValidURL();
        });

        etAddLink.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO) {
                Utility.hideKeyboard(context, etAddLink);
                callForValidURL();
            }
            return false;
        });

        etAddLink.setOnFocusChangeListener((view, b) -> {
            Utility.showLog("OnFocus", "Changed");
            if (!b && isValidURLNeedToCall) {
                callForValidURL();
            }
            isValidURLNeedToCall = true;
        });

        etAddLink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    txtInvalidDomain.setVisibility(View.GONE);
                }
            }
        });
    }

    public void finishActivity() {
        context.cleanUpMemory();
        context.setResult(Activity.RESULT_CANCELED);
        context.finish();
        context.overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }

    private boolean isTrimmed(VideoModel video) {
        return video.trimDuration > 0;
    }

    private void registerActivityCallBack() {
        loginActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Constants.IS_FEED_REFRESH = true;
                    }
                });
    }

    public void startVideoPlaying(boolean isNeedToMerge) {
        boolean isStartSelected = false;
        startPos = -1;
        endPos = -1;
        for (int i = 0; i < context.videoList.size(); i++) {
            VideoModel video = context.videoList.get(i);
            if (video.isFullTrim && !isStartSelected) {
                startPos = i;
                firstVideoPath = video.videoFileList.get(0).filePath;
                isFront = video.videoFileList.get(0).isFront;
                if (isFront) {
                    cameraFacing = CameraCharacteristics.LENS_FACING_FRONT;
                } else {
                    cameraFacing = CameraCharacteristics.LENS_FACING_BACK;
                }
                isStartSelected = true;
            } else if (isStartSelected) {
                if (video.isFullTrim) {
                    endPos = i;
                    break;
                }
            }
        }

        if (startPos >= 0 && endPos == -1) {
            endPos = startPos;
        }

        if (startPos != endPos) {
            firstVideoTrimMillis = (long) (context.videoList.get(startPos).fullTrimStartMillis * context.videoList.get(startPos).videoFileList.get(0).videoSpeed);
        } else {
            firstVideoTrimMillis = (long) ((context.videoList.get(startPos).fullTrimStartMillis + context.videoList.get(startPos).trimStartMillis) * context.videoList.get(startPos).videoFileList.get(0).videoSpeed);
        }
        setVideoPlay();
        if (isNeedToMerge) {
            executeMergeVideoCommand();
        }
    }

    private String getVideoResolution() {
        VideoModel video = context.videoList.get(0);
        Size size = video.previewSize;
        float ratio20by9 = 20f / 9f;
        float ratio19by9 = 19f / 9f;
        float ratio18by9 = 18f / 9f;
        float ratio17by9 = 17f / 9f;
        float ratio16by9 = 16f / 9f;

        float deviceRatio = (size.getWidth() * 1.0f) / (size.getHeight() * 1.0f);

        String outPutResolution;

        if (deviceRatio >= ratio20by9) {
            outPutResolution = "800x1776";
        } else if (deviceRatio >= ratio19by9) {
            outPutResolution = "760x1604";
        } else if (deviceRatio >= ratio18by9) {
            outPutResolution = "736x1472";
        } else if (deviceRatio >= ratio17by9) {
            outPutResolution = "720x1354";
        } else if (deviceRatio >= ratio16by9) {
            outPutResolution = "688x1220";
        } else {
            outPutResolution = "640x960";
        }

        return outPutResolution;
    }

    private String getVideoResolutionRatio() {
        VideoModel video = context.videoList.get(0);
        Size size = video.previewSize;
        float ratio20by9 = 20f / 9f;
        float ratio19by9 = 19f / 9f;
        float ratio18by9 = 18f / 9f;
        float ratio17by9 = 17f / 9f;
        float ratio16by9 = 16f / 9f;

        float deviceRatio = (size.getWidth() * 1.0f) / (size.getHeight() * 1.0f);

        String outPutResolution;

        if (deviceRatio >= ratio20by9) {
            outPutResolution = "0.45";
        } else if (deviceRatio >= ratio19by9) {
            outPutResolution = "0.4737";
        } else if (deviceRatio >= ratio18by9) {
            outPutResolution = "0.5";
        } else if (deviceRatio >= ratio17by9) {
            outPutResolution = "0.5294";
        } else if (deviceRatio >= ratio16by9) {
            outPutResolution = "0.5625";
        } else {
            outPutResolution = "0.6666";
        }

        return outPutResolution;
    }

    private Size getVideoSize() {
        VideoModel video = context.videoList.get(startPos);
        return video.previewSize;
    }

    private String getVideoAspectRatio() {
        Size mVideoSize = getVideoSize();
        String ratio;

        float ratio20by9 = 20f / 9f;
        float ratio19by9 = 19f / 9f;
        float ratio18by9 = 18f / 9f;
        float ratio17by9 = 17f / 9f;
        float ratio16by9 = 16f / 9f;

        float deviceRatio = (mVideoSize.getWidth() * 1.0f) / (mVideoSize.getHeight() * 1.0f);

        if (deviceRatio >= ratio20by9) {
            ratio = "20:9";
        } else if (deviceRatio >= ratio19by9) {
            ratio = "19:9";
        } else if (deviceRatio >= ratio18by9) {
            ratio = "18:9";
        } else if (deviceRatio >= ratio17by9) {
            ratio = "17:9";
        } else if (deviceRatio >= ratio16by9) {
            ratio = "16:9";
        } else {
            ratio = "3:2";
        }

        return ratio;
    }

//    private void goToPublishVideoFragment() {
//        SharedPrefUtils.setIntPreference(context, Constants.NO_OF_PARTS, endPos - startPos + 1);
//        VideoModel video = context.videoList.get(startPos);
//        Size mVideoSize = video.previewSize;
//        context.publishVideoFragment = new PublishVideoFragment();
//        Bundle args = new Bundle();
//        args.putBoolean("isEdit", false);
//        args.putString("videoFilePath", firstVideoPath);
//        args.putLong("firstVideoTrimMillis", firstVideoTrimMillis == 0 ? 1 : firstVideoTrimMillis);
//        args.putString("compressVideoFilePath", destinationPath);
//        args.putInt("videoSizeH", mVideoSize.getHeight());
//        args.putInt("videoSizeW", mVideoSize.getWidth());
//        args.putInt("videoDuration", (int) totalDuration);
//        args.putInt("cameraFacing", cameraFacing);
//        args.putString("link", link);
//        context.publishVideoFragment.setArguments(args);
//        context.getSupportFragmentManager().beginTransaction().add(R.id.content, context.publishVideoFragment).addToBackStack("Publish").commit();
//    }

    private void goToRTFragment() {
        //SharedPrefUtils.setIntPreference(context, Constants.NO_OF_PARTS, endPos - startPos + 1);
        VideoModel video = context.videoList.get(startPos);
        Size mVideoSize = video.previewSize;
        context.roundTableVideoFragment = new RoundTableVideoFragment();
        Bundle args = new Bundle();
        args.putBoolean("isEdit", false);
        args.putString("videoFilePath", firstVideoPath);
        args.putLong("firstVideoTrimMillis", firstVideoTrimMillis == 0 ? 1 : firstVideoTrimMillis);
        args.putString("compressVideoFilePath", destinationPath);
        args.putInt("videoSizeH", mVideoSize.getHeight());
        args.putInt("videoSizeW", mVideoSize.getWidth());
        args.putInt("videoDuration", (int) totalDuration);
        args.putInt("cameraFacing", cameraFacing);
        args.putString("link", link);
        if(context.isFromTemplate) args.putString("template_title", context.rtName); args.putString("template_description", context.rtDesc); args.putInt("template_id", context.templateId);
        context.roundTableVideoFragment.setArguments(args);
        context.getSupportFragmentManager().beginTransaction().add(R.id.content, context.roundTableVideoFragment).addToBackStack("RT").commit();
    }

    private int getTotalVideoCount() {
        int count = 0;
        for (int i = startPos; i <= endPos; i++) {
            VideoModel video = context.videoList.get(i);
            boolean isTrimmed = isTrimmed(video);
            long lastEndMillis = 0;
            for (int j = 0; j < video.videoFileList.size(); j++) {
                VideoFileModel videoFileModel = video.videoFileList.get(j);
                if (isTrimmed) {
                    if (lastEndMillis > video.trimEndMillis) {
                        break;
                    }
                    lastEndMillis += videoFileModel.trimEndMillis;
                    if (video.trimStartMillis > lastEndMillis) {
                        continue;
                    }
                }
                count++;
            }
        }
        return count;
    }

    private int getTotalDuration() {
        int duration = 0;
        for (int i = startPos; i <= endPos; i++) {
            VideoModel video = context.videoList.get(i);
            duration += video.actualDuration;
        }
        return Math.round(duration);
    }

    /**
     * Command for download video
     */
    private void executeDownloadH264VideoCommand() {
        File destinationLocation = context.getExternalFilesDir(Constants.DOWNLOAD_DIRECTORY);
        File dest = null;
        if (destinationLocation.exists() || destinationLocation.mkdir()) {
            String fileName = context.videoList.get(0).videoFileList.get(0).fileName;
            try {
                int index = fileName.lastIndexOf("_");
                fileName = fileName.substring(0, index) + ".mp4";
            } catch (Exception e) {
                showLogException(e);
            }
            dest = new File(destinationLocation, fileName);
            context.downloadedVideoPath = dest.getAbsolutePath();
        }
        if (dest.exists()) {
            BaseAPIService.dismissProgressDialog();
            downloadVideo(context.downloadedVideoPath);
        } else {
            if (!BaseAPIService.isShowingProgressDialog()) {
                BaseAPIService.showProgressDialog(context);
            }
            generateH264WaterMarkCommand();
        }
    }

    private float getExactTotalDuration() {
        long duration = 0;
        for (int i = startPos; i <= endPos; i++) {
            VideoModel video = context.videoList.get(i);
            boolean isTrimmed = isTrimmed(video);
            long lastEndMillis = 0;
            for (int j = 0; j < video.videoFileList.size(); j++) {
                VideoFileModel videoFileModel = video.videoFileList.get(j);
                if (isTrimmed) {
                    if (lastEndMillis > video.trimEndMillis) {
                        break;
                    }
                    lastEndMillis += videoFileModel.trimEndMillis;
                    if (video.trimStartMillis > lastEndMillis) {
                        continue;
                    }
                }
                duration += videoFileModel.trimEndMillis - videoFileModel.trimStartMillis;
            }
        }
        return duration / 1000f;
    }

    private void generateH264WaterMarkCommand() {
        //boolean isLoggedIn = Utility.isLoggedIn(context);
        boolean isLoggedIn = true;
        if (isAllFileGenerated || !isLoggedIn) {
            boolean isAudioOnlyOption = context.isAudioReply();
            String EFFECT_NAME = "circleclose";
            String userNamePath = "", genuinLogoPath = "", fromUserNamePath = "", userImagePath = "";
            String userNameWaterMarkPath = "", fullNameWaterMarkPath = "", bioWaterMarkPath = "";
            String bgPath = "", cameraRollPath = "";

            File gif = new File(context.getCacheDir(), "genuin_new_logo.gif");
            String gifPath = gif.getAbsolutePath();

            File bg = new File(context.getCacheDir(), "bg.jpg");
            bgPath = bg.getAbsolutePath();

//            File genuinLogo = new File(context.getCacheDir(), "genuin_new_logo.png");
//            genuinLogoPath = genuinLogo.getAbsolutePath();

            File genuinLogo = new File(context.getCacheDir(), "ic_genuin_watermark.png");
            genuinLogoPath = genuinLogo.getAbsolutePath();

            File cameraRoll = new File(context.getCacheDir(), "camera_roll_new.png");
            cameraRollPath = cameraRoll.getAbsolutePath();

            boolean isRecordForOther = context.videoOptions == CameraNewActivity.VideoOptions.RECORD_FOR_OTHER;
            boolean isAnyNonGenuinVideo = context.isAnyNonGenuinVideo();
            float resizeFactor = getResizeFactor();

            if (isLoggedIn) {
//                MembersModel user = Utility.getCurrentUserObject(context, "");
//                String userName = isRecordForOther ? context.contactModelForOther.getGenuin().getUserName() : user.getNickname();
//                String fullName = isRecordForOther ? context.contactModelForOther.getGenuin().getName() : user.getName();
//                String bio = isRecordForOther ? context.contactModelForOther.getGenuin().getBio() : user.getBio();
//                boolean isAvatar = isRecordForOther ? context.contactModelForOther.getGenuin().getIsAvatar() : user.isAvatar();
//                String userImage = isRecordForOther ? context.contactModelForOther.getGenuin().getProfileImage() : user.getProfileImage();
                String userName = "test";
                String fullName = "Monali";
                String bio = "@monali";
                String nickname = "@monali";
                File userNameFile = new File(context.getCacheDir(), userName + "_18_new.png");
                userNamePath = userNameFile.getAbsolutePath();

                File userNameWaterMark = new File(context.getCacheDir(), userName + "_32_new.png");
                userNameWaterMarkPath = userNameWaterMark.getAbsolutePath();

                if (!TextUtils.isEmpty(fullName)) {
                    File fullNameWaterMark = new File(context.getCacheDir(), "fullName.png");
                    fullNameWaterMarkPath = fullNameWaterMark.getAbsolutePath();
                }

                if (!TextUtils.isEmpty(bio)) {
                    File bioWaterMark = new File(context.getCacheDir(), "bio.png");
                    bioWaterMarkPath = bioWaterMark.getAbsolutePath();
                }

                File userImageFile;
//                if (isAvatar) {
//                    userImageFile = new File(context.getCacheDir(), userImage + "_img.png");
//                } else {
                    userImageFile = new File(context.getCacheDir(), userName + "_img.png");
               // }
                userImagePath = userImageFile.getAbsolutePath();

                if (isRecordForOther) {
                    if (isAnyNonGenuinVideo) {
                        File fromUserNameCRoll = new File(context.getCacheDir(), nickname + "_camera_new.png");
                        fromUserNamePath = fromUserNameCRoll.getAbsolutePath();
                    } else {
                        File fromUserName = new File(context.getCacheDir(), nickname+ "_from_new.png");
                        fromUserNamePath = fromUserName.getAbsolutePath();
                    }
                }
            }

            String complexCommand = "-y ";

            String concatCommand = "";
            String concatCommand1 = "";

            if (startPos != -1) {
                int count = 0;
                String outPutResolution = getVideoResolution();
                outPutResolution = outPutResolution.replace("x", ":");
                String outPutAspectRatio = getVideoResolutionRatio();
                int totalVideoCount = context.isPhotoReply() ? 1 : getTotalVideoCount();
                float exactDuration = context.isPhotoReply() ? 5 : getExactTotalDuration();
                float exactDurationWithPlus = exactDuration + 0.5f;
                float exactDurationWithMinus = exactDuration - 1.0f;
                float totalDuration = exactDuration + 3f;
                int audioMixPos = totalVideoCount + 1;
               // int logoWidth = SharedPrefUtils.getIntPreference(context, Constants.PREF_LOGO_WIDTH) + 8;

                if (context.isPhotoReply()) {
                    File photo_bg = new File(context.getCacheDir(), "photo_bg.png");
                    String photoBgPath = photo_bg.getAbsolutePath();
                    complexCommand += "-loop 1 -t " + 5 + " -i " + photoBgPath + " ";
                    concatCommand += "[" + count + ":v]setpts=PTS*1,scale=" + outPutResolution + "[v" + count + "];";
                    concatCommand1 += "[v" + count + "][" + audioMixPos + "]";
                    count++;
                } else {
                    if (isAudioOnlyOption) {
                        File audio_bg = new File(context.getCacheDir(), "audio_bg.png");
                        String audioBgPath = audio_bg.getAbsolutePath();
                        complexCommand += "-loop 1 -t " + getTotalDuration() + " -i " + audioBgPath + " ";
                        concatCommand += "[0]scale=" + outPutResolution + ",format=yuv420p[v0];";
                        count++;
                    }
                    for (int i = startPos; i <= endPos; i++) {
                        VideoModel video = context.videoList.get(i);
                        boolean isTrimmed = isTrimmed(video);
                        boolean isFirst = true;
                        long lastEndMillis = 0;
                        for (int j = 0; j < video.videoFileList.size(); j++) {
                            long startPositionMs = 0, endPositionMs = 0;
                            VideoFileModel videoFileModel = video.videoFileList.get(j);
                            if (isTrimmed) {
                                if (lastEndMillis > video.trimEndMillis) {
                                    break;
                                }
                                lastEndMillis += videoFileModel.trimEndMillis;
                                if (video.trimStartMillis > lastEndMillis) {
                                    continue;
                                }
                                if (isFirst) {
                                    isFirst = false;
                                    startPositionMs = video.trimStartMillis - (lastEndMillis - videoFileModel.trimEndMillis);
                                } else {
                                    startPositionMs = videoFileModel.trimStartMillis;
                                }

                                if (lastEndMillis > video.trimEndMillis) {
                                    endPositionMs = videoFileModel.trimEndMillis - (lastEndMillis - video.trimEndMillis);
                                } else {
                                    endPositionMs = videoFileModel.trimEndMillis;
                                }
                            }
                            boolean isCurrentFileHaveAudio = isVideoHaveAudioTrack(videoFileModel.filePath);
                            complexCommand += "-i ";
                            complexCommand += videoFileModel.filePath + " ";
                            if (isTrimmed) {
                                if (videoFileModel.videoSpeed == 0.3f) {
                                    if (!isAudioOnlyOption) {
                                        if (videoFileModel.isFront) {
                                            concatCommand += "[" + count + ":v]settb=AVTB,fps=30/1,trim=" + getTrimString(startPositionMs, endPositionMs) + ",setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + ",hflip[v" + count + "];";
                                        } else {
                                            concatCommand += "[" + count + ":v]settb=AVTB,fps=30/1,trim=" + getTrimString(startPositionMs, endPositionMs) + ",setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + "[v" + count + "];";
                                        }
                                    }
                                    if (isCurrentFileHaveAudio) {
                                        concatCommand += "[" + count + ":a]atrim=" + getTrimString(startPositionMs, endPositionMs) + ",asetpts=PTS-STARTPTS,atempo=0.55,atempo=0.55,highpass=f=200,lowpass=f=3000,volume=5dB[a" + count + "];";
                                    }
                                } else {
                                    if (!isAudioOnlyOption) {
                                        if (videoFileModel.isFront) {
                                            concatCommand += "[" + count + ":v]settb=AVTB,fps=30/1,trim=" + getTrimString(startPositionMs, endPositionMs) + ",setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + ",hflip[v" + count + "];";
                                        } else {
                                            concatCommand += "[" + count + ":v]settb=AVTB,fps=30/1,trim=" + getTrimString(startPositionMs, endPositionMs) + ",setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + "[v" + count + "];";
                                        }
                                    }
                                    if (isCurrentFileHaveAudio) {
                                        concatCommand += "[" + count + ":a]atrim=" + getTrimString(startPositionMs, endPositionMs) + ",asetpts=PTS-STARTPTS,atempo=" + video.videoFileList.get(0).videoSpeed + ",highpass=f=200,lowpass=f=3000,volume=5dB[a" + count + "];";
                                    }
                                }
                            } else {
                                if (videoFileModel.videoSpeed == 0.3f) {
                                    if (!isAudioOnlyOption) {
                                        if (videoFileModel.isFront) {
                                            concatCommand += "[" + count + ":v]settb=AVTB,fps=30/1,setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + ",hflip[v" + count + "];";
                                        } else {
                                            concatCommand += "[" + count + ":v]settb=AVTB,fps=30/1,setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + "[v" + count + "];";
                                        }
                                    }
                                    if (isCurrentFileHaveAudio) {
                                        concatCommand += "[" + count + ":a]asetpts=PTS-STARTPTS,atempo=0.55,atempo=0.55,highpass=f=200,lowpass=f=3000,volume=5dB[a" + count + "];";
                                    }
                                } else {
                                    if (!isAudioOnlyOption) {
                                        if (videoFileModel.isFront) {
                                            concatCommand += "[" + count + ":v]settb=AVTB,fps=30/1,setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + ",hflip[v" + count + "];";
                                        } else {
                                            concatCommand += "[" + count + ":v]settb=AVTB,fps=30/1,setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + "[v" + count + "];";
                                        }
                                    }
                                    if (isCurrentFileHaveAudio) {
                                        concatCommand += "[" + count + ":a]asetpts=PTS-STARTPTS,atempo=" + video.videoFileList.get(0).videoSpeed + ",highpass=f=200,lowpass=f=3000,volume=5dB[a" + count + "];";
                                    }
                                }
                            }
                            if (isAudioOnlyOption) {
                                concatCommand1 += "[a" + count + "]";
                            } else {
                                if (isCurrentFileHaveAudio) {
                                    concatCommand1 += "[v" + count + "][a" + count + "]";
                                } else {
                                    concatCommand1 += "[v" + count + "][" + audioMixPos + "]";
                                }
                            }
                            count++;
                        }
                    }
                }

                complexCommand += "-loop 1 -t " + 3 + " -i " + bgPath;
                concatCommand += "[" + (count) + ":v]settb=AVTB,fps=30/1,setpts=PTS-STARTPTS,scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + "[bg];";

                complexCommand += " -f lavfi -t 0.1 -i anullsrc";

                int totalCount = count + 2;
                int layerCount = 0;

                complexCommand += " -ignore_loop 0 -i " + gifPath;
                concatCommand += "[" + totalCount + ":v]setpts=PTS-STARTPTS+" + (exactDurationWithPlus) + "/TB[delayedGif];";
                if (isLoggedIn) {
                    totalCount++;
                    complexCommand += " -loop 1 -t " + (exactDuration + 2) + " -i " + userImagePath;
                    concatCommand += "[" + totalCount + ":v]setpts=PTS-STARTPTS/TB,scale=-1:'min((t-" + (exactDuration) + ")*600,456)':eval=frame[image];";
                    totalCount++;
                    complexCommand += " -loop 1 -t " + (exactDuration + 2) + " -i " + userNameWaterMarkPath;
                    concatCommand += "[" + totalCount + ":v]setpts=PTS-STARTPTS/TB,fade=in:st=" + (exactDuration) + ":d=1:alpha=1[userName];";
                    totalCount++;
                    layerCount++;
                    if (!TextUtils.isEmpty(fullNameWaterMarkPath)) {
                        complexCommand += " -loop 1 -t " + (exactDuration + 2) + " -i " + fullNameWaterMarkPath;
                        concatCommand += "[" + totalCount + ":v]setpts=PTS-STARTPTS/TB,fade=in:st=" + (exactDuration) + ":d=1:alpha=1[fullName];";
                        totalCount++;
                    }
                    if (!TextUtils.isEmpty(bioWaterMarkPath)) {
                        complexCommand += " -loop 1 -t " + (exactDuration + 2) + " -i " + bioWaterMarkPath;
                        concatCommand += "[" + totalCount + ":v]setpts=PTS-STARTPTS/TB,fade=in:st=" + (exactDuration) + ":d=1:alpha=1[bio];";
                        totalCount++;
                    }
                    complexCommand += " -i " + userNamePath;
                    concatCommand += "[" + totalCount + ":v]setpts=PTS*1[l" + layerCount + "];";
                }
                totalCount++;
                layerCount++;
                complexCommand += " -i " + genuinLogoPath + " ";
                concatCommand += "[" + totalCount + ":v]setpts=PTS*1[l" + layerCount + "];";
                if (isRecordForOther) {
                    totalCount++;
                    layerCount++;
                    complexCommand += "-i " + fromUserNamePath + " ";
                    concatCommand += "[" + totalCount + ":v]setpts=PTS*1[l" + layerCount + "];";
                } else if (isAnyNonGenuinVideo) {
                    totalCount++;
                    layerCount++;
                    complexCommand += "-i " + cameraRollPath + " ";
                    concatCommand += "[" + totalCount + ":v]setpts=PTS*1[l" + layerCount + "];";
                }

                boolean isOverlayExist = false;
                for (int i = 0; i < draggableViewsList.size(); i++) {
                    ImageStickerModel model = draggableViewsList.get(i);
                    if (model.getType() == LayerType.GIF) {
                        String gifFilePath = model.getGifFilePath();
                        if (!TextUtils.isEmpty(gifFilePath) && new File(gifFilePath).exists()) {
                            totalCount++;
                            layerCount++;
                            complexCommand += "-ignore_loop 0 -i " + model.getGifFilePath() + " ";
                            concatCommand += "[" + totalCount + ":v]scale=" + model.getWidth() + ":" + model.getHeight() + ",rotate=" + model.getRotationAngel() + ":c=none:ow=rotw(" + model.getRotationAngel() + "):oh=roth(" + model.getRotationAngel() + ")[l" + layerCount + "];";
                            isOverlayExist = true;
                        }
                    } else {
                        String filePath = model.getFilePath();
                        if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                            totalCount++;
                            layerCount++;
                            complexCommand += "-i " + model.getFilePath() + " ";
                            concatCommand += "[" + totalCount + ":v]setpts=PTS*1[l" + layerCount + "];";
                            isOverlayExist = true;
                        }
                    }
                }

                String audioBioPath = "";
                if (isAudioOnlyOption) {
                    totalCount++;
                    audioBioPath = getAudioBioPath();
                    complexCommand += " -ignore_loop 0 -i " + getAudioGIFPath() + " ";
                    complexCommand += " -i " + getAudioImagePath() + " ";
                    complexCommand += " -i " + getAudioUserNamePath() + " ";
                    if (!TextUtils.isEmpty(audioBioPath)) {
                        complexCommand += " -i " + getAudioBioPath() + " ";
                    }
                    int gifHW = (int) (Utility.dpToPx(115, context) * resizeFactor);
                    int imageHW = (int) (Utility.dpToPx(88, context) * resizeFactor);
                    concatCommand += "[" + (totalCount) + ":v]setpts=PTS-STARTPTS/TB,scale=" + gifHW + ":" + gifHW + "[audioGif];";
                    concatCommand += "[" + (totalCount + 1) + ":v]setpts=PTS-STARTPTS/TB,scale=" + imageHW + ":" + imageHW + "[audioImage];";
                    concatCommand += "[" + (totalCount + 2) + ":v]setpts=PTS-STARTPTS/TB[audioName];";
                    if (!TextUtils.isEmpty(audioBioPath)) {
                        concatCommand += "[" + (totalCount + 3) + ":v]setpts=PTS-STARTPTS/TB[audioBio];";
                    }
                }

                complexCommand += "-filter_complex \"";

                complexCommand += concatCommand;
                complexCommand += concatCommand1;
                if (isAudioOnlyOption) {
                    complexCommand += "concat=n=" + (count - 1) + ":v=0:a=1";
                    complexCommand += "[a];[v0]settb=AVTB,fps=30/1[video];";
                } else {
                    complexCommand += "concat=n=" + count + ":v=1:a=1";
                    complexCommand += "[vid][aid];[vid]settb=AVTB,fps=30/1[video];";
                }
                complexCommand += "[video][bg]xfade=transition=" + EFFECT_NAME + ":duration=1:offset=" + (exactDurationWithMinus) + ",format=yuv420p[xF];";
                int oCount, lCount;
                if (isLoggedIn) {
                    lCount = 2;
                    complexCommand += "[xF][delayedGif]overlay=x=(W-w)/2:y=H-h-176:shortest=1:enable='between(t," + (exactDurationWithPlus) + "," + (totalDuration) + ")'[o1];";
                    complexCommand += "[o1][image]overlay=(W-w)/2:H/2-h:enable='between(t," + (exactDuration + 0.2f) + "," + (totalDuration) + ")'[o2];";
                    complexCommand += "[o2][userName]overlay=(W-w)/2:H/2+45:enable='between(t," + (exactDuration + 0.2f) + "," + (totalDuration) + ")'[o3];";
                    if (!TextUtils.isEmpty(fullNameWaterMarkPath) && !TextUtils.isEmpty(bioWaterMarkPath)) {
                        oCount = 6;
                        complexCommand += "[o3][fullName]overlay=(W-w)/2:H/2+115:enable='between(t," + (exactDuration + 0.2f) + "," + (totalDuration) + ")'[o4];";
                        complexCommand += "[o4][bio]overlay=(W-w)/2:H/2+185:enable='between(t," + (exactDuration + 0.2f) + "," + (totalDuration) + ")'[o5];";
                        complexCommand += "[o5][l1]overlay=x=16:y=H/4+45:enable='between(t,0," + (exactDuration - 0.5f) + ")'[o6];";
                        complexCommand += "[o6][l2]overlay=x=16:y=H/4:enable='between(t,0," + (exactDuration - 0.5f) + ")'";
                    } else if (!TextUtils.isEmpty(fullNameWaterMarkPath)) {
                        oCount = 5;
                        complexCommand += "[o3][fullName]overlay=(W-w)/2:H/2+115:enable='between(t," + (exactDuration + 0.2f) + "," + (totalDuration) + ")'[o4];";
                        complexCommand += "[o4][l1]overlay=x=16:y=H/4+45:enable='between(t,0," + (exactDuration - 0.5f) + ")'[o5];";
                        complexCommand += "[o5][l2]overlay=x=16:y=H/4:enable='between(t,0," + (exactDuration - 0.5f) + ")'";
                    } else if (!TextUtils.isEmpty(bioWaterMarkPath)) {
                        oCount = 5;
                        complexCommand += "[o3][bio]overlay=(W-w)/2:H/2+115:enable='between(t," + (exactDuration + 0.2f) + "," + (totalDuration) + ")'[o4];";
                        complexCommand += "[o4][l1]overlay=x=16:y=H/4+45:enable='between(t,0," + (exactDuration - 0.5f) + ")'[o5];";
                        complexCommand += "[o5][l2]overlay=x=16:y=H/4:enable='between(t,0," + (exactDuration - 0.5f) + ")'";
                    } else {
                        oCount = 4;
                        complexCommand += "[o3][l1]overlay=x=16:y=H/4+45:enable='between(t,0," + (exactDuration - 0.5f) + ")'[o4];";
                        complexCommand += "[o4][l2]overlay=x=16:y=H/4:enable='between(t,0," + (exactDuration - 0.5f) + ")'";
                    }

                } else {
                    complexCommand += "[xF][delayedGif]overlay=x=(W-w)/2:y=(H-h)/2:shortest=1:enable='between(t," + (exactDurationWithPlus) + "," + (totalDuration) + ")'[o1];";
                    complexCommand += "[o1][l1]overlay=x=16:y=(H/3)-h:enable='between(t,0," + (exactDuration - 0.5f) + ")'";
                    oCount = 1;
                    lCount = 1;
                }

                if (isRecordForOther || isAnyNonGenuinVideo) {
                    oCount++;
                    lCount++;
                    complexCommand += "[o" + oCount + "];[o" + oCount + "]";
                    complexCommand += "[l" + lCount + "]overlay=x=W-w-16:y=(H*2)/3:enable='between(t,0," + (exactDuration - 0.5f) + ")'";
                }

                if (isOverlayExist) {
                    for (int i = 0; i < draggableViewsList.size(); i++) {
                        ImageStickerModel model = draggableViewsList.get(i);
                        if (model.getType() == LayerType.GIF) {
                            if (!TextUtils.isEmpty(model.getGifFilePath())) {
                                oCount++;
                                lCount++;
                                complexCommand += "[o" + oCount + "];[o" + oCount + "]";
                                complexCommand += "[l" + lCount + "]overlay=" + model.getViewX() + ":" + model.getViewY() + ":shortest=1:enable='between(t,0," + (exactDuration - 0.5f) + ")'";
                            }
                        } else {
                            if (!TextUtils.isEmpty(model.getFilePath())) {
                                oCount++;
                                lCount++;
                                complexCommand += "[o" + oCount + "];[o" + oCount + "]";
                                complexCommand += "[l" + lCount + "]overlay=" + model.getViewX() + ":" + model.getViewY() + ":enable='between(t,0," + (exactDuration - 0.5f) + ")'";
                            }
                        }
                    }
                }
                if (isAudioOnlyOption) {
                    complexCommand += "[o];[o]";
                    int height = (int) (getTopAudioHeader() * resizeFactor);
                    int imageY = (int) (height + (getImageTopAudioHeader() * resizeFactor));
                    int userNameY = (int) (height + (getUserNameTopAudioHeader() * resizeFactor));
                    int bioY = (int) (height + (getBioTopAudioHeader() * resizeFactor));
                    complexCommand += "[audioGif]overlay=(W-w)/2:" + height + ":shortest=1:enable='between(t,0," + (exactDuration - 0.5f) + ")'";
                    complexCommand += "[oGif];[oGif][audioImage]overlay=(W-w)/2:" + imageY + ":enable='between(t,0," + (exactDuration - 0.5f) + ")'";
                    complexCommand += "[oImage];[oImage][audioName]overlay=(W-w)/2:" + userNameY + ":enable='between(t,0," + (exactDuration - 0.5f) + ")'";
                    if (!TextUtils.isEmpty(audioBioPath)) {
                        complexCommand += "[oName];[oName][audioBio]overlay=(W-w)/2:" + bioY + ":enable='between(t,0," + (exactDuration - 0.5f) + ")'";
                    }
                    complexCommand += "[v]\"";
                } else {
                    complexCommand += "[v];[aid][" + audioMixPos + "]amix[a]\"";
                }
                complexCommand += " -map \"[v]\" -map \"[a]\"";
                complexCommand += " -c:v libx264 -preset ultrafast -c:a aac -b:a 192k ";
                complexCommand += context.downloadedVideoPath + " -async 1 -vsync 2";
                //TODO: FFMPEG not integrated
                //startFFMpegCommand(complexCommand, context.downloadedVideoPath, Constants.SESSION_DOWNLOAD);
            } else {
                if (Utility.isNetworkAvailable(context)) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!isAllFileGenerated) {
                            saveFilesIfNotExist();
                        }
                        generateH264WaterMarkCommand();
                    }, 500);
                } else {
                    Utility.showToast(context, context.getResources().getString(R.string.no_internet));
                }
            }
        }
    }

    private int getTopAudioHeader() {
        return (llAudioHeader.getTop() - rlHeaderMain.getTop());
    }

    private int getImageTopAudioHeader() {
        return rlAudioImage.getTop();
    }

    private int getUserNameTopAudioHeader() {
        return tvAudioUserName.getTop();
    }

    private int getBioTopAudioHeader() {
        return (int) (tvAudioUserName.getBottom() + Utility.dpToPx(4, context));
    }

    /**
     * Command for merging video
     */
    private void executeMergeVideoCommand() {
        if (!TextUtils.isEmpty(context.downloadedVideoPath)) {
            File file = new File(context.downloadedVideoPath);
            if (file.exists()) {
                file.delete();
            }
        }

        if (!TextUtils.isEmpty(context.coverPhotoPath)) {
            File file = new File(context.coverPhotoPath);
            if (file.exists()) {
                file.delete();
            }
        }
        boolean isAudioOnlyOption = context.isAudioReply();
        float resizeFactor = getResizeFactor();

        executeImageCommand();

        File destinationLocation = context.getExternalFilesDir(Constants.MERGE_DIRECTORY);
        if (destinationLocation.exists() || destinationLocation.mkdir()) {
            //String userId = SharedPrefUtils.getStringPreference(context, Constants.PREF_USER);
            String userId = SDKInitiate.INSTANCE.getUserId();
            userId = userId + "_";
            String fileName = context.videoList.get(0).videoFileList.get(0).fileName;
            try {
                int index = fileName.lastIndexOf("_");
                fileName = userId + fileName.substring(0, index) + ".mp4";
            } catch (Exception e) {
                showLogException(e);
            }
            File dest = new File(destinationLocation, fileName);
            destinationPath = dest.getAbsolutePath();
            String complexCommand = "-y ";

            String concatCommand = "";
            String concatCommand1 = "";

            if (startPos != -1) {
                int count = 0;
                String outPutResolution = getVideoResolution();
                outPutResolution = outPutResolution.replace("x", ":");
                String outPutAspectRatio = getVideoResolutionRatio();
                int totalVideoCount = getTotalVideoCount();
                boolean isVideoFoundWithoutAudio = false;
                if (context.isPhotoReply()) {
                    isVideoFoundWithoutAudio = true;
                    File photo_bg = new File(context.getCacheDir(), "photo_bg.png");
                    String photoBgPath = photo_bg.getAbsolutePath();
                    complexCommand += "-loop 1 -t " + 5 + " -i " + photoBgPath + " ";
                    concatCommand += "[" + count + ":v]setpts=PTS*1,scale=" + outPutResolution + ",format=yuv420p[v" + count + "];";
                    concatCommand1 += "[v" + count + "][" + totalVideoCount + "]";
                    count++;
                } else {
                    if (isAudioOnlyOption) {
                        File audio_bg = new File(context.getCacheDir(), "audio_bg.png");
                        String audioBgPath = audio_bg.getAbsolutePath();
                        complexCommand += "-loop 1 -t " + getTotalDuration() + " -i " + audioBgPath + " ";
                        concatCommand += "[0]scale=" + outPutResolution + ",format=yuv420p[v0];";
                        count++;
                    }
                    for (int i = startPos; i <= endPos; i++) {
                        VideoModel video = context.videoList.get(i);
                        boolean isTrimmed = isTrimmed(video);
                        boolean isFirst = true;
                        long lastEndMillis = 0;
                        for (int j = 0; j < video.videoFileList.size(); j++) {
                            long startPositionMs = 0, endPositionMs = 0;
                            VideoFileModel videoFileModel = video.videoFileList.get(j);
                            if (isTrimmed) {
                                if (lastEndMillis > video.trimEndMillis) {
                                    break;
                                }
                                lastEndMillis += videoFileModel.trimEndMillis;
                                if (video.trimStartMillis > lastEndMillis) {
                                    continue;
                                }
                                if (isFirst) {
                                    isFirst = false;
                                    startPositionMs = video.trimStartMillis - (lastEndMillis - videoFileModel.trimEndMillis);
                                } else {
                                    startPositionMs = videoFileModel.trimStartMillis;
                                }

                                if (lastEndMillis > video.trimEndMillis) {
                                    endPositionMs = videoFileModel.trimEndMillis - (lastEndMillis - video.trimEndMillis);
                                } else {
                                    endPositionMs = videoFileModel.trimEndMillis;
                                }
                            }
                            boolean isCurrentFileHaveAudio = isVideoHaveAudioTrack(videoFileModel.filePath);
                            if (!isCurrentFileHaveAudio) {
                                isVideoFoundWithoutAudio = true;
                            }
                            complexCommand += "-i " + videoFileModel.filePath + " ";
                            if (isTrimmed) {
                                if (videoFileModel.videoSpeed == 0.3f) {
                                    if (!isAudioOnlyOption) {
                                        if (videoFileModel.isFront) {
                                            concatCommand += "[" + count + ":v]trim=" + getTrimString(startPositionMs, endPositionMs) + ",setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + ",hflip[v" + count + "];";
                                        } else {
                                            concatCommand += "[" + count + ":v]trim=" + getTrimString(startPositionMs, endPositionMs) + ",setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + "[v" + count + "];";
                                        }
                                    }
                                    if (isCurrentFileHaveAudio) {
                                        concatCommand += "[" + count + ":a]atrim=" + getTrimString(startPositionMs, endPositionMs) + ",asetpts=PTS-STARTPTS,atempo=0.55,atempo=0.55,highpass=f=200,lowpass=f=3000,volume=5dB[a" + count + "];";
                                    }
                                } else {
                                    if (!isAudioOnlyOption) {
                                        if (videoFileModel.isFront) {
                                            concatCommand += "[" + count + ":v]trim=" + getTrimString(startPositionMs, endPositionMs) + ",setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + ",hflip[v" + count + "];";
                                        } else {
                                            concatCommand += "[" + count + ":v]trim=" + getTrimString(startPositionMs, endPositionMs) + ",setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + "[v" + count + "];";
                                        }
                                    }
                                    if (isCurrentFileHaveAudio) {
                                        concatCommand += "[" + count + ":a]atrim=" + getTrimString(startPositionMs, endPositionMs) + ",asetpts=PTS-STARTPTS,atempo=" + video.videoFileList.get(0).videoSpeed + ",highpass=f=200,lowpass=f=3000,volume=5dB[a" + count + "];";
                                    }
                                }
                            } else {
                                if (videoFileModel.videoSpeed == 0.3f) {
                                    if (!isAudioOnlyOption) {
                                        if (videoFileModel.isFront) {
                                            concatCommand += "[" + count + ":v]setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + ",hflip[v" + count + "];";
                                        } else {
                                            concatCommand += "[" + count + ":v]setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + "[v" + count + "];";
                                        }
                                    }
                                    if (isCurrentFileHaveAudio) {
                                        concatCommand += "[" + count + ":a]asetpts=PTS-STARTPTS,atempo=0.55,atempo=0.55,highpass=f=200,lowpass=f=3000,volume=5dB[a" + count + "];";
                                    }
                                } else {
                                    if (!isAudioOnlyOption) {
                                        if (videoFileModel.isFront) {
                                            concatCommand += "[" + count + ":v]setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + ",hflip[v" + count + "];";
                                        } else {
                                            concatCommand += "[" + count + ":v]setpts=(PTS-STARTPTS)*" + 1 / video.videoFileList.get(0).videoSpeed + ",scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + "[v" + count + "];";
                                        }
                                    }
                                    if (isCurrentFileHaveAudio) {
                                        concatCommand += "[" + count + ":a]asetpts=PTS-STARTPTS,atempo=" + video.videoFileList.get(0).videoSpeed + ",highpass=f=200,lowpass=f=3000,volume=5dB[a" + count + "];";
                                    }
                                }
                            }
                            if (isAudioOnlyOption) {
                                concatCommand1 += "[a" + count + "]";
                            } else {
                                if (isCurrentFileHaveAudio) {
                                    concatCommand1 += "[v" + count + "][a" + count + "]";
                                } else {
                                    concatCommand1 += "[v" + count + "][" + totalVideoCount + "]";
                                }
                            }
                            count++;
                        }
                    }
                }

                if (isVideoFoundWithoutAudio) {
                    complexCommand += "-f lavfi -t 0.1 -i anullsrc ";
                }

                boolean isOverlayExist = false;
                int layerCount = 0;
                int totalCount = isVideoFoundWithoutAudio ? count + 1 : count;

                for (int i = 0; i < draggableViewsList.size(); i++) {
                    ImageStickerModel model = draggableViewsList.get(i);
                    if (model.getType() == LayerType.GIF) {
                        String gifFilePath = model.getGifFilePath();
                        if (!TextUtils.isEmpty(gifFilePath) && new File(gifFilePath).exists()) {
                            layerCount++;
                            complexCommand += "-ignore_loop 0 -i " + model.getGifFilePath() + " ";
                            concatCommand += "[" + totalCount + ":v]scale=" + model.getWidth() + ":" + model.getHeight() + ",rotate=" + model.getRotationAngel() + ":c=none:ow=rotw(" + model.getRotationAngel() + "):oh=roth(" + model.getRotationAngel() + ")[l" + layerCount + "];";
                            isOverlayExist = true;
                            totalCount++;
                        }
                    } else {
                        String filePath = model.getFilePath();
                        if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                            layerCount++;
                            complexCommand += "-i " + model.getFilePath() + " ";
                            concatCommand += "[" + totalCount + ":v]setpts=PTS*1[l" + layerCount + "];";
                            isOverlayExist = true;
                            totalCount++;
                        }
                    }
                }
                String audioBioPath = "";
                if (isAudioOnlyOption) {
                    audioBioPath = getAudioBioPath();
                    complexCommand += " -ignore_loop 0 -i " + getAudioGIFPath() + " ";
                    complexCommand += " -i " + getAudioImagePath() + " ";
                    complexCommand += " -i " + getAudioUserNamePath() + " ";
                    if (!TextUtils.isEmpty(audioBioPath)) {
                        complexCommand += " -i " + getAudioBioPath() + " ";
                    }
                    int gifHW = (int) (Utility.dpToPx(115, context) * resizeFactor);
                    int imageHW = (int) (Utility.dpToPx(88, context) * resizeFactor);
                    concatCommand += "[" + (totalCount) + ":v]setpts=PTS-STARTPTS/TB,scale=" + gifHW + ":" + gifHW + "[audioGif];";
                    concatCommand += "[" + (totalCount + 1) + ":v]setpts=PTS-STARTPTS/TB,scale=" + imageHW + ":" + imageHW + "[audioImage];";
                    concatCommand += "[" + (totalCount + 2) + ":v]setpts=PTS-STARTPTS/TB[audioName];";
                    if (!TextUtils.isEmpty(audioBioPath)) {
                        concatCommand += "[" + (totalCount + 3) + ":v]setpts=PTS-STARTPTS/TB[audioBio];";
                    }
                }

                complexCommand += "-filter_complex \"";

                complexCommand += concatCommand;
                complexCommand += concatCommand1;

                if (isAudioOnlyOption) {
                    complexCommand += "concat=n=" + (count - 1) + ":v=0:a=1";
                } else {
                    complexCommand += "concat=n=" + count + ":v=1:a=1";
                }

                if (isOverlayExist || isAudioOnlyOption) {
                    if (isAudioOnlyOption) {
                        complexCommand += "[a];[v0]";
                    } else {
                        if (isVideoFoundWithoutAudio) {
                            complexCommand += "[vid][aid];[vid]";
                        } else {
                            complexCommand += "[vid][a];[vid]";
                        }
                    }
                    String concatOverlayCommand = "";
                    layerCount = 0;
                    for (int i = 0; i < draggableViewsList.size(); i++) {
                        ImageStickerModel model = draggableViewsList.get(i);
                        if (model.getType() == LayerType.GIF) {
                            if (!TextUtils.isEmpty(model.getGifFilePath())) {
                                if (!TextUtils.isEmpty(concatOverlayCommand)) {
                                    concatOverlayCommand += "[o" + layerCount + "];[o" + layerCount + "]";
                                }
                                layerCount++;
                                concatOverlayCommand += "[l" + layerCount + "]overlay=" + model.getViewX() + ":" + model.getViewY() + ":shortest=1";
                            }
                        } else {
                            if (!TextUtils.isEmpty(model.getFilePath())) {
                                if (!TextUtils.isEmpty(concatOverlayCommand)) {
                                    concatOverlayCommand += "[o" + layerCount + "];[o" + layerCount + "]";
                                }
                                layerCount++;
                                concatOverlayCommand += "[l" + layerCount + "]overlay=" + model.getViewX() + ":" + model.getViewY();
                            }
                        }
                    }
                    if (isAudioOnlyOption) {
                        if (!TextUtils.isEmpty(concatOverlayCommand)) {
                            concatOverlayCommand += "[o];[o]";
                        }
                        int height = (int) (getTopAudioHeader() * resizeFactor);
                        int imageY = (int) (height + (getImageTopAudioHeader() * resizeFactor));
                        int userNameY = (int) (height + (getUserNameTopAudioHeader() * resizeFactor));
                        int bioY = (int) (height + (getBioTopAudioHeader() * resizeFactor));
                        concatOverlayCommand += "[audioGif]overlay=(W-w)/2:" + height + ":shortest=1";
                        concatOverlayCommand += "[oGif];[oGif][audioImage]overlay=(W-w)/2:" + imageY;
                        concatOverlayCommand += "[oImage];[oImage][audioName]overlay=(W-w)/2:" + userNameY;
                        if (!TextUtils.isEmpty(audioBioPath)) {
                            concatOverlayCommand += "[oName];[oName][audioBio]overlay=(W-w)/2:" + bioY;
                        }
                    }
                    complexCommand += concatOverlayCommand;
                    if (isVideoFoundWithoutAudio) {
                        complexCommand += "[v];[aid][" + totalVideoCount + "]amix[a]\" -map \"[v]\" -map \"[a]\" ";
                    } else {
                        complexCommand += "[v]\" -map \"[v]\" -map \"[a]\" ";
                    }
                } else {
                    if (isVideoFoundWithoutAudio) {
                        complexCommand += "[v][aid];[aid][" + totalVideoCount + "]amix[a]\" -map \"[v]\" -map \"[a]\" ";
                    } else {
                        complexCommand += "\" ";
                    }
                }
//                boolean is265Supported = !SharedPrefUtils.getBoolPreference(context, Constants.PREF_IS_H265_FAILED);
//                if (is265Supported && ConnectivityCheckManager.getInstance(context).isMobileDataConnected) {
//                    Utility.showLog("TAG", "h265 is running");
//                    complexCommand += "-c:v libx265 -tag:v hvc1 -preset ultrafast -c:a aac -b:a 192k ";
//                } else {
//                    Utility.showLog("TAG", "h264 is running");
//                    complexCommand += "-c:v libx264 -preset ultrafast -c:a aac -b:a 192k ";
//                }
//                if (context.isAudioReply()) {
//                    complexCommand += "-shortest ";
//                }
//                complexCommand += destinationPath + " -async 1 -vsync 2";
//
//                context.ffMpegCommand = complexCommand;
//                context.isCompressionDone = false;
               // startFFMpegCommand(complexCommand, destinationPath, Constants.SESSION_MERGE);
            }
        }
    }

//    private void startFFMpegCommand(String command, String tag, String whichSession) {
//        cancelLastSession(tag);
//        GenuinFFMpegManager.getInstance().addValueToHashmap(tag, false);
//        OneTimeWorkRequest mergeRequest =
//                new OneTimeWorkRequest.Builder(CompressionWorker.class)
//                        .setInputData(createInputDataForUri(command, tag, whichSession))
//                        .addTag(tag)
//                        .build();
//        workManager.enqueue(mergeRequest);
//    }

//    private void cancelLastSession(String tag) {
//        ListenableFuture<List<WorkInfo>> list = workManager.getWorkInfosByTag(tag);
//        try {
//            List<WorkInfo> workInfoList = list.get();
//            for (WorkInfo workInfo : workInfoList) {
//                Utility.showLog("TAG", tag + " worker");
//                WorkInfo.State state = workInfo.getState();
//                long sessionId = workInfo.getOutputData().getLong("sessionId", -1);
//                if (sessionId != -1) {
//                    FFmpegKit.cancel(sessionId);
//                }
//                Utility.showLog("TAG", state.toString());
//            }
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }
//        workManager.cancelAllWorkByTag(tag);
//    }

//    private Data createInputDataForUri(String command, String path, String whichSession) {
//        Data.Builder builder = new Data.Builder();
//        builder.putString("path", path);
//        builder.putString("command", command);
//        builder.putString("from", context.from);
//        builder.putString("whichSession", whichSession);
//        builder.putInt("convType", context.convType);
//        builder.putString("chatId", context.chatId);
//        if (whichSession.equalsIgnoreCase(Constants.SESSION_MERGE)) {
//            builder.putLong("totalDuration", totalDuration);
//        }
//        return builder.build();
//    }

    private boolean isVideoHaveAudioTrack(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            boolean audioTrack;
            retriever.setDataSource(path);
            String hasAudioStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
            if (hasAudioStr != null && hasAudioStr.equals("yes")) {
                audioTrack = true;
            } else {
                audioTrack = false;
            }
            return audioTrack;
        } catch (Exception e) {
            return true;
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getTrimString(long startMills, long endMillis) {
        String trimStr;
        String start = Utility.milliToStringMerge1(startMills);
        String end = Utility.milliToStringMerge1(endMillis);
        trimStr = start + ":" + end;
        return trimStr;
    }

    /*private void execFFmpegBinary(final String[] command) {

        if (session != null) {
            session.cancel();
            FFmpegKit.cancel(session.getSessionId());
            if (session.getFuture() != null)
                session.getFuture().cancel(true);
            if (future != null)
                future.cancel(true);
            session = null;
        }

        try {
            final long startTime = System.currentTimeMillis();
            Utility.showLog(Constants.TAG, "Trimming Start: " + startTime);

            session = new FFmpegSession(command, session -> {
                if (VideoMergeAndPlayFragment.this.session.getSessionId() != session.getSessionId()) {
                    Utility.showLog(Constants.TAG, "Old session. Ignore");
                    return;
                } else {
                    context.runOnUiThread(() -> {
                        VideoMergeAndPlayFragment.this.session = null;
                        if (VideoMergeAndPlayFragment.this.shouldDoReMerge) {
                            VideoMergeAndPlayFragment.this.shouldDoReMerge = false;
                        }
                    });
                }

                ReturnCode returnCode = session.getReturnCode();

                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    Utility.showLog(Constants.TAG, "Async command execution completed successfully.");
                    final long totalTime = (System.currentTimeMillis() - startTime) / 1000;
                    HashMap<String, Object> map = new HashMap<String, Object>() {{
                        put("user_id", context.userId);
                        put("device_id", context.deviceId);
                        put("no_of_parts", endPos - startPos + 1);
                        put("duration", totalTime);
                    }};
                    GenuInApplication.getInstance().createDataDogLogs(Constants.RECORD_PREVIEW_TIME, map);
                    Utility.showLog(Constants.TAG, "Time: " + totalTime);
                    if ((isSendClick || isDownloadClick) && !isPaused) {
                        context.runOnUiThread(() -> {
                            if (isSendClick) {
                                publishClickManage();
                            } else {
                                isDownloadClick = false;
                                ivVideoMergeDownload.performClick();
                            }
                        });
                    } else if (isNeedToGiveCallBackForFFMpeg()) {
                        Intent intent = new Intent("FFMPEG_COMPLETE");
                        intent.putExtra("isCompleted", true);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                } else if (ReturnCode.isCancel(session.getReturnCode())) {
                    BaseAPIService.dismissProgressDialog();
                    isSendClick = false;
                    isDownloadClick = false;
                    if (isNeedToGiveCallBackForFFMpeg()) {
                        Intent intent = new Intent("FFMPEG_COMPLETE");
                        intent.putExtra("isCompleted", false);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                    Utility.showLog(Constants.TAG, "Async command execution cancelled by user.");
                } else {
                    Utility.showLog(Constants.TAG, "Async command execution failed with rc = " + returnCode);
//                        showToast(context, getString(R.string.cant_trim_video));
                    BaseAPIService.dismissProgressDialog();
                    isSendClick = false;
                    isDownloadClick = false;
                    if (isNeedToGiveCallBackForFFMpeg()) {
                        Intent intent = new Intent("FFMPEG_COMPLETE");
                        intent.putExtra("isCompleted", false);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }

                    String buffer = session.getOutput();
                    FFmpegKitConfig.printToLogcat(Log.INFO, buffer);
                }
            }, new LogCallback() {
                @Override
                public void apply(com.arthenica.ffmpegkit.Log log) {
                    Utility.showLog(Constants.TAG, "FFmpegLog: " + log.getMessage());
                }
            }, statistics -> Utility.showLog(Constants.TAG, "Statistics: " + statistics.toString()));

            GenuinFFmpegExecuteTask asyncFFmpegExecuteTask = new GenuinFFmpegExecuteTask(session);
            future = executorService.submit(asyncFFmpegExecuteTask);

            long ffmpegSessionId = session.getSessionId();
            Utility.showLog(Constants.TAG, "FFMpegSession: " + ffmpegSessionId);
        } catch (Exception e) {
            BaseAPIService.dismissProgressDialog();
        }
    }*/

    private boolean isNeedToGiveCallBackForFFMpeg() {
        return context.videoOptions != CameraNewActivity.VideoOptions.REPLY_REACTION && context.videoOptions != CameraNewActivity.VideoOptions.COMMENT;
    }

    private void setCenterMessage() {
//        if (context.videoOptions == CameraNewActivity.VideoOptions.DIRECT || context.videoOptions == CameraNewActivity.VideoOptions.GROUP) {
//            llPrivateVideoMsg.setVisibility(View.VISIBLE);
//            if (context.selectedContacts != null && context.selectedContacts.size() > 0) {
//                String sendToString = context.getSendToStr();
//                tvPrivateMsg.setText(String.format("Video message for%s", sendToString));
//            } else {
//                tvPrivateMsg.setText(context.getResources().getString(R.string.direct_message));
//            }
//            ivPrivacyType.setImageResource(R.drawable.ic_lock);
//        } else
            if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
            llPrivateVideoMsg.setVisibility(View.VISIBLE);
            tvPrivateMsg.setText(String.format("%s %s", context.getResources().getString(R.string.posting_to), context.communityHandle));
            if (context.privacyOptionsTypeRT == PrivacyOptionsType.EVERYONE) {
                ivPrivacyType.setImageResource(R.drawable.ic_globe);
            } else {
                ivPrivacyType.setImageResource(R.drawable.ic_icon_link);
            }
        } else if (context.videoOptions == CameraNewActivity.VideoOptions.RECORD_FOR_OTHER) {
//            llPrivateVideoMsg.setVisibility(View.VISIBLE);
//            tvPrivateMsg.setText(String.format("%s @%s", context.getResources().getString(R.string.public_video_for_other), context.contactModelForOther.getGenuin().getUserName()));
//            if (context.privacyOptionsType == PrivacyOptionsType.EVERYONE) {
//                ivPrivacyType.setImageResource(R.drawable.ic_globe);
//            } else {
//                ivPrivacyType.setImageResource(R.drawable.ic_icon_link);
//            }
        } else if (context.videoOptions == CameraNewActivity.VideoOptions.REPLY_REACTION) {
            llPrivateVideoMsg.setVisibility(View.VISIBLE);
            if (context.convType == VideoConvType.ROUND_TABLE.getValue()) {
                tvPrivateMsg.setText(String.format("%s %s", context.getResources().getString(R.string.posting_to), context.group.getName()));
                if (context.privacyOptionsTypeRT == PrivacyOptionsType.EVERYONE) {
                    ivPrivacyType.setImageResource(R.drawable.ic_globe);
                } else {
                    ivPrivacyType.setImageResource(R.drawable.ic_icon_link);
                }
            } else if (context.convType == VideoConvType.GROUP.getValue()) {
                tvPrivateMsg.setText(String.format("%s %s", context.getResources().getString(R.string.reply_in), context.group.getName()));
                ivPrivacyType.setImageResource(R.drawable.ic_lock);
            } else {
                tvPrivateMsg.setText(String.format("%s %s", context.getResources().getString(R.string.reply_to), context.toReplyUserName));
                ivPrivacyType.setImageResource(R.drawable.ic_lock);
            }
        } else if (context.videoOptions == CameraNewActivity.VideoOptions.COMMENT) {
            llPrivateVideoMsg.setVisibility(View.VISIBLE);
            tvPrivateMsg.setText(String.format("%s @%s's video", context.getResources().getString(R.string.comment_on), context.chat.getOwner().getNickname()));
            if (context.settings != null && context.settings.getDiscoverable()) {
                ivPrivacyType.setVisibility(View.VISIBLE);
                ivPrivacyType.setImageResource(R.drawable.ic_globe);
            } else {
                ivPrivacyType.setVisibility(View.GONE);
            }
        } else {
            llPrivateVideoMsg.setVisibility(View.GONE);
        }
    }

    public void setVideoPlay() {
        Utility.showLog("TAG", "SetVideoPlay");
        totalDuration = 0;
        setCenterMessage();
        stopPlaying();
        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        playerHelper.clear();
        for (int i = startPos; i <= endPos; i++) {
            final VideoModel video = context.videoList.get(i);
            long startMillis, endMillis;
            boolean isTrimmed = isTrimmed(video);
            if (isTrimmed) {
                startMillis = video.trimStartMillis;
                endMillis = video.trimEndMillis;
            } else {
                startMillis = 0L;
                endMillis = (long) (video.actualDuration * 1000);
            }
            boolean isFirst = true;
            long lastEndMillis = 0;
            totalDuration += endMillis - startMillis;
            for (int j = 0; j < video.videoFileList.size(); j++) {
                long startPositionMs, endPositionMs;
                VideoFileModel videoFileModel = video.videoFileList.get(j);
                if (isTrimmed) {
                    if (lastEndMillis > video.trimEndMillis) {
                        break;
                    }
                    lastEndMillis += videoFileModel.trimEndMillis;
                    if (video.trimStartMillis > lastEndMillis) {
                        continue;
                    }
                    if (isFirst) {
                        isFirst = false;
                        if (!isFirstTimeCoverImageGenerated) {
                            isFirstTimeCoverImageGenerated = true;
                            context.screenShotIsFront = videoFileModel.isFront;
                            context.screenShotMediaPath = videoFileModel.filePath;
                            executeImageCommand();
//                        generateFirstBitmap(videoFileModel.filePath, videoFileModel.isFront, startPositionMs);
                        }
                        startPositionMs = video.trimStartMillis - (lastEndMillis - videoFileModel.trimEndMillis);
                    } else {
                        startPositionMs = videoFileModel.trimStartMillis;
                    }

                    if (lastEndMillis > video.trimEndMillis) {
                        endPositionMs = videoFileModel.trimEndMillis - (lastEndMillis - video.trimEndMillis);
                    } else {
                        endPositionMs = videoFileModel.trimEndMillis;
                    }

                    MediaItem.ClippingConfiguration clippingConfiguration = new MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionMs(startPositionMs)
                            .setEndPositionMs(endPositionMs)
                            .setStartsAtKeyFrame(true).build();
                    MediaItem builder = new MediaItem.Builder()
                            .setUri(videoFileModel.filePath)
                            .setMediaId(i + (videoFileModel.isFront ? "front" : "back"))
                            .setClippingConfiguration(clippingConfiguration)
                            .build();
                    mediaItems.add(builder);
                    PlayerHelperModel playerHelperModel = new PlayerHelperModel();
                    playerHelperModel.isFront = videoFileModel.isFront;
                    playerHelperModel.selectedPos = i;
                    playerHelper.add(playerHelperModel);
                } else {
                    startPositionMs = videoFileModel.trimStartMillis;
                    endPositionMs = videoFileModel.trimEndMillis;
                    if (isFirst) {
                        isFirst = false;
                        if (!isFirstTimeCoverImageGenerated) {
                            isFirstTimeCoverImageGenerated = true;
                            context.screenShotIsFront = videoFileModel.isFront;
                            context.screenShotMediaPath = videoFileModel.filePath;
                            executeImageCommand();
//                        generateFirstBitmap(videoFileModel.filePath, videoFileModel.isFront, startPositionMs);
                        }
                    }
                    MediaItem.ClippingConfiguration clippingConfiguration = new MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionMs(startPositionMs)
                            .setEndPositionMs(endPositionMs)
                            .setStartsAtKeyFrame(true)
                            .build();
                    MediaItem builder = new MediaItem.Builder()
                            .setUri(videoFileModel.filePath)
                            .setClippingConfiguration(clippingConfiguration)
                            .build();
                    mediaItems.add(builder);
                    PlayerHelperModel playerHelperModel = new PlayerHelperModel();
                    playerHelperModel.isFront = videoFileModel.isFront;
                    playerHelperModel.selectedPos = i;
                    playerHelper.add(playerHelperModel);
                }
            }
        }
        player = new ExoPlayer.Builder(context).build();
        player.addMediaItems(mediaItems);
        player.prepare();
        player.seekTo(0L);
        player.addListener(playerListener);
        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        videoView.setPlayer(player);
        if (dialog != null && dialog.isShowing()) {
            player.setPlayWhenReady(false);
        } else
            player.setPlayWhenReady(true);

        setPublishButton();
        allTimerCounter();
    }

    private double getDuration() {
        double sec = (totalDuration) / 1000f;
        return Double.parseDouble(String.format(Locale.ENGLISH, "%.1f", sec));
    }

    private void setPublishButton() {
        if (getDuration() >= MIN_DURATION) {
            btnPublish.setRippleColor(context.getResources().getColorStateList(R.color.white_opacity20, null));
            btnPublish.setTextColor(context.getResources().getColor(R.color.colorWhite, null));
            btnPublish.setBackgroundTintList(context.getResources().getColorStateList(R.color.splash_background, null));
        } else {
            btnPublish.setRippleColor(context.getResources().getColorStateList(R.color.transparent, null));
            btnPublish.setTextColor(context.getResources().getColor(R.color.white_opacity60, null));
            btnPublish.setBackgroundTintList(context.getResources().getColorStateList(R.color.splash_background_opacity60, null));
        }
        if (context.videoOptions == CameraNewActivity.VideoOptions.REPLY_REACTION) {
            if (context.convType == VideoConvType.ROUND_TABLE.getValue()) {
                btnPublish.setText(context.getResources().getString(R.string.post));
            } else {
                btnPublish.setText(context.getResources().getString(R.string.send));
            }
        } else if (context.videoOptions == CameraNewActivity.VideoOptions.COMMENT) {
            btnPublish.setText(context.getResources().getString(R.string.send));
        } else if (context.videoOptions == CameraNewActivity.VideoOptions.RECORD_FOR_OTHER) {
            btnPublish.setText(context.getResources().getString(R.string.publish));
        } else {
            btnPublish.setText(context.getResources().getString(R.string.next));
        }
    }

    private void stopPlaying() {
        if (player != null) {
            player.removeListener(playerListener);
            player.setPlayWhenReady(false);
            player.stop();
            player.clearMediaItems();
            player = null;
        }
    }

    private void allTimerCounter() {
        cancelTimerCounter();
        final int videoDuration = (int) totalDuration;
        progressTimer.setMax(videoDuration);
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                context.runOnUiThread(() -> {
                    if (isAdded()) {
                        try {
                            int cumulativePos = 0;
                            for (int i = 0; i < player.getCurrentPeriodIndex(); i++) {
                                cumulativePos += (player.getMediaItemAt(i).clippingConfiguration.endPositionMs - player.getMediaItemAt(i).clippingConfiguration.startPositionMs);
                            }
                            progressTimer.setProgress((int) (cumulativePos + player.getCurrentPosition()));
                        } catch (Exception e) {
                            showLogException(e);
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, 50);
    }

    private void cancelTimerCounter() {
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onDetach() {
        cancelTimerCounter();
        stopPlaying();
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        cancelTimerCounter();
       // EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void downloadClickManage() {
        if (!isCoverCommandExecuted) {
            isDownloadClick = true;
            BaseAPIService.showProgressDialog(context);
            return;
        }
        try {
//            HashMap<String, Object> map = new HashMap<String, Object>() {{
//                put("user_id", context.userId);
//                put("device_id", context.deviceId);
//                put("event_id", context.uuid);
//                put("title", Constants.RECORD_PREVIEW_DOWNLOAD_CLICKED);
//                if (context.prevEventTime != 0) {
//                    put("duration", (System.currentTimeMillis() - context.prevEventTime) / 1000);
//                } else {
//                    put("duration", 0);
//                }
//            }};
//            context.prevEventTime = System.currentTimeMillis();
//            GenuInApplication.getInstance().sendEventLogs(Constants.RECORD_PREVIEW_DOWNLOAD_CLICKED, map);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startDownload();
            } else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(context,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            Constants.WRITE_STORAGE_PERMISSION);
                } else {
                    startDownload();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDownload() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
        if (context.isCompressionDone) {
            executeWaterMarkVideoCommand();
        } else {
//            FFmpegSession fFmpegSession = GenuinFFMpegManager.getInstance().getLastMergeSession();
//            if (fFmpegSession != null) {
//                String command = fFmpegSession.getCommand();
//                boolean is264CommandRunning = command.contains("libx264");
//                List<Statistics> statistics = fFmpegSession.getStatistics();
//                if (statistics.size() > 0) {
//                    Statistics statisticsModel = statistics.get(statistics.size() - 1);
//                    long finishedExecutionDuration = statisticsModel.getTime();
//                    Utility.showLog("Statistics", finishedExecutionDuration + " TIme");
//                    if (finishedExecutionDuration >= (totalDuration * THRESHOLD_PER) || is264CommandRunning) {
//                        // wait for h265 to finish or already 264 is running
//                        if (!BaseAPIService.isShowingProgressDialog()) {
//                            BaseAPIService.showProgressDialog(context);
//                        }
//                        isDownloadClick = true;
//                    } else {
//                        // execute download h264
//                        executeDownloadH264VideoCommand();
//                    }
//                } else {
//                    // execute download h264
//                    executeDownloadH264VideoCommand();
//                }
//            } else {
                // execute download h264
                executeDownloadH264VideoCommand();
           // }
        }
        //sendDownloadClicked();
    }

    private void saveTranscribeForAudio() {
        File audioBgFile = new File(context.getCacheDir(), "transcribe.png");
        if (audioBgFile.exists()) {
            audioBgFile.delete();
        }
        try {
            ArrayList<EditorFontModel> fontModelArrayList = Utility.getFontListFromJSONAssets(context);
            ArrayList<EditorColorsModel> colorsModelArrayList = Utility.getColorListFromAssets(context);
            if (fontModelArrayList.size() > 0) {
                selectedFontModel = fontModelArrayList.get(0);
                selectedFontModel.setFontSize(20);
                Typeface typeface = Utility.setFontFromRes(context, selectedFontModel.getFontId());
                tvTranscribedText.setTypeface(typeface);
            }
            if (colorsModelArrayList.size() > 0) {
                selectedColorModel = colorsModelArrayList.get(0);
                selectedColorModel.setCurrentFontColor(Color.WHITE);
            }
            tvTranscribedText.setText(fullTranscribedText);
            Bitmap transcribeBmp = Utility.loadFixedWidthBitmapFromView(tvTranscribedText, transcribeMaxWidth);
            if (transcribeBmp != null) {
                addTextStickers(fullTranscribedText, transcribeBmp, LayerType.TRANSCRIBE);
                FileOutputStream out = new FileOutputStream(audioBgFile);
                transcribeBmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveFilesIfNotExist() {
        File file = new File(context.getCacheDir(), "genuin_new_logo.gif");
        if (!file.exists()) {
            try {
                InputStream is = context.getResources().openRawResource(R.raw.genuin_new_logo);
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int current;
                while ((current = bis.read()) != -1) {
                    baos.write(current);
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File logoFile = new File(context.getCacheDir(), "ic_genuin_watermark.png");
        if (!logoFile.exists()) {
            try {
                InputStream is = context.getResources().openRawResource(R.raw.ic_genuin_watermark);
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int current;
                while ((current = bis.read()) != -1) {
                    baos.write(current);
                }
                FileOutputStream fos = new FileOutputStream(logoFile);
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File bgFile = new File(context.getCacheDir(), "bg.jpg");
        if (!bgFile.exists()) {
            try {
                InputStream is = context.getResources().openRawResource(R.raw.bg);
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int current;
                while ((current = bis.read()) != -1) {
                    baos.write(current);
                }
                FileOutputStream fos = new FileOutputStream(bgFile);
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File genuinFile = new File(context.getCacheDir(), "genuin_new_logo.png");
        if (!genuinFile.exists()) {
            try {
                Bitmap finalLogoBitmap = Utility.loadBitmapFromView(tvGenuinLogo);
                if (finalLogoBitmap != null) {
                    //SharedPrefUtils.setIntPreference(context, Constants.PREF_LOGO_WIDTH, finalLogoBitmap.getWidth());
                    FileOutputStream out = new FileOutputStream(genuinFile);
                    finalLogoBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    finalLogoBitmap.recycle();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File cameraRollFile = new File(context.getCacheDir(), "camera_roll_new.png");
        if (!cameraRollFile.exists()) {
            try {
                tvFromCameraRoll.setText(String.format("%s %s", context.getResources().getString(R.string.slash_from), context.getResources().getString(R.string.camera_roll)));
                Bitmap cameraRollBitmap = Utility.loadBitmapFromView(tvFromCameraRoll);
                if (cameraRollBitmap != null) {
                    FileOutputStream out = new FileOutputStream(cameraRollFile);
                    cameraRollBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    cameraRollBitmap.recycle();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (context.isAudioReply()) {
            saveRequiredFilesForAudio();
        }

       // if (Utility.isLoggedIn(context)) {
            boolean isRecordForOther = context.videoOptions == CameraNewActivity.VideoOptions.RECORD_FOR_OTHER;
            //MembersModel user = Utility.getCurrentUserObject(context, "");

//            String userName = isRecordForOther ? context.contactModelForOther.getGenuin().getUserName() : user.getNickname();
//            String fullName = isRecordForOther ? context.contactModelForOther.getGenuin().getName() : user.getName();
//            String bio = isRecordForOther ? context.contactModelForOther.getGenuin().getBio() : user.getBio();
//            String userImagePath = isRecordForOther ? context.contactModelForOther.getGenuin().getProfileImage() : user.getProfileImage();
//            boolean isAvatar = isRecordForOther ? context.contactModelForOther.getGenuin().getIsAvatar() : user.isAvatar();

            String userName = "test";
            String fullName = "Monali";
            String bio = "@monali";
            String nickname = "@monali";
            File userFile = new File(context.getCacheDir(), userName + "_18_new.png");
            if (!userFile.exists()) {
                try {
                    tvUserName.setText(String.format("@%s", userName));
                    Bitmap finalBitmap = Utility.loadBitmapFromView(tvUserName);
                    if (finalBitmap != null) {
                        FileOutputStream out = new FileOutputStream(userFile);
                        finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                        finalBitmap.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            File user32File = new File(context.getCacheDir(), userName + "_32_new.png");
            if (!user32File.exists()) {
                try {
                    tvUserNameWaterMark.setText(String.format("@%s", userName));
                    Bitmap final32Bitmap = Utility.loadFixedWidthBitmapFromView(tvUserNameWaterMark);
                    if (final32Bitmap != null) {
                        FileOutputStream out = new FileOutputStream(user32File);
                        final32Bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                        final32Bitmap.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!TextUtils.isEmpty(fullName)) {
                File fullNameFile = new File(context.getCacheDir(), "fullName.png");
                try {
                    if (fullNameFile.exists()) {
                        fullNameFile.delete();
                    }
                    tvFullNameWaterMark.setText(fullName);
                    Bitmap finalFullNameBitmap = Utility.loadFixedWidthBitmapFromView(tvFullNameWaterMark);
                    if (finalFullNameBitmap != null) {
                        FileOutputStream out = new FileOutputStream(fullNameFile);
                        finalFullNameBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                        finalFullNameBitmap.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!TextUtils.isEmpty(bio)) {
                File bioNameFile = new File(context.getCacheDir(), "bio.png");
                try {
                    if (bioNameFile.exists()) {
                        bioNameFile.delete();
                    }
                    tvBioWaterMark.setText(bio);
                    tvBioWaterMark.invalidate();
                    Bitmap finalBioBitmap = Utility.loadFixedWidthBitmapFromView(tvBioWaterMark);
                    if (finalBioBitmap != null) {
                        FileOutputStream out = new FileOutputStream(bioNameFile);
                        finalBioBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                        finalBioBitmap.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (isRecordForOther) {
                if (context.isAnyNonGenuinVideo()) {
                    File fromUserNameCameraRollFile = new File(context.getCacheDir(), nickname + "_camera_new.png");
                    if (!fromUserNameCameraRollFile.exists()) {
                        try {
                            tvFromUserName.setText(String.format("%s @%s's %s", context.getResources().getString(R.string.slash_from), nickname, context.getResources().getString(R.string.camera_roll)));
                            Bitmap finalBitmapFromCameraRoll = Utility.loadBitmapFromView(tvFromUserName);
                            if (finalBitmapFromCameraRoll != null) {
                                FileOutputStream out = new FileOutputStream(fromUserNameCameraRollFile);
                                finalBitmapFromCameraRoll.compress(Bitmap.CompressFormat.PNG, 100, out);
                                out.flush();
                                out.close();
                                finalBitmapFromCameraRoll.recycle();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    File fromUserNameFile = new File(context.getCacheDir(), nickname + "_from_new.png");
                    if (!fromUserNameFile.exists()) {
                        try {
                            tvFromUserName.setText(String.format("%s @%s", context.getResources().getString(R.string.slash_from),  nickname));
                            Bitmap finalBitmapFromUser = Utility.loadBitmapFromView(tvFromUserName);
                            if (finalBitmapFromUser != null) {
                                FileOutputStream out = new FileOutputStream(fromUserNameFile);
                                finalBitmapFromUser.compress(Bitmap.CompressFormat.PNG, 100, out);
                                out.flush();
                                out.close();
                                finalBitmapFromUser.recycle();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            File file_photo_bg = new File(context.getCacheDir(), "photo_bg.png");
            if (!file_photo_bg.exists()) {
                try {
                    InputStream is = context.getResources().openRawResource(R.raw.photo_bg);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int current;
                    while ((current = bis.read()) != -1) {
                        baos.write(current);
                    }
                    FileOutputStream fos = new FileOutputStream(file_photo_bg);
                    fos.write(baos.toByteArray());
                    fos.flush();
                    fos.close();
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//            if (isAvatar) {
//                File userImageFile = new File(context.getCacheDir(), userImagePath + "_img.png");
//                if (!userImageFile.exists()) {
//                    int res = context.getResources().getIdentifier(userImagePath,
//                            "raw", context.getPackageName());
//                    int drawableRes = context.getResources().getIdentifier(userImagePath,
//                            "drawable", context.getPackageName());
//                    ivWaterMarkProfile.setCircleBackgroundColorResource(LottieAnimModel.getMapData().get(res));
//                    ivWaterMarkProfile.setImageResource(drawableRes);
//                    try {
//                        Bitmap finalBitmapImage = Utility.loadBitmapFromView(ivWaterMarkProfile);
//                        if (finalBitmapImage != null) {
//                            FileOutputStream out = new FileOutputStream(userImageFile);
//                            finalBitmapImage.compress(Bitmap.CompressFormat.PNG, 100, out);
//                            out.flush();
//                            out.close();
//                            finalBitmapImage.recycle();
//                            isAllFileGenerated = true;
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    isAllFileGenerated = true;
//                }
//            } else {
//                File userImageFile = new File(context.getCacheDir(), userName + "_img.png");
//                if (!userImageFile.exists()) {
//                    Glide.with(context).asBitmap().load(userImagePath).apply(new RequestOptions().override(456, 456)).into(new CustomTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                            try {
//                                ivWaterMarkProfile.setImageBitmap(resource);
//                                Bitmap finalBitmapImage = Utility.loadBitmapFromView(ivWaterMarkProfile);
//                                if (finalBitmapImage != null) {
//                                    FileOutputStream out = new FileOutputStream(userImageFile);
//                                    finalBitmapImage.compress(Bitmap.CompressFormat.PNG, 100, out);
//                                    out.flush();
//                                    out.close();
//                                    finalBitmapImage.recycle();
//                                    isAllFileGenerated = true;
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onLoadCleared(@Nullable Drawable placeholder) {
//
//                        }
//                    });
//                } else {
                    //isAllFileGenerated = true;
                //}
           // }
      //  } else {
            isAllFileGenerated = true;
        //}
    }

    private float getResizeFactor() {
        String videoRes = getVideoResolution();
        String[] videoHeightWidth = videoRes.split("x");
        int videoWidth = Integer.parseInt(videoHeightWidth[0]);
        int videoHeight = Integer.parseInt(videoHeightWidth[1]);
        Size videoSize = new Size(videoWidth, videoHeight);

        Rect playerViewRect = new Rect();
        videoView.getGlobalVisibleRect(playerViewRect);

        float widthFactor = videoSize.getWidth() * 1.0f / playerViewRect.width();
        float heightFactor = videoSize.getHeight() * 1.0f / playerViewRect.height();
        float resizeFactor = Math.max(widthFactor, heightFactor);
        return resizeFactor;
    }

    private void saveRequiredFilesForAudio() {
        File audioBgFile = new File(context.getCacheDir(), "audio_bg.png");
        if (audioBgFile.exists()) {
            audioBgFile.delete();
        }
        try {
            Bitmap audioBgBmp = Utility.loadBitmapFromView(ivAudioOnly);
            if (audioBgBmp != null) {
                FileOutputStream out = new FileOutputStream(audioBgFile);
                audioBgBmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
                audioBgBmp.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        File fileAudioGIF = new File(context.getCacheDir(), "audio_gif.gif");
        if (!fileAudioGIF.exists()) {
            try {
                InputStream is = context.getResources().openRawResource(R.raw.audio_gif);
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int current;
                while ((current = bis.read()) != -1) {
                    baos.write(current);
                }
                FileOutputStream fos = new FileOutputStream(fileAudioGIF);
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        float resizeFactor = getResizeFactor();

        //MembersModel user = Utility.getCurrentUserObject(context, "");
        String userName = "@Monali";
        String bio = "Monali";
        File userFile = new File(context.getCacheDir(), userName + "_audio.png");
        if (!userFile.exists()) {
            try {
                Bitmap finalBitmap = Utility.loadBitmapFromView(tvAudioUserName);
                if (finalBitmap != null) {
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(finalBitmap,
                            (int) (finalBitmap.getWidth() * resizeFactor),
                            (int) (finalBitmap.getHeight() * resizeFactor),
                            true);
                    if (resizedBitmap != null) {
                        FileOutputStream out = new FileOutputStream(userFile);
                        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                        finalBitmap.recycle();
                        resizedBitmap.recycle();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(bio)) {
            File bioNameFile = new File(context.getCacheDir(), "bio_audio.png");
            try {
                if (bioNameFile.exists()) {
                    bioNameFile.delete();
                }
                Bitmap finalBioBitmap = Utility.loadBitmapFromView(tvAudioBio);
                if (finalBioBitmap != null) {
                    Bitmap resizedBioBitmap = Bitmap.createScaledBitmap(finalBioBitmap,
                            (int) (finalBioBitmap.getWidth() * resizeFactor),
                            (int) (finalBioBitmap.getHeight() * resizeFactor),
                            true);
                    if (resizedBioBitmap != null) {
                        FileOutputStream out = new FileOutputStream(bioNameFile);
                        resizedBioBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                        finalBioBitmap.recycle();
                        resizedBioBitmap.recycle();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void executeWaterMarkVideoCommand() {
        File destinationLocation = context.getExternalFilesDir(Constants.DOWNLOAD_DIRECTORY);
        File dest = null;
        if (destinationLocation.exists() || destinationLocation.mkdir()) {
            String fileName = context.videoList.get(0).videoFileList.get(0).fileName;
            try {
                int index = fileName.lastIndexOf("_");
                fileName = fileName.substring(0, index) + ".mp4";
            } catch (Exception e) {
                showLogException(e);
            }
            dest = new File(destinationLocation, fileName);
            context.downloadedVideoPath = dest.getAbsolutePath();
        }
        assert dest != null;
        if (dest.exists()) {
            BaseAPIService.dismissProgressDialog();
            downloadVideo(context.downloadedVideoPath);
        } else {
            if (!BaseAPIService.isShowingProgressDialog()) {
                BaseAPIService.showProgressDialog(context);
            }
            generateWaterMarkCommand();
        }
    }

    private float getExactDuration(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(String.valueOf(Uri.parse(filePath)));
        long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        try {
            retriever.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return duration / 1000f;
    }

    private void generateWaterMarkCommand() {
        //boolean isLoggedIn = Utility.isLoggedIn(context);
        boolean isLoggedIn = true;
        if (isAllFileGenerated || !isLoggedIn) {
            String EFFECT_NAME = "circleclose";
            String userNamePath = "", genuinLogoPath, fromUserNamePath = "", userImagePath = "";
            String userNameWaterMarkPath = "", fullNameWaterMarkPath = "", bioWaterMarkPath = "";
            String bgPath = "", cameraRollPath = "";
            String outPutResolution = getVideoResolution();
            outPutResolution = outPutResolution.replace("x", ":");
            String outPutAspectRatio = getVideoResolutionRatio();
            float exactDuration = getExactDuration(destinationPath);
            float exactDurationWithPlus = exactDuration + 0.5f;
            float exactDurationWithMinus = exactDuration - 1.0f;
            float totalDuration = exactDuration + 3f;
            int audioMixPos = isLoggedIn ? 7 : 4;
//            int logoWidth = SharedPrefUtils.getIntPreference(context, Constants.PREF_LOGO_WIDTH) + 8;

            File gif = new File(context.getCacheDir(), "genuin_new_logo.gif");
            String gifPath = gif.getAbsolutePath();

            File bg = new File(context.getCacheDir(), "bg.jpg");
            bgPath = bg.getAbsolutePath();

//            File genuinLogo = new File(context.getCacheDir(), "genuin_new_logo.png");
//            genuinLogoPath = genuinLogo.getAbsolutePath();

            File genuinLogo = new File(context.getCacheDir(), "ic_genuin_watermark.png");
            genuinLogoPath = genuinLogo.getAbsolutePath();

            File cameraRoll = new File(context.getCacheDir(), "camera_roll_new.png");
            cameraRollPath = cameraRoll.getAbsolutePath();

            boolean isRecordForOther = context.videoOptions == CameraNewActivity.VideoOptions.RECORD_FOR_OTHER;
            boolean isAnyNonGenuinVideo = context.isAnyNonGenuinVideo();

            if (isLoggedIn) {
//                MembersModel user = Utility.getCurrentUserObject(context, "");
//                String userName = isRecordForOther ? context.contactModelForOther.getGenuin().getUserName() : user.getNickname();
//                String fullName = isRecordForOther ? context.contactModelForOther.getGenuin().getName() : user.getName();
//                String bio = isRecordForOther ? context.contactModelForOther.getGenuin().getBio() : user.getBio();
//                boolean isAvatar = isRecordForOther ? context.contactModelForOther.getGenuin().getIsAvatar() : user.isAvatar();
//                String userImage = isRecordForOther ? context.contactModelForOther.getGenuin().getProfileImage() : user.getProfileImage();

                String userName = "Monali";
                String fullName = "test";
                String bio = "monali";
                String nickname = "@Monali";

                File userNameFile = new File(context.getCacheDir(), userName + "_18_new.png");
                userNamePath = userNameFile.getAbsolutePath();

                File userNameWaterMark = new File(context.getCacheDir(), userName + "_32_new.png");
                userNameWaterMarkPath = userNameWaterMark.getAbsolutePath();

                if (!TextUtils.isEmpty(fullName)) {
                    File fullNameWaterMark = new File(context.getCacheDir(), "fullName.png");
                    fullNameWaterMarkPath = fullNameWaterMark.getAbsolutePath();
                }

                if (!TextUtils.isEmpty(bio)) {
                    File bioWaterMark = new File(context.getCacheDir(), "bio.png");
                    bioWaterMarkPath = bioWaterMark.getAbsolutePath();
                }

                File userImageFile = null;
//                if (isAvatar) {
//                    userImageFile = new File(context.getCacheDir(), userImage + "_img.png");
//                } else {
//                    userImageFile = new File(context.getCacheDir(), userName + "_img.png");
//                }
                //userImagePath = userImageFile.getAbsolutePath();

                if (isRecordForOther) {
                    if (isAnyNonGenuinVideo) {
                        File fromUserNameCRoll = new File(context.getCacheDir(), nickname + "_camera_new.png");
                        fromUserNamePath = fromUserNameCRoll.getAbsolutePath();
                    } else {
                        File fromUserName = new File(context.getCacheDir(), nickname + "_from_new.png");
                        fromUserNamePath = fromUserName.getAbsolutePath();
                    }
                }
            }

            String complexCommand = "-y";
            complexCommand += " -i " + destinationPath;
            complexCommand += " -loop 1 -t " + 3 + " -i " + bgPath;
            complexCommand += " -ignore_loop 0 -i " + gifPath;
            if (isLoggedIn) {
                complexCommand += " -loop 1 -t " + (exactDuration + 3) + " -i " + userImagePath;
                complexCommand += " -loop 1 -t " + (exactDuration + 3) + " -i " + userNameWaterMarkPath;
                if (!TextUtils.isEmpty(fullNameWaterMarkPath)) {
                    audioMixPos++;
                    complexCommand += " -loop 1 -t " + (exactDuration + 3) + " -i " + fullNameWaterMarkPath;
                }
                if (!TextUtils.isEmpty(bioWaterMarkPath)) {
                    audioMixPos++;
                    complexCommand += " -loop 1 -t " + (exactDuration + 3) + " -i " + bioWaterMarkPath;
                }
                complexCommand += " -i " + userNamePath;
            }
            complexCommand += " -i " + genuinLogoPath;
            if (isRecordForOther) {
                audioMixPos++;
                complexCommand += " -i " + fromUserNamePath;
            } else if (isAnyNonGenuinVideo) {
                audioMixPos++;
                complexCommand += " -i " + cameraRollPath;
            }
            complexCommand += " -f lavfi -t 3 -i anullsrc";

            complexCommand += " -filter_complex ";
            complexCommand += "\"[0:v]settb=AVTB,fps=30/1,setpts=PTS-STARTPTS,scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + "[v0];";
            complexCommand += "[1:v]settb=AVTB,fps=30/1,setpts=PTS-STARTPTS,scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + "[v1];";
            complexCommand += "[2:v]setpts=PTS-STARTPTS+" + (exactDurationWithPlus) + "/TB[delayedGif];";
            if (isLoggedIn) {
                complexCommand += "[3:v]setpts=PTS-STARTPTS/TB,scale=-1:'min((t-" + (exactDuration) + ")*600,456)':eval=frame[image];";
                complexCommand += "[4:v]setpts=PTS-STARTPTS/TB,fade=in:st=" + (exactDuration) + ":d=1:alpha=1[userName];";
                if (!TextUtils.isEmpty(fullNameWaterMarkPath) && !TextUtils.isEmpty(bioWaterMarkPath)) {
                    complexCommand += "[5:v]setpts=PTS-STARTPTS/TB,fade=in:st=" + (exactDuration) + ":d=1:alpha=1[fullName];";
                    complexCommand += "[6:v]setpts=PTS-STARTPTS/TB,fade=in:st=" + (exactDuration) + ":d=1:alpha=1[bio];";
                } else if (!TextUtils.isEmpty(fullNameWaterMarkPath)) {
                    complexCommand += "[5:v]setpts=PTS-STARTPTS/TB,fade=in:st=" + (exactDuration) + ":d=1:alpha=1[fullName];";
                } else if (!TextUtils.isEmpty(bioWaterMarkPath)) {
                    complexCommand += "[5:v]setpts=PTS-STARTPTS/TB,fade=in:st=" + (exactDuration) + ":d=1:alpha=1[bio];";
                }
            }
            complexCommand += "[v0][v1]xfade=transition=" + EFFECT_NAME + ":duration=1:offset=" + (exactDurationWithMinus) + ",format=yuv420p[xF];";
            if (isLoggedIn) {
                complexCommand += "[xF][delayedGif]overlay=x=(W-w)/2:y=H-h-176:shortest=1:enable='between(t," + (exactDurationWithPlus) + "," + (totalDuration) + ")'[o1];";
                complexCommand += "[o1][image]overlay=(W-w)/2:H/2-h:enable='between(t," + (exactDuration + 0.2f) + "," + (totalDuration) + ")'[o2];";
                complexCommand += "[o2][userName]overlay=(W-w)/2:H/2+45:enable='between(t," + (exactDuration + 0.2f) + "," + (totalDuration) + ")'";
                if (!TextUtils.isEmpty(fullNameWaterMarkPath) && !TextUtils.isEmpty(bioWaterMarkPath)) {
                    complexCommand += "[o3];[o3][fullName]overlay=(W-w)/2:H/2+115:enable='between(t," + (exactDuration + 0.2f) + "," + (totalDuration) + ")'";
                    complexCommand += "[o4];[o4][bio]overlay=(W-w)/2:H/2+185:enable='between(t," + (exactDuration + 0.2f) + "," + (totalDuration) + ")',";
                } else if (!TextUtils.isEmpty(fullNameWaterMarkPath)) {
                    complexCommand += "[o3];[o3][fullName]overlay=(W-w)/2:H/2+115:enable='between(t," + (exactDuration + 0.2f) + "," + (totalDuration) + ")',";
                } else if (!TextUtils.isEmpty(bioWaterMarkPath)) {
                    complexCommand += "[o3];[o3][bio]overlay=(W-w)/2:H/2+115:enable='between(t," + (exactDuration + 0.2f) + "," + (totalDuration) + ")',";
                } else {
                    complexCommand += ",";
                }
                complexCommand += "overlay=x=16:y=H/4+45:enable='between(t,0," + (exactDuration - 0.5f) + ")',";
                complexCommand += "overlay=x=16:y=H/4:enable='between(t,0," + (exactDuration - 0.5f) + ")'";
            } else {
                complexCommand += "[xF][delayedGif]overlay=x=(W-w)/2:y=(H-h)/2:shortest=1:enable='between(t," + (exactDurationWithPlus) + "," + (totalDuration) + ")',";
                complexCommand += "overlay=x=16:y=(H/3)-h:enable='between(t,0," + (exactDuration - 0.5f) + ")'";
            }

            if (isRecordForOther || isAnyNonGenuinVideo) {
                complexCommand += ",overlay=x=W-w-16:y=(H*2)/3:enable='between(t,0," + (exactDuration - 0.5f) + ")'";
            }
            complexCommand += "[v];";
            complexCommand += "[0:a][" + audioMixPos + "]amix[a]\"";
            complexCommand += " -map \"[v]\" -map \"[a]\"";
            complexCommand += " -c:v libx264 -preset ultrafast -c:a aac -b:a 192k ";
            complexCommand += context.downloadedVideoPath + " -async 1 -vsync 2";
            Utility.showLog("DownloadComm", complexCommand);
            //startFFMpegCommand(complexCommand, context.downloadedVideoPath, Constants.SESSION_DOWNLOAD);
        } else {
            if (Utility.isNetworkAvailable(context)) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!isAllFileGenerated) {
                        saveFilesIfNotExist();
                    }
                    generateWaterMarkCommand();
                }, 500);
            } else {
                Utility.showToast(context, context.getResources().getString(R.string.no_internet));
            }
        }
    }

    private void downloadVideo(String filePath) {
        try {
            File sourceFile = new File(filePath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                String fileName = System.currentTimeMillis() + ".mp4";
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/Genuin");
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 1);
                Uri collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri videoUri = resolver.insert(collection, contentValues);
                if (videoUri != null) {
                    try (ParcelFileDescriptor pfd = resolver.openFileDescriptor(videoUri, "w", null)) {
                        FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());
                        FileInputStream in = new FileInputStream(sourceFile);
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.close();
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                contentValues.clear();
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
                if (videoUri != null) {
                    resolver.update(videoUri, contentValues, null, null);
                }
//                HashMap<String, Object> mapp = new HashMap<String, Object>() {{
//                    put("user_id", context.userId);
//                    put("device_id", context.deviceId);
//                    put("event_id", context.uuid);
//                    put("title", Constants.RECORD_PREVIEW_DOWNLOAD_STATUS);
//                    if (context.prevEventTime != 0) {
//                        put("duration", (System.currentTimeMillis() - context.prevEventTime) / 1000);
//                    } else {
//                        put("duration", 0);
//                    }
//                    put("status", "success");
//                }};
//                GenuInApplication.getInstance().sendEventLogs(Constants.RECORD_PREVIEW_DOWNLOAD_STATUS, mapp);
//                context.prevEventTime = System.currentTimeMillis();
                Utility.showToast(context, context.getResources().getString(R.string.video_save_to_gallery));
            } else {
                File destinationLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Genuin");
                if (destinationLocation.exists() || destinationLocation.mkdir()) {
                    String fileName = System.currentTimeMillis() + ".mp4";
                    File destFile = new File(destinationLocation, fileName);
                    downloadFile(sourceFile, destFile);
                    Utility.showToast(context, context.getResources().getString(R.string.video_save_to_gallery));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.llEditClips) {
            stopPlaying();
            cancelTimerCounter();
            //context.goToVideoTrimmer();
            //sendEditClipsOpenedClicked();
        } else if (id == R.id.ivVideoMergeBack) {
            backManage();
        } else if (id == R.id.ivVideoMergeDownload) {
            downloadClickManage();
        } else if (id == R.id.btnPublish || id == R.id.btnSendTutorial) {
            if (getDuration() >= MIN_DURATION) {
                // Internet condition removed so user can proceed with next step without internet also
                if (isCoverCommandExecuted) {
                    publishClickManage();
                } else {
                    isPublishClicked = true;
                    BaseAPIService.showProgressDialog(context);
                }
            } else {
                if (context.videoOptions == CameraNewActivity.VideoOptions.PUBLIC || context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
                    Utility.showToast(context, context.getResources().getString(R.string.video_trim_validation));
                } else {
                    Utility.showToast(context, context.getResources().getString(R.string.video_trim_validation_send));
                }
            }
        } else if (id == R.id.imgCloseFailure || id == R.id.btnGotItFailure) {
            Constants.IS_FEED_REFRESH = true;
            reactionBlurLayout.setVisibility(View.GONE);
            if (btnGotItFailure.getText().toString().equalsIgnoreCase(context.getResources().getString(R.string.go_to_inbox))) {
                Constants.GO_TO_INBOX = true;
            }
            finishActivity();
        } else if (id == R.id.relative_tutorial_send) {
            relativeTutorialSend.setVisibility(View.GONE);
            // Internet condition removed so user can proceed with next step without internet also
            if (isCoverCommandExecuted) {
                publishClickManage();
            } else {
                isPublishClicked = true;
                BaseAPIService.showProgressDialog(context);
            }
        } else if (id == R.id.llAddLink) {
            if (llAddLinkInfo.getVisibility() == View.VISIBLE) {
                Utility.hideKeyboard(context, etAddLink);
                llAddLinkInfo.setVisibility(View.GONE);
                //sendAddLinkCloseClicked();
            } else {
                llAddLinkInfo.setVisibility(View.VISIBLE);
                etAddLink.requestFocus();
                Utility.showKeyboard(context, etAddLink);
                //sendAddLinkOpenedClicked();
            }
        } else if (id == R.id.llCloseLink) {
            if (TextUtils.isEmpty(etAddLink.getText())) {
                Utility.hideKeyboard(context, etAddLink);
                llAddLinkInfo.setVisibility(View.GONE);
                //sendAddLinkCloseClicked();
            } else {
                etAddLink.setText("");
                txtInvalidDomain.setVisibility(View.GONE);
            }
        } else if (id == R.id.llAddSticker || id == R.id.rlStickers) {
            if (llAddLinkInfo.getVisibility() == View.VISIBLE) {
                Utility.hideKeyboard(context, etAddLink);
                llAddLinkInfo.setVisibility(View.GONE);
                //sendAddLinkCloseClicked();
            } else {
                selectedFontModel = null;
                selectedColorModel = null;
                currentDraggableView = null;
                currentLayer = null;
                textToUpdate = null;
                hideShowView(true);
                openTextEditor(null);
            }
        } else if (id == R.id.cardCoverPhoto) {
            stopPlaying();
            cancelTimerCounter();
            context.goToChangeVideoCover();
            //sendCoverImageOpenedClicked();
        } else if (id == R.id.llGiphySticker) {
            player.setPlayWhenReady(false);
            GiphyDialogFragment giphyDialogFragment = new GiphyGenuinManager().getGiphyDialogInstance();
            if (giphyDialogFragment != null) {
                giphyDialogFragment.show(getChildFragmentManager(), "giphy_dialog");
                giphyDialogFragment.setGifSelectionListener(new GiphyDialogFragment.GifSelectionListener() {
                    @Override
                    public void onGifSelected(@NonNull Media media, @Nullable String s, @NonNull GPHContentType gphContentType) {
//                            GPHMediaView gphMediaView = new GPHMediaView(context);
//                            gphMediaView.setMedia(media, RenditionType.original, null);
                        player.setPlayWhenReady(true);
                        BaseAPIService.showProgressDialog(context);
                        ArrayList bitmaps = new ArrayList<>();
                        ArrayList<Integer> mDelays = new ArrayList<>();
                        Glide.with(context)
                                .asGif()
                                .load((Objects.requireNonNull(media.getImages().getOriginal()).getGifUrl()))
                                .into(new CustomTarget<GifDrawable>() {
                                    @Override
                                    public void onResourceReady(@NonNull GifDrawable resource, @Nullable Transition<? super GifDrawable> transition) {
                                        try {
                                            String gifFilePath = ImageUtils.saveStickerAsGIF(context, resource, media.getId() + ".gif");
                                            Object GifState = resource.getConstantState();
                                            Field frameLoader = GifState.getClass().getDeclaredField("frameLoader");
                                            frameLoader.setAccessible(true);
                                            Object gifFrameLoader = frameLoader.get(GifState);

                                            Field gifDecoder = gifFrameLoader.getClass().getDeclaredField("gifDecoder");
                                            gifDecoder.setAccessible(true);
                                            StandardGifDecoder standardGifDecoder = (StandardGifDecoder) gifDecoder.get(gifFrameLoader);
                                            for (int i = 0; i < standardGifDecoder.getFrameCount(); i++) {
                                                standardGifDecoder.advance();
                                                bitmaps.add(standardGifDecoder.getNextFrame());
                                                mDelays.add(standardGifDecoder.getDelay(i));
                                            }
                                            addGIFLayer(bitmaps, mDelays, gifFilePath, standardGifDecoder.getWidth(), standardGifDecoder.getHeight());
                                            BaseAPIService.dismissProgressDialog();
                                            //sendStickerModuleClosedClicked(true);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                        player.setPlayWhenReady(true);
                                        BaseAPIService.dismissProgressDialog();
                                    }
                                });
                    }

                    @Override
                    public void onDismissed(@NonNull GPHContentType gphContentType) {
                        player.setPlayWhenReady(true);
                        BaseAPIService.dismissProgressDialog();
                    }

                    @Override
                    public void didSearchTerm(@NonNull String s) {

                    }
                });
            }
        }
    }

    private void openTextEditor(Layer layer) {
        LayerType layerType = LayerType.IMAGE;
        if (layer != null) {
            currentLayer = layer;
            layerType = currentLayer.layerType;
            currentLayer.isVisible = false;
            currentLayer.reDraw();
            currentDraggableView = layer.draggableTextView;
            View view = layer.draggableTextView.getChildAt(0);
            if (view instanceof TextView) {
                textToUpdate = ((TextView) view).getText().toString();
            }
            int viewId = currentDraggableView.getId();
            selectedColorModel = colorsModelHashMap.get(viewId);
            selectedFontModel = fontModelHashMap.get(viewId);
        }

        int[] locations = getVideoViewLocation();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog = new VideoTextEditorDialog(context, textToUpdate, locations, selectedFontModel, selectedColorModel, layerType, this);
        }
        dialog.show();
    }

    private void addTextStickers(String textToUpdate, Bitmap bmp, LayerType layerType) {
        try {
            llDeleteSticker.setVisibility(View.GONE);
            DraggableTextView draggableTextView = new DraggableTextView(context);

            int viewId = new Random().nextInt();

            draggableTextView.setId(viewId);
            draggableTextView.updateTextViewAttrs(textToUpdate, selectedFontModel, selectedColorModel);

            String videoRes = getVideoResolution();
            String[] videoHeightWidth = videoRes.split("x");
            int videoWidth = Integer.parseInt(videoHeightWidth[0]);
            int videoHeight = Integer.parseInt(videoHeightWidth[1]);
            Size videoSize = new Size(videoWidth, videoHeight);
            draggableTextView.setVideoActualSize(videoSize);

            Rect playerViewRect = new Rect();
            videoView.getGlobalVisibleRect(playerViewRect);
            draggableTextView.setVideoContainerRect(playerViewRect);
            colorsModelHashMap.put(viewId, selectedColorModel);
            fontModelHashMap.put(viewId, selectedFontModel);
            addUpdateView(draggableTextView);
//            Bitmap bmp = loadBitmapFromView(draggableTextView);
            if (bmp != null) {
                if (draggableLayers.layers.size() == 0) {
                    float widthFactor = videoSize.getWidth() * 1.0f / playerViewRect.width();
                    float heightFactor = videoSize.getHeight() * 1.0f / playerViewRect.height();
                    draggableLayers.setValues(playerViewRect.left, playerViewRect.top, widthFactor, heightFactor);
                }
                draggableLayers.addLayer(context, bmp, viewId, draggableTextView, layerType);
                if (layerType == LayerType.TRANSCRIBE) {
                    draggableLayers.layers.get(draggableLayers.layers.size() - 1).layerTranslateToTranscribe(context, transcribeMaxWidth, transcribeY);
                }
                draggableLayers.invalidate();
                if (draggableLayers.layers.size() == 1) {
                    rlStickers.addView(draggableLayers);
                }
                draggableLayers.layers.get(draggableLayers.layers.size() - 1).convertToImage();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addGIFLayer(ArrayList<Bitmap> bmp, ArrayList<Integer> mDelays, String gifFilePath, int width, int height) {
        try {
            llDeleteSticker.setVisibility(View.GONE);
            int viewId = new Random().nextInt();
            DraggableImageView imageView = new DraggableImageView(context);
            imageView.setId(viewId);

            String videoRes = getVideoResolution();
            String[] videoHeightWidth = videoRes.split("x");
            int videoWidth = Integer.parseInt(videoHeightWidth[0]);
            int videoHeight = Integer.parseInt(videoHeightWidth[1]);
            Size videoSize = new Size(videoWidth, videoHeight);
            imageView.setVideoActualSize(videoSize);

            Rect playerViewRect = new Rect();
            videoView.getGlobalVisibleRect(playerViewRect);
            imageView.setVideoContainerRect(playerViewRect);
            addUpdateGIFView(imageView, gifFilePath, width, height);
            if (bmp != null) {
                if (draggableLayers.layers.size() == 0) {
                    float widthFactor = videoSize.getWidth() * 1.0f / playerViewRect.width();
                    float heightFactor = videoSize.getHeight() * 1.0f / playerViewRect.height();
                    draggableLayers.setValues(playerViewRect.left, playerViewRect.top, widthFactor, heightFactor);
                }
                draggableLayers.addLayer(context, bmp, mDelays, viewId, imageView);
                draggableLayers.invalidate();
                if (draggableLayers.layers.size() == 1) {
                    rlStickers.addView(draggableLayers);
                }
                draggableLayers.layers.get(draggableLayers.layers.size() - 1).convertToImage();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void hideShowView(boolean isHideRequired) {
        if (player != null) {
            player.setPlayWhenReady(!isHideRequired);
        }

        rlHeader.setVisibility(isHideRequired ? View.GONE : View.VISIBLE);
        llAddSticker.setVisibility(isHideRequired ? View.GONE : View.VISIBLE);
        if (!context.isReplyReactionWithoutVideo()) {
            llEditClips.setVisibility(isHideRequired ? View.GONE : View.VISIBLE);
            cardCoverPhoto.setVisibility(isHideRequired ? View.GONE : View.VISIBLE);
        }
        progressTimer.setVisibility(isHideRequired ? View.GONE : View.VISIBLE);
        btnPublish.setVisibility(isHideRequired ? View.GONE : View.VISIBLE);
        llAddLink.setVisibility(isHideRequired ? View.GONE : View.VISIBLE);
        llGiphySticker.setVisibility(isHideRequired ? View.GONE : View.VISIBLE);

        if (context.isDirectOrRoundTable() || context.videoOptions == CameraNewActivity.VideoOptions.RECORD_FOR_OTHER) {
            llPrivateVideoMsg.setVisibility(isHideRequired ? View.GONE : View.VISIBLE);
        }
        llDeleteSticker.setVisibility(View.GONE);
    }

    private void addUpdateView(DraggableBaseCustomView baseCustomView, LayerType layerType) {
        ImageStickerModel model = new ImageStickerModel();
        model.setViewId(baseCustomView.getId());
        model.setWidth(baseCustomView.getWidth());
        model.setHeight(baseCustomView.getHeight());
        model.setType(layerType);
        draggableViewsList.add(model);
    }

    private void addUpdateView(DraggableBaseCustomView baseCustomView) {
        ImageStickerModel model = new ImageStickerModel();
        model.setViewId(baseCustomView.getId());
        model.setWidth(baseCustomView.getWidth());
        model.setHeight(baseCustomView.getHeight());
        model.setType(LayerType.IMAGE);
        draggableViewsList.add(model);
    }

    private void addUpdateGIFView(DraggableBaseCustomView baseCustomView, String gifFilePath, int width, int height) {
        ImageStickerModel model = new ImageStickerModel();
        model.setViewId(baseCustomView.getId());
        model.setWidth(width);
        model.setHeight(height);
        model.setGifFilePath(gifFilePath);
        model.setType(LayerType.GIF);
        draggableViewsList.add(model);
    }

    private void publishClickManage() {
        cancelTimerCounter();
        player.setPlayWhenReady(false);
        relativeTutorialSend.setVisibility(View.GONE);
        if (!checkLinkValidation()) {
            isNeedToPerformAction = true;
            if (!BaseAPIService.isShowingProgressDialog()) {
                BaseAPIService.showProgressDialog(context);
            }
            callForValidURL();
            return;
        }
        if (context.videoOptions == CameraNewActivity.VideoOptions.PUBLIC) {
//            HashMap<String, Object> map = new HashMap<String, Object>() {{
//                put("user_id", context.userId);
//                put("device_id", context.deviceId);
//                put("event_id", context.uuid);
//                put("title", Constants.PUBLISH_CLICKED);
//                if (context.prevEventTime != 0) {
//                    put("duration", (System.currentTimeMillis() - context.prevEventTime) / 1000);
//                } else {
//                    put("duration", 0);
//                }
//            }};
//            GenuInApplication.getInstance().sendEventLogs(Constants.PUBLISH_CLICKED, map);
            context.prevEventTime = System.currentTimeMillis();
           // goToPublishVideoFragment();
          //  sendNextButtonClicked(Constants.SCREEN_GENUIN_VIDEO_PUBLISH);
        } else if (context.videoOptions == CameraNewActivity.VideoOptions.DIRECT) {
            //goToDirectVideoFragment();
           // sendNextButtonClicked(Constants.SCREEN_DM_PUBLISH);
        } else if (context.videoOptions == CameraNewActivity.VideoOptions.GROUP) {
            //goToGroupVideoFragment();
            //sendNextButtonClicked(Constants.SCREEN_GROUP_PUBLISH);
        } else if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
            goToRTFragment();
            //sendNextButtonClicked(Constants.SCREEN_RT_PUBLISH)
//        } else if (Utility.isLoggedIn(context) && context.from.equalsIgnoreCase(Constants.FROM_REACTION)) {
//            try {
//                if (!BaseAPIService.isShowingProgressDialog()) {
//                    BaseAPIService.showProgressDialog(context);
//                }
//                checkForReaction();
//                //sendSendButtonClicked(Constants.UNDETERMINED);
//            } catch (Exception e) {
//                showLogException(e);
//            }
//        } else if (Utility.isLoggedIn(context) && context.from.equalsIgnoreCase(Constants.FROM_CHAT)) {
//            try {
//                uploadVideoService();
//                sendSendButtonClicked(Constants.SCREEN_INBOX);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else if (Utility.isLoggedIn(context) && (context.from.equalsIgnoreCase(Constants.FROM_COMMENT) || context.from.equalsIgnoreCase(Constants.FROM_RECORD_FOR_OTHER))) {
//            try {
//                uploadVideoService();
//                if (context.from.equalsIgnoreCase(Constants.FROM_COMMENT)) {
//                    sendSendButtonClicked(Constants.SCREEN_COMMENT);
//                } else {
//                    sendSendButtonClicked(Constants.UNDETERMINED);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        } else {
            uploadVideoService();
            //sendSendButtonClicked(Constants.UNDETERMINED);
        }
    }

    private void checkForReaction() {
        try {
            module = Constants.CHECK_VIDEO;
            String videoId = context.discoverVO.getVideoId();
            JSONObject jsonObject = new JSONObject();
            new BaseAPIService(getContext(), module + videoId, Utility.getRequestBody(jsonObject.toString()), true, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    try {
                        uploadVideoService();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    BaseAPIService.dismissProgressDialog();
                    boolean noNetwork = false;
                    if (player.isPlaying()) {
                        player.pause();
                    }
                    if (error.contains(Constants.CODE_5095) || error.contains(Constants.CODE_5096)) {
                        error = error.substring(4);
                        btnGotItFailure.setText(context.getResources().getString(R.string.go_to_inbox));
                    } else if (error.contains(Constants.NO_NETWORK)) {
                        noNetwork = true;
                        btnGotItFailure.setText(context.getResources().getString(R.string.got_it));
                    } else {
                        btnGotItFailure.setText(context.getResources().getString(R.string.got_it));
                    }
                    if (noNetwork) {
                        tvReactionFailure.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        tvReactionFailure.setText(context.getResources().getString(R.string.no_internet));
                    } else
                        tvReactionFailure.setText(error);

                    reactionBlurLayout.setVisibility(View.VISIBLE);
                }
            }, "POST", false);
        } catch (Exception e) {
            showLogException(e);
        }
    }

    /*private void checkForReply() {
        try {
            module = Constants.CHECK_REPLY;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("video_url", new File(destinationPath).getName());
            jsonObject.put("chat_id", context.chatId);
            new BaseAPIService(getContext(), module, Utility.getRequestBody(jsonObject.toString()), true, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    try {
                        uploadVideoService();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    BaseAPIService.dismissProgressDialog();
                    if (player.isPlaying()) {
                        player.pause();
                    }
                    btnGotItFailure.setText(context.getResources().getString(R.string.go_to_inbox));
                    tvReactionFailure.setText(error);
                    reactionBlurLayout.setVisibility(View.VISIBLE);
                    try {
                        if (Utility.getDBHelper() != null) {
                            Utility.getDBHelper().deleteChatWithStatus(context.chatId);
                        }
                    } catch (Exception e) {
                        Utility.showLogException(e);
                    }
                }
            }, "POST", false);
        } catch (Exception e) {
            showLogException(e);
        }
    }*/

    private boolean checkLinkValidation() {
        if (!TextUtils.isEmpty(etAddLink.getText())) {
            link = etAddLink.getText().toString().trim();
        } else {
            link = "";
        }
        if (!TextUtils.isEmpty(link)) {
            return isValidatedUrl;
        } else {
            return true;
        }
    }

    private void callForValidURL() {
        try {
            if (etAddLink.getText().toString().trim().length() > 0) {
                try {
                    startMills = System.currentTimeMillis();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("url", etAddLink.getText().toString().trim());
                    new BaseAPIService(getActivity(), Constants.VALID_URL,
                            Utility.getRequestBody(jsonObject.toString()), true,
                            new ResponseListener() {
                                @Override
                                public void onSuccess(String response) {
                                    JSONObject responseJson;
                                    try {
                                        endMillis = System.currentTimeMillis();
                                       // final String userId = SharedPrefUtils.getStringPreference(context, Constants.PREF_USER);
                                        final String userId = SDKInitiate.INSTANCE.getUserId();
                                        @SuppressLint("HardwareIds")
                                        final String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//                                        Utility.insertEventToAppsFlyer(context, Constants.LINK_ADD_TO_VIDEO);
                                        responseJson = new JSONObject(response);
                                        if (responseJson.has(Constants.JSON_DATA)) {
                                            JSONObject dataJson = responseJson.getJSONObject(Constants.JSON_DATA);
                                            boolean isValidURL = dataJson.optBoolean("valid_url", false);
                                            if (isValidURL) {
                                                isValidatedUrl = true;
                                                isValidURLNeedToCall = false;
                                                llAddLinkInfo.setVisibility(View.GONE);
                                                //sendAddLinkCloseClicked();
                                                txtInvalidDomain.setVisibility(View.GONE);
                                                if (isNeedToPerformAction) {
                                                    isNeedToPerformAction = false;
                                                    btnPublish.performClick();
                                                }
                                            } else {
                                                BaseAPIService.dismissProgressDialog();
                                                isValidatedUrl = false;
                                                llAddLinkInfo.setVisibility(View.VISIBLE);
                                                txtInvalidDomain.setVisibility(View.VISIBLE);
                                                etAddLink.requestFocus();
                                                if (isNeedToPerformAction) {
                                                    isNeedToPerformAction = false;
                                                    setVideoPlay();
                                                }
                                            }
                                        } else if (responseJson.has("code")) {
                                            BaseAPIService.dismissProgressDialog();
                                            isValidatedUrl = false;
                                            llAddLinkInfo.setVisibility(View.VISIBLE);
                                            txtInvalidDomain.setVisibility(View.VISIBLE);
                                            etAddLink.requestFocus();
                                        }
                                    } catch (JSONException e) {
                                        BaseAPIService.dismissProgressDialog();
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(String error) {
                                    isValidatedUrl = false;
                                    llAddLinkInfo.setVisibility(View.VISIBLE);
                                    txtInvalidDomain.setVisibility(View.VISIBLE);
                                    etAddLink.requestFocus();
                                }
                            }, "POST", !isNeedToPerformAction);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                isValidURLNeedToCall = false;
                llAddLinkInfo.setVisibility(View.GONE);
                txtInvalidDomain.setVisibility(View.GONE);
                //sendAddLinkCloseClicked();
            }
        } catch (Exception e) {
            showLogException(e);
        }
    }

    private void uploadVideoService() {
        Constants.START_MILLIS_REPLY = System.currentTimeMillis();
       // if (Utility.isLoggedIn(context)) {
            stopPlaying();
            if (!TextUtils.isEmpty(etAddLink.getText())) {
                link = etAddLink.getText().toString().trim();
            } else {
                link = "";
            }
            File destinationLocation = context.getExternalFilesDir(Constants.MERGE_DIRECTORY);
            File videoFile = new File(destinationPath);
            String fileName = videoFile.getName().replace(".mp4", ".png");
            File file;
            if (TextUtils.isEmpty(context.coverPhotoPath)) {
                file = new File(destinationLocation, fileName);
                try {
                    Bitmap mBitmap = context.getCoverBitmap(destinationPath, false);
                    if (mBitmap != null) {
                        try (FileOutputStream out = new FileOutputStream(file)) {
                            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            } else {
                file = new File(context.coverPhotoPath);
            }
            if (file.exists()) {
                if (context.isCompressionDone) {
                    try {
                        String finalUrl = destinationPath.substring(destinationPath.lastIndexOf('/') + 1);
                        File to = new File(context.getCacheDir(), finalUrl);
                        InputStream in = new FileInputStream(videoFile);
                        OutputStream out = new FileOutputStream(to);

                        // Copy the bits from instream to outstream
                        byte[] buf = new byte[1024];
                        int len;

                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        in.close();
                        out.close();
                    } catch (Exception e) {
                        showLogException(e);
                    }
                }
                if (context.from.equalsIgnoreCase(Constants.FROM_RECORD_FOR_OTHER)) {
                    //insertPublicVideoForOther(file.getPath());
                } else if (context.from.equalsIgnoreCase(Constants.FROM_COMMENT)) {
                    //insertCommentToDB(file.getPath());
                } else {
                    //insertReactionOrReplyToDB(file.getPath());
                }
                if (context.from.equalsIgnoreCase(Constants.FROM_REACTION)) {
                    Constants.IS_REACTION_GIVEN = true;
                }

                if (context.from.equalsIgnoreCase(Constants.FROM_RECORD_FOR_OTHER)) {
                    showSuccessPage(file.getPath());
                }

                if (context.isCompressionDone) {
                    VideoParamsModel videoParamsModel = new VideoParamsModel();
                    videoParamsModel.from = context.from;
                    if (context.from.equalsIgnoreCase(Constants.FROM_REACTION)) {
                        videoParamsModel.discoverModel = context.discoverVO;
                    } else if (context.from.equalsIgnoreCase(Constants.FROM_CHAT)) {
                        videoParamsModel.chatId = context.chatId;
                        videoParamsModel.convType = context.convType;
                    } else if (context.from.equalsIgnoreCase(Constants.FROM_COMMENT)) {
                        videoParamsModel.chatId = context.chatId;
                        videoParamsModel.videoId = context.videoId;
                        //EventBus.getDefault().post(new CommentCountEvent());
                    }
                    videoParamsModel.isVideoRequired = true;
                    videoParamsModel.isImageRequired = true;
                    videoParamsModel.link = link;
                    videoParamsModel.duration = String.valueOf(Utility.getDurationInt(totalDuration));
                    videoParamsModel.resolution = getVideoResolution();
                    videoParamsModel.aspectRatio = getVideoAspectRatio();
                    videoParamsModel.size = "5";
                    if (context.selectedQuestion != null) {
                        JSONArray jsonArray = new JSONArray();
                        jsonArray.put(context.selectedQuestion.getQuestionId());
                        videoParamsModel.selectedQuestions = jsonArray.toString();
                    }
                    try {
                        JSONObject jsonMetaData = new JSONObject();
                        if (context.selectedTopic != null) {
                            jsonMetaData.put("topic", context.selectedTopic.getName());
                        }
                        jsonMetaData.put("contains_external_videos", context.isAnyNonGenuinVideo());
                        jsonMetaData.put("media_type", MediaType.getMediaType(context.replyOptions));
                        videoParamsModel.metaData = jsonMetaData.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    videoParamsModel.videoFile = destinationPath;
                    videoParamsModel.imageFile = file.getPath();
                    if (context.from.equalsIgnoreCase(Constants.FROM_RECORD_FOR_OTHER)) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("discoverable", true);
                            if (context.selectedTopic != null) {
                                jsonObject.put("topic", context.selectedTopic.getName());
                            }
                            jsonObject.put("contains_external_videos", context.isAnyNonGenuinVideo());
                            jsonObject.put("media_type", MediaType.getMediaType(context.replyOptions));
                            videoParamsModel.settings = jsonObject.toString();
                            videoParamsModel.qrCode = context.qrCode;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    //TODO: Not added yet
                    //UploadQueueManager.getInstance().uploadVideo(context, videoParamsModel);
                } else {
                   // GenuinFFMpegManager.getInstance().addValueToHashmap(destinationPath, true);
                }

//                if (!context.from.equalsIgnoreCase(Constants.FROM_RECORD_FOR_OTHER)) {
//                    if (context.from.equalsIgnoreCase(Constants.FROM_CHAT)) {
//                        if (StoryViewActivity.activity != null) {
//                            StoryViewActivity.activity.finish();
//                        }
//                        if (RTDetailsActivity.activity != null) {
//                            RTDetailsActivity.activity.finish();
//                        }
//                    }
//                    context.cleanUpMemory();
//                    context.finish();
//                }
            }
//        } else {
//            stopPlaying();
//            BaseAPIService.dismissProgressDialog();
//            Constants.WITHOUT_LOGIN_FROM = Constants.FROM_REACTION;
//            Intent i = new Intent(context, LoginActivity.class);
//            loginActivityResultLauncher.launch(i);
//            context.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
//        }
    }

//    private void insertReactionOrReplyToDB(String imagePath) {
//        long timeStamp = System.currentTimeMillis();
//        if (context.from.equalsIgnoreCase(Constants.FROM_REACTION)) {
//            try {
//                ChatModel chat = new ChatModel();
//                chat.setChatId("-101");
//                chat.setVideoUrl(context.discoverVO.getVideoUrl());
//                chat.setThumbnailUrl(context.discoverVO.getVideoThumbnail());
//                chat.setConversationId(context.discoverVO.getVideoId());
//                chat.setConversationAt(String.valueOf(timeStamp));
//                chat.setReply(false);
//                chat.setRead(true);
//                chat.setLink(context.discoverVO.getLink());
//                chat.setDuration(context.discoverVO.getDuration());
//                chat.setResolution(context.discoverVO.getResolution());
//                chat.setSize(context.discoverVO.getSize());
//                chat.setAspectRatio(context.discoverVO.getAspectRatio());
//                chat.setVideoUploadStatus(3);
//                chat.setImageUploadStatus(2);
//                chat.setIsReplyReceived(0);
//                chat.setFfMpegCommand("");
//                chat.setCompressionStatus(1);
//                chat.setConvType(VideoConvType.REACTION.getValue());
//                chat.setQuestions(context.discoverVO.getQuestions());
//
//                ChatModel chat1 = new ChatModel();
//                chat1.setChatId("-101");
//                chat1.setVideoUrl(context.discoverVO.getVideoUrl());
//                chat1.setThumbnailUrl(context.discoverVO.getVideoThumbnail());
//                chat1.setConversationId(context.discoverVO.getVideoId());
//                chat1.setDuration(String.valueOf(Utility.getDurationInt(totalDuration)));
//                chat1.setLink(link);
//                chat1.setResolution(getVideoResolution());
//                chat1.setLocalVideoPath(destinationPath);
//                chat1.setImagePath(imagePath);
//                chat1.setFirstVideoLocalPath(firstVideoPath);
//                chat1.setSize("5");
//                chat1.setAspectRatio(getVideoAspectRatio());
//                chat1.setConversationAt(String.valueOf(timeStamp));
//                chat1.setReply(false);
//                chat1.setRead(true);
//                chat1.setVideoUploadStatus(1);
//                chat1.setImageUploadStatus(1);
//                chat1.setIsReplyReceived(0);
//                chat1.setCompressionStatus(context.isCompressionDone ? 1 : 0);
//                chat1.setFfMpegCommand(context.ffMpegCommand);
//                chat1.setFront(isFront);
//                chat1.setConvType(VideoConvType.REACTION.getValue());
//                if (context.selectedQuestion != null) {
//                    List<QuestionModel> questions = new ArrayList<>();
//                    questions.add(context.selectedQuestion);
//                    chat1.setQuestions(questions);
//                }
//                MetaDataModel metaDataModel = new MetaDataModel();
//                metaDataModel.setContainsExternalVideos(context.isAnyNonGenuinVideo());
//                metaDataModel.setMediaType(MediaType.getMediaType(context.replyOptions));
//                if (context.selectedTopic != null) {
//                    metaDataModel.setTopic(context.selectedTopic.getName());
//                }
//                chat1.setMetaData(metaDataModel);
//
//                GroupModel group = new GroupModel();
//                group.setGroupId("");
//                group.setName("");
//                group.setDescription("");
//                group.setVideoURL(context.discoverVO.getVideoUrl());
//                List<MembersModel> membersList = new ArrayList<>();
//                MembersModel otherUserObject = Utility.getOtherUserObject(context.discoverVO);
//                MembersModel currentUserObject = Utility.getCurrentUserObject(context, context.discoverVO.getVideoUrl());
//                membersList.add(otherUserObject);
//                membersList.add(currentUserObject);
//                group.setMembers(membersList);
//
//                if (Utility.getDBHelper() != null) {
//                    chat.setOwner(otherUserObject);
//                    chat1.setOwner(currentUserObject);
//                    Utility.getDBHelper().insertReactionOrReply(chat, true);
//                    Utility.getDBHelper().insertReactionOrReply(chat1, true);
//                    Utility.getDBHelper().insertGroupAndMembers("-101", group, timeStamp, 1, 1);
//                }
//            } catch (Exception e) {
//                showLogException(e);
//            }
//        } else if (context.from.equalsIgnoreCase(Constants.FROM_CHAT)) {
//            if (context.convType == VideoConvType.ROUND_TABLE.getValue()) {
//                MessageModel messageModel = new MessageModel();
//                messageModel.setChatId(context.chatId);
//                messageModel.setMediaUrl("");
//                messageModel.setThumbnailUrl("");
//                messageModel.setLocalVideoPath(destinationPath);
//                messageModel.setMessageId("");
//                messageModel.setMessageAt(String.valueOf(timeStamp + NEAR_FUTURE_TIME));
//                messageModel.setLink(link);
//                messageModel.setRead(true);
//                messageModel.setVideoUploadStatus(1);
//                messageModel.setImageUploadStatus(1);
//                messageModel.setLocalImagePath(imagePath);
//                messageModel.setCompressionStatus(context.isCompressionDone ? 1 : 0);
//                messageModel.setFfMpegCommand(context.ffMpegCommand);
//                messageModel.setConvType(context.convType);
//                messageModel.setOwner(Utility.getCurrentUserObject(context, ""));
//                if (context.selectedQuestion != null) {
//                    List<QuestionModel> questions = new ArrayList<>();
//                    questions.add(context.selectedQuestion);
//                    messageModel.setQuestions(questions);
//                }
//                MetaDataModel metaDataModel = new MetaDataModel();
//                metaDataModel.setContainsExternalVideos(context.isAnyNonGenuinVideo());
//                metaDataModel.setMediaType(MediaType.getMediaType(context.replyOptions));
//                metaDataModel.setDuration(String.valueOf(Utility.getDurationInt(totalDuration)));
//                metaDataModel.setResolution(getVideoResolution());
//                metaDataModel.setSize("5");
//                metaDataModel.setAspectRatio(getVideoAspectRatio());
//                if (context.selectedTopic != null) {
//                    metaDataModel.setTopic(context.selectedTopic.getName());
//                }
//                messageModel.setMetaData(metaDataModel);
//
//                if (Utility.getDBHelper() != null) {
//                    Utility.getDBHelper().insertLoopVideo(messageModel);
//                    Utility.getDBHelper().updateLatestMessageAt(context.chatId);
//                }
//            } else {
//                ChatModel chat = new ChatModel();
//                chat.setChatId(context.chatId);
//                chat.setVideoUrl("");
//                chat.setThumbnailUrl("");
//                chat.setLocalVideoPath(destinationPath);
//                chat.setFirstVideoLocalPath(firstVideoPath);
//                chat.setConversationId("");
//                chat.setDuration(String.valueOf(Utility.getDurationInt(totalDuration)));
//                chat.setLink(link);
//                chat.setResolution(getVideoResolution());
//                chat.setSize("5");
//                chat.setAspectRatio(getVideoAspectRatio());
//                chat.setConversationAt(String.valueOf(timeStamp + NEAR_FUTURE_TIME));
//                chat.setReply(false);
//                chat.setRead(true);
//                chat.setVideoUploadStatus(1);
//                chat.setImageUploadStatus(1);
//                chat.setImagePath(imagePath);
//                chat.setIsReplyReceived(1);
//                chat.setCompressionStatus(context.isCompressionDone ? 1 : 0);
//                chat.setFfMpegCommand(context.ffMpegCommand);
//                chat.setFront(isFront);
//                chat.setConvType(context.convType);
//                chat.setOwner(Utility.getCurrentUserObject(context, ""));
//                if (context.selectedQuestion != null) {
//                    List<QuestionModel> questions = new ArrayList<>();
//                    questions.add(context.selectedQuestion);
//                    chat.setQuestions(questions);
//                }
//                MetaDataModel metaDataModel = new MetaDataModel();
//                metaDataModel.setContainsExternalVideos(context.isAnyNonGenuinVideo());
//                metaDataModel.setMediaType(MediaType.getMediaType(context.replyOptions));
//                if (context.selectedTopic != null) {
//                    metaDataModel.setTopic(context.selectedTopic.getName());
//                }
//                chat.setMetaData(metaDataModel);
//                if (context.settings != null) {
//                    chat.setSettings(context.settings);
//                }
//
//                if (Utility.getDBHelper() != null) {
//                    Utility.getDBHelper().insertReactionOrReply(chat, false);
//                }
//            }
//        }
//    }
//
//    private void insertCommentToDB(String imagePath) {
//        long timeStamp = System.currentTimeMillis();
//        CommentModel comment = new CommentModel();
//        comment.setCommentId("-101");
//        comment.setChatId(context.chatId);
//        comment.setVideoId(context.videoId);
//        comment.setFileURL("");
//        comment.setThumbnail("");
//        comment.setFileLocalVideoPath(destinationPath);
//        comment.setImageLocalVideoPath(imagePath);
//        comment.setCreatedAt(String.valueOf(timeStamp + NEAR_FUTURE_TIME));
//        comment.setDuration(String.valueOf(Utility.getDurationInt(totalDuration)));
//        comment.setLink(link);
//        comment.setRead(true);
//        comment.setFileUploadStatus(1);
//        comment.setImageUploadStatus(1);
//        comment.setApiStatus(1);
//        comment.setFileType(CommentFileType.VIDEO.getValue());
//        comment.setRetry(false);
//        comment.setShareURL("");
//        comment.setNoOfViews("");
//        comment.setFfMpegCommand(context.ffMpegCommand);
//        comment.setCompressionStatus(context.isCompressionDone ? 1 : 0);
//        comment.setOwner(Utility.getCurrentUserObject(context, ""));
//        if (context.selectedQuestion != null) {
//            List<QuestionModel> questions = new ArrayList<>();
//            questions.add(context.selectedQuestion);
//            comment.setQuestions(questions);
//        }
//        MetaDataModel metaDataModel = new MetaDataModel();
//        metaDataModel.setContainsExternalVideos(context.isAnyNonGenuinVideo());
//        metaDataModel.setMediaType(MediaType.getMediaType(context.replyOptions));
//        if (context.selectedTopic != null) {
//            metaDataModel.setTopic(context.selectedTopic.getName());
//        }
//        comment.setMetaData(metaDataModel);
//
//        if (Utility.getDBHelper() != null) {
//            Utility.getDBHelper().insertComment(comment);
//        }
//    }
//
//    private void insertPublicVideoForOther(String imagePath) {
//        Constants.START_MILLIS_POST = System.currentTimeMillis();
//        PublicVideoModel publicVideoVOPosted = new PublicVideoModel();
//        publicVideoVOPosted.setVideoId("-101");
//        publicVideoVOPosted.setVideoUrl(destinationPath);
//        publicVideoVOPosted.setVideoThumbnail(imagePath);
//        publicVideoVOPosted.setLocalVideoPath(destinationPath);
//        publicVideoVOPosted.setImagePath(imagePath);
//        publicVideoVOPosted.setDuration(String.valueOf(Utility.getDurationInt(totalDuration)));
//        publicVideoVOPosted.setDescription("");
//        publicVideoVOPosted.setLink(link);
//        publicVideoVOPosted.setAspectRatio(getVideoAspectRatio());
//        publicVideoVOPosted.setIsFlag(0);
//        publicVideoVOPosted.setNoOfViews(0);
//        publicVideoVOPosted.setNoOfWaiting(0);
//        publicVideoVOPosted.setNoOfConversation(0);
//        publicVideoVOPosted.setResolution(getVideoResolution());
//        publicVideoVOPosted.setSize("5");
//        publicVideoVOPosted.setShareUrl("");
//        publicVideoVOPosted.setVideoUploadStatus(0);
//        publicVideoVOPosted.setImageUploadStatus(0);
//        publicVideoVOPosted.setQrCode(context.qrCode);
//        if (context.selectedQuestion != null) {
//            JSONArray jsonArray = new JSONArray();
//            jsonArray.put(context.selectedQuestion.getQuestionId());
//            publicVideoVOPosted.setSelectedQuestions(jsonArray.toString());
//        } else {
//            publicVideoVOPosted.setSelectedQuestions("");
//        }
//        MetaDataModel metaDataModel = new MetaDataModel();
//        metaDataModel.setContainsExternalVideos(context.isAnyNonGenuinVideo());
//        metaDataModel.setMediaType(MediaType.getMediaType(context.replyOptions));
//        if (context.selectedTopic != null) {
//            metaDataModel.setTopic(context.selectedTopic.getName());
//        } else {
//            metaDataModel.setTopic("");
//        }
//        publicVideoVOPosted.setMetaData(metaDataModel);
//        publicVideoVOPosted.setDiscoverEnabled(1);
//        publicVideoVOPosted.setApiStatus(0);
//        publicVideoVOPosted.setFfMpegCommand(context.ffMpegCommand);
//        publicVideoVOPosted.setCompressionStatus(context.isCompressionDone ? 1 : 0);
//
//        if (Utility.getDBHelper() != null) {
//            if (!Utility.getDBHelper().checkForPublicVideo(destinationPath)) {
//                Utility.getDBHelper().insertPublicVideoRecord(publicVideoVOPosted);
//            }
//        }
//    }

    private void showSuccessPage(String imagePath) {
        //Removed confetti from here.
        isPublished = true;
        layoutSuccessPage.setVisibility(View.VISIBLE);
        MaterialButton btnShare = layoutSuccessPage.findViewById(R.id.btnShare);
        ImageView ivCloseSuccess = layoutSuccessPage.findViewById(R.id.ivCloseSuccess);
        btnShare.setText(context.getResources().getString(R.string.record_another_video));
        ivCloseSuccess.setOnClickListener(v -> backManage());

        btnShare.setOnClickListener(v -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            context.videoList.clear();
            context.isFullTrim = false;
            context.getSupportFragmentManager().popBackStack();
            if (context.mCameraFragment != null) {
                context.timeStamp = "";
                context.mCameraFragment.closeAndReopenCamera();
                context.mCameraFragment.setProgressAndVideo();
            }
        }, 100));

        ImageView imgThumbnail = layoutSuccessPage.findViewById(R.id.imgThumbnail);
        TextView tvViewTimeDuration = layoutSuccessPage.findViewById(R.id.tvViewTimeDuration);
        if (!TextUtils.isEmpty(imagePath)) {
            Glide.with(context).load(imagePath).into(imgThumbnail);
        }
        tvViewTimeDuration.setText(Utility.getDurationText(String.valueOf(totalDuration)));
    }

    @Override
    public void onResume() {
        super.onResume();
        int count = context.getSupportFragmentManager().getBackStackEntryCount();
        if (relativeTutorialSend.getVisibility() != View.VISIBLE) {
            if (count == 2 && isPaused) {
                setVideoPlay();
            }
            isPaused = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
        stopPlaying();
        cancelTimerCounter();
        if (reactionBlurLayout.getVisibility() == View.VISIBLE) {
            reactionBlurLayout.setVisibility(View.GONE);
            context.finish();
        }
    }

    private void showStartOverAlert() {
        mStartOverDialog = new Dialog(context);
        mStartOverDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mStartOverDialog.setContentView(R.layout.common_simple_dialog_new);
        mStartOverDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mStartOverDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mStartOverDialog.show();

        CustomTextView tvTitle = mStartOverDialog.findViewById(R.id.dialog_title);
        CustomTextView tvMsg = mStartOverDialog.findViewById(R.id.dialog_message);
        CustomTextView btnCancel = mStartOverDialog.findViewById(R.id.dialog_btn_cancel);
        CustomTextView btnYes = mStartOverDialog.findViewById(R.id.dialog_btn_yes);

        btnYes.setText(getResources().getString(R.string.start_over));
        tvTitle.setText(getResources().getString(R.string.genuin));
        tvTitle.setVisibility(View.VISIBLE);
        tvMsg.setText(getResources().getString(R.string.text_start_over_msg));
        btnCancel.setOnClickListener(v -> {
            mStartOverDialog.dismiss();
        });
        btnYes.setOnClickListener(v -> {
            mStartOverDialog.dismiss();
            shouldAskStartOver = false;
            backManage();
        });
    }

    public void backManage() {
        if (isPublished) {
            context.cleanUpMemory();
            context.finish();
            return;
        }

        if ((context.isPhotoReply() || context.isTextReply()) && shouldAskStartOver) {
            showStartOverAlert();
            return;
        }

        context.etDesc = "";
        context.etLink = "";

//        HashMap<String, Object> map = new HashMap<String, Object>() {{
//            put("user_id", context.userId);
//            put("device_id", context.deviceId);
//            put("event_id", context.uuid);
//            if (context.prevEventTime != 0) {
//                put("duration", (System.currentTimeMillis() - context.prevEventTime) / 1000);
//            } else {
//                put("duration", 0);
//            }
//            put("title", Constants.RECORD_PREVIEW_BACK_CLICK);
//        }};
//        GenuInApplication.getInstance().sendEventLogs(Constants.RECORD_PREVIEW_BACK_CLICK, map);
        context.prevEventTime = System.currentTimeMillis();
        isValidURLNeedToCall = false;
        Utility.hideKeyboard(context, null);
        context.progressArray = new int[2];
        context.videoProgress = 0;
        stopPlaying();
        cancelTimerCounter();
        if (draggableViewsList != null) {
            for (int i = 0; i < draggableViewsList.size(); i++) {
                ImageStickerModel model = draggableViewsList.get(i);
                File file = new File(Objects.requireNonNull(model.getFilePath()));
                if (file.exists())
                    file.delete();
            }
            //removing all views
            draggableViewsList.clear();
            draggableViewsList = null;
        }
        //TODO: Need to check for FFMPEG
        //cancelLastSession(destinationPath);
        context.getSupportFragmentManager().popBackStack();
        if (context.mCameraFragment != null) {
            context.mCameraFragment.closeAndReopenCamera();
            if (context.isPhotoReply() || context.isTextReply()) {
                context.videoList.clear();
            }
            context.mCameraFragment.setProgressAndVideo();
        }
        //sendCameraPreviewClosed();
    }

    public void downloadFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } catch (Exception e) {
            showLogException(e);
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == -1) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.storage_permission_dialog_title);
                builder.setPositiveButton(R.string.txt_toolbar_settings, (dialogInterface, i) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                });
                builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
                builder.setMessage(R.string.not_allowed_storage);
                builder.setOnCancelListener(DialogInterface::dismiss);
                builder.show();
            }
        } else if (requestCode == Constants.WRITE_STORAGE_PERMISSION) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        ivVideoMergeDownload.performClick();
                    }
                }
            }
        }
    }


    private void openQuestionDialog(Layer layer) {
        if (questionModel == null) return;
        if (layer != null) {
            currentLayer = layer;
            currentLayer.isVisible = false;
            currentLayer.reDraw();
            currentDraggableQuestionView = (DraggableQuestionView) layer.draggableTextView;
            maxQuestionFontSize = currentDraggableQuestionView.getMaxFontSize();
            currentFontSize = currentDraggableQuestionView.getCurrentFontSize();
            minFontSize = currentDraggableQuestionView.getMinFontSize();
        }
        VideoQuestionEditorDialog questionEditorDialog = new VideoQuestionEditorDialog(context, questionModel, getVideoViewLocation(), this, maxQuestionFontSize, minFontSize, currentFontSize);
        questionEditorDialog.show();
    }

    private void addQuestion() {
        try {
            if (questionModel == null) return;
            int newHeight = getVideoViewHeight();

            Utility.printErrorLog("~~VideoViewHeight: " + newHeight);
            DraggableQuestionView draggableQuestionView = new DraggableQuestionView(context, newHeight);

            int viewId = new Random().nextInt();
            draggableQuestionView.setId(viewId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                draggableQuestionView.updateTextViewAttrs(questionModel, maxQuestionFontSize, maxQuestionFontSize);
            }

            String videoRes = getVideoResolution();
            String[] videoHeightWidth = videoRes.split("x");
            int videoWidth = Integer.parseInt(videoHeightWidth[0]);
            int videoHeight = Integer.parseInt(videoHeightWidth[1]);
            Size videoSize = new Size(videoWidth, videoHeight);
            draggableQuestionView.setVideoActualSize(videoSize);

            Rect playerViewRect = new Rect();
            videoView.getGlobalVisibleRect(playerViewRect);
            draggableQuestionView.setVideoContainerRect(playerViewRect);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            float dip = 50f;
            Resources r = getResources();
            float px50Value = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dip,
                    r.getDisplayMetrics()
            );
            params.setMargins((int) px50Value, 0, (int) px50Value, 0);
            draggableQuestionView.setLayoutParams(params);
            rlStickers.addView(draggableQuestionView);
            draggableQuestionView.setVisibility(View.INVISIBLE);

            rlStickers.post(() -> {
                Bitmap bmp = Utility.loadBitmapFromView(draggableQuestionView);
                rlStickers.removeView(draggableQuestionView);
                if (bmp != null) {
                    if (draggableLayers.layers.size() == 0) {
                        float widthFactor = videoSize.getWidth() * 1.0f / playerViewRect.width();
                        float heightFactor = videoSize.getHeight() * 1.0f / playerViewRect.height();
                        draggableLayers.setValues(playerViewRect.left, playerViewRect.top, widthFactor, heightFactor);
                    }
                    draggableLayers.addLayer(context, bmp, viewId, draggableQuestionView);
                    draggableLayers.invalidate();
                    if (draggableLayers.layers.size() == 1) {
                        rlStickers.addView(draggableLayers);
                    }
                    draggableLayers.layers.get(draggableLayers.layers.size() - 1).convertToImage();
                }
            });
            addUpdateView(draggableQuestionView);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addTopic() {
        try {
            if (context.selectedTopic == null) return;
            int viewId = new Random().nextInt();
            DraggableImageView imageView = new DraggableImageView(context);
            imageView.setImage(context.selectedTopic);
            imageView.setId(viewId);

            String videoRes = getVideoResolution();
            String[] videoHeightWidth = videoRes.split("x");
            int videoWidth = Integer.parseInt(videoHeightWidth[0]);
            int videoHeight = Integer.parseInt(videoHeightWidth[1]);
            Size videoSize = new Size(videoWidth, videoHeight);
            imageView.setVideoActualSize(videoSize);

            Rect playerViewRect = new Rect();
            videoView.getGlobalVisibleRect(playerViewRect);
            imageView.setVideoContainerRect(playerViewRect);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            imageView.setLayoutParams(params);
            rlStickers.addView(imageView);
            imageView.setVisibility(View.INVISIBLE);

            rlStickers.post(() -> {
                Bitmap bmp = Utility.loadBitmapFromView(imageView);
                rlStickers.removeView(imageView);
                if (bmp != null) {
                    if (draggableLayers.layers.size() == 0) {
                        float widthFactor = videoSize.getWidth() * 1.0f / playerViewRect.width();
                        float heightFactor = videoSize.getHeight() * 1.0f / playerViewRect.height();
                        draggableLayers.setValues(playerViewRect.left, playerViewRect.top, widthFactor, heightFactor);
                    }
                    draggableLayers.addLayer(context, bmp, viewId, imageView);
                    if (context.selectedQuestion != null) {
                        draggableLayers.layers.get(draggableLayers.layers.size() - 1).layerTranslateToBelowQuestion(context);
                    }
                    draggableLayers.invalidate();
                    if (draggableLayers.layers.size() == 1) {
                        rlStickers.addView(draggableLayers);
                    }
                    draggableLayers.layers.get(draggableLayers.layers.size() - 1).convertToImage();
                }
            });
            addUpdateView(imageView);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addImage() {
        try {
            if (context.videoList.size() == 0) {
                return;
            }
            int viewId = new Random().nextInt();
            DraggableImageViewFull imageView = new DraggableImageViewFull(context);
            imageView.setImageResourceBg(context.videoList.get(0).videoFileList.get(0).bmp);
            imageView.setId(viewId);

            String videoRes = getVideoResolution();
            String[] videoHeightWidth = videoRes.split("x");
            int videoWidth = Integer.parseInt(videoHeightWidth[0]);
            int videoHeight = Integer.parseInt(videoHeightWidth[1]);
            Size videoSize = new Size(videoWidth, videoHeight);
            imageView.setVideoActualSize(videoSize);

            Rect playerViewRect = new Rect();
            videoView.getGlobalVisibleRect(playerViewRect);
            imageView.setVideoContainerRect(playerViewRect);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            imageView.setLayoutParams(params);
            rlStickers.addView(imageView);
            imageView.setVisibility(View.INVISIBLE);

            rlStickers.post(() -> {
                Bitmap bmp = Utility.loadBitmapFromView(imageView);
                rlStickers.removeView(imageView);
                if (bmp != null) {
                    if (draggableLayers.layers.size() == 0) {
                        float widthFactor = videoSize.getWidth() * 1.0f / playerViewRect.width();
                        float heightFactor = videoSize.getHeight() * 1.0f / playerViewRect.height();
                        draggableLayers.setValues(playerViewRect.left, playerViewRect.top, widthFactor, heightFactor);
                    }
                    draggableLayers.addLayer(context, bmp, viewId, imageView, LayerType.FULL_IMAGE);
                    draggableLayers.invalidate();
                    if (draggableLayers.layers.size() == 1) {
                        rlStickers.addView(draggableLayers);
                    }
                    draggableLayers.layers.get(draggableLayers.layers.size() - 1).convertToImage();
                }
            });
            addUpdateView(imageView, LayerType.FULL_IMAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    private void addAudio() {
//        if (audioPos >= context.videoList.size()) {
//            return;
//        }
//        VideoModel videoModel = context.videoList.get(audioPos);
//        if (TextUtils.isEmpty(videoModel.transcribedText)) {
//            WavToText.with(context).convertWavIntoText(videoModel.videoFileList.get(0).filePath, new WavToTextCallback() {
//                @Override
//                public void onSuccess(String waveToText) {
//                    if (audioPos < context.videoList.size()) {
//                        context.videoList.get(audioPos).transcribedText = waveToText;
//                    }
//                    audioPos++;
//
//                    if (!TextUtils.isEmpty(fullTranscribedText)) {
//                        fullTranscribedText += "\n";
//                    }
//
//                    fullTranscribedText += waveToText;
//
//                    if (audioPos == context.videoList.size()) {
//                        lottieAudioProgress.setVisibility(View.GONE);
//                        startVideoPlaying(false);
//                        saveTranscribeForAudio();
//                        addQuestion();
//                        addTopic();
//                    } else {
//                        addAudio();
//                    }
//                }
//
//                @Override
//                public void onFailed(String reason) {
//                    audioPos++;
//                    if (audioPos == context.videoList.size()) {
//                        lottieAudioProgress.setVisibility(View.GONE);
//                        startVideoPlaying((questionModel == null && context.selectedTopic == null));
//                        addQuestion();
//                        addTopic();
//                    } else {
//                        addAudio();
//                    }
//                }
//            });
//        } else {
//            audioPos++;
//
//            if (!TextUtils.isEmpty(fullTranscribedText)) {
//                fullTranscribedText += "\n";
//            }
//
//            fullTranscribedText += videoModel.transcribedText;
//
//            if (audioPos == context.videoList.size()) {
//                lottieAudioProgress.setVisibility(View.GONE);
//                startVideoPlaying(false);
//                saveTranscribeForAudio();
//                addQuestion();
//                addTopic();
//            } else {
//                addAudio();
//            }
//        }
//    }

    private int getVideoViewHeight() {
        float percentage = Constants.QUESTION_VIEW_MAX_HEIGHT_PERCENTAGE;
        int newHeight = videoView.getMeasuredHeight() > 0 ? (int) ((videoView.getMeasuredHeight() * percentage)) : (int) (videoView.getHeight() * percentage);
        return newHeight;
    }

    @Override
    public void onPositiveButtonClick(@NonNull String text, @NonNull EditorFontModel fontModel, @NonNull EditorColorsModel colorsModel, Bitmap bmp) {
        hideShowView(false);
        rlStickers.setEnabled(true);
        selectedFontModel = fontModel;
        selectedColorModel = colorsModel;
        textToUpdate = text;

        if (currentDraggableView != null) {
            DraggableTextView textView = (DraggableTextView) currentDraggableView;

            colorsModelHashMap.put(textView.getId(), colorsModel);
            fontModelHashMap.put(textView.getId(), fontModel);

            textView.updateTextViewAttrs(textToUpdate, fontModel, colorsModel);
            rlStickers.post(() -> {
                if (bmp != null) {
                    currentLayer.bitmap = bmp;
                    currentLayer.bounds = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
                    currentLayer.isVisible = true;
                    currentLayer.reDraw();
                    draggableLayers.invalidate();
                    draggableLayers.layers.get(draggableLayers.layers.size() - 1).convertToImage();
                }
            });
        } else {
            if (!TextUtils.isEmpty(textToUpdate)) {
                addTextStickers(textToUpdate, bmp, LayerType.IMAGE);
            }
        }
        //sendAddTextClosedClicked(text);
    }

    @Override
    public void onPositiveButtonClick(float maxFontSize, float minFontSize, float currentFontValue, Bitmap bmp) {
        hideShowView(false);
        rlStickers.setEnabled(true);

        this.maxQuestionFontSize = maxFontSize;
        this.minFontSize = minFontSize;
        this.currentFontSize = currentFontValue;

        if (currentDraggableQuestionView != null) {
            currentDraggableQuestionView.updateTextViewAttrs(currentFontValue);
            rlStickers.post(() -> {
                if (bmp != null) {
                    currentLayer.bitmap = bmp;
                    currentLayer.bounds = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
                    currentLayer.isVisible = true;
                    currentLayer.reDraw();
                    draggableLayers.invalidate();
                    draggableLayers.layers.get(draggableLayers.layers.size() - 1).convertToImage();
                }
            });
        } else {
            addQuestion();
        }
    }

    @Override
    public void onNegativeButtonClick() {
        hideShowView(false);
        rlStickers.setEnabled(true);
        if (currentLayer != null) {
            currentLayer.isVisible = true;
            currentLayer.reDraw();
        }
        currentDraggableView = null;
        currentDraggableQuestionView = null;
        currentLayer = null;
    }

    @Override
    public void onDismissListener() {
        hideShowView(false);
        //sendAddTextClosedClicked("");
    }

    @Override
    public void onClearCurrentOverlay() {
        if (currentLayer != null) {
            onDeleteSticker(currentLayer);
        } else {
            hideShowView(false);
        }
    }

    private ImageStickerModel getImageStickerObject(int viewId) {
        ImageStickerModel model = null;
        Utility.printErrorLog("ImageSticker: getImageStickerObject");
        if (draggableViewsList != null && draggableViewsList.size() > 0) {
            for (int i = 0; i < draggableViewsList.size(); i++) {
                if (viewId == draggableViewsList.get(i).getViewId()) {
                    model = draggableViewsList.get(i);
                    break;
                }
            }
        }
        return model;
    }

    private void updateUI(boolean isFromMove, boolean isIntersects, Layer layer) {
        if (isIntersects) {
            llDeleteSticker.setScaleX(1.5f);
            llDeleteSticker.setScaleY(1.5f);
            llDeleteSticker.bringToFront();
            Utility.printErrorLog("Main: overlapped");
            if (layer.layerType != LayerType.FULL_IMAGE) {
                if (isVibrateShouldPlay) {
                    Utility.vibrateDevice(context);
                    isVibrateShouldPlay = false;
                }
                if (isFromMove) {
                    Utility.printErrorLog("Main: overlapped from isMove");
                    layer.isOverlapped = true;
                    layer.reDraw();
                } else {
                    Utility.printErrorLog("Main: overlapped and animation started..");
                    Utility.vibrateDevice(context);
                    layer.isDeleteAnim = true;
                    float deleteCenterX = llDeleteSticker.getX() + (llDeleteSticker.getWidth() / 2f);
                    float deleteCenterY = llDeleteSticker.getY() + Utility.dpToPx(5, context) + tvDeleteSticker.getHeight() + (llDeleteSticker.getHeight() / 2f);
                    layer.setDeleteCenter(deleteCenterX, deleteCenterY);
                    layer.doScaleAnimation();
                }
            }
        } else {
            llDeleteSticker.setScaleX(1f);
            llDeleteSticker.setScaleY(1f);
            draggableLayers.bringToFront();
            isVibrateShouldPlay = true;
            Utility.printErrorLog("Main: No overlapped full visibility..");
            layer.isOverlapped = false;
            layer.reDraw();
            rlStickers.setEnabled(true);
        }
    }

    private void removeView(int viewId) {
        if (draggableViewsList != null && draggableViewsList.size() > 0) {
            for (int i = 0; i < draggableViewsList.size(); i++) {
                if (viewId == draggableViewsList.get(i).getViewId()) {
                    ImageStickerModel model = draggableViewsList.get(i);
                    File file = new File(model.getFilePath());
                    if (file.exists())
                        file.delete();

                    draggableViewsList.remove(i);
                    break;
                }
            }
        }
    }

    @Override
    public void onTouchDown() {
        hideShowView(true);
    }

    @Override
    public void onTouchMove(float touchX, float touchY, @NonNull Layer layer) {
        rlStickers.setEnabled(false);
        if (layer.layerType != LayerType.FULL_IMAGE) {
            llDeleteSticker.setVisibility(View.VISIBLE);
        }
        boolean isViewOverlap = isViewOverlapped(touchX, touchY);
        updateUI(true, isViewOverlap, layer);
    }

    @Override
    public void onTouchRelease(float touchX, float touchY, @NonNull Layer layer) {
        Utility.printErrorLog("ImageSticker: : onTouchRelease");
        isVibrateShouldPlay = false;
        if (llDeleteSticker.getVisibility() == View.VISIBLE && isViewOverlapped(touchX, touchY)) {
            llDeleteSticker.bringToFront();
            updateUI(false, true, layer);
        } else {
            Utility.printErrorLog("ImageSticker: Not overlapped");
            hideShowView(false);
            updateUI(false, false, layer);
        }
    }

    @Override
    public void onTouchClick(@NonNull Layer layer) {
        rlStickers.setEnabled(false);
        hideShowView(true);
        View view = layer.draggableTextView.getChildAt(0);
        if (view instanceof TextView) {
            openTextEditor(layer);
        } else if (view instanceof RelativeLayout) {
            openQuestionDialog(layer);
        } else {
            hideShowView(false);
            updateUI(false, false, layer);
        }
    }

    private boolean isViewOverlapped(float touchX, float touchY) {
        Rect deleteRect = new Rect();
        llDeleteSticker.getGlobalVisibleRect(deleteRect);
        return deleteRect.contains((int) touchX, (int) touchY);
    }

    @Override
    public void onCaptureImage(@NonNull String path, @NonNull Layer layer) {
        currentDraggableView = null;
        currentLayer = null;
        ImageStickerModel model = getImageStickerObject(layer.viewId);
        if (layer.layerType == LayerType.FULL_IMAGE) {
            model.setFilePath(path);
            model.setViewX(layer.translatedXY[0]);
            model.setViewY(layer.translatedXY[1]);
            model.setScaleFactor(layer.scaleFactor);
            model.setRotationAngel(layer.angle);
        } else {
            draggableViewsList.remove(model);
            if (model != null) {
                model.setFilePath(path);
                model.setViewX(layer.translatedXY[0]);
                model.setViewY(layer.translatedXY[1]);
                model.setScaleFactor(layer.scaleFactor);
                model.setRotationAngel(layer.angle);
                if (model.getType() == LayerType.GIF) {
                    model.setHeight(layer.originalHeight);
                    model.setWidth(layer.originalWidth);
                }
                draggableViewsList.add(model);
            }
        }
        executeMergeVideoCommand();
    }

    @Override
    public void onDeleteSticker(@NonNull Layer layer) {
        draggableLayers.removeLayer(layer);
        if (draggableLayers.layers.size() == 0) {
            rlStickers.removeView(draggableLayers);
        }
        llDeleteSticker.setVisibility(View.GONE);
        rlStickers.setEnabled(true);
        hideShowView(false);
        View view = layer.draggableTextView.getChildAt(0);
        int id = layer.viewId;
        if (view instanceof TextView) {
            colorsModelHashMap.remove(id);
            fontModelHashMap.remove(id);
        } else if (view instanceof RelativeLayout) {
            questionModel = null;
            if (context.mCameraFragment != null) {
                context.mCameraFragment.deleteQuestion();
            }
        } else if (view instanceof ImageView) {
            if (context.mCameraFragment != null) {
                context.mCameraFragment.deleteTopic();
            }
        }
        removeView(id);
        executeMergeVideoCommand();
    }

    private String getAudioGIFPath() {
        File audio_gif = new File(context.getCacheDir(), "audio_gif.gif");
        return audio_gif.getAbsolutePath();
    }

    private String getAudioUserNamePath() {
        //MembersModel user = Utility.getCurrentUserObject(context, "");
        //String userName = user.getNickname();
        String userName = "test";
        File userNameFile = new File(context.getCacheDir(), userName + "_audio.png");
        return userNameFile.getAbsolutePath();
    }

    private String getAudioBioPath() {
       // MembersModel user = Utility.getCurrentUserObject(context, "");
        String userBio = "test";
        if (!TextUtils.isEmpty(userBio)) {
            File bioFile = new File(context.getCacheDir(), "bio_audio.png");
            return bioFile.getAbsolutePath();
        } else {
            return "";
        }
    }

    private String getAudioImagePath() {
       // MembersModel user = Utility.getCurrentUserObject(context, "");
        //String userName = user.getNickname();
       // String userImage = user.getProfileImage();
        String userName = "Monali";
        //String userImage = "test";
        File userImageFile;
//        if (user.isAvatar()) {
//            userImageFile = new File(context.getCacheDir(), userImage + "_img.png");
//        } else {
            userImageFile = new File(context.getCacheDir(), userName + "_img.png");
       // }
        return userImageFile.getAbsolutePath();
    }

    public void setVideoCoverImage(Bitmap icon) {
        executeImageCommand();
    }

    private void executeImageCommand() {
        isCoverCommandExecuted = false;
        String outPutResolution = getVideoResolution();
        float resizeFactor = getResizeFactor();
        outPutResolution = outPutResolution.replace("x", ":");
        String outPutAspectRatio = getVideoResolutionRatio();
        File destinationLocation = context.getExternalFilesDir(Constants.MERGE_DIRECTORY);
        File dest;
        if (destinationLocation.exists() || destinationLocation.mkdir()) {
            String fileName = context.videoList.get(0).videoFileList.get(0).fileName;
            try {
                int index = fileName.lastIndexOf("_");
                fileName = fileName.substring(0, index) + ".png";
            } catch (Exception e) {
                showLogException(e);
            }
            dest = new File(destinationLocation, fileName);
            context.coverPhotoPath = dest.getAbsolutePath();
        }

        File file = new File(context.coverPhotoPath);
        if (file.exists()) {
            file.delete();
        }
        boolean isImageExist = false;
        for (int i = 0; i < draggableViewsList.size(); i++) {
            ImageStickerModel model = draggableViewsList.get(i);
            String filePath = model.getFilePath();
            if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                isImageExist = true;
                break;
            }
        }
        String complexCommand = "";
        String audioBioPath = "";
        long seekPos = context.progressArray.length == 0 ? firstVideoTrimMillis : context.progressArray[1];
        String seekStr = Utility.milliToString(seekPos);
        complexCommand += "-ss " + seekStr;
        if (context.isPhotoReply()) {
            File photo_bg = new File(context.getCacheDir(), "photo_bg.png");
            String photoBgPath = photo_bg.getAbsolutePath();
            complexCommand += " -i " + photoBgPath + " ";
        } else if (context.isAudioReply()) {
            File audio_bg = new File(context.getCacheDir(), "audio_bg.png");
            String audioBgPath = audio_bg.getAbsolutePath();
            complexCommand += " -i " + audioBgPath + " ";
        } else {
            complexCommand += " -i " + context.screenShotMediaPath + " ";
        }

        for (int i = 0; i < draggableViewsList.size(); i++) {
            ImageStickerModel model = draggableViewsList.get(i);
            String filePath = model.getFilePath();
            if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                complexCommand += "-i " + model.getFilePath() + " ";
            }
        }

        if (context.isAudioReply()) {
            audioBioPath = getAudioBioPath();
            complexCommand += " -i " + getAudioGIFPath() + " ";
            complexCommand += " -i " + getAudioImagePath() + " ";
            complexCommand += " -i " + getAudioUserNamePath() + " ";
            if (!TextUtils.isEmpty(audioBioPath)) {
                complexCommand += " -i " + getAudioBioPath() + " ";
            }
        }

        complexCommand += "-filter_complex \"";

        if (context.isPhotoReply() || context.isAudioReply()) {
            complexCommand += "[0:v]settb=AVTB,fps=30/1,setpts=PTS*1,scale=" + outPutResolution + "[v]";
            if (context.isAudioReply()) {
                int count = draggableViewsList.size() + 1;
                int gifHW = (int) (Utility.dpToPx(115, context) * resizeFactor);
                int imageHW = (int) (Utility.dpToPx(88, context) * resizeFactor);
                complexCommand += ";[" + (count) + ":v]setpts=PTS-STARTPTS/TB,scale=" + gifHW + ":" + gifHW + "[audioGif]";
                complexCommand += ";[" + (count + 1) + ":v]setpts=PTS-STARTPTS/TB,scale=" + imageHW + ":" + imageHW + "[audioImage]";
                complexCommand += ";[" + (count + 2) + ":v]setpts=PTS-STARTPTS/TB[audioName]";
                if (!TextUtils.isEmpty(audioBioPath)) {
                    complexCommand += ";[" + (count + 3) + ":v]setpts=PTS-STARTPTS/TB[audioBio]";
                }
            }
        } else {
            if (context.screenShotIsFront) {
                complexCommand += "[0:v]settb=AVTB,fps=30/1,setpts=PTS-STARTPTS,scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + ",hflip[v]";
            } else {
                complexCommand += "[0:v]settb=AVTB,fps=30/1,setpts=PTS-STARTPTS,scale=" + outPutResolution + ":force_original_aspect_ratio=decrease,pad=" + outPutResolution + ":(ow-iw)/2:(oh-ih)/2,setdar=" + outPutAspectRatio + "[v]";
            }
        }

        if (isImageExist || context.isAudioReply()) {
            complexCommand += ";[v]";
            String concatCommand = "";
            for (int i = 0; i < draggableViewsList.size(); i++) {
                ImageStickerModel model = draggableViewsList.get(i);

                if (!TextUtils.isEmpty(concatCommand)) {
                    concatCommand += ",";
                }

                String filePath = model.getFilePath();
                if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                    concatCommand += "overlay=" + model.getViewX() + ":" + model.getViewY();
                }
            }
            if (context.isAudioReply()) {
                if (!TextUtils.isEmpty(concatCommand)) {
                    concatCommand += "[o];[o]";
                }
                int height = (int) (getTopAudioHeader() * resizeFactor);
                int imageY = (int) (height + (getImageTopAudioHeader() * resizeFactor));
                int userNameY = (int) (height + (getUserNameTopAudioHeader() * resizeFactor));
                int bioY = (int) (height + (getBioTopAudioHeader() * resizeFactor));
                concatCommand += "[audioGif]overlay=(W-w)/2:" + height + ":shortest=1";
                concatCommand += "[oGif];[oGif][audioImage]overlay=(W-w)/2:" + imageY;
                concatCommand += "[oImage];[oImage][audioName]overlay=(W-w)/2:" + userNameY;
                if (!TextUtils.isEmpty(audioBioPath)) {
                    concatCommand += "[oName];[oName][audioBio]overlay=(W-w)/2:" + bioY;
                }
            }

            complexCommand += concatCommand;
            complexCommand += "[i]\" -map \"[i]\" -frames:v 1 -q:v 2 " + context.coverPhotoPath;
        } else {
            complexCommand += "\" -map \"[v]\" -frames:v 1 -q:v 2 " + context.coverPhotoPath;
        }
        //startFFMpegCommand(complexCommand, context.coverPhotoPath, Constants.SESSION_IMAGE);
    }

//    private void generateFirstBitmap(String path, boolean isFront, long millis) {
//        try {
//            Bitmap bitmap = Utility.retrieveVideoFrameFromVideo(path, isFront, millis);
//            if (bitmap != null) {
//                ivCoverPhoto.setImageBitmap(bitmap);
//            }
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//    }

    private void setAspectRationAdjustment() {
        if (context.videoList.size() == 0) {
            return;
        }
        int[] screenWidthHeight = new int[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            screenWidthHeight = Utility.getScreenWidthHeight(getActivity());
        }
        VideoModel video = context.videoList.get(0);
        Size size = video.previewSize;

        float screenWidth = screenWidthHeight[0];
        float screenHeight = screenWidthHeight[1];

        float videoWidth = size.getHeight();
        float videoHeight = size.getWidth();

        float widthToHeight = screenWidth / screenHeight;
        float widthToHeightVideo = videoHeight / videoWidth;

        double screenAspect = (Math.round(1000 * widthToHeight)) / 1000d;
        double previewAspect = (Math.round(1000 * widthToHeightVideo)) / 1000d;

        int marginBottom;

        marginBottom = Math.abs(previewAspect - screenAspect) < 0.001 ? 0 : (int) Utility.dpToPx(58f, context);

        if (marginBottom == 0) {
            llProgressBar.setBackgroundColor(context.getColor(R.color.transparent));
        } else {
            llProgressBar.setBackgroundColor(context.getColor(R.color.colorBlack));
        }

        float newHeight = (int) ((screenWidth * videoHeight) / videoWidth);
        float newWidth = (int) ((screenHeight * videoWidth) / videoHeight);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
        layoutParams.height = (int) newHeight;
        layoutParams.width = (int) screenWidth;
        layoutParams.setMargins(0, 0, 0, marginBottom);
        cardView.setLayoutParams(layoutParams);
        rlHeaderMain.bringToFront();
    }

    private int[] getVideoViewLocation() {
        int[] locationScreen = new int[2];
        videoView.getLocationOnScreen(locationScreen);
        return locationScreen;
    }
}