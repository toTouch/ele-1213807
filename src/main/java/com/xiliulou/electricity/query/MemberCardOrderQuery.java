package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-11 18:23
 **/
@Data
@Builder
public class MemberCardOrderQuery {

    private String phone;
    private String orderId;
    private Integer cardType;
    private Long queryStartTime;
    private Long queryEndTime;
    private Long size;
    private Long offset;

    private Integer payType;

    private Integer cardModel;

    private Integer tenantId;

    private Integer status;

    private Long franchiseeId;
    private List<Long> franchiseeIds;
    private String franchiseeName;
    private String userName;

    /**
     * 套餐订单来源，1：扫码，2：线上，3：后台
     */
    private Integer source;

    /**
     * 扫码的柜机
     */
    private Long refId;

    /**
     * 套餐购买次数
     */
    private Integer cardPayCount;
}
