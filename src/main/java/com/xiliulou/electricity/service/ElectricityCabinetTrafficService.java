package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetTraffic;

import java.time.LocalDate;

public interface ElectricityCabinetTrafficService {
    int insertOrUpdate(ElectricityCabinetTraffic electricityCabinetTraffic);

    ElectricityCabinetTraffic queryById(Long id);

    int updateById(ElectricityCabinetTraffic electricityCabinetTraffic);

    int insertOne(ElectricityCabinetTraffic electricityCabinetTraffic);

    R queryList(Long size, Long offset, Integer electricityCabinetId, String electricityCabinetName, LocalDate date,Long beginTime,Long endTime);

    void expiredDel();
}
