package com.xiliulou.electricity.entity.battery;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author SJP
 * @date 2025-02-14 15:12
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_electricity_battery_label")
public class ElectricityBatteryLabel {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * sn码
     */
    private String sn;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 领用人，管理员uid或者商户id
     */
    private Long receiverId;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    private Long createTime;
    
    private Long updateTime;
}
