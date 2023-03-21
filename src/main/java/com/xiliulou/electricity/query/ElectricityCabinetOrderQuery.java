package com.xiliulou.electricity.query;


import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * (TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
@Builder
public class ElectricityCabinetOrderQuery {

    /**
     * 订单编号--时间戳+柜子id+仓门号+用户id+5位随机数,20190203 21 155 1232)
     */
    private String orderId;
    /**
     * 换电人手机号
     */
    private String phone;

    /**
     * 订单的状态
     */
    private String status;

    private Long size;
    private Long offset;

    private Long uid;

    private Long beginTime;
    private Long endTime;


    private Integer paymentMethod;

    private List<Integer> eleIdList;

    private Integer tenantId;

    private Integer source;

    private String electricityCabinetName;

    private Integer oldCellNo;
    
    private String name;

}
