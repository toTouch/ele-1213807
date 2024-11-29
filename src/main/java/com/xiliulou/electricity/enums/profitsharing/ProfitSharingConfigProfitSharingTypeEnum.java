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
public enum ProfitSharingConfigProfitSharingTypeEnum {
    
    ORDER_SCALE(1, "按照订单比例"),
    ;
    
    private Integer code;
    
    private String desc;
    
    
    ProfitSharingConfigProfitSharingTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
