package com.xiliulou.electricity.dto;


import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author : renhang
 * @description IsSupportFreeServiceFeeDTO
 * @date : 2025-03-27 14:51
 **/
@Data
@Accessors(chain = true)
public class IsSupportFreeServiceFeeDTO {

    private Boolean supportFreeServiceFee;

    private BigDecimal freeServiceFee;
}
