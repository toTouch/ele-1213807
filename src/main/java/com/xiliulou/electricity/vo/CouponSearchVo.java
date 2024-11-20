package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @Author SongJinpan
 * @description:
 * @Date 2024/4/9 15:56
 */

@Data
public class CouponSearchVo {
    
    private Long id;
    
    /**
     * 优惠券名称
     */
    private String name;
    
    /**
     * 优惠金额
     */
    private BigDecimal amount;
    
    /**
     * 优惠类型，1--减免券，2--打折券，3-天数劵
     */
    private Integer discountType;
}
