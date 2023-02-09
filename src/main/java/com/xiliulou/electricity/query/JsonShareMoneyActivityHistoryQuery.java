package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: lxc
 * @Date: 2021/4/15 16:02
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JsonShareMoneyActivityHistoryQuery {
    private Long size;
    private Long offset;
    private Long uid;
    private Integer activityId;
    private Integer tenantId;
    
    private Long id;
}
