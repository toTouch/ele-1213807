package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.OtaFileConfig;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (OtaFileConfig)表数据库访问层
 *
 * @author Hardy
 * @since 2022-10-12 09:24:47
 */
public interface OtaFileConfigMapper extends BaseMapper<OtaFileConfig> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    OtaFileConfig queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<OtaFileConfig> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     * @return 对象列表
     */
    List<OtaFileConfig> queryAll();
    
    /**
     * 新增数据
     *
     * @param otaFileConfig 实例对象
     * @return 影响行数
     */
    int insertOne(OtaFileConfig otaFileConfig);
    
    /**
     * 修改数据
     *
     * @param otaFileConfig 实例对象
     * @return 影响行数
     */
    int update(OtaFileConfig otaFileConfig);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    OtaFileConfig queryByType(Integer type);
    
    Integer insertOrupdate(OtaFileConfig otaFileConfig);
}
