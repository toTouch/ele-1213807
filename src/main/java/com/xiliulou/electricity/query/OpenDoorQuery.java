package com.xiliulou.electricity.query;

import lombok.Data;


/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
public class OpenDoorQuery {

    private String orderId;
    //开门类型 1--旧仓门开门 2--新仓门开门
    private Integer openType;

    //旧仓门开门
    public static final Integer OLD_OPEN_TYPE = 1;
    //新仓门开门
    public static final Integer NEW_OPEN_TYPE = 2;


}