package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.config.ExchangeConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.EleEsignConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.LessTimeExchangeDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.EleUserEsignRecord;
import com.xiliulou.electricity.entity.ElectricityAppConfig;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityExceptionOrderStatusRecord;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserCarDeposit;
import com.xiliulou.electricity.entity.UserCarMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.CellTypeEnum;
import com.xiliulou.electricity.enums.ExchangeTypeEnum;
import com.xiliulou.electricity.enums.OrderCheckEnum;
import com.xiliulou.electricity.enums.OrderDataModeEnums;
import com.xiliulou.electricity.enums.SelectionExchageEunm;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.ElectricityCabinetOrderMapper;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.query.HomepageElectricityExchangeFrequencyQuery;
import com.xiliulou.electricity.query.LessExchangeSelfOpenCellQuery;
import com.xiliulou.electricity.query.OpenDoorQuery;
import com.xiliulou.electricity.query.OpenFullCellQuery;
import com.xiliulou.electricity.query.OrderQuery;
import com.xiliulou.electricity.query.OrderQueryV2;
import com.xiliulou.electricity.query.OrderQueryV3;
import com.xiliulou.electricity.query.OrderSelectionExchangeQuery;
import com.xiliulou.electricity.query.OrderSelfOpenCellQuery;
import com.xiliulou.electricity.query.SelectionExchangeCheckQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.EleUserEsignRecordService;
import com.xiliulou.electricity.service.ElectricityAppConfigService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetPhysicsOperRecordService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityExceptionOrderStatusRecordService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserActiveInfoService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserCarDepositService;
import com.xiliulou.electricity.service.UserCarMemberCardService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.ttl.ChannelSourceContextHolder;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorServiceWrapper;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorsSupport;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.VersionUtil;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.ExchangeOrderMsgShowVO;
import com.xiliulou.electricity.vo.ExchangeUnFinishOrderVo;
import com.xiliulou.electricity.vo.ExchangeUserSelectVo;
import com.xiliulou.electricity.vo.HomepageElectricityExchangeFrequencyVo;
import com.xiliulou.electricity.vo.WarnMsgVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单表(TElectricityCabinetOrder)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@Service("electricityCabinetOrderService")
@Slf4j
public class ElectricityCabinetOrderServiceImpl implements ElectricityCabinetOrderService {
    
    @Resource
    private ElectricityCabinetOrderMapper electricityCabinetOrderMapper;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    
    @Autowired
    StoreService storeService;
    
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    ElectricityExceptionOrderStatusRecordService electricityExceptionOrderStatusRecordService;
    
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
    
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    UserBatteryService userBatteryService;
    
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    
    @Autowired
    UserCarDepositService userCarDepositService;
    
    @Autowired
    UserActiveInfoService userActiveInfoService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;
    
    @Autowired
    CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;
    
    @Autowired
    UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Resource
    private TenantService tenantService;
    
    @Resource
    private ElectricityCabinetPhysicsOperRecordService electricityCabinetPhysicsOperRecordService;
    
    @Resource
    private ExchangeConfig exchangeConfig;
    
    @Resource
    private ElectricityAppConfigService electricityAppConfigService;
    
    @Resource
    private EleUserEsignRecordService eleUserEsignRecordService;
    
    @Resource
    private ElectricityCabinetOrderHistoryService electricityCabinetOrderHistoryService;
    
    public static final String ORDER_LESS_TIME_EXCHANGE_CABINET_VERSION="2.1.19";
    
    TtlXllThreadPoolExecutorServiceWrapper executorServiceWrapper = TtlXllThreadPoolExecutorsSupport
            .get(XllThreadPoolExecutors.newFixedThreadPool("ELE_USER_ORDER_LIST", 3, "ele_user_order_list_thread"));
    
    
    /**
     * 修改数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetOrder electricityCabinetOrder) {
        return this.electricityCabinetOrderMapper.updateById(electricityCabinetOrder);
        
    }
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param orderId 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetOrder queryByOrderId(String orderId) {
        return this.electricityCabinetOrderMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getOrderId, orderId));
    }
    
    /**
     * 新增订单
     *
     * @param electricityCabinetOrder
     */
    @Override
    public void insertOrder(ElectricityCabinetOrder electricityCabinetOrder) {
        this.electricityCabinetOrderMapper.insert(electricityCabinetOrder);
    }
    
    @Deprecated
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R openDoor(OpenDoorQuery openDoorQuery) {
        if (Objects.isNull(openDoorQuery.getOrderId())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper
                .selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, openDoorQuery.getOrderId()));
        if (Objects.isNull(electricityCabinetOrder)) {
            log.warn("ELECTRICITY  WARN! not found order,orderId={} ", openDoorQuery.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        //开新门开旧门不易前端为准，以订单状态为准
        if (electricityCabinetOrder.getOrderSeq() < ElectricityCabinetOrder.STATUS_CHECK_OLD_AND_NEW) {
            openDoorQuery.setOpenType(OpenDoorQuery.OLD_OPEN_TYPE);
        } else {
            openDoorQuery.setOpenType(OpenDoorQuery.NEW_OPEN_TYPE);
        }
        
        //旧电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.OLD_OPEN_TYPE)) {
            if (!Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT) && !Objects
                    .equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_CHECK_FAIL) && !Objects
                    .equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_CHECK_BATTERY_EXISTS) && !Objects
                    .equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_OPEN_FAIL)) {
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
        }
        
        //新电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.NEW_OPEN_TYPE)) {
            if (!Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS) && !Objects
                    .equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_CHECK_FAIL) && !Objects
                    .equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_CHECK_BATTERY_NOT_EXISTS) && !Objects
                    .equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_FAIL)) {
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
        }
        
        //判断开门用户是否匹配
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(electricityCabinetOrder.getUid(), user.getUid())) {
            return R.fail("ELECTRICITY.0016", "订单用户不匹配，非法开门");
        }
        
        //查找换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.warn("ELECTRICITY  WARN! not found electricityCabinet ！electricityCabinetId={}", electricityCabinetOrder.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        if (!eleResult) {
            log.warn("ELECTRICITY  WARN!  electricityCabinet is offline ！electricityCabinet={}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }
        
        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("order  WARN! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("order  WARN! user is unUsable! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("ELE MEMBERCARD WARN! not found franchisee,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0038", "加盟商不存在");
        }
        
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
        if (Objects.isNull(electricityBattery)) {
            log.warn("ELE WARN! not found user bind battery,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }
        
        //旧电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.OLD_OPEN_TYPE)) {
            //发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cell_no", electricityCabinetOrder.getOldCellNo());
            dataMap.put("order_id", electricityCabinetOrder.getOrderId());
            dataMap.put("status", electricityCabinetOrder.getStatus());
            
            //是否开启电池检测
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
            if (Objects.nonNull(electricityConfig)) {
                if (Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
                    dataMap.put("is_checkBatterySn", true);
                    dataMap.put("user_binding_battery_sn", electricityBattery.getSn());
                } else {
                    dataMap.put("is_checkBatterySn", false);
                }
            }
            
            if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
                dataMap.put("model_type", false);
            } else {
                dataMap.put("model_type", true);
                dataMap.put("multiBatteryModelName", electricityBattery.getModel());
            }
            
            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId()).data(dataMap)
                    .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.ELE_COMMAND_ORDER_OPEN_OLD_DOOR)
                    .build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        }
        
        //新电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.NEW_OPEN_TYPE)) {
            //发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cell_no", electricityCabinetOrder.getNewCellNo());
            dataMap.put("order_id", electricityCabinetOrder.getOrderId());
            dataMap.put("serial_number", electricityCabinetOrder.getNewElectricityBatterySn());
            dataMap.put("status", electricityCabinetOrder.getStatus().toString());
            dataMap.put("old_cell_no", electricityCabinetOrder.getOldCellNo());
            
            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId()).data(dataMap)
                    .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.ELE_COMMAND_ORDER_OPEN_NEW_DOOR)
                    .build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        }
        redisService.delete(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + electricityCabinetOrder.getOrderId());
        return R.ok();
    }
    
    @Slave
    @Override
    public R queryList(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        Integer orderMode = electricityCabinetOrderQuery.getOrderMode();
        List<ElectricityCabinetOrderVO> electricityCabinetOrderVOList = null;
        if (Objects.isNull(orderMode) || Objects.equals(orderMode, OrderDataModeEnums.CURRENT_ORDER.getCode())) {
            electricityCabinetOrderVOList = electricityCabinetOrderMapper.queryList(electricityCabinetOrderQuery);
        } else {
            electricityCabinetOrderVOList = electricityCabinetOrderHistoryService.queryList(electricityCabinetOrderQuery);
        }
        
        if (ObjectUtil.isEmpty(electricityCabinetOrderVOList)) {
            return R.ok(new ArrayList<>());
        }
        
        if (ObjectUtil.isNotEmpty(electricityCabinetOrderVOList)) {
            // 批量查询会员信息
            Map<Long, String> userNameMap = new HashMap<>();
            List<Long> uIdList = electricityCabinetOrderVOList.stream().map(ElectricityCabinetOrderVO::getUid).collect(Collectors.toList());
            List<UserInfo> userInfos = userInfoService.listByUidList(uIdList);
            if (ObjectUtils.isNotEmpty(userInfos)) {
                userNameMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getUid, UserInfo::getName));
            }
            Map<Long, String> finalUserNameMap = userNameMap;
            
            electricityCabinetOrderVOList.parallelStream().forEach(e -> {
                
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(e.getElectricityCabinetId());
                e.setElectricityCabinetName(Objects.isNull(electricityCabinet) ? "" : electricityCabinet.getName());
                
                // 设置会员名称
                if (ObjectUtils.isNotEmpty(finalUserNameMap.get(e.getUid()))) {
                    e.setUName(finalUserNameMap.get(e.getUid()));
                }
                
                if (Objects.nonNull(e.getStatus()) && e.getStatus().equals(ElectricityCabinetOrder.ORDER_CANCEL) || Objects.nonNull(e.getStatus()) && e.getStatus()
                        .equals(ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL)) {
                    ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinetOrderQuery.getTenantId());
                    ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(e.getOrderId());
                    if (Objects.nonNull(electricityConfig) && Objects.equals(ElectricityConfig.ENABLE_SELF_OPEN, electricityConfig.getIsEnableSelfOpen()) && Objects
                            .nonNull(electricityExceptionOrderStatusRecord) && Objects
                            .equals(electricityExceptionOrderStatusRecord.getIsSelfOpenCell(), ElectricityExceptionOrderStatusRecord.NOT_SELF_OPEN_CELL)) {
                        if (Objects.equals(electricityExceptionOrderStatusRecord.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)
                                && (System.currentTimeMillis() - electricityExceptionOrderStatusRecord.getCreateTime()) / 1000 / 60 <= 3) {
                            e.setSelfOpenCell(ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY);
                        }
                    }
                }
                
                // 设置加盟商名称
                Franchisee franchisee = franchiseeService.queryByIdFromCache(e.getFranchiseeId());
                if (Objects.nonNull(franchisee)) {
                    e.setFranchiseeName(franchisee.getName());
                }
                
            });
        }
        
        return R.ok(electricityCabinetOrderVOList);
    }
    
    private void isConformSpecialScene(ElectricityCabinetOrderVO electricityCabinetOrder, Integer tenantId) {
        if (Objects.isNull(electricityCabinetOrder.getElectricityCabinetId()) || Objects.isNull(electricityCabinetOrder.getOldCellNo())) {
            log.warn("isConformSpecialScene.electricityCabinetOrder info is null, eid is{}", electricityCabinetOrder.getElectricityCabinetId());
            return;
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.isNull(electricityConfig) || Objects.equals(ElectricityConfig.DISABLE_SELF_OPEN, electricityConfig.getIsEnableSelfOpen())) {
            log.warn("isConformSpecialScene.electricityConfig is null,tenantId is {}", tenantId);
            return;
        }
        
        ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService
                .queryByOrderId(electricityCabinetOrder.getOrderId());
        if (Objects.isNull(electricityExceptionOrderStatusRecord) || Objects
                .equals(electricityExceptionOrderStatusRecord.getIsSelfOpenCell(), ElectricityExceptionOrderStatusRecord.SELF_OPEN_CELL)) {
            log.warn("isConformSpecialScene.electricityExceptionOrderStatusRecord is null or selfOpenCell, orderId is {}", electricityCabinetOrder.getOrderId());
            return;
        }
        
        if (Objects.equals(electricityExceptionOrderStatusRecord.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)
                && Double.valueOf(System.currentTimeMillis() - electricityExceptionOrderStatusRecord.getCreateTime()) / 1000 / 60 <= 3) {
            CompletableFuture<Integer> isExistNewExchangeOrderFuture = CompletableFuture.supplyAsync(() -> {
                return electricityCabinetOrderMapper
                        .existSameCabinetCellSameTimeOpenExchangeOrder(electricityCabinetOrder.getId(), electricityCabinetOrder.getElectricityCabinetId(),
                                electricityCabinetOrder.getOldCellNo());
            }, executorServiceWrapper).exceptionally(e -> {
                log.error("isConformSpecialScene.isExistNewExchangeOrderFuture is error, ,orderId is :{} !", electricityCabinetOrder.getOrderId(), e);
                return null;
            });
            
            CompletableFuture<Integer> isExistNewReturnOrderFuture = CompletableFuture.supplyAsync(() -> {
                return rentBatteryOrderService
                        .existSameCabinetCellSameTimeOpenReturnOrder(electricityCabinetOrder.getCreateTime(), electricityCabinetOrder.getElectricityCabinetId(),
                                electricityCabinetOrder.getOldCellNo());
            }, executorServiceWrapper).exceptionally(e -> {
                log.error("isConformSpecialScene.isExistNewReturnOrderFuture is error, ,orderId is :{}  !", electricityCabinetOrder.getOrderId(), e);
                return null;
            });
            
            CompletableFuture<Integer> isExistNewOperRecordFuture = CompletableFuture.supplyAsync(() -> {
                return electricityCabinetPhysicsOperRecordService
                        .existSameCabinetCellSameTimeOpenRecord(electricityCabinetOrder.getCreateTime(), electricityCabinetOrder.getElectricityCabinetId(),
                                electricityCabinetOrder.getOldCellNo());
            }, executorServiceWrapper).exceptionally(e -> {
                log.error("isConformSpecialScene.isExistNewOperRecordFuture is error,orderId is :{} !", electricityCabinetOrder.getOrderId(), e);
                return null;
            });
            
            try {
                CompletableFuture.allOf(isExistNewExchangeOrderFuture, isExistNewReturnOrderFuture, isExistNewOperRecordFuture).get();
                Integer isExistNewExchangeOrder = isExistNewExchangeOrderFuture.get();
                Integer isExistNewReturnOrder = isExistNewReturnOrderFuture.get();
                Integer isExistNewOperRecord = isExistNewOperRecordFuture.get();
                log.debug("isConformSpecialScene start selfOpen check, orderId is {}，isExistNewExchangeOrder is {},isExistNewReturnOrder is {},isExistNewOperRecord is {} ",
                        electricityCabinetOrder.getOrderId(), isExistNewExchangeOrder, isExistNewReturnOrder, isExistNewOperRecord);
                if (Objects.isNull(isExistNewExchangeOrder) && Objects.isNull(isExistNewReturnOrder) && Objects.isNull(isExistNewOperRecord)) {
                    electricityCabinetOrder.setSelfOpenCell(ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY);
                }
            } catch (Exception e) {
                log.error("isConformSpecialScene.complateFuture is error", e);
            }
            
        }
        
    }
    
    @Override
    @Slave
    public R queryCount(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        Integer orderMode = electricityCabinetOrderQuery.getOrderMode();
        if (Objects.isNull(orderMode) || Objects.equals(orderMode, OrderDataModeEnums.CURRENT_ORDER.getCode())) {
            return R.ok(electricityCabinetOrderMapper.queryCount(electricityCabinetOrderQuery));
        } else {
            return electricityCabinetOrderHistoryService.queryCount(electricityCabinetOrderQuery);
        }
    }
    
    @Slave
    @Override
    public Integer homepageExchangeOrderSumCount(HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery) {
        return electricityCabinetOrderMapper.homepageExchangeOrderSumCount(homepageElectricityExchangeFrequencyQuery);
    }
    
    @Slave
    @Override
    public List<HomepageElectricityExchangeFrequencyVo> homepageExchangeFrequency(HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery) {
        return electricityCabinetOrderMapper.homepageExchangeFrequency(homepageElectricityExchangeFrequencyQuery);
    }
    
    @Slave
    @Override
    public List<HomepageElectricityExchangeFrequencyVo> homepageExchangeFrequencyCount(HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery) {
        return electricityCabinetOrderMapper.homepageExchangeFrequencyCount(homepageElectricityExchangeFrequencyQuery);
    }
    
    @Slave
    @Override
    public Integer queryCountForScreenStatistic(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        return electricityCabinetOrderMapper.queryCount(electricityCabinetOrderQuery);
    }
    
    @Override
    @Transactional
    public R endOrder(String orderId) {
        //结束异常订单只改订单状态，不用考虑其他
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper.selectOne(
                Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, orderId)
                        .eq(ElectricityCabinetOrder::getTenantId, TenantContextHolder.getTenantId())
                        .notIn(ElectricityCabinetOrder::getStatus, ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS, ElectricityCabinetOrder.ORDER_CANCEL,
                                ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL));
        if (Objects.isNull(electricityCabinetOrder)) {
            log.warn("ELECTRICITY  WARN! not found order,orderId={} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCabinetOrder.getUid());
        if (Objects.isNull(userInfo)) {
            return R.fail("ELECTRICITY.0015", "未找到用户");
        }
        
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.ORDER_CANCEL) || Objects
                .equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL)) {
            return R.fail("100230", "订单状态异常");
        }
        
        ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
        newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
        newElectricityCabinetOrder.setOrderSeq(ElectricityCabinetOrder.STATUS_ORDER_EXCEPTION_CANCEL);
        newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL);
        newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrderMapper.updateById(newElectricityCabinetOrder);
        
        //回退单电套餐次数
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            if (Objects.nonNull(userBatteryMemberCard)) {
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                if (Objects.nonNull(batteryMemberCard) && Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)) {
                    userBatteryMemberCardService.plusCount(userBatteryMemberCard.getUid());
                }
            }
        }
        
        //回退车电一体套餐次数
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            carRentalPackageMemberTermBizService.addResidue(userInfo.getTenantId(), userInfo.getUid());
        }
        
        //删除开门失败缓存
        redisService.delete(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId);
        
        return R.ok();
    }
    
    
    @Override
    public Integer homeOneCount(Long first, Long now, List<Integer> eleIdList, Integer tenantId) {
        Integer count = electricityCabinetOrderMapper.homeOneCount(first, now, eleIdList, tenantId);
        Integer historyCount = electricityCabinetOrderHistoryService.homeOneCount(first, now, eleIdList, tenantId);
        return count + historyCount;
    }
    
    @Slave
    @Override
    public BigDecimal homeOneSuccess(Long first, Long now, List<Integer> eleIdList, Integer tenantId) {
        Integer countTotal = homeOneCount(first, now, eleIdList, tenantId);
        Integer successTotal = electricityCabinetOrderMapper.homeOneSuccess(first, now, eleIdList, tenantId);
        Integer historySuccessTotal = electricityCabinetOrderHistoryService.homeOneSuccess(first, now, eleIdList, tenantId);
        successTotal += historySuccessTotal;
        if (successTotal == 0 || countTotal == 0) {
            return BigDecimal.valueOf(0);
        }
        return BigDecimal.valueOf(successTotal).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(countTotal), BigDecimal.ROUND_HALF_EVEN);
    }
    
    @Slave
    @Override
    public List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay, List<Integer> eleIdList, Integer tenantId) {
        return electricityCabinetOrderMapper.homeThree(startTimeMilliDay, endTimeMilliDay, eleIdList, tenantId);
    }
    
    @Override
    public Integer homeMonth(Long uid, Long first, Long now) {
        return electricityCabinetOrderMapper.selectCount(
                new LambdaQueryWrapper<ElectricityCabinetOrder>().between(ElectricityCabinetOrder::getCreateTime, first, now).eq(ElectricityCabinetOrder::getUid, uid));
    }
    
    @Override
    public Integer homeTotal(Long uid) {
        return electricityCabinetOrderMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getUid, uid));
    }
    
    
    @Override
    public ElectricityCabinetOrder queryByUid(Long uid) {
        return electricityCabinetOrderMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getUid, uid)
                .notIn(ElectricityCabinetOrder::getStatus, ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS, ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL,
                        ElectricityCabinetOrder.ORDER_CANCEL).orderByDesc(ElectricityCabinetOrder::getCreateTime).last("limit 0,1"));
    }
    
    @Override
    public ElectricityCabinetOrder queryByCellNoAndEleId(Integer eleId, Integer cellNo) {
        return electricityCabinetOrderMapper.selectOne(
                new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getElectricityCabinetId, eleId).eq(ElectricityCabinetOrder::getOldCellNo, cellNo).or()
                        .eq(ElectricityCabinetOrder::getNewCellNo, cellNo).orderByDesc(ElectricityCabinetOrder::getCreateTime).last("limit 0,1"));
    }
    
    @Override
    public String findUsableCellNo(Integer id) {
        List<ElectricityCabinetBox> usableBoxes = electricityCabinetBoxService.queryNoElectricityBatteryBox(id);
        if (!DataUtil.collectionIsUsable(usableBoxes)) {
            return null;
        }
        
        List<Integer> boxes = usableBoxes.stream().map(ElectricityCabinetBox::getCellNo).map(Integer::parseInt).sorted(Integer::compareTo).collect(Collectors.toList());
        
        //查看有没有初始化过设备的上次操作过的格挡,这里不必关心线程安全，不需要保证原子性
        if (!redisService.hasKey(CacheConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id)) {
            redisService.setNx(CacheConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id, boxes.get(0).toString());
        }
        
        String lastCellNo = redisService.get(CacheConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id);
        
        boxes = rebuildByCellCircleForDevice(boxes, Integer.parseInt(lastCellNo));
        
        for (Integer box : boxes) {
            if (redisService.setNx(CacheConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + id + "_" + box.toString(), "1", 300 * 1000L, false)) {
                redisService.set(CacheConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id, box.toString());
                return box.toString();
            }
        }
        
        return null;
    }
    
    @Override
    public R queryNewStatus(String orderId) {
        
        Map<String, Object> map = new HashMap<>();
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper
                .selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, orderId));
        if (Objects.isNull(electricityCabinetOrder)) {
            log.warn("ELECTRICITY  WARN! not found order,orderId={} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        String status = electricityCabinetOrder.getStatus();
        
        //订单状态旧门开门中
        if (electricityCabinetOrder.getOrderSeq() < ElectricityCabinetOrder.STATUS_INIT_BATTERY_CHECK_SUCCESS) {
            status = electricityCabinetOrder.getOldCellNo() + "号仓门开门中";
        }
        
        //订单状态新门开门中
        if (electricityCabinetOrder.getOrderSeq() > ElectricityCabinetOrder.STATUS_CHECK_OLD_AND_NEW
                && electricityCabinetOrder.getOrderSeq() < ElectricityCabinetOrder.STATUS_COMPLETE_OPEN_SUCCESS) {
            status = electricityCabinetOrder.getNewCellNo() + "号仓门开门中";
        }
        
        //旧电池开门成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_OPEN_SUCCESS)) {
            status = electricityCabinetOrder.getOldCellNo() + "号仓门开门成功，电池检测中";
        }
        
        //旧电池检测成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
            status = "旧电池已存入," + electricityCabinetOrder.getNewCellNo() + "号仓门开门中";
        }
        
        //订单状态新门成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)) {
            status = electricityCabinetOrder.getNewCellNo() + "号仓门开门成功，电池检测中";
        }
        
        //订单状态新电池取走
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            status = "新电池已取走,订单完成";
        }
        
        //订单状态
        map.put("status", status);
        
        //页面图片显示
        Integer picture = 0;
        
        //return
        if (electricityCabinetOrder.getOrderSeq() < ElectricityCabinetOrder.STATUS_CHECK_OLD_AND_NEW) {
            picture = 1;
        }
        
        //rent
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS) || Objects
                .equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_OPEN_SUCCESS) || Objects
                .equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS) || Objects
                .equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            picture = 2;
        }
        
        //error
        if (electricityCabinetOrder.getOrderSeq().equals(ElectricityCabinetOrder.STATUS_ORDER_CANCEL) || electricityCabinetOrder.getOrderSeq()
                .equals(ElectricityCabinetOrder.STATUS_ORDER_EXCEPTION_CANCEL)) {
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinetOrder.getTenantId());
            
            if (Objects.nonNull(electricityConfig) && Objects.equals(ElectricityConfig.ENABLE_SELF_OPEN, electricityConfig.getIsEnableSelfOpen())) {
                ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(orderId);
                if (Objects.nonNull(electricityExceptionOrderStatusRecord) && Objects
                        .equals(electricityExceptionOrderStatusRecord.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)) {
                    map.put("selfOpenCell", ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY);
                }
            }
            
            picture = 3;
        }
        
        //订单状态
        map.put("picture", picture);
        
        //是否出错 0--未出错 1--出错
        Integer type = 0;
        //是否重试 0--重试  1--不能重试
        Integer isTry = 1;
        
        String result = redisService.get(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId);
        if (StringUtils.isNotEmpty(result)) {
            WarnMsgVo warnMsgVo = JsonUtil.fromJson(result, WarnMsgVo.class);
            boolean isNeedEndOrder = warnMsgVo.getIsNeedEndOrder();
            if (!isNeedEndOrder) {
                isTry = 0;
            }
            
            String msg = warnMsgVo.getMsg();
            
            //出错信息
            map.put("queryStatus", msg);
            type = 1;
        }
        
        map.put("type", type);
        map.put("isTry", isTry);
        log.info("map is -->{}", map);
        return R.ok(map);
    }
    
    @Override
    public R selfOpenCell(OrderSelfOpenCellQuery orderSelfOpenCellQuery) {
        
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("self open cell order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //是否存在未完成的租电池订单
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(user.getUid());
        if (Objects.nonNull(rentBatteryOrder)) {
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "100023", "存在未完成租电订单，不能自助开仓");
            } else if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "100024", "存在未完成还电订单，不能自助开仓");
            }
        }
        
        ElectricityCabinetOrder oldElectricityCabinetOrder = queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "100022", "存在未完成换电订单，不能自助开仓");
        }
        
        ElectricityCabinetOrder electricityCabinetOrder = queryByOrderId(orderSelfOpenCellQuery.getOrderId());
        if (Objects.isNull(electricityCabinetOrder)) {
            log.warn("self open cell WARN! not found order,orderId={} ", orderSelfOpenCellQuery.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.warn("self open cell WARN! not found electricityCabinet ！electricityCabinetId={}", electricityCabinetOrder.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        if (!eleResult) {
            log.warn("self open cell WARN!  electricityCabinet is offline ！electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }
        
        //换电柜营业时间
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }
        
        //        //下单锁住柜机
        //        boolean result = redisService.setNx(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 60 * 1000L, false);
        //        if (!result) {
        //            return R.fail("ELECTRICITY.00105", "该柜机有人正在下单，请稍等片刻");
        //        }
        
        ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService
                .queryByOrderId(orderSelfOpenCellQuery.getOrderId());
        
        Long now = System.currentTimeMillis();
        if (Objects.isNull(electricityExceptionOrderStatusRecord) || !Objects
                .equals(electricityExceptionOrderStatusRecord.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)) {
            log.warn("SELF OPEN CELL WARN! not old cell exception,orderId={}", orderSelfOpenCellQuery.getOrderId());
            return R.fail("100020", "非旧仓门异常无法自主开仓");
        }
        
        if (Double.valueOf(now - electricityExceptionOrderStatusRecord.getCreateTime()) / 1000 / 60 > 3) {
            log.warn("SELF OPEN CELL WARN! self open cell timeout,orderId={}", orderSelfOpenCellQuery.getOrderId());
            return R.fail("100026", "自助开仓已超开仓时间");
        }
        
        if (Objects.equals(electricityExceptionOrderStatusRecord.getIsSelfOpenCell(), ElectricityExceptionOrderStatusRecord.SELF_OPEN_CELL)) {
            log.warn("SELF OPEN CELL WARN! self open cell fail,orderId={}", orderSelfOpenCellQuery.getOrderId());
            return R.fail("100021", "该订单已进行自助开仓");
        }
        
        //查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            log.warn("self open cell order  WARN! not found store ！electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.warn("self open cell order  WARN! not found store ！storeId={}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        
        //查找门店加盟商
        if (Objects.isNull(store.getFranchiseeId())) {
            log.warn("self open cell order  WARN! not found Franchisee ！storeId={}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }
        
        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("self open cell order  WARN! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("self open cell order WARN! user is unUsable! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("self open cell order WARN! user not auth,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        
        //判断该换电柜加盟商和用户加盟商是否一致
        if (!Objects.equals(store.getFranchiseeId(), userInfo.getFranchiseeId())) {
            log.warn("self open cell order  WARN!FranchiseeId is not equal!uid={} , FranchiseeId1={} ,FranchiseeId2={}", user.getUid(), store.getFranchiseeId(),
                    userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
        }
        
      /*  //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("self open cell order  ERROR! not pay deposit,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }*/
        
        //未租电池
        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("self open cell order  WARN! user not rent battery,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }
        
        ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService
                .queryByCellNo(electricityCabinetOrder.getElectricityCabinetId(), electricityExceptionOrderStatusRecord.getCellNo() + "");
        if (Objects.isNull(electricityCabinetBox)) {
            log.warn("self open cell order  WARN! not find cellNO! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0006", "未找到此仓门");
        }
        
        // 自主开仓特殊场景校验
        if (Objects.nonNull(electricityCabinetOrder.getElectricityCabinetId()) && Objects.nonNull(electricityExceptionOrderStatusRecord.getCellNo())) {
            Integer isExistNewExchangeOrder = electricityCabinetOrderMapper
                    .existSameCabinetCellSameTimeOpenExchangeOrder(electricityCabinetOrder.getId(), electricityCabinetOrder.getElectricityCabinetId(),
                            electricityExceptionOrderStatusRecord.getCellNo());
            if (Objects.nonNull(isExistNewExchangeOrder)) {
                log.warn("selfOpenCell.existExchangeOrder, orderId is {}", electricityCabinetOrder.getOrderId());
                return R.fail("100666", "系统识别归还仓门内电池为新订单，无法执行自助开仓操作");
            }
            Integer isExistNewReturnOrder = rentBatteryOrderService
                    .existSameCabinetCellSameTimeOpenReturnOrder(electricityCabinetOrder.getCreateTime(), electricityCabinetOrder.getElectricityCabinetId(),
                            electricityExceptionOrderStatusRecord.getCellNo());
            if (Objects.nonNull(isExistNewReturnOrder)) {
                log.warn("selfOpenCell.existNewReturnOrder, orderId is {}", electricityCabinetOrder.getOrderId());
                return R.fail("100666", "系统识别归还仓门内电池为新订单，无法执行自助开仓操作");
            }
            Integer isExistNewOperRecord = electricityCabinetPhysicsOperRecordService
                    .existSameCabinetCellSameTimeOpenRecord(electricityCabinetOrder.getCreateTime(), electricityCabinetOrder.getElectricityCabinetId(),
                            electricityExceptionOrderStatusRecord.getCellNo());
            if (Objects.nonNull(isExistNewOperRecord)) {
                log.warn("selfOpenCell.existNewOperRecord, orderId is {}", electricityCabinetOrder.getOrderId());
                return R.fail("100666", "系统识别归还仓门内电池为新订单，无法执行自助开仓操作");
            }
        }
        
        try {
            ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecordUpdate = new ElectricityExceptionOrderStatusRecord();
            electricityExceptionOrderStatusRecordUpdate.setId(electricityExceptionOrderStatusRecord.getId());
            electricityExceptionOrderStatusRecordUpdate.setUpdateTime(System.currentTimeMillis());
            electricityExceptionOrderStatusRecordUpdate.setIsSelfOpenCell(ElectricityExceptionOrderStatusRecord.SELF_OPEN_CELL);
            electricityExceptionOrderStatusRecordService.update(electricityExceptionOrderStatusRecordUpdate);
            
            if (electricityCabinet.getVersion().isBlank() || VersionUtil.compareVersion(electricityCabinet.getVersion(), ElectricityCabinetOrderOperHistory.THREE_PERIODS_SUCCESS_RATE_VERSION) < 0) {
                ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder().createTime(System.currentTimeMillis())
                        .orderId(orderSelfOpenCellQuery.getOrderId()).tenantId(electricityCabinet.getTenantId()).msg("旧电池检测失败，自助开仓")
                        .seq(ElectricityCabinetOrderOperHistory.SELF_OPEN_CELL_SEQ).type(ElectricityCabinetOrderOperHistory.ORDER_TYPE_EXCHANGE)
                        .result(ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS).build();
                
                electricityCabinetOrderOperHistoryService.insert(history);
            }
            
            ElectricityCabinetOrder electricityCabinetOrderUpdate = new ElectricityCabinetOrder();
            electricityCabinetOrderUpdate.setId(electricityCabinetOrder.getId());
            electricityCabinetOrderUpdate.setUpdateTime(System.currentTimeMillis());
            electricityCabinetOrderUpdate.setRemark("自助开仓");
            update(electricityCabinetOrderUpdate);
            
            //发送自助开仓命令
            //发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("orderId", orderSelfOpenCellQuery.getOrderId());
            dataMap.put("cellNo", electricityExceptionOrderStatusRecord.getCellNo());
            dataMap.put("batteryName", electricityCabinetOrder.getOldElectricityBatterySn());
            dataMap.put("userSelfOpenCell", true);
            
            String sessionId = CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId();
            
            HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(sessionId).data(dataMap).productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.SELF_OPEN_CELL).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
            return R.ok(sessionId);
        } catch (Exception e) {
            log.error("order is error" + e);
            return R.fail("ELECTRICITY.0025", "自助开仓失败");
        } finally {
            redisService.delete(CacheConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + electricityCabinetOrder.getElectricityCabinetId() + "_"
                    + electricityExceptionOrderStatusRecord.getCellNo());
        }
        
    }
    
    @Override
    public R checkOpenSessionId(String sessionId) {
        String s = redisService.get(CacheConstant.ELE_OPERATOR_SELF_OPEN_CEE_CACHE_KEY + sessionId);
        if (StrUtil.isEmpty(s)) {
            return R.ok("0001");
        }
        if ("true".equalsIgnoreCase(s)) {
            return R.ok("0002");
        } else {
            return R.ok("0003");
        }
    }
    
    public static List<Integer> rebuildByCellCircleForDevice(List<Integer> cellNos, Integer lastCellNo) {
        
        if (cellNos.get(0) > lastCellNo) {
            return cellNos;
        }
        
        int index = 0;
        
        for (int i = 0; i < cellNos.size(); i++) {
            if (cellNos.get(i) > lastCellNo) {
                index = i;
                break;
            }
            
            if (cellNos.get(i).equals(lastCellNo)) {
                index = i + 1;
                break;
            }
        }
        
        List<Integer> firstSegmentList = cellNos.subList(0, index);
        List<Integer> twoSegmentList = cellNos.subList(index, cellNos.size());
        
        ArrayList<Integer> resultList = com.google.common.collect.Lists.newArrayList();
        resultList.addAll(twoSegmentList);
        resultList.addAll(firstSegmentList);
        
        return resultList;
    }
    
    @Deprecated
    public String generateOrderId(Integer id, String cellNo, Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + id + cellNo + uid;
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
    public Triple<Boolean, String, Object> orderV2(OrderQueryV2 orderQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ORDER ERROR!  not found user,eid={}", orderQuery.getEid());
            return Triple.of(false, "100001", "未能找到用户");
        }
        
        Triple<Boolean, String, Object> checkExistsOrderResult = checkUserExistsUnFinishOrder(user.getUid());
        if (checkExistsOrderResult.getLeft()) {
            log.warn("ORDER WARN! user exists unFinishOrder! uid={}", user.getUid());
            return Triple.of(false, checkExistsOrderResult.getMiddle(), checkExistsOrderResult.getRight());
        }
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(orderQuery.getEid());
        if (Objects.isNull(electricityCabinet)) {
            return Triple.of(false, "100003", "柜机不存在");
        }
        
        //换电柜是否打烊
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            return Triple.of(false, "100203", "换电柜已打烊");
        }
        
        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        if (!eleResult) {
            return Triple.of(false, "100004", "柜机不在线");
        }
        
        //这里加柜机的缓存，为了限制不同时分配格挡
        if (!redisService.setNx(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "100214", "已有其他用户正在使用中，请稍后再试");
        }
        
        if (!redisService.setNx(CacheConstant.ORDER_TIME_UID + user.getUid(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "100002", "下单过于频繁");
        }
        
        try {
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.warn("ORDER WARN!  not found store ！uid={},eid={},storeId={}", user.getUid(), electricityCabinet.getId(), electricityCabinet.getStoreId());
                return Triple.of(false, "100204", "未找到门店");
            }
            
            //校验用户
            UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
            if (Objects.isNull(userInfo)) {
                log.warn("ORDER WARN! not found user info,uid={} ", user.getUid());
                return Triple.of(false, "100205", "未找到用户审核信息");
            }
            
            //用户是否可用
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("ORDER WARN! user is unUsable,uid={} ", user.getUid());
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }
            
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("ORDER WARN! userinfo is UN AUTH! uid={}", user.getUid());
                return Triple.of(false, "100206", "用户未审核");
            }
            
            //电子签署拦截
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
            if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsEnableEsign(), EleEsignConstant.ESIGN_ENABLE)) {
                EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordService.queryEsignFinishedRecordByUser(user.getUid(), Long.valueOf(user.getTenantId()));
                if (Objects.isNull(eleUserEsignRecord)) {
                    return Triple.of(false, "100329", "请先完成电子签名");
                }
            }
            
            //查询用户绑定的电池列表
            List<String> batteryTypeList = userBatteryTypeService.selectByUid(userInfo.getUid());
            
            Triple<Boolean, String, Object> rentBatteryResult = null;
            if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                //处理单电
                rentBatteryResult = handlerSingleExchangeBattery(userInfo, store, electricityCabinet, orderQuery, batteryTypeList);
                if (Boolean.FALSE.equals(rentBatteryResult.getLeft())) {
                    return rentBatteryResult;
                }
            } else if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
                //处理车电一体
                rentBatteryResult = handlerExchangeBatteryCar(userInfo, store, electricityCabinet, orderQuery, batteryTypeList);
                if (Boolean.FALSE.equals(rentBatteryResult.getLeft())) {
                    return rentBatteryResult;
                }
            } else {
                log.warn("RENTBATTERY WARN! not pay deposit,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
            }
            
            return rentBatteryResult;
        } catch (BizException e) {
            throw new BizException(e.getErrCode(), e.getErrMsg());
        } finally {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            redisService.delete(CacheConstant.ORDER_TIME_UID + user.getUid());
        }
    }
    
    
    @Override
    public Triple<Boolean, String, Object> orderV3(OrderQueryV3 orderQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ORDER ERROR!  not found user,eid={}", orderQuery.getEid());
            return Triple.of(false, "100001", "未能找到用户");
        }
        
        Triple<Boolean, String, Object> checkExistsOrderResult = checkUserExistsUnFinishOrder(user.getUid());
        if (checkExistsOrderResult.getLeft()) {
            log.warn("ORDER WARN! user exists unFinishOrder! uid={}", user.getUid());
            return Triple.of(false, checkExistsOrderResult.getMiddle(), checkExistsOrderResult.getRight());
        }
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(orderQuery.getEid());
        if (Objects.isNull(electricityCabinet)) {
            return Triple.of(false, "100003", "柜机不存在");
        }
        
        //换电柜是否打烊
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            return Triple.of(false, "100203", "换电柜已打烊");
        }
        
        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        if (!eleResult) {
            return Triple.of(false, "100004", "柜机不在线");
        }
        
        //这里加柜机的缓存，为了限制不同时分配格挡
        if (!redisService.setNx(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "100214", "已有其他用户正在使用中，请稍后再试");
        }
        
        if (!redisService.setNx(CacheConstant.ORDER_TIME_UID + user.getUid(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "100002", "下单过于频繁");
        }
        
        try {
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.warn("ORDER WARN!  not found store ！uid={},eid={},storeId={}", user.getUid(), electricityCabinet.getId(), electricityCabinet.getStoreId());
                return Triple.of(false, "100204", "未找到门店");
            }
            
            //校验用户
            UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
            if (Objects.isNull(userInfo)) {
                log.warn("ORDER WARN! not found user info,uid={} ", user.getUid());
                return Triple.of(false, "100205", "未找到用户审核信息");
            }
            
            //用户是否可用
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("ORDER WARN! user is unUsable,uid={} ", user.getUid());
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }
            
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("ORDER WARN! userinfo is UN AUTH! uid={}", user.getUid());
                return Triple.of(false, "100206", "用户未审核");
            }
            
            //电子签署拦截
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
            if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsEnableEsign(), EleEsignConstant.ESIGN_ENABLE)) {
                EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordService.queryEsignFinishedRecordByUser(user.getUid(), Long.valueOf(user.getTenantId()));
                if (Objects.isNull(eleUserEsignRecord)) {
                    return Triple.of(false, "100329", "请先完成电子签名");
                }
            }
            
            
            //查询用户绑定的电池列表
            List<String> batteryTypeList = userBatteryTypeService.selectByUid(userInfo.getUid());
            
            Triple<Boolean, String, Object> rentBatteryResult = null;
            if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                //处理单电
                rentBatteryResult = handlerSingleExchangeBatteryV3(userInfo, store, electricityCabinet, orderQuery, batteryTypeList);
                if (Boolean.FALSE.equals(rentBatteryResult.getLeft())) {
                    return rentBatteryResult;
                }
            } else if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
                //处理车电一体
                rentBatteryResult = handlerExchangeBatteryCarV3(userInfo, store, electricityCabinet, orderQuery, batteryTypeList);
                if (Boolean.FALSE.equals(rentBatteryResult.getLeft())) {
                    return rentBatteryResult;
                }
            } else {
                log.warn("RENTBATTERY WARN! not pay deposit,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
            }
            
            return rentBatteryResult;
        } catch (BizException e) {
            throw new BizException(e.getErrCode(), e.getErrMsg());
        } finally {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            redisService.delete(CacheConstant.ORDER_TIME_UID + user.getUid());
        }
    }
    
    @Override
    public Triple<Boolean, String, Object> selectionExchangeCheck(SelectionExchangeCheckQuery exchangeQuery) {
        // 判断用户信息
        Long uid = SecurityUtils.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        
        // 校验用户状态
        Triple<Boolean, String, Object> userStatus = verifyUserStatus(userInfo);
        if (Boolean.FALSE.equals(userStatus.getLeft())) {
            return userStatus;
        }
        
        //查询后台开关
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig.getIsSelectionExchange()) || Objects.equals(electricityConfig.getIsSelectionExchange(),
                SelectionExchageEunm.DISABLE_SELECTION_EXCHANGE.getCode())) {
            return Triple.of(false, "100551", "选仓换电开关已关闭");
        }
        
        Triple<Boolean, String, Object> checkExistsOrderResult = checkUserExistsUnFinishOrder(userInfo.getUid());
        if (checkExistsOrderResult.getLeft()) {
            log.warn("SelectionExchangeCheck EXCHANGE ORDER WARN! user exists unFinishOrder! uid={}", userInfo.getUid());
            return Triple.of(false, checkExistsOrderResult.getMiddle(), checkExistsOrderResult.getRight());
        }
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(exchangeQuery.getEid());
        
        //校验柜机状态
        OrderSelectionExchangeQuery query = new OrderSelectionExchangeQuery();
        query.setEid(exchangeQuery.getEid());
        Triple<Boolean, String, Object> cabinetStatus = verifyElectricityCabinetStatus(electricityCabinet, query);
        if (Boolean.FALSE.equals(cabinetStatus.getLeft())) {
            return cabinetStatus;
        }
        
        //校验加盟商
        Franchisee franchisee = franchiseeService.queryByIdFromCache(electricityCabinet.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("SelectionExchangeCheck EXCHANGE ORDER WARN! not found franchisee,franchiseeId={},uid={}", electricityCabinet.getFranchiseeId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        //柜机加盟商与用户加盟商不一致
        if (!Objects.equals(franchisee.getId(), userInfo.getFranchiseeId())) {
            log.warn("SelectionExchangeCheck EXCHANGE ORDER WARN! user fId  is not equal franchiseeId uid={} ,fid={}", userInfo.getUid(), userInfo.getFranchiseeId());
            return Triple.of(false, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
        }
        
        //校验门店信息
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.warn("SelectionExchangeCheck EXCHANGE ORDER WARN!  not found store!uid={},eid={},storeId={}", userInfo.getUid(), electricityCabinet.getId(), electricityCabinet.getStoreId());
            return Triple.of(false, "100204", "未找到门店");
        }
        
        //查询用户绑定的电池
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
        
        // 多次换电拦截
        if (StringUtils.isNotBlank(electricityCabinet.getVersion()) && VersionUtil.compareVersion(electricityCabinet.getVersion(), ORDER_LESS_TIME_EXCHANGE_CABINET_VERSION) >= 0) {
            LessTimeExchangeDTO exchangeDTO = LessTimeExchangeDTO.builder().eid(exchangeQuery.getEid()).isReScanExchange(exchangeQuery.getIsReScanExchange()).build();
            Pair<Boolean, Object> pair = this.lessTimeExchangeTwoCountAssert(userInfo, electricityCabinet, electricityBattery, exchangeDTO, OrderCheckEnum.CHECK.getCode());
            if (pair.getLeft()) {
                return Triple.of(true, null, pair.getRight());
            }
        }
        
        // 如果超过5分钟或者返回false，前端不进行弹窗
        ExchangeUserSelectVo vo = new ExchangeUserSelectVo();
        vo.setIsEnterMoreExchange(ExchangeUserSelectVo.NOT_ENTER_MORE_EXCHANGE);
        return Triple.of(true, null, vo);
        
    }
    
    /**
     * @return Boolean=false继续走正常换电
     */
    private Pair<Boolean, Object> lessTimeExchangeTwoCountAssert(UserInfo userInfo, ElectricityCabinet cabinet, ElectricityBattery electricityBattery,
            LessTimeExchangeDTO exchangeDTO, Integer code) {
        if (Objects.equals(exchangeDTO.getIsReScanExchange(), OrderQueryV3.RESCAN_EXCHANGE)) {
            log.info("OrderV3 INFO! not same cabinet, normal exchange");
            return Pair.of(false, null);
        }
       
        Long uid = userInfo.getUid();
        ElectricityCabinetOrder lastOrder = electricityCabinetOrderMapper.selectLatelyExchangeOrder(uid, System.currentTimeMillis());
        if (Objects.isNull(lastOrder)) {
            log.warn("OrderV3 WARN! lowTimeExchangeTwoCountAssert.lastOrder is null, currentUid is {}", uid);
            return Pair.of(false, null);
        }
        
        // 默认取5分钟的订单，可选择配置
        Long scanTime = StrUtil.isEmpty(exchangeConfig.getScanTime()) ? 180000L : Long.valueOf(exchangeConfig.getScanTime());
        log.info("OrderV3 INFO! lessTimeExchangeTwoCountAssert.scanTime is {} ,currentTime is {}", scanTime, System.currentTimeMillis());
        
        if (System.currentTimeMillis() - lastOrder.getCreateTime() > scanTime) {
            log.warn("OrderV3 WARN! lowTimeExchangeTwoCountAssert.lastOrder over 5 minutes,lastOrderId is {} ", lastOrder.getOrderId());
            return Pair.of(false, null);
        }
        
        // 扫码柜机和订单不是同一个柜机进行处理
        if (!Objects.equals(lastOrder.getElectricityCabinetId(), exchangeDTO.getEid())) {
            log.warn("OrderV3 WARN! scan eid not equal order eid, orderEid is {}, scanEid is {}", lastOrder.getElectricityCabinetId(), exchangeDTO.getEid());
            return scanCabinetNotEqualOrderCabinetHandler(userInfo, electricityBattery, lastOrder);
        }
        
        if (Objects.equals(lastOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            // 上一个成功
            return lastExchangeSuccessHandler(lastOrder, cabinet, electricityBattery, userInfo);
        } else {
            // 上一个失败
            return lastExchangeFailHandler(lastOrder, cabinet, electricityBattery, userInfo, code);
        }
    }
    
    private Pair<Boolean, Object> scanCabinetNotEqualOrderCabinetHandler(UserInfo userInfo, ElectricityBattery electricityBattery, ElectricityCabinetOrder lastOrder) {
        // 用户绑定的电池为空，走正常换电
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.warn("OrderV3 WARN! scan eid not equal order eid, userBindingBatterySn is null, uid is {}", userInfo.getUid());
            return Pair.of(false, null);
        }
        
        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(electricityBattery.getSn(), lastOrder.getElectricityCabinetId());
        if (Objects.isNull(cabinetBox)) {
            log.warn("OrderV3 WARN! userBindingBatterySn.cabinetBox is null, sn is {}", electricityBattery.getSn());
            return Pair.of(false, null);
        }
        
        // 用户电池在上一个柜机，并且仓门关闭
        if (Objects.equals(lastOrder.getElectricityCabinetId(), cabinetBox.getElectricityCabinetId())) {
            // 返回柜机名称和重新扫码标识
            ElectricityCabinet orderCabinet = electricityCabinetService.queryByIdFromCache(lastOrder.getElectricityCabinetId());
            if (Objects.isNull(orderCabinet)) {
                log.error("OrderV3 ERROR! lastOrder.cabinet is null, eid is {}", lastOrder.getElectricityCabinetId());
                return Pair.of(false, null);
            }
            ExchangeUserSelectVo vo = ExchangeUserSelectVo.builder().isTheSameCabinet(ExchangeUserSelectVo.NOT_SAME_CABINET).cabinetName(orderCabinet.getName()).build();
            return Pair.of(true, vo);
        } else {
            return Pair.of(false, null);
        }
    }
    
    /**
     * 是否符合自主开仓
     * @return
     */
    private Boolean isSatisfySelfOpenCondition(ElectricityCabinetOrder order, Integer cell, Integer newOrOldCellFlag) {
        if (Objects.isNull(cell)){
            log.error("orderV3 Error! isSatisfySelfOpenCondition.params.cell is null");
            return false;
        }
        // 上个订单+5分钟是否存在换电、退电、操作记录
        Long startTime = order.getUpdateTime();
        Long endTime = startTime + 1000 * 60 * 5;
        Integer eid = order.getElectricityCabinetId();
        Integer existExchangeOrder = electricityCabinetOrderMapper.existExchangeOrderInSameCabinetAndCell(order.getId(), endTime, eid, cell, newOrOldCellFlag);
        if (Objects.nonNull(existExchangeOrder)) {
            log.warn("orderV3 warn! isSatisfySelfOpenCondition.existExchangeOrder, orderId:{}", order.getOrderId());
            return false;
        }
        Integer existReturnOrder = rentBatteryOrderService.existReturnOrderInSameCabinetAndCell(startTime, endTime, eid, cell);
        if (Objects.nonNull(existReturnOrder)) {
            log.warn("orderV3 warn! isSatisfySelfOpenCondition.existReturnOrder, orderId:{}", order.getOrderId());
            return false;
        }
        Integer existOpenRecord = electricityCabinetPhysicsOperRecordService.existOpenRecordInSameCabinetAndCell(startTime, endTime, eid, cell);
        if (Objects.nonNull(existOpenRecord)) {
            log.warn("orderV3 warn! isSatisfySelfOpenCondition.existOpenRecord, orderId:{}", order.getOrderId());
            return false;
        }
        return true;
    }
    
    private Pair<Boolean, Object> lastExchangeSuccessHandler(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery,
            UserInfo userInfo) {
        // 上次成功不可能为空
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.error("OrderV3 Error! lastExchangeSuccessHandler.userBindBattery is null, lastOrderId is {}", lastOrder.getOrderId());
            throw new CustomBusinessException("上次换电成功，但是用户绑定电池为空");
        }
        
        ExchangeUserSelectVo vo = new ExchangeUserSelectVo();
        vo.setIsEnterMoreExchange(ExchangeUserSelectVo.ENTER_MORE_EXCHANGE);
        vo.setLastExchangeIsSuccess(ExchangeUserSelectVo.LAST_EXCHANGE_SUCCESS);
        
        // 自主开仓条件校验
        if (!this.isSatisfySelfOpenCondition(lastOrder, lastOrder.getNewCellNo(), ElectricityCabinetOrder.NEW_CELL)) {
            vo.setIsSatisfySelfOpen(ExchangeUserSelectVo.NOT_SATISFY_SELF_OPEN);
            log.warn("OrderV3 WARN! lastExchangeSuccessHandler is not satisfySelfOpenCondition, orderId is{}", lastOrder.getOrderId());
            return Pair.of(true, vo);
        }
        
        vo.setIsSatisfySelfOpen(ExchangeUserSelectVo.IS_SATISFY_SELF_OPEN);
        vo.setCabinetName(cabinet.getName());
        vo.setOrderId(lastOrder.getOrderId());
        // 上一次成功，这次只能返回新仓门
        vo.setCell(lastOrder.getNewCellNo());
        
        // 用户电池是否在仓
        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(electricityBattery.getSn(), cabinet.getId());
        
        log.info("OrderV3 INFO! lastExchangeSuccessHandler.cabinetBox is {}, lastOrder is {}", Objects.nonNull(cabinetBox) ? JsonUtil.toJson(cabinetBox) : "null",
                JsonUtil.toJson(lastOrder));
        // 电池在仓，并且电池所在仓门=上个订单的新仓门
        if (Objects.nonNull(cabinetBox) && StrUtil.isNotBlank(cabinetBox.getCellNo()) && Objects.equals(Integer.valueOf(cabinetBox.getCellNo()), lastOrder.getNewCellNo())) {
            // 在仓内，分配上一个订单的新仓门
            vo.setIsBatteryInCell(ExchangeUserSelectVo.BATTERY_IN_CELL);
            // 如果是换电，才要后台自主开仓
            String sessionId = this.backSelfOpen(lastOrder.getNewCellNo(), electricityBattery.getSn(), lastOrder, cabinet, "后台自助开仓");
            vo.setSessionId(sessionId);
            
            return Pair.of(true, vo);
        } else {
            // 没有在仓
            vo.setIsBatteryInCell(ExchangeUserSelectVo.BATTERY_NOT_CELL);
            return Pair.of(true, vo);
        }
    }
    
    
    private Pair<Boolean, Object> lastExchangeFailHandler(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery, UserInfo userInfo, Integer code) {
        ExchangeUserSelectVo vo = new ExchangeUserSelectVo();
        vo.setIsEnterMoreExchange(ExchangeUserSelectVo.ENTER_MORE_EXCHANGE);
        vo.setLastExchangeIsSuccess(ExchangeUserSelectVo.LAST_EXCHANGE_FAIL);
        
        String orderStatus = lastOrder.getOrderStatus();
        if (StrUtil.isEmpty(orderStatus)) {
            log.info("OrderV3 INFO! lastExchangeFailHandler.orderStatus is null, orderId is {}", lastOrder.getOrderId());
            return Pair.of(false, null);
        }
        
        log.info("OrderV3 INFO! lastExchangeFailHandler.orderStatus is {}", orderStatus);
        //  旧仓门电池检测失败或超时 或者 旧仓门开门失败
        if (Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_OPEN_FAIL) || Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)) {
            return oldCellCheckFail(lastOrder, electricityBattery, vo, cabinet, userInfo, code);
        }
        
        //  新仓门开门失败
        if (Objects.equals(orderStatus, ElectricityCabinetOrder.COMPLETE_OPEN_FAIL)) {
            return newCellOpenFail(lastOrder, electricityBattery, vo, cabinet, userInfo);
        }
        
        return Pair.of(false, null);
    }
    
    private Pair<Boolean, Object> newCellOpenFail(ElectricityCabinetOrder lastOrder, ElectricityBattery electricityBattery, ExchangeUserSelectVo vo, ElectricityCabinet cabinet,
            UserInfo userInfo) {
        if (!this.isSatisfySelfOpenCondition(lastOrder, lastOrder.getNewCellNo(), ElectricityCabinetOrder.NEW_CELL)) {
            // 新仓门不满足开仓条件
            vo.setIsSatisfySelfOpen(ExchangeUserSelectVo.NOT_SATISFY_SELF_OPEN);
            log.warn("OrderV3 WARN!newCellOpenFail is not SatisfySelfOpen, orderId is{}", lastOrder.getOrderId());
            return Pair.of(true, vo);
        }
        
        vo.setIsSatisfySelfOpen(ExchangeUserSelectVo.IS_SATISFY_SELF_OPEN);
        vo.setCell(lastOrder.getNewCellNo());
        vo.setOrderId(lastOrder.getOrderId());
      
        
        // 用户绑定电池为空，返回自主开仓
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.warn("OrderV3 WARN!newCellOpenFail.userBindingBatterySn  is null, uid is {}", lastOrder.getUid());
            // 没有在仓，需要返回前端仓门号
            vo.setIsBatteryInCell(ExchangeUserSelectVo.BATTERY_NOT_CELL);
            return Pair.of(true, vo);
        }
        
        String userBindingBatterySn = electricityBattery.getSn();
        // 用户电池是否在仓
        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(userBindingBatterySn, cabinet.getId());
        
        ElectricityBattery battery = electricityBatteryService.queryBySnFromDb(userBindingBatterySn);
        
        log.info("OrderV3 INFO! newCellOpenFail.cabinetBox is {}, battery is {}, lastOrder is {}", Objects.nonNull(cabinetBox) ? JsonUtil.toJson(cabinetBox) : "null",
                Objects.nonNull(battery) ? JsonUtil.toJson(battery) : "null", JsonUtil.toJson(lastOrder));
        
        // 用户绑定的电池状态是否为租借状态 && 用户绑定的电池在仓 & 电池所在的仓门=上个订单的旧仓门；开新仓门
        if (Objects.nonNull(cabinetBox) && Objects.nonNull(battery) && Objects.equals(battery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE) && StrUtil.isNotBlank(
                cabinetBox.getCellNo()) && Objects.equals(Integer.valueOf(cabinetBox.getCellNo()), lastOrder.getOldCellNo())) {
            vo.setIsBatteryInCell(ExchangeUserSelectVo.BATTERY_IN_CELL);
            vo.setIsEnterTakeBattery(ExchangeUserSelectVo.ENTER_TAKE_BATTERY);
            vo.setCellType(CellTypeEnum.NEW_CELL.getCode());
            // 新仓门取电
            vo.setSessionId(this.openFullBatteryCellHandler(lastOrder, cabinet, lastOrder.getNewCellNo(), userBindingBatterySn, cabinetBox.getCellNo()));
           
            return Pair.of(true, vo);
        } else {
            // 没有在仓，需要返回前端仓门号
            vo.setIsBatteryInCell(ExchangeUserSelectVo.BATTERY_NOT_CELL);
            return Pair.of(true, vo);
        }
    }
    
    private Pair<Boolean, Object> oldCellCheckFail(ElectricityCabinetOrder lastOrder, ElectricityBattery electricityBattery, ExchangeUserSelectVo vo, ElectricityCabinet cabinet,
            UserInfo userInfo,Integer code) {
        
        if (!this.isSatisfySelfOpenCondition(lastOrder, lastOrder.getOldCellNo(), ElectricityCabinetOrder.OLD_CELL)) {
            // 旧仓门不满足开仓条件
            vo.setIsSatisfySelfOpen(ExchangeUserSelectVo.NOT_SATISFY_SELF_OPEN);
            log.warn("OrderV3 WARN! oldCellCheckFail is not SatisfySelfOpen, orderId is{}", lastOrder.getOrderId());
            return Pair.of(true, vo);
        }
        
        vo.setOrderId(lastOrder.getOrderId());
        vo.setIsSatisfySelfOpen(ExchangeUserSelectVo.IS_SATISFY_SELF_OPEN);
        
        
        // 用户绑定电池为空，返回自主开仓
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.warn("OrderV3 WARN!oldCellCheckFail.userBindingBatterySn  is null, uid is {}", lastOrder.getUid());
            // 不在仓，前端会自主开仓
            vo.setIsBatteryInCell(ExchangeUserSelectVo.BATTERY_NOT_CELL);
            vo.setCell(lastOrder.getOldCellNo());
            return Pair.of(true, vo);
        }
        
        String userBindingBatterySn = electricityBattery.getSn();
        // 用户电池是否在仓
        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(userBindingBatterySn, cabinet.getId());
        
        log.info("OrderV3 INFO! oldCellCheckFail.cabinetBox is {}, lastOrder is {}", Objects.nonNull(cabinetBox) ? JsonUtil.toJson(cabinetBox) : "null",
                JsonUtil.toJson(lastOrder));
        
        // 租借在仓（上一个订单旧仓门内），仓门锁状态：关闭
        if (Objects.nonNull(cabinetBox) && Objects.equals(cabinetBox.getIsLock(), ElectricityCabinetBox.CLOSE_DOOR) && StrUtil.isNotBlank(cabinetBox.getCellNo()) && Objects.equals(
                Integer.valueOf(cabinetBox.getCellNo()), lastOrder.getOldCellNo())) {
            
            vo.setIsBatteryInCell(ExchangeUserSelectVo.BATTERY_IN_CELL);
            vo.setIsEnterTakeBattery(ExchangeUserSelectVo.ENTER_TAKE_BATTERY);
            vo.setCellType(CellTypeEnum.OLD_CELL.getCode());
            
            // 只有换电，才去获取满电仓，而选仓取电不走这里
            if (Objects.equals(code,OrderCheckEnum.ORDER.getCode())) {
                // 获取满电仓
                Integer cellNo = this.getFullCellHandler(cabinet, userInfo);
                vo.setCell(cellNo);
                String sessionId = this.openFullBatteryCellHandler(lastOrder, cabinet, cellNo, userBindingBatterySn, cabinetBox.getCellNo());
                vo.setSessionId(sessionId);
            }
            
            return Pair.of(true, vo);
        } else {
            // 不在仓，前端会自主开仓
            vo.setIsBatteryInCell(ExchangeUserSelectVo.BATTERY_NOT_CELL);
            vo.setCell(lastOrder.getOldCellNo());
            return Pair.of(true, vo);
        }
    }
    
    private Boolean isEnterSelectCellExchange(UserInfo userInfo) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            log.warn("isEnterSelectCellExchange.electricityConfig is null, tenantId is {}", userInfo.getTenantId());
            return false;
        }
        
        ElectricityAppConfig electricityAppConfig = electricityAppConfigService.queryFromCacheByUid(userInfo.getUid());
        if (Objects.isNull(electricityAppConfig)) {
            log.warn("isEnterSelectCellExchange.electricityAppConfig is null, userId is {}", userInfo.getUid());
            return false;
        }
        
        if (Objects.equals(electricityConfig.getIsSelectionExchange(), SelectionExchageEunm.ENABLE_SELECTION_EXCHANGE.getCode()) && Objects.equals(
                electricityAppConfig.getIsSelectionExchange(), SelectionExchageEunm.ENABLE_SELECTION_EXCHANGE.getCode())) {
            // 允许选仓换电
            return true;
        }
        return false;
    }
    
    private Integer getFullCellHandler(ElectricityCabinet cabinet, UserInfo userInfo) {
        // 执行取电流程，下发开满电仓指令， 按照租电分配满电仓走
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        // 分配满电仓
        Triple<Boolean, String, Object> getFullCellResult = allocateFullBatteryBox(cabinet, userInfo, franchisee);
        if (Boolean.FALSE.equals(getFullCellResult.getLeft())) {
            throw new BizException(getFullCellResult.getMiddle(), "换电柜暂无满电电池");
        }
        Integer cellNo = Integer.valueOf((String) getFullCellResult.getRight());
        return cellNo;
    }
    
    
    private String backSelfOpen(Integer cell, String userBindingBatterySn, ElectricityCabinetOrder order, ElectricityCabinet cabinet, String msg) {
        if (cabinet.getVersion().isBlank() || VersionUtil.compareVersion(cabinet.getVersion(), ElectricityCabinetOrderOperHistory.THREE_PERIODS_SUCCESS_RATE_VERSION) < 0) {
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder().createTime(System.currentTimeMillis()).orderId(order.getOrderId())
                    .tenantId(order.getTenantId()).msg(msg).seq(ElectricityCabinetOrderOperHistory.SELF_OPEN_CELL_SEQ).type(ElectricityCabinetOrderOperHistory.ORDER_TYPE_EXCHANGE)
                    .result(ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS).build();
        
            electricityCabinetOrderOperHistoryService.insert(history);
        }
        
        ElectricityCabinetOrder electricityCabinetOrderUpdate = new ElectricityCabinetOrder();
        electricityCabinetOrderUpdate.setId(order.getId());
        electricityCabinetOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrderUpdate.setRemark("后台自助开仓");
        update(electricityCabinetOrderUpdate);
        
        //发送自助开仓命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("orderId", order.getOrderId());
        dataMap.put("cellNo", cell);
        dataMap.put("batteryName", userBindingBatterySn);
        dataMap.put("userSelfOpenCell", false);
        
        String sessionId = CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + order.getOrderId();
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(sessionId).data(dataMap).productKey(cabinet.getProductKey()).deviceName(cabinet.getDeviceName())
                .command(ElectricityIotConstant.SELF_OPEN_CELL).build();
        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, cabinet);
        return sessionId;
    }
    
    
    @Override
    public Triple<Boolean, String, Object> orderSelectionExchange(OrderSelectionExchangeQuery exchangeQuery) {
        // 判断用户信息
        Long uid = SecurityUtils.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        
        // 校验用户状态
        Triple<Boolean, String, Object> userStatus = verifyUserStatus(userInfo);
        if (Boolean.FALSE.equals(userStatus.getLeft())) {
            return userStatus;
        }
        
        //查询后台开关
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig.getIsSelectionExchange()) || Objects
                .equals(electricityConfig.getIsSelectionExchange(), SelectionExchageEunm.DISABLE_SELECTION_EXCHANGE.getCode())) {
            return Triple.of(false, "100551", "选仓换电开关已关闭");
        }
        
        Triple<Boolean, String, Object> checkExistsOrderResult = checkUserExistsUnFinishOrder(userInfo.getUid());
        if (checkExistsOrderResult.getLeft()) {
            log.warn("SELECTION EXCHANGE ORDER WARN! user exists unFinishOrder! uid={}", userInfo.getUid());
            return Triple.of(false, checkExistsOrderResult.getMiddle(), checkExistsOrderResult.getRight());
        }
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(exchangeQuery.getEid());
        
        //校验柜机状态
        Triple<Boolean, String, Object> cabinetStatus = verifyElectricityCabinetStatus(electricityCabinet, exchangeQuery);
        if (Boolean.FALSE.equals(cabinetStatus.getLeft())) {
            return cabinetStatus;
        }
        
        //电子签署拦截
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsEnableEsign(), EleEsignConstant.ESIGN_ENABLE)) {
            EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordService.queryEsignFinishedRecordByUser(userInfo.getUid(), Long.valueOf(userInfo.getTenantId()));
            if (Objects.isNull(eleUserEsignRecord)) {
                return Triple.of(false, "100329", "请先完成电子签名");
            }
        }
        
        //校验加盟商
        Franchisee franchisee = franchiseeService.queryByIdFromCache(electricityCabinet.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("SELECTION EXCHANGE ORDER WARN! not found franchisee,franchiseeId={},uid={}", electricityCabinet.getFranchiseeId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        //柜机加盟商与用户加盟商不一致
        if (!Objects.equals(franchisee.getId(), userInfo.getFranchiseeId())) {
            log.warn("SELECTION EXCHANGE ORDER WARN! user fId  is not equal franchiseeId uid={} ,fid={}", userInfo.getUid(), userInfo.getFranchiseeId());
            return Triple.of(false, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
        }
        
        //校验门店信息
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.warn("SELECTION EXCHANGE ORDER WARN!  not found store!uid={},eid={},storeId={}", userInfo.getUid(), electricityCabinet.getId(), electricityCabinet.getStoreId());
            return Triple.of(false, "100204", "未找到门店");
        }
        
        //获取格挡
        ElectricityCabinetBox selectBox = electricityCabinetBoxService.queryByCellNo(electricityCabinet.getId(), String.valueOf(exchangeQuery.getSelectionCellNo()));
        if (Objects.isNull(selectBox)) {
            log.warn("SELECTION EXCHANGE ORDER WARN!  not found select cell!uid={},eid={},cellNo={}", userInfo.getUid(), electricityCabinet.getId(),
                    exchangeQuery.getSelectionCellNo());
            return Triple.of(false, "100553", "未找到此仓门");
        }
        
        //校验选择的仓门是否可用
        
        List<String> userBatteryTypeList = userBatteryTypeService.selectByUid(userInfo.getUid());
        
        Triple<Boolean, String, Object> exchangeStatus = isExchangeStatus(electricityCabinet, selectBox, userInfo, franchisee, userBatteryTypeList);
        if (Boolean.FALSE.equals(exchangeStatus.getLeft())) {
            return exchangeStatus;
        }
        
        // 选仓换电分配空仓适配舒适换电
        Pair<Boolean, Integer> usableEmptyCellNo = electricityCabinetService.selectCellExchangeFindUsableEmptyCellNo(selectBox.getElectricityCabinetId(),
                electricityCabinet.getVersion(),uid);
        if (Boolean.FALSE.equals(usableEmptyCellNo.getLeft())) {
            log.warn("SELECTION EXCHANGE ORDER WARN!  not found usable empty cell!uid={},eid={}", userInfo.getUid(), electricityCabinet.getId());
            return Triple.of(false, "100215", "当前无空余格挡可供换电，请联系客服！");
        }
        
        Triple<Boolean, String, Object> rentBatteryResult = null;
        //查询用户绑定的电池
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            //处理单电
            rentBatteryResult = handlerSelectionExchangeSingleBattery(userInfo, store, electricityCabinet, franchisee, selectBox, usableEmptyCellNo, electricityConfig,
                    electricityBattery, userBatteryTypeList);
            if (Boolean.FALSE.equals(rentBatteryResult.getLeft())) {
                return rentBatteryResult;
            }
        } else if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            //处理车电一体
            rentBatteryResult = handlerSelectionExchangeBatteryCar(userInfo, store, electricityCabinet, franchisee, selectBox, usableEmptyCellNo, electricityConfig,
                    electricityBattery, userBatteryTypeList);
            if (Boolean.FALSE.equals(rentBatteryResult.getLeft())) {
                return rentBatteryResult;
            }
        } else {
            log.warn("SELECTION EXCHANGE ORDER WARN!! not pay deposit,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }
        return rentBatteryResult;
    }
    
    @Override
    public Triple<Boolean, String, Object> orderSelectionExchangeTakeBattery(OrderSelectionExchangeQuery exchangeQuery) {
        // 判断用户信息
        Long uid = SecurityUtils.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        
        if (Objects.isNull(exchangeQuery.getOrderId())){
            return Triple.of(false, "100485", "选仓换电订单号不能为空");
        }
        
        // 校验用户状态
        Triple<Boolean, String, Object> userStatus = verifyUserStatus(userInfo);
        if (Boolean.FALSE.equals(userStatus.getLeft())) {
            return userStatus;
        }
        
        //查询后台开关
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig.getIsSelectionExchange()) || Objects.equals(electricityConfig.getIsSelectionExchange(),
                SelectionExchageEunm.DISABLE_SELECTION_EXCHANGE.getCode())) {
            return Triple.of(false, "100551", "选仓换电开关已关闭");
        }
        
        Triple<Boolean, String, Object> checkExistsOrderResult = checkUserExistsUnFinishOrder(userInfo.getUid());
        if (checkExistsOrderResult.getLeft()) {
            log.warn("SELECTION EXCHANGE ORDER WARN! user exists unFinishOrder! uid={}", userInfo.getUid());
            return Triple.of(false, checkExistsOrderResult.getMiddle(), checkExistsOrderResult.getRight());
        }
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(exchangeQuery.getEid());
        
        //校验柜机状态
        Triple<Boolean, String, Object> cabinetStatus = verifyElectricityCabinetStatus(electricityCabinet, exchangeQuery);
        if (Boolean.FALSE.equals(cabinetStatus.getLeft())) {
            return cabinetStatus;
        }
        
        //校验加盟商
        Franchisee franchisee = franchiseeService.queryByIdFromCache(electricityCabinet.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("SELECTION EXCHANGE ORDER WARN! not found franchisee,franchiseeId={},uid={}", electricityCabinet.getFranchiseeId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        //柜机加盟商与用户加盟商不一致
        if (!Objects.equals(franchisee.getId(), userInfo.getFranchiseeId())) {
            log.warn("SELECTION EXCHANGE ORDER WARN! user fId  is not equal franchiseeId uid={} ,fid={}", userInfo.getUid(), userInfo.getFranchiseeId());
            return Triple.of(false, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
        }
        
        //校验门店信息
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.warn("SELECTION EXCHANGE ORDER WARN!  not found store!uid={},eid={},storeId={}", userInfo.getUid(), electricityCabinet.getId(), electricityCabinet.getStoreId());
            return Triple.of(false, "100204", "未找到门店");
        }
        
        //获取格挡
        ElectricityCabinetBox selectBox = electricityCabinetBoxService.queryByCellNo(electricityCabinet.getId(), String.valueOf(exchangeQuery.getSelectionCellNo()));
        if (Objects.isNull(selectBox)) {
            log.warn("SELECTION EXCHANGE ORDER WARN!  not found select cell!uid={},eid={},cellNo={}", userInfo.getUid(), electricityCabinet.getId(),
                    exchangeQuery.getSelectionCellNo());
            return Triple.of(false, "100553", "未找到此仓门");
        }
        
        //校验选择的仓门是否可用
        List<String> userBatteryTypeList = userBatteryTypeService.selectByUid(userInfo.getUid());
        
        Triple<Boolean, String, Object> exchangeStatus = isExchangeStatus(electricityCabinet, selectBox, userInfo, franchisee, userBatteryTypeList);
        if (Boolean.FALSE.equals(exchangeStatus.getLeft())) {
            return exchangeStatus;
        }
        
        String orderId = exchangeQuery.getOrderId();
        
        ElectricityCabinetOrder cabinetOrder = this.queryByOrderId(orderId);
        if (Objects.isNull(cabinetOrder)) {
            return Triple.of(false, "100486", "不存在的换电订单号");
        }
        
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
        
        ExchangeUserSelectVo vo = new ExchangeUserSelectVo();
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.warn("SelectExchangeTakeBattery WARN ! electricityBattery  is null, uid is {}", cabinetOrder.getUid());
            vo.setIsBatteryInCell(ExchangeUserSelectVo.BATTERY_NOT_CELL);
            return Triple.of(true, null, vo);
        }
        
        // 选仓取电流程
        String userBindingBatterySn = electricityBattery.getSn();
        String sessionId = this.openFullBatteryCellHandler(cabinetOrder, electricityCabinet, exchangeQuery.getSelectionCellNo(), userBindingBatterySn,
                cabinetOrder.getOldCellNo().toString());
        vo.setIsBatteryInCell(ExchangeUserSelectVo.BATTERY_IN_CELL);
        vo.setSessionId(sessionId);
        
        return Triple.of(true, null, vo);
    }
    
    private Triple<Boolean, String, Object> handlerSelectionExchangeSingleBattery(UserInfo userInfo, Store store, ElectricityCabinet electricityCabinet, Franchisee franchisee,
            ElectricityCabinetBox selectBox, Pair<Boolean, Integer> usableEmptyCellNo, ElectricityConfig electricityConfig, ElectricityBattery electricityBattery,
            List<String> userBatteryTypeList) {
        
        //判断用户押金
        Triple<Boolean, String, Object> checkUserDepositResult = checkUserDeposit(userInfo, store, userInfo);
        if (Boolean.FALSE.equals(checkUserDepositResult.getLeft())) {
            return checkUserDepositResult;
        }
        
        //判断用户套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("SELECTION EXCHAGE ORDER WARN! user haven't memberCard uid={}", userInfo.getUid());
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
        
        // 判断车电关联
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenCarBatteryBind(), ElectricityConfig.ENABLE_CAR_BATTERY_BIND)) {
            if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
                try {
                    if (carRentalPackageMemberTermBizService.isExpirePackageOrder(userInfo.getTenantId(), userInfo.getUid())) {
                        log.warn("SELECTION EXCHAGE ORDER WARN! user car memberCard expire,uid={}", userInfo.getUid());
                        return Triple.of(false, "100233", "租车套餐已过期");
                    }
                } catch (Exception e) {
                    log.warn("SELECTION EXCHAGE ORDER WARN!acquire car membercard expire result fail,uid={}", userInfo.getUid(), e);
                }
            }
        }
        
        // 用户未绑定电池
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW) && Objects.isNull(electricityBattery)) {
            return Triple.of(false, "300880", "系统检测到您未绑定电池，请检查");
        }
        
        //修改按此套餐的次数
        Triple<Boolean, String, String> modifyResult = checkAndModifyMemberCardCount(userBatteryMemberCard, batteryMemberCard);
        if (Boolean.FALSE.equals(modifyResult.getLeft())) {
            return Triple.of(false, modifyResult.getMiddle(), modifyResult.getRight());
        }
        
        ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
                .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.EXCHANGE_BATTERY, userInfo.getUid())).uid(userInfo.getUid()).phone(userInfo.getPhone())
                .electricityCabinetId(selectBox.getElectricityCabinetId()).oldCellNo(usableEmptyCellNo.getRight()).newCellNo(Integer.parseInt(selectBox.getCellNo()))
                .orderSeq(ElectricityCabinetOrder.STATUS_INIT).status(ElectricityCabinetOrder.INIT).source(ExchangeTypeEnum.SELECTION_EXCHANGE.getCode())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).storeId(electricityCabinet.getStoreId()).franchiseeId(store.getFranchiseeId())
                .tenantId(TenantContextHolder.getTenantId()).build();
        
        if (ExchangeTypeEnum.NORMAL_EXCHANGE.getCode().equals(electricityCabinetOrder.getSource())||ExchangeTypeEnum.SELECTION_EXCHANGE.getCode().equals(electricityCabinetOrder.getSource())){
            electricityCabinetOrder.setChannel(ChannelSourceContextHolder.get());
        }
        
        electricityCabinetOrderMapper.insert(electricityCabinetOrder);
        
        //记录活跃时间
        userActiveInfoService.userActiveRecord(userInfo);
        
        HashMap<String, Object> commandData = Maps.newHashMap();
        commandData.put("orderId", electricityCabinetOrder.getOrderId());
        commandData.put("placeCellNo", electricityCabinetOrder.getOldCellNo());
        commandData.put("takeCellNo", electricityCabinetOrder.getNewCellNo());
        commandData.put("phone", userInfo.getPhone());
        
        //判断是否开启了电池检测
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
            commandData.put("userBindingBatterySn", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getSn());
        }
        
        commandData.put("newUserBindingBatterySn", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getSn());
        
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.nonNull(electricityBattery)) {
                commandData.put("multiBatteryModelName", electricityBattery.getModel());
                commandData.put("multiBatteryModelNameList", JsonUtil.toJson(Lists.newArrayList(electricityBattery.getModel())));
            } else {
                ElectricityBattery lastElectricityBattery = selectLastExchangeOrderBattery(userInfo);
                commandData.put("multiBatteryModelName", Objects.isNull(lastElectricityBattery) ? "UNKNOWN" : lastElectricityBattery.getModel());
                commandData.put("multiBatteryModelNameList", JsonUtil.toJson(userBatteryTypeList));
            }
        }
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + ":" + electricityCabinetOrder.getOrderId())
                .data(commandData).productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.ELE_COMMAND_NEW_EXCHANGE_ORDER).build();
        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        if (Boolean.FALSE.equals(result.getLeft())) {
            return Triple.of(false, "100218", "下单消息发送失败");
        }
        
        return Triple.of(true, null, electricityCabinetOrder.getOrderId());
    }
    
    private Triple<Boolean, String, Object> handlerSelectionExchangeBatteryCar(UserInfo userInfo, Store store, ElectricityCabinet electricityCabinet, Franchisee franchisee,
            ElectricityCabinetBox selectBox, Pair<Boolean, Integer> usableEmptyCellNo, ElectricityConfig electricityConfig, ElectricityBattery electricityBattery,
            List<String> batteryTypeList) {
        
        //判断车电一体套餐状态
        if (carRentalPackageMemberTermBizService.isExpirePackageOrder(userInfo.getTenantId(), userInfo.getUid())) {
            log.warn("EXCHANGE WARN! user memberCard disable,uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户套餐不可用");
        }
        
        //判断用户电池服务费
        if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid()))) {
            log.warn("ORDER WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "300001", "存在滞纳金，请先缴纳");
        }
        
        // 用户未绑定电池
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW) && Objects.isNull(electricityBattery)) {
            return Triple.of(false, "300880", "系统检测到您未绑定电池，请检查");
        }
        
        //修改按此套餐的次数
        carRentalPackageMemberTermBizService.substractResidue(userInfo.getTenantId(), userInfo.getUid());
        
        ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
                .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.EXCHANGE_BATTERY, userInfo.getUid())).uid(userInfo.getUid()).phone(userInfo.getPhone())
                .electricityCabinetId(selectBox.getElectricityCabinetId()).oldCellNo(usableEmptyCellNo.getRight()).newCellNo(Integer.parseInt(selectBox.getCellNo()))
                .orderSeq(ElectricityCabinetOrder.STATUS_INIT).status(ElectricityCabinetOrder.INIT).source(ExchangeTypeEnum.SELECTION_EXCHANGE.getCode())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).storeId(electricityCabinet.getStoreId()).franchiseeId(store.getFranchiseeId())
                .tenantId(TenantContextHolder.getTenantId()).build();
    
        if (ExchangeTypeEnum.NORMAL_EXCHANGE.getCode().equals(electricityCabinetOrder.getSource())||ExchangeTypeEnum.SELECTION_EXCHANGE.getCode().equals(electricityCabinetOrder.getSource())){
            electricityCabinetOrder.setChannel(ChannelSourceContextHolder.get());
        }
        
        electricityCabinetOrderMapper.insert(electricityCabinetOrder);
        
        //记录活跃时间
        userActiveInfoService.userActiveRecord(userInfo);
        
        HashMap<String, Object> commandData = Maps.newHashMap();
        commandData.put("orderId", electricityCabinetOrder.getOrderId());
        commandData.put("placeCellNo", electricityCabinetOrder.getOldCellNo());
        commandData.put("takeCellNo", electricityCabinetOrder.getNewCellNo());
        commandData.put("phone", userInfo.getPhone());
        
        if (Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
            commandData.put("userBindingBatterySn", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getSn());
        }
        
        commandData.put("newUserBindingBatterySn", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getSn());
       
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.nonNull(electricityBattery)) {
                commandData.put("multiBatteryModelName", electricityBattery.getModel());
                commandData.put("multiBatteryModelNameList", JsonUtil.toJson(Lists.newArrayList(electricityBattery.getModel())));
            } else {
                ElectricityBattery lastElectricityBattery = selectLastExchangeOrderBattery(userInfo);
                commandData.put("multiBatteryModelName", Objects.isNull(lastElectricityBattery) ? "UNKNOWN" : lastElectricityBattery.getModel());
                commandData.put("multiBatteryModelNameList", JsonUtil.toJson(batteryTypeList));
            }
        }
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + ":" + electricityCabinetOrder.getOrderId())
                .data(commandData).productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.ELE_COMMAND_NEW_EXCHANGE_ORDER).build();
        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        if (Boolean.FALSE.equals(result.getLeft())) {
            return Triple.of(false, "100218", "下单消息发送失败");
        }
        
        return Triple.of(true, null, electricityCabinetOrder.getOrderId());
    }
    
    private Triple<Boolean, String, Object> isExchangeStatus(ElectricityCabinet electricityCabinet, ElectricityCabinetBox selectBox, UserInfo userInfo, Franchisee franchisee,
            List<String> userBatteryTypeList) {
        Double fullyCharged = electricityCabinet.getFullyCharged();
        
        //判断仓门中是否为空仓
        if (Objects.isNull(selectBox.getPower())) {
            log.warn("SELECTION EXCHANGE ORDER WARN! electricityCabinet select cell is empty! electricityCabinetId={},uid={}", electricityCabinet.getId(), userInfo.getUid());
            return Triple.of(false, "100556", "选中仓门为空仓");
        }
        
        if (Objects.isNull(fullyCharged)) {
            log.warn("SELECTION EXCHANGE ORDER WARN! electricityCabinet fully charged is empty! electricityCabinetId={},uid={}", electricityCabinet.getId(), userInfo.getUid());
            return Triple.of(false, "100557", "柜机的满电标准为空");
        }
        
        // 是否达到柜机的可换电标准
        if (selectBox.getPower() < fullyCharged) {
            log.warn("SELECTION EXCHANGE ORDER WARN! electricityCabinet select cell does not meet exchange standards! electricityCabinetId={},uid={}", electricityCabinet.getId(),
                    userInfo.getUid());
            return Triple.of(false, "100554", "选中仓门的电池未达到可换电标准");
        }
        
        // 换电、车电一体套餐类型判断
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) && !userBatteryTypeList.contains(selectBox.getBatteryType())) {
            log.warn("SELECTION EXCHANGE ORDER WARN! user battery type not equals memberCard battery type! electricityCabinetId={},uid={}", electricityCabinet.getId(),
                    userInfo.getUid());
            return Triple.of(false, "100297", "电池型号与用户绑定的型号不一致");
        }
        
        // 过滤掉异常锁仓的
        if (Objects.equals(selectBox.getUsableStatus(), ElectricityCabinet.ELECTRICITY_CABINET_UN_USABLE_STATUS)) {
            log.warn("SELECTION EXCHANGE ORDER WARN! electricityCabinet select cell not available! electricityCabinetId={},cell={}", electricityCabinet.getId(),
                    selectBox.getCellNo());
            return Triple.of(false, "100025", "该仓门已被禁用");
        }
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> verifyUserStatus(UserInfo userInfo) {
        if (Objects.isNull(userInfo)) {
            log.warn("SELECTION EXCHANGE BATTERY WARN! not found user");
            return Triple.of(false, "100001", "未能找到用户");
        }
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("SELECTION EXCHANGE BATTERY WARN! user is unUsable,uid={} ", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("SELECTION EXCHANGE BATTERY WARN! userinfo is UN AUTH! uid={}", userInfo.getUid());
            return Triple.of(false, "100206", "用户未审核");
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && !Objects
                .equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            log.warn("SELECTION EXCHANGE BATTERY ERROR! user didn't pay a deposit,uid={},fid={}", userInfo.getUid(), userInfo.getFranchiseeId());
            return Triple.of(false, "100209", "用户未缴纳押金");
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> verifyElectricityCabinetStatus(ElectricityCabinet electricityCabinet, OrderSelectionExchangeQuery exchangeQuery) {
        if (Objects.isNull(electricityCabinet) || !Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("SELECTION EXCHAGE ORDER WARN! electricityCabinet not exists! electricityCabinetId={}", exchangeQuery.getEid());
            return Triple.of(false, "100003", "柜机不存在");
        }
        
        //换电柜是否打烊
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            log.warn("SELECTION EXCHAGE ORDER WARN! electricityCabinet is not business! electricityCabinetId={}", exchangeQuery.getEid());
            return Triple.of(false, "100203", "换电柜已打烊");
        }
        
        return Triple.of(true, null, null);
    }
    
   
    
    private Triple<Boolean, String, Object> handlerExchangeBatteryCar(UserInfo userInfo, Store store, ElectricityCabinet electricityCabinet, OrderQueryV2 orderQuery,
            List<String> batteryTypeList) {
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("EXCHANGE WARN! not found franchisee,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        //判断车电一体套餐状态
        if (carRentalPackageMemberTermBizService.isExpirePackageOrder(userInfo.getTenantId(), userInfo.getUid())) {
            log.warn("EXCHANGE WARN! user memberCard disable,uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户套餐不可用");
        }
        
        //        //判断用户押金
        //        Triple<Boolean, String, Object> checkUserDepositResult = checkUserDeposit(userInfo, store, userInfo);
        //        if (Boolean.FALSE.equals(checkUserDepositResult.getLeft())) {
        //            return checkUserDepositResult;
        //        }
        
        //        //判断用户套餐
        //        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        //        if (Objects.isNull(userBatteryMemberCard)) {
        //            log.warn("ORDER WARN! user haven't memberCard uid={}", userInfo.getUid());
        //            return Triple.of(false, "100210", "用户未开通套餐");
        //        }
        //
        //        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
        //            log.warn("ORDER WARN! user's member card is stop! uid={}", userInfo.getUid());
        //            return Triple.of(false, "100211", "换电套餐停卡审核中");
        //        }
        //
        //        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
        //            log.warn("ORDER WARN! user's member card is stop! uid={}", userInfo.getUid());
        //            return Triple.of( false,"100211", "换电套餐已暂停");
        //        }
        //            Triple<Boolean, String, Object> checkUserMemberCardResult = checkUserMemberCard(userBatteryMemberCard, user);
        //            if (Boolean.FALSE.equals(checkUserMemberCardResult.getLeft())) {
        //                return checkUserMemberCardResult;
        //            }
        
        //判断用户电池服务费
        if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid()))) {
            log.warn("ORDER WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "300001", "存在滞纳金，请先缴纳");
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            log.warn("ORDER WARN! not found electricityConfig,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.100000", "系统异常");
        }
        
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW) && Objects.isNull(electricityBattery)) {
            return Triple.of(false, "300880", "系统检测到您未绑定电池，请检查");
        }
        
        //默认是小程序下单
        if (Objects.isNull(orderQuery.getSource())) {
            orderQuery.setSource(OrderQuery.SOURCE_WX_MP);
        }
        
        Pair<Boolean, Integer> usableEmptyCellNo = electricityCabinetService.findUsableEmptyCellNoV2(userInfo.getUid(), electricityCabinet.getId(),
                electricityCabinet.getVersion());
        if (Boolean.FALSE.equals(usableEmptyCellNo.getLeft())) {
            return Triple.of(false, "100215", "当前无空余格挡可供换电，请联系客服！");
        }
        
        Triple<Boolean, String, Object> usableBatteryCellNoResult = electricityCabinetService.findUsableBatteryCellNoV3(electricityCabinet.getId(), franchisee,
                electricityCabinet.getFullyCharged(), electricityBattery, userInfo.getUid());
        if (Boolean.FALSE.equals(usableBatteryCellNoResult.getLeft())) {
            return Triple.of(false, usableBatteryCellNoResult.getMiddle(), usableBatteryCellNoResult.getRight());
        }
        
        //修改按此套餐的次数
        carRentalPackageMemberTermBizService.substractResidue(userInfo.getTenantId(), userInfo.getUid());
        
        ElectricityCabinetBox electricityCabinetBox = (ElectricityCabinetBox) usableBatteryCellNoResult.getRight();
        
        ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
                .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.EXCHANGE_BATTERY, userInfo.getUid())).uid(userInfo.getUid()).phone(userInfo.getPhone())
                .electricityCabinetId(orderQuery.getEid()).oldCellNo(usableEmptyCellNo.getRight()).newCellNo(Integer.parseInt(electricityCabinetBox.getCellNo()))
                .orderSeq(ElectricityCabinetOrder.STATUS_INIT).status(ElectricityCabinetOrder.INIT).source(orderQuery.getSource()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).storeId(electricityCabinet.getStoreId()).franchiseeId(store.getFranchiseeId()).tenantId(TenantContextHolder.getTenantId())
                .build();
    
        if (ExchangeTypeEnum.NORMAL_EXCHANGE.getCode().equals(electricityCabinetOrder.getSource())||ExchangeTypeEnum.SELECTION_EXCHANGE.getCode().equals(electricityCabinetOrder.getSource())){
            electricityCabinetOrder.setChannel(ChannelSourceContextHolder.get());
        }
        
        electricityCabinetOrderMapper.insert(electricityCabinetOrder);
        
        //记录活跃时间
        userActiveInfoService.userActiveRecord(userInfo);
        
        HashMap<String, Object> commandData = Maps.newHashMap();
        commandData.put("orderId", electricityCabinetOrder.getOrderId());
        commandData.put("placeCellNo", electricityCabinetOrder.getOldCellNo());
        commandData.put("takeCellNo", electricityCabinetOrder.getNewCellNo());
        commandData.put("phone", userInfo.getPhone());
        
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
            commandData.put("userBindingBatterySn", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getSn());
        }
        
        commandData.put("newUserBindingBatterySn", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getSn());
        
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.nonNull(electricityBattery)) {
                commandData.put("multiBatteryModelName", electricityBattery.getModel());
                commandData.put("multiBatteryModelNameList", JsonUtil.toJson(Lists.newArrayList(electricityBattery.getModel())));
            } else {
                ElectricityBattery lastElectricityBattery = selectLastExchangeOrderBattery(userInfo);
                commandData.put("multiBatteryModelNameList", JsonUtil.toJson(batteryTypeList));
                commandData.put("multiBatteryModelName", Objects.isNull(lastElectricityBattery) ? "UNKNOWN" : lastElectricityBattery.getModel());
            }
        }
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + ":" + electricityCabinetOrder.getOrderId())
                .data(commandData).productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.ELE_COMMAND_NEW_EXCHANGE_ORDER).build();
        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        if (Boolean.FALSE.equals(result.getLeft())) {
            return Triple.of(false, "100218", "下单消息发送失败");
        }
        
        return Triple.of(true, null, electricityCabinetOrder.getOrderId());
    }
    
    private Triple<Boolean, String, Object> handlerSingleExchangeBattery(UserInfo userInfo, Store store, ElectricityCabinet electricityCabinet, OrderQueryV2 orderQuery,
            List<String> batteryTypeList) {
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("ORDER WARN! not found franchisee,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        
        //判断用户押金
        Triple<Boolean, String, Object> checkUserDepositResult = checkUserDeposit(userInfo, store, userInfo);
        if (Boolean.FALSE.equals(checkUserDepositResult.getLeft())) {
            return checkUserDepositResult;
        }
        
        //判断用户套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("ORDER WARN! user haven't memberCard uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户未开通套餐");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("ORDER WARN! user's member card is stop! uid={}", userInfo.getUid());
            return Triple.of(false, "100211", "换电套餐停卡审核中");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("ORDER WARN! user's member card is stop! uid={}", userInfo.getUid());
            return Triple.of(false, "100211", "换电套餐已暂停");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("ORDER WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }
        
        //判断用户电池服务费
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService
                .acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("ORDER WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.100000", "存在电池服务费");
        }
        
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)
                && userBatteryMemberCard.getRemainingNumber() <= 0)) {
            log.warn("ORDER WARN! battery memberCard is Expire,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0023", "套餐已过期");
        }
        
        //判断车电关联是否可换电
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenCarBatteryBind(), ElectricityConfig.ENABLE_CAR_BATTERY_BIND)) {
            if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
                try {
                    if (carRentalPackageMemberTermBizService.isExpirePackageOrder(userInfo.getTenantId(), userInfo.getUid())) {
                        log.warn("ORDER WARN! user car memberCard expire,uid={}", userInfo.getUid());
                        return Triple.of(false, "100233", "租车套餐已过期");
                    }
                } catch (Exception e) {
                    log.error("ORDER ERROR!acquire car membercard expire result fail,uid={}", userInfo.getUid(), e);
                }
            }
        }
        
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW) && Objects.isNull(electricityBattery)) {
            return Triple.of(false, "300880", "系统检测到您未绑定电池，请检查");
        }
        
        //默认是小程序下单
        if (Objects.isNull(orderQuery.getSource())) {
            orderQuery.setSource(OrderQuery.SOURCE_WX_MP);
        }
        
        Pair<Boolean, Integer> usableEmptyCellNo = electricityCabinetService.findUsableEmptyCellNoV2(userInfo.getUid(), electricityCabinet.getId(),
                electricityCabinet.getVersion());
        if (Boolean.FALSE.equals(usableEmptyCellNo.getLeft())) {
            log.warn("ORDER WARN! There is no empty cell.");
            return Triple.of(false, "100215", "当前无空余格挡可供换电，请联系客服！");
        }
        
        
        Triple<Boolean, String, Object> usableBatteryCellNoResult = electricityCabinetService.findUsableBatteryCellNoV3(electricityCabinet.getId(), franchisee,
                electricityCabinet.getFullyCharged(), electricityBattery, userInfo.getUid());
        if (Boolean.FALSE.equals(usableBatteryCellNoResult.getLeft())) {
            return Triple.of(false, usableBatteryCellNoResult.getMiddle(), usableBatteryCellNoResult.getRight());
        }
        
       
        
        //修改按此套餐的次数
        Triple<Boolean, String, String> modifyResult = checkAndModifyMemberCardCount(userBatteryMemberCard, batteryMemberCard);
        if (Boolean.FALSE.equals(modifyResult.getLeft())) {
            return Triple.of(false, modifyResult.getMiddle(), modifyResult.getRight());
        }
        
        ElectricityCabinetBox electricityCabinetBox = (ElectricityCabinetBox) usableBatteryCellNoResult.getRight();
        
        ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
                .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.EXCHANGE_BATTERY, userInfo.getUid())).uid(userInfo.getUid()).phone(userInfo.getPhone())
                .electricityCabinetId(orderQuery.getEid()).oldCellNo(usableEmptyCellNo.getRight()).newCellNo(Integer.parseInt(electricityCabinetBox.getCellNo()))
                .orderSeq(ElectricityCabinetOrder.STATUS_INIT).status(ElectricityCabinetOrder.INIT).source(orderQuery.getSource()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).storeId(electricityCabinet.getStoreId()).franchiseeId(store.getFranchiseeId()).tenantId(TenantContextHolder.getTenantId())
                .build();
    
        if (ExchangeTypeEnum.NORMAL_EXCHANGE.getCode().equals(electricityCabinetOrder.getSource())||ExchangeTypeEnum.SELECTION_EXCHANGE.getCode().equals(electricityCabinetOrder.getSource())){
            electricityCabinetOrder.setChannel(ChannelSourceContextHolder.get());
        }
        
        electricityCabinetOrderMapper.insert(electricityCabinetOrder);
        
        //记录活跃时间
        userActiveInfoService.userActiveRecord(userInfo);
        
        HashMap<String, Object> commandData = Maps.newHashMap();
        commandData.put("orderId", electricityCabinetOrder.getOrderId());
        commandData.put("placeCellNo", electricityCabinetOrder.getOldCellNo());
        commandData.put("takeCellNo", electricityCabinetOrder.getNewCellNo());
        commandData.put("phone", userInfo.getPhone());
        
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
            commandData.put("userBindingBatterySn", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getSn());
        }
        
        commandData.put("newUserBindingBatterySn", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getSn());
       
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.nonNull(electricityBattery)) {
                commandData.put("multiBatteryModelName", electricityBattery.getModel());
                commandData.put("multiBatteryModelNameList", JsonUtil.toJson(batteryTypeList));
            } else {
                ElectricityBattery lastElectricityBattery = selectLastExchangeOrderBattery(userInfo);
                commandData.put("multiBatteryModelName", Objects.isNull(lastElectricityBattery) ? "UNKNOWN" : lastElectricityBattery.getModel());
                commandData.put("multiBatteryModelNameList", JsonUtil.toJson(batteryTypeList));
            }
        }
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + ":" + electricityCabinetOrder.getOrderId())
                .data(commandData).productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.ELE_COMMAND_NEW_EXCHANGE_ORDER).build();
        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        if (Boolean.FALSE.equals(result.getLeft())) {
            return Triple.of(false, "100218", "下单消息发送失败");
        }
        
        return Triple.of(true, null, electricityCabinetOrder.getOrderId());
    }
    
    private ElectricityBattery selectLastExchangeOrderBattery(UserInfo userInfo) {
        ElectricityCabinetOrder lastElectricityCabinetOrder = selectLatestByUidV2(userInfo.getUid());
        if (Objects.isNull(lastElectricityCabinetOrder) || StringUtils.isBlank(lastElectricityCabinetOrder.getNewElectricityBatterySn())) {
            return null;
        }
        
        return electricityBatteryService.queryBySnFromDb(lastElectricityCabinetOrder.getNewElectricityBatterySn());
    }
    
    @Override
    public ElectricityCabinetOrder selectLatestByUidV2(Long uid) {
        return electricityCabinetOrderMapper.selectLatestByUidV2(uid);
    }
    
    private Triple<Boolean, String, Object> checkUserCarMemberCard(UserCarMemberCard userCarMemberCard, UserInfo user) {
        
        //用户未缴纳押金可直接换电
        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarDeposit)) {
            return Triple.of(true, null, null);
        }
        
        //用户未缴纳押金可直接换电
        if (!Objects.equals(user.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            return Triple.of(true, null, null);
        }
        
        //用户从未买过车辆套餐则可直接换电
        if (Objects.isNull(userCarMemberCard) || Objects.isNull(userCarMemberCard.getMemberCardExpireTime()) || Objects.equals(userCarMemberCard.getMemberCardExpireTime(), 0L)) {
            return Triple.of(false, "100232", "未购买租车套餐");
        }
        
        //套餐是否可用
        long now = System.currentTimeMillis();
        if (userCarMemberCard.getMemberCardExpireTime() < now) {
            log.error("ORDER ERROR! user's carMemberCard is expire! uid={} cardId={}", user.getUid(), userCarMemberCard.getCardId());
            return Triple.of(false, "100233", "租车套餐已过期");
        }
        return Triple.of(true, null, null);
    }
    
    private String generateExchangeOrderId(Long uid) {
        return String.valueOf(uid) + System.currentTimeMillis() / 1000 + RandomUtil.randomNumbers(3);
    }
    
    @Override
    public Triple<Boolean, String, String> checkAndModifyMemberCardCount(UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard) {
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER) || Objects
                .equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)) {
            Integer row = userBatteryMemberCardService.minCount(userBatteryMemberCard);
            if (row < 1) {
                log.warn("ORDER WARN! memberCard's count modify fail, uid={} ,mid={}", userBatteryMemberCard.getUid(), userBatteryMemberCard.getId());
                return Triple.of(false, "100213", "套餐剩余次数不足");
            }
        }
        return Triple.of(true, null, null);
    }
    
    
    private Triple<Boolean, String, Object> checkUserDeposit(UserInfo userInfo, Store store, UserInfo user) {
        if (Objects.isNull(userInfo.getFranchiseeId())) {
            log.warn("ORDER WARN! not found franchiseeUser! uid={}", user.getUid());
            return Triple.of(false, "100207", "用户加盟商信息未找到");
        }
        
        if (!Objects.equals(store.getFranchiseeId(), userInfo.getFranchiseeId())) {
            log.warn("ORDER WARN! storeId  is not equal franchieseeId uid={} , store's fid={} ,fid={}", user.getUid(), store.getFranchiseeId(), userInfo.getFranchiseeId());
            return Triple.of(false, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("ORDER WARN! user didn't pay a deposit,uid={},fid={}", user.getUid(), userInfo.getFranchiseeId());
            return Triple.of(false, "100209", "用户未缴纳押金");
        }
        
        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("ORDER WARN! user not rent battery! uid={}", user.getUid());
            return Triple.of(false, "100222", "用户还没有租借电池");
        }
        return Triple.of(true, null, null);
    }
    
    @Deprecated
    private Triple<Boolean, String, Object> checkUserMemberCard(UserBatteryMemberCard userBatteryMemberCard, TokenUser user) {
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects
                .isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("ORDER WARN! user haven't memberCard uid={}", user.getUid());
            return Triple.of(false, "100210", "用户未开通套餐");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("ORDER WARN! user's member card is stop! uid={}", user.getUid());
            return Triple.of(false, "100211", "用户套餐已暂停");
        }
        
        //套餐是否可用
        long now = System.currentTimeMillis();
        if (userBatteryMemberCard.getMemberCardExpireTime() < now) {
            log.warn("ORDER WARN! user's member card is expire! uid={} cardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false, "100212", "用户套餐已过期");
        }
        
        //如果用户不是送的套餐
        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        if (!Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.LIMITED_COUNT_TYPE) && userBatteryMemberCard.getRemainingNumber() < 0) {
                log.warn("ORDER WARN! user's count < 0 ,uid={},cardId={}", user.getUid(), electricityMemberCard.getType());
                return Triple.of(false, "100213", "用户套餐剩余次数不足");
            }
        }
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> checkUserExistsUnFinishOrder(Long uid) {
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(uid);
        if (Objects.nonNull(rentBatteryOrder) && Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
            return Triple.of(true, "100200", new ExchangeUnFinishOrderVo(rentBatteryOrder.getOrderId()));
        } else if (Objects.nonNull(rentBatteryOrder) && Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
            return Triple.of(true, "100202", new ExchangeUnFinishOrderVo(rentBatteryOrder.getOrderId()));
        }
        
        //是否存在未完成的换电订单
        ElectricityCabinetOrder oldElectricityCabinetOrder = queryByUid(uid);
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return Triple.of(true, "100201", new ExchangeUnFinishOrderVo(oldElectricityCabinetOrder.getOrderId()));
        }
        
        return Triple.of(false, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryOrderStatusForShow(String orderId) {
        ElectricityCabinetOrder electricityCabinetOrder = queryByOrderId(orderId);
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("ORDER ERROR! query order not found,uid={},orderId={}", SecurityUtils.getUid(), orderId);
            return Triple.of(false, "100221", "未能查找到订单");
        }
        
        String status = electricityCabinetOrder.getStatus();
        ExchangeOrderMsgShowVO showVo = new ExchangeOrderMsgShowVO();
        showVo.setType(ExchangeOrderMsgShowVO.TYPE_SUCCESS);
        
        if (isOpenPlaceCellStatus(status)) {
            showVo.setStatus(electricityCabinetOrder.getOldCellNo() + "号仓门开门中");
        }
        
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_OPEN_SUCCESS)) {
            showVo.setStatus(electricityCabinetOrder.getOldCellNo() + "号仓门开门成功，电池检测中");
        }
        
        //旧电池检测成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
            showVo.setStatus("旧电池已存入," + electricityCabinetOrder.getNewCellNo() + "号仓门开门中");
        }
        
        //订单状态新门成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)) {
            showVo.setStatus(electricityCabinetOrder.getNewCellNo() + "号仓门开门成功，电池检测中");
        }
        
        //订单状态新电池取走
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            showVo.setStatus("新电池已取走,订单完成");
        }
        
        if (isPlaceBatteryAllStatus(status)) {
            showVo.setPicture(ExchangeOrderMsgShowVO.PLACE_BATTERY_IMG);
        }
        
        if (isTakeBatteryAllStatus(status)) {
            showVo.setPicture(ExchangeOrderMsgShowVO.TAKE_BATTERY_IMG);
        }
        
        if (isExceptionOrder(status)) {
            showVo.setPicture(ExchangeOrderMsgShowVO.EXCEPTION_IMG);
            //检查这里是否需要自助开仓
            checkIsNeedSelfOpenCell(electricityCabinetOrder, showVo);
            showVo.setType(ExchangeOrderMsgShowVO.TYPE_FAIL);
            showVo.setStatus(redisService.get(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId));
        }
        return Triple.of(true, null, showVo);
    }
    
    
    @Override
    public ElectricityCabinetOrder selectLatestByUid(Long uid, Integer tenantId) {
        return electricityCabinetOrderMapper.selectLatestByUid(uid, tenantId);
    }
    
    @Override
    public Triple<Boolean, String, Object> bluetoothExchangeCheck(String productKey, String deviceName) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("BLUETOOTH EXCHANGE WARN! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100001", "未能找到用户");
        }
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.warn("BLUETOOTH EXCHANGE WARN! not found electricityCabinet,p={},d={}", productKey, deviceName);
            return Triple.of(false, "100003", "柜机不存在");
        }
        
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.warn("BLUETOOTH EXCHANGE WARN! not found store,eid={}", electricityCabinet.getId());
            return Triple.of(false, "100003", "柜机不存在");
        }
        
        if (!Objects.equals(store.getFranchiseeId(), userInfo.getFranchiseeId())) {
            log.warn("BLUETOOTH EXCHANGE WARN! user franchiseeId not equals store franchiseeId,uid={},storeId={}", userInfo.getFranchiseeId(), store.getId());
            return Triple.of(false, "ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致");
        }
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public ElectricityCabinetOrderVO selectLatestOrderAndCabinetInfo(Long uid) {
        return electricityCabinetOrderMapper.selectLatestOrderAndCabinetInfo(uid);
    }
    
    /**
     * 更新用户手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    @Override
    public Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone) {
        return electricityCabinetOrderMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }
    
    @Override
    @Slave
    public R listSuperAdminPage(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        Integer orderMode = electricityCabinetOrderQuery.getOrderMode();
        List<ElectricityCabinetOrderVO> electricityCabinetOrderVOList = new ArrayList<>();
        if (Objects.isNull(orderMode) || Objects.equals(orderMode, OrderDataModeEnums.CURRENT_ORDER.getCode())) {
            electricityCabinetOrderMapper.selectListSuperAdminPage(electricityCabinetOrderQuery);
        } else {
            electricityCabinetOrderHistoryService.listSuperAdminPage(electricityCabinetOrderQuery);
        }
        
        if (ObjectUtil.isEmpty(electricityCabinetOrderVOList)) {
            return R.ok(new ArrayList<>());
        }
        
        if (ObjectUtil.isNotEmpty(electricityCabinetOrderVOList)) {
            // 批量查询会员信息
            Map<Long, String> userNameMap = new HashMap<>();
            List<Long> uIdList = electricityCabinetOrderVOList.stream().map(ElectricityCabinetOrderVO::getUid).collect(Collectors.toList());
            List<UserInfo> userInfos = userInfoService.listByUidList(uIdList);
            if (ObjectUtils.isNotEmpty(userInfos)) {
                userNameMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getUid, UserInfo::getName));
            }
            Map<Long, String> finalUserNameMap = userNameMap;
            
            electricityCabinetOrderVOList.parallelStream().forEach(e -> {
                if (Objects.nonNull(e.getTenantId())) {
                    Tenant tenant = tenantService.queryByIdFromCache(e.getTenantId());
                    e.setTenantName(Objects.isNull(tenant) ? null : tenant.getName());
                }
                
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(e.getElectricityCabinetId());
                e.setElectricityCabinetName(Objects.isNull(electricityCabinet) ? "" : electricityCabinet.getName());
                
                // 设置会员名称
                if (ObjectUtils.isNotEmpty(finalUserNameMap.get(e.getUid()))) {
                    e.setUName(finalUserNameMap.get(e.getUid()));
                }
                
                if (Objects.nonNull(e.getStatus()) && e.getStatus().equals(ElectricityCabinetOrder.ORDER_CANCEL) || Objects.nonNull(e.getStatus()) && e.getStatus()
                        .equals(ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL)) {
                    
                    ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinetOrderQuery.getTenantId());
                    ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(e.getOrderId());
                    if (Objects.nonNull(electricityConfig) && Objects.equals(ElectricityConfig.ENABLE_SELF_OPEN, electricityConfig.getIsEnableSelfOpen()) && Objects
                            .nonNull(electricityExceptionOrderStatusRecord) && Objects
                            .equals(electricityExceptionOrderStatusRecord.getIsSelfOpenCell(), ElectricityExceptionOrderStatusRecord.NOT_SELF_OPEN_CELL)) {
                        if (Objects.equals(electricityExceptionOrderStatusRecord.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)
                                && (System.currentTimeMillis() - electricityExceptionOrderStatusRecord.getCreateTime()) / 1000 / 60 <= 3) {
                            e.setSelfOpenCell(ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY);
                        }
                    }
                }
                
                // 设置加盟商名称
                Franchisee franchisee = franchiseeService.queryByIdFromCache(e.getFranchiseeId());
                if (Objects.nonNull(franchisee)) {
                    e.setFranchiseeName(franchisee.getName());
                }
                
            });
        }
        
        return R.ok(electricityCabinetOrderVOList);
    }
    
    @Slave
    @Override
    public List<ElectricityCabinetOrder> selectTodayExchangeOrder(Integer eid, long todayStartTimeStamp, long todayEndTimeStamp, Integer tenantId) {
        return electricityCabinetOrderMapper.selectTodayExchangeOrder(eid, todayStartTimeStamp, todayEndTimeStamp, tenantId);
    }
    
    @Slave
    @Override
    public List<ElectricityCabinetOrder> selectMonthExchangeOrders(Integer eid, long todayStartTimeStamp, long todayEndTimeStamp, Integer tenantId) {
        return electricityCabinetOrderMapper.selectMonthExchangeOrders(eid, todayStartTimeStamp, todayEndTimeStamp, tenantId);
    }
    
    @Override
    public R queryListv2(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        List<ElectricityCabinetOrderVO> electricityCabinetOrderVOList = electricityCabinetOrderMapper.queryList(electricityCabinetOrderQuery);
        if (ObjectUtil.isEmpty(electricityCabinetOrderVOList)) {
            return R.ok(new ArrayList<>());
        }
        
        // 批量查询会员信息
        Map<Long, String> userNameMap = new HashMap<>();
        List<Long> uIdList = electricityCabinetOrderVOList.stream().map(ElectricityCabinetOrderVO::getUid).collect(Collectors.toList());
        List<UserInfo> userInfos = userInfoService.listByUidList(uIdList);
        if (ObjectUtils.isNotEmpty(userInfos)) {
            userNameMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getUid, UserInfo::getName));
        }
        Map<Long, String> finalUserNameMap = userNameMap;
        
        electricityCabinetOrderVOList.parallelStream().forEach(e -> {
            
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(e.getElectricityCabinetId());
            e.setElectricityCabinetName(Objects.isNull(electricityCabinet) ? "" : electricityCabinet.getName());
            
            // 设置会员名称
            if (ObjectUtils.isNotEmpty(finalUserNameMap.get(e.getUid()))) {
                e.setUName(finalUserNameMap.get(e.getUid()));
            }
            
            if (Objects.nonNull(e.getStatus()) && (e.getStatus().equals(ElectricityCabinetOrder.ORDER_CANCEL) || e.getStatus()
                    .equals(ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL))) {
                // 自主开仓判断
                isConformSpecialScene(e, electricityCabinetOrderQuery.getTenantId());
            }
            
            // 设置加盟商名称
            Franchisee franchisee = franchiseeService.queryByIdFromCache(e.getFranchiseeId());
            if (Objects.nonNull(franchisee)) {
                e.setFranchiseeName(franchisee.getName());
            }
            
        });
        
        return R.ok(electricityCabinetOrderVOList);
    }
    
    
    @Override
    public Triple<Boolean, String, Object> queryOrderStatusForShowV2(String orderId) {
        ElectricityCabinetOrder electricityCabinetOrder = queryByOrderId(orderId);
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("ORDER ERROR! query order not found,uid={},orderId={}", SecurityUtils.getUid(), orderId);
            return Triple.of(false, "100221", "未能查找到订单");
        }
        
        String status = electricityCabinetOrder.getStatus();
        ExchangeOrderMsgShowVO showVo = new ExchangeOrderMsgShowVO();
        showVo.setType(ExchangeOrderMsgShowVO.TYPE_SUCCESS);
        showVo.setSource(electricityCabinetOrder.getSource());
        
        if (isOpenPlaceCellStatus(status)) {
            showVo.setStatus(electricityCabinetOrder.getOldCellNo() + "号仓门开门中");
        }
        
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_OPEN_SUCCESS)) {
            showVo.setStatus(electricityCabinetOrder.getOldCellNo() + "号仓门开门成功，电池检测中");
        }
        
        //旧电池检测成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
            showVo.setStatus("旧电池已存入," + electricityCabinetOrder.getNewCellNo() + "号仓门开门中");
        }
        
        //订单状态新门成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)) {
            showVo.setStatus(electricityCabinetOrder.getNewCellNo() + "号仓门开门成功，电池检测中");
        }
        
        //订单状态新电池取走
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            showVo.setStatus("新电池已取走,订单完成");
        }
        
        if (isPlaceBatteryAllStatus(status)) {
            showVo.setPicture(ExchangeOrderMsgShowVO.PLACE_BATTERY_IMG);
        }
        
        if (isTakeBatteryAllStatus(status)) {
            showVo.setPicture(ExchangeOrderMsgShowVO.TAKE_BATTERY_IMG);
        }
        //  异常订单
        if (isExceptionOrder(status)) {
            showVo.setPicture(ExchangeOrderMsgShowVO.EXCEPTION_IMG);
            showVo.setCheckTimeOut(ExchangeOrderMsgShowVO.CHECK_TIME_OUT);
            showVo.setType(ExchangeOrderMsgShowVO.TYPE_FAIL);
            
            showVo.setStatus(redisService.get(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId));
        }
        return Triple.of(true, null, showVo);
    }
    
    @Override
    public R lessExchangeSelfOpenCell(LessExchangeSelfOpenCellQuery query) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("self open cell order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //是否存在未完成的租电池订单
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(user.getUid());
        if (Objects.nonNull(rentBatteryOrder)) {
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "100023", "存在未完成租电订单，不能自助开仓");
            } else if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "100024", "存在未完成还电订单，不能自助开仓");
            }
        }
        
        ElectricityCabinetOrder oldElectricityCabinetOrder = queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "100022", "存在未完成换电订单，不能自助开仓");
        }
        
        ElectricityCabinetOrder electricityCabinetOrder = queryByOrderId(query.getOrderId());
        if (Objects.isNull(electricityCabinetOrder)) {
            log.warn("self open cell WARN! not found order,orderId={} ", query.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(query.getEid());
        if (Objects.isNull(electricityCabinet)) {
            log.warn("self open cell WARN! not found electricityCabinet ！electricityCabinetId={}", query.getEid());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        if (!eleResult) {
            log.warn("self open cell WARN!  electricityCabinet is offline ！electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }
        
        //换电柜营业时间
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }
        
        // 自主开仓时间校验
        String orderExceptionStartTime = redisService.get(CacheConstant.ALLOW_SELF_OPEN_CELL_START_TIME + query.getOrderId());
        log.info("lessExchangeSelfOpenCell Info! orderExceptionStartTime is {}", orderExceptionStartTime);
        if (StrUtil.isEmpty(orderExceptionStartTime)) {
            return R.fail("100667", "自主开仓超时");
        }
        if (Double.valueOf(System.currentTimeMillis() - Long.valueOf(orderExceptionStartTime)) / 1000 / 60 > 5) {
            log.warn("SELF OPEN CELL WARN! self open cell timeout,orderId={}", query.getOrderId());
            return R.fail("100667", "自主开仓超时");
        }
        
        
        //查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            log.warn("self open cell order  WARN! not found store ！electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.warn("self open cell order  WARN! not found store ！storeId={}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        
        //查找门店加盟商
        if (Objects.isNull(store.getFranchiseeId())) {
            log.warn("self open cell order  WARN! not found Franchisee ！storeId={}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }
        
        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("self open cell order  WARN! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("self open cell order WARN! user is unUsable! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("self open cell order WARN! user not auth,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        
        //判断该换电柜加盟商和用户加盟商是否一致
        if (!Objects.equals(store.getFranchiseeId(), userInfo.getFranchiseeId())) {
            log.warn("self open cell order  WARN!FranchiseeId is not equal!uid={} , FranchiseeId1={} ,FranchiseeId2={}", user.getUid(), store.getFranchiseeId(),
                    userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
        }
        
        //未租电池
        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("self open cell order  WARN! user not rent battery,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }
        
        ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(query.getEid(), String.valueOf(query.getCellNo()));
        if (Objects.isNull(electricityCabinetBox)) {
            log.warn("self open cell order  WARN! not find cellNO! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0006", "未找到此仓门");
        }
        
        // 自主开仓特殊场景校验
        if (Objects.nonNull(query.getEid()) && Objects.nonNull(query.getCellNo())) {
            Integer isExistNewExchangeOrder = electricityCabinetOrderMapper.existSameCabinetCellSameTimeOpenExchangeOrder(electricityCabinetOrder.getId(),
                    electricityCabinetOrder.getElectricityCabinetId(), query.getCellNo());
            if (Objects.nonNull(isExistNewExchangeOrder)) {
                log.warn("selfOpenCell.existExchangeOrder, orderId is {}", electricityCabinetOrder.getOrderId());
                return R.fail("100667", "用户自主开仓，系统识别归还仓门内电池为新订单，无法执行自助开仓操作");
            }
            Integer isExistNewReturnOrder = rentBatteryOrderService.existSameCabinetCellSameTimeOpenReturnOrder(electricityCabinetOrder.getCreateTime(),
                    electricityCabinetOrder.getElectricityCabinetId(), query.getCellNo());
            if (Objects.nonNull(isExistNewReturnOrder)) {
                log.warn("selfOpenCell.existNewReturnOrder, orderId is {}", electricityCabinetOrder.getOrderId());
                return R.fail("100667", "用户自主开仓，系统识别归还仓门内电池为新订单，无法执行自助开仓操作");
            }
            Integer isExistNewOperRecord = electricityCabinetPhysicsOperRecordService.existSameCabinetCellSameTimeOpenRecord(electricityCabinetOrder.getCreateTime(),
                    electricityCabinetOrder.getElectricityCabinetId(), query.getCellNo());
            if (Objects.nonNull(isExistNewOperRecord)) {
                log.warn("selfOpenCell.existNewOperRecord, orderId is {}", electricityCabinetOrder.getOrderId());
                return R.fail("100667", "用户自主开仓，系统识别归还仓门内电池为新订单，无法执行自助开仓操作");
            }
        }
        
        try {
            // 用户自助开仓
            if (electricityCabinet.getVersion().isBlank() || VersionUtil.compareVersion(electricityCabinet.getVersion(), ElectricityCabinetOrderOperHistory.THREE_PERIODS_SUCCESS_RATE_VERSION) < 0) {
                ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder().createTime(System.currentTimeMillis()).orderId(query.getOrderId())
                        .tenantId(electricityCabinet.getTenantId()).msg("用户自助开仓").seq(ElectricityCabinetOrderOperHistory.SELF_OPEN_CELL_SEQ)
                        .type(ElectricityCabinetOrderOperHistory.ORDER_TYPE_EXCHANGE).result(ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS).build();
                
                electricityCabinetOrderOperHistoryService.insert(history);
            }
            
            // 如果旧电池检测失败会在这个表里面，导致在订单记录中存在自主开仓，所以移除旧版本的自主开仓记录
            electricityExceptionOrderStatusRecordService.queryRecordAndUpdateStatus(electricityCabinetOrder.getOrderId());
            
            
            ElectricityCabinetOrder electricityCabinetOrderUpdate = new ElectricityCabinetOrder();
            electricityCabinetOrderUpdate.setId(electricityCabinetOrder.getId());
            electricityCabinetOrderUpdate.setUpdateTime(System.currentTimeMillis());
            electricityCabinetOrderUpdate.setRemark("用户自助开仓");
            update(electricityCabinetOrderUpdate);
            
            //发送自助开仓命令
            //发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("orderId", query.getOrderId());
            dataMap.put("cellNo", query.getCellNo());
            if (!Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS) && Objects.equals(
                    electricityCabinetOrder.getNewCellNo(), query.getCellNo())) {
                dataMap.put("isTakeCell", true);
            }
            dataMap.put("userSelfOpenCell", true);
           
            //dataMap.put("batteryName", electricityCabinetOrder.getOldElectricityBatterySn());
            
            String sessionId = CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId();
            
            HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(sessionId).data(dataMap).productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.SELF_OPEN_CELL).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
            return R.ok(sessionId);
        } catch (Exception e) {
            log.error("order is error" + e);
            return R.fail("ELECTRICITY.0025", "自助开仓失败");
        } finally {
            redisService.delete(CacheConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + electricityCabinetOrder.getElectricityCabinetId() + "_" + query.getCellNo());
        }
        
    }
    
    private void checkIsNeedSelfOpenCell(ElectricityCabinetOrder electricityCabinetOrder, ExchangeOrderMsgShowVO showVo) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinetOrder.getTenantId());
        if (Objects.isNull(electricityConfig) || Objects.equals(ElectricityConfig.DISABLE_SELF_OPEN, electricityConfig.getIsEnableSelfOpen())) {
            return;
        }
        
        ElectricityExceptionOrderStatusRecord statusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(electricityCabinetOrder.getOrderId());
        if (Objects.isNull(statusRecord)) {
            return;
        }
        
        showVo.setSelfOpenCell(ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY);
        
    }
    
    private boolean isExceptionOrder(String status) {
        return status.equals(ElectricityCabinetOrder.ORDER_CANCEL) || ElectricityCabinetOrder.INIT_DEVICE_USING.equals(status) || status
                .equals(ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL);
    }
    
    private boolean isTakeBatteryAllStatus(String status) {
        return status.equals(ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS) || status.equals(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS) || status
                .equals(ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS);
    }
    
    private boolean isPlaceBatteryAllStatus(String status) {
        return status.equals(ElectricityCabinetOrder.INIT) || status.equals(ElectricityCabinetOrder.INIT_OPEN_SUCCESS);
    }
    
    private boolean isOpenPlaceCellStatus(String status) {
        return status.equals(ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS) || status.equals(ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS) || status
                .equals(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS) || status.equals(ElectricityCabinetOrder.INIT);
    }
    
    
    
    private Triple<Boolean, String, Object> handlerSingleExchangeBatteryV3(UserInfo userInfo, Store store, ElectricityCabinet electricityCabinet, OrderQueryV3 orderQuery,
            List<String> batteryTypeList) {
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("ORDER WARN! not found franchisee,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        //判断用户押金
        Triple<Boolean, String, Object> checkUserDepositResult = checkUserDeposit(userInfo, store, userInfo);
        if (Boolean.FALSE.equals(checkUserDepositResult.getLeft())) {
            return checkUserDepositResult;
        }
        
        //判断用户套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("ORDER WARN! user haven't memberCard uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户未开通套餐");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("ORDER WARN! user's member card is stop! uid={}", userInfo.getUid());
            return Triple.of(false, "100211", "换电套餐停卡审核中");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("ORDER WARN! user's member card is stop! uid={}", userInfo.getUid());
            return Triple.of(false, "100211", "换电套餐已暂停");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("ORDER WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }
        
        //判断用户电池服务费
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("ORDER WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.100000", "存在电池服务费");
        }
        
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)
                && userBatteryMemberCard.getRemainingNumber() <= 0)) {
            log.warn("ORDER WARN! battery memberCard is Expire,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0023", "套餐已过期");
        }
        
        //判断车电关联是否可换电
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenCarBatteryBind(), ElectricityConfig.ENABLE_CAR_BATTERY_BIND)) {
            if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
                try {
                    if (carRentalPackageMemberTermBizService.isExpirePackageOrder(userInfo.getTenantId(), userInfo.getUid())) {
                        log.warn("ORDER WARN! user car memberCard expire,uid={}", userInfo.getUid());
                        return Triple.of(false, "100233", "租车套餐已过期");
                    }
                } catch (Exception e) {
                    log.error("ORDER ERROR!acquire car membercard expire result fail,uid={}", userInfo.getUid(), e);
                }
            }
        }
        
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
        
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW) && Objects.isNull(electricityBattery)) {
            return Triple.of(false, "300880", "系统检测到您未绑定电池，请检查");
        }
        
        // 多次扫码处理
        if (!Objects.equals(orderQuery.getExchangeBatteryType(), OrderQueryV3.NORMAL_EXCHANGE)) {
            if (StringUtils.isNotBlank(electricityCabinet.getVersion())
                    && VersionUtil.compareVersion(electricityCabinet.getVersion(), ORDER_LESS_TIME_EXCHANGE_CABINET_VERSION) >= 0) {
                LessTimeExchangeDTO exchangeDTO = LessTimeExchangeDTO.builder().eid(orderQuery.getEid()).isReScanExchange(orderQuery.getIsReScanExchange()).build();
                Pair<Boolean, Object> pair = this.lessTimeExchangeTwoCountAssert(userInfo, electricityCabinet, electricityBattery, exchangeDTO, OrderCheckEnum.ORDER.getCode());
                if (pair.getLeft()) {
                    // 返回让前端选择
                    return Triple.of(true, null, pair.getRight());
                }
            }
        }
        
        //默认是小程序下单
        if (Objects.isNull(orderQuery.getSource())) {
            orderQuery.setSource(OrderQuery.SOURCE_WX_MP);
        }
        
        Pair<Boolean, Integer> usableEmptyCellNo = electricityCabinetService.findUsableEmptyCellNoV2(userInfo.getUid(), electricityCabinet.getId(),
                electricityCabinet.getVersion());
        if (Boolean.FALSE.equals(usableEmptyCellNo.getLeft())) {
            return Triple.of(false, "100215", "当前无空余格挡可供换电，请联系客服！");
        }
        
        // 分配满电仓门
        Triple<Boolean, String, Object> usableBatteryCellNoResult = electricityCabinetService.findUsableBatteryCellNoV3(electricityCabinet.getId(), franchisee,
                electricityCabinet.getFullyCharged(), electricityBattery, userInfo.getUid());
        if (Boolean.FALSE.equals(usableBatteryCellNoResult.getLeft())) {
            return Triple.of(false, usableBatteryCellNoResult.getMiddle(), usableBatteryCellNoResult.getRight());
        }
        
        //修改按此套餐的次数
        Triple<Boolean, String, String> modifyResult = checkAndModifyMemberCardCount(userBatteryMemberCard, batteryMemberCard);
        if (Boolean.FALSE.equals(modifyResult.getLeft())) {
            return Triple.of(false, modifyResult.getMiddle(), modifyResult.getRight());
        }
        
        ElectricityCabinetBox electricityCabinetBox = (ElectricityCabinetBox) usableBatteryCellNoResult.getRight();
        
        ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
                .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.EXCHANGE_BATTERY, userInfo.getUid())).uid(userInfo.getUid()).phone(userInfo.getPhone())
                .electricityCabinetId(orderQuery.getEid()).oldCellNo(usableEmptyCellNo.getRight()).newCellNo(Integer.parseInt(electricityCabinetBox.getCellNo()))
                .orderSeq(ElectricityCabinetOrder.STATUS_INIT).status(ElectricityCabinetOrder.INIT).source(orderQuery.getSource()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).storeId(electricityCabinet.getStoreId()).franchiseeId(store.getFranchiseeId()).tenantId(TenantContextHolder.getTenantId())
                .build();
    
        if (ExchangeTypeEnum.NORMAL_EXCHANGE.getCode().equals(electricityCabinetOrder.getSource())||ExchangeTypeEnum.SELECTION_EXCHANGE.getCode().equals(electricityCabinetOrder.getSource())){
            electricityCabinetOrder.setChannel(ChannelSourceContextHolder.get());
        }
        
        electricityCabinetOrderMapper.insert(electricityCabinetOrder);
        
        //记录活跃时间
        userActiveInfoService.userActiveRecord(userInfo);
        
        HashMap<String, Object> commandData = Maps.newHashMap();
        commandData.put("orderId", electricityCabinetOrder.getOrderId());
        commandData.put("placeCellNo", electricityCabinetOrder.getOldCellNo());
        commandData.put("takeCellNo", electricityCabinetOrder.getNewCellNo());
        commandData.put("phone", userInfo.getPhone());
        
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
            commandData.put("userBindingBatterySn", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getSn());
        }
        
        commandData.put("newUserBindingBatterySn", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getSn());
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.nonNull(electricityBattery)) {
                commandData.put("multiBatteryModelName", electricityBattery.getModel());
                commandData.put("multiBatteryModelNameList", JsonUtil.toJson(batteryTypeList));
            } else {
                ElectricityBattery lastElectricityBattery = selectLastExchangeOrderBattery(userInfo);
                commandData.put("multiBatteryModelName", Objects.isNull(lastElectricityBattery) ? "UNKNOWN" : lastElectricityBattery.getModel());
                commandData.put("multiBatteryModelNameList", JsonUtil.toJson(batteryTypeList));
            }
        }
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + ":" + electricityCabinetOrder.getOrderId())
                .data(commandData).productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.ELE_COMMAND_NEW_EXCHANGE_ORDER).build();
        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        if (Boolean.FALSE.equals(result.getLeft())) {
            return Triple.of(false, "100218", "下单消息发送失败");
        }
        
        ExchangeUserSelectVo vo = new ExchangeUserSelectVo();
        vo.setIsEnterMoreExchange(ExchangeUserSelectVo.NOT_ENTER_MORE_EXCHANGE);
        vo.setOrderId(electricityCabinetOrder.getOrderId());
        return Triple.of(true, null, vo);
    }
    
    
    private Triple<Boolean, String, Object> handlerExchangeBatteryCarV3(UserInfo userInfo, Store store, ElectricityCabinet electricityCabinet, OrderQueryV3 orderQuery,
            List<String> batteryTypeList) {
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("EXCHANGE WARN! not found franchisee,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        //判断车电一体套餐状态
        if (carRentalPackageMemberTermBizService.isExpirePackageOrder(userInfo.getTenantId(), userInfo.getUid())) {
            log.warn("EXCHANGE WARN! user memberCard disable,uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户套餐不可用");
        }
       
        //判断用户电池服务费
        if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid()))) {
            log.warn("ORDER WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "300001", "存在滞纳金，请先缴纳");
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            log.warn("ORDER WARN! not found electricityConfig,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.100000", "系统异常");
        }
        
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
        
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW) && Objects.isNull(electricityBattery)) {
            return Triple.of(false, "300880", "系统检测到您未绑定电池，请检查");
        }
        
        // 多次换电拦截
        if (!Objects.equals(orderQuery.getExchangeBatteryType(), OrderQueryV3.NORMAL_EXCHANGE)) {
            if (StringUtils.isNotBlank(electricityCabinet.getVersion())
                    && VersionUtil.compareVersion(electricityCabinet.getVersion(), ORDER_LESS_TIME_EXCHANGE_CABINET_VERSION) >= 0) {
                
                LessTimeExchangeDTO exchangeDTO = LessTimeExchangeDTO.builder().eid(orderQuery.getEid()).isReScanExchange(orderQuery.getIsReScanExchange()).build();
                Pair<Boolean, Object> pair = this.lessTimeExchangeTwoCountAssert(userInfo, electricityCabinet, electricityBattery, exchangeDTO, OrderCheckEnum.ORDER.getCode());
                if (pair.getLeft()) {
                    // 返回让前端选择
                    return Triple.of(true, null, pair.getRight());
                }
            }
        }
        
        //默认是小程序下单
        if (Objects.isNull(orderQuery.getSource())) {
            orderQuery.setSource(OrderQuery.SOURCE_WX_MP);
        }
        
        Pair<Boolean, Integer> usableEmptyCellNo = electricityCabinetService.findUsableEmptyCellNoV2(userInfo.getUid(), electricityCabinet.getId(),
                electricityCabinet.getVersion());
        if (Boolean.FALSE.equals(usableEmptyCellNo.getLeft())) {
            return Triple.of(false, "100215", "当前无空余格挡可供换电，请联系客服！");
        }
        
       
        Triple<Boolean, String, Object> usableBatteryCellNoResult = electricityCabinetService.findUsableBatteryCellNoV3(electricityCabinet.getId(), franchisee,
                electricityCabinet.getFullyCharged(), electricityBattery, userInfo.getUid());
        if (Boolean.FALSE.equals(usableBatteryCellNoResult.getLeft())) {
            return Triple.of(false, usableBatteryCellNoResult.getMiddle(), usableBatteryCellNoResult.getRight());
        }
        
        //修改按此套餐的次数
        carRentalPackageMemberTermBizService.substractResidue(userInfo.getTenantId(), userInfo.getUid());
        
        ElectricityCabinetBox electricityCabinetBox = (ElectricityCabinetBox) usableBatteryCellNoResult.getRight();
        
        ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
                .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.EXCHANGE_BATTERY, userInfo.getUid())).uid(userInfo.getUid()).phone(userInfo.getPhone())
                .electricityCabinetId(orderQuery.getEid()).oldCellNo(usableEmptyCellNo.getRight()).newCellNo(Integer.parseInt(electricityCabinetBox.getCellNo()))
                .orderSeq(ElectricityCabinetOrder.STATUS_INIT).status(ElectricityCabinetOrder.INIT).source(orderQuery.getSource()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).storeId(electricityCabinet.getStoreId()).franchiseeId(store.getFranchiseeId()).tenantId(TenantContextHolder.getTenantId())
                .build();
    
        if (ExchangeTypeEnum.NORMAL_EXCHANGE.getCode().equals(electricityCabinetOrder.getSource())||ExchangeTypeEnum.SELECTION_EXCHANGE.getCode().equals(electricityCabinetOrder.getSource())){
            electricityCabinetOrder.setChannel(ChannelSourceContextHolder.get());
        }
        
        electricityCabinetOrderMapper.insert(electricityCabinetOrder);
        
        //记录活跃时间
        userActiveInfoService.userActiveRecord(userInfo);
        
        HashMap<String, Object> commandData = Maps.newHashMap();
        commandData.put("orderId", electricityCabinetOrder.getOrderId());
        commandData.put("placeCellNo", electricityCabinetOrder.getOldCellNo());
        commandData.put("takeCellNo", electricityCabinetOrder.getNewCellNo());
        commandData.put("phone", userInfo.getPhone());
        
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
            commandData.put("userBindingBatterySn", electricityBattery.getSn());
        }
        
        commandData.put("newUserBindingBatterySn", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getSn());
     
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.nonNull(electricityBattery)) {
                commandData.put("multiBatteryModelName", electricityBattery.getModel());
                commandData.put("multiBatteryModelNameList", JsonUtil.toJson(Lists.newArrayList(electricityBattery.getModel())));
            } else {
                ElectricityBattery lastElectricityBattery = selectLastExchangeOrderBattery(userInfo);
                commandData.put("multiBatteryModelNameList", JsonUtil.toJson(batteryTypeList));
                commandData.put("multiBatteryModelName", Objects.isNull(lastElectricityBattery) ? "UNKNOWN" : lastElectricityBattery.getModel());
            }
        }
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + ":" + electricityCabinetOrder.getOrderId())
                .data(commandData).productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.ELE_COMMAND_NEW_EXCHANGE_ORDER).build();
        Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        if (Boolean.FALSE.equals(result.getLeft())) {
            return Triple.of(false, "100218", "下单消息发送失败");
        }
        
        ExchangeUserSelectVo vo = new ExchangeUserSelectVo();
        vo.setIsEnterMoreExchange(ExchangeUserSelectVo.NOT_ENTER_MORE_EXCHANGE);
        vo.setOrderId(electricityCabinetOrder.getOrderId());
        return Triple.of(true, null, vo);
    }
    
    @Override
    public R openFullCell(OpenFullCellQuery query) {
//        ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder().createTime(System.currentTimeMillis()).orderId(query.getOrderId())
//                .tenantId(electricityCabinet.getTenantId()).msg("自助开仓").seq(ElectricityCabinetOrderOperHistory.SELF_OPEN_CELL_SEQ)
//                .type(ElectricityCabinetOrderOperHistory.ORDER_TYPE_EXCHANGE).result(ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS).build();
//        electricityCabinetOrderOperHistoryService.insert(history);

//        ElectricityCabinetOrder electricityCabinetOrderUpdate = new ElectricityCabinetOrder();
//        electricityCabinetOrderUpdate.setId(electricityCabinetOrder.getId());
//        electricityCabinetOrderUpdate.setUpdateTime(System.currentTimeMillis());
//        electricityCabinetOrderUpdate.setRemark("自助开仓");
//        update(electricityCabinetOrderUpdate);
        
        ElectricityCabinetOrder electricityCabinetOrder = queryByOrderId(query.getOrderId());
        if (Objects.isNull(electricityCabinetOrder)) {
            log.warn("self open cell WARN! not found order,orderId={} ", query.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.warn("self open cell WARN! not found electricityCabinet ！electricityCabinetId={}", electricityCabinetOrder.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        return null;
    }
    
    @Override
    @Slave
    public List<ElectricityCabinetOrder> listByOrderIdList(Set<String> exchangeOrderIdList) {
        return electricityCabinetOrderMapper.selectListByOrderIdList(exchangeOrderIdList);
    }
    
    /**
     * 开满电仓命令下发
     * @param cabinetOrder
     * @param cabinet
     * @param cellNo
     * @param batteryName
     * @return
     */
    private String openFullBatteryCellHandler(ElectricityCabinetOrder cabinetOrder, ElectricityCabinet cabinet, Integer cellNo, String batteryName,
            String oldCell) {
        
        if (cabinet.getVersion().isBlank() || VersionUtil.compareVersion(cabinet.getVersion(), ElectricityCabinetOrderOperHistory.THREE_PERIODS_SUCCESS_RATE_VERSION) < 0) {
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder().createTime(System.currentTimeMillis())
                    .orderId(cabinetOrder.getOrderId()).tenantId(cabinet.getTenantId()).msg("电池检测成功")
                    .seq(ElectricityCabinetOrderOperHistory.OPEN_FULL_CELL_BATTERY).type(ElectricityCabinetOrderOperHistory.ORDER_TYPE_EXCHANGE)
                    .result(ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS).build();
            
            electricityCabinetOrderOperHistoryService.insert(history);
        }
        
        ElectricityCabinetOrder electricityCabinetOrderUpdate = new ElectricityCabinetOrder();
        electricityCabinetOrderUpdate.setId(cabinetOrder.getId());
        electricityCabinetOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrderUpdate.setNewCellNo(cellNo);
        electricityCabinetOrderUpdate.setRemark("取电流程");
        update(electricityCabinetOrderUpdate);
        
        //发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("orderId", cabinetOrder.getOrderId());
        dataMap.put("placeCellNo", oldCell);
        dataMap.put("takeCellNo", cellNo);
        dataMap.put("batteryName", batteryName);
        
        String sessionId = CacheConstant.OPEN_FULL_CELL + "_" + cabinetOrder.getOrderId();
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(sessionId).data(dataMap).productKey(cabinet.getProductKey()).deviceName(cabinet.getDeviceName())
                .command(ElectricityIotConstant.OPEN_FULL_CELL).build();
        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, cabinet);
        
        // 设置状态redis
        redisService.set(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + cabinetOrder.getOrderId(), "取电中，请稍后", 5L, TimeUnit.MINUTES);
        
        return sessionId;
    }
    
    
    @Override
    public Triple<Boolean, String, Object> allocateFullBatteryBox(ElectricityCabinet electricityCabinet, UserInfo userInfo, Franchisee franchisee) {
        // 满电标准的电池
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryElectricityBatteryBox(electricityCabinet, null, null,
                electricityCabinet.getFullyCharged());
        
        // 过滤掉电池名称不符合标准的
        List<ElectricityCabinetBox> exchangeableList = electricityCabinetBoxList.stream().filter(item -> filterNotExchangeable(item)).collect(Collectors.toList());
        
        if (CollectionUtils.isEmpty(exchangeableList)) {
            log.warn("Get Full Battery Warn ! not found electricityCabinetBoxList,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
        }
        
        List<Long> batteryIds = exchangeableList.stream().map(ElectricityCabinetBox::getBId).collect(Collectors.toList());
        
        List<ElectricityBattery> electricityBatteries = electricityBatteryService.selectByBatteryIds(batteryIds);
        if (CollUtil.isEmpty(electricityBatteries)) {
            return Triple.of(false, "100225", "电池不存在");
        }
        
        // 把本柜机加盟商的绑定电池信息拿出来
        electricityBatteries = electricityBatteries.stream().filter(e -> Objects.equals(e.getFranchiseeId(), franchisee.getId())).collect(Collectors.toList());
        if (!DataUtil.collectionIsUsable(electricityBatteries)) {
            log.warn("EXCHANGE WARN!battery not bind franchisee,eid={}", electricityCabinet.getId());
            return Triple.of(false, "100219", "电池没有绑定加盟商,无法换电，请联系客服在后台绑定");
        }
        
        // 获取全部可用电池id
        List<Long> bindingBatteryIds = electricityBatteries.stream().map(ElectricityBattery::getId).collect(Collectors.toList());
        
        // 把加盟商绑定的电池过滤出来
        exchangeableList = exchangeableList.stream().filter(e -> bindingBatteryIds.contains(e.getBId())).collect(Collectors.toList());
        
        
        
        String fullBatteryCell = null;
        
        for (int i = 0; i < exchangeableList.size(); i++) {
            // 20240614修改：过滤掉电池不符合标准的电池
            fullBatteryCell = rentBatteryOrderService.acquireFullBatteryBox(exchangeableList, userInfo, franchisee, electricityCabinet.getFullyCharged());
            if (StringUtils.isBlank(fullBatteryCell)) {
                log.info("RENT BATTERY INFO!not found fullBatteryCell,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
            }
            
            if (redisService.setNx(CacheConstant.CACHE_LAST_ALLOCATE_FULLY_BATTERY_CELL + electricityCabinet.getId() + ":" + fullBatteryCell, "1", 4 * 1000L, false)) {
                return Triple.of(true, null, fullBatteryCell);
            }
        }
        
        return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
    }
    private boolean filterNotExchangeable(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.nonNull(electricityCabinetBox) && Objects.nonNull(electricityCabinetBox.getPower()) && StringUtils.isNotBlank(electricityCabinetBox.getSn()) && !StringUtils.startsWithIgnoreCase(
                electricityCabinetBox.getSn(), "UNKNOW");
    }
 
}
