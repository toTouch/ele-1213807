package com.xiliulou.electricity.query.car;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;

/**
 * @author: BaoYu
 * @Date: 2023/9/8 10:58
 * @Description:
 */

@Data
public class CarRentalPackageRefundReq {

    @NotEmpty(message = "套餐订单不能为空")
    private String packageOrderNo;

    private BigDecimal estimatedRefundAmount;

    private Long uid;

}
