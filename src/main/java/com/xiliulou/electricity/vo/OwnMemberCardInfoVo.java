package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-11 14:57
 **/
@Data
public class OwnMemberCardInfoVo {
    /**
     * 月卡过期时间
     */
    private Long memberCardExpireTime;
    /**
     * 剩余使用次数
     */
    private Long remainingNumber;


    private Long days;
    private Integer type;
    private String name;

    /**
     * 套餐id
     */
    private Integer cardId;

    private Integer memberCardDisableStatus;

    /**
     * 有效天数
     */
    private Long validDays;

    private String carName;

    //最大使用次数
    private Long maxUseCount;

    /**
     * 停卡时间
     */
    private Long disableMemberCardTime;

    /**
     * 停卡截止时间
     */
    private Long endTime;
}
