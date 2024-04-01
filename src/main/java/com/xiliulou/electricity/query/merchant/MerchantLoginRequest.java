package com.xiliulou.electricity.query.merchant;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2024/2/18 10:15
 */
@Data
public class MerchantLoginRequest {
    /**
     * 微信登陆code
     */
    @NotNull(message = "code不能为空")
    private String code;
    
    /**
     * 微信登陆数据
     */
    @NotNull(message = "data不能为空")
    private String data;
    
    /**
     * 微信解密向量
     */
    @NotNull(message = "iv不能为空")
    private String iv;
}

