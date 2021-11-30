package com.xiliulou.electricity.web.query;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2021/11/10 3:53 下午
 */
@Data
public class ApiOrderQuery {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 是否需要操作记录
     */
    private Boolean needOperateRecord;
}
