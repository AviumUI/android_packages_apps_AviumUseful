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

package org.exthm.exthmuseful.service.torch;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.exthm.exthmuseful.UsefulSettingsActivity;
import org.exthm.exthmuseful.R;
import org.exthm.exthmuseful.common.FloatingIconClickListener;
import org.exthm.exthmuseful.common.FloatingIconManager;

public class TorchService extends Service implements TorchStateListener, FloatingIconClickListener {

    private static final String TAG = "TorchService";
    private TorchDetector torchDetector;
    private FloatingIconManager floatingIconManager;

    private static final String CHANNEL_ID = "TorchMonitorServiceChannel";
    private static final int NOTIFICATION_ID = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        torchDetector = new TorchDetector(this, this);
        floatingIconManager = new FloatingIconManager(
                this,
                R.drawable.icon_torch_off,
                this
        );

        createNotificationChannel();
        Notification notification = createNotification("");
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
        if (torchDetector != null && !torchDetector.isDetecting()) {
            boolean started = torchDetector.startDetection();
            if (!started) {
                Toast.makeText(this, "没有相机权限", Toast.LENGTH_LONG).show();
                stopSelf();
                return START_NOT_STICKY;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onTorchStateChanged(boolean isOn) {
        if (floatingIconManager != null) {
            if (isOn) {
                floatingIconManager.show();
            } else {
                floatingIconManager.hide();
            }
        }
    }

    @Override
    public void onTorchUnavailable(String cameraId) {
        if (floatingIconManager != null) {
            floatingIconManager.hide();
        }
        updateNotification("手电筒被占用了");
    }
    @Override
    public void onIconClick() {
        if (torchDetector != null) {
            torchDetector.setTorchMode(false);
        }
    }
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "手电筒监听",
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
                .setContentTitle("手电筒建议")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.icon_torch_off)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateNotification(String contentText) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            Notification notification = createNotification(contentText);
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (torchDetector != null) {
            torchDetector.stopDetection();
        }
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
