package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.EleNewOtaUpgrade;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (EleNewOtaUpgrade)表数据库访问层
 *
 * @author Hardy
 * @since 2023-02-20 15:58:54
 */
public interface EleNewOtaUpgradeMapper extends BaseMapper<EleNewOtaUpgrade> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EleNewOtaUpgrade queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EleNewOtaUpgrade> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param eleNewOtaUpgrade 实例对象
     * @return 对象列表
     */
    List<EleNewOtaUpgrade> queryAll(EleNewOtaUpgrade eleNewOtaUpgrade);
    
    /**
     * 新增数据
     *
     * @param eleNewOtaUpgrade 实例对象
     * @return 影响行数
     */
    int insertOne(EleNewOtaUpgrade eleNewOtaUpgrade);
    
    /**
     * 修改数据
     *
     * @param eleNewOtaUpgrade 实例对象
     * @return 影响行数
     */
    int update(EleNewOtaUpgrade eleNewOtaUpgrade);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
}
