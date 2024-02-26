package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/2/26 12:50
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_cabinet_bind_history")
public class MerchantCabinetBindHistory {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 商户id
     */
    private Long merchantId;
    
    /**
     * 柜机id
     */
    private Long cabinetId;
    
    /**
     * 场地id
     */
    private Long placeId;
    
    /**
     * 开始时间
     */
    private Long startTime;
    
    /**
     * 结束时间
     */
    private Long endTime;
    /**
     * 计算月份
     */
    private String calculateMonth;
    
    /**
     * 场地费范围总和（元）
     */
    private BigDecimal placeFee;
    /**
     * 状态(1-解绑，0-绑定)
     */
    private Integer status;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    private Integer delFlag;
    private Long createTime;
    private Long updateTime;
}
