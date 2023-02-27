package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zgw
 * @date 2023/2/17 13:58
 * @mood
 */
@Data
public class CarMemberCardRenewalAddAndUpdate {
    
    /**
     * 用户Id
     */
    @NotNull(message = "用户id不能为空!")
    private Long uid;
    
    /**
     * 租赁方式
     */
    private String rentType;
    
    /**
     * 周期
     */
    private Integer validDays;
}
