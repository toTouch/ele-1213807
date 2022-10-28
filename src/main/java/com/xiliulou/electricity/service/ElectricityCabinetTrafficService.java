package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetTraffic;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import javax.servlet.http.HttpServletResponse;

public interface ElectricityCabinetTrafficService {

    int insertOrUpdate(ElectricityCabinetTraffic electricityCabinetTraffic);

    ElectricityCabinetTraffic queryById(Long id);

    int updateById(ElectricityCabinetTraffic electricityCabinetTraffic);

    int insertOne(ElectricityCabinetTraffic electricityCabinetTraffic);

    R queryList(Long size, Long offset, Integer electricityCabinetId, String electricityCabinetName, LocalDate date, Long beginTime, Long endTime);

    void exportExcel(Integer electricityCabinetId, String electricityCabinetName, LocalDate date, Long beginTime, Long endTime, HttpServletResponse response);

    void expiredDel();
}
