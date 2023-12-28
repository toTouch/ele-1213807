package com.xiliulou.electricity.request.tenantNote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2023/12/28 9:58
 * @desc
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantRechargePageRequest {
    /**
     * 租户Id
     */
    private Integer tenantId;
}
