/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/26
 */

package com.xiliulou.electricity.enums.profitsharing;

import lombok.Getter;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/26 17:04
 */
@Getter
public enum ProfitSharingTradeOderSupportRefundEnum {
    YES(0, "是"),
    NO(1, "否"),
    ;
    
    private Integer code;
    
    private String desc;
    
    ProfitSharingTradeOderSupportRefundEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
