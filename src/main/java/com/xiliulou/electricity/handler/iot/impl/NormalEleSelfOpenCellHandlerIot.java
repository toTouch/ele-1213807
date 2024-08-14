package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.BatteryTrackRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityExceptionOrderStatusRecord;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.BatteryTrackRecordService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityExceptionOrderStatusRecordService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
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

/**
 * @author: hrp
 * @Date: 2022/07/22 17:02
 * @Description:
 */
@Service(value = ElectricityIotConstant.NORMAL_ELE_ORDER_SELF_OPEN_CELL_HANDLER)
@Slf4j
public class NormalEleSelfOpenCellHandlerIot extends AbstractElectricityIotHandler {
    
    
    @Resource
    RedisService redisService;
    
    
    @Resource
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Resource
    ElectricityExceptionOrderStatusRecordService electricityExceptionOrderStatusRecordService;
    
    @Resource
    ElectricityBatteryService electricityBatteryService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
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
        
        if (Objects.isNull(eleSelfOPenCellOrderVo)) {
            log.error("SELF OPEN CELL ERROR! eleSelfOPenCellOrderVo is null , sessionId {}", sessionId);
            return;
        }
        //幂等加锁
        Boolean result = redisService.setNx(CacheConstant.SELF_OPEN_CALL_CACHE_KEY + eleSelfOPenCellOrderVo.getOrderId(), "true", 10 * 1000L, true);
        if (!result) {
            log.error("SELF OPEN CELL ERROR orderId is lock, sessionId is {}, orderId is {}", sessionId, eleSelfOPenCellOrderVo.getOrderId());
            return;
        }
        
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.queryByOrderId(eleSelfOPenCellOrderVo.getOrderId());
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("SELF OPEN CELL ERROR! not found order! sessionId is {}, orderId:{}", sessionId, eleSelfOPenCellOrderVo.getOrderId());
            return;
        }
        
        // 自主开仓后修改订单阶段状态
        ElectricityCabinetOrder updateCabinetOrder = new ElectricityCabinetOrder();
        updateCabinetOrder.setId(electricityCabinetOrder.getId());
        updateCabinetOrder.setUpdateTime(System.currentTimeMillis());
        if (eleSelfOPenCellOrderVo.result){
            updateCabinetOrder.setOrderStatus(ElectricityCabinetOrder.SELF_OPEN_CELL_SUCCESS);
        }else {
            updateCabinetOrder.setOrderStatus(ElectricityCabinetOrder.SELF_OPEN_CELL_FAIL);
        }
        electricityCabinetOrderService.update(updateCabinetOrder);
        
        
        // 开门电仓判断, 自主新仓开新仓门需要更新订单状态
        // 上报的仓门==订单新仓门 && 订单旧电池存在 & 新电池不存在 && 上报开门成功
        if (Objects.equals(electricityCabinetOrder.getNewCellNo(), eleSelfOPenCellOrderVo.getCellNo()) && Objects.nonNull(electricityCabinetOrder.getOldElectricityBatterySn())
                && Objects.isNull(electricityCabinetOrder.getNewElectricityBatterySn()) && eleSelfOPenCellOrderVo.getResult()) {
            
            log.info("SELF OPEN CELL INFO! Open Full Cell， sessionId is {}", sessionId);
            
            // 幂等
            if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
                log.warn("order status have succeeded,sessionId is {}, orderId is {}", sessionId, electricityCabinetOrder.getOrderId());
                return;
            }
            
            // 订单完成。扣减套餐次数
            userBatteryMemberCardService.deductionPackageNumberHandler(electricityCabinetOrder, sessionId);
            
            
            // 修改订单最终状态为成功
            ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
            newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
            newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
            newElectricityCabinetOrder.setOrderSeq(ElectricityCabinetOrder.STATUS_COMPLETE_OPEN_SUCCESS);
            newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS);
            newElectricityCabinetOrder.setOrderStatus(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS);
            newElectricityCabinetOrder.setNewElectricityBatterySn(eleSelfOPenCellOrderVo.getTakeBatteryName());
            newElectricityCabinetOrder.setSwitchEndTime(eleSelfOPenCellOrderVo.getReportTime());
            newElectricityCabinetOrder.setRemark("新仓门自助开仓");
            electricityCabinetOrderService.update(newElectricityCabinetOrder);
            
            // 处理取走电池的相关信息（解绑(包括异常交换)&绑定）
            takeBatteryHandler(eleSelfOPenCellOrderVo, electricityCabinetOrder, electricityCabinet);
            
            // 处理用户套餐如果扣成0次，将套餐改为失效套餐，即过期时间改为当前时间
            userBatteryMemberCardService.handleExpireMemberCard(eleSelfOPenCellOrderVo.getSessionId(),electricityCabinetOrder);
            
            return;
        }
        
        // 注意成功率优化2期，添加后台自主开仓和前端自主开仓(不使用这个表存储),这里会查询为空返回。 新的自主开新仓 无法走到这里,这里保留作为旧的自主开仓
        ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(
                eleSelfOPenCellOrderVo.getOrderId());
        if (Objects.isNull(electricityExceptionOrderStatusRecord)) {
            log.warn("SELF OPEN CELL WARN! not found user! sessionId is {}, userId:{}", sessionId, eleSelfOPenCellOrderVo.getOrderId());
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
    
    
    
    
    private void takeBatteryHandler(EleSelfOPenCellOrderVo eleSelfOPenCellOrderVo, ElectricityCabinetOrder cabinetOrder, ElectricityCabinet electricityCabinet) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(cabinetOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("SELF OPEN CELL  error! userInfo is null!uid={},sessionId is {} ,orderId={}", cabinetOrder.getUid(), eleSelfOPenCellOrderVo.getSessionId(),
                    eleSelfOPenCellOrderVo.getOrderId());
            return;
        }
        
        // 用户以前绑定的电池
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUid(cabinetOrder.getUid());
        
        // 订单中的归还电池
        ElectricityBattery placeBattery = electricityBatteryService.queryBySnFromDb(cabinetOrder.getOldElectricityBatterySn());
        
        // 电池解绑
        if (Objects.nonNull(oldElectricityBattery)) {
            log.info("SELF OPEN CELL  info! sessionId is {}, userBindBatterSn:{}, returnBatterSn:{}", eleSelfOPenCellOrderVo.getSessionId(), oldElectricityBattery.getSn(),
                    cabinetOrder.getOldElectricityBatterySn());
            // 如果放入的电池与用户绑定的电池不一致,异常交换
            if (!Objects.equals(oldElectricityBattery.getSn(), cabinetOrder.getOldElectricityBatterySn())) {
                // 解绑用户 原来绑定的电池
                unbindUserOriginalBattery(oldElectricityBattery, placeBattery);
                
                if (Objects.nonNull(placeBattery)) {
                    // 解绑归还的电池
                    returnBattery(placeBattery, cabinetOrder.getUid());
                }
                
            } else {
                // 正常换电：没有异常交换, 解绑用户原来的电池
                returnBattery(oldElectricityBattery, cabinetOrder.getUid());
            }
        } else {
            // 异常交换如果放入的电池的uid为空，则需要清除guessId
            returnBattery(placeBattery, cabinetOrder.getUid());
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
            log.info("SELF OPEN CELL info! sessionId is {}, takeBattery.bindTime={},current time={}", eleSelfOPenCellOrderVo.getSessionId(), bindTime, System.currentTimeMillis());
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
            log.info("SELF OPEN CELL INFO! placeBatteryUid={},oldElectricityBattery uid={}", placeBattery.getUid(), oldElectricityBattery.getUid());
            newElectricityBattery.setGuessUid(placeBattery.getUid());
        }
        
        //如果放入的电池和用户绑定的电池不一样且放入的电池uid为空,guessUid不为空
        if (Objects.nonNull(placeBattery) && Objects.isNull(placeBattery.getUid()) && Objects.nonNull(placeBattery.getGuessUid())) {
            log.info("SELF OPEN CELL INFO! placeBatteryUid is null,oldElectricityBattery uid={},placeBatteryGuessUid={}", oldElectricityBattery.getUid(),
                    placeBattery.getGuessUid());
            newElectricityBattery.setGuessUid(placeBattery.getGuessUid());
        }
        
        //如果放入的电池和用户绑定的电池不一样且放入的电池uid为空,guessUid不为空
        if (Objects.nonNull(placeBattery) && Objects.isNull(placeBattery.getUid()) && Objects.isNull(placeBattery.getGuessUid())) {
            log.info("SELF OPEN CELL INFO!  placeBatteryUid is null,placeBatteryGuessUid is null");
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
        log.info("SELF OPEN CELL INFO! returnBattery.bindTime={},current time={}", bindTime, System.currentTimeMillis());
        if (Objects.isNull(bindTime) || bindTime < System.currentTimeMillis()) {
            newElectricityBattery.setBindTime(System.currentTimeMillis());
            electricityBatteryService.updateBatteryUser(newElectricityBattery);
        }
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


