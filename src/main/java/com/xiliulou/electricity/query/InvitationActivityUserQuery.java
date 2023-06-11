package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-05-16:14
 */
@Data
@Builder
public class InvitationActivityUserQuery {
    private Long size;
    private Long offset;

    private Integer tenantId;
    private String userName;
    private String phone;

    private Long uid;
    private Long activityId;
}
