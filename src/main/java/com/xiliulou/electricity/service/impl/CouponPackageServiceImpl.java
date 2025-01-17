package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.CouponPackageItemBO;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.CouponPackageMapper;
import com.xiliulou.electricity.query.CouponPackageEditQuery;
import com.xiliulou.electricity.query.CouponPackagePageQuery;
import com.xiliulou.electricity.service.CouponPackageItemService;
import com.xiliulou.electricity.service.CouponPackageService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.CouponPackageDetailsVO;
import com.xiliulou.electricity.vo.CouponPackagePageVO;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    @Resource
    private AssertPermissionService assertPermissionService;

    @Resource
    private FranchiseeService franchiseeService;


    private void checkCouponAndBuildPackageItem(List<CouponPackageEditQuery.CouponPackageItemQuery> list, Long franchiseeId, List<CouponPackageItem> itemList, AtomicReference<Integer> sumCount) {
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

            CouponPackageItem.CouponPackageItemBuilder itemBuilder = CouponPackageItem.builder().couponId(item.getCouponId())
                    .couponName(coupon.getName()).discountType(coupon.getDiscountType()).superposition(coupon.getSuperposition())
                    .days(coupon.getDays()).count(item.getCount()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis());
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

            sumCount.updateAndGet(v -> v + item.getCount());
        });
    }


    @Override
    public R addOrEdit(CouponPackageEditQuery query) {
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
        checkCouponAndBuildPackageItem(query.getItemList(), query.getFranchiseeId(), itemList, sumCount);

        if (CollUtil.isEmpty(itemList)) {
            return R.fail("402024", "优惠券为空");
        }

        CouponPackage.CouponPackageBuilder packageBuilder = CouponPackage.builder().name(query.getName()).couponCount(sumCount.get()).isCanBuy(query.getIsCanBuy()).amount(BigDecimal.valueOf(query.getAmount()))
                .userName(user.getUsername()).tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(query.getFranchiseeId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis());

        Long packageId = null;
        if (Objects.isNull(query.getId())) {
            CouponPackage couponPackage = packageBuilder.build();
            couponPackageMapper.saveCouponPackage(couponPackage);
            packageId = couponPackage.getId();
        } else {
            CouponPackage couponPackage = couponPackageMapper.selectCouponPackageById(query.getId());
            if (Objects.isNull(couponPackage)) {
                return R.fail("402025", "优惠券包不存在");
            }
            packageId = couponPackage.getId();

            CouponPackage updateCouponPackage = packageBuilder.id(couponPackage.getId()).build();
            couponPackageMapper.updateCouponPackage(updateCouponPackage);
            // 删除优惠券包下的优惠券
            packageItemService.deletePackItemByPackageId(couponPackage.getId());
        }

        // 批量insert coupon item
        Long finalPackageId = packageId;
        itemList.forEach(item -> {
            item.setPackageId(finalPackageId);
        });
        packageItemService.batchSavePackItem(itemList);

        return R.ok();
    }

    @Override
    public CouponPackageDetailsVO editEcho(Long packageId) {
        CouponPackage couponPackage = couponPackageMapper.selectCouponPackageById(packageId);
        if (Objects.isNull(couponPackage)) {
            throw new BizException("402025", "优惠券包不存在");
        }
        // CouponPackageDetailsVO
        CouponPackageDetailsVO couponPackageDetailsVO = CouponPackageDetailsVO.builder().id(couponPackage.getId()).name(couponPackage.getName()).count(couponPackage.getCouponCount())
                .franchiseeId(couponPackage.getFranchiseeId()).isCanBuy(couponPackage.getIsCanBuy()).amount(couponPackage.getAmount().doubleValue()).build();

        List<CouponPackageItem> couponPackageItemList = packageItemService.listCouponPackageItemByPackageId(packageId);
        if (CollUtil.isEmpty(couponPackageItemList)) {
            return couponPackageDetailsVO;
        }
        couponPackageDetailsVO.setItemDetailsVOList(couponPackageItemList.stream().map(item -> {
            return BeanUtil.copyProperties(item, CouponPackageDetailsVO.CouponPackageItemDetailsVO.class);
        }).collect(Collectors.toList()));

        return couponPackageDetailsVO;
    }

    @Override
    public void del(Long packageId) {
        CouponPackage couponPackage = couponPackageMapper.selectCouponPackageById(packageId);
        if (Objects.isNull(couponPackage)) {
            throw new BizException("402025", "优惠券包不存在");
        }

        couponPackageMapper.deleteCouponPackageById(packageId);
        packageItemService.deletePackItemByPackageId(packageId);
    }

    @Override
    public R pageList(CouponPackagePageQuery query) {
        Pair<Boolean, List<Long>> checkPermission = preCheckPermission();
        if (!checkPermission.getLeft()) {
            return R.ok(CollUtil.newArrayList());
        }

        query.setFranchiseeIds(checkPermission.getRight());
        query.setTenantId(TenantContextHolder.getTenantId());

        List<CouponPackage> packageList = couponPackageMapper.selectPageCouponPackage(query);
        if (CollUtil.isEmpty(packageList)) {
            return R.ok(Collections.emptyList());
        }

        // 聚合
        List<Long> packageIdList = packageList.stream().map(CouponPackage::getId).collect(Collectors.toList());
        List<CouponPackageItemBO> packageItemList = packageItemService.listCouponPackageItemByPackageIds(packageIdList);
        Map<Long, CouponPackageItemBO> packageItemBoMap = new HashMap<>(10);
        if (CollUtil.isEmpty(packageItemList)) {
            packageItemBoMap =
                    packageItemList.stream().collect(Collectors.toMap(CouponPackageItemBO::getPackageId, item -> item, (k1, k2) -> k1));
        }

        Map<Long, CouponPackageItemBO> finalPackageItemBoMap = packageItemBoMap;
        return R.ok(packageList.stream().map(item -> {
            CouponPackagePageVO vo = BeanUtil.copyProperties(item, CouponPackagePageVO.class);

            CouponPackageItemBO itemBO = finalPackageItemBoMap.get(item.getId());
            if (Objects.nonNull(itemBO)) {
                vo.setCouponNameStr(itemBO.getCouponNameStr());
                vo.setEffectStr(itemBO.getEffectStr());
            }

            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            vo.setFranchiseeName(Objects.isNull(franchisee) ? null : franchisee.getName());

            return vo;
        }).collect(Collectors.toList()));
    }


    @Override
    @Slave
    public R<Integer> pageCount(CouponPackagePageQuery query) {
        Pair<Boolean, List<Long>> checkPermission = preCheckPermission();
        if (!checkPermission.getLeft()) {
            return R.ok(NumberConstant.ZERO);
        }
        query.setFranchiseeIds(checkPermission.getRight());
        query.setTenantId(TenantContextHolder.getTenantId());
        return R.ok(couponPackageMapper.selectCountCouponPackage(query));
    }


    private Pair<Boolean, List<Long>> preCheckPermission() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return Pair.of(false, Collections.emptyList());
        }
        return assertPermissionService.assertPermissionByPair(user);
    }
}
