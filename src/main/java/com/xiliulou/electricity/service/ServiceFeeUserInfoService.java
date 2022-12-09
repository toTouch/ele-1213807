package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeVO;

import java.util.List;

/**
 * 用户停卡绑定(TServiceFeeUserInfo)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
public interface ServiceFeeUserInfoService {


    int insert(ServiceFeeUserInfo serviceFeeUserInfo);

    int update(ServiceFeeUserInfo serviceFeeUserInfo);

    ServiceFeeUserInfo queryByUidFromCache(Long uid);

    void updateByUid(ServiceFeeUserInfo serviceFeeUserInfo);

    EleBatteryServiceFeeVO queryUserBatteryServiceFee(Long uid);
}
