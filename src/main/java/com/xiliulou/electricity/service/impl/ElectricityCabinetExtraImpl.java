package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ElectricityCabinetExtra;
import com.xiliulou.electricity.mapper.ElectricityCabinetExtraMapper;
import com.xiliulou.electricity.service.ElectricityCabinetExtraService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 柜机扩展表服务层
 * @date 2024/4/23 13:57:12
 */
@Slf4j
@Service
public class ElectricityCabinetExtraImpl implements ElectricityCabinetExtraService {
    
    @Resource
    private ElectricityCabinetExtraMapper electricityCabinetExtraMapper;
    
    @Slave
    @Override
    public ElectricityCabinetExtra queryByEid(Long eid, Integer tenantId) {
        return electricityCabinetExtraMapper.selectByEid(eid, tenantId);
    }
    
    @Override
    public Integer insertOrUpdate(ElectricityCabinetExtra electricityCabinetExtra) {
        ElectricityCabinetExtra exist = this.queryByEid(electricityCabinetExtra.getEid(), electricityCabinetExtra.getTenantId());
        if (Objects.isNull(exist)) {
            return electricityCabinetExtraMapper.insertOne(electricityCabinetExtra);
        }
    
        if (!Objects.equals(exist.getSn(), electricityCabinetExtra.getSn()) || Objects.equals(exist.getBatteryCountType(), electricityCabinetExtra.getBatteryCountType())) {
            return NumberConstant.ZERO;
        }
    
        exist.setBatteryCountType(electricityCabinetExtra.getBatteryCountType());
        exist.setUpdateTime(electricityCabinetExtra.getUpdateTime());
        
        return electricityCabinetExtraMapper.update(exist);
    }
    
    @Override
    public Integer insertOne(ElectricityCabinetExtra electricityCabinetExtra) {
        return electricityCabinetExtraMapper.insertOne(electricityCabinetExtra);
    }
    
    @Override
    public Integer update(ElectricityCabinetExtra electricityCabinetExtra) {
        return electricityCabinetExtraMapper.update(electricityCabinetExtra);
    }
}
