package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.Set;


/**
 * @author: maxiaodong
 * @Date: 2023/12/1 10:02
 * @Description:
 */
@Data
@Builder
public class UserAmountQueryModel {
    private Integer tenantId;
    
    private Set<Long> uidList;
}
