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

import androidx.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressMatcherUtil {

    // 正则
    // 顺丰: SF 开头
    // 中通: 7 开头
    // 圆通: YT 开头
    // 极兔: JT 开头
    private static final Pattern EXPRESS_TRACKING_PATTERN =
            Pattern.compile("^(SF\\d{13}|7\\d{13}|YT\\d{13}|JT\\d{13})$");

    @Nullable
    public static String getMatchedExpressNumber(@Nullable String text) {
        if (text == null) {
            return null;
        }
        String trimmedText = text.trim(); // 去除前后空格
        if (trimmedText.isEmpty()) {
            return null;
        }

        Matcher matcher = EXPRESS_TRACKING_PATTERN.matcher(trimmedText);
        if (matcher.matches()) {
            return trimmedText; // 返回匹配到的完整单号
        }
        return null;
    }
}