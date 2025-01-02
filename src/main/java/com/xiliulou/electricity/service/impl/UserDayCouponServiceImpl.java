package com.xiliulou.electricity.service.impl;


import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.CouponDayRecordEntity;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.DayCouponUseScope;
import com.xiliulou.electricity.enums.UserCouponStatus;
import com.xiliulou.electricity.factory.coupon.UserDayCouponStrategyFactory;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.xiliulou.electricity.constant.CacheConstant.LOCK_USER_DAY_COUPON_USE_SCOPE;

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
    
    private final RedisService redisService;
    
    private final CouponDayRecordService couponDayRecordService;

    private final UserInfoService userInfoService;
    
    @Override
    public R<?> useDayCoupon(Integer couponId) {
        if (Objects.isNull(couponId)){
            return R.fail("400001","请选择正确的优惠券使用");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        if (Objects.isNull(tenantId)){
            return R.fail("400002","暂无使用权限");
        }
        
        //判断用户相关
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (Objects.isNull(userInfo)){
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo info = userInfoService.queryByUidFromDB(userInfo.getUid());

        if (Objects.isNull(info)){
            return R.fail("ELECTRICITY.0001","未找到用户");
        }

        if (Objects.equals(UserInfo.USER_UN_USABLE_STATUS, info.getUsableStatus())) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }


        String lockKey = String.format(LOCK_USER_DAY_COUPON_USE_SCOPE, tenantId, userInfo.getUid(),couponId);
        if (!redisService.setNx(lockKey, "1" , 5 * 1000L, false)){
            return R.fail("400003","系统繁忙，请稍后再试");
        }
        try {
            //判断优惠券相关
            UserCoupon userCoupon = userCouponService.queryByIdFromDB(couponId);
            if (Objects.isNull(userCoupon)) {
                return R.fail("400001","请选择正确的优惠券使用");
            }

            if (!Objects.equals(userCoupon.getStatus(), UserCoupon.STATUS_UNUSED)) {
                return R.fail("400005",String.format("该优惠券%s,请重新加载信息", UserCouponStatus.getUserCouponStatus(userCoupon.getStatus()).getDesc()));
            }
            if (userCoupon.getDeadline() < System.currentTimeMillis()) {
                return R.fail("400004","该优惠券已过期");
            }
            if (!Objects.equals(userCoupon.getDiscountType(), UserCoupon.DAYS)) {
                return R.fail("400006","该优惠券非天数券，请选择其他优惠券");
            }

            if (!Objects.equals(Long.valueOf(userCoupon.getFranchiseeId()), info.getFranchiseeId())) {
                return R.fail("402031", "加盟商不一致");
            }
            
            Coupon coupon = couponService.queryByIdFromDB(userCoupon.getCouponId());
            if (Objects.isNull(coupon)) {
                return R.fail("400007","请选择正确的优惠券使用");
            }
            DayCouponUseScope useScope = DayCouponUseScope.getByCode(coupon.getUseScope());
            //判断套餐相关
            Long uid = userInfo.getUid();
            DayCouponStrategy strategy = userDayCouponStrategyFactory.getDayCouponStrategy(useScope,tenantId, uid);
            if (Objects.isNull(strategy)) {
                return R.fail("400008",String.format("请先购买%s套餐后使用", useScope.getDesc()));
            }
            
            if (strategy.isReturnThePackage(tenantId, uid)) {
                return R.fail("400016","您退租正在审核中，暂无法使用");
            }

            Pair<Boolean, Boolean> depositPair = strategy.isReturnTheDeposit(tenantId, uid);
            if (Objects.nonNull(depositPair)) {
                if (Objects.nonNull(depositPair.getRight()) && depositPair.getRight()) {
                    return R.fail("400015","您退押正在审核中，暂无法使用");
                }

                if (Objects.nonNull(depositPair.getLeft()) && depositPair.getLeft()) {
                    return R.fail("400009","您已退押，暂无法使用，请缴纳押金后使用");
                }
            }
            
            if (strategy.isLateFee(tenantId, uid)) {
                return R.fail("400010","您有未缴纳的滞纳金，暂无法使用，请缴纳后使用");
            }
            Pair<Boolean, Boolean> freezeOrAudit = strategy.isFreezeOrAudit(tenantId, uid);
            if (Objects.nonNull(freezeOrAudit)) {
                if (Objects.nonNull(freezeOrAudit.getLeft()) && freezeOrAudit.getLeft()) {
                    return R.fail("400011","您当前套餐已冻结，暂无法使用，请启用后使用");
                }
                if (Objects.nonNull(freezeOrAudit.getRight()) && freezeOrAudit.getRight()) {
                    return R.fail("400012","您有在申请的冻结套餐，暂无法使用，请启用后使用");
                }
            }
            if (strategy.isOverdue(tenantId, uid)) {
                return R.fail("400013",String.format("您当前套餐已过期，请先购买%s套餐后使用", useScope.getDesc()));
            }

            if (!strategy.isPackageInUse(tenantId, uid)) {
                return R.fail("400008",String.format("请先购买%s套餐后使用", useScope.getDesc()));
            }
            
            Triple<Boolean,Long ,String> processed = strategy.process(coupon, tenantId, uid);
            
            if (Objects.isNull(processed) || !processed.getLeft()) {
                return R.fail("400014","优惠券使用失败");
            }
            
            //更新优惠券状态
            UserCoupon update = UserCoupon.builder().id(userCoupon.getId()).status(UserCoupon.STATUS_USED).updateTime(System.currentTimeMillis()).tenantId(tenantId)
                    .orderId(processed.getRight()).build();
            userCouponService.updateStatus(update);
            //保存天数券记录
            CouponDayRecordEntity entity = new CouponDayRecordEntity();
            entity.setCouponId(Long.valueOf(coupon.getId()));
            entity.setTenantId(Long.valueOf(tenantId));
            entity.setDays(coupon.getCount());
            entity.setCreateTime(System.currentTimeMillis());
            entity.setUseScope(coupon.getUseScope());
            entity.setUid(uid);
            entity.setDelFlag(CouponDayRecordEntity.DEL_NORMAL);
            entity.setPackageOrder(processed.getRight());
            entity.setPackageId(processed.getMiddle());
            couponDayRecordService.save(entity);
            return R.ok();
        }finally {
            redisService.remove(lockKey);
        }
    }
}
