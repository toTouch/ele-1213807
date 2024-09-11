

package com.xiliulou.electricity.enums.profitsharing;

import lombok.Getter;

import java.util.List;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/23 08:58
 */
@Getter
public enum ProfitSharingConfigOrderTypeEnum {
    
    BATTERY_PACKAGE(1, "换电-套餐购买"),
    INSURANCE(2, "换电-保险购买"),
    BATTERY_SERVICE_FEE(4, "换电-滞纳金缴纳"),
    ;
    
    private Integer code;
    
    private String desc;
    
    
    ProfitSharingConfigOrderTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
}
