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

import android.util.Log;
import androidx.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressMatcherUtil {
    private static final String TAG = "ExpressMatcherUtil";

    /**
     * 匹配快递单号
     * @param text 输入文本
     * @return 匹配到的快递单号，未匹配返回null
     */
    @Nullable
    public static String getMatchedExpressNumber(@Nullable String text) {
        if (text == null) {
            return null;
        }
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            return null;
        }

        for (ExpressMatchRules.ExpressRule rule : ExpressMatchRules.RULES) {
            Pattern pattern = Pattern.compile(rule.getPattern());
            Matcher matcher = pattern.matcher(trimmedText);
            if (matcher.matches()) {
                return trimmedText;
            }
        }
        return null;
    }

    @Nullable
    public static ExpressMatchRules.ExpressRule getMatchedExpressRule(@Nullable String text) {
        if (text == null) {
            return null;
        }
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            return null;
        }

        for (ExpressMatchRules.ExpressRule rule : ExpressMatchRules.RULES) {
            Pattern pattern = Pattern.compile(rule.getPattern());
            Matcher matcher = pattern.matcher(trimmedText);
            if (matcher.matches()) {
                return rule;
            }
        }
        return null;
    }
}