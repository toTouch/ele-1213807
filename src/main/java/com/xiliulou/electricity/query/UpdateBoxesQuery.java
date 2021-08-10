package com.xiliulou.electricity.query;

import lombok.Data;


/**
 * 换电柜仓门表(TElectricityCabinetBox)实体类
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@Data
public class UpdateBoxesQuery {
    /**
     * 仓门Id
     */
    private Long id;
    /**
     * 仓门号
     */
    private String cellNo;
}
