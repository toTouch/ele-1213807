package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.BoxOtherProperties;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 换电柜仓门其它属性(BoxOtherProperties)表数据库访问层
 *
 * @author zzlong
 * @since 2022-11-03 19:51:35
 */
public interface BoxOtherPropertiesMapper extends BaseMapper<BoxOtherProperties> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BoxOtherProperties selectById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<BoxOtherProperties> selectByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param boxOtherProperties 实例对象
     * @return 对象列表
     */
    List<BoxOtherProperties> selectByQuery(BoxOtherProperties boxOtherProperties);
    
    /**
     * 新增数据
     *
     * @param boxOtherProperties 实例对象
     * @return 影响行数
     */
    int insertOne(BoxOtherProperties boxOtherProperties);
    
    /**
     * 修改数据
     *
     * @param boxOtherProperties 实例对象
     * @return 影响行数
     */
    int update(BoxOtherProperties boxOtherProperties);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
}
