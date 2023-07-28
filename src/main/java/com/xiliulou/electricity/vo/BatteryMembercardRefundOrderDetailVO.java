package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-12-16:57
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryMembercardRefundOrderDetailVO {
    private BigDecimal payAmount;

    private BigDecimal refundAmount;

    private Long remainingNumber;

    private Long remainingTime;

    /**
     * 0:不限制,1:限制
     */
    private Integer limitCount;

    /**
     * 租期单位 0：分钟，1：天
     */
    private Integer rentUnit;
}
