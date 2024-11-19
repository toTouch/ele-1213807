/**
 *  Create date: 2024/7/16
 */

package com.xiliulou.electricity.enums;

import lombok.Getter;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/16 18:46
 */
@Getter
public enum RefundTypeEnum {
    
    BATTERY_DEPOSIT(1, "电池押金"),
    BATTERY_RENTAL(2, "电池租金"),
    CAR_DEPOSIT(3, "车辆押金"),
    CAR_RENTAL(4, "车辆租金"),
    ;
    
    private Integer code;
    
    private String desc;
    
    RefundTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
