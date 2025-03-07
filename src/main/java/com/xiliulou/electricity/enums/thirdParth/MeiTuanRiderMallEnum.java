package com.xiliulou.electricity.enums.thirdParth;

import lombok.Getter;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/19 18:24:19
 */
@Getter
public enum MeiTuanRiderMallEnum {
    ENABLE_MEI_TUAN_RIDER_MALL(0, "开启美团骑手商城"),
    DISABLE_MEI_TUAN_RIDER_MALL(1, "关闭美团骑手商城"),
    CANCEL_ED_DELIVER_ERROR(31, "该订单已取消，不能发货"),
    ORDER_STATUS_WAIT_PAY(1, "待付款"),
    ORDER_STATUS_PAID(10, "已付款"),
    VP_RECHARGE_STATUS_SUCCESS(1, "充值成功"),
    VP_RECHARGE_STATUS_FAIL(2, "充值失败"),
    ORDER_HANDLE_REASON_STATUS_HANDLE(1, "已处理"),
    ORDER_HANDLE_REASON_STATUS_UNHANDLED(2, "未处理"),
    
    ORDER_HANDLE_REASON_STATUS_SYNCED(3, "已对账"),
    ORDER_STATUS_CANCELED(20, "订单取消"),
    ORDER_STATUS_DELIVERED(40, "已发货"),
    ORDER_STATUS_FINISHED(90, "订单完成"),
    ORDER_USE_STATUS_UNUSED(0, "未使用"),
    ORDER_USE_STATUS_USED(1, "已使用"),
    ORDER_USE_STATUS_INVALID(2, "已失效"),
    ORDER_USE_STATUS_REFUNDED(3, "已退租");
    
    private final Integer code;
    
    private final String desc;
    
    MeiTuanRiderMallEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
