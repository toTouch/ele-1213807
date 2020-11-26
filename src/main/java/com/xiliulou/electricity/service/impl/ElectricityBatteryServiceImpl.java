package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 换电柜电池表(ElectricityBattery)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
@Service("ElectricityBatteryService")
public class ElectricityBatteryServiceImpl implements ElectricityBatteryService {
    @Resource
    private ElectricityBatteryMapper electricitybatterymapper;

    /**
     * 保存电池
     *
     * @param
     * @return
     */
    @Override
    public R save(ElectricityBattery electricityBattery) {
        if (Objects.nonNull(electricityBattery.getAgent())) {
            // TODO: 2020/11/26 0026 校验代理商合法性
        }
        if (Objects.nonNull(electricityBattery.getShopId())) {
            // TODO: 2020/11/26 0026 校验商铺/门店合法性
        }
        electricityBattery.setCreateTime(System.currentTimeMillis());
        electricityBattery.setUpdateTime(System.currentTimeMillis());

        return R.ok(electricitybatterymapper.insert(electricityBattery));
    }
}