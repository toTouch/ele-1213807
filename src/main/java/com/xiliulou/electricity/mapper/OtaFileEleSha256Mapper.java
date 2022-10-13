package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.OtaFileEleSha256;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (OtaFileEleSha256)表数据库访问层
 *
 * @author zzgw
 * @since 2022-10-12 17:31:09
 */
public interface OtaFileEleSha256Mapper extends BaseMapper<OtaFileEleSha256> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    OtaFileEleSha256 queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<OtaFileEleSha256> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param otaFileEleSha256 实例对象
     * @return 对象列表
     */
    List<OtaFileEleSha256> queryAll(OtaFileEleSha256 otaFileEleSha256);
    
    /**
     * 新增数据
     *
     * @param otaFileEleSha256 实例对象
     * @return 影响行数
     */
    int insertOne(OtaFileEleSha256 otaFileEleSha256);
    
    /**
     * 修改数据
     *
     * @param otaFileEleSha256 实例对象
     * @return 影响行数
     */
    int update(OtaFileEleSha256 otaFileEleSha256);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    OtaFileEleSha256 queryByEid(Integer eid);
}
