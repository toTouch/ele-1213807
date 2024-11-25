package com.xiliulou.electricity.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.electricity.entity.CouponDayRecordEntity;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * Description: This interface is CouponDayRecordService!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/14
 **/
public interface CouponDayRecordService extends IService<CouponDayRecordEntity> {
    
    /**
     * <p>Title: queryDaysByUidAndPackageId </p>
     * <p>Project: CouponDayRecordService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 根据用户UID和套餐订单号获取用户在该套餐使用天数券添加的天数,没有天数返回 0</p>
     *
     * @param tenantId  tenantId 租户id
     * @param uid       uid 用户ID
     * @param orderNo orderNo 套餐订单号
     * @param scope     scope 使用范围 {@link  com.xiliulou.electricity.enums.DayCouponUseScope}
     * @return java.lang.Integer
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/14
     */
    Integer queryDaysByUidAndPackageOrderNo(@NotNull Integer tenantId, @NotNull Long uid, @NotNull String orderNo, @NotNull Integer scope);
    
    /**
     * <p>Title: cleanDaysByUidAndPackageId </p>
     * <p>Project: CouponDayRecordService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 根据用户UID和套餐订单号退押后清除用户在该套餐使用天数券添加的天数</p>
     *
     * @param tenantId  tenantId
     * @param uid       uid
     * @param orderNo orderNo 套餐订单号
     * @param scope     scope 使用范围 {@link  com.xiliulou.electricity.enums.DayCouponUseScope}
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/14
     */
    void cleanDaysByUidAndPackageOrderNo(@NotNull Integer tenantId, @NotNull Long uid,  @NotNull String orderNo, @NotNull Integer scope);
    
}
