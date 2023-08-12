package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeVO;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户停卡绑定(TServiceFeeUserInfo)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
public interface ServiceFeeUserInfoService {

    int insert(ServiceFeeUserInfo serviceFeeUserInfo);

    ServiceFeeUserInfo queryByUidFromCache(Long uid);

    void updateByUid(ServiceFeeUserInfo serviceFeeUserInfo);

    EleBatteryServiceFeeVO queryUserBatteryServiceFee(Long uid);
    
    BigDecimal queryUserBatteryServiceFee(UserInfo userInfo);

    Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFee(UserInfo userInfo, UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard, ServiceFeeUserInfo serviceFeeUserInfo);

    Integer deleteByUid(Long uid);
}
