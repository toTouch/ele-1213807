package com.xiliulou.electricity.query;


import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
@Builder
public class ElectricityCabinetOrderOperHistoryQuery {

    /**
     * 订单编号--时间戳+柜子id+仓门号+用户id+5位随机数,20190203 21 155 1232)
     */
    private String orderId;

    private Long size;
    private Long offset;





}
