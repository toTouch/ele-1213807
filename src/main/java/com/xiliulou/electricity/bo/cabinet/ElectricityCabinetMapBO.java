package com.xiliulou.electricity.bo.cabinet;

import lombok.Data;


/**
 * @author HeYafeng
 * @description 电柜地图BO
 * @date 2024/4/23 16:32:37
 */
@Data
public class ElectricityCabinetMapBO {
    
    /**
     * 换电柜Id
     */
    private Integer id;
    
    /**
     * 换电柜名称
     */
    private String name;
    
    /**
     * 柜机地址
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
     * 物联网连接状态（0--连网，1--断网）
     */
    private Integer onlineStatus;
    
    private Integer usableStatus;
    
    /**
     * 少电多电类型：0-正常 1-少电 2-多电
     */
    private Integer batteryCountType;

    /**
     * 柜机供电类型：0--市电，1--反向供电
     */
    private Integer powerType;
}