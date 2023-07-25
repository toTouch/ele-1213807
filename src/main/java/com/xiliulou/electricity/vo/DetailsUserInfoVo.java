package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zgw
 * @date 2023/2/13 15:27
 * @mood
 */
@Data
public class DetailsUserInfoVo {
    
    /**
     * 用户名称
     */
    private String name;
    
    /**
     * 联系方式
     */
    private String phone;

    /**
     * 电池押金状态 0--未缴纳押金，1--已缴纳押金,2--押金退款中
     */
    private Integer batteryDepositStatus;

    /**
     * 车辆押金状态
     */
    private Integer carDepositStatus;

    /**
     * 车电一体押金状态
     */
    private Integer carBatteryDepositStatus;
    
    /**
     * 电池租借状态
     */
    private Integer batteryRentStatus;
    
    /**
     * 车辆租借状态
     */
    private Integer carRentStatus;
    
    /**
     * 车辆sn码
     */
    private String carSn;
    
    /**
     * 电池月卡营业额
     */
    private BigDecimal memberCardTurnover;
    
    /**
     * 租车月卡营业额
     */
    private BigDecimal carMemberCardTurnover;
    
    /**
     * 服务费营业额
     */
    private BigDecimal batteryServiceFee;
    
    /**
     * 认证时间
     */
    private Long userCertificationTime;

    private String franchiseeName;

    private String storeName;

    private Long franchiseeId;

    private Long storeId;
}
