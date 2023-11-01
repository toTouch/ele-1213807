package com.xiliulou.electricity.handler.iot.impl;

import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.electricity.service.retrofit.BatteryPlatRetrofitService;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.web.query.battery.BatteryChangeSocQuery;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service(value = ElectricityIotConstant.NORMAL_NEW_EXCHANGE_ORDER_HANDLER)
@Slf4j
public class NormalNewExchangeOrderHandlerIot extends AbstractElectricityIotHandler {
    
    @Resource
    private CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    ElectricityExceptionOrderStatusRecordService electricityExceptionOrderStatusRecordService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    WechatTemplateNotificationConfig wechatTemplateNotificationConfig;
    
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    
    @Autowired
    BatteryTrackRecordService batteryTrackRecordService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    BatteryPlatRetrofitService batteryPlatRetrofitService;
    
    @Autowired
    TenantService tenantService;
    
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private ElectricityBatteryDataService electricityBatteryDataService;
    
    XllThreadPoolExecutorService callBatterySocThreadPool = XllThreadPoolExecutors.newFixedThreadPool("CALL_BATTERY_SOC_CHANGE", 2, "callBatterySocChange");
    
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        ExchangeOrderRsp exchangeOrderRsp = JsonUtil.fromJson(receiverMessage.getOriginContent(), ExchangeOrderRsp.class);
        if (Objects.isNull(exchangeOrderRsp)) {
            log.error("EXCHANGE ORDER ERROR! originData is null! requestId={}", receiverMessage.getSessionId());
            return;
        }
        
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.queryByOrderId(exchangeOrderRsp.getOrderId());
        if (Objects.isNull(electricityCabinetOrder)) {
            log.warn("EXCHANGE ORDER WARN! order not found !requestId={},orderId={}", receiverMessage.getSessionId(), exchangeOrderRsp.getOrderId());
            return;
        }
        
        if (Objects.equals(exchangeOrderRsp.getOrderStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            //确认订单结束
            senOrderSuccessMsg(electricityCabinet, electricityCabinetOrder);
            log.info("EXCHANGE ORDER INFO! send order success msg! requestId={},orderId={},uid={}", receiverMessage.getSessionId(), exchangeOrderRsp.getOrderId(),
                    electricityCabinetOrder.getUid());
        }
        
        // 处理失败回退电池套餐次数
        handlePackageNumber(exchangeOrderRsp, receiverMessage, electricityCabinetOrder);
        
        if (electricityCabinetOrder.getOrderSeq() > exchangeOrderRsp.getOrderSeq()) {
            //确认订单结束
            log.error("EXCHANGE ORDER ERROR! rsp order seq is lower order! requestId={},orderId={},uid={}", receiverMessage.getSessionId(), exchangeOrderRsp.getOrderId(),
                    electricityCabinetOrder.getUid());
            return;
        }
        
        //是否开启异常仓门锁仓
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinetOrder.getTenantId());
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenDoorLock(), ElectricityConfig.OPEN_DOOR_LOCK) && exchangeOrderRsp.getIsException()) {
            lockExceptionDoor(electricityCabinetOrder, exchangeOrderRsp);
        }
        
        if (exchangeOrderRsp.getIsException()) {
            handleOrderException(electricityCabinetOrder, exchangeOrderRsp, electricityConfig);
            return;
        }
        
        ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
        newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
        newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        newElectricityCabinetOrder.setOrderSeq(exchangeOrderRsp.getOrderSeq());
        newElectricityCabinetOrder.setStatus(exchangeOrderRsp.getOrderStatus());
        newElectricityCabinetOrder.setOldElectricityBatterySn(exchangeOrderRsp.getPlaceBatteryName());
        newElectricityCabinetOrder.setNewElectricityBatterySn(exchangeOrderRsp.getTakeBatteryName());
        newElectricityCabinetOrder.setOldCellNo(exchangeOrderRsp.getPlaceCellNo());
        newElectricityCabinetOrder.setNewCellNo(exchangeOrderRsp.getTakeCellNo());
        if (exchangeOrderRsp.getOrderStatus().equals(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            newElectricityCabinetOrder.setSwitchEndTime(exchangeOrderRsp.getReportTime());
        }
        electricityCabinetOrderService.update(newElectricityCabinetOrder);
        
        //处理放入电池的相关信息
        handlePlaceBatteryInfo(exchangeOrderRsp, electricityCabinetOrder, electricityCabinet);
        
        //处理取走电池的相关信息
        handleTakeBatteryInfo(exchangeOrderRsp, electricityCabinetOrder, electricityCabinet);
        
        //处理用户套餐如果扣成0次，将套餐改为失效套餐，即过期时间改为当前时间
        handleExpireMemberCard(exchangeOrderRsp, electricityCabinetOrder);
    }
    
    /**
     * 异常失败，回退套餐次数
     *
     * @param exchangeOrderRsp
     * @param receiverMessage
     * @param electricityCabinetOrder
     */
    private void handlePackageNumber(ExchangeOrderRsp exchangeOrderRsp, ReceiverMessage receiverMessage, ElectricityCabinetOrder electricityCabinetOrder) {
        log.info("NormalNewExchangeOrderHandlerIot.postHandleReceiveMsg, handlePackageNumber, requestId is {}, orderId is {}, uid is {}, order status is {}",
                receiverMessage.getSessionId(), exchangeOrderRsp.getOrderId(), electricityCabinetOrder.getUid(), exchangeOrderRsp.getOrderStatus());
        // 定义异常状态，此处需要考虑后续抽出枚举或者常量池的方法
        List<String> warnStateList = new ArrayList<>();
        warnStateList.add(ElectricityCabinetOrder.ORDER_CANCEL);
        warnStateList.add(ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL);
        warnStateList.add(ElectricityCabinetOrder.COMPLETE_OPEN_FAIL);
        warnStateList.add(ElectricityCabinetOrder.COMPLETE_CHECK_BATTERY_NOT_EXISTS);
        warnStateList.add(ElectricityCabinetOrder.COMPLETE_CHECK_FAIL);
        warnStateList.add(ElectricityCabinetOrder.INIT_BATTERY_CHECK_TIMEOUT);
        warnStateList.add(ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL);
        warnStateList.add(ElectricityCabinetOrder.INIT_OPEN_FAIL);
        warnStateList.add(ElectricityCabinetOrder.INIT_CHECK_BATTERY_EXISTS);
        warnStateList.add(ElectricityCabinetOrder.INIT_CHECK_FAIL);
        warnStateList.add(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_TIMEOUT);
        warnStateList.add(ElectricityCabinetOrder.INIT_DEVICE_USING);
        
        if (warnStateList.contains(exchangeOrderRsp.getOrderStatus())) {
            // 通过订单的 UID 获取用户信息
            UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCabinetOrder.getUid());
            
            //回退单电套餐次数
            if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
                if (Objects.nonNull(userBatteryMemberCard)) {
                    BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                    if (Objects.nonNull(batteryMemberCard) && Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)) {
                        log.info("NormalNewExchangeOrderHandlerIot.postHandleReceiveMsg handlePackageNumber, refund user battery member card number.");
                        userBatteryMemberCardService.plusCount(userBatteryMemberCard.getUid());
                    }
                }
            }
            
            //回退车电一体套餐次数
            if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
                log.info("NormalNewExchangeOrderHandlerIot.postHandleReceiveMsg handlePackageNumber, refund user car_battery member number.");
                carRentalPackageMemberTermBizService.addResidue(userInfo.getTenantId(), userInfo.getUid());
            }
            
        }
    }
    
    private void senOrderSuccessMsg(ElectricityCabinet electricityCabinet, ElectricityCabinetOrder electricityCabinetOrder) {
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("orderId", electricityCabinetOrder.getOrderId());
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + electricityCabinetOrder.getOrderId()).data(dataMap)
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.EXCHANGE_ORDER_MANAGE_SUCCESS)
                .build();
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        if (!sendResult.getLeft()) {
            log.error("EXCHANGE ERROR! send orderSuccessAck command error! orderId:{}", electricityCabinetOrder.getOrderId());
        }
    }
    
    private void handleTakeBatteryInfo(ExchangeOrderRsp exchangeOrderRsp, ElectricityCabinetOrder electricityCabinetOrder, ElectricityCabinet electricityCabinet) {
        if (!exchangeOrderRsp.getOrderStatus().equals(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            return;
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCabinetOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("EXCHANGE ORDER ERROR! userInfo is null!uid={},requestId={},orderId={}", electricityCabinetOrder.getUid(), exchangeOrderRsp.getSessionId(),
                    exchangeOrderRsp.getOrderId());
            return;
        }
        
        //查看用户是否有以前绑定的电池
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUid(electricityCabinetOrder.getUid());
        
        //更新放入电池的状态
        ElectricityBattery placeBattery = electricityBatteryService.queryBySnFromDb(exchangeOrderRsp.getPlaceBatteryName());
        log.info("exchange:111111111111");
        if (Objects.nonNull(oldElectricityBattery)) {
            //如果放入的电池与用户绑定的电池不一致
            log.info("exchange:22222222222222222");
            if (!Objects.equals(oldElectricityBattery.getSn(), exchangeOrderRsp.getPlaceBatteryName())) {
                //更新用户绑定的电池状态
                ElectricityBattery newElectricityBattery = new ElectricityBattery();
                newElectricityBattery.setId(oldElectricityBattery.getId());
                newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_EXCEPTION);
                newElectricityBattery.setUid(null);
                newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                newElectricityBattery.setElectricityCabinetId(null);
                newElectricityBattery.setElectricityCabinetName(null);
                log.info("exchange:333333333333333333333");
                
                //如果放入的电池和用户绑定的电池不一样且放入的电池uid不为空
                if (Objects.nonNull(placeBattery) && !Objects.equals(oldElectricityBattery.getUid(), placeBattery.getUid()) && Objects.nonNull(placeBattery.getUid())) {
                    log.info("on placeBatteryUid={},oldElectricityBattery uid={}", placeBattery.getUid(), oldElectricityBattery.getUid());
                    newElectricityBattery.setGuessUid(placeBattery.getUid());
                }
                
                //如果放入的电池和用户绑定的电池不一样且放入的电池uid为空,guessUid不为空
                if (Objects.nonNull(placeBattery) && Objects.isNull(placeBattery.getUid()) && Objects.nonNull(placeBattery.getGuessUid())) {
                    log.info("on placeBatteryUid is null,oldElectricityBattery uid={},placeBatteryGuessUid={}", oldElectricityBattery.getUid(), placeBattery.getGuessUid());
                    newElectricityBattery.setGuessUid(placeBattery.getGuessUid());
                }
                
                //如果放入的电池和用户绑定的电池不一样且放入的电池uid为空,guessUid不为空
                if (Objects.nonNull(placeBattery) && Objects.isNull(placeBattery.getUid()) && Objects.isNull(placeBattery.getGuessUid())) {
                    log.info("on placeBatteryUid is null,placeBatteryGuessUid is null");
                    newElectricityBattery.setGuessUid(null);
                }
                
                electricityBatteryService.updateBatteryUser(newElectricityBattery);
                log.info("exchange:333333333333333333333");
                if (Objects.nonNull(placeBattery)) {
                    returnBattery(placeBattery, electricityCabinetOrder.getUid());
                }
                
            } else {
                returnBattery(oldElectricityBattery, electricityCabinetOrder.getUid());
            }
        } else {
            log.info("exchange:444444444444444444444");
            //异常交换如果放入的电池的uid为空，则需要清除guessId
            returnBattery(placeBattery, electricityCabinetOrder.getUid());
        }
        
        //电池改为在用
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(exchangeOrderRsp.getTakeBatteryName());
        if (Objects.nonNull(electricityBattery)) {
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(electricityBattery.getId());
            newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_LEASE);
            newElectricityBattery.setElectricityCabinetId(null);
            newElectricityBattery.setElectricityCabinetName(null);
            newElectricityBattery.setUid(electricityCabinetOrder.getUid());
            newElectricityBattery.setExchangeCount(electricityBattery.getExchangeCount() + 1);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            newElectricityBattery.setBorrowExpireTime(Long.parseLong(wechatTemplateNotificationConfig.getExpirationTime()) * 3600000 + System.currentTimeMillis());
            
            //设置电池的绑定时间
            Long bindTime = electricityBattery.getBindTime();
            log.info("on3 bindTime={},current time={}", bindTime, System.currentTimeMillis());
            if (Objects.isNull(bindTime) || bindTime < System.currentTimeMillis()) {
                newElectricityBattery.setBindTime(System.currentTimeMillis());
                electricityBatteryService.updateBatteryUser(newElectricityBattery);
            }
            
            //保存取走电池格挡
            redisService.set(CacheConstant.CACHE_PRE_TAKE_CELL + electricityCabinet.getId(), String.valueOf(electricityCabinetOrder.getNewCellNo()), 2L, TimeUnit.DAYS);
            
            handleCallBatteryChangeSoc(electricityBattery);
            
        } else {
            log.error("EXCHANGE ORDER ERROR! takeBattery is null!uid={},requestId={},orderId={}", userInfo.getUid(), exchangeOrderRsp.getSessionId(),
                    exchangeOrderRsp.getOrderId());
        }
        
        BatteryTrackRecord placeatteryTrackRecord = new BatteryTrackRecord().setSn(exchangeOrderRsp.getPlaceBatteryName()).setEId(Long.valueOf(electricityCabinet.getId()))
                .setEName(electricityCabinet.getName()).setENo(exchangeOrderRsp.getPlaceCellNo()).setType(BatteryTrackRecord.TYPE_EXCHANGE_IN)
                .setCreateTime(TimeUtils.convertToStandardFormatTime(exchangeOrderRsp.getReportTime())).setOrderId(exchangeOrderRsp.getOrderId());
        batteryTrackRecordService.putBatteryTrackQueue(placeatteryTrackRecord);
        
        BatteryTrackRecord takeBatteryTrackRecord = new BatteryTrackRecord().setSn(exchangeOrderRsp.getTakeBatteryName()).setEId(Long.valueOf(electricityCabinet.getId()))
                .setEName(electricityCabinet.getName()).setENo(exchangeOrderRsp.getTakeCellNo()).setType(BatteryTrackRecord.TYPE_EXCHANGE_OUT)
                .setCreateTime(TimeUtils.convertToStandardFormatTime(exchangeOrderRsp.getReportTime())).setOrderId(exchangeOrderRsp.getOrderId()).setUid(userInfo.getUid())
                .setName(userInfo.getName()).setPhone(userInfo.getPhone());
        batteryTrackRecordService.putBatteryTrackQueue(takeBatteryTrackRecord);
    }
    
    private void returnBattery(ElectricityBattery placeBattery, Long uid) {
        //通过guessUid获取电池信息; 如果有电池的guessUid为当前换电用户 ,则将此电池更新为放入电池的Uid
        List<ElectricityBattery> electricityBatteries = electricityBatteryService.listBatteryByGuessUid(uid);
        if (CollectionUtils.isNotEmpty(electricityBatteries) && !Objects.equals(placeBattery.getUid(), uid)) {
            List<Long> batteryIdList = electricityBatteries.stream().map(ElectricityBattery::getId).collect(Collectors.toList());
            if(Objects.nonNull(placeBattery.getUid())){
                electricityBatteryService.batchUpdateBatteryGuessUid(batteryIdList, placeBattery.getUid());
            }else {
                electricityBatteryService.batchUpdateBatteryGuessUid(batteryIdList, placeBattery.getGuessUid());
            }
        }
        
        ElectricityBattery newElectricityBattery = new ElectricityBattery();
        newElectricityBattery.setId(placeBattery.getId());
        newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_RETURN);
        newElectricityBattery.setUid(null);
        newElectricityBattery.setGuessUid(null);
        newElectricityBattery.setBorrowExpireTime(null);
        newElectricityBattery.setElectricityCabinetId(null);
        newElectricityBattery.setElectricityCabinetName(null);
        newElectricityBattery.setUpdateTime(System.currentTimeMillis());
        
        Long bindTime = placeBattery.getBindTime();
        //如果绑定时间为空或者电池绑定时间小于当前时间则更新电池信息
        log.info("bindTime={},current time={}", bindTime, System.currentTimeMillis());
        if (Objects.isNull(bindTime) || bindTime < System.currentTimeMillis()) {
            newElectricityBattery.setBindTime(System.currentTimeMillis());
            electricityBatteryService.updateBatteryUser(newElectricityBattery);
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
    
    private void handlePlaceBatteryInfo(ExchangeOrderRsp exchangeOrderRsp, ElectricityCabinetOrder electricityCabinetOrder, ElectricityCabinet electricityCabinet) {
        if (!exchangeOrderRsp.getOrderStatus().equals(ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
            return;
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCabinetOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("EXCHANGE ORDER ERROR! userInfo is null!uid={},requestId={},orderId={}", electricityCabinetOrder.getUid(), exchangeOrderRsp.getSessionId(),
                    exchangeOrderRsp.getOrderId());
            return;
        }

     /*   //查看用户是否有绑定的电池,绑定电池和放入电池不一致则绑定电池处于游离态
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(electricityCabinetOrder.getUid());
        if (Objects.nonNull(electricityBattery) && !Objects.equals(electricityBattery.getSn(),
                exchangeOrderRsp.getPlaceBatteryName())) {
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(electricityBattery.getId());
            newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_EXCEPTION);
            newElectricityBattery.setUid(null);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            newElectricityBattery.setElectricityCabinetId(null);
            newElectricityBattery.setElectricityCabinetName(null);
            electricityBatteryService.updateBatteryUser(newElectricityBattery);
        }

        //放入电池改为在仓
        //获取放入电池
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySnFromDb(
                exchangeOrderRsp.getPlaceBatteryName());
        if (Objects.nonNull(oldElectricityBattery)) {
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(oldElectricityBattery.getId());
            newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_RETURN);
            newElectricityBattery.setElectricityCabinetId(electricityCabinet.getId());
            newElectricityBattery.setElectricityCabinetName(electricityCabinet.getName());
            newElectricityBattery.setUid(null);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            newElectricityBattery.setBorrowExpireTime(null);
            electricityBatteryService.updateBatteryUser(newElectricityBattery);
        }*/
        
    }
    
    
    private void handleOrderException(ElectricityCabinetOrder electricityCabinetOrder, ExchangeOrderRsp exchangeOrderRsp, ElectricityConfig electricityConfig) {
        //取消订单
        ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
        newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
        newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.ORDER_CANCEL);
        newElectricityCabinetOrder.setOrderSeq(ElectricityCabinetOrder.STATUS_ORDER_CANCEL);
        electricityCabinetOrderService.update(newElectricityCabinetOrder);
        
        //判断是否满足可以自助开仓
        if (allowSelfOpenStatus(exchangeOrderRsp.orderStatus, electricityConfig)) {
            ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = new ElectricityExceptionOrderStatusRecord();
            electricityExceptionOrderStatusRecord.setOrderId(electricityCabinetOrder.getOrderId());
            electricityExceptionOrderStatusRecord.setTenantId(electricityCabinetOrder.getTenantId());
            electricityExceptionOrderStatusRecord.setStatus(exchangeOrderRsp.getOrderStatus());
            electricityExceptionOrderStatusRecord.setOrderSeq(exchangeOrderRsp.getOrderSeq());
            electricityExceptionOrderStatusRecord.setCreateTime(System.currentTimeMillis());
            electricityExceptionOrderStatusRecord.setUpdateTime(System.currentTimeMillis());
            electricityExceptionOrderStatusRecord.setCellNo(electricityCabinetOrder.getOldCellNo());
            electricityExceptionOrderStatusRecordService.insert(electricityExceptionOrderStatusRecord);
        }
        
        //错误信息保存到缓存里，方便前端显示
        redisService.set(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + exchangeOrderRsp.getOrderId(), exchangeOrderRsp.getMsg(), 5L, TimeUnit.MINUTES);
    }
    
    private void handleExpireMemberCard(ExchangeOrderRsp exchangeOrderRsp, ElectricityCabinetOrder electricityCabinetOrder) {
        if (!exchangeOrderRsp.getOrderStatus().equals(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            return;
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCabinetOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("EXCHANGE ORDER ERROR! userInfo is null!uid={},requestId={},orderId={}", electricityCabinetOrder.getUid(), exchangeOrderRsp.getSessionId(),
                    exchangeOrderRsp.getOrderId());
            return;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(electricityCabinetOrder.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("EXCHANGE ORDER WARN! userBatteryMemberCard is null!uid={},requestId={},orderId={}", electricityCabinetOrder.getUid(), exchangeOrderRsp.getSessionId(),
                    exchangeOrderRsp.getOrderId());
            return;
        }
        
        //判断套餐是否限次
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            return;
        }
        
        if (!((Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0))) {
            return;
        }
        
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis());
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
    }
    
    
    private boolean allowSelfOpenStatus(String orderStatus, ElectricityConfig electricityConfig) {
        return orderStatus.equals(ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL) && Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsEnableSelfOpen(),
                ElectricityConfig.ENABLE_SELF_OPEN);
    }
    
    // TODO: 2022/8/1 异常锁定格挡
    private void lockExceptionDoor(ElectricityCabinetOrder electricityCabinetOrder, ExchangeOrderRsp exchangeOrderRsp) {
        
        //上报的订单状态值
        String orderStatus = exchangeOrderRsp.getOrderStatus();
        if (Objects.isNull(orderStatus)) {
            log.error("ELE LOCK CELL orderStatus is null! orderId={}", exchangeOrderRsp.getOrderId());
            return;
        }
        
        //仓门编号
        Integer cellNo = null;
        //电柜Id
        Integer electricityCabinetId = null;
        
        //旧仓门异常
        if (Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_OPEN_FAIL) || Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_CHECK_FAIL) || Objects.equals(
                orderStatus, ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL) || Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_CHECK_BATTERY_EXISTS) || Objects.equals(
                orderStatus, ElectricityCabinetOrder.INIT_BATTERY_CHECK_TIMEOUT)) {
            cellNo = electricityCabinetOrder.getOldCellNo();
            electricityCabinetId = electricityCabinetOrder.getElectricityCabinetId();
        } else if (Objects.equals(orderStatus, ElectricityCabinetOrder.COMPLETE_OPEN_FAIL) || Objects.equals(orderStatus, ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_TIMEOUT)) {
            cellNo = electricityCabinetOrder.getNewCellNo();
            electricityCabinetId = electricityCabinetOrder.getElectricityCabinetId();
        }
        
        if (Objects.isNull(cellNo) || Objects.isNull(electricityCabinetId)) {
            log.warn("ELE LOCK CELL cellNo or electricityCabinetId is null! orderId:{}", exchangeOrderRsp.getOrderId());
            return;
        }
        
        //对异常仓门进行锁仓处理
        electricityCabinetBoxService.disableCell(cellNo, electricityCabinetId);
        
        //查询三元组信息
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        
        //发送锁仓命令
        //发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("cell_no", cellNo);
        dataMap.put("lockType", 1);
        dataMap.put("isForbidden", true);
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(UUID.randomUUID().toString().replace("-", "")).data(dataMap)
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.ELE_COMMAND_CELL_UPDATE).build();
        
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        if (!sendResult.getLeft()) {
            log.error("ELE LOCK CELL ERROR! send command error! orderId:{}", exchangeOrderRsp.getOrderId());
        }
    }
    
    
    @Data
    class ExchangeOrderRsp {
        
        private String sessionId;
        
        //订单id
        public String orderId;
        
        //正确或者错误信息，当isProcessFail使用该msg
        public String msg;
        
        /**
         * 订单状态： INIT_CHECK_FAIL(1.1), INIT_OPEN_FAIL(2.1), INIT_OPEN_SUCCESS(2.0), INIT_BATTERY_CHECK_FAIL(3.1) INIT_BATTERY_CHECK_SUCCESS(3.0) COMPLETE_OPEN_FAIL(5.1)
         * COMPLETE_OPEN_SUCCESS(5.0) COMPLETE_BATTERY_TAKE_TIMEOUT(6.1) COMPLETE_BATTERY_TAKE_SUCCESS(6.0)
         */
        public String orderStatus;
        
        //订单状态序号
        private Double orderSeq;
        
        //是否需要结束订单
        private Boolean isException;
        
        //创建时间
        private Long reportTime;
        
        private Integer placeCellNo;
        
        private String placeBatteryName;
        
        private String takeBatteryName;
        
        private Integer takeCellNo;
    }
}


