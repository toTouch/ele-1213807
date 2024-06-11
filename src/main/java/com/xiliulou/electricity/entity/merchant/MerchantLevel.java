package com.xiliulou.electricity.entity.merchant;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商户等级表(MerchantLevel)实体类
 *
 * @author Eclair
 * @since 2024-02-04 14:35:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_level")
public class MerchantLevel {
    
    private Long id;
    
    /**
     * 商户等级
     */
    private String level;
    
    /**
     * 商户等级名称
     */
    private String name;
    
    /**
     * 规则
     */
    private String rule;
    
    private Integer delFlag;
    
    private Long franchiseeId;
    
    private Integer tenantId;
    
    private Long createTime;
    
    private Long updateTime;
    
}
