package com.xiliulou.electricity.request.thirdPartyMall;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/7 17:22:54
 */
@Data
public class CreateMemberCardOrderRequest {
    
    @NotBlank(message = "订单号不能为空")
    private String orderId;
    
}
