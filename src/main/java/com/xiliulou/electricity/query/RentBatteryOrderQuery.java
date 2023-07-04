package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
public class RentBatteryOrderQuery {
    private Long size;
    private Long offset;
    /**
     * 用户名字
     */
    private String name;
    private String phone;

    private Long beginTime;
    private Long endTime;
    private String status;
    private String orderId;
    private Integer type;
    private List<Integer> eleIdList;
    private Integer tenantId;
    private List<Long> franchiseeIds;
    private List<Long> storeIds;
}
