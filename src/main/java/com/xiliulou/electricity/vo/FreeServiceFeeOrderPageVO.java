package com.xiliulou.electricity.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : renhang
 * @description FreeServiceFeeOrderPageVO
 * @date : 2025-03-28 10:38
 **/
@Data
public class FreeServiceFeeOrderPageVO {

    private Long id;

    private String orderId;

    private String freeDepositOrderId;

    private Long uid;

    private String name;

    private String phone;

    private BigDecimal payAmount;

    private Integer depositType;

    private Long franchiseeId;

    private String franchiseeName;

    private Integer status;

    private String paymentChannel;
}
