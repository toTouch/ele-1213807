package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.vo.ElectricityBatteryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 换电柜电池表(ElectricityBattery)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
public interface ElectricityBatteryMapper extends BaseMapper<ElectricityBattery> {


    IPage getElectricityBatteryPage(Page page, @Param("query") ElectricityBatteryQuery electricityBatteryQuery,
                                    @Param("offset") Long offset, @Param("size") Long size);

    List<ElectricityBattery> homeTwo(@Param("batteryIdList") List<Long> batteryIdList);

    ElectricityBatteryVo selectBatteryInfo(@Param("uid") Long uid);


    ElectricityBatteryVo queryById(@Param("id") Long electricityBatteryId);
}
