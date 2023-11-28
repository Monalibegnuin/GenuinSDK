package com.begenuin.library.common;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import androidx.annotation.NonNull;

import com.begenuin.library.R;

public class FlipAnimator {
    private static String TAG = FlipAnimator.class.getSimpleName();
    private static AnimatorSet leftIn, rightOut, leftOut, rightIn, upIn, upOut;

    /**
     * Performs flip animation on two views
     */
    public static void flipView(Context context, final View back, final View front, boolean showFront) {
        leftIn = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_in);
        rightOut = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_right_out);
        leftOut = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_out);
        rightIn = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_right_in);

        final AnimatorSet showFrontAnim = new AnimatorSet();
        final AnimatorSet showBackAnim = new AnimatorSet();

        leftIn.setTarget(back);
        rightOut.setTarget(front);
        showFrontAnim.playTogether(leftIn, rightOut);

        leftOut.setTarget(back);
        rightIn.setTarget(front);
        showBackAnim.playTogether(rightIn, leftOut);

        if (showFront) {
            showFrontAnim.start();
        } else {
            showBackAnim.start();
        }
    }
    public static void verticallyFlipView(Context context, final View askQuestion, final View subscribe){
        askQuestion.setRotationX(-180f);
        upIn = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_up_in);
        upOut = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_up_out);
        float scale = context.getResources().getDisplayMetrics().density;
        askQuestion.setCameraDistance(8000*scale);
        subscribe.setCameraDistance(8000*scale);
        final AnimatorSet showBackAnim = new AnimatorSet();
        upOut.setTarget(subscribe);
        upIn.setTarget(askQuestion);
        showBackAnim.setStartDelay(500);
        showBackAnim.playTogether(upOut, upIn);
        showBackAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                subscribe.setEnabled(false);
                askQuestion.setEnabled(false);
                new Handler().postDelayed(() -> {
                    askQuestion.setVisibility(View.VISIBLE);
                }, 500);
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                subscribe.setRotationX(0f);
                subscribe.setEnabled(true);
                askQuestion.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
        showBackAnim.start();
    }
    public interface AnimationEndListener{

    }
}