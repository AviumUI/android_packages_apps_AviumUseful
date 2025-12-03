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

package org.exthm.exthmuseful.service.delivery;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.exthm.exthmuseful.R;
import org.exthm.exthmuseful.common.FloatingIconClickListener;
import org.exthm.exthmuseful.common.FloatingIconManager;
import org.exthm.exthmuseful.service.url.ClipboardListener;

public class DeliveryService extends Service implements
        ClipboardListener.OnClipboardContentChanged,
        FloatingIconClickListener {

    private static final String TAG = "DeliveryService";
    private static final String CHANNEL_ID = "DeliveryServiceChannel";
    private static final int NOTIFICATION_ID = 6;

    private ClipboardListener clipboardListener;
    private FloatingIconManager floatingIconManager;
    private String currentDetectedExpressNumber = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        clipboardListener = new ClipboardListener(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification notification = buildForegroundNotification();
        startForeground(NOTIFICATION_ID, notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);

        if (clipboardListener != null) {
            clipboardListener.startListening();
        }

        return START_STICKY;
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "快递建议",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification buildForegroundNotification() {
        Intent notificationIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_box)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (clipboardListener != null) {
            clipboardListener.stopListening();
        }
        if (floatingIconManager != null) {
            floatingIconManager.release();
            floatingIconManager = null;
        }
        stopForeground(true);
    }

    @Override
    public void onContentChanged(String newContent) {
        currentDetectedExpressNumber = ExpressMatcherUtil.getMatchedExpressNumber(newContent);

        if (currentDetectedExpressNumber != null) {

            Context appContext = getApplicationContext();
            if (floatingIconManager == null) {
                floatingIconManager = new FloatingIconManager(appContext, R.drawable.icon_box, (FloatingIconClickListener) this);
            }

            if (!floatingIconManager.isShowing()) {
                floatingIconManager.show();
            }
        } else {
            currentDetectedExpressNumber = null;
            if (floatingIconManager != null && floatingIconManager.isShowing()) {
                floatingIconManager.hide();
            }
        }
    }
    @Override
    public void onIconClick() {
        if (currentDetectedExpressNumber != null) {
            String url = "https://www.baidu.com/s?wd=" + Uri.encode(currentDetectedExpressNumber);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(browserIntent);
            } catch (ActivityNotFoundException ignored) {
            }

            if (floatingIconManager != null && floatingIconManager.isShowing()) {
                floatingIconManager.hide();
            }
        }
    }
}