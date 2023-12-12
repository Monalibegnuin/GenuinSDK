package com.begenuin.library.views.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class Camera2PermissionDialog extends DialogFragment {

    public static final String FRAGMENT_DIALOG = "PermissionDialog";

    public static final int REQUEST_PERMISSIONS = 1;
    public static final int REQUEST_STORAGE_PERMISSIONS = 2;

    public static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    public static final String[] PHOTO_PERMISSIONS = {
            Manifest.permission.CAMERA,
    };

    public static final String[] AUDIO_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
    };

    public static final String[] STORAGE_PERMISSIONS_33 = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
    };

    public static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private Fragment mParentFragment;
    private String mRationaleMessage;

    public static Camera2PermissionDialog newInstance(Fragment mParentFragment, String mRationaleMessage) {
        Camera2PermissionDialog f = new Camera2PermissionDialog();
        f.mParentFragment = mParentFragment;
        f.mRationaleMessage = mRationaleMessage;
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(mRationaleMessage)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> getActivity().requestPermissions(VIDEO_PERMISSIONS, REQUEST_PERMISSIONS))
                .setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> mParentFragment.getActivity().finish())
                .create();
    }

}