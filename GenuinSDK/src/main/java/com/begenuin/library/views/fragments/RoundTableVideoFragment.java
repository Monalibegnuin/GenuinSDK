package com.begenuin.library.views.fragments;

import static com.begenuin.library.common.Utility.showKeyboardForCustomEditText;
import static com.begenuin.library.common.Utility.showLogException;
import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;

import com.begenuin.library.R;
import com.begenuin.library.SDKInitiate;
import com.begenuin.library.common.Constants;
import com.begenuin.library.common.ImageUtils;
import com.begenuin.library.common.Utility;
import com.begenuin.library.common.customViews.CustomEditTextWithError;
import com.begenuin.library.common.customViews.CustomTextView;
import com.begenuin.library.common.customViews.tooltip.SimpleTooltip;
import com.begenuin.library.core.enums.PrivacyOptionsType;
import com.begenuin.library.core.interfaces.ResponseListener;
import com.begenuin.library.data.remote.BaseAPIService;
import com.begenuin.library.views.activities.CameraNewActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class RoundTableVideoFragment extends Fragment implements View.OnClickListener {

    private String compressVideoFilePath;
    private int videoHeight, videoWidth;
    private int videoDuration;
    private String link;

    private View view;
    private ImageView imgClose;
    private CustomEditTextWithError etRtName;
    private CustomEditTextWithError etRTDesc;
    private MaterialButton btnPublishRT;
    private FlexboxLayout flexboxLayout;

    private MaterialButton btnDone;
    private LinearLayout linearWhiteDummy;
    private LinearLayout llWhoCanSee;
    private LinearLayout llPrivacyOption;
    private ScrollView scrollSuccessPage;
    private ImageView ivThumbnail;
    private FrameLayout flSuccessClose;
    //private CustomTextView tvChangeDp;
    private ImageView ivPrivacyType, ivHeaderPrivacy;
    private TextView tvPrivacyOption, tvRTName, tvRTDesc;

    //LocalBroadcastManager lbm;
    public CameraNewActivity context;
    private long mLastClickTime = 0;
    private boolean isSearchAPICall = true;
    private BaseAPIService searchService;
    public boolean isPublished;
    private String broadCastedShareURL = "";
    private File file;
    private Uri fileUri;
    private String fileUriPath;
    private String fileName;
    private Dialog mConfirmDialog;
    private BottomSheetDialog bottomSheetDialog;
    private SimpleTooltip customTooltip;
    private Handler handler;
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher, galleryActivityResultLauncher, cropActivityResultLauncher;
    private ActivityResultLauncher<String> mPermissionResult;

    public RoundTableVideoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (CameraNewActivity) getActivity();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Bundle args = getArguments();
        if (args != null) {
            videoHeight = args.getInt("videoSizeH");
            videoWidth = args.getInt("videoSizeW");
            videoDuration = args.getInt("videoDuration");
            compressVideoFilePath = args.getString("compressVideoFilePath");
            link = args.getString("link");
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_roundtable_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        initViews();
        prepareViews();
        registerActivityCallBack();
        /*
        *
        if(!context.isBlurbShown) {
            context.isBlurbShown = true;
            tvPrivacyOption.post(() -> {
                float width = tvPrivacyOption.getWidth() / 2f;
                float fortyDp = Utility.dpToPx(24, context);
                showToolTip(width + fortyDp);
            });
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();
        //EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        //EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initViews() {
        imgClose = view.findViewById(R.id.imgClose);
        etRtName = view.findViewById(R.id.c_etRTName);
        etRTDesc = view.findViewById(R.id.c_etRTDesc);
        flexboxLayout = view.findViewById(R.id.flexboxLayout);
        btnPublishRT = view.findViewById(R.id.btnPublishRT);

        scrollSuccessPage = view.findViewById(R.id.scrollSuccessPage);
        linearWhiteDummy = view.findViewById(R.id.linearWhiteDummy);
        flSuccessClose = view.findViewById(R.id.flSuccessClose);
        ivThumbnail = view.findViewById(R.id.ivThumbnail);
        btnDone = view.findViewById(R.id.btnDone);
        //llDp = view.findViewById(R.id.llDp);
        //tvChangeDp = view.findViewById(R.id.tvChangeDp);
        llWhoCanSee = view.findViewById(R.id.llWhoCanSee);
        ivPrivacyType = view.findViewById(R.id.ivPrivacyType);
        tvPrivacyOption = view.findViewById(R.id.tvPrivacyOption);
        llPrivacyOption = view.findViewById(R.id.llPrivacyOption);
        ivHeaderPrivacy = view.findViewById(R.id.ivHeaderPrivacy);
        //llSuccessDp = view.findViewById(R.id.llSuccessDp);
        tvRTName = view.findViewById(R.id.tvRTName);
        tvRTDesc = view.findViewById(R.id.tvRTDesc);
        etRtName.isSetTextRequired(true);
        etRTDesc.isSetTextRequired(true);
    }

    private void setStarColor() {
        Spannable wordToSpan = new SpannableString(context.getResources().getString(R.string.roundtable_name));
        wordToSpan.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.trim_border, null)), wordToSpan.length() - 1, wordToSpan.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        Spannable wordToSpan1 = new SpannableString(context.getResources().getString(R.string.roundtable_description));
        wordToSpan1.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.trim_border, null)), wordToSpan1.length() - 1, wordToSpan1.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void prepareViews() {
        setStarColor();
        if (!context.rtName.isEmpty()) {
            etRtName.setText(context.rtName);
            etRtName.setTextCounter(String.format(Locale.ENGLISH, "%d/24", context.rtName.length()));
        }
        if (!context.rtDesc.isEmpty()) {
            etRTDesc.setText(context.rtDesc);
            etRTDesc.setTextCounter(String.format(Locale.ENGLISH, "%d/80", context.rtDesc.length()));
        }

        if (context.privacyOptionsTypeRT == PrivacyOptionsType.EVERYONE) {
            ivPrivacyType.setImageResource(R.drawable.ic_globe);
            ivHeaderPrivacy.setImageResource(R.drawable.ic_globe);
            tvPrivacyOption.setText(context.getResources().getString(R.string.everyone));
        } else {
            ivPrivacyType.setImageResource(R.drawable.ic_icon_link);
            ivHeaderPrivacy.setImageResource(R.drawable.ic_icon_link);
            tvPrivacyOption.setText(context.getResources().getString(R.string.unlisted));
        }

        setButtonEnableDisable();
        /*if (!context.imageToSet.isEmpty()) {
            llDp.setDpWithImage(context, false, context.imageToSet, context.imageToSet, false);
        } else {
            llDp.setEmptyDp();
        }*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            etRtName.setEdRequestFocus(View.FOCUS_DOWN);
        }

        showKeyboardForCustomEditText(context, etRtName);
        imgClose.setOnClickListener(this);
        flSuccessClose.setOnClickListener(this);
        btnPublishRT.setOnClickListener(this);
        btnDone.setOnClickListener(this);
        //llDp.setOnClickListener(this);
        //tvChangeDp.setOnClickListener(this);
        llWhoCanSee.setOnClickListener(this);

        etRTDesc.setOnTextChangeListener(new CustomEditTextWithError.CustomEditTextWithErrorListeners() {
            @Override
            public void beforeTextChange(@Nullable CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChange(@Nullable CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChange(@Nullable Editable s) {
                if (!TextUtils.isEmpty(s) && s.length() > 0) {
                    etRTDesc.setTextCounter((String.format(Locale.ENGLISH, "%d/80", s.length())));
                } else {
                    etRTDesc.setTextCounter(context.getResources().getString(R.string.zero_80));
                }
                setButtonEnableDisable();
                if (isSearchAPICall) {
                    callForSearchTags();
                }
            }

            @Override
            public void setOnFocusChangeListener(@NonNull View v, boolean hasFocus) {

            }
        });

        etRTDesc.setOnTouchListener((v, event) -> {
            if (etRTDesc.hasFocus()) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                }
            }
            return false;
        });

        etRtName.setOnTextChangeListener(
                new CustomEditTextWithError.CustomEditTextWithErrorListeners() {
                    @Override
                    public void beforeTextChange(@Nullable CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChange(@Nullable CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void afterTextChange(@Nullable Editable s) {
                        if (!TextUtils.isEmpty(s) && s.length() > 0) {
                            etRtName.setTextCounter(String.format(Locale.ENGLISH, "%d/24", s.length()));
                        } else {
                            etRtName.setTextCounter(context.getResources().getString(R.string.zero_24));
                        }
                        setButtonEnableDisable();
                    }

                    @Override
                    public void setOnFocusChangeListener(@NonNull View v, boolean hasFocus) {
                        if (hasFocus) {
                            flexboxLayout.removeAllViews();
                        }
                    }
                }
        );
    }

    private void showToolTip(float rightMargin) {
        customTooltip = new SimpleTooltip.Builder(context)
                .anchorView(llPrivacyOption)
                .text(context.getResources().getString(R.string.visibility_options_blurb_text))
                .gravity(Gravity.TOP)
                .margin(context.getResources().getDimension(R.dimen.dimen_1dp))
                .animated(false)
                .dismissOnOutsideTouch(true)
                .dismissOnInsideTouch(true)
                .arrowHeight(Utility.dpToPx(10, context))
                .arrowWidth(Utility.dpToPx(10, context))
                .arrowColor(context.getResources().getColor(R.color.colorWhite, null))
                .backgroundColor(context.getResources().getColor(R.color.colorWhite, null))
                .textColor(context.getResources().getColor(R.color.black_111111, null))
                .arrowMargin(rightMargin)
                .ignoreOverlay(true)
                .build();
        customTooltip.show();
        if (handler != null) {
            handler.removeCallbacks(null);
        }
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (isAdded()) {
                if (customTooltip.isShowing()) {
                    customTooltip.dismiss();
                }
            }
        }, 4000);
    }

    private void registerActivityCallBack() {
        cameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        cropImage();
                    }
                });

        galleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        assert result.getData() != null;
                        Uri selectedImage = result.getData().getData();
                        InputStream imageStream = null;
                        try {
                            imageStream = context.getContentResolver().openInputStream(selectedImage);
                        } catch (FileNotFoundException e) {
                            showLogException(e);
                        }

                        if (imageStream != null) {
                            file = getOutputMediaFile();
                            fileUri = getOutputMediaFileUri();
                            copyStreamToFile(imageStream, file);
                            cropImage();
                        }
                    }
                });

        cropActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        assert result.getData() != null;
                        try {
                            Bitmap photo = ImageUtils.handleSamplingAndRotationBitmap(context, fileUri,
                                    fileUriPath);
                            context.imageToSet = ImageUtils.saveImage(photo, fileName, context.getApplicationContext());
                            //llDp.setDpWithImage(context, false, context.imageToSet, context.imageToSet, false);
                            //sendSegmentLogs(Constants.CREATE_LOOP_DISPLAY_PICTURE_CHANGED, new Properties());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        mPermissionResult = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (result) {
                        openCamera();
                    } else {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(context,
                                Manifest.permission.CAMERA)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle(R.string.camera_permission_dialog_title);
                            builder.setPositiveButton(R.string.txt_toolbar_settings, (dialogInterface, i) -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            });
                            builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
                            builder.setMessage(R.string.not_allowed_camera);
                            builder.setOnCancelListener(DialogInterface::dismiss);
                            builder.show();
                        }
                    }
                });
    }
    private void removeDp() {
        context.imageToSet = "";
        //llDp.setEmptyDp();
        //sendSegmentLogs(Constants.CREATE_LOOP_DISPLAY_PICTURE_CHANGED, new Properties());
    }

    private void showConfirmDialog() {
        mConfirmDialog = new Dialog(context);
        mConfirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mConfirmDialog.setContentView(R.layout.common_simple_dialog_new);
        mConfirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mConfirmDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mConfirmDialog.show();

        CustomTextView tvTitle = mConfirmDialog.findViewById(R.id.dialog_title);
        CustomTextView tvMsg = mConfirmDialog.findViewById(R.id.dialog_message);
        tvMsg.setVisibility(View.GONE);
        CustomTextView btnCancel = mConfirmDialog.findViewById(R.id.dialog_btn_cancel);
        CustomTextView btnYes = mConfirmDialog.findViewById(R.id.dialog_btn_yes);

        tvTitle.setText(getResources().getString(R.string.remove_photo_title));
        btnYes.setText(getResources().getString(R.string.btn_remove));
        btnCancel.setOnClickListener(v -> mConfirmDialog.dismiss());
        btnYes.setOnClickListener(v -> {
            mConfirmDialog.dismiss();
            context.imageToSet = "";
            bottomSheetDialog.dismiss();
            removeDp();
        });
    }

//    private void openBottomSheetDialog() {
//        View bottomSheetView = getLayoutInflater().inflate(R.layout.alert_profile_image_selection, null);
//        CustomTextView txtSelect = bottomSheetView.findViewById(R.id.txtSelect);
//        CustomTextView txtCancel = bottomSheetView.findViewById(R.id.txtCancel);
//        CustomTextView txtCapture = bottomSheetView.findViewById(R.id.txtCapture);
//        LinearLayout llRemovePhoto = bottomSheetView.findViewById(R.id.llRemovePhoto);
//        if (!TextUtils.isEmpty(context.imageToSet)) {
//            llRemovePhoto.setVisibility(View.VISIBLE);
//        }
//        txtSelect.setOnClickListener(v -> {
//            selectImage();
//            bottomSheetDialog.dismiss();
//        });
//        bottomSheetDialog = new BottomSheetDialog(context, R.style.SheetDialog1);
//        bottomSheetDialog.setContentView(bottomSheetView);
//        bottomSheetDialog.setCancelable(true);
//        bottomSheetDialog.show();
//        txtCapture.setOnClickListener(
//                v -> {
//                    captureImage();
//                    bottomSheetDialog.dismiss();
//                }
//        );
//        txtCancel.setOnClickListener(
//                v -> bottomSheetDialog.dismiss()
//        );
//
//        llRemovePhoto.setOnClickListener(v -> {
//            bottomSheetDialog.dismiss();
//            showConfirmDialog();
//        });
//    }

    private void captureImage() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            mPermissionResult.launch(Manifest.permission.CAMERA);
        } else {
            try {
                openCamera();
            } catch (SecurityException exception) {
                showLogException(exception);
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = getOutputMediaFile();
        fileUri = getOutputMediaFileUri();
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        cameraIntent.putExtra("com.google.assistant.extra.USE_FRONT_CAMERA", true);
        cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);

        // Samsung
        cameraIntent.putExtra("camerafacing", "front");
        cameraIntent.putExtra("previous_mode", "front");

        // Huawei
        cameraIntent.putExtra("default_camera", "1");
        cameraIntent.putExtra("default_mode", "com.huawei.camera2.mode.photo.PhotoMode");

        cameraActivityResultLauncher.launch(cameraIntent);
    }

    private void selectImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        galleryActivityResultLauncher.launch(galleryIntent);
    }

    private Uri getOutputMediaFileUri() {
        return FileProvider.getUriForFile(
                context, "",
                getOutputMediaFile()
        );
    }

    private File getOutputMediaFile() {
        //final String userId = SharedPrefUtils.getStringPreference(context, Constants.PREF_USER);
        final String userId = SDKInitiate.INSTANCE.getUserId();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        fileName = userId + "_" + timeStamp + ".jpg";
        File mediaStorageDir = context.getExternalFilesDir(Constants.POSTS_IMAGES_DIRECTORY);
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName);
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }
        fileUriPath = mediaFile.getPath();

        return new File(fileUriPath);
    }

    public void cropImage() {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            context.getApplicationContext().grantUriPermission("com.android.camera",
                    fileUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cropIntent.setDataAndType(fileUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 1024);
            cropIntent.putExtra("outputY", 1024);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            cropIntent.putExtra("return-data", false);
            cropActivityResultLauncher.launch(cropIntent);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException ane) {
            // display an error message
            try {
                Bitmap photo = ImageUtils.handleSamplingAndRotationBitmap(context, fileUri,
                        fileUriPath);
                context.imageToSet = ImageUtils.saveImage(photo, fileName, context.getApplicationContext());
                //llDp.setDpWithImage(context, false, context.imageToSet, context.imageToSet, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Copy an InputStream to a File.
    private void copyStreamToFile(InputStream in, File file) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if (out != null) {
                    out.close();
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addViewToFlex(String newTag) {
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        TextView textView = new TextView(context);
        textView.setText(String.format("#%s", newTag));
        TextViewCompat.setTextAppearance(textView, R.style.fontForFlexLayout);
        linearLayout.addView(textView);
        FlexboxLayout.LayoutParams lpRight = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT);
        lpRight.setMargins(5, 0, 5, 0);
        linearLayout.setLayoutParams(lpRight);
        linearLayout.setPadding(10, 10, 10, 10);

        linearLayout.setOnClickListener(v -> {
            isSearchAPICall = false;
            int startSelection = etRTDesc.edSelectionStart();
            String selectedWord = "";
            int length = 0;
            for (String currentWord : etRTDesc.getText().toString().split(" ")) {
                System.out.println(currentWord);
                length = length + currentWord.length() + 1;
                if (length > startSelection) {
                    selectedWord = currentWord;
                    break;
                }

            }
            String toReplaceStr = "";

            if (length == 0 || length == startSelection) {
                length = length + 1;
            }
            //    toReplaceStr = "#" + v.getTag().toString().trim() + " ";

            Editable editable = etRTDesc.edGetText();
            editable.replace(length - selectedWord.length() - 1, length - 1, toReplaceStr);
            etRTDesc.setText(editable.toString());
            int position = length - selectedWord.length() + toReplaceStr.length() - 1;
            Selection.setSelection(etRTDesc.edGetText(), Math.min(position, 80));
            flexboxLayout.removeView(v);
            isSearchAPICall = true;
        });
        linearLayout.setTag(newTag);
        flexboxLayout.addView(linearLayout);
    }

    private void callForSearchTags() {
        try {
            if (etRTDesc.getText().trim().length() > 0) {
                try {
                    String[] searchArr = etRTDesc.getText().trim().split(" ");
                    String searchText = "";
                    if (searchArr.length > 0) {
                        searchText = searchArr[searchArr.length - 1];
                    }
                    if (searchService != null) {
                        searchService.cancelCall();
                    }
                    searchService = new BaseAPIService(context, Constants.AUTO_SUGGESTIONS, searchText, new ResponseListener() {
                        @Override
                        public void onSuccess(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray dataJson;
                                dataJson = jsonObject.getJSONArray(Constants.JSON_DATA);

                                if (dataJson.length() > 0) {
                                    flexboxLayout.removeAllViews();
                                    if (etRtName.edIsFocused()) {
                                        return;
                                    }
                                    for (int i = 0; i < dataJson.length(); i++) {
                                        addViewToFlex(dataJson.getString(i));
                                    }
                                } else {
                                    flexboxLayout.removeAllViews();
                                }
                            } catch (Exception e) {
                                showLogException(e);
                            }
                        }

                        @Override
                        public void onFailure(String error) {

                        }
                    }, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                flexboxLayout.removeAllViews();
            }

        } catch (Exception e) {
            showLogException(e);
        }
    }

    private void setButtonEnableDisable() {
        if (!TextUtils.isEmpty(etRtName.getText().trim()) && !TextUtils.isEmpty(etRTDesc.getText().toString().trim())) {
            btnPublishRT.setRippleColor(getResources().getColorStateList(R.color.white_opacity20, null));
            btnPublishRT.setBackgroundColor(getResources().getColor(R.color.splash_background, null));
        } else {
            btnPublishRT.setRippleColor(getResources().getColorStateList(R.color.transparent, null));
            btnPublishRT.setBackgroundColor(getResources().getColor(R.color.splash_bg_opacity20, null));
        }
    }

    private void finishCameraActivity() {
        context.cleanUpMemory();
        //EventBus.getDefault().post(new GoToInboxEvent());
        Intent resultIntent = new Intent();
        context.setResult(Activity.RESULT_OK, resultIntent);
        context.finish();
    }

    public void backManage() {
        Utility.hideKeyboard(context);
        if (searchService != null) {
            searchService.cancelCall();
        }
        if (customTooltip != null && customTooltip.isShowing()) {
            customTooltip.dismiss();
        }
        if (isPublished) {
            finishCameraActivity();
        } else {
            context.rtName = etRtName.getText().trim();
            context.rtDesc = etRTDesc.getText().toString().trim();
            context.getSupportFragmentManager().popBackStack();
            if (context.mergeAndPlayFragment != null) {
                context.mergeAndPlayFragment.setVideoPlay();
            }
            //context.sendSegmentBackLogs(Constants.RT_CREATE_CLOSED, Constants.SCREEN_RT_PUBLISH);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        int id = v.getId();
        if (id == R.id.imgClose || id == R.id.flSuccessClose || id == R.id.btnDone) {
            backManage();
        } else if (id == R.id.btnPublishRT) {
            if (!TextUtils.isEmpty(etRtName.getText().trim()) && !TextUtils.isEmpty(etRTDesc.getText().toString().trim())) {
                context.rtName = etRtName.getText().toString().trim();
                context.rtDesc = etRTDesc.getText().toString().trim();
                if (searchService != null) {
                    searchService.cancelCall();
                    searchService = null;
                }
                context.insertAndUploadLoopVideo(compressVideoFilePath, videoDuration, link, videoWidth, videoHeight);
                showSuccessPage();
                new Handler(Looper.getMainLooper()).postDelayed(() -> Utility.hideKeyboard(context), 100);
            }
        } else if (id == R.id.btnShare) {
            //                if (Utility.getDBHelper() != null) {
//                    if (!TextUtils.isEmpty(broadCastedShareURL)) {
//                        Utility.shareVideoLink(context, broadCastedShareURL, "-101", Constants.FROM_ROUND_TABLE);
//                    } else {
//                        String shareURL = Utility.getDBHelper().getShareURLForRT(compressVideoFilePath);
//                        if (!TextUtils.isEmpty(shareURL)) {
//                            Utility.shareVideoLink(context, shareURL, "-101", Constants.FROM_ROUND_TABLE);
//                        } else {
//                            callGenerateShareURL();
//                        }
//                    }
//                }
        } else if (id == R.id.llDp) {//openBottomSheetDialog(); //|| id == R.id.tvChangeDp
        } else if (id == R.id.llWhoCanSee) {//context.openBottomSheetDialogForPrivacyOptions(ivPrivacyType, tvPrivacyOption, ivHeaderPrivacy);
        }
    }

    private void showSuccessPage() {
        isPublished = true;
        //Confetti shapes removed.
        btnDone.setVisibility(View.VISIBLE);
        scrollSuccessPage.setVisibility(View.VISIBLE);
        linearWhiteDummy.setVisibility(View.VISIBLE);
        /*if (!TextUtils.isEmpty(context.imageToSet)) {
            llSuccessDp.setDpWithImage(context, false, context.imageToSet, context.imageToSet, false);
        } else {
            llSuccessDp.setDpWithInitials(context.rtName, "#A4E6DA", "#49CDB5");
        }*/
        tvRTName.setText(context.rtName);
        tvRTDesc.setText(context.rtDesc);
        //Confetti removed from here.

        try {
            if (!TextUtils.isEmpty(context.coverPhotoPath) && new File(context.coverPhotoPath).exists()) {
                Glide.with(context).asDrawable().load(context.coverPhotoPath).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).into(ivThumbnail);
            } else {
                Bitmap thumb = context.getCoverBitmap(compressVideoFilePath, false);
                if (thumb != null) {
                    ivThumbnail.setImageBitmap(thumb);
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

//    private void callGenerateShareURL() {
//        try {
//            String module = Constants.GENERATE_SHARE_URL;
//            new BaseAPIService(context, module, true, "2", new ResponseListener() {
//                @Override
//                public void onSuccess(String response) {
//                    try {
//                        JSONObject object = new JSONObject(response);
//                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);
//                        if (dataJson.has("share_url")) {
//                            if (!TextUtils.isEmpty(broadCastedShareURL)) {
//                                Utility.shareVideoLink(context, broadCastedShareURL, "-101", Constants.FROM_ROUND_TABLE);
//                            } else {
//                                String shareURL = dataJson.optString("share_url", "");
//                                if (Utility.getDBHelper() != null) {
//                                    String checkURL = Utility.getDBHelper().getShareURLForRT(compressVideoFilePath);
//                                    if (!TextUtils.isEmpty(checkURL)) {
//                                        Utility.shareVideoLink(context, checkURL, "-101", Constants.FROM_ROUND_TABLE);
//                                    } else {
//                                        Utility.getDBHelper().updateShareURLForRT(shareURL, compressVideoFilePath);
//                                        Utility.shareVideoLink(context, shareURL, "-101", Constants.FROM_ROUND_TABLE);
//                                    }
//                                }
//                            }
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(String error) {
//
//                }
//            }, "GET_SHARE_URL", true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    @Subscribe
//    public void onShareURLEvent(ShareURLRTEventEvent event) {
//        String videoPath = event.videoPath;
//        if (videoPath.equalsIgnoreCase(compressVideoFilePath)) {
//            broadCastedShareURL = event.shareURL;
//        }
//    }
}