package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import org.apache.ibatis.annotations.Param;

/**
 * 换电柜仓门表(TElectricityCabinetBox)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
public interface ElectricityCabinetBoxMapper extends BaseMapper<ElectricityCabinetBox> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetBox queryById(Long id);

    /**
     * @return 对象列表
     */
    IPage  queryList(Page page, @Param("query") ElectricityCabinetBoxQuery electricityCabinetBoxQuery);


    /**
     * 修改数据
     *
     * @param electricityCabinetBox 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinetBox electricityCabinetBox);


    void batchDeleteBoxByElectricityCabinetId(@Param("id") Integer id, @Param("updateTime") Long updateTime);


    void modifyByCellNo(ElectricityCabinetBox electricityCabinetBox);
}