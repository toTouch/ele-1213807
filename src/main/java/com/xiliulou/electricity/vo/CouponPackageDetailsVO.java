package com.xiliulou.electricity.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : renhang
 * @description CouponPackageDetailsVO
 * @date : 2025-01-17 10:07
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponPackageDetailsVO {


    private Long id;


    private String name;


    /**
     * 数量
     */
    private Integer count;


    private Long franchiseeId;

    /**
     * 是否可购买
     */
    private Integer isCanBuy;

    /**
     * 购买金额
     */
    private Double amount;


    private List<CouponPackageItemDetailsVO> itemDetailsVOList;


    @Data
    public static class CouponPackageItemDetailsVO {

        private Long couponId;

        private String couponName;

        /**
         * 作用
         */
        private String effect;

        /**
         * 有效天数
         */
        private Integer days;

        /**
         * 数量
         */
        private Integer count;

        /**
         * 是否可叠加
         */
        private Integer superposition;

    }
}
