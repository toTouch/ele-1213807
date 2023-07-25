package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author : eclair
 * @date : 2023/7/19 13:25
 */
@Data
@Builder
public class ElePowerListQuery {
    private Integer size;
    private Integer offset;
    private Long eid;
    private Long startTime;
    private Long endTime;
    private Integer tenantId;
}
