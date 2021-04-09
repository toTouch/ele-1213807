package com.xiliulou.electricity.query;
import lombok.Data;

/**
 * 用户列表(TUserInfo)实体类
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@Data
public class BatteryReportQuery {
    /**
     * 电池sn
     */
    private String batteryName;
    /**
     * 电池电量
     */
    private Double power;
    /**
     * 地址经度
     */
    private Double longitude;
    /**
     * 地址纬度
     */
    private Double latitude;

}