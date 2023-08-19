package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: lxc
 * @Date: 2021/4/15 16:02
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShareActivityRecordQuery {
    private Long size;
    private Long offset;
    private String phone;
    private String name;
    private Long uid;

    private Integer tenantId;
    
    private Long startTime;
    
    private Long endTime;

    private List<Long> franchiseeIds;

    private List<Long> storeIds;
}
