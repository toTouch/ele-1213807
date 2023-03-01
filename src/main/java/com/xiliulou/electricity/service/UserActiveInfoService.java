package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.UserActiveInfo;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.UserActiveInfoQuery;

import java.util.List;

/**
 * (UserActiveInfo)表服务接口
 *
 * @author zgw
 * @since 2023-03-01 10:15:10
 */
public interface UserActiveInfoService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    UserActiveInfo queryByIdFromDB(Long id);
    
    UserActiveInfo queryByUIdFromDB(Long uid);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    UserActiveInfo queryByUIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<UserActiveInfo> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param userActiveInfo 实例对象
     * @return 实例对象
     */
    UserActiveInfo insert(UserActiveInfo userActiveInfo);
    
    /**
     * 修改数据
     *
     * @param userActiveInfo 实例对象
     * @return 实例对象
     */
    Integer update(UserActiveInfo userActiveInfo);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    UserActiveInfo insertOrUpdate(UserActiveInfo userActiveInfo);
    
    UserActiveInfo userActiveRecord(UserInfo userInfo);
    
    R queryList(UserActiveInfoQuery query);
    
    R queryCount(UserActiveInfoQuery query);
}
