package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityBatteryModel;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.mapper.ElectricityCabinetMapper;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.ElectricityBatteryModelService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 换电柜表(TElectricityCabinet)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Service("electricityCabinetService")
@Slf4j
public class ElectricityCabinetServiceImpl implements ElectricityCabinetService {
    @Resource
    private ElectricityCabinetMapper electricityCabinetMapper;
    @Autowired
    ElectricityCabinetModelService electricityCabinetModelService;
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    ElectricityBatteryModelService electricityBatteryModelService;
    @Autowired
    CityService cityService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinet queryByIdFromDB(Integer id) {
        return this.electricityCabinetMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinet queryByIdFromCache(Integer id) {
        //先查缓存
        ElectricityCabinet cacheElectricityCabinet = redisService.getWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + id, ElectricityCabinet.class);
        if (Objects.nonNull(cacheElectricityCabinet)) {
            return cacheElectricityCabinet;
        }
        //缓存没有再查数据库
        ElectricityCabinet electricityCabinet = electricityCabinetMapper.queryById(id);
        if (Objects.isNull(electricityCabinet)) {
            return null;
        }
        //放入缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + id, electricityCabinet);
        return electricityCabinet;
    }


    /**
     * 新增数据
     *
     * @param electricityCabinet 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinet insert(ElectricityCabinet electricityCabinet) {
        this.electricityCabinetMapper.insert(electricityCabinet);
        return electricityCabinet;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinet 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinet electricityCabinet) {
        return this.electricityCabinetMapper.update(electricityCabinet);

    }

    @Override
    public R save(ElectricityCabinet electricityCabinet) {
        if(Objects.isNull(electricityCabinet.getPowerStatus())){
            electricityCabinet.setPowerStatus(ElectricityCabinet.ELECTRICITY_CABINET_NO_POWER_STATUS);
        }
        if(Objects.isNull(electricityCabinet.getOnlineStatus())){
            electricityCabinet.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
        }
        electricityCabinet.setCreateTime(System.currentTimeMillis());
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinet.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        //三元组
        List<ElectricityCabinet> existsElectricityCabinetList = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>()
                .eq(ElectricityCabinet::getProductKey, electricityCabinet.getProductKey())
                .eq(ElectricityCabinet::getDeviceName, electricityCabinet.getDeviceName())
                .eq(ElectricityCabinet::getDeviceSecret, electricityCabinet.getDeviceSecret())
                .eq(ElectricityCabinet::getDelFlag,ElectricityCabinet.DEL_NORMAL));
        if (DataUtil.collectionIsUsable(existsElectricityCabinetList)) {
            return R.fail("ELECTRICITY.0002","换电柜的三元组已存在");
        }
        //或换电柜编号
        List<ElectricityCabinet> existsElectricityCabinets = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>()
                .eq(ElectricityCabinet::getSn, electricityCabinet.getSn()).eq(ElectricityCabinet::getDelFlag,ElectricityCabinet.DEL_NORMAL));
        if (DataUtil.collectionIsUsable(existsElectricityCabinets)) {
            return R.fail("ELECTRICITY.0003","换电柜编号已存在");
        }
        //查找快递柜型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(electricityCabinet.getModelId());
        if (Objects.isNull(electricityCabinetModel)) {
            return R.fail("ELECTRICITY.0004","未找到换电柜型号");
        }
        electricityCabinetMapper.insertOne(electricityCabinet);
        //新增缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
        //添加快递柜格挡
        electricityCabinetBoxService.batchInsertBoxByModelId(electricityCabinetModel, electricityCabinet.getId());
        return R.ok(electricityCabinet.getId());
    }

    @Override
    public R edit(ElectricityCabinet electricityCabinet) {
        if(Objects.isNull(electricityCabinet.getId())){
            return R.fail("ELECTRICITY.0007","不合法的参数");
        }
        ElectricityCabinet oldElectricityCabinet = queryByIdFromCache(electricityCabinet.getId());
        if (Objects.isNull(oldElectricityCabinet)) {
            return R.fail("ELECTRICITY.0005","未找到换电柜");
        }
        //三元组
        List<ElectricityCabinet> existsElectricityCabinetList = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>()
                .eq(ElectricityCabinet::getProductKey, electricityCabinet.getProductKey())
                .eq(ElectricityCabinet::getDeviceName, electricityCabinet.getDeviceName())
                .eq(ElectricityCabinet::getDeviceSecret, electricityCabinet.getDeviceSecret())
                .eq(ElectricityCabinet::getDelFlag,ElectricityCabinet.DEL_NORMAL));
        if (DataUtil.collectionIsUsable(existsElectricityCabinetList)) {
            for (ElectricityCabinet existsElectricityCabinet : existsElectricityCabinetList) {
                if (!Objects.equals(existsElectricityCabinet.getId(), electricityCabinet.getId())) {
                    return R.fail("ELECTRICITY.0002","换电柜的三元组已存在");
                }
            }
        }
        //或换电柜编号
        List<ElectricityCabinet> existsElectricityCabinets = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>()
                .eq(ElectricityCabinet::getSn, electricityCabinet.getSn()).eq(ElectricityCabinet::getDelFlag,ElectricityCabinet.DEL_NORMAL));
        if (DataUtil.collectionIsUsable(existsElectricityCabinets)) {
            for (ElectricityCabinet existsElectricityCabinet : existsElectricityCabinets) {
                if (!Objects.equals(existsElectricityCabinet.getId(), electricityCabinet.getId())) {
                    return R.fail("ELECTRICITY.0003","换电柜编号已存在");
                }
            }
        }
        //快递柜老型号
        Integer oldModelId = oldElectricityCabinet.getModelId();
        //查找快递柜型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(electricityCabinet.getModelId());
        if (Objects.isNull(electricityCabinetModel)) {
            return R.fail("ELECTRICITY.0004","未找到换电柜型号");
        }
        if (!oldModelId.equals(electricityCabinet.getModelId())) {
            return R.fail("ELECTRICITY.0010","不能修改型号");
        }
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinetMapper.update(electricityCabinet);
        //更新缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
        //添加快递柜格挡
        if (!oldModelId.equals(electricityCabinet.getModelId())) {
            electricityCabinetBoxService.batchDeleteBoxByElectricityCabinetId(electricityCabinet.getId());
            electricityCabinetBoxService.batchInsertBoxByModelId(electricityCabinetModel, electricityCabinet.getId());
        }
        return R.ok();
    }

    @Override
    public R delete(Integer id) {
        ElectricityCabinet electricityCabinet = queryByIdFromCache(id);
        if(Objects.isNull(electricityCabinet)){
            return R.fail("ELECTRICITY.0005","未找到换电柜");
        }
        //删除数据库
        electricityCabinet.setId(id);
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinet.setDelFlag(ElectricityCabinet.DEL_DEL);
        electricityCabinetMapper.update(electricityCabinet);
        //删除缓存
        redisService.deleteKeys(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + id);
        electricityCabinetBoxService.batchDeleteBoxByElectricityCabinetId(id);
        return R.ok();
    }

    @Override
    public R queryList(ElectricityCabinetQuery electricityCabinetQuery) {
        List<ElectricityCabinetVO> electricityCabinetList = electricityCabinetMapper.queryList(electricityCabinetQuery);
        if(ObjectUtil.isNotEmpty(electricityCabinetList)) {
            electricityCabinetList.parallelStream().forEach(e -> {
                //地区
                City city=cityService.queryByIdFromCache(e.getAreaId());
                if (Objects.nonNull(city)) {
                    e.setAreaName(city.getCity());
                }
                //查找型号名称
                ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(e.getModelId());
                if (Objects.nonNull(electricityCabinetModel)) {
                    e.setModelName(electricityCabinetModel.getName());
                }
                //查满仓空仓数
                Integer electricityBatteryTotal = 0;
                Integer fullyElectricityBattery = 0;
                Integer noElectricityBattery = 0;
                List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(e.getId());
                if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
                    for (ElectricityCabinetBox electricityCabinetBox : electricityCabinetBoxList) {
                        //满仓个数
                        ElectricityBattery electricityBattery = electricityBatteryService.queryById(electricityCabinetBox.getElectricityBatteryId());
                        if (Objects.nonNull(electricityBattery)) {
                            if (electricityBattery.getCapacity() >= e.getFullyCharged()) {
                                fullyElectricityBattery = fullyElectricityBattery + 1;
                            }
                        }
                    }
                    //空仓
                    noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
                    //电池总数
                    electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
                }
                e.setElectricityBatteryTotal(electricityBatteryTotal);
                e.setNoElectricityBattery(noElectricityBattery);
                e.setFullyElectricityBattery(fullyElectricityBattery);
            });
        }
        return R.ok(electricityCabinetList.stream().sorted(Comparator.comparing(ElectricityCabinetVO::getUpdateTime).reversed()).collect(Collectors.toList()));
    }



    @Override
    public R showInfoByDistance(ElectricityCabinetQuery electricityCabinetQuery) {
        List<ElectricityCabinetVO> electricityCabinetList = electricityCabinetMapper.showInfoByDistance(electricityCabinetQuery);
        if(ObjectUtil.isNotEmpty(electricityCabinetList)) {
            electricityCabinetList.parallelStream().forEach(e -> {
                //查满仓空仓数
                Integer electricityBatteryTotal = 0;
                Integer fullyElectricityBattery = 0;
                Integer noElectricityBattery = 0;
                Set<String> set= new  HashSet();
                List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(e.getId());
                if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
                    for (ElectricityCabinetBox electricityCabinetBox:electricityCabinetBoxList) {
                        //满仓个数
                        ElectricityBattery electricityBattery=electricityBatteryService.queryById(electricityCabinetBox.getElectricityBatteryId());
                        if(Objects.nonNull(electricityBattery)) {
                            if (electricityBattery.getCapacity() >= e.getFullyCharged()) {
                                fullyElectricityBattery = fullyElectricityBattery + 1;
                            }
                            ElectricityBatteryModel electricityBatteryModel=electricityBatteryModelService.getElectricityBatteryModelById(electricityBattery.getModelId());
                            if(Objects.nonNull(electricityBatteryModel)){
                                set.add(electricityBatteryModel.getVoltage()+"V"+electricityBatteryModel.getCapacity()+"M");
                            }
                        }
                    }
                    //空仓
                    noElectricityBattery = (int)electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
                    //电池总数
                    electricityBatteryTotal = (int)electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
                }
                e.setElectricityBatteryTotal(electricityBatteryTotal);
                e.setNoElectricityBattery(noElectricityBattery);
                e.setFullyElectricityBattery(fullyElectricityBattery);
                e.setElectricityBatteryFormat(set);
            });
        }
        return R.ok(electricityCabinetList);
    }

    @Override
    public Integer queryByModelId(Integer id) {
        return electricityCabinetMapper.selectCount(Wrappers.<ElectricityCabinet>lambdaQuery().eq(ElectricityCabinet::getModelId,id).eq(ElectricityCabinet::getDelFlag,ElectricityCabinet.DEL_NORMAL));
    }

    @Override
    public R disable(Integer id) {
        if(Objects.isNull(id)){
            return R.fail("ELECTRICITY.0007","不合法的参数");
        }
        ElectricityCabinet oldElectricityCabinet = queryByIdFromCache(id);
        if (Objects.isNull(oldElectricityCabinet)) {
            return R.fail("ELECTRICITY.0005","未找到换电柜");
        }
        if(Objects.equals(oldElectricityCabinet.getUsableStatus(),ElectricityCabinet.ELECTRICITY_CABINET_UN_USABLE_STATUS)){
            return R.fail("ELECTRICITY.0012","快递柜已禁用，不能重复操作");
        }
        ElectricityCabinet electricityCabinet=new ElectricityCabinet();
        electricityCabinet.setId(id);
        electricityCabinet.setUsableStatus(ElectricityCabinet.ELECTRICITY_CABINET_UN_USABLE_STATUS);
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinetMapper.update(electricityCabinet);
        //更新缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
        return R.ok();
    }

    @Override
    public R reboot(Integer id) {
        if(Objects.isNull(id)){
            return R.fail("ELECTRICITY.0007","不合法的参数");
        }
        ElectricityCabinet oldElectricityCabinet = queryByIdFromCache(id);
        if (Objects.isNull(oldElectricityCabinet)) {
            return R.fail("ELECTRICITY.0005","未找到换电柜");
        }
        ElectricityCabinet electricityCabinet=new ElectricityCabinet();
        electricityCabinet.setId(id);
        electricityCabinet.setUsableStatus(ElectricityCabinet.ELECTRICITY_CABINET_USABLE_STATUS);
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinetMapper.update(electricityCabinet);
        //更新缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
        return R.ok();
    }


    private boolean isNoElectricityBattery(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
    }

    private boolean isElectricityBattery(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);
    }
}