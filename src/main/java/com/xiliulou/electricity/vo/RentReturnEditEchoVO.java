package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: RentReturnEditEchoVO
 * @description:
 * @author: renhang
 * @create: 2024-05-08 09:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RentReturnEditEchoVO {
    
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
     * 最小保留电池数量
     */
    private Integer minRetainBatteryCount;
    
    /**
     * 最大保留电池数量
     */
    private Integer maxRetainBatteryCount;
}
