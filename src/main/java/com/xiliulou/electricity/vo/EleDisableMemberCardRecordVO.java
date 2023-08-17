package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-08-17-19:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleDisableMemberCardRecordVO {

    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 月卡名称
     */
    private String memberCardName;

    /**
     * 停卡单号
     */
    private String disableMemberCardNo;

    /**
     * 停卡状态
     */
    private Integer status;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 错误原因
     */
    private String errMsg;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * 月卡剩余天数
     */
    private Long cardDays;

    /**
     * 用户选择的停卡天数
     */
    private Integer chooseDays;

    /**
     * 用户真实的停卡天数
     */
    private Integer realDays;

    /**
     * 停卡付费状态(0--初始化 1--未支付服务费，2--已支付服务费)
     */
    private Integer payStatus;

    /**
     * 电池服务费收费标准 按天计费
     */
    private BigDecimal chargeRate;

    /**
     * 停卡是否限制时间 (0--不限制，1--限制)
     */
    private Integer disableCardTimeType;

    /**
     * 停卡截止时间
     */
    private Long disableDeadline;

    /**
     * 停卡时用户绑定的套餐
     */
    private Long batteryMemberCardId;

    private Long disableMemberCardTime;

    /**
     * 停卡原因
     */
    private String applyReason;

    /**
     * 停卡审核时间
     */
    private Long disableTime;

    private Long enableTime;

    private Long franchiseeId;

    private Long storeId;
}
