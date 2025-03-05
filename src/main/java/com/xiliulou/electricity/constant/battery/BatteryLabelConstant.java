package com.xiliulou.electricity.constant.battery;

import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;

import java.util.Set;

/**
 * @author: SJP
 * @Desc:
 * @create: 2025-02-20 19:19
 **/
public interface BatteryLabelConstant {
    
    Set<Integer> RENT_LABEL_SET = Set.of(BatteryLabelEnum.RENT_NORMAL.getCode(), BatteryLabelEnum.RENT_OVERDUE.getCode(), BatteryLabelEnum.RENT_LONG_TERM_UNUSED.getCode());
    
    Set<Integer> RECEIVED_LABEL_SET = Set.of(BatteryLabelEnum.RECEIVED_ADMINISTRATORS.getCode(), BatteryLabelEnum.RECEIVED_MERCHANT.getCode());
    
}
