package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author Hardy
 * @date 2022/08/11 16:50
 * @mood
 */
@Data
public class BatteryStatisticalVo {


    private Integer sumCount;

    private Integer wareHouseCount;

    private Integer stockCount;

    private Integer leaseCount;

    private Integer exceptionFreeCount;

    private Integer exceptionCount;

}
