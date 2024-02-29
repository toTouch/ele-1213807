package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 小程序端电柜电费月度统计表（存的是两个月前的数据，近2个月的数据需要实时查询）
 * @date 2024/2/21 18:02:41
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@TableName("t_merchant_cabinet_power_month_record_pro")
public class MerchantCabinetPowerMonthRecordPro {
    
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
     * 当月耗电量
     */
    private BigDecimal monthSumPower;
    
    /**
     * 当月电费
     */
    private BigDecimal monthSumCharge;
    
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
