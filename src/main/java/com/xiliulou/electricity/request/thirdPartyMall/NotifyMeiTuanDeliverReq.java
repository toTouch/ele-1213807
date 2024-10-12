package com.xiliulou.electricity.request.thirdPartyMall;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author HeYafeng
 * @date 2024/10/12 15:22:20
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NotifyMeiTuanDeliverReq {
    
    @NotNull(message = "tenantId不能为空")
    private Integer tenantId;
    
    @NotBlank(message = "美团订单号不能为空")
    private String orderId;
    
    private String coupon;
    
    @NotNull(message = "充值状态不能为空")
    private Integer vpRechargeStatus;
    
    @NotNull(message = "套餐开始时间不能为空")
    private Long vpComboStartTime;
    
    @NotNull(message = "套餐结束时间不能为空")
    private Long vpComboEndTime;
}
