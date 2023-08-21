package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-06-14:22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarLockCtrlHistoryQuery {

    private Long size;

    private Long offset;

    private Integer tenantId;

    private String carSn;

    private Long uid;

    private String name;

    private String phone;

    private Long beginTime;

    private Long endTime;

    private List<Long> franchiseeIds;

    private List<Long> storeIds;
}
