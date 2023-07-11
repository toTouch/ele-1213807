package com.xiliulou.electricity.vo.car;

import lombok.Data;

import java.io.Serializable;

/**
 * 车辆信息
 *
 * @author xiaohui.song
 **/
@Data
public class CarVO implements Serializable {

    private static final long serialVersionUID = -6974069090885954269L;

    /**
     * 车辆<code>SN</code>码
     */
    private String carSn;

    /**
     * 所属门店名称
     */
    private String storeName;

    /**
     * 车辆经度
     */
    private Double longitude;

    /**
     * 车辆纬度
     */
    private Double latitude;

}
