package com.xiliulou.electricity.query.enterprise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/22 15:14
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterprisePackageOrderQuery {

    private Long packageId;

    private Long enterpriseId;

    private Long uid;

    private Integer tenantId;




}
