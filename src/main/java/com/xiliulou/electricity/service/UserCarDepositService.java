package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserCarDeposit;

import java.util.List;

/**
 * (UserCarDeposit)表服务接口
 *
 * @author zzlong
 * @since 2022-12-07 17:35:45
 */
public interface UserCarDepositService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserCarDeposit selectByUidFromDB(Long uid);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserCarDeposit selectByUidFromCache(Long uid);

    /**
     * 新增数据
     *
     * @param userCarDeposit 实例对象
     * @return 实例对象
     */
    UserCarDeposit insert(UserCarDeposit userCarDeposit);

    /**
     * 修改数据
     *
     * @param userCarDeposit 实例对象
     * @return 实例对象
     */
    Integer updateByUid(UserCarDeposit userCarDeposit);

    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 是否成功
     */
    Integer deleteByUid(Long uid);

}
