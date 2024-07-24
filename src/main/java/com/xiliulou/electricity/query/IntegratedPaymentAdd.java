package com.xiliulou.electricity.query;

import com.xiliulou.pay.base.enums.ChannelEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: Miss.Li
 * @Date: 2022/12/12 09:07
 * @Description:
 */
@Data
public class IntegratedPaymentAdd {
    
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
    private String paymentChannel = ChannelEnum.WECHAT.getCode();
    
    /**
     * 优惠券id
     */
    private Integer userCouponId;
    
    //优惠券
    private List<Integer> userCouponIds;
}
