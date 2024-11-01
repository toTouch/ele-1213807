package com.xiliulou.electricity.request.thirdPartyMall;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 限制下单请求
 * @date 2024/8/28 13:10:13
 */
@Data
public class LimitTradeRequest {
    
    private String appId;
    
    private String appKey;
    
    private Long timestamp;
    
    private String sign;
    
    private String account;
    
    private String providerSkuId;
}
