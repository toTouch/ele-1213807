package com.xiliulou.electricity.vo.api;

import com.xiliulou.electricity.entity.ApiOrderOperHistory;
import lombok.Data;

import java.util.List;

@Data
public class ApiRentOrderVo {

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
     * 租借的电池编号
     */
    private String batterySn;

    /**
     * 电池类型
     */
    private String batteryType;

    /**
     * 换电柜id
     */
    private Integer eid;

    /**
     * 柜门号
     */
    private Integer cellNo;

    /**
     * 订单序列号
     */
    private Double orderSeq;

    /**
     * 订单编号
     */
    private String status;


    private List<ApiOrderOperHistory> operateRecords;
}
