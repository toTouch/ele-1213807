package com.xiliulou.electricity.query;


import com.xiliulou.electricity.query.base.BasePageQuery;
import lombok.Data;

import java.util.List;

/**
 * @author : renhang
 * @description FreeServiceFeePageQuery
 * @date : 2025-03-28 10:30
 **/
@Data
public class FreeServiceFeePageQuery extends BasePageQuery {

    private Integer tenantId;

    private String orderId;

    private Long uid;

    private Long franchiseeId;

    /**
     * 支付状态
     */
    private Integer status;

    /**
     * 支付渠道
     */
    private Integer paymentChannel;

    private Long payStartTime;

    private Long payEndTime;

    /**
     * 免押服务费类型
     */
    private Integer depositType;

    private List<Long> franchiseeIds;
}
