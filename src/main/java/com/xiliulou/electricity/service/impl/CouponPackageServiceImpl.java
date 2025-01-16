package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.CouponPackage;
import com.xiliulou.electricity.entity.CouponPackageItem;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.CouponPackageMapper;
import com.xiliulou.electricity.query.CouponPackageEditQuery;
import com.xiliulou.electricity.service.CouponPackageItemService;
import com.xiliulou.electricity.service.CouponPackageService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author : renhang
 * @description CouponPackageServiceImpl
 * @date : 2025-01-16 14:59
 **/
@Service
public class CouponPackageServiceImpl implements CouponPackageService {

    @Resource
    private CouponPackageMapper couponPackageMapper;

    @Resource
    private CouponService couponService;

    @Resource
    private CouponPackageItemService packageItemService;


    private void checkCouponAndBuild(List<CouponPackageEditQuery.CouponPackageItemQuery> list, Long franchiseeId, List<CouponPackageItem> itemList, AtomicReference<Integer> sumCount) {

        list.forEach(item -> {
            Coupon coupon = couponService.queryByIdFromCache(item.getCouponId().intValue());
            if (Objects.isNull(coupon)) {
                throw new BizException("ELECTRICITY.0085", "未找到优惠券，请检查");
            }

            String errorMsgName = String.format("[%s]", coupon.getName());
            if (Objects.isNull(coupon.getFranchiseeId()) || !Objects.equals(Long.valueOf(coupon.getFranchiseeId()), franchiseeId)) {
                throw new BizException("402021", errorMsgName + "优惠券加盟商不一致");
            }

            if (Objects.equals(coupon.getEnabledState(), Coupon.COUPON_UNABLE_STATUS)) {
                throw new BizException("402015", errorMsgName + "优惠券已禁用，请检查选择的优惠券状态");
            }
            if (Objects.equals(coupon.getDelFlag(), Coupon.DEL_DEL)) {
                throw new BizException("402022", errorMsgName + "优惠券已删除，请检查选择的优惠券状态");
            }
            sumCount.updateAndGet(v -> v + item.getCount());

            CouponPackageItem.CouponPackageItemBuilder itemBuilder = CouponPackageItem.builder().couponId(item.getCouponId()).couponName(coupon.getName())
                    .discountType(coupon.getDiscountType())
                    .days(coupon.getDays()).count(item.getCount()).delFlag(CouponPackageItem.DEL_NORMAL).createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis());
            if (Objects.equals(coupon.getDiscountType(), Coupon.FULL_REDUCTION)) {
                // 减免券
                itemBuilder.discount(coupon.getAmount().doubleValue()).effect(String.format("减免%s元", coupon.getAmount()));
            } else if (Objects.equals(coupon.getDiscountType(), Coupon.DAY_VOUCHER)) {
                // 天数券
                itemBuilder.discount(coupon.getCount().doubleValue()).effect(String.format("赠送%d天", coupon.getCount()));
            } else {
                throw new BizException("402023", "不支持的优惠券");
            }
            itemList.add(itemBuilder.build());
        });
    }


    @Override
    public R add(CouponPackageEditQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(query.getIsCanBuy(), CouponPackage.CAN_BUY) && Objects.isNull(query.getAmount())) {
            return R.fail("402020", "可购买时，购买金额不能为空");
        }

        // 优惠券校验（加盟商，禁用，删除）
        List<CouponPackageItem> itemList = CollUtil.newArrayList();
        AtomicReference<Integer> sumCount = new AtomicReference<>(0);
        checkCouponAndBuild(query.getItemList(), query.getFranchiseeId(), itemList, sumCount);

        if (CollUtil.isEmpty(itemList)) {
            return R.fail("402024", "优惠券为空");
        }

        CouponPackage couponPackage = CouponPackage.builder().name(query.getName()).couponCount(sumCount.get()).isCanBuy(query.getIsCanBuy()).amount(BigDecimal.valueOf(query.getAmount()))
                .userName(user.getUsername()).delFlag(CouponPackage.DEL_NORMAL).tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(query.getFranchiseeId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        couponPackageMapper.saveCouponPackage(couponPackage);

        // 批量insert coupon item
        itemList.forEach(item -> {
            item.setPackageId(couponPackage.getId());
        });
        packageItemService.batchSavePackItem(itemList);

        return R.ok();
    }

    @Override
    public R edit(CouponPackageEditQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(query.getIsCanBuy(), CouponPackage.CAN_BUY) && Objects.isNull(query.getAmount())) {
            return R.fail("402020", "可购买时，购买金额不能为空");
        }

        CouponPackage couponPackage = couponPackageMapper.selectCouponPackageById(query.getId());
        if (Objects.isNull(couponPackage)) {
            return R.fail("402025", "优惠券包不存在");
        }


        return null;
    }


}
