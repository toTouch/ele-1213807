/**
 * Create date: 2024/8/27
 */

package com.xiliulou.electricity.enums.profitsharing;

import lombok.Getter;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/27 20:05
 */
@Getter
public enum ProfitSharingTradeMixedOrderStateEnum {
    
    INIT(0, "初始化"),
    COMPLETE(1,"已完成")
    ;
    
    
    private Integer code;
    
    private String desc;
    
    ProfitSharingTradeMixedOrderStateEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
