package com.xiliulou.electricity.query;
import com.xiliulou.electricity.entity.BatteryOtherProperties;
import lombok.Data;

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

    private Boolean hasOtherAttr;

    private BatteryOtherProperties batteryAttr;

}
