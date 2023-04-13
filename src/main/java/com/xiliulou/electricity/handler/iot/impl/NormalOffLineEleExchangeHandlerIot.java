package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.vo.OperateMsgVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
    ElectricityMemberCardService electricityMemberCardService;
    
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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("OFFLINE EXCHANGE NO sessionId={}", sessionId);
            return;
        }
        
        OfflineEleOrderVo offlineEleOrderVo = JsonUtil.fromJson(receiverMessage.getOriginContent(),
                OfflineEleOrderVo.class);
        
        //查找用户
        User user = userService.queryByUserPhone(offlineEleOrderVo.getPhone(), User.TYPE_USER_NORMAL_WX_PRO,
                electricityCabinet.getTenantId());
        if (Objects.isNull(user)) {
            senMsg(electricityCabinet, offlineEleOrderVo, user);
            log.error("OFFLINE EXCHANGE ERROR! not found user! userPhone={}", offlineEleOrderVo.getPhone());
            return;
        }
        
        //幂等加锁
        Boolean result = redisService.setNx(
                CacheConstant.OFFLINE_ELE_RECEIVER_CACHE_KEY + offlineEleOrderVo.getOrderId()
                        + receiverMessage.getType(), "true", 10 * 1000L, true);
        if (!result) {
            senMsg(electricityCabinet, offlineEleOrderVo, user);
            log.error("OFFLINE EXCHANGE orderId is lock,orderId={}", offlineEleOrderVo.getOrderId());
            return;
        }
        
        ElectricityCabinetOfflineReportOrder oldElectricityCabinetOfflineReportOrder = electricityCabinetOfflineReportOrderService.queryByOrderId(
                offlineEleOrderVo.getOrderId());
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
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(
                userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("OFFLINE EXCHANGE ERROR! user haven't memberCard uid={}", user.getUid());
            return;
        }
        
        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(
                userBatteryMemberCard.getMemberCardId().intValue());
        if (Objects.isNull(electricityMemberCard)) {
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
                .orderId(generateOrderId(electricityCabinet.getId(), offlineEleOrderVo.getNewCellNo(), user.getUid()))
                .uid(user.getUid()).phone(offlineEleOrderVo.getPhone()).electricityCabinetId(electricityCabinet.getId())
                .oldCellNo(Integer.valueOf(offlineEleOrderVo.getOldCellNo())).newCellNo(newCellNo)
                .newElectricityBatterySn(offlineEleOrderVo.getNewElectricityBatterySn())
                .oldElectricityBatterySn(offlineEleOrderVo.getOldElectricityBatterySn()).orderSeq(null)
                .status(orderStatus).source(offlineEleOrderVo.getOfflineOrderStatus()).paymentMethod(electricityMemberCard.getType())
                .createTime(offlineEleOrderVo.getStartTime()).updateTime(offlineEleOrderVo.getEndTime())
                .storeId(electricityCabinet.getStoreId()).tenantId(electricityCabinet.getTenantId()).build();
        electricityCabinetOrderService.insertOrder(electricityCabinetOrder);
        
        //这里处理电池轨迹
        handleBatteryTrackRecord(electricityCabinetOrder, electricityCabinet);
        
        //操作记录
        List<OperateMsgVo> operateMsgVoList = offlineEleOrderVo.getMsg();
        OffLineElectricityCabinetOrderOperHistory offLineElectricityCabinetOrderOperHistory = OffLineElectricityCabinetOrderOperHistory.builder()
                .orderId(electricityCabinetOrder.getOrderId()).type(ORDER_SOURCE_FOR_OFFLINE)
                .tenantId(electricityCabinet.getTenantId()).operateMsgVos(operateMsgVoList).build();
        electricityCabinetOrderOperHistoryService.insertOffLineOperateHistory(
                offLineElectricityCabinetOrderOperHistory);
        
        senMsg(electricityCabinet, offlineEleOrderVo, user);
        
        //新增离线换电上报订单数据
        ElectricityCabinetOfflineReportOrder electricityCabinetOfflineReportOrder = ElectricityCabinetOfflineReportOrder.builder()
                .orderId(offlineEleOrderVo.getOrderId()).createTime(System.currentTimeMillis()).build();
        electricityCabinetOfflineReportOrderService.insertOrder(electricityCabinetOfflineReportOrder);
        
        if (offlineEleOrderVo.getIsProcessFail()) {
            log.warn("OFFLINE EXCHANGE ERROR! exchange exception!orderId={}", offlineEleOrderVo.getOrderId());
            senMsg(electricityCabinet, offlineEleOrderVo, user);
            return;
        }
        
        if (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
            //扣除月卡
            userBatteryMemberCardService.minCountForOffLineEle(userBatteryMemberCard);
        }
        
        //用户绑定电池和还入电池是否一致，不一致绑定的电池更新为游离态
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
        if (Objects.nonNull(electricityBattery)) {
            if (!Objects.equals(electricityBattery.getSn(), offlineEleOrderVo.getOldElectricityBatterySn())) {
                ElectricityBattery newElectricityBattery = new ElectricityBattery();
                newElectricityBattery.setId(electricityBattery.getId());
                //                newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_FREE);
                newElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_EXCEPTION);
                newElectricityBattery.setUid(null);
                newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                newElectricityBattery.setElectricityCabinetId(null);
                newElectricityBattery.setElectricityCabinetName(null);
                newElectricityBattery.setBorrowExpireTime(null);
                electricityBatteryService.updateBatteryUser(newElectricityBattery);
            }
        }
        
        //更新旧电池为在仓
        ElectricityBattery oldElectricityBattery = electricityBatteryService
                .queryBySnFromDb(offlineEleOrderVo.getOldElectricityBatterySn());
        if (Objects.isNull(oldElectricityBattery)) {
            log.error("OFFLINE EXCHANGE ERROR! electricityBattery is null! BatterySn={}",
                    offlineEleOrderVo.getOldElectricityBatterySn());
            return;
        }
        ElectricityBattery inWarehouseElectricityBattery = new ElectricityBattery();
        inWarehouseElectricityBattery.setId(oldElectricityBattery.getId());
        //        InWarehouseElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
        inWarehouseElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_RETURN);
        inWarehouseElectricityBattery.setElectricityCabinetId(electricityCabinet.getId());
        inWarehouseElectricityBattery.setElectricityCabinetName(electricityCabinet.getName());
        inWarehouseElectricityBattery.setUid(null);
        inWarehouseElectricityBattery.setUpdateTime(System.currentTimeMillis());
        inWarehouseElectricityBattery.setBorrowExpireTime(null);
        electricityBatteryService.updateBatteryUser(inWarehouseElectricityBattery);
    
        //更新新电池为在用
        ElectricityBattery newElectricityBattery = electricityBatteryService.queryBySnFromDb(
                offlineEleOrderVo.getNewElectricityBatterySn());
        if (Objects.isNull(newElectricityBattery)) {
            log.error("OFFLINE EXCHANGE ERROR! electricityBattery is null! BatterySn={}",
                    offlineEleOrderVo.getNewElectricityBatterySn());
            return;
        }
        ElectricityBattery usingElectricityBattery = new ElectricityBattery();
        usingElectricityBattery.setId(newElectricityBattery.getId());
        //        UsingElectricityBattery.setStatus(ElectricityBattery.LEASE_STATUS);
        usingElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_LEASE);
        usingElectricityBattery.setElectricityCabinetId(null);
        usingElectricityBattery.setElectricityCabinetName(null);
        usingElectricityBattery.setUid(user.getUid());
        usingElectricityBattery.setUpdateTime(System.currentTimeMillis());
        usingElectricityBattery.setBorrowExpireTime(
                Long.parseLong(wechatTemplateNotificationConfig.getExpirationTime()) * 3600000
                        + System.currentTimeMillis());
        electricityBatteryService.updateBatteryUser(usingElectricityBattery);
    }
    
    private void handleBatteryTrackRecord(ElectricityCabinetOrder electricityCabinetOrder,
            ElectricityCabinet electricityCabinet) {
        if (StrUtil.isEmpty(electricityCabinetOrder.getOldElectricityBatterySn()) || StrUtil.isEmpty(
                electricityCabinetOrder.getNewElectricityBatterySn())) {
            return;
        }
        
        BatteryTrackRecord outBatteryTrackRecord = new BatteryTrackRecord().setSn(
                        electricityCabinetOrder.getNewElectricityBatterySn()).setEId(Long.valueOf(electricityCabinet.getId()))
                .setEName(electricityCabinet.getName()).setENo(electricityCabinetOrder.getNewCellNo())
                .setType(BatteryTrackRecord.TYPE_OFFLINE_EXCHANGE_OUT).setCreateTime(electricityCabinetOrder.getUpdateTime())
                .setOrderId(electricityCabinetOrder.getOrderId());
        batteryTrackRecordService.insert(outBatteryTrackRecord);
        
        BatteryTrackRecord inBatteryTrackRecord = new BatteryTrackRecord().setSn(
                        electricityCabinetOrder.getOldElectricityBatterySn()).setEId(Long.valueOf(electricityCabinet.getId()))
                .setEName(electricityCabinet.getName()).setENo(electricityCabinetOrder.getOldCellNo())
                .setType(BatteryTrackRecord.TYPE_OFFLINE_EXCHANGE_IN).setCreateTime(electricityCabinetOrder.getCreateTime())
                .setOrderId(electricityCabinetOrder.getOrderId());
        batteryTrackRecordService.insert(inBatteryTrackRecord);
    }
    
    private void senMsg(ElectricityCabinet electricityCabinet, OfflineEleOrderVo offlineEleOrderVo, User user) {
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("orderId", offlineEleOrderVo.getOrderId());
        dataMap.put("status", offlineEleOrderVo.getStatus());
        
        Long uid = -1L;
        if (Objects.nonNull(user)) {
            uid = user.getUid();
        }
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(
                        CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + uid + "_"
                                + offlineEleOrderVo.getOrderId()).data(dataMap).productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
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
         * */
        private Integer offlineOrderStatus;
    }
}


