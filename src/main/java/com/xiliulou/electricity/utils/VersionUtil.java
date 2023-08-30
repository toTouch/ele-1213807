package com.xiliulou.electricity.utils;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-24-9:53
 */
public class VersionUtil {

    private VersionUtil() {
    }

    /**
     * 版本号大小比较
     * 第一个参数大返回正数，第二个参数大返回负数，一样返回0
     *
     * @param sourceVersion
     * @param targetVersion
     * @return
     */
    public static int compareVersion(String sourceVersion, String targetVersion) {
        if (sourceVersion.equals(targetVersion)) {
            return 0;
        }

        String[] version1Array = sourceVersion.split("\\.");
        String[] version2Array = targetVersion.split("\\.");

        int index = 0;

        //获取最小长度值
        int minLen = Math.min(version1Array.length, version2Array.length);

        int diff = 0;

        //循环判断每位的大小
        while (index < minLen && (diff = Integer.parseInt(version1Array[index]) - Integer.parseInt(version2Array[index])) == 0) {
            index++;
        }

        if (diff == 0) {
            //如果位数不一致，比较多余位数
            for (int i = index; i < version1Array.length; i++) {
                if (Integer.parseInt(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Integer.parseInt(version2Array[i]) > 0) {
                    return -1;
                }
            }

            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }
}
