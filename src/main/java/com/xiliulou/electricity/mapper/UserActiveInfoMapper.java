package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.UserActiveInfo;
import com.xiliulou.electricity.entity.UserBattery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UserActiveInfo)表数据库访问层
 *
 * @author Hardy
 * @since 2023-03-01 10:15:10
 */
public interface UserActiveInfoMapper extends BaseMapper<UserActiveInfo> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    UserActiveInfo queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<UserActiveInfo> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param userActiveInfo 实例对象
     * @return 对象列表
     */
    List<UserActiveInfo> queryAll(UserActiveInfo userActiveInfo);
    
    /**
     * 新增数据
     *
     * @param userActiveInfo 实例对象
     * @return 影响行数
     */
    int insertOne(UserActiveInfo userActiveInfo);
    
    /**
     * 修改数据
     *
     * @param userActiveInfo 实例对象
     * @return 影响行数
     */
    int update(UserActiveInfo userActiveInfo);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    int insertOrUpdate(UserActiveInfo userActiveInfo);
    
    UserActiveInfo queryByUId(Long uid);
}
