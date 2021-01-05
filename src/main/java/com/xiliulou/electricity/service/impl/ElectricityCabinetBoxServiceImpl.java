package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Maps;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.mapper.ElectricityCabinetBoxMapper;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.vo.ElectricityCabinetBoxVO;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 换电柜仓门表(TElectricityCabinetBox)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@Service("electricityCabinetBoxService")
public class ElectricityCabinetBoxServiceImpl implements ElectricityCabinetBoxService {
    @Resource
    private ElectricityCabinetBoxMapper electricityCabinetBoxMapper;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetBox queryByIdFromDB(Long id) {
        return this.electricityCabinetBoxMapper.queryById(id);
    }

    @Override
    public void batchInsertBoxByModelId(ElectricityCabinetModel electricityCabinetModel, Integer id) {
        if (Objects.nonNull(id)) {
            for (int i = 1; i <= electricityCabinetModel.getNum(); i++) {
                ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
                electricityCabinetBox.setElectricityCabinetId(id);
                electricityCabinetBox.setCellNo(String.valueOf(i));
                electricityCabinetBox.setCreateTime(System.currentTimeMillis());
                electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
                electricityCabinetBox.setDelFlag(ElectricityCabinetBox.DEL_NORMAL);
                electricityCabinetBoxMapper.insert(electricityCabinetBox);
            }
        }
    }

    @Override
    public void batchDeleteBoxByElectricityCabinetId(Integer id) {
        electricityCabinetBoxMapper.batchDeleteBoxByElectricityCabinetId(id, System.currentTimeMillis());
    }

    @Override
    @DS("slave_1")
    public R queryList(ElectricityCabinetBoxQuery electricityCabinetBoxQuery) {
        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOList = electricityCabinetBoxMapper.queryList(electricityCabinetBoxQuery);
        if (ObjectUtil.isNotEmpty(electricityCabinetBoxVOList)) {
            electricityCabinetBoxVOList.parallelStream().forEach(e -> {
                ElectricityBattery electricityBattery = electricityBatteryService.queryById(e.getElectricityBatteryId());
                if (Objects.nonNull(electricityBattery)) {
                    e.setSerialNumber(electricityBattery.getSerialNumber());
                    e.setCapacity(electricityBattery.getCapacity());
                }
            });
        }
        return R.ok(electricityCabinetBoxVOList.stream().sorted(Comparator.comparing(ElectricityCabinetBoxVO::getCellNo).reversed()).collect(Collectors.toList()));
    }

    @Override
    public R modify(ElectricityCabinetBox electricityCabinetBox) {
        electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
        electricityCabinetBoxMapper.update(electricityCabinetBox);
        return R.ok();
    }


    @Override
    public List<ElectricityCabinetBox> queryBoxByElectricityCabinetId(Integer id) {
        return electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id).eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL));
    }

    @Override
    public List<ElectricityCabinetBox> queryNoElectricityBatteryBox(Integer id) {
        return electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id)
                .eq(ElectricityCabinetBox::getStatus, ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY).eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL));
    }

    @Override
    public List<ElectricityCabinetBox> queryElectricityBatteryBox(Integer id, String cellNo) {
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id)
                .eq(ElectricityCabinetBox::getStatus, ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY).eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL)
                .ne(ElectricityCabinetBox::getCellNo, cellNo));
        List<ElectricityCabinetBox> electricityCabinetBoxes = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
            electricityCabinetBoxList.parallelStream().forEach(e -> {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(id);
                if (Objects.nonNull(electricityCabinet)) {
                    ElectricityBattery electricityBattery = electricityBatteryService.queryById(e.getElectricityBatteryId());
                    if (Objects.nonNull(electricityBattery)) {
                        if (electricityBattery.getCapacity() >= electricityCabinet.getFullyCharged()) {
                            electricityCabinetBoxes.add(e);
                        }
                    }
                }
            });
        }
        return electricityCabinetBoxes;
    }

    @Override
    public void modifyByCellNo(ElectricityCabinetBox electricityCabinetBox) {
        electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
        electricityCabinetBoxMapper.modifyByCellNo(electricityCabinetBox);
    }

    @Override
    public ElectricityCabinetBox queryByCellNo(Integer electricityCabinetId, String cellNo) {
        return electricityCabinetBoxMapper.selectOne(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, electricityCabinetId)
                .eq(ElectricityCabinetBox::getCellNo, cellNo).eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL));
    }


}