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
public enum ProfitSharingTradeOderProcessStateEnum {
    INIT(0, "初始化"),
    AWAIT(1, "待发起分账"),
    SUCCESS(2, "分账发起成功"),
    FAIL(3, "分账发起失败"),
    LAPSED(4, "已失效"),
    ;
    
    private Integer code;
    
    private String desc;
    
    ProfitSharingTradeOderProcessStateEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
