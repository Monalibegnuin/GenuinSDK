package com.begenuin.library.views.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.begenuin.library.data.viewmodel.GenuinFFMpegManager;
import com.begenuin.library.data.viewmodel.UploadQueueManager;
import com.begenuin.library.views.fragments.ChangeVideoThumbnailCoverFragment;
import com.begenuin.library.R;
import com.begenuin.library.SDKInitiate;
import com.begenuin.library.common.CameraUtil;
import com.begenuin.library.common.Constants;
import com.begenuin.library.common.Utility;
import com.begenuin.library.common.customViews.PullBackLayout;
import com.begenuin.library.core.enums.MediaType;
import com.begenuin.library.core.enums.PrivacyOptionsType;
import com.begenuin.library.core.enums.VideoConvType;
import com.begenuin.library.data.model.ChatModel;
import com.begenuin.library.data.model.DiscoverModel;
import com.begenuin.library.data.model.GroupModel;
import com.begenuin.library.data.model.LoopsModel;
import com.begenuin.library.data.model.MembersModel;
import com.begenuin.library.data.model.MessageModel;
import com.begenuin.library.data.model.MetaDataModel;
import com.begenuin.library.data.model.QuestionModel;
import com.begenuin.library.data.model.SettingsModel;
import com.begenuin.library.data.model.TopicModel;
import com.begenuin.library.data.model.VideoModel;
import com.begenuin.library.data.model.VideoParamsModel;
import com.begenuin.library.data.remote.BaseAPIService;
import com.begenuin.library.data.viewmodel.ExploreViewModel;
import com.begenuin.library.views.fragments.Camera2PermissionDialog;
import com.begenuin.library.views.fragments.CameraNewFragment;
import com.begenuin.library.views.fragments.RoundTableVideoFragment;
import com.begenuin.library.views.fragments.VideoMergeAndPlayFragment;
//import com.begenuin.library.views.fragments.VideoTrimFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CameraNewActivity extends AppCompatActivity implements PullBackLayout.Callback {

    public CameraNewFragment mCameraFragment;
    public CameraNewFragment mRetakeFragment;
    //public VideoTrimFragment videoTrimFragment;
    public VideoMergeAndPlayFragment mergeAndPlayFragment;
//    public PublishVideoFragment publishVideoFragment;
//    public DirectVideoFragment directVideoFragment;
//    public GroupVideoFragment groupVideoFragment;
    public RoundTableVideoFragment roundTableVideoFragment;
    public PullBackLayout pullerCameraActivity;
    public FrameLayout contentGallery;
    public ChangeVideoThumbnailCoverFragment changeVideoThumbnilFragment;
    public ArrayList<VideoModel> videoList = new ArrayList<>();
    public boolean isFullTrim = false;
    public boolean isRetake = false;
    public boolean isLongPress = false;
    public String retakeFileName = "";
    public String imageToSet = "";
    public int retakePos = -1;
    public String from = Constants.FROM_PUBLIC_VIDEO;
    public int mCameraFacing = CameraCharacteristics.LENS_FACING_FRONT;
    public String chatId, videoId;
    public String communityId, communityHandle;
    public int convType = 0;
    public GroupModel group;
    public SettingsModel settings;
    public ChatModel chat;
    public DiscoverModel discoverVO;
    public QuestionModel selectedQuestion;
    public TopicModel selectedTopic;
    public String userId = "";
    public String deviceId = "";
    public String etDesc = "";
    public String etLink = "";
    public String groupName = "";
    public String groupDesc = "";
    public String rtName = "";
    public String rtDesc = "";
    public int templateId = 0;
    //public ContactsModel contactModelForOther;
    public String timeStamp = "";
    public String ffMpegCommand = "";
    public boolean isCompressionDone = false;
    public boolean isRecordFinishedLogged = false;

    public boolean isNeedToShowPopUp = false;
    public boolean isTabNeedToVisible = true;
    public String questionRedirectId = "";
    public String qrCode = "";
    //public ArrayList<ContactsModel> selectedContacts = new ArrayList<>();
    public ArrayList<ExploreViewModel> mediaList = new ArrayList<>();
    //    public Bitmap coverPhoto;
    public String screenShotMediaPath;
    public boolean screenShotIsFront;
    public int screenHeight;
    public String coverPhotoPath;
    public String downloadedVideoPath;
    public long startMillis;
    public boolean isDataDogLogged;
    public boolean isQnA;
    public boolean isLoopQnA;
    public PrivacyOptionsType privacyOptionsType = PrivacyOptionsType.EVERYONE;
    public PrivacyOptionsType privacyOptionsTypeRT = PrivacyOptionsType.EVERYONE;
    public boolean isBlurbShown = false;
    private BottomSheetDialog bottomSheetDialogPrivacyOptions;
    //public HashMap<String, ContactsModel> contactListHashMap = new HashMap<>();
    public String toReplyUserName;
    public ReplyOptions replyOptions = ReplyOptions.VIDEO;
    public boolean isFromTemplate = false;
    public boolean isWelcomeFlag = false;

    public enum ReplyOptions {
        VIDEO,
        IMAGE,
        AUDIO,
        TEXT
    }

    public enum PrivateVideoSelection {
        NONE,
        DIALOG,
        CONTACT_LIST
    }

    public enum PrivateVideoCreateFrom {
        NONE,
        FROM_INBOX,
        FROM_VIEW_GROUP,
        FROM_PROFILE,
        FROM_NOTIFICATIONS,
        FROM_COMMUNITY_DETAILS,
    }

    public enum VideoOptions {
        PUBLIC,
        DIRECT,
        GROUP,
        ROUND_TABLE,
        REPLY_REACTION,
        COMMENT,
        RECORD_FOR_OTHER
    }

    public PrivateVideoSelection privateVideoSelection = PrivateVideoSelection.NONE;
    public PrivateVideoCreateFrom privateVideoCreateFrom = PrivateVideoCreateFrom.NONE;
    public VideoOptions videoOptions = VideoOptions.PUBLIC;
    public String uuid = "";
    public long prevEventTime;
    public int[] progressArray = new int[2];
    public int videoProgress = 0;
    public String textVideoPath;
    public Bundle savedInstanceState;
    public boolean isRestartAfterCrash = false;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        Utility.changeWindowStatusBarColor(CameraNewActivity.this, R.color.black_111111);
        setContentView(R.layout.activity_camera_new);
        startMillis = System.currentTimeMillis();
        isRestartAfterCrash = false;
        if (savedInstanceState != null) {
            isRestartAfterCrash = true;
            mCameraFragment = (CameraNewFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mCameraFragment");
        }
        initControls();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        try {
            if (getSupportFragmentManager() != null) {
                getSupportFragmentManager().putFragment(outState, "mCameraFragment", mCameraFragment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("HardwareIds")
    private void initControls() {
        getDataFromIntent();
        uuid = UUID.randomUUID().toString();
        pullerCameraActivity = findViewById(R.id.pullerCameraActivity);
        pullerCameraActivity.setCallback(this);
        pullerCameraActivity.setXDragEnable(false);

        int[] hw = new int[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            hw = Utility.getScreenWidthHeight(CameraNewActivity.this);
        }
        assert hw != null;
        screenHeight = hw[1];

        contentGallery = findViewById(R.id.contentGallery);
        contentGallery.post(() -> contentGallery.setTranslationY(screenHeight));

        videoList.clear();
        mCameraFragment = new CameraNewFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content, mCameraFragment).addToBackStack("Camera").commit();

//        if (Utility.isLoggedIn(this)) {
//            userId = SharedPrefUtils.getStringPreference(this, Constants.PREF_USER);
//        }
        userId = SDKInitiate.INSTANCE.getUserId();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void getDataFromIntent() {
        if (getIntent().getExtras() != null) {
            from = getIntent().getExtras().getString("from");
            if (from.equalsIgnoreCase(Constants.FROM_ROUND_TABLE)) {
                    videoOptions = VideoOptions.ROUND_TABLE;
                    privateVideoCreateFrom = PrivateVideoCreateFrom.FROM_COMMUNITY_DETAILS;
                    communityId = getIntent().getExtras().getString("community_id", "");
                    communityHandle = getIntent().getExtras().getString("community_handle", "");
//                    rtName = communityHandle;
//                    rtDesc = "Welcome loop description";
                    }
                }
    }

    public boolean isRequiredPermissionsGranted() {
        boolean isGranted = false;
        if (isTextReply()) {
            isGranted = true;
        } else if (isPhotoReply() && CameraUtil.hasPermissionsGranted(this, Camera2PermissionDialog.PHOTO_PERMISSIONS)) {
            isGranted = true;
        } else if (isAudioReply() && CameraUtil.hasPermissionsGranted(this, Camera2PermissionDialog.AUDIO_PERMISSIONS)) {
            isGranted = true;
        } else if (CameraUtil.hasPermissionsGranted(this, Camera2PermissionDialog.VIDEO_PERMISSIONS)) {
            isGranted = true;
        }
        return isGranted;
    }

    public boolean canRequestRequiredPermissions() {
        boolean canRequest = false;
        if (isPhotoReply() && CameraUtil.shouldShowRequestPermissionRationale(this, Camera2PermissionDialog.PHOTO_PERMISSIONS)) {
            canRequest = true;
        } else if (isAudioReply() && CameraUtil.shouldShowRequestPermissionRationale(this, Camera2PermissionDialog.AUDIO_PERMISSIONS)) {
            canRequest = true;
        } else if (CameraUtil.shouldShowRequestPermissionRationale(this, Camera2PermissionDialog.VIDEO_PERMISSIONS)) {
            canRequest = true;
        }
        return canRequest;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Camera2PermissionDialog.REQUEST_PERMISSIONS:
                if (mCameraFragment != null) {
                    if (isRequiredPermissionsGranted()) {
                        mCameraFragment.permissionEnableDisableViews(true);
                    } else {
                        mCameraFragment.permissionEnableDisableViews(false);
                    }
                }
                break;
            case Camera2PermissionDialog.REQUEST_STORAGE_PERMISSIONS:
                if (mCameraFragment != null) {
                    if (isStoragePermissionsGranted()) {
                        //mCameraFragment.llGallery.performClick();
                    } else {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle(getResources().getString(R.string.txt_gallery_per_msg));
                            builder.setMessage(getResources().getString(R.string.txt_gallery_per));
                            builder.setPositiveButton(getResources().getString(R.string.go_to_settings), (dialogInterface, i) -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            });
                            builder.setNegativeButton(getResources().getString(R.string.cancel), null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setAllCaps(false);
                            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setAllCaps(false);
                        }
                    }
                }
                break;
            case Constants.WRITE_STORAGE_PERMISSION:
                if (mergeAndPlayFragment != null) {
                    mergeAndPlayFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void goToVideoTrimmer() {
//        videoTrimFragment = new VideoTrimFragment();
//        Bundle args = new Bundle();
//        args.putLong("previousEventTime", prevEventTime);
//        videoTrimFragment.setArguments(args);
//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.content, videoTrimFragment)
//                .addToBackStack("VideoTrim").commit();
//        // Set up the transaction.
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        // Define the shared element transition.
//        videoTrimFragment.setSharedElementEnterTransition(new SharedTransition());
//        videoTrimFragment.setSharedElementReturnTransition(new SharedTransition());
//
//        // The rest of the views are just fading in/out.
//        videoTrimFragment.setEnterTransition(new Fade());
//        mergeAndPlayFragment.setExitTransition(new Fade());
//
//        // Now use the image's view and the target transitionName to define the shared element.
//        transaction.addSharedElement(mergeAndPlayFragment.videoView, "sharedVideoView");
//
//        // Replace the fragment.
//        transaction.replace(R.id.content, videoTrimFragment, videoTrimFragment.getClass().getSimpleName());
//
//        // Enable back navigation with shared element transitions.
//        transaction.addToBackStack("VideoTrim");
//
//        // Finally press play.
//        transaction.commit();
    }

    public void goToRetakeFragment(int videoPos) {
        mRetakeFragment = new CameraNewFragment();
        Bundle args = new Bundle();
        args.putInt("pos", videoPos);
        mRetakeFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content, mRetakeFragment)
                .addToBackStack("Retake").commit();
    }

    public void goToVideoPlayFragment() {
        mergeAndPlayFragment = new VideoMergeAndPlayFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content, mergeAndPlayFragment)
                .addToBackStack("Play").commit();
    }

    @Override
    public void onPullStart() {
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        getWindow().setStatusBarColor(getResources().getColor(R.color.transparent, null));
        if (mCameraFragment != null) {
            mCameraFragment.getParentView().setBackgroundResource(R.color.transparent);
        }
    }

    @Override
    public void onPull(float progress) {
        Utility.showLog("tag", progress + " ");
    }

    @Override
    public void onPullCancel() {
        getWindow().setBackgroundDrawableResource(R.color.colorWhite);
        getWindow().setStatusBarColor(getResources().getColor(R.color.black_111111, null));
        if (mCameraFragment != null) {
            mCameraFragment.getParentView().setBackgroundResource(R.color.colorBlack);
        }
    }

    @Override
    public void onPullComplete() {
        cleanUpMemory();
        if (privateVideoCreateFrom == PrivateVideoCreateFrom.FROM_VIEW_GROUP) {
//            if (DirectDetailsActivity.activity != null) {
//                DirectDetailsActivity.activity.finish();
//            }
            finish();
        } else {
            finish();
            overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
        }
    }

    public boolean isAnyNonGenuinVideo() {
        boolean isFound = false;
        if (videoList.size() > 0) {
            for (int i = 0; i < videoList.size(); i++) {
                if (videoList.get(i).isFromGallery) {
                    isFound = true;
                    break;
                }
            }
        }
        return isFound;
    }

    public void cleanUpMemory() {
        try {
            File galleryVideoLocation = getExternalFilesDir(Constants.GALLERY_DIRECTORY);
            if (galleryVideoLocation.isDirectory()) {
                String[] children = galleryVideoLocation.list();
                if (children != null) {
                    for (String child : children) {
                        new File(galleryVideoLocation, child).delete();
                    }
                }
            }
        } catch (Exception e) {
            Utility.showLogException(e);
        }
    }

    @Override
    public void onBackPressed() {

        if (mergeAndPlayFragment != null && mergeAndPlayFragment.reactionBlurLayout != null) {
            if (mergeAndPlayFragment.reactionBlurLayout.getVisibility() == View.VISIBLE) {
                mergeAndPlayFragment.reactionBlurLayout.setVisibility(View.GONE);
                mergeAndPlayFragment.finishActivity();
                return;
            }
        }

        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else if (count == 1) {
            if (mCameraFragment != null) {
                mCameraFragment.showCloseConfirmAlert();
            }
        } else if (count == 2) {
//            if (galleryFragment != null) {
//                galleryFragment.backManage(true);
//            } else {
//                if (mergeAndPlayFragment != null) {
//                    mergeAndPlayFragment.backManage();
//                }
//            }
        } else if (count == 3) {
            Fragment fragment = getSupportFragmentManager().getFragments().get(count - 1);
//            if (fragment instanceof VideoTrimFragment) {
//                if (videoTrimFragment != null) {
//                    //videoTrimFragment.backManage();
//                }
//            } else
                if (fragment instanceof ChangeVideoThumbnailCoverFragment) {
                if (changeVideoThumbnilFragment != null) {
                    changeVideoThumbnilFragment.backManage();
                }
            } else {
                if (videoOptions == VideoOptions.DIRECT) {
//                    if (directVideoFragment != null) {
//                        directVideoFragment.backManage();
//                    }
                } else if (videoOptions == VideoOptions.GROUP) {
//                    if (groupVideoFragment != null) {
//                        groupVideoFragment.backManage();
//                    }
                } else if (videoOptions == VideoOptions.ROUND_TABLE) {
                    if (roundTableVideoFragment != null) {
                        roundTableVideoFragment.backManage();
                    }
                } else {
//                    if (publishVideoFragment != null) {
//                        publishVideoFragment.backManage();
//                    }
                }
            }
        } else if (count == 4) {
            Fragment fragment = getSupportFragmentManager().getFragments().get(count - 1);
            if (fragment instanceof CameraNewFragment) {
                if (mRetakeFragment != null) {
                    mRetakeFragment.backManage();
                }
            }
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    public boolean isDirectOrRoundTable() {
        return (videoOptions == VideoOptions.DIRECT || videoOptions == VideoOptions.GROUP || videoOptions == VideoOptions.ROUND_TABLE);
    }

//    public String getSendToStr() {
//        String sendToString = "";
//        if (selectedContacts != null && selectedContacts.size() > 0) {
//            if (videoOptions == VideoOptions.ROUND_TABLE) {
//                if (selectedContacts.size() == 1) {
//                    String userName;
//                    if (selectedContacts.get(0).getGenuin() != null) {
//                        userName = "@" + selectedContacts.get(0).getGenuin().getUserName();
//                    } else {
//                        userName = selectedContacts.get(0).getFirstName();
//                    }
//                    sendToString = " " + userName;
//                } else {
//                    int otherCount = selectedContacts.size();
//                    sendToString = " " + otherCount + " others";
//                }
//            } else {
//                String userName;
//                if (selectedContacts.get(0).getGenuin() != null) {
//                    userName = "@" + selectedContacts.get(0).getGenuin().getUserName();
//                } else {
//                    userName = selectedContacts.get(0).getFirstName();
//                }
//                if (selectedContacts.size() == 1) {
//                    sendToString = " " + userName;
//                } else {
//                    int otherCount = selectedContacts.size() - 1;
//                    if (otherCount == 1) {
//                        sendToString = " " + userName + " & 1 other";
//                    } else {
//                        sendToString = " " + userName + " & " + otherCount + " others";
//                    }
//                }
//            }
//        }
//        return sendToString;
//    }

    public void goToChangeVideoCover() {
        Utility.printErrorLog("Camera: ChangeVideoCover progressArray: " + progressArray[0] + ", " + progressArray[1]);
        Utility.printErrorLog("Camera: ChangeVideoCover customProgress: " + videoProgress);

        changeVideoThumbnilFragment = new ChangeVideoThumbnailCoverFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content, changeVideoThumbnilFragment)
                .addToBackStack("changeVideoThumb").commit();
    }

    public Bitmap getCoverBitmap(String path, boolean isFront) {
        try {
            return Utility.retrieveVideoFrameFromVideo(path, isFront, videoProgress == 0 ? 1 : videoProgress);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    public void onChangeCoverDoneBtnClick(Bitmap bitmap, int[] progressArray, int customProgress, String mediaPath, boolean isFront) {
        this.progressArray = progressArray;
        this.videoProgress = customProgress;
        this.screenShotMediaPath = mediaPath;
        this.screenShotIsFront = isFront;
        Utility.printErrorLog("Camera: DoneBtnClick progressArray: " + progressArray[0] + ", " + progressArray[1]);
        Utility.printErrorLog("Camera: DoneBtnClick customProgress: " + videoProgress);

        getSupportFragmentManager().popBackStack();
        changeVideoThumbnilFragment = null;
        if (mergeAndPlayFragment != null) {
            mergeAndPlayFragment.startVideoPlaying(false);
            mergeAndPlayFragment.setVideoCoverImage(bitmap);
        }
        BaseAPIService.dismissProgressDialog();
    }

    public void onChangeCoverCancelBtnClick() {
        getSupportFragmentManager().popBackStack();
        changeVideoThumbnilFragment = null;
        if (mergeAndPlayFragment != null) {
            mergeAndPlayFragment.startVideoPlaying(false);
        }
        BaseAPIService.dismissProgressDialog();
    }

    public void insertAndUploadLoopVideo(String compressVideoFilePath, long videoDuration, String link, int videoWidth, int videoHeight) {
        File videoFile = new File(compressVideoFilePath);
        File file;
        if (TextUtils.isEmpty(coverPhotoPath)) {
            File destinationLocation = getExternalFilesDir(Constants.MERGE_DIRECTORY);
            String fileName = videoFile.getName().replace(".mp4", ".png");
            file = new File(destinationLocation, fileName);
            try {
                Bitmap mBitmap = getCoverBitmap(compressVideoFilePath, false);
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
            file = new File(coverPhotoPath);
        }
        if (file.exists()) {
            String imagePath = file.getAbsolutePath();
            String selectedContactsStr = "";
            if (isCompressionDone) {
                try {
                    String finalUrl = compressVideoFilePath.substring(compressVideoFilePath.lastIndexOf('/') + 1);
                    File to = new File(getCacheDir(), finalUrl);
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
                    Utility.showLogException(e);
                }
            }
            try {
                Constants.START_MILLIS_POST = System.currentTimeMillis();
                MessageModel message = new MessageModel();
                message.setChatId("-101");
                message.setMediaUrl(compressVideoFilePath);
                message.setThumbnailUrl(imagePath);
                message.setLocalVideoPath(compressVideoFilePath);
                message.setLocalImagePath(imagePath);
                message.setMessageSummary("");
                message.setLink(link);
                message.setMessageId("");
                message.setMessageAt(String.valueOf(Constants.START_MILLIS_POST));
                message.setRead(true);
                message.setRetry(false);
                message.setConvType(VideoConvType.ROUND_TABLE.getValue());
                message.setVideoUploadStatus(1);
                message.setImageUploadStatus(1);
                message.setDpUploadStatus(TextUtils.isEmpty(imageToSet) ? 2 : 1);
                message.setFfMpegCommand(ffMpegCommand);
                message.setCompressionStatus(isCompressionDone ? 1 : 0);
                if (selectedQuestion != null) {
                    List<QuestionModel> questions = new ArrayList<>();
                    questions.add(selectedQuestion);
                    message.setQuestions(questions);
                }
                MetaDataModel metaDataModel = new MetaDataModel();
                metaDataModel.setContainsExternalVideos(isAnyNonGenuinVideo());
                metaDataModel.setMediaType(MediaType.getMediaType(replyOptions));
                metaDataModel.setDuration(Utility.getDurationInt(videoDuration) + "");
                metaDataModel.setAspectRatio(Utility.getVideoAspectRatio(videoWidth, videoHeight));
                metaDataModel.setResolution(Utility.getVideoResolution(videoWidth, videoHeight));
                metaDataModel.setSize("5");
                if (selectedTopic != null) {
                    metaDataModel.setTopic(selectedTopic.getName());
                }
                message.setMetaData(metaDataModel);

//                MembersModel currentUserObject = Utility.getCurrentUserObject(this, compressVideoFilePath);
//                message.setOwner(currentUserObject);
                GroupModel group = new GroupModel();
                group.setGroupId("");
                group.setName(rtName);
                group.setDescription(TextUtils.isEmpty(rtDesc) ? "" : rtDesc);
                if (!TextUtils.isEmpty(imageToSet)) {
                    group.setDp(imageToSet);
                    group.setSmallDp(imageToSet);
                }
                group.setVideoURL(compressVideoFilePath);
                List<MembersModel> membersList = new ArrayList<>();
                //membersList.add(currentUserObject);

//                for (ContactsModel contact : selectedContacts) {
//                    MembersModel member = new MembersModel();
//                    if (contact.getGenuin() != null) {
//                        ContactsModel.Genuin genuin = contact.getGenuin();
//                        member.setName(genuin.getName());
//                        member.setAvatar(genuin.getIsAvatar());
//                        member.setMemberStatus("1");
//                        member.setProfileImage(genuin.getProfileImage());
//                        member.setNickname(genuin.getUserName());
//                        member.setUserId(genuin.getUuid());
//                    } else {
//                        member.setName(contact.getFirstName());
//                        member.setProfileImage("");
//                        member.setMemberStatus("2");
//                        member.setUserId(contact.getPhoneNumber());
//                    }
//                    member.setPhone(contact.getPhoneNumber());
//                    member.setProfileImageL("");
//                    member.setProfileImageM("");
//                    member.setProfileImageS("");
//                    member.setMemberRole("2");
//                    member.setVideoURL(compressVideoFilePath);
//                    membersList.add(member);
//                }
                group.setMembers(membersList);
                group.setNoOfMembers(String.valueOf(membersList.size()));

                SettingsModel settings = new SettingsModel();
                settings.setDiscoverable(privacyOptionsTypeRT == PrivacyOptionsType.EVERYONE);

                LoopsModel loop = new LoopsModel();
                loop.setChatId("-101");
                loop.setConvType(VideoConvType.ROUND_TABLE.getValue());
                ArrayList<MessageModel> messages = new ArrayList<>();
                messages.add(message);
                loop.setLatestMessages(messages);
                loop.setGroup(group);
                loop.setSettings(settings);
                loop.setShareUrl("");
                loop.setNoOfViews("0");
                loop.setLatestMessageAt(String.valueOf(Constants.START_MILLIS_POST));
                loop.setUnreadMessageCount("0");
                loop.setLocalVideoPath(compressVideoFilePath);
                if (!TextUtils.isEmpty(communityId)) {
                    loop.setCommunityId(communityId);
                }
                loop.setTemplateId(templateId);
                // Need to change while implementing welcome loop
                loop.setWelcomeLoop(isWelcomeFlag);
                if (Utility.getDBHelper() != null) {
                    Utility.getDBHelper().insertORUpdateLoop(loop);
                }
                Type listType = new TypeToken<List<MembersModel>>() {
                }.getType();
                selectedContactsStr = new Gson().toJson(membersList, listType);
            } catch (Exception e) {
                Utility.showLogException(e);
            }
            if (isCompressionDone) {
                uploadDirectORRTVideo(VideoConvType.ROUND_TABLE.getValue(), compressVideoFilePath, imagePath, videoDuration, link, videoWidth, videoHeight, selectedContactsStr);
            } else {
                GenuinFFMpegManager.getInstance().addValueToHashmap(compressVideoFilePath, true);
            }
        }
    }

    public void insertAndUploadVideo(int convType, String compressVideoFilePath, long videoDuration, String link, int videoWidth,
                                     int videoHeight) {
        File videoFile = new File(compressVideoFilePath);
        File file;
        if (TextUtils.isEmpty(coverPhotoPath)) {
            File destinationLocation = getExternalFilesDir(Constants.MERGE_DIRECTORY);
            String fileName = videoFile.getName().replace(".mp4", ".png");
            file = new File(destinationLocation, fileName);
            try {
                Bitmap mBitmap = getCoverBitmap(compressVideoFilePath, false);
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
            file = new File(coverPhotoPath);
        }
        if (file.exists()) {
            String imagePath = file.getAbsolutePath();
            String selectedContactsStr = "";
            if (isCompressionDone) {
                try {
                    String finalUrl = compressVideoFilePath.substring(compressVideoFilePath.lastIndexOf('/') + 1);
                    File to = new File(getCacheDir(), finalUrl);
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
                    Utility.showLogException(e);
                }
            }
            try {
                Constants.START_MILLIS_POST = System.currentTimeMillis();
                ChatModel chat = new ChatModel();
                chat.setChatId("-101");
                chat.setVideoUrl(compressVideoFilePath);
                chat.setThumbnailUrl(imagePath);
                chat.setLocalVideoPath(compressVideoFilePath);
                chat.setImagePath(imagePath);
                chat.setDuration(Utility.getDurationInt(videoDuration) + "");
                chat.setDescription("");
                chat.setLink(link);
                chat.setAspectRatio(Utility.getVideoAspectRatio(videoWidth, videoHeight));
                chat.setResolution(Utility.getVideoResolution(videoWidth, videoHeight));
                chat.setSize("5");
                chat.setConversationId("");
                chat.setConversationAt(String.valueOf(Constants.START_MILLIS_POST));
                chat.setReply(false);
                chat.setRead(true);
                chat.setRetry(false);
                chat.setIsReplyOrReaction(1);
                chat.setFromStatus("1");
                chat.setConvType(convType);
                chat.setVideoUploadStatus(1);
                chat.setImageUploadStatus(1);
                chat.setDpUploadStatus(TextUtils.isEmpty(imageToSet) ? 2 : 1);
                chat.setIsReplyReceived(0);
                chat.setFfMpegCommand(ffMpegCommand);
                chat.setCompressionStatus(isCompressionDone ? 1 : 0);
                if (selectedQuestion != null) {
                    List<QuestionModel> questions = new ArrayList<>();
                    questions.add(selectedQuestion);
                    chat.setQuestions(questions);
                }
                MetaDataModel metaDataModel = new MetaDataModel();
                metaDataModel.setContainsExternalVideos(isAnyNonGenuinVideo());
                metaDataModel.setMediaType(MediaType.getMediaType(replyOptions));
                if (selectedTopic != null) {
                    metaDataModel.setTopic(selectedTopic.getName());
                }
                chat.setMetaData(metaDataModel);

                //MembersModel currentUserObject = Utility.getCurrentUserObject(this, compressVideoFilePath);
                MembersModel currentUserObject = new MembersModel();
                currentUserObject.setUserId(SDKInitiate.INSTANCE.getUserId());
                currentUserObject.setName("TestSDK");
                currentUserObject.setNickname("Test");
                currentUserObject.setPhone("8779084869");
                chat.setOwner(currentUserObject);
                GroupModel group = new GroupModel();
                group.setGroupId("");
                if (convType == VideoConvType.ROUND_TABLE.getValue()) {
                    group.setName(rtName);
                    group.setDescription(TextUtils.isEmpty(rtDesc) ? "" : rtDesc);
                    SettingsModel settings = new SettingsModel();
                    settings.setDiscoverable(privacyOptionsTypeRT == PrivacyOptionsType.EVERYONE);
                    chat.setSettings(settings);
                } else if (convType == VideoConvType.GROUP.getValue()) {
                    group.setName(groupName);
                    group.setDescription(TextUtils.isEmpty(groupDesc) ? "" : groupDesc);
                } else {
                    group.setName("");
                    group.setDescription("");
                }
                if (!TextUtils.isEmpty(imageToSet)) {
                    group.setDp(imageToSet);
                    group.setSmallDp(imageToSet);
                }
                group.setVideoURL(compressVideoFilePath);
                List<MembersModel> membersList = new ArrayList<>();
                membersList.add(currentUserObject);

//                for (ContactsModel contact : selectedContacts) {
//                    MembersModel member = new MembersModel();
//                    if (contact.getGenuin() != null) {
//                        ContactsModel.Genuin genuin = contact.getGenuin();
//                        member.setName(genuin.getName());
//                        member.setAvatar(genuin.getIsAvatar());
//                        member.setMemberStatus("1");
//                        member.setProfileImage(genuin.getProfileImage());
//                        member.setNickname(genuin.getUserName());
//                        member.setUserId(genuin.getUuid());
//                    } else {
//                        member.setName(contact.getFirstName());
//                        member.setProfileImage("");
//                        member.setMemberStatus("2");
//                        member.setUserId("-101");
//                    }
//                    member.setPhone(contact.getPhoneNumber());
//                    member.setProfileImageL("");
//                    member.setProfileImageM("");
//                    member.setProfileImageS("");
//                    member.setMemberRole("2");
//                    member.setVideoURL(compressVideoFilePath);
//                    membersList.add(member);
//                }
                group.setMembers(membersList);
                Type listType = new TypeToken<List<MembersModel>>() {
                }.getType();
                selectedContactsStr = new Gson().toJson(membersList, listType);
                if (Utility.getDBHelper() != null) {
                    Utility.getDBHelper().insertDirectVideo(chat);
                    Utility.getDBHelper().insertGroupAndMembers("-101", group, Constants.START_MILLIS_POST, convType, 1);
                }
            } catch (Exception e) {
                Utility.showLogException(e);
            }
            if (isCompressionDone) {
                uploadDirectORRTVideo(convType, compressVideoFilePath, imagePath, videoDuration, link, videoWidth, videoHeight, selectedContactsStr);
            } else {
                GenuinFFMpegManager.getInstance().addValueToHashmap(compressVideoFilePath, true);
            }
        }
    }

    private void uploadDirectORRTVideo(int convType, String compressVideoFilePath, String imagePath, long videoDuration, String link, int videoWidth, int videoHeight, String selectedContactsStr) {
        VideoParamsModel videoParamsModel = new VideoParamsModel();
        videoParamsModel.isVideoRequired = true;
        videoParamsModel.isImageRequired = true;
        videoParamsModel.isDpRequired = !TextUtils.isEmpty(imageToSet);
        if (convType == VideoConvType.ROUND_TABLE.getValue()) {
            videoParamsModel.from = Constants.FROM_ROUND_TABLE;
            videoParamsModel.rtName = rtName;
            videoParamsModel.rtDesc = rtDesc;
            videoParamsModel.communityId = communityId;
            SettingsModel settings = new SettingsModel();
            settings.setDiscoverable(privacyOptionsTypeRT == PrivacyOptionsType.EVERYONE);
            videoParamsModel.settingsModel = settings;
            if(isFromTemplate) videoParamsModel.templateId = templateId;
            if(isWelcomeFlag) videoParamsModel.isWelcomeLoop = isWelcomeFlag;
        } else if (convType == VideoConvType.GROUP.getValue()) {
            videoParamsModel.from = Constants.FROM_GROUP;
            videoParamsModel.groupName = groupName;
            videoParamsModel.groupDesc = groupDesc;
        } else if (convType == VideoConvType.DIRECT.getValue()) {
            videoParamsModel.from = Constants.FROM_DIRECT;
        }
        videoParamsModel.link = link;
        videoParamsModel.duration = String.valueOf(Utility.getDurationInt(videoDuration));
        videoParamsModel.resolution = Utility.getVideoResolution(videoWidth, videoHeight);
        videoParamsModel.aspectRatio = Utility.getVideoAspectRatio(videoWidth, videoHeight);
        videoParamsModel.size = "5";
        videoParamsModel.selectedContacts = selectedContactsStr;
        if (selectedQuestion != null) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(selectedQuestion.getQuestionId());
            videoParamsModel.selectedQuestions = jsonArray.toString();
        }
        try {
            JSONObject jsonMetaData = new JSONObject();
            if (selectedTopic != null) {
                jsonMetaData.put("topic", selectedTopic.getName());
            }
            jsonMetaData.put("contains_external_videos", isAnyNonGenuinVideo());
            jsonMetaData.put("media_type", MediaType.getMediaType(replyOptions));
            videoParamsModel.metaData = jsonMetaData.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        videoParamsModel.videoFile = compressVideoFilePath;
        videoParamsModel.imageFile = imagePath;
        videoParamsModel.dpFile = imageToSet;
        UploadQueueManager.getInstance().uploadVideo(CameraNewActivity.this, videoParamsModel);
    }
//    public String getNonGenuinUsers() {
//        String mobileNos = "";
//        if (selectedContacts.size() > 0) {
//            for (int i = 0; i < selectedContacts.size(); i++) {
//                if (selectedContacts.get(i).getGenuin() == null) {
//                    if (!TextUtils.isEmpty(mobileNos)) {
//                        mobileNos += ";";
//                    }
//                    mobileNos += selectedContacts.get(i).getPhoneNumber();
//                }
//            }
//        }
//        return mobileNos;
//    }

//    public void openBottomSheetDialogForPrivacyOptions(ImageView ivPrivacyType, TextView tvPrivacyOption, ImageView ivHeader) {
//        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_privacy_options, null);
//        RelativeLayout rlEveryOne = bottomSheetView.findViewById(R.id.rlEveryOne);
//        RelativeLayout rlUnlisted = bottomSheetView.findViewById(R.id.rlUnlisted);
//        ImageView ivEveryOne = bottomSheetView.findViewById(R.id.ivEveryOne);
//        ImageView ivUnlisted = bottomSheetView.findViewById(R.id.ivUnlisted);
//        TextView tvUnlisted = bottomSheetView.findViewById(R.id.tvUnlisted);
//        TextView tvWhoCanSee = bottomSheetView.findViewById(R.id.tvWhoCanSee);
//
//        if (videoOptions == VideoOptions.ROUND_TABLE) {
//            tvWhoCanSee.setText(getResources().getString(R.string.who_can_see_this_loop));
//            tvUnlisted.setText(getResources().getString(R.string.unlisted_desc_rt));
//            if (privacyOptionsTypeRT == PrivacyOptionsType.EVERYONE) {
//                ivEveryOne.setImageResource(R.drawable.ic_tick);
//                ivUnlisted.setImageResource(R.drawable.ic_untick);
//            } else {
//                ivEveryOne.setImageResource(R.drawable.ic_untick);
//                ivUnlisted.setImageResource(R.drawable.ic_tick);
//            }
//            Properties properties = new Properties();
//            properties.put(Constants.KEY_EVENT_RECORD_SCREEN, Constants.SCREEN_RT_PUBLISH);
//            properties.put(Constants.KEY_EVENT_TARGET_SCREEN, Constants.NONE);
//            GenuInApplication.getInstance().sendEventLogs(Constants.CREATE_LOOP_PRIVACY_MENU_CLICKED, properties);
//        } else {
//            tvUnlisted.setText(getResources().getString(R.string.unlisted_desc_public));
//            if (privacyOptionsType == PrivacyOptionsType.EVERYONE) {
//                ivEveryOne.setImageResource(R.drawable.ic_tick);
//                ivUnlisted.setImageResource(R.drawable.ic_untick);
//            } else {
//                ivEveryOne.setImageResource(R.drawable.ic_untick);
//                ivUnlisted.setImageResource(R.drawable.ic_tick);
//            }
//        }
//
//        rlEveryOne.setOnClickListener(v -> {
//            bottomSheetDialogPrivacyOptions.dismiss();
//            if (videoOptions == VideoOptions.ROUND_TABLE) {
//                privacyOptionsTypeRT = PrivacyOptionsType.EVERYONE;
//                if (ivHeader != null) {
//                    ivHeader.setImageResource(R.drawable.ic_globe_with_bg);
//                }
//            } else {
//                privacyOptionsType = PrivacyOptionsType.EVERYONE;
//            }
//            ivPrivacyType.setImageResource(R.drawable.ic_globe_with_bg);
//            if (tvPrivacyOption != null) {
//                tvPrivacyOption.setText(getResources().getString(R.string.everyone));
//            }
//        });
//
//        rlUnlisted.setOnClickListener(v -> {
//            bottomSheetDialogPrivacyOptions.dismiss();
//            if (videoOptions == VideoOptions.ROUND_TABLE) {
//                privacyOptionsTypeRT = PrivacyOptionsType.UNLISTED;
//                if (ivHeader != null) {
//                    ivHeader.setImageResource(R.drawable.ic_icon_link);
//                }
//            } else {
//                privacyOptionsType = PrivacyOptionsType.UNLISTED;
//            }
//            ivPrivacyType.setImageResource(R.drawable.ic_icon_link);
//            if (tvPrivacyOption != null) {
//                tvPrivacyOption.setText(getResources().getString(R.string.unlisted));
//            }
//        });
//
//        // Bottom sheet dialog
//        bottomSheetDialogPrivacyOptions = new BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme);
//        bottomSheetDialogPrivacyOptions.setContentView(bottomSheetView);
//        bottomSheetDialogPrivacyOptions.setCancelable(true);
//        bottomSheetDialogPrivacyOptions.show();
//    }

    public boolean isReplyReactionWithoutVideo() {
        return ((videoOptions == VideoOptions.REPLY_REACTION || videoOptions == VideoOptions.COMMENT) && (replyOptions == ReplyOptions.AUDIO || replyOptions == ReplyOptions.TEXT));
    }

    public boolean isVideoReply() {
        return (videoOptions == VideoOptions.REPLY_REACTION || videoOptions == VideoOptions.COMMENT) && replyOptions == ReplyOptions.VIDEO;
    }

    public boolean isPhotoReply() {
        return (videoOptions == VideoOptions.REPLY_REACTION || videoOptions == VideoOptions.COMMENT) && replyOptions == ReplyOptions.IMAGE;
    }

    public boolean isAudioReply() {
        return (videoOptions == VideoOptions.REPLY_REACTION || videoOptions == VideoOptions.COMMENT) && replyOptions == ReplyOptions.AUDIO;
    }

    public boolean isTextReply() {
        return (videoOptions == VideoOptions.REPLY_REACTION || videoOptions == VideoOptions.COMMENT) && replyOptions == ReplyOptions.TEXT;
    }

    public String getTextVideoPath() {
        File file = new File(getCacheDir(), "text_bg_video.mp4");
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = getAssets().open("text_bg_video.mp4")) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onGalleryScroll(float diffY) {
        float y = contentGallery.getTranslationY();
        if (y > 0) {
            contentGallery.setTranslationY((y - diffY));
            contentGallery.invalidate();
        }
    }

    public void onGalleryScrollDown(float diffY) {
        float y = contentGallery.getTranslationY();
        contentGallery.setTranslationY((y + diffY));
        contentGallery.invalidate();
    }

    public boolean isStoragePermissionsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return CameraUtil.hasPermissionsGranted(this, Camera2PermissionDialog.STORAGE_PERMISSIONS_33);
        } else {
            return CameraUtil.hasPermissionsGranted(this, Camera2PermissionDialog.STORAGE_PERMISSIONS);
        }
    }


//    public void onGallerySwipeDown() {
//        float y = contentGallery.getTranslationY();
//        ObjectAnimator tranYAnim = ObjectAnimator.ofFloat(contentGallery, "translationY", y, (screenHeight + 50));
//        tranYAnim.setDuration(300);
//        tranYAnim.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(@NonNull Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(@NonNull Animator animator) {
//                if (galleryFragment != null) {
//                    galleryFragment.backManage(false);
//                }
//            }
//
//            @Override
//            public void onAnimationCancel(@NonNull Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(@NonNull Animator animator) {
//
//            }
//        });
//        tranYAnim.start();
//    }

//    public void onGallerySwipeCancel() {
//        float y = contentGallery.getTranslationY();
//        if (y > screenHeight / 2f) {
//            onGallerySwipeDown();
//        } else {
//            ObjectAnimator tranYAnim = ObjectAnimator.ofFloat(contentGallery, "translationY", y, 0f);
//            tranYAnim.setDuration(300);
//            tranYAnim.start();
//        }
//    }
}