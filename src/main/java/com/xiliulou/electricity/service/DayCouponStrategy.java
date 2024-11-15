package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.enums.DayCouponUseScope;
import org.apache.commons.lang3.tuple.Pair;

/**
 * <p>
 * Description: This interface is DayCouponStrategy!
 * 实现该类型各项检查接口时，无需做多余判断，仅判断对应接口邀请即可
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/13
 **/
public interface DayCouponStrategy {

    /**
     * <p>Title: getScope </p>
     * <p>Project: DayCouponStrategy</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 获取策略套餐的类型,</p>
     * @see com.xiliulou.electricity.enums.DayCouponUseScope
     * @param tenantId tenantId
     * @param uid uid
     * @return com.xiliulou.electricity.enums.DayCouponUseScope
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/14
    */
    DayCouponUseScope getScope(Integer tenantId,Long uid);
    
    /**
     * <p>Title: isLateFee </p>
     * <p>Project: DayCouponCheck</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 用户是否存在滞纳金 , true 存在</p>
     *
     * @param uid uid
     * @return boolean
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/13
     */
    boolean isLateFee(Integer tenantId,Long uid);
    
    /**
     * <p>Title: isFreeze </p>
     * <p>Project: DayCouponCheck</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 是否存在冻结或者审核中的套餐，true 存在，返回null或false，均表示不存在</p>
     * <p>left -- 是否冻结</p>
     * <p>right -- 是否冻结审核中</p>
     * @param uid uid
     * @return boolean
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/13
     */
    Pair<Boolean,Boolean> isFreezeOrAudit(Integer tenantId,Long uid);
    
    /**
     * <p>Title: isOverdue </p>
     * <p>Project: DayCouponCheck</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 套餐是否已过期，true 已过期</p>
     *
     * @param uid uid
     * @return boolean
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/13
     */
    boolean isOverdue(Integer tenantId,Long uid);
    
    /**
     * <p>Title: isReturnTheDeposit </p>
     * <p>Project: DayCouponCheck</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 是否已退押,true 已退押</p>
     *
     * @param uid uid
     * @return boolean
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/13
     */
    boolean isReturnTheDeposit(Integer tenantId,Long uid);
    
    /**
     * <p>Title: isPackageInUse </p>
     * <p>Project: DayCouponCheck</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 用户是否有使用中的套餐,true 有使用中套餐</p>
     *
     * @param uid uid
     * @return boolean
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/13
     */
    boolean isPackageInUse(Integer tenantId,Long uid);
    
    /**
     * <p>Title: process </p>
     * <p>Project: DayCouponStrategy</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 增加用户套餐天数</p>
     * <p>调用此接口时，前置校验已全部完成，仅实现增加天数逻辑即可</p>
     * <p>优惠券天数字段为 coupon.getCount() </p>
     * <p>left --  业务是否成功 </p>
     * <p>right -- 套餐id(成功必须返回) </p>
     * @param coupon coupon
     * @param uid uid
     * @return boolean
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/13
     */
    Pair<Boolean,Long> process(Coupon coupon, Integer tenantId, Long uid);
    
}
