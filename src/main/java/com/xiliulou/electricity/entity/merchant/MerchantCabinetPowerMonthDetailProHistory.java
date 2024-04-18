package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 小程序端电柜电费月度详情表（存的是两个月前的数据，近2个月的数据需要实时查询）-柜机详情中历史数据
 * @date 2024/2/21 18:02:41
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@TableName("t_merchant_cabinet_power_month_detail_pro_history")
public class MerchantCabinetPowerMonthDetailProHistory {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 商户ID
     */
    private Long merchantId;
    
    /**
     * 场地ID
     */
    private Long placeId;
    
    /**
     * 电柜ID
     */
    private Long eid;
    
    private String sn;
    
    /**
     * 计算月份 yyyy-MM
     */
    private String date;
    
    /**
     * 用电量
     */
    private BigDecimal sumPower;
    
    /**
     * 用电电费
     */
    private BigDecimal sumCharge;
    
    /**
     * 开始时间
     */
    private Long beginTime;
    
    /**
     * 结束时间
     */
    private Long endTime;
    
    /**
     * 柜机和商户的绑定状态：0-绑定，1-解绑
     */
    private Integer cabinetMerchantBindStatus;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 删除标记，0--正常 1--删除
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
    
}
