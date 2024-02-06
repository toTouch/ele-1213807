package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/2/6 10:36
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_place_fee_month")
public class MerchantPlaceFeeMonth {
    /**
     * id
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
     * 计算月份
     */
    private Long calculateDate;
    
    /**
     * 场地费月结总和（元）
     */
    private BigDecimal placeFee;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 删除标记(0-未删除，1-已删除)
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    private Long updateTime;
}
