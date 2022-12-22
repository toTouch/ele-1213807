package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-22-19:17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RentCarMemberCardOrderQuery {

    private Long size;
    private Long offset;
    private String name;
    private String phone;

    private Long beginTime;
    private Long endTime;
    private Integer status;
    private String orderId;

    private Integer tenantId;

    private List<Long> franchiseeIds;

    private Long franchiseeId;

    private List<Long> storeIds;

    private Long storeId;


}
