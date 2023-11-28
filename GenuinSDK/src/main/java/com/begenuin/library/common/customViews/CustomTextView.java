package com.begenuin.library.common.customViews;

import android.content.Context;
import android.util.AttributeSet;


/**
 * Purpose: This class infoSubCropListModels the font to textView according to attribute
 *
 * @author
 * @version 1.0
 * @date
 */
public class CustomTextView extends androidx.appcompat.widget.AppCompatTextView {

    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        CustomFontHelper.setCustomFont(this, context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        CustomFontHelper.setCustomFont(this, context, attrs);
    }
}