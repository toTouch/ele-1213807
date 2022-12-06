package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserBattery;

import java.util.List;

/**
 * (UserBattery)表服务接口
 *
 * @author zzlong
 * @since 2022-12-06 13:39:24
 */
public interface UserBatteryService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBattery selectByUidFromDB(Long uid);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBattery selectByUidFromCache(Long uid);
    
    /**
     * 新增数据
     *
     * @param userBattery 实例对象
     * @return 实例对象
     */
    UserBattery insert(UserBattery userBattery);
    
    /**
     * 修改数据
     *
     * @param userBattery 实例对象
     * @return 实例对象
     */
    Integer updateByUid(UserBattery userBattery);
    
    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 是否成功
     */
    Integer deleteByUid(Long uid);
    
}
