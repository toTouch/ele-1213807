package com.xiliulou.electricity.entity.battery;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * @author SJP
 * @date 2025-02-14 15:12
 **/
@Data
@Builder
@TableName("t_electricity_battery_label")
public class ElectricityBatteryLabel {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * sn码
     */
    private String sn;
    
    /**
     * 电池标签
     */
    private Integer label;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 领用管理员id
     */
    private Long administratorId;
    
    /**
     * 领用商户id
     */
    private Long merchantId;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    private Integer delFlag;
    
    private Long createTime;
    
    private Long updateTime;
}
