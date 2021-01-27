package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
public class ElectricityCabinetQuery {
    private Long size;
    private Long offset;
    /**
     * 电池编号
     */
    private String sn;
    /**
     * 换电柜名称
     */
    private String name;
    /**
     * 换电柜地址
     */
    private String address;
    /**
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;
    /**
     * 电源状态(0--通电，1--断电)
     */
    private Integer powerStatus;
    /**
     * 物联网连接状态（0--连网，1--断网）
     */
    private Integer onlineStatus;
    private Double distance;
    private Double lon;
    private Double lat;

    private Long beginTime;
    private Long endTime;

    private List<Integer> storeIdList;

    private List<Long> uidList;
}
