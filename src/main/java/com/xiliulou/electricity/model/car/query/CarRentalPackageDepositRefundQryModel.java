package com.xiliulou.electricity.model.car.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐押金退款表，DB层查询模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageDepositRefundQryModel implements Serializable {

    private static final long serialVersionUID = -8720030687946314245L;

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
