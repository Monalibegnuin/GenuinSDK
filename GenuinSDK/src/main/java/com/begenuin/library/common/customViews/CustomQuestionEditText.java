package com.begenuin.library.common.customViews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class CustomQuestionEditText extends AppCompatEditText {
    public CustomQuestionEditText(@NonNull Context context) {
        super(context);
    }

    public CustomQuestionEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomQuestionEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
