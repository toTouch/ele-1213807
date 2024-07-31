package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryTrackRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityExceptionOrderStatusRecord;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.queue.EleOperateQueueHandler;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryTrackRecordService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityExceptionOrderStatusRecordService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author: hrp
 * @Date: 2022/07/22 17:02
 * @Description:
 */
@Service(value = ElectricityIotConstant.NORMAL_ELE_ORDER_SELF_OPEN_CELL_HANDLER)
@Slf4j
public class NormalEleSelfOpenCellHandlerIot extends AbstractElectricityIotHandler {
    
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    EleOperateQueueHandler eleOperateQueueHandler;
    
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Autowired
    ElectricityExceptionOrderStatusRecordService electricityExceptionOrderStatusRecordService;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Resource
    private BatteryMemberCardService batteryMemberCardService;
    
    @Resource
    private CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;
    
    
    @Resource
    UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Resource
    private WechatTemplateNotificationConfig wechatTemplateNotificationConfig;
    
    @Resource
    private BatteryTrackRecordService batteryTrackRecordService;
    
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("SELF OPEN CELL EXCHANGE NO sessionId,{}", receiverMessage.getOriginContent());
            return;
        }
        
        EleSelfOPenCellOrderVo eleSelfOPenCellOrderVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleSelfOPenCellOrderVo.class);
        
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.queryByOrderId(eleSelfOPenCellOrderVo.getOrderId());
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("SELF OPEN CELL ERROR! not found order! orderId:{}", eleSelfOPenCellOrderVo.getOrderId());
            return;
        }
        
        // 开门电仓判断, 自主新仓开新仓门需要更新订单状态
        // 上报的仓门==订单新仓门 && 订单旧电池存在 & 新电池不存在 && 上报开门成功
        if (Objects.equals(electricityCabinetOrder.getNewCellNo(), eleSelfOPenCellOrderVo.getCellNo()) && Objects.nonNull(electricityCabinetOrder.getOldElectricityBatterySn())
                && Objects.isNull(electricityCabinetOrder.getNewElectricityBatterySn()) && eleSelfOPenCellOrderVo.getResult()) {
            log.info("SELF OPEN CELL INFO! Open Full Cell");
            
            if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
                log.warn("order status have succeeded, orderId is {}", electricityCabinetOrder.getOrderId());
                return;
            }
            
            // 订单完成。扣减套餐次数
            deductionPackageNumberHandler(electricityCabinetOrder);
            
            // 修改订单最终状态为成功
            ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
            newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
            newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
            newElectricityCabinetOrder.setOrderSeq(ElectricityCabinetOrder.STATUS_COMPLETE_OPEN_SUCCESS);
            newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS);
            newElectricityCabinetOrder.setNewElectricityBatterySn(eleSelfOPenCellOrderVo.getTakeBatteryName());
            newElectricityCabinetOrder.setSwitchEndTime(eleSelfOPenCellOrderVo.getReportTime());
            newElectricityCabinetOrder.setRemark("新仓门自助开仓");
            electricityCabinetOrderService.update(newElectricityCabinetOrder);
            
            // 处理取走电池的相关信息（解绑&绑定）
            takeBatteryHandler(eleSelfOPenCellOrderVo, electricityCabinetOrder, electricityCabinet);
            
            // 处理用户套餐如果扣成0次，将套餐改为失效套餐，即过期时间改为当前时间
            handleExpireMemberCard(eleSelfOPenCellOrderVo, electricityCabinetOrder);
            
        }
        
        // 新的自主开仓无法走到这里
        ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(
                eleSelfOPenCellOrderVo.getOrderId());
        if (Objects.isNull(electricityExceptionOrderStatusRecord)) {
            log.warn("SELF OPEN CELL ERROR! not found user! userId:{}", eleSelfOPenCellOrderVo.getOrderId());
            return;
        }
        
        //幂等加锁
        Boolean result = redisService.setNx(CacheConstant.SELF_OPEN_CALL_CACHE_KEY + eleSelfOPenCellOrderVo.getOrderId() + receiverMessage.getType(), "true", 10 * 1000L, true);
        if (!result) {
            log.error("OFFLINE EXCHANGE orderId is lock,{}", eleSelfOPenCellOrderVo.getOrderId());
            return;
        }
        
        //操作回调的放在redis中,记录开门结果
        if (Objects.nonNull(eleSelfOPenCellOrderVo.getResult()) && eleSelfOPenCellOrderVo.getResult()) {
            redisService.set(CacheConstant.ELE_OPERATOR_SELF_OPEN_CEE_CACHE_KEY + sessionId, "true", 30L, TimeUnit.SECONDS);
        } else {
            redisService.set(CacheConstant.ELE_OPERATOR_SELF_OPEN_CEE_CACHE_KEY + sessionId, "false", 30L, TimeUnit.SECONDS);
        }
        
        ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecordUpdate = new ElectricityExceptionOrderStatusRecord();
        electricityExceptionOrderStatusRecordUpdate.setId(electricityExceptionOrderStatusRecord.getId());
        electricityExceptionOrderStatusRecordUpdate.setUpdateTime(System.currentTimeMillis());
        if (Objects.equals(eleSelfOPenCellOrderVo.getStatus(), ElectricityExceptionOrderStatusRecord.STATUS_OPEN_FAIL) || Objects.equals(eleSelfOPenCellOrderVo.getStatus(),
                ElectricityExceptionOrderStatusRecord.BATTERY_NOT_MATCH)) {
            electricityExceptionOrderStatusRecordUpdate.setOpenCellStatus(ElectricityExceptionOrderStatusRecord.OPEN_CELL_FAIL);
        }
        electricityExceptionOrderStatusRecordService.update(electricityExceptionOrderStatusRecordUpdate);
    }
    
    
    private void deductionPackageNumberHandler(ElectricityCabinetOrder cabinetOrder) {
        
        // 通过订单的 UID 获取用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(cabinetOrder.getUid());
        
        // 扣减单电套餐次数
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            if (Objects.nonNull(userBatteryMemberCard)) {
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                if (Objects.nonNull(batteryMemberCard) && Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)) {
                    log.info("SELF OPEN CELL INFO! deductionPackageNumberHandler deduct battery member card,");
                    Integer row = userBatteryMemberCardService.minCount(userBatteryMemberCard);
                    if (row < 1) {
                        log.warn("SELF OPEN CELL  WARN! memberCard's count modify fail, uid={} ,mid={}", userBatteryMemberCard.getUid(), userBatteryMemberCard.getId());
                        throw new BizException("100213", "换电套餐剩余次数不足");
                    }
                }
            }
        }
        
        // 扣减车电一体套餐次数
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            log.info("SELF OPEN CELL INFO! postHandleReceiveMsg handlePackageNumber, refund user car_battery member number.");
            if (!carRentalPackageMemberTermBizService.substractResidue(userInfo.getTenantId(), userInfo.getUid())) {
                throw new BizException("100213", "车电一体套餐剩余次数不足");
            }
        }
        
    }
    
    
    private void handleExpireMemberCard(EleSelfOPenCellOrderVo oPenCellOrderVo, ElectricityCabinetOrder electricityCabinetOrder) {
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCabinetOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("SELF OPEN CELL ERROR! userInfo is null!uid={},requestId={},orderId={}", electricityCabinetOrder.getUid(), oPenCellOrderVo.getSessionId(),
                    oPenCellOrderVo.getOrderId());
            return;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(electricityCabinetOrder.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("SELF OPEN CELL  WARN! userBatteryMemberCard is null!uid={},requestId={},orderId={}", electricityCabinetOrder.getUid(), oPenCellOrderVo.getSessionId(),
                    oPenCellOrderVo.getOrderId());
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
        
        UserBatteryMemberCardPackage userBatteryMemberCardPackageLatest = userBatteryMemberCardPackageService.selectNearestByUid(userBatteryMemberCard.getUid());
        if (Objects.isNull(userBatteryMemberCardPackageLatest)) {
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
            userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        }
    }
    
    
    private void takeBatteryHandler(EleSelfOPenCellOrderVo eleSelfOPenCellOrderVo, ElectricityCabinetOrder cabinetOrder, ElectricityCabinet electricityCabinet) {
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(cabinetOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("SELF OPEN CELL  error! userInfo is null!uid={},sessionId is {} ,orderId={}", cabinetOrder.getUid(), eleSelfOPenCellOrderVo.getSessionId(),
                    eleSelfOPenCellOrderVo.getOrderId());
            return;
        }
        
        //  用户以前绑定的电池
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUid(cabinetOrder.getUid());
        
        // 电池解绑
        if (Objects.nonNull(oldElectricityBattery)) {
            log.info("SELF OPEN CELL info! userBindBatterSn:{}", oldElectricityBattery.getSn());
            
            ElectricityBattery updateBattery = new ElectricityBattery();
            updateBattery.setId(oldElectricityBattery.getId());
            updateBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_RETURN);
            updateBattery.setUid(null);
            updateBattery.setGuessUid(null);
            updateBattery.setBorrowExpireTime(null);
            updateBattery.setElectricityCabinetId(null);
            updateBattery.setElectricityCabinetName(null);
            updateBattery.setUpdateTime(System.currentTimeMillis());
            
            Long bindTime = oldElectricityBattery.getBindTime();
            //如果绑定时间为空 或者 电池绑定时间小于当前时间则更新电池信息
            log.info("SELF OPEN CELL info! bindTime={},current time={}", bindTime, System.currentTimeMillis());
            if (Objects.isNull(bindTime) || bindTime < System.currentTimeMillis()) {
                updateBattery.setBindTime(System.currentTimeMillis());
                electricityBatteryService.updateBatteryUser(updateBattery);
            }
            
        }
        
        // 取走的电池绑定用户
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(eleSelfOPenCellOrderVo.getTakeBatteryName());
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
            log.info("SELF OPEN CELL info! takeBattery.bindTime={},current time={}", bindTime, System.currentTimeMillis());
            if (Objects.isNull(bindTime) || bindTime < System.currentTimeMillis()) {
                newElectricityBattery.setBindTime(System.currentTimeMillis());
                electricityBatteryService.updateBatteryUser(newElectricityBattery);
                
            }
            
            //保存取走电池格挡
            redisService.set(CacheConstant.CACHE_PRE_TAKE_CELL + electricityCabinet.getId(), String.valueOf(cabinetOrder.getNewCellNo()), 2L, TimeUnit.DAYS);
            
            
        } else {
            log.error("SELF OPEN CELL error! takeBattery is null!uid={},requestId={},orderId={}", userInfo.getUid(), eleSelfOPenCellOrderVo.getSessionId(),
                    eleSelfOPenCellOrderVo.getOrderId());
        }
        
        BatteryTrackRecord placeatteryTrackRecord = new BatteryTrackRecord().setSn(cabinetOrder.getOldElectricityBatterySn()).setEId(Long.valueOf(electricityCabinet.getId()))
                .setEName(electricityCabinet.getName()).setENo(cabinetOrder.getOldCellNo()).setType(BatteryTrackRecord.TYPE_EXCHANGE_IN)
                .setCreateTime(TimeUtils.convertToStandardFormatTime(eleSelfOPenCellOrderVo.getReportTime())).setOrderId(eleSelfOPenCellOrderVo.getOrderId());
        batteryTrackRecordService.putBatteryTrackQueue(placeatteryTrackRecord);
        
        BatteryTrackRecord takeBatteryTrackRecord = new BatteryTrackRecord().setSn(eleSelfOPenCellOrderVo.getTakeBatteryName()).setEId(Long.valueOf(electricityCabinet.getId()))
                .setEName(electricityCabinet.getName()).setENo(cabinetOrder.getNewCellNo()).setType(BatteryTrackRecord.TYPE_EXCHANGE_OUT)
                .setCreateTime(TimeUtils.convertToStandardFormatTime(eleSelfOPenCellOrderVo.getReportTime())).setOrderId(eleSelfOPenCellOrderVo.getOrderId())
                .setUid(userInfo.getUid()).setName(userInfo.getName()).setPhone(userInfo.getPhone());
        batteryTrackRecordService.putBatteryTrackQueue(takeBatteryTrackRecord);
        
    }
    
    @Data
    class EleSelfOPenCellOrderVo {
        
        //订单Id
        private String orderId;
        
        //orderStatus
        private String status;
        
        //msg
        private String msg;
        
        private Integer cellNo;
        
        /**
         * 保留
         */
        private String batteryName;
        
        private Boolean result;
        
        /**
         * 自主开仓取走满电仓
         */
        private String takeBatteryName;
        
        /**
         * 创建时间
         */
        private Long reportTime;
        
        
        private String sessionId;
    }
}


