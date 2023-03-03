package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zgw
 * @date 2023/2/13 10:35
 * @mood
 */
@Data
public class UserTurnoverVo {
    
    /**
     * 电池月卡营业额
     */
    private BigDecimal memberCardTurnover;
    
    /**
     * 租车月卡营业额
     */
    private BigDecimal carMemberCardTurnover;
    
    /**
     * 服务费营业额
     */
    private BigDecimal batteryServiceFee;
}
