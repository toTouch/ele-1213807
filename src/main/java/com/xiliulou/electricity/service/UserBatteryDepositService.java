package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserBatteryDeposit;

import java.math.BigDecimal;

/**
 * (UserBatteryDeposit)表服务接口
 *
 * @author zzlong
 * @since 2022-12-06 13:40:21
 */
public interface UserBatteryDepositService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBatteryDeposit selectByUidFromDB(Long uid);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserBatteryDeposit selectByUidFromCache(Long uid);
    
    /**
     * 新增数据
     *
     * @param userBatteryDeposit 实例对象
     * @return 实例对象
     */
    Integer insert(UserBatteryDeposit userBatteryDeposit);

    UserBatteryDeposit insertOrUpdate(UserBatteryDeposit userBatteryDeposit);

    /**
     * 修改数据
     *
     * @param userBatteryDeposit 实例对象
     * @return 实例对象
     */
    Integer updateByUid(UserBatteryDeposit userBatteryDeposit);
    
    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 是否成功
     */
    Integer deleteByUid(Long uid);


    Integer logicDeleteByUid(Long uid);

    /**
     * 同步车电一体押金数据
     */
    Integer synchronizedUserBatteryDepositInfo(Long uid, Long mid, String orderId, BigDecimal batteryDeposit);

    UserBatteryDeposit queryByUid(Long uid);
}
