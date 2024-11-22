/**
 * Create date: 2024/8/28
 */

package com.xiliulou.electricity.enums.profitsharing;

import lombok.Getter;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/28 17:33
 */
@Getter
public enum ProfitSharingStatisticsTypeEnum {
    
    MONTH(1, "按月统计"),
    ;
    
    private Integer code;
    
    private String desc;
    
    
    ProfitSharingStatisticsTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
