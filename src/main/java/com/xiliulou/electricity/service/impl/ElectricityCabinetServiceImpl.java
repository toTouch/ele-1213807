package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.mapper.ElectricityCabinetMapper;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.PageUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.iot.entity.AliIotRsp;
import com.xiliulou.iot.entity.AliIotRspDetail;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.service.PubHardwareService;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shaded.org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
    @Autowired
    PubHardwareService pubHardwareService;
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    @Autowired
    ElectricityConfigService electricityConfigService;

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
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //操作频繁
        boolean result = redisService.setNx(ElectricityCabinetConstant.ELE_SAVE_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
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
        int insert = electricityCabinetMapper.insert(electricityCabinet);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //新增缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName(), electricityCabinet);
            //添加快递柜格挡
            electricityCabinetBoxService.batchInsertBoxByModelId(electricityCabinetModel, electricityCabinet.getId());
            return electricityCabinet;
        });
        return R.ok(electricityCabinet.getId());
    }

    @Override
    @Transactional
    public R edit(ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //操作频繁
        boolean result = redisService.setNx(ElectricityCabinetConstant.ELE_EDIT_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
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
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName(), electricityCabinet);
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
            redisService.deleteKeys(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
            electricityCabinetBoxService.batchDeleteBoxByElectricityCabinetId(id);
            return null;
        });
        return R.ok();
    }

    public static void main(String[] args) {
        System.out.println(Long.valueOf((new Double(Math.ceil(1.1))).longValue()));
    }

    @Override
    @DS("slave_1")
    public R queryList(ElectricityCabinetQuery electricityCabinetQuery) {
        Page page = PageUtil.getPage(electricityCabinetQuery.getOffset(), electricityCabinetQuery.getSize());
        electricityCabinetMapper.queryList(page, electricityCabinetQuery);
        if (ObjectUtil.isEmpty(page.getRecords())) {
            return R.ok(page);
        }
        List<ElectricityCabinetVO> electricityCabinetList = page.getRecords();
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
                Integer fullyElectricityBattery = electricityCabinetMapper.queryFullyElectricityBattery(e.getId());
                Integer electricityBatteryTotal = 0;
                Integer noElectricityBattery = 0;
                List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(e.getId());
                if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
                    //空仓
                    noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
                    //电池总数
                    electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
                }

                boolean result = deviceIsOnline(e.getProductKey(), e.getDeviceName());
                if (result) {
                    e.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS);
                    e.setPowerStatus(ElectricityCabinet.ELECTRICITY_CABINET_POWER_STATUS);
                } else {
                    e.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
                    e.setPowerStatus(ElectricityCabinet.ELECTRICITY_CABINET_NO_POWER_STATUS);
                }
                e.setElectricityBatteryTotal(electricityBatteryTotal);
                e.setNoElectricityBattery(noElectricityBattery);
                e.setFullyElectricityBattery(fullyElectricityBattery);
            });
        }
        page.setRecords(electricityCabinetList.stream().sorted(Comparator.comparing(ElectricityCabinetVO::getCreateTime).reversed()).collect(Collectors.toList()));
        return R.ok(page);
    }


    @Override
    @DS("slave_1")
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
                        e.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
                    } else {
                        e.setBusinessTimeType(ElectricityCabinetVO.ILLEGAL_DATA);
                        Integer index = businessTime.indexOf("-");
                        if (!Objects.equals(index, -1) && index > 0) {
                            e.setBusinessTimeType(ElectricityCabinetVO.CUSTOMIZE_TIME);
                            Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                            Long beginTime = getTime(totalBeginTime);
                            Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                            Long endTime = getTime(totalEndTime);
                            e.setBeginTime(totalBeginTime);
                            e.setEndTime(totalEndTime);
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
                Integer fullyElectricityBattery = electricityCabinetMapper.queryFullyElectricityBattery(e.getId());
                //查满仓空仓数
                Integer electricityBatteryTotal = 0;
                Integer noElectricityBattery = 0;
                Set<String> set = new HashSet();
                List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(e.getId());
                if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
                    //空仓
                    noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
                    //电池总数
                    electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
                }

                //电池型号
                List<BatteryFormat> batteryFormatList = electricityCabinetMapper.queryElectricityBatteryFormat(e.getId());
                if (ObjectUtil.isNotEmpty(batteryFormatList)) {
                    for (BatteryFormat batteryFormat : batteryFormatList) {
                        set.add(batteryFormat.getVoltage() + "V" + " " + batteryFormat.getCapacity() + "M");
                    }
                }

                e.setElectricityBatteryTotal(electricityBatteryTotal);
                e.setNoElectricityBattery(noElectricityBattery);
                e.setFullyElectricityBattery(fullyElectricityBattery);
                e.setElectricityBatteryFormat(set);

                //动态查询在线状态
                boolean result = deviceIsOnline(e.getProductKey(), e.getDeviceName());
                if (result) {
                    e.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS);
                    e.setPowerStatus(ElectricityCabinet.ELECTRICITY_CABINET_POWER_STATUS);
                } else {
                    e.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
                    e.setPowerStatus(ElectricityCabinet.ELECTRICITY_CABINET_NO_POWER_STATUS);
                }
                if (Objects.equals(e.getUsableStatus(), ElectricityCabinet.ELECTRICITY_CABINET_USABLE_STATUS) && Objects.equals(e.getPowerStatus(), ElectricityCabinet.ELECTRICITY_CABINET_POWER_STATUS)
                        && Objects.equals(e.getOnlineStatus(), ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS)) {
                    electricityCabinets.add(e);
                }
            });
        }
        return R.ok(electricityCabinets.stream().sorted(Comparator.comparing(ElectricityCabinetVO::getDistance)).collect(Collectors.toList()));
    }

    @Override
    public boolean deviceIsOnline(String productKey, String deviceName) {
        AliIotRsp aliIotRsp = pubHardwareService.queryDeviceInfoFromIot(productKey, deviceName);
        log.info("aliIotRsp is --> {}", aliIotRsp);
        if (Objects.isNull(aliIotRsp)) {
            return false;
        }

        AliIotRspDetail detail = aliIotRsp.getData();
        if (Objects.isNull(detail)) {
            return false;
        }

        String status = Optional.ofNullable(aliIotRsp.getData().getStatus()).orElse("UNKNOW").toLowerCase();
        if ("online".equalsIgnoreCase(status)) {
            return true;
        }
        return false;
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
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName(), electricityCabinet);
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
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName(), electricityCabinet);
        return R.ok();
    }


    @Override
    public R homeOne(Integer type) {
        if (type == 1) {
            //查用户
            Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1);
            Date dateBefore = cal.getTime();
            Long firstTodayBefore = DateUtil.beginOfDay(dateBefore).getTime();
            Long endToday = DateUtil.endOfDay(dateBefore).getTime();
            return getHomeOne(firstToday, firstTodayBefore, endToday);
        }
        if (type == 2) {
            //查用户
            Long firstWeek = DateUtil.beginOfWeek(new Date()).getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -7);
            Date dateBefore = cal.getTime();
            Long firstWeekBefore = DateUtil.beginOfWeek(dateBefore).getTime();
            Long endWeek = DateUtil.endOfWeek(dateBefore).getTime();
            return getHomeOne(firstWeek, firstWeekBefore, endWeek);
        }
        if (type == 3) {
            //查用户
            Long firstMonth = DateUtil.beginOfMonth(new Date()).getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -1);
            Date dateBefore = cal.getTime();
            Long firstMonthBefore = DateUtil.beginOfMonth(dateBefore).getTime();
            Long endMonth = DateUtil.endOfMonth(dateBefore).getTime();
            return getHomeOne(firstMonth, firstMonthBefore, endMonth);
        }
        if (type == 4) {
            //查用户
            Long firstQuarter = DateUtil.beginOfQuarter(new Date()).getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, ((int) cal.get(Calendar.MONTH) / 3 - 1) * 3 - (int) cal.get(Calendar.MONTH));
            Date dateBefore = cal.getTime();
            Long firstQuarterBefore = DateUtil.beginOfQuarter(dateBefore).getTime();
            Long endQuarter = DateUtil.endOfQuarter(dateBefore).getTime();
            return getHomeOne(firstQuarter, firstQuarterBefore, endQuarter);
        }
        if (type == 5) {
            //查用户
            Long firstYear = DateUtil.beginOfYear(new Date()).getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.YEAR, -1);
            Date dateBefore = cal.getTime();
            Long firstYearBefore = DateUtil.beginOfYear(dateBefore).getTime();
            Long endYear = DateUtil.endOfYear(dateBefore).getTime();
            return getHomeOne(firstYear, firstYearBefore, endYear);
        }
        return R.ok();
    }

    public R getHomeOne(Long first, Long firstBefore, Long end) {
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
        BigDecimal beforeMoney = electricityMemberCardOrderService.homeOne(firstBefore, end);
        BigDecimal totalMoney = electricityMemberCardOrderService.homeOne(0L, now);
        if (Objects.isNull(nowMoney)) {
            nowMoney = BigDecimal.valueOf(0);
        }
        if (Objects.isNull(beforeMoney)) {
            beforeMoney = BigDecimal.valueOf(0);
        }
        if (Objects.isNull(totalMoney)) {
            totalMoney = BigDecimal.valueOf(0);
        }
        HashMap<String, String> moneyInfo = new HashMap<>();
        moneyInfo.put("nowMoney", nowMoney.toString());
        moneyInfo.put("beforMoney", beforeMoney.toString());
        moneyInfo.put("totalMoney", totalMoney.toString());
        homeOne.put("moneyInfo", moneyInfo);
        //换电
        Integer nowCount = electricityCabinetOrderService.homeOneCount(first, now);
        Integer beforeCount = electricityCabinetOrderService.homeOneCount(firstBefore, end);
        Integer count = electricityCabinetOrderService.homeOneCount(0L, now);
        //成功率
        BigDecimal successOrder = electricityCabinetOrderService.homeOneSuccess(first, now);
        HashMap<String, String> orderInfo = new HashMap<>();
        orderInfo.put("nowCount", nowCount.toString());
        orderInfo.put("beforCount", beforeCount.toString());
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
        List<ElectricityCabinet> electricityCabinetList = electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL).eq(Objects.nonNull(areaId), ElectricityCabinet::getAreaId, areaId));
        Integer total = electricityCabinetList.size();
        Integer onlineCount = 0;
        Integer offlineCount = 0;
        if (ObjectUtil.isNotEmpty(electricityCabinetList)) {
            for (ElectricityCabinet electricityCabinet : electricityCabinetList) {
                boolean result = deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
                if (result) {
                    onlineCount++;
                } else {
                    offlineCount++;
                }
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
        Double battery = null;
        Long cardDay = null;
        Integer serviceStatus = 1;
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
            serviceStatus = userInfo.getServiceStatus();
        }

        //本月换电
        Integer monthCount = electricityCabinetOrderService.homeMonth(user.getUid(), firstMonth, now);
        //总换电
        Integer totalCount = electricityCabinetOrderService.homeTotal(user.getUid());
        homeInfo.put("monthCount", monthCount.toString());
        homeInfo.put("totalCount", totalCount.toString());
        homeInfo.put("battery", String.valueOf(battery));
        homeInfo.put("cardDay", String.valueOf(cardDay));
        homeInfo.put("serviceStatus", String.valueOf(serviceStatus));
        return R.ok(homeInfo);
    }

    @Override
    public R queryByDevice(String productKey, String deviceName, String deviceSecret) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //动态查询在线状态
        boolean eleResult = deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //2.判断用户是否有电池是否有月卡
        UserInfo userInfo = userInfoService.queryByUid(user.getUid());
        //用户是否可用
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! not found userInfo ");
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        //判断是否开通服务
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.NO_SERVICE_STATUS)) {
            log.error("ELECTRICITY  ERROR! not found userInfo ");
            return R.fail("ELECTRICITY.0021", "未开通服务");
        }
        //判断是否电池
        if (Objects.isNull(userInfo.getNowElectricityBatterySn())) {
            log.error("ELECTRICITY  ERROR! not found userInfo ");
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }
        //判断用户是否开通月卡
        if (Objects.isNull(userInfo.getMemberCardExpireTime()) || Objects.isNull(userInfo.getRemainingNumber())) {
            log.error("ELECTRICITY  ERROR! not found memberCard ");
            return R.fail("ELECTRICITY.0022", "未开通月卡");
        }
        Long now = System.currentTimeMillis();
        if (userInfo.getMemberCardExpireTime() < now || userInfo.getRemainingNumber() == 0) {
            log.error("ELECTRICITY  ERROR! not found memberCard ");
            return R.fail("ELECTRICITY.0023", "月卡已过期");
        }
        ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
        BeanUtil.copyProperties(electricityCabinet, electricityCabinetVO);

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
                    Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                    Long beginTime = getTime(totalBeginTime);
                    Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                    Long endTime = getTime(totalEndTime);
                    electricityCabinetVO.setBeginTime(totalBeginTime);
                    electricityCabinetVO.setEndTime(totalEndTime);
                    Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                    if (firstToday + beginTime > now || firstToday + endTime < now) {
                        return R.fail("ELECTRICITY.0017", "换电柜已打烊");
                    } else {
                        electricityCabinetVO.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
                    }
                }
            }
        }

        //查满仓空仓数
        Integer fullyElectricityBattery = electricityCabinetMapper.queryFullyElectricityBattery(electricityCabinet.getId());
        if (fullyElectricityBattery <= 0) {
            return R.fail("ELECTRICITY.0026", "换电柜暂无满电电池");
        }
        //查满仓空仓数
        Integer electricityBatteryTotal = 0;
        Integer noElectricityBattery = 0;
        Set<String> set = new HashSet();
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinetVO.getId());
        if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
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
    public ElectricityCabinet queryFromCacheByProductAndDeviceName(String productKey, String deviceName) {
        //先查缓存
        ElectricityCabinet cacheElectricityCabinet = redisService.getWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_DEVICE + productKey + deviceName, ElectricityCabinet.class);
        if (Objects.nonNull(cacheElectricityCabinet)) {
            return cacheElectricityCabinet;
        }
        //缓存没有再查数据库
        ElectricityCabinet electricityCabinet = electricityCabinetMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinet>()
                .eq(ElectricityCabinet::getProductKey, productKey).eq(ElectricityCabinet::getDeviceName, deviceName).eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
        if (Objects.isNull(electricityCabinet)) {
            return null;
        }
        //放入缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_DEVICE + productKey + deviceName, electricityCabinet);
        return electricityCabinet;
    }

    @Override
    public R checkOpenSessionId(String sessionId) {
        String s = redisService.get(ElectricityCabinetConstant.ELE_OPERATOR_CACHE_KEY + sessionId);
        if (StrUtil.isEmpty(s)) {
            return R.ok("0001");
        }
        if ("true".equalsIgnoreCase(s)) {
            return R.ok("0002");
        } else {
            return R.ok("0003");
        }
    }

    @Override
    public R sendCommandToEleForOuter(EleOuterCommandQuery eleOuterCommandQuery) {
        //不合法的参数
        if (Objects.isNull(eleOuterCommandQuery.getCommand())
                || Objects.isNull(eleOuterCommandQuery.getDeviceName())
                || Objects.isNull(eleOuterCommandQuery.getProductKey())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        String sessionId = UUID.randomUUID().toString().replace("-", "");
        eleOuterCommandQuery.setSessionId(sessionId);

        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(eleOuterCommandQuery.getProductKey(), eleOuterCommandQuery.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //不合法的命令
        if (!HardwareCommand.ELE_COMMAND_MAPS.containsKey(eleOuterCommandQuery.getCommand())) {
            return R.fail("ELECTRICITY.0036", "不合法的命令");
        }

        if(Objects.equals(HardwareCommand.ELE_COMMAND_CELL_ALL_OPEN_DOOR,eleOuterCommandQuery.getCommand())){
            List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinet.getId());
            if (ObjectUtil.isEmpty(electricityCabinetBoxList)) {
                return R.fail("ELECTRICITY.0014", "换电柜没有仓门，不能开门");
            }
            HashMap<String, Object> dataMap = Maps.newHashMap();
            List<String> cellList = new ArrayList<>();
            for (ElectricityCabinetBox electricityCabinetBox : electricityCabinetBoxList) {
                cellList.add(electricityCabinetBox.getCellNo());
            }
            dataMap.put("cell_list", cellList);
            eleOuterCommandQuery.setData(dataMap);
        }

        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(eleOuterCommandQuery.getSessionId())
                .data(eleOuterCommandQuery.getData())
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(HardwareCommand.ELE_COMMAND_CELL_UPDATE)
                .build();

        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        //发送命令失败
        if (!result.getLeft()) {
            return R.fail("ELECTRICITY.0037", "发送命令失败");
        }
        return R.ok(sessionId);
    }

    @Override
    public R queryByDeviceOuter(String productKey, String deviceName, String deviceSecret) {
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        Long now = System.currentTimeMillis();
        ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
        BeanUtil.copyProperties(electricityCabinet, electricityCabinetVO);

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
                    Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                    Long beginTime = getTime(totalBeginTime);
                    Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                    Long endTime = getTime(totalEndTime);
                    electricityCabinetVO.setBeginTime(totalBeginTime);
                    electricityCabinetVO.setEndTime(totalEndTime);
                    Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                    if (firstToday + beginTime > now || firstToday + endTime < now) {
                        electricityCabinetVO.setIsBusiness(ElectricityCabinetVO.IS_NOT_BUSINESS);
                    } else {
                        electricityCabinetVO.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
                    }
                }
            }
        }

        //查满仓空仓数
        Integer fullyElectricityBattery = electricityCabinetMapper.queryFullyElectricityBattery(electricityCabinet.getId());
        //查满仓空仓数
        Integer electricityBatteryTotal = 0;
        Integer noElectricityBattery = 0;
        Set<String> set = new HashSet();
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinetVO.getId());
        if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
            //空仓
            noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
            //电池总数
            electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
        }

        //换电柜名称换成平台名称
        String name=null;
        ElectricityConfig electricityConfig=electricityConfigService.queryOne();
        if(Objects.nonNull(electricityConfig)){
            name=electricityConfig.getName();
        }

        electricityCabinetVO.setName(name);
        electricityCabinetVO.setElectricityBatteryTotal(electricityBatteryTotal);
        electricityCabinetVO.setNoElectricityBattery(noElectricityBattery);
        electricityCabinetVO.setFullyElectricityBattery(fullyElectricityBattery);
        electricityCabinetVO.setElectricityBatteryFormat(set);
        return R.ok(electricityCabinetVO);
    }


    private boolean isNoElectricityBattery(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
    }

    private boolean isElectricityBattery(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);
    }

    public Long getTime(Long time) {
        Date date1 = new Date(time);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = dateFormat.format(date1);
        Date date2 = null;
        try {
            date2 = dateFormat.parse(format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Long ts = date2.getTime();
        return time - ts;
    }
}