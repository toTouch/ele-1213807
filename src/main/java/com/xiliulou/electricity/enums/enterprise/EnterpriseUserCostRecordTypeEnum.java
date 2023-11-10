package com.xiliulou.electricity.enums.enterprise;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/20 8:55
 */

@Getter
@AllArgsConstructor
public enum EnterpriseUserCostRecordTypeEnum implements BasicEnum<Integer, String> {
    
    USER_COST_TYPE_BATTERY(1, "企业渠道电池订单"),
    
    USER_COST_TYPE_CAR_RENTAL(2, "企业渠道租车订单"),
    
    ;
    
    private final Integer code;
    
    private final String desc;
    
    
}
