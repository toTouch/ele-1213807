package com.xiliulou.electricity.request.meituan;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author HeYafeng
 * @description 限制下单请求
 * @date 2024/8/28 13:10:13
 */
@Data
public class LimitTradeRequest {
    
    @NotBlank(message = "appId不能为空")
    private String appId;
    
    @NotBlank(message = "appKey不能为空")
    private String appKey;
    
    @NotNull(message = "timestamp不能为空")
    private Long timestamp;
    
    @NotBlank(message = "sign不能为空")
    private String sign;
    
    @NotBlank(message = "account不能为空")
    private String account;
    
    @NotNull(message = "providerSkuId不能为空")
    private String providerSkuId;
}
