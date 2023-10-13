package com.xiliulou.electricity.vo.enterprise;

import com.xiliulou.electricity.enums.enterprise.UserCostTypeEnum;
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
     * 消费类型： 1-购买套餐, 2-租电池, 3-还电池, 4-冻结套餐, 5-退押金
     * @see UserCostTypeEnum
     */
    private Integer costType;
    
    private String orderNo;
    
    private Long packageId;
    
    private String packageName;
    
    private BigDecimal payAmount;
    
    private BigDecimal depositAmount;
    
    private BigDecimal insuranceAmount;
    
    private Long operationTime;
    
}
