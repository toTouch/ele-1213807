package com.xiliulou.electricity.enums.enterprise;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CloudBeanUseRecordTypeEnum implements BasicEnum<Integer, String> {
    TYPE_PAY_MEMBERCARD(0, "套餐代付"),
    TYPE_RECYCLE(1, "云豆回收"),
    TYPE_USER_RECHARGE(2, "云豆充值"),
    TYPE_PRESENT(3, "赠送"),
    TYPE_ADMIN_RECHARGE(4, "后台充值"),
    TYPE_ADMIN_DEDUCT(5, "后台扣除"),
    ;
    
    private final Integer code;
    
    private final String desc;
}
