package com.begenuin.library.common.customViews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.begenuin.library.R;
import com.begenuin.library.common.Utility;

public class CustomIcon extends LinearLayout {

    private ImageView imageView;

    public CustomIcon(Context context) {
        super(context);
    }

    public CustomIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context, attrs);
    }

    public CustomIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context, attrs);
    }

    private void initViews(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomIcon);
        int customIcon = typedArray.getResourceId(R.styleable.CustomIcon_custom_icon, R.drawable.ic_save_select);
        int insideImageHeight = typedArray.getInt(R.styleable.CustomIcon_insideIconHeight, 0);
        int insideImageWidth = typedArray.getInt(R.styleable.CustomIcon_insideIconWidth, 0);

        View view = inflate(context, R.layout.custom_icon, null);
        imageView = view.findViewById(R.id.ivCustomIcon);
        imageView.setImageResource(customIcon);

        if (customIcon != R.drawable.ic_reply && customIcon != R.drawable.ic_video) {
            imageView.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorWhite, null)));
        }

        if (insideImageHeight > 0 && insideImageWidth > 0) {
            imageView.getLayoutParams().width = insideImageWidth;
            imageView.getLayoutParams().height = insideImageHeight;
        }
        int containerSize = (int) Utility.dpToPx(48, context);
        LayoutParams layoutParams = new LayoutParams(containerSize, containerSize);
        view.setLayoutParams(layoutParams);
        super.addView(view);
    }

    public void setCustomIcon(int icon) {
        imageView.setImageResource(icon);
    }

    public ImageView getImageView() {
        return imageView;
    }
}
