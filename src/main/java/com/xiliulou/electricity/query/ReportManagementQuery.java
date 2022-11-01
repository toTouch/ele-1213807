package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportManagementQuery {

    private Long id;
    private Long size;
    private Long offset;

    private Long startTime;
    private Long endTime;
    private Integer tenantId;



}
