package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * 换电柜仓门表(TElectricityCabinetBox)实体类
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@Data
public class ElectricityCabinetBoxVO {
    /**
    * 仓门Id
    */
    private Long id;
    /**
    * 所属换电柜柜Id
    */
    private Integer electricityCabinetId;
    /**
    * 仓门号
    */
    private String cellNo;
    /**
    * 电池Id
    */
    private Long electricityBatteryId;
    /**
    * 可用状态（0-禁用，1-可用）
    */
    private Integer usableStatus;
    /**
    * 仓门状态（0-开门，1-关门）
    */
    private Integer boxStatus;
    /**
    * 状态（0-无电池，1-有电池）
    */
    private Integer status;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;
    /**
    * 是否删除（0-正常，1-删除）
    */
    private Integer delFlag;
    //电池编号
    private String sn;
    /**
     * 电池电量
     */
    private Double power;


}