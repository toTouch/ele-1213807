package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : eclair
 * @date : 2024/2/27 14:29
 */
@Data
public class ElectricityMerchantProConfigVO implements Serializable {
    
    private static final long serialVersionUID = 1748659377875993595L;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 租户编号
     */
    private String tenantCode;
    
    /**
     * 客服电话
     */
    private String servicePhone;
}

