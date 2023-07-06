package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
     * 分帐配置id
     */
    private Long divisionAccountConfigId;

    /**
     * 套餐来源
     */
    private Integer source;

    /**
     * 套餐名称
     */
    private String membercardName;

    private Long beginTime;
    private Long endTime;

    private List<Long> franchiseeIds;

    private List<Long> storeIds;
}
