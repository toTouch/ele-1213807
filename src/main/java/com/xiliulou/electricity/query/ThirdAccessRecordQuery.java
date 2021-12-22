package com.xiliulou.electricity.query;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2021/8/4 5:48 下午
 */
@Data
public class ThirdAccessRecordQuery {
    private Integer size;
    private Integer offset;
    private String requestId;
    private Integer tenantId;
}
