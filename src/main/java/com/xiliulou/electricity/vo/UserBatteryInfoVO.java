package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 *
 * @author makejava
 * @since 2022-07-07 14:17:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserBatteryInfoVO {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 服务状态 (0--初始化,1--已实名认证)
     */
    private Integer serviceStatus;

    //审核状态(0--等待审核中,1--审核被拒绝,2--审核通过,3--活体检测失败,4--活体检测成功)
    private Integer authStatus;

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    //租户
    private Integer tenantId;

    private Long userInfoId;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    /**
     * 套餐id
     */
    private Integer cardId;
    /**
     * 套餐名称
     */
    private String cardName;
    /**
     * 类型(0:月卡,1:季卡,2:年卡)
     */
    private Integer cardType;
    /**
     * 月卡过期时间
     */
    private Long memberCardExpireTime;
    /**
     * 月卡剩余次数
     */
    private Long remainingNumber;

    /**
     * 月卡剩余天数
     */
    private Long cardDays;

    /**
     * 当前电池编号
     */
    private String nowElectricityBatterySn;
    /**
     * 租电池押金
     */
    private BigDecimal batteryDeposit;
    /**
     * 电池租赁状态 0--未租电池，1--已租电池
     */
    private Integer batteryRentStatus;

    /**
     * 电池押金状态 0--未缴纳押金，1--已缴纳押金,2--押金退款中
     */
    private Integer batteryDepositStatus;

    /**
     * 车辆租赁状态
     */
    private Integer carRentStatus;

    /**
     * 车辆押金状态
     */
    private Integer carDepositStatus;

    /**
     * 电量
     */
    private Double power;

    private Long uid;

    private String carSn;

    /**
     * 缴纳押金时间
     */
    private Long payDepositTime;

    /**
     * 套餐购买时间
     */
    private Long memberCardCreateTime;

    /**
     * 电池型号
     */
    private String model;

    /**
     * 用户认证时间
     */
    private Long userCertificationTime;

    /**
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;


    /**
     * 月卡停启用状态 0--启用 1--停用
     */
    private Integer memberCardDisableStatus;

    /**
     * 月卡暂停启用更新时间
     */
    private Long disableMemberCardTime;

    private Integer modelType;

    private String orderId;

    private Long storeId;
    /**
     * 电池业务状态
     */
    private Integer businessStatus;

    /**
     * 是否购买不保险
     */
    private Integer isUse;

    /**
     * 保险过期时间
     */
    private Long insuranceExpireTime;

    /**
     * 保险购买时间
     */
    private Long payInsuranceTime;
}
