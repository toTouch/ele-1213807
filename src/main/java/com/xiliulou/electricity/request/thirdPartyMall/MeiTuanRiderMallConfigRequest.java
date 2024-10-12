package com.xiliulou.electricity.request.thirdPartyMall;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author HeYafeng
 * @description 美团骑手商城配置信息
 * @date 2024/8/28 10:38:52
 */
@Data
public class MeiTuanRiderMallConfigRequest {
    
    @NotBlank(message = "appId不能为空")
    private String appId;
    
    @NotBlank(message = "appKey不能为空")
    private String appKey;
    
    @NotBlank(message = "secret不能为空")
    private String secret;
}
