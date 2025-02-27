package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeVO;
import com.xiliulou.electricity.vo.UserServiceFeeDetail;
import org.apache.commons.lang3.tuple.Triple;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    
    /**
     * 调用本方法时，参数 batteryMemberCard 已不再使用，可以传null，并同步修改减少各业务内的IO查询
     * 等待其他业务修改大部分修改完成后可以将本方法的形参 BatteryMemberCard 去掉
     *
     * 注意注意注意:修改此方法时，请注意同步修改批量查询滞纳金的方法-acquireUserBatteryServiceFeeByUserList
     */
    Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFee(UserInfo userInfo, UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard, ServiceFeeUserInfo serviceFeeUserInfo);
    
    Map<Long, BigDecimal> acquireUserBatteryServiceFeeByUserList(List<UserInfo> userInfoList, List<UserBatteryMemberCard> userBatteryMemberCardList,
            List<BatteryMemberCard> batteryMemberCardList, List<ServiceFeeUserInfo> serviceFeeUserInfoList);
    
    Integer deleteByUid(Long uid);

    void unbindServiceFeeInfoByUid(Long uid);

    List<ServiceFeeUserInfo> selectDisableMembercardList(int offset, int size);

    List<UserServiceFeeDetail> selectUserBatteryServiceFee();

    BigDecimal selectBatteryServiceFeeByUid(Long uid);
    
    Integer deleteById(Long id);
    
    List<ServiceFeeUserInfo> listByUidList(List<Long> uidList);
}
