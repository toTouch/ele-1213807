package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ShareMoneyActivityPackage;

import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/31 14:55
 * @Description:
 */
public interface ShareMoneyActivityPackageService {

    Integer addShareMoneyActivityPackage(ShareMoneyActivityPackage shareMoneyActivityPackage);


    Integer addShareMoneyActivityPackages(List<ShareMoneyActivityPackage> shareMoneyActivityPackages);

    List<ShareMoneyActivityPackage> findActivityPackages(ShareMoneyActivityPackage shareMoneyActivityPackage);

    List<ShareMoneyActivityPackage> findActivityPackagesByCouponId(Long activityId);

    List<ShareMoneyActivityPackage> findPackagesByCouponIdAndType(Long activityId, Integer packageType);

}
