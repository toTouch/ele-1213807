package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (InvitationActivityMemberCard)实体类
 *
 * @author Eclair
 * @since 2023-06-05 15:31:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_invitation_activity_member_card")
public class InvitationActivityMemberCard {

    private Long id;
    /**
     * 活动Id
     */
    private Long activityId;
    /**
     * 套餐Id
     */
    private Long mid;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * 套餐类型
     */
    private Integer packageType;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
