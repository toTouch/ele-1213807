package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityBatteryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 电池型号(ElectricityBatteryModel)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 14:44:44
 */
public interface ElectricityBatteryModelMapper extends BaseMapper<ElectricityBatteryModel> {


    List<ElectricityBatteryModel> getElectricityBatteryModelPage(@Param("offset") Long offset, @Param("size") Long size, @Param("name") String name);
}