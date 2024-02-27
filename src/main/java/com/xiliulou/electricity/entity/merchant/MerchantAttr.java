package com.xiliulou.electricity.entity.merchant;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 商户升级配置(MerchantAttr)实体类
 *
 * @author zzlong
 * @since 2024-02-04 09:14:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_attr")
public class MerchantAttr {
    
    private Long id;
    
    /**
     * 升级条件 1：拉新人数，2：续费人数，3：全部
     */
    private Integer upgradeCondition;
    
    /**
     * 邀请时效
     */
    private Integer invitationValidTime;
    
    /**
     * 邀请时效单位
     */
    private Integer validTimeUnit;
    
    /**
     * 邀请保护期
     */
    private Integer invitationProtectionTime;
    
    /**
     * 邀请保护期单位
     */
    private Integer protectionTimeUnit;
    
    private Integer delFlag;
    
    private Long merchantId;
    
    private Integer tenantId;
    
    private Long createTime;
    
    private Long updateTime;
    
}
