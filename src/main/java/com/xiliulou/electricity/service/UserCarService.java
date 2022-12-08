package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserCar;

import java.util.List;

/**
 * (UserCar)表服务接口
 *
 * @author zzlong
 * @since 2022-12-07 17:35:15
 */
public interface UserCarService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserCar selectByUidFromDB(Long uid);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserCar selectByUidFromCache(Long uid);

    /**
     * 新增数据
     *
     * @param userCar 实例对象
     * @return 实例对象
     */
    UserCar insert(UserCar userCar);

    /**
     * 修改数据
     *
     * @param userCar 实例对象
     * @return 实例对象
     */
    Integer updateByUid(UserCar userCar);

    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 是否成功
     */
    Integer deleteByUid(Long uid);

}
