package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.iot.model.v20180120.GetDeviceStatusResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.config.EleCommonConfig;
import com.xiliulou.electricity.config.EleIotOtaPathConfig;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.OtaConstant;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.entity.BatteryGeo;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.CabinetMoveHistory;
import com.xiliulou.electricity.entity.CarDepositOrder;
import com.xiliulou.electricity.entity.EleCabinetCoreData;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleOtaFile;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityAbnormalMessageNotify;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetServer;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.MaintenanceUserNotifyConfig;
import com.xiliulou.electricity.entity.Message;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.entity.OtaFileConfig;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBattery;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.ElectricityCabinetMapper;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.query.BatteryReportQuery;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityCabinetAddressQuery;
import com.xiliulou.electricity.query.ElectricityCabinetImportQuery;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.query.ElectricityCabinetTransferQuery;
import com.xiliulou.electricity.query.FreeCellNoQuery;
import com.xiliulou.electricity.query.HomePageDepositQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.query.HomepageElectricityExchangeFrequencyQuery;
import com.xiliulou.electricity.query.LowBatteryExchangeModel;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.electricity.service.BatteryGeoService;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.BatteryOtherPropertiesService;
import com.xiliulou.electricity.service.CabinetMoveHistoryService;
import com.xiliulou.electricity.service.CarDepositOrderService;
import com.xiliulou.electricity.service.CarMemberCardOrderService;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.EleCabinetCoreDataService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleOtaFileService;
import com.xiliulou.electricity.service.EleOtaUpgradeService;
import com.xiliulou.electricity.service.EleOtherConfigService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetFileService;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetServerService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.MaintenanceUserNotifyConfigService;
import com.xiliulou.electricity.service.OtaFileConfigService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.VersionUtil;
import com.xiliulou.electricity.vo.CabinetBatteryVO;
import com.xiliulou.electricity.vo.EleCabinetDataAnalyseVO;
import com.xiliulou.electricity.vo.ElectricityCabinetBatchOperateVo;
import com.xiliulou.electricity.vo.ElectricityCabinetBoxVO;
import com.xiliulou.electricity.vo.ElectricityCabinetExcelVO;
import com.xiliulou.electricity.vo.ElectricityCabinetExtendDataVO;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.HomePageDepositVo;
import com.xiliulou.electricity.vo.HomePageElectricityOrderVo;
import com.xiliulou.electricity.vo.HomePageTurnOverAnalysisVo;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import com.xiliulou.electricity.vo.HomePageTurnOverVo;
import com.xiliulou.electricity.vo.HomePageUserAnalysisVo;
import com.xiliulou.electricity.vo.HomePageUserByWeekDayVo;
import com.xiliulou.electricity.vo.HomepageBatteryFrequencyVo;
import com.xiliulou.electricity.vo.HomepageBatteryVo;
import com.xiliulou.electricity.vo.HomepageElectricityExchangeFrequencyVo;
import com.xiliulou.electricity.vo.HomepageElectricityExchangeVo;
import com.xiliulou.electricity.vo.HomepageOverviewDetailVo;
import com.xiliulou.electricity.vo.SearchVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.service.IotAcsService;
import com.xiliulou.iot.service.PubHardwareService;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
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
    
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN);
    
    
    private static final String BATTERY_FULL_CONDITION = "batteryFullCondition";
    
    //    @Value("${testFactory.tenantId}")
    //    private Integer testFactoryTenantId;
    
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
    EleIotOtaPathConfig eleIotOtaPathConfig;
    
    @Autowired
    OtaFileConfigService otaFileConfigService;
    
    @Autowired
    EleOtaUpgradeService eleOtaUpgradeService;
    
    @Autowired
    ElectricityCabinetFileService electricityCabinetFileService;
    
    @Autowired
    StorageConfig storageConfig;
    
    @Autowired
    EleCommonConfig eleCommonConfig;
    
    @Autowired
    private ElectricityCabinetServerService electricityCabinetServerService;
    
    @Autowired
    RocketMqService rocketMqService;
    
    @Autowired
    MaintenanceUserNotifyConfigService maintenanceUserNotifyConfigService;
    
    @Qualifier("aliyunOssService")
    @Autowired
    StorageService storageService;
    
    @Autowired
    UserDataScopeService userDataScopeService;
    
    @Autowired
    UserBatteryService userBatteryService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    CarDepositOrderService carDepositOrderService;
    
    @Autowired
    CarMemberCardOrderService carMemberCardOrderService;
    
    @Autowired
    EleCabinetCoreDataService eleCabinetCoreDataService;
    
    @Autowired
    EleOtaFileService eleOtaFileService;
    
    
    @Autowired
    BatteryGeoService batteryGeoService;
    
    @Autowired
    BatteryModelService batteryModelService;
    
    @Autowired
    CabinetMoveHistoryService cabinetMoveHistoryService;
    
    @Autowired
    EleOtherConfigService eleOtherConfigService;
    
    @Autowired
    UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;
    
    @Autowired
    CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;
    
    @Autowired
    BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    /**
     * 根据主键ID集获取柜机基本信息
     *
     * @param ids 主键ID集
     * @return
     */
    @Slave
    @Override
    public List<ElectricityCabinet> listByIds(Set<Integer> ids) {
        LambdaQueryWrapper<ElectricityCabinet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ElectricityCabinet::getId, new ArrayList<>(ids));
        List<ElectricityCabinet> electricityCabinets = electricityCabinetMapper.selectList(queryWrapper);
        return electricityCabinets;
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
        int update = this.electricityCabinetMapper.updateById(electricityCabinet);
        
        if (update > 0) {
            //更新缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId());
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
        }
        return update;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        List<ElectricityCabinet> existsElectricityCabinetList = electricityCabinetMapper.selectList(
                new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getProductKey, electricityCabinet.getProductKey())
                        .eq(ElectricityCabinet::getDeviceName, electricityCabinet.getDeviceName())
                        //                .eq(ElectricityCabinet::getDeviceSecret, electricityCabinet.getDeviceSecret())
                        .eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
        if (DataUtil.collectionIsUsable(existsElectricityCabinetList)) {
            return R.fail("ELECTRICITY.0002", "换电柜的三元组已存在");
        }
        
        //查找快递柜型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(electricityCabinet.getModelId());
        if (Objects.isNull(electricityCabinetModel)) {
            return R.fail("ELECTRICITY.0004", "未找到换电柜型号");
        }
        
        //加盟商
        if (Objects.nonNull(electricityCabinetAddAndUpdate.getStoreId())) {
            Store store = storeService.queryByIdFromCache(electricityCabinetAddAndUpdate.getStoreId());
            electricityCabinet.setFranchiseeId(Objects.nonNull(store) ? store.getFranchiseeId() : null);
        }
        
        int insert = electricityCabinetMapper.insert(electricityCabinet);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            
            //新增缓存
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName(), electricityCabinet);
            
            //添加快递柜格挡
            electricityCabinetBoxService.batchInsertBoxByModelId(electricityCabinetModel, electricityCabinet.getId());
            //添加服务时间记录
            electricityCabinetServerService.insertOrUpdateByElectricityCabinet(electricityCabinet, electricityCabinet);
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
        List<ElectricityCabinet> existsElectricityCabinetList = electricityCabinetMapper.selectList(
                new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getProductKey, electricityCabinet.getProductKey())
                        .eq(ElectricityCabinet::getDeviceName, electricityCabinet.getDeviceName())
                        //.eq(ElectricityCabinet::getDeviceSecret, electricityCabinet.getDeviceSecret())
                        .eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
        if (DataUtil.collectionIsUsable(existsElectricityCabinetList)) {
            for (ElectricityCabinet existsElectricityCabinet : existsElectricityCabinetList) {
                if (!Objects.equals(existsElectricityCabinet.getId(), electricityCabinet.getId())) {
                    return R.fail("ELECTRICITY.0002", "换电柜的三元组已存在");
                }
            }
        }
        
        //加盟商
        if (Objects.nonNull(electricityCabinetAddAndUpdate.getStoreId())) {
            Store store = storeService.queryByIdFromCache(electricityCabinetAddAndUpdate.getStoreId());
            electricityCabinet.setFranchiseeId(Objects.nonNull(store) ? store.getFranchiseeId() : null);
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
        electricityCabinet.setTenantId(TenantContextHolder.getTenantId());
        
        int update = electricityCabinetMapper.updateEleById(electricityCabinet);
        DbUtils.dbOperateSuccessThen(update, () -> {
            
            //更新缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId());
            
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + oldElectricityCabinet.getProductKey() + oldElectricityCabinet.getDeviceName());
            
            //添加快递柜格挡
            if (!oldModelId.equals(electricityCabinet.getModelId())) {
                electricityCabinetBoxService.batchDeleteBoxByElectricityCabinetId(electricityCabinet.getId());
                electricityCabinetBoxService.batchInsertBoxByModelId(electricityCabinetModel, electricityCabinet.getId());
            }
            
            //修改柜机服务时间信息
            electricityCabinetServerService.insertOrUpdateByElectricityCabinet(electricityCabinet, oldElectricityCabinet);
            
            //云端下发命令修改换电标准
            if (!Objects.equals(oldElectricityCabinet.getFullyCharged(), electricityCabinet.getFullyCharged())) {
                this.updateFullyChargedByCloud(electricityCabinet);
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
        electricityCabinet.setTenantId(TenantContextHolder.getTenantId());
        int update = electricityCabinetMapper.updateEleById(electricityCabinet);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //删除缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + id);
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
            
            //删除格挡
            electricityCabinetBoxService.batchDeleteBoxByElectricityCabinetId(id);
            
            electricityCabinetServerService.logicalDeleteByEid(id);
            return null;
        });
        return R.ok();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> physicsDelete(ElectricityCabinet electricityCabinet) {
        int delete = electricityCabinetMapper.deleteById(electricityCabinet.getId());
        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId());
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
            
            //删除电柜服务时间
            electricityCabinetServerService.deleteByEid(electricityCabinet.getId());
        });
        return delete > 0 ? Triple.of(true, null, null) : Triple.of(false, "", "删除失败");
    }
    
    @Override
    public R queryList(ElectricityCabinetQuery electricityCabinetQuery) {
        
        List<ElectricityCabinetVO> electricityCabinetList = electricityCabinetMapper.queryList(electricityCabinetQuery);
        if (ObjectUtil.isEmpty(electricityCabinetList)) {
            return R.ok();
        }
        
        if (ObjectUtil.isNotEmpty(electricityCabinetList)) {
            electricityCabinetList.parallelStream().forEach(e -> {
                
                Store store = storeService.queryByIdFromCache(Long.valueOf(e.getStoreId()));
                e.setStoreName(Objects.isNull(store) ? "" : store.getName());
                
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
                Integer fullyElectricityBattery = 0;
                int electricityBatteryTotal = 0;
                int noElectricityBattery = 0;
                int batteryInElectricity = 0;
/*                List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService
                        .queryBoxByElectricityCabinetId(e.getId());
                if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {

                    //空仓
                    noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery)
                            .count();

                    //禁用的仓门
                    batteryInElectricity = (int) electricityCabinetBoxList.stream().filter(this::isBatteryInElectricity)
                            .count();

                    //电池总数
                    electricityBatteryTotal = (int) electricityCabinetBoxList.stream()
                            .filter(this::isElectricityBattery).count();
                }*/
                
                Double fullyCharged = e.getFullyCharged();
                
                List<ElectricityCabinetBox> cabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(e.getId());
                if (!CollectionUtils.isEmpty(cabinetBoxList)) {
                    //空仓
                    noElectricityBattery = (int) cabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
                    //有电池数量
                    batteryInElectricity = (int) cabinetBoxList.stream().filter(this::isBatteryInElectricity).count();
                    //电池总数
                    electricityBatteryTotal = (int) cabinetBoxList.stream().filter(this::isElectricityBattery).count();
                    //可换电电池数
                    fullyElectricityBattery = (int) cabinetBoxList.stream().filter(i -> isExchangeable(i, fullyCharged)).count();
                }
                
                boolean result = deviceIsOnline(e.getProductKey(), e.getDeviceName());
                
                ElectricityCabinet item = new ElectricityCabinet();
                item.setUpdateTime(System.currentTimeMillis());
                item.setId(e.getId());
                
                if (result) {
                    item.setOnlineStatus(e.getOnlineStatus());
                    checkCupboardStatusAndUpdateDiff(true, item);
                    e.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS);
                } else {
                    item.setOnlineStatus(e.getOnlineStatus());
                    checkCupboardStatusAndUpdateDiff(false, item);
                    e.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
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
                
                ElectricityCabinetServer electricityCabinetServer = electricityCabinetServerService.queryByProductKeyAndDeviceName(e.getProductKey(), e.getDeviceName());
                if (Objects.nonNull(electricityCabinetServer)) {
                    e.setServerBeginTime(electricityCabinetServer.getServerBeginTime());
                    e.setServerEndTime(electricityCabinetServer.getServerEndTime());
                }
            });
        }
        electricityCabinetList.stream().sorted(Comparator.comparing(ElectricityCabinetVO::getCreateTime).reversed()).collect(Collectors.toList());
        return R.ok(electricityCabinetList);
    }
    
    @Override
    public Triple<Boolean, String, Object> updateOnlineStatus(Long id) {
        ElectricityCabinet electricityCabinet = this.queryByIdFromCache(id.intValue());
        if (Objects.isNull(electricityCabinet) || !Objects.equals(TenantContextHolder.getTenantId(), electricityCabinet.getTenantId())) {
            return Triple.of(false, "100003", "柜机不存在");
        }
        
        ElectricityCabinet electricityCabinetUpdate = new ElectricityCabinet();
        
        if (deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName())) {
            electricityCabinetUpdate.setOnlineStatus(electricityCabinet.getOnlineStatus());
            checkCupboardStatusAndUpdateDiff(true, electricityCabinetUpdate);
        } else {
            electricityCabinetUpdate.setOnlineStatus(electricityCabinet.getOnlineStatus());
            checkCupboardStatusAndUpdateDiff(false, electricityCabinetUpdate);
        }
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> updateAddress(ElectricityCabinetAddressQuery eleCabinetAddressQuery) {
        ElectricityCabinet electricityCabinet = this.queryByIdFromCache(eleCabinetAddressQuery.getId());
        if (Objects.isNull(electricityCabinet) || !Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        ElectricityCabinet electricityCabinetUpdate = new ElectricityCabinet();
        electricityCabinetUpdate.setId(electricityCabinet.getId());
        electricityCabinetUpdate.setAddress(eleCabinetAddressQuery.getAddress());
        electricityCabinetUpdate.setLatitude(eleCabinetAddressQuery.getLatitude());
        electricityCabinetUpdate.setLongitude(eleCabinetAddressQuery.getLongitude());
        this.electricityCabinetMapper.updateById(electricityCabinetUpdate);
        return Triple.of(true, null, null);
    }
    
    @Slave
    @Override
    public CabinetBatteryVO batteryStatistics(Long eid) {
        ElectricityCabinet cabinet = this.queryByIdFromCache(eid.intValue());
        if (Objects.isNull(cabinet)) {
            return null;
        }
        
        Double fullyCharged = cabinet.getFullyCharged();
        
        List<ElectricityCabinetBox> cabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(eid.intValue());
        if (CollectionUtils.isEmpty(cabinetBoxList)) {
            return null;
        }
        
        CabinetBatteryVO cabinetBatteryVO = new CabinetBatteryVO();
        //空仓
        long emptyCellNumber = cabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
        //有电池仓门
        long haveBatteryNumber = cabinetBoxList.stream().filter(this::isBatteryInElectricity).count();
        //可换电数量
        long exchangeableNumber = cabinetBoxList.stream().filter(item -> isExchangeable(item, fullyCharged)).count();
        
        cabinetBatteryVO.setEmptyCellNumber(emptyCellNumber);
        cabinetBatteryVO.setHaveBatteryNumber(haveBatteryNumber);
        cabinetBatteryVO.setExchangeableNumber(exchangeableNumber);
        return cabinetBatteryVO;
    }
    
    /**
     * TODO 优化
     *
     * @param electricityCabinetQuery
     * @return
     */
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
                
                //电柜不在线也返回，可离线换电
                if (Objects.equals(e.getUsableStatus(), ElectricityCabinet.ELECTRICITY_CABINET_USABLE_STATUS)) {
                    electricityCabinets.add(e);
                }
            });
        }
        return R.ok(electricityCabinets.stream().sorted(Comparator.comparing(ElectricityCabinetVO::getDistance)).collect(Collectors.toList()));
    }
    
    /**
     * TODO 优化
     *
     * @param electricityCabinetQuery
     * @return
     */
    @Override
    public R showInfoByDistanceV2(ElectricityCabinetQuery electricityCabinetQuery) {
        
        Double distanceMax = Objects.isNull(eleCommonConfig.getShowDistance()) ? 50000D : eleCommonConfig.getShowDistance();
        if (Objects.isNull(electricityCabinetQuery.getDistance()) || electricityCabinetQuery.getDistance() > distanceMax) {
            electricityCabinetQuery.setDistance(distanceMax);
        }
        
        List<ElectricityCabinetVO> electricityCabinetList = electricityCabinetMapper.showInfoByDistance(electricityCabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinetList)) {
            return R.ok(Collections.emptyList());
        }
        
        List<ElectricityCabinetVO> resultVo = electricityCabinetList.stream().map(e -> {
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
            
            List<ElectricityCabinetBox> cabinetBoxList = electricityCabinetBoxService.selectEleBoxAttrByEid(e.getId());
            if (CollectionUtils.isEmpty(cabinetBoxList)) {
                return null;
            }
            
            //空仓
            long emptyCellNumber = cabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
            //有电池仓门
            long haveBatteryNumber = cabinetBoxList.stream().filter(this::isBatteryInElectricity).count();
            
            //可换电数量
            List<ElectricityCabinetBox> exchangeableList = cabinetBoxList.stream().filter(item -> isExchangeable(item, e.getFullyCharged())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(exchangeableList)) {
                assignExchangeableBatteyType(exchangeableList, e);
               // assignExchangeableVoltageAndCapacity(exchangeableList, e);
            }
            long exchangeableNumber = exchangeableList.size();
            
            //满电电池数量
            long fullyElectricityBattery = cabinetBoxList.stream().filter(this::isFullBattery).count();
            
            e.setElectricityBatteryTotal((int) haveBatteryNumber);
            e.setNoElectricityBattery((int) emptyCellNumber);
            e.setFullyElectricityBattery((int) exchangeableNumber);//兼容2.0小程序首页显示问题
            e.setFullyBatteryNumber((int) fullyElectricityBattery);
            e.setExchangeBattery((int) exchangeableNumber);
            
            assignBatteryTypes(cabinetBoxList, e);
            
            //电柜不在线也返回，可离线换电
            if (Objects.equals(e.getUsableStatus(), ElectricityCabinet.ELECTRICITY_CABINET_USABLE_STATUS)) {
                return e;
            }
            return null;
            
        }).filter(Objects::nonNull).collect(Collectors.toList());
        
        return R.ok(resultVo.stream().sorted(Comparator.comparing(ElectricityCabinetVO::getDistance)).collect(Collectors.toList()));
    }
    
    private void assignExchangeableBatteyType(List<ElectricityCabinetBox> exchangeableList, ElectricityCabinetVO e) {
        HashMap<String, Integer> batteryTypeMap = new HashMap<>();
        exchangeableList.forEach(electricityCabinetBox -> {
            String batteryType = electricityCabinetBox.getBatteryType();
            if (StringUtils.isNotBlank(batteryType)) {
                String key = subStringButteryType(batteryType);
                if (StringUtils.isNotBlank(key)) {
                    //统计可换电电池型号
                    if (batteryTypeMap.containsKey(key)) {
                        Integer count = batteryTypeMap.get(key);
                        batteryTypeMap.put(key, count + 1);
                    } else {
                        batteryTypeMap.put(key, 1);
                    }
                }
            }
        });
        e.setExchangebleMapes(batteryTypeMap);
    }
    
    private void assignExchangeableVoltageAndCapacity(List<ElectricityCabinetBox> exchangeableList, ElectricityCabinetVO e) {
        HashMap<String, Integer> voltageAndCapacityMap = new HashMap<>();
        
        // 根据可换电格挡电池的sn列表查询电池列表获取容量
        List<String> snList = exchangeableList.stream().map(ElectricityCabinetBox::getSn).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<ElectricityBattery> batteryList = electricityBatteryService.listBatteryBySnList(snList);
        
        Map<String, Integer> capacityMap = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(batteryList)) {
            capacityMap = batteryList.stream().collect(Collectors.toMap(ElectricityBattery::getSn, ElectricityBattery::getCapacity, (k1, k2) -> k1));
        }
        
        // 获取电池的容量
        Map<String, Integer> finalCapacityMap = capacityMap;
        
        exchangeableList.forEach(electricityCabinetBox -> {
            String batteryType = electricityCabinetBox.getBatteryType();
            String sn = electricityCabinetBox.getSn();
            if (StringUtils.isNotBlank(batteryType)) {
                String key = subStringVoltageAndCapacity(batteryType, finalCapacityMap.get(sn));
                
                // 统计可换电电池型号
                if (voltageAndCapacityMap.containsKey(key)) {
                    Integer count = voltageAndCapacityMap.get(key);
                    voltageAndCapacityMap.put(key, count + 1);
                } else {
                    voltageAndCapacityMap.put(key, 1);
                }
            }
        });
        e.setVoltageAndCapacityMapes(voltageAndCapacityMap);
    }
    
    public HashMap<String, Integer> assignExchangeableVoltageAndCapacityV2(List<ElectricityCabinetBox> exchangeableList) {
        HashMap<String, Integer> voltageAndCapacityMap = new HashMap<>();
        // 根据可换电格挡电池的sn列表查询电池列表获取容量
        List<String> snList = exchangeableList.stream().map(ElectricityCabinetBox::getSn).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<ElectricityBattery> batteryList = electricityBatteryService.listBatteryBySnList(snList);
        
        Map<String, Integer> capacityMap = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(batteryList)) {
            capacityMap = batteryList.stream().collect(Collectors.toMap(ElectricityBattery::getSn, ElectricityBattery::getCapacity, (k1, k2) -> k1));
        }
        
        // 获取电池的容量
        Map<String, Integer> finalCapacityMap = capacityMap;
        
        exchangeableList.forEach(electricityCabinetBox -> {
            String batteryType = electricityCabinetBox.getBatteryType();
            String sn = electricityCabinetBox.getSn();
            if (StringUtils.isNotBlank(batteryType)) {
                String key = subStringVoltageAndCapacity(batteryType, finalCapacityMap.get(sn));
                
                // 统计可换电电池型号
                if (voltageAndCapacityMap.containsKey(key)) {
                    Integer count = voltageAndCapacityMap.get(key);
                    voltageAndCapacityMap.put(key, count + 1);
                } else {
                    voltageAndCapacityMap.put(key, 1);
                }
            }
        });
        return voltageAndCapacityMap;
    }
    
    private void assignBatteryTypes(List<ElectricityCabinetBox> cabinetBoxList, ElectricityCabinetVO e) {
        if (CollectionUtils.isEmpty(cabinetBoxList)) {
            return;
        }
        
        Map<String, Long> batteryTypeMapes = Maps.newHashMap();
        List<ElectricityCabinetBox> cabinetBoxes = cabinetBoxList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getSn()) && StringUtils.isNotBlank(item.getBatteryType()) && !StringUtils.startsWithIgnoreCase(item.getSn(), "UNKNOW"))
                .peek(i -> i.setBatteryType(i.getBatteryType().substring(i.getBatteryType().indexOf("_") + 1)
                        .substring(0, i.getBatteryType().substring(i.getBatteryType().indexOf("_") + 1).indexOf("_")))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(cabinetBoxes)) {
            return;
        }
        
        Set<String> batterySet = cabinetBoxes.stream().map(ElectricityCabinetBox::getBatteryType).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(batterySet)) {
            return;
        }
        
        batterySet.forEach(batteryType -> {
            long count = cabinetBoxes.stream().filter(t -> Objects.equals(t.getBatteryType(), batteryType) && Objects.nonNull(t.getPower()) && Objects.nonNull(e.getFullyCharged())
                    && t.getPower() >= e.getFullyCharged()).count();
            batteryTypeMapes.put(batteryType, count);
        });
        
        e.setBatteryTypeMapes(batteryTypeMapes);
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
                //根据电池id和加盟商id查询电池
                ElectricityBattery battery = electricityBatteryService.selectByBatteryIdAndFranchiseeId(item, franchiseeId);
                if (Objects.nonNull(battery)) {
                    count++;
                }
            }
            
            if (count < 1) {
                return Triple.of(false, "0", "加盟商未绑定满电电池");
            }
            
            return Triple.of(false, "0", "换电柜暂无满电电池");
        }
        
        for (Long item : ids) {
            ElectricityBattery battery = electricityBatteryService.selectByBatteryIdAndFranchiseeId(item, franchiseeId);
            if (Objects.nonNull(battery)) {
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
            ElectricityBattery battery = electricityBatteryService.selectByBatteryIdAndFranchiseeId(item, franchiseeId);
            if (Objects.nonNull(battery)) {
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
        GetDeviceStatusResponse getDeviceStatusResponse = pubHardwareService.queryDeviceStatusFromIot(productKey, deviceName);
        if (Objects.isNull(getDeviceStatusResponse)) {
            return false;
        }
        
        GetDeviceStatusResponse.Data data = getDeviceStatusResponse.getData();
        if (Objects.isNull(data)) {
            return false;
        }
        
        String status = Optional.ofNullable(data.getStatus()).orElse("UNKNOW").toLowerCase();
        if ("ONLINE".equalsIgnoreCase(status)) {
            return true;
        }
        return false;
    }
    
    
    @Override
    public Integer queryByModelId(Integer id) {
        return electricityCabinetMapper.selectCount(
                Wrappers.<ElectricityCabinet>lambdaQuery().eq(ElectricityCabinet::getModelId, id).eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
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
        electricityCabinet.setTenantId(TenantContextHolder.getTenantId());
        electricityCabinetMapper.updateEleById(electricityCabinet);
        
        //更新缓存
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
        
        redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + oldElectricityCabinet.getProductKey() + oldElectricityCabinet.getDeviceName());
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
            if (Objects.equals(user.getType(), User.TYPE_USER_SUPER) || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
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
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER) && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userDataType:{}", user.getDataType());
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
                if (Objects.equals(user.getType(), User.TYPE_USER_SUPER) || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
                    //1、直接查柜子
                    electricityCabinetList = this.electricityCabinetMapper.homeOne(finalEleIdList, tenantId);
                } else if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
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
                        boolean result = Objects.equals(electricityCabinet.getOnlineStatus(), ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS);
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
            if (Objects.equals(user.getType(), User.TYPE_USER_SUPER) || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(),
                    User.DATA_TYPE_FRANCHISEE)) {
                
                BigDecimal moneyCount = null;
                //查月卡
                if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
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
            if (Objects.equals(user.getType(), User.TYPE_USER_SUPER) || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(),
                    User.DATA_TYPE_FRANCHISEE)) {
                
                Integer storeCount = 0;
                //查用户
                if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
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
        } catch (Exception e) {
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
        if (Objects.equals(user.getType(), User.TYPE_USER_SUPER) || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(),
                User.DATA_TYPE_FRANCHISEE)) {
            
            //如果用户类型不等于admin和运营商，则查询绑定的月卡
            if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
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
            if (Objects.equals(user.getType(), User.TYPE_USER_SUPER) || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
                homeThree = userInfoService.homeThree(beginTime, endTime, tenantId);
            }
            return R.ok(homeThree);
        }
        
        //查柜机
        if (type == 2) {
            //如果用户类型不等于admin和运营商，则查询绑定的换电柜
            if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER) && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
                
                UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
                if (Objects.isNull(userTypeService)) {
                    log.warn("USER TYPE ERROR! not found operate service! userDataType:{}", user.getDataType());
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
            if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER) && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
                
                UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
                if (Objects.isNull(userTypeService)) {
                    log.warn("USER TYPE ERROR! not found operate service! userDataType:{}", user.getDataType());
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
            if (Objects.equals(user.getType(), User.TYPE_USER_SUPER) || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(),
                    User.DATA_TYPE_FRANCHISEE)) {
                
                //查用户
                if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
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
    public List<HashMap<String, String>> homeThreeInner(Long startTimeMilliDay, Long endTimeMilliDay, List<Integer> eleIdList, Integer tenantId) {
        return electricityCabinetMapper.homeThree(startTimeMilliDay, endTimeMilliDay, eleIdList, tenantId);
    }
    
    @Override
    public R home() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        HashMap<String, Object> homeInfo = new HashMap<>();
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
            log.error("HOME  ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        //判断用户套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        
        ElectricityMemberCard electricityMemberCard = null;
        if (Objects.nonNull(userBatteryMemberCard) && !Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_MEMBER_CARD_ID_ZERO)) {
            electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        }
        
        if (Objects.nonNull(userBatteryMemberCard) && Objects.isNull(electricityMemberCard) && !Objects.equals(userBatteryMemberCard.getRemainingNumber(),
                UserBatteryMemberCard.SEND_REMAINING_NUMBER) && !Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_MEMBER_CARD_ID_ZERO)) {
            log.error("HOME ERROR! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }
        
        //套餐剩余天数
        Double cardDay = 0.0D;
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            now = userBatteryMemberCard.getDisableMemberCardTime();
        }
        
        if (Objects.nonNull(userBatteryMemberCard) && !Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER) && Objects.nonNull(
                electricityMemberCard)) {
            if (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                if (Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && Objects.nonNull(userBatteryMemberCard.getRemainingNumber())
                        && userBatteryMemberCard.getRemainingNumber() > 0 && userBatteryMemberCard.getMemberCardExpireTime() > now) {
                    cardDay = Math.ceil((userBatteryMemberCard.getMemberCardExpireTime() - now) / 1000 / 60 / 60 / 24.0);
                }
            } else if (Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && Objects.nonNull(userBatteryMemberCard.getRemainingNumber())
                    && userBatteryMemberCard.getMemberCardExpireTime() > now) {
                cardDay = Math.ceil((userBatteryMemberCard.getMemberCardExpireTime() - now) / 1000 / 60 / 60 / 24.0);
            }
        } else {
            if (Objects.nonNull(userBatteryMemberCard) && Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && Objects.nonNull(
                    userBatteryMemberCard.getRemainingNumber()) && userBatteryMemberCard.getRemainingNumber() > 0 && userBatteryMemberCard.getMemberCardExpireTime() > now) {
                cardDay = Math.ceil((userBatteryMemberCard.getMemberCardExpireTime() - now) / 1000 / 60 / 60 / 24.0);
            }
        }
        
        //我的电池
        Double battery = null;
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
        if (Objects.nonNull(electricityBattery)) {
            battery = electricityBattery.getPower();
        }
        
        //套餐到期时间
        
        String memberCardExpireTime = null;
        Integer memberCardDisableStatus = null;
        if (Objects.nonNull(userBatteryMemberCard)) {
            memberCardExpireTime =
                    Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) ? DateUtil.format(DateUtil.date(userBatteryMemberCard.getMemberCardExpireTime()),
                            DatePattern.NORM_DATE_FORMAT) : "";
            memberCardDisableStatus = userBatteryMemberCard.getMemberCardStatus();
        }
        homeInfo.put("memberCardExpireTime", memberCardExpireTime);
        //月卡剩余天数
        homeInfo.put("monthCount", monthCount);
        homeInfo.put("totalCount", totalCount);
        //        homeInfo.put("serviceStatus", serviceStatus);
        homeInfo.put("cardDay", cardDay.intValue());
        homeInfo.put("battery", battery);
        homeInfo.put("memberCardDisableStatus", memberCardDisableStatus);
        
        return R.ok(homeInfo);
    }
    
    
    @Override
    public ElectricityCabinet queryByProductAndDeviceName(String productKey, String deviceName) {
        
        ElectricityCabinet electricityCabinet = electricityCabinetMapper.selectOne(
                new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getProductKey, productKey).eq(ElectricityCabinet::getDeviceName, deviceName)
                        .eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
        if (Objects.isNull(electricityCabinet)) {
            return null;
        }
        return electricityCabinet;
    }
    
    @Override
    public ElectricityCabinet queryFromCacheByProductAndDeviceName(String productKey, String deviceName) {
        //先查缓存
        ElectricityCabinet cacheElectricityCabinet = redisService.getWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + productKey + deviceName, ElectricityCabinet.class);
        if (Objects.nonNull(cacheElectricityCabinet)) {
            return cacheElectricityCabinet;
        }
        
        //缓存没有再查数据库
        ElectricityCabinet electricityCabinet = electricityCabinetMapper.selectOne(
                new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getProductKey, productKey).eq(ElectricityCabinet::getDeviceName, deviceName)
                        .eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
        if (Objects.isNull(electricityCabinet)) {
            return null;
        }
        
        //放入缓存
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + productKey + deviceName, electricityCabinet);
        return electricityCabinet;
    }
    
    @Override
    public R checkOpenSessionId(String sessionId) {
        String result = redisService.get(CacheConstant.ELE_OPERATOR_CACHE_KEY + sessionId);
        if (StrUtil.isEmpty(result)) {
            return R.ok("0001");
        }
        
        Map<String, Object> map = JsonUtil.fromJson(result, Map.class);
        String value = map.get("success").toString();
        if ("true".equalsIgnoreCase(value)) {
            return R.ok("0002");
        } else {
            return R.ok(map);
        }
    }
    
    @Override
    public R sendCommandToEleForOuter(EleOuterCommandQuery eleOuterCommandQuery) {
        //不合法的参数
        if (Objects.isNull(eleOuterCommandQuery.getCommand()) || Objects.isNull(eleOuterCommandQuery.getDeviceName()) || Objects.isNull(eleOuterCommandQuery.getProductKey())) {
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
            return R.fail("100004", "柜机不在线");
        }
        
        //不合法的命令
        //        if (!ElectricityIotConstant.ELE_COMMAND_MAPS.containsKey(eleOuterCommandQuery.getCommand())) {
        if (!ElectricityIotConstant.isLegalCommand(eleOuterCommandQuery.getCommand())) {
            return R.fail("ELECTRICITY.0036", "不合法的命令");
        }
        
        Map<String, Object> dataMap = null;
        if (CollectionUtils.isEmpty(eleOuterCommandQuery.getData())) {
            dataMap = Maps.newHashMap();
        } else {
            dataMap = eleOuterCommandQuery.getData();
        }
        
        dataMap.put("uid", SecurityUtils.getUid());
        dataMap.put("username", SecurityUtils.getUserInfo().getUsername());
        eleOuterCommandQuery.setData(dataMap);
        
        //开全部门 -->  cell_all_open_door
        if (Objects.equals(ElectricityIotConstant.ELE_COMMAND_CELL_ALL_OPEN_DOOR, eleOuterCommandQuery.getCommand())) {
            List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinet.getId());
            if (ObjectUtil.isEmpty(electricityCabinetBoxList)) {
                return R.fail("ELECTRICITY.0014", "换电柜没有仓门，不能开门");
            }
            
            List<String> cellList = new ArrayList<>();
            for (ElectricityCabinetBox electricityCabinetBox : electricityCabinetBoxList) {
                cellList.add(electricityCabinetBox.getCellNo());
            }
            dataMap.put("cell_list", cellList);
            
        }
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(eleOuterCommandQuery.getSessionId()).data(eleOuterCommandQuery.getData())
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(eleOuterCommandQuery.getCommand()).build();
        
        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        //发送命令失败
        if (!result.getLeft()) {
            return R.fail("ELECTRICITY.0037", "发送命令失败");
        }
        return R.ok(sessionId);
    }
    
    
    @Override
    public R sendCommandToEleForOuterSuper(EleOuterCommandQuery eleOuterCommandQuery) {
        //不合法的参数
        if (Objects.isNull(eleOuterCommandQuery.getCommand()) || Objects.isNull(eleOuterCommandQuery.getDeviceName()) || Objects.isNull(eleOuterCommandQuery.getProductKey())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        eleOuterCommandQuery.setSessionId(sessionId);
        
        ElectricityCabinet electricityCabinet = queryByProductAndDeviceName(eleOuterCommandQuery.getProductKey(), eleOuterCommandQuery.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
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
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(eleOuterCommandQuery.getSessionId()).data(eleOuterCommandQuery.getData())
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(eleOuterCommandQuery.getCommand()).build();
        
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
                
                //获取柜机可换电数和空仓数
                List<ElectricityCabinetBox> electricityCabinetBoxes = electricityCabinetBoxService.queryAllBoxByElectricityCabinetId(e.getId());
                if (!CollectionUtils.isEmpty(electricityCabinetBoxes)) {
                    
                    ElectricityCabinet eleCabinet = this.queryByIdFromCache(e.getId());
                    if (Objects.isNull(eleCabinet)) {
                        return;
                    }
                    
                    //空仓
                    Long emptyCellNumber = electricityCabinetBoxes.stream().filter(this::isNoElectricityBattery).count();
                    //有电池仓门
                    Long haveBatteryNumber = electricityCabinetBoxes.stream().filter(this::isBatteryInElectricity).count();
                    //可换电数量
                    Long exchangeableNumber = electricityCabinetBoxes.stream().filter(item -> isExchangeable(item, eleCabinet.getFullyCharged())).count();
                    
                    e.setNoElectricityBattery(emptyCellNumber.intValue());
                    e.setFullyElectricityBattery(exchangeableNumber.intValue());
                    e.setElectricityBatteryTotal(haveBatteryNumber.intValue());
                }
                
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
        return electricityCabinetMapper.selectList(
                new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getStoreId, storeId).eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
        
    }
    
    @Override
    public R queryByDevice(String productKey, String deviceName) {
        
        //换电柜
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.error("queryByDevice  ERROR! not found electricityCabinet ！productKey{},deviceName{}", productKey, deviceName);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
        BeanUtils.copyProperties(electricityCabinet, electricityCabinetVO);
        
        Franchisee franchisee = (Franchisee) franchiseeService.queryByCabinetId(electricityCabinet.getId(), electricityCabinet.getTenantId()).getData();
        if (Objects.nonNull(franchisee)) {
            electricityCabinetVO.setFranchiseeName(franchisee.getName());
            electricityCabinetVO.setFranchiseeId(franchisee.getId());
        }
        
        if (deviceIsOnline(productKey, deviceName)) {
            electricityCabinetVO.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS);
            checkCupboardStatusAndUpdateDiff(true, electricityCabinet);
        } else {
            electricityCabinetVO.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
            checkCupboardStatusAndUpdateDiff(false, electricityCabinet);
        }
        
        // 设置营业时间
        assignBusinessTime(electricityCabinetVO);
        
        return R.ok(electricityCabinetVO);
        
    }
    
    private void assignBusinessTime(ElectricityCabinetVO electricityCabinetVO) {
        //营业时间
        if (Objects.nonNull(electricityCabinetVO.getBusinessTime())) {
            String businessTime = electricityCabinetVO.getBusinessTime();
            if (Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                electricityCabinetVO.setBusinessTimeType(ElectricityCabinetVO.ALL_DAY);
                electricityCabinetVO.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
            } else {
                electricityCabinetVO.setBusinessTimeType(ElectricityCabinetVO.ILLEGAL_DATA);
                int index = businessTime.indexOf("-");
                if (!Objects.equals(index, -1) && index > 0) {
                    electricityCabinetVO.setBusinessTimeType(ElectricityCabinetVO.CUSTOMIZE_TIME);
                    Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                    Long beginTime = getTime(totalBeginTime);
                    Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                    Long endTime = getTime(totalEndTime);
                    electricityCabinetVO.setBeginTime(totalBeginTime);
                    electricityCabinetVO.setEndTime(totalEndTime);
                    Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                    long now = System.currentTimeMillis();
                    if (firstToday + beginTime > now || firstToday + endTime < now) {
                        electricityCabinetVO.setIsBusiness(ElectricityCabinetVO.IS_NOT_BUSINESS);
                    } else {
                        electricityCabinetVO.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
                    }
                }
            }
        }
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
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("queryByDevice  ERROR! not auth,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        
        //判断该换电柜加盟商和用户加盟商是否一致
        if (!Objects.equals(store.getFranchiseeId(), userInfo.getFranchiseeId())) {
            log.error("queryByDevice  ERROR!FranchiseeId is not equal!uid={} , FranchiseeId1={} ,FranchiseeId2={}", user.getUid(), store.getFranchiseeId(),
                    userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
        }
        
        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("queryByDevice  ERROR! user not pay deposit,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(
                userBatteryMemberCard.getRemainingNumber())) {
            log.warn("HOME WARN! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }
        
        Long now = System.currentTimeMillis();
        if (userBatteryMemberCard.getMemberCardExpireTime() < now || userBatteryMemberCard.getRemainingNumber() == 0) {
            log.error("queryByDevice ERROR!  memberCard is  Expire !  uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0023", "月卡已过期");
        }
        
        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        if (Objects.isNull(electricityMemberCard)) {
            log.error("HOME ERROR! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }
        
        //未租电池
        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.error("queryByDevice ERROR! user not rent battery,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("ELE MEMBERCARD ERROR! not found franchisee,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0038", "加盟商不存在");
        }
        
        UserBattery userBattery = userBatteryService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBattery)) {
            log.error("ELE MEMBERCARD ERROR! not found userBattery,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0033", "用户未绑定电池型号");
        }
        
        ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
        BeanUtil.copyProperties(electricityCabinet, electricityCabinetVO);
        
        //        查满仓空仓数
        Triple<Boolean, String, Object> tripleResult;
        if (Objects.equals(franchisee.getModelType(), FranchiseeUserInfo.NEW_MODEL_TYPE)) {
            tripleResult = queryFullyElectricityBatteryByOrder(electricityCabinet.getId(), null, userInfo.getFranchiseeId());
        } else {
            tripleResult = queryFullyElectricityBatteryByExchangeOrder(electricityCabinet.getId(), null, userInfo.getFranchiseeId(), electricityCabinet.getTenantId());
        }
        
        if (Objects.isNull(tripleResult)) {
            Integer value = checkIsLowBatteryExchange(electricityCabinet.getTenantId(), electricityCabinet.getId(), userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0026", "换电柜暂无满电电池", value);
        }
        
        if (!tripleResult.getLeft()) {
            Integer value = checkIsLowBatteryExchange(electricityCabinet.getTenantId(), electricityCabinet.getId(), userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0026", tripleResult.getRight().toString(), value);
        }
        
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
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("queryByRentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getAllowRentEle(), ElectricityConfig.NOT_ALLOW_RENT_ELE)) {
            return R.fail("ELECTRICITY.100271", "当前柜机不支持租电");
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
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }
        
        //换电柜是否出现异常被锁住
        String isLock = redisService.get(CacheConstant.UNLOCK_CABINET_CACHE + electricityCabinet.getId());
        if (StringUtils.isNotEmpty(isLock)) {
            log.error("queryByRentBattery  ERROR!  electricityCabinet is lock ！electricityCabinet={}", electricityCabinet);
            return R.fail("ELECTRICITY.0063", "换电柜出现异常，暂时不能下单");
        }
        
        //营业时间
        boolean result = this.isBusiness(electricityCabinet);
        if (result) {
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }
        
        //查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            log.error("queryByDevice  ERROR! not found store ！electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.error("queryByDevice  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        
        //查找门店加盟商
        if (Objects.isNull(store.getFranchiseeId())) {
            log.error("queryByDevice  ERROR! not found Franchisee ！storeId={}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }
        
        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("queryByRentBattery  ERROR! not found user!uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("queryByRentBattery  ERROR! user is unUsable! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("queryByRentBattery  ERROR! USER not auth,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        
        //判断该换电柜加盟商和用户加盟商是否一致
        if (!Objects.equals(store.getFranchiseeId(), userInfo.getFranchiseeId())) {
            log.error("queryByDevice  ERROR!FranchiseeId is not equal!uid={} , FranchiseeId1={} ,FranchiseeId2={}", user.getUid(), store.getFranchiseeId(),
                    userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
        }
/*
        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("queryByDevice  ERROR! user not pay deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService
                .selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("HOME WARN! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            Long now = System.currentTimeMillis();
            if (userBatteryMemberCard.getMemberCardExpireTime() < now) {
                log.error("rentBattery  ERROR! memberCard  is Expire,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0023", "月卡已过期");
            }
        }
        */
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("ELE MEMBERCARD ERROR! not found franchisee,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0038", "加盟商不存在");
        }
        
        //组装数据
        ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
        BeanUtil.copyProperties(electricityCabinet, electricityCabinetVO);
/*
        //查满仓空仓数
        int electricityBatteryTotal = 0;
        int noElectricityBattery = 0;
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService
                .queryBoxByElectricityCabinetId(electricityCabinetVO.getId());
        if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
            //空仓
            noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery)
                    .count();
            //电池总数
            electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery)
                    .count();
        }

        Triple<Boolean, String, Object> tripleResult;
        //查满仓空仓数
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            tripleResult = queryFullyElectricityBatteryByOrder(electricityCabinet.getId(), userBattery.getBatteryType(), userInfo.getFranchiseeId());
        } else {
            tripleResult = queryFullyElectricityBatteryByOrder(electricityCabinet.getId(), null,
                    userInfo.getFranchiseeId());
        }

        //已租电池则还电池
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
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
        }*/
        return R.ok(electricityCabinetVO);
    }
    
    @Slave
    @Override
    public List<Map<String, Object>> queryNameList(Long size, Long offset, List<Integer> eleIdList, Integer tenantId) {
        return electricityCabinetMapper.queryNameList(size, offset, eleIdList, tenantId);
    }
    
    @Override
    public R batteryReport(BatteryReportQuery batteryReportQuery) {
        
        String batteryName = batteryReportQuery.getBatteryName();
        if (StringUtils.isEmpty(batteryName)) {
            log.error("batteryName is null");
            return R.ok();
        }
        ElectricityBattery electricityBattery = electricityBatteryService.queryPartAttrBySnFromCache(batteryName);
        if (Objects.isNull(electricityBattery)) {
            log.warn("ele battery error! no electricityBattery,sn,{}", batteryName);
            return R.ok();
        }
        
        TenantContextHolder.setTenantId(electricityBattery.getTenantId());
        //电池电量上报变化在百分之50以上，不更新电池电量
        Double power = batteryReportQuery.getPower();
        //修改电池
        ElectricityBattery newElectricityBattery = new ElectricityBattery();
        newElectricityBattery.setId(electricityBattery.getId());
        
        BatteryGeo batteryGeo = new BatteryGeo();
        batteryGeo.setSn(electricityBattery.getSn());
        batteryGeo.setCreateTime(System.currentTimeMillis());
        batteryGeo.setUpdateTime(System.currentTimeMillis());
        batteryGeo.setTenantId(electricityBattery.getTenantId());
        batteryGeo.setFranchiseeId(electricityBattery.getFranchiseeId());
        
        if (Objects.nonNull(power)) {
            newElectricityBattery.setPower(power);
        }
        
        Double latitude = batteryReportQuery.getLatitude();
        if (Objects.nonNull(latitude)) {
            batteryGeo.setLatitude(latitude);
            newElectricityBattery.setLatitude(latitude);
        }
        
        Double longitude = batteryReportQuery.getLongitude();
        if (Objects.nonNull(longitude)) {
            batteryGeo.setLongitude(longitude);
            newElectricityBattery.setLongitude(longitude);
        }
        electricityBattery.setUpdateTime(System.currentTimeMillis());
        newElectricityBattery.setTenantId(electricityBattery.getTenantId());
        newElectricityBattery.setUpdateTime(System.currentTimeMillis());
        electricityBatteryService.update(newElectricityBattery);
        
        if (Objects.nonNull(batteryGeo.getLatitude()) && Objects.nonNull(batteryGeo.getLongitude())) {
            batteryGeoService.insertOrUpdate(batteryGeo);
        }
        
        //电池上报是否有其他信息,只处理电量
        //        if (Objects.nonNull(batteryReportQuery.getHasOtherAttr()) && batteryReportQuery.getHasOtherAttr()) {
        //            BatteryOtherProperties batteryOtherProperties = batteryReportQuery.getBatteryAttr();
        //            batteryOtherProperties.setBatteryName(batteryName);
        //            batteryOtherPropertiesService.insertOrUpdate(batteryOtherProperties);
        //        }
        
        return R.ok();
    }
    
    @Slave
    @Override
    public Integer selectOfflinePageCount(ElectricityCabinetQuery cabinetQuery) {
        return electricityCabinetMapper.selectOfflinePageCount(cabinetQuery);
    }
    
    @Slave
    @Override
    public List<EleCabinetDataAnalyseVO> selectLockCellByQuery(ElectricityCabinetQuery cabinetQuery) {
        return electricityCabinetMapper.selectLockCellByQuery(cabinetQuery);
    }
    
    @Slave
    @Override
    public Integer selectLockPageCount(ElectricityCabinetQuery cabinetQuery) {
        return electricityCabinetMapper.selectLockPageCount(cabinetQuery);
    }
    
    @Slave
    @Override
    public List<EleCabinetDataAnalyseVO> selectPowerPage(ElectricityCabinetQuery cabinetQuery) {
        log.info("start battery power query, cabinet query = {}", JsonUtil.toJson(cabinetQuery));
        return electricityCabinetMapper.selectPowerPage(cabinetQuery);
    }
    
    @Slave
    @Override
    public Integer selectPowerPageCount(ElectricityCabinetQuery cabinetQuery) {
        return electricityCabinetMapper.selectPowerPageCount(cabinetQuery);
    }
    
    @Override
    public boolean isNoElectricityBattery(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
    }
    
    @Override
    public boolean isBatteryInElectricity(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);
    }
    
    private boolean isElectricityBattery(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);
    }
    
    @Override
    public boolean isExchangeable(ElectricityCabinetBox electricityCabinetBox, Double fullyCharged) {
        return Objects.nonNull(electricityCabinetBox.getPower()) && Objects.nonNull(fullyCharged) && electricityCabinetBox.getPower() >= fullyCharged && StringUtils.isNotBlank(
                electricityCabinetBox.getSn()) && !StringUtils.startsWithIgnoreCase(electricityCabinetBox.getSn(), "UNKNOW");
    }
    
    @Override
    public boolean isFullBattery(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.nonNull(electricityCabinetBox.getPower()) && electricityCabinetBox.getPower() == 100d && StringUtils.isNotBlank(electricityCabinetBox.getSn())
                && !StringUtils.startsWithIgnoreCase(electricityCabinetBox.getSn(), "UNKNOW");
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
    
    @Slave
    @Override
    public R queryCount(ElectricityCabinetQuery electricityCabinetQuery) {
        return R.ok(electricityCabinetMapper.queryCount(electricityCabinetQuery));
    }
    
    @Override
    public Integer queryCountByStoreId(Long id) {
        return electricityCabinetMapper.selectCount(
                new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getStoreId, id).eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL)
                        .last("limit 0,1"));
    }
    
    @Override
    public R checkBattery(String productKey, String deviceName, String batterySn, Boolean isParseBattery) {
        //换电柜
        ElectricityCabinet electricityCabinet = queryByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.error("checkBattery error! no electricityCabinet,productKey={},deviceName={}", productKey, deviceName);
            return R.failMsg("未找到换电柜");
        }
        
        //电池
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(batterySn);
        if (Objects.isNull(electricityBattery)) {
            log.error("checkBattery error! no electricityBattery,sn={}", batterySn);
            return R.failMsg("未找到电池");
        }
        
        if (!Objects.equals(electricityCabinet.getTenantId(), electricityBattery.getTenantId())) {
            log.error("checkBattery error! tenantId is not equal,tenantId1={},tenantId2={}", electricityCabinet.getTenantId(), electricityBattery.getTenantId());
            return R.failMsg("电池与换电柜租户不匹配");
        }
        
        //查电池所属加盟商
        if (Objects.isNull(electricityBattery.getFranchiseeId())) {
            log.error("checkBattery error! battery not bind franchisee,electricityBatteryId={}", electricityBattery.getId());
            return R.failMsg("电池未绑定加盟商");
        }
        // 查换电柜所属加盟商
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.error("checkBattery error! not find store,storeId={}", electricityCabinet.getStoreId());
            return R.failMsg("找不到换电柜门店");
        }
        
        if (!Objects.equals(store.getFranchiseeId(), electricityBattery.getFranchiseeId())) {
            log.error("checkBattery error! franchisee is not equal,franchiseeId1={},franchiseeId2={}", store.getFranchiseeId(), electricityBattery.getFranchiseeId());
            return R.failMsg("电池加盟商与电柜加盟商不匹配");
        }
        
        //检查电池和用户是否匹配
        
        return R.ok();
    }
    
    @Override
    public R queryById(Integer id) {
        ElectricityCabinet electricityCabinet = queryByIdFromCache(id);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        if (!Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
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
        
        electricityCabinetVO.setOnlineStatus(electricityCabinet.getOnlineStatus());
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
    
    @Slave
    @Override
    public R queryCabinetBelongFranchisee(Integer id) {
        return franchiseeService.queryByCabinetId(id, TenantContextHolder.getTenantId());
    }
    
    /**
     * 换电柜3.0
     */
    @Override
    public Triple<Boolean, String, Object> findUsableBatteryCellNoV3(Integer eid, Franchisee franchisee, Double fullyCharged, ElectricityBattery electricityBattery, Long uid) {
        
        Integer tenantId = TenantContextHolder.getTenantId();
        if (Objects.nonNull(tenantId) && Objects.nonNull(eleCommonConfig.getSpecialTenantId()) && Objects.equals(eleCommonConfig.getSpecialTenantId(), tenantId)) {
            List<ElectricityCabinetBox> usableBatteryCellNos = electricityCabinetBoxService.queryUsableBatteryCellNo(eid, null, fullyCharged);
            if (CollectionUtils.isEmpty(usableBatteryCellNos)) {
                return Triple.of(false, "100216", "换电柜暂无满电电池");
            }
            
            List<Long> batteryIds = usableBatteryCellNos.stream().map(ElectricityCabinetBox::getBId).collect(Collectors.toList());
            
            List<ElectricityBattery> electricityBatteries = electricityBatteryService.selectByBatteryIds(batteryIds);
            if (CollectionUtils.isEmpty(electricityBatteries)) {
                return Triple.of(false, "100225", "电池不存在");
            }
            
            //把本柜机加盟商的绑定电池信息拿出来
            electricityBatteries = electricityBatteries.stream().filter(e -> Objects.equals(e.getFranchiseeId(), franchisee.getId())).collect(Collectors.toList());
            if (!DataUtil.collectionIsUsable(electricityBatteries)) {
                return Triple.of(false, "100219", "电池没有绑定加盟商,无法换电，请联系客服在后台绑定");
            }
            
            //获取全部可用电池id
            List<Long> bindingBatteryIds = electricityBatteries.stream().map(ElectricityBattery::getId).collect(Collectors.toList());
            
            //把加盟商绑定的电池过滤出来
            usableBatteryCellNos = usableBatteryCellNos.stream().filter(e -> bindingBatteryIds.contains(e.getBId())).collect(Collectors.toList());
            
            //获取用户绑定的型号
            List<String> userBatteryTypes = userBatteryTypeService.selectByUid(uid);
            if (!CollectionUtils.isEmpty(userBatteryTypes)) {
                usableBatteryCellNos = usableBatteryCellNos.stream().filter(e -> StringUtils.isNotBlank(e.getBatteryType()) && userBatteryTypes.contains(e.getBatteryType()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(usableBatteryCellNos)) {
                    return Triple.of(false, "100217", "换电柜暂无可用型号的满电电池");
                }
                
                //电量最大的
                Double maxPower = usableBatteryCellNos.get(0).getPower();
                usableBatteryCellNos = usableBatteryCellNos.stream().filter(item -> Objects.equals(item.getPower(), maxPower)).collect(Collectors.toList());
                if (usableBatteryCellNos.size() == 1) {
                    return Triple.of(true, null, usableBatteryCellNos.get(0));
                }
            }
            
            usableBatteryCellNos = usableBatteryCellNos.stream().filter(item -> StringUtils.isNotBlank(item.getSn()) && Objects.nonNull(item.getPower()))
                    .sorted(Comparator.comparing(ElectricityCabinetBox::getPower).reversed()).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(usableBatteryCellNos)) {
                return Triple.of(false, "", "换电柜暂无满电电池");
            }
            
            //如果存在多个电量相同的格挡，取充电器电压最大
            Double maxPower = usableBatteryCellNos.get(0).getPower();
            ElectricityCabinetBox usableCabinetBox = usableBatteryCellNos.stream().filter(item -> Objects.equals(item.getPower(), maxPower))
                    .filter(item -> Objects.nonNull(item.getChargeV())).sorted(Comparator.comparing(ElectricityCabinetBox::getChargeV)).reduce((first, second) -> second)
                    .orElse(null);
            if (Objects.isNull(usableCabinetBox)) {
                return Triple.of(false, "", "换电柜暂无满电电池");
            }
            
            return Triple.of(true, null, usableCabinetBox);
        } else {
            List<ElectricityCabinetBox> usableBatteryCellNos = electricityCabinetBoxService.queryUsableBatteryCellNo(eid, null, fullyCharged);
            if (CollectionUtils.isEmpty(usableBatteryCellNos)) {
                log.warn("EXCHANGE WARN!nou found full battery box,eid={}", eid);
                return Triple.of(false, "100216", "换电柜暂无满电电池");
            }
            
            List<Long> batteryIds = usableBatteryCellNos.stream().map(ElectricityCabinetBox::getBId).collect(Collectors.toList());
            
            List<ElectricityBattery> electricityBatteries = electricityBatteryService.selectByBatteryIds(batteryIds);
            if (CollectionUtils.isEmpty(electricityBatteries)) {
                return Triple.of(false, "100225", "电池不存在");
            }
            
            //把本柜机加盟商的绑定电池信息拿出来
            electricityBatteries = electricityBatteries.stream().filter(e -> Objects.equals(e.getFranchiseeId(), franchisee.getId())).collect(Collectors.toList());
            if (!DataUtil.collectionIsUsable(electricityBatteries)) {
                log.warn("EXCHANGE WARN!battery not bind franchisee,eid={}", eid);
                return Triple.of(false, "100219", "电池没有绑定加盟商,无法换电，请联系客服在后台绑定");
            }
            
            //获取全部可用电池id
            List<Long> bindingBatteryIds = electricityBatteries.stream().map(ElectricityBattery::getId).collect(Collectors.toList());
            
            //把加盟商绑定的电池过滤出来
            usableBatteryCellNos = usableBatteryCellNos.stream().filter(e -> bindingBatteryIds.contains(e.getBId())).collect(Collectors.toList());
            
            //多型号满电电池分配规则：优先分配当前用户绑定电池型号的电池，没有则分配电量最大的   若存在多个电量最大的，则分配用户绑定电池型号串数最大的电池
            if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                if (Objects.nonNull(electricityBattery)) {
                    //用户当前绑定电池的型号
                    String userCurrentBatteryType = electricityBattery.getModel();
                    List<ElectricityCabinetBox> userCurrentBatteryUsableBatteryCellNos = usableBatteryCellNos.stream()
                            .filter(e -> StrUtil.equalsIgnoreCase(e.getBatteryType(), userCurrentBatteryType)).collect(Collectors.toList());
                    
                    if (CollectionUtils.isEmpty(userCurrentBatteryUsableBatteryCellNos)) {
                        //获取用户绑定的型号
                        List<String> userBatteryTypes = userBatteryTypeService.selectByUid(uid);
                        if (CollectionUtils.isEmpty(userBatteryTypes)) {
                            log.error("ELE ERROR!not found use binding battery type,uid={}", uid);
                            return Triple.of(false, "100352", "未找到用户电池型号");
                        }
                        
                        usableBatteryCellNos = usableBatteryCellNos.stream()
                                .filter(e -> StringUtils.isNotBlank(e.getBatteryType()) && userBatteryTypes.contains(e.getBatteryType())).collect(Collectors.toList());
                    } else {
                        usableBatteryCellNos = userCurrentBatteryUsableBatteryCellNos;
                    }
                } else {
                    //获取用户绑定的型号
                    List<String> userBatteryTypes = userBatteryTypeService.selectByUid(uid);
                    if (CollectionUtils.isEmpty(userBatteryTypes)) {
                        log.error("ELE ERROR!not found use binding battery type,uid={}", uid);
                        return Triple.of(false, "100352", "未找到用户电池型号");
                    }
                    
                    usableBatteryCellNos = usableBatteryCellNos.stream().filter(e -> StringUtils.isNotBlank(e.getBatteryType()) && userBatteryTypes.contains(e.getBatteryType()))
                            .collect(Collectors.toList());
                }
            }
            
            usableBatteryCellNos = usableBatteryCellNos.stream().filter(item -> StringUtils.isNotBlank(item.getSn()) && Objects.nonNull(item.getPower()))
                    .sorted(Comparator.comparing(ElectricityCabinetBox::getPower).reversed()).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(usableBatteryCellNos)) {
                log.warn("EXCHANGE WARN!usableBatteryCellNos is empty,eid={}", eid);
                return Triple.of(false, "100216", "换电柜暂无满电电池");
            }
            
            Double maxPower = usableBatteryCellNos.get(0).getPower();
            usableBatteryCellNos = usableBatteryCellNos.stream().filter(item -> Objects.equals(item.getPower(), maxPower)).collect(Collectors.toList());
            if (usableBatteryCellNos.size() == 1) {
                return Triple.of(true, null, usableBatteryCellNos.get(0));
            }
            
            //如果存在多个电量相同的格挡，取充电器电压最大
            ElectricityCabinetBox usableCabinetBox = usableBatteryCellNos.stream().filter(item -> Objects.nonNull(item.getChargeV()))
                    .sorted(Comparator.comparing(ElectricityCabinetBox::getChargeV)).reduce((first, second) -> second).orElse(null);
            if (Objects.isNull(usableCabinetBox)) {
                log.warn("EXCHANGE WARN!nou found full battery,eid={}", eid);
                return Triple.of(false, "100216", "换电柜暂无满电电池");
            }
            
            return Triple.of(true, null, usableCabinetBox);
        }
    }
    
    /**
     * 换电柜2.0
     */
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
                return Triple.of(false, "100223", "换电柜没有标准型号电池");
            }
        }
        
        List<Long> batteryIds = usableBatteryCellNos.stream().map(ElectricityCabinetBox::getBId).collect(Collectors.toList());
        
        List<ElectricityBattery> electricityBatteries = electricityBatteryService.selectByBatteryIds(batteryIds);
        if (CollectionUtils.isEmpty(electricityBatteries)) {
            return Triple.of(false, "100225", "电池不存在");
        }
        
        //把本柜机加盟商的绑定电池信息拿出来
        electricityBatteries = electricityBatteries.stream().filter(e -> Objects.equals(e.getFranchiseeId(), franchiseeId)).collect(Collectors.toList());
        if (!DataUtil.collectionIsUsable(electricityBatteries)) {
            return Triple.of(false, "100219", "电池没有绑定加盟商,无法换电，请联系客服在后台绑定");
        }
        
        //获取全部可用电池id
        List<Long> bindingBatteryIds = electricityBatteries.stream().map(ElectricityBattery::getId).collect(Collectors.toList());
        //把加盟商绑定的电池过滤出来
        usableBatteryCellNos = usableBatteryCellNos.stream().filter(e -> bindingBatteryIds.contains(e.getBId())).collect(Collectors.toList());
        
        //查最大电量是否有多个格挡，如果有取最大充电器电压
        final Double MAX_POWER = usableBatteryCellNos.get(0).getPower();
        usableBatteryCellNos = usableBatteryCellNos.stream().filter(item -> Objects.equals(item.getPower(), MAX_POWER)).collect(Collectors.toList());
        
        int maxChargeVIndex = 0;
        for (int i = 0; i < usableBatteryCellNos.size(); i++) {
            Double maxChargeV = Optional.ofNullable(usableBatteryCellNos.get(maxChargeVIndex).getChargeV()).orElse(0.0);
            Double chargeV = Optional.ofNullable(usableBatteryCellNos.get(i).getChargeV()).orElse(0.0);
            
            if (maxChargeV.compareTo(chargeV) < 0) {
                maxChargeVIndex = i;
            }
        }
        return Triple.of(true, null, usableBatteryCellNos.get(maxChargeVIndex));
    }
    
    @Override
    public Pair<Boolean, Integer> findUsableEmptyCellNo(Integer eid) {
        List<FreeCellNoQuery> electricityCabinetBoxes = electricityCabinetBoxService.findUsableEmptyCellNo(eid);
        if (!DataUtil.collectionIsUsable(electricityCabinetBoxes)) {
            return Pair.of(false, null);
        }
        
        //本次分配的格挡
        Integer allocationCellNo = null;
        
        try {
            //可用格挡只有一个默认直接分配
            if (electricityCabinetBoxes.size() == 1) {
                allocationCellNo = Integer.valueOf(electricityCabinetBoxes.get(0).getCellNo());
                return Pair.of(true, allocationCellNo);
            }
            
            //可使用格挡
            List<Integer> usableEmptyCellNos = electricityCabinetBoxes.parallelStream().map(FreeCellNoQuery::getCellNo).map(Integer::valueOf).collect(Collectors.toList());
            //上次分配格档
            String cacheDistributionCell = redisService.get(CacheConstant.CACHE_DISTRIBUTION_CELL + eid);
            Integer occupyCellNo = null;
            if (StrUtil.isNotBlank(cacheDistributionCell)) {
                occupyCellNo = Integer.valueOf(cacheDistributionCell);
            }
            //可分配（可使用 - 已分配）格挡
            List<Integer> distributableEmptyCellNos = new ArrayList<>(usableEmptyCellNos);
            distributableEmptyCellNos.remove(occupyCellNo);
            
            //可分配格挡只有一个默认直接分配
            if (distributableEmptyCellNos.size() == 1) {
                allocationCellNo = distributableEmptyCellNos.get(0);
                return Pair.of(true, allocationCellNo);
            }
            
            //分配上一次取出的格挡
            allocationCellNo = preTakeCellAllocation(distributableEmptyCellNos, eid);
            if (Objects.nonNull(allocationCellNo)) {
                return Pair.of(true, allocationCellNo);
            }
            
            //分配空闲时间最大的格挡
            allocationCellNo = freeTimeMaxCellAllocation(electricityCabinetBoxes, distributableEmptyCellNos);
            if (Objects.nonNull(allocationCellNo)) {
                return Pair.of(true, allocationCellNo);
            }
            
            //随机分配格挡,
            allocationCellNo = distributableEmptyCellNos.get(ThreadLocalRandom.current().nextInt(electricityCabinetBoxes.size()));
            //随机分配格挡distributableEmptyCellNos不会为空，严谨加上判空
            if (Objects.nonNull(allocationCellNo)) {
                return Pair.of(true, allocationCellNo);
            }
            
            return Pair.of(false, null);
        } finally {
            //只记录本次分配过得格挡，
            //假设这次分配出去的是空闲时间最大格挡，说明被取走的格挡不会被分配，
            //下次将空闲时间最大格挡在可分配格挡删除，则会走到随机分配
            if (Objects.nonNull(allocationCellNo)) {
                redisService.set(CacheConstant.CACHE_DISTRIBUTION_CELL + eid, String.valueOf(allocationCellNo), 3L, TimeUnit.MINUTES);
            }
        }
    }
    
    private Integer freeTimeMaxCellAllocation(List<FreeCellNoQuery> electricityCabinetBoxes, List<Integer> distributableEmptyCellNos) {
        List<FreeCellNoQuery> freeTimeCells = electricityCabinetBoxes.parallelStream().filter(item -> Objects.nonNull(item.getEmptyGridStartTime()))
                .filter(item -> distributableEmptyCellNos.contains(Integer.valueOf(item.getCellNo()))).sorted(Comparator.comparing(FreeCellNoQuery::getEmptyGridStartTime))
                .collect(Collectors.toList());
        //如果空闲格挡为空或格挡为空
        if (CollectionUtils.isEmpty(freeTimeCells) || StrUtil.isBlank(freeTimeCells.get(0).getCellNo())) {
            return null;
        }
        
        Integer cellNo = Integer.valueOf(freeTimeCells.get(0).getCellNo());
        
        //可分配格挡中不存在
        if (!distributableEmptyCellNos.contains(cellNo)) {
            return null;
        }
        return cellNo;
    }
    
    private Integer preTakeCellAllocation(List<Integer> distributableEmptyCellNos, Integer eid) {
        String preTakeCell = redisService.get(CacheConstant.CACHE_PRE_TAKE_CELL + eid);
        //上一次取出格挡不存在
        if (StrUtil.isBlank(preTakeCell)) {
            return null;
        }
        
        //可分配格挡中不存在
        Integer cellNo = Integer.valueOf(preTakeCell);
        if (!distributableEmptyCellNos.contains(cellNo)) {
            return null;
        }
        
        return cellNo;
    }
    
    @Override
    public R getFranchisee(String productKey, String deviceName) {
        //换电柜
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.error("getFranchisee  ERROR! not found electricityCabinet,productKey={},deviceName={}", productKey, deviceName);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        //查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            log.error("getFranchisee  ERROR! not found store,electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.error("getFranchisee  ERROR! not found store,storeId={}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        
        //查找门店加盟商
        if (Objects.isNull(store.getFranchiseeId())) {
            log.error("getFranchisee  ERROR! not found Franchisee,storeId={}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromDB(store.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("getFranchisee  ERROR! not found Franchisee ！franchiseeId{}", store.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        
        return R.ok(franchisee);
    }
    
    @Slave
    @Override
    public Integer querySumCount(ElectricityCabinetQuery electricityCabinetQuery) {
        return electricityCabinetMapper.queryCount(electricityCabinetQuery);
    }
    
    @Slave
    @Override
    public Integer queryCountByStoreIds(Integer tenantId, List<Long> storeIds) {
        return electricityCabinetMapper.queryCountByStoreIds(tenantId, storeIds);
    }
    
    @Slave
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
    
    @Slave
    @Override
    public R queryAllElectricityCabinet(ElectricityCabinetQuery electricityCabinetQuery) {
        return R.ok(electricityCabinetMapper.queryList(electricityCabinetQuery));
    }
    
    @Override
    public List<ElectricityCabinet> selectBystoreIds(List<Long> storeIds) {
        return electricityCabinetMapper.selectList(
                new LambdaQueryWrapper<ElectricityCabinet>().in(ElectricityCabinet::getStoreId, storeIds).eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
    }
    
    @Override
    public List<ElectricityCabinet> selectByFranchiseeIds(List<Long> franchiseeIds) {
        return electricityCabinetMapper.selectList(new LambdaQueryWrapper<ElectricityCabinet>().in(ElectricityCabinet::getFranchiseeId, franchiseeIds)
                .eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
    }
    
    private void checkCupboardStatusAndUpdateDiff(boolean isOnline, ElectricityCabinet electricityCabinet) {
        if ((!isOnline && isCupboardAttrIsOnline(electricityCabinet)) || (isOnline && !isCupboardAttrIsOnline(electricityCabinet))) {
            ElectricityCabinet update = new ElectricityCabinet();
            update.setId(electricityCabinet.getId());
            update.setOnlineStatus(isOnline ? ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS : ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
            update.setUpdateTime(System.currentTimeMillis());
            idempotentUpdateCupboard(electricityCabinet, update);
        }
    }
    
    private boolean isCupboardAttrIsOnline(ElectricityCabinet electricityCabinet) {
        return Objects.equals(ElectricityCabinet.STATUS_ONLINE, electricityCabinet.getOnlineStatus());
        //        return ElectricityCabinet.IOT_STATUS_ONLINE.equalsIgnoreCase(electricityCabinet.getOnlineStatus().toString());
    }
    
    @Override
    public int idempotentUpdateCupboard(ElectricityCabinet electricityCabinet, ElectricityCabinet updateElectricityCabinet) {
        Integer update = update(updateElectricityCabinet);
        if (update > 0) {
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId());
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
        }
        return update;
    }
    
    private Double checkLowBatteryExchangeMinimumBatteryPowerStandard(Integer tenantId, Integer electricityCabinetId) {
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        
        Double fullyCharged = electricityCabinet.getFullyCharged();
        
        if (Objects.isNull(fullyCharged) || Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsLowBatteryExchange(),
                ElectricityConfig.NOT_LOW_BATTERY_EXCHANGE)) {
            return fullyCharged;
        }
        List<LowBatteryExchangeModel> list = JsonUtil.fromJsonArray(electricityConfig.getLowBatteryExchangeModel(), LowBatteryExchangeModel.class);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmmss");
        Long now = System.currentTimeMillis();
        for (LowBatteryExchangeModel lowBatteryExchangeModel : list) {
            if (Integer.parseInt(simpleDateFormat.format(now)) > Integer.parseInt(simpleDateFormat.format(lowBatteryExchangeModel.getExchangeBeginTime()))
                    && Integer.parseInt(simpleDateFormat.format(now)) < Integer.parseInt(simpleDateFormat.format(lowBatteryExchangeModel.getExchangeEndTime())) && Objects.nonNull(
                    lowBatteryExchangeModel.getBatteryPowerStandard()) && lowBatteryExchangeModel.getBatteryPowerStandard() < fullyCharged) {
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
            if (Objects.nonNull(electricityBatteries) && Integer.parseInt(simpleDateFormat.format(now)) > Integer.parseInt(
                    simpleDateFormat.format(lowBatteryExchangeModel.getExchangeBeginTime())) && Integer.parseInt(simpleDateFormat.format(now)) < Integer.parseInt(
                    simpleDateFormat.format(lowBatteryExchangeModel.getExchangeEndTime()))) {
                for (ElectricityBattery electricityBattery : electricityBatteries) {
                    //电池所在仓门非禁用
                    ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryBySn(electricityBattery.getSn(), electricityCabinetId);
                    if (Objects.nonNull(electricityCabinetBox)) {
                        if (Objects.nonNull(electricityBattery.getPower()) && Objects.nonNull(lowBatteryExchangeModel.getBatteryPowerStandard())
                                && electricityBattery.getPower() > lowBatteryExchangeModel.getBatteryPowerStandard()) {
                            ElectricityBattery battery = electricityBatteryService.selectByBatteryIdAndFranchiseeId(electricityBattery.getId(), franchiseeId);
                            if (Objects.nonNull(battery)) {
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
        
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryAllBoxByElectricityCabinetId(electricityCabinetId);
        if (!CollectionUtils.isEmpty(electricityCabinetBoxList)) {
            List<ElectricityCabinetBoxVO> electricityCabinetBoxVOList = Lists.newArrayList();
            
            electricityCabinetBoxList.forEach(item -> {
                ElectricityCabinetBoxVO electricityCabinetBoxVO = new ElectricityCabinetBoxVO();
                BeanUtils.copyProperties(item, electricityCabinetBoxVO);
                
                ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(item.getSn());
                if (!Objects.isNull(electricityBattery)) {
                    electricityCabinetBoxVO.setPower(electricityBattery.getPower());
                    electricityCabinetBoxVO.setChargeStatus(electricityBattery.getChargeStatus());
                    electricityCabinetBoxVO.setExchange(electricityBattery.getPower() >= fullyCharged ? ElectricityCabinetBoxVO.EXCHANGE_YES : ElectricityCabinetBoxVO.EXCHANGE_NO);
                    if (StringUtils.isNotBlank(electricityBattery.getSn())) {
                        electricityCabinetBoxVO.setBatteryShortType(batteryModelService.analysisBatteryTypeByBatteryName(electricityBattery.getSn()));
                    }
                }
                
                //如果电池类型为空,则返回null
                if (StringUtils.isBlank(electricityCabinetBoxVO.getBatteryType())) {
                    electricityCabinetBoxVO.setBatteryType(null);
                }
                
                if (StringUtils.isNotBlank(item.getBatteryType())) {
                    String batteryType = item.getBatteryType();
                    //设置电池短型号
                    electricityCabinetBoxVO.setBatteryModelShortType(subStringButteryType(batteryType));
                    //设置电池电压 容量
                    StringBuilder voltageAndCapacity = new StringBuilder();
                    String batteryV = batteryType.substring(batteryType.indexOf("_") + 1).substring(0, batteryType.substring(batteryType.indexOf("_") + 1).indexOf("_"));
                    voltageAndCapacity.append(batteryV);
                    if (Objects.nonNull(electricityBattery) && Objects.nonNull(electricityBattery.getCapacity()) && !Objects.equals(NumberConstant.ZERO,
                            electricityBattery.getCapacity())) {
                        voltageAndCapacity.append(StringConstant.FORWARD_SLASH).append(electricityBattery.getCapacity()).append(BatteryConstant.CAPACITY_UNIT);
                    }
                    electricityCabinetBoxVO.setBatteryVoltageAndCapacity(voltageAndCapacity.toString());
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
        Integer tenantId = TenantContextHolder.getTenantId();
        
        HomePageTurnOverVo homePageTurnOverVo = new HomePageTurnOverVo();
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(homePageTurnOverVo);
            }
        }
        
        long todayStartTime = DateUtils.getTodayStartTime();
        //购买换电月卡
        List<Long> finalFranchiseeIds = franchiseeIds;
        CompletableFuture<Void> batteryMemberCard = CompletableFuture.runAsync(() -> {
            BigDecimal batteryMemberCardTurnover = electricityMemberCardOrderService.queryBatteryMemberCardTurnOver(tenantId, null, finalFranchiseeIds);
            BigDecimal todayBatteryMemberCardTurnover = electricityMemberCardOrderService.queryBatteryMemberCardTurnOver(tenantId, todayStartTime, finalFranchiseeIds);
            homePageTurnOverVo.setBatteryMemberCardTurnover(batteryMemberCardTurnover);
            homePageTurnOverVo.setTodayBatteryMemberCardTurnover(todayBatteryMemberCardTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        //租车套餐
        CompletableFuture<Void> carMemberCard = CompletableFuture.runAsync(() -> {
            BigDecimal carMemberCardTurnover = carMemberCardOrderService.queryCarMemberCardTurnOver(tenantId, null, finalFranchiseeIds);
            BigDecimal todayCarMemberCardTurnover = carMemberCardOrderService.queryCarMemberCardTurnOver(tenantId, todayStartTime, finalFranchiseeIds);
            homePageTurnOverVo.setCarMemberCardTurnover(carMemberCardTurnover);
            homePageTurnOverVo.setTodayCarMemberCardTurnover(todayCarMemberCardTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query CAR TenantTurnOver error!", e);
            return null;
        });
        
        //电池服务费
        CompletableFuture<Void> batteryServiceFee = CompletableFuture.runAsync(() -> {
            BigDecimal batteryServiceFeeTurnover = eleBatteryServiceFeeOrderService.queryTurnOver(tenantId, null, finalFranchiseeIds);
            BigDecimal todayBatteryServiceFeeTurnover = eleBatteryServiceFeeOrderService.queryTurnOver(tenantId, todayStartTime, finalFranchiseeIds);
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
            homePageTurnOverVo.setSumTurnover(
                    homePageTurnOverVo.getBatteryMemberCardTurnover().add(homePageTurnOverVo.getBatteryServiceFeeTurnover()).add(homePageTurnOverVo.getCarMemberCardTurnover()));
            homePageTurnOverVo.setTodayTurnover(homePageTurnOverVo.getTodayBatteryMemberCardTurnover().add(homePageTurnOverVo.getTodayBatteryServiceFeeTurnover())
                    .add(homePageTurnOverVo.getTodayCarMemberCardTurnover()));
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
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        HomePageDepositQuery qeury = new HomePageDepositQuery();
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(qeury);
            }
        }
        
        long todayStartTime = DateUtils.getTodayStartTime();
        
        //缴纳电池押金
        List<Long> finalFranchiseeIds = franchiseeIds;
        CompletableFuture<Void> batteryDeposit = CompletableFuture.runAsync(() -> {
            BigDecimal onlineBatteryDepositTurnover = eleDepositOrderService.queryDepositTurnOverByDepositType(tenantId, null, EleDepositOrder.ELECTRICITY_DEPOSIT,
                    finalFranchiseeIds, EleDepositOrder.ONLINE_DEPOSIT_PAYMENT);
            BigDecimal offlineBatteryDepositTurnover = eleDepositOrderService.queryDepositTurnOverByDepositType(tenantId, null, EleDepositOrder.ELECTRICITY_DEPOSIT,
                    finalFranchiseeIds, EleDepositOrder.OFFLINE_DEPOSIT_PAYMENT);
            BigDecimal freeBatteryDepositTurnover = eleDepositOrderService.queryDepositTurnOverByDepositType(tenantId, null, EleDepositOrder.ELECTRICITY_DEPOSIT,
                    finalFranchiseeIds, EleDepositOrder.FREE_DEPOSIT_PAYMENT);
            
            qeury.setOnlineBatteryDeposit(onlineBatteryDepositTurnover);
            qeury.setOfflineBatteryDeposit(offlineBatteryDepositTurnover);
            qeury.setFreeBatteryDeposit(freeBatteryDepositTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        //今日电池押金
        CompletableFuture<Void> batteryDepositToDay = CompletableFuture.runAsync(() -> {
            BigDecimal todayOnlineBatteryDeposit = eleDepositOrderService.queryDepositTurnOverByDepositType(tenantId, todayStartTime, EleDepositOrder.ELECTRICITY_DEPOSIT,
                    finalFranchiseeIds, EleDepositOrder.ONLINE_DEPOSIT_PAYMENT);
            BigDecimal todayOfflineBatteryDeposit = eleDepositOrderService.queryDepositTurnOverByDepositType(tenantId, todayStartTime, EleDepositOrder.ELECTRICITY_DEPOSIT,
                    finalFranchiseeIds, EleDepositOrder.OFFLINE_DEPOSIT_PAYMENT);
            BigDecimal todayFreeBatteryDeposit = eleDepositOrderService.queryDepositTurnOverByDepositType(tenantId, todayStartTime, EleDepositOrder.ELECTRICITY_DEPOSIT,
                    finalFranchiseeIds, EleDepositOrder.FREE_DEPOSIT_PAYMENT);
            
            qeury.setTodayOfflineBatteryDeposit(todayOfflineBatteryDeposit);
            qeury.setTodayOnlineBatteryDeposit(todayOnlineBatteryDeposit);
            qeury.setTodayFreeBatteryDeposit(todayFreeBatteryDeposit);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        //租车押金
        CompletableFuture<Void> carDeposit = CompletableFuture.runAsync(() -> {
            BigDecimal onlineCarDepositTurnover = carDepositOrderService.queryDepositTurnOverByDepositType(tenantId, null, EleDepositOrder.RENT_CAR_DEPOSIT, finalFranchiseeIds,
                    CarDepositOrder.ONLINE_PAYTYPE);
            BigDecimal offlineCarDepositTurnover = carDepositOrderService.queryDepositTurnOverByDepositType(tenantId, null, EleDepositOrder.RENT_CAR_DEPOSIT, finalFranchiseeIds,
                    CarDepositOrder.OFFLINE_PAYTYPE);
            BigDecimal freeCarDepositTurnover = carDepositOrderService.queryDepositTurnOverByDepositType(tenantId, null, EleDepositOrder.RENT_CAR_DEPOSIT, finalFranchiseeIds,
                    CarDepositOrder.FREE_DEPOSIT_PAYTYPE);
            //            BigDecimal todayCarDeposit = carDepositOrderService
            //                    .queryDepositTurnOverByDepositType(tenantId, todayStartTime, EleDepositOrder.RENT_CAR_DEPOSIT,
            //                            finalFranchiseeIds);
            //            homePageDepositVo.setCarDeposit(carDepositTurnover);
            //            homePageDepositVo.setTodayCarDeposit(todayCarDeposit);
            
            qeury.setOnlineCarDeposit(onlineCarDepositTurnover);
            qeury.setOfflineCarDeposit(offlineCarDepositTurnover);
            qeury.setFreeCarDeposit(freeCarDepositTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        //今日租车押金
        CompletableFuture<Void> carDepositToDay = CompletableFuture.runAsync(() -> {
            BigDecimal todayOnlineCarDeposit = carDepositOrderService.queryDepositTurnOverByDepositType(tenantId, todayStartTime, EleDepositOrder.RENT_CAR_DEPOSIT,
                    finalFranchiseeIds, CarDepositOrder.ONLINE_PAYTYPE);
            BigDecimal todayOfflineCarDeposit = carDepositOrderService.queryDepositTurnOverByDepositType(tenantId, todayStartTime, EleDepositOrder.RENT_CAR_DEPOSIT,
                    finalFranchiseeIds, CarDepositOrder.OFFLINE_PAYTYPE);
            BigDecimal todayFreeCarDeposit = carDepositOrderService.queryDepositTurnOverByDepositType(tenantId, todayStartTime, EleDepositOrder.RENT_CAR_DEPOSIT,
                    finalFranchiseeIds, CarDepositOrder.FREE_DEPOSIT_PAYTYPE);
            
            qeury.setTodayOnlineCarDeposit(todayOnlineCarDeposit);
            qeury.setTodayOfflineCarDeposit(todayOfflineCarDeposit);
            qeury.setTodayFreeCarDeposit(todayFreeCarDeposit);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        //代扣记录
        CompletableFuture<Void> depositFreeAlipay = CompletableFuture.runAsync(() -> {
            BigDecimal eleFreeDepositAlipay = eleDepositOrderService.queryFreeDepositAlipayTurnOver(tenantId, null, EleDepositOrder.ELECTRICITY_DEPOSIT, finalFranchiseeIds);
            BigDecimal carFreeDepositAlipay = carDepositOrderService.queryFreeDepositAlipayTurnOver(tenantId, null, EleDepositOrder.RENT_CAR_DEPOSIT, finalFranchiseeIds);
            
            qeury.setBatteryFreeDepositAlipay(eleFreeDepositAlipay);
            qeury.setCarFreeDepositAlipay(carFreeDepositAlipay);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query depositFreeAlipay error!", e);
            return null;
        });
        
        //        //退电池押金
        //        CompletableFuture<Void> refundBatteryDeposit = CompletableFuture.runAsync(() -> {
        //            BigDecimal todayRefundDeposit = refundOrderService
        //                    .queryTurnOverByTime(tenantId, todayStartTime, null, finalFranchiseeIds);
        //            BigDecimal historyRefundDeposit = refundOrderService
        //                    .queryTurnOverByTime(tenantId, null, EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER,
        //                            finalFranchiseeIds);
        //            homePageDepositVo.setTodayRefundDeposit(todayRefundDeposit);
        //            homePageDepositVo.setHistoryRefundBatteryDeposit(historyRefundDeposit);
        //        }, executorService).exceptionally(e -> {
        //            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
        //            return null;
        //        });
        
        //今日退电池押金
        CompletableFuture<Void> refundBatteryDeposit = CompletableFuture.runAsync(() -> {
            BigDecimal todayOnlineRefundDeposit = refundOrderService.queryTurnOverByTime(tenantId, todayStartTime, EleDepositOrder.ELECTRICITY_DEPOSIT, finalFranchiseeIds,
                    EleDepositOrder.ONLINE_DEPOSIT_PAYMENT);
            BigDecimal todayOfflineRefundDeposit = refundOrderService.queryTurnOverByTime(tenantId, todayStartTime, EleDepositOrder.ELECTRICITY_DEPOSIT, finalFranchiseeIds,
                    EleDepositOrder.OFFLINE_DEPOSIT_PAYMENT);
            BigDecimal todayFreeRefundDeposit = refundOrderService.queryTurnOverByTime(tenantId, todayStartTime, EleDepositOrder.ELECTRICITY_DEPOSIT, finalFranchiseeIds,
                    EleDepositOrder.FREE_DEPOSIT_PAYMENT);
            
            qeury.setTodayOnlineRefundDeposit(todayOnlineRefundDeposit);
            qeury.setTodayOfflineRefundDeposit(todayOfflineRefundDeposit);
            qeury.setTodayFreeRefundDeposit(todayFreeRefundDeposit);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query refundBatteryDeposit error!", e);
            return null;
        });
        
        //历史退电池押金
        CompletableFuture<Void> refundBatteryDepositHistory = CompletableFuture.runAsync(() -> {
            BigDecimal historyOnlineRefundDeposit = refundOrderService.queryTurnOverByTime(tenantId, null, EleDepositOrder.ELECTRICITY_DEPOSIT, finalFranchiseeIds,
                    EleDepositOrder.ONLINE_DEPOSIT_PAYMENT);
            BigDecimal historyOfflineRefundDeposit = refundOrderService.queryTurnOverByTime(tenantId, null, EleDepositOrder.ELECTRICITY_DEPOSIT, finalFranchiseeIds,
                    EleDepositOrder.OFFLINE_DEPOSIT_PAYMENT);
            BigDecimal historyFreeRefundDeposit = refundOrderService.queryTurnOverByTime(tenantId, null, EleDepositOrder.ELECTRICITY_DEPOSIT, finalFranchiseeIds,
                    EleDepositOrder.FREE_DEPOSIT_PAYMENT);
            
            qeury.setHistoryOnlineRefundBatteryDeposit(historyOnlineRefundDeposit);
            qeury.setHistoryOfflineRefundBatteryDeposit(historyOfflineRefundDeposit);
            qeury.setHistoryFreeRefundBatteryDeposit(historyFreeRefundDeposit);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query refundBatteryDepositHistory error!", e);
            return null;
        });
        
        //今日退租车押金
        CompletableFuture<Void> refundCarDeposit = CompletableFuture.runAsync(() -> {
            BigDecimal todayOnlineRefundDeposit = refundOrderService.queryCarRefundTurnOverByTime(tenantId, todayStartTime, EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER,
                    finalFranchiseeIds, EleDepositOrder.ONLINE_DEPOSIT_PAYMENT);
            BigDecimal todayOfflineRefundDeposit = refundOrderService.queryCarRefundTurnOverByTime(tenantId, todayStartTime, EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER,
                    finalFranchiseeIds, EleDepositOrder.OFFLINE_DEPOSIT_PAYMENT);
            BigDecimal todayFreeRefundDeposit = refundOrderService.queryCarRefundTurnOverByTime(tenantId, todayStartTime, EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER,
                    finalFranchiseeIds, EleDepositOrder.FREE_DEPOSIT_PAYMENT);
            
            qeury.setTodayOnlineCarRefundDeposit(todayOnlineRefundDeposit);
            qeury.setTodayOfflineCarRefundDeposit(todayOfflineRefundDeposit);
            qeury.setTodayFreeCarRefundDeposit(todayFreeRefundDeposit);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query refundCarDeposit error!", e);
            return null;
        });
        
        //历史退租车押金
        CompletableFuture<Void> refundCarDepositHistory = CompletableFuture.runAsync(() -> {
            BigDecimal historyOnlineRefundDeposit = refundOrderService.queryCarRefundTurnOverByTime(tenantId, null, EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER,
                    finalFranchiseeIds, EleDepositOrder.ONLINE_DEPOSIT_PAYMENT);
            BigDecimal historyOfflineRefundDeposit = refundOrderService.queryCarRefundTurnOverByTime(tenantId, null, EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER,
                    finalFranchiseeIds, EleDepositOrder.OFFLINE_DEPOSIT_PAYMENT);
            BigDecimal historyFreeRefundDeposit = refundOrderService.queryCarRefundTurnOverByTime(tenantId, null, EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER, finalFranchiseeIds,
                    EleDepositOrder.FREE_DEPOSIT_PAYMENT);
            
            qeury.setHistoryOnlineRefundCarDeposit(historyOnlineRefundDeposit);
            qeury.setHistoryOfflineRefundCarDeposit(historyOfflineRefundDeposit);
            qeury.setHistoryFreeRefundCarDeposit(historyFreeRefundDeposit);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query refundCarDepositHistory error!", e);
            return null;
        });
        
        //等待所有线程停止
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(batteryDeposit, carDeposit, refundBatteryDeposit, refundCarDeposit, batteryDepositToDay, carDepositToDay,
                refundBatteryDepositHistory, refundCarDepositHistory, depositFreeAlipay);
        HomePageDepositVo vo = new HomePageDepositVo();
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
            //            homePageDepositVo.setBatteryDeposit(
            //                    homePageDepositVo.getBatteryDeposit().subtract(homePageDepositVo.getHistoryRefundBatteryDeposit()));
            //            homePageDepositVo.setCarDeposit(
            //                    homePageDepositVo.getCarDeposit().subtract(homePageDepositVo.getHistoryRefundCarDeposit()));
            //            homePageDepositVo.setSumDepositTurnover(
            //                    homePageDepositVo.getBatteryDeposit().add(homePageDepositVo.getCarDeposit()));
            //            homePageDepositVo.setTodayPayDeposit(
            //                    homePageDepositVo.getTodayBatteryDeposit().add(homePageDepositVo.getTodayCarDeposit()));
            
            BigDecimal payBatteryDeposit = qeury.getOnlineBatteryDeposit().add(qeury.getOfflineBatteryDeposit()).subtract(qeury.getHistoryOnlineRefundBatteryDeposit())
                    .subtract(qeury.getHistoryOfflineRefundBatteryDeposit());
            BigDecimal freeBatteryDeposit = qeury.getFreeBatteryDeposit().subtract(qeury.getHistoryFreeRefundBatteryDeposit()).subtract(qeury.getBatteryFreeDepositAlipay());
            BigDecimal batteryDepositSum = payBatteryDeposit.add(freeBatteryDeposit);
            BigDecimal payCarDeposit = qeury.getOnlineCarDeposit().add(qeury.getOfflineCarDeposit()).subtract(qeury.getHistoryOnlineRefundCarDeposit())
                    .subtract(qeury.getHistoryOfflineRefundCarDeposit());
            BigDecimal freeCarDeposit = qeury.getFreeCarDeposit().subtract(qeury.getHistoryFreeRefundCarDeposit()).subtract(qeury.getCarFreeDepositAlipay());
            BigDecimal carDepositSum = payCarDeposit.add(freeCarDeposit);
            BigDecimal todayPayRefundDeposit = qeury.getTodayOnlineRefundDeposit().add(qeury.getTodayOfflineBatteryDeposit()).add(qeury.getTodayOnlineCarRefundDeposit())
                    .add(qeury.getTodayOfflineCarRefundDeposit());
            BigDecimal todayFreeRefundDeposit = qeury.getTodayFreeRefundDeposit().add(qeury.getTodayFreeCarRefundDeposit());
            BigDecimal todayRefundDeposit = todayPayRefundDeposit.add(todayFreeRefundDeposit);
            BigDecimal todayPayDeposit = qeury.getTodayOnlineBatteryDeposit().add(qeury.getTodayOfflineBatteryDeposit()).add(qeury.getTodayOnlineCarDeposit())
                    .add(qeury.getTodayOfflineCarRefundDeposit());
            BigDecimal todayFreeDeposit = qeury.getTodayFreeBatteryDeposit().add(qeury.getTodayFreeCarDeposit());
            BigDecimal todayDeposit = todayPayDeposit.add(todayFreeDeposit);
            BigDecimal sumDepositTurnover = batteryDepositSum.add(carDepositSum);
            
            vo.setPayBatteryDeposit(payBatteryDeposit);
            vo.setFreeBatteryDeposit(freeBatteryDeposit);
            vo.setBatteryDeposit(batteryDepositSum);
            vo.setPayCarDeposit(payCarDeposit);
            vo.setFreeCarDeposit(freeCarDeposit);
            vo.setCarDeposit(carDepositSum);
            vo.setTodayPayRefundDeposit(todayPayRefundDeposit);
            vo.setTodayFreeRefundDeposit(todayFreeRefundDeposit);
            vo.setTodayRefundDeposit(todayRefundDeposit);
            vo.setTodayPayDeposit(todayPayDeposit);
            vo.setTodayFreeDeposit(todayFreeDeposit);
            vo.setTodayDeposit(todayDeposit);
            vo.setSumDepositTurnover(sumDepositTurnover);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }
        
        return R.ok(vo);
    }
    
    @Override
    public R homepageOverviewDetail() {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        HomepageOverviewDetailVo homepageOverviewDetailVo = new HomepageOverviewDetailVo();
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(homepageOverviewDetailVo);
            }
        }
        
        //实名认证用户
        CompletableFuture<Void> authenticationUser = CompletableFuture.runAsync(() -> {
            Integer authenticationUserCount = userInfoService.queryAuthenticationUserCount(tenantId);
            homepageOverviewDetailVo.setAuthenticationUserCount(authenticationUserCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query authenticationUser error!", e);
            return null;
        });
        
        //门店
        List<Long> finalFranchiseeIds = franchiseeIds;
        //查询所有门店
        List<Long> stores = null;
        if (!CollectionUtils.isEmpty(stores)) {
            stores = storeService.queryStoreIdByFranchiseeId(finalFranchiseeIds);
        }
        
        CompletableFuture<Void> store = CompletableFuture.runAsync(() -> {
            StoreQuery storeQuery = StoreQuery.builder().franchiseeIds(finalFranchiseeIds).tenantId(tenantId).build();
            Integer storeCount = storeService.queryCountForHomePage(storeQuery);
            homepageOverviewDetailVo.setStoreCount(storeCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        //柜机
        List<Long> finalStores = stores;
        CompletableFuture<Void> electricityCabinet = CompletableFuture.runAsync(() -> {
            Integer electricityCabinetCount = electricityCabinetService.queryCountByStoreIds(tenantId, finalStores);
            homepageOverviewDetailVo.setElectricityCabinetCount(electricityCabinetCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        //车辆
        CompletableFuture<Void> car = CompletableFuture.runAsync(() -> {
            Integer carCount = electricityCarService.queryCountByStoreIds(tenantId, finalStores);
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
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        HomePageTurnOverAnalysisVo homePageTurnOverAnalysisVo = new HomePageTurnOverAnalysisVo();
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(homePageTurnOverAnalysisVo);
            }
        }
        
        //购买换电月卡
        List<Long> finalFranchiseeIds = franchiseeIds;
        CompletableFuture<Void> batteryMemberCard = CompletableFuture.runAsync(() -> {
            List<HomePageTurnOverGroupByWeekDayVo> batteryMemberCardTurnover = electricityMemberCardOrderService.queryBatteryMemberCardTurnOverByCreateTime(tenantId,
                    finalFranchiseeIds, beginTime, endTime);
            homePageTurnOverAnalysisVo.setBatteryMemberCardAnalysis(batteryMemberCardTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        //购买租车套餐
        CompletableFuture<Void> carMemberCard = CompletableFuture.runAsync(() -> {
            List<HomePageTurnOverGroupByWeekDayVo> carMemberCardTurnover = carMemberCardOrderService.queryCarMemberCardTurnOverByCreateTime(tenantId, finalFranchiseeIds, beginTime,
                    endTime);
            homePageTurnOverAnalysisVo.setCarMemberCardAnalysis(carMemberCardTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        //电池服务费
        CompletableFuture<Void> batteryServiceFee = CompletableFuture.runAsync(() -> {
            List<HomePageTurnOverGroupByWeekDayVo> batteryServiceFeeTurnover = eleBatteryServiceFeeOrderService.queryTurnOverByCreateTime(tenantId, finalFranchiseeIds, beginTime,
                    endTime);
            homePageTurnOverAnalysisVo.setBatteryServiceFeeAnalysis(batteryServiceFeeTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        //电池押金
        CompletableFuture<Void> batteryDeposit = CompletableFuture.runAsync(() -> {
            List<HomePageTurnOverGroupByWeekDayVo> batteryDepositTurnover = eleDepositOrderService.queryDepositTurnOverAnalysisByDepositType(tenantId,
                    EleDepositOrder.ELECTRICITY_DEPOSIT, finalFranchiseeIds, beginTime, endTime);
            homePageTurnOverAnalysisVo.setBatteryDepositAnalysis(batteryDepositTurnover);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        //租车押金
        CompletableFuture<Void> carDeposit = CompletableFuture.runAsync(() -> {
            List<HomePageTurnOverGroupByWeekDayVo> carDepositTurnOver = carDepositOrderService.queryDepositTurnOverAnalysisByDepositType(tenantId, EleDepositOrder.RENT_CAR_DEPOSIT,
                    finalFranchiseeIds, beginTime, endTime);
            homePageTurnOverAnalysisVo.setCarDepositAnalysis(carDepositTurnOver);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        //总套餐营业额统计
        CompletableFuture<Void> sumMemberCard = CompletableFuture.runAsync(() -> {
            BigDecimal sumMemberCardTurnOver = electricityMemberCardOrderService.querySumMemberCardTurnOver(tenantId, finalFranchiseeIds, beginTime, endTime);
            BigDecimal sumBatteryService = eleBatteryServiceFeeOrderService.queryAllTurnOver(tenantId, finalFranchiseeIds, beginTime, endTime);
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
            BigDecimal sumDepositTurnOver = eleDepositOrderService.querySumDepositTurnOverAnalysis(tenantId, finalFranchiseeIds, beginTime, endTime);
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
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
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
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        HomePageElectricityOrderVo homePageElectricityOrderVo = new HomePageElectricityOrderVo();
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }
        
        List<Integer> eleIdList = null;
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(homePageElectricityOrderVo);
            }
            
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(homePageElectricityOrderVo);
            }
        }
        
        //换电成功订单数量统计
        List<Integer> finalEleIdList = eleIdList;
        List<Long> finalFranchiseeIds = franchiseeIds;
        //查询所有门店
        List<Long> stores = null;
        if (!CollectionUtils.isEmpty(finalFranchiseeIds)) {
            stores = storeService.queryStoreIdByFranchiseeId(finalFranchiseeIds);
        }
        
        //换电柜在线总数统计
        List<Long> finalStores = stores;
        CompletableFuture<Void> electricityOnlineCabinetCount = CompletableFuture.runAsync(() -> {
            Integer onLineCount = electricityCabinetService.queryCountByStoreIdsAndStatus(tenantId, finalStores, ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS);
            homePageElectricityOrderVo.setOnlineElectricityCabinet(onLineCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricityCabinetTurnOver error!", e);
            return null;
        });
        
        //换电柜离线总数统计
        CompletableFuture<Void> electricityOfflineCabinetCount = CompletableFuture.runAsync(() -> {
            Integer offLineCount = electricityCabinetService.queryCountByStoreIdsAndStatus(tenantId, finalStores, ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
            homePageElectricityOrderVo.setOfflineElectricityCabinet(offLineCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricityCabinetTurnOver error!", e);
            return null;
        });
        
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(electricityOnlineCabinetCount, electricityOfflineCabinetCount);
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
            List<HomepageElectricityExchangeFrequencyVo> homepageExchangeFrequency = electricityCabinetOrderService.homepageExchangeFrequency(
                    homepageElectricityExchangeFrequencyQuery);
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
            
            List<HomepageElectricityExchangeVo> result = homepageElectricityExchangeVos.stream()
                    .sorted(Comparator.comparing(HomepageElectricityExchangeVo::getExchangeFrequency).reversed()).collect(Collectors.toList());
            homepageElectricityExchangeFrequencyVo.setHomepageElectricityExchangeVos(result);
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
    
    @Slave
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
        List<ElectricityCabinetFile> electricityCabinetFiles = electricityCabinetFileService.queryByDeviceInfo(electricityCabinetId.longValue(),
                ElectricityCabinetFile.TYPE_ELECTRICITY_CABINET, storageConfig.getIsUseOSS());
        List<String> cabinetPhoto = new ArrayList<>();
        
        for (ElectricityCabinetFile electricityCabinetFile : electricityCabinetFiles) {
            if (StringUtils.isNotEmpty(electricityCabinetFile.getName())) {
                cabinetPhoto.add("https://" + storageConfig.getUrlPrefix() + "/" + electricityCabinetFile.getName());
            }
        }
        return R.ok(cabinetPhoto);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryElectricityCabinetBoxInfoByCabinetId(Integer electricityCabinetId) {
        // 判断用户信息
        Long uid = SecurityUtils.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        
        // 校验用户状态
        Triple<Boolean, String, Object> userStatus = verifyUserStatus(userInfo);
        if (Boolean.FALSE.equals(userStatus.getLeft())) {
            return userStatus;
        }
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        
        // 校验柜机状态
        Triple<Boolean, String, Object> electricityCabinetStatus = verifyElectricityCabinetStatus(electricityCabinet, userInfo);
        if (Boolean.FALSE.equals(electricityCabinetStatus.getLeft())) {
            return electricityCabinetStatus;
        }
        
        //校验套餐
        Triple<Boolean, String, Object> memberCardStatus = verficationMemberCardStatus(userInfo);
        if (Boolean.FALSE.equals(memberCardStatus.getLeft())) {
            return memberCardStatus;
        }
        
        //获取加盟商类型
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("QUERY SELECTION EXCHANGE WARN! not found franchisee,franchiseeId={},uid={}", electricityCabinet.getFranchiseeId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        //柜机加盟商与用户加盟商不一致
        if (!Objects.equals(franchisee.getId(), electricityCabinet.getFranchiseeId())) {
            log.warn("QUERY SELECTION EXCHANGE WARN! user franchiseeId  is not equal franchiseeId uid={} ,fid={}", userInfo.getUid(), userInfo.getFranchiseeId());
            return Triple.of(false, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
        }
        
        // 获取格挡信息
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryAllBoxByElectricityCabinetId(electricityCabinetId);
        if (CollectionUtils.isEmpty(electricityCabinetBoxList)) {
            log.warn("QUERY SELECTION EXCHANGE WARN! not found electricity boxes,electricityCabinetId={},uid={}", electricityCabinetId, userInfo.getUid());
            return Triple.of(false, "100552", "换电柜没有仓门，不能开门");
        }
        
        //查询用户的电池列表
        List<String> userBatteryTypeList = userBatteryTypeService.selectByUid(userInfo.getUid());
        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOList = Lists.newArrayList();
        
        electricityCabinetBoxList.forEach(item -> {
            ElectricityCabinetBoxVO electricityCabinetBoxVO = new ElectricityCabinetBoxVO();
            BeanUtils.copyProperties(item, electricityCabinetBoxVO);
            if (StringUtils.isNotBlank(item.getSn()) && !StringUtils.startsWithIgnoreCase(item.getSn(), "UNKNOW")) {
                //是否可换电
                electricityCabinetBoxVO.setExchange(isExchangeStatus(electricityCabinet, userBatteryTypeList, userInfo, electricityCabinetBoxVO, franchisee));
            }
            
            //设置充电状态
            ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(item.getSn());
            if (Objects.nonNull(electricityBattery)) {
                electricityCabinetBoxVO.setChargeStatus(electricityBattery.getChargeStatus());
            }
            
            if (StringUtils.isNotBlank(item.getBatteryType())) {
                String batteryType = item.getBatteryType();
                electricityCabinetBoxVO.setBatteryModelShortType(subStringButteryType(batteryType));
                String batteryV = batteryType.substring(batteryType.indexOf("_") + 1).substring(0, batteryType.substring(batteryType.indexOf("_") + 1).indexOf("_"));
                
                StringBuilder voltageAndCapacity = new StringBuilder();
                voltageAndCapacity.append(batteryV);
                //设置电池电压 容量
                if (Objects.nonNull(electricityBattery) && Objects.nonNull(electricityBattery.getCapacity()) && !Objects.equals(NumberConstant.ZERO,
                        electricityBattery.getCapacity())) {
                    voltageAndCapacity.append(StringConstant.FORWARD_SLASH).append(electricityBattery.getCapacity()).append(BatteryConstant.CAPACITY_UNIT);
                }
                electricityCabinetBoxVO.setBatteryVoltageAndCapacity(voltageAndCapacity.toString());
            }
            electricityCabinetBoxVOList.add(electricityCabinetBoxVO);
        });
        
        List<ElectricityCabinetBoxVO> resultList = Lists.newArrayList();
        
        //排序
        if (!CollectionUtils.isEmpty(electricityCabinetBoxVOList)) {
            resultList = electricityCabinetBoxVOList.stream().sorted(Comparator.comparing(item -> Integer.parseInt(item.getCellNo()))).collect(Collectors.toList());
        }
        return Triple.of(true, null, resultList);
        
    }
    
    private String subStringButteryType(String batteryType) {
        if (StringUtils.isBlank(batteryType)) {
            return null;
        }
        String batteryV = batteryType.substring(batteryType.indexOf("_") + 1).substring(0, batteryType.substring(batteryType.indexOf("_") + 1).indexOf("_"));
        //截取串数
        String num = batteryType.substring(batteryType.lastIndexOf("_") + 1);
        return batteryV + "/" + num;
    }
    
    private String subStringVoltageAndCapacity(String batteryType, Integer capacity) {
        StringBuilder voltageAndCapacity = new StringBuilder();
        if (StringUtils.isNotBlank(batteryType)) {
            String batteryV = batteryType.substring(batteryType.indexOf("_") + 1).substring(0, batteryType.substring(batteryType.indexOf("_") + 1).indexOf("_"));
            voltageAndCapacity.append(batteryV);
        }
        
        if (Objects.nonNull(capacity) && !Objects.equals(NumberConstant.ZERO, capacity)) {
            voltageAndCapacity.append(StringConstant.FORWARD_SLASH).append(capacity).append(BatteryConstant.CAPACITY_UNIT);
        }
        //截取串数
        return voltageAndCapacity.toString();
    }
    
    private Triple<Boolean, String, Object> verficationMemberCardStatus(UserInfo userInfo) {
        //校验用户套餐
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            Triple<Boolean, String, Object> userMemberCardStatus = verifyUserBatteryMemberCardStatus(userBatteryMemberCard, userInfo);
            if (Boolean.FALSE.equals(userMemberCardStatus.getLeft())) {
                return userMemberCardStatus;
            }
        } else if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            // 校验车电一体套餐
            Triple<Boolean, String, Object> carMemberCardStatus = verifyCarMemberCardStatus(userInfo);
            if (Boolean.FALSE.equals(carMemberCardStatus.getLeft())) {
                return carMemberCardStatus;
            }
        } else {
            log.warn("QUERY SELECTION EXCHANGE ERROR! not pay deposit,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }
        return Triple.of(true, null, null);
    }
    
    private Integer isExchangeStatus(ElectricityCabinet electricityCabinet, List<String> userBatteryTypeList, UserInfo userInfo, ElectricityCabinetBoxVO electricityCabinetBoxVO,
            Franchisee franchisee) {
        // 是否达到柜机的可换电标准
        if (electricityCabinetBoxVO.getPower() < electricityCabinet.getFullyCharged()) {
            log.warn("QUERY SELECTION EXCHANGE WARN!power={},fullyCharge={},cellNo={},uid={}", electricityCabinetBoxVO.getPower(), electricityCabinet.getFullyCharged(),
                    electricityCabinetBoxVO.getCellNo(), userInfo.getUid());
            return ElectricityCabinetBoxVO.EXCHANGE_NO;
        }
        
        // 过滤掉异常锁仓的
        if (Objects.equals(electricityCabinetBoxVO.getUsableStatus(), ElectricityCabinet.ELECTRICITY_CABINET_UN_USABLE_STATUS)) {
            log.warn("QUERY SELECTION EXCHANGE WARN!usableStaus={},cellNo={},uid={}", electricityCabinetBoxVO.getUsableStatus(), electricityCabinetBoxVO.getCellNo(),
                    userInfo.getUid());
            return ElectricityCabinetBoxVO.EXCHANGE_NO;
        }
        
        // 换电、车电一体套餐电池类型判断
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) && !userBatteryTypeList.contains(electricityCabinetBoxVO.getBatteryType())) {
            log.warn("QUERY SELECTION EXCHANGE WARN!batteryType={},cellNo={},uid={}", electricityCabinetBoxVO.getBatteryType(), electricityCabinetBoxVO.getCellNo(),
                    userInfo.getUid());
            return ElectricityCabinetBoxVO.EXCHANGE_NO;
        }
        return ElectricityCabinetBoxVO.EXCHANGE_YES;
    }
    
    private Triple<Boolean, String, Object> verifyUserStatus(UserInfo user) {
        if (Objects.isNull(user)) {
            log.warn("QUERY SELECTION EXCHANGE ERROR! not found user");
            return Triple.of(false, "100001", "未能找到用户");
        }
        //用户是否可用
        if (Objects.equals(user.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("QUERY SELECTION EXCHANGE ERROR! user is unUsable,uid={} ", user.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(user.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("QUERY SELECTION EXCHANGE ERROR! userinfo is UN AUTH! uid={}", user.getUid());
            return Triple.of(false, "100206", "用户未审核");
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> verifyElectricityCabinetStatus(ElectricityCabinet electricityCabinet, UserInfo userInfo) {
        if (Objects.isNull(electricityCabinet) || !Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("QUERY SELECTION EXCHANGE ERROR! not found electricityCabinet,electricityCabinetId={},userId={}", electricityCabinet.getId(), userInfo.getUid());
            return Triple.of(false, "100003", "柜机不存在");
        }
        
        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.warn("QUERY SELECTION EXCHANGE ERROR! electricityCabinet is offline,electricityCabinetId={},,userId={}", electricityCabinet.getId(), userInfo.getUid());
            return Triple.of(false, "100004", "柜机不在线");
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> verifyUserBatteryMemberCardStatus(UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo) {
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(),
                NumberConstant.ZERO_L)) {
            log.warn("QUERY SELECTION EXCHANGE ERROR! user haven't memberCard uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户未开通套餐");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("SELECTION EXCHAGE ORDER WARN! user's member card is stop! uid={}", userInfo.getUid());
            return Triple.of(false, "100211", "换电套餐停卡审核中");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("SELECTION EXCHAGE ORDER WARN! user's member card is stop! uid={}", userInfo.getUid());
            return Triple.of(false, "100211", "换电套餐已暂停");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("SELECTION EXCHAGE ORDER WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }
        
        //校验是否有退租审核中的订单
        BatteryMembercardRefundOrder batteryMembercardRefundOrder = batteryMembercardRefundOrderService.selectLatestByMembercardOrderNo(userBatteryMemberCard.getOrderId());
        if (Objects.nonNull(batteryMembercardRefundOrder) && Objects.equals(batteryMembercardRefundOrder.getStatus(), BatteryMembercardRefundOrder.STATUS_AUDIT)) {
            return Triple.of(false, "100282", "租金退款审核中，请等待审核确认后操作");
        }
        
        //判断用户电池服务费
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("SELECTION EXCHAGE ORDER WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.100000", "存在电池服务费");
        }
        
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)
                && userBatteryMemberCard.getRemainingNumber() <= 0)) {
            log.warn("SELECTION EXCHAGE ORDER WARN! battery memberCard is Expire,uid={}", userInfo.getUid());
            return Triple.of(false, "100555", "套餐已过期");
        }
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> verifyCarMemberCardStatus(UserInfo userInfo) {
        //判断车电一体套餐状态
        if (carRentalPackageMemberTermBizService.isExpirePackageOrder(userInfo.getTenantId(), userInfo.getUid())) {
            log.warn("QUERY SELECTION EXCHANGE ERROR! user memberCard disable,uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户套餐不可用");
        }
        
        //判断用户电池服务费
        if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid()))) {
            log.warn("ORDER WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "300001", "存在滞纳金，请先缴纳");
        }
        return Triple.of(true, null, null);
    }
    
    public R otaCommand(Integer eid, Integer operateType, Integer versionType, List<Integer> cellNos) {
        
        Long uid = SecurityUtils.getUid();
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        ElectricityCabinet electricityCabinet = queryByIdFromCache(eid);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        if (OtaConstant.OTA_TYPE_DOWNLOAD > operateType || OtaConstant.OTA_SIX_IN_ONE_TYPE_UPGRADE < operateType) {
            log.error("ELECTRICITY  ERROR!  ota  operate type illegal！electricityCabinet={},operateType={}", electricityCabinet, operateType);
            return R.fail("100302", "ota操作类型不合法");
        }
        
        // 查询柜机当前版本
        Integer versionPrefix = getVersionPrefix(eid);
        if (Objects.isNull(versionPrefix)) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is not version ！eid={}", eid);
            return R.fail("100312", "柜机暂无版本号，无法ota升级");
        }
        
        // 版本号前缀：旧版（大于等于50）、新版（小于10）、六合一版（大于等于10且小于20）
        String sessionPrefix = getSessionPrefix(operateType, versionType, versionPrefix);
        String sessionId = sessionPrefix + UUID.randomUUID().toString().replaceAll("-", "");
        
        Map<String, Object> data = Maps.newHashMap();
        data.put(OtaConstant.OTA_OPERATE_TYPE, operateType);
        data.put(OtaConstant.OTA_USERID, user.getUid());
        data.put(OtaConstant.OTA_USERNAME, user.getName());
        
        Triple<Boolean, String, Object> assembleContent = assembleContent(eid, operateType, cellNos, versionType, sessionId);
        if (Boolean.TRUE.equals(assembleContent.getLeft())) {
            data.put(OtaConstant.OTA_CONTENT, JsonUtil.toJson(assembleContent.getRight()));
        }
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(sessionId).data(data).productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.OTA_OPERATE).build();
        
        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        //发送命令失败
        if (!result.getLeft()) {
            return R.fail("ELECTRICITY.0037", "发送命令失败");
        }
        
        return R.ok(sessionId);
    }
    
    private String getSessionPrefix(Integer operateType, Integer versionType, Integer versionPrefix) {
        String sessionPrefix = null;
        // 下载时 versionType:1--旧的（版本号>=50.0） 2--新的（版本号<10） 3--六合一（10.0<=版本<20.0）
        // 同步和升级时 versionType=0
        if (OtaConstant.OTA_TYPE_DOWNLOAD.equals(operateType) || OtaConstant.OTA_SIX_IN_ONE_TYPE_DOWNLOAD.equals(operateType)) {
            switch (versionType) {
                case OtaConstant.OTA_VERSIONTYPE_OLD:
                    sessionPrefix = OtaConstant.SESSION_PREFIX_OLD;
                    break;
                case OtaConstant.OTA_VERSIONTYPE_NEW:
                    sessionPrefix = OtaConstant.SESSION_PREFIX_NEW;
                    break;
                case OtaConstant.OTA_VERSIONTYPE_SIX_IN_ONE:
                    sessionPrefix = OtaConstant.SESSION_PREFIX_SIX_IN_ONE;
                    break;
                default:
                    sessionPrefix = "";
                    break;
            }
        } else {
            if (OtaFileConfig.MIX_SIX_IN_ONE_BOARD_VERSION > versionPrefix) {
                sessionPrefix = OtaConstant.SESSION_PREFIX_NEW;
            } else if (OtaFileConfig.MIX_SIX_IN_ONE_BOARD_VERSION <= versionPrefix && versionPrefix < OtaFileConfig.MAX_SIX_IN_ONE_BOARD_VERSION) {
                sessionPrefix = OtaConstant.SESSION_PREFIX_SIX_IN_ONE;
            } else if (OtaFileConfig.MIN_OLD_BOARD_VERSION <= versionPrefix) {
                sessionPrefix = OtaConstant.SESSION_PREFIX_OLD;
            }
        }
        return sessionPrefix;
    }
    
    private Triple<Boolean, String, Object> assembleContent(Integer eid, Integer operateType, List<Integer> cellNos, Integer fileType, String sessionId) {
        Map<String, Object> content = new HashMap<>();
        
        if (OtaConstant.OTA_TYPE_DOWNLOAD.equals(operateType) || OtaConstant.OTA_SIX_IN_ONE_TYPE_DOWNLOAD.equals(operateType)) {
            OtaFileConfig coreBoardOtaFileConfig = null;
            OtaFileConfig subBoardOtaFileConfig = null;
            //ota文件是否存在
            if (Objects.equals(fileType, EleOtaFile.TYPE_OLD_FILE)) {
                coreBoardOtaFileConfig = otaFileConfigService.queryByType(OtaFileConfig.TYPE_OLD_CORE_BOARD);
                subBoardOtaFileConfig = otaFileConfigService.queryByType(OtaFileConfig.TYPE_OLD_SUB_BOARD);
            } else if (Objects.equals(fileType, EleOtaFile.TYPE_NEW_FILE)) {
                coreBoardOtaFileConfig = otaFileConfigService.queryByType(OtaFileConfig.TYPE_CORE_BOARD);
                subBoardOtaFileConfig = otaFileConfigService.queryByType(OtaFileConfig.TYPE_SUB_BOARD);
            } else if (Objects.equals(fileType, EleOtaFile.TYPE_SIX_IN_ONE_FILE)) {
                coreBoardOtaFileConfig = otaFileConfigService.queryByType(OtaFileConfig.TYPE_SIX_IN_ONE_CORE_BOARD);
                subBoardOtaFileConfig = otaFileConfigService.queryByType(OtaFileConfig.TYPE_SIX_IN_ONE_SUB_BOARD);
            }
            
            if (Objects.isNull(coreBoardOtaFileConfig) || Objects.isNull(subBoardOtaFileConfig)) {
                log.error("SEND DOWNLOAD OTA CONMMAND ERROR! incomplete upgrade file error! coreBoard={}, subBoard={}", coreBoardOtaFileConfig, subBoardOtaFileConfig);
                return Triple.of(Boolean.FALSE, "100301", "ota升级文件不完整，请联系客服处理");
            }
            
            createOrUpdateEleOtaFile(eid, fileType, coreBoardOtaFileConfig, subBoardOtaFileConfig);
            
            content.put(OtaConstant.OTA_CORE_FILE_URL, coreBoardOtaFileConfig.getDownloadLink());
            content.put(OtaConstant.OTA_CORE_FILE_SHA256HEX, coreBoardOtaFileConfig.getSha256Value());
            content.put(OtaConstant.OTA_SUB_FILE_URL, subBoardOtaFileConfig.getDownloadLink());
            content.put(OtaConstant.OTA_SUB_FILE_SHA256HEX, subBoardOtaFileConfig.getSha256Value());
        } else if (OtaConstant.OTA_TYPE_UPGRADE.equals(operateType) || OtaConstant.OTA_SIX_IN_ONE_TYPE_UPGRADE.equals(operateType)) {
            if (!DataUtil.collectionIsUsable(cellNos)) {
                return Triple.of(Boolean.FALSE, "100303", "升级内容为空，请选择您要升级的板子");
            }
            eleOtaUpgradeService.updateEleOtaUpgradeAndSaveHistory(cellNos, eid, sessionId);
            content.put(OtaConstant.OTA_CONTENT_CELLNOS, cellNos);
        }
        
        return Triple.of(Boolean.TRUE, null, content);
    }
    
    private void createOrUpdateEleOtaFile(Integer eid, Integer fileType, OtaFileConfig coreBoardOtaFileConfig, OtaFileConfig subBoardOtaFileConfig) {
        EleOtaFile eleOtaFile = eleOtaFileService.queryByEid(eid);
        if (Objects.nonNull(eleOtaFile)) {
            EleOtaFile update = new EleOtaFile();
            update.setId(eleOtaFile.getId());
            update.setCoreSha256Value(coreBoardOtaFileConfig.getSha256Value());
            update.setSubSha256Value(subBoardOtaFileConfig.getSha256Value());
            update.setFileType(fileType);
            update.setUpdateTime(System.currentTimeMillis());
            eleOtaFileService.update(update);
            return;
        }
        
        EleOtaFile create = new EleOtaFile();
        create.setElectricityCabinetId(eid);
        create.setCoreSha256Value(coreBoardOtaFileConfig.getSha256Value());
        create.setSubSha256Value(subBoardOtaFileConfig.getSha256Value());
        create.setCoreName(coreBoardOtaFileConfig.getName());
        create.setSubName(subBoardOtaFileConfig.getName());
        create.setFileType(fileType);
        create.setUpdateTime(System.currentTimeMillis());
        create.setCreateTime(System.currentTimeMillis());
        eleOtaFileService.insert(create);
    }
    
    private Integer getVersionPrefix(Integer eid) {
        EleCabinetCoreData eleCabinetCoreData = eleCabinetCoreDataService.selectByEleCabinetId(eid);
        if (Objects.nonNull(eleCabinetCoreData) && StringUtils.isNotEmpty(eleCabinetCoreData.getCoreVersion())) {
            String version = eleCabinetCoreData.getCoreVersion();
            if (!NumberUtil.equals(NumberConstant.MINUS_ONE, version.indexOf("."))) {
                return Integer.parseInt(version.substring(0, version.indexOf(".")));
            } else {
                return Integer.parseInt(version);
            }
        }
        
        List<ElectricityCabinetBox> electricityCabinetBoxes = electricityCabinetBoxService.queryAllBoxByElectricityCabinetId(eid);
        if (CollectionUtils.isEmpty(electricityCabinetBoxes)) {
            return null;
        }
        
        List<ElectricityCabinetBox> collect = electricityCabinetBoxes.parallelStream().filter(item -> StrUtil.isNotBlank(item.getVersion())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            return null;
        }
        
        String version = collect.get(0).getVersion();
        if (!NumberUtil.equals(NumberConstant.MINUS_ONE, version.indexOf("."))) {
            return Integer.parseInt(version.substring(0, version.indexOf(".")));
        } else {
            return Integer.parseInt(version);
        }
    }
    
    @Override
    public R checkOtaSession(String sessionId) {
        String s = redisService.get(CacheConstant.OTA_OPERATE_CACHE + sessionId);
        if (StrUtil.isBlank(s)) {
            return R.ok();
        }
        return R.ok(s);
    }
    
    @Slave
    @Override
    public R selectEleCabinetListByLongitudeAndLatitude(ElectricityCabinetQuery cabinetQuery) {
        List<ElectricityCabinet> electricityCabinets = electricityCabinetMapper.selectEleCabinetListByLongitudeAndLatitude(cabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinets)) {
            return R.ok(Collections.EMPTY_LIST);
        }
        
        return R.ok(electricityCabinets);
    }
    
    @Slave
    @Override
    public List<ElectricityCabinet> superAdminSelectByQuery(ElectricityCabinetQuery query) {
        List<ElectricityCabinet> list = electricityCabinetMapper.superAdminSelectByQuery(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        
        return list;
    }
    
    @Override
    public R acquireIdcardFileSign() {
        return R.ok(storageService.getOssUploadSign("saas/cabinet/"));
    }
    
    @Slave
    @Override
    public R queryName(Integer tenantId, Integer id) {
        return R.ok(electricityCabinetMapper.queryName(tenantId, id));
    }
    
    @Slave
    @Override
    public List<ElectricityCabinet> eleCabinetSearch(ElectricityCabinetQuery query) {
        List<ElectricityCabinet> electricityCabinets = electricityCabinetMapper.eleCabinetSearch(query);
        if (CollectionUtils.isEmpty(electricityCabinets)) {
            Collections.emptyList();
        }
        
        return electricityCabinets;
    }
    
    @Slave
    @Override
    public List<ElectricityCabinet> selectByQuery(ElectricityCabinetQuery query) {
        List<ElectricityCabinet> electricityCabinets = electricityCabinetMapper.selectByQuery(query);
        if (CollectionUtils.isEmpty(electricityCabinets)) {
            Collections.emptyList();
        }
        
        return electricityCabinets;
    }
    
    public ElectricityCabinet selectByProductKeyAndDeviceNameFromDB(String productKey, String deviceName, Integer tenantId) {
        return electricityCabinetMapper.selectOne(
                new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL).eq(ElectricityCabinet::getProductKey, productKey)
                        .eq(ElectricityCabinet::getDeviceName, deviceName).eq(ElectricityCabinet::getTenantId, tenantId));
    }
    
    @Slave
    @Override
    public List<EleCabinetDataAnalyseVO> selecteleCabinetVOByQuery(ElectricityCabinetQuery cabinetQuery) {
        return electricityCabinetMapper.selecteleCabinetVOByQuery(cabinetQuery);
    }
    
    @Slave
    @Override
    public R superAdminQueryName(Integer id) {
        return R.ok(electricityCabinetMapper.queryName(null, id));
    }
    
    @Override
    public List<Integer> selectEidByStoreId(Long storeId) {
        return electricityCabinetMapper.selectEidByStoreId(TenantContextHolder.getTenantId(), storeId);
    }
    
    @Slave
    @Override
    public List<ElectricityCabinetVO> selectElectricityCabinetByAddress(ElectricityCabinetQuery electricityCabinetQuery) {
        List<ElectricityCabinetVO> electricityCabinets = electricityCabinetMapper.selectElectricityCabinetByAddress(electricityCabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinets)) {
            return Collections.EMPTY_LIST;
        }
        
        return electricityCabinets.stream().peek(item -> {
            
            //营业时间
            if (Objects.nonNull(item.getBusinessTime())) {
                String businessTime = item.getBusinessTime();
                if (Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                    item.setBusinessTimeType(ElectricityCabinetVO.ALL_DAY);
                    item.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
                } else {
                    item.setBusinessTimeType(ElectricityCabinetVO.ILLEGAL_DATA);
                    int index = businessTime.indexOf("-");
                    if (!Objects.equals(index, -1) && index > 0) {
                        item.setBusinessTimeType(ElectricityCabinetVO.CUSTOMIZE_TIME);
                        Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                        Long beginTime = getTime(totalBeginTime);
                        Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                        Long endTime = getTime(totalEndTime);
                        item.setBeginTime(totalBeginTime);
                        item.setEndTime(totalEndTime);
                        Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                        long now = System.currentTimeMillis();
                        if (firstToday + beginTime > now || firstToday + endTime < now) {
                            item.setIsBusiness(ElectricityCabinetVO.IS_NOT_BUSINESS);
                        } else {
                            item.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
                        }
                    }
                }
            }
            
            Double fullyCharged = item.getFullyCharged();
            
            List<ElectricityCabinetBox> cabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(item.getId());
            if (!CollectionUtils.isEmpty(cabinetBoxList)) {
                //空仓
                long emptyCellNumber = cabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
                //满电数量
                long fullBatteryNumber = cabinetBoxList.stream().filter(this::isFullBattery).count();
                //可换电数量
                List<ElectricityCabinetBox> exchangeableList = cabinetBoxList.stream().filter(e -> isExchangeable(e, fullyCharged)).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(exchangeableList)) {
                    assignExchangeableBatteyType(exchangeableList, item);
                    assignExchangeableVoltageAndCapacity(exchangeableList, item);
                }
                long exchangeableNumber = exchangeableList.size();
                
                item.setNoElectricityBattery((int) emptyCellNumber);
                item.setFullyElectricityBattery((int) exchangeableNumber);//兼容2.0小程序首页显示问题
                item.setExchangeBattery((int) exchangeableNumber);
                item.setFullyBatteryNumber((int) fullBatteryNumber);
                
                Map<String, Long> batteryTypeMapes = cabinetBoxList.stream().filter(e -> StringUtils.isNotBlank(e.getSn()) && StringUtils.isNotBlank(e.getBatteryType()))
                        .map(i -> i.getBatteryType().substring(i.getBatteryType().indexOf("_") + 1)
                                .substring(0, i.getBatteryType().substring(i.getBatteryType().indexOf("_") + 1).indexOf("_")))
                        .collect(Collectors.groupingBy(a -> a, Collectors.counting()));
                item.setBatteryTypeMapes(batteryTypeMapes);
            }
            
            //获取柜机图片
            List<String> electricityCabinetPicture = getElectricityCabinetPicture(item.getId().longValue());
            if (!CollectionUtils.isEmpty(electricityCabinetPicture)) {
                item.setPictureUrl(electricityCabinetPicture.get(0));
            }
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public R batchOperateList(ElectricityCabinetQuery query) {
        List<ElectricityCabinetBatchOperateVo> list = electricityCabinetMapper.batchOperateList(query);
        if (ObjectUtil.isEmpty(list)) {
            return R.ok(Collections.emptyList());
        }
        
        list.parallelStream().peek(item -> {
            ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(item.getModelId());
            item.setModelName(electricityCabinetModel.getName());
        }).collect(Collectors.toList());
        
        return R.ok(list);
    }
    
    @Override
    public R cabinetSearch(Long size, Long offset, String name, Integer tenantId) {
        List<SearchVo> voList = electricityCabinetMapper.cabinetSearch(size, offset, name, tenantId);
        return R.ok(voList);
    }
    
    
    /**
     * 通过云端下发命令更新换电标准
     */
    private void updateFullyChargedByCloud(ElectricityCabinet electricityCabinet) {
        
        Map<String, Object> params = Maps.newHashMap();
        params.put(BATTERY_FULL_CONDITION, electricityCabinet.getFullyCharged());
        
        EleOuterCommandQuery commandQuery = new EleOuterCommandQuery();
        commandQuery.setProductKey(electricityCabinet.getProductKey());
        commandQuery.setDeviceName(electricityCabinet.getDeviceName());
        commandQuery.setCommand(ElectricityIotConstant.ELE_OTHER_SETTING);
        commandQuery.setData(params);
        
        try {
            sendCommandToEleForOuter(commandQuery);
        } catch (Exception e) {
            log.error("ELE ERROR！set batteryFullCondition error,electricityCabinetId={}", electricityCabinet.getId(), e);
        }
    }
    
    
    /**
     * 满仓提醒
     *
     * @param messages
     */
    @Override
    public void sendFullBatteryMessage(List<Message> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        
        messages.parallelStream().forEach(item -> {
            log.info("DELY QUEUE LISTENER INFO! full battery message={}", JsonUtil.toJson(item));
            
            if (StringUtils.isBlank(item.getMsg())) {
                return;
            }
            
            Integer electricityCabinetId = Integer.parseInt(item.getMsg());
            
            ElectricityCabinet electricityCabinet = this.queryByIdFromCache(electricityCabinetId);
            if (Objects.isNull(electricityCabinet)) {
                return;
            }
            
            //获取所有启用的格挡
            List<ElectricityCabinetBox> electricityCabinetBoxes = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinetId);
            if (org.apache.commons.collections.CollectionUtils.isEmpty(electricityCabinetBoxes)) {
                return;
            }
            
            //过滤没有电池的格挡
            List<ElectricityCabinetBox> haveBatteryBoxs = electricityCabinetBoxes.stream().filter(e -> StringUtils.isBlank(e.getSn())).collect(Collectors.toList());
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(haveBatteryBoxs)) {
                return;
            }
            
            Boolean cacheFlag = redisService.setNx(CacheConstant.FULL_BOX_ELECTRICITY_CACHE + electricityCabinetId, "1", 1800 * 1000L, false);
            if (cacheFlag) {
                List<MqNotifyCommon<ElectricityAbnormalMessageNotify>> messageNotifyList = buildAbnormalMessageNotify(electricityCabinet);
                if (CollectionUtils.isEmpty(messageNotifyList)) {
                    return;
                }
                
                messageNotifyList.forEach(i -> {
                    rocketMqService.sendAsyncMsg(MqProducerConstant.TOPIC_MAINTENANCE_NOTIFY, JsonUtil.toJson(i), "", "", 0);
                });
            }
        });
    }
    
    @Override
    public List<MqNotifyCommon<ElectricityAbnormalMessageNotify>> buildAbnormalMessageNotify(ElectricityCabinet electricityCabinet) {
        MaintenanceUserNotifyConfig notifyConfig = maintenanceUserNotifyConfigService.queryByTenantIdFromCache(electricityCabinet.getTenantId());
        if (Objects.isNull(notifyConfig) || StringUtils.isBlank(notifyConfig.getPhones())) {
            log.warn("ELE BATTERY REPORT WARN! not found maintenanceUserNotifyConfig,tenantId={}", electricityCabinet.getTenantId());
            return Collections.EMPTY_LIST;
        }
        
        List<String> phones = JSON.parseObject(notifyConfig.getPhones(), List.class);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(phones)) {
            log.warn("ELE BATTERY REPORT WARN! phones is empty,tenantId={}", electricityCabinet.getTenantId());
            return Collections.EMPTY_LIST;
        }
        
        return phones.parallelStream().map(item -> {
            ElectricityAbnormalMessageNotify abnormalMessageNotify = new ElectricityAbnormalMessageNotify();
            abnormalMessageNotify.setAddress(electricityCabinet.getAddress());
            abnormalMessageNotify.setEquipmentNumber(electricityCabinet.getName());
            abnormalMessageNotify.setExceptionType(ElectricityAbnormalMessageNotify.BATTERY_FULL_TYPE);
            abnormalMessageNotify.setDescription(ElectricityAbnormalMessageNotify.BATTERY_FULL_MSG);
            abnormalMessageNotify.setReportTime(formatter.format(LocalDateTime.now()));
            
            MqNotifyCommon<ElectricityAbnormalMessageNotify> abnormalMessageNotifyCommon = new MqNotifyCommon<>();
            abnormalMessageNotifyCommon.setTime(System.currentTimeMillis());
            abnormalMessageNotifyCommon.setType(MqNotifyCommon.TYPE_ABNORMAL_ALARM);
            abnormalMessageNotifyCommon.setPhone(item);
            abnormalMessageNotifyCommon.setData(abnormalMessageNotify);
            return abnormalMessageNotifyCommon;
        }).collect(Collectors.toList());
        
    }
    
    /**
     * 获取柜机图片
     */
    private List<String> getElectricityCabinetPicture(Long eid) {
        try {
            List<ElectricityCabinetFile> electricityCabinetFileList = electricityCabinetFileService.selectByFileTypeAndEid(eid, ElectricityCabinetFile.TYPE_ELECTRICITY_CABINET);
            if (CollectionUtils.isEmpty(electricityCabinetFileList)) {
                return Collections.EMPTY_LIST;
            }
            
            List<ElectricityCabinetFile> cabinetFiles = electricityCabinetFileList.parallelStream().peek(item -> {
                item.setUrl(storageService.getOssFileUrl(storageConfig.getBucketName(), item.getName(), System.currentTimeMillis() + 10 * 60 * 1000L));
            }).collect(Collectors.toList());
            
            return cabinetFiles.parallelStream().map(ElectricityCabinetFile::getUrl).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("ELE ERROR! get electricityCabinet picture error", e);
        }
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public Triple<Boolean, String, Object> existsElectricityCabinet(String productKey, String deviceName) {
        Integer tenantId = TenantContextHolder.getTenantId();
        if (Objects.isNull(tenantId) || StringUtils.isEmpty(productKey) || StringUtils.isEmpty(deviceName)) {
            return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
        }
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            return Triple.of(false, "ELECTRICITY.0005", "未找到换电柜");
        }
        if (!tenantId.equals(electricityCabinet.getTenantId())) {
            log.error("query existsElectricityCabinet  ERROR!tenantId is not equal!tenantId1={}, tenantId2={} ,sn={}", tenantId, electricityCabinet.getTenantId(),
                    electricityCabinet.getSn());
            return Triple.of(false, "100373", "当前运营商与柜机所属运营商不一致");
        }
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> batchDeleteCabinet(Set<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
        }
        
        for (Integer id : ids) {
            ElectricityCabinet electricityCabinet = queryByIdFromCache(id);
            if (Objects.isNull(electricityCabinet)) {
                log.info("ELE INFO!delete fail,not found cabinet,id={}", id);
                continue;
            }
            
            ElectricityCabinet electricityCabinetUpdate = new ElectricityCabinet();
            electricityCabinetUpdate.setId(id);
            electricityCabinetUpdate.setUpdateTime(System.currentTimeMillis());
            electricityCabinetUpdate.setDelFlag(ElectricityCabinet.DEL_DEL);
            electricityCabinetUpdate.setTenantId(TenantContextHolder.getTenantId());
            
            DbUtils.dbOperateSuccessThenHandleCache(electricityCabinetMapper.updateEleById(electricityCabinetUpdate), i -> {
                redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + id);
                redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
                
                //删除格挡
                electricityCabinetBoxService.batchDeleteBoxByElectricityCabinetId(id);
            });
            
        }
        
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> batchImportCabinet(List<ElectricityCabinetImportQuery> list) {
        if (!redisService.setNx(CacheConstant.ELE_BATCH_IMPORT + SecurityUtils.getUid(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        Triple<Boolean, String, Object> verifyBatchImportResult = verifyBatchImportParams(list);
        if (Boolean.FALSE.equals(verifyBatchImportResult.getLeft())) {
            return verifyBatchImportResult;
        }
        
        for (ElectricityCabinetImportQuery query : list) {
            ElectricityCabinet electricityCabinet = new ElectricityCabinet();
            BeanUtils.copyProperties(query, electricityCabinet);
            electricityCabinet.setBusinessTime(ElectricityCabinetAddAndUpdate.ALL_DAY);
            electricityCabinet.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
            electricityCabinet.setDelFlag(ElectricityCabinet.DEL_NORMAL);
            electricityCabinet.setTenantId(TenantContextHolder.getTenantId());
            electricityCabinet.setCreateTime(System.currentTimeMillis());
            electricityCabinet.setUpdateTime(System.currentTimeMillis());
            
            DbUtils.dbOperateSuccessThenHandleCache(electricityCabinetMapper.insert(electricityCabinet), i -> {
                //添加格挡
                electricityCabinetBoxService.batchInsertBoxByModelId(electricityCabinetModelService.queryByIdFromCache(query.getModelId()), electricityCabinet.getId());
                //添加服务时间记录
                electricityCabinetServerService.insertOrUpdateByElectricityCabinet(electricityCabinet, electricityCabinet);
            });
        }
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public void batchUpdate(List<ElectricityCabinet> list) {
        list.forEach(item -> DbUtils.dbOperateSuccessThenHandleCache(electricityCabinetMapper.updateEleById(item), i -> {
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + item.getId());
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + item.getProductKey() + item.getDeviceName());
        }));
    }
    
    @Override
    public Triple<Boolean, String, Object> batchUpdateAddress(List<ElectricityCabinet> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Triple.of(false, null, "参数不合法");
        }
        
        List<ElectricityCabinet> updateList = new ArrayList<>(list.size());
        
        list.forEach(item -> {
            ElectricityCabinet electricityCabinet = this.queryByIdFromCache(item.getId());
            if (Objects.isNull(electricityCabinet) || !Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
                throw new BizException("ELECTRICITY.0007", "不合法的参数");
            }
            
            electricityCabinet.setAddress(item.getAddress());
            electricityCabinet.setLatitude(item.getLatitude());
            electricityCabinet.setLongitude(item.getLongitude());
            electricityCabinet.setUpdateTime(System.currentTimeMillis());
            updateList.add(electricityCabinet);
        });
        
        this.batchUpdate(updateList);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> transferCabinet(ElectricityCabinetTransferQuery query) {
        
        Store store = storeService.queryByIdFromCache(query.getStoreId());
        if (Objects.isNull(store) || !Objects.equals(store.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("ELE ERROR!not found store,storeId={}", query.getStoreId());
            return Triple.of(false, "", "门店不存在");
        }
        
        //查询工厂租户下是否有该柜机
        ElectricityCabinet testFactoryCabinet = this.selectByProductKeyAndDeviceNameFromDB(query.getProductKey(), query.getDeviceName(), eleCommonConfig.getTestFactoryTenantId());
        if (Objects.isNull(testFactoryCabinet)) {
            log.error("ELE ERROR!not found testFactoryCabinet,p={},d={},tenantId={}", query.getProductKey(), query.getDeviceName(), eleCommonConfig.getTestFactoryTenantId());
            return Triple.of(false, "", "柜机不存在");
        }
        
        //获取工厂柜机型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(testFactoryCabinet.getModelId());
        if (Objects.isNull(electricityCabinetModel)) {
            log.error("ELE ERROR!not found electricityCabinetModel,p={},d={},tenantId={}", query.getProductKey(), query.getDeviceName(), eleCommonConfig.getTestFactoryTenantId());
            return Triple.of(false, "", "柜机型号不存在");
        }
        
        Integer modelId = null;
        //查询当前租户下是否有该型号，若没有则新建
        ElectricityCabinetModel cabinetModel = electricityCabinetModelService.selectByNum(electricityCabinetModel.getNum(), TenantContextHolder.getTenantId());
        if (Objects.isNull(cabinetModel)) {
            ElectricityCabinetModel cabinetModelInsert = buildCabinetModel(electricityCabinetModel);
            electricityCabinetModelService.insert(cabinetModelInsert);
            modelId = cabinetModelInsert.getId();
        } else {
            modelId = cabinetModel.getId();
        }
        
        //当前租户下新增柜机
        ElectricityCabinet electricityCabinetInsert = new ElectricityCabinet();
        electricityCabinetInsert.setName(query.getName());
        electricityCabinetInsert.setSn(query.getDeviceName());
        electricityCabinetInsert.setModelId(modelId);
        electricityCabinetInsert.setProductKey(query.getProductKey());
        electricityCabinetInsert.setDeviceName(query.getDeviceName());
        electricityCabinetInsert.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        electricityCabinetInsert.setAddress(query.getAddress());
        electricityCabinetInsert.setLatitude(query.getLatitude());
        electricityCabinetInsert.setLongitude(query.getLongitude());
        electricityCabinetInsert.setUsableStatus(ElectricityCabinet.ELECTRICITY_CABINET_USABLE_STATUS);
        electricityCabinetInsert.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
        electricityCabinetInsert.setVersion(testFactoryCabinet.getVersion());
        electricityCabinetInsert.setFullyCharged(testFactoryCabinet.getFullyCharged());
        electricityCabinetInsert.setServicePhone(testFactoryCabinet.getServicePhone());
        electricityCabinetInsert.setBusinessTime(testFactoryCabinet.getBusinessTime());
        electricityCabinetInsert.setCreateTime(System.currentTimeMillis());
        electricityCabinetInsert.setUpdateTime(System.currentTimeMillis());
        electricityCabinetInsert.setTenantId(TenantContextHolder.getTenantId());
        electricityCabinetInsert.setStoreId(query.getStoreId());
        electricityCabinetInsert.setVersion(testFactoryCabinet.getVersion());
        electricityCabinetInsert.setExchangeType(testFactoryCabinet.getExchangeType());
        
        //物理删除工厂测试柜机
        this.physicsDelete(testFactoryCabinet);
        
        DbUtils.dbOperateSuccessThenHandleCache(electricityCabinetMapper.insert(electricityCabinetInsert), i -> {
            electricityCabinetBoxService.batchInsertBoxByModelId(electricityCabinetModel, electricityCabinetInsert.getId());
            electricityCabinetServerService.insertOrUpdateByElectricityCabinet(electricityCabinetInsert, electricityCabinetInsert);
        });
        
        //生成迁移记录
        cabinetMoveHistoryService.insert(buildCabinetMoveHistory(testFactoryCabinet, electricityCabinetInsert));
        
        return Triple.of(true, null, null);
    }
    
    private CabinetMoveHistory buildCabinetMoveHistory(ElectricityCabinet testFactoryCabinet, ElectricityCabinet electricityCabinetInsert) {
        CabinetMoveHistory cabinetMoveHistory = new CabinetMoveHistory();
        cabinetMoveHistory.setUid(SecurityUtils.getUid());
        cabinetMoveHistory.setEid(electricityCabinetInsert.getId().longValue());
        cabinetMoveHistory.setOldInfo(JsonUtil.toJson(testFactoryCabinet));
        cabinetMoveHistory.setProductKey(electricityCabinetInsert.getProductKey());
        cabinetMoveHistory.setDeviceName(electricityCabinetInsert.getDeviceName());
        cabinetMoveHistory.setTenantId(TenantContextHolder.getTenantId());
        cabinetMoveHistory.setCreateTime(System.currentTimeMillis());
        cabinetMoveHistory.setUpdateTime(System.currentTimeMillis());
        return cabinetMoveHistory;
    }
    
    private ElectricityCabinetModel buildCabinetModel(ElectricityCabinetModel electricityCabinetModel) {
        ElectricityCabinetModel cabinetModelInsert = new ElectricityCabinetModel();
        cabinetModelInsert.setName(electricityCabinetModel.getName());
        cabinetModelInsert.setNum(electricityCabinetModel.getNum());
        cabinetModelInsert.setDelFlag(ElectricityCabinetModel.DEL_NORMAL);
        cabinetModelInsert.setTenantId(TenantContextHolder.getTenantId());
        cabinetModelInsert.setCreateTime(System.currentTimeMillis());
        cabinetModelInsert.setUpdateTime(System.currentTimeMillis());
        return cabinetModelInsert;
    }
    
    private Triple<Boolean, String, Object> verifyBatchImportParams(List<ElectricityCabinetImportQuery> list) {
        Set<String> deviceNameSet = list.stream().map(ElectricityCabinetImportQuery::getDeviceName).collect(Collectors.toSet());
        if (deviceNameSet.size() != list.size()) {
            return Triple.of(false, "", "三元组重复");
        }
        
        for (ElectricityCabinetImportQuery cabinetImportQuery : list) {
            ElectricityCabinet electricityCabinet = this.queryByProductAndDeviceName(cabinetImportQuery.getProductKey(), cabinetImportQuery.getDeviceName());
            if (Objects.nonNull(electricityCabinet)) {
                return Triple.of(false, "", "三元组已存在");
            }
            
            Store store = storeService.queryByIdFromCache(cabinetImportQuery.getStoreId());
            if (Objects.isNull(store) || !Objects.equals(store.getTenantId(), TenantContextHolder.getTenantId())) {
                return Triple.of(false, "", "门店不存在");
            }
            
            ElectricityCabinetModel cabinetModel = electricityCabinetModelService.queryByIdFromCache(cabinetImportQuery.getModelId());
            if (Objects.isNull(cabinetModel) || !Objects.equals(cabinetModel.getTenantId(), TenantContextHolder.getTenantId())) {
                return Triple.of(false, "", "柜机型号不存在");
            }
        }
        
        return Triple.of(true, null, null);
    }
    
    @Slave
    @Override
    public void exportExcel(ElectricityCabinetQuery query, HttpServletResponse response) {
        
        List<ElectricityCabinetVO> electricityCabinetList = electricityCabinetMapper.queryList(query);
        if (CollectionUtils.isEmpty(electricityCabinetList)) {
            throw new CustomBusinessException("柜机列表为空！");
        }
        
        List<ElectricityCabinetExcelVO> excelVOS = new ArrayList<>(electricityCabinetList.size());
        int index = 0;
        
        for (ElectricityCabinetVO cabinetVO : electricityCabinetList) {
            
            ElectricityCabinetModel cabinetModel = electricityCabinetModelService.queryByIdFromCache(cabinetVO.getModelId());
            
            index++;
            
            ElectricityCabinetExcelVO excelVO = new ElectricityCabinetExcelVO();
            excelVO.setId(index);
            excelVO.setSn(cabinetVO.getSn());
            excelVO.setName(cabinetVO.getName());
            excelVO.setAddress(cabinetVO.getAddress());
            excelVO.setUsableStatus(Objects.equals(cabinetVO.getUsableStatus(), ElectricityCabinet.ELECTRICITY_CABINET_USABLE_STATUS) ? "启用" : "禁用");
            excelVO.setModelName(Objects.nonNull(cabinetModel) ? cabinetModel.getName() : "");
            excelVO.setVersion(cabinetVO.getVersion());
            excelVO.setFranchiseeName(acquireFranchiseeNameByStore(cabinetVO.getStoreId()));
            excelVO.setCreateTime(Objects.nonNull(cabinetVO.getCreateTime()) ? DateUtil.format(DateUtil.date(cabinetVO.getCreateTime()), DatePattern.NORM_DATETIME_FORMATTER) : "");
            excelVO.setExchangeType(acquireExchangeType(cabinetVO.getExchangeType()));
            
            ElectricityCabinetServer electricityCabinetServer = electricityCabinetServerService.queryByProductKeyAndDeviceName(cabinetVO.getProductKey(),
                    cabinetVO.getDeviceName());
            if (Objects.nonNull(electricityCabinetServer)) {
                excelVO.setServerEndTime(Objects.nonNull(electricityCabinetServer.getServerEndTime()) ? DateUtil.format(DateUtil.date(electricityCabinetServer.getServerEndTime()),
                        DatePattern.NORM_DATETIME_FORMATTER) : "");
            }
            
            excelVOS.add(excelVO);
        }
        
        String fileName = "电柜列表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, ElectricityCabinetExcelVO.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).doWrite(excelVOS);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }
    
    private String acquireExchangeType(Integer exchangeType) {
        String type = null;
        switch (exchangeType) {
            case 1:
                type = "有屏";
                break;
            case 2:
                type = "无屏";
                break;
            case 3:
                type = "单片机";
                break;
            default:
                type = "未知";
                break;
        }
        return type;
    }
    
    private String acquireFranchiseeNameByStore(Integer storeId) {
        String name = "";
        
        if (Objects.isNull(storeId)) {
            return name;
        }
        
        Store store = storeService.queryByIdFromCache(storeId.longValue());
        if (Objects.isNull(store)) {
            return name;
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(store.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return name;
        }
        
        return franchisee.getName();
    }
    
    /**
     * 查询柜机扩展参数
     *
     * @param electricityCabinetId 柜机id
     * @return 柜机扩展参数
     */
    public R queryElectricityCabinetExtendData(Integer electricityCabinetId) {
        
        //校验柜机Id
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(electricityCabinet) || !Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("100003", "柜机不存在");
        }
        
        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        String netType = null;
        //如果柜机在线，则需要取柜机上报的信号
        if (eleResult) {
            netType = redisService.get(CacheConstant.CACHE_ELECTRICITY_CABINET_EXTEND_DATA + electricityCabinetId);
        }
        
        return R.ok(netType);
    }
    
    public R showBatteryVAndCapacity(Integer electricityCabinetId) {
        //校验柜机Id
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(electricityCabinet) || !Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("100003", "柜机不存在");
        }
        
        List<ElectricityCabinetBox> cabinetBoxList = electricityCabinetBoxService.selectEleBoxAttrByEid(electricityCabinet.getId());
        if (CollectionUtils.isEmpty(cabinetBoxList)) {
            return R.ok();
        }
        
        //可换电数量
        List<ElectricityCabinetBox> exchangeableList = cabinetBoxList.stream().filter(item -> isExchangeable(item, electricityCabinet.getFullyCharged()))
                .collect(Collectors.toList());
        return R.ok(assignExchangeableVoltageAndCapacityV2(exchangeableList));
    }
}
