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
public class OpenDoorQuery {

    private String orderId;


}