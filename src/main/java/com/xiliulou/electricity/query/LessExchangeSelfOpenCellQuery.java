package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @ClassName: LessExchangeSelfOpenCellQuery
 * @description:
 * @author: renhang
 * @create: 2024-07-22 15:01
 */
@Data
public class LessExchangeSelfOpenCellQuery {
    
    @NotEmpty(message = "订单号不能为空")
    private String orderId;
    
    /**
     * 柜机
     */
    @NotNull(message = "换电柜不能为空!")
    private Integer eid;
    
    
    /**
     * 仓门
     */
    @NotNull(message = "仓门不能为空!")
    private Integer cellNo;
    
}
