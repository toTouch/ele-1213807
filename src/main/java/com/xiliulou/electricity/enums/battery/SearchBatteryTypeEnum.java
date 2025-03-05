package com.xiliulou.electricity.enums.battery;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: SJP
 * @Desc:
 * @create: 2025-03-03 13:52
 **/
@Getter
@AllArgsConstructor
public enum SearchBatteryTypeEnum {
    // 柜机锁仓查询电池
    SEARCH_FOR_LOCK(1, "柜机锁仓查询电池"),
    ;
    
    private final Integer code;
    
    private final String desc;
}
