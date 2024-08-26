package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-20-18:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeDepositRechargeRecordQuery {

    private Long size;

    private Long offset;

    private Integer tenantId;

    private String tenantName;

    private Long startTime;

    private Long endTime;
    
    private Integer freeType;
}
