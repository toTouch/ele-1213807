package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetTraffic;
import com.xiliulou.electricity.mapper.ElectricityCabinetTrafficMapper;
import com.xiliulou.electricity.service.ElectricityCabinetTrafficService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Objects;

/**
 * zgw
 */
@Service
@Slf4j
public class ElectricityCabinetTrafficServiceImpl implements ElectricityCabinetTrafficService {

    @Resource
    ElectricityCabinetTrafficMapper electricityCabinetTrafficMapper;

    @Override
    public int insertOrUpdate(ElectricityCabinetTraffic electricityCabinetTraffic) {
        ElectricityCabinetTraffic queryById = queryById(electricityCabinetTraffic.getId());
        if(Objects.isNull(queryById)) {
            return insertOne(electricityCabinetTraffic);
        }
        return updateById(electricityCabinetTraffic);
    }

    @Override
    public ElectricityCabinetTraffic queryById(Long id) {
        if(Objects.isNull(id)) {
            return null;
        }
        return electricityCabinetTrafficMapper.queryById(id);
    }

    @Override
    public int updateById(ElectricityCabinetTraffic electricityCabinetTraffic) {
        return electricityCabinetTrafficMapper.updateOneById(electricityCabinetTraffic);
    }

    @Override
    public int insertOne(ElectricityCabinetTraffic electricityCabinetTraffic){
        return electricityCabinetTrafficMapper.insertOne(electricityCabinetTraffic);
    }

    @Override
    public R queryList(Long size, Long offset, Integer electricityCabinetId, String electricityCabinetName, LocalDate date) {
        return R.ok(electricityCabinetTrafficMapper.queryList(size, offset, electricityCabinetId, electricityCabinetName, date));
    }

    @Override
    public void expiredDel() {
        Long time = System.currentTimeMillis() - (1000L * 3600 * 24 * 365);
        electricityCabinetTrafficMapper.removeLessThanTime(time);
    }
}
