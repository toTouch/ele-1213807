package com.xiliulou.electricity.dto;


import com.xiliulou.electricity.entity.UserInfo;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : renhang
 * @description CreateFreeServiceFeeOrderDTO
 * @date : 2025-03-27 17:06
 **/
@Data
@Builder
public class CreateFreeServiceFeeOrderDTO {
    private UserInfo userInfo;

    /**
     * 押金订单号
     */
    private String depositOrderId;

    /**
     * 免押服务费
     */
    private BigDecimal freeServiceFee;

    /**
     * 支付状态
     */
    private Integer status;


    /**
     * 支付时间
     */
    private Long payTime;

    /**
     * 支付取渠道
     */
    private String paymentChannel;

}
