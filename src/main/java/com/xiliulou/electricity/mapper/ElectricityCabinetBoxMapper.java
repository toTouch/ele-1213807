package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 换电柜仓门表(TElectricityCabinetBox)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
public interface ElectricityCabinetBoxMapper extends BaseMapper<ElectricityCabinetBox>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetBox queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetBox> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param electricityCabinetBox 实例对象
     * @return 对象列表
     */
    List<ElectricityCabinetBox> queryAll(ElectricityCabinetBox electricityCabinetBox);

    /**
     * 新增数据
     *
     * @param electricityCabinetBox 实例对象
     * @return 影响行数
     */
    int insertOne(ElectricityCabinetBox electricityCabinetBox);

    /**
     * 修改数据
     *
     * @param electricityCabinetBox 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinetBox electricityCabinetBox);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    void batchDeleteBoxByElectricityCabinetId(Integer id);
}