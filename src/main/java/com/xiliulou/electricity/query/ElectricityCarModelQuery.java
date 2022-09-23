package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: eclair
 * @Date: 2022/6/6 10:02
 * @Description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ElectricityCarModelQuery {
    private Long size;
    private Long offset;
    private String name;
    private List<Long> franchiseeIds;
    private Long franchiseeId;
    private List<Long> storeIds;
    private Long uid;

    //租户id
    private Integer tenantId;
}
