package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysOptLogTypeEnum implements BasicEnum<Integer, String> {
    MANUAL_LOCKDOWN(1, "人工锁仓"),
    MANUAL_OPENING_OF_POSITIONS(2, "人工开仓"),
    RESTART_THE_BATTERY_SWAPPING_CABINET(3, "重启换电柜"),
    PROHIBIT_ENABLE_BATTERY_SWAPPING_CABINET(4, "禁/启用换电柜"),
    WRITE_OFF_OF_COUPONS(5, "核销优惠券"),
    BATCH_DISTRIBUTION_OF_COUPONS(6, "批量发放优惠券"),
    WITHDRAWAL_REVIEW(7, "提现审核"),
    BATCH_WITHDRAWAL_REVIEW(8, "批量提现审核"),
    DELETE_COUPON(9, "删除优惠券"),
    CREATE_COUPON(10, "创建优惠券"),
    UP_DOWN_PACKAGE(11, "上/下架套餐"),
    EDIT_PACKAGE(12, "编辑套餐"),
    EDIT_USER_PACKAGE(13, "编辑用户套餐"),
    USER_PACKAGE_RENEWAL(14, "用户套餐续费"),
    MODIFY_USER_INVITER(15, "修改用户邀请人"),
    EDIT_CUSTOMER_SERVICE_PHONE_NUMBER(16, "编辑客服电话"),
    EDIT_WITHDRAWAL_PASSWORD(17, "编辑提现密码"),
    EDIT_REAL_NAME_AUTHENTICATION_INFORMATION(18, "编辑实名认证信息"),
    EDIT_PLATFORM_INFORMATION(19, "编辑平台信息"),
    EDIT_NOTIFICATION_MESSAGE(20, "编辑通知消息"),
    BATTERY_OUTBOUND(21, "电池出库"),
    UNBIND_BATTERIES_IN_THE_BACKGROUND(22, "后台解绑电池"),
    BINDING_BATTERIES_IN_THE_BACKGROUND(23, "后台绑定电池"),
    CHANGE_PHONE_NUMBER(24, "修改手机号"),
    UNBIND_WECHAT(25, "解绑微信"),
    DELETE_USER(26, "删除用户"),
    PACKAGE_FREEZE_REVIEW(27, "套餐冻结审核"),
    FREEZE_PACKAGE(28, "套餐冻结"),
    EDIT_BATTERY_BACK(29, "'后台编辑电池"),
    EDIT_APPLET_ONLINE_SERVICE_BACK(30, "'编辑微信小程序在线客服"),
    ;
    
    private final Integer code;
    
    private final String desc;
}
