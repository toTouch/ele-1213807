package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import java.util.List;

import com.xiliulou.electricity.query.ElectricityCabinetModelQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 换电柜型号表(TElectricityCabinetModel)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:01:04
 */
public interface ElectricityCabinetModelMapper extends BaseMapper<ElectricityCabinetModel>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetModel queryById(Integer id);

    /**
     * @return 对象列表
     */
    List<ElectricityCabinetModel> queryList(@Param("query") ElectricityCabinetModelQuery electricityCabinetModelQuery);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param electricityCabinetModel 实例对象
     * @return 对象列表
     */
    List<ElectricityCabinetModel> queryAll(ElectricityCabinetModel electricityCabinetModel);

    /**
     * 新增数据
     *
     * @param electricityCabinetModel 实例对象
     * @return 影响行数
     */
    int insertOne(ElectricityCabinetModel electricityCabinetModel);

    /**
     * 修改数据
     *
     * @param electricityCabinetModel 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinetModel electricityCabinetModel);


}