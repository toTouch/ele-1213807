package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 *
 * @author makejava
 * @since 2022-07-20 16:00:45
 */
@Data
public class OrderSelfOpenCellQuery {

    /**
     * 换电柜id
     */
    @NotNull(message = "换电订单id不能为空!", groups = {CreateGroup.class})
    private String orderId;

    //微信公众号来源
    public static final Integer SOURCE_WX_MP = 1;
    //微信小程序来源
    public static final Integer SOURCE_WX_RPO = 2;
    //低电量换电
    public static final Integer LOW_BATTERY_ORDER = 6;

}
