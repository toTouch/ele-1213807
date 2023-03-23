package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserChannel;
import com.xiliulou.electricity.query.UserChannelQuery;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (UserChannel)表服务接口
 *
 * @author Hardy
 * @since 2023-03-22 15:34:57
 */
public interface UserChannelService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    UserChannel queryByUidFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    UserChannel queryByUidFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<UserChannel> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param userChannel 实例对象
     * @return 实例对象
     */
    UserChannel insert(UserChannel userChannel);
    
    /**
     * 修改数据
     *
     * @param userChannel 实例对象
     * @return 实例对象
     */
    Integer update(UserChannel userChannel);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    Triple<Boolean, String, Object> queryList(Long offset, Long size, String name, String phone);
    
    Triple<Boolean, String, Object> queryCount(String name, String phone);
    
    Triple<Boolean, String, Object> saveOne(Long uid);
}
