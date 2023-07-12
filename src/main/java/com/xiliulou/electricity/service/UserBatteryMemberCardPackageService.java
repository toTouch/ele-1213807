package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;

import java.util.List;

/**
 * (UserBatteryMemberCardPackage)表服务接口
 *
 * @author zzlong
 * @since 2023-07-12 14:44:01
 */
public interface UserBatteryMemberCardPackageService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    UserBatteryMemberCardPackage queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    UserBatteryMemberCardPackage queryByIdFromCache(Long id);

    /**
     * 修改数据
     *
     * @param userBatteryMemberCardPackage 实例对象
     * @return 实例对象
     */
    Integer update(UserBatteryMemberCardPackage userBatteryMemberCardPackage);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Integer insert(UserBatteryMemberCardPackage userBatteryMemberCardPackage);
}
