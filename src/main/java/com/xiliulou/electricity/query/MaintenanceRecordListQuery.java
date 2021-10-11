package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author : eclair
 * @date : 2021/9/26 3:16 下午
 */
@Data
@Builder
public class MaintenanceRecordListQuery {
    private Integer size;
    private Integer offset;
    private Long beginTime;
    private Long endTime;
    private String type;
    private String status;
    private Long uid;
    private String phone;
    private Integer electricityCabinetId;

    private Integer tenantId;

    private List<Integer> eleIdList;
}
