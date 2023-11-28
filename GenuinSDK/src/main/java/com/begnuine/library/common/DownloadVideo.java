package com.begnuine.library.common;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.begenuine.feedscreensdk.common.Constants;
import com.begnuine.library.R;
import com.begnuine.library.core.interfaces.OnVideoDownload;
import com.begnuine.library.data.model.DiscoverModel;
import com.begnuine.library.data.model.LottieAnimModel;
import com.begnuine.library.data.model.MembersModel;
import com.begnuine.library.data.remote.BaseAPIService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class DownloadVideo {

    private long prevEventTime;
    private Activity context;
    private DiscoverModel selectedVideo;
    private ArrayList<TextView> textViewArrayList;
    private CircleImageView ivWaterMarkProfile;
    private int downloadID = 0;
    private OnVideoDownload onVideoDownload;
    String downloadedVideoPath = "";

    public DownloadVideo(Activity context) {
        this.context = context;
    }

    public void initDownload(DiscoverModel model, ArrayList<TextView> textViewArrayList, CircleImageView ivWaterMarkProfile) {
        this.selectedVideo = model;
        this.textViewArrayList = textViewArrayList;
        this.ivWaterMarkProfile = ivWaterMarkProfile;
        //initDatadog();
    }

    public void setDownloadListener(OnVideoDownload onVideoDownload) {
        this.onVideoDownload = onVideoDownload;
    }

//    private void initDatadog() {
//        HashMap<String, Object> map = new HashMap<String, Object>() {{
//            put("user_id", Utility.getLoggedInUserId(context));
//            put("device_id", Utility.getDeviceId(context));
//            put("event_id", UUID.randomUUID().toString());
//            put("title", Constants.RECORD_PREVIEW_DOWNLOAD_CLICKED);
//            if (prevEventTime != 0) {
//                put("duration", (System.currentTimeMillis() - prevEventTime) / 1000);
//            } else {
//                put("duration", 0);
//            }
//        }};
//        prevEventTime = System.currentTimeMillis();
//       // GenuInApplication.getInstance().sendEventLogs(Constants.RECORD_PREVIEW_DOWNLOAD_CLICKED, map);
//    }

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        } else
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void saveDownloadedVideoToGallery(Activity activity) {
        String videoUrl = selectedVideo.getVideoURL();
        String finalUrl = videoUrl.substring(videoUrl.lastIndexOf('/') + 1);

        File destinationLocation = activity.getExternalFilesDir(Constants.MERGE_DIRECTORY);
        File dirPath = activity.getCacheDir();

        String localPath = destinationLocation.getAbsolutePath() + File.separator + finalUrl;
        String cachedPath = dirPath.getAbsolutePath() + File.separator + finalUrl;
        File localFile = new File(localPath);

        File file = new File(cachedPath);
        if (file.exists()) {
            selectedVideo.setLocalVideoPath(file.getAbsolutePath());
            executeCommand(activity, file.getAbsolutePath());
        } else if (localFile.exists()) {
            selectedVideo.setLocalVideoPath(localFile.getAbsolutePath());
            executeCommand(activity, localFile.getAbsolutePath());
        } else {

            if (!Utility.isNetworkAvailable(context)) {
                if (onVideoDownload != null) {
                    onVideoDownload.onDownloadVideoFailure(-101);
                }
                onVideoDownload = null;
                return;
            }

            if (!BaseAPIService.isShowingProgressDialog())
                BaseAPIService.showProgressDialog(context);

            if (downloadID != 0) {
                BaseAPIService.dismissProgressDialog();
                return;
            }

//            downloadID = PRDownloader.download(videoUrl, dirPath.toString(), File.separator + finalUrl)
//                    .build()
//                    .setOnStartOrResumeListener(() -> {
//                    })
//                    .setOnPauseListener(() -> Utility.showLog("VideoA " + "FeedViewPause", "paused"))
//                    .setOnProgressListener(progress -> {
//                    })
//                    .setOnCancelListener(() -> Utility.showLog("VideoA " + "FeedViewCancel", "Cancelled"))
//                    .start(new OnDownloadListener() {
//                        @Override
//                        public void onDownloadComplete() {
//                            File file = new File(activity.getCacheDir(), finalUrl);
//                            String inputPath = file.getAbsolutePath();
//                            selectedVideo.setLocalVideoPath(inputPath);
//                            executeCommand(activity, inputPath);
//                        }
//
//                        @Override
//                        public void onError(Error error) {
//                            PRDownloader.resume(downloadID);
//                            BaseAPIService.dismissProgressDialog();
//                            if (onVideoDownload != null) {
//                                onVideoDownload.onDownloadVideoFailure(0);
//                            }
//                            onVideoDownload = null;
//                        }
//                    });
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

    private void executeCommand(Activity activity, String inputPath) {
        File destinationLocation = activity.getExternalFilesDir(Constants.DOWNLOAD_DIRECTORY);
        File dest = null;
        if (destinationLocation.exists() || destinationLocation.mkdir()) {
            //String fileName = System.currentTimeMillis() + ".mp4";
            String fileName = "Download_" + inputPath.substring(inputPath.lastIndexOf('/') + 1);
            dest = new File(destinationLocation, fileName);
            downloadedVideoPath = dest.getAbsolutePath();
        }
        if (dest != null && dest.exists()) { //if video has been already downloaded in Downloads directory then take it from there.
            context.runOnUiThread(() -> downloadVideoNew(downloadedVideoPath));
        } else {
            Utility.printErrorLog("dest is null..");
            if (!BaseAPIService.isShowingProgressDialog()) {
                BaseAPIService.showProgressDialog(activity);
            }
            saveFilesIfNotExist(inputPath);
        }
    }

    private void generateWaterMarkCommand(String inputPath) {
        String EFFECT_NAME = "circleclose";
        String userNamePath, genuinLogoPath, fromUserNamePath = "", userImagePath;
        String bgPath, cameraRollPath;
        String userNameWaterMarkPath = "", fullNameWaterMarkPath = "", bioWaterMarkPath = "";
        float exactDuration = getExactDuration(inputPath);
        float exactDurationWithPlus = exactDuration + 0.5f;
        float exactDurationWithMinus = exactDuration - 1.0f;
        float totalDuration = exactDuration + 3f;
        int audioMixPos = 7;
        //int logoWidth = SharedPrefUtils.getIntPreference(context, Constants.PREF_LOGO_WIDTH) + 8;

        File gif = new File(context.getCacheDir(), "genuin_new_logo.gif");
        String gifPath = gif.getAbsolutePath();

        File bg = new File(context.getCacheDir(), "bg.jpg");
        bgPath = bg.getAbsolutePath();

//        File genuinLogo = new File(context.getCacheDir(), "genuin_new_logo.png");
//        genuinLogoPath = genuinLogo.getAbsolutePath();

        File genuinLogo = new File(context.getCacheDir(), "ic_genuin_watermark.png");
        genuinLogoPath = genuinLogo.getAbsolutePath();

        File cameraRoll = new File(context.getCacheDir(), "camera_roll_new.png");
        cameraRollPath = cameraRoll.getAbsolutePath();

        //MembersModel user = Utility.getCurrentUserObject(context, "");
        //TODO: Will get user data from app after login
        MembersModel user = null;
        boolean isRecordForOther = false;
        if (selectedVideo.getRecordedBy() != null && selectedVideo.getRecordedBy().getUserId() != null) {
            isRecordForOther = !user.getUserId().equalsIgnoreCase(selectedVideo.getRecordedBy().getUserId());
        }

        boolean isAnyNonGenuinVideo = false;
        if (selectedVideo.getSettings() != null && selectedVideo.getSettings().getContainsExternalVideos() != null) {
            isAnyNonGenuinVideo = selectedVideo.getSettings().getContainsExternalVideos();
        }

        String userName = isRecordForOther ? selectedVideo.getRecordedBy().getNickname() : user.getNickname();
        String fullName = user.getName();
        String bio = user.getBio();

        File userNameFile = new File(context.getCacheDir(), user.getNickname() + "_18_new.png");
        userNamePath = userNameFile.getAbsolutePath();

        File userNameWaterMark = new File(context.getCacheDir(), user.getNickname() + "_32_new.png");
        userNameWaterMarkPath = userNameWaterMark.getAbsolutePath();

        if (!TextUtils.isEmpty(fullName)) {
            File fullNameWaterMark = new File(context.getCacheDir(), "fullName.png");
            fullNameWaterMarkPath = fullNameWaterMark.getAbsolutePath();
        }

        if (!TextUtils.isEmpty(bio)) {
            File bioWaterMark = new File(context.getCacheDir(), "bio.png");
            bioWaterMarkPath = bioWaterMark.getAbsolutePath();
        }

        File userImage;
        if (user.isAvatar()) {
            userImage = new File(context.getCacheDir(), user.getProfileImage() + "_img.png");
        } else {
            userImage = new File(context.getCacheDir(), user.getNickname() + "_img.png");
        }
        userImagePath = userImage.getAbsolutePath();

        if (isRecordForOther) {
            if (isAnyNonGenuinVideo) {
                File fromUserNameCRoll = new File(context.getCacheDir(), userName + "_camera_new.png");
                fromUserNamePath = fromUserNameCRoll.getAbsolutePath();
            } else {
                File fromUserName = new File(context.getCacheDir(), userName + "_from_new.png");
                fromUserNamePath = fromUserName.getAbsolutePath();
            }
        }

        String complexCommand = "-y";
        complexCommand += " -i " + inputPath;
        complexCommand += " -loop 1 -t " + 3 + " -i " + bgPath;
        complexCommand += " -ignore_loop 0 -i " + gifPath;
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
        complexCommand += "\"[0:v]settb=AVTB,fps=30/1,setpts=PTS-STARTPTS,scale=720:1280:force_original_aspect_ratio=decrease,pad=720:1280:(ow-iw)/2:(oh-ih)/2,setdar=9/16[v0];";
        complexCommand += "[1:v]settb=AVTB,fps=30/1,setpts=PTS-STARTPTS,scale=720:1280:force_original_aspect_ratio=decrease,pad=720:1280:(ow-iw)/2:(oh-ih)/2,setdar=9/16[v1];";
        complexCommand += "[2:v]setpts=PTS-STARTPTS+" + (exactDurationWithPlus) + "/TB[delayedGif];";
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
        complexCommand += "[v0][v1]xfade=transition=" + EFFECT_NAME + ":duration=1:offset=" + (exactDurationWithMinus) + ",format=yuv420p[xF];";
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

        if (isRecordForOther || isAnyNonGenuinVideo) {
            complexCommand += ",overlay=x=W-w-16:y=(H*2)/3:enable='between(t,0," + (exactDuration - 0.5f) + ")'";
        }
        complexCommand += "[v];";
        complexCommand += "[0:a][" + audioMixPos + "]amix[a]\"";
        complexCommand += " -map \"[v]\" -map \"[a]\"";
        complexCommand += " -c:v libx264 -preset ultrafast -c:a aac -b:a 192k ";
        complexCommand += downloadedVideoPath + " -async 1 -vsync 2";

        try {
            final long startTime = System.currentTimeMillis();
            Utility.printErrorLog("Trimming Start: " + startTime);
//            FFmpegKit.executeAsync(complexCommand, session -> {
//                BaseAPIService.dismissProgressDialog();
//                if (ReturnCode.isSuccess(session.getReturnCode())) {
//                    Utility.showLog(Constants.TAG, "Async command execution completed successfully.");
//                    final long totalTime = (System.currentTimeMillis() - startTime) / 1000;
//                    Utility.showLog("TAG", "Time: " + totalTime);
//                    context.runOnUiThread(() -> downloadVideoNew(downloadedVideoPath));
//                } else {
//                    String buffer = session.getOutput();
//                    FFmpegKitConfig.printToLogcat(Log.INFO, buffer);
//                }
//            });
        } catch (Exception e) {
            BaseAPIService.dismissProgressDialog();
        }
    }

    private void saveFilesIfNotExist(String inputPath) {
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
                Bitmap finalLogoBitmap = Utility.loadBitmapFromView(textViewArrayList.get(0));
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
                textViewArrayList.get(1).setText(String.format("%s %s", context.getResources().getString(R.string.slash_from), context.getResources().getString(R.string.camera_roll)));
                Bitmap cameraRollBitmap = Utility.loadBitmapFromView(textViewArrayList.get(1));
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

        //MembersModel user = Utility.getCurrentUserObject(context, "");
        //TODO: Will get user data from app after login
        MembersModel user = null;
        boolean isRecordForOther = false;
        if (selectedVideo.getRecordedBy() != null && selectedVideo.getRecordedBy().getUserId() != null) {
            isRecordForOther = !user.getUserId().equalsIgnoreCase(selectedVideo.getRecordedBy().getUserId());
        }
        boolean isAnyNonGenuinVideo = false;
        if (selectedVideo.getSettings() != null && selectedVideo.getSettings().getContainsExternalVideos() != null) {
            isAnyNonGenuinVideo = selectedVideo.getSettings().getContainsExternalVideos();
        }

        String userName = isRecordForOther ? selectedVideo.getRecordedBy().getNickname() : user.getNickname();
        String fullName = user.getName();
        String bio = user.getBio();
        String userImagePath = user.getProfileImage();
        boolean isAvatar = user.isAvatar();

        File userFile = new File(context.getCacheDir(), user.getNickname() + "_18_new.png");
        if (!userFile.exists()) {
            try {
                textViewArrayList.get(2).setText(String.format("@%s", user.getNickname()));
                Bitmap finalBitmap = Utility.loadBitmapFromView(textViewArrayList.get(2));
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

        File user32File = new File(context.getCacheDir(), user.getNickname() + "_32_new.png");
        if (!user32File.exists()) {
            try {
                textViewArrayList.get(3).setText(String.format("@%s", user.getNickname()));
                Bitmap final32Bitmap = Utility.loadFixedWidthBitmapFromView(textViewArrayList.get(3));
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
                textViewArrayList.get(5).setText(fullName);
                Bitmap finalFullNameBitmap = Utility.loadFixedWidthBitmapFromView(textViewArrayList.get(5));
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
                textViewArrayList.get(6).setText(bio);
                Bitmap finalBioBitmap = Utility.loadFixedWidthBitmapFromView(textViewArrayList.get(6));
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
            if (isAnyNonGenuinVideo) {
                File fromUserNameCameraRollFile = new File(context.getCacheDir(), userName + "_camera_new.png");
                if (!fromUserNameCameraRollFile.exists()) {
                    try {
                        textViewArrayList.get(4).setText(String.format("%s @%s's %s", context.getResources().getString(R.string.slash_from), userName, context.getResources().getString(R.string.camera_roll)));
                        Bitmap finalBitmapFromCameraRoll = Utility.loadBitmapFromView(textViewArrayList.get(4));
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
                File fromUserNameFile = new File(context.getCacheDir(), userName + "_from_new.png");
                if (!fromUserNameFile.exists()) {
                    try {
                        textViewArrayList.get(4).setText(String.format("%s @%s", context.getResources().getString(R.string.slash_from), userName));
                        Bitmap finalBitmapFromUser = Utility.loadBitmapFromView(textViewArrayList.get(4));
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

        if (isAvatar) {
            File userImageFile = new File(context.getCacheDir(), userImagePath + "_img.png");
            if (!userImageFile.exists()) {
                int res = context.getResources().getIdentifier(userImagePath,
                        "raw", context.getPackageName());
                int drawableRes = context.getResources().getIdentifier(userImagePath,
                        "drawable", context.getPackageName());
                ivWaterMarkProfile.setCircleBackgroundColorResource(LottieAnimModel.getMapData().get(res));
                ivWaterMarkProfile.setImageResource(drawableRes);
                try {
                    Bitmap finalBitmapImage = Utility.loadBitmapFromView(ivWaterMarkProfile);
                    if (finalBitmapImage != null) {
                        FileOutputStream out = new FileOutputStream(userImageFile);
                        finalBitmapImage.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                        finalBitmapImage.recycle();
                        generateWaterMarkCommand(inputPath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                generateWaterMarkCommand(inputPath);
            }
        } else {
            File userImageFile = new File(context.getCacheDir(), user.getNickname() + "_img.png");
            if (!userImageFile.exists()) {
                Glide.with(context).asBitmap().load(userImagePath).apply(new RequestOptions().override(456, 456)).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        try {
                            ivWaterMarkProfile.setImageBitmap(resource);
                            Bitmap finalBitmapImage = Utility.loadBitmapFromView(ivWaterMarkProfile);
                            if (finalBitmapImage != null) {
                                FileOutputStream out = new FileOutputStream(userImageFile);
                                finalBitmapImage.compress(Bitmap.CompressFormat.PNG, 100, out);
                                out.flush();
                                out.close();
                                finalBitmapImage.recycle();
                                generateWaterMarkCommand(inputPath);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
            } else {
                generateWaterMarkCommand(inputPath);
            }
        }
    }

    private void downloadVideoNew(String filePath) {
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
                        BaseAPIService.dismissProgressDialog();
                    }
                }
                contentValues.clear();
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
                if (videoUri != null) {
                    resolver.update(videoUri, contentValues, null, null);
                }

//                 String deviceId = Utility.getDeviceId(context);
//                 HashMap<String, Object> mapp = new HashMap<String, Object>() {{
//                    put("user_id", Utility.getLoggedInUserId(context));
//                    put("device_id", deviceId);
//                    put("event_id", UUID.randomUUID().toString());
//                    put("title", Constants.RECORD_PREVIEW_DOWNLOAD_STATUS);
//                    if (prevEventTime != 0) {
//                        put("duration", (System.currentTimeMillis() - prevEventTime) / 1000);
//                    } else {
//                        put("duration", 0);
//                    }
//                    put("status", "success");
//                }};
//                GenuInApplication.getInstance().sendEventLogs(Constants.RECORD_PREVIEW_DOWNLOAD_STATUS, mapp);
                prevEventTime = System.currentTimeMillis();

                if (onVideoDownload != null) {
                    onVideoDownload.onSuccessfullyDownloadVideo();
                }
                onVideoDownload = null;
                BaseAPIService.dismissProgressDialog();
            } else {
                File destinationLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Genuin");
                if (destinationLocation.exists() || destinationLocation.mkdir()) {
                    String fileName = System.currentTimeMillis() + ".mp4";
                    File destFile = new File(destinationLocation, fileName);
                    downloadFile(sourceFile, destFile);
                    if (onVideoDownload != null) {
                        onVideoDownload.onSuccessfullyDownloadVideo();
                    }
                    onVideoDownload = null;
                    BaseAPIService.dismissProgressDialog();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            BaseAPIService.dismissProgressDialog();
        }
    }

    private void downloadFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        try (FileChannel source = new FileInputStream(sourceFile).getChannel(); FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        } catch (Exception e) {
            Utility.showLogException(e);
            BaseAPIService.dismissProgressDialog();
        }
    }
}