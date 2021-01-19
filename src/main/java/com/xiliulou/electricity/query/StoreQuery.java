package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
public class StoreQuery {
    private Long size;
    private Long offset;
    /**
     * 门店名称
     */
    private String name;
    /**
     * 门店地区Id
     */
    private Integer areaId;
    private Long beginTime;
    private Long endTime;
    private Double distance;
    private Double lon;
    private Double lat;
    /**
     * 租电池服务(0--支持，1--不支持)
     */
    private Integer batteryService;
    /**
     * 租车服务(0--支持，1--不支持)
     */
    private Integer carService;
    /**
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;
    /**
     * 门店账号
     */
    private String sn;
    /**
     * 门店地址
     */
    private String address;
}
