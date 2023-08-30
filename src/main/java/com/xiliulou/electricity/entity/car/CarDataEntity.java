package com.xiliulou.electricity.entity.car;

import lombok.Data;

/**
 * 车辆数据模型
 */
@Data
public class CarDataEntity {
    private Long uid; // userID
    private String carSn;  // 车辆序列号
    private String model;  // 车辆型号
    private String franchiseeName;  // 加盟商名称
    private String storeName;  // 门店名称
    private String userName;  // 用户名称
    private String phone;  // 用户电话
    private String carStatus;  // 车辆状态
    private Long updateTime;  // 更新时间
    private Long createTime;  // 创建时间
    private Long dueTime; // 套餐到期时间
    private double longitude;
    private double latitude;
}
