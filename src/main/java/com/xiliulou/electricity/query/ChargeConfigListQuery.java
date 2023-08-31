package com.xiliulou.electricity.query;

import lombok.Data;

import java.util.List;

/**
 * @author : eclair
 * @date : 2023/7/18 10:25
 */
@Data
public class ChargeConfigListQuery {
    /**
     * 电价名称
     */
    private String name;

    private Integer franchiseeId;
    private List<Long> franchiseeIds;

    private Integer storeId;
    private List<Long> storeIds;
    /**
     * 电柜id
     */
    private Long eid;

    private Integer tenantId;

    private Integer size;

    private Integer offset;
}
