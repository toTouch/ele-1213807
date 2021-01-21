package com.xiliulou.electricity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

/**
 * 换电柜仓门表(TElectricityCabinetBox)实体类
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectricityCabinetBoxDTO {
    /**
    * 所属换电柜柜Id
    */
    private Integer electricityCabinetId;
    /**
    * 仓门号
    */
    private String cellNo;
    /**
    * 可用状态（0-可用，1-禁用）
    */
    private Integer usableStatus;
    /**
    * 仓门状态（0-开门，1-关门）
    */
    private Integer boxStatus;
    /**
    * 状态（0-有电池，1-无电池）
    */
    private Integer status;
    /**
     * sn码
     */
    private String sn;


}