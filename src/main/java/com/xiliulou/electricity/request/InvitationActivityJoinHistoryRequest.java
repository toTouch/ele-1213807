package com.xiliulou.electricity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 邀请活动加入历史记录请求
 * @author zbz
 * @date 2024-08-01
 */
@Data
@AllArgsConstructor
public class InvitationActivityJoinHistoryRequest {
    
    /**
     * 邀请人记录id recordId
     */
    private Long id;
    private Integer tenantId;

    private Integer status;

    private Long joinUid;

    private String userName;

    private String phone;

    private Long uid;

    private Long activityId;

    private String activityName;

    private Long beginTime;

    private Long endTime;

    private List<Long> franchiseeIds;

    private List<Long> storeIds;

    private Integer payCount;
}
