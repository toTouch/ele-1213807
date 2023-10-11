package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: hrp
 * @Date: 2022/08/16 10:02
 * @Description:
 */
@Data
@Builder
public class BatteryServiceFeeQuery {


    private Long size;
    private Long offset;


    private String phone;
    private Long uid;
    private String name;

    private Long beginTime;
    private Long endTime;

    private Integer status;

    private Integer source;
    private Integer tenantId;

    private String orderId;
    
    private List<Long> franchiseeIds;
    private List<Long> storeIds;

    /**
     * 支付时间开始
     */
    private Long payTimeBegin;

    /**
     * 支付时间截止
     */
    private Long payTimeEnd;
}
