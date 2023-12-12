package com.begenuin.library.views.fragments;

import static com.begenuin.library.common.Utility.getRequestBody;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraCharacteristics;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import com.airbnb.lottie.LottieAnimationView;
import com.begenuin.library.R;
import com.begenuin.library.common.Constants;
import com.begenuin.library.common.Utility;
import com.begenuin.library.common.customViews.AutoFitTextureView;
import com.begenuin.library.common.customViews.CustomLeftRightSwipeGesture;
import com.begenuin.library.common.customViews.CustomTextView;
import com.begenuin.library.common.customViews.OverlayView;
import com.begenuin.library.common.customViews.tooltip.SimpleTooltip;
import com.begenuin.library.core.enums.PrivacyOptionsType;
import com.begenuin.library.core.enums.VideoConvType;
import com.begenuin.library.core.interfaces.Camera2Listener;
import com.begenuin.library.core.interfaces.OnSwipeGestureListener;
import com.begenuin.library.core.interfaces.QuestionViewModelListener;
import com.begenuin.library.core.interfaces.ResponseListener;
import com.begenuin.library.data.model.GroupModel;
import com.begenuin.library.data.model.LottieAnimModel;
import com.begenuin.library.data.model.MembersModel;
import com.begenuin.library.data.model.QuestionModel;
import com.begenuin.library.data.model.TopicModel;
import com.begenuin.library.data.model.VideoFileModel;
import com.begenuin.library.data.model.VideoModel;
import com.begenuin.library.data.remote.BaseAPIService;
import com.begenuin.library.data.viewmodel.QuestionViewModel;
import com.begenuin.library.databinding.FragmentCameraNewBinding;
import com.begenuin.library.views.activities.CameraNewActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraNewFragment extends Camera2Fragment implements Camera2Listener, View.OnClickListener,
        QuestionViewModelListener {

    //public View view;
    private File videoFile;
    private File galleryVideoFile;

    public CameraNewActivity context;
    private boolean isRecording;
    private int seekTextWidth = 0;
    private Dialog mCloseDialog;
    private long mLastClickTime = 0;
    private long MAX_TIME = 60000;
    private long ACTUAL_MAX_TIME = 60000;
    private long COUNT_DOWN_TIME = 3000;
    private long CURRENT_PROGRESS = 0;
    private CountDownTimer countDownTimer;
    private int progressRecorded = 0;
    private long retakeDuration = 0, timerDuration = 0;
    private boolean isRetakeFromCamera = false;
    private boolean isRetakeRemoved = false;
    private MaterialButton btnOpenSetting;
    private AppCompatSeekBar viewAlreadyRecorded;
    private float currentVideoScale = 1.0f;
    private float currentVideoSpeed = 1.0f;
    private boolean isCameraEnable = true;
    private boolean sameClicked = false;
    boolean isFirstTime = true;
    private LottieAnimationView animationViewProfile, animationViewProfile1, animationAudio;
//    private ConstraintLayout clProfileList;
//    private RecyclerView rvProfileList;
//    private LinearLayout llProfileLayout;
    private Dialog mSendToDialog;
    private Dialog mSyncContactsDialog, mAddContactDialog;
    private Dialog mDeleteClipDialog;
    //private FrameLayout profileBlurLayout;

    private boolean isPerformTabSwitch = true;
    private boolean isLongPressed = false;
    private boolean isQuestionChanged = false;
    private float startLocation;
    long previousEventTime;
    //private ActivityResultLauncher<Intent> loginActivityResultLauncher, contactActivityResultLauncher;
    private ActivityResultLauncher<String> mPermissionResult;
    private boolean isSwitchedCameraWhileRecording;
    private Handler answerHandler;
    private SimpleTooltip customTooltip;
    private QuestionModel newCustomQuestionModel;
    private String oldQuestionText = "";
    private final ArrayList<TopicModel> topicList = new ArrayList<>();
    private Typeface typeface;
    private final List<VideoFileModel> retakeTempFileList = new ArrayList<>();
    private boolean isLoopQnA, isZeroQuestions;
    private QuestionModel loopSelectedQuestion = null;
    FragmentCameraNewBinding cameraFragmentBinding;

    public CameraNewFragment() {
        // Required empty public constructor
    }

    boolean zoomSelect = false;

    @Override
    public AutoFitTextureView getTextureView() {
        return cameraFragmentBinding.cameraPreviewNew;
    }

    @Override
    public LinearLayout getLLAudio() {
        return cameraFragmentBinding.llAudio;
    }

    @Override
    public RelativeLayout getRLText() {
        return cameraFragmentBinding.rlText;
    }

    @Override
    public OverlayView getOverlayView() {
        return cameraFragmentBinding.overlayView;
    }

    @Override
    public int getTextureResource() {
        return R.id.camera_preview_new;
    }

    @Override
    public int getPermissionResources() {
        return R.id.rtl_permission_new;
    }

    @Override
    public void collapseAnimation() {
        //collapse(clProfileList);
    }

    @Override
    public View getParentView() {
        return cameraFragmentBinding.flMain;
    }

    @Override
    public void onSwipeLeft() {
        if (context.contentGallery.getVisibility() == View.GONE) {
            if (isSwipeAllowed() && cameraFragmentBinding.tabLayout != null && cameraFragmentBinding.tabLayout.getTabCount() > 0) {
                int pos = cameraFragmentBinding.tabLayout.getSelectedTabPosition();
                if (pos < cameraFragmentBinding.tabLayout.getTabCount() - 1) {
                    Objects.requireNonNull(cameraFragmentBinding.tabLayout.getTabAt(pos + 1)).select();
                }
            }
        } else {
            onSwipeCancel();
        }
    }

    @Override
    public void onSwipeRight() {
        if (context.contentGallery.getVisibility() == View.GONE) {
            if (isSwipeAllowed() && cameraFragmentBinding.tabLayout != null && cameraFragmentBinding.tabLayout.getTabCount() > 0) {
                int pos = cameraFragmentBinding.tabLayout.getSelectedTabPosition();
                if (pos > 0) {
                    cameraFragmentBinding.tabLayout.getTabAt(pos - 1).select();
                }
            }
        } else {
            onSwipeCancel();
        }
    }

    @Override
    public void onSwipeTop() {
        if (isSwipeUpAllowed()) {
            float y = context.contentGallery.getTranslationY();
            ObjectAnimator tranYAnim = ObjectAnimator.ofFloat(context.contentGallery, "translationY", y, 0f);
            tranYAnim.setDuration(300);
            tranYAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animator) {

                }

                @Override
                public void onAnimationEnd(@NonNull Animator animator) {
                    context.contentGallery.animate().cancel();
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animator) {

                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animator) {

                }
            });
            tranYAnim.start();
        }
    }

    @Override
    public void onScroll(float diffY) {
        if (isSwipeUpAllowed()) {
            if (context.contentGallery.getVisibility() == View.GONE) {
                Utility.showLog("Tag", diffY + "");
//                if (context.isStoragePermissionsGranted()) {
//                    selectVideoFromGallery();
//                }
            }
            context.contentGallery.setTranslationY(context.screenHeight - diffY);
            context.contentGallery.invalidate();
        }
    }

    @Override
    public void onSwipeCancel() {
        if (isSwipeUpAllowed() && context.contentGallery.getVisibility() == View.VISIBLE) {
            float y = context.contentGallery.getTranslationY();
            ObjectAnimator tranYAnim = ObjectAnimator.ofFloat(context.contentGallery, "translationY", y, context.screenHeight + 50);
            tranYAnim.setDuration(300);
            tranYAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animator) {

                }

                @Override
                public void onAnimationEnd(@NonNull Animator animator) {
                    context.onBackPressed();
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animator) {

                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animator) {

                }
            });
            tranYAnim.start();
        }
    }

    @Override
    public void onImageCaptured() {
        closeCamera();
        addVideoToList(false);
        new Handler(Looper.getMainLooper()).postDelayed(() -> isCameraEnable = true, 300);
        goToVideoPlayFragment();
    }

    private boolean isSwipeAllowed() {
        return (context.videoList == null || context.videoList.size() == 0) && !isRecording && cameraFragmentBinding.llTimerDetails.getVisibility() == View.GONE &&
                (cameraFragmentBinding.frmCameraRecord.getVisibility() == View.VISIBLE || context.isTextReply()) && cameraFragmentBinding.llTabLayout.getVisibility() == View.VISIBLE;
    }

    private boolean isSwipeUpAllowed() {
        return !isRecording && cameraFragmentBinding.llGallery.isEnabled() && cameraFragmentBinding.llGallery.getVisibility() == View.VISIBLE && cameraFragmentBinding.llTimerDetails.getVisibility() == View.GONE;
    }

    @Override
    public void doubleTapped() {
        cameraFragmentBinding.imgSwitchCamera.performClick();
    }

    @Override
    public void isZoomSelected(boolean select) {
        zoomSelect = select;
        if (select) {
            setTextColorGray(cameraFragmentBinding.tvScale1X, cameraFragmentBinding.tvScale2X, cameraFragmentBinding.tvScale4X);
        } else {
            setTextColor(cameraFragmentBinding.tvScale1X, cameraFragmentBinding.tvScale2X, cameraFragmentBinding.tvScale4X);
        }
    }

    @Override
    public boolean isFirstTime() {
        return isFirstTime;
    }

    @Override
    public void pullerDragEnabled(boolean enable) {
        setPullerIsDragEnable(enable);
    }

    private void setPullerIsDragEnable(boolean enable) {
        // if gallery view is present we need to disable puller.
        if (context.contentGallery.getVisibility() == View.GONE) {
            if (context.privateVideoCreateFrom == CameraNewActivity.PrivateVideoCreateFrom.FROM_VIEW_GROUP) {
                context.pullerCameraActivity.setIsDragEnable(false);
            } else {
                context.pullerCameraActivity.setIsDragEnable(enable);
            }
        }
    }

    @Override
    public File getVideoFile(Context context) {
        try {
            File location = context.getExternalFilesDir(Constants.VIDEO_DIRECTORY);
            videoFile = new File(location, getFileName() + Constants.VIDEO_FORMAT);
        } catch (Exception e) {
            videoFile = new File(context.getExternalFilesDir(Constants.VIDEO_DIRECTORY), getFileName() + Constants.VIDEO_FORMAT);
        }
        return videoFile;
    }

    @Override
    public File getAudioFile(Context context) {
        try {
            File location = context.getExternalFilesDir(Constants.VIDEO_DIRECTORY);
            videoFile = new File(location, getFileName() + Constants.AUDIO_FORMAT);
        } catch (Exception e) {
            videoFile = new File(context.getExternalFilesDir(Constants.VIDEO_DIRECTORY), getFileName() + Constants.AUDIO_FORMAT);
        }
        return videoFile;
    }

    @Override
    public File getPhotoFile(Context context) {
        try {
            File location = context.getExternalFilesDir(Constants.VIDEO_DIRECTORY);
            videoFile = new File(location, getFileName() + Constants.IMAGE_FORMAT);
        } catch (Exception e) {
            videoFile = new File(context.getExternalFilesDir(Constants.VIDEO_DIRECTORY), getFileName() + Constants.IMAGE_FORMAT);
        }
        return videoFile;
    }

    @Override
    public File getCurrentVideoFile() {
        return videoFile;
    }

    @Override
    public File getGalleryVideoFile() {
        return galleryVideoFile;
    }

    private String getFileName() {
        String fileName;
        if (context.timeStamp.isEmpty()) {
            context.timeStamp = String.valueOf(new Date().getTime());
            fileName = context.timeStamp + "_Part1";
        } else {
            fileName = context.timeStamp + "_Part" + getVideoPartNo();
        }
        return fileName;
    }

    private int getVideoPartNo() {
        int partNo = 1;
        File location = context.getExternalFilesDir(Constants.VIDEO_DIRECTORY);
        if (location.exists()) {
            File[] files = location.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains(context.timeStamp)) {
                        Utility.showLog("Files", "FileName:" + file.getName());
                        if (file.length() == 0) {
//                            file.delete();
//                            continue;
                            Utility.showLog("Files", "L 0 FileName:" + file.getName());
                        }
                        partNo++;
                    }
                }
            }
        }
        Utility.showLog("TAG", partNo + " Part No");
        return partNo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = (CameraNewActivity) getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        cameraFragmentBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.fragment_camera_new, container, false);
        return cameraFragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        long start = System.currentTimeMillis();
//        SharedPrefUtils.setLongPreference(context, "videoDuration", start);
        setCameraFacing(context.mCameraFacing);
        // && Objects.equals(context.from, Constants.FROM_CHAT)
        isLoopQnA = (context.convType == VideoConvType.ROUND_TABLE.getValue());
        if (getArguments() != null) {
            int retakePos = getArguments().getInt("pos");
            retakeDuration = (long) (context.videoList.get(retakePos).actualDuration * 1000);
        } else {
            if (context.isNeedToShowPopUp) {
                showIntroductoryPopUp();
            }
        }
        typeface = Typeface.create(ResourcesCompat.getFont(context, R.font.avenir_next_medium),
                Typeface.NORMAL);
        initViews();
        registerActivityCallBack();
        QuestionViewModel.getInstance().setListener(this);
        if (context.videoOptions == CameraNewActivity.VideoOptions.RECORD_FOR_OTHER) {
            new Handler().postDelayed(() -> {
                //if (Utility.isLoggedIn(context)) {
                    //getProfileDetailsAPI();
//                } else {
//                    goToLoginActivity();
//                }
            }, 100);
        }
    }

    private void registerActivityCallBack() {
//        loginActivityResultLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        Constants.IS_FEED_REFRESH = true;
//                        if (context.videoOptions == CameraNewActivity.VideoOptions.DIRECT) {
//                            selectDirectVideo(false);
//                        } else if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
//                            selectRoundTableVideo(false);
//                        } else if (context.videoOptions == CameraNewActivity.VideoOptions.RECORD_FOR_OTHER) {
//                            //getProfileDetailsAPI();
//                        }
//                    } else {
//                        if (context.videoOptions == CameraNewActivity.VideoOptions.RECORD_FOR_OTHER) {
//                            context.onPullComplete();
//                        } else {
//                            disableViews();
//                        }
//                    }
//                });
//
//        contactActivityResultLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        assert result.getData() != null;
//                        context.selectedContacts = (ArrayList<ContactsModel>) result.getData().getExtras().getSerializable("selected_contacts");
//                        if (context.selectedContacts.size() > 0) {
//                            setContactsList();
//                        }
//                    } else {
//                        if (context.videoOptions != CameraNewActivity.VideoOptions.ROUND_TABLE) {
//                            if (context.selectedContacts.size() == 0) {
//                                disableViews();
//                                ivAddContact.performClick();
//                            } else if (context.privateVideoSelection == CameraNewActivity.PrivateVideoSelection.DIALOG) {
//                                ivAddContact.performClick();
//                            }
//                        }
//                    }
//                });

//        mPermissionResult = registerForActivityResult(
//                new ActivityResultContracts.RequestPermission(),
//                result -> {
//                    if (result) {
//                        requestContactPermission();
//                    } else {
//                        disableViews();
//                        if (!ActivityCompat.shouldShowRequestPermissionRationale(context,
//                                Manifest.permission.READ_CONTACTS)) {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                            builder.setTitle(context.getResources().getString(R.string.contact_sync_permission));
//                            builder.setMessage(context.getResources().getString(R.string.contact_sync_permission_msg));
//                            builder.setPositiveButton(context.getResources().getString(R.string.go_to_settings), (dialogInterface, i) -> {
//                                Intent intent = new Intent();
//                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
//                                intent.setData(uri);
//                                startActivity(intent);
//                            });
//                            builder.setNegativeButton(context.getResources().getString(R.string.cancel), (dialogInterface, i) -> disableViews());
//                            AlertDialog dialog = builder.create();
//                            dialog.show();
//                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setAllCaps(false);
//                            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setAllCaps(false);
//                        }
//                    }
//                });
    }

    public void addGalleryVideo(String path) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            context.runOnUiThread(() -> {
                if (!BaseAPIService.isShowingProgressDialog()) {
                    BaseAPIService.showProgressDialog(context);
                }
            });
            File from = new File(path);
            File to = new File(galleryVideoFile.getAbsolutePath());
            if (to.exists()) {
                to.delete();
            }
            try {
                boolean isCreated = to.createNewFile();
                Utility.showLog("TAG", "File Created " + isCreated);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                copyFile(from, to);
            } catch (Exception e) {
                Utility.showLogException(e);
            }
            handler.postDelayed(() -> {
                if (galleryVideoFile.exists() && galleryVideoFile.length() > 0) {
                    addVideoToList(true);
                    if (context.isPhotoReply()) {
                        closeCamera();
                        goToVideoPlayFragment();
                    } else {
                        manageViews(View.VISIBLE);
                        cameraFragmentBinding.llProgressBar.setVisibility(View.VISIBLE);
                        setProgressAndVideo();
                    }
                }
                context.runOnUiThread(BaseAPIService::dismissProgressDialog);
            }, 300);
        });
    }

    public void copyFile(File source, File destination) throws IOException {
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(destination);

        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;

        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.flush();
        out.close();
    }

    private void setPrivacyLayout() {
        cameraFragmentBinding.llPrivacyLayout.setVisibility(View.VISIBLE);
        if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
            if (context.privacyOptionsTypeRT == PrivacyOptionsType.EVERYONE) {
                cameraFragmentBinding.ivPrivacyType.setImageResource(R.drawable.ic_globe_with_bg);
                cameraFragmentBinding.ivLock.setImageResource(R.drawable.ic_globe_with_bg);
                cameraFragmentBinding.tvPrivacyOption.setText(context.getResources().getString(R.string.everyone));
            } else {
                cameraFragmentBinding.ivPrivacyType.setImageResource(R.drawable.ic_icon_link);
                cameraFragmentBinding.ivLock.setImageResource(R.drawable.ic_icon_link);
                cameraFragmentBinding.tvPrivacyOption.setText(context.getResources().getString(R.string.unlisted));
            }
        } else if (context.videoOptions == CameraNewActivity.VideoOptions.PUBLIC) {
            if (context.privacyOptionsType == PrivacyOptionsType.EVERYONE) {
                cameraFragmentBinding.ivPrivacyType.setImageResource(R.drawable.ic_globe_with_bg);
                cameraFragmentBinding.tvPrivacyOption.setText(context.getResources().getString(R.string.everyone));
            } else {
                cameraFragmentBinding.ivPrivacyType.setImageResource(R.drawable.ic_icon_link);
                cameraFragmentBinding.tvPrivacyOption.setText(context.getResources().getString(R.string.unlisted));
            }
        }
    }

    private void disableViews() {
        cameraFragmentBinding.ivAddContact.setVisibility(View.VISIBLE);
//        if (Utility.isLoggedIn(context) && context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
//            setPrivacyLayout();
//            return;
//        }
        isViewsDisable = true;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            enableDisableLayout(false);
            cameraFragmentBinding.llRetake.setVisibility(View.INVISIBLE);
            cameraFragmentBinding.llDeleteClip.setVisibility(View.GONE);
            cameraFragmentBinding.ivRecordDone.setVisibility(View.GONE);
            setImageColorGray(cameraFragmentBinding.ivScale, cameraFragmentBinding.ivLength, cameraFragmentBinding.ivTimer, cameraFragmentBinding.ivSpeed,
                    cameraFragmentBinding.ivCollab);
            setTextColorGray(cameraFragmentBinding.tvScale, cameraFragmentBinding.tvLength, cameraFragmentBinding.tvTimer, cameraFragmentBinding.tvSpeed,
                    cameraFragmentBinding.tvCollab);
            setTextColorGray(cameraFragmentBinding.tvScale1X, cameraFragmentBinding.tvScale2X, cameraFragmentBinding.tvScale4X);
            setTextColorGray(cameraFragmentBinding.tvTimer6Sec, cameraFragmentBinding.tvTimer15Sec, cameraFragmentBinding.tvTimer60Sec);
            setTextColorGray(cameraFragmentBinding.tvSpeedPoint3X, cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeed1X,
                    cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeed3X);
            setImageColorGray(cameraFragmentBinding.imgSwitchFlash, cameraFragmentBinding.imgSwitchCamera, cameraFragmentBinding.imgSwitchBg);
        }, 200);
    }

    private void enableViews() {
        isViewsDisable = false;
        cameraFragmentBinding.ivAddContact.setVisibility(View.INVISIBLE);
        cameraFragmentBinding.llPrivacyLayout.setVisibility(View.GONE);
        if (context.videoList.size() > 0) {
            if (isLastVideoFromGallery()) {
                cameraFragmentBinding.llRetake.setVisibility(View.INVISIBLE);
            } else {
                cameraFragmentBinding.llRetake.setVisibility(View.VISIBLE);
            }
            cameraFragmentBinding.llDeleteClip.setVisibility(View.VISIBLE);
            cameraFragmentBinding.ivRecordDone.setVisibility(View.VISIBLE);
        }
        enableDisableLayout(true);
        if (context.isRequiredPermissionsGranted()) {
            setImageColor(cameraFragmentBinding.imgSwitchFlash, cameraFragmentBinding.imgSwitchCamera, cameraFragmentBinding.imgSwitchBg);
        }

        if (cameraFragmentBinding.llScaleDetails.getVisibility() == View.VISIBLE) {
            setTextColor(cameraFragmentBinding.tvScale, cameraFragmentBinding.tvLength, cameraFragmentBinding.tvTimer,
                    cameraFragmentBinding.tvSpeed, cameraFragmentBinding.tvCollab);
            setImageColor(cameraFragmentBinding.ivScale, cameraFragmentBinding.ivLength, cameraFragmentBinding.ivTimer,
                    cameraFragmentBinding.ivSpeed, cameraFragmentBinding.ivCollab);
        } else if (cameraFragmentBinding.llSpeedDetails.getVisibility() == View.VISIBLE) {
            setTextColor(cameraFragmentBinding.tvSpeed, cameraFragmentBinding.tvLength, cameraFragmentBinding.tvTimer,
                    cameraFragmentBinding.tvScale, cameraFragmentBinding.tvCollab);
            setImageColor(cameraFragmentBinding.ivSpeed, cameraFragmentBinding.ivLength, cameraFragmentBinding.ivTimer,
                    cameraFragmentBinding.ivScale, cameraFragmentBinding.ivCollab);
        } else if (cameraFragmentBinding.llTimerDetails.getVisibility() == View.VISIBLE) {
            setTextColor(cameraFragmentBinding.tvTimer, cameraFragmentBinding.tvLength, cameraFragmentBinding.tvScale,
                    cameraFragmentBinding.tvSpeed, cameraFragmentBinding.tvCollab);
            setImageColor(cameraFragmentBinding.ivTimer, cameraFragmentBinding.ivLength, cameraFragmentBinding.ivScale,
                    cameraFragmentBinding.ivSpeed, cameraFragmentBinding.ivCollab);
        }

//        else if (llTimeSelection.getVisibility() == View.VISIBLE) {
//            setTextColor(tvLength, tvSpeed, tvTimer, tvScale, tvCollab);
//            setImageColor(ivLength, ivSpeed, ivTimer, ivScale, ivCollab);
//        }

        if (currentVideoScale == 1.0f) {
            setTextColor(cameraFragmentBinding.tvScale1X, cameraFragmentBinding.tvScale2X, cameraFragmentBinding.tvScale4X);
        } else if (currentVideoScale == 2.0f) {
            setTextColor(cameraFragmentBinding.tvScale2X, cameraFragmentBinding.tvScale1X, cameraFragmentBinding.tvScale4X);
        } else if (currentVideoScale == 4.0f) {
            setTextColor(cameraFragmentBinding.tvScale4X, cameraFragmentBinding.tvScale1X, cameraFragmentBinding.tvScale2X);
        }

        if (currentVideoSpeed == 0.3f) {
            setTextColor(cameraFragmentBinding.tvSpeedPoint3X, cameraFragmentBinding.tvSpeedPoint5X,
                    cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeed3X);
        } else if (currentVideoSpeed == 0.5f) {
            setTextColor(cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeedPoint3X,
                    cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeed3X);
        } else if (currentVideoSpeed == 1f) {
            setTextColor(cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeedPoint5X,
                    cameraFragmentBinding.tvSpeedPoint3X, cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeed3X);
        } else if (currentVideoSpeed == 2f) {
            setTextColor(cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeedPoint5X,
                    cameraFragmentBinding.tvSpeedPoint3X, cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeed3X);
        } else if (currentVideoSpeed == 3f) {
            setTextColor(cameraFragmentBinding.tvSpeed3X, cameraFragmentBinding.tvSpeedPoint3X,
                    cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeed2X);
        }

        if (ACTUAL_MAX_TIME == 6000) {
            setTextColor(cameraFragmentBinding.tvTimer6Sec,cameraFragmentBinding.tvTimer15Sec, cameraFragmentBinding.tvTimer60Sec);
        } else if (ACTUAL_MAX_TIME == 15000) {
            setTextColor(cameraFragmentBinding.tvTimer15Sec, cameraFragmentBinding.tvTimer6Sec, cameraFragmentBinding.tvTimer60Sec);
        } else if (ACTUAL_MAX_TIME == 60000) {
            setTextColor(cameraFragmentBinding.tvTimer60Sec, cameraFragmentBinding.tvTimer6Sec, cameraFragmentBinding.tvTimer15Sec);
        }
    }

    private void setViewAndChildrenEnabled(View view, boolean enabled) {
        if (view != null) {
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View child = viewGroup.getChildAt(i);
                    setViewAndChildrenEnabled(child, enabled);
                }
            }
        }
    }

    private void enableDisableLayout(boolean enable) {
        setViewAndChildrenEnabled(cameraFragmentBinding.llVideoEditingOptions, enable);
        setViewAndChildrenEnabled(cameraFragmentBinding.llScaleDetails, enable);
        setViewAndChildrenEnabled(cameraFragmentBinding.llSpeedDetails, enable);
//        setViewAndChildrenEnabled(llTimeSelection, enable);
        if (context.isRequiredPermissionsGranted()) {
            setViewAndChildrenEnabled(cameraFragmentBinding.frameCameraSwitch, enable);
            setViewAndChildrenEnabled(cameraFragmentBinding.imgSwitchFlash, enable);
        }
        setViewAndChildrenEnabled(cameraFragmentBinding.llGallery, enable);
        setViewAndChildrenEnabled(cameraFragmentBinding.llQuestionSelect, enable);
        setViewAndChildrenEnabled(cameraFragmentBinding.llCameraQuestionView, enable);
        if (enable) {
            if (MAX_TIME <= 1000) {
                cameraFragmentBinding.frmCameraRecord.setEnabled(false);
                cameraFragmentBinding.frmCameraRecord.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
            } else {
                cameraFragmentBinding.frmCameraRecord.setEnabled(true);
                cameraFragmentBinding.frmCameraRecord.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
            }
            cameraFragmentBinding.llGallery.setAlpha(1f);
            cameraFragmentBinding.llQuestionSelect.setAlpha(1f);
            cameraFragmentBinding.llCameraQuestionView.setAlpha(1f);
        } else {
            cameraFragmentBinding.frmCameraRecord.setEnabled(false);
            cameraFragmentBinding.frmCameraRecord.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
            cameraFragmentBinding.llGallery.setAlpha(0.5f);
            cameraFragmentBinding. llQuestionSelect.setAlpha(0.5f);
            cameraFragmentBinding.llCameraQuestionView.setAlpha(0.5f);
        }
    }

    private void showIntroductoryPopUp() {
        Dialog mDialog = new Dialog(context);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.dialog_common_new);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mDialog.show();

        CustomTextView tvTitle = mDialog.findViewById(R.id.dialog_title);
        tvTitle.setVisibility(View.GONE);
        CustomTextView tvMsg = mDialog.findViewById(R.id.dialog_message);
        MaterialButton btnOkay = mDialog.findViewById(R.id.btnOkay);

        tvMsg.setText(context.getResources().getText(R.string.introductory_message));
        btnOkay.setText(context.getResources().getText(R.string.txt_ok));

        btnOkay.setOnClickListener(v -> mDialog.dismiss());
    }

    private void setGroupDesc() {
        Spannable wordToSpan = new SpannableString(context.getResources().getString(R.string.group_desc));
        wordToSpan.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.colorPrimary, null)), 31, 32, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        cameraFragmentBinding.tutorialGroup.tvGroupDesc.setText(wordToSpan);
    }

    private void setRecordForOtherDesc(String userName) {
        cameraFragmentBinding.tvSendToPrefix.setText(context.getResources().getString(R.string.public_video_for_other));
        cameraFragmentBinding.tvSendToStr.setText(String.format(" @%s", userName));
        cameraFragmentBinding.tutorialRecordForOther.tvOtherUserName.setText(String.format("@%s", userName));
        cameraFragmentBinding.tutorialRecordForOther.tvRecordForOtherDesc.setText(context.getResources().getString(R.string.record_for_other_desc, userName));
    }

    private void setPostingToForLoop() {
        cameraFragmentBinding.tvSendToPrefix.setText(context.getResources().getString(R.string.posting_to));
        cameraFragmentBinding.tvSendToStr.setText(String.format(" %s", context.communityHandle));
        cameraFragmentBinding.ivLock.setImageResource(R.drawable.ic_globe_with_bg);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
//        lnrPermission = cameraFragmentBinding.findViewById(R.id.rtl_permission_new);
//        flMain = view.findViewById(R.id.flMain);
//        tabLayout = view.findViewById(R.id.tab_layout);
//        llTabLayout = view.findViewById(R.id.llTabLayout);
//        llMiddle = view.findViewById(R.id.llMiddle);
//        tvMiddleText = view.findViewById(R.id.tvMiddleText);
//        llAddPlusContact = view.findViewById(R.id.llAddPlusContact);
//        tvAddParticipants = view.findViewById(R.id.tvAddParticipants);
//        llPrivacyLayout = view.findViewById(R.id.llPrivacyLayout);
//        ivPrivacyType = view.findViewById(R.id.ivPrivacyType);
//        tvPrivacyOption = view.findViewById(R.id.tvPrivacyOption);
//        tutorialRoundTable = view.findViewById(R.id.tutorialRoundTable);
//        tutorialGroup = view.findViewById(R.id.tutorialGroup);
//        tutorialRecordForOther = view.findViewById(R.id.tutorialRecordForOther);
//        tutorialPublicVideo = view.findViewById(R.id.tutorialPublicVideo);
//        tvGroupDesc = tutorialGroup.findViewById(R.id.tvGroupDesc);
//        tvRecordForOtherDesc = tutorialRecordForOther.findViewById(R.id.tvRecordForOtherDesc);
//        tvOtherUserName = tutorialRecordForOther.findViewById(R.id.tvOtherUserName);
//        btnOpenSetting = view.findViewById(R.id.btn_setting);
//        mTextureView = view.findViewById(R.id.camera_preview_new);
//        overlayView = view.findViewById(R.id.overlay_view);
//        llProgressBar = view.findViewById(R.id.llProgressBar);
//
////        profileBlurLayout = view.findViewById(R.id.profileBlurLayout);
////        profileBlurLayout.setVisibility(View.GONE);
//
//        questionBlurLayout = view.findViewById(R.id.questionBlurLayout);
//        questionBlurLayout.setVisibility(View.GONE);
//
//        llQuestionLayout = view.findViewById(R.id.llQuestionLayout);
//        llNoQuestions = view.findViewById(R.id.llNoQuestions);
//        llAddNewQuestion = view.findViewById(R.id.llAddNewQuestion);
//        questionView = view.findViewById(R.id.questionView);
        //ivEditQuestion = questionView.findViewById(R.id.ivEditQuestion);
        int[] arr = new int[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            arr = Utility.getScreenWidthHeight(context);
        }
        int maxHeightPx = (int) (arr[1] * 0.33f);
        int maxWidth = (int) (arr[0] - Utility.dpToPx(52, context));
        cameraFragmentBinding.cameraQuestionView.setMaxWidth(maxWidth);
        cameraFragmentBinding.cameraQuestionView.setQuestionViewMaxHeight(maxHeightPx);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) cameraFragmentBinding.llQuestionLayout.getLayoutParams();
        int bottomMargin = (int) ((arr[1] - maxHeightPx - Utility.dpToPx(30, context)) / 2);
        layoutParams.setMargins(0, 0, 0, bottomMargin);
//        llShuffleQuestion = view.findViewById(R.id.llShuffleQuestion);
//        llShareQuestion = view.findViewById(R.id.llShareQuestion);
//        tvBlurBottomText = view.findViewById(R.id.tvBlurBottomText);
//        flRecordAnswer = view.findViewById(R.id.flRecordAnswer);
//        ivDeleteQuestion = view.findViewById(R.id.ivDeleteQuestion);
//        tvQuestionHeader = view.findViewById(R.id.tvQuestionHeader);
//        tvNotificationDesc = view.findViewById(R.id.tvNotificationDesc);
        if (isLoopQnA) {
            cameraFragmentBinding.customQuestionView.tvNotificationDesc.setVisibility(View.VISIBLE);
        } else {
            cameraFragmentBinding.customQuestionView.tvNotificationDesc.setVisibility(View.GONE);
        }
//        View dialogViewProfile = view.findViewById(R.id.dialogViewProfile);
//        tvMemberName = dialogViewProfile.findViewById(R.id.tvMemberName);
//        tvMemberUserName = dialogViewProfile.findViewById(R.id.tvMemberUserName);
//        tvMemberBio = dialogViewProfile.findViewById(R.id.tvMemberBio);
//        tvGroupInfoDialogHeader = dialogViewProfile.findViewById(R.id.tvGroupInfoDialogHeader);
//        tvGroupInfoInitials = dialogViewProfile.findViewById(R.id.tvInitials);
//        ivContactPicture = dialogViewProfile.findViewById(R.id.ivContactPicture);
//        animationView = dialogViewProfile.findViewById(R.id.animationView);
//        llClose = dialogViewProfile.findViewById(R.id.llClose);
//        ivCloseQuestion = view.findViewById(R.id.ivCloseQuestion);
//        flRecordButton = view.findViewById(R.id.flRecordButton);
//        frmCameraRecord = view.findViewById(R.id.frm_camera_record);
//        frameCameraSwitch = view.findViewById(R.id.frame_camera_switch);
//        imgCircle2 = view.findViewById(R.id.img_circle2);
//        imgCircle2.setVisibility(View.GONE);
//        imgFlash = view.findViewById(R.id.img_switch_flash);
//        imgSwitchCamera = view.findViewById(R.id.img_switch_camera);
//        imgSwitchBg = view.findViewById(R.id.imgSwitchBg);
//        imgFlash.setImageResource(R.drawable.ic_flashoff);
//        relative_white = view.findViewById(R.id.relative_white);
//        rlRecord = view.findViewById(R.id.rlRecord);
//        ivLength = view.findViewById(R.id.ivLength);
//        tvLength = view.findViewById(R.id.tvLength);
//        ivQuestion = view.findViewById(R.id.ivQuestion);
//        ivTopic = view.findViewById(R.id.ivTopic);
//        ivRemoveTopic = view.findViewById(R.id.ivRemoveTopic);
//        ivRemoveQuestion = view.findViewById(R.id.ivRemoveQuestion);
//        tvLabelQuestion = view.findViewById(R.id.tvLabelQuestion);
//        tvLabelTopic = view.findViewById(R.id.tvLabelTopic);
//        llBottomMiddle = view.findViewById(R.id.llBottomMiddle);
//        llBottomCountDown = view.findViewById(R.id.llBottomCountDown);
//        llVideoEditingOptions = view.findViewById(R.id.llVideoEditingOptions);
//        llScale = view.findViewById(R.id.llScale);
//        llTimer = view.findViewById(R.id.llTimer);
//        llSpeed = view.findViewById(R.id.llSpeed);
////        llLength = view.findViewById(R.id.llLength);
//        llQuestion = view.findViewById(R.id.llQuestion);
//        llTopic = view.findViewById(R.id.llTopic);
//        if (!Constants.IS_SPEED_ENABLE) {
//            llSpeed.setVisibility(View.GONE);
//        }
//        llCollab = view.findViewById(R.id.llCollab);
//        ivScale = view.findViewById(R.id.ivScale);
//        ivTimer = view.findViewById(R.id.ivTimer);
//        ivSpeed = view.findViewById(R.id.ivSpeed);
//        ivCollab = view.findViewById(R.id.ivCollab);
//        tvScale = view.findViewById(R.id.tvScale);
//        tvTimer = view.findViewById(R.id.tvTimer);
//        tvSpeed = view.findViewById(R.id.tvSpeed);
//        tvCollab = view.findViewById(R.id.tvCollab);
//        btnStartCountDown = view.findViewById(R.id.btnStartCountDown);
//        viewAlreadyRecorded = view.findViewById(R.id.viewAlreadyRecorded);
//        viewAlreadyRecorded.setOnTouchListener((view1, motionEvent) -> true);
//        llTimerDetails = view.findViewById(R.id.llTimerDetails);
//        tvTimerText = view.findViewById(R.id.tvTimerText);
//        rlSeekBar = view.findViewById(R.id.rlSeekBar);
//        llScaleDetails = view.findViewById(R.id.llScaleDetails);
//        llSpeedDetails = view.findViewById(R.id.llSpeedDetails);
//        ivAddContact = view.findViewById(R.id.ivAddContact);
//        cameraFragmentBinding.tvRecordTimer = view.findViewById(R.id.cameraFragmentBinding.tvRecordTimer);
//        timerSeekBar = view.findViewById(R.id.timerSeekBar);
//        rlStopRecordingAfter = view.findViewById(R.id.rlStopRecordingAfter);
//        seekProgressValue = view.findViewById(R.id.seekProgressValue);
//        seekProgressValue.post(() -> seekTextWidth = seekProgressValue.getWidth());
//
//        //llProfileLayout = view.findViewById(R.id.llProfileLayout);
//        rlUserImage = view.findViewById(R.id.rlUserImage);
//        rlUserImage1 = view.findViewById(R.id.rlUserImage1);
//        ivUserProfile = view.findViewById(R.id.ivUserProfile);
//        ivUserProfile1 = view.findViewById(R.id.ivUserProfile1);
//        ivUpDown = view.findViewById(R.id.ivUpDown);
//        animationViewProfile = view.findViewById(R.id.animationViewProfile);
//        animationViewProfile1 = view.findViewById(R.id.animationViewProfile1);
//        tvInitials = view.findViewById(R.id.tvInitials);
////        clProfileList = view.findViewById(R.id.clProfileList);
////        rvProfileList = view.findViewById(R.id.rvProfileList);
////        rvProfileList.setLayoutManager(new LinearLayoutManager(context));
////        rvProfileList.setHasFixedSize(true);
//
//        llGallery = view.findViewById(R.id.llGallery);
//        ivLock = view.findViewById(R.id.ivLock);
//        llAudio = view.findViewById(R.id.llAudio);
//        rlText = view.findViewById(R.id.rlText);

        cameraFragmentBinding.timerSeekBar.getParent().requestDisallowInterceptTouchEvent(true);

        cameraFragmentBinding.timerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                setTimerDetailsText(progress, seekBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//        stepProgressView = view.findViewById(R.id.stepProgressView);
//        int[] ints = new int[0];
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
//            ints = Utility.getScreenWidthHeight(context);
//        }
//        stepProgressView.setProgressBarWidth((float) ints[0]);
//        stepProgressView.requestLayout();
//
//        llRetake = view.findViewById(R.id.llRetake);
//        llDeleteClip = view.findViewById(R.id.llDeleteClip);
//        ivRecordDone = view.findViewById(R.id.ivRecordDone);
//        llBottomLayoutCamera = view.findViewById(R.id.llBottomLayoutCamera);
//        ivCloseRecord = view.findViewById(R.id.ivCloseRecord);
//        ivCloseRecord1 = view.findViewById(R.id.ivCloseRecord1);
//
//        viewRedDot = view.findViewById(R.id.viewRedDot);
//        tvTimer6Sec = view.findViewById(R.id.tvTimer6Sec);
//        tvTimer15Sec = view.findViewById(R.id.tvTimer15Sec);
//        tvTimer60Sec = view.findViewById(R.id.tvTimer60Sec);
//        tvTimer3Sec = view.findViewById(R.id.tvTimer3Sec);
//        tvTimer10Sec = view.findViewById(R.id.tvTimer10Sec);
//        tvScale1X = view.findViewById(R.id.tvScale1X);
//        tvScale2X = view.findViewById(R.id.tvScale2X);
//        tvScale4X = view.findViewById(R.id.tvScale4X);
//        tvSpeedPoint3X = view.findViewById(R.id.tvSpeedPoint3X);
//        tvSpeedPoint5X = view.findViewById(R.id.tvSpeedPoint5X);
//        tvSpeed1X = view.findViewById(R.id.tvSpeed1X);
//        tvSpeed2X = view.findViewById(R.id.tvSpeed2X);
//        tvSpeed3X = view.findViewById(R.id.tvSpeed3X);
//        tvSendToStr = view.findViewById(R.id.tvSendToStr);
//        tvSendToPrefix = view.findViewById(R.id.tvSendToPrefix);
//        llSendToLayout = view.findViewById(R.id.llSendToLayout);
//        llQuestionSelect = view.findViewById(R.id.llQuestionSelect);
//        llCameraQuestionView = view.findViewById(R.id.llCameraQuestionView);
//        cameraQuestionView = view.findViewById(R.id.cameraQuestionView);
//        llAddEditQuestion = view.findViewById(R.id.llAddEditQuestion);
//        customAddQuestionView = view.findViewById(R.id.customAddQuestionView);
//        edtAddQuestion = view.findViewById(R.id.edtAddQuestion);
//        tvCharCount = view.findViewById(R.id.tvCharCount);
//        ivBack = view.findViewById(R.id.ivBackNew);
//        tvDone = view.findViewById(R.id.tvDone);
//        tvCustomQuestionHeader = view.findViewById(R.id.tvCustomQuestionHeader);
//        rlAddCustomQuestion = view.findViewById(R.id.rlAddCustomQuestion);
//        rlQuestion = view.findViewById(R.id.rlQuestion);
//        tvAddEditQuestion = view.findViewById(R.id.tvAddEditQuestion);
//        ivAddEditQuestion = view.findViewById(R.id.ivAddEditQuestion);
//        ivAudioGif = view.findViewById(R.id.ivAudioGif);
//        ivAudioProfile = view.findViewById(R.id.ivAudioProfile);
//        animationAudio = view.findViewById(R.id.animationAudio);
//        rlAudioImage = view.findViewById(R.id.rlAudioImage);

        setCameraVisibility();
        onClickHandle();
//        setRoundTableDesc();
        setGroupDesc();
        setUpTabData();

        if (context.isRetake) {
            cameraFragmentBinding.llProgressBar.setVisibility(View.VISIBLE);
            cameraFragmentBinding.llTabLayout.setVisibility(View.GONE);
            cameraFragmentBinding.llBottomMiddle.setVisibility(View.VISIBLE);
            cameraFragmentBinding.stepProgressView.setTotalProgress((int) retakeDuration);
            if (Constants.IS_SPEED_ENABLE) {
                cameraFragmentBinding.llSpeed.performClick();
                if (currentVideoSpeed == 0.3f) {
                    setTextColor(cameraFragmentBinding.tvSpeedPoint3X, cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeed1X,
                            cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeed3X);
                } else if (currentVideoSpeed == 0.5f) {
                    setTextColor(cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeedPoint3X, cameraFragmentBinding.tvSpeed1X,
                            cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeed3X);
                } else if (currentVideoSpeed == 1f) {
                    setTextColor(cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeedPoint3X,
                            cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeed3X);
                } else if (currentVideoSpeed == 2f) {
                    setTextColor(cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeedPoint3X,
                            cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeed3X);
                } else if (currentVideoSpeed == 3f) {
                    setTextColor(cameraFragmentBinding.tvSpeed3X, cameraFragmentBinding.tvSpeedPoint3X, cameraFragmentBinding.tvSpeedPoint5X,
                            cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeed2X);
                }
            }
        }

        if (context.isLongPress) {
            new Handler(Looper.getMainLooper()).postDelayed(this::startLongPressCountDownTimer, 300);
        }

        if (!TextUtils.isEmpty(context.questionRedirectId) || context.isQnA) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> cameraFragmentBinding.llQuestion.performClick(), 300);
        }

        // if-condition split to clear the loop questions array when suggested questions is clicked.
        if (context.isLoopQnA) {
            new Handler(Looper.getMainLooper()).postDelayed(() ->cameraFragmentBinding.llQuestion.performClick(), 300);
        }

        if (isLoopQnA) {
            QuestionViewModel.getInstance().loopQuestionsArr.clear();
        }
    }

    private void setAudioProfile() {
        Glide.with(context).asGif().load(R.drawable.audio_gif).into(cameraFragmentBinding.ivAudioGif);
       // MembersModel user = Utility.getCurrentUserObject(context, "");
        try {
//            if (user.isAvatar()) {
//                int res = context.getResources().getIdentifier(user.getProfileImage(),
//                        "raw", context.getPackageName());
//                Drawable color = new ColorDrawable(context.getResources().getColor(LottieAnimModel.getMapData().get(res), null));
//                ivAudioProfile.setImageDrawable(color);
//                animationAudio.setVisibility(View.VISIBLE);
//                animationAudio.setAnimation(res);
//                animationAudio.playAnimation();
//            } else {
                animationAudio.setVisibility(View.GONE);
//                if (!TextUtils.isEmpty(user.getProfileImageM())) {
//                    Utility.displayProfileImage(context, user.getProfileImageM(), ivAudioProfile);
//                } else {
//                    Utility.displayProfileImage(context, user.getProfileImage(), ivAudioProfile);
//                }
           // }
        } catch (Exception e) {
            Utility.showLogException(e);
        }
    }

    private void setCameraVisibility() {
        if (context.isReplyReactionWithoutVideo()) {
            if (context.isAudioReply()) {
                cameraFragmentBinding.rlText.setVisibility(View.GONE);
                cameraFragmentBinding.llAudio.setVisibility(View.VISIBLE);
                cameraFragmentBinding.frmCameraRecord.setVisibility(View.VISIBLE);
                setAudioProfile();
            } else {
                cameraFragmentBinding.llAudio.setVisibility(View.GONE);
                cameraFragmentBinding.rlText.setVisibility(View.VISIBLE);
                cameraFragmentBinding.frmCameraRecord.setVisibility(View.INVISIBLE);
            }
            cameraFragmentBinding.imgSwitchCamera.clearAnimation();
            cameraFragmentBinding.imgSwitchCamera.setVisibility(View.INVISIBLE);
            cameraFragmentBinding.imgSwitchBg.setVisibility(View.INVISIBLE);
            cameraFragmentBinding.imgSwitchFlash.setVisibility(View.INVISIBLE);
            cameraFragmentBinding.llGallery.setVisibility(View.INVISIBLE);
            if (cameraFragmentBinding.llScaleDetails.getVisibility() == View.VISIBLE) {
                cameraFragmentBinding.llScale.performClick();
            }
            cameraFragmentBinding.llScale.setVisibility(View.GONE);
            cameraFragmentBinding.llTimer.setVisibility(View.GONE);
        } else {
            cameraFragmentBinding.llAudio.setVisibility(View.GONE);
            cameraFragmentBinding.rlText.setVisibility(View.GONE);
            cameraFragmentBinding.frmCameraRecord.setVisibility(View.VISIBLE);
            cameraFragmentBinding.imgSwitchBg.setVisibility(View.VISIBLE);
            cameraFragmentBinding.imgSwitchCamera.setVisibility(View.VISIBLE);
            cameraFragmentBinding.imgSwitchFlash.setVisibility(View.VISIBLE);
            if (!context.isRetake) {
                cameraFragmentBinding.llGallery.setVisibility(View.VISIBLE);
            }
            cameraFragmentBinding.llScale.setVisibility(View.VISIBLE);
            cameraFragmentBinding.llTimer.setVisibility(View.VISIBLE);
            cameraFragmentBinding.rlAudioImage.animate().cancel();
            cameraFragmentBinding.rlAudioImage.clearAnimation();
        }
    }

    private void generateTempQuestionModel() {
        if (newCustomQuestionModel == null) {
            newCustomQuestionModel = new QuestionModel();
        }
        newCustomQuestionModel.setQuestionId(Constants.DUMMY_MODEL_ID);
        //newCustomQuestionModel.setOwner(Utility.getCurrentUserObject(context, ""));
        newCustomQuestionModel.setQuestion("");
        oldQuestionText = newCustomQuestionModel.getQuestion();
    }

    private void showCameraQuestionView() {
        cameraFragmentBinding.llCameraQuestionView.setVisibility(View.VISIBLE);
        setCameraQuestionView();
    }

    private void setCameraQuestionView() {
        if (isQuestionChanged) {
            isQuestionChanged = false;
            int[] arr = new int[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                arr = Utility.getScreenWidthHeight(context);
            }
            int maxHeight16Px = (int) (arr[1] * 0.16f);
            int maxWidth = (int) ((arr[0] * 0.70f) - Utility.dpToPx(20, context));
            cameraFragmentBinding.cameraQuestionView.setQuestionViewMaxHeight(maxHeight16Px);
            cameraFragmentBinding.cameraQuestionView.setMaxWidth(maxWidth);
            cameraFragmentBinding.cameraQuestionView.updateTextViewAttrs(context.selectedQuestion, Constants.QUESTION_FONT_MAX_DEFAULT_SIZE, Constants.QUESTION_FONT_MAX_DEFAULT_SIZE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpTabData() {
        if (context.videoOptions != CameraNewActivity.VideoOptions.REPLY_REACTION && context.videoOptions != CameraNewActivity.VideoOptions.COMMENT && context.videoOptions != CameraNewActivity.VideoOptions.RECORD_FOR_OTHER) {
            if (context.privateVideoCreateFrom == CameraNewActivity.PrivateVideoCreateFrom.NONE) {
                cameraFragmentBinding.tabLayout.addTab(cameraFragmentBinding.tabLayout.newTab().setText(context.getResources().getString(R.string.direct_video)));
                cameraFragmentBinding.tabLayout.addTab(cameraFragmentBinding.tabLayout.newTab().setText(context.getResources().getString(R.string.public_video)));
//                tabLayout.addTab(tabLayout.newTab().setText(context.getResources().getString(R.string.roundtable_video)));
            } else {
                if (context.videoOptions == CameraNewActivity.VideoOptions.DIRECT || context.videoOptions == CameraNewActivity.VideoOptions.GROUP) {
                    cameraFragmentBinding.tabLayout.addTab(cameraFragmentBinding.tabLayout.newTab().setText(context.getResources().getString(R.string.direct_video)));
                } else if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
                    cameraFragmentBinding.tabLayout.addTab(cameraFragmentBinding.tabLayout.newTab().setText(context.getResources().getString(R.string.roundtable_video)));
                }
            }

            if (context.isRetake) {
                centerTextManage();
                if (context.videoOptions == CameraNewActivity.VideoOptions.DIRECT || context.videoOptions == CameraNewActivity.VideoOptions.GROUP) {
                    cameraFragmentBinding.tvMiddleText.setText(context.getResources().getString(R.string.direct_video));
                } else if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
                    cameraFragmentBinding.tvMiddleText.setText(context.getResources().getString(R.string.roundtable_video));
                } else {
                    cameraFragmentBinding.tvMiddleText.setText(context.getResources().getString(R.string.public_video));
                }
            } else {
                cameraFragmentBinding.tabLayout.post(() -> {
                    if (context.isDirectOrRoundTable() && context.privateVideoCreateFrom != CameraNewActivity.PrivateVideoCreateFrom.NONE) {
//                        if (context.selectedContacts.size() > 0) {
//                            setContactsList();
//                        }
                        if (context.videoOptions == CameraNewActivity.VideoOptions.DIRECT || context.videoOptions == CameraNewActivity.VideoOptions.GROUP) {
                            cameraFragmentBinding.tvMiddleText.setText(context.getResources().getString(R.string.direct_video));
//                            if (context.selectedContacts.size() == 0) {
//                                disableViews();
//                            }
                        } else {
                            cameraFragmentBinding.tvMiddleText.setText(context.getResources().getString(R.string.roundtable_video));
                        }
                    } else {
                        if (context.videoOptions == CameraNewActivity.VideoOptions.DIRECT) {
                            cameraFragmentBinding.tvMiddleText.setText(context.getResources().getString(R.string.direct_video));
                            selectDirectVideo(true);
                        } else if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
                            Objects.requireNonNull(cameraFragmentBinding.tabLayout.getTabAt(2)).select();
                        } else {
                            cameraFragmentBinding.llQuestion.setVisibility(View.VISIBLE);
                            cameraFragmentBinding.llTopic.setVisibility(View.VISIBLE);
                            if (!context.isQnA) {
//                                boolean isTutorialShown = SharedPrefUtils.getBoolPreference(context, Constants.PREF_TUTORIAL_PUBLIC_VIDEO);
//                                if (!isTutorialShown) {
//                                    tutorialPublicVideo.setVisibility(View.VISIBLE);
//                                    SharedPrefUtils.setBoolPreference(context, Constants.PREF_TUTORIAL_PUBLIC_VIDEO, true);
//                                }
                            }
                            Objects.requireNonNull(cameraFragmentBinding.tabLayout.getTabAt(1)).select();
                        }
                    }
                });
                cameraFragmentBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        int position = tab.getPosition();
                        switchTab(position);
                        sendCameraSliderMoved(position);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });
            }
        } else {
            if (context.videoOptions == CameraNewActivity.VideoOptions.REPLY_REACTION || context.videoOptions == CameraNewActivity.VideoOptions.COMMENT) {
                if (context.videoOptions == CameraNewActivity.VideoOptions.COMMENT) {
                    setCommentMiddleText();
                } else {
                    if (context.convType == VideoConvType.ROUND_TABLE.getValue()) {
//                    tabLayout.addTab(tabLayout.newTab().setText(context.getResources().getString(R.string.participate)));
                        setParticipateMiddleText();
                    } else {
//                    tabLayout.addTab(tabLayout.newTab().setText(context.getResources().getString(R.string.reply)));
                        //setReplyMiddleText();
                    }
                }
                cameraFragmentBinding.tabLayout.addTab(cameraFragmentBinding.tabLayout.newTab().setText(context.getResources().getString(R.string.reply_video)));
//                tabLayout.addTab(tabLayout.newTab().setText(context.getResources().getString(R.string.reply_photo)));
                cameraFragmentBinding.tabLayout.addTab(cameraFragmentBinding.tabLayout.newTab().setText(context.getResources().getString(R.string.reply_audio)));
                cameraFragmentBinding.tabLayout.addTab(cameraFragmentBinding.tabLayout.newTab().setText(context.getResources().getString(R.string.reply_text)));
                cameraFragmentBinding.tabLayout.addTab(cameraFragmentBinding.tabLayout.newTab().setText(context.getResources().getString(R.string.reply_text)));
                cameraFragmentBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        int position = tab.getPosition();
                        switchTab(position);
                        sendCameraSliderMoved(position);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });
                CustomLeftRightSwipeGesture swipeGesture = new CustomLeftRightSwipeGesture(context, new OnSwipeGestureListener() {
                    @Override
                    public void onScrollDown(float diffY) {

                    }

                    @Override
                    public void onSwipeCancel() {
                        if (context.isTextReply()) {
                            addVideoToList(false);
                            if (context.videoList.size() > 0) {
                                setPullerIsDragEnable(false);
                                goToVideoPlayFragment();
                            }
                        }
                    }

                    @Override
                    public void onSwipeBottom() {

                    }

                    @Override
                    public void onSwipeTop() {

                    }

                    @Override
                    public void onScroll(float diffY) {

                    }

                    @Override
                    public void onSwipeRight() {
                        Utility.printErrorLog("On Swipe Right");
                        CameraNewFragment.this.onSwipeRight();
                    }

                    @Override
                    public void onSwipeLeft() {
                        Utility.printErrorLog("On Swipe Left");
                        CameraNewFragment.this.onSwipeLeft();
                    }
                });

                cameraFragmentBinding.llAudio.setOnTouchListener((view, motionEvent) -> {
                    swipeGesture.onTouch(view, motionEvent);
                    return true;
                });

                cameraFragmentBinding.rlText.setOnTouchListener((view, motionEvent) -> {
                    swipeGesture.onTouch(view, motionEvent);
                    return true;
                });

                cameraFragmentBinding.tabLayout.post(() -> {
                    if (context.replyOptions == CameraNewActivity.ReplyOptions.VIDEO) {
                        if (context.isRequiredPermissionsGranted()) {
                            cameraFragmentBinding.tvMiddleText.setText(context.getResources().getString(R.string.reply_video));
                        } else {
                            selectReplyVideo();
                        }
                    } /*else if (context.replyOptions == CameraNewActivity.ReplyOptions.IMAGE) {
                        Objects.requireNonNull(tabLayout.getTabAt(1)).select();
                    }*/ else if (context.replyOptions == CameraNewActivity.ReplyOptions.AUDIO) {
                        Objects.requireNonNull(cameraFragmentBinding.tabLayout.getTabAt(1)).select();
                    } else if (context.replyOptions == CameraNewActivity.ReplyOptions.TEXT) {
                        Objects.requireNonNull(cameraFragmentBinding.tabLayout.getTabAt(2)).select();
                    }
                });
            } else {
                cameraFragmentBinding.llPrivacyLayout.setVisibility(View.GONE);
                cameraFragmentBinding.ivLock.setImageResource(R.drawable.ic_globe);
                cameraFragmentBinding.tabLayout.addTab(cameraFragmentBinding.tabLayout.newTab().setText(context.getResources().getString(R.string.public_video)));
            }
        }
        //disableTabTouch();
        CustomLeftRightSwipeGesture swipeGesture = new CustomLeftRightSwipeGesture(context, new OnSwipeGestureListener() {

            @Override
            public void onScrollDown(float diffY) {

            }

            @Override
            public void onSwipeCancel() {

            }

            @Override
            public void onSwipeBottom() {

            }

            @Override
            public void onSwipeTop() {

            }

            @Override
            public void onScroll(float diffY) {

            }

            @Override
            public void onSwipeRight() {
                Utility.printErrorLog("On Swipe Right");
                CameraNewFragment.this.onSwipeRight();
            }

            @Override
            public void onSwipeLeft() {
                Utility.printErrorLog("On Swipe Left");
                CameraNewFragment.this.onSwipeLeft();
            }
        });
        cameraFragmentBinding.tabLayout.setGesture(swipeGesture);
    }

//    private void setReplyMiddleText() {
//        if (context.group != null || context.discoverVO != null) {
//            llPrivacyLayout.setVisibility(View.VISIBLE);
//            if (!context.isLongPress) {
//                //llProfileLayout.setVisibility(View.VISIBLE);
//            }
//            rlUserImage.setVisibility(View.VISIBLE);
//            ivPrivacyType.setImageResource(R.drawable.ic_lock);
//            if (context.convType == VideoConvType.GROUP.getValue()) {
//                //llProfileLayout.setTag(context.group);
//                String groupName = context.group.getName();
//                TypefaceSpan typefaceSpan = new TypefaceSpan(typeface);
//                SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
//                stringBuilder.append(context.getResources().getString(R.string.reply_in));
//                stringBuilder.setSpan(typefaceSpan, 0, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                stringBuilder.append(" ");
//                stringBuilder.append(groupName);
//                tvPrivacyOption.setText(stringBuilder);
//                setGroupProfilePic(context.group);
//            } else {
//                String userName = "";
//                if (context.convType == VideoConvType.DIRECT.getValue() || context.from.equalsIgnoreCase(Constants.FROM_CHAT)) {
//                   // final String userId = SharedPrefUtils.getStringPreference(context, Constants.PREF_USER);
//                    if (context.group.getMembers() != null) {
//                        for (int i = 0; i < context.group.getMembers().size(); i++) {
//                            MembersModel member = context.group.getMembers().get(i);
//                            if (!member.getUserId().equalsIgnoreCase(userId)) {
//                                llProfileLayout.setTag(member);
//                                if (!TextUtils.isEmpty(member.getNickname())) {
//                                    userName = String.format("@%s", member.getNickname());
//                                    context.toReplyUserName = userName;
//                                    setMemberProfilePic(member);
//                                } else {
//                                    if (Utility.getDBHelper() != null) {
//                                        context.contactListHashMap = Utility.getDBHelper().getAllContactsHashMap();
//                                    }
//                                    if (context.contactListHashMap != null && context.contactListHashMap.containsKey(member.getPhone())) {
//                                        userName = Objects.requireNonNull(context.contactListHashMap.get(member.getPhone())).getFirstName();
//                                    } else {
//                                        userName = PhoneNumberUtils.formatNumber("+" + member.getPhone(), Utility.getDeviceCountryCode(context));
//                                    }
//                                    context.toReplyUserName = userName;
//                                    animationViewProfile.setVisibility(View.GONE);
//                                    Glide.with(context).load(R.drawable.placeholder_contact).into(ivUserProfile);
//                                }
//                                break;
//                            }
//                        }
//                    }
//                } else {
//                    if (context.discoverVO != null) {
//                        llPrivacyLayout.setTag(context.discoverVO);
//                        userName = String.format("@%s", context.discoverVO.getNickName());
//                        context.toReplyUserName = userName;
//                        if (context.discoverVO.getAvatar()) {
//                            int res = context.getResources().getIdentifier(context.discoverVO.getProfileImage(),
//                                    "raw", context.getPackageName());
//                            Drawable color = new ColorDrawable(context.getResources().getColor(LottieAnimModel.getMapData().get(res), null));
//                            ivUserProfile.setImageDrawable(color);
//                            animationViewProfile.setVisibility(View.VISIBLE);
//                            animationViewProfile.setAnimation(res);
//                            animationViewProfile.playAnimation();
//                        } else {
//                            animationViewProfile.setVisibility(View.GONE);
//                            if (TextUtils.isEmpty(context.discoverVO.getProfileImageM())) {
//                                Glide.with(context).load(context.discoverVO.getProfileImage()).apply(RequestOptions.circleCropTransform().circleCrop()).into(ivUserProfile);
//                            } else {
//                                Glide.with(context).load(context.discoverVO.getProfileImageM()).apply(RequestOptions.circleCropTransform().circleCrop()).into(ivUserProfile);
//                            }
//                        }
//                    }
//                }
//                TypefaceSpan typefaceSpan = new TypefaceSpan(typeface);
//                SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
//                stringBuilder.append(context.getResources().getString(R.string.reply));
//                stringBuilder.setSpan(typefaceSpan, 0, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                stringBuilder.append(" ");
//                stringBuilder.append(userName);
//                tvPrivacyOption.setText(stringBuilder);
//            }
//        } else {
//            llPrivacyLayout.setVisibility(View.GONE);
//        }
//    }

    private void setParticipateMiddleText() {
        if (context.group != null) {
            //llProfileLayout.setTag(context.group);
            cameraFragmentBinding.llPrivacyLayout.setVisibility(View.VISIBLE);
            if (!context.isLongPress) {
               // llProfileLayout.setVisibility(View.VISIBLE);
            }
            cameraFragmentBinding.rlUserImage.setVisibility(View.VISIBLE);
            if (context.privacyOptionsTypeRT == PrivacyOptionsType.UNLISTED) {
                cameraFragmentBinding.ivPrivacyType.setImageResource(R.drawable.ic_icon_link);
            } else {
                cameraFragmentBinding.ivPrivacyType.setImageResource(R.drawable.ic_globe);
            }
            String groupName = context.group.getName();
            TypefaceSpan typefaceSpan = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                typefaceSpan = new TypefaceSpan(typeface);
            }
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            stringBuilder.append(context.getResources().getString(R.string.posting_to));
            stringBuilder.setSpan(typefaceSpan, 0, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.append(" ");
            stringBuilder.append(groupName);
            cameraFragmentBinding.tvPrivacyOption.setText(stringBuilder);
            setGroupProfilePic(context.group);
        } else {
            cameraFragmentBinding.llPrivacyLayout.setVisibility(View.GONE);
        }
    }

    private void setCommentMiddleText() {
        if (context.chat != null) {
            MembersModel owner = context.chat.getOwner();
            cameraFragmentBinding.llPrivacyLayout.setVisibility(View.VISIBLE);
           // llProfileLayout.setVisibility(View.VISIBLE);
            cameraFragmentBinding.rlUserImage.setVisibility(View.VISIBLE);
            //llProfileLayout.setTag(owner);
            if (context.settings != null && context.settings.getDiscoverable()) {
                cameraFragmentBinding.ivPrivacyType.setVisibility(View.VISIBLE);
                cameraFragmentBinding.ivPrivacyType.setImageResource(R.drawable.ic_globe);
            } else {
                cameraFragmentBinding.ivPrivacyType.setVisibility(View.GONE);
            }
            String ownerName = String.format("@%s's", owner.getNickname());
            TypefaceSpan typefaceSpan = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                typefaceSpan = new TypefaceSpan(typeface);
            }
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            stringBuilder.append(context.getResources().getString(R.string.comment_on));
            stringBuilder.setSpan(typefaceSpan, 0, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.append(" ");
            stringBuilder.append(ownerName);
            stringBuilder.append(" ");
            int length = context.getResources().getString(R.string.comment_on).length() + ownerName.length() + 2;
            stringBuilder.append(context.getResources().getString(R.string.video));
            TypefaceSpan typefaceSpan1 = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                typefaceSpan1 = new TypefaceSpan(typeface);
            }
            stringBuilder.setSpan(typefaceSpan1, length, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            cameraFragmentBinding.tvPrivacyOption.setText(stringBuilder);
            setMemberProfilePic(owner);
        } else {
            cameraFragmentBinding.llPrivacyLayout.setVisibility(View.GONE);
        }
    }

    private void setMemberProfilePic(MembersModel membersModel) {
        if (membersModel.isAvatar()) {
            int res = context.getResources().getIdentifier(membersModel.getProfileImage(),
                    "raw", context.getPackageName());
            Drawable color = new ColorDrawable(context.getResources().getColor(LottieAnimModel.getMapData().get(res), null));
            cameraFragmentBinding.ivUserProfile.setImageDrawable(color);
            animationViewProfile.setVisibility(View.VISIBLE);
            animationViewProfile.setAnimation(res);
            animationViewProfile.playAnimation();
        } else {
            animationViewProfile.setVisibility(View.GONE);
            if (TextUtils.isEmpty(membersModel.getProfileImageM())) {
                Glide.with(context).load(membersModel.getProfileImage()).apply(RequestOptions.circleCropTransform().circleCrop()).into(cameraFragmentBinding.ivUserProfile);
            } else {
                Glide.with(context).load(membersModel.getProfileImageM()).apply(RequestOptions.circleCropTransform().circleCrop()).into(cameraFragmentBinding.ivUserProfile);
            }
        }
    }

    private void setGroupProfilePic(GroupModel group) {
        animationViewProfile.setVisibility(View.GONE);
        if (TextUtils.isEmpty(group.getDp())) {
            cameraFragmentBinding.tvInitials.setVisibility(View.VISIBLE);
            cameraFragmentBinding.ivUserProfile.setImageResource(R.color.transparent);
            cameraFragmentBinding.ivUserProfile.setCircleBackgroundColor(Color.parseColor(group.getColorCode()));
            setTextForDp(group.getName(), cameraFragmentBinding.tvInitials);
            cameraFragmentBinding.tvInitials.setTextColor(Color.parseColor(group.getTextColorCode()));
        } else {
            cameraFragmentBinding.tvInitials.setVisibility(View.GONE);
            Utility.displayProfileImage(context, group.getDp(), cameraFragmentBinding.ivUserProfile);
        }
    }

    public void setTextForDp(String name, TextView textView) {
        name = name.replaceAll("\\s+", " ").trim();
        String[] nameArr = name.split(" ");
        if (nameArr.length > 1) {
            textView.setText(String.format("%s%s", getFirstSymbol(nameArr[0].toUpperCase()), getFirstSymbol(nameArr[1].toUpperCase())));
        } else if (nameArr.length == 1) {
            textView.setText(String.format("%s", getFirstSymbol(nameArr[0].toUpperCase())));
        } else {
            textView.setText("");
        }
    }

    private String getFirstSymbol(String text) {
        StringBuilder sequence = new StringBuilder(text.length());
        boolean isInJoin = false;
        int codePoint;

        for (int i = 0; i < text.length(); i = text.offsetByCodePoints(i, 1)) {
            codePoint = text.codePointAt(i);

            if (codePoint == 0x200D) {
                isInJoin = true;
                if (sequence.length() == 0)
                    continue;
            } else {
                if ((sequence.length() > 0) && (!isInJoin)) break;
                isInJoin = false;
            }

            sequence.appendCodePoint(codePoint);
        }

        if (isInJoin) {
            for (int i = sequence.length() - 1; i >= 0; --i) {
                if (sequence.charAt(i) == 0x200D)
                    sequence.deleteCharAt(i);
                else
                    break;
            }
        }
        return sequence.toString();
    }

    private void sendCameraSliderMoved(int pos) {
        String itemSelected;
        switch (pos) {
            case 1:
                itemSelected = "Public";
                break;
            case 2:
                itemSelected = "Roundtable";
                break;
            default:
                itemSelected = "Direct";
                break;
        }
    }

    private void switchTab(int position) {
        if (isPerformTabSwitch) {
            if (context.videoOptions == CameraNewActivity.VideoOptions.REPLY_REACTION || context.videoOptions == CameraNewActivity.VideoOptions.COMMENT) {
                if (position == 0) {
                    selectReplyVideo();
                } else if (position == 1) {
                    selectReplyAudio();
//                    selectReplyPhoto();
                } else if (position == 2) {
//                    selectReplyAudio();
                    selectReplyText();
                } /*else if (position == 3) {
                    selectReplyText();
                }*/
            } else {
                if (position == 0) {
                    context.videoOptions = CameraNewActivity.VideoOptions.DIRECT;
                    cameraFragmentBinding.tvMiddleText.setText(context.getResources().getString(R.string.direct_video));
                    cameraFragmentBinding.ivLock.setImageResource(R.drawable.ic_lock);
//                    if (context.selectedContacts.size() == 0) {
//                        selectDirectVideo(false);
//                    } else if (context.videoList.size() == 0) {
//                        llSendToLayout.setVisibility(View.VISIBLE);
//                        llPrivacyLayout.setVisibility(View.GONE);
//                    }
//                    if (context.selectedContacts.size() > 0) {
//                        setSendToStr();
//                        restoreDefaultProfileList();
//                        // remove video size condition if need to display profile view after recording
//                        if (context.videoList.size() == 0) {
//                            //llProfileLayout.setVisibility(View.VISIBLE);
//                        }
//                        if (context.selectedContacts.size() > 1) {
//                            context.videoOptions = CameraNewActivity.VideoOptions.GROUP;
//                        } else {
//                            context.videoOptions = CameraNewActivity.VideoOptions.DIRECT;
//                        }
//                    }
                } else if (position == 1) {
                    selectPublicVideo();
                    if (context.videoList.size() == 0) {
                        setPrivacyLayout();
                    }
                } else if (position == 2) {
                    context.videoOptions = CameraNewActivity.VideoOptions.ROUND_TABLE;
                    cameraFragmentBinding.tvMiddleText.setText(context.getResources().getString(R.string.roundtable_video));
                    if (context.privacyOptionsTypeRT == PrivacyOptionsType.EVERYONE) {
                        cameraFragmentBinding.ivLock.setImageResource(R.drawable.ic_globe);
                    } else {
                        cameraFragmentBinding.ivLock.setImageResource(R.drawable.ic_icon_link);
                    }
//                    if (context.selectedContacts.size() == 0) {
//                        selectRoundTableVideo(true);
//                        setPrivacyLayout();
//                    } else if (context.videoList.size() == 0) {
//                        llSendToLayout.setVisibility(View.VISIBLE);
//                        llPrivacyLayout.setVisibility(View.GONE);
//                    }
//                    if (context.selectedContacts.size() > 0) {
//                        setSendToStr();
//                        restoreDefaultProfileList();
//                        // remove video size condition if need to display profile view after recording
//                        if (context.videoList.size() == 0) {
//                            //llProfileLayout.setVisibility(View.VISIBLE);
//                        }
//                    }
                }
            }
        }
        isPerformTabSwitch = true;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClickHandle() {
        cameraFragmentBinding.llQuestionLayout.setOnClickListener(this);
        cameraFragmentBinding.imgSwitchCamera.setOnClickListener(this);
        cameraFragmentBinding.llRetake.setOnClickListener(this);
        cameraFragmentBinding.llDeleteClip.setOnClickListener(this);
        cameraFragmentBinding.ivRecordDone.setOnClickListener(this);
        cameraFragmentBinding.ivCloseRecord.setOnClickListener(this);
        cameraFragmentBinding.ivCloseRecord1.setOnClickListener(this);
        cameraFragmentBinding.imgSwitchFlash.setOnClickListener(this);
        cameraFragmentBinding.cameraQuestionView.setOnClickListener(this);
        cameraFragmentBinding.btnStartCountDown.setOnClickListener(this);
        cameraFragmentBinding.llScale.setOnClickListener(this);
        cameraFragmentBinding.llTimer.setOnClickListener(this);
        cameraFragmentBinding.llSpeed.setOnClickListener(this);
        cameraFragmentBinding.llCollab.setOnClickListener(this);
        cameraFragmentBinding.tvScale1X.setOnClickListener(this);
        cameraFragmentBinding.tvScale2X.setOnClickListener(this);
        cameraFragmentBinding.tvScale4X.setOnClickListener(this);
//        llLength.setOnClickListener(this);
        cameraFragmentBinding.llQuestion.setOnClickListener(this);
        cameraFragmentBinding.llTopic.setOnClickListener(this);
        cameraFragmentBinding.llSendToLayout.setOnClickListener(this);
        cameraFragmentBinding.ivAddContact.setOnClickListener(this);
        cameraFragmentBinding.llAddPlusContact.setOnClickListener(this);
        cameraFragmentBinding.llQuestionSelect.setOnClickListener(this);
        cameraFragmentBinding.cameraQuestionView.setOnClickListener(this);
        cameraFragmentBinding.ivDeleteQuestion.setOnClickListener(this);
        cameraFragmentBinding.llShareQuestion.setOnClickListener(this);
        cameraFragmentBinding.llGallery.setOnClickListener(this);
        cameraFragmentBinding.llPrivacyLayout.setOnClickListener(this);
        cameraFragmentBinding.frmCameraRecord.setOnLongClickListener(v -> {
            Utility.showLog("Tag", "LongClick : " + isRecording);
            if (!isRecording && !context.isRetake) {
                isLongPressed = true;
                Utility.vibrateDevice(context);
                onCameraControlClick();
                return true;
            } else {
                return false;
            }
        });

        cameraFragmentBinding.frmCameraRecord.setOnClickListener(v -> {
            Utility.showLog("Tag", "Click : " + isLongPressed);
            isLongPressed = false;
            onCameraControlClick();
        });

        cameraFragmentBinding.frmCameraRecord.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                startLocation = event.getY();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                Utility.showLog("Tag", "Action Up : " + isLongPressed);
                if (isLongPressed) {
                    onCameraControlClick();
                    isLongPressed = false;
                    return true;
                } else {
                    return false;
                }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (isLongPressed && (startLocation - event.getY()) > 10) {
                    Utility.showLog("Move", startLocation + " : " + event.getY());
                    setZoom(startLocation, event.getY());
                }
            }
            return false;
        });

//        cameraFragmentBinding.tutorialGroup.setOnClickListener(
//                v -> cameraFragmentBinding.tutorialGroup.setVisibility(View.GONE));

        cameraFragmentBinding.tutorialRoundTable.setOnClickListener(v -> {
            cameraFragmentBinding.tutorialRoundTable.setVisibility(View.GONE);
            //showAddContactsDialog();
        });

//        cameraFragmentBinding.tutorialRecordForOther.setOnClickListener(
//                v -> cameraFragmentBinding.tutorialRecordForOther.setVisibility(View.GONE));

        cameraFragmentBinding.tutorialPublicVideo.setOnClickListener(v -> {
            cameraFragmentBinding.tutorialPublicVideo.setVisibility(View.GONE);
        });

        //profileBlurLayout.setOnClickListener(v -> profileBlurLayout.setVisibility(View.GONE));

        cameraFragmentBinding.ivCloseQuestion.setOnClickListener(v -> {
           // sendSegmentLogs(Constants.QNA_MODULE_CLOSED, new Properties());
            questionNormalFromBlur();
        });

//        llClose.setOnClickListener(v -> profileBlurLayout.setVisibility(View.GONE));

        cameraFragmentBinding.flRecordAnswer.setOnClickListener(v -> selectQuestion());

        cameraFragmentBinding.llShuffleQuestion.setOnClickListener(v -> {
            if (isLoopQnA) {
                shuffleLoopQuestion();
            } else {
                shuffleQuestion();
            }
        });

        cameraFragmentBinding.tvTimer6Sec.setOnClickListener(view12 -> {
            MAX_TIME = 6000;
            ACTUAL_MAX_TIME = 6000;
            cameraFragmentBinding.timerSeekBar.setMax((int) (ACTUAL_MAX_TIME / 100));
            cameraFragmentBinding.timerSeekBar.setProgress(0);
            viewAlreadyRecorded.setMax((int) (ACTUAL_MAX_TIME / 100));
            viewAlreadyRecorded.setProgress(0);
            previousEventTime = System.currentTimeMillis();
            setTextColor(cameraFragmentBinding.tvTimer6Sec, cameraFragmentBinding.tvTimer15Sec, cameraFragmentBinding.tvTimer60Sec);
            cameraFragmentBinding.stepProgressView.setTotalProgress((int) ACTUAL_MAX_TIME);
            setProgressAndVideo();
        });

        cameraFragmentBinding.tvTimer15Sec.setOnClickListener(view13 -> {
            MAX_TIME = 15000;
            ACTUAL_MAX_TIME = 15000;
            cameraFragmentBinding.timerSeekBar.setMax((int) (ACTUAL_MAX_TIME / 100));
            cameraFragmentBinding.timerSeekBar.setProgress(0);
            previousEventTime = System.currentTimeMillis();
            viewAlreadyRecorded.setMax((int) (ACTUAL_MAX_TIME / 100));
            viewAlreadyRecorded.setProgress(0);
            setTextColor(cameraFragmentBinding.tvTimer15Sec, cameraFragmentBinding.tvTimer6Sec, cameraFragmentBinding.tvTimer60Sec);
            cameraFragmentBinding.stepProgressView.setTotalProgress((int) ACTUAL_MAX_TIME);
            setProgressAndVideo();
        });

        cameraFragmentBinding.tvTimer60Sec.setOnClickListener(view14 -> {
            MAX_TIME = 60000;
            ACTUAL_MAX_TIME = 60000;
            cameraFragmentBinding.timerSeekBar.setMax((int) (ACTUAL_MAX_TIME / 100));
            cameraFragmentBinding.timerSeekBar.setProgress(0);
            viewAlreadyRecorded.setMax((int) (ACTUAL_MAX_TIME / 100));
            viewAlreadyRecorded.setProgress(0);
            previousEventTime = System.currentTimeMillis();
            setTextColor(cameraFragmentBinding.tvTimer60Sec, cameraFragmentBinding.tvTimer6Sec, cameraFragmentBinding.tvTimer15Sec);
            cameraFragmentBinding.stepProgressView.setTotalProgress((int) ACTUAL_MAX_TIME);
            setProgressAndVideo();
        });

        cameraFragmentBinding.tvTimer3Sec.setOnClickListener(view15 -> {
            COUNT_DOWN_TIME = 3000;
        });

        cameraFragmentBinding.tvTimer10Sec.setOnClickListener(view16 -> {
            COUNT_DOWN_TIME = 10000;
        });

        cameraFragmentBinding.tvSpeedPoint3X.setOnClickListener(view17 -> {
            currentVideoSpeed = 0.3f;
            setTextColor(cameraFragmentBinding.tvSpeedPoint3X, cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeed1X,
                    cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeed3X);
        });

        cameraFragmentBinding.tvSpeedPoint5X.setOnClickListener(view18 -> {
            currentVideoSpeed = 0.5f;
            setTextColor(cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeedPoint3X,
                    cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeed3X);
        });

        cameraFragmentBinding.tvSpeed1X.setOnClickListener(view19 -> {
            currentVideoSpeed = 1f;
            setTextColor(cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeedPoint3X,
                    cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeed3X);
        });

        cameraFragmentBinding.tvSpeed2X.setOnClickListener(view110 -> {
            currentVideoSpeed = 2f;
            setTextColor(cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeedPoint3X,
                    cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeed3X);
        });

        cameraFragmentBinding.tvSpeed3X.setOnClickListener(view111 -> {
            currentVideoSpeed = 3f;
            setTextColor(cameraFragmentBinding.tvSpeed3X, cameraFragmentBinding.tvSpeedPoint3X, cameraFragmentBinding.tvSpeedPoint5X,
                    cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeed2X);
        });

//        llProfileLayout.setOnClickListener(v -> {
//            if (context.videoOptions == CameraNewActivity.VideoOptions.RECORD_FOR_OTHER) {
//                if (context.contactModelForOther != null) {
//                    showProfileDialog(context.contactModelForOther);
//                }
//            } else if (context.videoOptions == CameraNewActivity.VideoOptions.REPLY_REACTION) {
//                if (context.from.equalsIgnoreCase(Constants.FROM_REACTION)) {
//                    ContactsModel contactsModel = Utility.convertDiscoverIntoContact(context.discoverVO);
//                    showProfileDialog(contactsModel);
//                } else {
//                    if (context.convType == VideoConvType.DIRECT.getValue() || context.convType == VideoConvType.REACTION.getValue()) {
//                        MembersModel membersModel = (MembersModel) v.getTag();
//                        ContactsModel contactsModel = Utility.convertMemberIntoContactForOther(membersModel);
//                        showProfileDialog(contactsModel);
//                    } else {
//                        GroupModel groupModel = (GroupModel) v.getTag();
//                        showGroupInfoDialog(groupModel);
//                    }
//                }
//            } else if (context.videoOptions == CameraNewActivity.VideoOptions.COMMENT) {
//                MembersModel membersModel = (MembersModel) v.getTag();
//                ContactsModel contactsModel = Utility.convertMemberIntoContactForOther(membersModel);
//                showProfileDialog(contactsModel);
//            } else {
//                if (context.selectedContacts.size() == 1) {
//                    if (context.selectedContacts.get(0).getGenuin() != null) {
//                        showProfileDialog(context.selectedContacts.get(0));
//                    }
//                } else {
//                    if (clProfileList.getVisibility() == View.VISIBLE) {
//                        collapse(clProfileList);
//                    } else {
//                        rlUserImage.setVisibility(View.GONE);
//                        rlUserImage1.setVisibility(View.GONE);
//                        llSendToLayout.setVisibility(View.GONE);
//                        llQuestionSelect.setVisibility(View.GONE);
//                        cameraFragmentBinding.tvRecordTimer.setVisibility(View.GONE);
//                        rvProfileList.scrollToPosition(0);
//                        expand(clProfileList);
//                    }
//                }
//            }
//        });

        cameraFragmentBinding.llAddEditQuestion.setOnClickListener(this);
        cameraFragmentBinding.customQuestionView.ivBackNew.setOnClickListener(this);
        cameraFragmentBinding.customQuestionView.tvDone.setOnClickListener(this);

        cameraFragmentBinding.questionView.questionViewBindingCustom.edtAddQuestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Utility.printErrorLog("Question: ~~~~ beforeTextChanged: ");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Utility.printErrorLog("Question: ~~~~ onTextChanged: ");
                if (before == 0 && count == 1 && s.charAt(start) == '\n') {
                    cameraFragmentBinding.questionView.questionViewBindingCustom.edtAddQuestion.getText().replace(start, start + 1, ""); //remove the <enter>
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Utility.printErrorLog("Question: ~~~~ afterTextChanged");
                setDoneButtonEnableDisable(!TextUtils.isEmpty( cameraFragmentBinding.questionView.questionViewBindingCustom.edtAddQuestion.getText().toString().trim()));
                if (s == null || s.length() == 0) {
                    cameraFragmentBinding.customQuestionView.customAddQuestionView.setCharCount(0);
                    return;
                }

                if (s.length() >=cameraFragmentBinding.customQuestionView. customAddQuestionView.getMaxChar())
                    s.delete(cameraFragmentBinding.customQuestionView.customAddQuestionView.getMaxChar(), s.length());

                cameraFragmentBinding.customQuestionView.customAddQuestionView.setCharCount(s.length());
            }
        });
    }

    private void setDoneButtonEnableDisable(boolean isEnable) {
        if (isEnable && isCustomQuestionChanged()) {
            cameraFragmentBinding.customQuestionView.tvDone.setEnabled(true);
            cameraFragmentBinding.customQuestionView.tvDone.setTextColor(context.getColor(R.color.colorWhite));
        } else {
            cameraFragmentBinding.customQuestionView.tvDone.setEnabled(false);
            cameraFragmentBinding.customQuestionView.tvDone.setTextColor(context.getColor(R.color.white_opacity40));
        }
    }

    private boolean isCustomQuestionChanged() {
        return !oldQuestionText.equals( cameraFragmentBinding.questionView.questionViewBindingCustom.edtAddQuestion.getText().toString().trim());
    }

    private void selectQuestion() {
        if (isLoopQnA) {
            if (QuestionViewModel.getInstance().loopQuestionsArr.size() == 0) {
                return;
            }
        } else {
            if (QuestionViewModel.getInstance().masterQuestionsArr.size() == 0) {
                return;
            }
        }
        questionNormalFromBlur();
        if (isLoopQnA) {
            context.selectedQuestion = loopSelectedQuestion;
        } else {
            context.selectedQuestion = QuestionViewModel.getInstance().masterQuestionsArr.get(0);
        }
        cameraFragmentBinding.ivQuestion.setImageResource(R.drawable.ic_question_box);
        cameraFragmentBinding.ivQuestion.setImageTintList(null);
        cameraFragmentBinding.ivQuestion.clearColorFilter();
        cameraFragmentBinding.tvLabelQuestion.setTextColor(context.getResources().getColor(R.color.colorWhite, null));
        cameraFragmentBinding.llQuestionSelect.setVisibility(View.GONE);
        showCameraQuestionView();
    }

    private void rotateArrow() {
        cameraFragmentBinding.ivUpDown.setRotation(cameraFragmentBinding.ivUpDown.getRotation() + 180);
    }

//    public void expand(@NotNull final View v) {
//        isExpanded = true;
//        rotateArrow();
//        int dp = context.selectedContacts.size() * 38 - 8;
//        final int targetHeight = (int) Utility.dpToPx(Math.min(dp, 182),
//                context);
//        v.clearAnimation();
//        v.getLayoutParams().height = 1;
//        v.setVisibility(View.VISIBLE);
//        Animation a = new Animation() {
//            @Override
//            protected void applyTransformation(float interpolatedTime, Transformation t) {
//                v.getLayoutParams().height = interpolatedTime == 1
//                        ? ConstraintLayout.LayoutParams.WRAP_CONTENT
//                        : (int) (targetHeight * interpolatedTime);
//                v.requestLayout();
//            }
//
//            @Override
//            public boolean willChangeBounds() {
//                return true;
//            }
//        };
//
//        a.setDuration((int) (1.5 * (targetHeight / v.getContext().getResources().getDisplayMetrics().density)));
//        a.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                llAddPlusContact.setVisibility(View.VISIBLE);
//                if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
//                    tvAddParticipants.setText(context.getResources().getString(R.string.add_co_hosts));
//                } else {
//                    tvAddParticipants.setText(context.getResources().getString(R.string.add_people));
//                }
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        v.startAnimation(a);
//    }

    public void collapse(@NotNull final View v) {
        isExpanded = false;
        rotateArrow();
        final int initialHeight = v.getMeasuredHeight();
        v.clearAnimation();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
//                    if (context.selectedContacts.size() > 1) {
//                        rlUserImage1.setVisibility(View.VISIBLE);
//                    }
                    cameraFragmentBinding.rlUserImage.setVisibility(View.VISIBLE);
                    if (context.videoList.size() == 0) {
                        cameraFragmentBinding.llSendToLayout.setVisibility(View.VISIBLE);
                        if (context.selectedQuestion == null) {
                            cameraFragmentBinding.llQuestionSelect.setVisibility(View.VISIBLE);
                        }
                    } else if (!isRecording) {
                        cameraFragmentBinding.tvRecordTimer.setVisibility(View.VISIBLE);
                    }
                    cameraFragmentBinding.llAddPlusContact.setVisibility(View.GONE);

                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setDuration((int) (1.5 * (initialHeight / v.getContext().getResources().getDisplayMetrics().density)));
        v.startAnimation(a);
    }

    public void restoreDefaultProfileList() {
        isExpanded = false;
//        if (context.selectedContacts.size() > 1) {
//            rlUserImage1.setVisibility(View.VISIBLE);
//        }
        cameraFragmentBinding.rlUserImage.setVisibility(View.VISIBLE);
        if (context.videoList.size() == 0) {
            cameraFragmentBinding.llSendToLayout.setVisibility(View.VISIBLE);
        }
        cameraFragmentBinding.ivUpDown.setRotation(270f);
        cameraFragmentBinding.llAddPlusContact.setVisibility(View.GONE);
        //clProfileList.setVisibility(View.GONE);
    }

//    private void setContactsList() {
//        if (context.videoOptions != CameraNewActivity.VideoOptions.ROUND_TABLE) {
//            if (context.selectedContacts.size() > 1) {
//                context.videoOptions = CameraNewActivity.VideoOptions.GROUP;
//            } else {
//                context.videoOptions = CameraNewActivity.VideoOptions.DIRECT;
//            }
//        } else {
//            if (context.privacyOptionsTypeRT == PrivacyOptionsType.EVERYONE) {
//                ivLock.setImageResource(R.drawable.ic_globe);
//            } else {
//                ivLock.setImageResource(R.drawable.ic_icon_link);
//            }
//        }
//        context.privateVideoSelection = CameraNewActivity.PrivateVideoSelection.CONTACT_LIST;
//        enableViews();
//        setSendToStr();
//    }

    private void showTopicDialog() {
        if (!context.isRequiredPermissionsGranted()) {
            return;
        }
        TopicModel topicModel1 = new TopicModel();
        topicModel1.setName(context.getResources().getString(R.string.topic1));
        topicModel1.setIcon(R.drawable.ic_topic1_small);
        topicModel1.setSticker(R.drawable.ic_topic1);
        topicModel1.setStickerSelected(R.drawable.ic_topic1_selected);
        topicModel1.setSelected(context.selectedTopic != null && context.selectedTopic.getName().equalsIgnoreCase(context.getResources().getString(R.string.topic1)));

        TopicModel topicModel2 = new TopicModel();
        topicModel2.setName(context.getResources().getString(R.string.topic2));
        topicModel2.setIcon(R.drawable.ic_topic2_small);
        topicModel2.setSticker(R.drawable.ic_topic2);
        topicModel2.setStickerSelected(R.drawable.ic_topic2_selected);
        topicModel2.setSelected(context.selectedTopic != null && context.selectedTopic.getName().equalsIgnoreCase(context.getResources().getString(R.string.topic2)));

        TopicModel topicModel3 = new TopicModel();
        topicModel3.setName(context.getResources().getString(R.string.topic3));
        topicModel3.setIcon(R.drawable.ic_topic3_small);
        topicModel3.setSticker(R.drawable.ic_topic3);
        topicModel3.setStickerSelected(R.drawable.ic_topic3_selected);
        topicModel3.setSelected(context.selectedTopic != null && context.selectedTopic.getName().equalsIgnoreCase(context.getResources().getString(R.string.topic3)));

        TopicModel topicModel4 = new TopicModel();
        topicModel4.setName(context.getResources().getString(R.string.topic4));
        topicModel4.setIcon(R.drawable.ic_topic4_small);
        topicModel4.setSticker(R.drawable.ic_topic4);
        topicModel4.setStickerSelected(R.drawable.ic_topic4_selected);
        topicModel4.setSelected(context.selectedTopic != null && context.selectedTopic.getName().equalsIgnoreCase(context.getResources().getString(R.string.topic4)));

        topicList.clear();
        topicList.add(topicModel1);
        topicList.add(topicModel2);
        topicList.add(topicModel3);
        topicList.add(topicModel4);

        Dialog dialogTopic = new Dialog(context);
        dialogTopic.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogTopic.setContentView(R.layout.topic_dialog);
        dialogTopic.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogTopic.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView dialogTitle = dialogTopic.findViewById(R.id.dialogTitle);
        ImageView ivWeAreHiring = dialogTopic.findViewById(R.id.ivWeAreHiring);
        ImageView ivOpenToWork = dialogTopic.findViewById(R.id.ivOpenToWork);
        ImageView ivReadyToInvest = dialogTopic.findViewById(R.id.ivReadyToInvest);
        ImageView ivSeekingInvestment = dialogTopic.findViewById(R.id.ivSeekingInvestment);
        MaterialButton btnTopic = dialogTopic.findViewById(R.id.btnTopic);

        if (context.selectedTopic == null) {
            dialogTitle.setText(context.getResources().getString(R.string.choose_topic));
            btnTopic.setText(context.getResources().getString(R.string.skip));
        } else {
            dialogTitle.setText(context.getResources().getString(R.string.change_topic));
            btnTopic.setText(context.getResources().getString(R.string.remove_topic));
        }

        if (topicList.get(0).isSelected()) {
            ivWeAreHiring.setImageResource(R.drawable.ic_topic1_selected);
        } else if (topicList.get(1).isSelected()) {
            ivOpenToWork.setImageResource(R.drawable.ic_topic2_selected);
        } else if (topicList.get(2).isSelected()) {
            ivReadyToInvest.setImageResource(R.drawable.ic_topic3_selected);
        } else if (topicList.get(3).isSelected()) {
            ivSeekingInvestment.setImageResource(R.drawable.ic_topic4_selected);
        }

        ivSeekingInvestment.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ivReadyToInvest.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout llTopicMain = dialogTopic.findViewById(R.id.llTopicMain);
        float widthDiff = 0; // 58 = 24 * 2[left/right padding] + 10 space between 2 image
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            widthDiff = Utility.getScreenWidthHeight(context)[0] - ivSeekingInvestment.getMeasuredWidth() - ivReadyToInvest.getMeasuredWidth() - Utility.dpToPx(58, context);
        }
        float seventy2Dp = Utility.dpToPx(72, context); // 72 = 36 * 2[left right margin for dialog]
        int margin;
        if (widthDiff < seventy2Dp) {
            if (widthDiff < 0) {
                widthDiff = 0;
            }
            margin = (int) (widthDiff / 2);
        } else {
            margin = (int) (seventy2Dp / 2);
        }

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) llTopicMain.getLayoutParams();
        layoutParams.setMargins(margin, 0, margin, 0);

        ivWeAreHiring.setOnClickListener(v -> {
            selectTopic(0);
            dialogTopic.dismiss();
        });

        ivOpenToWork.setOnClickListener(v -> {
            selectTopic(1);
            dialogTopic.dismiss();
        });

        ivReadyToInvest.setOnClickListener(v -> {
            selectTopic(2);
            dialogTopic.dismiss();
        });

        ivSeekingInvestment.setOnClickListener(v -> {
            selectTopic(3);
            dialogTopic.dismiss();
        });

        btnTopic.setOnClickListener(v -> {
            if (context.selectedTopic != null) {
                deleteTopic();
            }
            dialogTopic.dismiss();
        });

        dialogTopic.show();
    }

    private void selectTopic(int pos) {
        topicList.get(pos).setSelected(true);
        if (context.selectedTopic == null) {
        } else {
            String previousTopic = context.selectedTopic.getName();
        }
        context.selectedTopic = topicList.get(pos);
        cameraFragmentBinding.ivTopic.setImageResource(context.selectedTopic.getIcon());
        cameraFragmentBinding.tvLabelTopic.setTextColor(context.getResources().getColor(R.color.colorWhite, null));
    }

//    private void setDirectProfileList() {
//        if (context.selectedContacts.size() > 0) {
//            // remove video size condition if need to display profile view after recording
//            if (context.videoList.size() == 0) {
//                //llProfileLayout.setVisibility(View.VISIBLE);
//            }
//            rlUserImage.setVisibility(View.VISIBLE);
//            if (context.selectedContacts.size() >= 1) {
//                rlUserImage1.setVisibility(View.GONE);
//                ivUpDown.setVisibility(View.GONE);
//                ContactsModel contactList = context.selectedContacts.get(0);
//                if (contactList.getGenuin() != null) {
//                    if (contactList.getGenuin().getIsAvatar()) {
//                        int res = context.getResources().getIdentifier(contactList.getGenuin().getProfileImage(),
//                                "raw", context.getPackageName());
//                        Drawable color = new ColorDrawable(context.getResources().getColor(LottieAnimModel.getMapData().get(res), null));
//                        ivUserProfile.setImageDrawable(color);
//                        animationViewProfile.setVisibility(View.VISIBLE);
//                        animationViewProfile.setAnimation(res);
//                        animationViewProfile.playAnimation();
//                    } else {
//                        animationViewProfile.setVisibility(View.GONE);
//                        if (TextUtils.isEmpty(contactList.getGenuin().getProfileImageM())) {
//                            Glide.with(context).load(contactList.getGenuin().getProfileImage()).apply(RequestOptions.circleCropTransform().circleCrop()).into(ivUserProfile);
//                        } else {
//                            Glide.with(context).load(contactList.getGenuin().getProfileImageM()).apply(RequestOptions.circleCropTransform().circleCrop()).into(ivUserProfile);
//                        }
//                    }
//                } else {
//                    animationViewProfile.setVisibility(View.GONE);
//                    Glide.with(context).load(R.drawable.placeholder_contact).into(ivUserProfile);
//                }
//            }
//            if (context.selectedContacts.size() > 1) {
//                ivUpDown.setVisibility(View.VISIBLE);
//                rlUserImage1.setVisibility(View.VISIBLE);
//                ContactsModel contactList = context.selectedContacts.get(1);
//                if (contactList.getGenuin() != null) {
//                    if (contactList.getGenuin().getIsAvatar()) {
//                        int res = context.getResources().getIdentifier(contactList.getGenuin().getProfileImage(),
//                                "raw", context.getPackageName());
//                        Drawable color = new ColorDrawable(context.getResources().getColor(LottieAnimModel.getMapData().get(res), null));
//                        ivUserProfile1.setImageDrawable(color);
//                        animationViewProfile1.setVisibility(View.VISIBLE);
//                        animationViewProfile1.setAnimation(res);
//                        animationViewProfile1.playAnimation();
//                    } else {
//                        animationViewProfile1.setVisibility(View.GONE);
//                        if (TextUtils.isEmpty(contactList.getGenuin().getProfileImageM())) {
//                            Glide.with(context).load(contactList.getGenuin().getProfileImage()).apply(RequestOptions.circleCropTransform().circleCrop()).into(ivUserProfile1);
//                        } else {
//                            Glide.with(context).load(contactList.getGenuin().getProfileImageM()).apply(RequestOptions.circleCropTransform().circleCrop()).into(ivUserProfile1);
//                        }
//                    }
//                } else {
//                    animationViewProfile1.setVisibility(View.GONE);
//                    Glide.with(context).load(R.drawable.placeholder_contact).into(ivUserProfile1);
//                }
//                ProfileListAdapter profileListAdapter = new ProfileListAdapter(context, context.selectedContacts, this::showProfileDialog);
//                rvProfileList.setAdapter(profileListAdapter);
//            }
//        }
//    }

//    private void showProfileDialog(ContactsModel contact) {
//        profileBlurLayout.setVisibility(View.VISIBLE);
//        tvGroupInfoDialogHeader.setVisibility(View.GONE);
//        if (contact.getGenuin() != null) {
//            tvMemberUserName.setText(String.format("@%s", contact.getGenuin().getUserName()));
//            if (TextUtils.isEmpty(contact.getGenuin().getName())) {
//                if (TextUtils.isEmpty(contact.getFirstName())) {
//                    tvMemberName.setVisibility(View.GONE);
//                } else {
//                    tvMemberName.setVisibility(View.VISIBLE);
//                    tvMemberName.setText(contact.getFirstName());
//                }
//            } else {
//                tvMemberName.setVisibility(View.VISIBLE);
//                tvMemberName.setText(contact.getGenuin().getName());
//            }
//            if (TextUtils.isEmpty(contact.getGenuin().getBio())) {
//                tvMemberBio.setVisibility(View.GONE);
//            } else {
//                tvMemberBio.setVisibility(View.VISIBLE);
//                tvMemberBio.setText(contact.getGenuin().getBio());
//            }
//            if (contact.getGenuin().getIsAvatar()) {
//                int res = context.getResources().getIdentifier(contact.getGenuin().getProfileImage(),
//                        "raw", context.getPackageName());
//                Drawable color = new ColorDrawable(context.getResources().getColor(LottieAnimModel.getMapData().get(res), null));
//                ivContactPicture.setImageDrawable(color);
//                animationView.setVisibility(View.VISIBLE);
//                animationView.setAnimation(res);
//                animationView.playAnimation();
//            } else {
//                animationView.setVisibility(View.GONE);
//                if (TextUtils.isEmpty(contact.getGenuin().getProfileImageM())) {
//                    Glide.with(context).load(contact.getGenuin().getProfileImage()).apply(RequestOptions.circleCropTransform().circleCrop()).into(ivContactPicture);
//                } else {
//                    Glide.with(context).load(contact.getGenuin().getProfileImageM()).apply(RequestOptions.circleCropTransform().circleCrop()).into(ivContactPicture);
//                }
//            }
//        } else {
//            animationView.setVisibility(View.GONE);
//            tvMemberUserName.setText(contact.getFirstName());
//            Glide.with(context).load(R.drawable.placeholder_contact).into(ivContactPicture);
//        }
//    }

//    private void showGroupInfoDialog(GroupModel group) {
//        //profileBlurLayout.setVisibility(View.VISIBLE);
//        tvGroupInfoDialogHeader.setVisibility(View.VISIBLE);
//        if (context.convType == VideoConvType.ROUND_TABLE.getValue()) {
//            if (context.privacyOptionsTypeRT == PrivacyOptionsType.EVERYONE) {
//                tvGroupInfoDialogHeader.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_globe), null, null, null);
//            } else {
//                tvGroupInfoDialogHeader.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_icon_link), null, null, null);
//            }
//        } else {
//            tvGroupInfoDialogHeader.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_lock), null, null, null);
//        }
//        tvMemberUserName.setText(group.getName());
//        tvMemberName.setVisibility(View.GONE);
//        if (TextUtils.isEmpty(group.getDescription())) {
//            tvMemberBio.setVisibility(View.GONE);
//        } else {
//            tvMemberBio.setVisibility(View.VISIBLE);
//            tvMemberBio.setText(group.getDescription());
//        }
//        animationView.setVisibility(View.GONE);
//        if (TextUtils.isEmpty(group.getDp())) {
//            tvGroupInfoInitials.setVisibility(View.VISIBLE);
//            ivContactPicture.setImageResource(R.color.transparent);
//            ivContactPicture.setCircleBackgroundColor(Color.parseColor(group.getColorCode()));
//            setTextForDp(group.getName(), tvGroupInfoInitials);
//            tvGroupInfoInitials.setTextColor(Color.parseColor(group.getTextColorCode()));
//        } else {
//            tvGroupInfoInitials.setVisibility(View.GONE);
//            Utility.displayProfileImage(context, group.getDp(), ivContactPicture);
//        }
//    }

    private void showPublicVideoTutorial() {
        if (context.isRequiredPermissionsGranted()) {
            //boolean isTutorialShown = SharedPrefUtils.getBoolPreference(context, Constants.PREF_TUTORIAL_PUBLIC_VIDEO);
//            if (!isTutorialShown) {
//                tutorialPublicVideo.setVisibility(View.VISIBLE);
//                SharedPrefUtils.setBoolPreference(context, Constants.PREF_TUTORIAL_PUBLIC_VIDEO, true);
//            }
        }
    }

    private void selectReplyVideo() {
        context.replyOptions = CameraNewActivity.ReplyOptions.VIDEO;
        cameraFragmentBinding.tvMiddleText.setText(context.getResources().getString(R.string.reply_video));
        cameraFragmentBinding.rtlPermissionNew.setVisibility(View.GONE);
        setCameraVisibility();
        if (context.isRequiredPermissionsGranted()) {
            new Handler().postDelayed(this::resumeCamera, 200);
        } else {
//            boolean isCameraPerAsked = SharedPrefUtils.getBoolPreference(context, Constants.PREF_IS_CAMERA_PERMISSION_ASKED);
//            boolean isMicPerAsked = SharedPrefUtils.getBoolPreference(context, Constants.PREF_IS_MIC_PERMISSION_ASKED);
//            if (isCameraPerAsked && isMicPerAsked) {
//                boolean canRequestPermissions = context.canRequestRequiredPermissions();
//                if (canRequestPermissions) {
//                    requestPermissions();
//                } else {
//                    permissionEnableDisableViews(false);
//                }
//            } else {
//                requestPermissions();
//            }
        }
    }

//    private void selectReplyPhoto() {
//        context.replyOptions = CameraNewActivity.ReplyOptions.IMAGE;
//        tvMiddleText.setText(context.getResources().getString(R.string.reply_photo));
//        rtlPermissionNew.setVisibility(View.GONE);
//        setCameraVisibility();
//        if (context.isRequiredPermissionsGranted()) {
//            new Handler().postDelayed(this::resumeCamera, 200);
//        } else {
//            boolean isCameraPerAsked = SharedPrefUtils.getBoolPreference(context, Constants.PREF_IS_CAMERA_PERMISSION_ASKED);
//            if (isCameraPerAsked) {
//                boolean canRequestPermissions = context.canRequestRequiredPermissions();
//                if (canRequestPermissions) {
//                    requestPermissions();
//                } else {
//                    permissionEnableDisableViews(false);
//                }
//            } else {
//                requestPermissions();
//            }
//        }
//    }

    private void selectReplyAudio() {
        context.replyOptions = CameraNewActivity.ReplyOptions.AUDIO;
        cameraFragmentBinding.tvMiddleText.setText(context.getResources().getString(R.string.reply_audio));
        cameraFragmentBinding.rtlPermissionNew.setVisibility(View.GONE);
        setCameraVisibility();
        if (!context.isRequiredPermissionsGranted()) {
//            boolean isMicPerAsked = SharedPrefUtils.getBoolPreference(context, Constants.PREF_IS_MIC_PERMISSION_ASKED);
//            if (isMicPerAsked) {
//                boolean canRequestPermissions = context.canRequestRequiredPermissions();
//                if (canRequestPermissions) {
//                    requestPermissions();
//                } else {
//                    permissionEnableDisableViews(false);
//                }
//            } else {
//                requestPermissions();
//            }
        }
        new Handler().postDelayed(this::pauseCamera, 200);
    }

    private void selectReplyText() {
        context.replyOptions = CameraNewActivity.ReplyOptions.TEXT;
        cameraFragmentBinding.tvMiddleText.setText(context.getResources().getString(R.string.reply_text));
        cameraFragmentBinding.rtlPermissionNew.setVisibility(View.GONE);
        setCameraVisibility();
        new Handler().postDelayed(this::pauseCamera, 200);
    }

    private void selectPublicVideo() {
        enableViews();
        context.videoOptions = CameraNewActivity.VideoOptions.PUBLIC;
        cameraFragmentBinding.tvMiddleText.setText(context.getResources().getString(R.string.public_video));
        cameraFragmentBinding.llSendToLayout.setVisibility(View.GONE);
        //llProfileLayout.setVisibility(View.GONE);
        if (!context.isQnA) {
            showPublicVideoTutorial();
        }
    }

    private void selectDirectVideo(boolean isDelayNeeded) {
        //if (Utility.isLoggedIn(context)) {
            enableViews();
//            if (isContactPermissionAllowed()) {
//                checkForSendToDialog(true);
//            } else {
//                showSyncContactsDialog();
//            }
//        } else {
//            if (isDelayNeeded) {
//                new Handler(Looper.getMainLooper()).postDelayed(this::goToLoginActivity, 300);
//            } else {
//                goToLoginActivity();
//            }
//        }
    }

//    private void inviteCoHosts() {
//        if (isContactPermissionAllowed()) {
//            goToContactsSync(false);
//        } else {
//            showSyncContactsDialog();
//        }
//    }

    private void selectRoundTableVideo(boolean isDelayNeeded) {
        //if (Utility.isLoggedIn(context)) {
            enableViews();
//        } else {
//            if (isDelayNeeded) {
//                new Handler(Looper.getMainLooper()).postDelayed(this::goToLoginActivity, 300);
//            } else {
//                goToLoginActivity();
//            }
//        }
    }

//    private void goToLoginActivity() {
//        Constants.WITHOUT_LOGIN_FROM = Constants.FROM_CREATE_POST;
//        Intent i = new Intent(context, LoginActivity.class);
//        loginActivityResultLauncher.launch(i);
//        context.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
//    }

//    private void checkForSendToDialog(boolean shouldAddContactOpen) {
//        if ((context.privateVideoCreateFrom == CameraNewActivity.PrivateVideoCreateFrom.NONE || context.privateVideoCreateFrom == CameraNewActivity.PrivateVideoCreateFrom.FROM_NOTIFICATIONS) && context.videoOptions != CameraNewActivity.VideoOptions.ROUND_TABLE) {
//            String userId = SharedPrefUtils.getStringPreference(context, Constants.PREF_USER);
//            List<MembersModel> membersList = Utility.getDBHelper().getRecentConversationMembers(userId);
//            if (membersList.size() > 0) {
//                if (membersList.size() > 10) {
//                    showSendToDialog(membersList.subList(0, 10));
//                } else {
//                    showSendToDialog(membersList);
//                }
//            } else {
//                if (shouldAddContactOpen) {
//                    showAddContactsDialog();
//                } else {
//                    goToContactsSync(false);
//                }
//            }
//        } else {
//            if (shouldAddContactOpen) {
//                // Remove everyone/unlisted selection for Loop
////                showAddContactsDialog();
//                ivAddContact.setVisibility(View.VISIBLE);
//            } else {
//                goToContactsSync(false);
//            }
//        }
//    }

//    private void showSendToDialog(List<MembersModel> membersList) {
//        mSendToDialog = new Dialog(context);
//        mSendToDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        mSendToDialog.setContentView(R.layout.dialog_send_to);
//        mSendToDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
////        if (membersList.size() == 1) {
////            mSendToDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
////        } else {
////            mSendToDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
////        }
//        mSendToDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        mSendToDialog.show();
//        mSendToDialog.setOnCancelListener(dialogInterface -> {
//            if (context.selectedContacts.size() == 0) {
//                disableViews();
//                sendSegmentLogs(Constants.DM_RECENT_MODAL_CLOSED, new Properties());
//            }
//        });
//        RecyclerView recyclerSend = mSendToDialog.findViewById(R.id.recyclerSend);
////        int spanCount = (membersList.size() == 1) ? 2 : 3;
//        int spanCount = 3;
//        recyclerSend.setLayoutManager(new GridLayoutManager(context, spanCount));
//        int paddingPixel = (int) Utility.dpToPx(40, context);
//        int[] arr = Utility.getScreenWidthHeight(context);
//        float totalPixel = (arr[0] - Utility.dpToPx(280, context)) / 3;
//        paddingPixel = (int) Math.min(totalPixel, paddingPixel);
//        recyclerSend.addItemDecoration(new GridSpacingItemDecoration(spanCount, paddingPixel, false));
//        MembersModel members1 = new MembersModel();
//        members1.setNickname("Others");
//        members1.setUserId("-1");
//        membersList.add(members1);
//
//        MembersModel membersNewGroup = new MembersModel();
//        membersNewGroup.setNickname(context.getResources().getString(R.string.new_group));
//        membersNewGroup.setUserId("-2");
//        membersList.add(0, membersNewGroup);
//
//        SendToAdapter sendToAdapter = new SendToAdapter(context, membersList, member -> {
//            mSendToDialog.dismiss();
//            if (member.getUserId().equalsIgnoreCase("-1")) {
//                goToContactsSync(false);
//                sendSegmentLogs(Constants.OTHERS_CLICKED, new Properties());
//            } else if (member.getUserId().equalsIgnoreCase("-2")) {
//                goToContactsSync(true);
//                sendSegmentLogs(Constants.NEW_GROUP_CLICKED, new Properties());
//            } else {
//                enableViews();
//                context.privateVideoSelection = CameraNewActivity.PrivateVideoSelection.DIALOG;
//                convertMemberIntoContact(member);
//                Properties properties = new Properties();
//                properties.put(Constants.KEY_INTIATED_WITH, member.getNickname());
//                sendSegmentLogs(Constants.DM_INITIATED, properties);
//            }
//        });
//        recyclerSend.setAdapter(sendToAdapter);
//    }

//    private void convertMemberIntoContact(MembersModel member) {
//        context.selectedContacts.clear();
//        ContactsModel contact = new ContactsModel();
//        ContactsModel.Genuin genuin = new ContactsModel.Genuin();
//        genuin.setName(member.getName());
//        genuin.setIsAvatar(member.isAvatar());
//        genuin.setProfileImage(member.getProfileImage());
//        genuin.setProfileImageL(member.getProfileImageL());
//        genuin.setProfileImageM(member.getProfileImageM());
//        genuin.setProfileImageS(member.getProfileImageS());
//        genuin.setUserName(member.getNickname());
//        genuin.setUuid(member.getUserId());
//        genuin.setBio(member.getBio());
//        contact.setPhoneNumber(member.getPhone());
//        contact.setFirstName(member.getName());
//        contact.setGenuin(genuin);
//        context.selectedContacts.add(contact);
//        setSendToStr();
//    }
//
//    private void convertMemberIntoContactForOther(MembersModel member) {
//        context.contactModelForOther = Utility.convertMemberIntoContactForOther(member);
//        setRecordForOtherDesc(member.getNickname());
//    }

//    private void setSendToStr() {
//        if (context.videoOptions != CameraNewActivity.VideoOptions.ROUND_TABLE) {
//            String sendToString = context.getSendToStr();
//            tvSendToPrefix.setText(context.getResources().getString(R.string.message));
//            tvSendToStr.setText(sendToString);
//            if (context.videoList.size() == 0) {
//                llSendToLayout.setVisibility(View.VISIBLE);
//            } else {
//                llSendToLayout.setVisibility(View.GONE);
//            }
//        }
//        //setDirectProfileList();
//    }
//
//    private void showSyncContactsDialog() {
//        SharedPrefUtils.setBoolPreference(context, Constants.PREF_IS_SHOWN_LOOP_CONTACT, true);
//        mSyncContactsDialog = new Dialog(context);
//        mSyncContactsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        mSyncContactsDialog.setContentView(R.layout.alert_send_contacts);
//        mSyncContactsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        mSyncContactsDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        mSyncContactsDialog.show();
//
//        CustomTextView tvTitle = mSyncContactsDialog.findViewById(R.id.dialog_title);
//        CustomTextView tvMsg = mSyncContactsDialog.findViewById(R.id.dialog_message);
//        CustomTextView btnSyncContacts = mSyncContactsDialog.findViewById(R.id.dialog_btn_sync_contact);
//        CustomTextView btnNoThanks = mSyncContactsDialog.findViewById(R.id.dialog_btn_no_thanks);
//
//        tvTitle.setText(context.getResources().getString(R.string.sync_dialog_title));
//        tvMsg.setText(context.getResources().getString(R.string.sync_dialog_message));
//        btnSyncContacts.setText(context.getResources().getString(R.string.sync_dialog_yes));
//        if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
//            btnNoThanks.setText(context.getResources().getString(R.string.skip));
//        } else {
//            btnNoThanks.setText(context.getResources().getString(R.string.sync_dialog_no));
//        }
//
//        btnSyncContacts.setOnClickListener(v -> {
//            mSyncContactsDialog.dismiss();
//            requestContactPermission();
//        });
//
//        btnNoThanks.setOnClickListener(v -> {
//            mSyncContactsDialog.dismiss();
//            disableViews();
//        });
//
//        mSyncContactsDialog.setOnCancelListener(dialogInterface -> disableViews());
//    }
//
//    private void showAddContactsDialog() {
////        boolean isTutorialShown = SharedPrefUtils.getBoolPreference(context, Constants.PREF_TUTORIAL_RT);
////        if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE && !isTutorialShown) {
////            SharedPrefUtils.setBoolPreference(context, Constants.PREF_TUTORIAL_RT, true);
////            tutorialRoundTable.setVisibility(View.VISIBLE);
////        } else {
//        mAddContactDialog = new Dialog(context);
//        mAddContactDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        mAddContactDialog.setContentView(R.layout.alert_send_contacts);
//        mAddContactDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        mAddContactDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        mAddContactDialog.show();
//
//        CustomTextView tvTitle = mAddContactDialog.findViewById(R.id.dialog_title);
//        CustomTextView tvMsg = mAddContactDialog.findViewById(R.id.dialog_message);
//        CustomTextView btnAddContacts = mAddContactDialog.findViewById(R.id.dialog_btn_sync_contact);
//        CustomTextView btnCancel = mAddContactDialog.findViewById(R.id.dialog_btn_no_thanks);
//        LinearLayout llRTPrivacyOptions = mAddContactDialog.findViewById(R.id.llRTPrivacyOptions);
//        CardView cardUnlisted = mAddContactDialog.findViewById(R.id.cardUnlisted);
//        CardView cardEveryone = mAddContactDialog.findViewById(R.id.cardEveryone);
//        ImageView ivUnlisted = mAddContactDialog.findViewById(R.id.ivUnlisted);
//        ImageView ivEveryOne = mAddContactDialog.findViewById(R.id.ivEveryOne);
//        TextView tvUnlisted = mAddContactDialog.findViewById(R.id.tvUnlisted);
//        TextView tvEveryOne = mAddContactDialog.findViewById(R.id.tvEveryOne);
//
//        if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
//            llRTPrivacyOptions.setVisibility(View.VISIBLE);
//            tvTitle.setText(context.getResources().getString(R.string.roundtable_header));
//            if (context.privacyOptionsTypeRT == PrivacyOptionsType.EVERYONE) {
//                tvMsg.setText(context.getResources().getString(R.string.roundtable_desc_public));
//                cardEveryone.setCardBackgroundColor(context.getResources().getColor(R.color.black_111111, null));
//                cardUnlisted.setCardBackgroundColor(context.getResources().getColor(R.color.color_E7E7E7, null));
//                ivEveryOne.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
//                ivUnlisted.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.black_111111, null)));
//                tvEveryOne.setTextColor(context.getResources().getColor(R.color.colorWhite, null));
//                tvUnlisted.setTextColor(context.getResources().getColor(R.color.black_111111, null));
//                tvMsg.setText(context.getResources().getString(R.string.roundtable_desc_public));
//            } else {
//                tvMsg.setText(context.getResources().getString(R.string.roundtable_desc_unlisted));
//                cardUnlisted.setCardBackgroundColor(context.getResources().getColor(R.color.black_111111, null));
//                cardEveryone.setCardBackgroundColor(context.getResources().getColor(R.color.color_E7E7E7, null));
//                ivUnlisted.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
//                ivEveryOne.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.black_111111, null)));
//                tvUnlisted.setTextColor(context.getResources().getColor(R.color.colorWhite, null));
//                tvEveryOne.setTextColor(context.getResources().getColor(R.color.black_111111, null));
//                tvMsg.setText(context.getResources().getString(R.string.roundtable_desc_unlisted));
//            }
//
//            cardUnlisted.setOnClickListener(view -> {
//                context.privacyOptionsTypeRT = PrivacyOptionsType.UNLISTED;
//                setPrivacyLayout();
//                cardUnlisted.setCardBackgroundColor(context.getResources().getColor(R.color.black_111111, null));
//                cardEveryone.setCardBackgroundColor(context.getResources().getColor(R.color.color_E7E7E7, null));
//                ivUnlisted.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
//                ivEveryOne.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.black_111111, null)));
//                tvUnlisted.setTextColor(context.getResources().getColor(R.color.colorWhite, null));
//                tvEveryOne.setTextColor(context.getResources().getColor(R.color.black_111111, null));
//                tvMsg.setText(context.getResources().getString(R.string.roundtable_desc_unlisted));
//                sendSegmentLogs(Constants.LOOP_DIALOG_UNLISTED_CLICKED, new Properties());
//            });
//
//            cardEveryone.setOnClickListener(view -> {
//                context.privacyOptionsTypeRT = PrivacyOptionsType.EVERYONE;
//                setPrivacyLayout();
//                cardEveryone.setCardBackgroundColor(context.getResources().getColor(R.color.black_111111, null));
//                cardUnlisted.setCardBackgroundColor(context.getResources().getColor(R.color.color_E7E7E7, null));
//                ivEveryOne.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
//                ivUnlisted.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.black_111111, null)));
//                tvEveryOne.setTextColor(context.getResources().getColor(R.color.colorWhite, null));
//                tvUnlisted.setTextColor(context.getResources().getColor(R.color.black_111111, null));
//                tvMsg.setText(context.getResources().getString(R.string.roundtable_desc_public));
//                sendSegmentLogs(Constants.LOOP_DIALOG_EVERYONE_CLICKED, new Properties());
//            });
//
//        } else {
//            llRTPrivacyOptions.setVisibility(View.GONE);
//            setLeftLockDrawable(tvTitle);
//            tvTitle.setText(context.getResources().getString(R.string.send_direct_message));
//            tvMsg.setText(context.getResources().getString(R.string.add_contact_dialog_message));
//        }
//        btnAddContacts.setText(context.getResources().getString(R.string.add_contact_dialog_yes));
//        if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
//            btnCancel.setText(context.getResources().getString(R.string.skip));
//        } else {
//            btnCancel.setText(context.getResources().getString(R.string.add_contact_dialog_no));
//        }
//
//        btnAddContacts.setOnClickListener(v -> {
//            mAddContactDialog.dismiss();
//            goToContactsSync(false);
//            Properties properties = new Properties();
//            properties.put(Constants.KEY_EVENT_RECORD_SCREEN, Constants.SCREEN_CAMERA);
//            properties.put(Constants.KEY_EVENT_TARGET_SCREEN, Constants.SCREEN_CONTACT);
//            if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
//                GenuInApplication.getInstance().sendEventLogs(Constants.RT_PARTICIPATE_ADD_INITIATED, properties);
//            } else {
//                GenuInApplication.getInstance().sendEventLogs(Constants.DM_SELECT_CONTACT_INITIATED, properties);
//            }
//        });
//
//        btnCancel.setOnClickListener(v -> {
//            mAddContactDialog.dismiss();
//            disableViews();
//            if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
//                sendSegmentLogs(Constants.RT_PARTICIPATE_ADD_SKIPPED, new Properties());
//            } else {
//                sendSegmentLogs(Constants.DM_SELECT_CONTACT_DIALOG_CLOSED, new Properties());
//            }
//        });
//
//        mAddContactDialog.setOnCancelListener(dialogInterface -> {
//            disableViews();
//            if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
//                sendSegmentLogs(Constants.RT_PARTICIPATE_ADD_SKIPPED, new Properties());
//            } else {
//                sendSegmentLogs(Constants.DM_SELECT_CONTACT_DIALOG_CLOSED, new Properties());
//            }
//        });
////        }
//    }

    private void setLeftLockDrawable(TextView textView) {
        textView.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_lock), null, null, null);
    }

    private void setTimerDetailsText(int progress, SeekBar seekBar) {
        if (progress < progressRecorded) {
            cameraFragmentBinding.timerSeekBar.setProgress(progressRecorded);
            progress = progressRecorded;
        }
        int val = (progress * (seekBar.getWidth())) / seekBar.getMax();
        timerDuration = (progress - progressRecorded) * 100L;
        cameraFragmentBinding.seekProgressValue.setText(String.format("%ss", Double.parseDouble(String.format(Locale.ENGLISH, "%.1f", (double) ((progress - progressRecorded) / 10f)))));
        if (ACTUAL_MAX_TIME == 6000) {
            int x = (int)  cameraFragmentBinding.rlSeekBar.getX() + val - (int) (progress * 0.7) - seekTextWidth - 75;
            Utility.showLog("Vishal_Tag",  cameraFragmentBinding.rlSeekBar.getX() + " Progress : " + progress + ", X : " + val);
            cameraFragmentBinding.seekProgressValue.setX(x);
        } else if (ACTUAL_MAX_TIME == 15000) {
            if ((progress - progressRecorded) >= 100) {
                cameraFragmentBinding.seekProgressValue.setX( cameraFragmentBinding.rlSeekBar.getLeft() + val - seekTextWidth - (int) (progress * 0.25) - 100);
            } else {
                cameraFragmentBinding.seekProgressValue.setX( cameraFragmentBinding.rlSeekBar.getLeft() + val - seekTextWidth - (int) (progress * 0.25) - 80);
            }
        } else if (ACTUAL_MAX_TIME == 60000) {
            if ((progress - progressRecorded) >= 100) {
                cameraFragmentBinding.seekProgressValue.setX( cameraFragmentBinding.rlSeekBar.getLeft() + val - seekTextWidth - (int) (progress * 0.06) - 100);
            } else {
                cameraFragmentBinding.seekProgressValue.setX( cameraFragmentBinding.rlSeekBar.getLeft() + val - seekTextWidth - (int) (progress * 0.06) - 80);
            }
        }
        cameraFragmentBinding.seekProgressValue.setVisibility(View.VISIBLE);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!context.isRetake) {
            setNormal();
            if (!context.isTextReply()) {
                if (context.videoList.size() == 0) {
                    cameraFragmentBinding.llProgressBar.setVisibility(View.GONE);
                    cameraFragmentBinding.llBottomLayoutCamera.setVisibility(View.VISIBLE);
//                llTimeSelection.setVisibility(View.VISIBLE);
                } else {
                    manageViews(View.VISIBLE);
                }
                setProgressAndVideo();
            }
        } else {
            cameraFragmentBinding.llBottomLayoutCamera.setVisibility(View.VISIBLE);
            cameraFragmentBinding.llBottomMiddle.setVisibility(View.VISIBLE);
            if (!context.isReplyReactionWithoutVideo()) {
                cameraFragmentBinding.imgSwitchFlash.setVisibility(View.VISIBLE);
            }
            cameraFragmentBinding.frameCameraSwitch.setVisibility(View.VISIBLE);
            cameraFragmentBinding.frmCameraRecord.setEnabled(true);
            if (isRetakeFromCamera) {
                CURRENT_PROGRESS = 0;
                cameraFragmentBinding.stepProgressView.setTotalProgress((int) retakeDuration);
                cameraFragmentBinding.stepProgressView.setCurrentProgress(0);
                cameraFragmentBinding.stepProgressView.getMarkers().clear();
                cameraFragmentBinding.tvRecordTimer.setText("");
                manageViews(View.GONE);
            } else {
//                llLength.setVisibility(View.GONE);
//                llTimeSelection.setVisibility(View.INVISIBLE);
                cameraFragmentBinding.llProgressBar.setVisibility(View.VISIBLE);
                cameraFragmentBinding.stepProgressView.setTotalProgress((int) retakeDuration);
                cameraFragmentBinding.stepProgressView.setCurrentProgress(0);
                cameraFragmentBinding.stepProgressView.getMarkers().clear();
                cameraFragmentBinding.frmCameraRecord.setEnabled(true);
                cameraFragmentBinding.ivCloseRecord.setVisibility(View.VISIBLE);
                cameraFragmentBinding.tvRecordTimer.setText("");
                if (Constants.IS_SPEED_ENABLE) {
//                    currentVideoSpeed = context.videoList.get(retakePos).videoSpeed;
                    cameraFragmentBinding.llSpeed.performClick();
                    if (currentVideoSpeed == 0.3f) {
                        setTextColor( cameraFragmentBinding.tvSpeedPoint3X, cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeed1X,
                                cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeed3X);
                    } else if (currentVideoSpeed == 0.5f) {
                        setTextColor(cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeedPoint3X, cameraFragmentBinding.tvSpeed1X,
                                cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeed3X);
                    } else if (currentVideoSpeed == 1f) {
                        setTextColor(cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeedPoint3X,
                                cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeed3X);
                    } else if (currentVideoSpeed == 2f) {
                        setTextColor(cameraFragmentBinding.tvSpeed2X, cameraFragmentBinding.tvSpeedPoint5X, cameraFragmentBinding.tvSpeedPoint3X,
                                cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeed3X);
                    } else if (currentVideoSpeed == 3f) {
                        setTextColor(cameraFragmentBinding.tvSpeed3X, cameraFragmentBinding.tvSpeedPoint3X, cameraFragmentBinding.tvSpeedPoint5X,
                                cameraFragmentBinding.tvSpeed1X, cameraFragmentBinding.tvSpeed2X);
                    }
                }
            }
        }
        cameraFragmentBinding.llVideoEditingOptions.setVisibility(View.VISIBLE);
        if (!context.isTextReply()) {
            cameraFragmentBinding.frmCameraRecord.setVisibility(View.VISIBLE);
        }
        if (context.isRequiredPermissionsGranted()) {
            permissionEnableDisableViews(true);
        } else if (context.isRestartAfterCrash) {
            permissionEnableDisableViews(false);
        }
    }

    @Override
    public void onPause() {
//        if (profileBlurLayout.getVisibility() == View.VISIBLE) {
//            profileBlurLayout.setVisibility(View.GONE);
//        }
        cancelTimer();
        recordAnimationClear();
        isRecording = false;
        isSwitchedCameraWhileRecording = false;
        cameraFragmentBinding.viewRedDot.setVisibility(View.INVISIBLE);
        cameraFragmentBinding.ivCloseRecord.setVisibility(View.VISIBLE);
        setTextColor( cameraFragmentBinding.tvScale1X,  cameraFragmentBinding.tvScale2X,  cameraFragmentBinding.tvScale4X);
        setTextColor( cameraFragmentBinding.tvSpeed1X,  cameraFragmentBinding.tvSpeedPoint5X,  cameraFragmentBinding.tvSpeedPoint3X,
                cameraFragmentBinding.tvSpeed2X,  cameraFragmentBinding.tvSpeed3X);
        currentVideoScale = 1.0f;
        currentVideoSpeed = 1.0f;
        super.onPause();
    }

    private long getDuration(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(String.valueOf(Uri.parse(filePath)));
        long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        try {
            retriever.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return duration;
    }

    public void setProgressAndVideo() {
        if (context.videoList.size() > 0) {
            setPullerIsDragEnable(false);
            isFirstTime = false;
//            llLength.setVisibility(View.VISIBLE);
            setImageColor( cameraFragmentBinding.ivLength);
            setTextColor( cameraFragmentBinding.tvLength);
//            closeAndReopenCamera();
            cameraFragmentBinding.stepProgressView.getMarkers().clear();
            cameraFragmentBinding. stepProgressView.setTotalProgress((int) ACTUAL_MAX_TIME);
            int duration = 0;
            for (int i = 0; i < context.videoList.size(); i++) {
                VideoModel video = context.videoList.get(i);
                duration += (int) (video.actualDuration * 1000);
                if (i == context.videoList.size() - 1 && isSwitchedCameraWhileRecording) {
                    break;
                }
                cameraFragmentBinding.stepProgressView.getMarkers().add(duration);
            }
            cameraFragmentBinding.stepProgressView.setCurrentProgress(duration);
            double progress = duration / 1000f;
            setRecordText(progress);
            CURRENT_PROGRESS = cameraFragmentBinding.stepProgressView.getCurrentProgress();
            MAX_TIME = ACTUAL_MAX_TIME - duration;
            if (isViewsDisable) {
                cameraFragmentBinding.llGallery.setEnabled(false);
                cameraFragmentBinding.llGallery.setAlpha(0.5f);
                cameraFragmentBinding.frmCameraRecord.setEnabled(false);
                cameraFragmentBinding.frmCameraRecord.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
            } else {
                cameraFragmentBinding.llGallery.setEnabled(true);
                cameraFragmentBinding.llGallery.setAlpha(1f);
                if (MAX_TIME <= 1000) {
                    cameraFragmentBinding.frmCameraRecord.setEnabled(false);
                    cameraFragmentBinding. frmCameraRecord.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
                    cameraFragmentBinding.llGallery.setVisibility(View.INVISIBLE);
                } else {
                    cameraFragmentBinding.frmCameraRecord.setEnabled(true);
                    cameraFragmentBinding.frmCameraRecord.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
                    if (!context.isRetake && !isRecording && !context.isReplyReactionWithoutVideo()) {
                        cameraFragmentBinding.llGallery.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {
            if (isViewsDisable) {
                cameraFragmentBinding.frmCameraRecord.setEnabled(false);
                cameraFragmentBinding.frmCameraRecord.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
            } else {
                cameraFragmentBinding.frmCameraRecord.setEnabled(true);
                cameraFragmentBinding.frmCameraRecord.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
            }
            setPullerIsDragEnable(true);
            CURRENT_PROGRESS = 0;
            isFirstTime = true;
            MAX_TIME = ACTUAL_MAX_TIME;
            cameraFragmentBinding.stepProgressView.setTotalProgress((int) ACTUAL_MAX_TIME);
            cameraFragmentBinding.stepProgressView.setCurrentProgress(0);
            cameraFragmentBinding.stepProgressView.getMarkers().clear();
            cameraFragmentBinding.tvRecordTimer.setText("");
            manageViews(View.GONE);
        }
//        if (isExpanded) {
//            collapse(clProfileList);
//        }
    }

    /**
     * Start countdown timer to show recording time
     */
    public void onCameraControlClick() {
        try {
            if (!isRecording) {
                if (isCameraEnable) {
                    if (context.isPhotoReply()) {
                        setPullerIsDragEnable(false);
                        isFirstTime = false;
                        context.isTabNeedToVisible = false;
                        captureImage();
                    } else {
                        //sendSegmentLogs(Constants.RECORDING_START, new Properties());
                        if (context.isRetake) {
                            startRetake();
                        } else {
                            startRecording();
                        }
                    }
                }
            } else {
                stopRecording(false);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void startRecording() {
        setPullerIsDragEnable(false);
        isFirstTime = false;
        context.isTabNeedToVisible = false;
        isRecording = true;
        cameraFragmentBinding.viewRedDot.setVisibility(View.VISIBLE);
        setNormal();
        manageViews(View.GONE);
        startRecordingVideo();
        recordAnimationStart();
        setTimer();
    }

    private void startTimerRecording() {
        if (context.isPhotoReply()) {
            captureImage();
        } else {
            isRecording = true;
            cameraFragmentBinding.viewRedDot.setVisibility(View.VISIBLE);
            manageViews(View.GONE);
            startRecordingVideo();
            recordAnimationStart();
            timerDuration = (long) (timerDuration * currentVideoSpeed);
            if (mCameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
                isFlashBlink = true;
            }
            setTimer(timerDuration);
        }
    }

    private void startRetake() {
        if (!isRetakeFromCamera && context.retakePos < context.videoList.size()) {
            retakeTempFileList.clear();
            retakeTempFileList.addAll(context.videoList.get(context.retakePos).videoRetakeFileList);
        } else if (isRetakeFromCamera) {
            isRetakeRemoved = false;
        }
        setPullerIsDragEnable(false);
        Glide.get(context).clearMemory();
        new Thread(() -> Glide.get(context).clearDiskCache()).start();
        isRecording = true;
        cameraFragmentBinding.viewRedDot.setVisibility(View.VISIBLE);
        setNormal();
        cameraFragmentBinding.frmCameraRecord.setEnabled(false);
        cameraFragmentBinding.ivCloseRecord.setVisibility(View.INVISIBLE);
        if (!isRetakeFromCamera) {
            context.mCameraFacing = mCameraFacing;
        }
        manageViews(View.GONE);
        startRecordingVideo();
        recordAnimationStart();
        retakeDuration = (long) (retakeDuration * currentVideoSpeed);
        setRetakeTimer(retakeDuration, 0);
    }

    private void stopRecording(boolean isRedirectToPreview) {
        isRecording = false;
        isCameraEnable = false;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            context.runOnUiThread(() -> {
                cameraFragmentBinding.viewRedDot.setVisibility(View.INVISIBLE);
                setTextColor( cameraFragmentBinding.tvScale1X,  cameraFragmentBinding.tvScale2X,  cameraFragmentBinding.tvScale4X);
                setTextColor( cameraFragmentBinding.tvSpeed1X,  cameraFragmentBinding.tvSpeedPoint5X,  cameraFragmentBinding.tvSpeedPoint3X,
                        cameraFragmentBinding.tvSpeed2X,  cameraFragmentBinding.tvSpeed3X);
                recordAnimationClear();
            });
            stopRecordingVideoNew();
            context.runOnUiThread(() -> {
                addVideoToList(false);
                isSwitchedCameraWhileRecording = false;
                manageViews(View.VISIBLE);
                cancelTimer();
                setProgressAndVideo();
            });

            handler.post(() -> {
                if (context.videoList.size() > 0 && context.videoList.get(context.videoList.size() - 1).actualDuration < 1.0f) {
                    deleteLastClip(false);
                } else {
                    startPreview();
                }
            });
        });
        new Handler(Looper.getMainLooper()).postDelayed(() -> isCameraEnable = true, 300);
        currentVideoSpeed = 1.0f;
        currentVideoScale = 1.0f;
    }

    private void stopAndStartRecording() {
        closeCamera();
        if (context.isRetake && !isRetakeFromCamera) {
            addRetakeVideoToList();
        } else {
            if (isRetakeFromCamera) {
                if (context.videoList.size() > 0) {
                    VideoModel video = context.videoList.get(context.videoList.size() - 1);
                    context.videoList.remove(video);
                    isRetakeRemoved = true;
                }
            }
            addVideoToList(false);
        }
        isSwitchedCameraWhileRecording = true;
        cancelTimer();
        if (!isRetakeFromCamera && !context.isRetake) {
            setProgressAndVideo();
        }
        if (mCameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
            mCameraFacing = CameraCharacteristics.LENS_FACING_FRONT;
        } else {
            mCameraFacing = CameraCharacteristics.LENS_FACING_BACK;
        }
        context.mCameraFacing = mCameraFacing;
        rotateImage(cameraFragmentBinding.imgSwitchCamera);
        openCamera();
        isCameraEnable = false;
        startRecordingAfterSwitch();
    }

    private void startRecordingAfterSwitch() {
        if (isMediaRecorderPrepared) {
            if (isFlashOn) {
                if (mCameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraFragmentBinding.relativeWhite.setVisibility(View.VISIBLE);
                    setFullBrightness();
                } else {
                    cameraFragmentBinding.relativeWhite.setVisibility(View.GONE);
                }
            }
            isCameraEnable = true;
            startRecordingVideo();
            if (isRetakeFromCamera || context.isRetake) {
                long progress = cameraFragmentBinding.stepProgressView.getCurrentProgress();
                setRetakeTimer(retakeDuration - progress, progress);
            } else {
                setTimer();
            }
        } else {
            new Handler().postDelayed(this::startRecordingAfterSwitch, 100);
        }
    }

    private void addVideoToList(boolean isFromGallery) {
        VideoModel video;
        File videoFile;
        if (isFromGallery) {
            videoFile = new File(galleryVideoFile.getAbsolutePath());
        } else {
            if (this.videoFile == null) {
                this.videoFile = getVideoFile(context);
            }
            videoFile = new File(this.videoFile.getAbsolutePath());
        }
        if (context.isTextReply()) {
            if (TextUtils.isEmpty(context.textVideoPath)) {
                context.textVideoPath = context.getTextVideoPath();
            }
            File fromFile = new File(context.textVideoPath);
            if (videoFile.exists()) {
                videoFile.delete();
            }
            try {
                boolean isCreated = videoFile.createNewFile();
                Utility.showLog("TAG", "File Created " + isCreated);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                copyFile(fromFile, videoFile);
            } catch (Exception e) {
                Utility.showLogException(e);
            }
        }
        Utility.showLog("File", videoFile.getAbsolutePath());

        if (isSwitchedCameraWhileRecording && context.videoList.size() > 0) {
            video = context.videoList.get(context.videoList.size() - 1);
        } else {
            video = new VideoModel();
        }
        long currentFileDuration;
        if (isRetakeFromCamera) {
            video.isSkipMemory = true;
            video.duration = Double.parseDouble(String.format(Locale.ENGLISH, "%.1f", (double) retakeDuration / 1000f));
            video.actualDuration = Double.parseDouble(String.format(Locale.ENGLISH, "%.1f", (double) retakeDuration / 1000f));
            try {
                currentFileDuration = getDuration(videoFile.getAbsolutePath());
            } catch (Exception e) {
                currentFileDuration = (long) (getDuration() * 1000);
            }
        } else {
            video.duration += getDuration();

            try {
                currentFileDuration = getDuration(videoFile.getAbsolutePath());
                double sec = currentFileDuration / 1000f;
                video.actualDuration += Double.parseDouble(String.format(Locale.ENGLISH, "%.1f", sec));
            } catch (Exception e) {
                currentFileDuration = (long) (video.duration * 1000);
                video.actualDuration += video.duration;
            }
        }

        if (video.actualDuration == 0 && !context.isPhotoReply()) {
            Utility.showToast(context, context.getResources().getString(R.string.unable_add_clip));
            return;
        }

//        if (isFromGallery) {
//            Properties properties = new Properties();
//            properties.put(Constants.KEY_UPLOAD_VIDEO_LENGTH, video.actualDuration + "s");
//            sendSegmentLogs(Constants.VIDEO_UPLOADED_FROM_LIBRARY, properties);
//        }

        video.actualDurationWithoutSpeed = video.actualDuration;
        video.duration = Double.parseDouble(String.format(Locale.ENGLISH, "%.1f", video.duration / currentVideoSpeed));
        video.actualDuration = Double.parseDouble(String.format(Locale.ENGLISH, "%.1f", video.actualDuration / currentVideoSpeed));
        video.previewSize = getPreviewSize();
        video.isFromGallery = isFromGallery;
        VideoFileModel videoFileModel = new VideoFileModel();
        videoFileModel.filePath = videoFile.getAbsolutePath();
        videoFileModel.fileName = videoFile.getName();
        if (isFromGallery || context.isTextReply()) {
            videoFileModel.isFront = false;
        } else {
            videoFileModel.isFront = getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT;
        }
        videoFileModel.videoZoomLevel = currentVideoScale;
        videoFileModel.videoSpeed = currentVideoSpeed;
        videoFileModel.trimStartMillis = 0;
        videoFileModel.trimEndMillis = currentFileDuration;
        if (context.videoList.size() == 0) {
            video.isSelected = true;
        }
        if (context.isRetake && !isRetakeFromCamera) {
            video.videoRetakeFileList.add(videoFileModel);
        } else {
            video.videoFileList.add(videoFileModel);
        }
        if (!isSwitchedCameraWhileRecording || context.videoList.size() == 0) {
            context.videoList.add(video);
        }
        if (!context.isReplyReactionWithoutVideo()) {
            Glide.with(context.getApplicationContext()).asBitmap().load(videoFileModel.filePath).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    Bitmap bmp = resource;
                    if (videoFileModel.isFront) {
                        bmp = createFlippedBitmap(resource, true, false);
                    }
                    videoFileModel.bmp = bmp;
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
        }
    }

    public void addRetakeVideoToList() {
        long currentFileDuration;
        VideoModel video = context.videoList.get(context.retakePos);
        try {
            currentFileDuration = getDuration(videoFile.getAbsolutePath());
        } catch (Exception e) {
            currentFileDuration = (long) (getDuration() * 1000);
        }
        VideoFileModel videoFileModel = new VideoFileModel();
        videoFileModel.filePath = videoFile.getAbsolutePath();
        videoFileModel.fileName = videoFile.getName();
        videoFileModel.isFront = getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT;
        videoFileModel.videoZoomLevel = currentVideoScale;
        videoFileModel.videoSpeed = currentVideoSpeed;
        videoFileModel.trimStartMillis = 0;
        videoFileModel.trimEndMillis = currentFileDuration;
        video.videoRetakeFileList.add(videoFileModel);
        if (retakeTempFileList != null && retakeTempFileList.size() > 0) {
            video.videoRetakeFileList.removeAll(retakeTempFileList);
        }
        Glide.with(context.getApplicationContext()).asBitmap().load(videoFileModel.filePath).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Bitmap bmp = resource;
                if (videoFileModel.isFront) {
                    bmp = createFlippedBitmap(resource, true, false);
                }
                videoFileModel.bmp = bmp;
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    public Bitmap createFlippedBitmap(Bitmap source, boolean xFlip, boolean yFlip) {
        Matrix matrix = new Matrix();
        matrix.postScale(xFlip ? -1 : 1, yFlip ? -1 : 1, source.getWidth() / 2f, source.getHeight() / 2f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private double getDuration() {
        double sec = (cameraFragmentBinding.stepProgressView.getCurrentProgress() - CURRENT_PROGRESS) / 1000f;
        return Double.parseDouble(String.format(Locale.ENGLISH, "%.1f", sec));
    }

    private void centerTextManage() {
        if (context.videoOptions != CameraNewActivity.VideoOptions.RECORD_FOR_OTHER) {
            cameraFragmentBinding.llTabLayout.setVisibility(View.GONE);
            cameraFragmentBinding.llMiddle.setVisibility(View.VISIBLE);
        }
    }

    private boolean isLastVideoFromGallery() {
        if (context.videoList.size() > 0) {
            return context.videoList.get(context.videoList.size() - 1).isFromGallery;
        } else {
            return false;
        }
    }

    private void manageViews(int visibility) {
        if (context.videoList.size() == 0 && !context.isLongPress) {
            cameraFragmentBinding.llRetake.setVisibility(View.INVISIBLE);
            cameraFragmentBinding.llDeleteClip.setVisibility(View.GONE);
            cameraFragmentBinding.ivRecordDone.setVisibility(View.GONE);
            if (isRecording) {
                cameraFragmentBinding.tvRecordTimer.setVisibility(View.VISIBLE);
                cameraFragmentBinding.llSendToLayout.setVisibility(View.GONE);
                cameraFragmentBinding.llPrivacyLayout.setVisibility(View.GONE);
                cameraFragmentBinding.llQuestionSelect.setVisibility(View.GONE);
                cameraFragmentBinding.ivAddContact.setVisibility(View.INVISIBLE);
               // llProfileLayout.setVisibility(View.GONE);
                cameraFragmentBinding.llProgressBar.setVisibility(View.VISIBLE);
                cameraFragmentBinding.imgSwitchFlash.setVisibility(View.INVISIBLE);
                cameraFragmentBinding.llGallery.setVisibility(View.INVISIBLE);
//                frameCameraSwitch.setVisibility(View.INVISIBLE);
                centerTextManage();
//                llTimeSelection.setVisibility(View.INVISIBLE);
                cameraFragmentBinding.llVideoEditingOptions.setVisibility(View.GONE);
                cameraFragmentBinding.llCameraQuestionView.setVisibility(View.GONE);
            } else {
                cameraFragmentBinding.tvRecordTimer.setVisibility(View.GONE);
                cameraFragmentBinding.llProgressBar.setVisibility(View.GONE);
                cameraFragmentBinding.llBottomLayoutCamera.setVisibility(View.VISIBLE);
                if (!context.isReplyReactionWithoutVideo()) {
                    if (!context.isRetake) {
                        cameraFragmentBinding.llGallery.setVisibility(View.VISIBLE);
                    }
                    cameraFragmentBinding.imgSwitchFlash.setVisibility(View.VISIBLE);
                }
                cameraFragmentBinding.llBottomMiddle.setVisibility(View.VISIBLE);
                cameraFragmentBinding.frameCameraSwitch.setVisibility(View.VISIBLE);
                if (context.isRetake) {
//                    llTimeSelection.setVisibility(View.INVISIBLE);
                    cameraFragmentBinding.llVideoEditingOptions.setVisibility(View.GONE);
                    centerTextManage();
                } else {
//                    llLength.setVisibility(View.VISIBLE);
                    cameraFragmentBinding.llVideoEditingOptions.setVisibility(View.VISIBLE);
//                    llTimeSelection.setVisibility(View.VISIBLE);
                    cameraFragmentBinding.llTimerDetails.setVisibility(View.GONE);
                    cameraFragmentBinding.llBottomMiddle.setVisibility(View.VISIBLE);
                    cameraFragmentBinding.llBottomCountDown.setVisibility(View.GONE);
                    cameraFragmentBinding.rlRecord.setVisibility(View.VISIBLE);
                    if (context.videoOptions != CameraNewActivity.VideoOptions.REPLY_REACTION && context.videoOptions != CameraNewActivity.VideoOptions.COMMENT) {
                        if (context.isTabNeedToVisible) {
                            cameraFragmentBinding. llTabLayout.setVisibility(View.VISIBLE);
                            cameraFragmentBinding.llMiddle.setVisibility(View.GONE);
                        } else {
                            centerTextManage();
                        }

//                        if (context.isDirectOrRoundTable() && context.selectedContacts.size() > 0) {
//                            llSendToLayout.setVisibility(View.VISIBLE);
//                           // llProfileLayout.setVisibility(View.VISIBLE);
//                            ivAddContact.setVisibility(View.INVISIBLE);
//                        } else if (context.videoOptions == CameraNewActivity.VideoOptions.RECORD_FOR_OTHER && context.contactModelForOther != null) {
//                            llSendToLayout.setVisibility(View.VISIBLE);
//                            //llProfileLayout.setVisibility(View.VISIBLE);
//                            llPrivacyLayout.setVisibility(View.GONE);
//                        } else {
                           // llProfileLayout.setVisibility(View.GONE);
                            if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
                                setPostingToForLoop();
                                cameraFragmentBinding.llSendToLayout.setVisibility(View.VISIBLE);
                            } else if (context.videoOptions != CameraNewActivity.VideoOptions.DIRECT && context.videoOptions != CameraNewActivity.VideoOptions.GROUP) {
                                setPrivacyLayout();
                                cameraFragmentBinding.llSendToLayout.setVisibility(View.GONE);
                            } else {
                                cameraFragmentBinding.llPrivacyLayout.setVisibility(View.GONE);
                                cameraFragmentBinding.llSendToLayout.setVisibility(View.GONE);
                            }
                            if (context.isDirectOrRoundTable()) {
                                cameraFragmentBinding.ivAddContact.setVisibility(View.VISIBLE);
                            } else {
                                cameraFragmentBinding.ivAddContact.setVisibility(View.INVISIBLE);
                            }
                        //}
                    } else {
                        cameraFragmentBinding.llPrivacyLayout.setVisibility(View.VISIBLE);
                        //llProfileLayout.setVisibility(View.VISIBLE);
                    }
                    cameraFragmentBinding.llQuestion.setVisibility(View.VISIBLE);
                    cameraFragmentBinding.llTopic.setVisibility(View.VISIBLE);
                    cameraFragmentBinding.ivRemoveQuestion.setVisibility(View.GONE);
                    cameraFragmentBinding.ivRemoveTopic.setVisibility(View.GONE);
                    if (context.selectedQuestion == null) {
                        cameraFragmentBinding.llQuestionSelect.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {
            cameraFragmentBinding.llQuestionSelect.setVisibility(View.GONE);
            if (context.selectedQuestion == null) {
                cameraFragmentBinding.llQuestion.setVisibility(View.GONE);
            } else {
                cameraFragmentBinding. ivRemoveQuestion.setVisibility(visibility);
                cameraFragmentBinding.llCameraQuestionView.setVisibility(visibility);
            }
            if (context.selectedTopic == null) {
                cameraFragmentBinding.llTopic.setVisibility(View.GONE);
            } else {
                cameraFragmentBinding.ivRemoveTopic.setVisibility(visibility);
            }
            if (isViewsDisable) {
                cameraFragmentBinding.llRetake.setVisibility(View.INVISIBLE);
                cameraFragmentBinding.llDeleteClip.setVisibility(View.GONE);
                cameraFragmentBinding.ivRecordDone.setVisibility(View.GONE);
            } else {
                if (visibility == View.GONE || isLastVideoFromGallery()) {
                    cameraFragmentBinding.llRetake.setVisibility(View.INVISIBLE);
                } else {
                    cameraFragmentBinding.llRetake.setVisibility(visibility);
                }
                cameraFragmentBinding.llDeleteClip.setVisibility(visibility);
                cameraFragmentBinding.ivRecordDone.setVisibility(visibility);
            }

            centerTextManage();

            if (visibility == View.GONE) {
                cameraFragmentBinding.imgSwitchFlash.setVisibility(View.INVISIBLE);
                cameraFragmentBinding.llGallery.setVisibility(View.INVISIBLE);
                if (context.isDirectOrRoundTable()) {
                    if (isExpanded) {
                        restoreDefaultProfileList();
                    }
                    //llProfileLayout.setVisibility(View.GONE);
                }
            } else {
                if (!context.isReplyReactionWithoutVideo()) {
                    cameraFragmentBinding.llGallery.setVisibility(visibility);
                    cameraFragmentBinding.imgSwitchFlash.setVisibility(visibility);
                }
                cameraFragmentBinding.frameCameraSwitch.setVisibility(visibility);
                if (isExpanded) {
                    restoreDefaultProfileList();
                }
                // change to visible if profile layout need to display after recording
                //llProfileLayout.setVisibility(View.GONE);
            }

            if (isExpanded || !isRecording) {
                cameraFragmentBinding.tvRecordTimer.setVisibility(View.GONE);
            } else {
                cameraFragmentBinding.tvRecordTimer.setVisibility(View.VISIBLE);
            }
            cameraFragmentBinding.llVideoEditingOptions.setVisibility(visibility);
            cameraFragmentBinding.llSendToLayout.setVisibility(View.GONE);
            cameraFragmentBinding.llPrivacyLayout.setVisibility(View.GONE);
            cameraFragmentBinding.ivAddContact.setVisibility(View.INVISIBLE);
        }
    }

    private void setTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        MAX_TIME = (long) (MAX_TIME * currentVideoSpeed);

        countDownTimer = new CountDownTimer(MAX_TIME, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                long finishedSeconds = CURRENT_PROGRESS + (long) ((MAX_TIME - millisUntilFinished) / currentVideoSpeed);
                cameraFragmentBinding.stepProgressView.setCurrentProgress((int) finishedSeconds);
                double progress = finishedSeconds / 1000f;
                setRecordText(progress);
            }

            @Override
            public void onFinish() {
                if (isAdded()) {
                    context.prevEventTime = previousEventTime;
                    setFlashOffCamera();
                    stopRecording(true);
                }
            }
        };
        countDownTimer.start();
    }

    private void goToVideoPlayFragment() {
        for (int i = 0; i < context.videoList.size(); i++) {
            context.videoList.get(i).isFullTrim = false;
        }
        VideoModel video = context.videoList.get(0);
        storeMultiTrimData(video, (long) (video.actualDuration * 1000));
        if (context.videoList.size() > 1) {
            VideoModel video1 = context.videoList.get(context.videoList.size() - 1);
            storeMultiTrimData(video1, (long) (video1.actualDuration * 1000));
        }
        context.goToVideoPlayFragment();
    }

    private void storeMultiTrimData(VideoModel video, long endTrimMillis) {
        video.fullTrimStartMillis = 0;
        video.fullTrimEndMillis = endTrimMillis;
        video.fullTrimDuration = getDuration(endTrimMillis);
        video.isFullTrim = true;
        context.isFullTrim = true;
    }

    private double getDuration(long endTrimMillis) {
        double sec = (endTrimMillis) / 1000f;
        return Double.parseDouble(String.format(Locale.ENGLISH, "%.1f", sec));
    }

    private void setRecordText(double progress) {
        if (isRetakeFromCamera || context.isRetake) {
            progress = (retakeDuration / 1000f) - progress;
        } else {
            progress = (ACTUAL_MAX_TIME / 1000f) - progress;
        }
        if (progress < 0) {
            progress = 0;
        }
        int progressValue = (int) Math.round(progress);
        String value;
        if (progressValue < 10) {
            value = "0" + progressValue + "s";
        } else {
            value = progressValue + "s";
        }
        cameraFragmentBinding.tvRecordTimer.setText(value);
    }

    private void setRetakeTimer(final long maxTime, final long progress) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(maxTime, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                long finishedSeconds = (long) ((maxTime - millisUntilFinished) / currentVideoSpeed) + progress;
                cameraFragmentBinding.stepProgressView.setCurrentProgress((int) (finishedSeconds));
                double progress = finishedSeconds / 1000f;
                setRecordText(progress);
            }

            @Override
            public void onFinish() {
                isRecording = false;
                context.isRetake = false;
                context.retakeFileName = "";
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                executor.execute(() -> {
                    context.runOnUiThread(() -> {
                        cameraFragmentBinding.viewRedDot.setVisibility(View.INVISIBLE);
                        recordAnimationClear();
                        if (!isRetakeFromCamera) {
                            setFlashOffCamera();
                        }
                    });
                    stopRecordingVideoNew();
                    handler.post(() -> {
                        if (isRetakeFromCamera) {
                            if (context.videoList.size() > 0 && !isRetakeRemoved) {
                                VideoModel video = context.videoList.get(context.videoList.size() - 1);
                                context.videoList.remove(video);
                            }
                            addVideoToList(false);
                            isSwitchedCameraWhileRecording = false;
                            isRetakeFromCamera = false;
                            isRetakeRemoved = false;
                            setProgressAndVideo();
                            manageViews(View.VISIBLE);
                            cameraFragmentBinding.ivCloseRecord.setVisibility(View.VISIBLE);
                            //Function call shifted down here to prevent the file name
                            //from getting renamed before the retaken video gets added
                            //to the list.
                            startPreview();
                        } else {
                            addRetakeVideoToList();
                            isSwitchedCameraWhileRecording = false;
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                context.getSupportFragmentManager().popBackStack();
                                context.mRetakeFragment = null;
//                                if (context.videoTrimFragment != null) {
//                                    //context.videoTrimFragment.refreshSectionView();
//                                }
                            }, 100);
                        }
                    });
                });
            }
        };
        countDownTimer.start();
    }

    private void setTimer(final long maxTime) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(maxTime, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                long finishedSeconds = CURRENT_PROGRESS + (long) ((maxTime - millisUntilFinished) / currentVideoSpeed);
                cameraFragmentBinding.stepProgressView.setCurrentProgress((int) finishedSeconds);
                double progress = finishedSeconds / 1000f;
                setRecordText(progress);
            }

            @Override
            public void onFinish() {
                if (isAdded()) {
                    if (MAX_TIME <= 0) {
                        setFlashOffCamera();
                    }
                    stopRecording(true);
                }
            }
        };
        countDownTimer.start();
    }

    private void cancelTimer() {
        cameraFragmentBinding.tvTimerText.setVisibility(View.GONE);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }

    Animation zoomIn, zoomOut;
    boolean isStopAnimation;
    AnimatorSet scaleAnim = new AnimatorSet();

    private void recordAnimationStart() {
        context.runOnUiThread(() -> {
            if (context.isPhotoReply()) {
                return;
            }

            if (isFlashOn && mCameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                cameraFragmentBinding.relativeWhite.setVisibility(View.VISIBLE);
                setFullBrightness();
            }
            isStopAnimation = false;

            cameraFragmentBinding.flRecordButton.setBackgroundResource(R.drawable.record_button_ring);
            cameraFragmentBinding.imgCircle2.setVisibility(View.VISIBLE);
            cameraFragmentBinding.frmCameraRecord.setAlpha(0);

            zoomIn = AnimationUtils.loadAnimation(context, R.anim.scale_camera_progress);
            zoomOut = AnimationUtils.loadAnimation(context, R.anim.scale_down_camera_progress);

            cameraFragmentBinding.flRecordButton.startAnimation(zoomIn);

            zoomIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!isStopAnimation) {
                        cameraFragmentBinding.flRecordButton.startAnimation(zoomOut);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            zoomOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!isStopAnimation) {
                        cameraFragmentBinding.flRecordButton.startAnimation(zoomIn);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            if (context.isAudioReply()) {
                cameraFragmentBinding.ivAudioGif.animate().alpha(1).setDuration(500);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat( cameraFragmentBinding.rlAudioImage, "scaleX", 1.0f, 0.8f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat( cameraFragmentBinding.rlAudioImage, "scaleY", 1.0f, 0.8f);

                scaleX.setRepeatCount(ObjectAnimator.INFINITE);
                scaleX.setRepeatMode(ObjectAnimator.REVERSE);

                scaleY.setRepeatCount(ObjectAnimator.INFINITE);
                scaleY.setRepeatMode(ObjectAnimator.REVERSE);

                scaleAnim.setDuration(1000);
                scaleAnim.play(scaleX).with(scaleY);
                scaleAnim.start();
            }
        });
    }

    private void recordAnimationClear() {
        context.runOnUiThread(() -> {
            if (context.isPhotoReply()) {
                return;
            }
            isStopAnimation = true;
            cameraFragmentBinding.flRecordButton.setBackgroundColor(context.getResources().getColor(R.color.transparent, null));
            cameraFragmentBinding.frmCameraRecord.setBackgroundResource(R.drawable.ic_record);
            cameraFragmentBinding.frmCameraRecord.setAlpha(1);
            cameraFragmentBinding.imgCircle2.setVisibility(View.GONE);
            cameraFragmentBinding.relativeWhite.setVisibility(View.GONE);
            cameraFragmentBinding.flRecordButton.animate().cancel();
            cameraFragmentBinding.flRecordButton.clearAnimation();
            if (context.isAudioReply()) {
                if (scaleAnim != null && scaleAnim.isRunning()) {
                    scaleAnim.cancel();
                }
                cameraFragmentBinding.ivAudioGif.animate().alpha(0).setDuration(500);
                cameraFragmentBinding.rlAudioImage.animate().setDuration(500).scaleX(1).scaleY(1);
                cameraFragmentBinding.rlAudioImage.clearAnimation();
            }
        });
    }

    public void showCloseConfirmAlert() {
        if (isRecording && !isRetakeFromCamera) {
            stopRecording(false);
        }

        if ( cameraFragmentBinding.questionBlurLayout.getVisibility() == View.VISIBLE) {
            if ( cameraFragmentBinding.rlAddCustomQuestion.getVisibility() == View.VISIBLE) {
                //rlAddCustomQuestion.setVisibility(View.GONE);
                //rlQuestion.setVisibility(View.VISIBLE);
                handleQuestionBack();
            } else {
                questionNormalFromBlur();
            }
            return;
        }

        if (context.videoList.size() == 0) {
            context.onPullComplete();
            return;
        }

        if (isRetakeFromCamera) {
            backFromRetake();
            return;
        }

        mCloseDialog = new Dialog(context);
        mCloseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mCloseDialog.setContentView(R.layout.common_simple_dialog);
        mCloseDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mCloseDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mCloseDialog.show();

        CustomTextView tvTitle = mCloseDialog.findViewById(R.id.dialog_title);
        CustomTextView tvMsg = mCloseDialog.findViewById(R.id.dialog_message);
        CustomTextView btnCancel = mCloseDialog.findViewById(R.id.dialog_btn_cancel);
        CustomTextView btnDiscard = mCloseDialog.findViewById(R.id.dialog_btn_yes);
        CustomTextView btnStartOver = mCloseDialog.findViewById(R.id.dialog_btn_next);
        btnStartOver.setVisibility(View.VISIBLE);
        btnStartOver.setText(context.getResources().getString(R.string.start_over));
        btnCancel.setTextColor(getResources().getColor(R.color.red, null));
        btnDiscard.setTextColor(getResources().getColor(R.color.splash_background, null));
        btnDiscard.setText(getResources().getString(R.string.btn_discard));
        tvMsg.setText(getResources().getString(R.string.txt_close_confirm_sub));
        tvTitle.setVisibility(View.GONE);

        btnCancel.setOnClickListener(v -> {
            mCloseDialog.dismiss();
        });

        btnDiscard.setOnClickListener(v -> {
            mCloseDialog.dismiss();
            backManage();
        });

        btnStartOver.setOnClickListener(view -> {
            mCloseDialog.dismiss();
            context.videoList.clear();
            context.isRetake = false;
            isRetakeFromCamera = false;
            cameraFragmentBinding.frmCameraRecord.setEnabled(true);
            cameraFragmentBinding.frmCameraRecord.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
            manageViews(View.VISIBLE);
            setNormal();
            setProgressAndVideo();
//            llTimeSelection.setVisibility(View.VISIBLE);
            cameraFragmentBinding.tvTimer6Sec.setVisibility(View.VISIBLE);
            cameraFragmentBinding.tvTimer15Sec.setVisibility(View.VISIBLE);
            cameraFragmentBinding.tvTimer60Sec.setVisibility(View.VISIBLE);
            //sendSegmentLogs(Constants.CAMERA_CLOSE_START_OVER_CLICKED, new Properties());
        });
    }

    private void setFlashOffCamera() {
        if (isFlashOn) {
            isFlashOn = false;
            setFlashOff();
            cameraFragmentBinding.relativeWhite.setVisibility(View.GONE);
            cameraFragmentBinding.imgSwitchFlash.setImageResource(R.drawable.ic_flashoff);
        }
    }

    public void selectVideoFromGallery() {
        //sendSegmentLogs(Constants.UPLOAD_CLICKED, new Properties());
        if (videoFile == null) {
            if (context.isPhotoReply()) {
                videoFile = getPhotoFile(context);
            } else {
                videoFile = getVideoFile(context);
            }
        }
        galleryVideoFile = new File(videoFile.getAbsolutePath());
        closeCamera();
       //context.goToGalleryFragment(MAX_TIME);
    }

    public void permissionEnableDisableViews(boolean enable) {
        setViewAndChildrenEnabled( cameraFragmentBinding.frameCameraSwitch, enable);
        setViewAndChildrenEnabled( cameraFragmentBinding.imgSwitchFlash, enable);
        if (enable) {
            cameraFragmentBinding.imgSwitchBg.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
            cameraFragmentBinding.imgSwitchCamera.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
            cameraFragmentBinding.imgSwitchFlash.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
        } else {
            cameraFragmentBinding.imgSwitchBg.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
            cameraFragmentBinding.imgSwitchCamera.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
            cameraFragmentBinding.imgSwitchFlash.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
        }
        showPermissionView(enable ? View.GONE : View.VISIBLE);
    }

    private void backFromRetake() {
        if (isRecording) {
            isRecording = false;
            isCameraEnable = false;
            cameraFragmentBinding.viewRedDot.setVisibility(View.INVISIBLE);
            setTextColor( cameraFragmentBinding.tvScale1X,  cameraFragmentBinding.tvScale2X,  cameraFragmentBinding.tvScale4X);
            setTextColor( cameraFragmentBinding.tvSpeed1X,  cameraFragmentBinding.tvSpeedPoint5X,  cameraFragmentBinding.tvSpeedPoint3X,
                    cameraFragmentBinding.tvSpeed2X,  cameraFragmentBinding.tvSpeed3X);
            recordAnimationClear();
            stopRecordingCamera();
            isSwitchedCameraWhileRecording = false;
        }
        isRetakeFromCamera = false;
        isRetakeRemoved = false;
        context.isRetake = false;
        context.retakeFileName = "";
        manageViews(View.VISIBLE);
        cameraFragmentBinding.ivCloseRecord.setVisibility(View.VISIBLE);
        cancelTimer();
        setNormal();
        setProgressAndVideo();
        closeAndReopenCamera();
        cameraFragmentBinding.frmCameraRecord.setEnabled(true);
        cameraFragmentBinding.frmCameraRecord.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
        //sendSegmentLogs(Constants.CAMERA_RETAKE_CLOSED, new Properties());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        if (isExpanded) {
            //collapse(clProfileList);
        }
        if (view ==  cameraFragmentBinding.questionView.questionViewBinding.ivEditQuestion) {
            if (isLoopQnA) {
                newCustomQuestionModel = QuestionViewModel.getInstance().loopQuestionsArr.get(0);
            } else {
                newCustomQuestionModel = QuestionViewModel.getInstance().masterQuestionsArr.get(0);
            }
            cameraFragmentBinding.customQuestionView.customAddQuestionView.setCustomQuestionData(newCustomQuestionModel, newCustomQuestionModel.getQuestion().length());
            oldQuestionText = newCustomQuestionModel.getQuestion();
            openViewWithAnimation();
        }
        int id = view.getId();
        if (id == R.id.llPrivacyLayout) {
            if (context.videoOptions == CameraNewActivity.VideoOptions.PUBLIC || context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
                //context.openBottomSheetDialogForPrivacyOptions(ivPrivacyType, tvPrivacyOption, null);
            }
        } else if (id == R.id.llGallery) {
//            if (context.isStoragePermissionsGranted()) {
//                selectVideoFromGallery();
//                context.contentGallery.setTranslationY(context.screenHeight);
//                ObjectAnimator tranYAnim = ObjectAnimator.ofFloat(context.contentGallery, "translationY", context.screenHeight, 0f);
//                tranYAnim.setDuration(300);
//                tranYAnim.start();
//            } else {
//                context.requestStoragePermissions();
//            }
        } else if (id == R.id.btn_setting) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        } else if (id == R.id.frm_camera_record) {
            if (isCameraEnable) {
                onCameraControlClick();
            }
        } else if (id == R.id.img_switch_camera) {
            if (isRecording) {
                stopAndStartRecording();
                //sendSegmentLogs(Constants.SWITCH_CAMERA_WHILE_RECORDING, new Properties());
            } else {
                setTextColor( cameraFragmentBinding.tvScale1X,  cameraFragmentBinding.tvScale2X,  cameraFragmentBinding.tvScale4X);
                currentVideoScale = 1.0f;
                switchCameraNew( cameraFragmentBinding.imgSwitchCamera);
                String mCameraState;
                if (getCameraFacing() == CameraCharacteristics.LENS_FACING_BACK) {
                    context.mCameraFacing = CameraCharacteristics.LENS_FACING_BACK;
                    //map.put("selection", "back");
                    mCameraState = "back";
                } else {
                    context.mCameraFacing = CameraCharacteristics.LENS_FACING_FRONT;
                    //map.put("selection", "front");
                    mCameraState = "front";
                }
            }
        } else if (id == R.id.llRetake) {
            if (context.videoList.size() > 0) {
                retakeVideoFromCamera();
            }
        } else if (id == R.id.llDeleteClip) {
            if (context.videoList.size() > 0) {
                showDeleteClipAlert();
            }
        } else if (id == R.id.ivRecordDone) {
            if (context.videoList.size() > 0) {
                if (!context.isRecordFinishedLogged) {
                    context.isRecordFinishedLogged = true;
                }
                previousEventTime = System.currentTimeMillis();
                setFlashOffCamera();
                if (context.isDirectOrRoundTable()) {
                    restoreDefaultProfileList();
                }
                context.prevEventTime = previousEventTime;
                goToVideoPlayFragment();
//                    context.goToVideoTrimmer();
            }
        } else if (id == R.id.ivCloseRecord || id == R.id.ivCloseRecord1) {
            if (context.isRetake && !isRetakeFromCamera) {
                backManage();
            } else if (isRetakeFromCamera) {
                backFromRetake();
            } else {
                showCloseConfirmAlert();
                //sendSegmentLogs(Constants.CAMERA_CLOSE_CLICKED, new Properties());
            }
        } else if (id == R.id.img_switch_flash) {
            if (!isFlashOn) {
                isFlashOn = true;
                setFlashOn();
                cameraFragmentBinding.imgSwitchFlash.setImageResource(R.drawable.ic_flash);
            } else {
                isFlashOn = false;
                setFlashOff();
                cameraFragmentBinding.relativeWhite.setVisibility(View.GONE);
                cameraFragmentBinding.imgSwitchFlash.setImageResource(R.drawable.ic_flashoff);
            }
//            Properties propertiesFlash = new Properties();
//            propertiesFlash.put(Constants.KEY_FLASH_CURRENT_STATE, isFlashOn ? "on" : "off");
//            sendSegmentLogs(Constants.FLASH_TOGGLED, propertiesFlash);
        } else if (id == R.id.btnStartCountDown) {
            if (timerDuration >= 1000 || context.isPhotoReply()) {
                cameraFragmentBinding.tvTimerText.setVisibility(View.VISIBLE);
                setNormal();
                cameraFragmentBinding.llVideoEditingOptions.setVisibility(View.GONE);
                if (context.videoList.size() == 0) {
                    cameraFragmentBinding.llProgressBar.setVisibility(View.VISIBLE);
                    cameraFragmentBinding.imgSwitchFlash.setVisibility(View.INVISIBLE);
//                        frameCameraSwitch.setVisibility(View.INVISIBLE);
//                        llTimeSelection.setVisibility(View.INVISIBLE);
                    cameraFragmentBinding.llCameraQuestionView.setVisibility(View.GONE);
                    cameraFragmentBinding.llQuestionSelect.setVisibility(View.GONE);
                    cameraFragmentBinding.llPrivacyLayout.setVisibility(View.GONE);
                    //llProfileLayout.setVisibility(View.GONE);
                    //cameraFragmentBinding.ivContactPicture.setVisibility(View.GONE);
                    cameraFragmentBinding.llGallery.setVisibility(View.INVISIBLE);
                    centerTextManage();
                } else {
                    manageViews(View.GONE);
                }
                setPullerIsDragEnable(false);
                startCountDownTimer();
                if (context.isDirectOrRoundTable()) {
                    cameraFragmentBinding.llSendToLayout.setVisibility(View.GONE);
                    //llProfileLayout.setVisibility(View.GONE);
                    cameraFragmentBinding.ivAddContact.setVisibility(View.GONE);
                }
            } else {
                Utility.showToast(context, getString(R.string.record_more_than_a_second));
            }
        } else if (id == R.id.llScale) {
            previousEventTime = System.currentTimeMillis();
            String state;
            if (cameraFragmentBinding.llScaleDetails.getVisibility() == View.GONE) {
                state = "open";
                setTextColor(cameraFragmentBinding.tvScale, cameraFragmentBinding.tvLength, cameraFragmentBinding.tvTimer,
                        cameraFragmentBinding.tvSpeed, cameraFragmentBinding.tvCollab);
                setImageColor(cameraFragmentBinding.ivScale, cameraFragmentBinding.ivLength, cameraFragmentBinding.ivTimer,
                        cameraFragmentBinding.ivSpeed, cameraFragmentBinding.ivCollab);
//                    llTimeSelection.setVisibility(View.INVISIBLE);
                cameraFragmentBinding.llScaleDetails.setVisibility(View.VISIBLE);
                if (zoomSelect) {
                    setTextColorGray(cameraFragmentBinding.tvScale1X, cameraFragmentBinding.tvScale2X, cameraFragmentBinding.tvScale4X);
                } else if (currentVideoScale == 2.0f) {
                    setTextColor(cameraFragmentBinding.tvScale2X, cameraFragmentBinding.tvScale1X, cameraFragmentBinding.tvScale4X);
                } else if (currentVideoScale == 4.0f) {
                    setTextColor(cameraFragmentBinding.tvScale4X, cameraFragmentBinding.tvScale1X, cameraFragmentBinding.tvScale2X);
                } else {
                    setTextColor(cameraFragmentBinding.tvScale1X, cameraFragmentBinding.tvScale2X, cameraFragmentBinding.tvScale4X);
                }
                cameraFragmentBinding.llSpeedDetails.setVisibility(View.GONE);
                cameraFragmentBinding.llTimerDetails.setVisibility(View.GONE);
                cameraFragmentBinding.llBottomMiddle.setVisibility(View.VISIBLE);
                cameraFragmentBinding.llBottomCountDown.setVisibility(View.GONE);
                cameraFragmentBinding.rlRecord.setVisibility(View.VISIBLE);
            } else {
                state = "close";
                if (!context.isRetake) {
                    sameClicked = true;
                }
                setNormal();
            }
        } else if (id == R.id.llTimer) {
            previousEventTime = System.currentTimeMillis();

            if (!context.isRetake) {
                String stateTime;
                if (cameraFragmentBinding.llTimerDetails.getVisibility() == View.GONE) {
                    stateTime = "open";
                    setTextColor(cameraFragmentBinding.tvTimer, cameraFragmentBinding.tvLength, cameraFragmentBinding.tvScale,
                            cameraFragmentBinding.tvSpeed, cameraFragmentBinding.tvCollab);
                    setImageColor(cameraFragmentBinding.ivTimer,  cameraFragmentBinding.ivLength, cameraFragmentBinding.ivScale,
                            cameraFragmentBinding.ivSpeed, cameraFragmentBinding.ivCollab);
                    cameraFragmentBinding.rlRecord.setVisibility(View.INVISIBLE);
//                        llTimeSelection.setVisibility(View.INVISIBLE);
                    cameraFragmentBinding.llScaleDetails.setVisibility(View.GONE);
                    cameraFragmentBinding.llSpeedDetails.setVisibility(View.GONE);
                    cameraFragmentBinding.llTimerDetails.setVisibility(View.VISIBLE);
                    if (context.isPhotoReply()) {
                        cameraFragmentBinding.rlStopRecordingAfter.setVisibility(View.INVISIBLE);
                    } else {
                        cameraFragmentBinding.rlStopRecordingAfter.setVisibility(View.VISIBLE);
                    }
                    cameraFragmentBinding.llBottomMiddle.setVisibility(View.GONE);
                    cameraFragmentBinding.llBottomCountDown.setVisibility(View.VISIBLE);
                    cameraFragmentBinding.timerSeekBar.post(() -> {
                        progressRecorded = (int) ((ACTUAL_MAX_TIME - MAX_TIME) / 100);
                        cameraFragmentBinding.timerSeekBar.setProgress(cameraFragmentBinding.timerSeekBar.getMax());
                        viewAlreadyRecorded.setProgress(progressRecorded);
                        setTimerDetailsText(cameraFragmentBinding.timerSeekBar.getMax(), cameraFragmentBinding.timerSeekBar);
                    });
                } else {
                    stateTime = "close";
                    sameClicked = true;
                    setNormal();
                }
            } else {
                Utility.showToast(context, "This feature is not available for retake");
            }
        } else if (id == R.id.llSpeed) {
            if (cameraFragmentBinding.llSpeedDetails.getVisibility() == View.GONE) {
                setTextColor(cameraFragmentBinding.tvSpeed, cameraFragmentBinding.tvLength,
                        cameraFragmentBinding.tvTimer, cameraFragmentBinding.tvScale, cameraFragmentBinding.tvCollab);
                setImageColor(cameraFragmentBinding.ivSpeed, cameraFragmentBinding.ivLength, cameraFragmentBinding.ivTimer,
                        cameraFragmentBinding.ivScale, cameraFragmentBinding.ivCollab);
//                    llTimeSelection.setVisibility(View.INVISIBLE);
                cameraFragmentBinding.llSpeedDetails.setVisibility(View.VISIBLE);
                cameraFragmentBinding. llScaleDetails.setVisibility(View.GONE);
                cameraFragmentBinding.llTimerDetails.setVisibility(View.GONE);
                cameraFragmentBinding. llBottomMiddle.setVisibility(View.VISIBLE);
                cameraFragmentBinding.llBottomCountDown.setVisibility(View.GONE);
                cameraFragmentBinding.rlRecord.setVisibility(View.VISIBLE);
            } else {
                if (!context.isRetake) {
                    sameClicked = true;
                }
                setNormal();
            }
                /*case R.id.llLength:
                int duration = 0;
                for (int i = 0; i < context.videoList.size(); i++) {
                    VideoModel video = context.videoList.get(i);
                    duration += (int) (video.actualDuration * 1000);
                    stepProgressView.getMarkers().add(duration);
                }
                setTextColor(tvLength, tvSpeed, tvTimer, tvScale, tvCollab);
                setImageColor(ivLength, ivSpeed, ivTimer, ivScale, ivCollab);
                double progress = duration / 1000f;
                llTimeSelection.setVisibility(View.VISIBLE);
                if (progress >= 0 && progress < 6) {
                    tvTimer6Sec.setVisibility(View.VISIBLE);
                    tvTimer15Sec.setVisibility(View.VISIBLE);
                    tvTimer60Sec.setVisibility(View.VISIBLE);
                } else if (progress >= 6 && progress < 15) {
                    tvTimer6Sec.setVisibility(View.GONE);
                    tvTimer15Sec.setVisibility(View.VISIBLE);
                    tvTimer60Sec.setVisibility(View.VISIBLE);
                } else {
                    tvTimer15Sec.setVisibility(View.GONE);
                    tvTimer6Sec.setVisibility(View.GONE);
                    tvTimer60Sec.setVisibility(View.VISIBLE);
                }

                rlRecord.setVisibility(View.VISIBLE);
                llScaleDetails.setVisibility(View.GONE);
                llSpeedDetails.setVisibility(View.GONE);
                llTimerDetails.setVisibility(View.GONE);
                llBottomMiddle.setVisibility(View.VISIBLE);
                llBottomCountDown.setVisibility(View.GONE);
                break;*/
        } else if (id == R.id.llCollab) {
        } else if (id == R.id.tvScale1X) {
            setTextColor(cameraFragmentBinding.tvScale1X, cameraFragmentBinding.tvScale2X, cameraFragmentBinding.tvScale4X);
            currentVideoScale = 1.0f;
            setZoom(1.0f);
        } else if (id == R.id.tvScale2X) {
            setTextColor(cameraFragmentBinding.tvScale2X, cameraFragmentBinding.tvScale1X, cameraFragmentBinding.tvScale4X);
            currentVideoScale = 2.0f;
            setZoom(2.0f);
        } else if (id == R.id.tvScale4X) {
            setTextColor(cameraFragmentBinding.tvScale4X, cameraFragmentBinding.tvScale1X, cameraFragmentBinding.tvScale2X);
            currentVideoScale = 4.0f;
            setZoom(4.0f);
        } else if (id == R.id.llAddPlusContact) {
            if (context.videoOptions == CameraNewActivity.VideoOptions.RECORD_FOR_OTHER || context.privateVideoCreateFrom == CameraNewActivity.PrivateVideoCreateFrom.FROM_PROFILE) {
                return;
            }
            if (context.privateVideoCreateFrom == CameraNewActivity.PrivateVideoCreateFrom.FROM_VIEW_GROUP) {
                context.finish();
                context.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            } else {
                if (context.privateVideoSelection == CameraNewActivity.PrivateVideoSelection.DIALOG) {
                    //checkForSendToDialog(false);
                } else if (context.privateVideoSelection == CameraNewActivity.PrivateVideoSelection.CONTACT_LIST) {
                    //goToContactsSync(false);
                }
            }
        } else if (id == R.id.llSendToLayout) {
            if (context.videoOptions == CameraNewActivity.VideoOptions.RECORD_FOR_OTHER || context.privateVideoCreateFrom == CameraNewActivity.PrivateVideoCreateFrom.FROM_PROFILE) {
                return;
            }
            if (context.privateVideoCreateFrom == CameraNewActivity.PrivateVideoCreateFrom.FROM_VIEW_GROUP) {
                context.finish();
                context.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            } else {
                if (context.privateVideoSelection == CameraNewActivity.PrivateVideoSelection.DIALOG) {
                    //checkForSendToDialog(false);
                } else if (context.privateVideoSelection == CameraNewActivity.PrivateVideoSelection.CONTACT_LIST) {
                   // goToContactsSync(false);
                }
            }
        } else if (id == R.id.ivAddContact) {
            if (context.videoOptions == CameraNewActivity.VideoOptions.DIRECT) {
                selectDirectVideo(false);
                //sendSegmentLogs(Constants.CAMERA_DM_ADD_PARTICIPANTS_CLICKED, new Properties());
            } else if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
                //inviteCoHosts();
                //sendSegmentLogs(Constants.CAMERA_RT_ADD_PARTICIPANTS_CLICKED, new Properties());
            }
        } else if (id == R.id.llQuestionSelect) {
            //sendSegmentLogs(Constants.SUGGESTED_QUESTION_CLICKED, new Properties());
            cameraFragmentBinding.questionBlurLayout.setVisibility(View.VISIBLE);
            showQuestion();

            cameraFragmentBinding.questionBlurLayout.setVisibility(View.VISIBLE);
            showQuestion();
        } else if (id == R.id.cameraQuestionView) {
            cameraFragmentBinding.questionBlurLayout.setVisibility(View.VISIBLE);
            showQuestion();
        } else if (id == R.id.llQuestion) {
            if (context.videoList.size() == 0) {
                cameraFragmentBinding.questionBlurLayout.setVisibility(View.VISIBLE);
                showQuestion();
                showToolTip();
            } else {
                showQuestionRemoveAlert();
            }
        } else if (id == R.id.ivDeleteQuestion) {
            deleteQuestion();
            questionNormalFromBlur();
        } else if (id == R.id.llShareQuestion) {
            shareQuestion();
        } else if (id == R.id.ivBackNew) {
            Utility.hideKeyboard(context, null);
            String stateBack;
            if (TextUtils.isEmpty(cameraFragmentBinding.questionView.questionViewBindingCustom.edtAddQuestion.getText().toString().trim())) {
                stateBack = "empty";
            } else {
                stateBack = "filled";
            }
            handleQuestionBack();
        } else if (id == R.id.tvDone) {
            Utility.hideKeyboard(context, null);
            cameraFragmentBinding.questionView.questionViewBindingCustom.edtAddQuestion.clearComposingText();
            if (isCustomQuestionChanged()) {
//                try {
//                    String text = edtAddQuestion.getText().toString().trim();
//                    if (!TextUtils.isEmpty(text)) {
//                        Properties propertiesDone = new Properties();
//                        propertiesDone.put(Constants.KEY_QUESTION_STRING, text);
//                        propertiesDone.put(Constants.KEY_STRING_LENGTH, text.length());
//                        sendSegmentLogs(Constants.QNA_QUESTION_SUBMITTED, propertiesDone);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                callAddUpdateQuestionAPI();
            }
        } else if (id == R.id.llAddEditQuestion) {
            //sendSegmentLogs(Constants.QNA_ADD_A_QUESTION_CLICKED, new Properties());
            generateTempQuestionModel();
            openViewWithAnimation();
        } else if (id == R.id.llTopic) {
            if (context.videoList.size() == 0) {
                showTopicDialog();
            } else {
                showTopicRemoveAlert();
            }
        }
    }

    private void handleQuestionBack() {
        if (isCustomQuestionChanged()) {
            showBackConfirmAlert();
        } else {
            performSlideDownAnimation();
            if (isZeroQuestions) {
                resetCustomQuestionParams();
                cameraFragmentBinding.rlAddCustomQuestion.setAlpha(0);
                cameraFragmentBinding.llNoQuestions.setVisibility(View.VISIBLE);
                cameraFragmentBinding.rlAddCustomQuestion.setVisibility(View.GONE);
            }
        }
    }

    private void showBackConfirmAlert() {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.common_simple_dialog_new);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        CustomTextView tvTitle = dialog.findViewById(R.id.dialog_title);
        CustomTextView tvMsg = dialog.findViewById(R.id.dialog_message);
        CustomTextView btnCancel = dialog.findViewById(R.id.dialog_btn_cancel);
        CustomTextView btnYes = dialog.findViewById(R.id.dialog_btn_yes);

        btnYes.setText(getResources().getString(R.string.btn_discard));
        tvTitle.setText(getResources().getString(R.string.edit_discard_title));
        tvMsg.setText(getResources().getString(R.string.edit_discard_msg));
        tvTitle.setVisibility(View.VISIBLE);
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            //sendSegmentLogs(Constants.QNA_BACK_DIALOG_CANCEL_CLICKED, new Properties());
        });
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            try {
                String text = cameraFragmentBinding.questionView.questionViewBindingCustom.edtAddQuestion.getText().toString().trim();
//                if (!TextUtils.isEmpty(text)) {
//                    Properties properties = new Properties();
//                    properties.put(Constants.KEY_QUESTION_STRING, text);
//                    properties.put(Constants.KEY_STRING_LENGTH, text.length());
//                    sendSegmentLogs(Constants.QNA_BACK_DIALOG_DISCARD_CLICKED, properties);
//                }
                if (isZeroQuestions) {
                    resetCustomQuestionParams();
                    cameraFragmentBinding.rlAddCustomQuestion.setAlpha(0);
                    cameraFragmentBinding.llNoQuestions.setVisibility(View.VISIBLE);
                    cameraFragmentBinding.rlAddCustomQuestion.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            performSlideDownAnimation();
        });
    }

    private void showQuestionRemoveAlert() {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.common_simple_dialog_new);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        CustomTextView tvTitle = dialog.findViewById(R.id.dialog_title);
        CustomTextView tvMsg = dialog.findViewById(R.id.dialog_message);
        CustomTextView btnCancel = dialog.findViewById(R.id.dialog_btn_cancel);
        CustomTextView btnYes = dialog.findViewById(R.id.dialog_btn_yes);

        btnYes.setText(getResources().getString(R.string.btn_remove));
        tvTitle.setText(getResources().getString(R.string.question_discard_title));
        tvMsg.setText(getResources().getString(R.string.question_discard_msg));
        tvTitle.setVisibility(View.VISIBLE);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            deleteQuestion();
        });
    }

    private void showTopicRemoveAlert() {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.common_simple_dialog_new);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        CustomTextView tvTitle = dialog.findViewById(R.id.dialog_title);
        CustomTextView tvMsg = dialog.findViewById(R.id.dialog_message);
        CustomTextView btnCancel = dialog.findViewById(R.id.dialog_btn_cancel);
        CustomTextView btnYes = dialog.findViewById(R.id.dialog_btn_yes);

        btnYes.setText(getResources().getString(R.string.btn_remove));
        tvTitle.setText(getResources().getString(R.string.topic_discard_title));
        tvMsg.setText(getResources().getString(R.string.topic_discard_msg));
        tvTitle.setVisibility(View.VISIBLE);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            deleteTopic();
        });
    }

    private void shareQuestion() {
        String shareURL = "";
        String shareQuestionId = "";
        if (isLoopQnA) {
            if (QuestionViewModel.getInstance().loopQuestionsArr.size() == 0) {
                return;
            }
            shareURL = loopSelectedQuestion.getShareURL();
            shareQuestionId = loopSelectedQuestion.getQuestionId();
        } else {
            if (QuestionViewModel.getInstance().masterQuestionsArr.size() == 0) {
                return;
            }
            shareURL = QuestionViewModel.getInstance().masterQuestionsArr.get(0).getShareURL();
            shareQuestionId = QuestionViewModel.getInstance().masterQuestionsArr.get(0).getQuestionId();
        }
//        String shareURL = QuestionViewModel.getInstance().masterQuestionsArr.get(shufflePos).getShareURL();
        //Utility.shareVideoLink(context, shareURL, shareQuestionId, Constants.FROM_QUESTION);
        //sendSegmentLogs(Constants.QNA_SHARED, new Properties());
    }

    public void deleteQuestion() {
        context.selectedQuestion = null;
        cameraFragmentBinding.ivQuestion.setImageResource(R.drawable.ic_question_box);
        cameraFragmentBinding.ivQuestion.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
        cameraFragmentBinding.tvLabelQuestion.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
        if (context.videoList.size() == 0) {
            cameraFragmentBinding.llQuestionSelect.setVisibility(View.VISIBLE);
        } else {
            cameraFragmentBinding.llQuestion.setVisibility(View.GONE);
        }
        cameraFragmentBinding.llCameraQuestionView.setVisibility(View.GONE);
        //sendSegmentLogs(Constants.QNA_QUESTION_REMOVED, new Properties());
    }

    public void deleteTopic() {
        context.selectedTopic = null;
        cameraFragmentBinding.ivTopic.setImageResource(R.drawable.ic_topic);
        cameraFragmentBinding.tvLabelTopic.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
        if (context.videoList.size() > 0) {
            cameraFragmentBinding.llTopic.setVisibility(View.GONE);
        }
        //sendSegmentLogs(Constants.TOPIC_REMOVED, new Properties());
    }

    private void showToolTip() {
        if (cameraFragmentBinding.flRecordAnswer.getVisibility() == View.GONE) {
            return;
        }
        customTooltip = new SimpleTooltip.Builder(context)
                .anchorView(cameraFragmentBinding.flRecordAnswer)
                .text(context.getResources().getString(R.string.record_an_answer))
                .gravity(Gravity.START)
                .animated(false)
                .dismissOnOutsideTouch(true)
                .dismissOnInsideTouch(true)
                .arrowHeight(Utility.dpToPx(8, context))
                .arrowWidth(Utility.dpToPx(10, context))
                .ignoreOverlay(true)
                .build();
        customTooltip.show();
        if (answerHandler != null) {
            answerHandler.removeCallbacks(null);
        }
        answerHandler = new Handler(Looper.getMainLooper());
        answerHandler.postDelayed(() -> {
            if (isAdded()) {
                if (customTooltip.isShowing()) {
                    customTooltip.dismiss();
                }
            }
        }, 4000);
    }

    private void setUpLoopQnALayout() {
        cameraFragmentBinding.questionView.updateTextViewAttrs(loopSelectedQuestion, Constants.QUESTION_FONT_MAX_DEFAULT_SIZE, Constants.QUESTION_FONT_MAX_DEFAULT_SIZE);
        if (loopSelectedQuestion.getOwner() != null && loopSelectedQuestion.getOwner().getUserId().equalsIgnoreCase(context.userId)) {
            cameraFragmentBinding.questionView.questionViewBinding.ivEditQuestion.setVisibility(View.VISIBLE);
        } else {
            cameraFragmentBinding.questionView.questionViewBinding.ivEditQuestion.setVisibility(View.GONE);
        }
        if (cameraFragmentBinding.flRecordAnswer.getVisibility() == View.GONE) {
            cameraFragmentBinding.flRecordAnswer.setVisibility(View.VISIBLE);
            showToolTip();
        }
        cameraFragmentBinding.tvAddEditQuestion.setText(context.getResources().getString(R.string.add_a_question));
        cameraFragmentBinding.ivAddEditQuestion.setImageResource(R.drawable.ic_question_box);
        cameraFragmentBinding.llShuffleQuestion.setVisibility(View.VISIBLE);
        cameraFragmentBinding.llAddEditQuestion.setVisibility(View.VISIBLE);
        cameraFragmentBinding.flRecordAnswer.setVisibility(View.VISIBLE);
        if (context.selectedQuestion != null) {
            cameraFragmentBinding.tvQuestionHeader.setText(context.getResources().getString(R.string.change_question));
            cameraFragmentBinding.ivDeleteQuestion.setVisibility(View.VISIBLE);
        } else {
            cameraFragmentBinding.tvQuestionHeader.setText(context.getResources().getString(R.string.answer_a_quesiton));
            cameraFragmentBinding.ivDeleteQuestion.setVisibility(View.GONE);
        }
    }

    private void showQuestion() {
        setPullerIsDragEnable(false);

        if (context.videoOptions == CameraNewActivity.VideoOptions.PUBLIC) {
            cameraFragmentBinding.tvBlurBottomText.setText(context.getResources().getString(R.string.public_video));
        } else if (context.videoOptions == CameraNewActivity.VideoOptions.DIRECT || context.videoOptions == CameraNewActivity.VideoOptions.GROUP) {
            cameraFragmentBinding.tvBlurBottomText.setText(context.getResources().getString(R.string.direct_video));
        } else if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE) {
            cameraFragmentBinding.tvBlurBottomText.setText(context.getResources().getString(R.string.roundtable_video));
        } else if (context.videoOptions == CameraNewActivity.VideoOptions.COMMENT) {
            cameraFragmentBinding.tvBlurBottomText.setText(context.getResources().getString(R.string.comment));
        } else if (context.convType == VideoConvType.ROUND_TABLE.getValue()) {
            cameraFragmentBinding.tvBlurBottomText.setText(context.getResources().getString(R.string.post));
        } else {
            cameraFragmentBinding.tvBlurBottomText.setText(context.getResources().getString(R.string.reply));
        }

        if (isLoopQnA && loopSelectedQuestion == null) {
            cameraFragmentBinding.llQuestionLayout.setVisibility(View.INVISIBLE);
            QuestionViewModel.getInstance().syncLoopQuestions(context, context.chatId);
            BaseAPIService.showProgressDialog(context);
            cameraFragmentBinding.flRecordAnswer.setVisibility(View.GONE);
            return;
        } else if (!isLoopQnA) {
            if (QuestionViewModel.getInstance().status == QuestionViewModel.QuestionsSyncStatus.IN_PROGRESS) {
                cameraFragmentBinding.llQuestionLayout.setVisibility(View.INVISIBLE);
                BaseAPIService.showProgressDialog(context);
                return;
            } else if (QuestionViewModel.getInstance().status == QuestionViewModel.QuestionsSyncStatus.NONE || QuestionViewModel.getInstance().status == QuestionViewModel.QuestionsSyncStatus.FAILED) {
                cameraFragmentBinding.llQuestionLayout.setVisibility(View.INVISIBLE);
                BaseAPIService.showProgressDialog(context);
                QuestionViewModel.getInstance().syncQuestions(context, true);
                cameraFragmentBinding.flRecordAnswer.setVisibility(View.GONE);
                return;
            }
        }

        if (loopSelectedQuestion != null && isLoopQnA) {
            setUpLoopQnALayout();
            return;
        }

        if (context.selectedQuestion != null) {
//            shufflePos = QuestionViewModel.getInstance().masterQuestionsArr.indexOf(context.selectedQuestion);
//            prevShufflePos = shufflePos;
            if (cameraFragmentBinding.flRecordAnswer.getVisibility() == View.GONE) {
                cameraFragmentBinding.flRecordAnswer.setVisibility(View.VISIBLE);
                showToolTip();
            }
            cameraFragmentBinding.questionView.updateTextViewAttrs(context.selectedQuestion, Constants.QUESTION_FONT_MAX_DEFAULT_SIZE, Constants.QUESTION_FONT_MAX_DEFAULT_SIZE);
            if (context.selectedQuestion.getOwner() != null && context.selectedQuestion.getOwner().getUserId().equalsIgnoreCase(context.userId)) {
                cameraFragmentBinding.questionView.questionViewBinding.ivEditQuestion.setVisibility(View.VISIBLE);
            } else {
                cameraFragmentBinding.questionView.questionViewBinding.ivEditQuestion.setVisibility(View.GONE);
            }
            cameraFragmentBinding.tvAddEditQuestion.setText(context.getResources().getString(R.string.add_a_question));
            cameraFragmentBinding.ivAddEditQuestion.setImageResource(R.drawable.ic_question_box);
            cameraFragmentBinding.ivDeleteQuestion.setVisibility(View.VISIBLE);
            if (context.videoList.size() > 0) {
                cameraFragmentBinding.llShuffleQuestion.setVisibility(View.GONE);
                cameraFragmentBinding.flRecordAnswer.setVisibility(View.GONE);
                cameraFragmentBinding.llAddEditQuestion.setVisibility(View.GONE);
                cameraFragmentBinding.tvQuestionHeader.setText(context.getResources().getString(R.string.remove_question));
            } else {
                cameraFragmentBinding.llShuffleQuestion.setVisibility(View.VISIBLE);
                //if (Utility.isLoggedIn(context)) {
                cameraFragmentBinding.llAddEditQuestion.setVisibility(View.VISIBLE);
                //}
                cameraFragmentBinding.flRecordAnswer.setVisibility(View.VISIBLE);
                cameraFragmentBinding.tvQuestionHeader.setText(context.getResources().getString(R.string.change_question));
            }
        } else {
            cameraFragmentBinding.ivDeleteQuestion.setVisibility(View.INVISIBLE);
            cameraFragmentBinding.llShuffleQuestion.setVisibility(View.VISIBLE);
           // if (Utility.isLoggedIn(context)) {
            cameraFragmentBinding.llAddEditQuestion.setVisibility(View.VISIBLE);
           // }
            cameraFragmentBinding.flRecordAnswer.setVisibility(View.VISIBLE);
            cameraFragmentBinding.tvQuestionHeader.setText(context.getResources().getString(R.string.answer_a_quesiton));
            shuffleQuestion();
        }
    }

    private void shuffleLoopQuestion() {
        if (QuestionViewModel.getInstance().loopQuestionsArr.size() == 1) {
            cameraFragmentBinding.llShuffleQuestion.setAlpha(0.5f);
            cameraFragmentBinding.llShuffleQuestion.setEnabled(false);
        }
        if (QuestionViewModel.getInstance().shouldNextLoopApiCall()) {
            QuestionViewModel.getInstance().syncLoopQuestions(context, context.chatId);
        }
        loopSelectedQuestion = QuestionViewModel.getInstance().goNextLoopQuestion();
        if (loopSelectedQuestion != null) {
            showQuestion();
        }
    }

    private void shuffleQuestion() {
        if (QuestionViewModel.getInstance().masterQuestionsArr.size() == 0) {
            return;
        }
        resetCustomQuestionParams();

        if (!TextUtils.isEmpty(context.questionRedirectId)) {
            context.questionRedirectId = "";
            QuestionModel questionRedirectModel = QuestionViewModel.getInstance().masterQuestionsArr.get(0);
            if (questionRedirectModel != null) {
                isQuestionChanged = true;
                cameraFragmentBinding.questionView.updateTextViewAttrs(questionRedirectModel, Constants.QUESTION_FONT_MAX_DEFAULT_SIZE, Constants.QUESTION_FONT_MAX_DEFAULT_SIZE);
                if (questionRedirectModel.getOwner() != null && questionRedirectModel.getOwner().getUserId().equalsIgnoreCase(context.userId)) {
                    cameraFragmentBinding.questionView.questionViewBinding.ivEditQuestion.setVisibility(View.VISIBLE);
                } else {
                    cameraFragmentBinding.questionView.questionViewBinding.ivEditQuestion.setVisibility(View.GONE);
                }
                cameraFragmentBinding.tvAddEditQuestion.setText(context.getResources().getString(R.string.add_a_question));
                cameraFragmentBinding.ivAddEditQuestion.setImageResource(R.drawable.ic_question_box);
            }
        } else {
            if (QuestionViewModel.getInstance().masterQuestionsArr.size() == 1) {
                cameraFragmentBinding.llShuffleQuestion.setAlpha(0.5f);
                cameraFragmentBinding.llShuffleQuestion.setEnabled(false);
            }
            if (QuestionViewModel.getInstance().shouldNextApiCall()) {
                QuestionViewModel.getInstance().syncQuestions(context, false);
            }

            QuestionModel questionModel = QuestionViewModel.getInstance().getNextQuestion();
            if (questionModel != null) {
                isQuestionChanged = true;
                cameraFragmentBinding.questionView.updateTextViewAttrs(questionModel, Constants.QUESTION_FONT_MAX_DEFAULT_SIZE, Constants.QUESTION_FONT_MAX_DEFAULT_SIZE);
                if (questionModel.getOwner() != null && questionModel.getOwner().getUserId().equalsIgnoreCase(context.userId)) {
                    cameraFragmentBinding.questionView.questionViewBinding.ivEditQuestion.setVisibility(View.VISIBLE);
                } else {
                    cameraFragmentBinding.questionView.questionViewBinding.ivEditQuestion.setVisibility(View.GONE);
                }
                cameraFragmentBinding.tvAddEditQuestion.setText(context.getResources().getString(R.string.add_a_question));
                cameraFragmentBinding.ivAddEditQuestion.setImageResource(R.drawable.ic_question_box);
            }
        }
    }

    private void questionNormalFromBlur() {
        if (answerHandler != null) {
            answerHandler.removeCallbacks(null);
        }
        if (customTooltip != null && customTooltip.isShowing()) {
            customTooltip.dismiss();
        }
        cameraFragmentBinding.questionBlurLayout.setVisibility(View.GONE);
        cameraFragmentBinding.llNoQuestions.setVisibility(View.GONE);
        if (context.videoList.size() == 0) {
            setPullerIsDragEnable(true);
            if (context.isQnA) {
                showPublicVideoTutorial();
            }
        }
    }

    public boolean isContactPermissionAllowed() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestContactPermission() {
        if (!isContactPermissionAllowed()) {
            mPermissionResult.launch(Manifest.permission.READ_CONTACTS);
        } else {
            //checkForSendToDialog(false);
        }
    }

    public String getMimeType(Uri uri) {
        String mimeType;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

//    public void goToContactsSync(boolean isNewGroup) {
//        Intent intent = new Intent(context, ContactsSyncActivity.class);
//        if (context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE || context.privateVideoSelection == CameraNewActivity.PrivateVideoSelection.CONTACT_LIST) {
//            if (context.selectedContacts.size() > 0) {
//                intent.putExtra("selected_contacts", context.selectedContacts);
//            }
//        }
//        intent.putExtra("isCreateRoundTable", context.videoOptions == CameraNewActivity.VideoOptions.ROUND_TABLE);
//        intent.putExtra("isNewGroup", isNewGroup);
//        contactActivityResultLauncher.launch(intent);
//        context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//    }

    private void startLongPressCountDownTimer() {
        manageViews(View.GONE);
        context.isLongPress = false;
        cameraFragmentBinding.frmCameraRecord.setVisibility(View.INVISIBLE);
        cameraFragmentBinding.ivCloseRecord.setVisibility(View.INVISIBLE);
        cameraFragmentBinding.tvTimerText.setVisibility(View.VISIBLE);
        cameraFragmentBinding.tvTimerText.setText("3");
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(3000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                int timerValue = (int) ((millisUntilFinished) / 1000);
                cameraFragmentBinding.tvTimerText.setText(String.format(Locale.ENGLISH, "%d", timerValue + 1));
            }

            @Override
            public void onFinish() {
                cameraFragmentBinding.tvTimerText.setVisibility(View.GONE);
                if (!isHidden()) {
                    cameraFragmentBinding.frmCameraRecord.setVisibility(View.VISIBLE);
                    cameraFragmentBinding.ivCloseRecord.setVisibility(View.VISIBLE);
                    onCameraControlClick();
                }
            }
        };
        countDownTimer.start();
    }

    private void startCountDownTimer() {
        cameraFragmentBinding.frmCameraRecord.setVisibility(View.INVISIBLE);
        cameraFragmentBinding.ivCloseRecord.setVisibility(View.INVISIBLE);
        cameraFragmentBinding.tvTimerText.setText("");
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        String seconds = COUNT_DOWN_TIME == 3000 ? "3s" : "10s";
        String seconds_text = cameraFragmentBinding.seekProgressValue.getText().toString();
        countDownTimer = new CountDownTimer(COUNT_DOWN_TIME, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                if (mCameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    setFlashBlink();
                }
                int timerValue = (int) ((millisUntilFinished) / 1000);
                cameraFragmentBinding.tvTimerText.setText(String.format(Locale.ENGLISH, "%d", timerValue + 1));
            }

            @Override
            public void onFinish() {
                cameraFragmentBinding.tvTimerText.setVisibility(View.GONE);
                if (!isHidden()) {
                    if (isFlashOn) {
                        setFlashOn();
                    }
                    cameraFragmentBinding.stepProgressView.getMarkers().add((int) (ACTUAL_MAX_TIME - MAX_TIME + timerDuration));
                    cameraFragmentBinding.frmCameraRecord.setVisibility(View.VISIBLE);
                    cameraFragmentBinding.ivCloseRecord.setVisibility(View.VISIBLE);
                    mLastClickTime = SystemClock.elapsedRealtime();
                    startTimerRecording();
                }
            }
        };
        countDownTimer.start();
    }

    private void setNormal() {
        if (isViewsDisable) {
            setImageColorGray(cameraFragmentBinding.ivLength, cameraFragmentBinding.ivScale, cameraFragmentBinding.ivTimer,
                    cameraFragmentBinding.ivSpeed, cameraFragmentBinding.ivCollab);
            setTextColorGray(cameraFragmentBinding.tvLength, cameraFragmentBinding.tvSpeed, cameraFragmentBinding.tvTimer,
                    cameraFragmentBinding.tvScale, cameraFragmentBinding.tvCollab);
        } else {
            setImageColor(cameraFragmentBinding.ivLength, cameraFragmentBinding.ivScale, cameraFragmentBinding.ivTimer, cameraFragmentBinding.ivSpeed,
                    cameraFragmentBinding.ivCollab);
            setTextColor(cameraFragmentBinding.tvLength, cameraFragmentBinding.tvSpeed, cameraFragmentBinding.tvTimer,
                    cameraFragmentBinding.tvScale, cameraFragmentBinding.tvCollab);
        }

        cameraFragmentBinding. llTimerDetails.setVisibility(View.GONE);
        cameraFragmentBinding.llScaleDetails.setVisibility(View.GONE);
        cameraFragmentBinding.llSpeedDetails.setVisibility(View.GONE);
        cameraFragmentBinding.llBottomMiddle.setVisibility(View.VISIBLE);
        cameraFragmentBinding.llBottomCountDown.setVisibility(View.GONE);
        cameraFragmentBinding.rlRecord.setVisibility(View.VISIBLE);
        if (sameClicked) {
            int duration = 0;
            for (int i = 0; i < context.videoList.size(); i++) {
                VideoModel video = context.videoList.get(i);
                duration += (int) (video.actualDuration * 1000);
            }
            double progress = duration / 1000f;
            if (progress >= 0 && progress < 6) {
                cameraFragmentBinding.tvTimer6Sec.setVisibility(View.VISIBLE);
                cameraFragmentBinding.tvTimer15Sec.setVisibility(View.VISIBLE);
                cameraFragmentBinding.tvTimer60Sec.setVisibility(View.VISIBLE);
            } else if (progress > 6 && progress <= 15) {
                cameraFragmentBinding.tvTimer6Sec.setVisibility(View.GONE);
                cameraFragmentBinding.tvTimer15Sec.setVisibility(View.VISIBLE);
                cameraFragmentBinding.tvTimer60Sec.setVisibility(View.VISIBLE);
            } else {
                cameraFragmentBinding.tvTimer6Sec.setVisibility(View.GONE);
                cameraFragmentBinding.tvTimer15Sec.setVisibility(View.GONE);
                cameraFragmentBinding.tvTimer60Sec.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setImageColor(ImageView iv1) {
        iv1.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
    }

    private void setImageColor(ImageView iv1, ImageView iv2, ImageView iv3, ImageView iv4, ImageView iv5) {
        iv1.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
        iv2.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
        iv3.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
        iv4.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
        iv5.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
    }

    private void setImageColorGray(ImageView iv1, ImageView iv2, ImageView iv3, ImageView iv4, ImageView iv5) {
        iv1.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
        iv2.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
        iv3.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
        iv4.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
        iv5.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
    }

    private void setImageColorGray(ImageView iv1, ImageView iv2, ImageView iv3) {
        iv1.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
        iv2.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
        iv3.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white_opacity40, null)));
    }

    private void setImageColor(ImageView iv1, ImageView iv2, ImageView iv3) {
        iv1.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
        iv2.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
        iv3.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
    }

    private void setTextColor(CustomTextView tv1, CustomTextView tv2, CustomTextView tv3) {
        tv1.setTextColor(context.getResources().getColor(R.color.colorWhite, null));
        tv2.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
        tv3.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
    }

    private void setTextColorGray(CustomTextView tv1, CustomTextView tv2, CustomTextView tv3) {
        tv1.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
        tv2.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
        tv3.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
    }

    private void setTextColor(CustomTextView tv1, CustomTextView tv2) {
        tv1.setTextColor(context.getResources().getColor(R.color.colorWhite, null));
        tv2.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
    }

    private void setTextColor(CustomTextView tv1) {
        tv1.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
    }

    private void setTextColor(CustomTextView tv1, CustomTextView tv2, CustomTextView tv3, CustomTextView tv4, CustomTextView tv5) {
        tv1.setTextColor(context.getResources().getColor(R.color.colorWhite, null));
        tv2.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
        tv3.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
        tv4.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
        tv5.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
    }

    private void setTextColorGray(CustomTextView tv1, CustomTextView tv2, CustomTextView tv3, CustomTextView tv4, CustomTextView tv5) {
        tv1.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
        tv2.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
        tv3.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
        tv4.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
        tv5.setTextColor(context.getResources().getColor(R.color.white_opacity40, null));
    }

    private void showDeleteClipAlert() {
        mDeleteClipDialog = new Dialog(context);
        mDeleteClipDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDeleteClipDialog.setContentView(R.layout.common_simple_dialog_new);
        mDeleteClipDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDeleteClipDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mDeleteClipDialog.show();

        CustomTextView tvTitle = mDeleteClipDialog.findViewById(R.id.dialog_title);
        CustomTextView tvMsg = mDeleteClipDialog.findViewById(R.id.dialog_message);
        CustomTextView btnCancel = mDeleteClipDialog.findViewById(R.id.dialog_btn_cancel);
        CustomTextView btnYes = mDeleteClipDialog.findViewById(R.id.dialog_btn_yes);

        btnYes.setText(getResources().getString(R.string.txt_delete));
        tvTitle.setText(getResources().getString(R.string.delete_last_clip));
        tvTitle.setVisibility(View.VISIBLE);
        tvMsg.setText(getResources().getString(R.string.txt_delete_clip_sub));
        btnCancel.setOnClickListener(v -> {
            mDeleteClipDialog.dismiss();
            //sendSegmentLogs(Constants.LAST_CLIP_DELETE_CANCELED, new Properties());
        });
        btnYes.setOnClickListener(v -> {
            mDeleteClipDialog.dismiss();
            deleteLastClip(true);
        });
    }

    private void retakeVideoFromCamera() {
        VideoModel video = context.videoList.get(context.videoList.size() - 1);
        int actualDuration = (int) (video.actualDuration * 1000);
        cameraFragmentBinding.llScaleDetails.setVisibility(View.GONE);
        currentVideoScale = 1.0f;
        zoomSelect = false;
        setImageColorGray(cameraFragmentBinding.ivLength,cameraFragmentBinding.ivScale, cameraFragmentBinding.ivTimer,
                cameraFragmentBinding.ivSpeed, cameraFragmentBinding.ivCollab);
        setTextColorGray(cameraFragmentBinding.tvLength, cameraFragmentBinding.tvSpeed, cameraFragmentBinding.tvTimer,
                cameraFragmentBinding.tvScale, cameraFragmentBinding.tvCollab);
        context.isRetake = true;
        retakeDuration = actualDuration;
        isRetakeFromCamera = true;
        CURRENT_PROGRESS = 0;
        cameraFragmentBinding.stepProgressView.setTotalProgress(actualDuration);
        cameraFragmentBinding.stepProgressView.setCurrentProgress(0);
        cameraFragmentBinding.stepProgressView.getMarkers().clear();
        cameraFragmentBinding.tvRecordTimer.setText("");
        manageViews(View.GONE);
        cameraFragmentBinding.llBottomLayoutCamera.setVisibility(View.VISIBLE);
        cameraFragmentBinding.llBottomMiddle.setVisibility(View.VISIBLE);
        if (!context.isReplyReactionWithoutVideo()) {
            cameraFragmentBinding.imgSwitchFlash.setVisibility(View.VISIBLE);
        }
        cameraFragmentBinding.frameCameraSwitch.setVisibility(View.VISIBLE);
//            llLength.setVisibility(View.GONE);
        cameraFragmentBinding.llVideoEditingOptions.setVisibility(View.VISIBLE);
        cameraFragmentBinding.llGallery.setVisibility(View.INVISIBLE);
        centerTextManage();
       // sendSegmentLogs(Constants.RETAKE_INITIATED, new Properties());
    }

    private void deleteLastClip(boolean isCloseCamera) {
        VideoModel video = context.videoList.get(context.videoList.size() - 1);
        context.videoList.remove(video);
        cameraFragmentBinding.llScaleDetails.setVisibility(View.GONE);
        currentVideoScale = 1.0f;
        zoomSelect = false;
        setImageColorGray(cameraFragmentBinding.ivLength, cameraFragmentBinding.ivScale,cameraFragmentBinding.ivTimer,
                cameraFragmentBinding.ivSpeed, cameraFragmentBinding.ivCollab);
        setTextColorGray(cameraFragmentBinding.tvLength, cameraFragmentBinding.tvSpeed, cameraFragmentBinding.tvTimer,
                cameraFragmentBinding.tvScale, cameraFragmentBinding.tvCollab);
        //sendSegmentLogs(Constants.LAST_CLIP_DELETED, new Properties());
        if (context.videoList.size() == 0) {
            context.isRecordFinishedLogged = false;
            context.isFullTrim = false;
            cameraFragmentBinding.stepProgressView.getMarkers().clear();
            cameraFragmentBinding.stepProgressView.setCurrentProgress(0);
            cameraFragmentBinding.tvRecordTimer.setText("");
            CURRENT_PROGRESS = 0;
            MAX_TIME = ACTUAL_MAX_TIME;
            setNormal();
            manageViews(View.GONE);
            setPullerIsDragEnable(true);
            cameraFragmentBinding.llProgressBar.setVisibility(View.GONE);
        } else {
            if (isLastVideoFromGallery()) {
                cameraFragmentBinding.llRetake.setVisibility(View.INVISIBLE);
            } else {
                cameraFragmentBinding.llRetake.setVisibility(View.VISIBLE);
            }
            setProgressAndVideo();
            if (context.isFullTrim) {
                context.isFullTrim = false;
                for (int i = 0; i < context.videoList.size(); i++) {
                    context.videoList.get(i).isFullTrim = false;
                }
            }
        }
//        if (isCloseCamera) {
//            closeCamera();
//        }
//        deleteCurrentFileAndOpenCamera();
        startPreview();
        cameraFragmentBinding.frmCameraRecord.setEnabled(true);
        cameraFragmentBinding.frmCameraRecord.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
    }

    public void backManage() {
        if (context.isRetake) {
            context.isRetake = false;
            cancelTimer();
            context.getSupportFragmentManager().popBackStack();
            context.mRetakeFragment = null;
            //if (context.videoTrimFragment != null) {
                //context.videoTrimFragment.refreshSectionViewAfterClose();
            //}
            //sendSegmentLogs(Constants.CAMERA_RETAKE_CLOSED, new Properties());
        } else {
            context.onPullComplete();
        }
    }

    private void setUpNoQuestionsLayout() {
        isZeroQuestions = true;
        cameraFragmentBinding.flRecordAnswer.setVisibility(View.GONE);
        cameraFragmentBinding.tvQuestionHeader.setVisibility(View.GONE);
        cameraFragmentBinding.llNoQuestions.setVisibility(View.VISIBLE);
        cameraFragmentBinding.llAddNewQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sendSegmentLogs(Constants.QNA_ADD_A_QUESTION_CLICKED, new Properties());
                generateTempQuestionModel();
                cameraFragmentBinding.llNoQuestions.setVisibility(View.GONE);
                openViewWithAnimation();
                oldQuestionText = "";
                openAddCustomQuestionView();
            }
        });
    }

    @Override
    public void onQuestionsSyncSuccess(ArrayList<QuestionModel> questionsList, boolean isShowQuestion) {
        if (cameraFragmentBinding.questionBlurLayout.getVisibility() == View.GONE) {
            return;
        }
        if (isLoopQnA) {
            if (QuestionViewModel.getInstance().loopQuestionsArr.size() > 0) {
                loopSelectedQuestion = QuestionViewModel.getInstance().loopQuestionsArr.get(0);
                isQuestionChanged = true;
                cameraFragmentBinding.llNoQuestions.setVisibility(View.GONE);
            } else {
                isShowQuestion = false;
                BaseAPIService.dismissProgressDialog();
                setUpNoQuestionsLayout();
            }
            if (QuestionViewModel.getInstance().loopQuestionsArr.size() == 1) {
                cameraFragmentBinding.llShuffleQuestion.setAlpha(0.5f);
                cameraFragmentBinding.llShuffleQuestion.setEnabled(false);
            } else {
                cameraFragmentBinding.llShuffleQuestion.setAlpha(1f);
                cameraFragmentBinding.llShuffleQuestion.setEnabled(true);
            }
            context.group = QuestionViewModel.getInstance().conversation.getGroup();
            setUpTabData();
        }
        if (isShowQuestion) {
            BaseAPIService.dismissProgressDialog();
            cameraFragmentBinding.llQuestionLayout.setVisibility(View.VISIBLE);
            showQuestion();
        }
    }

    @Override
    public void onQuestionsSyncFailure() {
        BaseAPIService.dismissProgressDialog();
    }

    private void openAddCustomQuestionView() {
        boolean isQuestionModelAvailable = !TextUtils.isEmpty(newCustomQuestionModel.getQuestion());
        cameraFragmentBinding.customQuestionView.customAddQuestionView.setCustomQuestionData(newCustomQuestionModel, newCustomQuestionModel.getQuestion().length());
        cameraFragmentBinding.customQuestionView.tvCustomQuestionHeader.setText(isQuestionModelAvailable ? (R.string.edit_question) : (R.string.add_a_question));

        setDoneButtonEnableDisable(isQuestionModelAvailable);

        cameraFragmentBinding.rlQuestion.setVisibility(View.GONE);
        cameraFragmentBinding.rlAddCustomQuestion.setVisibility(View.VISIBLE);
    }

    private void resetCustomQuestionParams() {
        cameraFragmentBinding.questionView.questionViewBindingCustom.edtAddQuestion.setText("");
        cameraFragmentBinding.questionView.questionViewBindingCustom.tvCharCount.setText("0");
        newCustomQuestionModel = null;
    }

    private void callAddUpdateQuestionAPI() {
        if (TextUtils.isEmpty(newCustomQuestionModel.getQuestion())) {
            callAddAPI();
        } else {
            callEditQuestionAPI();
        }
    }

    private void openViewWithAnimation() {
        cameraFragmentBinding.rlAddCustomQuestion.setVisibility(View.VISIBLE);
        float y =  cameraFragmentBinding.llQuestionLayout.getY();
        AnimatorSet animationSet = new AnimatorSet();

        ObjectAnimator moveQuestionLayoutAnim = ObjectAnimator.ofFloat( cameraFragmentBinding.llQuestionLayout, "translationY", 0, -y / 4, -y / 2, -y * 3 / 4, -y); //rlQuestion.getY(),rlQuestion.getY()/2
        moveQuestionLayoutAnim.setDuration(Constants.ANIMATION_DURATION);
        moveQuestionLayoutAnim.setInterpolator(new LinearInterpolator());

        ObjectAnimator alphaQuestionAnim = ObjectAnimator.ofFloat( cameraFragmentBinding.rlQuestion, "alpha", 0.5f, 0.25f, 0, 0);
        alphaQuestionAnim.setDuration(Constants.ANIMATION_DURATION);
        alphaQuestionAnim.setInterpolator(new LinearInterpolator());

        ObjectAnimator moveCustomQuestionAnim = ObjectAnimator.ofFloat( cameraFragmentBinding.customQuestionView.customAddQuestionView, "translationY", y, y * 3 / 4, y / 2, y / 4, 0); //rlQuestion.getY(),rlQuestion.getY()/2
        moveCustomQuestionAnim.setDuration(Constants.ANIMATION_DURATION);
        moveCustomQuestionAnim.setInterpolator(new LinearInterpolator());

        ObjectAnimator alphaCustomQuestionAnim = ObjectAnimator.ofFloat( cameraFragmentBinding.rlAddCustomQuestion, "alpha", 0f, 0.5f, 0.75f, 1f);
        alphaCustomQuestionAnim.setDuration(Constants.ANIMATION_DURATION);
        alphaCustomQuestionAnim.setInterpolator(new LinearInterpolator());

        animationSet.playTogether(moveQuestionLayoutAnim, alphaQuestionAnim, moveCustomQuestionAnim, alphaCustomQuestionAnim);

        animationSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Utility.printErrorLog("onTransitionEnd");
                Utility.printErrorLog("onTransitionEnd for rlAddCustomQuestion");

                cameraFragmentBinding.rlQuestion.setVisibility(View.GONE);

                cameraFragmentBinding.questionView.questionViewBindingCustom.edtAddQuestion.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                openAddCustomQuestionView();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        animationSet.start();
    }

    private void performSlideDownAnimation() {
        cameraFragmentBinding.rlQuestion.setVisibility(View.VISIBLE);
        float y = cameraFragmentBinding.llQuestionLayout.getY();
        if (isLoopQnA) {
            if (QuestionViewModel.getInstance().loopQuestionsArr.size() == 1) {
                cameraFragmentBinding.llShuffleQuestion.setAlpha(0.5f);
                cameraFragmentBinding.llShuffleQuestion.setEnabled(false);
            } else {
                cameraFragmentBinding.llShuffleQuestion.setAlpha(1f);
                cameraFragmentBinding.llShuffleQuestion.setEnabled(true);
            }
        }
        AnimatorSet animationSet = new AnimatorSet();

        ObjectAnimator moveCustomQuestionAnim = ObjectAnimator.ofFloat( cameraFragmentBinding.customQuestionView.customAddQuestionView, "translationY", -y, -y * 3 / 4, -y / 2, -y / 4, 0); //0, rlAddQuestion.getY() / 2, rlAddQuestion.getY() //rlAddCustomQuestion.getY(), rlAddCustomQuestion.getY() / 2, 0
        moveCustomQuestionAnim.setDuration(Constants.ANIMATION_DURATION);
        moveCustomQuestionAnim.setInterpolator(new LinearInterpolator());

        ObjectAnimator alphaCustomQuestionAnim = ObjectAnimator.ofFloat(cameraFragmentBinding.rlAddCustomQuestion, "alpha", 0.5f, 0.25f, 0, 0);//1f, 0.5f, 0, 0);//
        alphaCustomQuestionAnim.setDuration(Constants.ANIMATION_DURATION);
        alphaCustomQuestionAnim.setInterpolator(new LinearInterpolator());

        ObjectAnimator moveQuestionLayoutAnim = ObjectAnimator.ofFloat(cameraFragmentBinding.llQuestionLayout, "translationY", 0, y / 4, y / 2, y * 3 / 4, y);//rlAddCustomQuestion.getY() / 2, 0); //rlAddQuestion.getY() / 2, 0
        moveQuestionLayoutAnim.setDuration(Constants.ANIMATION_DURATION);
        moveQuestionLayoutAnim.setInterpolator(new LinearInterpolator());

        ObjectAnimator alphaQuestionAnim = ObjectAnimator.ofFloat(cameraFragmentBinding.rlQuestion, "alpha", 0f, 0.5f, 0.75f, 1f);//0, 0.5f, 1f, 1f);//
        alphaQuestionAnim.setDuration(Constants.ANIMATION_DURATION);
        alphaQuestionAnim.setInterpolator(new LinearInterpolator());


        animationSet.playTogether(moveCustomQuestionAnim, alphaCustomQuestionAnim, moveQuestionLayoutAnim, alphaQuestionAnim);

        animationSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                resetCustomQuestionParams();
                cameraFragmentBinding.rlAddCustomQuestion.setAlpha(0);
                cameraFragmentBinding.rlQuestion.setAlpha(1);
                cameraFragmentBinding.rlAddCustomQuestion.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        animationSet.start();
    }

    private void callAddAPI() {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("question", cameraFragmentBinding.questionView.questionViewBindingCustom.edtAddQuestion.getText().toString().trim());
            if (isLoopQnA) {
                jsonRequest.put("chat_id", context.chatId);
            }
            new BaseAPIService(context, Constants.ADD_UPDATE_CUSTOM_QUESTION, getRequestBody(jsonRequest.toString()), true, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    Utility.printResponseLog(response);
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);
                        Gson gson = new Gson();
                        QuestionModel questionModel = gson.fromJson(dataJson.toString(), QuestionModel.class);
                        addUpdateQuestion(questionModel);
                        if (isLoopQnA) {
                            QuestionViewModel.getInstance().loopQuestionsArr.add(0, questionModel);
                        }
                        if (isZeroQuestions) {
                            cameraFragmentBinding.llQuestionLayout.setVisibility(View.VISIBLE);
                            loopSelectedQuestion = questionModel;
                            setUpLoopQnALayout();
                            isZeroQuestions = false;
                        }
                        performSlideDownAnimation();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    cameraFragmentBinding.rlAddCustomQuestion.setVisibility(View.VISIBLE);
                    cameraFragmentBinding.rlQuestion.setVisibility(View.GONE);
                    if (!error.equalsIgnoreCase("404")) {
                        Utility.showToast(context, error);
                    }
                }
            }, "POST", true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void addUpdateQuestion(QuestionModel questionModel) {
        isQuestionChanged = true;
        cameraFragmentBinding.questionView.questionViewBinding.ivEditQuestion.setVisibility(View.VISIBLE);
        cameraFragmentBinding.questionView.updateTextViewAttrs(questionModel, Constants.QUESTION_FONT_MAX_DEFAULT_SIZE, Constants.QUESTION_FONT_MAX_DEFAULT_SIZE);
        if (!isLoopQnA) {
            QuestionViewModel.getInstance().masterQuestionsArr.set(0, questionModel);
        }
        if (context.selectedQuestion != null && context.selectedQuestion.getQuestionId().equalsIgnoreCase(questionModel.getQuestionId())) {
            context.selectedQuestion = questionModel;
            setCameraQuestionView();
        }
    }

    private void callEditQuestionAPI() {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("question", cameraFragmentBinding.questionView.questionViewBindingCustom.edtAddQuestion.getText().toString().trim());
            jsonRequest.put("question_id", newCustomQuestionModel.getQuestionId());
            if (isLoopQnA) {
                jsonRequest.put("chat_id", context.chatId);
            }
            new BaseAPIService(context, Constants.ADD_UPDATE_CUSTOM_QUESTION, getRequestBody(jsonRequest.toString()), true, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    Utility.printErrorLog(response);
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);
                        Gson gson = new Gson();
                        QuestionModel questionModel = gson.fromJson(dataJson.toString(), QuestionModel.class);
                        addUpdateQuestion(questionModel);
                        if (isLoopQnA) {
                            QuestionViewModel.getInstance().loopQuestionsArr.set(0, questionModel);
                            for (int i = 0; i < QuestionViewModel.getInstance().loopQuestionsArr.size(); i++) {
                                if (QuestionViewModel.getInstance().loopQuestionsArr.get(i).getQuestionId().equalsIgnoreCase(questionModel.getQuestionId())) {
                                    QuestionViewModel.getInstance().loopQuestionsArr.set(i, questionModel);
                                }
                            }
                        }
                        performSlideDownAnimation();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    cameraFragmentBinding.rlAddCustomQuestion.setVisibility(View.VISIBLE);
                    cameraFragmentBinding.rlQuestion.setVisibility(View.GONE);
                    if (error.equalsIgnoreCase("404")) {
                        Utility.showToast(context, error);
                    }
                }
            }, "PUT", true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

//    private void getProfileDetailsAPI() {
//        Map<String, Object> map = new HashMap<>();
//        if (!TextUtils.isEmpty(context.qrCode)) {
//            map.put("qr_code", context.qrCode);
//        }
//        new BaseAPIService(context, Constants.QR_CODE, true, "", map, new ResponseListener() {
//            @Override
//            public void onSuccess(String response) {
//                JSONObject object;
//                try {
//                    object = new JSONObject(response);
//                    JSONObject dataJson = object.optJSONObject(Constants.JSON_DATA);
//                    if (dataJson != null) {
//                        JSONObject ownerJson = dataJson.optJSONObject("owner");
//                        llSendToLayout.setVisibility(View.VISIBLE);
//                        llProfileLayout.setVisibility(View.VISIBLE);
//                        rlUserImage.setVisibility(View.VISIBLE);
//                        tutorialRecordForOther.setVisibility(View.VISIBLE);
//                        llPrivacyLayout.setVisibility(View.GONE);
//                        MembersModel owner = new Gson().fromJson(ownerJson.toString(), MembersModel.class);
//                        convertMemberIntoContactForOther(owner);
//                        if (owner.isAvatar()) {
//                            int res = context.getResources().getIdentifier(owner.getProfileImage(),
//                                    "raw", context.getPackageName());
//                            Drawable color = new ColorDrawable(context.getResources().getColor(LottieAnimModel.getMapData().get(res), null));
//                            ivUserProfile.setImageDrawable(color);
//                            animationViewProfile.setVisibility(View.VISIBLE);
//                            animationViewProfile.setAnimation(res);
//                            animationViewProfile.playAnimation();
//                        } else {
//                            animationViewProfile.setVisibility(View.GONE);
//                            if (TextUtils.isEmpty(owner.getProfileImageM())) {
//                                Glide.with(context).load(owner.getProfileImage()).apply(RequestOptions.circleCropTransform().circleCrop()).into(ivUserProfile);
//                            } else {
//                                Glide.with(context).load(owner.getProfileImageM()).apply(RequestOptions.circleCropTransform().circleCrop()).into(ivUserProfile);
//                            }
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(String error) {
//                Utility.showToast(context, error);
//                context.onPullComplete();
//            }
//        }, "GET_DATA", true);
//    }
}
