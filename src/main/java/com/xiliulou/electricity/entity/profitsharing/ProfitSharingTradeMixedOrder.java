package com.xiliulou.electricity.entity.profitsharing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * 分账交易混合订单(TProfitSharingTradeMixedOrder)实体类
 *
 * @author makejava
 * @since 2024-08-27 19:19:18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfitSharingTradeMixedOrder implements Serializable {
    
    private static final long serialVersionUID = -80537955302595275L;
    
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
     * 第三方商户号
     */
    private String thirdMerchantId;
    
    /**
     * 第三方支付订单号
     */
    private String thirdOrderNo;
    
    /**
     * 处理状态：0-待处理，1-已完成
     */
    private Integer state;
    
    /**
     * 支付金额,单位元
     */
    private BigDecimal amount;
    
    /**
     * 删除标识：0-未删除 1-已删除
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 是否混合支付：0-是，1-否
     */
    private Integer whetherMixedPay;
}

