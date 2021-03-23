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
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * sn码
     */
    @NotEmpty(message = "电池编码不能不能为空!")
    private String sn;
    /**
     * 电池型号
     */
    private String model;
    /**
     * 电池电量
     */
    private Double power;
    /**
     * 电压
     */
    private Integer voltage;
    /**
     * 电池容量,单位(mah)
     */
    private Integer capacity;
    /**
     * 0：在仓，1：在库，2：租借
     */
    private Integer status;

    private Long createTime;

    private Long updateTime;

    private Integer delFlag;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;


    /**
     * 0：正常 1：故障
     */
    private Integer healthStatus;
    /**
     * 0--空闲 1--正在开机 2--充电中 3--充满电 4--限额充电 -1 未充电
     */
    private Integer chargeStatus;

    //在仓
    public static final Integer WARE_HOUSE_STATUS = 0;
    //在库
    public static final Integer STOCK_STATUS = 1;
    //租借
    public static final Integer LEASE_STATUS = 2;
    //异常取走
    public static final Integer EXCEPTION_STATUS = 3;

}