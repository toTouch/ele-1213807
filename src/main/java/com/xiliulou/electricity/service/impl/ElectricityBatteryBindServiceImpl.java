package com.xiliulou.electricity.service.impl;
import com.xiliulou.electricity.entity.ElectricityBatteryBind;
import com.xiliulou.electricity.mapper.ElectricityBatteryBindMapper;
import com.xiliulou.electricity.service.ElectricityBatteryBindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * (ElectricityBatteryBind)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Service("electricityBatteryBindService")
@Slf4j
public class ElectricityBatteryBindServiceImpl implements ElectricityBatteryBindService {
    @Resource
    ElectricityBatteryBindMapper electricityBatteryBindMapper;
    @Override
    public void deleteByUid(Long uid) {
        electricityBatteryBindMapper.deleteByUid(uid);
    }

    @Override
    public void insert(ElectricityBatteryBind electricityBatteryBind) {
        electricityBatteryBindMapper.insert(electricityBatteryBind);
    }
}