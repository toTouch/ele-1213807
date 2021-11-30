package com.xiliulou.electricity.vo.api;

import com.xiliulou.electricity.entity.ApiOrderOperHistory;
import lombok.Data;

import java.util.List;

@Data
public class ApiReturnOrderVo {
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
     * 归还的电池编号
     */
    private String batterySn;

    /**
     * 电池类型
     */
    private String batteryType;
    /**
     * 柜门号
     */
    private Integer cellNo;

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
