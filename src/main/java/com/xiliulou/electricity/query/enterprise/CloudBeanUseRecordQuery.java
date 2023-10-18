package com.xiliulou.electricity.query.enterprise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-18-15:49
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CloudBeanUseRecordQuery {

    private Long size;
    private Long offset;

    private Long enterpriseId;
    
    private Long uid;

    private Integer type;

    private Integer tenantId;

    private Long startTime;
    private Long endTime;
}
