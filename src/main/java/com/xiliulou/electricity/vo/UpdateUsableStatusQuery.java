package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.List;

/**
 * 换电柜仓门表(TElectricityCabinetBox)实体类
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@Data
public class UpdateUsableStatusQuery {
    /**
     * 仓门Id
     */
    private Long id;
    /**
     * 仓门号
     */
    private String cellNo;
    /**
    * 所属换电柜柜Id
    */
    private Integer electricityCabinetId;
    /**
    * 可用状态（0-可用，1-禁用）
    */
    private Integer usableStatus;

}
