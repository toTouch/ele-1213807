package com.xiliulou.electricity.enums.enterprise;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/27 16:16
 */

@Getter
@AllArgsConstructor
public enum UserCostTypeEnum implements BasicEnum<Integer, String> {
    
    COST_TYPE_PURCHASE_PACKAGE(1, "购买套餐"),
    
    COST_TYPE_RENT_BATTERY(2, "租电池"),
    
    COST_TYPE_RETURN_BATTERY(3, "还电池"),
    
    COST_TYPE_FREEZE_PACKAGE(4, "冻结套餐"),
    
    COST_TYPE_REFUND_DEPOSIT(5, "退押金"),
    
    ;
    
    private final Integer code;
    
    private final String desc;
}
