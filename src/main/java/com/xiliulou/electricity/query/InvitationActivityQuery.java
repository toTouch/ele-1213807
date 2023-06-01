package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-01-16:03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationActivityQuery {

    private Long size;
    private Long offset;

    private Integer tenantId;

    private Integer status;

    private String name;


}
