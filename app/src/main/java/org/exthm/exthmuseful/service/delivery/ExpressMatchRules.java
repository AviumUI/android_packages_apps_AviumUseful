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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.exthm.exthmuseful.service.delivery;

/**
 * 快递匹配规则配置文件
 */
public class ExpressMatchRules {

    /**
     * 快递匹配规则数组
     * - 快递公司名称
     * - 正则表达式模式
     * - 描述信息
     */
    public static final ExpressRule[] RULES = {
        // 顺丰速运: SF开头 + 13位数字
        new ExpressRule("顺丰速运", "^SF\\d{13}$", "SF开头后跟13位数字"),
        
        // 中通快递: 7开头 + 13位数字
        new ExpressRule("中通快递", "^7\\d{13}$", "7开头后跟13位数字"),
        
        // 圆通速递: YT开头 + 13位数字
        new ExpressRule("圆通速递", "^YT\\d{13}$", "YT开头后跟13位数字"),
        
        // 极兔速递: JT开头 + 13位数字
        new ExpressRule("极兔速递", "^JT\\d{13}$", "JT开头后跟13位数字"),
        
        // 韵达快递: YD开头 + 13位数字
        new ExpressRule("韵达快递", "^YD\\d{13}$", "YD开头后跟13位数字"),
        
        // 申通快递: ST开头 + 13位数字 或 77开头
        new ExpressRule("申通快递", "^(ST\\d{13}|77\\d{11})$", "ST开头后跟13位数字 或 77开头后跟11位数字"),
        
        // 德邦快递: DP开头 + 13位数字
        new ExpressRule("德邦快递", "^DP\\d{13}$", "DP开头后跟13位数字"),
        
        // 京东物流: JD开头 + 13位数字 或 JDV开头
        new ExpressRule("京东物流", "^(JD[V\\d]\\d{12,13})$", "JD或JDV开头后跟12-13位数字"),
        
        // 邮政EMS: 10开头 + 11位数字 或 EA/EB/EE/EE开头
        new ExpressRule("邮政EMS", "^(10\\d{11}|E[A-Z]\\d{9}CN)$", "10开头后跟11位数字 或 EA/EB等开头后跟9位数字+CN"),
        
        // 菜鸟裹裹: 66开头 + 11位数字
        new ExpressRule("菜鸟裹裹", "^66\\d{11}$", "66开头后跟11位数字"),
    };

    /**
     * 快递规则数据类
     */
    public static class ExpressRule {
        private final String companyName;  // 快递公司名称
        private final String pattern;      // 正则表达式
        private final String description;  // 描述信息

        public ExpressRule(String companyName, String pattern, String description) {
            this.companyName = companyName;
            this.pattern = pattern;
            this.description = description;
        }

        public String getCompanyName() {
            return companyName;
        }

        public String getPattern() {
            return pattern;
        }

        public String getDescription() {
            return description;
        }
    }
}
