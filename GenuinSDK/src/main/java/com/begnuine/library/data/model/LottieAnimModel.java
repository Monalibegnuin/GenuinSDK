package com.begnuine.library.data.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import com.begnuine.library.common.Utility;
import com.begnuine.library.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LottieAnimModel {

    int image;
    int background;

    public LottieAnimModel(int image, int background) {
        this.image = image;
        this.background = background;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public static HashMap<Integer, Integer> getMapData() {

        HashMap<Integer, Integer> tempData = new HashMap<>();
        tempData.put(R.raw.cow_face, R.color.color_bg_1);
        tempData.put(R.raw.alien, R.color.color_bg_2);
        tempData.put(R.raw.dog_face, R.color.color_bg_3);
        tempData.put(R.raw.sloth, R.color.color_bg_4);
        tempData.put(R.raw.frog, R.color.color_bg_5);
        tempData.put(R.raw.hear_no_evil_monkey, R.color.color_bg_6);
        tempData.put(R.raw.jack_o_lantern, R.color.color_bg_7);
        tempData.put(R.raw.owl, R.color.color_bg_8);
        tempData.put(R.raw.penguin, R.color.color_bg_9);
        tempData.put(R.raw.rabbit_face, R.color.color_bg_10);
        tempData.put(R.raw.pile_of_poo, R.color.color_bg_11);
        tempData.put(R.raw.pig_face, R.color.color_bg_12);
        tempData.put(R.raw.robot, R.color.color_bg_13);
        tempData.put(R.raw.ghost, R.color.color_bg_14);
        tempData.put(R.raw.teddy_bear, R.color.color_bg_15);
        tempData.put(R.raw.smiling_face_with_horns, R.color.color_bg_16);
        tempData.put(R.raw.smiling_face_with_sunglasses, R.color.color_bg_17);
        tempData.put(R.raw.snowman, R.color.color_bg_18);
        return tempData;
    }

    public static List<LottieAnimModel> getAllData() {
        List<LottieAnimModel> tempData = new ArrayList<>();
        tempData.add(new LottieAnimModel(R.raw.cow_face, R.color.color_bg_1));
        tempData.add(new LottieAnimModel(R.raw.alien, R.color.color_bg_2));
        tempData.add(new LottieAnimModel(R.raw.dog_face, R.color.color_bg_3));
        tempData.add(new LottieAnimModel(R.raw.sloth, R.color.color_bg_4));
        tempData.add(new LottieAnimModel(R.raw.frog, R.color.color_bg_5));
        tempData.add(new LottieAnimModel(R.raw.hear_no_evil_monkey, R.color.color_bg_6));
        tempData.add(new LottieAnimModel(R.raw.jack_o_lantern, R.color.color_bg_7));
        tempData.add(new LottieAnimModel(R.raw.owl, R.color.color_bg_8));
        tempData.add(new LottieAnimModel(R.raw.penguin, R.color.color_bg_9));
        tempData.add(new LottieAnimModel(R.raw.rabbit_face, R.color.color_bg_10));
        tempData.add(new LottieAnimModel(R.raw.pile_of_poo, R.color.color_bg_11));
        tempData.add(new LottieAnimModel(R.raw.pig_face, R.color.color_bg_12));
        tempData.add(new LottieAnimModel(R.raw.robot, R.color.color_bg_13));
        tempData.add(new LottieAnimModel(R.raw.ghost, R.color.color_bg_14));
        tempData.add(new LottieAnimModel(R.raw.teddy_bear, R.color.color_bg_15));
        tempData.add(new LottieAnimModel(R.raw.smiling_face_with_horns, R.color.color_bg_16));
        tempData.add(new LottieAnimModel(R.raw.smiling_face_with_sunglasses, R.color.color_bg_17));
        tempData.add(new LottieAnimModel(R.raw.snowman, R.color.color_bg_18));
        return tempData;
    }

    /*public static Pair<Drawable, ColorDrawable> getImageFromLottieAnimation(Context context, String imageName) {
        int resourceId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        int rawFileID = context.getResources().getIdentifier(imageName, "raw", context.getPackageName());
        Utility.printErrorLog("res-Id: " + resourceId + " rawFileID: " + rawFileID);
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), resourceId, null);
        ColorDrawable color = null;
        if (LottieAnimModel.getMapData().size() > 0 && LottieAnimModel.getMapData().containsKey(rawFileID)) {
            color = new ColorDrawable(context.getColor(LottieAnimModel.getMapData().get(rawFileID)));
        }
        //return new Pair(drawable, color);
        return Pair.create(drawable, color);
    }*/

  /*  public static Pair<Integer, Integer> getImageFromLottieAnimation(Context context, String imageName) {
        int resourceId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        int rawFileID = context.getResources().getIdentifier(imageName, "raw", context.getPackageName());
        int colorId = LottieAnimModel.getMapData().get(rawFileID);
        Utility.printErrorLog("res-Id: " + resourceId + " rawFileID: " + rawFileID + " colorId: " + colorId);
        return Pair.create(resourceId, colorId);
    }*/

    public static Drawable getImageFromLottieAnimation(Context context, String imageName) {
        int id = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), id, null);
        return drawable;
    }

    public static Integer getLottieBackgroundColor(Context context, String imageName) {
        int resourceId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        int rawFileID = context.getResources().getIdentifier(imageName, "raw", context.getPackageName());
        int colorId = LottieAnimModel.getMapData().get(rawFileID);
        int finalColor = ContextCompat.getColor(context, colorId);
        Utility.printErrorLog("res-Id: " + resourceId + " rawFileID: " + rawFileID + " colorId: " + colorId + " finalColor: " + finalColor);
        return finalColor;
    }


}
