package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.CouponActivityPackage;
import com.xiliulou.electricity.enums.SpecificPackagesEnum;
import com.xiliulou.electricity.mapper.CouponActivityPackageMapper;
import com.xiliulou.electricity.service.CouponActivityPackageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author: Kenneth
 * @Date: 2023/7/28 13:30
 * @Description:
 */

@Service
@Slf4j
public class CouponActivityPackageServiceImpl implements CouponActivityPackageService {

    @Autowired
    private CouponActivityPackageMapper couponActivityPackageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addCouponActivityPackage(CouponActivityPackage couponActivityPackage) {
        return couponActivityPackageMapper.insertOne(couponActivityPackage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addCouponActivityPackages(List<CouponActivityPackage> couponActivityPackageList) {
        return couponActivityPackageMapper.batchInsertCouponPackages(couponActivityPackageList);
    }

    @Slave
    @Override
    public List<CouponActivityPackage> findCouponActivityPackages(CouponActivityPackage couponActivityPackage) {
        return couponActivityPackageMapper.selectByQuery(couponActivityPackage);
    }

    @Slave
    @Override
    public List<CouponActivityPackage> findActivityPackagesByCouponId(Long couponId) {
        return couponActivityPackageMapper.selectActivityPackagesByCouponId(couponId);
    }

    @Slave
    @Override
    public List<CouponActivityPackage> findPackagesByCouponIdAndType(Long couponId, Integer packageType) {
        return couponActivityPackageMapper.selectPackagesByCouponIdAndPackageType(couponId, packageType);
    }

    /**
     * 检查集合中的优惠券是否针对当前套餐可用。有一个不满足条件，则返回false
     * @param coupons
     * @param packageId
     * @param packageType
     * @return
     */
    @Override
    public Boolean checkPackageIsValid(List<Coupon> coupons, Long packageId, Integer packageType) {

        if(CollectionUtils.isEmpty(coupons)){
            return Boolean.TRUE;
        }

        for(Coupon coupon : coupons){
            //检查优惠券的状态是否为可叠加
            if(Coupon.SUPERPOSITION_NO.equals(coupon.getSuperposition())
                    && SpecificPackagesEnum.SPECIFIC_PACKAGES_YES.getCode().equals(coupon.getSpecificPackages())){
                //如果是不可叠加，则需要判断指定的套餐是否可用
                CouponActivityPackage couponActivityPackage = couponActivityPackageMapper.selectCouponPackageByCondition(coupon.getId().longValue(), packageId, packageType);
                log.info("check the package is valid in coupon list, couponId = {}, package id = {}, package type = {}", coupon.getId(), packageId, packageType);
                if(Objects.isNull(couponActivityPackage)){
                    return Boolean.FALSE;
                }
            }
        }

        return Boolean.TRUE;
    }
}
