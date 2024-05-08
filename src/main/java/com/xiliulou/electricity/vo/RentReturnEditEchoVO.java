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
    
    /**
     * 最小保留电池数量
     */
    private Integer minRetainBatteryCount;
    
    /**
     * 最大保留电池数量
     */
    private Integer maxRetainBatteryCount;
}
