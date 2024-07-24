package com.xiliulou.electricity.query;

import com.xiliulou.pay.base.enums.ChannelEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-22-17:21
 */
@Data
public class FreeBatteryDepositHybridOrderQuery {
    //==============车辆================
    
    
    private Long storeId;
    
    
    private Long carModelId;
    
    
    private Integer rentTime;
    
    
    private String rentType;
    
    //==============电池================
    
    @NotNull(message = "加盟商不能为空!")
    private Long franchiseeId;
    
    @NotNull(message = "电池套餐不能为空!")
    private Integer memberCardId;
    
    private Integer model;
    
    private Integer insuranceId;
    
    private Integer userCouponId;
    
    private String productKey;
    
    private String deviceName;
    
    // 优惠券
    private List<Integer> userCouponIds;
    
    /**
     * 支付渠道 WECHAT-微信支付,ALIPAY-支付宝
     */
    private String paymentChannel = ChannelEnum.WECHAT.getCode();
}
