package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/25 16:10
 */
@Data
public class PlaceOrderQuery {
    
    private String productKey;
    
    private String deviceName;
    
    
    /**
     * 保险id
     */
    private Integer insuranceId;
    
    /**
     * 加盟商id
     */
    @NotNull(message = "加盟商Id不能为空!")
    private Long franchiseeId;
    
    /**
     * 电池类型
     */
    private Integer model;
    
    /**
     * 月卡id
     */
    @NotNull(message = "套餐Id不能为空!")
    private Long memberCardId;
    
    /**
     * 支付渠道 WECHAT-微信支付,ALIPAY-支付宝
     */
    private String paymentChannel;
    
    /**
     * 优惠券id
     */
    private Integer userCouponId;

    /**
     * 多张优惠券id
     */
    private List<Integer> userCouponIds;
    
    /**
     * 购买下单业务类型
     */
    private Integer placeOrderType;
    
    /**
     * 支付类型 0--线上 1--线下
     */
    private Integer payType;
    
    /**
     * 后台单独缴纳押金的金额
     */
    private BigDecimal depositAmount;
    
    /**
     * 后台绑定时，被操作用户的uid
     */
    private Long uid;
}
