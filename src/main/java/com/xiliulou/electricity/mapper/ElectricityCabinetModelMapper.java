package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.query.ElectricityCabinetModelQuery;
import org.apache.ibatis.annotations.Param;

/**
 * 换电柜型号表(TElectricityCabinetModel)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:01:04
 */
public interface ElectricityCabinetModelMapper extends BaseMapper<ElectricityCabinetModel> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetModel queryById(Integer id,Integer tenantId);

    /**
     * @return 对象列表
     */
    IPage queryList(Page page, @Param("query") ElectricityCabinetModelQuery electricityCabinetModelQuery);




}
