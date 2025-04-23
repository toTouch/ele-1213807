package com.xiliulou.electricity.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author : renhang
 * @description FreeServiceFeeInfoVO
 * @date : 2025-03-27 13:45
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FreeServiceFeeInfoVO {

    private Integer freeServiceFeeSwitch;

    private BigDecimal freeServiceFee;
}
