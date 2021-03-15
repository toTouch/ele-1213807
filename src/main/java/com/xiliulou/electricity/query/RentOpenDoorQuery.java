package com.xiliulou.electricity.query;

import lombok.Data;


/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
public class RentOpenDoorQuery {

    private String orderId;
    //开门类型 1--租电池开门 2--还电池开门
    private Integer openType;

    //租电池开门
    public static final Integer RENT_OPEN_TYPE = 1;
    //还电池开门
    public static final Integer RETURN_OPEN_TYPE = 2;


}