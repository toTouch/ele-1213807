package com.xiliulou.electricity.handler.iot.impl;

import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.vo.WarnMsgVo;
import com.xiliulou.iot.entity.HardwareCommand;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import java.util.HashMap;
import java.util.UUID;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Service(value = ElectricityIotConstant.NORMAL_NEW_EXCHANGE_ORDER_HANDLER)
@Slf4j
public class NormalNewExchangeOrderHandlerIot extends AbstractElectricityIotHandler {
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
    FranchiseeUserInfoService franchiseeUserInfoService;
    @Autowired
    WechatTemplateNotificationConfig wechatTemplateNotificationConfig;

    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;


    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        ExchangeOrderRsp exchangeOrderRsp = JsonUtil.fromJson(receiverMessage.getOriginContent(), ExchangeOrderRsp.class);
        if (Objects.isNull(exchangeOrderRsp)) {
            log.error("EXCHANGE ORDER ERROR! originData is null! requestId={}", receiverMessage.getSessionId());
            return;
        }

        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.queryByOrderId(exchangeOrderRsp.getOrderId());
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("EXCHANGE ORDER ERROR! order not found !requestId={},orderId={}", receiverMessage.getSessionId(), exchangeOrderRsp.getOrderId());
            return;
        }

        if (exchangeOrderRsp.getOrderSeq() > exchangeOrderRsp.getOrderSeq()) {
            log.error("EXCHANGE ORDER ERROR! rsp order seq is lower order! requestId={},orderId={},uid={}", receiverMessage.getSessionId(), exchangeOrderRsp.getOrderId(), electricityCabinetOrder.getUid());
            return;
        }

        //是否开启异常仓门锁仓
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinetOrder.getTenantId());
        if (Objects.nonNull(electricityConfig)
                && Objects.equals(electricityConfig.getIsOpenDoorLock(), ElectricityConfig.OPEN_DOOR_LOCK)
                && exchangeOrderRsp.getIsException()) {
            lockExceptionDoor(electricityCabinetOrder);
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
        electricityCabinetOrderService.update(newElectricityCabinetOrder);

        //处理放入电池的相关信息
        handlePlaceBatteryInfo(exchangeOrderRsp, electricityCabinetOrder, electricityCabinet);

        //处理取走电池的相关信息
        handleTakeBatteryInfo(exchangeOrderRsp, electricityCabinetOrder, electricityCabinet);

    }

    private void handleTakeBatteryInfo(ExchangeOrderRsp exchangeOrderRsp, ElectricityCabinetOrder electricityCabinetOrder, ElectricityCabinet electricityCabinet) {
        if (!exchangeOrderRsp.getOrderStatus().equals(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            return;
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCabinetOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("EXCHANGE ORDER ERROR! userInfo is null!uid={},requestId={},orderId={}", electricityCabinetOrder.getUid(), exchangeOrderRsp.getSessionId(), exchangeOrderRsp.getOrderId());
            return;
        }

        //用户绑新电池
        FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
        franchiseeUserInfo.setUserInfoId(userInfo.getId());
        franchiseeUserInfo.setNowElectricityBatterySn(exchangeOrderRsp.getTakeBatteryName());
        franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfoService.updateByUserInfoId(franchiseeUserInfo);

        //查看用户是否有以前绑定的电池
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUid(electricityCabinetOrder.getUid());
        if (Objects.nonNull(oldElectricityBattery)) {
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(oldElectricityBattery.getId());
            newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_FREE);
            newElectricityBattery.setUid(null);
            newElectricityBattery.setElectricityCabinetId(null);
            newElectricityBattery.setElectricityCabinetName(null);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBatteryService.updateByOrder(newElectricityBattery);
        }


        //电池改为在用
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(exchangeOrderRsp.getTakeBatteryName());
        ElectricityBattery newElectricityBattery = new ElectricityBattery();
        newElectricityBattery.setId(electricityBattery.getId());
        newElectricityBattery.setStatus(ElectricityBattery.LEASE_STATUS);
        newElectricityBattery.setElectricityCabinetId(null);
        newElectricityBattery.setElectricityCabinetName(null);
        newElectricityBattery.setUid(electricityCabinetOrder.getUid());
        newElectricityBattery.setExchangeCount(electricityBattery.getExchangeCount() + 1);
        newElectricityBattery.setUpdateTime(System.currentTimeMillis());
        newElectricityBattery.setBorrowExpireTime(Long.parseLong(wechatTemplateNotificationConfig.getExpirationTime()) * 3600000 + System.currentTimeMillis());
        electricityBatteryService.updateByOrder(newElectricityBattery);
    }

    private void handlePlaceBatteryInfo(ExchangeOrderRsp exchangeOrderRsp, ElectricityCabinetOrder electricityCabinetOrder, ElectricityCabinet electricityCabinet) {
        if (!exchangeOrderRsp.getOrderStatus().equals(ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
            return;
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCabinetOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("EXCHANGE ORDER ERROR! userInfo is null!uid={},requestId={},orderId={}", electricityCabinetOrder.getUid(), exchangeOrderRsp.getSessionId(), exchangeOrderRsp.getOrderId());
            return;
        }

        FranchiseeUserInfo oldFranchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
        if (Objects.isNull(oldFranchiseeUserInfo)) {
            log.error("EXCHANGE ORDER ERROR! franchiseeUserInfo is null! uid={},requestId={},orderId:{}", electricityCabinetOrder.getUid(), exchangeOrderRsp.getSessionId(), electricityCabinetOrder.getOrderId());
            return;
        }

        //用户解绑旧电池 旧电池到底是哪块，不确定
        FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
        franchiseeUserInfo.setId(oldFranchiseeUserInfo.getId());
        franchiseeUserInfo.setNowElectricityBatterySn(null);
        franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfoService.update(franchiseeUserInfo);

        //查看用户是否有绑定的电池,绑定电池和放入电池不一致则绑定电池处于游离态
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(electricityCabinetOrder.getUid());
        if (Objects.nonNull(electricityBattery) && !Objects.equals(electricityBattery.getSn(), exchangeOrderRsp.getPlaceBatteryName())) {
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(electricityBattery.getId());
            newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_FREE);
            newElectricityBattery.setUid(null);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            newElectricityBattery.setElectricityCabinetId(null);
            newElectricityBattery.setElectricityCabinetName(null);
            electricityBatteryService.updateByOrder(newElectricityBattery);
        }

        //放入电池改为在仓
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySn(exchangeOrderRsp.getPlaceBatteryName());
        if (Objects.nonNull(oldElectricityBattery)) {
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(oldElectricityBattery.getId());
            newElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
            newElectricityBattery.setElectricityCabinetId(electricityCabinet.getId());
            newElectricityBattery.setElectricityCabinetName(electricityCabinet.getName());
            newElectricityBattery.setUid(null);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            newElectricityBattery.setBorrowExpireTime(null);
            electricityBatteryService.updateByOrder(newElectricityBattery);
        }


    }


    private void handleOrderException(ElectricityCabinetOrder electricityCabinetOrder, ExchangeOrderRsp exchangeOrderRsp, ElectricityConfig electricityConfig) {
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

    private boolean allowSelfOpenStatus(String orderStatus, ElectricityConfig electricityConfig) {
        return orderStatus.equals(ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL) && Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsEnableSelfOpen(), ElectricityConfig.ENABLE_SELF_OPEN);
    }

    // TODO: 2022/8/1 异常锁定格挡
    private void lockExceptionDoor(ElectricityCabinetOrder electricityCabinetOrder,ExchangeOrderRsp exchangeOrderRsp) {


        //上报的订单状态值
        String orderStatus = exchangeOrderRsp.getOrderStatus();
        if (Objects.isNull(orderStatus)) {
            log.error("ELE LOCK CELL orderStatus is null! orderId:{}", exchangeOrderRsp.getOrderId());
            return;
        }

        //仓门编号
        Integer cellNo = null;
        //电柜Id
        Integer electricityCabinetId = null;

        //旧仓门异常
        if (Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_OPEN_FAIL)
            || Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)
            || Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_BATTERY_CHECK_TIMEOUT)) {
            cellNo = electricityCabinetOrder.getOldCellNo();
            electricityCabinetId = electricityCabinetOrder.getElectricityCabinetId();
        } else if (Objects.equals(orderStatus, ElectricityCabinetOrder.COMPLETE_OPEN_FAIL)
            || Objects.equals(orderStatus, ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_TIMEOUT)) {
            cellNo = electricityCabinetOrder.getNewCellNo();
            electricityCabinetId = electricityCabinetOrder.getElectricityCabinetId();
        }

        if (Objects.isNull(cellNo) || Objects.isNull(electricityCabinetId)) {
            log.error("ELE LOCK CELL cellNo or electricityCabinetId is null! orderId:{}", exchangeOrderRsp.getOrderId());
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
        dataMap.put("isForbidden", true);

        HardwareCommandQuery comm = HardwareCommandQuery.builder()
            .sessionId(UUID.randomUUID().toString().replace("-", ""))
            .data(dataMap)
            .productKey(electricityCabinet.getProductKey())
            .deviceName(electricityCabinet.getDeviceName())
            .command(HardwareCommand.ELE_COMMAND_CELL_UPDATE)
            .build();

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
         * 订单状态：
         * INIT_CHECK_FAIL(1.1),
         * INIT_OPEN_FAIL(2.1),
         * INIT_OPEN_SUCCESS(2.0),
         * INIT_BATTERY_CHECK_FAIL(3.1)
         * INIT_BATTERY_CHECK_SUCCESS(3.0)
         * COMPLETE_OPEN_FAIL(5.1)
         * COMPLETE_OPEN_SUCCESS(5.0)
         * COMPLETE_BATTERY_TAKE_TIMEOUT(6.1)
         * COMPLETE_BATTERY_TAKE_SUCCESS(6.0)
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


