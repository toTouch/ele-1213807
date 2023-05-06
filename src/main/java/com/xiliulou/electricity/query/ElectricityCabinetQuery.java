package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElectricityCabinetQuery {

    private Integer id;

    private Long size;
    private Long offset;
    /**
     * 换电柜名称
     */
    private String name;
    /**
     * 换电柜地址
     */
    private String address;
    /**
     * 地址经度
     */
    private Double longitude;
    /**
     * 地址纬度
     */
    private Double latitude;
    /**
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;
    /**
     * 物联网连接状态（0--连网，1--断网）
     */
    private Integer onlineStatus;
    private Double distance;
    private Double lon;
    private Double lat;

    private Long beginTime;
    private Long endTime;

    private Integer tenantId;

    private List<Integer> eleIdList;

    private Long franchiseeId;

    /**
     *电柜型号id
     */
    private Integer modelId;

    /**
     * 柜机编号
     */
    private String sn;


}
