package com.xiliulou.electricity.query;

import com.xiliulou.electricity.query.base.BasePageQuery;
import lombok.Data;

/**
 * @ClassName: MutualExchangePageQuery
 * @description:
 * @author: renhang
 * @create: 2024-11-27 17:47
 */
@Data
public class MutualExchangePageQuery extends BasePageQuery {
    
    String combinedName;
    
    Integer status;
    
    Integer tenantId;
}
