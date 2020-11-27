package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.validation.constraints.NotEmpty;

/**
 * 电池型号(ElectricityBatteryModel)实体类
 *
 * @author makejava
 * @since 2020-11-26 14:44:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_battery_model")
public class ElectricityBatteryModel {

    private Integer id;
    /**
     * 电池型号名称
     */
    @NotEmpty(message = "电池型号名称")
    private String name;
    /**
     * 电压
     */
    private Integer voltage;
    /**
     * 电池容量,单位(mah)
     */
    private Integer capacity;
    /**
     * 类型(原材料)
     */
    private String startingMaterial;

    private Long createTime;

    private Long updateTime;
    @TableLogic
    private Object delFlag;


}