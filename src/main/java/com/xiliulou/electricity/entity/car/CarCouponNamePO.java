package com.xiliulou.electricity.entity.car;


import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * Description: This class is CarCouponNamePO!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/4/12
 **/
@Data
public class CarCouponNamePO {
    
    /**
     * <p>
     * Description: 主键
     * </p>
     */
    private Long id;
    
    /**
     * <p>
     * Description: 优惠劵名称
     * </p>
     */
    private String name;
    
    /**
     * <p>
     * Description: 类型1--减免券，2--折扣券，3--天数券
     * </p>
     */
    private Integer discountType;
    
    /**
     * <p>
     * Description: 优惠券金额
     * </p>
     */
    private BigDecimal amount;
    
    /**
     * <p>
     * Description: 天数
     * </p>
     */
    private Integer count;
}
