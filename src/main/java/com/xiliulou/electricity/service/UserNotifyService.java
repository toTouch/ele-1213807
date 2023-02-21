package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserNotify;
import com.xiliulou.electricity.query.UserNotifyQuery;

import java.util.List;

/**
 * (UserNotify)表服务接口
 *
 * @author zgw
 * @since 2023-02-21 09:10:41
 */
public interface UserNotifyService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    UserNotify queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    UserNotify queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<UserNotify> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param userNotify 实例对象
     * @return 实例对象
     */
    UserNotify insert(UserNotify userNotify);
    
    /**
     * 修改数据
     *
     * @param userNotify 实例对象
     * @return 实例对象
     */
    Integer update(UserNotify userNotify);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    UserNotify queryByTenantId();
    
    R deleteOne(Long id);
    
    R editOne(UserNotifyQuery userNotifyQuery);
}
