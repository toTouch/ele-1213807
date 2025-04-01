package com.xiliulou.electricity.vo;


import lombok.Builder;
import lombok.Data;

/**
 * @author : renhang
 * @description UserFreeServiceFeeStatusVO
 * @date : 2025-04-01 16:55
 **/
@Data
@Builder
public class UserFreeServiceFeeStatusVO {

    /**
     * 电免押服务费
     * 1已经支付，0未支付
     */
    private Integer batteryFreeServiceFeeStatus;

    /**
     * 车免押服务费
     * 1已经支付，0未支付
     */
    private Integer carFreeServiceFeeStatus;

}
