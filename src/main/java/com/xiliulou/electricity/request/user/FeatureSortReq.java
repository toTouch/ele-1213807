package com.xiliulou.electricity.request.user;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author HeYafeng
 * @date 2024/12/26 09:29:15
 */
@Builder
@Data
public class FeatureSortReq {
    
    @NotNull
    private Integer tenantId;
    
    @NotNull
    private Long uid;
}
