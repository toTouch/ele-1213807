/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/27
 */

package com.xiliulou.electricity.enums.profitsharing;

import lombok.Getter;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/27 20:05
 */
@Getter
public enum ProfitSharingTradeMixedStateEnum {
    
    INIT(0, "初始化"),
    COMPLETE(1,"已完成")
    ;
    
    
    private Integer code;
    
    private String desc;
    
    ProfitSharingTradeMixedStateEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
