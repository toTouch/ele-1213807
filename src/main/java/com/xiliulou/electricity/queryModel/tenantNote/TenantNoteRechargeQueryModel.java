package com.xiliulou.electricity.queryModel.tenantNote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2023/12/28 13:43
 * @desc
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TenantNoteRechargeQueryModel {
    /**
     * 租户Id
     */
    private Integer tenantId;
}
