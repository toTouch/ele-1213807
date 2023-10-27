package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 选仓换电请求类
 *
 * @author zhangyongbo
 * @since 2023-10-13
 */
@Data
public class OrderSelectionExchangeQuery {

    /**
     * 换电柜id
     */
    @NotNull(message = "换电柜id不能为空!")
    private Integer eid;
    
    /**
     * 换电类型 2--正常换电  3--离线换电 4--蓝牙换电  5--选仓换电
     */
    private Integer source;
    
    /**
     * 选择的仓门编号
     */
    @NotNull(message = "选择的仓门不能为空!")
    private Integer selectionCellNo;
    
}
