package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityCabinetPower;
import com.xiliulou.electricity.entity.ElectricityCabinetTraffic;
import com.xiliulou.electricity.vo.ElectricityCabinetTrafficVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface ElectricityCabinetTrafficMapper extends BaseMapper<ElectricityCabinetTraffic> {

    ElectricityCabinetTraffic queryById(@Param("id") Long id);

    int updateOneById(ElectricityCabinetTraffic electricityCabinetTraffic);

    int insertOne(ElectricityCabinetTraffic electricityCabinetTraffic);

    List<ElectricityCabinetTrafficVo> queryList(@Param("size")Long size,
                                                @Param("offset")Long offset,
                                                @Param("electricityCabinetId")Integer electricityCabinetId,
                                                @Param("electricityCabinetName")String electricityCabinetName,
                                                @Param("beginTime")Long beginTime,
                                                @Param("endTime")Long endTime,
                                                @Param("date")LocalDate date);

    ElectricityCabinetTrafficVo queryLatestTraffic(@Param("electricityCabinetId")Integer electricityCabinetId, @Param("electricityCabinetName")String electricityCabinetName);

    @Delete("delete from t_electricity_cabinet_traffic where create_time < #{time}")
    int removeLessThanTime(@Param("time")Long time);
}
