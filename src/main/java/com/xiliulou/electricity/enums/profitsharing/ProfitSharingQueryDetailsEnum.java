/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/27
 */

package com.xiliulou.electricity.enums.profitsharing;

import lombok.Getter;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/27 08:44
 */
@Getter
public enum ProfitSharingQueryDetailsEnum {
    
    PROFIT_SHARING_CONFIG(1, "分账方配置"),
    PROFIT_SHARING_CONFIG_AND_RECEIVER_CONFIG(2, "分账方配置+分账接收方配置"),
    ;
    
    private Integer code;
    
    private String desc;
    
    
    ProfitSharingQueryDetailsEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
