package com.xiliulou.electricity.enums.battery;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: SJP
 * @Desc: 电池标签状态值
 * @create: 2025-02-11 17:27
 **/
@Getter
@AllArgsConstructor
public enum BatteryLabelEnum {
    // 录入系统未出库，退库电池
    INVENTORY(1, "库存"),
    // 在柜机中
    IN_THE_CABIN(2, "在仓"),
    // 骑手租赁中
    RENT_NORMAL(3, "租借-正常"),
    // 用户套餐过期，电池租借未归还
    RENT_OVERDUE(4, "租借-逾期"),
    // 电池租借后${7}天未发生过租、换、退操作，时间可配置
    RENT_LONG_TERM_UNUSED(5, "租借-长时间未换电"),
    // 客服、运维开仓取电池（远程/本地开门，电池出仓），门店线下租赁场景
    RECEIVED_ADMINISTRATORS(6, "管理员领用"),
    // 商户处存放电池
    RECEIVED_MERCHANT(7, "商户领用"),
    // 电池损坏确认返厂维修
    IN_MAINTENANCE(8, "维修"),
    // 已确认丢失电池
    LOST(9, "丢失"),
    // 确定电池损坏无法使用
    SCRAP(10, "报废"),
    // 电池异常无法使用，运维无法及时到场，客服暂时将电池锁定在柜机仓内
    LOCKED_IN_THE_CABIN(11, "锁定在仓"),
    // 电池异常无法使用，运维无法及时到场，客服暂时将电池锁定在柜机仓内
    UNUSED(12, "闲置"),
    ;
    
    private final Integer code;
    
    private final String desc;
}
