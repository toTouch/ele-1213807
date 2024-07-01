package com.xiliulou.electricity.queryModel;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: EleCabinetExtraQueryModel
 * @description:
 * @author: renhang
 * @create: 2024-06-11 14:27
 */
@Data
@Builder
public class EleCabinetExtraQueryModel {
    
    private Long id;
    
    /**
     * 租电类型（全部可租电、不允许租电、最少保留一块电池、自定义） RentReturnNormEnum
     */
    private Integer rentTabType;
    
    /**
     * 退电类型（全部可退电、不允许退电、最少保留一个空仓、自定义） RentReturnNormEnum
     */
    private Integer returnTabType;
    
    /**
     * 最小保留电池数量，只有自定义才需要
     */
    private Integer minRetainBatteryCount;
    
    /**
     * 最大保留电池数量，只有自定义才需要
     */
    private Integer maxRetainBatteryCount;
    
    private Long updateTime;
}
