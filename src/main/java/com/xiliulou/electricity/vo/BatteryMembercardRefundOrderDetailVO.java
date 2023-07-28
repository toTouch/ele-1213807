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
}
