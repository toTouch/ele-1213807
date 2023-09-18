package com.xiliulou.electricity.query.enterprise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/18 14:00
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseChannelUserQuery {

    private Long uid;

    private String phone;

    private Integer invitationWay;

    private Long enterpriseId;

    private Long franchiseeId;

    private Long tenantId;

    private Integer renewalStatus;


}
