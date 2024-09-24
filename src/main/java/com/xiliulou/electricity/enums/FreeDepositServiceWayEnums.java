package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @ClassName: FreeDepositServiceWayEnums
 * @description:
 * @author: renhang
 * @create: 2024-08-22 15:42
 */
@Getter
@AllArgsConstructor
public enum FreeDepositServiceWayEnums {
    
    
    /**
     * 拍小租
     */
    PXZ(1, "pxzFreeDepositOrderServiceImpl"),
    
    /**
     * 蜂云
     */
    FY(2, "fyFreeDepositOrderServiceImpl"),
    ;
    
    
    /**
     * code
     */
    private final Integer channel;
    
    
    /**
     * 实现
     */
    private final String implService;
    
    
    public static String getClassStrByChannel(Integer channel) {
        return Arrays.stream(FreeDepositServiceWayEnums.values()).filter(e -> Objects.equals(channel, e.getChannel())).findFirst().orElse(null).getImplService();
    }
    
    
}
