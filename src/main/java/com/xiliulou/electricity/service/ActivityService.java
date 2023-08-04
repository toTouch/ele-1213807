package com.xiliulou.electricity.service;

import org.apache.commons.lang3.tuple.Triple;

public interface ActivityService {
    Triple<Boolean, String, Object> userActivityInfo();

    Triple<Boolean, String, Object> updateCouponByPackage(String orderNo, Integer packageType);

    Triple<Boolean, String, Object> handleActivityByPackage(String orderNo, Integer packageType);

    Triple<Boolean, String, Object> handleActivityByLogon(Long uid);

    Triple<Boolean, String, Object> handleActivityByRealName(Long uid);

}
