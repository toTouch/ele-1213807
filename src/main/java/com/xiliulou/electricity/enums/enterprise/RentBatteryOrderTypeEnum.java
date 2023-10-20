package com.xiliulou.electricity.enums.enterprise;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/20 10:37
 */

@Getter
@AllArgsConstructor
public enum RentBatteryOrderTypeEnum implements BasicEnum<Integer, String> {
    
    RENT_ORDER_TYPE_NORMAL(0, "普通租退电订单"),
    
    RENT_ORDER_TYPE_ENTERPRISE(1, "企业渠道租退电订单"),
    
    ;
    
    private final Integer code;
    
    private final String desc;
    
}
