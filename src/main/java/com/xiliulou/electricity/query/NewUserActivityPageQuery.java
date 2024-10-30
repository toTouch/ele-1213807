package com.xiliulou.electricity.query;

import com.xiliulou.electricity.query.base.BasePageQuery;
import lombok.Data;

/**
 * @ClassName: NewUserActivityPageQuery
 * @description:
 * @author: renhang
 * @create: 2024-10-14 16:40
 */
@Data
public class NewUserActivityPageQuery extends BasePageQuery {
    
    private Integer tenantId;
    
    private String name;
}
