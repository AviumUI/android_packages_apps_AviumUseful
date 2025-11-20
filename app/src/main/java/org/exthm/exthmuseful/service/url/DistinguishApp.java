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
import org.exthm.exthmuseful.R;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DistinguishApp {
    private static final String TAG = "DistinguishApp";

    // --- 正则表达式 ---
    // 淘宝 
    private static final Pattern TAOBAO_PATTERN = Pattern.compile(
            "(https?://(?:[\\w-]+\\.)?(?:e\\.tb\\.cn|m\\.tb\\.cn|item\\.taobao\\.com|a\\.m\\.taobao\\.com|s\\.taobao\\.com|detail\\.tmall\\.com)[^\\s\"]*)|(￥[a-zA-Z0-9]{8,15}￥|《[a-zA-Z0-9]{8,15}《|喵口令.{0,10}￥[a-zA-Z0-9]{8,15}￥)", // 匹配淘口令
            Pattern.CASE_INSENSITIVE
    );

    // 百度网盘 
    private static final Pattern BAIDU_NETDISK_PATTERN = Pattern.compile(
            "https?://pan\\.baidu\\.com/s/[\\w-]+",
            Pattern.CASE_INSENSITIVE
    );

    // 123云盘 
    private static final Pattern ONETWOTHREE_YUNPAN_PATTERN = Pattern.compile(
            "https?://(?:www\\.)?(?:123pan\\.com|123865\\.com)/s/[\\w-]+", 
            Pattern.CASE_INSENSITIVE
    );

    // 哔哩哔哩 
    private static final Pattern BILIBILI_PATTERN = Pattern.compile(
            "https?://(?:[\\w-]+\\.)?(?:b23\\.tv|bilibili\\.com)[^\\s\"]*",
            Pattern.CASE_INSENSITIVE
    );

    // 抖音 
    private static final Pattern DOUYIN_PATTERN = Pattern.compile(
            "https?://(?:[\\w-]+\\.)?douyin\\.com[^\\s\"]*",
            Pattern.CASE_INSENSITIVE
    );

    @Nullable
    public static AppMatchResult distinguish(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        Log.d(TAG, "Distinguishing text: " + text);

        Matcher matcher;

        // 淘宝匹配
        matcher = TAOBAO_PATTERN.matcher(text);
        if (matcher.find()) {
            return new AppMatchResult(AppMatchResult.PACKAGE_TAOBAO, R.drawable.icon_taobao, matcher.group(0)); 
        }

        // 百度网盘匹配
        matcher = BAIDU_NETDISK_PATTERN.matcher(text);
        if (matcher.find()) {
            return new AppMatchResult(AppMatchResult.PACKAGE_BAIDU_NETDISK, R.drawable.icon_baidu_netdisk, matcher.group(0));
        }

        // 123云盘匹配
        matcher = ONETWOTHREE_YUNPAN_PATTERN.matcher(text);
        if (matcher.find()) {
            return new AppMatchResult(AppMatchResult.PACKAGE_123YUNPAN, R.drawable.icon_pandownload, matcher.group(0));
        }

        Log.d(TAG, "未匹配");
        return null;
    }
}