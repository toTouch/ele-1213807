package com.xiliulou.electricity.model.car.opt;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 租车套餐购买订单数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderBuyOptModel implements Serializable {

    private static final long serialVersionUID = 6210569909931652061L;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 套餐ID
     */
    private Long rentalPackageId;

    /**
     * 使用的优惠券ID集
     */
    private List<Long> couponIds;

    /**
     * 保险ID
     */
    private Long insuranceId;

}
