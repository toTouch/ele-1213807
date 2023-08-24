package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-06-13:28
 */
@Data
@Builder
public class InvitationActivityRecordQuery {

    private Long size;

    private Long offset;

    private Integer tenantId;

    private Long uid;

    private String userName;

    private String phone;

    private Integer status;

    private Long beginTime;

    private Long endTime;

    private List<Long> franchiseeIds;

    private List<Long> storeIds;
}
