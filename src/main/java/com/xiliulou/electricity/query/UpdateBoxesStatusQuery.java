package com.xiliulou.electricity.query;

import lombok.Data;

import java.util.List;

/**
 * 换电柜仓门表(TElectricityCabinetBox)实体类
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@Data
public class UpdateBoxesStatusQuery {
    /**
    * 所属换电柜柜Id
    */
    private Integer electricityCabinetId;
    /**
    * 可用状态（0-可用，1-禁用）
    */
    private Integer usableStatus;


    List<UpdateBoxesQuery> updateBoxesQueryList;


}
