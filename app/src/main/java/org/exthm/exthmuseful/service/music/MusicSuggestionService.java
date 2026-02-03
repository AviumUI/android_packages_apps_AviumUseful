/*
 *
 * Copyright (C) 2025 The AviumUI Project
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.exthm.exthmuseful.service.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import org.exthm.exthmuseful.UsefulSettingsActivity;
import org.exthm.exthmuseful.R;
import org.exthm.exthmuseful.common.FloatingIconClickListener;
import org.exthm.exthmuseful.common.FloatingIconManager;

public class MusicSuggestionService extends Service implements HeadsetStateListener, FloatingIconClickListener {

    private static final String defaultMusicApp = String.valueOf(R.string.default_music_app);

    private static final String TAG = "MusicSuggestionService";
    private EarConnectListener earConnectListener;
    private FloatingIconManager floatingIconManager;

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_MUSIC_APP_PACKAGE = "music_app_suggestion";

    private static final String CHANNEL_ID = "MusicSuggestionChannel";
    private static final int NOTIFICATION_ID = 4;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MusicSuggestionService onCreate");

        earConnectListener = new EarConnectListener(this, this);
        floatingIconManager = new FloatingIconManager(
                this,
                R.drawable.icon_music,
                this
        );

        createNotificationChannel();
        Notification notification = createNotification();
        int fgsType = 0;
        fgsType = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
        try {
            startForeground(NOTIFICATION_ID, notification, fgsType);
        } catch (Exception e) {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (earConnectListener != null) {
            earConnectListener.startListening();
        }
        return START_STICKY;
    }

    @Override
    public void onHeadsetConnected(String deviceName, boolean isBluetooth) {
        if (floatingIconManager != null) {
            SharedPreferences appSharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String musicAppPackage = appSharedPref.getString(KEY_MUSIC_APP_PACKAGE, null);
            if (!TextUtils.isEmpty(musicAppPackage)) {
                floatingIconManager.show();
            } else {
                musicAppPackage = defaultMusicApp;
            }
        }
    }

    @Override
    public void onHeadsetDisconnected(boolean isBluetooth) {
        if (floatingIconManager != null) {
            floatingIconManager.hide();
        }
    }
    @Override
    public void onIconClick() {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String musicAppPackage = sharedPref.getString(KEY_MUSIC_APP_PACKAGE, null);

        if (!TextUtils.isEmpty(musicAppPackage)) {
            PackageManager pm = getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(musicAppPackage);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(launchIntent);
                } catch (Exception e) {
                    Log.e(TAG, "m没有此应用" + musicAppPackage);
                }
            } else {
                Log.w(TAG, "没有此应用" + musicAppPackage);
            }
        }

        if (floatingIconManager != null) {
            floatingIconManager.hide();
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "音乐建议服务",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, UsefulSettingsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("音乐建议")
                .setSmallIcon(R.drawable.icon_music)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (earConnectListener != null) {
            earConnectListener.stopListening();
        }
        if (floatingIconManager != null) {
            floatingIconManager.release();
        }
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
