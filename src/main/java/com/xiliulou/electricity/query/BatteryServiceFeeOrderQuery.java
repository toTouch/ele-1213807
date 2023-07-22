package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-22-9:41
 */
@Data
@Builder
public class BatteryServiceFeeOrderQuery {
    private Long size;

    private Long offset;

    private Long uid;

    private Integer status;

    private Long queryStartTime;

    private Long queryEndTime;

    private Integer tenantId;

    private List<Long> franchiseeIds;

    private List<Long> storeIds;
}
