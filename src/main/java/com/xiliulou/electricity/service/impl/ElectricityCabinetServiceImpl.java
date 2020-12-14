package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
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
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ElectricityCabinetMapper;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.ElectricityBatteryModelService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    @Autowired
    StoreService storeService;

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
    @Transactional
    public R save(ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        ElectricityCabinet electricityCabinet = new ElectricityCabinet();
        BeanUtil.copyProperties(electricityCabinetAddAndUpdate, electricityCabinet);
        if (Objects.isNull(electricityCabinet.getPowerStatus())) {
            electricityCabinet.setPowerStatus(ElectricityCabinet.ELECTRICITY_CABINET_NO_POWER_STATUS);
        }
        if (Objects.isNull(electricityCabinet.getOnlineStatus())) {
            electricityCabinet.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
        }
        if (Objects.equals(electricityCabinetAddAndUpdate.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.ALL_DAY)) {
            electricityCabinet.setBusinessTime(ElectricityCabinetAddAndUpdate.ALL_DAY);
        }
        if (Objects.equals(electricityCabinetAddAndUpdate.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.CUSTOMIZE_TIME)) {
            if (Objects.isNull(electricityCabinetAddAndUpdate.getBeginTime()) || Objects.isNull(electricityCabinetAddAndUpdate.getEndTime())
                    || electricityCabinetAddAndUpdate.getBeginTime() > electricityCabinetAddAndUpdate.getEndTime()) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
            electricityCabinet.setBusinessTime(electricityCabinetAddAndUpdate.getBeginTime() + "-" + electricityCabinetAddAndUpdate.getEndTime());
        }
        if (Objects.isNull(electricityCabinet.getBusinessTime())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        electricityCabinet.setCreateTime(System.currentTimeMillis());
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinet.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        //三元组
        List<ElectricityCabinet> existsElectricityCabinetList = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>()
                .eq(ElectricityCabinet::getProductKey, electricityCabinet.getProductKey())
                .eq(ElectricityCabinet::getDeviceName, electricityCabinet.getDeviceName())
                .eq(ElectricityCabinet::getDeviceSecret, electricityCabinet.getDeviceSecret())
                .eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
        if (DataUtil.collectionIsUsable(existsElectricityCabinetList)) {
            return R.fail("ELECTRICITY.0002", "换电柜的三元组已存在");
        }
        //或换电柜编号
        List<ElectricityCabinet> existsElectricityCabinets = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>()
                .eq(ElectricityCabinet::getSn, electricityCabinet.getSn()).eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
        if (DataUtil.collectionIsUsable(existsElectricityCabinets)) {
            return R.fail("ELECTRICITY.0003", "换电柜编号已存在");
        }
        //查找快递柜型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(electricityCabinet.getModelId());
        if (Objects.isNull(electricityCabinetModel)) {
            return R.fail("ELECTRICITY.0004", "未找到换电柜型号");
        }
        int insert = electricityCabinetMapper.insertOne(electricityCabinet);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //新增缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
            //添加快递柜格挡
            electricityCabinetBoxService.batchInsertBoxByModelId(electricityCabinetModel, electricityCabinet.getId());
            return electricityCabinet;
        });
        return R.ok(electricityCabinet.getId());
    }

    @Override
    @Transactional
    public R edit(ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        ElectricityCabinet electricityCabinet = new ElectricityCabinet();
        BeanUtil.copyProperties(electricityCabinetAddAndUpdate, electricityCabinet);
        ElectricityCabinet oldElectricityCabinet = queryByIdFromCache(electricityCabinet.getId());
        if (Objects.isNull(oldElectricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        if (Objects.nonNull(electricityCabinetAddAndUpdate.getBusinessTimeType())) {
            if (Objects.equals(electricityCabinetAddAndUpdate.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.ALL_DAY)) {
                electricityCabinet.setBusinessTime(ElectricityCabinetAddAndUpdate.ALL_DAY);
            }
            if (Objects.equals(electricityCabinetAddAndUpdate.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.CUSTOMIZE_TIME)) {
                if (Objects.isNull(electricityCabinetAddAndUpdate.getBeginTime()) || Objects.isNull(electricityCabinetAddAndUpdate.getEndTime())
                        || electricityCabinetAddAndUpdate.getBeginTime() > electricityCabinetAddAndUpdate.getEndTime()) {
                    return R.fail("ELECTRICITY.0007", "不合法的参数");
                }
                electricityCabinet.setBusinessTime(electricityCabinetAddAndUpdate.getBeginTime() + "-" + electricityCabinetAddAndUpdate.getEndTime());
            }
            if (Objects.isNull(electricityCabinet.getBusinessTime())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
        }
        //三元组
        List<ElectricityCabinet> existsElectricityCabinetList = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>()
                .eq(ElectricityCabinet::getProductKey, electricityCabinet.getProductKey())
                .eq(ElectricityCabinet::getDeviceName, electricityCabinet.getDeviceName())
                .eq(ElectricityCabinet::getDeviceSecret, electricityCabinet.getDeviceSecret())
                .eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
        if (DataUtil.collectionIsUsable(existsElectricityCabinetList)) {
            for (ElectricityCabinet existsElectricityCabinet : existsElectricityCabinetList) {
                if (!Objects.equals(existsElectricityCabinet.getId(), electricityCabinet.getId())) {
                    return R.fail("ELECTRICITY.0002", "换电柜的三元组已存在");
                }
            }
        }
        //或换电柜编号
        List<ElectricityCabinet> existsElectricityCabinets = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>()
                .eq(ElectricityCabinet::getSn, electricityCabinet.getSn()).eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
        if (DataUtil.collectionIsUsable(existsElectricityCabinets)) {
            for (ElectricityCabinet existsElectricityCabinet : existsElectricityCabinets) {
                if (!Objects.equals(existsElectricityCabinet.getId(), electricityCabinet.getId())) {
                    return R.fail("ELECTRICITY.0003", "换电柜编号已存在");
                }
            }
        }
        //快递柜老型号
        Integer oldModelId = oldElectricityCabinet.getModelId();
        //查找快递柜型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(electricityCabinet.getModelId());
        if (Objects.isNull(electricityCabinetModel)) {
            return R.fail("ELECTRICITY.0004", "未找到换电柜型号");
        }
        if (!oldModelId.equals(electricityCabinet.getModelId())) {
            return R.fail("ELECTRICITY.0010", "不能修改型号");
        }
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        int update = electricityCabinetMapper.update(electricityCabinet);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
            //添加快递柜格挡
            if (!oldModelId.equals(electricityCabinet.getModelId())) {
                electricityCabinetBoxService.batchDeleteBoxByElectricityCabinetId(electricityCabinet.getId());
                electricityCabinetBoxService.batchInsertBoxByModelId(electricityCabinetModel, electricityCabinet.getId());
            }
            return null;
        });
        return R.ok();
    }

    @Override
    @Transactional
    public R delete(Integer id) {
        ElectricityCabinet electricityCabinet = queryByIdFromCache(id);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        //删除数据库
        electricityCabinet.setId(id);
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinet.setDelFlag(ElectricityCabinet.DEL_DEL);
        int update = electricityCabinetMapper.update(electricityCabinet);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //删除缓存
            redisService.deleteKeys(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + id);
            electricityCabinetBoxService.batchDeleteBoxByElectricityCabinetId(id);
            return null;
        });
        return R.ok();
    }

    @Override
    public R queryList(ElectricityCabinetQuery electricityCabinetQuery) {
        List<ElectricityCabinetVO> electricityCabinetList = electricityCabinetMapper.queryList(electricityCabinetQuery);
        if (ObjectUtil.isNotEmpty(electricityCabinetList)) {
            electricityCabinetList.parallelStream().forEach(e -> {
                //营业时间
                if (Objects.nonNull(e.getBusinessTime())) {
                    String businessTime = e.getBusinessTime();
                    if (Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                        e.setBusinessTimeType(ElectricityCabinetVO.ALL_DAY);
                    } else {
                        e.setBusinessTimeType(ElectricityCabinetVO.ILLEGAL_DATA);
                        Integer index = businessTime.indexOf("-");
                        if (!Objects.equals(index, -1) && index > 0) {
                            e.setBusinessTimeType(ElectricityCabinetVO.CUSTOMIZE_TIME);
                            Long beginTime = Long.valueOf(businessTime.substring(0, index));
                            Long endTime = Long.valueOf(businessTime.substring(index + 1));
                            e.setBeginTime(beginTime);
                            e.setEndTime(endTime);
                        }
                    }
                }
                //地区
                City city = cityService.queryByIdFromCache(e.getAreaId());
                if (Objects.nonNull(city)) {
                    e.setAreaName(city.getCity());
                    e.setPid(city.getPid());
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
                //TODO 在线更新柜机
                e.setElectricityBatteryTotal(electricityBatteryTotal);
                e.setNoElectricityBattery(noElectricityBattery);
                e.setFullyElectricityBattery(fullyElectricityBattery);
            });
        }
        return R.ok(electricityCabinetList.stream().sorted(Comparator.comparing(ElectricityCabinetVO::getCreateTime).reversed()).collect(Collectors.toList()));
    }


    @Override
    public R showInfoByDistance(ElectricityCabinetQuery electricityCabinetQuery) {
        List<ElectricityCabinetVO> electricityCabinetList = electricityCabinetMapper.showInfoByDistance(electricityCabinetQuery);
        List<ElectricityCabinetVO> electricityCabinets = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(electricityCabinetList)) {
            electricityCabinetList.parallelStream().forEach(e -> {
                //营业时间
                if (Objects.nonNull(e.getBusinessTime())) {
                    String businessTime = e.getBusinessTime();
                    if (Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                        e.setBusinessTimeType(ElectricityCabinetVO.ALL_DAY);
                    } else {
                        e.setBusinessTimeType(ElectricityCabinetVO.ILLEGAL_DATA);
                        Integer index = businessTime.indexOf("-");
                        if (!Objects.equals(index, -1) && index > 0) {
                            e.setBusinessTimeType(ElectricityCabinetVO.CUSTOMIZE_TIME);
                            Long beginTime = Long.valueOf(businessTime.substring(0, index));
                            Long endTime = Long.valueOf(businessTime.substring(index + 1));
                            e.setBeginTime(beginTime);
                            e.setEndTime(endTime);
                            Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                            Long now = System.currentTimeMillis();
                            if (firstToday + beginTime > now || firstToday + endTime < now) {
                                e.setIsBusiness(ElectricityCabinetVO.IS_NOT_BUSINESS);
                            } else {
                                e.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
                            }
                        }
                    }
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
                //TODO 在线更新柜机
                if (Objects.equals(e.getUsableStatus(), ElectricityCabinet.ELECTRICITY_CABINET_USABLE_STATUS) && Objects.equals(e.getPowerStatus(), ElectricityCabinet.ELECTRICITY_CABINET_POWER_STATUS)
                        && Objects.equals(e.getOnlineStatus(), ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS)) {
                    electricityCabinets.add(e);
                }
            });
        }
        return R.ok(electricityCabinets);
    }

    @Override
    public Integer queryByModelId(Integer id) {
        return electricityCabinetMapper.selectCount(Wrappers.<ElectricityCabinet>lambdaQuery().eq(ElectricityCabinet::getModelId, id).eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
    }

    @Override
    @Transactional
    public R disable(Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        ElectricityCabinet oldElectricityCabinet = queryByIdFromCache(id);
        if (Objects.isNull(oldElectricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        if (Objects.equals(oldElectricityCabinet.getUsableStatus(), ElectricityCabinet.ELECTRICITY_CABINET_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0012", "快递柜已禁用，不能重复操作");
        }
        ElectricityCabinet electricityCabinet = new ElectricityCabinet();
        electricityCabinet.setId(id);
        electricityCabinet.setUsableStatus(ElectricityCabinet.ELECTRICITY_CABINET_UN_USABLE_STATUS);
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinetMapper.update(electricityCabinet);
        //更新缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
        return R.ok();
    }

    @Override
    @Transactional
    public R reboot(Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        ElectricityCabinet oldElectricityCabinet = queryByIdFromCache(id);
        if (Objects.isNull(oldElectricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        ElectricityCabinet electricityCabinet = new ElectricityCabinet();
        electricityCabinet.setId(id);
        electricityCabinet.setUsableStatus(ElectricityCabinet.ELECTRICITY_CABINET_USABLE_STATUS);
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinetMapper.update(electricityCabinet);
        //更新缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
        return R.ok();
    }

    @Override
    public R queryOne(Integer id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        ElectricityCabinet electricityCabinet = queryByIdFromCache(id);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        //判断是否开通服务
        UserInfo userInfo = userInfoService.queryByUid(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found userInfo ");
            return R.fail("ELECTRICITY.0020", "未开通服务");
        }
        ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
        BeanUtil.copyProperties(electricityCabinet, electricityCabinetVO);
        //查满仓空仓数
        Integer electricityBatteryTotal = 0;
        Integer fullyElectricityBattery = 0;
        Integer noElectricityBattery = 0;
        Set<String> set = new HashSet();
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinetVO.getId());
        if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
            for (ElectricityCabinetBox electricityCabinetBox : electricityCabinetBoxList) {
                //满仓个数
                ElectricityBattery electricityBattery = electricityBatteryService.queryById(electricityCabinetBox.getElectricityBatteryId());
                if (Objects.nonNull(electricityBattery)) {
                    if (electricityBattery.getCapacity() >= electricityCabinet.getFullyCharged()) {
                        fullyElectricityBattery = fullyElectricityBattery + 1;
                    }
                    ElectricityBatteryModel electricityBatteryModel = electricityBatteryModelService.getElectricityBatteryModelById(electricityBattery.getModelId());
                    if (Objects.nonNull(electricityBatteryModel)) {
                        set.add(electricityBatteryModel.getVoltage() + "V" + electricityBatteryModel.getCapacity() + "M");
                    }
                }
            }
            //营业时间
            if (Objects.nonNull(electricityCabinetVO.getBusinessTime())) {
                String businessTime = electricityCabinetVO.getBusinessTime();
                if (Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                    electricityCabinetVO.setBusinessTimeType(ElectricityCabinetVO.ALL_DAY);
                } else {
                    electricityCabinetVO.setBusinessTimeType(ElectricityCabinetVO.ILLEGAL_DATA);
                    Integer index = businessTime.indexOf("-");
                    if (!Objects.equals(index, -1) && index > 1) {
                        electricityCabinetVO.setBusinessTimeType(ElectricityCabinetVO.CUSTOMIZE_TIME);
                        Long beginTime = Long.valueOf(businessTime.substring(0, index));
                        Long endTime = Long.valueOf(businessTime.substring(index + 1));
                        electricityCabinetVO.setBeginTime(beginTime);
                        electricityCabinetVO.setEndTime(endTime);
                        Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                        Long now = System.currentTimeMillis();
                        if (firstToday + beginTime > now || firstToday + endTime < now) {
                            electricityCabinetVO.setIsBusiness(ElectricityCabinetVO.IS_NOT_BUSINESS);
                        } else {
                            electricityCabinetVO.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
                        }
                    }
                }
            }
            //空仓
            noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
            //电池总数
            electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
        }
        if (noElectricityBattery <= 0) {
            return R.fail("ELECTRICITY.0008", "换电柜暂无空仓");
        }
        electricityCabinetVO.setElectricityBatteryTotal(electricityBatteryTotal);
        electricityCabinetVO.setNoElectricityBattery(noElectricityBattery);
        electricityCabinetVO.setFullyElectricityBattery(fullyElectricityBattery);
        electricityCabinetVO.setElectricityBatteryFormat(set);
        return R.ok(electricityCabinetVO);
    }

    @Override
    public R homeOne(Integer type) {
        if (type == 1) {
            //查用户
            Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1);
            Date dateBefor = cal.getTime();
            Long firstTodayBefor = DateUtil.beginOfDay(dateBefor).getTime();
            Long endToday = DateUtil.endOfDay(dateBefor).getTime();
            return getHomeOne(firstToday, firstTodayBefor, endToday);
        }
        if (type == 2) {
            //查用户
            Long firstWeek = DateUtil.beginOfWeek(new Date()).getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -7);
            Date dateBefor = cal.getTime();
            Long firstWeekBefor = DateUtil.beginOfWeek(dateBefor).getTime();
            Long endWeek = DateUtil.endOfWeek(dateBefor).getTime();
            return getHomeOne(firstWeek, firstWeekBefor, endWeek);
        }
        if (type == 3) {
            //查用户
            Long firstMonth = DateUtil.beginOfMonth(new Date()).getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -1);
            Date dateBefor = cal.getTime();
            Long firstMonthBefor = DateUtil.beginOfMonth(dateBefor).getTime();
            Long endMonth = DateUtil.endOfMonth(dateBefor).getTime();
            return getHomeOne(firstMonth, firstMonthBefor, endMonth);
        }
        if (type == 4) {
            //查用户
            Long firstQuarter = DateUtil.beginOfQuarter(new Date()).getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, ((int) cal.get(Calendar.MONTH) / 3 - 1) * 3 - (int) cal.get(Calendar.MONTH));
            Date dateBefor = cal.getTime();
            Long firstQuarterBefor = DateUtil.beginOfQuarter(dateBefor).getTime();
            Long endQuarter = DateUtil.endOfQuarter(dateBefor).getTime();
            return getHomeOne(firstQuarter, firstQuarterBefor, endQuarter);
        }
        if (type == 5) {
            //查用户
            Long firstYear = DateUtil.beginOfYear(new Date()).getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.YEAR, -1);
            Date dateBefor = cal.getTime();
            Long firstYearBefor = DateUtil.beginOfQuarter(dateBefor).getTime();
            Long endYear = DateUtil.endOfQuarter(dateBefor).getTime();
            return getHomeOne(firstYear, firstYearBefor, endYear);
        }
        return R.ok();
    }

    public R getHomeOne(Long first, Long firstBefor, Long end) {
        HashMap<String, HashMap<String, String>> homeOne = new HashMap<>();
        Long now = System.currentTimeMillis();
        Integer totalCount = userInfoService.homeOneTotal(first, now);
        Integer serviceCount = userInfoService.homeOneService(first, now);
        Integer MemberCardCount = userInfoService.homeOneMemberCard(first, now);
        Integer allTotalCount = userInfoService.homeOneTotal(0L, now);
        HashMap<String, String> userInfo = new HashMap<>();
        userInfo.put("totalCount", totalCount.toString());
        userInfo.put("serviceCount", serviceCount.toString());
        userInfo.put("MemberCardCount", MemberCardCount.toString());
        userInfo.put("allTotalCount", allTotalCount.toString());
        homeOne.put("userInfo", userInfo);
        //查收益
        BigDecimal nowMoney = electricityMemberCardOrderService.homeOne(first, now);
        BigDecimal beforMoney = electricityMemberCardOrderService.homeOne(firstBefor, end);
        BigDecimal totalMoney = electricityMemberCardOrderService.homeOne(0L, now);
        if(Objects.isNull(nowMoney)){
            nowMoney=BigDecimal.valueOf(0);
        }
        if(Objects.isNull(beforMoney)){
            beforMoney=BigDecimal.valueOf(0);
        }
        if(Objects.isNull(totalMoney)){
            totalMoney=BigDecimal.valueOf(0);
        }
        HashMap<String, String> moneyInfo = new HashMap<>();
        moneyInfo.put("nowMoney", nowMoney.toString());
        moneyInfo.put("beforMoney", beforMoney.toString());
        moneyInfo.put("totalMoney", totalMoney.toString());
        homeOne.put("moneyInfo", moneyInfo);
        //换电
        Integer nowCount = electricityCabinetOrderService.homeOneCount(first, now);
        Integer beforCount = electricityCabinetOrderService.homeOneCount(firstBefor, end);
        Integer count = electricityCabinetOrderService.homeOneCount(0L, now);
        //成功率
        BigDecimal successOrder = electricityCabinetOrderService.homeOneSuccess(first, now);
        HashMap<String, String> orderInfo = new HashMap<>();
        orderInfo.put("nowCount", nowCount.toString());
        orderInfo.put("beforCount", beforCount.toString());
        orderInfo.put("successOrder", successOrder.toString());
        orderInfo.put("totalCount", count.toString());
        homeOne.put("orderInfo", orderInfo);
        return R.ok(homeOne);
    }

    @Override
    public R homeTwo(Integer areaId) {
        HashMap<String, HashMap<String, String>> homeTwo = new HashMap<>();
        //门店
        Integer totalCount = storeService.homeTwoTotal(areaId);
        Integer businessCount = storeService.homeTwoBusiness(areaId);
        Integer batteryCount = storeService.homeTwoBattery(areaId);
        Integer carCount = storeService.homeTwoCar(areaId);
        HashMap<String, String> storeInfo = new HashMap<>();
        storeInfo.put("totalCount", totalCount.toString());
        storeInfo.put("businessCount", businessCount.toString());
        storeInfo.put("batteryCount", batteryCount.toString());
        storeInfo.put("carCount", carCount.toString());
        homeTwo.put("storeInfo", storeInfo);
        //换电柜
        List<ElectricityCabinet> electricityCabinetList = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL).eq(Objects.nonNull(areaId),ElectricityCabinet::getAreaId, areaId));
        Integer total = electricityCabinetList.size();
        Integer onlineCount = 0;
        Integer offlineCount = 0;
        if (ObjectUtil.isNotEmpty(electricityCabinetList)) {
            for (ElectricityCabinet electricityCabinet : electricityCabinetList) {
                //TODO 查询在线离线
            }
        }
        HashMap<String, String> electricityCabinetInfo = new HashMap<>();
        electricityCabinetInfo.put("totalCount", total.toString());
        electricityCabinetInfo.put("onlineCount", onlineCount.toString());
        electricityCabinetInfo.put("offlineCount", offlineCount.toString());
        homeTwo.put("electricityCabinetInfo", electricityCabinetInfo);
        //电池
        List<ElectricityBattery> electricityBatteryList = electricityBatteryService.homeTwo(areaId);
        Integer batteryTotal = electricityBatteryList.size();
        Integer cabinetCount = 0;
        Integer userCount = 0;
        if (ObjectUtil.isNotEmpty(electricityBatteryList)) {
            if (ObjectUtil.isNotEmpty(electricityBatteryList)) {
                for (ElectricityBattery electricityBattery : electricityBatteryList) {
                    if (Objects.equals(electricityBattery.getStatus(), ElectricityBattery.WARE_HOUSE_STATUS)) {
                        cabinetCount = cabinetCount + 1;
                        userCount = userCount + 1;
                    }
                    if (Objects.equals(electricityBattery.getStatus(), ElectricityBattery.LEASE_STATUS)) {
                        userCount = userCount + 1;
                    }
                }
            }
        }
        HashMap<String, String> electricityBatteryInfo = new HashMap<>();
        electricityBatteryInfo.put("batteryTotal", batteryTotal.toString());
        electricityBatteryInfo.put("cabinetCount", cabinetCount.toString());
        electricityBatteryInfo.put("userCount", userCount.toString());
        homeTwo.put("electricityBatteryInfo", electricityBatteryInfo);
        return R.ok(homeTwo);
    }

    @Override
    public R homeThree(Integer day) {
        HashMap<String, HashMap<String, Object>> homeThree = new HashMap<>();
        //用户人数
        Long endTimeMilliDay = DateUtil.endOfDay(new Date()).getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -(day - 1));
        Date date = calendar.getTime();
        DateTime startTime = DateUtil.beginOfDay(date);
        long startTimeMilliDay = startTime.getTime();
        List<HashMap<String, String>> totalCountList = userInfoService.homeThreeTotal(startTimeMilliDay, endTimeMilliDay);
        List<HashMap<String, String>> serviceCountList = userInfoService.homeThreeService(startTimeMilliDay, endTimeMilliDay);
        List<HashMap<String, String>> memberCardCountList = userInfoService.homeThreeMemberCard(startTimeMilliDay, endTimeMilliDay);
        HashMap<String, Object> userInfo = new HashMap<>();
        userInfo.put("totalCountList", totalCountList);
        userInfo.put("serviceCountList", serviceCountList);
        userInfo.put("MemberCardCountList", memberCardCountList);
        homeThree.put("userInfo", userInfo);
        //查收益
        List<HashMap<String, String>> moneyList = electricityMemberCardOrderService.homeThree(startTimeMilliDay, endTimeMilliDay);
        HashMap<String, Object> moneyInfo = new HashMap<>();
        moneyInfo.put("moneyList", moneyList);
        homeThree.put("moneyInfo", moneyInfo);
        //换电
        List<HashMap<String, String>> orderList = electricityCabinetOrderService.homeThree(startTimeMilliDay, endTimeMilliDay);
        HashMap<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("orderList", orderList);
        homeThree.put("orderInfo", orderInfo);
        return R.ok(homeThree);
    }

    @Override
    public R home() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        HashMap<String, String> homeInfo = new HashMap<>();
        Long firstMonth = DateUtil.beginOfMonth(new Date()).getTime();
        Long now = System.currentTimeMillis();
        Integer battery = null;
        Long cardDay = null;
        UserInfo userInfo = userInfoService.queryByUid(user.getUid());
        if (Objects.nonNull(userInfo)) {
            //我的电池
            if (Objects.nonNull(userInfo.getNowElectricityBatterySn()) && Objects.equals(userInfo.getServiceStatus(), UserInfo.IS_SERVICE_STATUS)) {
                ElectricityBattery electricityBattery = electricityBatteryService.queryByUserSn(userInfo.getNowElectricityBatterySn());
                if (Objects.nonNull(electricityBattery)) {
                    battery = electricityBattery.getCapacity();
                }
            }
            //套餐剩余天数
            if (Objects.nonNull(userInfo.getMemberCardExpireTime()) && Objects.nonNull(userInfo.getRemainingNumber()) && userInfo.getMemberCardExpireTime() > now) {
                cardDay = (userInfo.getMemberCardExpireTime() - now) / 1000 / 60 / 60 / 24;
            }
        }
        //本月换电
        Integer monthCount = electricityCabinetOrderService.homeMonth(user.getUid(), firstMonth, now);
        //总换电
        Integer totalCount = electricityCabinetOrderService.homeTotal(user.getUid());
        homeInfo.put("monthCount", monthCount.toString());
        homeInfo.put("totalCount", totalCount.toString());
        homeInfo.put("battery", String.valueOf(battery));
        homeInfo.put("cardDay", String.valueOf(cardDay));
        return R.ok(homeInfo);
    }


    private boolean isNoElectricityBattery(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
    }

    private boolean isElectricityBattery(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);
    }
}