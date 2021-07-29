package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 换电柜电池表(ElectricityBattery)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
public interface ElectricityBatteryMapper extends BaseMapper<ElectricityBattery> {


    List<ElectricityBattery> queryList(@Param("query") ElectricityBatteryQuery electricityBatteryQuery,
                                    @Param("offset") Long offset, @Param("size") Long size);

	Integer queryCount(@Param("query") ElectricityBatteryQuery electricityBatteryQuery);

    ElectricityBattery selectBatteryInfo(@Param("uid") Long uid);

	Integer updateByOrder(ElectricityBattery electricityBattery);


}
