package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;


/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
public class UserAmountQuery {
    private Long size;
    private Long offset;

    private Integer tenantId;

    private String phone;

    private List<Long> franchiseeIds;

    private List<Long> storeIds;
}
