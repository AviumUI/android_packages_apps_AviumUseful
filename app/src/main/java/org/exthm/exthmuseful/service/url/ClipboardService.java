package org.exthm.exthmuseful.service.url;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import org.exthm.exthmuseful.MainActivity;
import org.exthm.exthmuseful.R;
import org.exthm.exthmuseful.common.FloatingIconClickListener;
import org.exthm.exthmuseful.common.FloatingIconManager;

public class ClipboardService extends Service implements ClipboardListener.OnClipboardContentChanged, FloatingIconClickListener {

    private static final String TAG = "ClipboardService";
    private ClipboardListener clipboardListener;
    private @Nullable FloatingIconManager floatingIconManager;
    private @Nullable AppMatchResult currentAppMatch;
    private static final String CHANNEL_ID = "ClipboardMonitorChannel";
    private static final int NOTIFICATION_ID = 5;

    private Handler autoHideHandler;
    private Runnable autoHideRunnable;
    private static final long AUTO_HIDE_DELAY_MS = 5000; //自动隐藏

    @Override
    public void onCreate() {
        super.onCreate();
        clipboardListener = new ClipboardListener(this, this);
        //图标消失
        autoHideHandler = new Handler(Looper.getMainLooper());
        autoHideRunnable = () -> {
            if (floatingIconManager != null && floatingIconManager.isShowing()) {
                floatingIconManager.hide();
            }
        };

        createNotificationChannel();

        Notification notification = createNotification("URL建议");
        int fgsType = 0;
        fgsType = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
        try {
            startForeground(NOTIFICATION_ID, notification, fgsType);
        } catch (Exception e) {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (clipboardListener != null) {
            clipboardListener.startListening();
        }
        return START_STICKY;
    }

    @Override
    public void onContentChanged(String newContent) {
        AppMatchResult newMatch = DistinguishApp.distinguish(newContent);
        cancelAutoHideTimer();
        if (newMatch != null) {
            boolean recreateManager = false;
            if (floatingIconManager == null) {
                recreateManager = true;
            } else if (currentAppMatch == null || !currentAppMatch.getPackageName().equals(newMatch.getPackageName())) {
                if (floatingIconManager.isShowing()) floatingIconManager.hide();
                floatingIconManager.release();
                floatingIconManager = null;
                recreateManager = true;
            }

            if (recreateManager) {
                floatingIconManager = new FloatingIconManager(this, newMatch.getIconResId(), this);
            }

            currentAppMatch = newMatch;

            if (floatingIconManager != null) {
                if (!floatingIconManager.isShowing()) {
                    floatingIconManager.show();
                }
                startAutoHideTimer();
            }
        } else {
            if (floatingIconManager != null) {
                if (floatingIconManager.isShowing()) floatingIconManager.hide();
                floatingIconManager.release();
                floatingIconManager = null;
            }
            currentAppMatch = null;
        }
    }

    @Override
    public void onIconClick() {
        cancelAutoHideTimer();

        if (currentAppMatch != null && !TextUtils.isEmpty(currentAppMatch.getPackageName())) {
            PackageManager pm = getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(currentAppMatch.getPackageName());

            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    startActivity(launchIntent);
                } catch (Exception e) {
                    Log.e(TAG, "启动应用 " + currentAppMatch.getPackageName() + " 失败: " + e.getMessage());
                }
            } else {
                Log.w(TAG, "无法获取应用 " + currentAppMatch.getPackageName() + " 的启动 Intent。");
            }
        }

        if (floatingIconManager != null) {
            floatingIconManager.hide();
            floatingIconManager.release();
            floatingIconManager = null;
        }
        currentAppMatch = null;
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
                "URL建议",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("URL建议")
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
        if (clipboardListener != null) {
            clipboardListener.stopListening();
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