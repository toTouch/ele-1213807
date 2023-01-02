package com.xiliulou.electricity.entity.clickhouse;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author : eclair
 * @date : 2023/1/2 09:30
 */
@Data
@TableName("t_car_attr")
public class CarAttr {
    
    /**
     * 设备号
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
     * 警告状态
     */
    private Integer warningMark;
    
    /**
     * 状态位
     */
    private Integer statusMark;
    
    /**
     * 海拔
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
     * 创建时间
     */
    private Timestamp createTime;
    
    /**
     * 车辆锁
     */
    private Integer doorStatus;
    
    /**
     * 车辆电路
     */
    private Integer vehicleCircuit;
    
}
