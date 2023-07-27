package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author : eclair
 * @date : 2023/7/19 16:27
 */
@Data
@Builder
public class PowerMonthStatisticsQuery {
    private String startDate;
    private String endDate;
    private Integer tenantId;
    private Long storeId;
    private Long franchiseeId;
    private Long eid;
    private Integer size;
    private Integer offset;
}
