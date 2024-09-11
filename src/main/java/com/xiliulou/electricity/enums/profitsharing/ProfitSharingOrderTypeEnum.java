/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/27
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
public enum ProfitSharingOrderTypeEnum {
    
    PROFIT_SHARING(0, "分出"),
    UNFREEZE(1, "解冻"),
    
    ;
    
    
    private Integer code;
    
    private String desc;
    
    ProfitSharingOrderTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
