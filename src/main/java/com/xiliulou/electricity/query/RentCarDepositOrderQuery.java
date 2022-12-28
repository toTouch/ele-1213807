package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-22-19:09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RentCarDepositOrderQuery {

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

    private String franchiseeName;

    private Long uid;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    private Integer depositType;
    private Integer payType;

    private Integer refundOrderType;

    private List<Long> storeIds;

    private Long storeId;

    private String carModel;

    private String storeName;
}
