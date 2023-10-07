package com.xiliulou.electricity.vo.enterprise;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/27 16:12
 */

@Data
public class EnterpriseUserCostDetailsVO {
    
    /**
     * 消费类型：
     */
    private Integer costType;
    
    private String orderNo;
    
    private Long packageId;
    
    private String packageName;
    
    private BigDecimal payAmount;
    
    private BigDecimal depositAmount;
    
    private Long operationTime;
    
}
