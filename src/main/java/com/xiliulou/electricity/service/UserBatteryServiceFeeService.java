package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserBatteryServiceFee;

import java.util.List;

/**
 * (UserBatteryServiceFee)表服务接口
 *
 * @author zzlong
 * @since 2022-12-06 13:39:51
 */
public interface UserBatteryServiceFeeService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBatteryServiceFee selectByUidFromDB(Long uid);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBatteryServiceFee selectByUidFromCache(Long uid);
    
    /**
     * 新增数据
     *
     * @param userBatteryServiceFee 实例对象
     * @return 实例对象
     */
    UserBatteryServiceFee insert(UserBatteryServiceFee userBatteryServiceFee);

    UserBatteryServiceFee insertOrUpdate(UserBatteryServiceFee userBatteryServiceFee);

    /**
     * 修改数据
     *
     * @param userBatteryServiceFee 实例对象
     * @return 实例对象
     */
    Integer updateByUid(UserBatteryServiceFee userBatteryServiceFee);
    
    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 是否成功
     */
    Integer deleteByUid(Long uid);
    
}
