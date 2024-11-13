package com.xiliulou.electricity.service.impl;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.enums.UserCouponStatus;
import com.xiliulou.electricity.factory.coupon.UserDayCouponStrategyFactory;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.DayCouponStrategy;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserDayCouponService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <p>
 * Description: This class is UserDayCouponServiceImpl!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/13
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDayCouponServiceImpl implements UserDayCouponService {
    
    private final UserDayCouponStrategyFactory userDayCouponStrategyFactory;
    
    private final UserCouponService userCouponService;
    
    private final CouponService couponService;
    
    @Override
    public R<?> useDayCoupon(Integer couponId) {
        if (Objects.isNull(couponId)){
            return R.failMsg("请选择正确的优惠券使用");
        }
        
        //判断用户相关
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (Objects.isNull(userInfo)){
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //判断优惠券相关
        UserCoupon userCoupon = userCouponService.queryByIdFromDB(couponId);
        if (Objects.isNull(userCoupon)){
            return R.failMsg("请选择正确的优惠券使用");
        }
        if (!Objects.equals(userCoupon.getStatus(),UserCoupon.STATUS_UNUSED)){
            return R.failMsg(String.format("该优惠券%s,请重新加载信息", UserCouponStatus.getUserCouponStatus(userCoupon.getStatus()).getDesc()));
        }
        if(userCoupon.getDeadline() < System.currentTimeMillis()){
            return R.failMsg("该优惠券已过期");
        }
        if (!Objects.equals(userCoupon.getDiscountType(), UserCoupon.DAYS)){
            return R.failMsg("该优惠券非天数券，请选择其他优惠券");
        }
        
        //判断套餐相关
        Long uid = userInfo.getUid();
        DayCouponStrategy strategy = userDayCouponStrategyFactory.getDayCouponStrategy(uid);
        if (Objects.isNull(strategy)){
            return R.failMsg("请先购买换电/租车/车电一体套餐后使用");
        }
        if (strategy.isLateFee(uid)){
            return R.failMsg("您有未缴纳的滞纳金，暂无法使用，请缴纳后使用");
        }
        Pair<Boolean, Boolean> freezeOrAudit = strategy.isFreezeOrAudit(uid);
        if (Objects.nonNull(freezeOrAudit)){
            if (Objects.nonNull(freezeOrAudit.getLeft()) && freezeOrAudit.getLeft()){
                return R.failMsg("您当前套餐已冻结，暂无法使用，请启用后使用");
            }
            if (Objects.nonNull(freezeOrAudit.getLeft()) && freezeOrAudit.getRight()){
                return R.failMsg("您有在申请的冻结套餐，暂无法使用，请启用后使用");
            }
        }
        if (strategy.isOverdue(uid)){
            return R.failMsg("您当前套餐已过期，请先购买换电/租车/车电一体套餐后使用");
        }
        if (strategy.isReturnTheDeposit(uid)){
            return R.failMsg("您已退押，暂无法使用，请缴纳押金后使用");
        }
        
        Coupon coupon = couponService.queryByIdFromDB(userCoupon.getCouponId());
        
        if (Objects.isNull(coupon)){
            return R.failMsg("请选择正确的优惠券使用");
        }
        
        return strategy.process(coupon, uid) ? R.ok() : R.failMsg("优惠券使用失败,请刷新后重试");
    }
}
