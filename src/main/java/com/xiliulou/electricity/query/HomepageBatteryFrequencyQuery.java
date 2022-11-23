package com.xiliulou.electricity.query;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: HRP
 * @Date: 2022/08/03 10:02
 * @Description:
 */
@Data
@Builder
public class HomepageBatteryFrequencyQuery {
    private Long size;
    private Long offset;

    private Integer tenantId;


    private Long beginTime;
    private Long endTime;


    private String batterySn;

    private Long franchiseeId;
    
    private List<Long> franchiseeIds;
}
