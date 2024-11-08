package com.xiliulou.electricity.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @ClassName: InitTenantSubscriptRequest
 * @description:
 * @author: renhang
 * @create: 2024-09-25 16:51
 */
@Data
@Builder
public class InitTenantSubscriptRequest {
    
    
    private List<Integer> tenantIds;
}
