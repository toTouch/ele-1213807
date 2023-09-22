package com.xiliulou.electricity.model.car.opt;

import com.xiliulou.electricity.enums.PayTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
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
     * 加盟商ID
     */
    private Integer franchiseeId;

    /**
     * 门店ID
     */
    private Integer storeId;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 套餐ID
     */
    private Long rentalPackageId;

    /**
     * 用户使用的优惠券ID集
     */
    private List<Long> userCouponIds;

    /**
     * 保险ID
     */
    private Long insuranceId;

    /**
     * 交易方式
     * <pre>
     *     1-线上
     *     2-线下
     * </pre>
     * @see PayTypeEnum
     */
    private Integer payType;

    /**
     * 实缴押金金额(元)
     */
    private BigDecimal deposit;

}
