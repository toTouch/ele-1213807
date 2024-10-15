package com.xiliulou.electricity.vo;

import com.xiliulou.core.base.enums.ChannelEnum;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-11 18:26
 **/
@Data
public class ElectricityMemberCardOrderVO extends ElectricityMemberCardOrder {
    private String phone;
    private String franchiseeName;
    private String userName;
    private String electricityCabinetName;
    private Integer cardPayCount;

    private OldUserActivityVO  oldUserActivityVO;

    /**
     * 交易方式 0--线上 1--线下
     */
    private Integer payType;

    private Long sendCouponId;

    private String sendCouponName;

    /**
     * 租期单位 0：分钟，1：天
     */
    private Integer rentUnit;
    /**
     * 租赁类型
     */
    private Integer rentType;
    /**
     * 0:不限制,1:限制
     */
    private Integer limitCount;
    /**
     * 租期
     */
    private Integer validDays;
    /**
     * 使用次数
     */
    private Long useCount;

    private List<String> batteryTypes;

    private String simpleBatteryType;

    private Integer isRefund;
    
    /**
     * 退租金天数限制
     */
    private Integer refundLimit;

    /**
     * 退租状态
     */
    private Integer rentRefundStatus;

    /**
     * 退租拒绝原因
     */
    private String rejectReason;
    
    /**
     * 订单类型： 0-普通换电订单，1-企业渠道换电订单
     * @see PackageOrderTypeEnum
     */
    private Integer orderType;
    
    /**
     * 购买套餐赠送的优惠券id
     */
    private String couponIds;
    
    private List<CouponSearchVo> coupons;
    
    /**
     * 支付方式
     * @see ChannelEnum
     */
    private String paymentChannel;
    private String tenantName;
    
    private Integer tenantId;
    
    private Integer installmentRecordStatus;
}
