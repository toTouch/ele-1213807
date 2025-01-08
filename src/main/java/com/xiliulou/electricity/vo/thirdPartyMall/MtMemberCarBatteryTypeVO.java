package com.xiliulou.electricity.vo.thirdPartyMall;

import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 套餐电池型号
 * @date 2024/8/29 14:26:38
 */
@Data
public class MtMemberCarBatteryTypeVO {
    
    /**
     * 套餐ID
     */
    private Long packageId;
    
    /**
     * 是否免押 0--是 1--否
     */
    private Integer freeDeposit;
    
    /**
     * 套餐电池型号
     */
    private List<String> batteryTypes;
    
}
