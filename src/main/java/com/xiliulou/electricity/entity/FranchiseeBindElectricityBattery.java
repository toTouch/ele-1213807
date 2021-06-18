package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * (ElectricityBatteryBind)实体类
 *
 * @author lxc
 * @since 2020-11-25 11:00:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_franchisee_bind_electricity_battery")
public class FranchiseeBindElectricityBattery {
    /**
     * 换电柜Id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * franchiseeId
     */
    private Integer franchiseeId;
    /**
     * 电池ID
     */
    private Long electricityBatteryId;

}
