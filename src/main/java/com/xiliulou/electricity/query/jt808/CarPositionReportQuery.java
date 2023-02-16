package com.xiliulou.electricity.query.jt808;

import lombok.Data;

/**
 * @author zgw
 * @date 2023/2/16 11:22
 * @mood
 */
@Data
public class CarPositionReportQuery {
    
    /**
     * 车辆Sn
     */
    private String devId;
    
    /**
     * 地址经度
     */
    private Double longitude;
    
    /**
     * 地址纬度
     */
    private Double latitude;
    
    /**
     * 请求id
     */
    private String requestId;
}
