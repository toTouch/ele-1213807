package com.xiliulou.electricity.query;


import lombok.Builder;
import lombok.Data;

/**
 * @author : renhang
 * @description UserDisableMemberQuery
 * @date : 2025-02-26 14:58
 **/
@Data
@Builder
public class UserDisableMemberQuery {

    private Long uid;

    private Long offset;

    private Long size;

    private Integer tenantId;
}
