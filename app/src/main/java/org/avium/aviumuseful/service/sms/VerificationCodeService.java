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

package org.avium.aviumuseful.service.sms;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import org.avium.aviumuseful.UsefulSettingsActivity;
import org.avium.aviumuseful.R;
import org.avium.aviumuseful.common.FloatingIconClickListener;
import org.avium.aviumuseful.common.FloatingIconManager;

public class VerificationCodeService extends Service implements FloatingIconClickListener {
    private static final String TAG = "VerificationCodeService";
    private static final String CHANNEL_ID = "VerificationCodeChannel";
    private static final int NOTIFICATION_ID = 6;
    private static final String EXTRA_CODE = "code";
    private static final long AUTO_HIDE_DELAY_MS = 10000; 

    private @Nullable FloatingIconManager floatingIconManager;
    private String currentCode;

    private Handler autoHideHandler;
    private Runnable autoHideRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        autoHideHandler = new Handler(Looper.getMainLooper());
        autoHideRunnable = () -> {
            if (floatingIconManager != null && floatingIconManager.isShowing()) {
                floatingIconManager.hide();
            }
        };

        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String code = intent.getStringExtra(EXTRA_CODE);
        if (TextUtils.isEmpty(code)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        currentCode = code;

        Notification notification = createNotification(getString(R.string.sms_code_notification_title));
        int fgsType = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
        try {
            startForeground(NOTIFICATION_ID, notification, fgsType);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start foreground service", e);
            stopSelf();
            return START_NOT_STICKY;
        }

        showFloatingIcon();

        return START_STICKY;
    }

    private void showFloatingIcon() {
        cancelAutoHideTimer();

        if (floatingIconManager != null) {
            if (floatingIconManager.isShowing()) {
                floatingIconManager.hide();
            }
            floatingIconManager.release();
        }

        floatingIconManager = new FloatingIconManager(this, R.drawable.icon_sms, this);
        floatingIconManager.show();

        startAutoHideTimer();
    }

    @Override
    public void onIconClick() {
        cancelAutoHideTimer();

        if (!TextUtils.isEmpty(currentCode)) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("verification_code", currentCode);
                clipboard.setPrimaryClip(clip);
                Log.d(TAG, "Verification code copied to clipboard: " + currentCode);
            }
        }

        if (floatingIconManager != null) {
            floatingIconManager.hide();
            floatingIconManager.release();
            floatingIconManager = null;
        }

        currentCode = null;
        stopSelf();
    }

    private void startAutoHideTimer() {
        cancelAutoHideTimer();
        autoHideHandler.postDelayed(autoHideRunnable, AUTO_HIDE_DELAY_MS);
    }

    private void cancelAutoHideTimer() {
        autoHideHandler.removeCallbacks(autoHideRunnable);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.sms_code_channel_name),
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String contentText) {
        Intent notificationIntent = new Intent(this, UsefulSettingsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.sms_code_notification_title))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelAutoHideTimer();
        if (floatingIconManager != null) {
            floatingIconManager.release();
            floatingIconManager = null;
        }
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
