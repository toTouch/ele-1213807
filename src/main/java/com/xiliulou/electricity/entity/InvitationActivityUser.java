package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (InvitationActivityUser)实体类
 *
 * @author Eclair
 * @since 2023-06-05 16:11:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_invitation_activity_user")
public class InvitationActivityUser {

    private Long id;
    /**
     * 活动Id
     */
    private Long activityId;
    /**
     * 用户Id
     */
    private Long uid;

    private Long operator;
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

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
