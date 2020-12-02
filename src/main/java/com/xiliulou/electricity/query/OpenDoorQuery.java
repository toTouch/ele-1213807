package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
@Builder
public class OpenDoorQuery {

    private String orderId;
    //开门类型 1--旧仓门开门 2--新仓门开门
    private Integer openType;

    //微信公众号来源
    public static final Integer OLD_OPEN_TYPE = 1;
    //微信小程序来源
    public static final Integer NEW_OPEN_TYPE = 2;


}