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




}
