/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/12
 */

package com.xiliulou.electricity.enums;

import lombok.Getter;

/**
 * description: 支付参数配置类型枚举
 *
 * @author caobotao.cbt
 * @date 2024/6/12 11:07
 */
@Getter
public enum AliPayConfigTypeEnum {
    
    DEFAULT_CONFIG(0, "默认配置"),
    FRANCHISEE_CONFIG(1, "加盟商配置");
    
    
    private Integer type;
    
    private String desc;
    
    AliPayConfigTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
