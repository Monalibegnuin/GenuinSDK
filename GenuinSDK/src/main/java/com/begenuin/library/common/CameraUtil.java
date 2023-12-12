package com.begenuin.library.common;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.util.Size;
import android.view.Surface;
import androidx.core.app.ActivityCompat;

import com.begenuin.library.common.customViews.AutoFitTextureView;

import java.util.ArrayList;
import java.util.Collections;

public class CameraUtil {

    /**
     * Select camera video size.
     *
     * @param choices -List of camera preview sizes
     * @return -Best video size.
     */
    public static Size chooseVideoSize(Size[] choices) {
        Size mSize = null;

        for (Size size : choices) {
            if (1280 == size.getWidth() && 720 == size.getHeight()) {
                mSize = size;
                break;
            }
        }

        if (mSize == null) {
            for (Size size : choices) {
                if (1920 == size.getWidth()) {
                    mSize = size;
                    break;
                }
            }
        }

        if (mSize == null) {
            return choices[0];
        }

        return mSize;
    }

    public static Size chooseSquareVideoSize(Size[] choices) {
        Size mSize = null;

        for (Size size : choices) {
            if (size.getWidth() >= 720 && size.getWidth() == size.getHeight()) {
                mSize = size;
            }
        }

        if (mSize == null) {
            for (Size size : choices) {
                if (1280 == size.getWidth() && 720 == size.getHeight()) {
                    mSize = size;
                    break;
                }
            }
        }

        if (mSize == null) {
            return choices[0];
        }

        return mSize;
    }

    public static Size firstVideoSize(Size[] choices) {
        return choices[0];
    }

    public static Size chooseOptimalVideoSize(Size[] outputSizes, int width, int height) {
        double preferredRatio = height / (double) width;
        Size currentOptimalSize = outputSizes[0];
        double currentOptimalRatio = currentOptimalSize.getWidth() / (double) currentOptimalSize.getHeight();
        for (Size currentSize : outputSizes) {
            double currentRatio = currentSize.getWidth() / (double) currentSize.getHeight();
            if (Math.abs(preferredRatio - currentRatio) <
                    Math.abs(preferredRatio - currentOptimalRatio)) {
                currentOptimalSize = currentSize;
                currentOptimalRatio = currentRatio;
            }
        }
        return currentOptimalSize;
    }

    /**
     * Get optimal size for camera preview
     *
     * @param sizes -List of all preview sizes
     * @param w     -Width of {@link android.view.TextureView}
     * @param h     -Height of {@link android.view.TextureView}
     * @return -Optimal preview size
     */
    public static Size getOptimalPreviewSize(Size[] sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Size size : sizes) {
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.getHeight() - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - h);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - h);
                }
            }
        }
        if (optimalSize == null)
            optimalSize = sizes[sizes.length - 1];
        Utility.showLog("TAG", "Optimal width: " + optimalSize.getWidth() + " height: " + optimalSize.getHeight());
        return optimalSize;
    }

    public static Size chooseOptimalSize(
            Size[] sizes, int textureViewWidth, int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio
    ) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        ArrayList<Size> bigEnough = new ArrayList<Size>();
        // Collect the supported resolutions that are smaller than the preview Surface
        ArrayList<Size> notBigEnough = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (int i = 0; i < sizes.length; i++) {
            Size option = sizes[i];
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new AutoFitTextureView.CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new AutoFitTextureView.CompareSizesByArea());
        } else {
            Utility.showLog("TAG", "Couldn't find any suitable preview size");
            return sizes[0];
        }
    }

    public static boolean shouldShowRequestPermissionRationale(Activity context, String[] permissions) {
        for (String permission : permissions) {
            if (!context.shouldShowRequestPermissionRationale(permission)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasPermissionsGranted(Activity context, String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get camera orientation
     *
     * @param rotation   -Rotation degree
     * @param upsideDown -Camera has upside down issue
     * @return -Rotation degree
     */
    public static int getOrientation(int rotation, boolean upsideDown) {
        Utility.showLog("Camera", rotation + " : " + upsideDown);
        if (upsideDown) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    return 270;
                case Surface.ROTATION_90:
                    return 180;
                case Surface.ROTATION_180:
                    return 90;
                case Surface.ROTATION_270:
                    return 0;
            }
        } else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    return 90;
                case Surface.ROTATION_90:
                    return 0;
                case Surface.ROTATION_180:
                    return 270;
                case Surface.ROTATION_270:
                    return 180;
            }
        }
        return 0;
    }

    private static CameraManager mCameraManager;

    public static CameraManager getCameraManager(Context context) {
        if (mCameraManager == null) {
            return mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        } else {
            return mCameraManager;
        }
    }

    /**
     * Determines if the dimensions are swapped given the phone's current rotation.
     *
     * @param displayRotation The current rotation of the display
     * @return true if the dimensions are swapped, false otherwise.
     */
    public static boolean areDimensionsSwapped(int displayRotation, int sensorOrientation) {
        boolean swappedDimensions = false;
        if (displayRotation == Surface.ROTATION_0 || displayRotation == Surface.ROTATION_180) {
            if (sensorOrientation == 90 || sensorOrientation == 270) {
                swappedDimensions = true;
            }
        }
        if (displayRotation == Surface.ROTATION_90 || displayRotation == Surface.ROTATION_270) {
            if (sensorOrientation == 0 || sensorOrientation == 180) {
                swappedDimensions = true;
            }
        }
        return swappedDimensions;
    }
}
