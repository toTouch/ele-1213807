package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 出账年月 yyyy-MM
     */
    private String date;
    
    /**
     * 记录单号
     */
    private String recordNo;
    
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

}
