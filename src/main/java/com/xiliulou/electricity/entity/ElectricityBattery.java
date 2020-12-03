package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 换电柜电池表(ElectricityBattery)实体类
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_battery")
public class ElectricityBattery {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
     * 所属店铺
     */
    private Integer shopId;
    /**
     * 代理商id
     */
    private Integer agentId;
    /**
     * sn码
     */
    @NotEmpty(message = "电池编码不能不能为空!")
    private String serialNumber;
    /**
     * 型号id
     */
    @NotNull(message = "电池型号不能为空!")
    private Integer modelId;
    /**
     * 电池电量
     */

    private Integer capacity;
    /**
     * 0：在仓，1：在库，2：租借
     */
    private Object status;

    private Long createTime;

    private Long updateTime;

    private Object delFlag;
    /**
     * 用户id
     */
    private Long uid;

    private Integer cabinetId;

}