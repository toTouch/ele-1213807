package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2023/2/15 11:46
 */
@Data
public class FreeDepositQuery {
    
    @NotNull(message = "加盟商id不能为空")
    private Long franchiseeId;
    
    @NotEmpty(message = "手机号不能为空")
    private String phoneNumber;
    
    @NotEmpty(message = "身份证不能为空")
    private String idCard;
    
    @NotEmpty(message = "真实姓名不能为空")
    private String realName;
    
    private Integer model;
    
    
    
}
