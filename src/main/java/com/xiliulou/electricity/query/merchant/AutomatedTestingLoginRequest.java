package com.xiliulou.electricity.query.merchant;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AutomatedTestingLoginRequest {
    
    /**
     * 微信登陆code
     */
    @NotBlank(message = "userPhone不能为空")
    private String userPhone;
    
    /**
     * 微信登陆数据
     */
    @NotBlank(message = "password不能为空")
    private String password;
    
    
    
    /**
     * tenantId
     */
    @NotBlank(message = "tenantId不能为空")
    private Integer tenantId;
    
    
    
    
}

