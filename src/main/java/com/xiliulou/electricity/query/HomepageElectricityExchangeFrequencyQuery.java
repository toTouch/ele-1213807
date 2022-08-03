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
public class HomepageElectricityExchangeFrequencyQuery {
    private Long size;
    private Long offset;

    private Integer tenantId;
    private String electricityCabinetName;

    private Long beginTime;
    private Long endTime;

    private Long franchiseeId;

    private List<Integer> eleIdList;
}
