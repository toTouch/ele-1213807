package com.xiliulou.electricity.vo.rental;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退租提示数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class RefundRentOrderHintVo implements Serializable {

    /**
     * 租金(支付价格)
     */
    private BigDecimal rentPayment;

    /**
     * 预估可退金额
     */
    private BigDecimal refundAmount;

    /**
     * 余量描述
     */
    private String residueStr;

}
