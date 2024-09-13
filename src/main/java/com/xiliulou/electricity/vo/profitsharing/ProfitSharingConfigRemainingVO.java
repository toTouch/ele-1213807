package com.xiliulou.electricity.vo.profitsharing;

import com.xiliulou.electricity.enums.ElectricityPayParamsConfigEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 分账方配置表(TProfitSharingConfig)实体类
 *
 * @author makejava
 * @since 2024-08-22 17:14:08
 */
@Data
public class ProfitSharingConfigRemainingVO {
    
    
    private Long id;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 剩余比例上限
     */
    private BigDecimal scaleLimit;
    
    
}

