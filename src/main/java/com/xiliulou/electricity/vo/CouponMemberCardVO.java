package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: CouponMemberCardVO
 * @description: 优惠券的关联的套餐信息vo
 * @author: renhang
 * @create: 2024-05-17 16:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponMemberCardVO {

    private Long id;
    
    /**
     * 套餐名称
     */
    private String name;
    
    /**
     * 优惠券id
     */
    private Long couponId;
}
