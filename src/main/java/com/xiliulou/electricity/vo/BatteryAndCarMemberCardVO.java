package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author renhang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryAndCarMemberCardVO {
    
    private Long id;
    
    /**
     * 套餐名称
     */
    private String name;
    
    /**
     * 套餐类型 1 - 换电， 2 - 租车
     */
    private Integer type;
}
