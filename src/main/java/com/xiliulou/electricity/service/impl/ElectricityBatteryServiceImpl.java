package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.utils.PageUtil;
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
        electricityBattery.setStatus(ElectricityBattery.STOCK_STATUS);
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
    @DS("slave_1")
    public R getElectricityBatteryPage(ElectricityBatteryQuery electricityBatteryQuery, Long offset, Long size) {
        Page page = PageUtil.getPage(offset, size);
        return R.ok(electricitybatterymapper.getElectricityBatteryPage(page, electricityBatteryQuery, offset, size));
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
    public ElectricityBattery queryByBindSn(String initElectricityBatterySn) {
        return electricitybatterymapper.selectOne(new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, initElectricityBatterySn)
                .ne(ElectricityBattery::getStatus, ElectricityBattery.LEASE_STATUS));
    }

    @Override
    public List<ElectricityBattery> homeTwo() {
        return electricitybatterymapper.homeTwo();
    }

    /**
     * 获取个人电池
     *
     * @param uid
     * @return
     */
    @Override
    public R getSelfBattery(Long uid) {

        return R.ok(baseMapper.selectBatteryInfo(uid));
    }


    @Override
    public ElectricityBattery queryBySn(String oldElectricityBatterySn) {
        return electricitybatterymapper.selectOne(new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, oldElectricityBatterySn));
    }

    @Override
    public ElectricityBattery queryByUnBindSn(String nowElectricityBatterySn) {
        return electricitybatterymapper.selectOne(new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, nowElectricityBatterySn)
                .eq(ElectricityBattery::getStatus, ElectricityBattery.LEASE_STATUS));
    }

    @Override
    public void updateStatus(ElectricityBattery electricityBattery) {
        electricitybatterymapper.update(electricityBattery);
    }

    @Override
    public R pageByFranchisee(ElectricityBatteryQuery electricityBatteryQuery, Long offset, Long size) {
        Page page = PageUtil.getPage(offset, size);
        return R.ok(electricitybatterymapper.pageByFranchisee(page, electricityBatteryQuery, offset, size));
    }

}