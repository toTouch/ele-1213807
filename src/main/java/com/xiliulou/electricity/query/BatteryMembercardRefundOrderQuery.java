package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-12-15:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryMembercardRefundOrderQuery {
    private Long size;
    private Long offset;

    private Long uid;

    private String phone;

    private Long mid;

    private String refundOrderNo;

    private Integer status;

    private Integer type;

    private Integer rentType;

    private Integer payType;

    private Long createTime;

    private Long updateTime;

    private Integer tenantId;

    private Long startTime;

    private Long endTime;

    private List<Long> franchiseeIds;

    private List<Long> storeIds;
}
