package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.ChannelActivity;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (ChannelActivity)表数据库访问层
 *
 * @author zgw
 * @since 2023-03-22 10:42:56
 */
public interface ChannelActivityMapper extends BaseMapper<ChannelActivity> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ChannelActivity queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ChannelActivity> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param channelActivity 实例对象
     * @return 对象列表
     */
    List<ChannelActivity> queryAll(ChannelActivity channelActivity);
    
    /**
     * 新增数据
     *
     * @param channelActivity 实例对象
     * @return 影响行数
     */
    int insertOne(ChannelActivity channelActivity);
    
    /**
     * 修改数据
     *
     * @param channelActivity 实例对象
     * @return 影响行数
     */
    int update(ChannelActivity channelActivity);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    List<ChannelActivity> queryList(@Param("offset") Long offset, @Param("size") Long size,
            @Param("tenantId") Integer tenantId);
}
