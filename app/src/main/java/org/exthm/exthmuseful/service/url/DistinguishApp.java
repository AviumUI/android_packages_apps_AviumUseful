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

import android.util.Log;
import androidx.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DistinguishApp {
    private static final String TAG = "DistinguishApp";

    @Nullable
    public static AppMatchResult distinguish(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        Log.d(TAG, "Distinguishing text: " + text);

        for (UrlMatchRules.AppUrlRule rule : UrlMatchRules.APP_RULES) {
            Pattern pattern = Pattern.compile(rule.getPattern(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return new AppMatchResult(
                    rule.getPackageName(),
                    rule.getIconResId(),
                    matcher.group(0),
                    false
                );
            }
        }

        Pattern genericPattern = Pattern.compile(
            UrlMatchRules.GENERIC_URL_RULE.getPattern(),
            Pattern.CASE_INSENSITIVE
        );
        Matcher genericMatcher = genericPattern.matcher(text);
        if (genericMatcher.find()) {
            String url = genericMatcher.group(0);
            Log.d(TAG, "匹配到通用URL: " + url);
            return new AppMatchResult(
                AppMatchResult.PACKAGE_WEB,
                UrlMatchRules.GENERIC_URL_RULE.getIconResId(),
                url,
                true
            );
        }

        Log.d(TAG, "未匹配");
        return null;
    }
}