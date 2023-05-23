package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.ChannelActivity;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (ChannelActivity)表服务接口
 *
 * @author zgw
 * @since 2023-03-22 10:42:56
 */
public interface ChannelActivityService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ChannelActivity queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ChannelActivity queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ChannelActivity> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param channelActivity 实例对象
     * @return 实例对象
     */
    ChannelActivity insert(ChannelActivity channelActivity);
    
    /**
     * 修改数据
     *
     * @param channelActivity 实例对象
     * @return 实例对象
     */
    Integer update(ChannelActivity channelActivity);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    Triple<Boolean, String, Object> queryList(Long offset, Long size);
    
    Triple<Boolean, String, Object> queryCount();
    
    Triple<Boolean, String, Object> updateStatus(Long id, Integer status);
    
    ChannelActivity findUsableActivity(Integer tenantId);
}
