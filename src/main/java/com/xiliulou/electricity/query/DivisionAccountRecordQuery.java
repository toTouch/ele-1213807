package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-24-16:26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DivisionAccountRecordQuery {

    private Long size;

    private Long offset;

    private Integer tenantId;

    /**
     * 套餐名称
     */
    private String membercardName;

    private Long beginTime;
    private Long endTime;
}
