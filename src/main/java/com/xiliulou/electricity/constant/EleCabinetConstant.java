package com.xiliulou.electricity.constant;

/**
 * 换电柜柜机相关常量
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-4-16-16:41
 */
public interface EleCabinetConstant {
    
    /**
     * 柜机供电类型：0--市电，1--反向供电
     */
    Integer POWER_TYPE_ORDINARY=0;
    Integer POWER_TYPE_BACKUP=1;
    
    /**
     * 柜机少电/多电类型：0--正常，1--少电，2--多电
     */
    int BATTERY_COUNT_TYPE_NORMAL = 0;
    int BATTERY_COUNT_TYPE_LESS = 1;
    int BATTERY_COUNT_TYPE_MORE = 2;
    
    /**
     * iot连接模式  0:阿里云 1：华为云 2:自建TCP
     */
    int ALI_IOT_PATTERN = 0;
    int TCP_PATTERN = 1;
    
}
