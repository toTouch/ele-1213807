package com.xiliulou.electricity.domain.car;

import lombok.Data;

import java.io.Serializable;

/**
 * 车辆领域模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarInfoDO implements Serializable {

    private static final long serialVersionUID = -4836742971455403056L;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 车辆ID
     */
    private Integer carId;

    /**
     * 车辆SN码
     */
    private String carSn;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;



}
