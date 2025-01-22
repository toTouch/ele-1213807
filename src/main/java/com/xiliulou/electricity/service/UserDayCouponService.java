package com.xiliulou.electricity.service;


import com.xiliulou.core.web.R;

/**
 * <p>
 * Description: This interface is UserDayCouponService!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/13
 **/
public interface UserDayCouponService {
    
    /**
     * <p>Title: useDayCoupon </p>
     * <p>Project: UserCouponService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 使用天数券</p>
     * @param userCouponId couponId 用户优惠券关联ID
     * @return com.xiliulou.core.web.R
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/13
     */
    R<?> useDayCoupon(Integer userCouponId);
}
