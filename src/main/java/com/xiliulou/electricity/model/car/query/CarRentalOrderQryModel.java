package com.xiliulou.electricity.model.car.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 车辆租赁订单表，DB层查询模型
 * @author xiaohui.song
 **/
@Data
public class CarRentalOrderQryModel implements Serializable {

    private static final long serialVersionUID = -8960817730474079740L;

    /**
     * 偏移量
     */
    private Integer offset = 0;

    /**
     * 取值数量
     */
    private Integer limitNum = 10;

    /**
     * 租户ID
     */
    private Integer tenantId;
}
