package com.xiliulou.electricity.vo.activity;


import lombok.Data;

/**
 * <p>
 * Description: This class is CouponNameVO!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/13
 **/
@Data
public class CouponNameVO {
    
    private Integer id;
    
    /**
     * 优惠券名称
     */
    private String name;
    
    /**
     * 优惠类型，1--减免券，2--打折券，3-体验劵
     */
    private Integer discountType;
}
