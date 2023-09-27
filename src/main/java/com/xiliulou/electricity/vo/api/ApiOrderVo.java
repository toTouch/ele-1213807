package com.xiliulou.electricity.vo.api;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author : eclair
 * @date : 2021/11/9 2:46 下午
 */
@Data
@AllArgsConstructor
public class ApiOrderVo {

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 订单状态
     */
    private String status;
}
