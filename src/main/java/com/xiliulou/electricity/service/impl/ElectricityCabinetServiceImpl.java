package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
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
import com.xiliulou.electricity.bo.asset.ElectricityCabinetBO;
import com.xiliulou.electricity.bo.cabinet.ElectricityCabinetMapBO;
import com.xiliulou.electricity.bo.merchant.AreaCabinetNumBO;
import com.xiliulou.electricity.config.CabinetConfig;
import com.xiliulou.electricity.config.EleCommonConfig;
import com.xiliulou.electricity.config.EleIotOtaPathConfig;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.DeviceReportConstant;
import com.xiliulou.electricity.constant.EleCabinetConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.OtaConstant;
import com.xiliulou.electricity.constant.RegularConstant;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.dto.ElectricityCabinetOtherSetting;
import com.xiliulou.electricity.converter.storage.StorageConverter;
import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.dto.ExchangeChainDTO;
import com.xiliulou.electricity.dto.QuickExchangeResultDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.BatteryModel;
import com.xiliulou.electricity.entity.CabinetMoveHistory;
import com.xiliulou.electricity.entity.EleCabinetCoreData;
import com.xiliulou.electricity.entity.EleDeviceCode;
import com.xiliulou.electricity.entity.EleOtaFile;
import com.xiliulou.electricity.entity.ElectricityAbnormalMessageNotify;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetExtra;
import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetServer;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.MaintenanceUserNotifyConfig;
import com.xiliulou.electricity.entity.Message;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.entity.OtaFileConfig;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.merchant.MerchantArea;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.EleCabinetModelHeatingEnum;
import com.xiliulou.electricity.enums.FlexibleRenewalEnum;
import com.xiliulou.electricity.enums.ExchangeAssertChainTypeEnum;
import com.xiliulou.electricity.enums.ExchangeTypeEnum;
import com.xiliulou.electricity.enums.RentReturnNormEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.asset.StockStatusEnum;
import com.xiliulou.electricity.enums.notify.AbnormalAlarmExceptionTypeEnum;
import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import com.xiliulou.electricity.enums.thirdParthMall.ThirdPartyMallEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.ElectricityCabinetMapper;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.mq.producer.MessageSendProducer;
import com.xiliulou.electricity.query.BatteryReportQuery;
import com.xiliulou.electricity.query.DeviceStatusQuery;
import com.xiliulou.electricity.query.EleCabinetPatternQuery;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityCabinetAddressQuery;
import com.xiliulou.electricity.query.ElectricityCabinetBatchEditRentReturnCountQuery;
import com.xiliulou.electricity.query.ElectricityCabinetBatchEditRentReturnQuery;
import com.xiliulou.electricity.query.ElectricityCabinetImportQuery;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.query.ElectricityCabinetTransferQuery;
import com.xiliulou.electricity.query.FreeCellNoQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.query.HomepageElectricityExchangeFrequencyQuery;
import com.xiliulou.electricity.query.LowBatteryExchangeModel;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.electricity.query.exchange.QuickExchangeQuery;
import com.xiliulou.electricity.queryModel.EleCabinetExtraQueryModel;
import com.xiliulou.electricity.request.asset.TransferCabinetModelRequest;
import com.xiliulou.electricity.request.merchant.MerchantAreaRequest;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.asset.AssetWarehouseService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.service.merchant.MerchantAreaService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeRecordService;
import com.xiliulou.electricity.service.pipeline.ProcessContext;
import com.xiliulou.electricity.service.pipeline.ProcessController;
import com.xiliulou.electricity.service.thirdPartyMall.PushDataToThirdService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.electricity.utils.AssertUtil;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.DeviceTextUtil;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.VersionUtil;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.electricity.vo.asset.AssetWarehouseNameVO;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.response.QueryDeviceDetailResult;
import com.xiliulou.iot.service.IotAcsService;
import com.xiliulou.iot.service.PubHardwareService;
import com.xiliulou.iot.service.RegisterDeviceService;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import io.undertow.server.session.SessionIdGenerator;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import java.util.HashSet;
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

import static com.xiliulou.electricity.constant.ElectricityIotConstant.ELE_COMMAND_CELL_UPDATE;
import static com.xiliulou.electricity.entity.ElectricityCabinet.ELECTRICITY_CABINET_USABLE_STATUS;
import static com.xiliulou.electricity.entity.ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY;
import static com.xiliulou.electricity.entity.ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY;
import static com.xiliulou.electricity.entity.ElectricityCabinetExtra.MIN_RETAIN_BATTERY;
import static com.xiliulou.electricity.entity.ElectricityCabinetExtra.MIN_RETAIN_EMPTY_CELL;
import static com.xiliulou.electricity.vo.ElectricityCabinetSimpleVO.IS_EXCHANGE;
import static com.xiliulou.electricity.vo.ElectricityCabinetSimpleVO.IS_RENT;
import static com.xiliulou.electricity.vo.ElectricityCabinetSimpleVO.IS_RETURN;

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
    
    /**
     * 柜机编辑时，设置换电标准属性值
     */
    private static final String BATTERY_FULL_CONDITION = "exchangeCondition";
    
    private static final String OPEN_FAN_CONDITION_KEY = "openFanCondition";
    
    private static final String OPEN_HEAT_CONDITION_KEY = "openHeatCondition";
    
    /**
     * 吞电池优化版本
     */
    private static final String ELE_CABINET_VERSION = "2.1.7";
    
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
    StorageConverter storageConverter;
    
    @Autowired
    EleCommonConfig eleCommonConfig;
    
    @Autowired
    private ElectricityCabinetServerService electricityCabinetServerService;
    
    @Autowired
    RocketMqService rocketMqService;
    
    @Autowired
    private MessageSendProducer messageSendProducer;
    
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
    OperateRecordUtil operateRecordUtil;
    
    @Autowired
    CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;
    
    @Autowired
    CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;
    
    @Autowired
    BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Autowired
    AssetWarehouseService assetWarehouseService;
    
    @Resource
    private MerchantPlaceFeeRecordService merchantPlaceFeeRecordService;
    
    @Resource
    private MerchantAreaService merchantAreaService;
    
    @Resource
    private ElectricityCabinetExtraService electricityCabinetExtraService;
    
    @Resource
    private ElectricityCabinetChooseCellConfigService chooseCellConfigService;
    
    @Autowired
    private EleDeviceCodeService eleDeviceCodeService;
    
    @Resource
    private PushDataToThirdService pushDataToThirdService;
    
    @Resource
    private ExchangeExceptionHandlerService exceptionHandlerService;
    
    @Autowired
    private RegisterDeviceService registerDeviceService;
    
    @Resource
    private CabinetConfig cabinetConfig;
    
    @Resource
    private TenantFranchiseeMutualExchangeService mutualExchangeService;


    @Resource
    private UserActiveInfoService userActiveInfoService;

    @Resource
    private ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;

    @Resource
    private ProcessController processController;

    @Resource
    private LessTimeExchangeService lessTimeExchangeService;

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
        // 先查缓存
        ElectricityCabinet cacheElectricityCabinet = redisService.getWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET + id, ElectricityCabinet.class);
        if (Objects.nonNull(cacheElectricityCabinet)) {
            return cacheElectricityCabinet;
        }
        // 缓存没有再查数据库
        ElectricityCabinet electricityCabinet = electricityCabinetMapper.selectById(id);
        if (Objects.isNull(electricityCabinet)) {
            return null;
        }
        
        // 放入缓存
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
            // 更新缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId());
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
        }
        return update;
    }
    
    @Override
    @Transactional
    public R edit(ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 柜机名称长度最大为30
        if (electricityCabinetAddAndUpdate.getName().length() > 30) {
            return R.fail("100377", "参数校验错误");
        }
        
        //  如果场地费不为空则需要判断 不能小于零 小数最多两位  整数不能大于8位
        if (Objects.nonNull(electricityCabinetAddAndUpdate.getPlaceFee())) {
            // 场地费必须大于零
            if (Objects.equals(electricityCabinetAddAndUpdate.getPlaceFee().compareTo(BigDecimal.ZERO), NumberConstant.MINUS_ONE)) {
                return R.fail("120235", "场地费必须大于等于零");
            }
            
            // 场地费不能是负数
            String placeFeeStr = electricityCabinetAddAndUpdate.getPlaceFee().toString();
            if (!RegularConstant.PLACE_PATTERN.matcher(placeFeeStr).matches()) {
                return R.fail("120234", "场地费保留两位小数且整数部分不能超过8位");
            }
        }
        
        // 操作频繁
        boolean result = redisService.setNx(CacheConstant.ELE_EDIT_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        // 换电柜
        ElectricityCabinet electricityCabinet = new ElectricityCabinet();
        BeanUtil.copyProperties(electricityCabinetAddAndUpdate, electricityCabinet);
        ElectricityCabinet oldElectricityCabinet = queryByIdFromCache(electricityCabinet.getId());
        if (Objects.isNull(oldElectricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        // 柜机扩展信息校验
        checkUpdateOneElectricityCabinetExtra(electricityCabinetAddAndUpdate);
        
        MerchantPlaceFeeRecord finalMerchantPlaceFeeRecord = getPlaceFeeRecord(oldElectricityCabinet, electricityCabinetAddAndUpdate, user);
        
        // 判断参数
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
        
        // 三元组
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
        
        // 加盟商
        if (Objects.nonNull(electricityCabinetAddAndUpdate.getStoreId())) {
            Store store = storeService.queryByIdFromCache(electricityCabinetAddAndUpdate.getStoreId());
            electricityCabinet.setFranchiseeId(Objects.nonNull(store) ? store.getFranchiseeId() : null);
        }
        
        // 快递柜老型号
        Integer oldModelId = oldElectricityCabinet.getModelId();
        // 查找快递柜型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(electricityCabinet.getModelId());
        if (Objects.isNull(electricityCabinetModel)) {
            return R.fail("ELECTRICITY.0004", "未找到换电柜型号");
        }
        if (!oldModelId.equals(electricityCabinet.getModelId())) {
            return R.fail("ELECTRICITY.0010", "不能修改型号");
        }
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinet.setTenantId(TenantContextHolder.getTenantId());
        
        // 扩展新的修改方法  场地费和区域不为空也可修改
        int update = electricityCabinetMapper.updateCabinetById(electricityCabinet);
        
        DbUtils.dbOperateSuccessThen(update, () -> {
            
            // 更新缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId());
            
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + oldElectricityCabinet.getProductKey() + oldElectricityCabinet.getDeviceName());
            
            // 更新换电柜GEO信息
            addElectricityCabinetLocToGeo(electricityCabinet);
            
            // 添加快递柜格挡
            if (!oldModelId.equals(electricityCabinet.getModelId())) {
                electricityCabinetBoxService.batchDeleteBoxByElectricityCabinetId(electricityCabinet.getId());
                electricityCabinetBoxService.batchInsertBoxByModelIdV2(electricityCabinetModel, electricityCabinet.getId());
            }
            
            // 修改柜机服务时间信息
            electricityCabinetServerService.insertOrUpdateByElectricityCabinet(electricityCabinet, oldElectricityCabinet);
            
            // 修改柜机额外信息
            updateElectricityCabinetExtra(electricityCabinetAddAndUpdate);
            
            // 增加场地费变更记录
            if (Objects.nonNull(finalMerchantPlaceFeeRecord)) {
                merchantPlaceFeeRecordService.asyncInsertOne(finalMerchantPlaceFeeRecord);
            }
            
            // 云端下发命令修改换电标准
            if (!Objects.equals(oldElectricityCabinet.getFullyCharged(), electricityCabinet.getFullyCharged())) {
                this.updateFullyChargedByCloud(electricityCabinet);
            }
            
            return null;
        });
        
        // 给第三方推送柜机信息
        pushDataToThirdService.asyncPushCabinetToThird(ThirdPartyMallEnum.MEI_TUAN_RIDER_MALL.getCode(), TtlTraceIdSupport.get(), electricityCabinet.getTenantId(),
                electricityCabinet.getId().longValue());
        
        return R.ok();
    }
    
    private void checkUpdateOneElectricityCabinetExtra(ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        if (Objects.isNull(electricityCabinetAddAndUpdate.getRentTabType())) {
            throw new CustomBusinessException("租电类型不能为空");
        }
        
        if (Objects.isNull(electricityCabinetAddAndUpdate.getReturnTabType())) {
            throw new CustomBusinessException("退电类型不能为空");
        }
        
        if (Objects.equals(electricityCabinetAddAndUpdate.getRentTabType(), RentReturnNormEnum.CUSTOM_RENT.getCode()) && Objects
                .isNull(electricityCabinetAddAndUpdate.getMinRetainBatteryCount())) {
            throw new CustomBusinessException("自定义，最小保留电池数不能为空");
        }
        
        if (Objects.equals(electricityCabinetAddAndUpdate.getReturnTabType(), RentReturnNormEnum.CUSTOM_RETURN.getCode()) && Objects
                .isNull(electricityCabinetAddAndUpdate.getMaxRetainBatteryCount())) {
            throw new CustomBusinessException("自定义！保留空仓数不能为空");
        }
    }
    
    private void updateElectricityCabinetExtra(ElectricityCabinetAddAndUpdate cabinetAddAndUpdate) {
        if (Objects.isNull(cabinetAddAndUpdate.getId())) {
            log.warn("updateElectricityCabinetExtra is error, cabinetId is null");
            return;
        }
        ElectricityCabinetExtra cabinetExtra = electricityCabinetExtraService.queryByEid(Long.valueOf(cabinetAddAndUpdate.getId()));
        if (Objects.isNull(cabinetExtra)) {
            log.warn("updateElectricityCabinetExtra is error, cabinetExtra is null, id:{}", cabinetAddAndUpdate.getId());
            return;
        }
        Integer rentTabType = cabinetAddAndUpdate.getRentTabType();
        Integer returnTabType = cabinetAddAndUpdate.getReturnTabType();
        
        // 更新
        EleCabinetExtraQueryModel extraQueryModel = EleCabinetExtraQueryModel.builder().id(Long.valueOf(cabinetAddAndUpdate.getId())).rentTabType(rentTabType)
                .returnTabType(returnTabType).maxRetainBatteryCount(cabinetAddAndUpdate.getMaxRetainBatteryCount())
                .minRetainBatteryCount(cabinetAddAndUpdate.getMinRetainBatteryCount()).updateTime(System.currentTimeMillis()).build();
        // 如果不是自定义租电，则设置为null
        if (!Objects.equals(rentTabType, RentReturnNormEnum.CUSTOM_RENT.getCode())) {
            extraQueryModel.setMinRetainBatteryCount(null);
        }
        // 如果不是自定义退电，则设置为null
        if (!Objects.equals(returnTabType, RentReturnNormEnum.CUSTOM_RETURN.getCode())) {
            extraQueryModel.setMaxRetainBatteryCount(null);
        }
        electricityCabinetExtraService.updateTabTypeCabinetExtra(extraQueryModel);
    }
    
    private MerchantPlaceFeeRecord getPlaceFeeRecord(ElectricityCabinet oldElectricityCabinet, ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate, TokenUser user) {
        BigDecimal oldFee = BigDecimal.ZERO;
        BigDecimal newFee = BigDecimal.ZERO;
        // 判断新的场地费用和就的场地费用是否存在变化如果存在变化则将变换存入到历史表
        if (Objects.nonNull(oldElectricityCabinet.getPlaceFee())) {
            oldFee = oldElectricityCabinet.getPlaceFee();
        } else {
            oldFee = new BigDecimal(NumberConstant.MINUS_ONE);
        }
        
        if (Objects.nonNull(electricityCabinetAddAndUpdate.getPlaceFee())) {
            newFee = electricityCabinetAddAndUpdate.getPlaceFee();
        } else {
            newFee = new BigDecimal(NumberConstant.MINUS_ONE);
        }
        
        MerchantPlaceFeeRecord merchantPlaceFeeRecord = null;
        // 场地费有变化则进行记录
        if (!Objects.equals(newFee.compareTo(oldFee), NumberConstant.ZERO)) {
            merchantPlaceFeeRecord = new MerchantPlaceFeeRecord();
            merchantPlaceFeeRecord.setCabinetId(electricityCabinetAddAndUpdate.getId());
            if (!Objects.equals(newFee.compareTo(BigDecimal.ZERO), NumberConstant.MINUS_ONE)) {
                merchantPlaceFeeRecord.setNewPlaceFee(newFee);
            }
            if (!Objects.equals(oldFee.compareTo(BigDecimal.ZERO), NumberConstant.MINUS_ONE)) {
                merchantPlaceFeeRecord.setOldPlaceFee(oldFee);
            }
            if (Objects.nonNull(user)) {
                merchantPlaceFeeRecord.setModifyUserId(user.getUid());
                merchantPlaceFeeRecord.setTenantId(oldElectricityCabinet.getTenantId());
                long currentTimeMillis = System.currentTimeMillis();
                merchantPlaceFeeRecord.setCreateTime(currentTimeMillis);
                merchantPlaceFeeRecord.setUpdateTime(currentTimeMillis);
            }
        }
        
        return merchantPlaceFeeRecord;
    }
    
    @Override
    @Transactional
    public R delete(Integer id) {
        
        ElectricityCabinet electricityCabinet = queryByIdFromCache(id);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        // 删除数据库
        electricityCabinet.setId(id);
        electricityCabinet.setUpdateTime(System.currentTimeMillis());
        electricityCabinet.setDelFlag(ElectricityCabinet.DEL_DEL);
        electricityCabinet.setTenantId(TenantContextHolder.getTenantId());
        
        // 解绑库房
        electricityCabinet.setWarehouseId(NumberConstant.ZERO_L);
        int update = electricityCabinetMapper.updateEleById(electricityCabinet);
        
        // 删除柜机扩展参数
        electricityCabinetExtraService
                .update(ElectricityCabinetExtra.builder().eid(Long.valueOf(id)).delFlag(electricityCabinet.getDelFlag()).updateTime(electricityCabinet.getUpdateTime()).build());
        
        DbUtils.dbOperateSuccessThen(update, () -> {
            // 删除缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + id);
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_EXTRA + id);
            
            // 删除柜机GEO信息
            redisService.removeGeoMember(CacheConstant.CACHE_ELECTRICITY_CABINET_GEO + electricityCabinet.getTenantId(), electricityCabinet.getId().toString());
            
            // 删除格挡
            electricityCabinetBoxService.batchDeleteBoxByElectricityCabinetId(id);
            
            electricityCabinetServerService.logicalDeleteByEid(id);
            
            return null;
        });
        
        return R.ok();
    }
    
    @Override
    public Triple<Boolean, String, Object> physicsDelete(ElectricityCabinet electricityCabinet) {
        int delete = electricityCabinetMapper.deleteById(electricityCabinet.getId());
        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId());
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
            // 删除柜机GEO信息
            redisService.removeGeoMember(CacheConstant.CACHE_ELECTRICITY_CABINET_GEO + electricityCabinet.getTenantId(), electricityCabinet.getId().toString());
            
            // 删除电柜服务时间
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
            // 获取库房名称列表 根据库房id查询库房名称，不需要过滤库房状态是已删除的
            List<Long> warehouseIdList = electricityCabinetList.stream().map(ElectricityCabinetVO::getWarehouseId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            List<AssetWarehouseNameVO> assetWarehouseNameVOS = assetWarehouseService.selectByIdList(warehouseIdList);
            
            Map<Long, String> warehouseNameVOMap = Maps.newHashMap();
            if (!CollectionUtils.isEmpty(assetWarehouseNameVOS)) {
                warehouseNameVOMap = assetWarehouseNameVOS.stream().collect(Collectors.toMap(AssetWarehouseNameVO::getId, AssetWarehouseNameVO::getName, (item1, item2) -> item2));
            }
            
            // 查询区域
            List<Long> areaIdList = electricityCabinetList.stream().map(ElectricityCabinetVO::getAreaId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            MerchantAreaRequest areaQuery = MerchantAreaRequest.builder().idList(areaIdList).build();
            List<MerchantArea> merchantAreaList = merchantAreaService.queryList(areaQuery);
            Map<Long, String> areaNameMap = Maps.newHashMap();
            if (!CollectionUtils.isEmpty(merchantAreaList)) {
                areaNameMap = merchantAreaList.stream().collect(Collectors.toMap(MerchantArea::getId, MerchantArea::getName, (item1, item2) -> item2));
            }
            
            // 柜机cell提取
            List<Integer> idList = electricityCabinetList.stream().map(ElectricityCabinetVO::getId).collect(Collectors.toList());
            List<ElectricityCabinetBox> boxList = electricityCabinetBoxService.listCabineBoxByEids(idList);
            Map<Integer, List<ElectricityCabinetBox>> electricityCabinetBoxMap = new HashMap<>();
            if (CollUtil.isNotEmpty(boxList)) {
                electricityCabinetBoxMap = boxList.stream().filter(e -> Objects.equals(e.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE))
                        .collect(Collectors.groupingBy(ElectricityCabinetBox::getElectricityCabinetId));
            }
            
            Map<Long, String> finalWarehouseNameVOMap = warehouseNameVOMap;
            Map<Long, String> finalAreaNameMap = areaNameMap;
            Map<Integer, List<ElectricityCabinetBox>> finalElectricityCabinetBoxMap = electricityCabinetBoxMap;
            
            electricityCabinetList.parallelStream().forEach(e -> {
                
                if (Objects.nonNull(e.getStoreId())) {
                    Store store = storeService.queryByIdFromCache(Long.valueOf(e.getStoreId()));
                    e.setStoreName(Objects.isNull(store) ? "" : store.getName());
                }
                
                // 营业时间
                if (Objects.nonNull(e.getBusinessTime()) && StringUtils.isNotBlank(e.getBusinessTime())) {
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
                
                // 查找型号名称
                ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(e.getModelId());
                if (Objects.nonNull(electricityCabinetModel)) {
                    e.setModelName(electricityCabinetModel.getName());
                    
                    // 赋值复合字段
                    StringBuilder manufacturerNameAndModelName = new StringBuilder();
                    if (StringUtils.isNotBlank(electricityCabinetModel.getManufacturerName())) {
                        manufacturerNameAndModelName.append(electricityCabinetModel.getManufacturerName());
                    }
                    
                    if (StringUtils.isNotBlank(manufacturerNameAndModelName.toString())) {
                        manufacturerNameAndModelName.append(StringConstant.FORWARD_SLASH);
                    }
                    
                    if (StringUtils.isNotBlank(electricityCabinetModel.getName())) {
                        manufacturerNameAndModelName.append(electricityCabinetModel.getName());
                    }
                    e.setManufacturerNameAndModelName(manufacturerNameAndModelName.toString());
                }
                
                // 查满仓空仓数
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
                
                List<ElectricityCabinetBox> cabinetBoxList = finalElectricityCabinetBoxMap.get(e.getId());
                if (!CollectionUtils.isEmpty(cabinetBoxList)) {
                    // 空仓
                    noElectricityBattery = (int) cabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
                    // 有电池数量
                    batteryInElectricity = (int) cabinetBoxList.stream().filter(this::isBatteryInElectricity).count();
                    // 电池总数
                    electricityBatteryTotal = (int) cabinetBoxList.stream().filter(this::isElectricityBattery).count();
                    // 可换电电池数
                    fullyElectricityBattery = (int) cabinetBoxList.stream().filter(i -> isExchangeable(i, fullyCharged)).count();
                }
                
                boolean result = deviceIsOnline(e.getProductKey(), e.getDeviceName(), e.getPattern());
                
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
                
                // 是否锁住
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
                
                // 设置运营商名称
                if (Objects.nonNull(e.getFranchiseeId())) {
                    Franchisee franchisee = franchiseeService.queryByIdFromCache(e.getFranchiseeId());
                    if (Objects.nonNull(franchisee)) {
                        e.setFranchiseeName(franchisee.getName());
                    }
                }
                
                // 设置仓库名称
                if (finalWarehouseNameVOMap.containsKey(e.getWarehouseId())) {
                    e.setWarehouseName(finalWarehouseNameVOMap.get(e.getWarehouseId()));
                }
                
                // 设置区域名称
                if (finalAreaNameMap.containsKey(e.getAreaId())) {
                    e.setAreaName(finalAreaNameMap.get(e.getAreaId()));
                }
            });
        }
        
        electricityCabinetList.stream().sorted(Comparator.comparing(ElectricityCabinetVO::getCreateTime).reversed()).collect(Collectors.toList());
        return R.ok(electricityCabinetList);
    }
    
    @Override
    public Triple<Boolean, String, Object> updateOnlineStatus(Long id) {
        ElectricityCabinet electricityCabinet = this.queryByIdFromCache(id.intValue());
        if (Objects.isNull(electricityCabinet)) {
            return Triple.of(false, "100003", "柜机不存在");
        }
        
        if (!SecurityUtils.isAdmin() && !Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100003", "柜机不存在");
        }
        
        ElectricityCabinet electricityCabinetUpdate = new ElectricityCabinet();
        
        if (deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern())) {
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
        // 更新柜机GEO缓存信息
        redisService.addGeo(CacheConstant.CACHE_ELECTRICITY_CABINET_GEO + electricityCabinet.getTenantId(), electricityCabinet.getId().toString(),
                new Point(eleCabinetAddressQuery.getLongitude(), eleCabinetAddressQuery.getLatitude()));
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
        // 空仓
        long emptyCellNumber = cabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
        // 有电池仓门
        long haveBatteryNumber = cabinetBoxList.stream().filter(this::isBatteryInElectricity).count();
        // 可换电数量
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
                // 营业时间
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
                
                // 查满仓空仓数
                Integer fullyElectricityBattery = queryFullyElectricityBattery(e.getId(), "-1");
                
                // 查满仓空仓数
                int electricityBatteryTotal = 0;
                int noElectricityBattery = 0;
                List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(e.getId());
                if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
                    
                    // 空仓
                    noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
                    
                    // 电池总数
                    electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
                }
                
                e.setElectricityBatteryTotal(electricityBatteryTotal);
                e.setNoElectricityBattery(noElectricityBattery);
                e.setFullyElectricityBattery(fullyElectricityBattery);
                
                ElectricityCabinet item = new ElectricityCabinet();
                item.setUpdateTime(System.currentTimeMillis());
                item.setId(e.getId());
                
                // 电柜不在线也返回，可离线换电
                if (Objects.equals(e.getUsableStatus(), ELECTRICITY_CABINET_USABLE_STATUS)) {
                    electricityCabinets.add(e);
                }
            });
        }
        return R.ok(electricityCabinets.stream().sorted(Comparator.comparing(ElectricityCabinetVO::getDistance)).collect(Collectors.toList()));
    }
    
    /**
     * @param electricityCabinetQuery
     * @return
     */
    @Override
    public R showInfoByDistanceV2(ElectricityCabinetQuery electricityCabinetQuery) {
        Double distanceMax = Objects.isNull(eleCommonConfig.getShowDistance()) ? 50000D : eleCommonConfig.getShowDistance();
        if (Objects.isNull(electricityCabinetQuery.getDistance()) || electricityCabinetQuery.getDistance() > distanceMax) {
            electricityCabinetQuery.setDistance(distanceMax);
        }
        
        List<ElectricityCabinetSimpleVO> resultVo;
        // 若enableGeo为true，则从redis中获取位置信息。反之从数据库中查询柜机位置信息
        if (eleCommonConfig.isEnableGeo()) {
            GeoResults<RedisGeoCommands.GeoLocation<String>> geoRadius = getGeoLocationGeoResults(electricityCabinetQuery);
            if (geoRadius == null) {
                return null;
            }
            
            resultVo = geoRadius.getContent().parallelStream().map(e -> {
                ElectricityCabinetSimpleVO electricityCabinetVO = new ElectricityCabinetSimpleVO();
                
                Integer eid = Integer.valueOf(e.getContent().getName());
                ElectricityCabinet electricityCabinet = queryByIdFromCache(eid);
                if (Objects.isNull(electricityCabinet) || !Objects.equals(ELECTRICITY_CABINET_USABLE_STATUS, electricityCabinet.getUsableStatus())) {
                    return null;
                }
                electricityCabinetVO.setId(electricityCabinet.getId());
                electricityCabinetVO.setName(electricityCabinet.getName());
                electricityCabinetVO.setSn(electricityCabinet.getSn());
                electricityCabinetVO.setServicePhone(electricityCabinet.getServicePhone());
                electricityCabinetVO.setAddress(electricityCabinet.getAddress());
                electricityCabinetVO.setOnlineStatus(electricityCabinet.getOnlineStatus());
                electricityCabinetVO.setLatitude(e.getContent().getPoint().getY());
                electricityCabinetVO.setLongitude(e.getContent().getPoint().getX());
                // 将公里数转化为米，返回给前端
                electricityCabinetVO.setDistance(e.getDistance().getValue() * 1000);
                return assignAttribute(electricityCabinetVO, electricityCabinet.getFullyCharged(), electricityCabinet.getBusinessTime());
            }).filter(Objects::nonNull).collect(Collectors.toList());
            
        } else {
            List<ElectricityCabinetVO> electricityCabinetList = electricityCabinetMapper.showInfoByDistance(electricityCabinetQuery);
            if (CollectionUtils.isEmpty(electricityCabinetList)) {
                return R.ok(Collections.emptyList());
            }
            
            resultVo = electricityCabinetList.parallelStream().map(e -> {
                if (!Objects.equals(ELECTRICITY_CABINET_USABLE_STATUS, e.getUsableStatus())) {
                    return null;
                }
                ElectricityCabinetSimpleVO electricityCabinetVO = new ElectricityCabinetSimpleVO();
                electricityCabinetVO.setId(e.getId());
                electricityCabinetVO.setName(e.getName());
                electricityCabinetVO.setAddress(e.getAddress());
                electricityCabinetVO.setLongitude(e.getLongitude());
                electricityCabinetVO.setLatitude(e.getLatitude());
                electricityCabinetVO.setOnlineStatus(e.getOnlineStatus());
                electricityCabinetVO.setFullyElectricityBattery(e.getFullyElectricityBattery());
                electricityCabinetVO.setDistance(e.getDistance());
                electricityCabinetVO.setSn(e.getSn());
                electricityCabinetVO.setServicePhone(e.getServicePhone());
                return assignAttribute(electricityCabinetVO, e.getFullyCharged(), e.getBusinessTime());
            }).filter(Objects::nonNull).collect(Collectors.toList());
            
        }
        
        return R.ok(resultVo.stream().sorted(Comparator.comparing(ElectricityCabinetSimpleVO::getDistance)).collect(Collectors.toList()));
    }
    
    private GeoResults<RedisGeoCommands.GeoLocation<String>> getGeoLocationGeoResults(ElectricityCabinetQuery electricityCabinetQuery) {
        RedisGeoCommands.GeoRadiusCommandArgs geoRadiusCommandArgs = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().includeCoordinates()
                .sortAscending();
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoRadius = redisService.getGeoRadius(CacheConstant.CACHE_ELECTRICITY_CABINET_GEO + electricityCabinetQuery.getTenantId(),
                new Circle(new Point(electricityCabinetQuery.getLon(), electricityCabinetQuery.getLat()),
                        new Distance(electricityCabinetQuery.getDistance() / 1000, Metrics.KILOMETERS)), geoRadiusCommandArgs);
        if (Objects.isNull(geoRadius) || !DataUtil.collectionIsUsable(geoRadius.getContent())) {
            log.info("GEO results is null, query info = {}", electricityCabinetQuery);
            return null;
        }
        return geoRadius;
    }
    
    /**
     * @param eid
     * @param exchangeableList： 可换电标准集合
     * @param cabinetBoxList：   可用的仓数
     * @return
     */
    private List<Integer> electricityCabinetLabelHandler(Integer eid, List<ElectricityCabinetBox> exchangeableList, List<ElectricityCabinetBox> cabinetBoxList) {
        List<Integer> label = CollUtil.newArrayList();
        
        // 空仓数量
        List<ElectricityCabinetBox> emptyCellList = cabinetBoxList.stream().filter(e -> Objects.equals(e.getStatus(), STATUS_NO_ELECTRICITY_BATTERY)).collect(Collectors.toList());
        
        // 在仓电池数
        List<ElectricityCabinetBox> haveBatteryCellList = cabinetBoxList.stream().filter(e -> Objects.equals(e.getStatus(), STATUS_ELECTRICITY_BATTERY))
                .collect(Collectors.toList());
        
        // 可换电数量,可换电池不为空 && 空仓不为空
        if (CollUtil.isNotEmpty(exchangeableList) && CollUtil.isNotEmpty(emptyCellList)) {
            label.add(IS_EXCHANGE);
        }
        
        ElectricityCabinetExtra cabinetExtra = electricityCabinetExtraService.queryByEidFromCache(Long.valueOf(eid));
        if (Objects.isNull(cabinetExtra)) {
            log.warn("electricityCabinetLabelHandler/cabinetExtra is null, eid is:{}", eid);
            return label;
        }
        // 租电 前提是存在可换电标准的电池
        if ((!Objects.equals(cabinetExtra.getRentTabType(), RentReturnNormEnum.NOT_RENT.getCode())) && CollUtil.isNotEmpty(exchangeableList)) {
            // 全部可租电
            if (Objects.equals(cabinetExtra.getRentTabType(), RentReturnNormEnum.ALL_RENT.getCode())) {
                label.add(IS_RENT);
            }
            // 最少保留一块电池
            if (Objects.equals(cabinetExtra.getRentTabType(), RentReturnNormEnum.MIN_RETAIN.getCode())) {
                if (haveBatteryCellList.size() > MIN_RETAIN_BATTERY) {
                    label.add(IS_RENT);
                }
            }
            // 自定义租电
            if (Objects.equals(cabinetExtra.getRentTabType(), RentReturnNormEnum.CUSTOM_RENT.getCode())) {
                if (haveBatteryCellList.size() > cabinetExtra.getMinRetainBatteryCount()) {
                    label.add(IS_RENT);
                }
            }
        }
        
        //  退电 前提是存在空仓
        Integer returnTabType = cabinetExtra.getReturnTabType();
        if ((!Objects.equals(returnTabType, RentReturnNormEnum.NOT_RETURN.getCode())) && CollUtil.isNotEmpty(emptyCellList)) {
            // 全部可退电
            if (Objects.equals(returnTabType, RentReturnNormEnum.ALL_RETURN.getCode())) {
                label.add(IS_RETURN);
            }
            // 最少保留一块电池
            if (Objects.equals(returnTabType, RentReturnNormEnum.MIN_RETURN.getCode())) {
                if (emptyCellList.size() > MIN_RETAIN_EMPTY_CELL) {
                    label.add(IS_RETURN);
                }
            }
            // 自定义租电
            if (Objects.equals(returnTabType, RentReturnNormEnum.CUSTOM_RETURN.getCode())) {
                if (emptyCellList.size() > cabinetExtra.getMaxRetainBatteryCount()) {
                    label.add(IS_RETURN);
                }
            }
        }
        return label;
    }
    
    
    private ElectricityCabinetSimpleVO assignAttribute(ElectricityCabinetSimpleVO e, Double fullyCharged, String businessTime) {
        
        if (Objects.nonNull(e.getDistance())) {
            // 乘以10，向下取整，再除以10,保留一位小数
            e.setDistance(Math.floor(e.getDistance() * 10.0) / 10.0);
        }
        
        // 营业时间
        if (StringUtils.isNotBlank(businessTime)) {
            if (Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                e.setBusinessTimeType(ElectricityCabinetVO.ALL_DAY);
                //                e.setIsBusiness(ElectricityCabinetVO.IS_BUSINESS);
            } else {
                e.setBusinessTimeType(ElectricityCabinetVO.ILLEGAL_DATA);
                int index = businessTime.indexOf("-");
                if (!Objects.equals(index, -1) && index > 0) {
                    e.setBusinessTimeType(ElectricityCabinetVO.CUSTOMIZE_TIME);
                    Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                    Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                    e.setBeginTime(totalBeginTime);
                    e.setEndTime(totalEndTime);
                }
            }
        }
        
        // 可用的仓数
        List<ElectricityCabinetBox> cabinetBoxList = electricityCabinetBoxService.selectEleBoxAttrByEid(e.getId());
        if (CollectionUtils.isEmpty(cabinetBoxList)) {
            return e;
        }
        // 可换电数量
        List<ElectricityCabinetBox> exchangeableList = cabinetBoxList.stream().filter(item -> isExchangeable(item, fullyCharged)).collect(Collectors.toList());
        long exchangeableNumber = exchangeableList.size();
        e.setFullyElectricityBattery((int) exchangeableNumber);// 兼容2.0小程序首页显示问题
        
        // 筛选可换、可租、可退标签返回
        e.setLabel(electricityCabinetLabelHandler(e.getId(), exchangeableList, cabinetBoxList));
        return e;
    }
    
    private void assignExchangeableBatteryType(List<ElectricityCabinetBox> exchangeableList, ElectricityCabinetVO e) {
        HashMap<String, Integer> batteryTypeMap = new HashMap<>();
        exchangeableList.forEach(electricityCabinetBox -> {
            String batteryType = electricityCabinetBox.getBatteryType();
            if (Objects.nonNull(batteryType)) {
                String key = subStringButteryType(batteryType);
                if (StringUtils.equals(StringUtils.EMPTY, batteryType)) {
                    key = BatteryConstant.DEFAULT_MODEL;
                }
                if (Objects.nonNull(key)) {
                    // 统计可换电电池型号
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
        Map<String, Integer> modelCapacityMap = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(batteryList)) {
            capacityMap = batteryList.stream().collect(Collectors.toMap(ElectricityBattery::getSn, ElectricityBattery::getCapacity, (k1, k2) -> k1));
            
            // 根据batteryType获取电池型号列表
            List<String> batteryTypeList = batteryList.stream().map(ElectricityBattery::getModel).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(batteryTypeList)) {
                List<BatteryModel> modelList = batteryModelService.listBatteryModelByBatteryTypeList(batteryTypeList, TenantContextHolder.getTenantId());
                if (!CollectionUtils.isEmpty(modelList)) {
                    modelCapacityMap = modelList.stream().filter(item -> Objects.nonNull(item.getCapacity()))
                            .collect(Collectors.toMap(BatteryModel::getBatteryType, BatteryModel::getCapacity, (k1, k2) -> k1));
                }
            }
        }
        
        // 获取电池的容量
        Map<String, Integer> finalCapacityMap = capacityMap;
        Map<String, Integer> finalModelCapacityMap = modelCapacityMap;
        
        exchangeableList.forEach(electricityCabinetBox -> {
            String batteryType = electricityCabinetBox.getBatteryType();
            String sn = electricityCabinetBox.getSn();
            if (Objects.nonNull(batteryType)) {
                Integer capacity = finalModelCapacityMap.get(batteryType);
                if (Objects.isNull(capacity) || Objects.equals(NumberConstant.ZERO, capacity)) {
                    capacity = finalCapacityMap.get(sn);
                }
                
                String key = subStringVoltageAndCapacity(batteryType, capacity);
                
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
        Map<String, Integer> modelCapacityMap = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(batteryList)) {
            capacityMap = batteryList.stream().collect(Collectors.toMap(ElectricityBattery::getSn, ElectricityBattery::getCapacity, (k1, k2) -> k1));
            
            // 根据batteryType获取电池型号列表
            List<String> batteryTypeList = batteryList.stream().map(ElectricityBattery::getModel).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(batteryTypeList)) {
                List<BatteryModel> modelList = batteryModelService.listBatteryModelByBatteryTypeList(batteryTypeList, TenantContextHolder.getTenantId());
                if (!CollectionUtils.isEmpty(modelList)) {
                    modelCapacityMap = modelList.stream().filter(item -> Objects.nonNull(item.getCapacity()))
                            .collect(Collectors.toMap(BatteryModel::getBatteryType, BatteryModel::getCapacity, (k1, k2) -> k1));
                }
            }
        }
        
        // 获取电池的容量
        Map<String, Integer> finalCapacityMap = capacityMap;
        Map<String, Integer> finalModelCapacityMap = modelCapacityMap;
        exchangeableList.forEach(electricityCabinetBox -> {
            String batteryType = electricityCabinetBox.getBatteryType();
            String sn = electricityCabinetBox.getSn();
            if (Objects.nonNull(batteryType)) {
                Integer capacity = finalModelCapacityMap.get(batteryType);
                if (Objects.isNull(capacity) || Objects.equals(NumberConstant.ZERO, capacity)) {
                    capacity = finalCapacityMap.get(sn);
                }
                String key = subStringVoltageAndCapacity(batteryType, capacity);
                
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
            // 检测是否开启低电量换电并且查询到符合标准的最低换电电量标准
            Double fullyCharged = checkLowBatteryExchangeMinimumBatteryPowerStandard(tenantId, id);
            ids = electricityCabinetMapper.queryFullyElectricityBatteryForLowBatteryExchange(id, batteryType, fullyCharged);
            if (ObjectUtils.isEmpty(ids)) {
                return Triple.of(false, "0", "换电柜暂无满电电池");
            }
            for (Long item : ids) {
                // 根据电池id和加盟商id查询电池
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
    public boolean deviceIsOnlineForIot(String productKey, String deviceName) {
        
        log.info("ElectricityCabinetServiceImpl.deviceIsOnlineForIot productKey:{},deviceName:{}", productKey, deviceName);
        GetDeviceStatusResponse getDeviceStatusResponse = pubHardwareService.queryDeviceStatusFromIot(productKey, deviceName);
        if (Objects.isNull(getDeviceStatusResponse)) {
            return false;
        }
        
        GetDeviceStatusResponse.Data data = getDeviceStatusResponse.getData();
        log.info("ElectricityCabinetServiceImpl.deviceIsOnlineForIot data:{}", JsonUtil.toJson(data));
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
    public boolean deviceIsOnline(String productKey, String deviceName, Integer pattern) {
        if (Objects.equals(pattern, EleCabinetConstant.TCP_PATTERN)) {
            return deviceIsOnlineForTcp(productKey, deviceName);
        }
        
        return deviceIsOnlineForIot(productKey, deviceName);
    }
    
    /**
     * TODO zhaohzilong 2024年08月26日 15:09:09 判断设备是否在线改为调用网关接口，从返回值中获取设备连接的网关IP
     *
     * @param productKey
     * @param deviceName
     * @return
     */
    @Override
    public boolean deviceIsOnlineForTcp(String productKey, String deviceName) {
        return redisService.hasKey(CacheConstant.CACHE_CABINET_SN_ONLINE + DeviceTextUtil.assembleSn(productKey, deviceName));
    }
    
    @Override
    public Integer queryByModelId(Integer id) {
        return electricityCabinetMapper
                .selectCount(Wrappers.<ElectricityCabinet>lambdaQuery().eq(ElectricityCabinet::getModelId, id).eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
    }
    
    @Override
    @Transactional
    public R updateStatus(Integer id, Integer usableStatus) {
        if (Objects.isNull(id) || Objects.isNull(usableStatus)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 换电柜
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
        
        // 更新缓存
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET + electricityCabinet.getId(), electricityCabinet);
        
        redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + oldElectricityCabinet.getProductKey() + oldElectricityCabinet.getDeviceName());
        operateRecordUtil.record(oldElectricityCabinet, electricityCabinet);
        
        // 给第三方推送柜机信息
        pushDataToThirdService.asyncPushCabinetToThird(ThirdPartyMallEnum.MEI_TUAN_RIDER_MALL.getCode(), TtlTraceIdSupport.get(), electricityCabinet.getTenantId(),
                electricityCabinet.getId().longValue());
        
        return R.ok();
    }
    
    @Slave
    @Override
    public R homeOneV2() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        HashMap<String, String> homeOne = new HashMap<>();
        // 电柜数
        homeOne.put("eleCount", "0");
        // 在线电柜
        homeOne.put("onlineEleCount", "0");
        // 离线电柜
        homeOne.put("offlineEleCount", "0");
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("homeOneV2 franchiseeIds is empty!");
                return R.ok(homeOne);
            }
        }
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(storeIds)) {
                log.warn("homeOneV2 storeIds is empty!");
                return R.ok(homeOne);
            }
        }
        
        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().tenantId(TenantContextHolder.getTenantId()).franchiseeIdList(franchiseeIds)
                .storeIdList(storeIds).build();
        List<ElectricityCabinetVO> electricityCabinetVOList = electricityCabinetMapper.selectListForStatistics(electricityCabinetQuery);
        
        if (CollectionUtils.isEmpty(electricityCabinetVOList)) {
            log.warn("homeOneV2 electricityCabinetVOList is empty!");
            return R.ok(homeOne);
        }
        
        Integer eleCount = electricityCabinetVOList.size();
        Integer onlineEleCount = (int) electricityCabinetVOList.stream()
                .filter(item -> Objects.equals(item.getOnlineStatus(), ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS)).count();
        Integer offlineEleCount = (int) electricityCabinetVOList.stream()
                .filter(item -> Objects.equals(item.getOnlineStatus(), ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS)).count();
        
        homeOne.put("eleCount", eleCount.toString());
        homeOne.put("onlineEleCount", onlineEleCount.toString());
        homeOne.put("offlineEleCount", offlineEleCount.toString());
        
        return R.ok(homeOne);
    }
    
    @Slave
    @Override
    public List<ElectricityCabinetCountVO> queryCabinetCount(ElectricityCabinetQuery cabinetQuery) {
        
        return electricityCabinetMapper.selectCabinetCount(cabinetQuery);
    }
    
    @Override
    public void addElectricityCabinetLocToGeo(ElectricityCabinet electricityCabinet) {
        if (Objects.isNull(electricityCabinet.getLatitude()) || Objects.isNull(electricityCabinet.getLongitude())) {
            log.error("Add electricity cabinet to geo error, device's location not found! sn={}, lat={}, lon={}", electricityCabinet.getSn(), electricityCabinet.getLatitude(),
                    electricityCabinet.getLongitude());
            return;
        }
        // 缓存柜机GEO信息
        redisService.addGeo(CacheConstant.CACHE_ELECTRICITY_CABINET_GEO + TenantContextHolder.getTenantId(), electricityCabinet.getId().toString(),
                new Point(electricityCabinet.getLongitude(), electricityCabinet.getLatitude()));
    }
    
    /**
     * <p>
     * Description: queryIdsBySnArray
     * </p>
     *
     * @param snList             snList
     * @param tenantId           tenantId
     * @param sourceFranchiseeId sourceFranchiseeId
     * @return java.util.List<java.lang.Long>
     * <p>Project: ElectricityCabinetServiceImpl</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#S5pYdtn2ooNnzqxWFbxcqGownbe">12.8 资产调拨（2条优化点)</a>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/18
     */
    @Slave
    @Override
    public Map<String, Long> listIdsBySnArray(List<String> snList, Integer tenantId, Long sourceFranchiseeId) {
        List<ElectricityCabinetBO> cabinetBOS = this.electricityCabinetMapper.selectListBySnArray(snList, tenantId, sourceFranchiseeId);
        if (CollectionUtils.isEmpty(cabinetBOS)) {
            return MapUtil.empty();
        }
        return cabinetBOS.stream().collect(Collectors.toMap(ElectricityCabinetBO::getSn, e -> e.getId().longValue(), (k1, k2) -> k1));
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
        
        // 本月换电
        Integer monthCount = electricityCabinetOrderService.homeMonth(user.getUid(), firstMonth, now);
        // 总换电
        Integer totalCount = electricityCabinetOrderService.homeTotal(user.getUid());
        
        // 校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("HOME  ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        // 判断用户套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        
        ElectricityMemberCard electricityMemberCard = null;
        if (Objects.nonNull(userBatteryMemberCard) && !Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_MEMBER_CARD_ID_ZERO)) {
            electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        }
        
        if (Objects.nonNull(userBatteryMemberCard) && Objects.isNull(electricityMemberCard) && !Objects
                .equals(userBatteryMemberCard.getRemainingNumber(), UserBatteryMemberCard.SEND_REMAINING_NUMBER) && !Objects
                .equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_MEMBER_CARD_ID_ZERO)) {
            log.error("HOME ERROR! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }
        
        // 套餐剩余天数
        Double cardDay = 0.0D;
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            now = userBatteryMemberCard.getDisableMemberCardTime();
        }
        
        if (Objects.nonNull(userBatteryMemberCard) && !Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER) && Objects
                .nonNull(electricityMemberCard)) {
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
            if (Objects.nonNull(userBatteryMemberCard) && Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && Objects
                    .nonNull(userBatteryMemberCard.getRemainingNumber()) && userBatteryMemberCard.getRemainingNumber() > 0
                    && userBatteryMemberCard.getMemberCardExpireTime() > now) {
                cardDay = Math.ceil((userBatteryMemberCard.getMemberCardExpireTime() - now) / 1000 / 60 / 60 / 24.0);
            }
        }
        
        // 我的电池
        Double battery = null;
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
        if (Objects.nonNull(electricityBattery)) {
            battery = electricityBattery.getPower();
        }
        
        // 套餐到期时间
        
        String memberCardExpireTime = null;
        Integer memberCardDisableStatus = null;
        if (Objects.nonNull(userBatteryMemberCard)) {
            memberCardExpireTime = Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) ? DateUtil
                    .format(DateUtil.date(userBatteryMemberCard.getMemberCardExpireTime()), DatePattern.NORM_DATE_FORMAT) : "";
            memberCardDisableStatus = userBatteryMemberCard.getMemberCardStatus();
        }
        homeInfo.put("memberCardExpireTime", memberCardExpireTime);
        // 月卡剩余天数
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
        // 先查缓存
        ElectricityCabinet cacheElectricityCabinet = redisService.getWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + productKey + deviceName, ElectricityCabinet.class);
        if (Objects.nonNull(cacheElectricityCabinet)) {
            return cacheElectricityCabinet;
        }
        
        // 缓存没有再查数据库
        ElectricityCabinet electricityCabinet = electricityCabinetMapper.selectOne(
                new LambdaQueryWrapper<ElectricityCabinet>().eq(ElectricityCabinet::getProductKey, productKey).eq(ElectricityCabinet::getDeviceName, deviceName)
                        .eq(ElectricityCabinet::getDelFlag, ElectricityCabinet.DEL_NORMAL));
        if (Objects.isNull(electricityCabinet)) {
            return null;
        }
        
        // 放入缓存
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
        // 不合法的参数
        if (Objects.isNull(eleOuterCommandQuery.getCommand()) || Objects.isNull(eleOuterCommandQuery.getDeviceName()) || Objects.isNull(eleOuterCommandQuery.getProductKey())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        eleOuterCommandQuery.setSessionId(sessionId);
        
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(eleOuterCommandQuery.getProductKey(), eleOuterCommandQuery.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        // 换电柜是否在线
        boolean eleResult = deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        if (!eleResult) {
            return R.fail("100004", "柜机不在线");
        }
        
        if (!ElectricityIotConstant.isLegalCommand(eleOuterCommandQuery.getCommand())) {
            return R.fail("ELECTRICITY.0036", "不合法的命令");
        }
        
        Map<String, Object> dataMap = null;
        if (CollectionUtils.isEmpty(eleOuterCommandQuery.getData())) {
            dataMap = Maps.newHashMap();
        } else {
            dataMap = eleOuterCommandQuery.getData();
        }
        
        try {
            // 校验高温散热参数
            if (dataMap.containsKey(OPEN_FAN_CONDITION_KEY)) {
                String fanStr = (String) dataMap.get(OPEN_FAN_CONDITION_KEY);
                int fan = Integer.parseInt(fanStr);
                if (fan < -50) {
                    fan = -50;
                } else if (fan > 150) {
                    fan = 150;
                }
                dataMap.put(OPEN_FAN_CONDITION_KEY, fan);
            }
            
            // 校验低温加热参数
            if (dataMap.containsKey(OPEN_HEAT_CONDITION_KEY)) {
                String heatStr = (String) dataMap.get(OPEN_HEAT_CONDITION_KEY);
                int heat = Integer.parseInt(heatStr);
                if (heat < -50) {
                    heat = -50;
                } else if (heat > 50) {
                    heat = 50;
                }
                dataMap.put(OPEN_HEAT_CONDITION_KEY, heat);
            }
        } catch (Exception e) {
            log.warn("openFanCondition or openHeatCondition check fail", e);
        }
        
        dataMap.put("uid", SecurityUtils.getUid());
        dataMap.put("username", SecurityUtils.getUserInfo().getUsername());
        eleOuterCommandQuery.setData(dataMap);
        
        // 开全部门 -->  cell_all_open_door
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
        
        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        // 发送命令失败
        if (!result.getLeft()) {
            return R.fail("ELECTRICITY.0037", "发送命令失败");
        }
        Map<String, Object> map = BeanUtil.beanToMap(comm, false, true);
        map.put("operateType", 1);
        map.put("deviceName", StringUtils.isBlank(electricityCabinet.getName()) ? electricityCabinet.getDeviceName() : electricityCabinet.getName());
        if (comm.getCommand().equals(ELE_COMMAND_CELL_UPDATE) && eleOuterCommandQuery.getData().containsKey("lockReason")) {
            map.put("command", "cell_update_down");
        }
        operateRecordUtil.record(null, map);
        return R.ok(sessionId);
    }
    
    @Override
    public R sendCommand(EleOuterCommandQuery eleOuterCommandQuery) {
        // 不合法的参数
        if (Objects.isNull(eleOuterCommandQuery.getCommand()) || Objects.isNull(eleOuterCommandQuery.getDeviceName()) || Objects.isNull(eleOuterCommandQuery.getProductKey())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
    
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        eleOuterCommandQuery.setSessionId(sessionId);
    
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(eleOuterCommandQuery.getProductKey(), eleOuterCommandQuery.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
    
        // 换电柜是否在线
        boolean eleResult = deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        if (!eleResult) {
            return R.fail("100004", "柜机不在线");
        }
    
        if (!ElectricityIotConstant.isLegalCommand(eleOuterCommandQuery.getCommand())) {
            return R.fail("ELECTRICITY.0036", "不合法的命令");
        }
    
        Map<String, Object> dataMap = null;
        if (CollectionUtils.isEmpty(eleOuterCommandQuery.getData())) {
            dataMap = Maps.newHashMap();
        } else {
            dataMap = eleOuterCommandQuery.getData();
        }
    
        dataMap.put("uid", NumberConstant.ONE_L);
        dataMap.put("username", "admin");
        eleOuterCommandQuery.setData(dataMap);
    
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(eleOuterCommandQuery.getSessionId()).data(eleOuterCommandQuery.getData())
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(eleOuterCommandQuery.getCommand()).build();
    
        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        // 发送命令失败
        if (!result.getLeft()) {
            return R.fail("ELECTRICITY.0037", "发送命令失败");
        }
        Map<String, Object> map = BeanUtil.beanToMap(comm, false, true);
        map.put("operateType", 1);
        map.put("deviceName", StringUtils.isBlank(electricityCabinet.getName()) ? electricityCabinet.getDeviceName() : electricityCabinet.getName());
        if (comm.getCommand().equals(ELE_COMMAND_CELL_UPDATE) && eleOuterCommandQuery.getData().containsKey("lockReason")) {
            map.put("command", "cell_update_down");
        }
//        operateRecordUtil.record(null, map);
        return R.ok(sessionId);
    }
    
    @Override
    public String acquireDeviceBindServerIp(String productKey, String deviceName) {
        return redisService.get(CacheConstant.CACHE_CABINET_SN_ONLINE + DeviceTextUtil.assembleSn(productKey, deviceName));
    }
    
    @Override
    public R sendCommandToEleForOuterSuper(EleOuterCommandQuery eleOuterCommandQuery) {
        // 不合法的参数
        if (Objects.isNull(eleOuterCommandQuery.getCommand()) || Objects.isNull(eleOuterCommandQuery.getDeviceName()) || Objects.isNull(eleOuterCommandQuery.getProductKey())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        eleOuterCommandQuery.setSessionId(sessionId);
        
        ElectricityCabinet electricityCabinet = queryByProductAndDeviceName(eleOuterCommandQuery.getProductKey(), eleOuterCommandQuery.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        // 不合法的命令
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
        
        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        // 发送命令失败
        if (!result.getLeft()) {
            return R.fail("ELECTRICITY.0037", "发送命令失败");
        }
        Map<String, Object> map = BeanUtil.beanToMap(comm, false, true);
        map.put("operateType", 1);
        map.put("deviceName", StringUtils.isBlank(electricityCabinet.getName()) ? electricityCabinet.getDeviceName() : electricityCabinet.getName());
        if (comm.getCommand().equals(ELE_COMMAND_CELL_UPDATE) && eleOuterCommandQuery.getData().containsKey("lockReason")) {
            map.put("command", "cell_update_down");
        }
        operateRecordUtil.record(null, map);
        return R.ok(sessionId);
    }
    
    @Override
    public R queryByDeviceOuter(String productKey, String deviceName) {
        ElectricityCabinet electricityCabinet = queryByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        // 营业时间
        boolean result = this.isBusiness(electricityCabinet);
        if (result) {
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }
        
        ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
        BeanUtil.copyProperties(electricityCabinet, electricityCabinetVO);
        
        // 查满仓空仓数
        int fullyElectricityBattery = queryFullyElectricityBattery(electricityCabinet.getId(), null);
        // 查满仓空仓数
        int electricityBatteryTotal = 0;
        int noElectricityBattery = 0;
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinetVO.getId());
        if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
            // 空仓
            noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
            // 电池总数
            electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
        }
        
        // 换电柜名称换成平台名称
        String name = null;
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinet.getTenantId());
        if (Objects.nonNull(electricityConfig)) {
            name = electricityConfig.getName();
        }
        
        // 租户code
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
                // 营业时间
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
                
                // 获取柜机可换电数和空仓数
                List<ElectricityCabinetBox> electricityCabinetBoxes = electricityCabinetBoxService.queryAllBoxByElectricityCabinetId(e.getId());
                if (!CollectionUtils.isEmpty(electricityCabinetBoxes)) {
                    
                    ElectricityCabinet eleCabinet = this.queryByIdFromCache(e.getId());
                    if (Objects.isNull(eleCabinet)) {
                        return;
                    }
                    
                    // 空仓
                    Long emptyCellNumber = electricityCabinetBoxes.stream().filter(this::isNoElectricityBattery).count();
                    // 有电池仓门
                    Long haveBatteryNumber = electricityCabinetBoxes.stream().filter(this::isBatteryInElectricity).count();
                    // 可换电数量
                    Long exchangeableNumber = electricityCabinetBoxes.stream().filter(item -> isExchangeable(item, eleCabinet.getFullyCharged())).count();
                    
                    e.setNoElectricityBattery(emptyCellNumber.intValue());
                    e.setFullyElectricityBattery(exchangeableNumber.intValue());
                    e.setElectricityBatteryTotal(haveBatteryNumber.intValue());
                }
                
                // 电柜不在线也返回，可离线换电
                if (Objects.equals(e.getUsableStatus(), ELECTRICITY_CABINET_USABLE_STATUS)) {
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
        
        // 换电柜
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.warn("queryByDevice WARN! not found electricityCabinet ！productKey{},deviceName{}", productKey, deviceName);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        ElectricityCabinetVO electricityCabinetVO = new ElectricityCabinetVO();
        BeanUtils.copyProperties(electricityCabinet, electricityCabinetVO);
        
        Franchisee franchisee = (Franchisee) franchiseeService.queryByCabinetId(electricityCabinet.getId(), electricityCabinet.getTenantId()).getData();
        if (Objects.nonNull(franchisee)) {
            electricityCabinetVO.setFranchiseeName(franchisee.getName());
            electricityCabinetVO.setFranchiseeId(franchisee.getId());
        }
        
        if (deviceIsOnline(productKey, deviceName, electricityCabinet.getPattern())) {
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
        // 营业时间
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
    public R queryByRentBattery(String productKey, String deviceName) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("queryByRentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //        Integer tenantId = TenantContextHolder.getTenantId();
        //        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        //        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getAllowRentEle(), ElectricityConfig.NOT_ALLOW_RENT_ELE)) {
        //            return R.fail("ELECTRICITY.100271", "当前柜机不支持租电");
        //        }
        
        // 是否存在未完成的租电池订单
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(user.getUid());
        if (Objects.nonNull(rentBatteryOrder)) {
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "ELECTRICITY.0013", "存在未完成租电订单，不能下单");
            } else if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "ELECTRICITY.0095", "存在未完成还电订单，不能下单");
            }
        }
        
        // 是否存在未完成的换电订单
        ElectricityCabinetOrder oldElectricityCabinetOrder = electricityCabinetOrderService.queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "ELECTRICITY.0094", "存在未完成换电订单，不能下单");
        }
        
        // 换电柜
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        // 动态查询在线状态
        boolean eleResult = deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        if (!eleResult) {
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }
        
        // 换电柜是否出现异常被锁住
        String isLock = redisService.get(CacheConstant.UNLOCK_CABINET_CACHE + electricityCabinet.getId());
        if (StringUtils.isNotEmpty(isLock)) {
            log.warn("queryByRentBattery  WARN!  electricityCabinet is lock ！electricityCabinet={}", electricityCabinet);
            return R.fail("ELECTRICITY.0063", "换电柜出现异常，暂时不能下单");
        }
        
        // 营业时间
        boolean result = this.isBusiness(electricityCabinet);
        if (result) {
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }
        
        // 查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            log.warn("queryByDevice  WARN! not found store ！electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.warn("queryByDevice  WARN! not found store ！storeId{}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        
        // 查找门店加盟商
        if (Objects.isNull(store.getFranchiseeId())) {
            log.warn("queryByDevice  WARN! not found Franchisee ！storeId={}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }
        
        // 用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("queryByRentBattery  WARN! not found user!uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        // 用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("queryByRentBattery  WARN! user is unUsable! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        // 未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("queryByRentBattery  WARN! USER not auth,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        
        // 判断该换电柜加盟商和用户加盟商是否一致
        if (!mutualExchangeService.isSatisfyFranchiseeMutualExchange(userInfo.getTenantId(), userInfo.getFranchiseeId(), store.getFranchiseeId())) {
            log.warn("queryByDevice  WARN!FranchiseeId is not equal!uid={} , FranchiseeId1={} ,FranchiseeId2={}", user.getUid(), userInfo.getFranchiseeId(),
                    store.getFranchiseeId());
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
            log.warn("ELE MEMBERCARD WARN! not found franchisee,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0038", "加盟商不存在");
        }
        
        // 组装数据
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
        // 电池电量上报变化在百分之50以上，不更新电池电量
        Double power = batteryReportQuery.getPower();
        // 修改电池
        ElectricityBattery newElectricityBattery = new ElectricityBattery();
        newElectricityBattery.setId(electricityBattery.getId());
        
        //        BatteryGeo batteryGeo = new BatteryGeo();
        //        batteryGeo.setSn(electricityBattery.getSn());
        //        batteryGeo.setCreateTime(System.currentTimeMillis());
        //        batteryGeo.setUpdateTime(System.currentTimeMillis());
        //        batteryGeo.setTenantId(electricityBattery.getTenantId());
        //        batteryGeo.setFranchiseeId(electricityBattery.getFranchiseeId());
        
        if (Objects.nonNull(power)) {
            newElectricityBattery.setPower(power);
        }
        
        Double latitude = batteryReportQuery.getLatitude();
        if (Objects.nonNull(latitude)) {
            //            batteryGeo.setLatitude(latitude);
            newElectricityBattery.setLatitude(latitude);
        }
        
        Double longitude = batteryReportQuery.getLongitude();
        if (Objects.nonNull(longitude)) {
            //            batteryGeo.setLongitude(longitude);
            newElectricityBattery.setLongitude(longitude);
        }
        electricityBattery.setUpdateTime(System.currentTimeMillis());
        newElectricityBattery.setTenantId(electricityBattery.getTenantId());
        newElectricityBattery.setUpdateTime(System.currentTimeMillis());
        electricityBatteryService.update(newElectricityBattery);
        
        //        if (Objects.nonNull(batteryGeo.getLatitude()) && Objects.nonNull(batteryGeo.getLongitude())) {
        //            batteryGeoService.insertOrUpdate(batteryGeo);
        //        }
        
        // 电池上报是否有其他信息,只处理电量
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
        return Objects.equals(electricityCabinetBox.getStatus(), STATUS_NO_ELECTRICITY_BATTERY);
    }
    
    @Override
    public boolean isBatteryInElectricity(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), STATUS_ELECTRICITY_BATTERY);
    }
    
    private boolean isElectricityBattery(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.equals(electricityCabinetBox.getStatus(), STATUS_ELECTRICITY_BATTERY);
    }
    
    @Override
    public boolean isExchangeable(ElectricityCabinetBox electricityCabinetBox, Double fullyCharged) {
        return Objects.nonNull(electricityCabinetBox.getPower()) && Objects.nonNull(fullyCharged) && electricityCabinetBox.getPower() >= fullyCharged && StringUtils
                .isNotBlank(electricityCabinetBox.getSn()) && !StringUtils.startsWithIgnoreCase(electricityCabinetBox.getSn(), "UNKNOW");
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
        // 营业时间
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
        // 换电柜
        ElectricityCabinet electricityCabinet = queryByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.warn("checkBattery warn! no electricityCabinet,productKey={},deviceName={}", productKey, deviceName);
            return R.failMsg("未找到换电柜");
        }
        
        // 电池
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(batterySn);
        if (Objects.isNull(electricityBattery)) {
            log.warn("checkBattery warn! no electricityBattery,sn={}", batterySn);
            return R.failMsg("未找到电池");
        }
        
        if (!Objects.equals(electricityCabinet.getTenantId(), electricityBattery.getTenantId())) {
            log.warn("checkBattery warn! tenantId is not equal,tenantId1={},tenantId2={}", electricityCabinet.getTenantId(), electricityBattery.getTenantId());
            return R.failMsg("电池与换电柜租户不匹配");
        }

        // 查电池所属加盟商
        if (Objects.isNull(electricityBattery.getFranchiseeId())) {
            log.warn("checkBattery warn! battery not bind franchisee,electricityBatteryId={}", electricityBattery.getId());
            return R.failMsg("电池未绑定加盟商");
        }
        // 查换电柜所属加盟商
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.warn("checkBattery warn! not find store,storeId={}", electricityCabinet.getStoreId());
            return R.failMsg("找不到换电柜门店");
        }
        
        if (!mutualExchangeService.isSatisfyFranchiseeMutualExchange(electricityCabinet.getTenantId(), store.getFranchiseeId(), electricityBattery.getFranchiseeId())) {
            log.warn("checkBattery warn! franchisee is not equal, eid is {}, franchiseeId1 is {}, franchiseeId2 is {}", electricityCabinet.getId(), store.getFranchiseeId(),
                    electricityBattery.getFranchiseeId());
            return R.failMsg("电池加盟商与电柜加盟商不匹配");
        }

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
        // 营业时间
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
        
        // 查找型号名称
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(electricityCabinetVO.getModelId());
        if (Objects.nonNull(electricityCabinetModel)) {
            electricityCabinetVO.setModelName(electricityCabinetModel.getName());
        }
        
        // 查满仓空仓数
        Integer fullyElectricityBattery = queryFullyElectricityBattery(electricityCabinetVO.getId(), null);
        int electricityBatteryTotal = 0;
        int noElectricityBattery = 0;
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinetVO.getId());
        if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
            
            // 空仓
            noElectricityBattery = (int) electricityCabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
            
            // 电池总数
            electricityBatteryTotal = (int) electricityCabinetBoxList.stream().filter(this::isElectricityBattery).count();
        }
        
        electricityCabinetVO.setOnlineStatus(electricityCabinet.getOnlineStatus());
        electricityCabinetVO.setElectricityBatteryTotal(electricityBatteryTotal);
        electricityCabinetVO.setNoElectricityBattery(noElectricityBattery);
        electricityCabinetVO.setFullyElectricityBattery(fullyElectricityBattery);
        
        // 是否锁住
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
    public Triple<Boolean, String, Object> findUsableBatteryCellNoV3(Integer eid, Franchisee franchisee, Double fullyCharged, ElectricityBattery electricityBattery, Long uid,
                                                                     Integer flexibleRenewalType, Set<Long> mutualFranchiseeSet) {

        Integer tenantId = TenantContextHolder.getTenantId();
        // 有锂换电大部分走选仓换电，少部分正常换电这里特殊处理
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
            
            // 把本柜机加盟商的绑定电池信息拿出来
            electricityBatteries = electricityBatteries.stream().filter(e -> mutualFranchiseeSet.contains(e.getFranchiseeId())).collect(Collectors.toList());
            if (!DataUtil.collectionIsUsable(electricityBatteries)) {
                log.warn("SpecialTenantId EXCHANGE WARN!battery not bind franchisee,eid={}，mutualFranchiseeSet is {}", eid,
                        CollUtil.isEmpty(mutualFranchiseeSet) ? "null" : JsonUtil.toJson(mutualFranchiseeSet));
                return Triple.of(false, "100219", "您的加盟商与电池加盟商不匹配，请更换柜机或联系客服处理。");
            }
            
            // 获取全部可用电池id
            List<Long> bindingBatteryIds = electricityBatteries.stream().map(ElectricityBattery::getId).collect(Collectors.toList());
            
            // 把加盟商绑定的电池过滤出来
            usableBatteryCellNos = usableBatteryCellNos.stream().filter(e -> bindingBatteryIds.contains(e.getBId())).collect(Collectors.toList());
            
            // 获取用户绑定的型号
            List<String> userBatteryTypes = userBatteryTypeService.selectByUid(uid);
            if (!CollectionUtils.isEmpty(userBatteryTypes)) {
                usableBatteryCellNos = usableBatteryCellNos.stream().filter(e -> StringUtils.isNotBlank(e.getBatteryType()) && userBatteryTypes.contains(e.getBatteryType()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(usableBatteryCellNos)) {
                    return Triple.of(false, "100217", "换电柜暂无可用型号的满电电池");
                }
                
                // 电量最大的
//                Double maxPower = usableBatteryCellNos.get(0).getPower();
//                usableBatteryCellNos = usableBatteryCellNos.stream().filter(item -> Objects.equals(item.getPower(), maxPower)).collect(Collectors.toList());
//                if (usableBatteryCellNos.size() == 1) {
//                    return Triple.of(true, null, usableBatteryCellNos.get(0));
//                }
            }
            
            usableBatteryCellNos = usableBatteryCellNos.stream().filter(item -> StringUtils.isNotBlank(item.getSn()) && Objects.nonNull(item.getPower()))
                    .sorted(Comparator.comparing(ElectricityCabinetBox::getPower).reversed()).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(usableBatteryCellNos)) {
                return Triple.of(false, "", "换电柜暂无满电电池");
            }
            
            // 过滤异常满电仓门
            Pair<Boolean, List<ElectricityCabinetBox>> filterFullExceptionCellPair = exceptionHandlerService.filterFullExceptionCell(usableBatteryCellNos);
            if (filterFullExceptionCellPair.getLeft()) {
                return commonGetFullCell(uid, eid, filterFullExceptionCellPair.getRight());
            }
            usableBatteryCellNos = filterFullExceptionCellPair.getRight();
            
            
            // 舒适换电分配满电仓适配
            Pair<Boolean, ElectricityCabinetBox> comfortExchangeGetFullCellPair = chooseCellConfigService.comfortExchangeGetFullCell(uid, usableBatteryCellNos, fullyCharged);
            if (comfortExchangeGetFullCellPair.getLeft()) {
                return Triple.of(true, null, comfortExchangeGetFullCellPair.getRight());
            }
            
            
            return commonGetFullCell(uid, eid, usableBatteryCellNos);
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
            
            // 把本柜机加盟商的绑定电池信息拿出来
            electricityBatteries = electricityBatteries.stream().filter(e -> mutualFranchiseeSet.contains(e.getFranchiseeId())).collect(Collectors.toList());
            if (!DataUtil.collectionIsUsable(electricityBatteries)) {
                log.warn("EXCHANGE WARN!battery not bind franchisee,eid={}，mutualFranchiseeSet is {}", eid,
                        CollUtil.isEmpty(mutualFranchiseeSet) ? "null" : JsonUtil.toJson(mutualFranchiseeSet));
                return Triple.of(false, "100219", "您的加盟商与电池加盟商不匹配，请更换柜机或联系客服处理。");
            }
            
            // 获取全部可用电池id
            List<Long> bindingBatteryIds = electricityBatteries.stream().map(ElectricityBattery::getId).collect(Collectors.toList());
            
            // 把加盟商绑定的电池过滤出来
            usableBatteryCellNos = usableBatteryCellNos.stream().filter(e -> bindingBatteryIds.contains(e.getBId())).collect(Collectors.toList());
            
            // 多型号满电电池分配规则：优先分配当前用户绑定电池型号的电池，没有则分配电量最大的   若存在多个电量最大的，则分配用户绑定电池型号串数最大的电池
            if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                if (Objects.nonNull(electricityBattery) && !Objects.equals(flexibleRenewalType, FlexibleRenewalEnum.EXCHANGE_BATTERY.getCode())) {
                    log.info("FIND USABLE BATTERY CELL NO! use old logic, flexibleRenewalType={}, uid={}.", flexibleRenewalType, uid);
                    // 灵活续费类型不为换电时，使用原逻辑获取满电仓
                    // 用户当前绑定电池的型号
                    String userCurrentBatteryType = electricityBattery.getModel();
                    List<ElectricityCabinetBox> userCurrentBatteryUsableBatteryCellNos = usableBatteryCellNos.stream()
                            .filter(e -> StrUtil.equalsIgnoreCase(e.getBatteryType(), userCurrentBatteryType)).collect(Collectors.toList());
                    
                    if (CollectionUtils.isEmpty(userCurrentBatteryUsableBatteryCellNos)) {
                        // 获取用户绑定的型号
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
                    // 灵活续费类型为换电时，获取用户绑定的型号，根据用户当前电池取新仓门的时候，会导致电池无法转换
                    log.info("FIND USABLE BATTERY CELL NO! flexibleRenewalType={}, uid={}.", flexibleRenewalType, uid);

                    // 获取用户绑定的型号
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
            
            // 过滤异常满电仓门
            Pair<Boolean, List<ElectricityCabinetBox>> filterFullExceptionCellPair = exceptionHandlerService.filterFullExceptionCell(usableBatteryCellNos);
            if (filterFullExceptionCellPair.getLeft()) {
                return commonGetFullCell(uid, eid, filterFullExceptionCellPair.getRight());
            }
            usableBatteryCellNos = filterFullExceptionCellPair.getRight();
            
            
            // 舒适换电分配满电仓适配
            Pair<Boolean, ElectricityCabinetBox> comfortExchangeGetFullCellPair = chooseCellConfigService.comfortExchangeGetFullCell(uid, usableBatteryCellNos, fullyCharged);
            if (comfortExchangeGetFullCellPair.getLeft()) {
                return Triple.of(true, null, comfortExchangeGetFullCellPair.getRight());
            }
            
            return commonGetFullCell(uid, eid, usableBatteryCellNos);
        }
    }
    
    private static Triple<Boolean, String, Object> commonGetFullCell(Long uid, Integer eid, List<ElectricityCabinetBox> usableBatteryCellNos) {
        log.info("EXCHANGE INFO! commonGetFullCell.uid is {}, eid is {}", uid, eid);
        
        Double maxPower = usableBatteryCellNos.get(0).getPower();
        usableBatteryCellNos = usableBatteryCellNos.stream().filter(item -> Objects.equals(item.getPower(), maxPower)).collect(Collectors.toList());
        if (usableBatteryCellNos.size() == 1) {
            return Triple.of(true, null, usableBatteryCellNos.get(0));
        }
        
        // 如果存在多个电量相同的格挡，取充电器电压最大
        ElectricityCabinetBox usableCabinetBox = usableBatteryCellNos.stream().filter(item -> Objects.nonNull(item.getChargeV()))
                .sorted(Comparator.comparing(ElectricityCabinetBox::getChargeV)).reduce((first, second) -> second).orElse(null);
        if (Objects.isNull(usableCabinetBox)) {
            log.warn("EXCHANGE WARN!nou found full battery,eid={}", eid);
            return Triple.of(false, "100216", "换电柜暂无满电电池");
        }
        
        return Triple.of(true, null, usableCabinetBox);
    }

    
    @Override
    public Pair<Boolean, Integer> findUsableEmptyCellNoV2(Long uid, Integer eid, String version) {
        
        // 旧版本仍走旧分配逻辑
        if (StringUtils.isNotBlank(version) && VersionUtil.compareVersion(ELE_CABINET_VERSION, version) > 0) {
            return this.findUsableEmptyCellNo(eid);
        }
        
        Integer cellNo = null;
        List<ElectricityCabinetBox> emptyCellList = electricityCabinetBoxService.listUsableEmptyCell(eid);
        if (CollectionUtils.isEmpty(emptyCellList)) {
            return Pair.of(false, null);
        }
        
        // 可用格挡只有一个默认直接分配
        if (emptyCellList.size() == 1) {
            cellNo = Integer.valueOf(emptyCellList.get(0).getCellNo());
            return Pair.of(true, cellNo);
        }
        
        // 过滤异常的仓内号
        Pair<Boolean, List<ElectricityCabinetBox>> filterEmptyExchangeCellPair = exceptionHandlerService.filterEmptyExceptionCell(eid, emptyCellList);
        // 没有正常仓门，随机获取异常仓门
        if (filterEmptyExchangeCellPair.getLeft()) {
            return Pair.of(true,
                    Integer.parseInt(filterEmptyExchangeCellPair.getRight().get(ThreadLocalRandom.current().nextInt(filterEmptyExchangeCellPair.getRight().size())).getCellNo()));
        }
        emptyCellList = filterEmptyExchangeCellPair.getRight();
        
        
        // 舒适换电分配空仓
        Pair<Boolean, Integer> comfortExchangeGetEmptyCellPair = chooseCellConfigService.comfortExchangeGetEmptyCell(uid, emptyCellList);
        if (comfortExchangeGetEmptyCellPair.getLeft()) {
            return comfortExchangeGetEmptyCellPair;
        }
        
        // 有多个空格挡  优先分配开门的格挡
        List<ElectricityCabinetBox> openDoorEmptyCellList = emptyCellList.stream().filter(item -> Objects.equals(item.getIsLock(), ElectricityCabinetBox.OPEN_DOOR))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(openDoorEmptyCellList)) {
            cellNo = Integer.parseInt(openDoorEmptyCellList.get(ThreadLocalRandom.current().nextInt(openDoorEmptyCellList.size())).getCellNo());
            return Pair.of(true, cellNo);
        }
        
        cellNo = Integer.parseInt(emptyCellList.get(ThreadLocalRandom.current().nextInt(emptyCellList.size())).getCellNo());
        return Pair.of(true, cellNo);
    }
    
    @Override
    public Pair<Boolean, Integer> findUsableEmptyCellNo(Integer eid) {
        List<FreeCellNoQuery> electricityCabinetBoxes = electricityCabinetBoxService.findUsableEmptyCellNo(eid);
        if (!DataUtil.collectionIsUsable(electricityCabinetBoxes)) {
            return Pair.of(false, null);
        }
        
        // 本次分配的格挡
        Integer allocationCellNo = null;
        
        try {
            // 可用格挡只有一个默认直接分配
            if (electricityCabinetBoxes.size() == 1) {
                allocationCellNo = Integer.valueOf(electricityCabinetBoxes.get(0).getCellNo());
                return Pair.of(true, allocationCellNo);
            }
            
            // 可使用格挡
            List<Integer> usableEmptyCellNos = electricityCabinetBoxes.parallelStream().map(FreeCellNoQuery::getCellNo).map(Integer::valueOf).collect(Collectors.toList());
            // 上次分配格档
            String cacheDistributionCell = redisService.get(CacheConstant.CACHE_DISTRIBUTION_CELL + eid);
            Integer occupyCellNo = null;
            if (StrUtil.isNotBlank(cacheDistributionCell)) {
                occupyCellNo = Integer.valueOf(cacheDistributionCell);
            }
            // 可分配（可使用 - 已分配）格挡
            List<Integer> distributableEmptyCellNos = new ArrayList<>(usableEmptyCellNos);
            distributableEmptyCellNos.remove(occupyCellNo);
            
            // 可分配格挡只有一个默认直接分配
            if (distributableEmptyCellNos.size() == 1) {
                allocationCellNo = distributableEmptyCellNos.get(0);
                return Pair.of(true, allocationCellNo);
            }
            
            // 分配上一次取出的格挡
            allocationCellNo = preTakeCellAllocation(distributableEmptyCellNos, eid);
            if (Objects.nonNull(allocationCellNo)) {
                return Pair.of(true, allocationCellNo);
            }
            
            // 分配空闲时间最大的格挡
            allocationCellNo = freeTimeMaxCellAllocation(electricityCabinetBoxes, distributableEmptyCellNos);
            if (Objects.nonNull(allocationCellNo)) {
                return Pair.of(true, allocationCellNo);
            }
            
            // 随机分配格挡,
            allocationCellNo = distributableEmptyCellNos.get(ThreadLocalRandom.current().nextInt(electricityCabinetBoxes.size()));
            // 随机分配格挡distributableEmptyCellNos不会为空，严谨加上判空
            if (Objects.nonNull(allocationCellNo)) {
                return Pair.of(true, allocationCellNo);
            }
            
            return Pair.of(false, null);
        } finally {
            // 只记录本次分配过得格挡，
            // 假设这次分配出去的是空闲时间最大格挡，说明被取走的格挡不会被分配，
            // 下次将空闲时间最大格挡在可分配格挡删除，则会走到随机分配
            if (Objects.nonNull(allocationCellNo)) {
                redisService.set(CacheConstant.CACHE_DISTRIBUTION_CELL + eid, String.valueOf(allocationCellNo), 3L, TimeUnit.MINUTES);
            }
        }
    }
    
    private Integer freeTimeMaxCellAllocation(List<FreeCellNoQuery> electricityCabinetBoxes, List<Integer> distributableEmptyCellNos) {
        List<FreeCellNoQuery> freeTimeCells = electricityCabinetBoxes.parallelStream().filter(item -> Objects.nonNull(item.getEmptyGridStartTime()))
                .filter(item -> distributableEmptyCellNos.contains(Integer.valueOf(item.getCellNo()))).sorted(Comparator.comparing(FreeCellNoQuery::getEmptyGridStartTime))
                .collect(Collectors.toList());
        // 如果空闲格挡为空或格挡为空
        if (CollectionUtils.isEmpty(freeTimeCells) || StrUtil.isBlank(freeTimeCells.get(0).getCellNo())) {
            return null;
        }
        
        Integer cellNo = Integer.valueOf(freeTimeCells.get(0).getCellNo());
        
        // 可分配格挡中不存在
        if (!distributableEmptyCellNos.contains(cellNo)) {
            return null;
        }
        return cellNo;
    }
    
    private Integer preTakeCellAllocation(List<Integer> distributableEmptyCellNos, Integer eid) {
        String preTakeCell = redisService.get(CacheConstant.CACHE_PRE_TAKE_CELL + eid);
        // 上一次取出格挡不存在
        if (StrUtil.isBlank(preTakeCell)) {
            return null;
        }
        
        // 可分配格挡中不存在
        Integer cellNo = Integer.valueOf(preTakeCell);
        if (!distributableEmptyCellNos.contains(cellNo)) {
            return null;
        }
        
        return cellNo;
    }
    
    @Override
    public R getFranchisee(String productKey, String deviceName) {
        // 换电柜
        ElectricityCabinet electricityCabinet = queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.error("getFranchisee  ERROR! not found electricityCabinet,productKey={},deviceName={}", productKey, deviceName);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        // 查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            log.error("getFranchisee  ERROR! not found store,electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.error("getFranchisee  ERROR! not found store,storeId={}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        
        // 查找门店加盟商
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
        
        DeviceStatusQuery deviceStatusQuery = JsonUtil.fromJson(apiRequestQuery.getData(), DeviceStatusQuery.class);
        if (Objects.isNull(deviceStatusQuery)) {
            return R.fail("SYSTEM.0003", "参数不合法");
        }
        
        String productKey = deviceStatusQuery.getProductKey();
        String deviceName = deviceStatusQuery.getDeviceName();
        Integer iotConnectMode = deviceStatusQuery.getIotConnectMode();
        
        if (StringUtils.isBlank(productKey) || StringUtils.isBlank(deviceName)) {
            return R.fail("SYSTEM.0003", "参数不合法");
        }
        
        if (Objects.equals(EleCabinetConstant.TCP_PATTERN, iotConnectMode)) {
            EleDeviceCode deviceCode = eleDeviceCodeService.queryBySnFromCache(productKey, deviceName);
            if (Objects.isNull(deviceCode)) {
                log.warn("checkDevice warn! not found deviceCode,p={},d={}", productKey, deviceName);
                return R.fail("CUPBOARD.10035", "iot链接失败，请联系管理员");
            }
            
            if (this.deviceIsOnlineForTcp(productKey, deviceName)) {
                log.warn("checkDevice warn!,device is online,p={},d={}", productKey, deviceName);
                return R.fail("CUPBOARD.10036", "三元组在线");
            }
        } else {
            Pair<Boolean, Object> result = iotAcsService.queryDeviceStatus(productKey, deviceName);
            if (!result.getLeft()) {
                log.warn("checkDevice warn! errorMsg={}", result.getLeft());
                return R.fail("CUPBOARD.10035", "iot链接失败，请联系管理员");
            }
            
            if (ElectricityCabinet.IOT_STATUS_ONLINE.equalsIgnoreCase(result.getRight().toString())) {
                log.warn("checkDevice warn!errorMsg={}", result.getRight());
                return R.fail("CUPBOARD.10036", "三元组在线");
            }
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
        
        if (Objects.isNull(fullyCharged) || Objects.isNull(electricityConfig) || Objects
                .equals(electricityConfig.getIsLowBatteryExchange(), ElectricityConfig.NOT_LOW_BATTERY_EXCHANGE)) {
            return fullyCharged;
        }
        List<LowBatteryExchangeModel> list = JsonUtil.fromJsonArray(electricityConfig.getLowBatteryExchangeModel(), LowBatteryExchangeModel.class);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmmss");
        Long now = System.currentTimeMillis();
        for (LowBatteryExchangeModel lowBatteryExchangeModel : list) {
            if (Integer.parseInt(simpleDateFormat.format(now)) > Integer.parseInt(simpleDateFormat.format(lowBatteryExchangeModel.getExchangeBeginTime()))
                    && Integer.parseInt(simpleDateFormat.format(now)) < Integer.parseInt(simpleDateFormat.format(lowBatteryExchangeModel.getExchangeEndTime())) && Objects
                    .nonNull(lowBatteryExchangeModel.getBatteryPowerStandard()) && lowBatteryExchangeModel.getBatteryPowerStandard() < fullyCharged) {
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
            if (Objects.nonNull(electricityBatteries) && Integer.parseInt(simpleDateFormat.format(now)) > Integer
                    .parseInt(simpleDateFormat.format(lowBatteryExchangeModel.getExchangeBeginTime())) && Integer.parseInt(simpleDateFormat.format(now)) < Integer
                    .parseInt(simpleDateFormat.format(lowBatteryExchangeModel.getExchangeEndTime()))) {
                for (ElectricityBattery electricityBattery : electricityBatteries) {
                    // 电池所在仓门非禁用
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
            
            // 获取电池型号
            List<BatteryModel> modelList = batteryModelService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
            Map<String, Integer> modelCapacityMap = Maps.newHashMap();
            if (!CollectionUtils.isEmpty(modelList)) {
                modelCapacityMap = modelList.stream().filter(item -> StringUtils.isNotBlank(item.getBatteryType()) && Objects.nonNull(item.getCapacity()))
                        .collect(Collectors.toMap(BatteryModel::getBatteryType, BatteryModel::getCapacity, (k1, k2) -> k1));
            }
            
            Map<String, Integer> finalModelCapacityMap = modelCapacityMap;
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
                
                // 如果电池类型为空,则返回null
                if (StringUtils.isBlank(electricityCabinetBoxVO.getBatteryType())) {
                    electricityCabinetBoxVO.setBatteryType(null);
                }
                
                if (Objects.nonNull(item.getBatteryType())) {
                    String batteryType = item.getBatteryType();
                    
                    if (StringUtils.equals(StringUtils.EMPTY, item.getBatteryType())) {
                        electricityCabinetBoxVO.setBatteryVoltageAndCapacity(BatteryConstant.DEFAULT_MODEL);
                    } else {
                        // 设置电池短型号
                        electricityCabinetBoxVO.setBatteryModelShortType(subStringButteryType(batteryType));
                        // 设置电池电压 容量
                        StringBuilder voltageAndCapacity = new StringBuilder();
                        String batteryV = batteryType.substring(batteryType.indexOf("_") + 1).substring(0, batteryType.substring(batteryType.indexOf("_") + 1).indexOf("_"));
                        voltageAndCapacity.append(batteryV);
                        
                        // 优先取电池型号列表的容量
                        Integer capacity = finalModelCapacityMap.get(batteryType);
                        if ((Objects.isNull(capacity) || Objects.equals(NumberConstant.ZERO, capacity)) && Objects.nonNull(electricityBattery)) {
                            capacity = electricityBattery.getCapacity();
                        }
                        if (Objects.nonNull(capacity) && !Objects.equals(NumberConstant.ZERO, capacity)) {
                            voltageAndCapacity.append(StringConstant.FORWARD_SLASH).append(capacity).append(BatteryConstant.CAPACITY_UNIT);
                        }
                        electricityCabinetBoxVO.setBatteryVoltageAndCapacity(voltageAndCapacity.toString());
                    }
                }
                
                electricityCabinetBoxVOList.add(electricityCabinetBoxVO);
            });
            
            // 排序
            if (!CollectionUtils.isEmpty(electricityCabinetBoxVOList)) {
                resultList = electricityCabinetBoxVOList.stream().sorted(Comparator.comparing(item -> Integer.parseInt(item.getCellNo()))).collect(Collectors.toList());
            }
            
        }
        return R.ok(resultList);
    }
    
    @Override
    public R homepageOverviewDetail() {
        // 用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 租户
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
        
        // 实名认证用户
        CompletableFuture<Void> authenticationUser = CompletableFuture.runAsync(() -> {
            Integer authenticationUserCount = userInfoService.queryAuthenticationUserCount(tenantId);
            homepageOverviewDetailVo.setAuthenticationUserCount(authenticationUserCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query authenticationUser error!", e);
            return null;
        });
        
        // 门店
        List<Long> finalFranchiseeIds = franchiseeIds;
        // 查询所有门店
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
        
        // 柜机
        List<Long> finalStores = stores;
        CompletableFuture<Void> electricityCabinet = CompletableFuture.runAsync(() -> {
            Integer electricityCabinetCount = electricityCabinetService.queryCountByStoreIds(tenantId, finalStores);
            homepageOverviewDetailVo.setElectricityCabinetCount(electricityCabinetCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        // 车辆
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
    public R homepageUserAnalysis(Long beginTime, Long enTime) {
        // 用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }
        
        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        HomePageUserAnalysisVo homePageUserAnalysisVo = new HomePageUserAnalysisVo();
        
        // 实名认证用户
        CompletableFuture<Void> authenticationUser = CompletableFuture.runAsync(() -> {
            List<HomePageUserByWeekDayVo> list = userInfoService.queryUserAnalysisForAuthUser(tenantId, beginTime, enTime);
            homePageUserAnalysisVo.setAuthenticationUserAnalysis(list);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        // 普通用户
        CompletableFuture<Void> normalUser = CompletableFuture.runAsync(() -> {
            List<HomePageUserByWeekDayVo> list = userInfoService.queryUserAnalysisByUserStatus(tenantId, User.TYPE_USER_NORMAL_WX_PRO, beginTime, enTime);
            homePageUserAnalysisVo.setNormalUserAnalysis(list);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        // 用户总数
        CompletableFuture<Void> userCount = CompletableFuture.runAsync(() -> {
            //            Integer count = userService.queryHomePageCount(User.TYPE_USER_NORMAL_WX_PRO, beginTime, enTime, tenantId);
            homePageUserAnalysisVo.setUserCount(0);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });
        
        // 等待所有线程停止
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
        
        // 用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 租户
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
        
        // 换电成功订单数量统计
        List<Integer> finalEleIdList = eleIdList;
        List<Long> finalFranchiseeIds = franchiseeIds;
        // 查询所有门店
        List<Long> stores = null;
        if (!CollectionUtils.isEmpty(finalFranchiseeIds)) {
            stores = storeService.queryStoreIdByFranchiseeId(finalFranchiseeIds);
        }
        
        // 换电柜在线总数统计
        List<Long> finalStores = stores;
        CompletableFuture<Void> electricityOnlineCabinetCount = CompletableFuture.runAsync(() -> {
            Integer onLineCount = electricityCabinetService.queryCountByStoreIdsAndStatus(tenantId, finalStores, ElectricityCabinet.ELECTRICITY_CABINET_ONLINE_STATUS);
            homePageElectricityOrderVo.setOnlineElectricityCabinet(onLineCount);
        }, executorService).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricityCabinetTurnOver error!", e);
            return null;
        });
        
        // 换电柜离线总数统计
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
            List<HomepageElectricityExchangeFrequencyVo> homepageExchangeFrequency = electricityCabinetOrderService
                    .homepageExchangeFrequency(homepageElectricityExchangeFrequencyQuery);
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
        
        // 等待所有线程停止
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
        
        // 等待所有线程停止
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
        List<ElectricityCabinetFile> electricityCabinetFiles = electricityCabinetFileService
                .queryByDeviceInfo(electricityCabinetId.longValue(), ElectricityCabinetFile.TYPE_ELECTRICITY_CABINET, storageConfig.getIsUseOSS());
        List<String> cabinetPhoto = new ArrayList<>();
        
        for (ElectricityCabinetFile electricityCabinetFile : electricityCabinetFiles) {
            if (StringUtils.isNotEmpty(electricityCabinetFile.getName())) {
                cabinetPhoto.add("https://" + storageConverter.getUrlPrefix() + "/" + electricityCabinetFile.getName());
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
        
        // 校验套餐
        Triple<Boolean, String, Object> memberCardStatus = verficationMemberCardStatus(userInfo);
        if (Boolean.FALSE.equals(memberCardStatus.getLeft())) {
            return memberCardStatus;
        }
        
        // 获取加盟商类型
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("QUERY SELECTION EXCHANGE WARN! not found franchisee,franchiseeId={},uid={}", electricityCabinet.getFranchiseeId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        if (!mutualExchangeService.isSatisfyFranchiseeMutualExchange(userInfo.getTenantId(), userInfo.getFranchiseeId(), electricityCabinet.getFranchiseeId())) {
            log.warn("QUERY SELECTION EXCHANGE WARN! user franchiseeId  is not equal franchiseeId uid={} ,fid={}", userInfo.getUid(), userInfo.getFranchiseeId());
            return Triple.of(false, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
        }
        
        // 获取格挡信息
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryAllBoxByElectricityCabinetId(electricityCabinetId);
        if (CollectionUtils.isEmpty(electricityCabinetBoxList)) {
            log.warn("QUERY SELECTION EXCHANGE WARN! not found electricity boxes,electricityCabinetId={},uid={}", electricityCabinetId, userInfo.getUid());
            return Triple.of(false, "100552", "换电柜没有仓门，不能开门");
        }
        
        // 查询用户的电池列表
        List<String> userBatteryTypeList = userBatteryTypeService.selectByUid(userInfo.getUid());
        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOList = Lists.newArrayList();
        
        // 获取电池型号
        List<BatteryModel> modelList = batteryModelService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        Map<String, Integer> modelCapacityMap = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(modelList)) {
            modelCapacityMap = modelList.stream().filter(item -> StringUtils.isNotBlank(item.getBatteryType()) && Objects.nonNull(item.getCapacity()))
                    .collect(Collectors.toMap(BatteryModel::getBatteryType, BatteryModel::getCapacity, (k1, k2) -> k1));
        }
        
        Map<String, Integer> finalModelCapacityMap = modelCapacityMap;
        electricityCabinetBoxList.forEach(item -> {
            ElectricityCabinetBoxVO electricityCabinetBoxVO = new ElectricityCabinetBoxVO();
            BeanUtils.copyProperties(item, electricityCabinetBoxVO);
            if (StringUtils.isNotBlank(item.getSn()) && !StringUtils.startsWithIgnoreCase(item.getSn(), "UNKNOW")) {
                // 是否可换电
                electricityCabinetBoxVO.setExchange(isExchangeStatus(electricityCabinet, userBatteryTypeList, userInfo, electricityCabinetBoxVO, franchisee));
            }
            
            // 设置充电状态
            ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(item.getSn());
            if (Objects.nonNull(electricityBattery)) {
                electricityCabinetBoxVO.setChargeStatus(electricityBattery.getChargeStatus());
            }
            
            if (Objects.nonNull(item.getBatteryType())) {
                String batteryType = item.getBatteryType();
                if (StringUtils.equals(StringUtils.EMPTY, item.getBatteryType())) {
                    electricityCabinetBoxVO.setBatteryVoltageAndCapacity(BatteryConstant.DEFAULT_MODEL);
                } else {
                    electricityCabinetBoxVO.setBatteryModelShortType(subStringButteryType(batteryType));
                    String batteryV = batteryType.substring(batteryType.indexOf("_") + 1).substring(0, batteryType.substring(batteryType.indexOf("_") + 1).indexOf("_"));
                    
                    StringBuilder voltageAndCapacity = new StringBuilder();
                    voltageAndCapacity.append(batteryV);
                    
                    // 优先取电池型号列表的容量
                    Integer capacity = finalModelCapacityMap.get(batteryType);
                    if ((Objects.isNull(capacity) || Objects.equals(NumberConstant.ZERO, capacity)) && Objects.nonNull(electricityBattery)) {
                        capacity = electricityBattery.getCapacity();
                    }
                    
                    // 设置电池电压 容量
                    if (Objects.nonNull(capacity) && !Objects.equals(NumberConstant.ZERO, capacity)) {
                        voltageAndCapacity.append(StringConstant.FORWARD_SLASH).append(capacity).append(BatteryConstant.CAPACITY_UNIT);
                    }
                    electricityCabinetBoxVO.setBatteryVoltageAndCapacity(voltageAndCapacity.toString());
                }
            }
            electricityCabinetBoxVOList.add(electricityCabinetBoxVO);
        });
        
        List<ElectricityCabinetBoxVO> resultList = Lists.newArrayList();
        
        // 排序
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
        // 截取串数
        String num = batteryType.substring(batteryType.lastIndexOf("_") + 1);
        return batteryV + "/" + num;
    }
    
    private String subStringVoltageAndCapacity(String batteryType, Integer capacity) {
        StringBuilder voltageAndCapacity = new StringBuilder();
        
        if (Objects.nonNull(batteryType)) {
            
            // 如果电池型号解析不出来 则默认标准型号
            if (StringUtils.equals(StringUtils.EMPTY, batteryType)) {
                return BatteryConstant.DEFAULT_MODEL;
            }
            
            String batteryV = batteryType.substring(batteryType.indexOf("_") + 1).substring(0, batteryType.substring(batteryType.indexOf("_") + 1).indexOf("_"));
            voltageAndCapacity.append(batteryV);
        }
        
        if (Objects.nonNull(capacity) && !Objects.equals(NumberConstant.ZERO, capacity)) {
            voltageAndCapacity.append(StringConstant.FORWARD_SLASH).append(capacity).append(BatteryConstant.CAPACITY_UNIT);
        }
        // 截取串数
        return voltageAndCapacity.toString();
    }
    
    private Triple<Boolean, String, Object> verficationMemberCardStatus(UserInfo userInfo) {
        // 校验用户套餐
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
        // 用户是否可用
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
        
        // 换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        if (!eleResult) {
            log.warn("QUERY SELECTION EXCHANGE ERROR! electricityCabinet is offline,electricityCabinetId={},,userId={}", electricityCabinet.getId(), userInfo.getUid());
            return Triple.of(false, "100004", "柜机不在线");
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> verifyUserBatteryMemberCardStatus(UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo) {
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects
                .equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
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
        
        // 校验是否有退租审核中的订单
        BatteryMembercardRefundOrder batteryMembercardRefundOrder = batteryMembercardRefundOrderService.selectLatestByMembercardOrderNo(userBatteryMemberCard.getOrderId());
        if (Objects.nonNull(batteryMembercardRefundOrder) && Objects.equals(batteryMembercardRefundOrder.getStatus(), BatteryMembercardRefundOrder.STATUS_AUDIT)) {
            return Triple.of(false, "100282", "租金退款审核中，请等待审核确认后操作");
        }
        
        // 判断用户电池服务费
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService
                .acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
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
        // 判断车电一体套餐状态
        if (carRentalPackageMemberTermBizService.isExpirePackageOrder(userInfo.getTenantId(), userInfo.getUid())) {
            log.warn("QUERY SELECTION EXCHANGE ERROR! user memberCard disable,uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户套餐不可用");
        }
        
        // 判断用户电池服务费
        if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid()))) {
            log.warn("ORDER WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "300001", "存在滞纳金，请先缴纳");
        }
        return Triple.of(true, null, null);
    }
    
    public R otaCommand(Integer eid, Integer operateType, Integer versionType, List<Integer> cellNos) {
        Long uid = SecurityUtils.getUid();
        User user = userService.queryByUidFromCache(uid);
        
        Triple<Boolean, String, Object> checkResult = this.preCheck(eid, user, operateType, versionType);
        if (!checkResult.getLeft()) {
            return R.fail(checkResult.getMiddle(), checkResult.getRight().toString());
        }
        
        ElectricityCabinet electricityCabinet = (ElectricityCabinet) checkResult.getRight();
        // 查询柜机当前版本
        String cabinetCoreOrSubVersion = this.getCabinetCoreOrSubVersion(eid);
        if (StringUtils.isBlank(cabinetCoreOrSubVersion)) {
            log.warn("otaCommand warn! electricityCabinet is not version! eid={}", eid);
            return R.fail("100312", "柜机暂无版本号，无法ota升级");
        }
        
        // sessionId
        String sessionId = getSessionId(versionType, eid);
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
        
        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        // 发送命令失败
        if (!result.getLeft()) {
            return R.fail("ELECTRICITY.0037", "发送命令失败");
        }
        
        return R.ok(sessionId);
    }
    
    private Triple<Boolean, String, Object> preCheck(Integer eid, User user, Integer operateType, Integer versionType) {
        if (Objects.isNull(user)) {
            log.warn("otaCommand warn! user is null");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        ElectricityCabinet electricityCabinet = queryByIdFromCache(eid);
        if (Objects.isNull(electricityCabinet)) {
            log.warn("otaCommand warn! electricityCabinet is null, eid={}", eid);
            return Triple.of(false, "ELECTRICITY.0005", "未找到换电柜");
        }
        
        List<Integer> operateTypeList = List.of(OtaConstant.OTA_TYPE_DOWNLOAD, OtaConstant.OTA_TYPE_SYNC, OtaConstant.OTA_TYPE_UPGRADE);
        if (!operateTypeList.contains(operateType)) {
            log.warn("otaCommand warn! ota operate type illegal! electricityCabinet={}, operateType={}", electricityCabinet, operateType);
            return Triple.of(false, "100302", "ota操作类型不合法");
        }
    
        List<Integer> versionTypeList = List.of(OtaConstant.OTA_VERSION_TYPE_OLD, OtaConstant.OTA_VERSION_TYPE_NEW, OtaConstant.OTA_VERSION_TYPE_SIX,
                OtaConstant.OTA_VERSION_TYPE_NEW_SIX, OtaConstant.OTA_VERSION_TYPE_FOR_SYNC_UPGRADE);
        if (!versionTypeList.contains(versionType)) {
            log.error("otaCommand warn! versionType illegal! electricityCabinet={}, operateType={}, versionType={}", electricityCabinet, operateType, versionType);
            return Triple.of(false, "100302", "ota操作类型不合法");
        }
        
        return Triple.of(true, null, electricityCabinet);
    }
    
    private String getSessionId(Integer versionType, Integer eid) {
        String sessionPrefix = "";
        // 下载时 versionType:1--旧的 2--新的 3--旧六合一 4--新六合一
        // 同步和升级时 versionType=0
        
        // 如果是同步和升级 操作，需要从数据库查询versionType
        if (Objects.isNull(versionType) || Objects.equals(versionType, NumberConstant.ZERO)) {
            EleOtaFile eleOtaFile = eleOtaFileService.queryByEid(eid);
            if (Objects.nonNull(eleOtaFile)) {
                versionType = eleOtaFile.getFileType();
            }
        }
        
        // 通过versionType解析sessionPrefix
        if (Objects.nonNull(versionType)) {
            switch (versionType) {
                case OtaConstant.OTA_VERSION_TYPE_OLD:
                    sessionPrefix = OtaConstant.SESSION_PREFIX_OLD;
                    break;
                case OtaConstant.OTA_VERSION_TYPE_NEW:
                    sessionPrefix = OtaConstant.SESSION_PREFIX_NEW;
                    break;
                case OtaConstant.OTA_VERSION_TYPE_SIX:
                    sessionPrefix = OtaConstant.SESSION_PREFIX_SIX;
                    break;
                case OtaConstant.OTA_VERSION_TYPE_NEW_SIX:
                    sessionPrefix = OtaConstant.SESSION_PREFIX_NEW_SIX;
                    break;
                default:
                    sessionPrefix = "";
                    break;
            }
        }
        return sessionPrefix + UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    private Triple<Boolean, String, Object> assembleContent(Integer eid, Integer operateType, List<Integer> cellNos, Integer versionType, String sessionId) {
        Map<String, Object> content = new HashMap<>();
        
        if (OtaConstant.OTA_TYPE_DOWNLOAD.equals(operateType)) {
            OtaFileConfig coreBoardOtaFileConfig = null;
            OtaFileConfig subBoardOtaFileConfig = null;
            
            switch (versionType) {
                case EleOtaFile.TYPE_OLD_FILE:
                    List<OtaFileConfig> otaFileConfigs1 = otaFileConfigService.listByTypes(List.of(OtaFileConfig.TYPE_OLD_CORE_BOARD, OtaFileConfig.TYPE_OLD_SUB_BOARD));
                    if (!CollectionUtils.isEmpty(otaFileConfigs1)) {
                        for (OtaFileConfig config : otaFileConfigs1) {
                            if (Objects.equals(config.getType(), OtaFileConfig.TYPE_OLD_CORE_BOARD)) {
                                coreBoardOtaFileConfig = config;
                            }
                            if (Objects.equals(config.getType(), OtaFileConfig.TYPE_OLD_SUB_BOARD)) {
                                subBoardOtaFileConfig = config;
                            }
                        }
                    }
                    break;
                case EleOtaFile.TYPE_NEW_FILE:
                    List<OtaFileConfig> otaFileConfigs2 = otaFileConfigService.listByTypes(List.of(OtaFileConfig.TYPE_CORE_BOARD, OtaFileConfig.TYPE_SUB_BOARD));
                    if (!CollectionUtils.isEmpty(otaFileConfigs2)) {
                        for (OtaFileConfig config : otaFileConfigs2) {
                            if (Objects.equals(config.getType(), OtaFileConfig.TYPE_CORE_BOARD)) {
                                coreBoardOtaFileConfig = config;
                            }
                            if (Objects.equals(config.getType(), OtaFileConfig.TYPE_SUB_BOARD)) {
                                subBoardOtaFileConfig = config;
                            }
                        }
                    }
                    break;
                case EleOtaFile.TYPE_SIX_FILE:
                    subBoardOtaFileConfig = otaFileConfigService.queryByType(OtaFileConfig.TYPE_SIX_SUB_BOARD);
                    coreBoardOtaFileConfig = subBoardOtaFileConfig;
                    break;
                case EleOtaFile.TYPE_NEW_SIX_FILE:
                    subBoardOtaFileConfig = otaFileConfigService.queryByType(OtaFileConfig.TYPE_NEW_SIX_SUB_BOARD);
                    coreBoardOtaFileConfig = subBoardOtaFileConfig;
                    break;
                default:
                    break;
            }
    
            if (Objects.isNull(coreBoardOtaFileConfig) || Objects.isNull(subBoardOtaFileConfig)) {
                log.error("SEND DOWNLOAD OTA CONMMAND ERROR! incomplete upgrade file error! coreBoard={}, subBoard={}", coreBoardOtaFileConfig, subBoardOtaFileConfig);
                return Triple.of(Boolean.FALSE, "100301", "ota升级文件不完整，请联系客服处理");
            }
            
            createOrUpdateEleOtaFile(eid, versionType, coreBoardOtaFileConfig, subBoardOtaFileConfig);
            
            content.put(OtaConstant.OTA_SUB_FILE_URL, subBoardOtaFileConfig.getDownloadLink());
            content.put(OtaConstant.OTA_SUB_FILE_SHA256HEX, subBoardOtaFileConfig.getSha256Value());
            content.put(OtaConstant.OTA_CORE_FILE_URL, coreBoardOtaFileConfig.getDownloadLink());
            content.put(OtaConstant.OTA_CORE_FILE_SHA256HEX, coreBoardOtaFileConfig.getSha256Value());
        } else if (OtaConstant.OTA_TYPE_UPGRADE.equals(operateType)) {
            if (!DataUtil.collectionIsUsable(cellNos)) {
                return Triple.of(Boolean.FALSE, "100303", "升级内容为空，请选择您要升级的板子");
            }
            eleOtaUpgradeService.updateEleOtaUpgradeAndSaveHistory(cellNos, eid, sessionId);
            content.put(OtaConstant.OTA_CONTENT_CELL_NOS, cellNos);
        }
        
        return Triple.of(Boolean.TRUE, null, content);
    }
    
    private void createOrUpdateEleOtaFile(Integer eid, Integer versionType, OtaFileConfig coreBoardOtaFileConfig, OtaFileConfig subBoardOtaFileConfig) {
        EleOtaFile eleOtaFile = eleOtaFileService.queryByEid(eid);
        String coreSha256Value = "";
        String coreName = "";
        String subSha256Value = "";
        String subName = "";
        
        if (Objects.nonNull(coreBoardOtaFileConfig)) {
            coreSha256Value = coreBoardOtaFileConfig.getSha256Value();
            coreName = coreBoardOtaFileConfig.getName();
        }
        
        if (Objects.nonNull(subBoardOtaFileConfig)) {
            subSha256Value = subBoardOtaFileConfig.getSha256Value();
            subName = subBoardOtaFileConfig.getName();
        }
        
        if (Objects.nonNull(eleOtaFile)) {
            EleOtaFile update = new EleOtaFile();
            update.setId(eleOtaFile.getId());
            update.setCoreSha256Value(coreSha256Value);
            update.setCoreName(coreName);
            update.setSubSha256Value(subSha256Value);
            update.setSubName(subName);
            update.setFileType(versionType);
            update.setUpdateTime(System.currentTimeMillis());
            eleOtaFileService.update(update);
            return;
        }
        
        EleOtaFile create = new EleOtaFile();
        create.setElectricityCabinetId(eid);
        create.setCoreSha256Value(coreSha256Value);
        create.setCoreName(coreName);
        create.setSubSha256Value(subSha256Value);
        create.setSubName(subName);
        create.setFileType(versionType);
        create.setUpdateTime(System.currentTimeMillis());
        create.setCreateTime(System.currentTimeMillis());
        eleOtaFileService.insert(create);
    }
    
    private String getCabinetCoreOrSubVersion(Integer eid) {
        EleCabinetCoreData eleCabinetCoreData = eleCabinetCoreDataService.selectByEid(eid);
        if (Objects.nonNull(eleCabinetCoreData) && StringUtils.isNotEmpty(eleCabinetCoreData.getCoreVersion())) {
            return eleCabinetCoreData.getCoreVersion();
        }
        
        List<ElectricityCabinetBox> electricityCabinetBoxes = electricityCabinetBoxService.queryAllBoxByElectricityCabinetId(eid);
        if (CollectionUtils.isEmpty(electricityCabinetBoxes)) {
            return null;
        }
        
        List<ElectricityCabinetBox> collect = electricityCabinetBoxes.parallelStream().filter(item -> StrUtil.isNotBlank(item.getVersion())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            return null;
        }
        
        return collect.get(0).getVersion();
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
        List<ElectricityCabinetMapBO> electricityCabinets;
        
        // 根据id查询
        if (Objects.nonNull(cabinetQuery.getId())) {
            ElectricityCabinet electricityCabinet = this.queryByIdFromCache(cabinetQuery.getId());
            
            ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().name(electricityCabinet.getName())
                    .distance(Objects.isNull(cabinetQuery.getDistance()) ? 1000D : cabinetQuery.getDistance()).lon(electricityCabinet.getLongitude())
                    .lat(electricityCabinet.getLatitude()).franchiseeId(electricityCabinet.getFranchiseeId()).tenantId(TenantContextHolder.getTenantId()).usableStatus(0).build();
            GeoResults<RedisGeoCommands.GeoLocation<String>> geoRadius = getGeoLocationGeoResults(electricityCabinetQuery);
            if (geoRadius == null) {
                return R.ok(Collections.EMPTY_LIST);
            }
            
            electricityCabinets = geoRadius.getContent().parallelStream().map(e -> {
                ElectricityCabinetMapBO electricityCabinetMapBO = new ElectricityCabinetMapBO();
                Integer eid = Integer.valueOf(e.getContent().getName());
                ElectricityCabinet electricityCabinetTemp = queryByIdFromCache(eid);
                if (Objects.isNull(electricityCabinetTemp) || !Objects.equals(ELECTRICITY_CABINET_USABLE_STATUS, electricityCabinet.getUsableStatus())) {
                    return null;
                }
                
                electricityCabinetMapBO.setId(electricityCabinetTemp.getId());
                electricityCabinetMapBO.setName(electricityCabinetTemp.getName());
                electricityCabinetMapBO.setAddress(electricityCabinetTemp.getAddress());
                electricityCabinetMapBO.setLongitude(electricityCabinetTemp.getLongitude());
                electricityCabinetMapBO.setLatitude(electricityCabinetTemp.getLatitude());
                electricityCabinetMapBO.setOnlineStatus(electricityCabinetTemp.getOnlineStatus());
                electricityCabinetMapBO.setUsableStatus(electricityCabinetTemp.getUsableStatus());
                
                return electricityCabinetMapBO;
                
            }).filter(Objects::nonNull).collect(Collectors.toList());
            
        } else {
            electricityCabinets = electricityCabinetMapper.selectEleCabinetListByLongitudeAndLatitude(cabinetQuery);
        }
        
        if (CollectionUtils.isEmpty(electricityCabinets)) {
            return R.ok(Collections.EMPTY_LIST);
        }
        
        List<Integer> cabinetIds = electricityCabinets.stream().map(ElectricityCabinetMapBO::getId).collect(Collectors.toList());
        
        // 分批次查询柜机格挡
        List<ElectricityCabinetBox> electricityCabinetBoxList = new ArrayList<>();
        
        List<List<Integer>> partitions = ListUtil.partition(cabinetIds, NumberConstant.TWO_HUNDRED);
        partitions.forEach(item -> {
            List<ElectricityCabinetBox> boxes = electricityCabinetBoxService.listByElectricityCabinetIdS(item, TenantContextHolder.getTenantId());
            electricityCabinetBoxList.addAll(boxes);
        });
        
        Map<Integer, List<ElectricityCabinetBox>> boxesByIdMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(electricityCabinetBoxList)) {
            for (ElectricityCabinetBox box : electricityCabinetBoxList) {
                boxesByIdMap.computeIfAbsent(box.getElectricityCabinetId(), k -> new ArrayList<>()).add(box);
            }
        }
        
        // 获取系统配置
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        
        List<ElectricityCabinetListMapVO> assembleCabinetList = new ArrayList<>();
        
        electricityCabinets.stream().filter(Objects::nonNull).forEach(cabinet -> {
            ElectricityCabinetListMapVO electricityCabinetListMapVO = new ElectricityCabinetListMapVO();
            BeanUtils.copyProperties(cabinet, electricityCabinetListMapVO);
            
            List<ElectricityCabinetBox> electricityCabinetBoxes = boxesByIdMap.getOrDefault(cabinet.getId(), Collections.emptyList());
            int boxNum = NumberConstant.ZERO;
            int batteryNum = NumberConstant.ZERO;
            int unusableBoxNum = NumberConstant.ZERO;
            
            if (!CollectionUtils.isEmpty(electricityCabinetBoxes)) {
                // 柜机格口数量
                boxNum = electricityCabinetBoxes.size();
                
                // 电池在仓数量统计
                batteryNum = (int) electricityCabinetBoxes.stream().filter(box -> Objects.equals(box.getStatus(), STATUS_ELECTRICITY_BATTERY)).count();
                
                // 电池锁仓数量统计
                unusableBoxNum = (int) electricityCabinetBoxes.stream()
                        .filter(box -> Objects.equals(box.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE)).count();
                
                // 是否锁仓柜机
                electricityCabinetListMapVO.setIsUnusable(unusableBoxNum > NumberConstant.ZERO);
                
                // 少电多电
                ElectricityCabinetListMapVO batteryCountVO = this.judgeBatteryCountType(cabinet, electricityConfig, boxNum, batteryNum);
                electricityCabinetListMapVO.setIsLowCharge(batteryCountVO.getIsLowCharge());
                electricityCabinetListMapVO.setIsFulCharge(batteryCountVO.getIsFulCharge());
            } else {
                // 无仓，显示为少电
                electricityCabinetListMapVO.setIsLowCharge(NumberConstant.ONE);
            }
            
            electricityCabinetListMapVO.setBoxNum(boxNum);
            electricityCabinetListMapVO.setBatteryNum(batteryNum);
            electricityCabinetListMapVO.setUnusableBoxNum(unusableBoxNum);
            
            assembleCabinetList.add(electricityCabinetListMapVO);
        });
        
        // 设置统计值
        Integer totalCount = assembleCabinetList.size();
        
        Integer lowChargeCount = (int) assembleCabinetList.stream().filter(cabinet -> Objects.equals(cabinet.getIsLowCharge(), NumberConstant.ONE)).count();
        
        Integer fullChargeCount = (int) assembleCabinetList.stream().filter(cabinet -> Objects.equals(cabinet.getIsFulCharge(), NumberConstant.ONE)).count();
        
        Integer unusableCount = (int) assembleCabinetList.stream().filter(cabinet -> BooleanUtils.isTrue(cabinet.getIsUnusable())).count();
        
        Integer offLineCount = (int) assembleCabinetList.stream().filter(cabinet -> Objects.equals(cabinet.getOnlineStatus(), NumberConstant.ONE)).count();
        
        // 0-全部、1-少电、2-多电、3-锁仓、4-离线
        List<ElectricityCabinetListMapVO> rspList = new ArrayList<>();
        switch (cabinetQuery.getStatus()) {
            default:
                // 默认少电
                rspList = assembleCabinetList.stream().filter(cabinet -> Objects.equals(cabinet.getIsLowCharge(), NumberConstant.ONE)).collect(Collectors.toList());
                break;
            case 0:
                rspList = assembleCabinetList;
                break;
            case 2:
                rspList = assembleCabinetList.stream().filter(cabinet -> Objects.equals(cabinet.getIsFulCharge(), NumberConstant.ONE)).collect(Collectors.toList());
                break;
            case 3:
                rspList = assembleCabinetList.stream().filter(cabinet -> BooleanUtils.isTrue(cabinet.getIsUnusable())).collect(Collectors.toList());
                break;
            case 4:
                rspList = assembleCabinetList.stream().filter(cabinet -> Objects.equals(cabinet.getOnlineStatus(), NumberConstant.ONE)).collect(Collectors.toList());
                break;
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            rspList = Collections.emptyList();
        }
        
        ElectricityCabinetMapVO rsp = ElectricityCabinetMapVO.builder().totalCount(totalCount).lowChargeCount(lowChargeCount).fullChargeCount(fullChargeCount)
                .unusableCount(unusableCount).offLineCount(offLineCount).electricityCabinetListMapVOList(rspList).build();
        
        return R.ok(rsp);
    }
    
    /***
     * 判断少电/多电
     */
    private ElectricityCabinetListMapVO judgeBatteryCountType(ElectricityCabinetMapBO cabinet, ElectricityConfig electricityConfig, Integer boxNum, Integer batteryNum) {
        ElectricityCabinetListMapVO electricityCabinetListMapVO = new ElectricityCabinetListMapVO();
        
        // 判断少/多电柜机
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getChargeRateType(), ElectricityConfig.CHARGE_RATE_TYPE_UNIFY)) {
            BigDecimal lowChargeRateBd = electricityConfig.getLowChargeRate();
            BigDecimal fullChargeRateBd = electricityConfig.getFullChargeRate();
            
            // 默认低电比例25% 多电比例75%
            int lowChargeRate = Objects.isNull(lowChargeRateBd) ? NumberConstant.TWENTY_FIVE : lowChargeRateBd.intValue();
            int fullChargeRate = Objects.isNull(fullChargeRateBd) ? NumberConstant.SEVENTY_FIVE : fullChargeRateBd.intValue();
            int chargeRate = BigDecimal.valueOf(batteryNum).multiply(NumberConstant.ONE_HUNDRED_BD).divide(BigDecimal.valueOf(boxNum), NumberConstant.ZERO, RoundingMode.DOWN)
                    .intValue();
            
            if (chargeRate <= lowChargeRate) {
                electricityCabinetListMapVO.setIsLowCharge(NumberConstant.ONE);
            } else if (chargeRate >= fullChargeRate) {
                electricityCabinetListMapVO.setIsFulCharge(NumberConstant.ONE);
            } else {
                electricityCabinetListMapVO.setIsLowCharge(NumberConstant.ZERO);
                electricityCabinetListMapVO.setIsFulCharge(NumberConstant.ZERO);
            }
        } else {
            // 单个配置
            Integer batteryCountType = cabinet.getBatteryCountType();
            if (Objects.nonNull(batteryCountType)) {
                switch (batteryCountType) {
                    // 少电
                    case EleCabinetConstant.BATTERY_COUNT_TYPE_LESS:
                        electricityCabinetListMapVO.setIsLowCharge(NumberConstant.ONE);
                        break;
                    // 多电
                    case EleCabinetConstant.BATTERY_COUNT_TYPE_MORE:
                        electricityCabinetListMapVO.setIsFulCharge(NumberConstant.ONE);
                        break;
                    // 正常
                    default:
                        electricityCabinetListMapVO.setIsLowCharge(NumberConstant.ZERO);
                        electricityCabinetListMapVO.setIsFulCharge(NumberConstant.ZERO);
                        break;
                }
            }
        }
        
        return electricityCabinetListMapVO;
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
        // 判断加盟商互通
        Pair<Boolean, Set<Long>> mutualExchangePair = mutualExchangeService.satisfyMutualExchangeFranchisee(electricityCabinetQuery.getTenantId(),
                electricityCabinetQuery.getFranchiseeId());
        if (mutualExchangePair.getLeft()) {
            electricityCabinetQuery.setFranchiseeIdList(new ArrayList<>(mutualExchangePair.getRight()));
        } else {
            electricityCabinetQuery.setFranchiseeIdList(CollUtil.newArrayList(electricityCabinetQuery.getFranchiseeId()));
        }

        List<ElectricityCabinetVO> electricityCabinets = electricityCabinetMapper.selectElectricityCabinetByAddress(electricityCabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinets)) {
            return Collections.EMPTY_LIST;
        }
        
        return electricityCabinets.parallelStream().peek(item -> {
            
            // 营业时间
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
                // 空仓
                long emptyCellNumber = cabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
                // 满电数量
                long fullBatteryNumber = cabinetBoxList.stream().filter(this::isFullBattery).count();
                // 可换电数量
                List<ElectricityCabinetBox> exchangeableList = cabinetBoxList.stream().filter(e -> isExchangeable(e, fullyCharged)).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(exchangeableList)) {
                    assignExchangeableBatteryType(exchangeableList, item);
                    // assignExchangeableVoltageAndCapacity(exchangeableList, item);
                }
                long exchangeableNumber = exchangeableList.size();
                
                item.setNoElectricityBattery((int) emptyCellNumber);
                item.setFullyElectricityBattery((int) exchangeableNumber);// 兼容2.0小程序首页显示问题
                item.setExchangeBattery((int) exchangeableNumber);
                item.setFullyBatteryNumber((int) fullBatteryNumber);
                
                Map<String, Long> batteryTypeMapes = cabinetBoxList.stream().filter(e -> StringUtils.isNotBlank(e.getSn()) && StringUtils.isNotBlank(e.getBatteryType()))
                        .map(i -> i.getBatteryType().substring(i.getBatteryType().indexOf("_") + 1)
                                .substring(0, i.getBatteryType().substring(i.getBatteryType().indexOf("_") + 1).indexOf("_")))
                        .collect(Collectors.groupingBy(a -> a, Collectors.counting()));
                item.setBatteryTypeMapes(batteryTypeMapes);
            }
            
            // 获取柜机图片
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
            
            // 获取所有启用的格挡
            List<ElectricityCabinetBox> electricityCabinetBoxes = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinetId);
            if (org.apache.commons.collections.CollectionUtils.isEmpty(electricityCabinetBoxes)) {
                return;
            }
            
            // 过滤没有电池的格挡
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
                    messageSendProducer.sendAsyncMsg(i, "", "", 0);
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
            abnormalMessageNotify.setExceptionType(AbnormalAlarmExceptionTypeEnum.BATTERY_FULL_TYPE.getType());
            abnormalMessageNotify.setDescription(AbnormalAlarmExceptionTypeEnum.BATTERY_FULL_TYPE.getDescription());
            abnormalMessageNotify.setReportTime(formatter.format(LocalDateTime.now()));
            
            MqNotifyCommon<ElectricityAbnormalMessageNotify> abnormalMessageNotifyCommon = new MqNotifyCommon<>();
            abnormalMessageNotifyCommon.setTime(System.currentTimeMillis());
            abnormalMessageNotifyCommon.setType(SendMessageTypeEnum.ABNORMAL_ALARM_NOTIFY.getType());
            abnormalMessageNotifyCommon.setPhone(item);
            abnormalMessageNotifyCommon.setTenantId(electricityCabinet.getTenantId());
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
                //                item.setUrl(storageService.getOssFileUrl(storageConfig.getBucketName(), item.getName(), System.currentTimeMillis() + 10 * 60 * 1000L));
                item.setUrl(storageConverter.generateUrl(item.getName(), System.currentTimeMillis() + 10 * 60 * 1000L));
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
            
            // 删除柜机扩展参数
            electricityCabinetExtraService
                    .update(ElectricityCabinetExtra.builder().eid(Long.valueOf(id)).delFlag(electricityCabinetUpdate.getDelFlag()).updateTime(electricityCabinet.getUpdateTime())
                            .build());
            
            DbUtils.dbOperateSuccessThenHandleCache(electricityCabinetMapper.updateEleById(electricityCabinetUpdate), i -> {
                redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + id);
                redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName());
                redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_EXTRA + id);
                
                // 删除格挡
                electricityCabinetBoxService.batchDeleteBoxByElectricityCabinetId(id);
                
                // 删除柜机GEO信息
                redisService.removeGeoMember(CacheConstant.CACHE_ELECTRICITY_CABINET_GEO + electricityCabinetUpdate.getTenantId(), electricityCabinetUpdate.getId().toString());
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
        
        if (CollUtil.isEmpty(list)) {
            return Triple.of(false, "ELECTRICITY.0007", "导入数据不能为空");
        }
        
        List<BatchImportCabinetFailVO> cabinetFailList = CollUtil.newArrayList();
        List<ElectricityCabinetImportQuery> cabinetSuccessList = CollUtil.newArrayList();
        verifyBatchImportParams(list, cabinetFailList, cabinetSuccessList);
        
        for (ElectricityCabinetImportQuery query : cabinetSuccessList) {
            ElectricityCabinet electricityCabinet = new ElectricityCabinet();
            BeanUtils.copyProperties(query, electricityCabinet);
            electricityCabinet.setBusinessTime(ElectricityCabinetAddAndUpdate.ALL_DAY);
            electricityCabinet.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
            electricityCabinet.setDelFlag(ElectricityCabinet.DEL_NORMAL);
            electricityCabinet.setTenantId(TenantContextHolder.getTenantId());
            electricityCabinet.setCreateTime(System.currentTimeMillis());
            electricityCabinet.setUpdateTime(System.currentTimeMillis());
            electricityCabinet.setStockStatus(StockStatusEnum.UN_STOCK.getCode());
            
            // 根据门店id查询加盟商id
            Store store = storeService.queryByIdFromCache(query.getStoreId());
            electricityCabinet.setFranchiseeId(store.getFranchiseeId());
            
            DbUtils.dbOperateSuccessThenHandleCache(electricityCabinetMapper.insert(electricityCabinet), i -> {
                // 添加格挡
                electricityCabinetBoxService.batchInsertBoxByModelIdV2(electricityCabinetModelService.queryByIdFromCache(query.getModelId()), electricityCabinet.getId());
                // 添加服务时间记录
                electricityCabinetServerService.insertOrUpdateByElectricityCabinet(electricityCabinet, electricityCabinet);
                
                // 缓存柜机GEO信息
                addElectricityCabinetLocToGeo(electricityCabinet);
                
                // 新增柜机扩展参数
                ElectricityCabinetExtra electricityCabinetExtra = ElectricityCabinetExtra.builder().eid(electricityCabinet.getId().longValue())
                        .batteryCountType(EleCabinetConstant.BATTERY_COUNT_TYPE_NORMAL).rentTabType(RentReturnNormEnum.ALL_RENT.getCode())
                        .returnTabType(RentReturnNormEnum.MIN_RETURN.getCode()).tenantId(electricityCabinet.getTenantId()).delFlag(electricityCabinet.getDelFlag())
                        .createTime(electricityCabinet.getCreateTime()).updateTime(electricityCabinet.getUpdateTime()).build();
                electricityCabinetExtraService.insertOne(electricityCabinetExtra);
            });
        }
        
        return Triple.of(true, null, BatchImportCabinetVo.builder().successCount(cabinetSuccessList.size()).failCount(cabinetFailList.size()).failVOS(cabinetFailList).build());
    }
    
    @Override
    public void batchUpdate(List<ElectricityCabinet> list) {
        list.forEach(item -> DbUtils.dbOperateSuccessThenHandleCache(electricityCabinetMapper.updateEleById(item), i -> {
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET + item.getId());
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_DEVICE + item.getProductKey() + item.getDeviceName());
            
            // 更新柜机GEO信息
            addElectricityCabinetLocToGeo(item);
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
    public Triple<Boolean, String, Object> listTransferCabinetModel(TransferCabinetModelRequest cabinetModelRequest) {
        // 查询工厂租户下是否有该柜机
        ElectricityCabinet testFactoryCabinet = this
                .selectByProductKeyAndDeviceNameFromDB(cabinetModelRequest.getProductKey(), cabinetModelRequest.getDeviceName(), eleCommonConfig.getTestFactoryTenantId());
        if (Objects.isNull(testFactoryCabinet)) {
            log.warn("ELE WARN!not found testFactoryCabinet,p={},d={},tenantId={}", cabinetModelRequest.getProductKey(), cabinetModelRequest.getDeviceName(),
                    eleCommonConfig.getTestFactoryTenantId());
            return Triple.of(false, "", "柜机不存在");
        }
        
        // 获取工厂柜机型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(testFactoryCabinet.getModelId());
        if (Objects.isNull(electricityCabinetModel)) {
            log.error("ELE ERROR!not found electricityCabinetModel,p={},d={},tenantId={}", cabinetModelRequest.getProductKey(), cabinetModelRequest.getDeviceName(),
                    eleCommonConfig.getTestFactoryTenantId());
            return Triple.of(false, "", "柜机型号不存在");
        }
        
        return Triple.of(true, null, electricityCabinetModelService.selectListByNum(electricityCabinetModel.getNum(), TenantContextHolder.getTenantId()));
    }
    
    
    @Override
    public Triple<Boolean, String, Object> transferCabinet(ElectricityCabinetTransferQuery query) {
        Store store = storeService.queryByIdFromCache(query.getStoreId());
        if (Objects.isNull(store) || !Objects.equals(store.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("ELE ERROR!not found store,storeId={}", query.getStoreId());
            return Triple.of(false, "", "门店不存在");
        }
        
        // 查询工厂租户下是否有该柜机
        ElectricityCabinet testFactoryCabinet = this.selectByProductKeyAndDeviceNameFromDB(query.getProductKey(), query.getDeviceName(), eleCommonConfig.getTestFactoryTenantId());
        if (Objects.isNull(testFactoryCabinet)) {
            log.warn("ELE WARN!not found testFactoryCabinet,p={},d={},tenantId={}", query.getProductKey(), query.getDeviceName(), eleCommonConfig.getTestFactoryTenantId());
            return Triple.of(false, "", "柜机不存在");
        }
        
        // 获取工厂柜机型号
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(testFactoryCabinet.getModelId());
        if (Objects.isNull(electricityCabinetModel)) {
            log.error("ELE ERROR!not found electricityCabinetModel,p={},d={},tenantId={}", query.getProductKey(), query.getDeviceName(), eleCommonConfig.getTestFactoryTenantId());
            return Triple.of(false, "", "柜机型号不存在");
        }
        
        checkUpdateBatchElectricityCabinetExtra(query.getRentTabType(), query.getReturnTabType(), query.getMinRetainBatteryCount(), query.getMaxRetainBatteryCount());
        
        Integer modelId = null;
        
        if (Objects.nonNull(query.getModelId())) {
            ElectricityCabinetModel cabinetModel = electricityCabinetModelService.queryByIdFromCache(query.getModelId());
            if (Objects.nonNull(cabinetModel)) {
                modelId = cabinetModel.getId();
            }
        } else {
            // 查询当前租户下是否有该型号，若没有则新建
            ElectricityCabinetModel cabinetModel = electricityCabinetModelService.selectByNum(electricityCabinetModel.getNum(), TenantContextHolder.getTenantId());
            if (Objects.isNull(cabinetModel)) {
                ElectricityCabinetModel cabinetModelInsert = buildCabinetModel(electricityCabinetModel);
                electricityCabinetModelService.insert(cabinetModelInsert);
                modelId = cabinetModelInsert.getId();
            } else {
                modelId = cabinetModel.getId();
            }
        }
        
        // 当前租户下新增柜机
        ElectricityCabinet electricityCabinetInsert = new ElectricityCabinet();
        
        electricityCabinetInsert.setBusinessTime(ElectricityCabinetAddAndUpdate.ALL_DAY);
        electricityCabinetInsert.setName(query.getName());
        electricityCabinetInsert.setSn(StringUtils.isBlank(query.getCabinetSn()) ? query.getDeviceName() : query.getCabinetSn());
        electricityCabinetInsert.setModelId(modelId);
        electricityCabinetInsert.setProductKey(query.getProductKey());
        electricityCabinetInsert.setDeviceName(query.getDeviceName());
        electricityCabinetInsert.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        electricityCabinetInsert.setAddress(query.getAddress());
        electricityCabinetInsert.setLatitude(query.getLatitude());
        electricityCabinetInsert.setLongitude(query.getLongitude());
        electricityCabinetInsert.setUsableStatus(ELECTRICITY_CABINET_USABLE_STATUS);
        electricityCabinetInsert.setOnlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS);
        electricityCabinetInsert.setVersion(testFactoryCabinet.getVersion());
        electricityCabinetInsert.setFullyCharged(ElectricityCabinetAddAndUpdate.FULL_CHARGED);
        electricityCabinetInsert.setServicePhone(testFactoryCabinet.getServicePhone());
        electricityCabinetInsert.setCreateTime(System.currentTimeMillis());
        electricityCabinetInsert.setUpdateTime(System.currentTimeMillis());
        electricityCabinetInsert.setTenantId(TenantContextHolder.getTenantId());
        electricityCabinetInsert.setStoreId(query.getStoreId());
        electricityCabinetInsert.setFranchiseeId(store.getFranchiseeId());
        electricityCabinetInsert.setVersion(testFactoryCabinet.getVersion());
        electricityCabinetInsert.setExchangeType(testFactoryCabinet.getExchangeType());
        electricityCabinetInsert.setStockStatus(StockStatusEnum.UN_STOCK.getCode());
        
        // 物理删除工厂测试柜机
        this.physicsDelete(testFactoryCabinet);
        
        DbUtils.dbOperateSuccessThenHandleCache(electricityCabinetMapper.insert(electricityCabinetInsert), i -> {
            electricityCabinetBoxService.batchInsertBoxByModelIdV2(electricityCabinetModel, electricityCabinetInsert.getId());
            electricityCabinetServerService.insertOrUpdateByElectricityCabinet(electricityCabinetInsert, electricityCabinetInsert);
            // 更新柜机GEO缓存信息
            redisService.addGeo(CacheConstant.CACHE_ELECTRICITY_CABINET_GEO + electricityCabinetInsert.getTenantId(), electricityCabinetInsert.getId().toString(),
                    new Point(electricityCabinetInsert.getLongitude(), electricityCabinetInsert.getLatitude()));
            
            // 新增柜机扩展参数
            ElectricityCabinetExtra electricityCabinetExtra = ElectricityCabinetExtra.builder().eid(electricityCabinetInsert.getId().longValue())
                    .rentTabType(query.getRentTabType()).returnTabType(query.getReturnTabType()).minRetainBatteryCount(query.getMinRetainBatteryCount())
                    .maxRetainBatteryCount(query.getMaxRetainBatteryCount()).batteryCountType(EleCabinetConstant.BATTERY_COUNT_TYPE_NORMAL)
                    .tenantId(electricityCabinetInsert.getTenantId()).delFlag(electricityCabinetInsert.getDelFlag()).createTime(electricityCabinetInsert.getCreateTime())
                    .updateTime(electricityCabinetInsert.getUpdateTime()).build();
            electricityCabinetExtraService.insertOne(electricityCabinetExtra);
        });
        
        // 下发重启命令
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("password", cabinetConfig.getInitPassword());
        dataMap.put("uid", SecurityUtils.getUid());
        dataMap.put("username", SecurityUtils.getUserInfo().getUsername());
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(UUID.randomUUID().toString().replace("-", "")).data(dataMap)
                .productKey(electricityCabinetInsert.getProductKey()).deviceName(electricityCabinetInsert.getDeviceName())
                .command(ElectricityIotConstant.ELE_COMMAND_CUPBOARD_RESTART).build();
        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinetInsert);
        
        // 生成迁移记录
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
        cabinetModelInsert.setManufacturerName(electricityCabinetModel.getManufacturerName());
        cabinetModelInsert.setCabinetSize(electricityCabinetModel.getCabinetSize());
        cabinetModelInsert.setCellSize(electricityCabinetModel.getCellSize());
        cabinetModelInsert.setScreenSize(electricityCabinetModel.getScreenSize());
        cabinetModelInsert.setWaterproofGrade(electricityCabinetModel.getWaterproofGrade());
        cabinetModelInsert.setHeating(EleCabinetModelHeatingEnum.HEATING_NOT_SUPPORT.getCode());
        return cabinetModelInsert;
    }
    
    private void verifyBatchImportParams(List<ElectricityCabinetImportQuery> list, List<BatchImportCabinetFailVO> cabinetFailVOS,
            List<ElectricityCabinetImportQuery> cabinetSuccessList) {
        
        // 去重
        List<ElectricityCabinetImportQuery> distinctList = list.stream().filter(e -> {
            Set<ElectricityCabinetImportQuery> seen = new HashSet<>();
            return seen.add(e);
        }).collect(Collectors.toList());
        
        if (distinctList.size() != list.size()) {
            // 差集
            List<ElectricityCabinetImportQuery> difference = new ArrayList<>(list);
            difference.removeAll(distinctList);
            difference.stream().forEach(e -> {
                cabinetFailVOS.add(BatchImportCabinetFailVO.builder().deviceName(e.getDeviceName()).reason("三元组重复").build());
            });
        }
        
        for (ElectricityCabinetImportQuery cabinetImportQuery : distinctList) {
            ElectricityCabinet electricityCabinet = this.queryByProductAndDeviceName(cabinetImportQuery.getProductKey(), cabinetImportQuery.getDeviceName());
            if (Objects.nonNull(electricityCabinet)) {
                cabinetFailVOS.add(BatchImportCabinetFailVO.builder().deviceName(cabinetImportQuery.getDeviceName()).reason("三元组已存在").build());
                continue;
            }
            
            Store store = storeService.queryByIdFromCache(cabinetImportQuery.getStoreId());
            if (Objects.isNull(store) || !Objects.equals(store.getTenantId(), TenantContextHolder.getTenantId())) {
                cabinetFailVOS.add(BatchImportCabinetFailVO.builder().deviceName(cabinetImportQuery.getDeviceName()).reason("门店不存在").build());
                continue;
            }
            
            ElectricityCabinetModel cabinetModel = electricityCabinetModelService.queryByIdFromCache(cabinetImportQuery.getModelId());
            if (Objects.isNull(cabinetModel) || !Objects.equals(cabinetModel.getTenantId(), TenantContextHolder.getTenantId())) {
                cabinetFailVOS.add(BatchImportCabinetFailVO.builder().deviceName(cabinetImportQuery.getDeviceName()).reason("柜机型号不存在").build());
                continue;
            }
            cabinetSuccessList.add(cabinetImportQuery);
        }
        
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
        
        // 获取库房名称列表 根据库房id查询库房名称，不需要过滤库房状态是已删除的
        List<Long> warehouseIdList = electricityCabinetList.stream().map(ElectricityCabinetVO::getWarehouseId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<AssetWarehouseNameVO> assetWarehouseNameVOS = assetWarehouseService.selectByIdList(warehouseIdList);
        
        Map<Long, String> warehouseNameVOMap = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(assetWarehouseNameVOS)) {
            warehouseNameVOMap = assetWarehouseNameVOS.stream().collect(Collectors.toMap(AssetWarehouseNameVO::getId, AssetWarehouseNameVO::getName, (item1, item2) -> item2));
        }
        
        Map<Long, String> finalWarehouseNameVOMap = warehouseNameVOMap;
        
        for (ElectricityCabinetVO cabinetVO : electricityCabinetList) {
            
            ElectricityCabinetModel cabinetModel = electricityCabinetModelService.queryByIdFromCache(cabinetVO.getModelId());
            
            index++;
            
            ElectricityCabinetExcelVO excelVO = new ElectricityCabinetExcelVO();
            excelVO.setId(index);
            excelVO.setSn(cabinetVO.getSn());
            excelVO.setName(cabinetVO.getName());
            excelVO.setAddress(cabinetVO.getAddress());
            excelVO.setUsableStatus(Objects.equals(cabinetVO.getUsableStatus(), ELECTRICITY_CABINET_USABLE_STATUS) ? "启用" : "禁用");
            excelVO.setModelName(Objects.nonNull(cabinetModel) ? cabinetModel.getName() : "");
            excelVO.setVersion(cabinetVO.getVersion());
            excelVO.setFranchiseeName(acquireFranchiseeNameByStore(cabinetVO.getStoreId()));
            excelVO.setCreateTime(Objects.nonNull(cabinetVO.getCreateTime()) ? DateUtil.format(DateUtil.date(cabinetVO.getCreateTime()), DatePattern.NORM_DATETIME_FORMATTER) : "");
            excelVO.setExchangeType(acquireExchangeType(cabinetVO.getExchangeType()));
            excelVO.setStockStatus(acquireStockStatus(cabinetVO.getStockStatus()));
            
            // 设置仓库名称
            if (finalWarehouseNameVOMap.containsKey(cabinetVO.getWarehouseId())) {
                excelVO.setWarehouseName(finalWarehouseNameVOMap.get(cabinetVO.getWarehouseId()));
            }
            
            ElectricityCabinetServer electricityCabinetServer = electricityCabinetServerService
                    .queryByProductKeyAndDeviceName(cabinetVO.getProductKey(), cabinetVO.getDeviceName());
            if (Objects.nonNull(electricityCabinetServer)) {
                excelVO.setServerEndTime(Objects.nonNull(electricityCabinetServer.getServerEndTime()) ? DateUtil
                        .format(DateUtil.date(electricityCabinetServer.getServerEndTime()), DatePattern.NORM_DATETIME_FORMATTER) : "");
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
    
    private String acquireStockStatus(Integer stockStatus) {
        String status = null;
        switch (stockStatus) {
            case 0:
                status = "库存";
                break;
            case 1:
                status = "已出库";
                break;
            default:
                status = StringUtils.EMPTY;
                break;
        }
        return status;
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
    @Override
    public R queryElectricityCabinetExtendData(Integer electricityCabinetId) {
        
        // 校验柜机Id
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("100003", "柜机不存在");
        }
        
        if (!SecurityUtils.isAdmin() && !Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("100003", "柜机不存在");
        }
        
        // 换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        String netType = null;
        // 如果柜机在线，则需要取柜机上报的信号
        if (eleResult) {
            netType = redisService.get(CacheConstant.CACHE_ELECTRICITY_CABINET_EXTEND_DATA + electricityCabinetId);
        }
        
        return R.ok(netType);
    }
    
    @Override
    public R showBatteryVAndCapacity(Integer electricityCabinetId) {
        // 校验柜机Id
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(electricityCabinet) || !Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("100003", "柜机不存在");
        }
        
        List<ElectricityCabinetBox> cabinetBoxList = electricityCabinetBoxService.selectEleBoxAttrByEid(electricityCabinet.getId());
        if (CollectionUtils.isEmpty(cabinetBoxList)) {
            return R.ok();
        }
        
        // 可换电数量
        List<ElectricityCabinetBox> exchangeableList = cabinetBoxList.stream().filter(item -> isExchangeable(item, electricityCabinet.getFullyCharged()))
                .collect(Collectors.toList());
        return R.ok(assignExchangeableVoltageAndCapacityV2(exchangeableList));
    }
    
    @Slave
    @Override
    public Integer existsByAreaId(Long areaId) {
        return electricityCabinetMapper.existsByAreaId(areaId);
    }
    
    @Slave
    @Override
    public List<AreaCabinetNumBO> countByAreaGroup(List<Long> areaIdList) {
        return electricityCabinetMapper.countByAreaGroup(areaIdList);
    }
    
    @Override
    public List<Integer> listIdsByName(String name) {
        return electricityCabinetMapper.listIdsByName(name);
    }
    
    @Override
    public RentReturnEditEchoVO rentReturnEditEcho(Long id) {
        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentReturnEditEcho is error! not found user ");
            new CustomBusinessException("用户未找到");
        }
        
        ElectricityCabinetExtra cabinetExtra = electricityCabinetExtraService.queryByEid(id);
        if (Objects.isNull(cabinetExtra)) {
            log.warn("rentReturnEditEcho is error, cabinetExtra is null, id:{}", id);
            return new RentReturnEditEchoVO();
        }
        
        return new RentReturnEditEchoVO(cabinetExtra.getEid(), cabinetExtra.getRentTabType(), cabinetExtra.getReturnTabType(), cabinetExtra.getMinRetainBatteryCount(),
                cabinetExtra.getMaxRetainBatteryCount());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchEditRentReturn(ElectricityCabinetBatchEditRentReturnQuery rentReturnQuery) {
        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentReturnEditEcho is error! not found user ");
            return R.fail("用户未找到");
        }
        if (CollUtil.isEmpty(rentReturnQuery.getCountQueryList())) {
            return R.fail("请至少选择一个柜机");
        }
        
        // 前置校验
        rentReturnQuery.getCountQueryList().forEach(e -> {
            checkUpdateBatchElectricityCabinetExtra(rentReturnQuery.getRentTabType(), rentReturnQuery.getReturnTabType(), e.getMinRetainBatteryCount(),
                    e.getMaxRetainBatteryCount());
        });
        
        for (ElectricityCabinetBatchEditRentReturnCountQuery query : rentReturnQuery.getCountQueryList()) {
            this.updateBatchElectricityCabinetExtra(query.getId(), rentReturnQuery.getRentTabType(), rentReturnQuery.getReturnTabType(), query.getMinRetainBatteryCount(),
                    query.getMaxRetainBatteryCount());
        }
        
        return R.ok();
    }
    
    private void updateBatchElectricityCabinetExtra(Integer eid, Integer rentTabType, Integer returnTabType, Integer minRetainBatteryCount, Integer maxRetainBatteryCount) {
        if (Objects.isNull(eid)) {
            log.warn("updateBatchElectricityCabinetExtra is error, cabinetId is null");
            return;
        }
        ElectricityCabinetExtra cabinetExtra = electricityCabinetExtraService.queryByEid(Long.valueOf(eid));
        if (Objects.isNull(cabinetExtra)) {
            log.warn("updateBatchElectricityCabinetExtra is error, cabinetExtra is null, id:{}", eid);
            return;
        }
        
        if (Objects.nonNull(rentTabType)) {
            // 如果不是自定义租电，则设置为null
            if (!Objects.equals(rentTabType, RentReturnNormEnum.CUSTOM_RENT.getCode())) {
                minRetainBatteryCount = null;
            }
            electricityCabinetExtraService.updateMinElectricityCabinetExtra(minRetainBatteryCount, rentTabType, eid);
        }
        
        if (Objects.nonNull(returnTabType)) {
            // 如果不是自定义退电，则设置为null
            if (!Objects.equals(returnTabType, RentReturnNormEnum.CUSTOM_RETURN.getCode())) {
                maxRetainBatteryCount = null;
            }
            electricityCabinetExtraService.updateMaxElectricityCabinetExtra(maxRetainBatteryCount, returnTabType, eid);
        }
        
    }
    
    private void checkUpdateBatchElectricityCabinetExtra(Integer rentTabType, Integer returnTabType, Integer minRetainBatteryCount, Integer maxRetainBatteryCount) {
        if (Objects.isNull(rentTabType) && Objects.isNull(returnTabType)) {
            throw new CustomBusinessException("设置可租可退标准异常");
        }
        if (Objects.equals(rentTabType, RentReturnNormEnum.CUSTOM_RENT.getCode()) && Objects.isNull(minRetainBatteryCount)) {
            throw new CustomBusinessException("自定义！最保留电池数不能为空");
        }
        
        if (Objects.equals(returnTabType, RentReturnNormEnum.CUSTOM_RETURN.getCode()) && Objects.isNull(maxRetainBatteryCount)) {
            throw new CustomBusinessException("自定义！保留空仓数不能为空");
        }
    }
    
    
    @Override
    public R rentReturnEditEchoByDeviceName(String productKey, String deviceName) {
        // 查询柜机
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("不存在的柜机");
        }
        
        ElectricityCabinetExtra cabinetExtra = electricityCabinetExtraService.queryByEid(Long.valueOf(electricityCabinet.getId()));
        if (Objects.isNull(cabinetExtra)) {
            log.warn("rentReturnEditEcho is error, cabinetExtra is null, id:{}", electricityCabinet.getId());
            return R.ok(new RentReturnEditEchoVO());
        }
        
        return R.ok(new RentReturnEditEchoVO(cabinetExtra.getEid(), cabinetExtra.getRentTabType(), cabinetExtra.getReturnTabType(), cabinetExtra.getMinRetainBatteryCount(),
                cabinetExtra.getMaxRetainBatteryCount()));
    }
    
    @Override
    public R updateCabinetPattern(EleCabinetPatternQuery query) {
        ElectricityCabinet electricityCabinet = this.queryFromCacheByProductAndDeviceName(query.getProductKey(), query.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        String deviceSecret = electricityCabinet.getDeviceSecret();
        
        //判断三元组是否在设备列表中存在,若不存在，新增
        EleDeviceCode deviceCodeCache = eleDeviceCodeService.queryBySnFromCache(query.getProductKey(), query.getDeviceName());
        if (Objects.isNull(deviceCodeCache)) {
            long time = System.currentTimeMillis();
            EleDeviceCode deviceCode = new EleDeviceCode();
            deviceCode.setProductKey(query.getProductKey());
            deviceCode.setDeviceName(query.getDeviceName());
            deviceCode.setSecret(StringUtils.isBlank(deviceSecret)?SecureUtil.hmacMd5(query.getProductKey() + query.getDeviceName()).digestHex(String.valueOf(time)):deviceSecret);
            deviceCode.setOnlineStatus(EleCabinetConstant.STATUS_OFFLINE);
            deviceCode.setRemark("");
            deviceCode.setDelFlag(CommonConstant.DEL_N);
            deviceCode.setCreateTime(time);
            deviceCode.setUpdateTime(time);
            eleDeviceCodeService.insert(deviceCode);
        }

        deviceCodeCache = eleDeviceCodeService.queryBySnFromCache(query.getProductKey(), query.getDeviceName());
        if (Objects.isNull(deviceCodeCache)) {
            return R.fail("100488", "设备不存在");
        }

        //如果没有deviceSecret，或者柜机表与设备表的deviceSecret不一致
        if (StringUtils.isBlank(deviceSecret) || !Objects.equals(deviceSecret, deviceCodeCache.getSecret())) {
            QueryDeviceDetailResult queryDeviceDetailResult = registerDeviceService.queryDeviceDetail(query.getProductKey(), query.getDeviceName());
            if (Objects.isNull(queryDeviceDetailResult) || StringUtils.isBlank(queryDeviceDetailResult.getDeviceSecret())) {
                log.warn("ELE WARN!not found deviceDetailResult,p={},d={}", query.getProductKey(), query.getDeviceName());
                return R.fail("100218", "iot消息发送失败");
            }

            //更新柜机deviceSecret
            ElectricityCabinet electricityCabinetUpdate = new ElectricityCabinet();
            electricityCabinetUpdate.setId(electricityCabinet.getId());
            electricityCabinetUpdate.setProductKey(electricityCabinet.getProductKey());
            electricityCabinetUpdate.setDeviceName(electricityCabinet.getDeviceName());
            electricityCabinetUpdate.setDeviceSecret(queryDeviceDetailResult.getDeviceSecret());
            electricityCabinetUpdate.setUpdateTime(System.currentTimeMillis());
            electricityCabinetService.update(electricityCabinetUpdate);

            //更新设备deviceSecret
            EleDeviceCode deviceCodeUpdate = new EleDeviceCode();
            deviceCodeUpdate.setId(deviceCodeCache.getId());
            deviceCodeUpdate.setSecret(queryDeviceDetailResult.getDeviceSecret());
            deviceCodeUpdate.setUpdateTime(System.currentTimeMillis());
            eleDeviceCodeService.updateById(deviceCodeUpdate, query.getProductKey(), query.getDeviceName());
        }
        
        String apiAddress = eleCommonConfig.getApiAddress();
        if (StringUtils.isBlank(apiAddress)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        Map<String, Object> params = Maps.newHashMap();
        params.put("apiAddress", apiAddress);
        params.put("iotConnectMode", query.getPattern());
        
        EleOuterCommandQuery commandQuery = new EleOuterCommandQuery();
        commandQuery.setProductKey(query.getProductKey());
        commandQuery.setDeviceName(query.getDeviceName());
        commandQuery.setCommand(ElectricityIotConstant.ELE_OTHER_SETTING);
        commandQuery.setData(params);
        return this.sendCommandToEleForOuter(commandQuery);
    }
    
    @Override
    @Slave
    public List<ElectricityCabinetBO> listByIdList(List<Integer> cabinetIdList) {
        return electricityCabinetMapper.selectListByIdList(cabinetIdList);
    }
    
    @Override
    public R listSuperAdminPage(ElectricityCabinetQuery electricityCabinetQuery) {
        List<ElectricityCabinetVO> electricityCabinetList = electricityCabinetMapper.selectListSuperAdminPage(electricityCabinetQuery);
        if (ObjectUtil.isEmpty(electricityCabinetList)) {
            return R.ok();
        }
        
        if (ObjectUtil.isNotEmpty(electricityCabinetList)) {
            // 获取库房名称列表 根据库房id查询库房名称，不需要过滤库房状态是已删除的
            List<Long> warehouseIdList = electricityCabinetList.stream().map(ElectricityCabinetVO::getWarehouseId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            List<AssetWarehouseNameVO> assetWarehouseNameVOS = assetWarehouseService.selectByIdList(warehouseIdList);
            
            Map<Long, String> warehouseNameVOMap = Maps.newHashMap();
            if (!CollectionUtils.isEmpty(assetWarehouseNameVOS)) {
                warehouseNameVOMap = assetWarehouseNameVOS.stream().collect(Collectors.toMap(AssetWarehouseNameVO::getId, AssetWarehouseNameVO::getName, (item1, item2) -> item2));
            }
            
            // 查询区域
            List<Long> areaIdList = electricityCabinetList.stream().map(ElectricityCabinetVO::getAreaId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            MerchantAreaRequest areaQuery = MerchantAreaRequest.builder().idList(areaIdList).build();
            List<MerchantArea> merchantAreaList = merchantAreaService.queryList(areaQuery);
            Map<Long, String> areaNameMap = Maps.newHashMap();
            if (!CollectionUtils.isEmpty(merchantAreaList)) {
                areaNameMap = merchantAreaList.stream().collect(Collectors.toMap(MerchantArea::getId, MerchantArea::getName, (item1, item2) -> item2));
            }
            
            Map<Long, String> finalWarehouseNameVOMap = warehouseNameVOMap;
            Map<Long, String> finalAreaNameMap = areaNameMap;
            
            electricityCabinetList.parallelStream().forEach(e -> {
                if (Objects.nonNull(e.getTenantId())) {
                    Tenant tenant = tenantService.queryByIdFromCache(e.getTenantId());
                    e.setTenantName(Objects.isNull(tenant) ? null : tenant.getName());
                }
                
                if (Objects.nonNull(e.getStoreId())) {
                    Store store = storeService.queryByIdFromCache(Long.valueOf(e.getStoreId()));
                    e.setStoreName(Objects.isNull(store) ? "" : store.getName());
                }
                
                // 营业时间
                if (Objects.nonNull(e.getBusinessTime()) && StringUtils.isNotBlank(e.getBusinessTime())) {
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
                
                // 查找型号名称
                ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelService.queryByIdFromCache(e.getModelId());
                if (Objects.nonNull(electricityCabinetModel)) {
                    e.setModelName(electricityCabinetModel.getName());
                    
                    // 赋值复合字段
                    StringBuilder manufacturerNameAndModelName = new StringBuilder();
                    if (StringUtils.isNotBlank(electricityCabinetModel.getManufacturerName())) {
                        manufacturerNameAndModelName.append(electricityCabinetModel.getManufacturerName());
                    }
                    
                    if (StringUtils.isNotBlank(manufacturerNameAndModelName.toString())) {
                        manufacturerNameAndModelName.append(StringConstant.FORWARD_SLASH);
                    }
                    
                    if (StringUtils.isNotBlank(electricityCabinetModel.getName())) {
                        manufacturerNameAndModelName.append(electricityCabinetModel.getName());
                    }
                    e.setManufacturerNameAndModelName(manufacturerNameAndModelName.toString());
                }
                
                // 查满仓空仓数
                Integer fullyElectricityBattery = 0;
                int electricityBatteryTotal = 0;
                int noElectricityBattery = 0;
                int batteryInElectricity = 0;
                
                Double fullyCharged = e.getFullyCharged();
                
                List<ElectricityCabinetBox> cabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(e.getId());
                if (!CollectionUtils.isEmpty(cabinetBoxList)) {
                    // 空仓
                    noElectricityBattery = (int) cabinetBoxList.stream().filter(this::isNoElectricityBattery).count();
                    // 有电池数量
                    batteryInElectricity = (int) cabinetBoxList.stream().filter(this::isBatteryInElectricity).count();
                    // 电池总数
                    electricityBatteryTotal = (int) cabinetBoxList.stream().filter(this::isElectricityBattery).count();
                    // 可换电电池数
                    fullyElectricityBattery = (int) cabinetBoxList.stream().filter(i -> isExchangeable(i, fullyCharged)).count();
                }
                
                boolean result = deviceIsOnline(e.getProductKey(), e.getDeviceName(), e.getPattern());
                
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
                
                // 是否锁住
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
                
                // 设置运营商名称
                if (Objects.nonNull(e.getFranchiseeId())) {
                    Franchisee franchisee = franchiseeService.queryByIdFromCache(e.getFranchiseeId());
                    if (Objects.nonNull(franchisee)) {
                        e.setFranchiseeName(franchisee.getName());
                    }
                }
                
                // 设置仓库名称
                if (finalWarehouseNameVOMap.containsKey(e.getWarehouseId())) {
                    e.setWarehouseName(finalWarehouseNameVOMap.get(e.getWarehouseId()));
                }
                
                // 设置区域名称
                if (finalAreaNameMap.containsKey(e.getAreaId())) {
                    e.setAreaName(finalAreaNameMap.get(e.getAreaId()));
                }
            });
        }
        electricityCabinetList.stream().sorted(Comparator.comparing(ElectricityCabinetVO::getCreateTime).reversed()).collect(Collectors.toList());
        return R.ok(electricityCabinetList);
    }
    
    @Override
    public Pair<Boolean, Integer> selectCellExchangeFindUsableEmptyCellNo(Integer eid, String version, Long uid) {
        // 旧版本仍走旧分配逻辑
        if (StringUtils.isNotBlank(version) && VersionUtil.compareVersion(ELE_CABINET_VERSION, version) > 0) {
            return this.findUsableEmptyCellNo(eid);
        }
        
        Integer cellNo = null;
        List<ElectricityCabinetBox> emptyCellList = electricityCabinetBoxService.listUsableEmptyCell(eid);
        if (CollectionUtils.isEmpty(emptyCellList)) {
            return Pair.of(false, null);
        }
        
        // 可用格挡只有一个默认直接分配
        if (emptyCellList.size() == 1) {
            cellNo = Integer.valueOf(emptyCellList.get(0).getCellNo());
            return Pair.of(true, cellNo);
        }
        
        // 空仓逻辑优化：过滤异常的空仓号
        Pair<Boolean, List<ElectricityCabinetBox>> filterEmptyExchangeCellPair = exceptionHandlerService.filterEmptyExceptionCell(eid, emptyCellList);
        if (filterEmptyExchangeCellPair.getLeft()) {
            return Pair.of(true,
                    Integer.parseInt(filterEmptyExchangeCellPair.getRight().get(ThreadLocalRandom.current().nextInt(filterEmptyExchangeCellPair.getRight().size())).getCellNo()));
        }
        emptyCellList = filterEmptyExchangeCellPair.getRight();
        
        
        // 舒适换电分配空仓
        Pair<Boolean, Integer> comfortExchangeGetEmptyCellPair = chooseCellConfigService.comfortExchangeGetEmptyCell(uid, emptyCellList);
        if (comfortExchangeGetEmptyCellPair.getLeft()) {
            return Pair.of(true, comfortExchangeGetEmptyCellPair.getRight());
        }
        
        // 有多个空格挡  优先分配开门的格挡
        List<ElectricityCabinetBox> openDoorEmptyCellList = emptyCellList.stream().filter(item -> Objects.equals(item.getIsLock(), ElectricityCabinetBox.OPEN_DOOR))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(openDoorEmptyCellList)) {
            cellNo = Integer.parseInt(openDoorEmptyCellList.get(ThreadLocalRandom.current().nextInt(openDoorEmptyCellList.size())).getCellNo());
            return Pair.of(true, cellNo);
        }
        
        cellNo = Integer.parseInt(emptyCellList.get(ThreadLocalRandom.current().nextInt(emptyCellList.size())).getCellNo());
        return Pair.of(true, cellNo);
    }




    @Override
    public R quickExchage(QuickExchangeQuery quickExchangeQuery) {
        // 校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(quickExchangeQuery.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("QuickExchange WARN! not found user info,uid={} ", quickExchangeQuery.getUid());
            return R.fail("100205", "未找到用户信息");
        }

        if (!redisService.setNx(CacheConstant.ORDER_TIME_UID + SecurityUtils.getUserInfo().getUid(), "1", 5 * 1000L, false)) {
            return R.fail("100002", "下单过于频繁");
        }
        // 这里加柜机的缓存，为了限制不同时分配格挡
        if (!redisService.setNx(CacheConstant.ORDER_ELE_ID + quickExchangeQuery.getEid(), "1", 5 * 1000L, false)) {
            return R.fail("100214", "已有其他用户正在使用中，请稍后再试");
        }

        try {

            // 构造责任链入参
            ProcessContext<ExchangeAssertProcessDTO> processContext = ProcessContext.builder().code(ExchangeAssertChainTypeEnum.QUICK_EXCHANGE_ASSERT.getCode()).processModel(
                    ExchangeAssertProcessDTO.builder().eid(quickExchangeQuery.getEid()).cellNo(quickExchangeQuery.getCellNo()).userInfo(userInfo)
                            .chainObject(new ExchangeChainDTO()).build()).needBreak(false).build();
            // 校验
            ProcessContext<ExchangeAssertProcessDTO> process = processController.process(processContext);
            if (process.getNeedBreak()) {
                log.warn("QuickExchange Warn! BreakReason is {}", JsonUtil.toJson(process.getResult()));
                return process.getResult();
            }

            ElectricityBattery electricityBattery = process.getProcessModel().getChainObject().getElectricityBattery();
            AssertUtil.assertObjectIsNull(electricityBattery, "300900", "系统检测到当前用户未绑定电池，请检查");

            ElectricityCabinet cabinet = process.getProcessModel().getChainObject().getElectricityCabinet();
            AssertUtil.assertObjectIsNull(cabinet, "100003", "找不到柜机");

            if (VersionUtil.compareVersion(cabinet.getVersion(), QuickExchangeQuery.QUICK_EXCHANGE_VERSION) < 0) {
                return R.fail("300901", "当前版本不支持快捷换电");
            }

            Franchisee franchisee = process.getProcessModel().getChainObject().getFranchisee();
            AssertUtil.assertObjectIsNull(franchisee, "120203", "加盟商不存在");

            String batteryName = process.getProcessModel().getChainObject().getBatteryName();
            if (!Objects.equals(batteryName, electricityBattery.getSn())) {
                log.warn("QuickExchange Warn! boxSn not equal userBingBatterySn ,eid is {}, cell is {} boxSn is {}, userBingBatterySn is {}", quickExchangeQuery.getEid(),
                        quickExchangeQuery.getCellNo(), batteryName, electricityBattery.getSn());
                return R.fail("100328", "当前格挡内电池和用户绑定电池不一致");
            }


            // 获取满电仓
            Triple<Boolean, String, Object> getFullCellBox = lessTimeExchangeService.allocateFullBatteryBox(cabinet, userInfo, franchisee);
            if (!getFullCellBox.getLeft()) {
                return R.fail("100216", "换电柜暂无满电电池");
            }

            // 注意：不用修改套餐次数，取电iot成功会扣减次数
            // 生成换电订单
            ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
                    .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.EXCHANGE_BATTERY, userInfo.getUid())).uid(userInfo.getUid()).phone(userInfo.getPhone())
                    .electricityCabinetId(quickExchangeQuery.getEid()).oldCellNo(quickExchangeQuery.getCellNo()).newCellNo(Integer.valueOf((String) getFullCellBox.getRight()))
                    .orderSeq(ElectricityCabinetOrder.STATUS_INIT).status(ElectricityCabinetOrder.INIT).source(ExchangeTypeEnum.QUICK_EXCHANGE.getCode())
                    .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).storeId(cabinet.getStoreId()).franchiseeId(franchisee.getId())
                    .tenantId(TenantContextHolder.getTenantId()).build();
            electricityCabinetOrderService.insertOrder(electricityCabinetOrder);


            // 换电之后的处理
            String sessionId = quickExchangeFollowHandler(cabinet, userInfo, electricityCabinetOrder, electricityBattery.getSn());

            return R.ok(QuickExchangeVO.builder().sessionId(sessionId).build());
        } catch (BizException e) {
            throw new BizException(e.getErrCode(), e.getErrMsg());
        } finally {
            redisService.delete(CacheConstant.ORDER_ELE_ID + quickExchangeQuery.getEid());
            redisService.delete(CacheConstant.ORDER_TIME_UID + userInfo.getUid());
        }
    }


    private String quickExchangeFollowHandler(ElectricityCabinet cabinet, UserInfo userInfo, ElectricityCabinetOrder cabinetOrder, String batteryName) {
        // 发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("orderId", cabinetOrder.getOrderId());
        dataMap.put("placeCellNo", cabinetOrder.getOldCellNo());
        dataMap.put("takeCellNo", cabinetOrder.getNewCellNo());
        dataMap.put("batteryName", batteryName);

        String sessionId = CacheConstant.OPEN_FULL_CELL + "_" + cabinetOrder.getOrderId();

        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(sessionId).data(dataMap).productKey(cabinet.getProductKey()).deviceName(cabinet.getDeviceName())
                .command(ElectricityIotConstant.OPEN_FULL_CELL).build();
        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, cabinet);

        try {
            // 初始化操作记录
            electricityCabinetOrderOperHistoryService.initExchangeOrderOperHistory(cabinetOrder.getOrderId(), cabinetOrder.getTenantId(), cabinetOrder.getOldCellNo());

            // 记录活跃时间
            userActiveInfoService.userActiveRecord(userInfo);
        } catch (Exception e) {
            log.error("QuickExchangeFollowHandler Error! ", e);
        }

        return sessionId;
    }

    @Override
    public R getQuickExchangeResult(String sessionId) {
        String result = redisService.get(CacheConstant.QUICK_EXCHANGE_RESULT_KEY + sessionId);
        if (StrUtil.isEmpty(result)) {
            return R.ok(QuickExchangeResultVO.builder().code("0001").build());
        }

        QuickExchangeResultDTO resultDTO = JsonUtil.fromJson(result, QuickExchangeResultDTO.class);
        QuickExchangeResultVO vo = QuickExchangeResultVO.builder().msg(resultDTO.getMsg()).build();
        if (resultDTO.getSuccess()) {
            vo.setCode("0002");
            return R.ok(vo);
        }
        vo.setCode("0003");
        return R.ok(vo);
    }
}
