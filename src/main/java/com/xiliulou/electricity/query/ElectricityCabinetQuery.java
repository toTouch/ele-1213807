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

    private String sn;
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
    
    private List<Integer> tenantIdList;

    private List<Integer> eleIdList;

    private Long franchiseeId;

    private Integer lockCellFlag;

    /**
     *电柜型号id
     */
    private Integer modelId;

    private Long storeId;
    
    /**
     * 供电类型
     */
    private Integer powerType;
    
    private Integer usableStatusCell;

    private Double fullChargeRate;
    private Double lowChargeRate;
    
    /**
     * 库存状态；0,库存；1,已出库
     */
    private Integer stockStatus;
    
    /**
     * 库房id
     */
    private Long warehouseId;

    /**
     * 加盟商ID集
     */
    private List<Long> franchiseeIdList;

    /**
     * 门店ID集
     */
    private List<Long> storeIdList;
    
    /**
     * 统计时间
     */
    private Long statisticDate;
    
    /**
     * 排序字段averageNumber
     */
    private Integer orderByAverageNumber;
    
    /**
     * 排序字段averageActivity
     */
    private Integer orderByAverageActivity;
    
    /**
     * 排序字段todayNumber
     */
    private Integer orderByTodayNumber;
    
    /**
     * 排序字段todayActivity
     */
    private Integer orderByTodayActivity;
    
    /**
     * 柜机状态 0-全部 1-少电（默认） 2-多电 3-锁仓（只要有一个仓被锁就算锁仓） 4-离线
     */
    private Integer status;
    
    /**
     * 区域ID
     */
    private Long areaId;
    
    /**
     * 柜机id集合
     */
    private List<Integer> idList;
    
    /**
     * 少电/多电类型：0-正常 1-少电 2-多电
     */
    private Integer batteryCountType;
    
    
    private String productKey;
    
    private String deviceName;
    
    private String version;
    
    private Integer pattern;
}
