package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
public class EleDepositOrderQuery {
    private Long size;
    private Long offset;
    /**
     * 用户名字
     */
    private String name;
    private String phone;

    private Long beginTime;
    private Long endTime;
    private Integer status;
    private String orderId;

    private Integer tenantId;

    private List<Long> franchiseeIds;
    private String franchiseeName;

    private Long uid;

    private Integer depositType;
    private Integer payType;

    private Integer refundOrderType;

    private List<Long> storeIds;

    private String carModel;

}
