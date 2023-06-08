package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-06-14:05
 */
@Data
public class InvitationActivityRecordVO {

    private Long id;
    /**
     * 活动id
     */
    private Long activityId;
    private String activityName;

    /**
     * 用户uid
     */
    private Long uid;
    private String userName;
    private String phone;
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

    private BigDecimal money;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;


}
