package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 换电柜电池表(ElectricityBattery)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
@Service
@Slf4j
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
        if (Objects.nonNull(electricityBattery.getAgentId())) {
            // TODO: 2020/11/26 0026 校验代理商合法性
        }
        if (Objects.nonNull(electricityBattery.getShopId())) {
            // TODO: 2020/11/26 0026 校验商铺/门店合法性
        }
        electricityBattery.setCreateTime(System.currentTimeMillis());
        electricityBattery.setUpdateTime(System.currentTimeMillis());

        return R.ok(electricitybatterymapper.insert(electricityBattery));
    }

    /**
     * 修改电池
     *
     * @param electricityBattery
     * @return
     */
    @Override
    public R update(ElectricityBattery electricityBattery) {
        ElectricityBattery electricityBatteryDb = electricitybatterymapper.selectById(electricityBattery.getId());
        if (Objects.isNull(electricityBatteryDb)) {
            log.error("UPDATE ELECTRICITY_BATTERY  ERROR, NOT FOUND ELECTRICITY_BATTERY BY ID:{}", electricityBattery.getId());
            return R.fail("电池不存在!");
        }
        if (Objects.nonNull(electricityBattery.getAgentId())) {
            //校验合法性
        }
        if (Objects.nonNull(electricityBattery.getShopId())) {
            //校验合法性
        }
        electricityBattery.setUpdateTime(System.currentTimeMillis());
        Integer rows = electricitybatterymapper.updateById(electricityBattery);
        if (rows > 0) {
            return R.ok();
        } else {
            return R.fail("修改失败!");

        }
    }

    /**
     * 电池分页
     *
     * @param electricityBatteryQuery
     * @param
     * @return
     */
    @Override
    public R getElectricityBatteryPage(ElectricityBatteryQuery electricityBatteryQuery, Long offset, Long size) {
        return R.ok(electricitybatterymapper.getElectricityBatteryPage(electricityBatteryQuery, offset, size));
    }

    @Override
    public ElectricityBattery queryById(Long electricityBatteryId) {
        return electricitybatterymapper.selectById(electricityBatteryId);
    }

    /**
     * 删除电池
     *
     * @param id
     * @return
     */
    @Override
    public R deleteElectricityBattery(Long id) {
        // TODO: 2020/11/30 0030  校验  电池是否正在被用!
        int raws = electricitybatterymapper.deleteById(id);
        if (raws > 0) {
            return R.ok();
        } else {
            return R.failMsg("删除失败!");
        }
    }
}