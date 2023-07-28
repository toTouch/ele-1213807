package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.CouponActivityPackage;
import com.xiliulou.electricity.mapper.CouponActivityPackageMapper;
import com.xiliulou.electricity.service.CouponActivityPackageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    public List<CouponActivityPackage> findActivityPackagesByCouponId(Long couponId) {
        return couponActivityPackageMapper.selectActivityPackagesByCouponId(couponId);
    }
}
