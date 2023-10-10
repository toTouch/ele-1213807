package com.xiliulou.electricity.query.enterprise;

import com.xiliulou.electricity.enums.enterprise.InvitationWayEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;

    /**
     * 企业ID
     */
    @NotBlank(message = "企业id不能为空", groups = {CreateGroup.class})
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
    @NotBlank(message = "自主续费状态不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer renewalStatus;
    
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;


}
