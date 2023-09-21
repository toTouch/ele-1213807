package com.xiliulou.electricity.vo.car;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class CarRentRefundVo implements Serializable {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 原因
     */
    private String reason;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 审核标记
     */
    private Boolean approveFlag;

    /**
     * 审核人UID
     */
    private Long uid;



}
