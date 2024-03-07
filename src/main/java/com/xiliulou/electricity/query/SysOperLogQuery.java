package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-10-12-16:57
 */
@Deprecated
@Data
@Builder
public class SysOperLogQuery {
    private Long size;
    private Long offset;
    private Long beginTime;
    private Long endTime;
    private Long operatorUid;
    private Integer status;
    private String title;
    private Integer tenantId;
}
