package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 柜机电费月度统计表
 * @date 2024/1/31 15:42:50
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@TableName("t_merchant_cabinet_power_month_record")
public class MerchantCabinetPowerMonthRecord {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 出账年月 yyyy-MM-01
     */
    private String date;
    
    /**
     * 场地数
     */
    private Integer placeCount;
    
    /**
     * 当月耗电量
     */
    private Double monthSumPower;
    
    /**
     * 当月电费
     */
    private Double monthSumCharge;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 0--正常 1--删除
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
     * 加盟商ID
     */
    private Long franchiseeId;
    
}
