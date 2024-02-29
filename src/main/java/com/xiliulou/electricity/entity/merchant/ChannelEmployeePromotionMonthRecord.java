package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/2/6 10:23
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_channel_employee_promotion_month_record")
public class ChannelEmployeePromotionMonthRecord {
    /**
     * 主键ID
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
     * 月拉新返现（元）
     */
    private BigDecimal monthFirstMoney;
    
    /**
     * 月续费返现（元）
     */
    private BigDecimal monthRenewMoney;
    
    /**
     * 月费返现总额（元）
     */
    private BigDecimal monthTotalMoney;
    
    /**
     * 日期 默认当月的最后一天中午十二点
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
}
