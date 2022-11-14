package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (BatteryChargeConfig)表实体类
 *
 * @author zzlong
 * @since 2022-08-12 14:49:37
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_battery_charge_config")
public class BatteryChargeConfig {

    private Long id;
    /**
     * 柜机模式
     */
    private String applicationModel;
    /**
     * 电池充电配置
     */
    private String config;

    private Long electricityCabinetId;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;
    /**
     * 租户id
     */
    private Integer tenantId;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
