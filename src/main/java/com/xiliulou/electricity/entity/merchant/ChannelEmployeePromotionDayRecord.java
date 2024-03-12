package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/2/6 9:16
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_channel_employee_promotion_day_record")
public class ChannelEmployeePromotionDayRecord {
    /**
     * 主键id
     */
    private Long id;
    /**
     * 渠道员ID
     */
    private Long channelEmployeesId;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 日拉新返现（元）
     */
    private BigDecimal dayFirstMoney;
    
    /**
     * 日续费返现（元）
     */
    private BigDecimal dayRenewMoney;
    
    /**
     * 日费返现总额（元）
     */
    private BigDecimal dayTotalMoney;
    
    /**
     * 出账日期
     */
    private Long feeDate;
    
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
    
    /**
     * 日费差额总额
     */
    private BigDecimal dayBalanceMoney;
    
    /**
     * 日续费差额总额
     */
    private BigDecimal dayRenewBalanceMoney ;
}
