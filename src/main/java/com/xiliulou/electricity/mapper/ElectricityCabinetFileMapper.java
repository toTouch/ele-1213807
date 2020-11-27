package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 换电柜文件表(TElectricityCabinetFile)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-27 10:17:18
 */
public interface ElectricityCabinetFileMapper extends BaseMapper<ElectricityCabinetFile>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetFile queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetFile> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param electricityCabinetFile 实例对象
     * @return 对象列表
     */
    List<ElectricityCabinetFile> queryAll(ElectricityCabinetFile electricityCabinetFile);

    /**
     * 新增数据
     *
     * @param electricityCabinetFile 实例对象
     * @return 影响行数
     */
    int insertOne(ElectricityCabinetFile electricityCabinetFile);

    /**
     * 修改数据
     *
     * @param electricityCabinetFile 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinetFile electricityCabinetFile);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}