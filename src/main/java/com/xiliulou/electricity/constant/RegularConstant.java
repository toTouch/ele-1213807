package com.xiliulou.electricity.constant;

import java.util.regex.Pattern;

/**
 * @author maxiaodong
 * @date 2024/2/28 9:53
 * @desc 正则表达是常量
 */
public class RegularConstant {
    
    /**
     * 场地费校验规则  整数最多八位，小数最多两位
     */
    public static final Pattern PLACE_PATTERN = Pattern.compile("^[1-9]\\d{0,7}(\\.\\d{1,2})?$|^0(\\.\\d{1,2})?$");
}
