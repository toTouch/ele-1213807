package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName: FreeDepositChannelEnum
 * @description:
 * @author: renhang
 * @create: 2024-08-21 19:13
 */
@Getter
@AllArgsConstructor
public enum FreeDepositChannelEnum {
    PXZ(1, "拍小租"),
    FY(2, "蜂云"),
    
    ;
    
    private final Integer channel;
    
    private final String desc;
}
