package com.xiliulou.electricity.entity.enterprise;


import com.xiliulou.electricity.enums.enterprise.CloudBeanStatusEnum;
import com.xiliulou.electricity.enums.enterprise.InvitationWayEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 企业渠道邀请用户表(EnterpriseChannelUser)实体类
 *
 * @author Eclair
 * @since 2023-09-14 10:18:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_enterprise_channel_user")
public class EnterpriseChannelUser {
    /**
     * 主键ID
     */
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
     * 云豆状态（0-初始态, 1-未回收, 2-已回收）
     * @see CloudBeanStatusEnum
     */
    private Integer cloudBeanStatus;
    
    /**
     * 邀请人UID
     */
    private Long inviterId;
    
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

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
