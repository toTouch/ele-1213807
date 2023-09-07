package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserAuthMessage;

import java.util.List;

/**
 * (UserAuthMessage)表服务接口
 *
 * @author zzlong
 * @since 2023-09-05 14:36:03
 */
public interface UserAuthMessageService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    UserAuthMessage queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    UserAuthMessage queryByIdFromCache(Long id);

    /**
     * 修改数据
     *
     * @param userAuthMessage 实例对象
     * @return 实例对象
     */
    Integer update(UserAuthMessage userAuthMessage);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Integer insert(UserAuthMessage userAuthMessage);

    UserAuthMessage selectLatestByUid(Long uid);
}
