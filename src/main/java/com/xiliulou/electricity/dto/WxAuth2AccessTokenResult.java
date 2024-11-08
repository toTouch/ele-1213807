package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class WxAuth2AccessTokenResult {
    
    /**
     * 授权用户唯一标识
     */
    private String openid;
    
    /**
     * 授权用户唯一标识
     */
    private String access_token;
    
    /**
     * access_token接口调用凭证超时时间，单位（秒）
     */
    private String expires_in;
    
    /**
     * 用户刷新access_token
     */
    private String refresh_token;
    
    /**
     * 用户授权的作用域，使用逗号（,）分隔
     */
    private String scope;
    
    /**
     * 用户统一标识。针对一个微信开放平台账号下的应用，同一用户的 unionid 是唯一的
     */
    private String unionid;
    
    private String errcode;
    
    private String errmsg;
}
