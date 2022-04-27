package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 用户离线换电前置检测实体类
 *
 * @author makejava
 * @since 2022-04-27 14:17:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFrontDetectionVO {

    /**
     * 用户状态
     */
    private Integer serviceStatus;

    /**
     * 密钥
     */
    private String secret;

    /**
     * 步长
     */
    private Long step;

    /**
     * 未找到用户
     */
    public static final Integer NOT_FOUND_USER = 0;

    /**
     * 用户被禁用
     */
    public static final Integer USER_IS_DISABLE = 1;

    /**
     * 未实名认证
     */
    public static final Integer USER_NOT_AUTHENTICATION = 2;

    /**
     * 未缴纳押金
     */
    public static final Integer USER_NOT_DEPOSIT = 3;

    /**
     * 未开通月卡
     */
    public static final Integer USER_NOT_MEMBER_CARD = 4;

    /**
     * 新用户活动赠送卡
     */
    public static final Integer IS_NEW_USER_ACTIVITY_CARD = 5;

    /**
     * 套餐不存在
     */
    public static final Integer MEMBER_CARD_NOT_EXIST = 6;

    /**
     * 月卡可用次数为负
     */
    public static final Integer MEMBER_CARD_NEGATIVE_NUMBER = 7;

    /**
     * 月卡用完
     */
    public static final Integer MEMBER_CARD_USE_UP = 8;

    /**
     * 用户未绑定电池
     */
    public static final Integer USER_NOT_BIND_BATTERY = 9;

    /**
     * 月卡过期
     */
    public static final Integer MEMBER_CARD_OVER_DUE = 10;

    public static final Integer USER_CAN_OFFLINE_ELECTRICITY = 11;

}
