package com.begenuin.library.views.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.begenuin.library.R;
import com.begenuin.library.common.CameraUtil;
import com.begenuin.library.common.DefaultExecutorSupplier;
import com.begenuin.library.common.Utility;
import com.begenuin.library.common.customViews.AutoFitTextureView;
import com.begenuin.library.common.customViews.CustomLeftRightSwipeGesture;
import com.begenuin.library.common.customViews.OverlayView;
import com.begenuin.library.core.interfaces.Camera2Listener;
import com.begenuin.library.core.interfaces.OnSwipeGestureListener;
import com.begenuin.library.databinding.FragmentCameraNewBinding;
import com.begenuin.library.views.activities.CameraNewActivity;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public abstract class Camera2Fragment extends Fragment implements Camera2Listener {

    CameraManager mCameraManager;
    public static final String TAG = "Camera2Fragment";
    //constant for defining the time duration between the click that can be considered as double-tap
    static final int MAX_DURATION = 300;
    private LinearLayout lnrPermission;
    private AutoFitTextureView mCameraLayout;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mPreviewSession;
    private Size mPreviewSize;
    private Size mVideoSize;
    private CaptureRequest.Builder mPreviewBuilder;
    protected ImageReader mImageReader;
    protected MediaRecorder mMediaRecorder;
    protected AudioRecord mMediaAudioRecorder;
    private int finalBufferSize;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private final Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private Camera2Listener mCamera2Listener;
    int brightnessValue, brightnessOriginal;
    int clickCount = 0;
    //variable for storing the time of first click
    long startTime;
    CameraCharacteristics characteristics = null;
    protected boolean isFlashBlink;
    protected boolean isFlashOn;
    private boolean isCameraOpen;
    private boolean upsideDown;
    private boolean isRecording;
    public boolean isViewsDisable;
    public boolean isExpanded;
    private Rect rect;
    private Matrix mFaceDetectionMatrix;
    private String cameraId;
    protected float fingerSpacing = 0;
    protected float zoomLevel = 1f;
    protected float maximumZoomLevel;
    protected Rect zoom;
    protected boolean isMediaRecorderPrepared;

    protected int maxAmplitude = 0;

    private final int maxRecordDuration = 60000;
    public int mCameraFacing = CameraCharacteristics.LENS_FACING_BACK;
    private final static int AUDIO_LEN_IN_SECOND = 6;
    private final static int SAMPLE_RATE = 16000;
    private final static int RECORDING_LENGTH = SAMPLE_RATE * AUDIO_LEN_IN_SECOND;

    public abstract AutoFitTextureView getTextureView();

    public abstract LinearLayout getLLAudio();

    public abstract RelativeLayout getRLText();

    public abstract OverlayView getOverlayView();

    public abstract int getTextureResource();

    public abstract int getPermissionResources();

    public abstract void collapseAnimation();

    public abstract void onSwipeLeft();

    public abstract void onSwipeRight();

    public abstract void onSwipeTop();

    public abstract void onScroll(float diffY);

    public abstract void onSwipeCancel();

    public abstract void onImageCaptured();

    public abstract View getParentView();

    private CameraNewActivity context;

    private boolean isAspectRatioSetUpDone;

    // Listeners
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
            mCameraOpenCloseLock.release();
            if (getActivity() != null && getActivity() instanceof CameraNewActivity) {
                CameraNewActivity activity = (CameraNewActivity) getActivity();
                if (!activity.isDataDogLogged) {
                    activity.isDataDogLogged = true;
                   // Utility.sendDataDogLatencyLogs(Constants.CAMERA_SCREEN_LOADED, activity.startMillis);
                }
            }

            if (null != mCameraLayout) {
                configureTransform(mCameraLayout.getWidth(), mCameraLayout.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (activity instanceof CameraNewActivity) {
                closeAndReopenCamera();
            } else if (null != activity) {
                activity.finish();
            }
        }
    };

    public abstract File getVideoFile(Context context);

    public abstract File getAudioFile(Context context);

    public abstract File getPhotoFile(Context context);

    public abstract File getCurrentVideoFile();

    public abstract File getGalleryVideoFile();

    public int getCameraFacing() {
        return mCameraFacing;
    }

    public void setCameraFacing(int mCameraFacing) {
        this.mCameraFacing = mCameraFacing;
    }

    public Size getVideoSize() {
        return mVideoSize;
    }

    public Size getPreviewSize() {
        return mVideoSize;
    }

    public void startRecordingVideo() throws IllegalStateException {
        if (context.isAudioReply()) {
            if (mMediaAudioRecorder == null) {
                isRecording = true;
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    if (SAMPLE_RATE > bufferSize) {
                        bufferSize = SAMPLE_RATE;
                    }
                    mMediaAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
                    if (mMediaAudioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
                        return;
                    }
                    finalBufferSize = bufferSize;
//                    mMediaAudioRecorder.setPositionNotificationPeriod(finalBufferSize);
//                    mMediaAudioRecorder.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
//                        @Override
//                        public void onMarkerReached(AudioRecord audioRecord) {
//
//                        }
//
//                        @Override
//                        public void onPeriodicNotification(AudioRecord audioRecord) {
//                            audioData = new short[finalBufferSize];
//                            audioRecord.read(audioData, 0, finalBufferSize);
//                            setTranscribe(audioData);
//                        }
//                    });
                    mMediaAudioRecorder.startRecording();
                    writeAudioData();
                });
            }
        } else {
            if (mMediaRecorder != null) {
                isRecording = true;
                // Start recording
                mMediaRecorder.start();
            }
        }
    }

    public void captureImage() {
        try {
            final List<Surface> outputSurfaces = new ArrayList<>();
            outputSurfaces.add(mImageReader.getSurface());
            outputSurfaces.add(new Surface(mCameraLayout.getSurfaceTexture()));
            final CaptureRequest.Builder mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mPreviewBuilder.addTarget(mImageReader.getSurface());
            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            int rotation = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                rotation = context.getDisplay().getRotation();
            }
            int orientation = CameraUtil.getOrientation(rotation, upsideDown);
            mPreviewBuilder.set(CaptureRequest.JPEG_ORIENTATION, orientation);
            if (zoom != null) {
                mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
            }
            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                mPreviewSession = session;
                                mPreviewSession.stopRepeating();
                                mPreviewSession.capture(mPreviewBuilder.build(), mSessionCaptureCallback, mBackgroundHandler);
                            } catch (final CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        }
                    }
                    , mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                context.runOnUiThread(Camera2Fragment.this::onImageCaptured);
            }
        }
    }

    /*private void setTranscribe(short[] audioData) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            float[] floatInputBuffer = new float[finalBufferSize];
            // feed in float values between -1.0f and 1.0f by dividing the signed 16-bit inputs.
            for (int i = 0; i < finalBufferSize; ++i) {
                floatInputBuffer[i] = audioData[i] / (float) Short.MAX_VALUE;
            }
            String waveToText = recognize(floatInputBuffer);
            Utility.showLog("Transcribe", waveToText);
        });
    }*/

    public void stopRecordingVideo() {
        stopRecordingVideo(false);
    }

    public void stopRecordingVideo(boolean kill) {
        // Stop Recording
        if (context.isAudioReply()) {
            try {
                if (mMediaAudioRecorder != null && isRecording) {
                    mMediaAudioRecorder.stop();
                    mMediaAudioRecorder.release();
                }
                mMediaAudioRecorder = null;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            isRecording = false;
        } else {
            closeCamera();
            isRecording = false;
            if (!kill) {
                openCamera(mCameraLayout.getWidth(), mCameraLayout.getHeight());
            }
        }
    }

    private final CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            Utility.showLog("Capture", "Started");
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
//            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
//            process(result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
        }
    };

    private void process(CaptureResult result) {
        Integer mode = result.get(CaptureResult.STATISTICS_FACE_DETECT_MODE);
        Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
        if (faces != null && mode != null) {
            if (faces.length > 0) {
                for (int i = 0; i < faces.length; i++) {
                    if (faces[i].getScore() > 50) {
                        Utility.showLog("Test", "faces : " + faces.length + " , mode : " + mode);
                        int left = faces[i].getBounds().left;
                        int top = faces[i].getBounds().top;
                        int right = faces[i].getBounds().right;
                        int bottom = faces[i].getBounds().bottom;
                        //float points[] = {(float)left, (float)top, (float)right, (float)bottom};

                        Rect uRect = new Rect(left, top, right, bottom);
                        RectF rectF = new RectF(uRect);
                        mFaceDetectionMatrix.mapRect(rectF);
                        //mFaceDetectionMatrix.mapPoints(points);
                        rectF.round(uRect);
                        //uRect.set((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
                        Utility.showLog("Test", "Activity rect" + i + " bounds: " + uRect);

                        final Rect rect = uRect;
                        context.runOnUiThread(() -> {
                            getOverlayView().setRect(rect);
                            getOverlayView().requestLayout();
                        });
                        break;
                    }
                }
            }
        }
    }

    public void openCamera() {
        openCamera(mCameraLayout.getWidth(), mCameraLayout.getHeight());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (CameraNewActivity) getActivity();
       // cameraFragmentBinding = new DataBindingUtil(new LayoutInflater(getContext()), R.layout.fragment_camera_new, null, false);
    }

    public abstract void doubleTapped();

    public abstract void isZoomSelected(boolean select);

    public abstract boolean isFirstTime();

    public abstract void pullerDragEnabled(boolean enable);

    public void stopRecordingCamera() {
        if (context.isAudioReply()) {
            try {
                if (mMediaAudioRecorder != null && isRecording) {
                    mMediaAudioRecorder.stop();
                    mMediaAudioRecorder.release();
                }
                mMediaAudioRecorder = null;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } else {
            closeCamera();
        }
        isRecording = false;
    }

    private final CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            mPreviewSession = cameraCaptureSession;
            updatePreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            Activity activity = getActivity();
            if (null != activity) {
                mCamera2Listener.onConfigurationFailed();
            }
        }

//        @Override
//        public void onClosed(@NonNull CameraCaptureSession session) {
//            super.onClosed(session);
//            stopBackgroundThread();
//        }
    };

    public void initCamera() {
        if (context != null) {
            context.startMillis = System.currentTimeMillis();

            if (mCameraLayout == null)
                mCameraLayout = getTextureView();//try to get camera layout if null
            if (!isCameraOpen) {
                if (mCameraLayout == null)
                    return;
                startBackgroundThread();
                if (mCameraLayout.isAvailable()) {
                    openCamera(mCameraLayout.getWidth(), mCameraLayout.getHeight());
                } else {
                    mCameraLayout.setSurfaceTextureListener(mSurfaceTextureListener);
                }
            }
        }
    }

    public void pauseCamera() {
        if (isRecording) {
            stopRecordingVideo(true);
            stopBackgroundThread();
        } else {
            if (isCameraOpen) {
                closeCamera();
                stopBackgroundThread();
            }
            if (!isAspectRatioSetUpDone && context.isReplyReactionWithoutVideo()) {
                setUpCamera();
            }
        }
    }

    @Override
    public void onPause() {
        pauseCamera();
        super.onPause();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
                mCamera2Listener.onInterruptedException(e);
            }
        }
    }

    protected void requestPermissions() {
        if (context.isTextReply()) {
            return;
        }
        if (context.isPhotoReply()) {
           // SharedPrefUtils.setBoolPreference(context, Constants.PREF_IS_CAMERA_PERMISSION_ASKED, true);
            ActivityCompat.requestPermissions(context, Camera2PermissionDialog.PHOTO_PERMISSIONS,
                    Camera2PermissionDialog.REQUEST_PERMISSIONS);
        } else if (context.isAudioReply()) {
            //SharedPrefUtils.setBoolPreference(context, Constants.PREF_IS_MIC_PERMISSION_ASKED, true);
            ActivityCompat.requestPermissions(context, Camera2PermissionDialog.AUDIO_PERMISSIONS,
                    Camera2PermissionDialog.REQUEST_PERMISSIONS);
        } else {
           // SharedPrefUtils.setBoolPreference(context, Constants.PREF_IS_CAMERA_PERMISSION_ASKED, true);
            //SharedPrefUtils.setBoolPreference(context, Constants.PREF_IS_MIC_PERMISSION_ASKED, true);
            ActivityCompat.requestPermissions(context, Camera2PermissionDialog.VIDEO_PERMISSIONS,
                    Camera2PermissionDialog.REQUEST_PERMISSIONS);
        }
    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeCamera();
    }

    public void resumeCamera() {
        if (isCameraOpen)
            closeCamera();

        if (context != null) {
            if (context.isReplyReactionWithoutVideo()) {
                startBackgroundThread();
                setUpCamera();
            } else {
                if (context.isRequiredPermissionsGranted()) {
                    mCameraLayout.setVisibility(View.VISIBLE);
                    initCamera();
                } else if (lnrPermission != null && lnrPermission.getVisibility() == View.VISIBLE) {
                    showPermissionView(View.VISIBLE);
                }
            }
        }
    }

    public void switchCameraNew(ImageView imgSwitchCamera) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            upsideDown = false;
            closeCamera();
            if (mCameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
                mCameraFacing = CameraCharacteristics.LENS_FACING_FRONT;
            } else {
                mCameraFacing = CameraCharacteristics.LENS_FACING_BACK;
            }
            rotateImage(imgSwitchCamera);
            deleteCurrentFileAndOpenCamera();
        });

    }

    private void openCamera(final int width, final int height) {
        DefaultExecutorSupplier.getInstance().forMainThreadTasks(0).execute(() -> {
            try {
                context.runOnUiThread(() -> isZoomSelected(false));
                setUpCamera();
                if (context.isReplyReactionWithoutVideo()) {
                    return;
                }
                if (!isCameraOpen) {
                    configureTransform(width, height);
                    if (context.isPhotoReply()) {
                        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.JPEG, 1);
                        mImageReader.setOnImageAvailableListener(onImageAvailableListener, mBackgroundHandler);
                    } else {
                        if (mMediaRecorder == null) {
                            mMediaRecorder = new MediaRecorder();
                        }
                    }
                    mCameraManager.openCamera(cameraId, mStateCallback, mBackgroundHandler);
                    isCameraOpen = true;
                } else {
                    closeCamera();
                }
            } catch (CameraAccessException cae) {
                cae.printStackTrace();
                mCamera2Listener.onCameraException(cae);
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                mCamera2Listener.onNullPointerException(npe);
            } catch (SecurityException e) {
                requestPermissions();
                e.printStackTrace();
                isCameraOpen = false;
            }
        });
    }

    private void setAspectRationAdjustment() {
        int[] screenWidthHeight = new int[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            screenWidthHeight = Utility.getScreenWidthHeight(context);
        }

        float screenWidth = screenWidthHeight[0];
        float screenHeight = screenWidthHeight[1];
        float videoWidth = mVideoSize.getWidth();
        float videoHeight = mVideoSize.getHeight();

        Utility.printErrorLog("~~~~ Ratio: video Width: " + videoWidth + " Height: " + videoHeight);
        Utility.printErrorLog("~~~~ Ratio: screen Width: " + screenWidth + " Height: " + screenHeight);

        float widthToHeight = screenWidth / screenHeight;
        float widthToHeightVideo = videoWidth / videoHeight;

        double screenAspect = (Math.round(1000 * widthToHeight)) / 1000d;
        double previewAspect = (Math.round(1000 * widthToHeightVideo)) / 1000d;

        Utility.printErrorLog("~~~~ Ratio: screenAspectRatio: " + screenAspect);
        Utility.printErrorLog("~~~~ Ratio: previewAspectRatio: " + previewAspect);

        int marginBottom = Math.abs(previewAspect - screenAspect) < 0.001 ? 0 : (int) Utility.dpToPx(58f, context);
        Utility.printErrorLog("~~~~ Ratio: marginBottom: " + marginBottom);

        LinearLayout llBottomLayoutCamera = getParentView().findViewById(R.id.llBottomLayoutCamera);
        View view = getParentView().findViewById(R.id.view1);
        View viewLeft = getParentView().findViewById(R.id.viewLeft);
        View viewRight = getParentView().findViewById(R.id.viewRight);
        if (marginBottom == 0) {
            llBottomLayoutCamera.setBackgroundColor(context.getColor(R.color.transparent));
            view.setBackgroundColor(context.getColor(R.color.transparent));
            viewLeft.setBackgroundColor(context.getColor(R.color.transparent));
            viewRight.setBackgroundColor(context.getColor(R.color.transparent));
        } else {
            llBottomLayoutCamera.setBackgroundColor(context.getColor(R.color.colorBlack));
            view.setBackgroundColor(context.getColor(R.color.colorBlack));
            viewLeft.setBackgroundResource(R.drawable.camera_gradient_left);
            viewRight.setBackgroundResource(R.drawable.camera_gradient_right);
        }

        int newHeight;

        newHeight = (int) ((screenWidth * videoWidth) / videoHeight);
//        newWidth = (int) ((screenHeight * videoHeight) / videoHeight);

        CardView cardView = getParentView().findViewById(R.id.cardViewCamera);
        ViewGroup.LayoutParams layoutCardParams = cardView.getLayoutParams();
        layoutCardParams.height = newHeight;
        layoutCardParams.width = (int) screenWidth;
        Utility.setMargins(cardView, 0, 0, 0, marginBottom);
        cardView.setLayoutParams(layoutCardParams);

        ViewGroup.LayoutParams layoutParams = mCameraLayout.getLayoutParams();
        layoutParams.height = newHeight;
        layoutParams.width = (int) screenWidth;
        Utility.setMargins(mCameraLayout, 0, 0, 0, marginBottom);
        mCameraLayout.setLayoutParams(layoutParams);

        if (getLLAudio() != null) {
            getLLAudio().setLayoutParams(layoutParams);
        }

        if (getRLText() != null) {
            getRLText().setLayoutParams(layoutParams);
        }

        getParentView().post(() -> {
            RelativeLayout cameraView = getParentView().findViewById(R.id.rtl_still_camera);
            int[] locationScreen = new int[2];
            mCameraLayout.getLocationOnScreen(locationScreen);
            int finalTop = locationScreen[1] - Utility.getStatusBarHeight(context);

            Utility.printErrorLog("~~~~ Ratio: locationScreen: " + locationScreen[0] + " " + locationScreen[1]);
            Utility.printErrorLog("~~~~ Ratio: pxToDp Location: " + Utility.pxToDp(locationScreen[1], context));
            Utility.printErrorLog("~~~~ Ratio: finalTop: " + finalTop);

            int marginForTopButton = finalTop + (int) Utility.dpToPx(24f, context);
            Utility.printErrorLog("~~~~ Ratio: marginForTopButton: " + marginForTopButton);

            ImageView ivClose = cameraView.findViewById(R.id.ivCloseRecord);
            LinearLayout llMessageView = cameraView.findViewById(R.id.llMessage);
            ImageView ivAddContact = cameraView.findViewById(R.id.ivAddContact);
            LinearLayout llProfileView = cameraView.findViewById(R.id.llProfileLayout);
            LinearLayout llRecorderTimer = cameraView.findViewById(R.id.llRecordTimer);
            if (ivClose != null) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivClose.getLayoutParams();
                params.topMargin = marginForTopButton;
                ivClose.setLayoutParams(params);
            }

            if (ivAddContact != null) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivAddContact.getLayoutParams();
                params.topMargin = marginForTopButton;
                ivAddContact.setLayoutParams(params);
            }

            if (llMessageView != null) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) llMessageView.getLayoutParams();
                params.topMargin = marginForTopButton;
                llMessageView.setLayoutParams(params);
            }

            if (llProfileView != null) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) llProfileView.getLayoutParams();
                params.topMargin = marginForTopButton;
                llProfileView.setLayoutParams(params);
            }

            if (llRecorderTimer != null) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) llRecorderTimer.getLayoutParams();
                params.topMargin = marginForTopButton;
                llRecorderTimer.setLayoutParams(params);
            }
        });
    }


    public void rotateImage(ImageView imgSwitchCamera) {
        context.runOnUiThread(() -> {
            RotateAnimation rotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(200);
            rotate.setInterpolator(new LinearInterpolator());
            rotate.setFillAfter(true);
            imgSwitchCamera.startAnimation(rotate);
        });
    }

    public void deleteCurrentFileAndOpenCamera() {
        File file = getCurrentVideoFile();
        if (file != null && file.exists() && file.length() == 0) {
            file.delete();
        }
        try {
            openCamera(mCameraLayout.getWidth(), mCameraLayout.getHeight());
        } catch (SecurityException se) {
            requestPermissions();
        }
    }

    public void closeAndReopenCamera() {
        closeCamera();
        deleteCurrentFileAndOpenCamera();
    }

    public void setBrightnessFull() {
        Window window = context.getWindow();
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
        brightnessValue = 255;
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = brightnessValue / (float) 255;
        window.setAttributes(layoutParams);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCameraLayout = view.findViewById(getTextureResource());
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            characteristics = manager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        maximumZoomLevel = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) * 10;
        CustomLeftRightSwipeGesture swipeGesture = new CustomLeftRightSwipeGesture(context, new OnSwipeGestureListener() {

            @Override
            public void onScrollDown(float diffY) {

            }

            @Override
            public void onSwipeCancel() {
                Camera2Fragment.this.onSwipeCancel();
            }

            @Override
            public void onSwipeBottom() {

            }

            @Override
            public void onSwipeTop() {
                Camera2Fragment.this.onSwipeTop();
            }

            @Override
            public void onScroll(float diffY) {
                Camera2Fragment.this.onScroll(diffY);
            }

            @Override
            public void onSwipeRight() {
                Utility.showLog("Tag", "On Swipe Right");
                Camera2Fragment.this.onSwipeRight();
            }

            @Override
            public void onSwipeLeft() {
                Utility.showLog("Tag", "On Swipe Left");
                Camera2Fragment.this.onSwipeLeft();
            }
        });

        mCameraLayout.setOnTouchListener((v, event) -> {
            swipeGesture.onTouch(v, event);
            if (isViewsDisable) {
                return true;
            } else if (isExpanded) {
                collapseAnimation();
                return true;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_UP:
                    clickCount++;
                    if (clickCount == 1) {
                        startTime = System.currentTimeMillis();
                    } else if (clickCount == 2) {
                        long duration = System.currentTimeMillis() - startTime;
                        if (duration <= MAX_DURATION) {
                            // double tapped
                            doubleTapped();
                            clickCount = 0;
                        } else {
                            clickCount = 1;
                            startTime = System.currentTimeMillis();
                        }
                        break;
                    }
                    break;
            }
            try {
                if (rect == null) return false;
                float currentFingerSpacing;
                if (event.getPointerCount() > 1) { //Multi touch.
                    currentFingerSpacing = getFingerSpacing(event);
                    float delta = 0.04f; //Control this value to control the zooming sensibility
                    if (fingerSpacing != 0) {
                        if (currentFingerSpacing > fingerSpacing) { // Don't over zoom in
                            if ((maximumZoomLevel - zoomLevel) <= delta) {
                                delta = maximumZoomLevel - zoomLevel;
                            }
                            zoomLevel = zoomLevel + delta;
                        } else if (currentFingerSpacing < fingerSpacing) { //Don't over zoom-out
                            if ((zoomLevel - delta) < 1f) {
                                delta = zoomLevel - 1f;
                            }
                            zoomLevel = zoomLevel - delta;
                        }

                        isZoomSelected(true);

                        float ratio = (float) 1 / zoomLevel; //This ratio is the ratio of cropped Rect to Camera's original(Maximum) Rect
                        //croppedWidth and croppedHeight are the pixels cropped away, not pixels after cropped
                        int croppedWidth = rect.width() - Math.round((float) rect.width() * ratio);
                        int croppedHeight = rect.height() - Math.round((float) rect.height() * ratio);
                        //Finally, zoom represents the zoomed visible area
                        zoom = new Rect(croppedWidth / 2, croppedHeight / 2,
                                rect.width() - croppedWidth / 2, rect.height() - croppedHeight / 2);
                        if (isFirstTime() && zoomLevel < 1.15) {
                            pullerDragEnabled(true);
                        } else if (isFirstTime() && zoomLevel > 1.15) {
                            pullerDragEnabled(false);
                        }
                        if (zoomLevel < 1.15) {
                            isZoomSelected(false);
                        }

                        mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
                    }
                    fingerSpacing = currentFingerSpacing;
                } else { //Single touch point, needs to return true in order to detect one more touch point
                    return true;
                }

                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mBackgroundHandler);
                return true;
            } catch (final Exception e) {
                //Error handling up to you
                return true;
            }
        });

        lnrPermission = view.findViewById(getPermissionResources());
        mCamera2Listener = this;
        if (context.savedInstanceState == null && !context.isRequiredPermissionsGranted() && context.videoOptions != CameraNewActivity.VideoOptions.REPLY_REACTION) {
            requestPermissions();
        }
    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    protected void setZoom(float startLocation, float dy) {
        zoomLevel = 1 + Math.abs((dy - startLocation) / 100);
        isZoomSelected(true);
        float ratio = (float) 1 / zoomLevel; //This ratio is the ratio of cropped Rect to Camera's original(Maximum) Rect
        //croppedWidth and croppedHeight are the pixels cropped away, not pixels after cropped
        int croppedWidth = rect.width() - Math.round((float) rect.width() * ratio);
        int croppedHeight = rect.height() - Math.round((float) rect.height() * ratio);
        //Finally, zoom represents the zoomed visible area
        zoom = new Rect(croppedWidth / 2, croppedHeight / 2,
                rect.width() - croppedWidth / 2, rect.height() - croppedHeight / 2);
        if (isFirstTime() && zoomLevel < 1.15) {
            pullerDragEnabled(true);
        } else if (isFirstTime() && zoomLevel > 1.15) {
            pullerDragEnabled(false);
        }
        if (zoomLevel < 1.15) {
            isZoomSelected(false);
        }
        if (zoom != null) {
            mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
        }
        try {
            if (mPreviewSession != null) {
                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

//    private void setupZoomHandler() {
//        ActiveArrayZoomHandlerBuilder.forView(mCameraLayout)
//                .setActiveArraySize(characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE))
//                .setMaxZoom(characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) * 10)
//                .setZoomListener(new ActiveArrayZoomHandler.IZoomHandlerListener() {
//                    @Override
//                    public void onZoomChanged(Rect zoom) {
//                        mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
//                        updatePreview();
//                    }
//                })
//                .build();
//    }

    public synchronized void closeCamera() {
        try {
            Utility.showLog("Camera", "Stop");
            mCameraOpenCloseLock.acquire();

            try {
                if (mPreviewSession != null) {
                    mPreviewSession.stopRepeating();
                    mPreviewSession.abortCaptures();
                    mPreviewSession = null;
                }
            } catch (IllegalStateException | CameraAccessException | SecurityException e) {
                e.printStackTrace();
            }
            try {
                if (mMediaRecorder != null && isRecording) {
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                }
                mMediaRecorder = null;
                isMediaRecorderPrepared = false;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }

            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            mCamera2Listener.onInterruptedException(ie);
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            isCameraOpen = false;
            mCameraOpenCloseLock.release();
        }
    }

    public void stopRecordingVideoNew() {
        try {
            if (context.isAudioReply()) {
                try {
                    if (mMediaAudioRecorder != null && isRecording) {
                        mMediaAudioRecorder.stop();
                        mMediaAudioRecorder.release();
                    }
                    mMediaAudioRecorder = null;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            } else {
                if (mMediaRecorder != null && isRecording) {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    mMediaRecorder.release();
                }
                mMediaRecorder = null;
                isMediaRecorderPrepared = false;
            }
            isRecording = false;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    protected void startPreview() {
        if (mCameraDevice == null || !mCameraLayout.isAvailable() || mPreviewSize == null) {
            return;
        }
        try {
            closePreviewSession();
            context.mCameraFacing = mCameraFacing;
            if (!context.isReplyReactionWithoutVideo() && !context.isPhotoReply()) {
                setUpMediaRecorder();
            }
            mCameraDevice.createCaptureSession(getSurfaces(), mSessionCallback, mBackgroundHandler);
        } catch (CameraAccessException cae) {
            cae.printStackTrace();
            mCamera2Listener.onCameraException(cae);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            mCamera2Listener.onIOException(ioe);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setZoom(float value) {
//        new Zoom(characteristics).setZoom(mPreviewBuilder, value);
        Rect rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        try {
            float ratio = (float) 1 / value; //This ratio is the ratio of cropped Rect to Camera's original(Maximum) Rect
            //croppedWidth and croppedHeight are the pixels cropped away, not pixels after cropped
            int croppedWidth = rect.width() - Math.round((float) rect.width() * ratio);
            int croppedHeight = rect.height() - Math.round((float) rect.height() * ratio);
            //Finally, zoom represents the zoomed visible area
            zoom = new Rect(croppedWidth / 2, croppedHeight / 2,
                    rect.width() - croppedWidth / 2, rect.height() - croppedHeight / 2);

            mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mBackgroundHandler);
            if (value == 2f) {
                zoomLevel = 2f;
            } else if (value == 4f) {
                zoomLevel = 4f;
            } else {
                zoomLevel = 1f;
            }
        } catch (Exception e) {
            Utility.showLogException(e);
        }
    }

    private List<Surface> getSurfaces() {
        List<Surface> surfaces = new ArrayList<>();
        try {
            SurfaceTexture texture = mCameraLayout.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            if (context.isPhotoReply()) {
                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            } else {
                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            }
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);
            if (!context.isPhotoReply()) {
                Surface recorderSurface = mMediaRecorder.getSurface();
                surfaces.add(recorderSurface);
                mPreviewBuilder.addTarget(recorderSurface);
            }
        } catch (CameraAccessException cae) {
            cae.printStackTrace();
            mCamera2Listener.onCameraException(cae);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return surfaces;
    }

    protected void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        if (!context.isPhotoReply()) {
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON);
            builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_FAST);
        }
    }

    public void resetBrightnessToPrevious() {
        Window window = context.getWindow();
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = brightnessOriginal / (float) 255;
        window.setAttributes(layoutParams);
    }

    protected void setUpMediaRecorder() throws IOException, CameraAccessException {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        File file = getVideoFile(activity);
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        mMediaRecorder.setMaxDuration(maxRecordDuration);
//        mMediaRecorder.setVideoEncodingBitRate(71303168);
//        mMediaRecorder.setVideoFrameRate(60);
        CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mMediaRecorder.setVideoEncodingBitRate(cpHigh.videoBitRate);
        mMediaRecorder.setVideoFrameRate(cpHigh.videoFrameRate);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            rotation = activity.getDisplay().getRotation();
        }
        int orientation = CameraUtil.getOrientation(rotation, upsideDown);
        mMediaRecorder.setOrientationHint(orientation);
        mMediaRecorder.prepare();
        isMediaRecorderPrepared = true;
        Utility.showLog("Media", "Prepare");
    }

    protected void setUpCamera() {
        if (context == null || context.isFinishing() || !context.isRequiredPermissionsGranted() || isCameraOpen) {
            return;
        }
        if (lnrPermission != null) {
            lnrPermission.setVisibility(View.GONE);
        }
        mCameraManager = CameraUtil.getCameraManager(context.getApplicationContext());
        try {
            if (!context.isReplyReactionWithoutVideo()) {
                if (!mCameraOpenCloseLock.tryAcquire(4000, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Time out waiting to lock camera opening.");
                }
                cameraId = mCameraManager.getCameraIdList()[0]; // Default to back camera
                for (String id : mCameraManager.getCameraIdList()) {
                    CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(id);
                    int cameraFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (cameraFacing == mCameraFacing) {
                        cameraId = id;
                        break;
                    }
                }
            }

            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {
                mVideoSize = CameraUtil.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
                mPreviewSize = CameraUtil.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));

                int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                // Camera is mounted the wrong way...
                upsideDown = (sensorOrientation == 270 && cameraId.equalsIgnoreCase("1"));
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mCameraLayout.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mCameraLayout.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }
                setAspectRationAdjustment();
                isAspectRatioSetUpDone = true;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            mCamera2Listener.onCameraException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void writeAudioData() { // to be called in a Runnable for a Thread created after call to startRecording()

        byte[] data = new byte[finalBufferSize / 2]; // assign size so that bytes are read in in chunks inferior to AudioRecord internal buffer size

        FileOutputStream outputStream = null;
        String pcmFileName = "";
        File wavFile = getAudioFile(context);
        try {
            int index = wavFile.getAbsolutePath().lastIndexOf('.');
            pcmFileName = wavFile.getAbsolutePath().substring(0, index) + ".pcm";
            outputStream = new FileOutputStream(pcmFileName); //fileName is path to a file, where audio data should be written
        } catch (FileNotFoundException e) {
            // handle error
        }

        if (outputStream != null) {
            while (isRecording) { // continueRecording can be toggled by a button press, handled by the main (UI) thread
                int read = mMediaAudioRecorder.read(data, 0, data.length);
                int cAmplitude = 0;
                for (int i = 0; i < read / 2; i++) {
                    short curSample = getShort(data[i * 2], data[i * 2 + 1]);
                    if (curSample > cAmplitude) {
                        cAmplitude = curSample;
                        maxAmplitude = cAmplitude;
                    }
                }

                try {
                    outputStream.write(data, 0, read);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                rawToWave(new File(pcmFileName), wavFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private short getShort(byte argB1, byte argB2) {
        return (short) (argB1 | (argB2 << 8));
    }

    private void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        try (DataInputStream input = new DataInputStream(new FileInputStream(rawFile))) {
            input.read(rawData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(waveFile))) {
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, SAMPLE_RATE); // sample rate
            writeInt(output, finalBufferSize * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }
            output.write(fullyReadFileToBytes(rawFile));
        }
    }

    byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte[] bytes = new byte[size];
        byte[] tmpBuff = new byte[size];
        try (FileInputStream fis = new FileInputStream(f)) {
            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        }
        return bytes;
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

    private String assetFilePath(Context context) {
        File file = new File(context.getFilesDir(), "silero.ptl");
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open("silero.ptl")) {
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

    /*private String recognizeHelper(float[] floatInputBuffer) {
        int length = floatInputBuffer.length;
        double[] wav2VecInput = new double[length];
        for (int n = 0; n < length; n++)
            wav2VecInput[n] = floatInputBuffer[n];

        FloatBuffer inTensorBuffer = Tensor.allocateFloatBuffer(length);
        for (double val : wav2VecInput)
            inTensorBuffer.put((float) val);

        Tensor inTensor = Tensor.fromBlob(inTensorBuffer, new long[]{1, length});
        Tensor op = module.forward(IValue.from(inTensor)).toTensor();
        String result = SileroDecoder(op);
        return result;
    }

    private String recognize(float[] floatInputBuffer) {
        int size = floatInputBuffer.length;

        StringBuilder result = new StringBuilder();
        if (size > RECORDING_LENGTH) {
            float[] temp = new float[RECORDING_LENGTH];
            int i = 0;
            while (i < (size - RECORDING_LENGTH)) {
                for (int j = 0; j < RECORDING_LENGTH; j++, i++) {
                    temp[j] = floatInputBuffer[i];
                }
                result.append(recognizeHelper(temp));
            }
            int length = size - i;
            float[] remainingTemp = new float[length];
            for (int j = 0; j < length; j++, i++) {
                remainingTemp[j] = floatInputBuffer[i];
            }
            result.append(recognizeHelper(remainingTemp));
        } else {
            result = new StringBuilder(recognizeHelper(floatInputBuffer));
        }
        return result.toString();
    }

    private int[] argmaxOned(float[] arr, int dim1, int dim2) {
        int[] ans = new int[dim1];
        int k = 0;
        for (int i = 0; i < dim1; i++) {
            float max = Integer.MIN_VALUE;
            int index = 0;
            for (int j = 0; j < dim2; j++) {
                if (arr[k] > max) {
                    max = arr[k];
                    index = j;
                }
                k++;
            }
            ans[i] = index;
        }
        return ans;
    }

    private String SileroDecoder(Tensor opProbs) {
        long[] shape = opProbs.shape();
        final int DIM1 = (int) shape[1];
        final int DIM2 = (int) shape[2];
        float[] array = opProbs.getDataAsFloatArray();
        String[] labels = {"_", "th", "the", "in", "an", "re", "er", "on", "at", "ou", "is", "en", "to", "and", "ed", "al", "as", "it", "ing", "or", "of", "es", "ar", "he", "le", "st", "se", "om", "ic", "be", "we", "ly", "that", "no", "wh", "ve", "ha", "you", "ch", "ion", "il", "ent", "ro", "me", "id", "ac", "gh", "for", "was", "lo", "ver", "ut", "li", "ld", "ay", "ad", "so", "ir", "im", "un", "wi", "ter", "are", "with", "ke", "ge", "do", "ur", "all", "ce", "ab", "mo", "go", "pe", "ne", "this", "ri", "ght", "de", "one", "us", "am", "out", "fe", "but", "po", "his", "te", "ho", "ther", "not", "con", "com", "ll", "they", "if", "ould", "su", "have", "ct", "ain", "our", "ation", "fr", "ill", "now", "sa", "had", "tr", "her", "per", "ant", "oun", "my", "ul", "ca", "by", "what", "red", "res", "od", "ome", "ess", "man", "ex", "she", "pl", "co", "wor", "pro", "up", "thing", "there", "ple", "ag", "can", "qu", "art", "ally", "ok", "from", "ust", "very", "sh", "ind", "est", "some", "ate", "wn", "ti", "fo", "ard", "ap", "him", "were", "ich", "here", "bo", "ity", "um", "ive", "ous", "way", "end", "ig", "pr", "which", "ma", "ist", "them", "like", "who", "ers", "when", "act", "use", "about", "ound", "gr", "et", "ide", "ight", "ast", "king", "would", "ci", "their", "other", "see", "ment", "ong", "wo", "ven", "know", "how", "said", "ine", "ure", "more", "der", "sel", "br", "ren", "ack", "ol", "ta", "low", "ough", "then", "peo", "ye", "ace", "people", "ink", "ort", "your", "will", "than", "pp", "any", "ish", "look", "la", "just", "tor", "ice", "itt", "af", "these", "sp", "has", "gre", "been", "ty", "ies", "ie", "get", "able", "day", "could", "bl", "two", "time", "beca", "into", "age", "ans", "mis", "new", "ree", "ble", "ite", "si", "urn", "ass", "cl", "ber", "str", "think", "dis", "mar", "ence", "pt", "self", "ated", "did", "el", "don", "ck", "ph", "ars", "ach", "fore", "its", "part", "ang", "cre", "well", "ions", "where", "ves", "ved", "em", "good", "because", "over", "ud", "ts", "off", "turn", "cr", "right", "ress", "most", "every", "pre", "fa", "fir", "ild", "pos", "down", "work", "ade", "say", "med", "also", "litt", "little", "ance", "come", "ving", "only", "ful", "ought", "want", "going", "spe", "ps", "ater", "first", "after", "ue", "ose", "mu", "iz", "ire", "int", "rest", "ser", "coun", "des", "light", "son", "let", "ical", "ick", "ra", "back", "mon", "ase", "ign", "ep", "world", "may", "read", "form", "much", "even", "should", "again", "make", "long", "sto", "cont", "put", "thr", "under", "cor", "bet", "jo", "car", "ile", "went", "yes", "ually", "row", "hand", "ak", "call", "ary", "ia", "many", "cho", "things", "try", "gl", "ens", "really", "happ", "great", "dif", "bu", "hi", "made", "room", "ange", "cent", "ious", "je", "three", "ward", "op", "gen", "those", "life", "tal", "pa", "through", "und", "cess", "before", "du", "side", "need", "less", "inter", "ting", "ry", "ise", "na", "men", "ave", "fl", "ction", "pres", "old", "something", "miss", "never", "got", "feren", "imp", "sy", "ations", "tain", "ning", "ked", "sm", "take", "ten", "ted", "ip", "col", "own", "stand", "add", "min", "wer", "ms", "ces", "gu", "land", "bod", "log", "cour", "ob", "vo", "ition", "hu", "came", "comp", "cur", "being", "comm", "years", "ily", "wom", "cer", "kind", "thought", "such", "tell", "child", "nor", "bro", "ial", "pu", "does", "head", "clo", "ear", "led", "llow", "ste", "ness", "too", "start", "mor", "used", "par", "play", "ents", "tri", "upon", "tim", "num", "ds", "ever", "cle", "ef", "wr", "vis", "ian", "sur", "same", "might", "fin", "differen", "sho", "why", "body", "mat", "beg", "vers", "ouse", "actually", "ft", "ath", "hel", "sha", "ating", "ual", "found", "ways", "must", "four", "gi", "val", "di", "tre", "still", "tory", "ates", "high", "set", "care", "ced", "last", "find", "au", "inte", "ev", "ger", "thank", "ss", "ict", "ton", "cal", "nat", "les", "bed", "away", "place", "house", "che", "ject", "sol", "another", "ited", "att", "face", "show", "ner", "ken", "far", "ys", "lect", "lie", "tem", "ened", "night", "while", "looking", "ah", "wal", "dr", "real", "cha", "exp", "war", "five", "pol", "fri", "wa", "cy", "fect", "xt", "left", "give", "soci", "cond", "char", "bor", "point", "number", "mister", "called", "six", "bre", "vi", "without", "person", "air", "different", "lot", "bit", "pass", "ular", "youn", "won", "main", "cri", "ings", "den", "water", "human", "around", "quest", "ters", "ities", "feel", "each", "friend", "sub", "though", "saw", "ks", "hund", "hundred", "times", "lar", "ff", "amer", "scho", "sci", "ors", "lt", "arch", "fact", "hal", "himself", "gener", "mean", "vol", "school", "ason", "fam", "ult", "mind", "itch", "ped", "home", "young", "took", "big", "love", "reg", "eng", "sure", "vent", "ls", "ot", "ince", "thous", "eight", "thousand", "better", "mom", "appe", "once", "ied", "mus", "stem", "sing", "ident", "als", "uh", "mem", "produ", "stud", "power", "atch", "bas", "father", "av", "nothing", "gir", "pect", "unt", "few", "kes", "eyes", "sk", "always", "ared", "toge", "stru", "together", "ics", "bus", "fort", "ween", "rep", "ically", "small", "ga", "mer", "ned", "ins", "between", "yet", "stre", "hard", "system", "course", "year", "cept", "publ", "sim", "sou", "ready", "follow", "present", "rel", "turned", "sw", "possi", "mother", "io", "bar", "ished", "dec", "ments", "pri", "next", "ross", "both", "ship", "ures", "americ", "eas", "asked", "iness", "serv", "ists", "ash", "uni", "build", "phone", "lau", "ctor", "belie", "change", "interest", "peri", "children", "thir", "lear", "plan", "import", "ational", "har", "ines", "dist", "selves", "city", "sen", "run", "law", "ghter", "proble", "woman", "done", "heart", "book", "aut", "ris", "lim", "looked", "vid", "fu", "bab", "ately", "ord", "ket", "oc", "doing", "area", "tech", "win", "name", "second", "certain", "pat", "lad", "quite", "told", "ific", "ative", "uring", "gg", "half", "reason", "moment", "ility", "ution", "shall", "aur", "enough", "idea", "open", "understand", "vie", "contin", "mal", "hor", "qui", "address", "heard", "help", "inst", "oney", "whole", "gover", "commun", "exam", "near", "didn", "logy", "oh", "tru", "lang", "restaur", "restaurant", "design", "ze", "talk", "leg", "line", "ank", "ond", "country", "ute", "howe", "hold", "live", "comple", "however", "ized", "ush", "seen", "bye", "fer", "ital", "women", "net", "state", "bur", "fac", "whe", "important", "dar", "nine", "sat", "fic", "known", "having", "against", "soon", "ety", "langu", "public", "sil", "best", "az", "knew", "black", "velo", "sort", "seven", "imag", "lead", "cap", "ask", "alth", "ature", "nam", "began", "white", "sent", "sound", "vir", "days", "anything", "yeah", "ub", "blo", "sun", "story", "dire", "money", "trans", "mil", "org", "grow", "cord", "pped", "cus", "spo", "sign", "beaut", "goodbye", "inde", "large", "question", "often", "hour", "que", "pur", "town", "ield", "coming", "door", "lig", "ling", "incl", "partic", "keep", "engl", "move", "later", "ants", "food", "lights", "bal", "words", "list", "aw", "allow", "aren", "pret", "tern", "today", "believe", "almost", "bir", "word", "possible", "ither", "case", "ried", "ural", "round", "twent", "develo", "plain", "ended", "iting", "chang", "sc", "boy", "gy", "since", "ones", "suc", "cas", "national", "plac", "teen", "pose", "started", "mas", "fi", "fif", "afr", "fully", "grou", "wards", "girl", "e", "t", "a", "o", "i", "n", "s", "h", "r", "l", "d", "u", "c", "m", "w", "f", "g", "y", "p", "b", "v", "k", "'", "x", "j", "q", "z", "-", "2", " "};
        int blankIdx = 0;
        int spaceIdx = 998;
        int twoIdx = 997;
        int[] argm = argmaxOned(array, DIM1, DIM2);

        List<Integer> finalLabels = new ArrayList<>();

        for (int k : argm) {
            if (k == twoIdx) {
                if (!finalLabels.isEmpty()) {
                    finalLabels.add(finalLabels.get(finalLabels.size() - 1));
                    if (finalLabels.get(finalLabels.size() - 1) != spaceIdx) {
                        finalLabels.add(spaceIdx);
                    }
                } else {
                    finalLabels.add(spaceIdx);
                }
            }
            if (k != blankIdx) {
                if (!finalLabels.isEmpty()) {
                    if (finalLabels.get(finalLabels.size() - 1) != k) {
                        finalLabels.add(k);
                    }
                } else {
                    finalLabels.add(k);
                }
            }
        }

        StringBuilder ans = new StringBuilder();
        for (int j = 0; j < finalLabels.size(); j++) {
            ans.append(labels[finalLabels.get(j)]);
        }
        return ans.toString();
    }*/

    public void setFlashOn() {
        try {
            isFlashOn = true;
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void setFlashOff() {
        try {
            isFlashOn = false;
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void setFlashBlink() {
        try {
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                    mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }, 300);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void setFlashBlink2() {
        try {
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    if (isFlashOn) {
                        mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                    } else {
                        mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                    }
                    mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }, 500);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (mCameraLayout == null || mPreviewSize == null || activity == null) {
            return;
        }
        int rotation = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            rotation = activity.getDisplay().getRotation();
        }
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY);
        } else {
            double aspectRatio = (double) mPreviewSize.getWidth() / mPreviewSize.getHeight();

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
            Utility.showLog("TAG", "video=" + mPreviewSize.getWidth() + "x" + mPreviewSize.getHeight() +
                    " view=" + viewWidth + "x" + viewHeight +
                    " newView=" + newWidth + "x" + newHeight +
                    " off=" + xoff + "," + yoff);

            mCameraLayout.getTransform(matrix);
            matrix.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
            matrix.postTranslate(xoff, yoff);
        }
        mCameraLayout.setTransform(matrix);
    }

    private final ImageReader.OnImageAvailableListener onImageAvailableListener = (ImageReader imReader) -> {
        if (mBackgroundHandler != null) {
            mBackgroundHandler.post(new ImageSaver(imReader.acquireNextImage(), getPhotoFile(context)));
        }
//        final Image image = imReader.acquireLatestImage();
//        final ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//        final byte[] bytes = new byte[buffer.capacity()];
//        buffer.get(bytes);
//        saveImageToDisk(bytes);
//        image.close();
    };

    private void saveImageToDisk(final byte[] bytes) {
        try (final OutputStream output = new FileOutputStream(getPhotoFile(context))) {
            output.write(bytes);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (mCameraDevice == null) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
            if (isFlashBlink) {
                isFlashBlink = false;
                setFlashBlink2();
            } else if (isFlashOn) {
                setFlashOn();
            }
        } catch (CameraAccessException cae) {
            cae.printStackTrace();
            mCamera2Listener.onCameraException(cae);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void setFullBrightness() {
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, (int) 10.0);
        try {
            if (mPreviewSession != null) {
                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            closeCamera();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    // Default Cam2Listener events

    @Override
    public void onCameraException(CameraAccessException cae) {
        cae.printStackTrace();
    }

    @Override
    public void onNullPointerException(NullPointerException npe) {
        npe.printStackTrace();
    }

    @Override
    public void onInterruptedException(InterruptedException ie) {
        ie.printStackTrace();
    }

    @Override
    public void onIOException(IOException ioe) {
        ioe.printStackTrace();
    }

    @Override
    public void onConfigurationFailed() {
        Utility.showLog(TAG, "Failed to configure camera");
    }

    public void showPermissionView(int visibility) {
        if (visibility == View.VISIBLE) {
            boolean hasCameraPermissionGranted = CameraUtil.hasPermissionsGranted(context, Camera2PermissionDialog.PHOTO_PERMISSIONS);
            boolean hasMicPermissionGranted = CameraUtil.hasPermissionsGranted(context, Camera2PermissionDialog.AUDIO_PERMISSIONS);
            ImageView ivPermission = lnrPermission.findViewById(R.id.ivPermission);
            TextView tvPermissionMsg = lnrPermission.findViewById(R.id.tvPermissionMsg);
            TextView tvRecordVideos = lnrPermission.findViewById(R.id.tvRecordVideos);
            if (context.isPhotoReply() && !hasCameraPermissionGranted) {
                ivPermission.setImageResource(R.drawable.ic_camera_permission);
                tvPermissionMsg.setText(context.getResources().getString(R.string.txt_camera_per1));
                tvRecordVideos.setText(context.getResources().getString(R.string.record_videos));
            } else if (context.isAudioReply() && !hasMicPermissionGranted) {
                ivPermission.setImageResource(R.drawable.ic_mic_permission);
                tvPermissionMsg.setText(context.getResources().getString(R.string.txt_camera_per2));
                tvRecordVideos.setText(context.getResources().getString(R.string.record_sounds));
            } else if (!hasCameraPermissionGranted && !hasMicPermissionGranted) {
                ivPermission.setImageResource(R.drawable.ic_camera_permission);
                tvPermissionMsg.setText(context.getResources().getString(R.string.txt_camera_per));
                tvRecordVideos.setText(context.getResources().getString(R.string.record_videos));
            } else if (!hasCameraPermissionGranted) {
                ivPermission.setImageResource(R.drawable.ic_camera_permission);
                tvPermissionMsg.setText(context.getResources().getString(R.string.txt_camera_per1));
                tvRecordVideos.setText(context.getResources().getString(R.string.record_videos));
            } else if (!hasMicPermissionGranted) {
                ivPermission.setImageResource(R.drawable.ic_mic_permission);
                tvPermissionMsg.setText(context.getResources().getString(R.string.txt_camera_per2));
                tvRecordVideos.setText(context.getResources().getString(R.string.record_sounds));
            }
        }
        lnrPermission.setVisibility(visibility);
    }
}