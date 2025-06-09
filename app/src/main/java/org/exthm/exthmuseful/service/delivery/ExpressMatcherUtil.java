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