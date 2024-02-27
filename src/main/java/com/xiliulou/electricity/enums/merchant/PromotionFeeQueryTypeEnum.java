package com.xiliulou.electricity.enums.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @ClassName : PromotionFeeQueryEnum
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
@Getter
@AllArgsConstructor
public enum PromotionFeeQueryTypeEnum {
    
    MERCHANT(1, "商户"),
    MERCHANT_EMPLOYEE(2, "场地员工"),
    CHANNEL_EMPLOYEE(3, "渠道员"),
    ;
    
    private final Integer code;
    
    private final String desc;
    
    public static boolean contains(Integer code) {
        for (PromotionFeeQueryTypeEnum m : PromotionFeeQueryTypeEnum.values()) {
            if (Objects.equals(m.getCode(),code)) {
                return true;
            }
        }
        return false;
    }
}
