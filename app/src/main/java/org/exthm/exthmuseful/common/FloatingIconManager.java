/*
 * Copyright (C) 2025-2026 The AviumUI Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.exthm.exthmuseful.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.view.animation.DecelerateInterpolator;

import org.exthm.exthmuseful.R;

public class FloatingIconManager {
    private static final String TAG = "FloatingIconManager";

    private Context context;
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams floatingViewParams;
    private boolean isViewAdded = false;
    private boolean isAnimating = false; // 防止动画期间重复操作
    private FloatingIconClickListener clickListener;
    private final int iconResourceId;
    private float initialTouchX_swipe;
    private float initialTouchY_swipe;
    private static final int CLICK_THRESHOLD_DP = 10;
    private static final int SWIPE_RIGHT_DISMISS_THRESHOLD_DP = 50;
    private static final int SWIPE_UP_DISMISS_THRESHOLD_DP = 50;
    private final int clickThresholdPx;
    private final int swipeRightDismissThresholdPx;
    private final int swipeUpDismissThresholdPx;

    private static final long ANIMATION_DURATION = 150; // 动画时长 (毫秒)，尽量短
    private static final long VIBRATION_DURATION = 10;

    private final Vibrator vibrator;

    public FloatingIconManager(Context context, int iconResourceId, FloatingIconClickListener listener) {
        this.context = context.getApplicationContext();
        this.windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        this.vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        this.iconResourceId = iconResourceId;
        this.clickListener = listener;
        this.clickThresholdPx = dpToPx(CLICK_THRESHOLD_DP);
        this.swipeRightDismissThresholdPx = dpToPx(SWIPE_RIGHT_DISMISS_THRESHOLD_DP);
        this.swipeUpDismissThresholdPx = dpToPx(SWIPE_UP_DISMISS_THRESHOLD_DP);

        createFloatingViewInternal();
    }

    private void doVibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(VIBRATION_DURATION);
            }
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (metrics.densityDpi / (float) DisplayMetrics.DENSITY_DEFAULT));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createFloatingViewInternal() {
        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                dpToPx(48),
                dpToPx(48)
        );
        container.setLayoutParams(containerParams);
        container.setBackgroundResource(R.drawable.circle_background);

        ImageView imageView = new ImageView(context);
        imageView.setImageResource(this.iconResourceId);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                dpToPx(32),
                dpToPx(32)
        );
        imageParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(imageParams);
        container.addView(imageView);

        container.setOnTouchListener((v, event) -> {
            if (isAnimating) return true; // 动画期间忽略触摸

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialTouchX_swipe = event.getRawX();
                    initialTouchY_swipe = event.getRawY();
                    doVibrate(); 
                    return true;

                case MotionEvent.ACTION_UP:
                    float finalTouchX = event.getRawX();
                    float finalTouchY = event.getRawY();
                    float deltaX = finalTouchX - initialTouchX_swipe;
                    float deltaY = finalTouchY - initialTouchY_swipe;

                    if (Math.abs(deltaX) < clickThresholdPx && Math.abs(deltaY) < clickThresholdPx) {
                        if (clickListener != null) {
                            clickListener.onIconClick();
                        }
                    } else if (deltaX > swipeRightDismissThresholdPx) {
                        doVibrate(); 
                        hide();
                    } else if (deltaY < -swipeUpDismissThresholdPx) {
                        doVibrate(); 
                        hide();
                    }
                    return true;
            }
            return false;
        });

        floatingView = container;
        // 初始时设置为不可见且缩放到最小，为出现动画做准备
        floatingView.setScaleX(0f);
        floatingView.setScaleY(0f);
        floatingView.setAlpha(0f); // 也可以结合透明度动画

        int layoutParamsType;
        layoutParamsType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;


        floatingViewParams = new WindowManager.LayoutParams(
                dpToPx(50),
                dpToPx(50),
                layoutParamsType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );

        floatingViewParams.gravity = Gravity.BOTTOM | Gravity.END;
        floatingViewParams.x = dpToPx(20);
        floatingViewParams.y = dpToPx(15);
    }

    public void show() {
        if (!Settings.canDrawOverlays(context)) {
            return;
        }

        if (floatingView != null && !isViewAdded && !isAnimating) {
            try {
                if (windowManager != null) {
                    floatingView.setScaleX(0f);
                    floatingView.setScaleY(0f);
                    floatingView.setAlpha(0f);

                    windowManager.addView(floatingView, floatingViewParams);
                    isViewAdded = true;
                    isAnimating = true;

                    floatingView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(ANIMATION_DURATION)
                            .setInterpolator(new DecelerateInterpolator())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    isAnimating = false;
                                }
                            })
                            .start();
                }
            } catch (Exception e) {
                if (isViewAdded) {
                    try { windowManager.removeView(floatingView); } catch (Exception ignored) {}
                    isViewAdded = false;
                }
                isAnimating = false;
            }
        }
    }

    public void hide() {
        if (isViewAdded && floatingView != null && windowManager != null && !isAnimating) {
            isAnimating = true;

            floatingView.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new DecelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (isViewAdded && floatingView != null && windowManager != null) {
                                try {
                                    windowManager.removeView(floatingView);
                                    isViewAdded = false;
                                } catch (Exception e) {
                                    isViewAdded = false;
                                }
                            }
                            isAnimating = false;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            if (isViewAdded && floatingView != null && windowManager != null) {
                                try {
                                    windowManager.removeView(floatingView);
                                } catch (Exception ignored) {}
                            }
                            isViewAdded = false;
                            isAnimating = false;
                        }
                    })
                    .start();
        }
    }

    public boolean isShowing() {
        return isViewAdded;
    }


    public void release() {
        if (isAnimating && floatingView != null) {
            floatingView.animate().cancel();
        }
        hide();
        floatingView = null;
        windowManager = null;
        clickListener = null;
        context = null;
    }
}