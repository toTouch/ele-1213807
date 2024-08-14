package com.xiliulou.electricity.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-08-14-16:50
 */
public class DeviceTextUtil {
    private static final String SEPARATOR = ":";
    
    /**
     * 柜机三元组切割
     */
    public static Pair<String, String> substringSn(final String sn) {
        if (StringUtils.isBlank(sn)) {
            return Pair.of("", "");
        }
        
        String[] str = sn.split(SEPARATOR);
        if (str.length != 2) {
            return Pair.of("", "");
        }
        
        return Pair.of(str[0], str[1]);
    }
    
    /**
     * 组装sn
     */
    public static String assembleSn(final String productKey, final String deviceName) {
        return productKey + SEPARATOR + deviceName;
    }
    
    public static String assembleMsgType(final String platform, final String type) {
        return platform + SEPARATOR + type;
    }
    
    private DeviceTextUtil() {
    }
}
