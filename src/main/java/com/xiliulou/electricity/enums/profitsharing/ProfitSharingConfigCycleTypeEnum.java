/**
 * Create date: 2024/8/23
 */

package com.xiliulou.electricity.enums.profitsharing;

import lombok.Getter;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/23 08:58
 */
@Getter
public enum ProfitSharingConfigCycleTypeEnum {
    
    D_1(1, "D+1"),
    ;
    
    private Integer code;
    
    private String desc;
    
    
    ProfitSharingConfigCycleTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
