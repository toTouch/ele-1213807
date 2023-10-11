package com.xiliulou.electricity.vo.api;

import com.xiliulou.electricity.entity.ApiOrderOperHistory;
import lombok.Data;

import java.util.List;

@Data
public class ApiExchangeOrderVo {

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 柜机id
     */
    private String cabinetName;

    /**
     * 柜机deviceName
     */
    private String deviceName;

    /**
     * 柜机productkey
     */
    private String productKey;

    /**
     * 放入的电池编号
     */
    private String putBatterySn;

    /**
     * 取走的电池编号
     */
    private String takeBatterySn;

    /**
     * 电池类型
     */
    private String batteryType;

    /**
     * 放入电池的仓门
     */
    private Integer putCellNo;

    /**
     * 取走的电池编号
     */
    private Integer takeCellNo;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 订单序列号
     */
    private Double orderSeq;

    private List<ApiOrderOperHistory> operateRecords;
}
