package com.xiliulou.electricity.query;

import com.xiliulou.electricity.query.base.BasePageQuery;
import lombok.Data;

/**
 * @ClassName: ShareAndUserActivityQuery
 * @description:
 * @author: renhang
 * @create: 2024-10-14 09:46
 */
@Data
public class ShareAndUserActivityQuery extends BasePageQuery {
    
    private Integer tenantId;
    
    private String name;
}
