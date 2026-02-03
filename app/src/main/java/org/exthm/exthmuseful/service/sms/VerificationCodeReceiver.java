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

package org.exthm.exthmuseful.service.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

public class VerificationCodeReceiver extends BroadcastReceiver {
    private static final String TAG = "VerificationCodeReceiver";
    public static final String ACTION_CODE_RECEIVED = "org.avium.action.VERIFICATION_CODE_RECEIVED";
    public static final String EXTRA_CODE = "code";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (ACTION_CODE_RECEIVED.equals(intent.getAction())) {
            SharedPreferences sharedPref = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
            boolean isEnabled = sharedPref.getBoolean("sms_code_suggestion", false);
            
            if (!isEnabled) {
                return;
            }

            String code = intent.getStringExtra(EXTRA_CODE);
            if (!TextUtils.isEmpty(code)) {
                Intent serviceIntent = new Intent(context, VerificationCodeService.class);
                serviceIntent.putExtra(EXTRA_CODE, code);
                context.startForegroundService(serviceIntent);
            }
        }
    }
}
