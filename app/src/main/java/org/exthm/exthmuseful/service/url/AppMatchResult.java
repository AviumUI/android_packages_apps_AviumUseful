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

import androidx.annotation.Nullable;

public class AppMatchResult {
    private final String packageName;
    private final int iconResId; // 推荐的图标资源 ID 
    private final String matchedUrl; // 匹配到的具体 URL，用于打开应用时传递

    public AppMatchResult(String packageName, int iconResId, @Nullable String matchedUrl) {
        this.packageName = packageName;
        this.iconResId = iconResId;
        this.matchedUrl = matchedUrl;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getIconResId() {
        return iconResId;
    }

    @Nullable
    public String getMatchedUrl() {
        return matchedUrl;
    }

    public static final String PACKAGE_TAOBAO = "com.taobao.taobao";
    public static final String PACKAGE_BAIDU_NETDISK = "com.baidu.netdisk";
    public static final String PACKAGE_123YUNPAN = "com.mfcloudcalculate.networkdisk";
}