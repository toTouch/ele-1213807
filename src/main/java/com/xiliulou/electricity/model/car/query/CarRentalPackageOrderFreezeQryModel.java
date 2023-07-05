package com.xiliulou.electricity.model.car.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐订单冻结表，DB层查询模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderFreezeQryModel implements Serializable {

    private static final long serialVersionUID = 5816010520362253658L;

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
