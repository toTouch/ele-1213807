package com.xiliulou.electricity.web.query.battery;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2023/5/9 08:13
 */
@Data
public class BatteryInfoQuery {
    @NotNull(message = "sn不能为空")
    private String sn;

    private Integer needLocation;

    private Integer needStatus;

    public static final Integer NEED = 1;
}
