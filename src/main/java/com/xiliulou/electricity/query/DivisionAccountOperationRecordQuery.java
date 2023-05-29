package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-05-09-11:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DivisionAccountOperationRecordQuery {

    private Long size;

    private Long offset;
    /**
     * 分账配置id
     */
    private Integer divisionAccountId;

    /**
     * 修改人id
     */
    private Long uid;
    /**
     * 业务名称
     */
    private String name;
    /**
     * 租户id
     */
    private Integer tenantId;

}
