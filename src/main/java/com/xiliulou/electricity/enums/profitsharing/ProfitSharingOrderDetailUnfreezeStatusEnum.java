/**
 * Create date: 2024/8/27
 */

package com.xiliulou.electricity.enums.profitsharing;

import lombok.Getter;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/27 17:12
 */
@Getter
public enum ProfitSharingOrderDetailUnfreezeStatusEnum {
    
    PENDING(0, "待处理"),
    DISPENSE_WITH(1, "无需解冻"),
    IN_PROCESS(2, "解冻中"),
    FAIL(3, "解冻失败"),
    SUCCESS(4, "解冻成功"),
    LAPSED(5, "失效"),
    ;
    
    
    private Integer code;
    
    private String desc;
    
    ProfitSharingOrderDetailUnfreezeStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
