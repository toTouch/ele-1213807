package com.xiliulou.electricity.enums.enterprise;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/7 15:02
 */

@Getter
@AllArgsConstructor
public enum EnterprisePaymentStatusEnum implements BasicEnum<Integer, String> {
    
    PAYMENT_TYPE_EXPIRED(1, "代付到期"),
    
    PAYMENT_TYPE_SUCCESS(2, "已代付"),
    
    PAYMENT_TYPE_NO_PAY(3, "未代付"),
    
    ;
    
    private final Integer code;
    
    private final String desc;
    
    
}
