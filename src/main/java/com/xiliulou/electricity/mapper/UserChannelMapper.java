package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.UserChannel;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UserChannel)表数据库访问层
 *
 * @author Hardy
 * @since 2023-03-22 15:34:56
 */
public interface UserChannelMapper extends BaseMapper<UserChannel> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    UserChannel queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<UserChannel> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param userChannel 实例对象
     * @return 对象列表
     */
    List<UserChannel> queryAll(UserChannel userChannel);
    
    /**
     * 新增数据
     *
     * @param userChannel 实例对象
     * @return 影响行数
     */
    int insertOne(UserChannel userChannel);
    
    /**
     * 修改数据
     *
     * @param userChannel 实例对象
     * @return 影响行数
     */
    int update(UserChannel userChannel);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    List<UserChannel> queryList(@Param("offset") Long offset, @Param("size") Long size, @Param("name") String name,
            @Param("phone") String phone);
    
    Long queryCount(@Param("name") String name, @Param("phone") String phone);
    
    UserChannel queryByUidFromDB(@Param("uid") Long uid);
}
