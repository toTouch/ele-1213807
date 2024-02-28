package com.xiliulou.electricity.constant;

import java.util.regex.Pattern;

/**
 * @author maxiaodong
 * @date 2024/2/28 9:53
 * @desc 正则表达是常量
 */
public class RegularConstant {
    public static final Pattern PLACE_PATTERN = Pattern.compile("^(([1-9]{1}\\d*)|(0{1}))(\\.\\d{1,2})?$");
    public static final Pattern PLACE_PATTERN_NEW = Pattern.compile("^[1-9]\\d{0,7}(\\.\\d{1,3})?$|^0(\\.\\d{1,2})?$");
    
}
