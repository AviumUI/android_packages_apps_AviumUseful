package org.exthm.exthmuseful;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.exthm.exthmuseful.service.UsefulService;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        Log.d(TAG, "自启动成功");

        try {
            Intent serviceIntent = new Intent(context, UsefulService.class);
            context.startService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "自启动失败", e);
        }
    }
}