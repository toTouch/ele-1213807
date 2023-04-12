package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.ChannelActivityHistory;
import com.xiliulou.electricity.vo.ChannelActivityHistoryVo;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (ChannelActivityHistory)表数据库访问层
 *
 * @author Hardy
 * @since 2023-03-23 09:24:23
 */
public interface ChannelActivityHistoryMapper extends BaseMapper<ChannelActivityHistory> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ChannelActivityHistory queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ChannelActivityHistory> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param channelActivityHistory 实例对象
     * @return 对象列表
     */
    List<ChannelActivityHistory> queryAll(ChannelActivityHistory channelActivityHistory);
    
    /**
     * 新增数据
     *
     * @param channelActivityHistory 实例对象
     * @return 影响行数
     */
    int insertOne(ChannelActivityHistory channelActivityHistory);
    
    /**
     * 修改数据
     *
     * @param channelActivityHistory 实例对象
     * @return 影响行数
     */
    int update(ChannelActivityHistory channelActivityHistory);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
    
    Long queryInviteCount(@Param("uid") Long uid);
    
    ChannelActivityHistory queryByUid(@Param("uid") Long uid);
    
    List<ChannelActivityHistoryVo> queryList(@Param("size") Long size, @Param("offset") Long offset,
            @Param("phone") String phone, @Param("tenantId") Integer tenantId, @Param("beginTime") Long beginTime,
            @Param("endTime") Long endTime);
    
    Long queryCount(@Param("phone") String phone, @Param("tenantId") Integer tenantId,
            @Param("beginTime") Long beginTime, @Param("endTime") Long endTime);
}
