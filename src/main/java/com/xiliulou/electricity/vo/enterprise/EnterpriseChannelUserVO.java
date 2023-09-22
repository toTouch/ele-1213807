package com.xiliulou.electricity.vo.enterprise;

import com.xiliulou.electricity.enums.enterprise.InvitationWayEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import lombok.Data;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/19 4:20
 */

@Data
public class EnterpriseChannelUserVO {

    private Long id;

    /**
     * 企业id
     */
    private Long enterpriseId;

    /**
     * 企业添加用户的uid
     */
    private Long uid;

    /**
     * 企业添加用户名
     */
    private String name;

    /**
     * 企业添加用户电话
     */
    private String phone;

    /**
     * 邀请方式 0:面对面添加,1:手机号添加
     * @see InvitationWayEnum
     */
    private Integer invitationWay;

    /**
     * 所属加盟商id
     */
    private Long franchiseeId;
    /**
     * 租户ID
     */
    private Long tenantId;
    /**
     * 自主续费状态 0:不自主续费, 1:自主续费
     * @see RenewalStatusEnum
     */
    private Integer renewalStatus;

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    /**
     * 备注
     */
    private String remark;

    private Integer authStatus;

}
