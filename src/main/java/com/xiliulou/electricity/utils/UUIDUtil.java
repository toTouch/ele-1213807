package com.xiliulou.electricity.utils;

import java.util.UUID;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-10-11-19:14
 */
public class UUIDUtil {

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
