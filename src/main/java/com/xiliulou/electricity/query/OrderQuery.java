package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;

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
    @NotNull(message = "换电柜id不能为空!", groups = {UpdateGroup.class})
    private Integer electricityCabinetId;
    /**
     * 下单来源 1--微信公众号 2--小程序
     */
    private Integer source;



    //微信公众号来源
    public static final Integer SOURCE_WX_MP = 1;
    //微信小程序来源
    public static final Integer SOURCE_WX_RPO = 2;
    //低电量换电
    public static final Integer LOW_BATTERY_ORDER = 6;

}
