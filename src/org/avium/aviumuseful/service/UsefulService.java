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

package org.avium.aviumuseful.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import org.avium.aviumuseful.R;
import org.avium.aviumuseful.service.music.MusicSuggestionService;
import org.avium.aviumuseful.service.screen.ScreenUsefulService;
import org.avium.aviumuseful.service.sms.VerificationCodeReceiver;
import org.avium.aviumuseful.service.torch.TorchService;
import org.avium.aviumuseful.service.url.ClipboardService;
import org.avium.aviumuseful.service.delivery.DeliveryService;

public class UsefulService extends Service {
    private static final String TAG = "UsefulService";
    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_MUSIC_SWITCH = "music_suggestion_switch";
    private static final String KEY_MUSIC_PACKAGE = "music_app_package_name";
    private static final String CHANNEL_ID = "UsefulServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private VerificationCodeReceiver smsCodeReceiver;
    private boolean isSmsCodeReceiverRegistered = false;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "主服务",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private Notification createNotification() {
        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("主服务")
                .setSmallIcon(R.drawable.icon_screen_lighton)
                .setOngoing(true)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "服务启动");

        registerPreferenceChangeListener();

        new Handler(Looper.getMainLooper()).postDelayed(this::checkAndStartSubServices, 500);
        return START_STICKY;
    }

    private void registerPreferenceChangeListener() {
        if (preferenceChangeListener != null) return;
        //在这里添加sharpperference监听
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferenceChangeListener = (sharedPreferences, key) -> {
            if ("screen_always_on".equals(key) ||
                "torch_suggestion".equals(key)||
                KEY_MUSIC_SWITCH.equals(key) ||
                "url_suggestion".equals(key)||
                "delivery_suggestion".equals(key) ||
                "sms_code_suggestion".equals(key)
            ) {
                new Handler(Looper.getMainLooper()).post(this::checkAndStartSubServices);
            }
        };
        sharedPref.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void checkAndStartSubServices() {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        //在这里拉起服务
        boolean isScreenOn = sharedPref.getBoolean("screen_always_on", false);
        if (isScreenOn) {
            if (!isServiceRunning(ScreenUsefulService.class)) {
                startService(new Intent(this, ScreenUsefulService.class));
                Log.d(TAG, "启动屏幕常亮服务");
            }
        } else {
            if (isServiceRunning(ScreenUsefulService.class)) {
                stopService(new Intent(this, ScreenUsefulService.class));
                Log.d(TAG, "停止屏幕常亮服务");
            }
        }

        boolean isTorchSuggestion = sharedPref.getBoolean("torch_suggestion", false);
        if (isTorchSuggestion) {
            if (!isServiceRunning(TorchService.class)) {
                startService(new Intent(this, TorchService.class));
                Log.d(TAG, "启动手电筒建议服务");
            }
        } else {
            if (isServiceRunning(TorchService.class)) {
                stopService(new Intent(this, TorchService.class));
                Log.d(TAG, "停止手电筒建议服务");
            }
        }

        boolean isMusicSwitchOn = sharedPref.getBoolean(KEY_MUSIC_SWITCH, false);
        // Get music app package.
        // If not specified, use R.string.default_music_app.
        String musicPackage = sharedPref.getString(KEY_MUSIC_PACKAGE, getString(R.string.default_music_app));
        if (isMusicSwitchOn && !TextUtils.isEmpty(musicPackage)) {
            if (!isServiceRunning(MusicSuggestionService.class)) {
                startService(new Intent(this, MusicSuggestionService.class));
                Log.d(TAG, "Started music suggestion service");
            }
        } else {
            if (isServiceRunning(MusicSuggestionService.class)) {
                stopService(new Intent(this, MusicSuggestionService.class));
                Log.d(TAG, "Stopped music suggestion service");
            }
        }

        boolean isUrlSuggestion = sharedPref.getBoolean("url_suggestion", false);
        if (isUrlSuggestion) {
            if (!isServiceRunning(ClipboardService.class)) {
                startService(new Intent(this, ClipboardService.class));
                Log.d(TAG, "启动URL建议服务");
            }
        } else {
            if (isServiceRunning(ClipboardService.class)) {
                stopService(new Intent(this, ClipboardService.class));
                Log.d(TAG, "停止URL建议服务");
            }
        }

        boolean isDeliverySuggestion = sharedPref.getBoolean("delivery_suggestion", false);
        if (isDeliverySuggestion) {
            if (!isServiceRunning(DeliveryService.class)) {
                startService(new Intent(this, DeliveryService.class));
                Log.d(TAG, "启动快递建议服务");
            }
        } else {
            if (isServiceRunning(DeliveryService.class)) {
                stopService(new Intent(this, DeliveryService.class));
                Log.d(TAG, "停止快递建议服务");
            }
        }

        boolean isSmsCodeSuggestion = sharedPref.getBoolean("sms_code_suggestion", false);
        if (isSmsCodeSuggestion) {
            registerSmsCodeReceiver();
        } else {
            unregisterSmsCodeReceiver();
        }

        scheduleSelfCheck();
    }

    private void registerSmsCodeReceiver() {
        if (isSmsCodeReceiverRegistered) {
            return;
        }
        try {
            if (smsCodeReceiver == null) {
                smsCodeReceiver = new VerificationCodeReceiver();
            }
            android.content.IntentFilter filter = new android.content.IntentFilter();
            filter.addAction(VerificationCodeReceiver.ACTION_CODE_RECEIVED);
            registerReceiver(smsCodeReceiver, filter);
            isSmsCodeReceiverRegistered = true;
        } catch (Exception e) {
            //ntd
        }
    }

    private void unregisterSmsCodeReceiver() {
        if (!isSmsCodeReceiverRegistered || smsCodeReceiver == null) {
            return;
        }
        try {
            unregisterReceiver(smsCodeReceiver);
            isSmsCodeReceiverRegistered = false;
        } catch (Exception e) {
            //ntd
        }
    }

    private void scheduleSelfCheck() {
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAndStartSubServices, 30_000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (preferenceChangeListener != null) {
            sharedPref.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
            preferenceChangeListener = null;
        }

        unregisterSmsCodeReceiver();

        Intent restartIntent = new Intent(this, UsefulService.class);
        startService(restartIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
