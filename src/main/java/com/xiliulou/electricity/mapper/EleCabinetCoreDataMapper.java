package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.EleCabinetCoreData;

import java.util.List;

import com.xiliulou.electricity.query.EleCabinetCoreDataQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 柜机核心板上报数据(EleCabinetCoreData)表数据库访问层
 *
 * @author zzlong
 * @since 2022-07-06 14:20:37
 */
public interface EleCabinetCoreDataMapper extends BaseMapper<EleCabinetCoreData> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EleCabinetCoreData queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EleCabinetCoreData> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param eleCabinetCoreData 实例对象
     * @return 对象列表
     */
    List<EleCabinetCoreData> queryAll(EleCabinetCoreData eleCabinetCoreData);

    /**
     * 新增数据
     *
     * @param eleCabinetCoreData 实例对象
     * @return 影响行数
     */
    int insertOne(EleCabinetCoreData eleCabinetCoreData);

    /**
     * 修改数据
     *
     * @param eleCabinetCoreData 实例对象
     * @return 影响行数
     */
    int update(EleCabinetCoreData eleCabinetCoreData);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    int atomicUpdateCabinetCoreData(EleCabinetCoreData cabinetCoreData);

    List<EleCabinetCoreData> selectListByQuery(EleCabinetCoreDataQuery eleCabinetCoreDataQuery);
    
    EleCabinetCoreData selectById( @Param("id") Integer id, @Param("tenantId")  Integer tenantId);
}
