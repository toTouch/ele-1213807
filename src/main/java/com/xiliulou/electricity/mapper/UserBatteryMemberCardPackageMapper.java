package com.xiliulou.electricity.mapper;

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
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    UserBatteryMemberCardPackage queryById(Long id);

    /**
     * 修改数据
     *
     * @param userBatteryMemberCardPackage 实例对象
     * @return 影响行数
     */
    int update(UserBatteryMemberCardPackage userBatteryMemberCardPackage);

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
}
