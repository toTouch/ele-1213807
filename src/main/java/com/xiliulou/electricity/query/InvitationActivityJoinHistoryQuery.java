package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-06-13:27
 */
@Data
@Builder
public class InvitationActivityJoinHistoryQuery {

    private Long size;

    private Long offset;

    private Integer tenantId;

    private String username;

    private String phone;

}
