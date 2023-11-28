package com.begnuine.library.common.customViews;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.airbnb.lottie.LottieAnimationView;
import java.util.Random;
import de.hdodenhof.circleimageview.CircleImageView;
import com.begnuine.library.common.Utility;
import com.begnuine.library.R;
import com.begnuine.library.data.model.LottieAnimModel;

public class DisplayPictureView extends LinearLayout {

    private CircleImageView ivProfile;
    private LottieAnimationView animationView;
    private TextView tvInitials;
    private ImageView ivOwner;
    private int roundSize = 0;
    private float innerTextSize = 0f;
    private int lottiePadding = 0;
    private int borderColor = 0;
    private int borderWidth = 0;

    private final int[] dpColorList = new int[]{R.color.color_dp_1, R.color.color_dp_2, R.color.color_dp_3,
            R.color.color_dp_4, R.color.color_dp_5, R.color.color_dp_6,
            R.color.color_dp_7, R.color.color_dp_8, R.color.color_dp_9, R.color.color_dp_10, R.color.color_dp_11};

    public DisplayPictureView(Context context) {
        super(context);
    }

    public DisplayPictureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews(context, attrs);
    }

    public DisplayPictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context, attrs);
    }

    public DisplayPictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews(context, attrs);
    }

    private void initViews(Context context, AttributeSet attrs) {
        Resources resources = getResources();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DisplayPictureView);
        roundSize = typedArray.getDimensionPixelSize(R.styleable.DisplayPictureView_roundSize, resources.getDimensionPixelSize(R.dimen.roundSize));
        innerTextSize = typedArray.getDimension(R.styleable.DisplayPictureView_innerTextSize, resources.getDimension(R.dimen.innerTextSize));
        lottiePadding = typedArray.getDimensionPixelSize(R.styleable.DisplayPictureView_lottiePadding, resources.getDimensionPixelSize(R.dimen.dimen_10dp));
        borderColor = typedArray.getColor(R.styleable.DisplayPictureView_dpBorderColor, resources.getColor(R.color.transparent, null));
        borderWidth = typedArray.getDimensionPixelSize(R.styleable.DisplayPictureView_dpBorderWidth, 0);
        View view = inflate(context, R.layout.dp_view, null);
        ivProfile = view.findViewById(R.id.ivProfile);
        if (borderWidth > 0) {
            ivProfile.setBorderWidth(borderWidth);
            ivProfile.setBorderColor(borderColor);
        }
        animationView = view.findViewById(R.id.animationView);
        animationView.setPadding(lottiePadding, lottiePadding, lottiePadding, lottiePadding);
        tvInitials = view.findViewById(R.id.tvInitials);
        ivOwner = view.findViewById(R.id.ivOwner);
        tvInitials.setTextSize(TypedValue.COMPLEX_UNIT_PX, innerTextSize);
        typedArray.recycle();
        super.addView(view);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = roundSize;
        params.height = roundSize;
        setLayoutParams(params);
    }

    public void setDpWithImage(Context context, boolean isAvatar, String profileImage, String smallProfileImage, boolean isExpand) {
        tvInitials.setVisibility(View.GONE);
        ivOwner.setVisibility(View.GONE);
        if (isAvatar) {
            int res = context.getResources().getIdentifier(profileImage,
                    "raw", context.getPackageName());
            if (LottieAnimModel.getMapData().containsKey(res)) {
                ivProfile.setImageResource(R.color.transparent);
                ivProfile.setCircleBackgroundColorResource(LottieAnimModel.getMapData().get(res));
                animationView.setVisibility(View.VISIBLE);
                animationView.setAnimation(res);
                animationView.playAnimation();
            }
        } else {
            animationView.setVisibility(View.GONE);
            ivProfile.setCircleBackgroundColorResource(R.color.transparent);
            Utility.displayProfileImage(context, profileImage, ivProfile);
        }
    }

    public void setDpForOwner(Activity context, boolean isAvatar, String profileImage) {
        tvInitials.setVisibility(View.GONE);
        ivOwner.setVisibility(View.VISIBLE);
        if (isAvatar) {
            int res = context.getResources().getIdentifier(profileImage,
                    "raw", context.getPackageName());
            if (LottieAnimModel.getMapData().containsKey(res)) {
                ivProfile.setImageResource(R.color.transparent);
                ivProfile.setCircleBackgroundColorResource(LottieAnimModel.getMapData().get(res));
                animationView.setVisibility(View.VISIBLE);
                animationView.setAnimation(res);
                animationView.playAnimation();
            }
        } else {
            animationView.setVisibility(View.GONE);
            ivProfile.setCircleBackgroundColorResource(R.color.transparent);
            Utility.displayProfileImage(context, profileImage, ivProfile);
        }
    }

    public void setEmptyDp() {
        animationView.setVisibility(View.GONE);
        tvInitials.setVisibility(View.GONE);
        ivOwner.setVisibility(View.GONE);
        ivProfile.setImageResource(R.drawable.ic_empty_dp);
    }

    public void setPlaceHolderContactDp() {
        animationView.setVisibility(View.GONE);
        tvInitials.setVisibility(View.GONE);
        ivOwner.setVisibility(View.GONE);
        ivProfile.setCircleBackgroundColorResource(R.color.transparent);
        ivProfile.setImageResource(R.drawable.placeholder_contact);
    }

    public void setBorderColor(int borderColor) {
        if (ivProfile != null) {
            ivProfile.setBorderWidth(borderWidth);
            ivProfile.setBorderColor(borderColor);
        }
    }

    public void removeBorder() {
        if (ivProfile != null) {
            ivProfile.setBorderWidth(0);
        }
    }


    public void setDpWithInitials(String name, String colorCode, String textColorCode) {
        animationView.setVisibility(View.GONE);
        tvInitials.setVisibility(View.VISIBLE);
        ivProfile.setImageResource(R.color.transparent);
        ivProfile.setCircleBackgroundColorResource(R.color.transparent);
        if (TextUtils.isEmpty(colorCode)) {
            // Changed from random color to specific color to align new loop design with loop publish screen Display Picture display
            ivProfile.setCircleBackgroundColor(Color.parseColor("#A4E6DA"));
        } else {
            ivProfile.setCircleBackgroundColor(Color.parseColor(colorCode));
        }
        setTextForDp(name);
        if (TextUtils.isEmpty(textColorCode)) {
            // Changed from random color to specific color to align new loop design with loop publish screen Display Picture display
            tvInitials.setTextColor(Color.parseColor("#49CDB5"));
        } else {
            tvInitials.setTextColor(Color.parseColor(textColorCode));
        }
    }

    public void setTextForDp(String name) {
        name = name.replaceAll("\\s+", " ").trim();
        String[] nameArr = name.split(" ");
        if (nameArr.length > 1) {
            tvInitials.setText(String.format("%s%s", getFirstSymbol(nameArr[0].toUpperCase()), getFirstSymbol(nameArr[1].toUpperCase())));
        } else if (nameArr.length == 1) {
            tvInitials.setText(String.format("%s", getFirstSymbol(nameArr[0].toUpperCase())));
        } else {
            tvInitials.setText("");
        }
    }

    private int getRandomDpColor() {
        Random random = new Random();
        int no = random.nextInt(11);
        return dpColorList[no];
    }

    private String getFirstSymbol(String text) {
        StringBuilder sequence = new StringBuilder(text.length());
        boolean isInJoin = false;
        int codePoint;

        for (int i = 0; i < text.length(); i = text.offsetByCodePoints(i, 1)) {
            codePoint = text.codePointAt(i);

            if (codePoint == 0x200D) {
                isInJoin = true;
                if (sequence.length() == 0)
                    continue;
            } else {
                if ((sequence.length() > 0) && (!isInJoin)) break;
                isInJoin = false;
            }

            sequence.appendCodePoint(codePoint);
        }

        if (isInJoin) {
            for (int i = sequence.length() - 1; i >= 0; --i) {
                if (sequence.charAt(i) == 0x200D)
                    sequence.deleteCharAt(i);
                else
                    break;
            }
        }
        return sequence.toString();
    }

    private void updateBlendingMode() {
        Paint paint = tvInitials.getPaint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
        tvInitials.invalidate();
    }
}
