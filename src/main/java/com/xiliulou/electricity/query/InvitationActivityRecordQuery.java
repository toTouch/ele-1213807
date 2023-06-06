package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-06-13:28
 */
@Data
@Builder
public class InvitationActivityRecordQuery {

    private Long size;

    private Long offset;

    private Integer tenantId;

    private String username;

    private String phone;


}
