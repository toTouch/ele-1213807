package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.UserNotify;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UserNotify)表数据库访问层
 *
 * @author zgw
 * @since 2023-02-21 09:10:40
 */
public interface UserNotifyMapper extends BaseMapper<UserNotify> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    UserNotify queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<UserNotify> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param userNotify 实例对象
     * @return 对象列表
     */
    List<UserNotify> queryAll(UserNotify userNotify);
    
    /**
     * 新增数据
     *
     * @param userNotify 实例对象
     * @return 影响行数
     */
    int insertOne(UserNotify userNotify);
    
    /**
     * 修改数据
     *
     * @param userNotify 实例对象
     * @return 影响行数
     */
    int update(UserNotify userNotify);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    UserNotify queryByTenantId(Integer tenantId);
}
