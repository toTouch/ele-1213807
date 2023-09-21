package com.xiliulou.electricity.query.enterprise;

import com.xiliulou.electricity.enums.enterprise.InvitationWayEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
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

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 被邀请用户ID
     */
    private Long uid;

    /**
     * 被邀请用户电话
     */
    private String phone;

    /**
     * 邀请方式
     * @see InvitationWayEnum
     */
    private Integer invitationWay;

    /**
     * 企业用户所属运营商ID
     */
    private Long franchiseeId;

    /**
     * 企业用户所属租户ID
     */
    private Long tenantId;

    /**
     * 续费状态
     * @see RenewalStatusEnum
     */
    private Integer renewalStatus;


}
