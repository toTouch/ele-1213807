package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * 小程序首页UserInfo视图对象
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-22-15:59
 */
@Data
public class UserInfoDetailVO {
    
    public static final Integer EXIST_MEMBER_CARD = 0;
    
    public static final Integer NOT_EXIST_MEMBER_CARD = 1;
    
    //审核状态(0--等待审核中,1--审核被拒绝,2--审核通过，审核通过表示已实名认证)
    private Integer authStatus;
    
    /**
     * 电池租赁状态 0--未租电池，1--已租电池
     */
    private Integer batteryRentStatus;
    
    /**
     * 电池押金状态 0--未缴纳押金，1--已缴纳押金,2--押金退款中,3--押金退款失败
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
     * 服务状态 (0--初始化,1--已实名认证)
     */
    private Integer serviceStatus;
    
    /**
     * 电池服务费
     */
    private EleBatteryServiceFeeVO batteryServiceFee;
    
    /**
     * 获取用户状态（离线换电）
     */
    private UserFrontDetectionVO userFrontDetection;
    
    /**
     * 是否购买套餐
     */
    private Integer isExistMemberCard;
    
    /**
     * 是否购买保险
     */
    private InsuranceUserInfoVo insuranceUserInfoVo;
    
    /**
     * 电池编号
     */
    private String batteryName;
    
    /**
     * 电池电量
     */
    private Double batteryPower;
}
