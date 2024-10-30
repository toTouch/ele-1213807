package com.xiliulou.electricity.query;

import com.xiliulou.electricity.query.base.BasePageQuery;
import lombok.Data;

import java.util.List;

/**
 * @ClassName: BatteryCarMemberListQuery
 * @description:
 * @author: renhang
 * @create: 2024-10-14 09:11
 */
@Data
public class BatteryCarMemberListQuery extends BasePageQuery {
    
    private String name;
    
    private Integer tenantId;
    
    private List<Long> franchiseeIds;
}
