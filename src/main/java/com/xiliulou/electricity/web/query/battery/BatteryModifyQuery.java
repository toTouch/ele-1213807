package com.xiliulou.electricity.web.query.battery;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2023/4/4 21:40
 */
@Data
public class BatteryModifyQuery {

    @NotNull(message = "原始SN不能为空")
    private String originalSn;

    @NotNull(message = "新SN不能为空")
    private String newSn;


}
