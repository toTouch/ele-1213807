package com.xiliulou.electricity.query;
import lombok.Data;


/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
public class ReturnBatteryQuery {

    /**
    * 换电柜id
    */
    private Integer electricityCabinetId;

}