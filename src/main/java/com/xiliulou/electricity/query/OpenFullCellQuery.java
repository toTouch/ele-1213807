package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author makejava
 * @since 2022-07-20 16:00:45
 */
@Data
public class OpenFullCellQuery {


    /**
     * 订单id
     */
    @NotNull(message = "换电订单id不能为空!")
    private String orderId;
    

}
