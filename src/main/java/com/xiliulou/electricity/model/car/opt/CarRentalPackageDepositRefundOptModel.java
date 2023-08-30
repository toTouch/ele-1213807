package com.xiliulou.electricity.model.car.opt;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 租车套餐押金退款表<br />
 * 转换DB
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageDepositRefundOptModel implements Serializable {

    private static final long serialVersionUID = -6279698714389334260L;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 押金缴纳订单编号
     */
    private String depositPayOrderNo;

    /**
     * 实际退款金额
     */
    private BigDecimal realAmount;

}
