package com.begenuin.library.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.begenuin.library.data.model.MembersModel;
import com.bumptech.glide.load.resource.gif.GifDrawable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ImageUtils {

    public static Bitmap convert(String base64Str) throws IllegalArgumentException {
        byte[] decodedBytes = Base64.decode(
                base64Str.substring(base64Str.indexOf(",") + 1),
                Base64.DEFAULT
        );

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static String convert(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    public Bitmap getCompressedBitmap(String imagePath) {
        float maxHeight = 1920.0f;
        float maxWidth = 1080.0f;
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);

        byte[] byteArray = out.toByteArray();

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        Utility.printErrorLog("bitmapWidth: bitmapRatio: " + bitmapRatio);
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        Utility.printErrorLog("bitmapWidth: bitmapRatio: finalWidth: " + width + " finalHeight: " + height);
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage, String uriStringPath)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(img, selectedImage, uriStringPath);
        return img;
    }

    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @param uriStringPath Image String Path
     * @return The resulted Bitmap after manipulation
     */
    public static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage, String uriStringPath) throws IOException {
        Utility.printErrorLog("bitmapWidth: rotateImageIfRequired: finalWidth: " + img.getWidth() + " finalHeight: " + img.getHeight());
        ExifInterface ei = new ExifInterface(uriStringPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        Utility.printErrorLog("bitmapWidth: rotateImageIfRequired: orientation: " + orientation);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                Utility.printErrorLog("bitmapWidth: rotateImageIfRequired: default no rotation: ");
                return img;
        }
    }

    public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws IOException {
        ExifInterface ei = new ExifInterface(image_absolute_path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        Utility.printErrorLog("bitmapWidth: modifyOrientation: finalWidth: " + bitmap.getWidth() + " finalHeight: " + bitmap.getHeight());
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                Utility.printErrorLog("bitmapWidth: modifyOrientation: ORIENTATION_ROTATE_90: ");
                return rotateImage(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                Utility.printErrorLog("bitmapWidth: modifyOrientation: ORIENTATION_ROTATE_180: ");
                return rotateImage(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                Utility.printErrorLog("bitmapWidth: modifyOrientation: ORIENTATION_ROTATE_270: ");
                return rotateImage(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                Utility.printErrorLog("bitmapWidth: modifyOrientation: ORIENTATION_FLIP_HORIZONTAL: ");
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                Utility.printErrorLog("bitmapWidth: modifyOrientation: ORIENTATION_FLIP_VERTICAL: ");
                return flip(bitmap, false, true);

            default:
                Utility.printErrorLog("bitmapWidth: modifyOrientation: default no rotation: ");
                return bitmap;
        }
    }

    private static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Utility.printErrorLog("bitmapWidth: flip: horizontal:" + horizontal + " vertical: " + vertical);
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Utility.printErrorLog("bitmapWidth: rotatedImg: finalWidth: " + img.getWidth() + " finalHeight: "
                + img.getHeight() + " degree: " + degree);
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        Utility.printErrorLog("bitmapWidth: rotatedImg: finalWidth: " + rotatedImg.getWidth() + " finalHeight: " + rotatedImg.getHeight());
        img.recycle();
        return rotatedImg;
    }

    public static String saveImage(Bitmap finalBitmap, String fileName, Context context) {

        File mediaStorageDir = context.getExternalFilesDir(Constants.POSTS_IMAGES_DIRECTORY);
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName);

        mediaFile.mkdirs();

        if (mediaFile.exists()) mediaFile.delete();
        try {
            FileOutputStream out = new FileOutputStream(mediaFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return mediaFile.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String saveStickerAsImage(Context context, Bitmap finalBitmap, int viewId) {
        String fileName = viewId + ".png";
        File mediaStorageDir = context.getExternalFilesDir(Constants.STICKER_IMAGES_DIRECTORY);
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName);

        mediaFile.mkdirs();

        if (mediaFile.exists()) mediaFile.delete();
        try {
            FileOutputStream out = new FileOutputStream(mediaFile);

            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return mediaFile.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String saveStickerAsGIF(Context context, GifDrawable gifDrawable, String fileName) {
        File mediaStorageDir = context.getExternalFilesDir(Constants.STICKER_IMAGES_DIRECTORY);
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName);
        ByteBuffer byteBuffer = gifDrawable.getBuffer();
        mediaFile.mkdirs();

        if (mediaFile.exists()) mediaFile.delete();
        try {
            FileOutputStream out = new FileOutputStream(mediaFile);
            byte[] bytes = new byte[byteBuffer.capacity()];
            ((ByteBuffer)(byteBuffer.duplicate().clear())).get(bytes);
            out.write(bytes, 0, bytes.length);
            out.flush();
            out.close();
            return mediaFile.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void saveQRImageToInternalStorage(Context context, Bitmap bitmap) {
        try {
            File file = new File(context.getCacheDir(), "QRCodeImages");
            if (!file.exists())
                file.mkdirs();
            FileOutputStream stream = new FileOutputStream(file + "/image.jpeg");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Bitmap readQRImageFromInternalStorage(Context context) {
        Bitmap bitmap = null;
        try {
            File file = new File(context.getCacheDir(), "QRCodeImages/image.jpeg");
            if (file.exists()) {
                FileInputStream inputStream = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } else {
                Utility.printErrorLog("bitmap might be deleted or no bitmap there yet.");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void removeQRCodeFromInternalStorage(Context context) {
        try {
            File file = new File(context.getCacheDir(), "QRCodeImages");
            if (file.isDirectory()) {
                String[] children = file.list();
                if (children != null && children.length > 0) {
                    for (String child : children) {
                        boolean isDeleted = new File(file, child).delete();
                        Utility.printErrorLog("file exists so isDeleted: " + isDeleted);
                    }
                } else {
                    Utility.printErrorLog("files might be deleted or no files there yet.");
                }

            }
//            MembersModel user = Utility.getCurrentUserObject(context, "");
//            File userImageFile = new File(context.getCacheDir(), user.getNickname() + "_img.png");
//            if (userImageFile.exists()) {
//                userImageFile.delete();
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static Bitmap mergeBitmaps(Bitmap logo, Bitmap qrcode) {
        Bitmap combined = Bitmap.createBitmap(qrcode.getWidth(), qrcode.getHeight(), qrcode.getConfig());
        Canvas canvas = new Canvas(combined);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        canvas.drawBitmap(qrcode, new Matrix(), null);
        Bitmap resizeLogo = Bitmap.createScaledBitmap(logo, canvasWidth / 5, canvasHeight / 5, true);
        int centreX = (canvasWidth - resizeLogo.getWidth()) / 2;
        int centreY = (canvasHeight - resizeLogo.getHeight()) / 2;
        canvas.drawBitmap(resizeLogo, centreX, centreY, null);
        return combined;
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void shareImage(Context context, String imageUrl) {
       // String userName = Utility.getCurrentUserNickName(context);
        String userName = "test";
        if(!TextUtils.isEmpty(userName)){
            // UTM parameters added.
            if (imageUrl.contains("?")) {
                imageUrl += "&" + Constants.DEEP_LINK_UTM_SOURCE;
            } else {
                imageUrl += "?" + Constants.DEEP_LINK_UTM_SOURCE;
            }
            imageUrl += "&" + Constants.DEEP_LINK_FROM_USERNAME + userName;
        }
        Uri contentUri = getBitmapUri(context);
        new ShareCompat.IntentBuilder(context)
                .setType("image/*")
                .addStream(contentUri)
                .setText(imageUrl)
                .startChooser();
    }


    private static Uri getBitmapUri(Context context) {
        File imagePath = new File(context.getCacheDir(), "QRCodeImages");
        File newFile = new File(imagePath, "image.jpeg");
        return FileProvider.getUriForFile(context, "", newFile);
    }
}
