package com.xiliulou.electricity.dto;

import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/7/26 9:55
 * @Description:
 */

@Data
public class DivisionAccountOrderDTO {

    /**
     * 订单号码
     */
    private String orderNo;

    /**
     * 套餐类型 (0 - 换电， 1 - 租车/车电一体)
     */
    private Integer Type;

    /**
     * 分账类型 (0 - 购买， 1 - 退租)
     */
    private Integer divisionAccountType;


}
