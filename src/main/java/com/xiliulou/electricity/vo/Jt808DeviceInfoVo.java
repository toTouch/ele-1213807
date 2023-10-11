package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2022/12/29 10:26
 */
@Data
public class Jt808DeviceInfoVo {
    
    /**
     * 来源ip
     */
    private String ip;
    
    /**
     * 设备id
     */
    private String devId;
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * 警告标识位
     */
    private Integer warningMark;
    
    /**
     * 状态位标志
     */
    private Integer statusMark;
    
    /**
     * 海拔高度
     */
    private Integer high;
    
    /**
     * 速度
     */
    private Integer speed;
    
    /**
     * 方向
     */
    private Integer direction;
    
    /**
     * 车辆电路状态
     */
    private Integer doorStatus;
}
