package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 换电柜电池表(ElectricityBattery)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
@Service
@Slf4j
public class ElectricityBatteryServiceImpl extends ServiceImpl<ElectricityBatteryMapper, ElectricityBattery> implements ElectricityBatteryService {
    @Resource
    private ElectricityBatteryMapper electricitybatterymapper;
    @Autowired
    StoreService storeService;

    /**
     * 保存电池
     *
     * @param
     * @return
     */
    @Override
    public R saveElectricityBattery(ElectricityBattery electricityBattery) {
        if (Objects.nonNull(electricityBattery.getAgentId())) {
            // TODO: 2020/11/26 0026 YG 校验代理商合法性
        }
        if (Objects.nonNull(electricityBattery.getShopId())) {
            Store store = storeService.queryByIdFromCache(electricityBattery.getShopId());
            if (Objects.isNull(store)) {
                log.error("SAVE_ELECTRICITY_BATTERY ERROR, ,NOT FOUND SHOP ,SHOPID:{}", electricityBattery.getShopId());
                return R.failMsg("未找到店铺!");
            }
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
        ElectricityBattery electricityBattery = electricitybatterymapper.selectById(id);
        if (Objects.isNull(electricityBattery)) {
            log.error("DELETE_ELECTRICITY_BATTERY  ERROR ,NOT FOUND ELECTRICITYBATTERY ID:{}", id);
            return R.failMsg("未找到电池!");
        }

        if (ObjectUtil.equal(ElectricityBattery.LEASE_STATUS, electricityBattery.getStatus())) {
            log.error("DELETE_ELECTRICITY_BATTERY  ERROR ,THIS ELECTRICITY_BATTERY IS USING:{}", id);
            return R.failMsg("电池正在租用中,无法删除!");
        }

        int raws = electricitybatterymapper.deleteById(id);
        if (raws > 0) {
            return R.ok();
        } else {
            return R.failMsg("删除失败!");
        }
    }

    @Override
    public Integer queryCountByShopId(Integer id) {
        return electricitybatterymapper.selectCount(new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getShopId, id)
                .in(ElectricityBattery::getStatus, ElectricityBattery.WARE_HOUSE_STATUS, ElectricityBattery.LEASE_STATUS));
    }

    @Override
    public ElectricityBattery queryBySn(String initElectricityBatterySn) {
        return electricitybatterymapper.selectOne(new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSerialNumber, initElectricityBatterySn)
                .eq(ElectricityBattery::getStatus, ElectricityBattery.STOCK_STATUS));
    }

    @Override
    public List<ElectricityBattery> homeTwo(Integer areaId) {
        return electricitybatterymapper.homeTwo(areaId);
    }

    /**
     * 获取个人电池
     *
     * @param uid
     * @return
     */
    @Override
    public R getSelfBattery(Long uid) {
        return R.ok(baseMapper.selectOne(Wrappers.<ElectricityBattery>lambdaQuery().eq(ElectricityBattery::getUid, uid)));
    }
}