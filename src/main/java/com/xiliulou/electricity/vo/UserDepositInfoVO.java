package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-03-02-16:40
 */
@Data
public class UserDepositInfoVO {

    private Integer batteryType;

    private Integer refundStatus;

    private BigDecimal deposit;

    private Long time;

    private String franchiseeName;

    private String store;

    private Integer rentBatteryStatus;

}
