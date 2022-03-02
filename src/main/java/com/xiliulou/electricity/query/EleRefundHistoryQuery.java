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
public class EleRefundHistoryQuery {

    /**
     * 退款单号
     */
    private String refundOrderNo;


    private Long size;
    private Long offset;


    private Long beginTime;
    private Long endTime;

    private Integer tenantId;




}
