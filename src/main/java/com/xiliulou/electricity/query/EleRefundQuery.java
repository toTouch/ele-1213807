package com.xiliulou.electricity.query;


import lombok.Builder;
import lombok.Data;

/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
@Builder
public class EleRefundQuery {

    /**
     * 缴纳押金订单编号
     */
    private String orderId;

    /**
     * 订单的状态
     */
    private Integer status;

    private Long size;
    private Long offset;


    private Long beginTime;
    private Long endTime;

    private Integer tenantId;

    /**
     * 所属加盟商名字
     */
    private String franchiseeName;

    private String phone;

    /**
     * 用户名字
     */
    private String name;

    /**
     * 退押金类型
     */
    private Integer refundOrderType;

    private Integer payType;

    private Long storeId;

    private Long franchiseeId;


}
