package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;


/**
 * @author: hrp
 * @Date: 2022/6/7 10:02
 * @Description:
 */
@Data
@Builder
public class ElectricityCarQuery {


    private Long size;
    private Long offset;
    /**
     * 车辆sn码
     */
    private String sn;
    /**
     * 车辆型号
     */
    private String model;
    /**
     * 车辆状态 0--空闲 1--租借
     */
    private Integer status;
    /**
     * 门店Id
     */
    private Long storeId;
    /**
     * 加盟商Id
     */
    private List<Long> franchiseeIds;
    /**
     * 手机号
     */
    private String Phone;

    /**
     * 电池sn码
     */
    private String batterySn;

    private Long beginTime;
    private Long endTime;

    private Integer tenantId;

    private List<Long> storeIds;
}
