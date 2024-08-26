

package com.xiliulou.electricity.enums.profitsharing;

import lombok.Getter;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/23 08:58
 */
@Getter
public enum ProfitSharingBusinessTypeEnum {
    
    BATTERY_PACKAGE(0, "换电-套餐购买"),
    INSURANCE(1, "换电-保险购买"),
    DEPOSIT(2, "换电-滞纳金缴纳"),
    BATTERY_SERVICE_FEE_FREEZE(3, "换电-滞纳金缴纳"),
    BATTERY_SERVICE_FEE_OVERDUE(4, "换电-滞纳金缴纳"),
    UNFREEZE(98, "解冻"),
    SYSTEM(99, "系统级别"),
    ;
    
    private Integer code;
    
    private String desc;
    
    
    ProfitSharingBusinessTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
}
