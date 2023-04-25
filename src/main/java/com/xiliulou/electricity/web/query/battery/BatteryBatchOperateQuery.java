package com.xiliulou.electricity.web.query.battery;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2023/4/4 21:40
 */
@Data
public class BatteryBatchOperateQuery {

    @NotNull(message = "sn不能为空")
    private String jsonBatterySnList;

}
