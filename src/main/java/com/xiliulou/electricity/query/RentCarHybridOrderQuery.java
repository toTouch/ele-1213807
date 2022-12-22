package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class RentCarHybridOrderQuery {
    @NotNull(message = "门店不能为空!")
    private Long storeId;

    @NotNull(message = "车辆型号不能为空!")
    private Long carModelId;

    @NotNull(message = "车辆租赁时间不能为空!")
    private Integer rentTime;

    private Integer insuranceId;

    private Long franchiseeId;

    private Integer model;

    private Integer memberCardId;

    private Integer userCouponId;
}
