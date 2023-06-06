package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (InvitationActivityJoinHistory)实体类
 *
 * @author Eclair
 * @since 2023-06-06 09:51:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_invitation_activity_join_history")
public class InvitationActivityJoinHistory {

    private Long id;
    /**
     * 邀请用户uid
     */
    private Long uid;
    /**
     * record_id
     */
    private Long recordId;
    /**
     * 参与用户uid
     */
    private Long joinUid;
    /**
     * 参与开始时间
     */
    private Long startTime;
    /**
     * 参与过期时间
     */
    private Long expiredTime;
    /**
     * 活动id
     */
    private Long activityId;
    /**
     * 参与状态 1--初始化，2--已参与，3--已过期
     */
    private Integer status;
    /**
     * 租户id
     */
    private Integer tenantId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //已参与
    public static Integer STATUS_INIT = 1;
    //邀请成功
    public static Integer STATUS_SUCCESS = 2;
    //已过期
    public static Integer STATUS_FAIL = 3;
    //被替换
    public static Integer STATUS_REPLACE = 4;
    //活动已下架
    public static Integer STATUS_OFF = 5;
}
