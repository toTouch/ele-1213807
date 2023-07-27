package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2023/7/19 13:23
 */
@Data
public class ElePowerVo {

    private String eName;

    private Long reportTime;

    private Long createTime;

    private Double sumPower;
    private Double hourPower;
    /**
     * 每小时电费
     */
    private Double electricCharge;
}
