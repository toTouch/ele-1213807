package com.xiliulou.electricity.query.car;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: BaoYu
 * @Date: 2023/9/8 10:58
 * @Description:
 */

@Data
public class CarRentalPackageRefundReq {

    private String packageOrderNo;

    private BigDecimal estimatedRefundAmount;

    private Long uid;

}
