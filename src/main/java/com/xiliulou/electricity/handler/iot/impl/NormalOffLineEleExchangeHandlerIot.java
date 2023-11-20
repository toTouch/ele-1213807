package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.vo.OperateMsgVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: HRP
 * @Date: 2022/03/03 15:22
 * @Description: 离线换电
 */
@Service(value = ElectricityIotConstant.NORMAL_OFFLINE_ELE_EXCHANGE_HANDLER)
@Slf4j
public class NormalOffLineEleExchangeHandlerIot extends AbstractElectricityIotHandler {
    
    /**
     * 订单来源 APP离线换电
     */
    private static final Integer ORDER_SOURCE_FOR_OFFLINE = 3;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    WechatTemplateNotificationConfig wechatTemplateNotificationConfig;
    
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Autowired
    BatteryOtherPropertiesService batteryOtherPropertiesService;
    
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    //    ElectricityMemberCardService electricityMemberCardService;
    
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    ElectricityCabinetOfflineReportOrderService electricityCabinetOfflineReportOrderService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    BatteryTrackRecordService batteryTrackRecordService;
    
    @Autowired
    UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Autowired
    private ElectricityMemberCardOrderService batteryMemberCardOrderService;
    
    @Autowired
    private UserBatteryTypeService userBatteryTypeService;
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("OFFLINE EXCHANGE NO sessionId={}", sessionId);
            return;
        }
        
        OfflineEleOrderVo offlineEleOrderVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), OfflineEleOrderVo.class);
        
        //查找用户
        User user = userService.queryByUserPhone(offlineEleOrderVo.getPhone(), User.TYPE_USER_NORMAL_WX_PRO, electricityCabinet.getTenantId());
        if (Objects.isNull(user)) {
            senMsg(electricityCabinet, offlineEleOrderVo, user);
            log.error("OFFLINE EXCHANGE ERROR! not found user! userPhone={}", offlineEleOrderVo.getPhone());
            return;
        }
        
        //幂等加锁
        Boolean result = redisService.setNx(CacheConstant.OFFLINE_ELE_RECEIVER_CACHE_KEY + offlineEleOrderVo.getOrderId() + receiverMessage.getType(), "true", 10 * 1000L, true);
        if (!result) {
            senMsg(electricityCabinet, offlineEleOrderVo, user);
            log.error("OFFLINE EXCHANGE orderId is lock,orderId={}", offlineEleOrderVo.getOrderId());
            return;
        }
        
        ElectricityCabinetOfflineReportOrder oldElectricityCabinetOfflineReportOrder = electricityCabinetOfflineReportOrderService.queryByOrderId(offlineEleOrderVo.getOrderId());
        if (Objects.nonNull(oldElectricityCabinetOfflineReportOrder)) {
            senMsg(electricityCabinet, offlineEleOrderVo, user);
            log.error("OFFLINE EXCHANGE orderId is lock,orderId={}", offlineEleOrderVo.getOrderId());
            return;
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("OFFLINE EXCHANGE ERROR! userInfo is null! userId={}", user.getUid());
            return;
        }
        
        //如果用户不是送的套餐
        //判断用户套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(
                userBatteryMemberCard.getRemainingNumber())) {
            log.warn("OFFLINE EXCHANGE ERROR! user haven't memberCard uid={}", user.getUid());
            return;
        }
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("OFFLINE EXCHANGE ERROR! user haven't memberCard uid={}", user.getUid());
            return;
        }
        
        //新仓门号处理
        Integer newCellNo = null;
        if (StringUtils.isNotEmpty(offlineEleOrderVo.getNewCellNo())) {
            newCellNo = Integer.valueOf(offlineEleOrderVo.getNewCellNo());
        }
        
        //订单状态处理
        String orderStatus = offlineEleOrderVo.getStatus();
        if (offlineEleOrderVo.getIsProcessFail()) {
            orderStatus = ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL;
        }
        
        //生成订单
        ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
                .orderId(generateOrderId(electricityCabinet.getId(), offlineEleOrderVo.getNewCellNo(), user.getUid())).uid(user.getUid()).phone(offlineEleOrderVo.getPhone())
                .electricityCabinetId(electricityCabinet.getId()).oldCellNo(Integer.valueOf(offlineEleOrderVo.getOldCellNo())).newCellNo(newCellNo)
                .newElectricityBatterySn(offlineEleOrderVo.getNewElectricityBatterySn()).oldElectricityBatterySn(offlineEleOrderVo.getOldElectricityBatterySn()).orderSeq(null)
                .status(orderStatus).source(Objects.isNull(offlineEleOrderVo.getOfflineOrderStatus()) ? ORDER_SOURCE_FOR_OFFLINE : offlineEleOrderVo.getOfflineOrderStatus())
                .paymentMethod(BatteryMemberCard.BUSINESS_TYPE_BATTERY).createTime(offlineEleOrderVo.getStartTime()).updateTime(offlineEleOrderVo.getEndTime())
                .storeId(electricityCabinet.getStoreId()).tenantId(electricityCabinet.getTenantId()).build();
        electricityCabinetOrderService.insertOrder(electricityCabinetOrder);
        
        //这里处理电池轨迹
        handleBatteryTrackRecord(electricityCabinetOrder, electricityCabinet, userInfo);
        
        //操作记录
        List<OperateMsgVo> operateMsgVoList = offlineEleOrderVo.getMsg();
        OffLineElectricityCabinetOrderOperHistory offLineElectricityCabinetOrderOperHistory = OffLineElectricityCabinetOrderOperHistory.builder()
                .orderId(electricityCabinetOrder.getOrderId()).type(ORDER_SOURCE_FOR_OFFLINE).tenantId(electricityCabinet.getTenantId()).operateMsgVos(operateMsgVoList).build();
        electricityCabinetOrderOperHistoryService.insertOffLineOperateHistory(offLineElectricityCabinetOrderOperHistory);
        
        senMsg(electricityCabinet, offlineEleOrderVo, user);
        
        //新增离线换电上报订单数据
        ElectricityCabinetOfflineReportOrder electricityCabinetOfflineReportOrder = ElectricityCabinetOfflineReportOrder.builder().orderId(offlineEleOrderVo.getOrderId())
                .createTime(System.currentTimeMillis()).build();
        electricityCabinetOfflineReportOrderService.insertOrder(electricityCabinetOfflineReportOrder);
        
        if (offlineEleOrderVo.getIsProcessFail()) {
            log.warn("OFFLINE EXCHANGE ERROR! exchange exception!orderId={}", offlineEleOrderVo.getOrderId());
            senMsg(electricityCabinet, offlineEleOrderVo, user);
            return;
        }
        
        if (!Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.UN_LIMIT) && Objects.nonNull(userBatteryMemberCard.getOrderEffectiveTime())) {
            //如果换电订单的时间在当前套餐生效时间之后，则扣减次数
            if (offlineEleOrderVo.getEndTime() > userBatteryMemberCard.getOrderEffectiveTime() && userBatteryMemberCard.getOrderExpireTime() > System.currentTimeMillis()) {
                //扣除月卡
                userBatteryMemberCardService.minCount(userBatteryMemberCard);
            }
            
            //如果套餐没过期并且剩余次数为1
            if ((userBatteryMemberCard.getOrderExpireTime() < System.currentTimeMillis()) || Objects.equals(userBatteryMemberCard.getOrderRemainingNumber(),
                    UserBatteryMemberCard.MEMBER_CARD_ONE_REMAINING)) {
                updateUserBatteryMemberCardInfo(userBatteryMemberCard, userInfo, offlineEleOrderVo.getEndTime());
            }
        }
        //查询当前归还的电池信息
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySnFromDb(offlineEleOrderVo.getOldElectricityBatterySn());
        //更新旧电池为在仓
        if (Objects.isNull(oldElectricityBattery)) {
            log.error("OFFLINE EXCHANGE ERROR! electricityBattery is null! BatterySn={}", offlineEleOrderVo.getOldElectricityBatterySn());
            return;
        }
        
        //通过guessUid获取电池信息; 如果有电池的guessUid为当前换电用户 ,则将此电池更新为放入电池的Uid
        List<ElectricityBattery> electricityBatteries = electricityBatteryService.listBatteryByGuessUid(userInfo.getUid());
        if (CollectionUtils.isNotEmpty(electricityBatteries) && !Objects.equals(oldElectricityBattery.getUid(), userInfo.getUid())) {
            List<Long> batteryIdList = electricityBatteries.stream().map(ElectricityBattery::getId).collect(Collectors.toList());
            if (Objects.nonNull(oldElectricityBattery.getUid())) {
                electricityBatteryService.batchUpdateBatteryGuessUid(batteryIdList, oldElectricityBattery.getUid());
            } else {
                electricityBatteryService.batchUpdateBatteryGuessUid(batteryIdList, oldElectricityBattery.getGuessUid());
            }
        }
        
        ElectricityBattery inWarehouseElectricityBattery = new ElectricityBattery();
        inWarehouseElectricityBattery.setId(oldElectricityBattery.getId());
        //        InWarehouseElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
        inWarehouseElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_RETURN);
        inWarehouseElectricityBattery.setElectricityCabinetId(electricityCabinet.getId());
        inWarehouseElectricityBattery.setElectricityCabinetName(electricityCabinet.getName());
        inWarehouseElectricityBattery.setUid(null);
        inWarehouseElectricityBattery.setGuessUid(null);
        inWarehouseElectricityBattery.setUpdateTime(System.currentTimeMillis());
        inWarehouseElectricityBattery.setBorrowExpireTime(null);
        Long returnBindTime = oldElectricityBattery.getBindTime();
        
        //如果绑定时间为空或者电池绑定时间小于当前时间则更新电池信息
        log.info("off returnBindTime={},end time={},batteryId={}", returnBindTime, offlineEleOrderVo.getEndTime(), inWarehouseElectricityBattery.getId());
        if (Objects.isNull(returnBindTime) || returnBindTime < offlineEleOrderVo.getEndTime()) {
            inWarehouseElectricityBattery.setBindTime(offlineEleOrderVo.getEndTime());
            electricityBatteryService.updateBatteryUser(inWarehouseElectricityBattery);
        }
        
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
        ElectricityBattery newElectricityBattery = electricityBatteryService.queryBySnFromDb(offlineEleOrderVo.getNewElectricityBatterySn());
        
        //如果已租电池的时间小于用户当前绑定电池的时间  则不需要更新
        if (Objects.nonNull(electricityBattery) && Objects.nonNull(electricityBattery.getBindTime()) && Objects.nonNull(newElectricityBattery.getBindTime())
                && electricityBattery.getBindTime() > offlineEleOrderVo.getEndTime()) {
            log.warn(
                    "OFFLINE EXCHANGE ERROR! electricityBattery bindTime less than new electricityBattery bindTime,electricityBattery bindTime={},new electricityBattery bindTime={}",
                    electricityBattery.getBindTime(), newElectricityBattery.getBindTime());
            return;
        }
        
        //用户绑定电池和还入电池是否一致，不一致绑定的电池更新为游离态
        if (Objects.nonNull(electricityBattery)) {
            //如果用户绑定的电池与放入的电池不一致 并且绑定的电池的uid和放入的电池的uid相同（离线换电会发生这种情况）
            if (!Objects.equals(electricityBattery.getSn(), oldElectricityBattery.getSn()) && Objects.equals(electricityBattery.getUid(), oldElectricityBattery.getUid())
                    && Objects.nonNull(electricityBattery.getBindTime()) && electricityBattery.getBindTime() > offlineEleOrderVo.getEndTime()) {
                log.warn("OFFLINE EXCHANGE ERROR! user binding battery! userId={},return batterySn={},binding batterySn={}", user.getUid(), oldElectricityBattery.getSn(),
                        electricityBattery.getSn());
                return;
            }
            
            if (!Objects.equals(electricityBattery.getSn(), offlineEleOrderVo.getOldElectricityBatterySn())) {
                ElectricityBattery newBattery = new ElectricityBattery();
                newBattery.setId(electricityBattery.getId());
                //                newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_FREE);
                newBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_EXCEPTION);
                newBattery.setUid(null);
                
                //设置用户绑定的电池的guessId为归还电池的用户Id
                if (Objects.nonNull(oldElectricityBattery.getUid()) && !Objects.equals(oldElectricityBattery.getUid(), electricityBattery.getUid())) {
                    newBattery.setGuessUid(oldElectricityBattery.getUid());
                }
                
                //设置用户绑定的电池的guessId为归还电池的guessUId
                if (Objects.isNull(oldElectricityBattery.getUid()) && Objects.nonNull(oldElectricityBattery.getGuessUid())) {
                    newBattery.setGuessUid(oldElectricityBattery.getGuessUid());
                }
                
                //设置用户绑定的电池的guessId为归还电池的guessUId
                if (Objects.isNull(oldElectricityBattery.getUid()) && Objects.isNull(oldElectricityBattery.getGuessUid())) {
                    newBattery.setGuessUid(null);
                }
                
                newBattery.setUpdateTime(System.currentTimeMillis());
                newBattery.setElectricityCabinetId(null);
                newBattery.setElectricityCabinetName(null);
                newBattery.setBorrowExpireTime(null);
                electricityBatteryService.updateBatteryUser(newBattery);
            }
        }
        
        //判断用户是已租赁电池状态
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_NO)) {
            log.warn("OFFLINE EXCHANGE ERROR! user not rent battery! userId={}", user.getUid());
            return;
        }
        
        //更新新电池为在用
        if (Objects.isNull(newElectricityBattery)) {
            log.warn("OFFLINE EXCHANGE ERROR! electricityBattery is null! BatterySn={}", offlineEleOrderVo.getNewElectricityBatterySn());
            return;
        }
        
        //后续日志代码需要删除
        if (Objects.nonNull(electricityBattery)) {
            log.info("currentUserId={},electricityBatterySn={},electricityBatteryBindTime={},newElectricityBatterySn={},newElectricityBatteryBindTime={},offerOrderEndTime={}",
                    user.getUid(), electricityBattery.getSn(), electricityBattery.getBindTime(), newElectricityBattery.getSn(), newElectricityBattery.getBindTime(),
                    offlineEleOrderVo.getEndTime());
        }
        
        ElectricityBattery usingElectricityBattery = new ElectricityBattery();
        usingElectricityBattery.setId(newElectricityBattery.getId());
        //        UsingElectricityBattery.setStatus(ElectricityBattery.LEASE_STATUS);
        usingElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_LEASE);
        usingElectricityBattery.setElectricityCabinetId(null);
        usingElectricityBattery.setElectricityCabinetName(null);
        usingElectricityBattery.setUid(user.getUid());
        usingElectricityBattery.setUpdateTime(System.currentTimeMillis());
        usingElectricityBattery.setBorrowExpireTime(Long.parseLong(wechatTemplateNotificationConfig.getExpirationTime()) * 3600000 + System.currentTimeMillis());
        
        //设置电池的绑定时间 1.必须为在租用户 2.要更新的电池的绑定时间为空或者小于上报订单的结束时间
        //设置电池的绑定时间
        Long bindTime = null;
        if (Objects.nonNull(electricityBattery)) {
            bindTime = newElectricityBattery.getBindTime();
        }
        log.info("off bindTime={},end time={},batterySn={}", bindTime, offlineEleOrderVo.getEndTime(), newElectricityBattery.getSn());
        if (Objects.isNull(bindTime) || bindTime < offlineEleOrderVo.getEndTime()) {
            usingElectricityBattery.setBindTime(offlineEleOrderVo.getEndTime());
            electricityBatteryService.updateBatteryUser(usingElectricityBattery);
        }
    }
    
    private void handleBatteryTrackRecord(ElectricityCabinetOrder electricityCabinetOrder, ElectricityCabinet electricityCabinet, UserInfo userInfo) {
        if (StrUtil.isEmpty(electricityCabinetOrder.getOldElectricityBatterySn()) || StrUtil.isEmpty(electricityCabinetOrder.getNewElectricityBatterySn())) {
            return;
        }
        
        BatteryTrackRecord outBatteryTrackRecord = new BatteryTrackRecord().setSn(electricityCabinetOrder.getNewElectricityBatterySn())
                .setEId(Long.valueOf(electricityCabinet.getId())).setEName(electricityCabinet.getName()).setENo(electricityCabinetOrder.getNewCellNo())
                .setType(BatteryTrackRecord.TYPE_OFFLINE_EXCHANGE_OUT).setCreateTime(TimeUtils.convertToStandardFormatTime(electricityCabinetOrder.getUpdateTime()))
                .setOrderId(electricityCabinetOrder.getOrderId()).setUid(userInfo.getUid()).setName(userInfo.getName()).setPhone(userInfo.getPhone());
        batteryTrackRecordService.putBatteryTrackQueue(outBatteryTrackRecord);
        
        BatteryTrackRecord inBatteryTrackRecord = new BatteryTrackRecord().setSn(electricityCabinetOrder.getOldElectricityBatterySn())
                .setEId(Long.valueOf(electricityCabinet.getId())).setEName(electricityCabinet.getName()).setENo(electricityCabinetOrder.getOldCellNo())
                .setType(BatteryTrackRecord.TYPE_OFFLINE_EXCHANGE_IN).setCreateTime(TimeUtils.convertToStandardFormatTime(electricityCabinetOrder.getCreateTime()))
                .setOrderId(electricityCabinetOrder.getOrderId());
        batteryTrackRecordService.putBatteryTrackQueue(inBatteryTrackRecord);
    }
    
    private void updateUserBatteryMemberCardInfo(UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo, Long endTime) {
        UserBatteryMemberCardPackage userBatteryMemberCardPackageLatest = userBatteryMemberCardPackageService.selectNearestByUid(userBatteryMemberCard.getUid());
        if (Objects.isNull(userBatteryMemberCardPackageLatest)) {
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(endTime);
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            return;
        }
        
        //更新当前用户绑定的套餐数据
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        userBatteryMemberCardUpdate.setMemberCardId(userBatteryMemberCardPackageLatest.getMemberCardId());
        userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis() + userBatteryMemberCardPackageLatest.getMemberCardExpireTime());
        userBatteryMemberCardUpdate.setMemberCardExpireTime(
                userBatteryMemberCard.getMemberCardExpireTime() - (userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis()));
        userBatteryMemberCardUpdate.setOrderRemainingNumber(userBatteryMemberCardPackageLatest.getRemainingNumber());
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        
        //删除资源包
        userBatteryMemberCardPackageService.deleteByOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        
        //更新原来绑定的套餐订单状态
        ElectricityMemberCardOrder oldMemberCardOrder = new ElectricityMemberCardOrder();
        oldMemberCardOrder.setOrderId(userBatteryMemberCard.getOrderId());
        oldMemberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
        oldMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardOrderService.updateStatusByOrderNo(oldMemberCardOrder);
        
        //更新新绑定的套餐订单的状态
        ElectricityMemberCardOrder currentMemberCardOrder = new ElectricityMemberCardOrder();
        currentMemberCardOrder.setOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        currentMemberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
        currentMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardOrderService.updateStatusByOrderNo(currentMemberCardOrder);
        
        ElectricityMemberCardOrder electricityMemberCardOrder = batteryMemberCardOrderService.selectByOrderNo(userBatteryMemberCardPackageLatest.getOrderId());
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("TRANSFER BATTERY MEMBER CARD PACKAGE ERROR!not found user member card order Info,uid={},orderId={}", userBatteryMemberCard.getUid(),
                    userBatteryMemberCardPackageLatest.getOrderId());
            return;
        }
        
        //更新用户电池型号
        userBatteryTypeService.updateUserBatteryType(electricityMemberCardOrder, userInfo);
    }
    
    private void senMsg(ElectricityCabinet electricityCabinet, OfflineEleOrderVo offlineEleOrderVo, User user) {
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("orderId", offlineEleOrderVo.getOrderId());
        dataMap.put("status", offlineEleOrderVo.getStatus());
        
        Long uid = -1L;
        if (Objects.nonNull(user)) {
            uid = user.getUid();
        }
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + uid + "_" + offlineEleOrderVo.getOrderId()).data(dataMap)
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.OFFLINE_ELE_EXCHANGE_ORDER_MANAGE_SUCCESS).build();
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        if (!sendResult.getLeft()) {
            log.error("OFFLINE EXCHANGE ERROR! send command error! orderId:{}", offlineEleOrderVo.getOrderId());
        }
    }
    
    private String generateOrderId(Integer id, String cellNo, Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + id + cellNo + uid;
    }
    
    
    @Data
    class OfflineEleOrderVo {
        
        /**
         * 换电柜旧仓门号
         */
        private String oldCellNo;
        
        /**
         * 换电柜新仓门号
         */
        private String newCellNo;
        
        /**
         * 旧电池编号
         */
        private String oldElectricityBatterySn;
        
        /**
         * 新电池编号
         */
        private String newElectricityBatterySn;
        
        /**
         * 订单状态
         */
        private String status;
        
        /**
         * 订单开始时间
         */
        private Long startTime;
        
        /**
         * 订单结束时间
         */
        private Long endTime;
        
        /**
         * 本次操作是否执行失败
         */
        private Boolean isProcessFail;
        
        /**
         * 订单号
         */
        private String orderId;
        
        /**
         * 用户手机号
         */
        private String phone;
        
        /**
         * 是否上报电池类型
         */
        private Boolean isMultiBatteryModel;
        
        /**
         * 旧电池电量
         */
        private Double power;
        
        /**
         * 操作记录列表
         */
        private List<OperateMsgVo> msg;
        /**
         * 订单来源 APP离线换电
         */
        //        protected static final Integer ORDER_SOURCE_FOR_OFFLINE = 3;
        
        /**
         * 离线订单类型  3 离线换电  4  蓝牙
         */
        private Integer offlineOrderStatus;
    }
}


