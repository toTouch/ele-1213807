package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.BatteryTrackRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ExchangeBatterySoc;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.BatteryTrackRecordService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ExchangeBatterySocService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.entity.ExchangeBatterySoc.RETURN_POWER_DEFAULT;

/**
 * @author: renhang
 * @Date: 2024/07/23
 * @Description: 打开满电仓, 取电流程
 */
@Service(value = ElectricityIotConstant.NORMAL_OPEN_FULL_CELL_HANDLER)
@Slf4j
public class NormalOpenFullyCellHandlerIot extends AbstractElectricityIotHandler {
    
    @Resource
    RedisService redisService;
    
    @Resource
    private ElectricityCabinetOrderService cabinetOrderService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private ElectricityBatteryService electricityBatteryService;
    
    @Resource
    private WechatTemplateNotificationConfig wechatTemplateNotificationConfig;
    
    @Resource
    private BatteryTrackRecordService batteryTrackRecordService;
    
    
    @Resource
    private ExchangeBatterySocService exchangeBatterySocService;
    
    XllThreadPoolExecutorService openFullBatteryExchangeBatterSocThreadPool = XllThreadPoolExecutors.newFixedThreadPool("OPEN_FULL_BATTERY_SOC_ANALYZE", 1,
            "open-full-battery-soc-pool-thread");
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("normalOpenFullyCellHandlerIot error! sessionId is null");
            return;
        }
        
        EleOpenFullCellRsp openFullCellRsp = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleOpenFullCellRsp.class);
        if (Objects.isNull(openFullCellRsp)) {
            log.error("normalOpenFullyCellHandlerIot error! openFullCellRsp is null");
            return;
        }
        
        if (!redisService.setNx(CacheConstant.OPEN_FULL_CELL_LIMIT + openFullCellRsp.getOrderId(), "1", 200L, false)) {
            log.info("normalOpenFullyCellHandlerIot.order is being processed,requestId={},orderId={}", receiverMessage.getSessionId(), openFullCellRsp.getOrderId());
            return;
        }
        
        ElectricityCabinetOrder cabinetOrder = cabinetOrderService.queryByOrderId(openFullCellRsp.getOrderId());
        if (Objects.isNull(cabinetOrder)) {
            log.error("normalOpenFullyCellHandlerIot error! order is null,sessionId is{}, orderId is {}", sessionId, openFullCellRsp.getOrderId());
        }
        
        if (cabinetOrder.getOrderSeq() > openFullCellRsp.getOrderSeq()) {
            log.warn("normalOpenFullyCellHandlerIot error! rsp order seq is lower order! requestId={},orderId={},uid={}", receiverMessage.getSessionId(),
                    openFullCellRsp.getOrderId(), cabinetOrder.getUid());
            return;
        }
        
        if (openFullCellRsp.isException) {
            log.warn("normalOpenFullyCellHandlerIot error! openFullCellRsp exception,requestId={}", receiverMessage.getSessionId());
            return;
        }
        
        // 修改订单最终状态为成功
        ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
        newElectricityCabinetOrder.setId(cabinetOrder.getId());
        newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        newElectricityCabinetOrder.setOrderSeq(openFullCellRsp.getOrderSeq());
        newElectricityCabinetOrder.setStatus(openFullCellRsp.getOrderStatus());
        newElectricityCabinetOrder.setNewElectricityBatterySn(openFullCellRsp.getTakeBatteryName());
        newElectricityCabinetOrder.setNewCellNo(openFullCellRsp.getTakeCellNo());
        if (openFullCellRsp.getOrderStatus().equals(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            newElectricityCabinetOrder.setSwitchEndTime(openFullCellRsp.getReportTime());
        }
        cabinetOrderService.update(newElectricityCabinetOrder);
        
        //处理取走电池的相关信息
        takeBatteryHandler(openFullCellRsp, cabinetOrder, electricityCabinet);
    }
    
    private void takeBatteryHandler(EleOpenFullCellRsp openFullCellRsp, ElectricityCabinetOrder cabinetOrder, ElectricityCabinet electricityCabinet) {
        if (!Objects.equals(openFullCellRsp.getOrderStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            log.warn("normalOpenFullyCellHandlerIot error! takeBatteryHandler.orderStatus not is success,orderStatus is {}", openFullCellRsp.getOrderStatus());
            return;
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(cabinetOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("normalOpenFullyCellHandlerIot error! userInfo is null!uid={},requestId={},orderId={}", cabinetOrder.getUid(), openFullCellRsp.getSessionId(),
                    openFullCellRsp.getOrderId());
            return;
        }
        
        //  用户以前绑定的电池
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUid(cabinetOrder.getUid());
        
        // 归还电池
        ElectricityBattery placeBattery = electricityBatteryService.queryBySnFromDb(openFullCellRsp.getPlaceBatteryName());
        
        // 电池解绑
        if (Objects.nonNull(oldElectricityBattery)) {
            log.info("normalOpenFullyCellHandlerIot info! userBindBatterSn:{}, returnBatterSn:{}", oldElectricityBattery.getSn(), openFullCellRsp.getPlaceBatteryName());
            
            // 如果放入的电池与用户绑定的电池不一致,异常交换
            if (!Objects.equals(oldElectricityBattery.getSn(), openFullCellRsp.getPlaceBatteryName())) {
                // 解绑用户 原来绑定的电池
                unbindUserOriginalBattery(oldElectricityBattery, placeBattery);
                
                if (Objects.nonNull(placeBattery)) {
                    // 解绑归还的电池
                    returnBattery(placeBattery, cabinetOrder.getUid());
                }
                
            } else {
                // 没有异常交换, 解绑用户原来的电池
                returnBattery(oldElectricityBattery, cabinetOrder.getUid());
                
            }
            
        } else {
            // 异常交换如果放入的电池的uid为空，则需要清除guessId
            returnBattery(placeBattery, cabinetOrder.getUid());
        }
        
        // 解绑电池，保存归还电池soc，兼容异常交换
        openFullBatteryExchangeBatterSocThreadPool.execute(() -> handlerUserRentBatterySoc(openFullCellRsp.getPlaceBatteryName(), openFullCellRsp.getPlaceBatterySoc()));
        
        // 取走的电池绑定用户
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(openFullCellRsp.getTakeBatteryName());
        if (Objects.nonNull(electricityBattery)) {
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(electricityBattery.getId());
            newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_LEASE);
            newElectricityBattery.setElectricityCabinetId(null);
            newElectricityBattery.setElectricityCabinetName(null);
            newElectricityBattery.setUid(cabinetOrder.getUid());
            newElectricityBattery.setExchangeCount(electricityBattery.getExchangeCount() + 1);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            newElectricityBattery.setBorrowExpireTime(Long.parseLong(wechatTemplateNotificationConfig.getExpirationTime()) * 3600000 + System.currentTimeMillis());
            
            //设置电池的绑定时间
            Long bindTime = electricityBattery.getBindTime();
            log.info("normalOpenFullyCellHandlerIot info! takeBattery.bindTime={},current time={}", bindTime, System.currentTimeMillis());
            if (Objects.isNull(bindTime) || bindTime < System.currentTimeMillis()) {
                newElectricityBattery.setBindTime(System.currentTimeMillis());
                electricityBatteryService.updateBatteryUser(newElectricityBattery);
                // 保存取走电池，记录电池soc
                openFullBatteryExchangeBatterSocThreadPool.execute(
                        () -> handlerUserTakeBatterySoc(cabinetOrder.getUid(), openFullCellRsp.getTakeBatteryName(), openFullCellRsp.getTakeBatterySoc()));
                
            }
            
            //保存取走电池格挡
            redisService.set(CacheConstant.CACHE_PRE_TAKE_CELL + electricityCabinet.getId(), String.valueOf(cabinetOrder.getNewCellNo()), 2L, TimeUnit.DAYS);
            
            
        } else {
            log.error("normalOpenFullyCellHandlerIot error! takeBattery is null!uid={},requestId={},orderId={}", userInfo.getUid(), openFullCellRsp.getSessionId(),
                    openFullCellRsp.getOrderId());
        }
        
        BatteryTrackRecord placeatteryTrackRecord = new BatteryTrackRecord().setSn(openFullCellRsp.getPlaceBatteryName()).setEId(Long.valueOf(electricityCabinet.getId()))
                .setEName(electricityCabinet.getName()).setENo(openFullCellRsp.getPlaceCellNo()).setType(BatteryTrackRecord.TYPE_EXCHANGE_IN)
                .setCreateTime(TimeUtils.convertToStandardFormatTime(openFullCellRsp.getReportTime())).setOrderId(openFullCellRsp.getOrderId());
        batteryTrackRecordService.putBatteryTrackQueue(placeatteryTrackRecord);
        
        BatteryTrackRecord takeBatteryTrackRecord = new BatteryTrackRecord().setSn(openFullCellRsp.getTakeBatteryName()).setEId(Long.valueOf(electricityCabinet.getId()))
                .setEName(electricityCabinet.getName()).setENo(openFullCellRsp.getTakeCellNo()).setType(BatteryTrackRecord.TYPE_EXCHANGE_OUT)
                .setCreateTime(TimeUtils.convertToStandardFormatTime(openFullCellRsp.getReportTime())).setOrderId(openFullCellRsp.getOrderId()).setUid(userInfo.getUid())
                .setName(userInfo.getName()).setPhone(userInfo.getPhone());
        batteryTrackRecordService.putBatteryTrackQueue(takeBatteryTrackRecord);
        
    }
    
    private void unbindUserOriginalBattery(ElectricityBattery oldElectricityBattery, ElectricityBattery placeBattery) {
        ElectricityBattery newElectricityBattery = new ElectricityBattery();
        newElectricityBattery.setId(oldElectricityBattery.getId());
        newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_EXCEPTION);
        newElectricityBattery.setUid(null);
        newElectricityBattery.setUpdateTime(System.currentTimeMillis());
        newElectricityBattery.setElectricityCabinetId(null);
        newElectricityBattery.setElectricityCabinetName(null);
        
        //如果放入的电池和用户绑定的电池不一样且放入的电池uid不为空
        if (Objects.nonNull(placeBattery) && !Objects.equals(oldElectricityBattery.getUid(), placeBattery.getUid()) && Objects.nonNull(placeBattery.getUid())) {
            log.info("normalOpenFullyCellHandlerIot，on placeBatteryUid={},oldElectricityBattery uid={}", placeBattery.getUid(), oldElectricityBattery.getUid());
            newElectricityBattery.setGuessUid(placeBattery.getUid());
        }
        
        //如果放入的电池和用户绑定的电池不一样且放入的电池uid为空,guessUid不为空
        if (Objects.nonNull(placeBattery) && Objects.isNull(placeBattery.getUid()) && Objects.nonNull(placeBattery.getGuessUid())) {
            log.info("normalOpenFullyCellHandlerIot， on placeBatteryUid is null,oldElectricityBattery uid={},placeBatteryGuessUid={}", oldElectricityBattery.getUid(),
                    placeBattery.getGuessUid());
            newElectricityBattery.setGuessUid(placeBattery.getGuessUid());
        }
        
        //如果放入的电池和用户绑定的电池不一样且放入的电池uid为空,guessUid不为空
        if (Objects.nonNull(placeBattery) && Objects.isNull(placeBattery.getUid()) && Objects.isNull(placeBattery.getGuessUid())) {
            log.info("normalOpenFullyCellHandlerIot，on placeBatteryUid is null,placeBatteryGuessUid is null");
            newElectricityBattery.setGuessUid(null);
        }
        
        electricityBatteryService.updateBatteryUser(newElectricityBattery);
    }
    
    private void returnBattery(ElectricityBattery placeBattery, Long uid) {
        //通过guessUid获取电池信息; 如果有电池的guessUid为当前换电用户 ,则将此电池更新为放入电池的Uid
        List<ElectricityBattery> electricityBatteries = electricityBatteryService.listBatteryByGuessUid(uid);
        if (CollectionUtils.isNotEmpty(electricityBatteries) && !Objects.equals(placeBattery.getUid(), uid)) {
            List<Long> batteryIdList = electricityBatteries.stream().map(ElectricityBattery::getId).collect(Collectors.toList());
            if (Objects.nonNull(placeBattery.getUid())) {
                electricityBatteryService.batchUpdateBatteryGuessUid(batteryIdList, placeBattery.getUid());
            } else {
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
    
    
    /**
     * 换电取走电池 记录soc
     */
    private void handlerUserTakeBatterySoc(Long uid, String takeBatterySn, Double takeAwayPower) {
        if (Objects.isNull(takeAwayPower)) {
            log.warn("normalOpenFullyCellHandlerIot.handlerUserTakeBatterySoc is error,takeAwayPower is null, sn={}", takeBatterySn);
            return;
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("normalOpenFullyCellHandlerIot.handlerUserTakeBatterySoc is error,userInfo is null, uid={},sn={}", uid, takeBatterySn);
            return;
        }
        
        try {
            ExchangeBatterySoc batterySoc = new ExchangeBatterySoc();
            batterySoc.setUid(userInfo.getUid());
            batterySoc.setSn(takeBatterySn);
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
            log.error("NormalNewExchangeOrderHandlerIot/handlerUserTakeBatterySoc/insert is exception, uid={},sn={}", userInfo.getUid(), takeBatterySn, e);
        }
        
    }
    
    /**
     * 换电归还电池 记录soc
     */
    private void handlerUserRentBatterySoc(String returnSn, Double returnPower) {
        if (StrUtil.isBlank(returnSn)) {
            log.warn("NormalNewExchangeOrderHandlerIot/handlerUserRentBatterySoc is error,returnSn is null");
            return;
        }
        if (Objects.isNull(returnPower)) {
            log.warn("NormalNewExchangeOrderHandlerIot/handlerUserRentBatterySoc is error,returnPower is null, returnSn={}", returnSn);
            return;
        }
        
        //  上报的sn绑定的用户+Sn;兼容异常交换场景
        ExchangeBatterySoc exchangeBatterySoc = exchangeBatterySocService.queryOneByUidAndSn(returnSn);
        if (Objects.isNull(exchangeBatterySoc)) {
            log.warn("NormalNewExchangeOrderHandlerIot/handlerUserRentBatterySoc is error, rentBatterySoc is  null,sn={}", returnSn);
            return;
        }
        try {
            if (Objects.equals(exchangeBatterySoc.getReturnPower(), RETURN_POWER_DEFAULT) && Objects.isNull(exchangeBatterySoc.getUpdateTime())) {
                ExchangeBatterySoc batterySoc = new ExchangeBatterySoc();
                batterySoc.setId(exchangeBatterySoc.getId());
                batterySoc.setReturnPower(returnPower);
                batterySoc.setPoorPower(exchangeBatterySoc.getTakeAwayPower() - returnPower);
                batterySoc.setUpdateTime(System.currentTimeMillis());
                exchangeBatterySocService.update(batterySoc);
            }
        } catch (Exception e) {
            log.error("NormalNewExchangeOrderHandlerIot/handlerUserRentBatterySoc update is exception,sn={}", returnPower, e);
        }
        
    }
    
    
    @Data
    class EleOpenFullCellRsp {
        
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
        
        /**
         * 归还的电池
         */
        private String placeBatteryName;
        
        /**
         * 归还的仓门号
         */
        private Integer placeCellNo;
        
        
        /**
         * 取走电池
         */
        private String takeBatteryName;
        
        /**
         * 取走仓门号
         */
        private Integer takeCellNo;
        
        
        /**
         * 归还soc
         */
        private Double placeBatterySoc;
        
        /**
         * 取走soc
         */
        private Double takeBatterySoc;
    }
    
}




