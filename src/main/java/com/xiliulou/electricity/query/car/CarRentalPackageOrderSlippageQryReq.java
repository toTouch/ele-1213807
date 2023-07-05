package com.xiliulou.electricity.query.car;

import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐订单逾期表，查询模型
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderSlippageQryReq implements Serializable {

    private static final long serialVersionUID = 2273061223218238582L;

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
