package com.begenuin.library.core.interfaces;

import android.graphics.Matrix;

public interface OnMatrixChangeListener {
        void onChange(Matrix matrix);

        void onTouchDown();

        void onTouchRelease();

        void onTouchClick();
    }