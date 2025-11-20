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