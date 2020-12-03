package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * 电池型号名称
     */
    @NotEmpty(message = "电池型号名称")
    private String name;
    /**
     * 电压
     */
    @NotNull(message = "电池电压不能为空!")
    private Integer voltage;
    /**
     * 电池容量,单位(mah)
     */
    @NotNull(message = "电池容量不能为空1")
    private Integer capacity;
    /**
     * 类型(原材料)
     */
    @NotEmpty(message = "电池种类不能为空!")
    private String startingMaterial;

    private Long createTime;

    private Long updateTime;
    @TableLogic
    private Object delFlag;

}