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

package org.exthm.exthmuseful.service.screen;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.content.pm.ServiceInfo;
import android.util.Log;

import androidx.annotation.Nullable;

import org.exthm.exthmuseful.R;
import org.exthm.exthmuseful.common.FloatingIconClickListener;
import org.exthm.exthmuseful.common.FloatingIconManager;

public class ScreenUsefulService extends Service implements FloatingIconClickListener {
    private static final String TAG = "ScreenUsefulService";
    private static final String CUSTOM_ACTION = "org.exthm.action.SCREEN_NEED_RELIGHT";
    private static final String EXTRA_STATE = "state";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "screen_service_channel";

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private FloatingIconManager floatingIconManager;

    private final BroadcastReceiver screenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                releaseWakeLock();
                if (floatingIconManager != null) {
                    floatingIconManager.isShowing();
                }
            }
        }
    };

    private final BroadcastReceiver screenRelightReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && CUSTOM_ACTION.equals(intent.getAction())) {
                int state = intent.getIntExtra(EXTRA_STATE, 0);
                if (floatingIconManager != null) {
                    if (state == 1) {
                        floatingIconManager.show();
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (floatingIconManager != null) floatingIconManager.hide();
                        }, 5000);
                    } else {
                        floatingIconManager.hide();
                    }
                }
            }
        }
    };

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE,
                "exthmuseful:screen_lock"
        );
        floatingIconManager = new FloatingIconManager(
                this,
                R.drawable.icon_screen_lighton,
                this
        );

        registerReceivers();
        try {
            startForeground(NOTIFICATION_ID, createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } catch (Exception e) {
            stopSelf();
        }
    }

    private void registerReceivers() {
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenStateReceiver, screenFilter);

        IntentFilter relightFilter = new IntentFilter(CUSTOM_ACTION);
        registerReceiver(screenRelightReceiver, relightFilter, Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onIconClick() {
        turnOnScreen();
        if (floatingIconManager != null) {
            floatingIconManager.hide();
        }
    }

    private void turnOnScreen() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "屏幕建议",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setShowBadge(false);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        return new Notification.Builder(this, CHANNEL_ID)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingIconManager != null) {
            floatingIconManager.release();
            floatingIconManager = null;
        }
        releaseWakeLock();
        try {
            unregisterReceiver(screenRelightReceiver);
            unregisterReceiver(screenStateReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "错误 " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "未知错误", e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}