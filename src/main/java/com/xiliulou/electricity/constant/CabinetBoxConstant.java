package com.xiliulou.electricity.constant;

/**
 * @author HeYafeng
 * @description 柜机相关常量
 * @date 2023/12/6 17:35:24
 */
public class CabinetBoxConstant {
    
    /**
     * 系统锁仓（2.0.2及以后1是人为，0是系统）
     */
    public static final Integer LOCK_BY_SYSTEM = 0;
    
    /**
     * 人为锁仓
     */
    public static final Integer LOCK_BY_USER = 1;
    
    /**
     * 异常锁仓原因
     */
    public static final Integer LOCK_REASON_EXCEPTION = 1008;
    
    public static final Integer LOCK_REASON_OTHER = 50006;

    public static final String APPLICATION_MODE_NORMAL_V = "NORMAL_V";

    public static final String APPLICATION_MODE_NORMAL = "NORMAL";
}
