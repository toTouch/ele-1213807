package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/2/17 22:50
 * @desc
 */
@Data
public class MerchantPlaceFeeRecordVO {
    
    /**
     * id
     */
    private Long id;
    
    /**
     * 柜机id
     */
    private Integer cabinetId;
    
    /**
     * 修改前场地费用（元）
     */
    private BigDecimal oldPlaceFee;
    
    /**
     * 修改后场地费用（元）
     */
    private BigDecimal newPlaceFee;
    
    /**
     * 修改人id
     */
    private Long modifyUserId;
    
    /**
     * 修改人名称
     */
    private String modifyUserName;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    
}
