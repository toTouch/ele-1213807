/**
 * Create date: 2024/8/23
 */

package com.xiliulou.electricity.enums.profitsharing;

import lombok.Getter;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/23 16:58
 */
@Getter
public enum ProfitSharingConfigReceiverStatusEnum {
    
    ENABLE(0, "启用"),
    DISABLED(1, "不可用");
    
    private Integer code;
    
    private String desc;
    
    
    ProfitSharingConfigReceiverStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
