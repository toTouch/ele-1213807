package com.xiliulou.electricity.queue;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.EleExceptionLockStorehouseDoorConfig;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.CabinetBoxConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.constant.OrderForBatteryConstants;
import com.xiliulou.electricity.dto.EleOpenDTO;
import com.xiliulou.electricity.dto.battery.BatteryLabelModifyDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryTrackRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityExceptionOrderStatusRecord;
import com.xiliulou.electricity.entity.ExchangeBatterySoc;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UserBattery;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.enums.enterprise.UserCostTypeEnum;
import com.xiliulou.electricity.enums.thirdParty.ThirdPartyOperatorTypeEnum;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseUserCostRecordService;
import com.xiliulou.electricity.service.retrofit.BatteryPlatRetrofitService;
import com.xiliulou.electricity.service.thirdParty.PushDataToThirdService;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.utils.OrderForBatteryUtil;
import com.xiliulou.electricity.web.query.battery.BatteryChangeSocQuery;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.xiliulou.electricity.entity.ExchangeBatterySoc.RETURN_POWER_DEFAULT;

/**
 * @author: lxc
 * @Date: 2020/12/3 08:31
 * @Description:
 */

@Service
@Slf4j
public class EleOperateQueueHandler {
    
    ExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("eleOperateQueueExecutor", 20, "ELE_OPERATE_QUEUE_EXECUTOR");
    
    ExecutorService startService = XllThreadPoolExecutors.newFixedThreadPool("eleOperateQueueStart", 1, "ELE_OPERATE_QUEUE_START");
    
    private volatile boolean shutdown = false;
    
    private final LinkedBlockingQueue<EleOpenDTO> queue = new LinkedBlockingQueue<>();
    
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
    
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    WechatTemplateNotificationConfig wechatTemplateNotificationConfig;
    
    @Autowired
    EleExceptionLockStorehouseDoorConfig eleExceptionLockStorehouseDoorConfig;
    
    @Autowired
    ElectricityExceptionOrderStatusRecordService electricityExceptionOrderStatusRecordService;
    
    @Autowired
    BatteryTrackRecordService batteryTrackRecordService;
    
    @Autowired
    UserBatteryService userBatteryService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    TenantService tenantService;
    
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    
    @Autowired
    BatteryPlatRetrofitService batteryPlatRetrofitService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    EnterpriseRentRecordService enterpriseRentRecordService;
    
    @Autowired
    EnterpriseUserCostRecordService enterpriseUserCostRecordService;
    
    @Autowired
    UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Autowired
    private ExchangeBatterySocService exchangeBatterySocService;
    
    @Resource
    private PushDataToThirdService pushDataToThirdService;
    
    @Resource
    private ExchangeExceptionHandlerService exceptionHandlerService;

    @Resource
    private ReturnBatterySuccessHandlerService returnBatterySuccessHandlerService;
    
    XllThreadPoolExecutorService callBatterySocThreadPool = XllThreadPoolExecutors.newFixedThreadPool("CALL_RENT_SOC_CHANGE", 1, "callRentSocChange");
    
    XllThreadPoolExecutorService operateBatterSocThreadPool = XllThreadPoolExecutors.newFixedThreadPool("OPERATE_BATTERY_SOC_ANALYZE", 1, "operate-battery-soc-pool-thread");
    
    
    @EventListener({WebServerInitializedEvent.class})
    public void startHandleElectricityCabinetOperate() {
        initElectricityCabinetOperate();
    }
    
    private void initElectricityCabinetOperate() {
        startService.execute(() -> {
            while (!shutdown) {
                EleOpenDTO eleOpenDTO = null;
                try {
                    eleOpenDTO = queue.take();
                    log.info(" QUEUE get a message ={}", eleOpenDTO);
                    
                    EleOpenDTO finalOpenDTO = eleOpenDTO;
                    executorService.execute(() -> {
                        handleOrderAfterOperated(finalOpenDTO);
                    });
                    
                } catch (Exception e) {
                    log.error("ELECTRICITY CABINET OPERATE QUEUE ERROR! ", e);
                }
                
            }
        });
    }
    
    /**
     * 接收到响应的操作信息
     *
     * @param finalOpenDTO
     */
    private void handleOrderAfterOperated(EleOpenDTO finalOpenDTO) {
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());
        
        //参数
        String type = finalOpenDTO.getType();
        String orderId = finalOpenDTO.getOrderId();
        Double orderSeq = finalOpenDTO.getOrderSeq();
        
        //查找订单
        if (Objects.equals(type, ElectricityIotConstant.ELE_COMMAND_INIT_EXCHANGE_ORDER_RSP) || Objects.equals(type,
                ElectricityIotConstant.ELE_COMMAND_COMPLETE_EXCHANGE_ORDER_RSP)) {
            
            ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.queryByOrderId(orderId);
            if (Objects.isNull(electricityCabinetOrder)) {
                return;
            }
            
            //是否开启异常仓门锁仓
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinetOrder.getTenantId());
            if (Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsOpenDoorLock(), ElectricityConfig.OPEN_DOOR_LOCK)) {
                lockExceptionDoor(electricityCabinetOrder, null, finalOpenDTO);
            }
            
            //若app订单状态大于云端订单状态则处理
            if (Objects.isNull(orderSeq) || orderSeq - electricityCabinetOrder.getOrderSeq() >= 1 || Math.abs(orderSeq - electricityCabinetOrder.getOrderSeq()) < 1) {
                if (Objects.equals(type, ElectricityIotConstant.ELE_COMMAND_INIT_EXCHANGE_ORDER_RSP)) {
                    handelInitExchangeOrder(electricityCabinetOrder, finalOpenDTO, electricityConfig);
                }
                
                if (Objects.equals(type, ElectricityIotConstant.ELE_COMMAND_COMPLETE_EXCHANGE_ORDER_RSP)) {
                    handelCompleteExchangeOrder(electricityCabinetOrder, finalOpenDTO);
                }
            }
        } else {
            if (!redisService.setNx(CacheConstant.RENT_BATTERY_ORDER_HANDLE_LIMIT + finalOpenDTO.getOrderId(), "1", 200L, false)) {
                log.info("RENT ORDER INFO! order is being processed,requestId={},orderId={}", finalOpenDTO.getSessionId(), finalOpenDTO.getOrderId());
                return;
            }
            
            try {
                //租还电池订单
                RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByOrderId(orderId);
                if (Objects.isNull(rentBatteryOrder)) {
                    return;
                }
                
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(rentBatteryOrder.getElectricityCabinetId());
                if (Objects.isNull(electricityCabinet)) {
                    log.warn("RENT ORDER WARN! not found electricityCabinet,requestId={},orderId={},uid={}", finalOpenDTO.getSessionId(), finalOpenDTO.getOrderId(),
                            rentBatteryOrder.getUid());
                }
                
                //租电订单确认
                if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                    rentOrderConfirm(electricityCabinet, finalOpenDTO);
                }
                //退电订单确认
                if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                    returnOrderConfirm(electricityCabinet, finalOpenDTO);
                }
                
                if (rentBatteryOrder.getOrderSeq() > finalOpenDTO.getOrderSeq()) {
                    log.warn("RENT ORDER WARN! rsp order seq is lower order! requestId={},orderId={},uid={}", finalOpenDTO.getSessionId(), finalOpenDTO.getOrderId(),
                            rentBatteryOrder.getUid());
                    return;
                }
                
                //换电柜异常
                ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(rentBatteryOrder.getTenantId());
                if (Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsOpenDoorLock(), ElectricityConfig.OPEN_DOOR_LOCK)) {
                    lockExceptionDoor(null, rentBatteryOrder, finalOpenDTO);
                }

                // 保存租/退二次扫码的自主开仓时间限制
                redisService.set(CacheConstant.RENT_RETURN_ALLOW_SELF_OPEN_CELL_START_TIME_KEY + rentBatteryOrder.getOrderId(), String.valueOf(System.currentTimeMillis()), 5L,
                        TimeUnit.MINUTES);
                
                if (finalOpenDTO.getIsProcessFail()) {
                    RentBatteryOrder newRentBatteryOrder = new RentBatteryOrder();
                    //若柜机正在使用中
                    if (RentBatteryOrder.INIT_DEVICE_USING.equals(finalOpenDTO.getOrderStatus())) {
                        newRentBatteryOrder.setTenantId(Tenant.SUPER_ADMIN_TENANT_ID);
                    }
                    
                    // 只有租电池异常才需要回退
                    if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)){
                        userBatteryMemberCardService.handlePackageNumber(rentBatteryOrder.getUid());
                    }
                    
                    // 保存租退异常格挡
                    exceptionHandlerService.saveRentReturnExceptionCell(finalOpenDTO.getOrderStatus(), electricityCabinet.getId(), rentBatteryOrder.getCellNo(),
                            finalOpenDTO.getSessionId());
                    
                    //取消订单
                    if (finalOpenDTO.getIsNeedEndOrder()) {
                        newRentBatteryOrder.setId(rentBatteryOrder.getId());
                        newRentBatteryOrder.setUpdateTime(System.currentTimeMillis());
                        newRentBatteryOrder.setOrderSeq(RentBatteryOrder.STATUS_ORDER_CANCEL);
                        newRentBatteryOrder.setStatus(RentBatteryOrder.ORDER_CANCEL);
                        rentBatteryOrderService.update(newRentBatteryOrder);
                        return;
                    }
                    
                    //订单状态
                    newRentBatteryOrder.setId(rentBatteryOrder.getId());
                    newRentBatteryOrder.setUpdateTime(System.currentTimeMillis());
                    newRentBatteryOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
                    newRentBatteryOrder.setStatus(finalOpenDTO.getOrderStatus());
                    rentBatteryOrderService.update(newRentBatteryOrder);
                    return;
                }
                
                //订单状态
                rentBatteryOrder.setUpdateTime(System.currentTimeMillis());
                rentBatteryOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
                rentBatteryOrder.setStatus(finalOpenDTO.getOrderStatus());
                if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                    rentBatteryOrder.setElectricityBatterySn(finalOpenDTO.getBatterySn());
                }
                
                if (Objects.nonNull(finalOpenDTO.getCellNo())) {
                    rentBatteryOrder.setCellNo(finalOpenDTO.getCellNo());
                }
                rentBatteryOrderService.update(rentBatteryOrder);
                
                handleRentOrder(rentBatteryOrder, finalOpenDTO, electricityCabinet);
                
                // 处理电池标签
                handleBatteryLabel(finalOpenDTO, rentBatteryOrder);
            } catch (Exception e) {
                log.error("RENT BATTERY HANDLER ERROR!", e);
            } finally {
                redisService.delete(CacheConstant.RENT_BATTERY_ORDER_HANDLE_LIMIT + finalOpenDTO.getOrderId());
            }
        }
    }
    
    
    /**
     * 异常仓门加锁
     *
     * @param electricityCabinetOrder
     * @param rentBatteryOrder
     */
    private void lockExceptionDoor(ElectricityCabinetOrder electricityCabinetOrder, RentBatteryOrder rentBatteryOrder, EleOpenDTO eleOpenDTO) {
        //上报的订单状态值
        String orderStatus = eleOpenDTO.getOrderStatus();
        if (Objects.isNull(orderStatus)) {
            log.warn("ELE LOCK CELL orderStatus is null! orderId:{}", eleOpenDTO.getOrderId());
            return;
        }
        
        //仓门编号
        Integer cellNo = null;
        //电柜Id
        Integer electricityCabinetId = null;
        
        if (Objects.nonNull(electricityCabinetOrder) && Objects.isNull(rentBatteryOrder)) {
            //旧仓门异常
            if (Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_OPEN_FAIL) || Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)
                    || Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_BATTERY_CHECK_TIMEOUT)) {
                cellNo = electricityCabinetOrder.getOldCellNo();
                electricityCabinetId = electricityCabinetOrder.getElectricityCabinetId();
            } else if (Objects.equals(orderStatus, ElectricityCabinetOrder.COMPLETE_OPEN_FAIL) || Objects.equals(orderStatus,
                    ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_TIMEOUT)) {
                cellNo = electricityCabinetOrder.getNewCellNo();
                electricityCabinetId = electricityCabinetOrder.getElectricityCabinetId();
            }
        } else {
            //租退电仓门异常
            if (Objects.equals(orderStatus, RentBatteryOrder.RENT_OPEN_FAIL) || Objects.equals(orderStatus, RentBatteryOrder.RENT_BATTERY_TAKE_TIMEOUT) || Objects.equals(
                    orderStatus, RentBatteryOrder.RETURN_OPEN_FAIL) || Objects.equals(orderStatus, RentBatteryOrder.RETURN_BATTERY_CHECK_TIMEOUT)) {
                cellNo = rentBatteryOrder.getCellNo();
                electricityCabinetId = rentBatteryOrder.getElectricityCabinetId();
            }
        }
        
        if (Objects.isNull(cellNo) || Objects.isNull(electricityCabinetId)) {
            log.warn("ELE LOCK CELL cellNo or electricityCabinetId is null! orderId:{}", eleOpenDTO.getOrderId());
            return;
        }
        
        //对异常仓门进行锁仓处理
        // electricityCabinetBoxService.disableCell(cellNo, electricityCabinetId);
        
        //查询三元组信息
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        
        //发送锁仓命令
        //发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("cell_no", cellNo);
        dataMap.put("lockType", CabinetBoxConstant.LOCK_BY_SYSTEM);
        dataMap.put("isForbidden", true);
        dataMap.put("lockReason", CabinetBoxConstant.LOCK_REASON_EXCEPTION);
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(UUID.randomUUID().toString().replace("-", "")).data(dataMap)
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.ELE_COMMAND_CELL_UPDATE).build();
        
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        if (!sendResult.getLeft()) {
            log.error("ELE LOCK CELL ERROR! send command error! orderId:{}", eleOpenDTO.getOrderId());
        }
    }
    
    
    public void shutdown() {
        shutdown = true;
        executorService.shutdown();
    }
    
    public void putQueue(EleOpenDTO eleOpenDTO) {
        try {
            queue.put(eleOpenDTO);
        } catch (InterruptedException e) {
            log.error("ELECTRICITY CABINET OPERATE QUEUE ERROR!", e);
        }
    }
    
    //开旧门通知
    public void handelInitExchangeOrder(ElectricityCabinetOrder electricityCabinetOrder, EleOpenDTO finalOpenDTO, ElectricityConfig electricityConfig) {
        
        //开门失败
        if (finalOpenDTO.getIsProcessFail()) {
            //取消订单
            if (finalOpenDTO.getIsNeedEndOrder()) {
                ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
                newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
                newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
                newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.ORDER_CANCEL);
                newElectricityCabinetOrder.setOrderSeq(ElectricityCabinetOrder.STATUS_ORDER_CANCEL);
                newElectricityCabinetOrder.setOldElectricityBatterySn(finalOpenDTO.getBatterySn());
                electricityCabinetOrderService.update(newElectricityCabinetOrder);
                
                if (allowSelfOpenStatus(finalOpenDTO.getOrderStatus(), electricityConfig)) {
                    ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = new ElectricityExceptionOrderStatusRecord();
                    electricityExceptionOrderStatusRecord.setOrderId(electricityCabinetOrder.getOrderId());
                    electricityExceptionOrderStatusRecord.setTenantId(electricityCabinetOrder.getTenantId());
                    electricityExceptionOrderStatusRecord.setStatus(finalOpenDTO.getOrderStatus());
                    electricityExceptionOrderStatusRecord.setOrderSeq(finalOpenDTO.getOrderSeq());
                    electricityExceptionOrderStatusRecord.setCreateTime(System.currentTimeMillis());
                    electricityExceptionOrderStatusRecord.setUpdateTime(System.currentTimeMillis());
                    electricityExceptionOrderStatusRecord.setCellNo(electricityCabinetOrder.getOldCellNo());
                    electricityExceptionOrderStatusRecordService.insert(electricityExceptionOrderStatusRecord);
                }
                
                //清除柜机锁定缓存
                return;
            }
            
            //修改订单
            ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
            newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
            newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
            newElectricityCabinetOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
            newElectricityCabinetOrder.setStatus(finalOpenDTO.getOrderStatus());
            newElectricityCabinetOrder.setOldElectricityBatterySn(finalOpenDTO.getBatterySn());
            electricityCabinetOrderService.update(newElectricityCabinetOrder);
            return;
        }
        
        //修改订单状态
        ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
        newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
        newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        newElectricityCabinetOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
        newElectricityCabinetOrder.setStatus(finalOpenDTO.getOrderStatus());
        newElectricityCabinetOrder.setOldElectricityBatterySn(finalOpenDTO.getBatterySn());
        electricityCabinetOrderService.update(newElectricityCabinetOrder);
        
        //订单状态为旧电池检测成功则分配新仓门
        if (Objects.equals(newElectricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
            String cellNo = null;
            try {//查找用户
                UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCabinetOrder.getUid());
                if (Objects.isNull(userInfo)) {
                    log.warn("userInfo is null!orderId={}", electricityCabinetOrder.getOrderId());
                    return;
                }
                
                UserBattery userBattery = userBatteryService.selectByUidFromCache(userInfo.getUid());
                if (Objects.isNull(userBattery)) {
                    log.warn("ELE ERROR!not found userBattery,uid={},sessionId={}", userInfo.getUid(), finalOpenDTO.getSessionId());
                    return;
                }
                
                Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
                if (Objects.isNull(franchisee)) {
                    log.warn("ELE ERROR!not found franchisee,uid={},franchiseeId={},sessionId={}", userInfo.getUid(), userInfo.getFranchiseeId(), finalOpenDTO.getSessionId());
                    return;
                }
                
                //查看用户是否有绑定的电池,绑定电池和放入电池不一致则绑定电池处于游离态
                ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(electricityCabinetOrder.getUid());
                if (Objects.nonNull(electricityBattery) && !Objects.equals(electricityBattery.getSn(), newElectricityCabinetOrder.getOldElectricityBatterySn())) {
                    ElectricityBattery newElectricityBattery = new ElectricityBattery();
                    newElectricityBattery.setId(electricityBattery.getId());
                    newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_EXCEPTION);
                    newElectricityBattery.setUid(null);
                    newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                    newElectricityBattery.setElectricityCabinetId(null);
                    newElectricityBattery.setElectricityCabinetName(null);
                    newElectricityBattery.setBorrowExpireTime(null);
                    
                    // 删除redis中保存的租电订单或换电订单
                    OrderForBatteryUtil.delete(electricityBattery.getSn());
                    
                    electricityBatteryService.updateBatteryUser(newElectricityBattery);
                }
                
                //放入电池改为在仓
                ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySnFromDb(newElectricityCabinetOrder.getOldElectricityBatterySn());
                if (Objects.nonNull(oldElectricityBattery)) {
                    ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
                    if (Objects.nonNull(electricityCabinet)) {
                        ElectricityBattery newElectricityBattery = new ElectricityBattery();
                        newElectricityBattery.setId(oldElectricityBattery.getId());
                        newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_RETURN);
                        newElectricityBattery.setElectricityCabinetId(electricityCabinet.getId());
                        newElectricityBattery.setElectricityCabinetName(electricityCabinet.getName());
                        newElectricityBattery.setUid(null);
                        newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                        newElectricityBattery.setBorrowExpireTime(null);
                        
                        // 删除redis中保存的租电订单或换电订单
                        OrderForBatteryUtil.delete(oldElectricityBattery.getSn());
                        
                        electricityBatteryService.updateBatteryUser(newElectricityBattery);
                    }
                }
                
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
                if (Objects.isNull(electricityCabinet)) {
                    log.warn("handelInitExchangeOrder is error!not found electricityCabinet! electricityCabinetId:{}", electricityCabinetOrder.getElectricityCabinetId());
                    return;
                }
                
                //分配电池 --只分配满电电池
                Triple<Boolean, String, Object> tripleResult;
                if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                    tripleResult = rentBatteryOrderService.findUsableBatteryCellNo(electricityCabinet, electricityCabinetOrder.getOldCellNo().toString(), null,
                            userInfo.getFranchiseeId(), electricityCabinetOrder.getSource());
                } else {
                    tripleResult = rentBatteryOrderService.findUsableBatteryCellNo(electricityCabinet, electricityCabinetOrder.getOldCellNo().toString(), null,
                            userInfo.getFranchiseeId(), electricityCabinetOrder.getSource());
                }
                
                if (Objects.isNull(tripleResult)) {
                    log.warn("check Old Battery not find fully battery1!orderId:{}", electricityCabinetOrder.getOrderId());
                    return;
                }
                
                if (!tripleResult.getLeft()) {
                    log.warn("check Old Battery not find fully battery2!orderId:{}", electricityCabinetOrder.getOrderId());
                    return;
                }
                
                cellNo = tripleResult.getMiddle();
                
                if (Objects.isNull(cellNo)) {
                    log.warn("check Old Battery not find fully battery3!orderId:{}", electricityCabinetOrder.getOrderId());
                    return;
                }
                
                //根据换电柜id和仓门查出电池
                ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinetOrder.getElectricityCabinetId(), cellNo);
                if (Objects.isNull(electricityCabinetBox)) {
                    log.warn("check Old Battery not find electricityCabinetBox! electricityCabinetId:{},cellNo:{}", electricityCabinetOrder.getElectricityCabinetId(), cellNo);
                    return;
                }
                ElectricityBattery newElectricityBattery = electricityBatteryService.queryBySnFromDb(electricityCabinetBox.getSn());
                if (Objects.isNull(newElectricityBattery)) {
                    log.warn("check Old Battery not find electricityBattery! sn:{}", electricityCabinetBox.getSn());
                    return;
                }
                
                //修改订单状态
                ElectricityCabinetOrder innerElectricityCabinetOrder = new ElectricityCabinetOrder();
                innerElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
                innerElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
                innerElectricityCabinetOrder.setNewElectricityBatterySn(newElectricityBattery.getSn());
                innerElectricityCabinetOrder.setNewCellNo(Integer.valueOf(cellNo));
                electricityCabinetOrderService.update(innerElectricityCabinetOrder);
                
                //发送命令
                HashMap<String, Object> dataMap = Maps.newHashMap();
                dataMap.put("cell_no", cellNo);
                dataMap.put("order_id", electricityCabinetOrder.getOrderId());
                dataMap.put("serial_number", newElectricityCabinetOrder.getNewElectricityBatterySn());
                dataMap.put("status", electricityCabinetOrder.getStatus());
                dataMap.put("old_cell_no", electricityCabinetOrder.getOldCellNo());
                
                HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(
                                CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getUid() + "_"
                                        + electricityCabinetOrder.getOrderId()).data(dataMap).productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName())
                        .command(ElectricityIotConstant.ELE_COMMAND_ORDER_OPEN_NEW_DOOR).build();
                eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
            } catch (Exception e) {
                log.error("e", e);
            } finally {
                redisService.delete(CacheConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + electricityCabinetOrder.getElectricityCabinetId() + "_" + cellNo);
            }
            
        }
    }
    
    //开新门通知
    public void handelCompleteExchangeOrder(ElectricityCabinetOrder electricityCabinetOrder, EleOpenDTO finalOpenDTO) {
        //开门失败
        if (finalOpenDTO.getIsProcessFail()) {
            //取消订单
            if (finalOpenDTO.getIsNeedEndOrder()) {
                ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
                newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
                newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
                newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.ORDER_CANCEL);
                newElectricityCabinetOrder.setOrderSeq(ElectricityCabinetOrder.STATUS_ORDER_CANCEL);
                electricityCabinetOrderService.update(newElectricityCabinetOrder);
                
                return;
            }
            
            //修改订单
            ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
            newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
            newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
            newElectricityCabinetOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
            newElectricityCabinetOrder.setStatus(finalOpenDTO.getOrderStatus());
            electricityCabinetOrderService.update(newElectricityCabinetOrder);
            return;
        }
        
        //修改订单
        ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
        newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
        newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        newElectricityCabinetOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
        newElectricityCabinetOrder.setStatus(finalOpenDTO.getOrderStatus());
        electricityCabinetOrderService.update(newElectricityCabinetOrder);
        
        if (Objects.equals(newElectricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCabinetOrder.getUid());
            if (Objects.isNull(userInfo)) {
                return;
            }
            
            //查看用户是否有以前绑定的电池
            ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUid(electricityCabinetOrder.getUid());
            if (Objects.nonNull(oldElectricityBattery)) {
                if (Objects.equals(oldElectricityBattery.getSn(), electricityCabinetOrder.getNewElectricityBatterySn())) {
                    return;
                }
                ElectricityBattery newElectricityBattery = new ElectricityBattery();
                newElectricityBattery.setId(oldElectricityBattery.getId());
                newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_EXCEPTION);
                newElectricityBattery.setUid(null);
                newElectricityBattery.setElectricityCabinetId(null);
                newElectricityBattery.setElectricityCabinetName(null);
                newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                
                // 删除redis中保存的租电订单或换电订单
                OrderForBatteryUtil.delete(oldElectricityBattery.getSn());
                
                electricityBatteryService.updateBatteryUser(newElectricityBattery);
            }
            
            //电池改为在用
            ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(electricityCabinetOrder.getNewElectricityBatterySn());
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(electricityBattery.getId());
            newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_LEASE);
            newElectricityBattery.setElectricityCabinetId(null);
            newElectricityBattery.setElectricityCabinetName(null);
            newElectricityBattery.setUid(electricityCabinetOrder.getUid());
            newElectricityBattery.setExchangeCount(electricityBattery.getExchangeCount() + 1);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            newElectricityBattery.setBorrowExpireTime(Integer.parseInt(wechatTemplateNotificationConfig.getExpirationTime()) * 3600000 + System.currentTimeMillis());
            electricityBatteryService.updateBatteryUser(newElectricityBattery);
            
            //缓存分配出去的格挡
            if (StrUtil.isNotBlank(electricityCabinetOrder.getNewElectricityBatterySn())) {
                redisService.set(CacheConstant.CACHE_PRE_TAKE_CELL + electricityCabinetOrder.getElectricityCabinetId(), String.valueOf(electricityCabinetOrder.getNewCellNo()), 2L,
                        TimeUnit.DAYS);
            }
            
            // 保存电池被取走对应的订单，供后台租借状态电池展示
            OrderForBatteryUtil.save(electricityCabinetOrder.getOrderId(), OrderForBatteryConstants.TYPE_ELECTRICITY_CABINET_ORDER, electricityBattery.getSn());
        }
    }
    
    //开租/还 电池门
    public void handleRentOrder(RentBatteryOrder rentBatteryOrder, EleOpenDTO finalOpenDTO, ElectricityCabinet electricityCabinet) {

/*        //开门失败
        if (finalOpenDTO.getIsProcessFail()) {
            //取消订单
            if (finalOpenDTO.getIsNeedEndOrder()) {
                RentBatteryOrder newRentBatteryOrder = new RentBatteryOrder();
                newRentBatteryOrder.setId(rentBatteryOrder.getId());
                newRentBatteryOrder.setUpdateTime(System.currentTimeMillis());
                newRentBatteryOrder.setOrderSeq(RentBatteryOrder.STATUS_ORDER_CANCEL);
                newRentBatteryOrder.setStatus(RentBatteryOrder.ORDER_CANCEL);
                rentBatteryOrderService.update(newRentBatteryOrder);
                
                return;
            }
            
            //订单状态
            RentBatteryOrder newRentBatteryOrder = new RentBatteryOrder();
            newRentBatteryOrder.setId(rentBatteryOrder.getId());
            newRentBatteryOrder.setUpdateTime(System.currentTimeMillis());
            newRentBatteryOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
            newRentBatteryOrder.setStatus(finalOpenDTO.getOrderStatus());
            rentBatteryOrderService.update(newRentBatteryOrder);
            return;
        }
        
        //订单状态
        rentBatteryOrder.setUpdateTime(System.currentTimeMillis());
        rentBatteryOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
        rentBatteryOrder.setStatus(finalOpenDTO.getOrderStatus());
        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
            rentBatteryOrder.setElectricityBatterySn(finalOpenDTO.getBatterySn());
        }
    
        if (Objects.nonNull(finalOpenDTO.getCellNo())) {
            rentBatteryOrder.setCellNo(finalOpenDTO.getCellNo());
        }
        rentBatteryOrderService.update(rentBatteryOrder);*/
        
        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT) && Objects.equals(finalOpenDTO.getOrderStatus(),
                RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS)) {
            
            checkRentBatteryDoor(rentBatteryOrder);
            
            if (StrUtil.isNotBlank(rentBatteryOrder.getElectricityBatterySn())) {
                redisService.set(CacheConstant.CACHE_PRE_TAKE_CELL + rentBatteryOrder.getElectricityCabinetId(), String.valueOf(rentBatteryOrder.getCellNo()), 2L, TimeUnit.DAYS);
            }
            
            //处理用户套餐如果扣成0次，将套餐改为失效套餐，即过期时间改为当前时间
            handleExpireMemberCard(rentBatteryOrder);
            
            enterpriseRentRecordService.saveEnterpriseRentRecord(rentBatteryOrder.getUid());
            //记录企业用户租电池记录
            enterpriseUserCostRecordService.asyncSaveUserCostRecordForRentalAndReturnBattery(UserCostTypeEnum.COST_TYPE_RENT_BATTERY.getCode(), rentBatteryOrder);
            
            // 给第三方推送租电订单
            pushDataToThirdService.asyncPushRentOrder(finalOpenDTO.getSessionId(), rentBatteryOrder.getTenantId(), rentBatteryOrder.getOrderId(),
                    ThirdPartyOperatorTypeEnum.ORDER_TYPE_RENT.getType());
        }
        
        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN) && Objects.equals(finalOpenDTO.getOrderStatus(),
                RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS)) {

            // 提出去优化
            returnBatterySuccessHandlerService.checkReturnBatteryDoor(rentBatteryOrder);


            enterpriseRentRecordService.saveEnterpriseReturnRecord(rentBatteryOrder.getUid());
            //记录企业用户还电池记录
            enterpriseUserCostRecordService.asyncSaveUserCostRecordForRentalAndReturnBattery(UserCostTypeEnum.COST_TYPE_RETURN_BATTERY.getCode(), rentBatteryOrder);
        }


    }
    
    //检测租电池
    public void checkRentBatteryDoor(RentBatteryOrder rentBatteryOrder) {
        BatteryTrackRecord batteryTrackRecord = new BatteryTrackRecord().setSn(rentBatteryOrder.getElectricityBatterySn())
                .setEId(Long.valueOf(rentBatteryOrder.getElectricityCabinetId()))
                .setEName(Optional.ofNullable(electricityCabinetService.queryByIdFromCache(rentBatteryOrder.getElectricityCabinetId())).map(ElectricityCabinet::getName).orElse(""))
                .setENo(rentBatteryOrder.getCellNo()).setType(BatteryTrackRecord.TYPE_RENT_OUT)
                .setCreateTime(TimeUtils.convertToStandardFormatTime(rentBatteryOrder.getUpdateTime())).setOrderId(rentBatteryOrder.getOrderId());
        
        //查找用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(rentBatteryOrder.getUid());
        if (Objects.isNull(userInfo)) {
            batteryTrackRecordService.putBatteryTrackQueue(batteryTrackRecord);
            return;
        }
        batteryTrackRecord.setUid(rentBatteryOrder.getUid()).setPhone(userInfo.getPhone()).setName(userInfo.getName());
        batteryTrackRecordService.putBatteryTrackQueue(batteryTrackRecord);
        
        //更新用户租赁状态
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_YES);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(updateUserInfo);
        
        //查看用户是否有以前绑定的电池
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUid(rentBatteryOrder.getUid());
        
        //查看当前租借的电池
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(rentBatteryOrder.getElectricityBatterySn());
        
        // 添加换电电池互斥锁
        redisService.setNx(String.format(CacheConstant.EXCHANGE_PLACE_BATTERY_MUTUAL_LOCK, rentBatteryOrder.getElectricityBatterySn()), "1", 3 * 1000L, false);
        
        if (Objects.nonNull(oldElectricityBattery)) {
            if (Objects.equals(oldElectricityBattery.getSn(), rentBatteryOrder.getElectricityBatterySn())) {
                return;
            }
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(oldElectricityBattery.getId());
            newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_EXCEPTION);
            newElectricityBattery.setUid(null);
            newElectricityBattery.setElectricityCabinetId(null);
            newElectricityBattery.setElectricityCabinetName(null);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            
            // 删除redis中保存的租电订单或换电订单
            OrderForBatteryUtil.delete(oldElectricityBattery.getSn());
            
            electricityBatteryService.updateBatteryUser(newElectricityBattery);
        }
        
        //电池改为在用
        ElectricityBattery newElectricityBattery = new ElectricityBattery();
        newElectricityBattery.setId(electricityBattery.getId());
        newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_LEASE);
        newElectricityBattery.setElectricityCabinetId(null);
        newElectricityBattery.setElectricityCabinetName(null);
        newElectricityBattery.setUid(rentBatteryOrder.getUid());
        // newElectricityBattery.setBorrowExpireTime(System.currentTimeMillis() + Integer.parseInt(wechatTemplateNotificationConfig.getExpirationTime()) * 3600);
        newElectricityBattery.setUpdateTime(System.currentTimeMillis());
        newElectricityBattery.setBorrowExpireTime(Integer.parseInt(wechatTemplateNotificationConfig.getExpirationTime()) * 3600000 + System.currentTimeMillis());
        
        //设置电池的绑定时间
        Long bindTime = electricityBattery.getBindTime();
        if (Objects.isNull(bindTime) || bindTime < System.currentTimeMillis()) {
            newElectricityBattery.setBindTime(System.currentTimeMillis());
            electricityBatteryService.updateBatteryUser(newElectricityBattery);
            handleCallBatteryChangeSoc(electricityBattery);
            // 取走电池保存取走电池的soc
            operateBatterSocThreadPool.execute(() -> handlerUserTakeBatterySoc(userInfo, electricityBattery.getSn(), electricityBattery.getPower()));
        }
        
        // 保存电池被取走对应的订单，供后台租借状态电池展示
        OrderForBatteryUtil.save(rentBatteryOrder.getOrderId(), OrderForBatteryConstants.TYPE_RENT_BATTERY_ORDER, electricityBattery.getSn());
    }
    
    
    /**
     * 租电电池 记录soc
     */
    private void handlerUserTakeBatterySoc(UserInfo userInfo, String sn, Double takeAwayPower) {
        if (Objects.isNull(takeAwayPower)) {
            log.warn("EleOperateQueueHandler/handlerUserTakeBatterySoc is error,takeAwayPower is null");
            return;
        }
        
        try {
            ExchangeBatterySoc batterySoc = new ExchangeBatterySoc();
            batterySoc.setUid(userInfo.getUid());
            batterySoc.setSn(sn);
            batterySoc.setTenantId(userInfo.getTenantId());
            batterySoc.setFranchiseeId(userInfo.getFranchiseeId());
            batterySoc.setStoreId(userInfo.getStoreId());
            batterySoc.setTakeAwayPower(takeAwayPower);
            batterySoc.setReturnPower(0.00);
            batterySoc.setPoorPower(0.00);
            batterySoc.setDelFlag(0);
            batterySoc.setCreateTime(System.currentTimeMillis());
            exchangeBatterySocService.insertOne(batterySoc);
        } catch (Exception e) {
            log.error("EleOperateQueueHandler/handlerUserTakeBatterySoc/insert is exception,uid ={}, sn={}", userInfo.getUid(), sn, e);
        }
        
    }
    
    /**
     * 退电电池 记录soc
     */
    private void handlerUserRentBatterySoc(String returnSn, Double returnPower) {
        if (StrUtil.isBlank(returnSn)) {
            log.warn("EleOperateQueueHandler/handlerUserRentBatterySoc is error,returnSn is null");
            return;
        }
        if (Objects.isNull(returnPower)) {
            log.warn("EleOperateQueueHandler/handlerUserRentBatterySoc is error,returnPower is null, sn={}", returnSn);
            return;
        }
        
        ExchangeBatterySoc exchangeBatterySoc = exchangeBatterySocService.queryOneByUidAndSn(returnSn);
        if (Objects.isNull(exchangeBatterySoc)) {
            log.warn("EleOperateQueueHandler/handlerUserRentBatterySoc is error, rentBatterySoc should is null, sn={}", returnSn);
            return;
        }
        
        try {
            // 处理异常交换
            if (Objects.equals(exchangeBatterySoc.getReturnPower(), RETURN_POWER_DEFAULT) && Objects.isNull(exchangeBatterySoc.getUpdateTime())) {
                ExchangeBatterySoc batterySoc = new ExchangeBatterySoc();
                batterySoc.setId(exchangeBatterySoc.getId());
                batterySoc.setReturnPower(returnPower);
                batterySoc.setPoorPower(exchangeBatterySoc.getTakeAwayPower() - returnPower);
                batterySoc.setUpdateTime(System.currentTimeMillis());
                exchangeBatterySocService.update(batterySoc);
            }
        } catch (Exception e) {
            log.error("EleOperateQueueHandler/handlerUserRentBatterySoc is error, sn={}", returnSn, e);
        }
    }
    
    //检测还电池
    public void checkReturnBatteryDoor(RentBatteryOrder rentBatteryOrder) {
        
        BatteryTrackRecord batteryTrackRecord = new BatteryTrackRecord().setSn(rentBatteryOrder.getElectricityBatterySn())
                .setEId(Long.valueOf(rentBatteryOrder.getElectricityCabinetId()))
                .setEName(Optional.ofNullable(electricityCabinetService.queryByIdFromCache(rentBatteryOrder.getElectricityCabinetId())).map(ElectricityCabinet::getName).orElse(""))
                .setENo(rentBatteryOrder.getCellNo()).setType(BatteryTrackRecord.TYPE_RETURN_IN)
                .setCreateTime(TimeUtils.convertToStandardFormatTime(rentBatteryOrder.getUpdateTime())).setOrderId(rentBatteryOrder.getOrderId());
        batteryTrackRecordService.putBatteryTrackQueue(batteryTrackRecord);
        
        //查找用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(rentBatteryOrder.getUid());
        if (Objects.isNull(userInfo)) {
            return;
        }
        
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_NO);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(updateUserInfo);
        
        //查看用户是否有绑定的电池,绑定电池和放入电池不一致则绑定电池处于游离态
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(rentBatteryOrder.getUid());
        
        //放入电池改为在仓
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySnFromDb(rentBatteryOrder.getElectricityBatterySn());
        
        if (Objects.nonNull(electricityBattery)) {
            if (!Objects.equals(electricityBattery.getSn(), rentBatteryOrder.getElectricityBatterySn())) {
                ElectricityBattery newElectricityBattery = new ElectricityBattery();
                newElectricityBattery.setId(electricityBattery.getId());
                newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_EXCEPTION);
                newElectricityBattery.setUid(null);
                //如果放入的电池和用户绑定的电池不一样且放入的电池不为空
                if (Objects.nonNull(oldElectricityBattery) && Objects.nonNull(oldElectricityBattery.getUid()) && !Objects.equals(electricityBattery.getUid(),
                        oldElectricityBattery.getUid())) {
                    newElectricityBattery.setGuessUid(oldElectricityBattery.getUid());
                }
                newElectricityBattery.setElectricityCabinetId(null);
                newElectricityBattery.setElectricityCabinetName(null);
                newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                
                // 删除redis中保存的租电订单或换电订单
                OrderForBatteryUtil.delete(electricityBattery.getSn());
                
                electricityBatteryService.updateBatteryUser(newElectricityBattery);
            }
        }
        
        if (Objects.nonNull(oldElectricityBattery)) {
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(rentBatteryOrder.getElectricityCabinetId());
            if (Objects.nonNull(electricityCabinet)) {
                ElectricityBattery newElectricityBattery = new ElectricityBattery();
                newElectricityBattery.setId(oldElectricityBattery.getId());
                newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_RETURN);
                newElectricityBattery.setElectricityCabinetId(electricityCabinet.getId());
                newElectricityBattery.setElectricityCabinetName(electricityCabinet.getName());
                newElectricityBattery.setUid(null);
                newElectricityBattery.setGuessUid(null);
                newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                newElectricityBattery.setBorrowExpireTime(null);
                
                // 删除redis中保存的租电订单或换电订单
                OrderForBatteryUtil.delete(oldElectricityBattery.getSn());
                
                Long bindTime = oldElectricityBattery.getBindTime();
                log.info("return bindTime={},currentTime={}", bindTime, System.currentTimeMillis());
                //如果绑定时间为空或者电池绑定时间小于当前时间则更新电池信息
                if (Objects.isNull(bindTime) || bindTime < System.currentTimeMillis()) {
                    newElectricityBattery.setBindTime(System.currentTimeMillis());
                    electricityBatteryService.updateBatteryUser(newElectricityBattery);
                }
            }
            
        }
        // 归还电池
        operateBatterSocThreadPool.execute(() -> handlerUserRentBatterySoc(oldElectricityBattery.getSn(), oldElectricityBattery.getPower()));
        
    }
    
    private boolean allowSelfOpenStatus(String orderStatus, ElectricityConfig electricityConfig) {
        return orderStatus.equals(ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL) && Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsEnableSelfOpen(),
                ElectricityConfig.ENABLE_SELF_OPEN);
    }
    
    private void handleExpireMemberCard(RentBatteryOrder rentBatteryOrder) {
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(rentBatteryOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("EXCHANGE ORDER ERROR! userInfo is null!uid={},orderId={}", rentBatteryOrder.getUid(), rentBatteryOrder.getOrderId());
            return;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(rentBatteryOrder.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("EXCHANGE ORDER WARN! userBatteryMemberCard is null!uid={},orderId={}", rentBatteryOrder.getUid(), rentBatteryOrder.getOrderId());
            return;
        }
        
        //判断套餐是否限次
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.error("EXCHANGE ORDER ERROR! batteryMemberCard is null!uid={},orderId={}", rentBatteryOrder.getUid(), rentBatteryOrder.getOrderId());
            return;
        }
        
        if (!((Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0))) {
            return;
        }
        
        UserBatteryMemberCardPackage userBatteryMemberCardPackageLatest = userBatteryMemberCardPackageService.selectNearestByUid(userBatteryMemberCard.getUid());
        if (Objects.isNull(userBatteryMemberCardPackageLatest)) {
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
            userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        }
    }
    
    private void rentOrderConfirm(ElectricityCabinet electricityCabinet, EleOpenDTO finalOpenDTO) {
        if (!(Objects.equals(finalOpenDTO.getOrderStatus(), RentBatteryOrder.RENT_INIT_CHECK) || Objects.equals(finalOpenDTO.getOrderStatus(),
                RentBatteryOrder.RENT_BATTERY_NOT_EXISTS) || Objects.equals(finalOpenDTO.getOrderStatus(), RentBatteryOrder.RENT_OPEN_FAIL) || Objects.equals(
                finalOpenDTO.getOrderStatus(), RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS) || Objects.equals(finalOpenDTO.getOrderStatus(), RentBatteryOrder.INIT_DEVICE_USING))) {
            return;
        }
        
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("orderId", finalOpenDTO.getOrderId());
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + finalOpenDTO.getOrderId()).data(dataMap)
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.ELE_COMMAND_RENT_ORDER_RSP_ACK)
                .build();
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        if (Boolean.FALSE.equals(sendResult.getLeft())) {
            log.error("RENT ORDER HANDLER WARN! send ack command error,orderId={}", finalOpenDTO.getOrderId());
        }
    }
    
    private void returnOrderConfirm(ElectricityCabinet electricityCabinet, EleOpenDTO finalOpenDTO) {
        if (!(Objects.equals(finalOpenDTO.getOrderStatus(), RentBatteryOrder.RETURN_INIT_CHECK) || Objects.equals(finalOpenDTO.getOrderStatus(),
                RentBatteryOrder.RETURN_BATTERY_EXISTS) || Objects.equals(finalOpenDTO.getOrderStatus(), RentBatteryOrder.RETURN_OPEN_FAIL) || Objects.equals(
                finalOpenDTO.getOrderStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_TIMEOUT) || Objects.equals(finalOpenDTO.getOrderStatus(),
                RentBatteryOrder.RETURN_BATTERY_CHECK_FAIL) || Objects.equals(finalOpenDTO.getOrderStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS) || Objects.equals(
                finalOpenDTO.getOrderStatus(), RentBatteryOrder.INIT_DEVICE_USING))) {
            return;
        }
        
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("orderId", finalOpenDTO.getOrderId());
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + finalOpenDTO.getOrderId()).data(dataMap)
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.ELE_COMMAND_RETURN_ORDER_RSP_ACK)
                .build();
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        if (Boolean.FALSE.equals(sendResult.getLeft())) {
            log.error("RETURN ORDER HANDLER WARN! send ack command error,orderId={}", finalOpenDTO.getOrderId());
        }
    }
    
    private void handleCallBatteryChangeSoc(ElectricityBattery electricityBattery) {
        //调用改变电池电量
        callBatterySocThreadPool.execute(() -> {
            Tenant tenant = tenantService.queryByIdFromCache(electricityBattery.getTenantId());
            if (Objects.isNull(tenant)) {
                return;
            }
            
            Map<String, String> headers = new HashMap<>();
            String time = String.valueOf(System.currentTimeMillis());
            headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
            headers.put(CommonConstant.INNER_HEADER_TIME, time);
            headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
            headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());
            
            BatteryChangeSocQuery query = new BatteryChangeSocQuery();
            query.setSoc(electricityBattery.getPower().intValue());
            query.setSn(electricityBattery.getSn());
            
            R r = batteryPlatRetrofitService.changeBatterySoc(headers, query);
            if (Objects.isNull(r) || !r.isSuccess()) {
                log.error("call battery sn error! sn={},result={}", electricityBattery.getSn(), null == r ? "" : r.getErrMsg());
            }
        });
    }
    
    private void handleBatteryLabel(EleOpenDTO finalOpenDTO, RentBatteryOrder rentBatteryOrder) {
        if (!Objects.equals(finalOpenDTO.getOrderStatus(), RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS) && !Objects.equals(finalOpenDTO.getOrderStatus(),
                RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS)) {
            return;
        }
        String sn = Objects.nonNull(finalOpenDTO.getBatterySn()) ? finalOpenDTO.getBatterySn() : rentBatteryOrder.getElectricityBatterySn();
        if (Objects.isNull(sn)) {
            log.error("HANDLE BATTERY LABEL ERROR! can not find battery sn, rentOrderRsp={}", finalOpenDTO);
            return;
        }
        ElectricityBattery battery = ElectricityBattery.builder().tenantId(rentBatteryOrder.getTenantId()).sn(sn).build();
        
        // 处理租电成功的
        if (Objects.equals(finalOpenDTO.getOrderStatus(), RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS)) {
            ElectricityCabinetBox box = ElectricityCabinetBox.builder().electricityCabinetId(rentBatteryOrder.getElectricityCabinetId()).cellNo(rentBatteryOrder.getCellNo().toString()).build();
            electricityBatteryService.asyncModifyLabel(battery, box, new BatteryLabelModifyDTO(BatteryLabelEnum.RENT_NORMAL.getCode()), false);
        }
        
        // 处理退电成功的
        if (Objects.equals(finalOpenDTO.getOrderStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS)) {
            electricityBatteryService.asyncModifyLabel(battery, null, new BatteryLabelModifyDTO(BatteryLabelEnum.UNUSED.getCode()), false);
        }
    }
}
