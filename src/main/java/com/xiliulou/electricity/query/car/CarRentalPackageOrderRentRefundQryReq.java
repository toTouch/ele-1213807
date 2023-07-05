package com.xiliulou.electricity.query.car;

import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐订单租金退款表，查询模型
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderRentRefundQryReq implements Serializable {

    private static final long serialVersionUID = -5181266277494113143L;

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
