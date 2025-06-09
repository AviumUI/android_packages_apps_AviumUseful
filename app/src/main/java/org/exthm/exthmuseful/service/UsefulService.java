package org.exthm.exthmuseful.service;

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
import android.util.Log;

import androidx.annotation.Nullable;

import org.exthm.exthmuseful.R;
import org.exthm.exthmuseful.service.music.MusicSuggestionService;
import org.exthm.exthmuseful.service.screen.ScreenUsefulService;
import org.exthm.exthmuseful.service.torch.TorchService;
import org.exthm.exthmuseful.service.url.ClipboardService;
import org.exthm.exthmuseful.service.delivery.DeliveryService;

public class UsefulService extends Service {
    private static final String TAG = "UsefulService";
    private static final String PREFS_NAME = "app_settings";
    private static final String CHANNEL_ID = "UsefulServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

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
                "music_suggestion".equals(key) ||
                "url_suggestion".equals(key)||
                "delivery_suggestion".equals(key)
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

        boolean isMusicSuggestion = sharedPref.getBoolean("music_suggestion", false);
        if (isMusicSuggestion) {
            if (!isServiceRunning(MusicSuggestionService.class)) {
                startService(new Intent(this, MusicSuggestionService.class));
                Log.d(TAG, "启动音乐建议服务");
            }
        } else {
            if (isServiceRunning(MusicSuggestionService.class)) {
                stopService(new Intent(this, MusicSuggestionService.class));
                Log.d(TAG, "停止音乐建议服务");
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

        scheduleSelfCheck();
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

        Intent restartIntent = new Intent(this, UsefulService.class);
        startService(restartIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}