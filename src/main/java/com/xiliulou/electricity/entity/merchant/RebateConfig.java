package com.xiliulou.electricity.entity.merchant;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 返利配置表(RebateConfig)实体类
 *
 * @author Eclair
 * @since 2024-02-04 16:32:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_rebate_config")
public class RebateConfig {
    
    private Long id;
    
    /**
     * 商户等级
     */
    private String level;
    
    /**
     * 套餐Id
     */
    private Long mid;
    
    /**
     * 渠道员拉新返现
     */
    private BigDecimal channelerInvitation;
    
    /**
     * 渠道员续费返现
     */
    private BigDecimal channelerRenewal;
    
    /**
     * 商户拉新返现
     */
    private BigDecimal merchantInvitation;
    
    /**
     * 商户续费返现
     */
    private BigDecimal merchantRenewal;
    
    /**
     * 状态 0:关闭,1:开启
     */
    private Integer status;
    
    private Integer delFlag;
    
    private Long franchiseeId;
    
    private Integer tenantId;
    
    private Long createTime;
    
    private Long updateTime;
    
}
