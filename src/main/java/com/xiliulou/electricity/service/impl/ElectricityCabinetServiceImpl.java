package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.mapper.ElectricityCabinetMapper;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.iot.entity.AliIotRsp;
import com.xiliulou.iot.entity.AliIotRspDetail;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.service.IotAcsService;
import com.xiliulou.iot.service.PubHardwareService;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.storage.config.StorageConfig;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    UserTypeFactory userTypeFactory;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    FranchiseeBindElectricityBatteryService franchiseeBindElectricityBatteryService;
    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    @Autowired
    BatteryOtherPropertiesService batteryOtherPropertiesService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    ExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("electricityCabinetServiceExecutor", 5, "ELECTRICITY_CABINET_SERVICE_EXECUTOR");
    @Autowired
    TenantService tenantService;
    @Autowired
    private IotAcsService iotAcsService;
    @Autowired
    EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    @Autowired
    EleRefundOrderService refundOrderService;
    @Autowired
    ElectricityCarService electricityCarService;

    @Autowired
    UserService userService;

    @Autowired
    ElectricityCabinetFileService electricityCabinetFileService;
    @Autowired
    StorageConfig storageConfig;

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinet queryByIdFromCache(Integer id) {
        //先查缓存
        ElectricityCabinet cacheElectricityCabinet = redisService.getWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET + id, ElectricityCabinet.class);
        if (Objects.nonNull(cacheElectricityCabinet)) {
            return cacheElectricityCabinet;
        }
        //缓存没有再查数据库
        ElectricityCabinet electricityCabinet = electricityCabinetMapper.selectById(id);
        if (Objects.isNull(electricityCabinet)) {
            return null;
        }
        //放入缓存
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET + id, electricityCabinet);
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
        return this.electricityCabinetMapper.updateById(electricityCabinet);

    }

    @Override
    @Transactional
    public R save(ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //操作频繁
        boolean result = redisService.setNx(CacheConstant.ELE_SAVE_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //换电柜
        ElectricityCabinet electricityCabinet = new ElectricityCabinet();
        BeanUtil.copyProperties(electricityCabinetAddAndUpdate, electricityCabinet);
        electricityCabinet.setTenantId(tenantId);

        //填充参数
        if (Objects.isNull(electricityCabinet.getOnlineStatus())) {
            electricityCabinet.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
        }
        if (Objects.equals(electricityCabinetAddAndUpdate.getBusinessTimeType(), ElectricityCabinetAddAndUpdate.ALL_DAY)) {
            electricityCabinet.setBusinessTime(ElectricityCabinetAddAndUpdate.ALL_DAY);
        }

        //判断参数
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

        //查找快递柜型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(electricityCabinet.getModelId());
        if (Objects.isNull(electricityCabinetModel)) {
            return R.fail("ELECTRICITY.0004", "未找到换电柜型号");
        }

        int insert = electricityCabinetMapper.insert(electricityCabinet);
        DbUtils.dbOperateSuccessThen(insert, () -> {

            //新增缓存
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName() + tenantId, electricityCabinet);

            //添加快递柜格挡
            electricityCabinetBoxService.batchInsertBoxByModelId(electricityCabinetModel, electricityCabinet.getId());
            return electricityCabinet;
        });
        return R.ok(electricityCabinet.getId());
    }

    @Override
    @Transactional
    public R edit(ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //操作频繁
        boolean result = redisService.setNx(CacheConstant.ELE_EDIT_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //换电柜
        ElectricityCabinet electricityCabinet = new ElectricityCabinet();
        BeanUtil.copyProperties(electricityCabinetAddAndUpdate, electricityCabinet);
        ElectricityCabinet oldElectricityCabinet = queryByIdFromCache(electricityCabinet.getId());
        if (Objects.isNull(oldElectricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //判断参数
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

        int update = electricityCabinetMapper.updateById(electricityCabinet);
        DbUtils.dbOperateSuccessThen(update, () -> {

            //更新缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId());

            //，key变化 先删除老的，以免老的删不掉
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + oldElectricityCabinet.getProductKey() + oldElectricityCabinet.getDeviceName() + oldElectricityCabinet.getTenantId());

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
        int update = electricityCabinetMapper.updateById(electricityCabinet);
        DbUtils.dbOperateSuccessThen(update, () -> {

            //删除缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + id);
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName() + electricityCabinet.getTenantId());

            //删除格挡
            electricityCabinetBoxService.batchDeleteBoxByElectricityCabinetId(id);
            return null;
        });
        return R.ok();
    }

    @Override
    @DS("slave_1")
    public R queryList(ElectricityCabinetQuery electricityCabinetQuery) {

        List<ElectricityCabinetVO> electricityCabinetList = electricityCabinetMapper.queryList(electricityCabinetQuery);
        if (ObjectUtil.isEmpty(electricityCabinetList)) {
            return R.ok();
        }
        if (ObjectUtil.isNotEmpty(electricityCabinetList)) {
            electricityCabinetList.parallelStream().forEach(e -> {


                //营业时间
                if (Objects.nonNull(e.getBusinessTime())) {
                    String businessTime = e.getBusinessTime();
                    if (Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                        e.setBusinessTimeType(ElectricityCabinetVO.ALL_DAY);
                    } else {
                        e.setBusinessTimeType(ElectricityCabinetVO.ILLEGAL_DATA);
                        int index = businessTime.indexOf("-");
                        if (!Objects.equals(index, -1) && index > 0) {
                            e.setBusinessTimeType(ElectricityCabinetVO.CUSTOMIZE_TIME);
                            Long beginTime = Long.valueOf(businessTime.substring(0, index));
                            Long endTime = Long.valueOf(businessTime.substring(index + 1));
                            e.setBeginTime(beginTime);
                            e.setEndTime(endTime);
                        }
                    }
                }

                //查找型号名称
                ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(e.getModelId());
                if (Objects.nonNull(electricityCabinetModel)) {
                    e.setModelName(electricityCabinetModel.getName());
                }

                //查满仓空仓数
                Integer fullyElectricityBattery = queryFullyElectricityBattery(e.getId(), "-1");
                int electricityBatteryTotal = 0;
                int noElectricityBattery = 0;
                int batteryInElectricity = 0;
                List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(e.getId());
                if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {

                    //空仓
                    noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();

                    //禁用的仓门
                    batteryInElectricity = (int) electricityCabinetBoxList.stream().filter(this::isBatteryInElectricity).count();

                    //电池总数
                    electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
                }

                boolean result = deviceIsOnline(e.getProductKey(), e.getDeviceName());

                ElectricityCabinet item = new ElectricityCabinet();
                item.setUpdateTime(System.currentTimeMillis());
                item.setId(e.getId());

                if (result) {
                    e.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS);
                    item.setOnlineStatus(e.getOnlineStatus());
                    checkCupboardStatusAndUpdateDiff(true, item);
                } else {
                    e.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
                    item.setOnlineStatus(e.getOnlineStatus());
                    checkCupboardStatusAndUpdateDiff(false, item);
                }
                e.setElectricityBatteryTotal(electricityBatteryTotal);
                e.setNoElectricityBattery(noElectricityBattery);
                e.setFullyElectricityBattery(fullyElectricityBattery);
                e.setBatteryInElectricity(batteryInElectricity);

                //是否锁住
                int isLock = 0;
                String LockResult = redisService.get(CacheConstant.UNLOCK_CABINET_CACHE + e.getId());
                if (StringUtil.isNotEmpty(LockResult)) {
                    isLock = 1;
                }
                e.setIsLock(isLock);
            });
        }
        electricityCabinetList.stream().sorted(Comparator.comparing(ElectricityCabinetVO::getCreateTime).reversed()).collect(Collectors.toList());
        return R.ok(electricityCabinetList);
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
                        int index = businessTime.indexOf("-");
                        if (!Objects.equals(index, -1) && index > 0) {
                            e.setBusinessTimeType(ElectricityCabinetVO.CUSTOMIZE_TIME);
                            Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                            Long beginTime = getTime(totalBeginTime);
                            Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                            Long endTime = getTime(totalEndTime);
                            e.setBeginTime(totalBeginTime);
                            e.setEndTime(totalEndTime);
                            Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                            long now = System.currentTimeMillis();
                            if (firstToday + beginTime > now || firstToday + endTime < now) {
                                e.setIsBusiness(ElectricityCabinetVO.IS_NOT_BUSINESS);
                            } else {
                                e.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
                            }
                        }
                    }
                }

                //查满仓空仓数
                Integer fullyElectricityBattery = queryFullyElectricityBattery(e.getId(), "-1");

                //查满仓空仓数
                int electricityBatteryTotal = 0;
                int noElectricityBattery = 0;
                List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(e.getId());
                if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {

                    //空仓
                    noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();

                    //电池总数
                    electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
                }

                e.setElectricityBatteryTotal(electricityBatteryTotal);
                e.setNoElectricityBattery(noElectricityBattery);
                e.setFullyElectricityBattery(fullyElectricityBattery);

                ElectricityCabinet item = new ElectricityCabinet();
                item.setUpdateTime(System.currentTimeMillis());
                item.setId(e.getId());

                //动态查询在线状态
                boolean result = deviceIsOnline(e.getProductKey(), e.getDeviceName());
                if (result) {
                    e.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS);
                    item.setOnlineStatus(e.getOnlineStatus());
                    checkCupboardStatusAndUpdateDiff(true, item);
                } else {
                    e.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
                    item.setOnlineStatus(e.getOnlineStatus());
                    checkCupboardStatusAndUpdateDiff(false, item);
                }
                //电柜不在线也返回，可离线换电
                if (Objects.equals(e.getUsableStatus(), ElectricityCabinet.ELECTRICITY_CABINET_USABLE_STATUS)) {
                    electricityCabinets.add(e);
                }
            });
        }
        return R.ok(electricityCabinets.stream().sorted(Comparator.comparing(ElectricityCabinetVO::getDistance)).collect(Collectors.toList()));
    }

    @Override
    public Integer queryFullyElectricityBattery(Integer id, String batteryType) {
        List<Long> ids = electricityCabinetMapper.queryFullyElectricityBattery(id, batteryType);
        Integer totalCount = ids.size();
        return totalCount;
    }

    public Triple<Boolean, String, Object> queryFullyElectricityBatteryByExchangeOrder(Integer id, String batteryType, Long franchiseeId, Integer tenantId) {

        List<Long> ids = electricityCabinetMapper.queryFullyElectricityBattery(id, batteryType);

        Integer count = 0;
        if (ObjectUtils.isEmpty(ids)) {
            //检测是否开启低电量换电并且查询到符合标准的最低换电电量标准
            Double fullyCharged = checkLowBatteryExchangeMinimumBatteryPowerStandard(tenantId, id);
            ids = electricityCabinetMapper.queryFullyElectricityBatteryForLowBatteryExchange(id, batteryType, fullyCharged);
            if (ObjectUtils.isEmpty(ids)) {
                return Triple.of(false, "0", "换电柜暂无满电电池");
            }
            for (Long item : ids) {
                FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = franchiseeBindElectricityBatteryService.queryByBatteryIdAndFranchiseeId(item, franchiseeId);
                if (Objects.nonNull(franchiseeBindElectricityBattery)) {
                    count++;
                }
            }

            if (count < 1) {
                return Triple.of(false, "0", "加盟商未绑定满电电池");
            }

            return Triple.of(false, "0", "换电柜暂无满电电池");
        }

        for (Long item : ids) {
            FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = franchiseeBindElectricityBatteryService.queryByBatteryIdAndFranchiseeId(item, franchiseeId);
            if (Objects.nonNull(franchiseeBindElectricityBattery)) {
                count++;
            }
        }

        if (count < 1) {
            return Triple.of(false, "0", "加盟商未绑定满电电池");
        }

        return Triple.of(true, count.toString(), null);
    }

    public Triple<Boolean, String, Object> queryFullyElectricityBatteryByOrder(Integer id, String batteryType, Long franchiseeId) {

        List<Long> ids = electricityCabinetMapper.queryFullyElectricityBattery(id, batteryType);
        if (ObjectUtils.isEmpty(ids)) {
            return Triple.of(false, "0", "换电柜暂无满电电池");
        }

        Integer count = 0;
        for (Long item : ids) {
            FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = franchiseeBindElectricityBatteryService.queryByBatteryIdAndFranchiseeId(item, franchiseeId);
            if (Objects.nonNull(franchiseeBindElectricityBattery)) {
                count++;
            }
        }

        if (count < 1) {
            return Triple.of(false, "0", "加盟商未绑定满电电池");
        }

        return Triple.of(true, count.toString(), null);
    }

    @Override
    public boolean deviceIsOnline(String productKey, String deviceName) {
        AliIotRsp aliIotRsp = pubHardwareService.queryDeviceInfoFromIot(productKey, deviceName);
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
    public R updateStatus(Integer id, Integer usableStatus) {
        if (Objects.isNull(id) || Objects.isNull(usableStatus)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        //换电柜
        ElectricityCabinet oldElectricityCabinet = queryByIdFromCache(id);
        if (Objects.isNull(oldElectricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        ElectricityCabinet electricityCabinet = new ElectricityCabinet();
        BeanUtil.copyProperties(oldElectricityCabinet, electricityCabinet);
        electricityCabinet.setId(id);
        electricityCabinet.setUsableStatus(usableStatus);
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinetMapper.updateById(electricityCabinet);

        //更新缓存
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);

        //，key变化 先删除老的，以免老的删不掉
        redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + oldElectricityCabinet.getProductKey() + oldElectricityCabinet.getDeviceName() + oldElectricityCabinet.getTenantId());
        //更新缓存
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName() + electricityCabinet.getTenantId(), electricityCabinet);
        return R.ok();
    }

    @Override
    public R homeOne(Long beginTime, Long endTime) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        HashMap<String, String> homeOne = new HashMap<>();
        //用户数
        homeOne.put("userCount", null);
        //总收益
        homeOne.put("moneyCount", null);
        //电柜数
        homeOne.put("eleCount", "0");
        //订单数
        homeOne.put("orderCount", "0");
        //门店数
        homeOne.put("storeCount", null);
        //在线电柜
        homeOne.put("onlineEleCount", "0");
        //离线电柜
        homeOne.put("offlineEleCount", "0");
        //成功率
        homeOne.put("successCount", "0");

        //1、查用户
        CompletableFuture<Void> successUserFuture = CompletableFuture.runAsync(() -> {
            if (Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                    || Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
                Integer userCount = userInfoService.homeOne(beginTime, endTime, tenantId);
                homeOne.put("userCount", userCount.toString());
            }
        }, executorService).exceptionally(e -> {
            log.error("QUERY home user ERROR! uid={}", user.getUid(), e);
            return null;
        });

        //2、换电柜
        boolean flag = true;
        List<Integer> eleIdList = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByUserType(user);
            if (ObjectUtil.isEmpty(eleIdList)) {
                flag = false;
            }
        }

        //2、查换电柜相关
        List<Integer> finalEleIdList = eleIdList;
        boolean finalFlag = flag;
        CompletableFuture<Void> successOrderFuture = CompletableFuture.runAsync(() -> {
            if (finalFlag) {
                //换电次数
                Integer orderCount = electricityCabinetOrderService.homeOneCount(beginTime, endTime, finalEleIdList, tenantId);
                //换电成功率
                BigDecimal successOrder = electricityCabinetOrderService.homeOneSuccess(beginTime, endTime, finalEleIdList, tenantId);

                homeOne.put("orderCount", orderCount.toString());
                homeOne.put("successCount", successOrder.toString());

                //电柜
                List<ElectricityCabinet> electricityCabinetList = null;
                if (Objects.equals(user.getType(), User.TYPE_USER_SUPER) || Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
                    //1、直接查柜子
                    electricityCabinetList = this.electricityCabinetMapper.homeOne(finalEleIdList, tenantId);
                } else if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
                    //1、查代理商
                    Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
                    //2、再找代理商下的门店
                    List<Store> storeList = storeService.queryByFranchiseeId(franchisee.getId());
                    electricityCabinetList = new ArrayList<>();
                    //3、再找门店绑定的柜子
                    for (Store store : storeList) {
                        List<ElectricityCabinet> storeElectricityCabinetList = electricityCabinetService.queryByStoreId(store.getId());
                        electricityCabinetList.addAll(storeElectricityCabinetList);
                    }
                } else {
                    //1、直接找门店
                    Store store = storeService.queryByUid(user.getUid());
                    //2、再找门店绑定的柜子
                    electricityCabinetList = electricityCabinetService.queryByStoreId(store.getId());
                }

                Integer eleCount = electricityCabinetList.size();
                Integer onlineEleCount = 0;
                Integer offlineEleCount = 0;
                if (ObjectUtil.isNotEmpty(electricityCabinetList)) {
                    for (ElectricityCabinet electricityCabinet : electricityCabinetList) {
                        boolean result = deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
                        if (result) {
                            onlineEleCount++;
                        } else {
                            offlineEleCount++;
                        }
                    }
                }
                homeOne.put("eleCount", eleCount.toString());
                homeOne.put("onlineEleCount", onlineEleCount.toString());
                homeOne.put("offlineEleCount", offlineEleCount.toString());
            }

        }, executorService).exceptionally(e -> {
            log.error("QUERY home order ERROR! uid={}", user.getUid(), e);
            return null;
        });

        //3、查月卡
        CompletableFuture<Void> successCardFuture = CompletableFuture.runAsync(() -> {
            if (Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                    || Objects.equals(user.getType(), User.TYPE_USER_OPERATE)
                    || Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {

                BigDecimal moneyCount = null;
                //查月卡
                if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
                    Franchisee franchisee = franchiseeService.queryByUid(user.getUid());

                    List<Integer> cardIdList = new ArrayList<>();
                    if (Objects.nonNull(franchisee)) {

                        //月卡
                        List<ElectricityMemberCard> electricityMemberCardList = electricityMemberCardService.queryByFranchisee(franchisee.getId());
                        if (ObjectUtil.isNotEmpty(electricityMemberCardList)) {
                            for (ElectricityMemberCard electricityMemberCard : electricityMemberCardList) {
                                cardIdList.add(electricityMemberCard.getId());
                            }
                        }
                    }
                    if (ObjectUtil.isNotEmpty(cardIdList)) {
                        moneyCount = electricityMemberCardOrderService.homeOne(beginTime, endTime, cardIdList, tenantId);
                    }
                } else {
                    moneyCount = electricityMemberCardOrderService.homeOne(beginTime, endTime, null, tenantId);
                }

                if (Objects.isNull(moneyCount)) {
                    moneyCount = BigDecimal.valueOf(0);
                }
                homeOne.put("moneyCount", moneyCount.toString());
            }
        }, executorService).exceptionally(e -> {
            log.error("QUERY home card ERROR! uid={}", user.getUid(), e);
            return null;
        });

        //4、门店
        CompletableFuture<Void> successStoreFuture = CompletableFuture.runAsync(() -> {
            if (Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                    || Objects.equals(user.getType(), User.TYPE_USER_OPERATE)
                    || Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {

                Integer storeCount = 0;
                //查用户
                if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
                    Franchisee franchisee = franchiseeService.queryByUid(user.getUid());

                    List<Long> storeIdList = new ArrayList<>();
                    if (Objects.nonNull(franchisee)) {
                        List<Store> franchiseeBindList = storeService.queryByFranchiseeId(franchisee.getId());
                        if (ObjectUtil.isNotEmpty(franchiseeBindList)) {
                            for (Store store : franchiseeBindList) {
                                storeIdList.add(store.getId());
                            }
                        }
                    }
                    if (ObjectUtil.isNotEmpty(storeIdList)) {
                        storeCount = storeService.homeOne(storeIdList, tenantId);

                    }
                } else {
                    storeCount = storeService.homeOne(null, tenantId);
                }
                homeOne.put("storeCount", storeCount.toString());
            }
        }, executorService).exceptionally(e -> {
            log.error("QUERY home store ERROR! uid={}", user.getUid(), e);
            return null;
        });

        CompletableFuture<Void> resultComplete = CompletableFuture.allOf(successUserFuture, successOrderFuture, successCardFuture, successStoreFuture);

        try {
            resultComplete.get(10, TimeUnit.SECONDS);
        } catch (
                Exception e) {
            log.error("operateOrderCount ERROR! uid={}", user.getUid(), e);
        }

        return R.ok(homeOne);
    }

    @Override
    public R homeTwo(Long beginTime, Long endTime) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //返回参数
        List<HashMap<String, String>> homeTwo = new ArrayList<>();

        //只有用户admin,运营商，加盟商时才有收益
        if (Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                || Objects.equals(user.getType(), User.TYPE_USER_OPERATE)
                || Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {

            //如果用户类型不等于admin和运营商，则查询绑定的月卡
            if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
                Franchisee franchisee = franchiseeService.queryByUid(user.getUid());

                //查不到加盟商
                if (Objects.isNull(franchisee)) {
                    log.info("homeTwo  info! not found franchisee！uid:{} ", user.getUid());
                    return R.ok(homeTwo);
                }

                //查不到加盟商月卡
                List<ElectricityMemberCard> electricityMemberCardList = electricityMemberCardService.queryByFranchisee(franchisee.getId());
                if (ObjectUtil.isEmpty(electricityMemberCardList)) {
                    log.info("homeTwo  info! not found ElectricityMemberCard！franchiseeId:{} ", franchisee.getId());
                    return R.ok(homeTwo);
                }

                List<Integer> cardIdList = new ArrayList<>();
                for (ElectricityMemberCard electricityMemberCard : electricityMemberCardList) {
                    cardIdList.add(electricityMemberCard.getId());
                }

                homeTwo = electricityMemberCardOrderService.homeTwo(beginTime, endTime, cardIdList, tenantId);
                return R.ok(homeTwo);
            }

            homeTwo = electricityMemberCardOrderService.homeTwo(beginTime, endTime, null, tenantId);
        }
        return R.ok(homeTwo);
    }

    @Override
    public R homeThree(Long beginTime, Long endTime, Integer type) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //返回参数
        List<HashMap<String, String>> homeThree = new ArrayList<>();

        //查用户
        if (type == 1) {
            if (Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                    || Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
                homeThree = userInfoService.homeThree(beginTime, endTime, tenantId);
            }
            return R.ok(homeThree);
        }

        //查柜机
        if (type == 2) {
            //如果用户类型不等于admin和运营商，则查询绑定的换电柜
            if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                    && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {

                UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
                if (Objects.isNull(userTypeService)) {
                    log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                    return R.fail("ELECTRICITY.0066", "用户权限不足");
                }
                //查询绑定的换电柜
                List<Integer> eleIdList = userTypeService.getEleIdListByUserType(user);
                if (ObjectUtil.isEmpty(eleIdList)) {
                    log.info("homeThree  info! not found ele！uid:{} ", user.getUid());
                    return R.ok(homeThree);
                }

                homeThree = homeThreeInner(beginTime, endTime, eleIdList, tenantId);

            } else {
                homeThree = homeThreeInner(beginTime, endTime, null, tenantId);
            }
        }

        //查换电
        if (type == 3) {

            //如果用户类型不等于admin和运营商，则查询绑定的换电柜
            if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                    && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {

                UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
                if (Objects.isNull(userTypeService)) {
                    log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                    return R.fail("ELECTRICITY.0066", "用户权限不足");
                }

                //查询绑定的换电柜
                List<Integer> eleIdList = userTypeService.getEleIdListByUserType(user);
                if (ObjectUtil.isEmpty(eleIdList)) {
                    log.info("homeThree  info! not found ele！uid:{} ", user.getUid());
                    return R.ok(homeThree);
                }

                homeThree = electricityCabinetOrderService.homeThree(beginTime, endTime, eleIdList, tenantId);

            } else {
                homeThree = electricityCabinetOrderService.homeThree(beginTime, endTime, null, tenantId);
            }

        }

        //查门店
        if (type == 4) {
            if (Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                    || Objects.equals(user.getType(), User.TYPE_USER_OPERATE)
                    || Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {

                //查用户
                if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
                    Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
                    if (Objects.isNull(franchisee)) {
                        log.info("homeThree  info! not found franchisee！uid:{} ", user.getUid());
                        return R.ok(homeThree);
                    }
                    List<Store> franchiseeBindList = storeService.queryByFranchiseeId(franchisee.getId());
                    if (ObjectUtil.isEmpty(franchiseeBindList)) {
                        log.info("homeThree  info! not found Store！franchiseeId:{} ", franchisee.getId());
                        return R.ok(homeThree);
                    }

                    List<Long> storeIdList = new ArrayList<>();
                    for (Store store : franchiseeBindList) {
                        storeIdList.add(store.getId());
                    }

                    homeThree = storeService.homeThree(beginTime, endTime, storeIdList, tenantId);

                } else {
                    homeThree = storeService.homeThree(beginTime, endTime, null, tenantId);
                }
            }

        }

        return R.ok(homeThree);

    }

    @Override
    public List<HashMap<String, String>> homeThreeInner(Long startTimeMilliDay, Long
            endTimeMilliDay, List<Integer> eleIdList, Integer tenantId) {
        return electricityCabinetMapper.homeThree(startTimeMilliDay, endTimeMilliDay, eleIdList, tenantId);
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
        Integer serviceStatus = 1;

        //本月换电
        Integer monthCount = electricityCabinetOrderService.homeMonth(user.getUid(), firstMonth, now);
        //总换电
        Integer totalCount = electricityCabinetOrderService.homeTotal(user.getUid());

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("order  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");

        }

        //套餐剩余天数
        long cardDay = 0;
        if (Objects.equals(franchiseeUserInfo.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE)) {
            now = franchiseeUserInfo.getDisableMemberCardTime();
        }

        if (!Objects.equals(franchiseeUserInfo.getCardType(), FranchiseeUserInfo.TYPE_COUNT)) {
            ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(franchiseeUserInfo.getCardId());
            if (Objects.nonNull(electricityMemberCard)) {
                if (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                    if (Objects.nonNull(franchiseeUserInfo.getMemberCardExpireTime()) && Objects.nonNull(franchiseeUserInfo.getRemainingNumber()) && franchiseeUserInfo.getRemainingNumber() > 0 && franchiseeUserInfo.getMemberCardExpireTime() > now) {
                        cardDay = (franchiseeUserInfo.getMemberCardExpireTime() - now) / 1000 / 60 / 60 / 24;
                    }
                } else if (Objects.nonNull(franchiseeUserInfo.getMemberCardExpireTime()) && Objects.nonNull(franchiseeUserInfo.getRemainingNumber()) && franchiseeUserInfo.getMemberCardExpireTime() > now) {
                    cardDay = (franchiseeUserInfo.getMemberCardExpireTime() - now) / 1000 / 60 / 60 / 24;
                }
            }
        } else {
            if (Objects.nonNull(franchiseeUserInfo.getMemberCardExpireTime()) && Objects.nonNull(franchiseeUserInfo.getRemainingNumber()) && franchiseeUserInfo.getRemainingNumber() > 0 && franchiseeUserInfo.getMemberCardExpireTime() > now) {
                cardDay = (franchiseeUserInfo.getMemberCardExpireTime() - now) / 1000 / 60 / 60 / 24;
            }
        }


        //我的电池
        Double battery = null;
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
        if (Objects.nonNull(electricityBattery)) {
            battery = electricityBattery.getPower();
        }

        //月卡剩余天数
        homeInfo.put("monthCount", monthCount.toString());
        homeInfo.put("totalCount", totalCount.toString());
        homeInfo.put("serviceStatus", String.valueOf(serviceStatus));
        homeInfo.put("cardDay", String.valueOf(cardDay));
        homeInfo.put("battery", String.valueOf(battery));
        return R.ok(homeInfo);
    }


    @Override
    public ElectricityCabinet queryByProductAndDeviceName(String productKey, String deviceName) {

        ElectricityCabinet electricityCabinet = electricityCabinetMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinet>()
                .eq(ElectricityCabinet::getProductKey, productKey).eq(ElectricityCabinet::getDeviceName, deviceName).eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
        if (Objects.isNull(electricityCabinet)) {
            return null;
        }
        return electricityCabinet;
    }

    @Override
    public ElectricityCabinet queryFromCacheByProductAndDeviceName(String productKey, String deviceName) {
        Integer tenantId = TenantContextHolder.getTenantId();
        //先查缓存
        ElectricityCabinet cacheElectricityCabinet = redisService.getWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + productKey + deviceName + tenantId, ElectricityCabinet.class);
        if (Objects.nonNull(cacheElectricityCabinet)) {
            return cacheElectricityCabinet;
        }

        //缓存没有再查数据库
        ElectricityCabinet electricityCabinet = electricityCabinetMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinet>()
                .eq(ElectricityCabinet::getProductKey, productKey).eq(ElectricityCabinet::getDeviceName, deviceName).eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL)
                .eq(ElectricityCabinet::getTenantId, tenantId));
        if (Objects.isNull(electricityCabinet)) {
            return null;
        }

        //放入缓存
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + productKey + deviceName + tenantId, electricityCabinet);
        return electricityCabinet;
    }

    @Override
    public R checkOpenSessionId(String sessionId) {
        String s = redisService.get(CacheConstant.ELE_OPERATOR_CACHE_KEY + sessionId);
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
//        if (!ElectricityIotConstant.ELE_COMMAND_MAPS.containsKey(eleOuterCommandQuery.getCommand())) {
        if (!ElectricityIotConstant.isLegalCommand(eleOuterCommandQuery.getCommand())) {
            return R.fail("ELECTRICITY.0036", "不合法的命令");
        }

        if (Objects.equals(ElectricityIotConstant.ELE_COMMAND_CELL_ALL_OPEN_DOOR, eleOuterCommandQuery.getCommand())) {
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
                .command(eleOuterCommandQuery.getCommand())
                .build();

        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        //发送命令失败
        if (!result.getLeft()) {
            return R.fail("ELECTRICITY.0037", "发送命令失败");
        }
        return R.ok(sessionId);
    }

    @Override
    public R queryByDeviceOuter(String productKey, String deviceName) {
        ElectricityCabinet electricityCabinet = queryByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //营业时间
        boolean result = this.isBusiness(electricityCabinet);
        if (result) {
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }

        ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
        BeanUtil.copyProperties(electricityCabinet, electricityCabinetVO);

        //查满仓空仓数
        int fullyElectricityBattery = queryFullyElectricityBattery(electricityCabinet.getId(), null);
        //查满仓空仓数
        int electricityBatteryTotal = 0;
        int noElectricityBattery = 0;
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinetVO.getId());
        if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
            //空仓
            noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
            //电池总数
            electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
        }

        //换电柜名称换成平台名称
        String name = null;
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinet.getTenantId());
        if (Objects.nonNull(electricityConfig)) {
            name = electricityConfig.getName();
        }

        //租户code
        electricityCabinetVO.setTenantId(electricityCabinet.getTenantId());
        Tenant tenant = tenantService.queryByIdFromCache(electricityCabinet.getTenantId());
        if (Objects.nonNull(tenant)) {
            electricityCabinetVO.setTenantCode(tenant.getCode());
        }

        electricityCabinetVO.setConfigName(name);
        electricityCabinetVO.setElectricityBatteryTotal(electricityBatteryTotal);
        electricityCabinetVO.setNoElectricityBattery(noElectricityBattery);
        electricityCabinetVO.setFullyElectricityBattery(fullyElectricityBattery);
        electricityCabinetVO.setBatteryFullCondition(electricityCabinetVO.getFullyCharged());
        return R.ok(electricityCabinetVO);
    }

    @Override
    public R showInfoByStoreId(Long storeId) {
        List<ElectricityCabinet> electricityCabinetList = queryByStoreId(storeId);
        if (ObjectUtil.isEmpty(electricityCabinetList)) {
            return R.ok();
        }
        List<ElectricityCabinetVO> electricityCabinetVOList = new ArrayList<>();
        for (ElectricityCabinet electricityCabinet : electricityCabinetList) {

            ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
            BeanUtil.copyProperties(electricityCabinet, electricityCabinetVO);
            electricityCabinetVOList.add(electricityCabinetVO);
        }
        if (ObjectUtil.isEmpty(electricityCabinetVOList)) {
            return R.ok();
        }
        List<ElectricityCabinetVO> electricityCabinetVOs = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(electricityCabinetVOList)) {
            electricityCabinetVOList.parallelStream().forEach(e -> {
                //营业时间
                if (Objects.nonNull(e.getBusinessTime())) {
                    String businessTime = e.getBusinessTime();
                    if (Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                        e.setBusinessTimeType(ElectricityCabinetVO.ALL_DAY);
                        e.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
                    } else {
                        e.setBusinessTimeType(ElectricityCabinetVO.ILLEGAL_DATA);
                        int index = businessTime.indexOf("-");
                        if (!Objects.equals(index, -1) && index > 0) {
                            e.setBusinessTimeType(ElectricityCabinetVO.CUSTOMIZE_TIME);
                            Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                            Long beginTime = getTime(totalBeginTime);
                            Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                            Long endTime = getTime(totalEndTime);
                            e.setBeginTime(totalBeginTime);
                            e.setEndTime(totalEndTime);
                            Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                            long now = System.currentTimeMillis();
                            if (firstToday + beginTime > now || firstToday + endTime < now) {
                                e.setIsBusiness(ElectricityCabinetVO.IS_NOT_BUSINESS);
                            } else {
                                e.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
                            }
                        }
                    }
                }

                //查满仓空仓数
                int fullyElectricityBattery = queryFullyElectricityBattery(e.getId(), null);
                //查满仓空仓数
                int electricityBatteryTotal = 0;
                int noElectricityBattery = 0;
                List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(e.getId());
                if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
                    //空仓
                    noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
                    //电池总数
                    electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
                }

                e.setElectricityBatteryTotal(electricityBatteryTotal);
                e.setNoElectricityBattery(noElectricityBattery);
                e.setFullyElectricityBattery(fullyElectricityBattery);

//                //动态查询在线状态
//                boolean result = deviceIsOnline(e.getProductKey(), e.getDeviceName());
//                if (result) {
//                    e.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS);
//                } else {
//                    e.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
//                }
                //电柜不在线也返回，可离线换电
                if (Objects.equals(e.getUsableStatus(), ElectricityCabinet.ELECTRICITY_CABINET_USABLE_STATUS)) {
                    electricityCabinetVOs.add(e);
                }
            });
        }
        return R.ok(electricityCabinetVOs);
    }

    @Override
    public List<ElectricityCabinet> queryByStoreId(Long storeId) {
        return electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>()
                .eq(ElectricityCabinet::getStoreId, storeId).eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));

    }

    @Override
    public R queryByDevice(String productKey, String deviceName) {

        //换电柜
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.error("queryByDevice  ERROR! not found electricityCabinet ！productKey{},deviceName{}", productKey, deviceName);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        return R.ok(electricityCabinet);

    }

    @Override
    public R queryByOrder(String productKey, String deviceName) {

        //登录用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("queryByDevice  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //是否存在未完成的租电池订单
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(user.getUid());
        if (Objects.nonNull(rentBatteryOrder)) {
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "ELECTRICITY.0013", "存在未完成租电订单，不能下单");
            } else if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "ELECTRICITY.0095", "存在未完成还电订单，不能下单");
            }
        }

        //是否存在未完成的换电订单
        ElectricityCabinetOrder oldElectricityCabinetOrder = electricityCabinetOrderService.queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "ELECTRICITY.0094", "存在未完成换电订单，不能下单");
        }

        //用户换电周期限制
        String orderLimit = redisService.get(CacheConstant.ORDER_TIME_UID + user.getUid());
        if (StringUtils.isNotEmpty(orderLimit)) {
            return R.fail("ELECTRICITY.0061", "下单过于频繁");
        }

        //换电柜
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.error("queryByDevice  ERROR! not found electricityCabinet ！productKey{},deviceName{}", productKey, deviceName);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //换电柜是否出现异常被锁住
        String isLock = redisService.get(CacheConstant.UNLOCK_CABINET_CACHE + electricityCabinet.getId());
        if (StringUtils.isNotEmpty(isLock)) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is lock ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0063", "换电柜出现异常，暂时不能下单");
        }

        //换电柜是否营业
        boolean result = this.isBusiness(electricityCabinet);
        if (result) {
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }

        //查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            log.error("queryByDevice  ERROR! not found store ！electricityCabinetId{}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.error("queryByDevice  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }

        //查找门店加盟商
        if (Objects.isNull(store.getFranchiseeId())) {
            log.error("queryByDevice  ERROR! not found Franchisee ！storeId{}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("queryByDevice  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("queryByDevice  ERROR! user is unusable! userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("queryByDevice  ERROR! not auth! userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");

        }

        //判断该换电柜加盟商和用户加盟商是否一致
        if (!Objects.equals(store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId())) {
            log.error("queryByDevice  ERROR!FranchiseeId is not equal!uid:{} , FranchiseeId1:{} ,FranchiseeId2:{}", user.getUid(), store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
        }

        //判断是否缴纳押金
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                || Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
            log.error("queryByDevice  ERROR! user not pay deposit! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //判断用户是否开通月卡
        if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime())
                || Objects.isNull(franchiseeUserInfo.getRemainingNumber())) {
            log.error("queryByDevice  ERROR!  not found memberCard! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0022", "未开通月卡");
        }
        Long now = System.currentTimeMillis();
        if (franchiseeUserInfo.getMemberCardExpireTime() < now || franchiseeUserInfo.getRemainingNumber() == 0) {
            log.error("queryByDevice  ERROR!  memberCard is  Expire !  uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0023", "月卡已过期");
        }

        //未租电池
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_DEPOSIT)) {
            log.error("queryByDevice  ERROR! USER not rent battery!  uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }

        ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
        BeanUtil.copyProperties(electricityCabinet, electricityCabinetVO);

//        查满仓空仓数
        Triple<Boolean, String, Object> tripleResult;
        if (Objects.equals(franchiseeUserInfo.getModelType(), FranchiseeUserInfo.NEW_MODEL_TYPE)) {
            tripleResult = queryFullyElectricityBatteryByOrder(electricityCabinet.getId(), franchiseeUserInfo.getBatteryType(), franchiseeUserInfo.getFranchiseeId());
        } else {
            tripleResult = queryFullyElectricityBatteryByExchangeOrder(electricityCabinet.getId(), null, franchiseeUserInfo.getFranchiseeId(), electricityCabinet.getTenantId());
        }

        if (Objects.isNull(tripleResult)) {
            Integer value = checkIsLowBatteryExchange(electricityCabinet.getTenantId(), electricityCabinet.getId(), franchiseeUserInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0026", "换电柜暂无满电电池", value);
        }

        if (!tripleResult.getLeft()) {
            Integer value = checkIsLowBatteryExchange(electricityCabinet.getTenantId(), electricityCabinet.getId(), franchiseeUserInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0026", tripleResult.getRight().toString(), value);
        }
//        Triple<Boolean, String, Object> tripleResult;
//        if (Objects.equals(franchiseeUserInfo.getModelType(), FranchiseeUserInfo.MEW_MODEL_TYPE)) {
//            tripleResult = queryFullyElectricityBatteryByOrder(electricityCabinet.getId(), franchiseeUserInfo.getBatteryType(), franchiseeUserInfo.getFranchiseeId());
//        } else {
//            tripleResult = queryFullyElectricityBatteryByOrder(electricityCabinet.getId(), null, franchiseeUserInfo.getFranchiseeId());
//        }
//
//        if (Objects.isNull(tripleResult)) {
//            return R.fail("ELECTRICITY.0026", "换电柜暂无满电电池");
//        }
//
//        if (!tripleResult.getLeft()) {
//            return R.fail("ELECTRICITY.0026", tripleResult.getRight().toString());
//        }

        //查满仓空仓数
        int electricityBatteryTotal = 0;
        int noElectricityBattery = 0;
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
        electricityCabinetVO.setFullyElectricityBattery(Integer.valueOf(tripleResult.getMiddle()));
        return R.ok(electricityCabinetVO);
    }

    @Override
    public R queryByRentBattery(String productKey, String deviceName) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("queryByRentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //是否存在未完成的租电池订单
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(user.getUid());
        if (Objects.nonNull(rentBatteryOrder)) {
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "ELECTRICITY.0013", "存在未完成租电订单，不能下单");
            } else if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "ELECTRICITY.0095", "存在未完成还电订单，不能下单");
            }
        }

        //是否存在未完成的换电订单
        ElectricityCabinetOrder oldElectricityCabinetOrder = electricityCabinetOrderService.queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "ELECTRICITY.0094", "存在未完成换电订单，不能下单");
        }

        //换电柜
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //动态查询在线状态
        boolean eleResult = deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("queryByRentBattery  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //换电柜是否出现异常被锁住
        String isLock = redisService.get(CacheConstant.UNLOCK_CABINET_CACHE + electricityCabinet.getId());
        if (StringUtils.isNotEmpty(isLock)) {
            log.error("queryByRentBattery  ERROR!  electricityCabinet is lock ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0063", "换电柜出现异常，暂时不能下单");
        }

        //营业时间
        boolean result = this.isBusiness(electricityCabinet);
        if (result) {
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }

        //查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            log.error("queryByDevice  ERROR! not found store ！electricityCabinetId{}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.error("queryByDevice  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }

        //查找门店加盟商
        if (Objects.isNull(store.getFranchiseeId())) {
            log.error("queryByDevice  ERROR! not found Franchisee ！storeId{}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("queryByRentBattery  ERROR! not found user!uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("queryByRentBattery  ERROR! user is unUsable! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("queryByRentBattery  ERROR! USER not auth! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");

        }

        //判断该换电柜加盟商和用户加盟商是否一致
        if (!Objects.equals(store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId())) {
            log.error("queryByDevice  ERROR!FranchiseeId is not equal!uid:{} , FranchiseeId1:{} ,FranchiseeId2:{}", user.getUid(), store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
        }

        //判断是否缴纳押金
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                || Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
            log.error("queryByDevice  ERROR! user not pay deposit! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //用户是否开通月卡
        if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime())
                || Objects.isNull(franchiseeUserInfo.getRemainingNumber())) {
            log.error("rentBattery  ERROR! not found memberCard ! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0022", "未开通月卡");
        }
        if (!Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_BATTERY)) {
            Long now = System.currentTimeMillis();
            if (franchiseeUserInfo.getMemberCardExpireTime() < now || franchiseeUserInfo.getRemainingNumber() == 0) {
                log.error("rentBattery  ERROR! memberCard  is Expire ! uid:{} ", user.getUid());
                return R.fail("ELECTRICITY.0023", "月卡已过期");
            }
        }

        //组装数据
        ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
        BeanUtil.copyProperties(electricityCabinet, electricityCabinetVO);

        //查满仓空仓数
        int electricityBatteryTotal = 0;
        int noElectricityBattery = 0;
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinetVO.getId());
        if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
            //空仓
            noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
            //电池总数
            electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
        }

        Triple<Boolean, String, Object> tripleResult;
        //查满仓空仓数
        if (Objects.equals(franchiseeUserInfo.getModelType(), FranchiseeUserInfo.NEW_MODEL_TYPE)) {
            tripleResult = queryFullyElectricityBatteryByOrder(electricityCabinet.getId(), franchiseeUserInfo.getBatteryType(), franchiseeUserInfo.getFranchiseeId());
        } else {
            tripleResult = queryFullyElectricityBatteryByOrder(electricityCabinet.getId(), null, franchiseeUserInfo.getFranchiseeId());
        }

        //已租电池则还电池
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_BATTERY)) {
            if (noElectricityBattery <= 0) {
                return R.fail("ELECTRICITY.0008", "换电柜暂无空仓");
            }
        } else {

            if (Objects.isNull(tripleResult)) {
                return R.fail("ELECTRICITY.0026", "换电柜暂无满电电池");
            }

            if (!tripleResult.getLeft()) {
                return R.fail("ELECTRICITY.0026", tripleResult.getRight().toString());
            }

        }

        electricityCabinetVO.setElectricityBatteryTotal(electricityBatteryTotal);
        electricityCabinetVO.setNoElectricityBattery(noElectricityBattery);

        if (Objects.nonNull(tripleResult)) {
            electricityCabinetVO.setFullyElectricityBattery(Integer.valueOf(tripleResult.getMiddle()));
        } else {
            electricityCabinetVO.setFullyElectricityBattery(0);
        }
        return R.ok(electricityCabinetVO);
    }

    @Override
    public List<Map<String, Object>> queryNameList(Long size, Long offset, List<Integer> eleIdList, Integer
            tenantId) {
        return electricityCabinetMapper.queryNameList(size, offset, eleIdList, tenantId);
    }

    @Override
    public R batteryReport(BatteryReportQuery batteryReportQuery) {

        String batteryName = batteryReportQuery.getBatteryName();
        if (StringUtils.isEmpty(batteryName)) {
            log.error("batteryName is null");
            return R.ok();
        }
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(batteryName);
        if (Objects.isNull(electricityBattery)) {
            log.error("ele battery error! no electricityBattery,sn,{}", batteryName);
            return R.ok();
        }

        //电池电量上报变化在百分之50以上，不更新电池电量
        Double power = batteryReportQuery.getPower();
        //修改电池
        ElectricityBattery newElectricityBattery = new ElectricityBattery();
        newElectricityBattery.setId(electricityBattery.getId());
        if (Objects.nonNull(power)) {
            newElectricityBattery.setPower(power);
        }
        Double latitude = batteryReportQuery.getLatitude();
        if (Objects.nonNull(latitude)) {
            newElectricityBattery.setLatitude(latitude);
        }
        Double longitude = batteryReportQuery.getLongitude();
        if (Objects.nonNull(longitude)) {
            newElectricityBattery.setLongitude(longitude);
        }
        electricityBattery.setUpdateTime(System.currentTimeMillis());
        electricityBatteryService.update(newElectricityBattery);

        //电池上报是否有其他信息
        if (Objects.nonNull(batteryReportQuery.getHasOtherAttr()) && batteryReportQuery.getHasOtherAttr()) {
            BatteryOtherProperties batteryOtherProperties = batteryReportQuery.getBatteryAttr();
            batteryOtherProperties.setBatteryName(batteryName);
            batteryOtherPropertiesService.insertOrUpdate(batteryOtherProperties);
        }

        return R.ok();
    }

    private boolean isNoElectricityBattery(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
    }

    private boolean isBatteryInElectricity(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);
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

    @Override
    public boolean isBusiness(ElectricityCabinet electricityCabinet) {
        //营业时间
        if (Objects.nonNull(electricityCabinet.getBusinessTime())) {
            String businessTime = electricityCabinet.getBusinessTime();
            if (!Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                int index = businessTime.indexOf("-");
                if (!Objects.equals(index, -1) && index > 0) {
                    Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                    long now = System.currentTimeMillis();
                    Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                    Long beginTime = getTime(totalBeginTime);
                    Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                    Long endTime = getTime(totalEndTime);
                    if (firstToday + beginTime > now || firstToday + endTime < now) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public R queryCount(ElectricityCabinetQuery electricityCabinetQuery) {
        return R.ok(electricityCabinetMapper.queryCount(electricityCabinetQuery));
    }

    @Override
    public Integer queryCountByStoreId(Long id) {
        return electricityCabinetMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getStoreId, id).eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL).last("limit 0,1"));
    }

    @Override
    public R checkBattery(String productKey, String deviceName, String batterySn, Boolean isParseBattery) {
        //换电柜
        ElectricityCabinet electricityCabinet = queryByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.error("checkBattery error! no electricityCabinet,productKey:{},deviceName:{}", productKey, deviceName);
            return R.fail("未找到换电柜");
        }

        //电池
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(batterySn);
        if (Objects.isNull(electricityBattery)) {
            log.error("checkBattery error! no electricityBattery,sn:{}", batterySn);
            return R.fail("未找到电池");
        }

        if (!Objects.equals(electricityCabinet.getTenantId(), electricityBattery.getTenantId())) {
            log.error("checkBattery error! tenantId is not equal,tenantId1:{},tenantId2:{}", electricityCabinet.getTenantId(), electricityBattery.getTenantId());
            return R.fail("电池与换电柜租户不匹配");
        }

        //电池加盟商是否匹配
        if (Objects.nonNull(isParseBattery) && isParseBattery) {
            //查电池所属加盟商
            FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = franchiseeBindElectricityBatteryService.queryByBatteryId(electricityBattery.getId());
            if (Objects.isNull(franchiseeBindElectricityBattery)) {
                log.error("checkBattery error! battery not bind franchisee,electricityBatteryId:{}", electricityBattery.getId());
                return R.fail("电池未绑定加盟商");
            }
            // 查换电柜所属加盟商
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.error("checkBattery error! not find store,storeId:{}", electricityCabinet.getStoreId());
                return R.fail("找不到换电柜门店");
            }

            if (!Objects.equals(store.getFranchiseeId(), franchiseeBindElectricityBattery.getFranchiseeId().longValue())) {
                log.error("checkBattery error! franchisee is not equal,franchiseeId1:{},franchiseeId2:{}", store.getFranchiseeId(), franchiseeBindElectricityBattery.getFranchiseeId());
                return R.fail("电池加盟商与电柜加盟商不匹配");
            }
        }

        //检查电池和用户是否匹配

        return R.ok();
    }

    @Override
    public R queryById(Integer id) {
        ElectricityCabinet electricityCabinet = queryByIdFromCache(id);
        ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
        BeanUtil.copyProperties(electricityCabinet, electricityCabinetVO);
        //营业时间
        if (Objects.nonNull(electricityCabinetVO.getBusinessTime())) {
            String businessTime = electricityCabinetVO.getBusinessTime();
            if (Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                electricityCabinetVO.setBusinessTimeType(ElectricityCabinetVO.ALL_DAY);
            } else {
                electricityCabinetVO.setBusinessTimeType(ElectricityCabinetVO.ILLEGAL_DATA);
                int index = businessTime.indexOf("-");
                if (!Objects.equals(index, -1) && index > 0) {
                    electricityCabinetVO.setBusinessTimeType(ElectricityCabinetVO.CUSTOMIZE_TIME);
                    Long beginTime = Long.valueOf(businessTime.substring(0, index));
                    Long endTime = Long.valueOf(businessTime.substring(index + 1));
                    electricityCabinetVO.setBeginTime(beginTime);
                    electricityCabinetVO.setEndTime(endTime);
                }
            }
        }

        //查找型号名称
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(electricityCabinetVO.getModelId());
        if (Objects.nonNull(electricityCabinetModel)) {
            electricityCabinetVO.setModelName(electricityCabinetModel.getName());
        }

        //查满仓空仓数
        Integer fullyElectricityBattery = queryFullyElectricityBattery(electricityCabinetVO.getId(), null);
        int electricityBatteryTotal = 0;
        int noElectricityBattery = 0;
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinetVO.getId());
        if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {

            //空仓
            noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();

            //电池总数
            electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
        }

        boolean result = deviceIsOnline(electricityCabinetVO.getProductKey(), electricityCabinetVO.getDeviceName());
        if (result) {
            electricityCabinetVO.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS);
        } else {
            electricityCabinetVO.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
        }
        electricityCabinetVO.setElectricityBatteryTotal(electricityBatteryTotal);
        electricityCabinetVO.setNoElectricityBattery(noElectricityBattery);
        electricityCabinetVO.setFullyElectricityBattery(fullyElectricityBattery);

        //是否锁住
        int isLock = 0;
        String LockResult = redisService.get(CacheConstant.UNLOCK_CABINET_CACHE + electricityCabinetVO.getId());
        if (StringUtil.isNotEmpty(LockResult)) {
            isLock = 1;
        }
        electricityCabinetVO.setIsLock(isLock);
        return R.ok(electricityCabinetVO);
    }

    @Override
    public R queryCabinetBelongFranchisee(Integer id) {
        return franchiseeService.queryByCabinetId(id);
    }

    @Override
    public Pair<Boolean, ElectricityCabinetBox> findUsableBatteryCellNo(Integer id, String batteryType, Double fullyCharged) {
        List<ElectricityCabinetBox> usableBatteryCellNos = electricityCabinetBoxService.queryUsableBatteryCellNo(id, null, fullyCharged);
        if (!DataUtil.collectionIsUsable(usableBatteryCellNos)) {
            return Pair.of(false, null);
        }

        return Pair.of(true, usableBatteryCellNos.get(0));
    }

    @Override
    public Triple<Boolean, String, Object> findUsableBatteryCellNoV2(Integer id, String batteryType, Double fullyCharged, Long franchiseeId) {
        //这里查所有电池
        List<ElectricityCabinetBox> usableBatteryCellNos = electricityCabinetBoxService.queryUsableBatteryCellNo(id, null, fullyCharged);
        if (!DataUtil.collectionIsUsable(usableBatteryCellNos)) {
            return Triple.of(false, "100216", "换电柜暂无满电电池");
        }

        if (StrUtil.isNotEmpty(batteryType)) {
            usableBatteryCellNos = usableBatteryCellNos.stream().filter(e -> StrUtil.equalsIgnoreCase(e.getBatteryType(), batteryType)).collect(Collectors.toList());
            if (!DataUtil.collectionIsUsable(usableBatteryCellNos)) {
                return Triple.of(false, "100217", "换电柜暂无可用型号的满电电池");
            }
        } else {
            usableBatteryCellNos = usableBatteryCellNos.stream().filter(e -> StrUtil.equalsIgnoreCase(e.getBatteryType(), batteryType)).collect(Collectors.toList());
            if (!DataUtil.collectionIsUsable(usableBatteryCellNos)) {
                return Triple.of(false, "100223", "换电柜没有非标准型号电池");
            }
        }

        List<Long> batteryIds = usableBatteryCellNos.stream().map(ElectricityCabinetBox::getBId).collect(Collectors.toList());
        List<FranchiseeBindElectricityBattery> franchiseeBindElectricityBatteries = franchiseeBindElectricityBatteryService.queryByBatteryIds(batteryIds);
        if (!DataUtil.collectionIsUsable(franchiseeBindElectricityBatteries)) {
            return Triple.of(false, "100219", "电池没有绑定加盟商,无法换电，请联系客服在后台绑定");
        }

        List<Long> bindingBatteryIds = franchiseeBindElectricityBatteries.stream().map(FranchiseeBindElectricityBattery::getElectricityBatteryId).collect(Collectors.toList());
        //把加盟商绑定的电池过滤出来
        usableBatteryCellNos = usableBatteryCellNos.stream().filter(e -> bindingBatteryIds.contains(e.getBId())).collect(Collectors.toList());
        return Triple.of(true, null, usableBatteryCellNos.get(0));
    }

    @Override
    public void unlockElectricityCabinet(Integer eid) {
        redisService.delete(CacheConstant.ORDER_ELE_ID + eid);
    }

    @Override
    public Pair<Boolean, Integer> findUsableEmptyCellNo(Integer eid) {
        List<ElectricityCabinetBox> usableEmptyCellNo = electricityCabinetBoxService.findUsableEmptyCellNo(eid);
        if (!DataUtil.collectionIsUsable(usableEmptyCellNo)) {
            return Pair.of(false, null);
        }
        return Pair.of(true, Integer.parseInt(usableEmptyCellNo.get(0).getCellNo()));

    }

    @Override
    public R getFranchisee(String productKey, String deviceName) {
        //换电柜
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.error("getFranchisee  ERROR! not found electricityCabinet ！productKey{},deviceName{}", productKey, deviceName);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("getFranchisee  ERROR! not found store ！electricityCabinetId{}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }


        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("getFranchisee  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }

        //查找门店加盟商
        if (Objects.isNull(store.getFranchiseeId())) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("getFranchisee  ERROR! not found Franchisee ！storeId{}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }

        Franchisee franchisee = franchiseeService.queryByIdFromDB(store.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("getFranchisee  ERROR! not found Franchisee ！franchiseeId{}", store.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }


        return R.ok(franchisee);
    }

    @Override
    public Integer querySumCount(ElectricityCabinetQuery electricityCabinetQuery) {
        return electricityCabinetMapper.queryCount(electricityCabinetQuery);
    }

    @Override
    public Integer queryCountByStoreIds(Integer tenantId, List<Long> storeIds) {
        return electricityCabinetMapper.queryCountByStoreIds(tenantId, storeIds);
    }

    @Override
    public Integer queryCountByStoreIdsAndStatus(Integer tenantId, List<Long> storeIds, Integer status) {
        return electricityCabinetMapper.queryCountByStoreIdsAndStatus(tenantId, storeIds, status);
    }

    @Override
    public R queryDeviceIsUnActiveFStatus(ApiRequestQuery apiRequestQuery) {

        JSONObject jsonObject = JSON.parseObject(apiRequestQuery.getData());
        String productKey = String.valueOf(jsonObject.get("productKey"));
        String deviceName = String.valueOf(jsonObject.get("deviceName"));

        if (org.apache.commons.lang3.StringUtils.isBlank(productKey) || org.apache.commons.lang3.StringUtils.isBlank(deviceName)) {
            return R.fail("SYSTEM.0003", "参数不合法");
        }

        Pair<Boolean, Object> result = iotAcsService.queryDeviceStatus(productKey, deviceName);
        if (!result.getLeft()) {
            log.error("acsClient link error! errorMsg={}", result.getLeft());
            return R.fail("CUPBOARD.10035", "iot链接失败，请联系管理员");
        }

        if (ElectricityCabinet.IOT_STATUS_ONLINE.equalsIgnoreCase(result.getRight().toString())) {
            log.error("Query device is unActive FStatus error!errorMsg={}", result.getRight());
            return R.fail("CUPBOARD.10036", "三元组在线");
        }
        return R.ok();
    }

    @Override
    public R queryAllElectricityCabinet(ElectricityCabinetQuery electricityCabinetQuery) {
        return R.ok(electricityCabinetMapper.queryList(electricityCabinetQuery));
    }


    private void checkCupboardStatusAndUpdateDiff(boolean isOnline, ElectricityCabinet electricityCabinet) {
        if (!isOnline && isCupboardAttrIsOnline(electricityCabinet) || isOnline && !isCupboardAttrIsOnline(electricityCabinet)) {
            ElectricityCabinet update = new ElectricityCabinet();
            update.setId(electricityCabinet.getId());
            update.setOnlineStatus(isOnline ? ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS : ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
            idempotentUpdateCupboard(electricityCabinet, update);
        }
    }

    private boolean isCupboardAttrIsOnline(ElectricityCabinet electricityCabinet) {
        return ElectricityCabinet.IOT_STATUS_ONLINE.equalsIgnoreCase(electricityCabinet.getOnlineStatus().toString());
    }

    @Override
    public int idempotentUpdateCupboard(ElectricityCabinet electricityCabinet, ElectricityCabinet updateElectricityCabinet) {
        Integer update = update(electricityCabinet);
        if (update > 0) {
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId());
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName() + electricityCabinet.getTenantId());
        }
        return update;
    }

    private Double checkLowBatteryExchangeMinimumBatteryPowerStandard(Integer tenantId, Integer electricityCabinetId) {
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);

        Double fullyCharged = electricityCabinet.getFullyCharged();

        if (Objects.isNull(fullyCharged) || Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsLowBatteryExchange(), ElectricityConfig.NOT_LOW_BATTERY_EXCHANGE)) {
            return fullyCharged;
        }
        List<LowBatteryExchangeModel> list = JsonUtil.fromJsonArray(electricityConfig.getLowBatteryExchangeModel(), LowBatteryExchangeModel.class);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmmss");
        Long now = System.currentTimeMillis();
        for (LowBatteryExchangeModel lowBatteryExchangeModel : list) {
            if (Integer.parseInt(simpleDateFormat.format(now)) > Integer.parseInt(simpleDateFormat.format(lowBatteryExchangeModel.getExchangeBeginTime())) && Integer.parseInt(simpleDateFormat.format(now)) < Integer.parseInt(simpleDateFormat.format(lowBatteryExchangeModel.getExchangeEndTime())) && Objects.nonNull(lowBatteryExchangeModel.getBatteryPowerStandard()) && lowBatteryExchangeModel.getBatteryPowerStandard() < fullyCharged) {
                fullyCharged = lowBatteryExchangeModel.getBatteryPowerStandard();
            }
        }
        return fullyCharged;
    }

    private Integer checkIsLowBatteryExchange(Integer tenantId, Integer electricityCabinetId, Long franchiseeId) {

        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        Integer result = null;
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsLowBatteryExchange(), ElectricityConfig.NOT_LOW_BATTERY_EXCHANGE)) {
            return result;
        }
        List<LowBatteryExchangeModel> list = JsonUtil.fromJsonArray(electricityConfig.getLowBatteryExchangeModel(), LowBatteryExchangeModel.class);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmmss");
        Long now = System.currentTimeMillis();
        List<ElectricityBattery> electricityBatteries = electricityBatteryService.queryWareHouseByElectricityCabinetId(electricityCabinetId);
        for (LowBatteryExchangeModel lowBatteryExchangeModel : list) {
            if (Objects.nonNull(electricityBatteries) && Integer.parseInt(simpleDateFormat.format(now)) > Integer.parseInt(simpleDateFormat.format(lowBatteryExchangeModel.getExchangeBeginTime())) && Integer.parseInt(simpleDateFormat.format(now)) < Integer.parseInt(simpleDateFormat.format(lowBatteryExchangeModel.getExchangeEndTime()))) {
                for (ElectricityBattery electricityBattery : electricityBatteries) {
                    //电池所在仓门非禁用
                    ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryBySn(electricityBattery.getSn(), electricityCabinetId);
                    if (Objects.nonNull(electricityCabinetBox)) {
                        if (Objects.nonNull(electricityBattery.getPower()) && Objects.nonNull(lowBatteryExchangeModel.getBatteryPowerStandard()) && electricityBattery.getPower() > lowBatteryExchangeModel.getBatteryPowerStandard()) {
                            //3、查加盟商是否绑定电池
                            FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = franchiseeBindElectricityBatteryService.queryByBatteryIdAndFranchiseeId(electricityBattery.getId(), franchiseeId);
                            if (Objects.nonNull(franchiseeBindElectricityBattery)) {
                                result = ElectricityConfig.LOW_BATTERY_EXCHANGE;
                                return result;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }


    @Override
    public R queryElectricityCabinetBoxInfoById(Integer electricityCabinetId) {
        List<ElectricityCabinetBoxVO> resultList = Lists.newArrayList();

        ElectricityCabinet electricityCabinet = queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELE ERROR! not found eletricity cabinet,electricityCabinetId={}", electricityCabinetId);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        Double fullyCharged = electricityCabinet.getFullyCharged();

//        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinetId);
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryAllBoxByElectricityCabinetId(electricityCabinetId);
        if (!CollectionUtils.isEmpty(electricityCabinetBoxList)) {
            List<ElectricityCabinetBoxVO> electricityCabinetBoxVOList = Lists.newArrayList();

            electricityCabinetBoxList.parallelStream().forEach(item -> {
                ElectricityCabinetBoxVO electricityCabinetBoxVO = new ElectricityCabinetBoxVO();
                BeanUtils.copyProperties(item, electricityCabinetBoxVO);

                ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(item.getSn());
                if (!Objects.isNull(electricityBattery)) {
                    electricityCabinetBoxVO.setPower(electricityBattery.getPower());
                    electricityCabinetBoxVO.setExchange(electricityBattery.getPower() >= fullyCharged ? ElectricityCabinetBoxVO.EXCHANGE_YES : ElectricityCabinetBoxVO.EXCHANGE_NO);
                }



                electricityCabinetBoxVOList.add(electricityCabinetBoxVO);
            });

            //排序
            if (!CollectionUtils.isEmpty(electricityCabinetBoxVOList)) {
                resultList = electricityCabinetBoxVOList.stream().sorted(Comparator.comparing(item -> Integer.parseInt(item.getCellNo()))).collect(Collectors.toList());
            }

        }
        return R.ok(resultList);
    }

    @Override
    public R homepageTurnover() {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }

        Long franchiseeId = null;
        Franchisee franchisee = null;
        if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
            franchisee = franchiseeService.queryByUid(user.getUid());
        }
        if (Objects.nonNull(franchisee)) {
            franchiseeId = franchisee.getId();
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        HomePageTurnOverVo homePageTurnOverVo = new HomePageTurnOverVo();

        long todayStartTime = DateUtils.getTodayStartTime();
        //购买换电月卡
        Long finalFranchiseeId = franchiseeId;
        CompletableFuture<Void> batteryMemberCard = CompletableFuture.runAsync(() -> {
            BigDecimal batteryMemberCardTurnover = electricityMemberCardOrderService.queryBatteryMemberCardTurnOver(tenantId, null, finalFranchiseeId);
            BigDecimal todayBatteryMemberCardTurnover = electricityMemberCardOrderService.queryBatteryMemberCardTurnOver(tenantId, todayStartTime, finalFranchiseeId);
            homePageTurnOverVo.setBatteryMemberCardTurnover(batteryMemberCardTurnover);
            homePageTurnOverVo.setTodayBatteryMemberCardTurnover(todayBatteryMemberCardTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //购买租车月卡
        CompletableFuture<Void> carMemberCard = CompletableFuture.runAsync(() -> {
            BigDecimal carMemberCardTurnover = electricityMemberCardOrderService.queryCarMemberCardTurnOver(tenantId, null, finalFranchiseeId);
            BigDecimal todayCarMemberCardTurnover = electricityMemberCardOrderService.queryCarMemberCardTurnOver(tenantId, todayStartTime, finalFranchiseeId);
            homePageTurnOverVo.setCarMemberCardTurnover(carMemberCardTurnover);
            homePageTurnOverVo.setTodayCarMemberCardTurnover(todayCarMemberCardTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //电池服务费
        CompletableFuture<Void> batteryServiceFee = CompletableFuture.runAsync(() -> {
            BigDecimal batteryServiceFeeTurnover = eleBatteryServiceFeeOrderService.queryTurnOver(tenantId, null, finalFranchiseeId);
            BigDecimal todayBatteryServiceFeeTurnover = eleBatteryServiceFeeOrderService.queryTurnOver(tenantId, todayStartTime, finalFranchiseeId);
            homePageTurnOverVo.setTodayBatteryServiceFeeTurnover(todayBatteryServiceFeeTurnover);
            homePageTurnOverVo.setBatteryServiceFeeTurnover(batteryServiceFeeTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //等待所有线程停止
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(batteryMemberCard, carMemberCard, batteryServiceFee);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
            homePageTurnOverVo.setSumTurnover(homePageTurnOverVo.getBatteryMemberCardTurnover().add(homePageTurnOverVo.getBatteryServiceFeeTurnover()).add(homePageTurnOverVo.getCarMemberCardTurnover()));
            homePageTurnOverVo.setTodayTurnover(homePageTurnOverVo.getTodayBatteryMemberCardTurnover().add(homePageTurnOverVo.getTodayBatteryServiceFeeTurnover()).add(homePageTurnOverVo.getTodayCarMemberCardTurnover()));
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(homePageTurnOverVo);
    }

    @Override
    public R homepageDeposit() {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }

        Long franchiseeId = null;
        Franchisee franchisee = null;
        if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
            franchisee = franchiseeService.queryByUid(user.getUid());
        }
        if (Objects.nonNull(franchisee)) {
            franchiseeId = franchisee.getId();
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        HomePageDepositVo homePageDepositVo = new HomePageDepositVo();

        long todayStartTime = DateUtils.getTodayStartTime();

        //缴纳电池押金
        Long finalFranchiseeId = franchiseeId;
        CompletableFuture<Void> batteryDeposit = CompletableFuture.runAsync(() -> {
            BigDecimal batteryDepositTurnover = eleDepositOrderService.queryDepositTurnOverByDepositType(tenantId, null, EleDepositOrder.ELECTRICITY_DEPOSIT, finalFranchiseeId);
            BigDecimal todayBatteryDeposit = eleDepositOrderService.queryDepositTurnOverByDepositType(tenantId, todayStartTime, EleDepositOrder.ELECTRICITY_DEPOSIT, finalFranchiseeId);
            homePageDepositVo.setBatteryDeposit(batteryDepositTurnover);
            homePageDepositVo.setTodayBatteryDeposit(todayBatteryDeposit);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //缴纳租车押金
        CompletableFuture<Void> carDeposit = CompletableFuture.runAsync(() -> {
            BigDecimal batteryDepositTurnover = eleDepositOrderService.queryDepositTurnOverByDepositType(tenantId, null, EleDepositOrder.RENT_CAR_DEPOSIT, finalFranchiseeId);
            BigDecimal todayBatteryDeposit = eleDepositOrderService.queryDepositTurnOverByDepositType(tenantId, todayStartTime, EleDepositOrder.RENT_CAR_DEPOSIT, finalFranchiseeId);
            homePageDepositVo.setCarDeposit(batteryDepositTurnover);
            homePageDepositVo.setTodayCarDeposit(todayBatteryDeposit);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //退电池押金
        CompletableFuture<Void> refundBatteryDeposit = CompletableFuture.runAsync(() -> {
            BigDecimal todayRefundDeposit = refundOrderService.queryTurnOverByTime(tenantId, todayStartTime, null);
            BigDecimal historyRefundDeposit = refundOrderService.queryTurnOverByTime(tenantId, null, EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
            homePageDepositVo.setTodayRefundDeposit(todayRefundDeposit);
            homePageDepositVo.setHistoryRefundBatteryDeposit(historyRefundDeposit);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //退租车押金
        CompletableFuture<Void> refundCarDeposit = CompletableFuture.runAsync(() -> {
            BigDecimal historyRefundDeposit = refundOrderService.queryTurnOverByTime(tenantId, null, EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER);
            homePageDepositVo.setHistoryRefundCarDeposit(historyRefundDeposit);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //等待所有线程停止
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(batteryDeposit, carDeposit, refundBatteryDeposit, refundCarDeposit);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
            homePageDepositVo.setBatteryDeposit(homePageDepositVo.getBatteryDeposit().subtract(homePageDepositVo.getHistoryRefundBatteryDeposit()));
            homePageDepositVo.setCarDeposit(homePageDepositVo.getCarDeposit().subtract(homePageDepositVo.getHistoryRefundCarDeposit()));
            homePageDepositVo.setSumDepositTurnover(homePageDepositVo.getBatteryDeposit().add(homePageDepositVo.getCarDeposit()));
            homePageDepositVo.setTodayPayDeposit(homePageDepositVo.getTodayBatteryDeposit().add(homePageDepositVo.getTodayCarDeposit()));
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(homePageDepositVo);
    }

    @Override
    public R homepageOverviewDetail() {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }

        Long franchiseeId = null;
        Franchisee franchisee = null;
        if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
            franchisee = franchiseeService.queryByUid(user.getUid());
        }
        if (Objects.nonNull(franchisee)) {
            franchiseeId = franchisee.getId();
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();


        HomepageOverviewDetailVo homepageOverviewDetailVo = new HomepageOverviewDetailVo();

        //实名认证用户
        CompletableFuture<Void> authenticationUser = CompletableFuture.runAsync(() -> {
            Integer authenticationUserCount = userInfoService.queryAuthenticationUserCount(tenantId);
            homepageOverviewDetailVo.setAuthenticationUserCount(authenticationUserCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //门店
        Long finalFranchiseeId = franchiseeId;
        //查询所有门店
        List<Long> stores = storeService.queryStoreIdByFranchiseeId(finalFranchiseeId);
        CompletableFuture<Void> store = CompletableFuture.runAsync(() -> {
            StoreQuery storeQuery = StoreQuery.builder()
                    .franchiseeId(finalFranchiseeId)
                    .tenantId(tenantId).build();
            Integer storeCount = storeService.queryCountForHomePage(storeQuery);
            homepageOverviewDetailVo.setStoreCount(storeCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //柜机
        CompletableFuture<Void> electricityCabinet = CompletableFuture.runAsync(() -> {
            Integer electricityCabinetCount = electricityCabinetService.queryCountByStoreIds(tenantId, stores);
            homepageOverviewDetailVo.setElectricityCabinetCount(electricityCabinetCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //车辆
        CompletableFuture<Void> car = CompletableFuture.runAsync(() -> {
            Integer carCount = electricityCarService.queryCountByStoreIds(tenantId, stores);
            homepageOverviewDetailVo.setCarCount(carCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(authenticationUser, store, electricityCabinet, car);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("ORDER STATISTICS ERROR!", e);
        }

        return R.ok(homepageOverviewDetailVo);
    }

    @Override
    public R homepageBenefitAnalysis(Long beginTime, Long endTime) {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }

        Long franchiseeId = null;
        Franchisee franchisee = null;
        if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
            franchisee = franchiseeService.queryByUid(user.getUid());
        }
        if (Objects.nonNull(franchisee)) {
            franchiseeId = franchisee.getId();
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        HomePageTurnOverAnalysisVo homePageTurnOverAnalysisVo = new HomePageTurnOverAnalysisVo();
        //购买换电月卡
        Long finalFranchiseeId = franchiseeId;
        CompletableFuture<Void> batteryMemberCard = CompletableFuture.runAsync(() -> {
            List<HomePageTurnOverGroupByWeekDayVo> batteryMemberCardTurnover = electricityMemberCardOrderService.queryBatteryMemberCardTurnOverByCreateTime(tenantId, finalFranchiseeId, beginTime, endTime);
            homePageTurnOverAnalysisVo.setBatteryMemberCardAnalysis(batteryMemberCardTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //购买租车月卡
        CompletableFuture<Void> carMemberCard = CompletableFuture.runAsync(() -> {
            List<HomePageTurnOverGroupByWeekDayVo> carMemberCardTurnover = electricityMemberCardOrderService.queryCarMemberCardTurnOverByCreateTime(tenantId, finalFranchiseeId, beginTime, endTime);
            homePageTurnOverAnalysisVo.setCarMemberCardAnalysis(carMemberCardTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //电池服务费
        CompletableFuture<Void> batteryServiceFee = CompletableFuture.runAsync(() -> {
            List<HomePageTurnOverGroupByWeekDayVo> batteryServiceFeeTurnover = eleBatteryServiceFeeOrderService.queryTurnOverByCreateTime(tenantId, finalFranchiseeId, beginTime, endTime);
            homePageTurnOverAnalysisVo.setBatteryServiceFeeAnalysis(batteryServiceFeeTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //电池押金
        CompletableFuture<Void> batteryDeposit = CompletableFuture.runAsync(() -> {
            List<HomePageTurnOverGroupByWeekDayVo> batteryDepositTurnover = eleDepositOrderService.queryDepositTurnOverAnalysisByDepositType(tenantId, EleDepositOrder.ELECTRICITY_DEPOSIT, finalFranchiseeId, beginTime, endTime);
            homePageTurnOverAnalysisVo.setBatteryDepositAnalysis(batteryDepositTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //租车押金
        CompletableFuture<Void> carDeposit = CompletableFuture.runAsync(() -> {
            List<HomePageTurnOverGroupByWeekDayVo> carDepositTurnOver = eleDepositOrderService.queryDepositTurnOverAnalysisByDepositType(tenantId, EleDepositOrder.RENT_CAR_DEPOSIT, finalFranchiseeId, beginTime, endTime);
            homePageTurnOverAnalysisVo.setCarDepositAnalysis(carDepositTurnOver);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //总套餐营业额统计
        CompletableFuture<Void> sumMemberCard = CompletableFuture.runAsync(() -> {
            BigDecimal sumMemberCardTurnOver = electricityMemberCardOrderService.querySumMemberCardTurnOver(tenantId, finalFranchiseeId, beginTime, endTime);
            BigDecimal sumBatteryService = eleBatteryServiceFeeOrderService.queryAllTurnOver(tenantId, finalFranchiseeId, beginTime, endTime);
            if (Objects.isNull(sumMemberCardTurnOver)) {
                sumMemberCardTurnOver = BigDecimal.valueOf(0);
            }
            if (Objects.isNull(sumBatteryService)) {
                sumBatteryService = BigDecimal.valueOf(0);
            }
            homePageTurnOverAnalysisVo.setMemberCardTurnOver(sumMemberCardTurnOver.add(sumBatteryService));
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //总押金营业额统计
        CompletableFuture<Void> sumDeposit = CompletableFuture.runAsync(() -> {
            BigDecimal sumDepositTurnOver = eleDepositOrderService.querySumDepositTurnOverAnalysis(tenantId, finalFranchiseeId, beginTime, endTime);
            homePageTurnOverAnalysisVo.setDepositTurnOver(sumDepositTurnOver);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });


        //等待所有线程停止
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(batteryMemberCard, carMemberCard, batteryServiceFee, batteryDeposit, carDeposit, sumMemberCard, sumDeposit);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(homePageTurnOverAnalysisVo);
    }

    @Override
    public R homepageUserAnalysis(Long beginTime, Long enTime) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        HomePageUserAnalysisVo homePageUserAnalysisVo = new HomePageUserAnalysisVo();

        //实名认证用户
        CompletableFuture<Void> authenticationUser = CompletableFuture.runAsync(() -> {
            List<HomePageUserByWeekDayVo> list = userInfoService.queryUserAnalysisForAuthUser(tenantId, beginTime, enTime);
            homePageUserAnalysisVo.setAuthenticationUserAnalysis(list);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //普通用户
        CompletableFuture<Void> normalUser = CompletableFuture.runAsync(() -> {
            List<HomePageUserByWeekDayVo> list = userInfoService.queryUserAnalysisByUserStatus(tenantId, User.TYPE_USER_NORMAL_WX_PRO, beginTime, enTime);
            homePageUserAnalysisVo.setNormalUserAnalysis(list);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //用户总数
        CompletableFuture<Void> userCount = CompletableFuture.runAsync(() -> {
            Integer count = userService.queryHomePageCount(User.TYPE_USER_NORMAL_WX_PRO, beginTime, enTime, tenantId);
            homePageUserAnalysisVo.setUserCount(count);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //等待所有线程停止
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(authenticationUser, normalUser, userCount);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(homePageUserAnalysisVo);
    }

    @Override
    public R homepageElectricityCabinetAnalysis() {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }

        List<Integer> eleIdList = null;
        Long franchiseeId = null;
        Franchisee franchisee = null;
        if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByUserType(user);
            franchisee = franchiseeService.queryByUid(user.getUid());
        }

        if (Objects.nonNull(franchisee)) {
            franchiseeId = franchisee.getId();
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        HomePageElectricityOrderVo homePageElectricityOrderVo = new HomePageElectricityOrderVo();

        //换电成功订单数量统计
        List<Integer> finalEleIdList = eleIdList;
        Long finalFranchiseeId = franchiseeId;
        //查询所有门店
        List<Long> stores = storeService.queryStoreIdByFranchiseeId(finalFranchiseeId);
        CompletableFuture<Void> electricityOrderSuccessCount = CompletableFuture.runAsync(() -> {
            ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder().tenantId(tenantId).eleIdList(finalEleIdList).status(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS).build();
            Integer orderSuccessCount = electricityCabinetOrderService.queryCountForScreenStatistic(electricityCabinetOrderQuery);
            homePageElectricityOrderVo.setOrderSuccessCount(orderSuccessCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });

        //换电总订单统计
        CompletableFuture<Void> electricitySunOrderCount = CompletableFuture.runAsync(() -> {
            ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder().tenantId(tenantId).eleIdList(finalEleIdList).build();
            Integer orderSumCount = electricityCabinetOrderService.queryCountForScreenStatistic(electricityCabinetOrderQuery);
            homePageElectricityOrderVo.setSumOrderCount(orderSumCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });

        //换电柜在线总数统计
        CompletableFuture<Void> electricityOnlineCabinetCount = CompletableFuture.runAsync(() -> {
            Integer onLineCount = electricityCabinetService.queryCountByStoreIdsAndStatus(tenantId, stores, ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS);
            homePageElectricityOrderVo.setOnlineElectricityCabinet(onLineCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricityCabinetTurnOver error!", e);
            return null;
        });

        //换电柜离线总数统计
        CompletableFuture<Void> electricityOfflineCabinetCount = CompletableFuture.runAsync(() -> {
            Integer offLineCount = electricityCabinetService.queryCountByStoreIdsAndStatus(tenantId, stores, ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
            homePageElectricityOrderVo.setOfflineElectricityCabinet(offLineCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricityCabinetTurnOver error!", e);
            return null;
        });

        //等待所有线程停止
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(electricityOrderSuccessCount, electricitySunOrderCount, electricityOnlineCabinetCount, electricityOfflineCabinetCount);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(homePageElectricityOrderVo);
    }

    @Override
    public R homepageExchangeOrderFrequency(HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery) {

        HomepageElectricityExchangeFrequencyVo homepageElectricityExchangeFrequencyVo = new HomepageElectricityExchangeFrequencyVo();

        CompletableFuture<Void> electricityOrderSumCount = CompletableFuture.runAsync(() -> {
            Integer sumCount = electricityCabinetOrderService.homepageExchangeOrderSumCount(homepageElectricityExchangeFrequencyQuery);
            homepageElectricityExchangeFrequencyVo.setSumFrequency(sumCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });


        CompletableFuture<Void> exchangeFrequency = CompletableFuture.runAsync(() -> {
            List<HomepageElectricityExchangeFrequencyVo> homepageExchangeFrequency = electricityCabinetOrderService.homepageExchangeFrequency(homepageElectricityExchangeFrequencyQuery);
            List<HomepageElectricityExchangeVo> homepageElectricityExchangeVos = new ArrayList<>();
            homepageExchangeFrequency.parallelStream().forEach(item -> {
                HomepageElectricityExchangeVo homepageElectricityExchangeVo = new HomepageElectricityExchangeVo();
                Store store = storeService.queryByIdFromCache(item.getStoreId());
                if (Objects.nonNull(store)) {
                    item.setStoreName(store.getName());
                }
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(item.getEleId());
                if (Objects.nonNull(electricityCabinet)) {
                    item.setElectricityName(electricityCabinet.getName());
                }
                homepageElectricityExchangeVo.setElectricityName(item.getElectricityName());
                homepageElectricityExchangeVo.setStoreName(item.getStoreName());
                homepageElectricityExchangeVo.setExchangeFrequency(item.getExchangeFrequency());
                homepageElectricityExchangeVos.add(homepageElectricityExchangeVo);
            });
            homepageElectricityExchangeFrequencyVo.setHomepageElectricityExchangeVos(homepageElectricityExchangeVos);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });

        CompletableFuture<Void> count = CompletableFuture.runAsync(() -> {
            List<HomepageElectricityExchangeFrequencyVo> sumCount = electricityCabinetOrderService.homepageExchangeFrequencyCount(homepageElectricityExchangeFrequencyQuery);
            if (Objects.nonNull(sumCount)) {
                homepageElectricityExchangeFrequencyVo.setCount(sumCount.size());
            }
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });

        //等待所有线程停止
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(electricityOrderSumCount, exchangeFrequency, count);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(homepageElectricityExchangeFrequencyVo);
    }

    @Override
    public R homepageBatteryAnalysis(HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery) {

        HomepageBatteryVo homepageBatteryVo = new HomepageBatteryVo();

        CompletableFuture<Void> electricityOrderSumCount = CompletableFuture.runAsync(() -> {
            List<HomepageBatteryFrequencyVo> list = electricityBatteryService.homepageBatteryAnalysis(homepageBatteryFrequencyQuery);
            homepageBatteryVo.setHomepageBatteryFrequencyVos(list);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });

        CompletableFuture<Void> count = CompletableFuture.runAsync(() -> {
            List<HomepageBatteryFrequencyVo> list = electricityBatteryService.homepageBatteryAnalysisCount(homepageBatteryFrequencyQuery);
            if (Objects.nonNull(list)) {
                homepageBatteryVo.setCount(list.size());
            }
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });


        //等待所有线程停止
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(electricityOrderSumCount, count);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(homepageBatteryVo);

    }

    @Override
    public R queryElectricityCabinetFileById(Integer electricityCabinetId) {
        List<ElectricityCabinetFile> electricityCabinetFiles=electricityCabinetFileService.queryByDeviceInfo(electricityCabinetId.longValue(),ElectricityCabinetFile.TYPE_ELECTRICITY_CABINET, storageConfig.getIsUseOSS());
        List<String> cabinetPhoto=new ArrayList<>();

        for (ElectricityCabinetFile electricityCabinetFile:electricityCabinetFiles){
            if (StringUtils.isNotEmpty(electricityCabinetFile.getName())) {
                cabinetPhoto.add("https://" + storageConfig.getUrlPrefix() + "/" + electricityCabinetFile.getName());
            }
        }
        return R.ok(cabinetPhoto);
    }
}
