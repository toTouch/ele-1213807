package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserDeposit;

import java.util.List;

/**
 * (UserDeposit)表服务接口
 *
 * @author zzlong
 * @since 2022-12-06 13:40:21
 */
public interface UserDepositService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserDeposit selectByUidFromDB(Long uid);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserDeposit selectByUidFromCache(Long uid);
    
    /**
     * 新增数据
     *
     * @param userDeposit 实例对象
     * @return 实例对象
     */
    UserDeposit insert(UserDeposit userDeposit);
    
    /**
     * 修改数据
     *
     * @param userDeposit 实例对象
     * @return 实例对象
     */
    Integer updateByUid(UserDeposit userDeposit);
    
    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 是否成功
     */
    Integer deleteByUid(Long uid);
    
}
