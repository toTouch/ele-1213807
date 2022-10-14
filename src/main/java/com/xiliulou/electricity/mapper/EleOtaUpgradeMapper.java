package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.EleOtaUpgrade;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (EleOtaUpgrade)表数据库访问层
 *
 * @author Hardy
 * @since 2022-10-14 09:01:59
 */
public interface EleOtaUpgradeMapper extends BaseMapper<EleOtaUpgrade> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EleOtaUpgrade queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EleOtaUpgrade> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param eleOtaUpgrade 实例对象
     * @return 对象列表
     */
    List<EleOtaUpgrade> queryAll(EleOtaUpgrade eleOtaUpgrade);
    
    /**
     * 新增数据
     *
     * @param eleOtaUpgrade 实例对象
     * @return 影响行数
     */
    int insertOne(EleOtaUpgrade eleOtaUpgrade);
    
    /**
     * 修改数据
     *
     * @param eleOtaUpgrade 实例对象
     * @return 影响行数
     */
    int update(EleOtaUpgrade eleOtaUpgrade);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
}
