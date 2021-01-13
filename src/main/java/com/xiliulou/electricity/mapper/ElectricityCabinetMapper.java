package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.BatteryFormat;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 换电柜表(TElectricityCabinet)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCabinetMapper extends BaseMapper<ElectricityCabinet> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinet queryById(Integer id);

    /**
     * @param electricityCabinetQuery
     * @return 对象列表
     */
    IPage queryList(Page page, @Param("query") ElectricityCabinetQuery electricityCabinetQuery);

    /**
     * 修改数据
     *
     * @param electricityCabinet 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinet electricityCabinet);


    List<ElectricityCabinetVO> showInfoByDistance(@Param("query") ElectricityCabinetQuery electricityCabinetQuery);

    Integer queryFullyElectricityBattery(Integer id);

    List<BatteryFormat> queryElectricityBatteryFormat(Integer id);
}