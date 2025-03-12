package com.xiliulou.electricity.query;


import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author : renhang
 * @description FreeDepositExpireRecordQuery
 * @date : 2025-02-25 14:07
 **/
@Data
@Builder
public class FreeDepositExpireRecordQuery {


    private Long size;

    private Long offset;

    private Long uid;

    private Integer depositType;

    private Integer tenantId;

    private Long franchiseeId;

    private List<Long> franchiseeIds;

    private Integer status;
}
