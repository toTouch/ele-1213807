package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ShareMoneyActivityPackage;
import com.xiliulou.electricity.mapper.ShareMoneyActivityPackageMapper;
import com.xiliulou.electricity.service.ShareMoneyActivityPackageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/31 14:58
 * @Description:
 */

@Service
@Slf4j
public class ShareMoneyActivityPackageServiceImpl implements ShareMoneyActivityPackageService {

    @Autowired
    private ShareMoneyActivityPackageMapper shareMoneyActivityPackageMapper;

    @Override
    public Integer addShareMoneyActivityPackage(ShareMoneyActivityPackage shareMoneyActivityPackage) {
        return shareMoneyActivityPackageMapper.insertOne(shareMoneyActivityPackage);
    }

    @Override
    public Integer addShareMoneyActivityPackages(List<ShareMoneyActivityPackage> shareMoneyActivityPackages) {
        return shareMoneyActivityPackageMapper.batchInsertActivityPackages(shareMoneyActivityPackages);
    }

    @Override
    public List<ShareMoneyActivityPackage> findActivityPackages(ShareMoneyActivityPackage shareMoneyActivityPackage) {
        return shareMoneyActivityPackageMapper.selectByQuery(shareMoneyActivityPackage);
    }

    @Override
    public List<ShareMoneyActivityPackage> findActivityPackagesByCouponId(Long activityId) {
        return shareMoneyActivityPackageMapper.selectActivityPackagesByActivityId(activityId);
    }

    @Override
    public List<ShareMoneyActivityPackage> findPackagesByCouponIdAndType(Long activityId, Integer packageType) {
        return shareMoneyActivityPackageMapper.selectPackagesByActivityIdAndPackageType(activityId, packageType);
    }
}
