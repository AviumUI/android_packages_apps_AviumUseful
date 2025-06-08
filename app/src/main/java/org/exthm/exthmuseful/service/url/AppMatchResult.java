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