package com.xiliulou.electricity.vo.enterprise;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/19 10:04
 */

@Data
public class EnterpriseUserCostRecordRemarkVO {
    
    /**
     * 订单支付金额
     */
    private BigDecimal payAmount;
    
    /**
     * 套餐购买时，缴纳押金金额
     */
    private BigDecimal depositAmount;
    
    /**
     * 套餐购买时，保险金额
     */
    private BigDecimal insuranceAmount;
    
}
