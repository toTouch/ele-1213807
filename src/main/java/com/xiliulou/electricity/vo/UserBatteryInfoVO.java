package com.xiliulou.electricity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    private Integer modelType;
}
