package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (InvitationActivityRecord)实体类
 *
 * @author Eclair
 * @since 2023-06-05 20:17:53
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_invitation_activity_record")
public class InvitationActivityRecord {

    private Long id;
    /**
     * 活动id
     */
    private Long activityId;
    /**
     * 加密code
     */
    private String code;
    /**
     * 用户uid
     */
    private Long uid;
    /**
     * 分享人数
     */
    private Integer shareCount;
    /**
     * 邀请成功人数
     */
    private Integer invitationCount;
    /**
     * 分享状态 1--初始化，2--已分享，3--分享失败
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;

    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //1 初始化  2 分享成功  3 分享失败
    public static final Integer STATUS_INIT = 1;
    public static final Integer STATUS_SUCCESS = 2;
    public static final Integer STATUS_FAIL = 3;

}
