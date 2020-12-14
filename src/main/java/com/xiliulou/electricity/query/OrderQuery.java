package com.xiliulou.electricity.query;
import lombok.Data;


/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
public class OrderQuery {

    /**
    * 换电柜id
    */
    private Integer electricityCabinetId;
    /**
    * 下单来源 1--微信公众号 2--小程序
    */
    private Integer source;

    //微信公众号来源
    public static final Integer SOURCE_WX_MP = 1;
    //微信小程序来源
    public static final Integer SOURCE_WX_RPO = 2;

}