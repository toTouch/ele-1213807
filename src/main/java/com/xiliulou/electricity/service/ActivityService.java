package com.xiliulou.electricity.service;

import com.xiliulou.electricity.dto.ActivityProcessDTO;
import org.apache.commons.lang3.tuple.Triple;

public interface ActivityService {
    Triple<Boolean, String, Object> userActivityInfo();

    Triple<Boolean, String, Object> updateCouponByPackage(String orderNo, Integer packageType);

    Triple<Boolean, String, Object> handleActivityByPackage(ActivityProcessDTO activityProcessDTO);

    Triple<Boolean, String, Object> handleActivityByLogon(ActivityProcessDTO activityProcessDTO);

    Triple<Boolean, String, Object> handleActivityByRealName(ActivityProcessDTO activityProcessDTO);

    void asyncProcessActivity(ActivityProcessDTO activityProcessDTO);

}
