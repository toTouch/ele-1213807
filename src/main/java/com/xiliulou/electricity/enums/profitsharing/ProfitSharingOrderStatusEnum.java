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
public enum ProfitSharingOrderStatusEnum {
    
    PROFIT_SHARING_ACCEPT(0, "已受理"),
    PROFIT_SHARING_IN_PROCESS(1, "处理中"),
    PROFIT_SHARING_COMPLETE(2, "分账完成"),
    PROFIT_SHARING_FAIL(3, "分账失败"),
    ;
    
    
    private Integer code;
    
    private String desc;
    
     ProfitSharingOrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
