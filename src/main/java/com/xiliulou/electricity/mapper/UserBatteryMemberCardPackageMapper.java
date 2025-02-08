package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.bo.batteryPackage.UserBatteryMemberCardPackageBO;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UserBatteryMemberCardPackage)表数据库访问层
 *
 * @author zzlong
 * @since 2023-07-12 14:44:01
 */
public interface UserBatteryMemberCardPackageMapper extends BaseMapper<UserBatteryMemberCardPackage> {

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer deleteByOrderId(String orderId);

    List<UserBatteryMemberCardPackage> selectByUid(Long uid);

    UserBatteryMemberCardPackage selectNearestByUid(Long uid);

    Integer deleteByUid(Long uid);

    UserBatteryMemberCardPackage selectByOrderNo(String orderId);

    Integer checkUserBatteryMemberCardPackageByUid(Long uid);
    
    Integer deleteChannelMemberCardByUid(@Param("uid") Long uid);
    
    List<UserBatteryMemberCardPackage> listChannelByUid(@Param("uid") Long uid);
    
    UserBatteryMemberCardPackageBO selectLastEnterprisePackageByUid(@Param("uid") Long uid);
    
    List<UserBatteryMemberCardPackageBO> selectListByUidList(@Param("uidList") List<Long> uidList, @Param("tenantId") Integer tenantId);
}
