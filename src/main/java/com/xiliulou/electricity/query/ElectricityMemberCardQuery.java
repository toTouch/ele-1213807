package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-09-17:29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectricityMemberCardQuery {

    private String name;

    private Integer tenantId;

    private Integer status;

    private Long franchiseeId;

    private Integer cardModel;
}
