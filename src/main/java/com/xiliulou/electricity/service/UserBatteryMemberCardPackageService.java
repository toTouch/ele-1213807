package com.xiliulou.electricity.service;

import com.xiliulou.electricity.bo.batteryPackage.UserBatteryMemberCardPackageBO;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserInfo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (UserBatteryMemberCardPackage)表服务接口
 *
 * @author zzlong
 * @since 2023-07-12 14:44:01
 */
public interface UserBatteryMemberCardPackageService {

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Integer insert(UserBatteryMemberCardPackage userBatteryMemberCardPackage);

    Integer deleteByOrderId(String orderId);

    List<UserBatteryMemberCardPackage> selectByUid(Long uid);

    UserBatteryMemberCardPackage selectNearestByUid(Long uid);

    void handlerTransferBatteryMemberCardPackage();

    Triple<Boolean, String, Object> batteryMembercardTransform(Long uid);

    Integer deleteByUid(Long uid);

    UserBatteryMemberCardPackage selectByOrderNo(String orderId);

    Integer checkUserBatteryMemberCardPackageByUid(Long uid);
    
    Integer deleteChannelMemberCardByUid(Long uid);
    
    List<UserBatteryMemberCardPackage> queryChannelListByUid(Long uid);
    
    UserBatteryMemberCardPackageBO queryEnterprisePackageByUid(Long uid);
    
    List<UserBatteryMemberCardPackageBO> listByUidList(List<Long> uidList, Integer tenantId);
}
