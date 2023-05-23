package com.xiliulou.electricity.web.query.battery;

import lombok.Data;

@Data
public class BatteryLocationTrackQuery {
    private String sn;

    private Long beginTime;
    private Long endTime;

}
