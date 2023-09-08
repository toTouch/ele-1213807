package com.xiliulou.electricity.vo.rental;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RentalPackageRefundVO implements Serializable {

    private String orderNo;

    private Long residueCount;

    private Long residueTime;

    private BigDecimal rentPayment;


}
