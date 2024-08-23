package com.xiliulou.electricity.vo.profitsharing;

import com.xiliulou.electricity.enums.ElectricityPayParamsConfigEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分账方配置表(TProfitSharingConfig)实体类
 *
 * @author makejava
 * @since 2024-08-22 17:14:08
 */
@Data
public class ProfitSharingConfigVO implements Serializable {
    
    private static final long serialVersionUID = 514513200105352426L;
    
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
     * 支付配置表id
     */
    private Integer payParamId;
    
    /**
     * 微信商户号
     */
    private String wechatMerchantId;
    /**
     * 配置类型
     *
     * @see ElectricityPayParamsConfigEnum
     */
    private Integer configType;
    
    /**
     * 配置状态：0-启用 1-禁用
     */
    private Integer configStatus;
    
    /**
     * 订单类型：1-换电-套餐购买 ，2-换电-保险购买，3-换电-滞纳金缴纳（如同时选择多个类型，则之为类型之和）
     */
    private Integer orderType;
    
    /**
     * 每月最大分账上限
     */
    private BigDecimal amountLimit;
    
    /**
     * 分账类型：1-按订单比例
     */
    private Integer profitSharingType;
    
    /**
     * 允许比例上限
     */
    private BigDecimal scaleLimit;
    
    /**
     * 周期类型：1:D+1
     */
    private Integer cycleType;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    
}

