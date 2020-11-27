package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.mapper.ElectricityCabinetMapper;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 换电柜表(TElectricityCabinet)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Service("electricityCabinetService")
public class ElectricityCabinetServiceImpl implements ElectricityCabinetService {
    @Resource
    private ElectricityCabinetMapper electricityCabinetMapper;
    @Autowired
    ElectricityCabinetModelService electricityCabinetModelService;
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;

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
        //TODO 判断参数
        electricityCabinet.setCreateTime(System.currentTimeMillis());
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinet.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        //三元组
        List<ElectricityCabinet> existsElectricityCabinetList = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getProductKey, electricityCabinet.getProductKey()).eq(ElectricityCabinet::getDeviceName, electricityCabinet.getDeviceName()).eq(ElectricityCabinet::getDeviceSecret, electricityCabinet.getDeviceSecret()));
        if (DataUtil.collectionIsUsable(existsElectricityCabinetList)) {
            return R.fail("SYSTEM.0002");
        }
        //或换电柜编号
        List<ElectricityCabinet> existsElectricityCabinets = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getSn, electricityCabinet.getSn()));
        if (DataUtil.collectionIsUsable(existsElectricityCabinets)) {
            return R.fail("SYSTEM.0003");
        }
        //查找快递柜型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(electricityCabinet.getModelId());
        if (Objects.isNull(electricityCabinetModel)) {
            return R.fail("SYSTEM.0004");
        }
        electricityCabinetMapper.insertOne(electricityCabinet);
        //新增缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
        //添加快递柜格挡
        electricityCabinetBoxService.batchInsertBoxByModelId(electricityCabinetModel, electricityCabinet.getId());
        return R.ok();
    }

    @Override
    public R edit(ElectricityCabinet electricityCabinet) {
        //TODO 判断参数
        ElectricityCabinet oldElectricityCabinet = queryByIdFromCache(electricityCabinet.getId());
        if (Objects.isNull(oldElectricityCabinet)) {
            return R.fail("SYSTEM.0005");
        }
        //三元组
        List<ElectricityCabinet> existsElectricityCabinetList = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getProductKey, electricityCabinet.getProductKey()).eq(ElectricityCabinet::getDeviceName, electricityCabinet.getDeviceName()).eq(ElectricityCabinet::getDeviceSecret, electricityCabinet.getDeviceSecret()));
        if (DataUtil.collectionIsUsable(existsElectricityCabinetList)) {
            for (ElectricityCabinet existsElectricityCabinet : existsElectricityCabinetList) {
                if (!Objects.equals(existsElectricityCabinet.getId(), electricityCabinet.getId())) {
                    return R.fail("SYSTEM.0002");
                }
            }
        }
        //或换电柜编号
        List<ElectricityCabinet> existsElectricityCabinets = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getSn, electricityCabinet.getSn()));
        if (DataUtil.collectionIsUsable(existsElectricityCabinets)) {
            for (ElectricityCabinet existsElectricityCabinet : existsElectricityCabinets) {
                if (!Objects.equals(existsElectricityCabinet.getId(), electricityCabinet.getId())) {
                    return R.fail("SYSTEM.0003");
                }
            }
        }
        //快递柜老型号
        Integer oldModelId = oldElectricityCabinet.getModelId();
        //查找快递柜型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(electricityCabinet.getModelId());
        if (Objects.isNull(electricityCabinetModel)) {
            return R.fail("SYSTEM.0004");
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
        //删除数据库
        ElectricityCabinet electricityCabinet = new ElectricityCabinet();
        electricityCabinet.setId(id);
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinet.setDelFlag(ElectricityCabinet.DEL_DEL);
        electricityCabinetMapper.update(electricityCabinet);
        //删除缓存
        redisService.deleteKeys(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + id);
        return R.ok();
    }

    @Override
    public R queryList(ElectricityCabinetQuery electricityCabinetQuery) {
        List<ElectricityCabinetVO> electricityCabinetList = electricityCabinetMapper.queryList(electricityCabinetQuery);
        List<ElectricityCabinetVO> electricityCabinetVOS = new ArrayList<>();
        if(ObjectUtil.isNotEmpty(electricityCabinetList)) {
            electricityCabinetList.parallelStream().forEach(e -> {
                //查找型号名称
                ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(e.getId());
                if (Objects.nonNull(electricityCabinetModel)) {
                    e.setModelName(electricityCabinetModel.getName());
                }
                //查满仓空仓数
                Integer electricityBatteryTotal = 0;
                Integer fullyElectricityBattery = 0;
                Integer noElectricityBattery = 0;
                List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(e.getId());
                if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
                    //TODO 满仓个数
                    //空仓
                    noElectricityBattery = (int)electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
                    //电池总数
                    electricityBatteryTotal = electricityCabinetBoxList.size()-noElectricityBattery;
                }
                e.setElectricityBatteryTotal(electricityBatteryTotal);
                e.setNoElectricityBattery(noElectricityBattery);
                e.setFullyElectricityBattery(fullyElectricityBattery);
                electricityCabinetVOS.add(e);
            });
        }
        return R.ok(electricityCabinetVOS.stream().sorted(Comparator.comparing(ElectricityCabinetVO::getId).reversed()).collect(Collectors.toList()));
    }



    @Override
    public R showInfoByDistance(ElectricityCabinetQuery electricityCabinetQuery) {
        List<ElectricityCabinetVO> electricityCabinetList = electricityCabinetMapper.showInfoByDistance(electricityCabinetQuery);
        List<ElectricityCabinetVO> electricityCabinetVOS = new ArrayList<>();
        if(ObjectUtil.isNotEmpty(electricityCabinetList)) {
            electricityCabinetList.parallelStream().forEach(e -> {
                //查满仓空仓数
                Integer electricityBatteryTotal = 0;
                Integer fullyElectricityBattery = 0;
                Integer noElectricityBattery = 0;
                List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(e.getId());
                if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
                    //TODO 满仓个数
                    //空仓
                    noElectricityBattery = (int)electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
                    //电池总数
                    electricityBatteryTotal = electricityCabinetBoxList.size()-noElectricityBattery;
                }
                e.setElectricityBatteryTotal(electricityBatteryTotal);
                e.setNoElectricityBattery(noElectricityBattery);
                e.setFullyElectricityBattery(fullyElectricityBattery);
                //TODO 电池规格
                electricityCabinetVOS.add(e);
            });
        }
        return R.ok(electricityCabinetVOS);
    }

    private boolean isNoElectricityBattery(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
    }
}