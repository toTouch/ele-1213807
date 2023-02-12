package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-06-18:22
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FranchiseeBatteryModelDTO {

    private Integer model;
    private BigDecimal batteryServiceFee;
    private BigDecimal batteryDeposit;

}
