package org.exthm.exthmuseful.service.url;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class ClipboardListener implements ClipboardManager.OnPrimaryClipChangedListener {
    private static final String TAG = "ClipboardListener";
    private Context context;
    private ClipboardManager clipboardManager;
    private OnClipboardContentChanged callback;
    private Handler mainHandler; 

    public interface OnClipboardContentChanged {
        void onContentChanged(String newContent);
    }

    public ClipboardListener(Context context, OnClipboardContentChanged callback) {
        this.context = context.getApplicationContext();
        this.clipboardManager = (ClipboardManager) this.context.getSystemService(Context.CLIPBOARD_SERVICE);
        this.callback = callback;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void startListening() {
        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(this);
            checkClipboardContent();
        } 
    }

    public void stopListening() {
        if (clipboardManager != null) {
            clipboardManager.removePrimaryClipChangedListener(this);
        }
    }

    @Override
    public void onPrimaryClipChanged() {
        checkClipboardContent();
    }

    private void checkClipboardContent() {
        if (clipboardManager != null && clipboardManager.hasPrimaryClip()) {
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                ClipData.Item item = clipData.getItemAt(0);
                CharSequence text = item.getText(); 
                if (text != null) {
                    final String content = text.toString();
                    if (callback != null) {
                        mainHandler.post(() -> callback.onContentChanged(content));
                    }
                } 
            }
        }
    }
}