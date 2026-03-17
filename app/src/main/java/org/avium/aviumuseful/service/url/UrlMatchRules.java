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

package org.avium.aviumuseful.service.url;

import org.avium.aviumuseful.R;

/**
 * 网址匹配规则配置文件
 */
public class UrlMatchRules {

    /**
     * 应用网址匹配规则数组
     */
    public static final AppUrlRule[] APP_RULES = {
        // 淘宝
        new AppUrlRule(
            AppMatchResult.PACKAGE_TAOBAO,
            R.drawable.icon_taobao,
            "(https?://(?:[\\w-]+\\.)?(?:e\\.tb\\.cn|m\\.tb\\.cn|item\\.taobao\\.com|a\\.m\\.taobao\\.com|s\\.taobao\\.com|detail\\.tmall\\.com)[^\\s\"]*)|(￥[a-zA-Z0-9]{8,15}￥|《[a-zA-Z0-9]{8,15}《|喵口令.{0,10}￥[a-zA-Z0-9]{8,15}￥)",
            "淘宝/天猫链接或淘口令"
        ),
        
        // 百度网盘
        new AppUrlRule(
            AppMatchResult.PACKAGE_BAIDU_NETDISK,
            R.drawable.icon_baidu_netdisk,
            "https?://pan\\.baidu\\.com/s/[\\w-]+",
            "百度网盘分享链接"
        ),
        
        // 123云盘
        new AppUrlRule(
            AppMatchResult.PACKAGE_123YUNPAN,
            R.drawable.icon_pandownload,
            "https?://(?:www\\.)?(?:123pan\\.com|123865\\.com)/s/[\\w-]+",
            "123云盘分享链接"
        ),
        
        // 哔哩哔哩
        new AppUrlRule(
            AppMatchResult.PACKAGE_BILIBILI,
            R.drawable.icon_box,
            "https?://(?:[\\w-]+\\.)?(?:b23\\.tv|bilibili\\.com)[^\\s\"]*",
            "哔哩哔哩视频链接"
        ),
        
        // 抖音
        new AppUrlRule(
            AppMatchResult.PACKAGE_DOUYIN,
            R.drawable.douyin,
            "https?://(?:[\\w-]+\\.)?douyin\\.com[^\\s\"]*",
            "抖音视频链接"
        ),
        
        // 拼多多
        new AppUrlRule(
            "com.xunmeng.pinduoduo",
            R.drawable.icon_pdd,
            "(https?://mobile\\.yangkeduo\\.com[^\\s\"]*)|([0-9a-zA-Z]{8,12})",
            "拼多多商品链接"
        ),
        
        // 京东
        new AppUrlRule(
            "com.jingdong.app.mall",
            R.drawable.icon_box,
            "https?://(?:[\\w-]+\\.)?(?:jd\\.com|3\\.cn)[^\\s\"]*",
            "京东商品链接"
        ),
        
        // 小红书
        new AppUrlRule(
            "com.xingin.xhs",
            R.drawable.icon_box,
            "https?://(?:[\\w-]+\\.)?(?:xiaohongshu\\.com|xhslink\\.com)[^\\s\"]*",
            "小红书笔记链接"
        ),
        
        // 微博
        new AppUrlRule(
            "com.sina.weibo",
            R.drawable.icon_box,
            "https?://(?:[\\w-]+\\.)?(?:weibo\\.com|weibo\\.cn)[^\\s\"]*",
            "微博链接"
        ),
        
        // 知乎
        new AppUrlRule(
            "com.zhihu.android",
            R.drawable.icon_box,
            "https?://(?:[\\w-]+\\.)?zhihu\\.com[^\\s\"]*",
            "知乎问答链接"
        ),
        
        // 网易云音乐
        new AppUrlRule(
            "com.netease.cloudmusic",
            R.drawable.icon_music,
            "https?://music\\.163\\.com[^\\s\"]*",
            "网易云音乐链接"
        ),
        
        // QQ音乐
        new AppUrlRule(
            "com.tencent.qqmusic",
            R.drawable.icon_music,
            "https?://(?:y\\.qq\\.com|c\\.y\\.qq\\.com)[^\\s\"]*",
            "QQ音乐链接"
        ),

        // 闲鱼
        new AppUrlRule(
            "com.taobao.idlefish", 
            R.drawable.icon_idlefish, 
            "(https?://(?:[\\w-]+\\.)?(?:goofish\\.com|idlefish\\.com)[^\\s\\ ]*)|(m\\.tb\\.cn/h\\.[\\w-]+)|(fu\uD83D\uDE4B\u200D♂️[a-zA-Z0-9]{8,15}口|淘\uD83D\uDE4B\u200D♂️[a-zA-Z0-9]{8,15}吧)", 
            "闲鱼商品链接或闲口令"
        ),

        // 支付宝
        new AppUrlRule(
            "com.eg.android.AlipayGphone", 
            R.drawable.icon_alipay, 
            "(https?://(?:[\\w-]+\\.)?alipay(?:objects)?\\.com/(?:_|[\\w-?=&/])+)|(#[a-zA-Z0-9]{10,25}#)|([a-zA-Z0-9]{8,15}\\s+http:/\\s+W:/F\\s+[a-zA-Z0-9]+\\s+\\$\\d+)|(打开支付宝.*搜索\\s*(\\d{7,10}))", 
            "支付宝红包、好友、转账或小程序口令"
        ),

        // 钉钉
        new AppUrlRule(
            "com.alibaba.android.rimet",
            R.drawable.icon_dingtalk, // 请确保你有对应的图标资源
            "(https?://(?:[\\w-]+\\.)?(?:dingtalk\\.com|dg\\.alipay\\.com)[^\\s\"]*)|(\\$[a-zA-Z0-9]{8,15}\\$)|(dtk:\\/\\/dingtalkweb\\/business\\/.*)",
            "钉钉链接或服务口令"
        ),

        // 夸克
        new AppUrlRule(
            "com.quark.browser", 
            R.drawable.icon_quark, 
            "https?://pan\\.quark\\.cn/s/[\\w-]+", 
            "夸克网盘分享链接"
        ),

        // 飞书
        new AppUrlRule(
            "com.ss.android.lark", 
            R.drawable.icon_feishu, 
            "https?://(?:[\\w-]+\\.)?(?:feishu\\.cn|larksuite\\.com)/(?:docx|docx|base|wiki|file)/[\\w-]+", 
            "飞书文档或多维表格链接"
        ),

    };

    /**
     * 通用网址匹配规则
     */
    public static final GenericUrlRule GENERIC_URL_RULE = new GenericUrlRule(
        AppMatchResult.PACKAGE_WEB,
        R.drawable.icon_web,
        "https?://[^\\s\"<>{}|\\^`\\[\\]]+",
        "通用网址匹配"
    );

    /**
     * 应用网址规则数据类
     */
    public static class AppUrlRule {
        private final String packageName;   // 应用包名
        private final int iconResId;        // 图标资源ID
        private final String pattern;       // 正则表达式
        private final String description;   // 描述信息

        public AppUrlRule(String packageName, int iconResId, String pattern, String description) {
            this.packageName = packageName;
            this.iconResId = iconResId;
            this.pattern = pattern;
            this.description = description;
        }

        public String getPackageName() {
            return packageName;
        }

        public int getIconResId() {
            return iconResId;
        }

        public String getPattern() {
            return pattern;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 通用网址规则数据类
     */
    public static class GenericUrlRule {
        private final String type;          // 类型标识
        private final int iconResId;        // 图标资源ID
        private final String pattern;       // 正则表达式
        private final String description;   // 描述信息

        public GenericUrlRule(String type, int iconResId, String pattern, String description) {
            this.type = type;
            this.iconResId = iconResId;
            this.pattern = pattern;
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public int getIconResId() {
            return iconResId;
        }

        public String getPattern() {
            return pattern;
        }

        public String getDescription() {
            return description;
        }
    }
}
