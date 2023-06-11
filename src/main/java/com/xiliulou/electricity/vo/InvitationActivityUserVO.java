package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-05-16:33
 */
@Data
public class InvitationActivityUserVO {
    private Long id;

    private Long uid;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 用户名
     */
    private String userName;

    private Long operator;
    /**
     * 操作人姓名
     */
    private String operatorName;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    private Long activityId;
    private String activityName;
}
