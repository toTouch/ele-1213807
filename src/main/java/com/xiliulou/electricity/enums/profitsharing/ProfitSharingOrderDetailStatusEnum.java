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
public enum ProfitSharingOrderDetailStatusEnum {
    
    ACCEPT(0, "已受理"),
    IN_PROCESS(1, "处理中"),
    COMPLETE(2, "分账完成"),
    FAIL(3, "分账失败");;
    
    
    private Integer code;
    
    private String desc;
    
    ProfitSharingOrderDetailStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
