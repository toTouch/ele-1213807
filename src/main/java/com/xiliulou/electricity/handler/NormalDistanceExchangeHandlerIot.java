package com.xiliulou.electricity.handler;

import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;


/**
 * @author: eclair
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service
@Slf4j
public class NormalDistanceExchangeHandlerIot extends AbstractIotMessageHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    DistanceExchangeOrderService distanceExchangeOrderService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    @Autowired
    DistanceOrderOperHistoryService distanceOrderOperHistoryService;


    @Override
    protected Pair<SendHardwareMessage, String> generateMsg(HardwareCommandQuery hardwareCommandQuery) {
        String sessionId = generateSessionId(hardwareCommandQuery);
        SendHardwareMessage message = SendHardwareMessage.builder()
                .sessionId(sessionId)
                .type(hardwareCommandQuery.getCommand())
                .data(hardwareCommandQuery.getData()).build();
        return Pair.of(message, sessionId);
    }

    @Override
    protected boolean receiveMessageProcess(ReceiverMessage receiverMessage) {
        DistanceExchangeOrderRsp distanceExchangeOrderRsp = JsonUtil.fromJson(receiverMessage.getOriginContent(), DistanceExchangeOrderRsp.class);
        if (Objects.isNull(distanceExchangeOrderRsp)) {
            log.error("DISTANCE EXCHANGE ERROR! parse rsp error! sessionId={}", receiverMessage.getSessionId());
            return false;
        }

        DistanceExchangeOrder distanceExchangeOrder = distanceExchangeOrderService.queryByOrderId(distanceExchangeOrderRsp.getOrderId());
        if (Objects.isNull(distanceExchangeOrder)) {
            log.error("DISTANCE EXCHANGE ERROR! not found exchangeOrder! sessionId={},orderId={}", receiverMessage.getSessionId(), distanceExchangeOrderRsp.getOrderId());
            return false;
        }

        if (distanceExchangeOrderRsp.getOrderSeq() <= distanceExchangeOrder.getOrderSeq()) {
            log.error("DISTANCE EXCHANGE ERROR! order's seq high mns's seq ! sessionId={},orderId={},orderSeq={},mnsSeq={}", receiverMessage.getSessionId(), distanceExchangeOrder.getOrderId(), distanceExchangeOrder.getOrderSeq(), distanceExchangeOrderRsp.getOrderSeq());
            return false;
        }


        if (distanceExchangeOrderRsp.getOrderStatus().equalsIgnoreCase(DistanceExchangeOrder.STATUS_EXCEPTION_ORDER) || distanceExchangeOrderRsp.getOrderStatus().equalsIgnoreCase(DistanceExchangeOrder.STATUS_NEED_PAY)) {
            redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + distanceExchangeOrder.getEId());
        }

        DistanceExchangeOrder updateOrder = new DistanceExchangeOrder();
        updateOrder.setUpdateTime(System.currentTimeMillis());
        updateOrder.setDistance(distanceExchangeOrderRsp.getDistance());
        updateOrder.setOrderSeq(distanceExchangeOrderRsp.getOrderSeq());
        updateOrder.setStatus(distanceExchangeOrderRsp.getOrderStatus());
        updateOrder.setPlaceBatteryName(distanceExchangeOrderRsp.getPlaceBatteryName());
        updateOrder.setTakeBatteryName(distanceExchangeOrderRsp.getTakeBatteryName());
        updateOrder.setPlaceCellNo(distanceExchangeOrderRsp.getPlaceCellNo());
        updateOrder.setTakeCellNo(distanceExchangeOrderRsp.getTakeCellNo());
        updateOrder.setDistance(distanceExchangeOrderRsp.getDistance());
        distanceExchangeOrderService.update(updateOrder);

        if (updateOrder.getStatus().equalsIgnoreCase(DistanceExchangeOrder.STATUS_PLACE_BATTERY_CHECK_SUCCESS)) {
            handlePlaceBatteryStatus(receiverMessage, distanceExchangeOrderRsp, distanceExchangeOrder, updateOrder);
        }

        if (updateOrder.getStatus().equalsIgnoreCase(DistanceExchangeOrder.STATUS_TAKE_BATTERY_SUCCESS)) {
            handleTakeBatteryStatus(receiverMessage, distanceExchangeOrderRsp, distanceExchangeOrder);
        }


        return true;
    }

    private void handleTakeBatteryStatus(ReceiverMessage receiverMessage, DistanceExchangeOrderRsp distanceExchangeOrderRsp, DistanceExchangeOrder distanceExchangeOrder) {
        UserInfo userInfo = userInfoService.queryByUid(distanceExchangeOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("DISTANCE EXCHANGE ERROR! not found userinfo! sessionId={},uid={}", receiverMessage.getSessionId(), distanceExchangeOrder.getUid());
            return;
        }

        FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
        franchiseeUserInfo.setUserInfoId(userInfo.getId());
        franchiseeUserInfo.setNowElectricityBatterySn(distanceExchangeOrderRsp.getTakeBatteryName());
        franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfoService.updateByUserInfoId(franchiseeUserInfo);

        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUid(distanceExchangeOrder.getUid());
        if (Objects.nonNull(oldElectricityBattery) && !Objects.equals(oldElectricityBattery.getSn(), distanceExchangeOrderRsp.getTakeBatteryName())) {
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(oldElectricityBattery.getId());
            newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_FREE);
            newElectricityBattery.setUid(null);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBatteryService.updateByOrder(newElectricityBattery);
        }

        //电池改为在用
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(distanceExchangeOrderRsp.getTakeBatteryName());
        ElectricityBattery newElectricityBattery = new ElectricityBattery();
        newElectricityBattery.setId(electricityBattery.getId());
        newElectricityBattery.setStatus(ElectricityBattery.LEASE_STATUS);
        newElectricityBattery.setElectricityCabinetId(null);
        newElectricityBattery.setUid(distanceExchangeOrder.getUid());
        newElectricityBattery.setUpdateTime(System.currentTimeMillis());
        electricityBatteryService.updateByOrder(newElectricityBattery);

        //删除柜机被锁缓存
        redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + distanceExchangeOrder.getEId());
    }

    private void handlePlaceBatteryStatus(ReceiverMessage receiverMessage, DistanceExchangeOrderRsp distanceExchangeOrderRsp, DistanceExchangeOrder distanceExchangeOrder, DistanceExchangeOrder updateOrder) {
        UserInfo userInfo = userInfoService.queryByUid(distanceExchangeOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("DISTANCE EXCHANGE ERROR! not found userinfo! sessionId={},uid={}", receiverMessage.getSessionId(), distanceExchangeOrder.getUid());
            return;
        }

        FranchiseeUserInfo oldFranchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
        if (Objects.isNull(oldFranchiseeUserInfo)) {
            log.error("DISTANCE EXCHANGE ERROR! franchiseeUserInfo is null! sessionId={},uid={}", receiverMessage.getSessionId(), userInfo.getUid());
            return;
        }

        //用户解绑旧电池 旧电池到底是哪块，不确定
        FranchiseeUserInfo updateFranchiseeUserInfo = new FranchiseeUserInfo();
        updateFranchiseeUserInfo.setId(oldFranchiseeUserInfo.getId());
        updateFranchiseeUserInfo.setNowElectricityBatterySn(null);
        updateFranchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfoService.update(updateFranchiseeUserInfo);

        //查看用户是否有绑定的电池,绑定电池和放入电池不一致则绑定电池处于游离态
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(distanceExchangeOrder.getUid());
        if (Objects.nonNull(electricityBattery) && !Objects.equals(electricityBattery.getSn(), distanceExchangeOrderRsp.getPlaceBatteryName())) {
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(electricityBattery.getId());
            newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_FREE);
            newElectricityBattery.setUid(null);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBatteryService.updateByOrder(newElectricityBattery);
        }


        //放入电池改为在仓
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySn(distanceExchangeOrderRsp.getPlaceBatteryName());
        if (Objects.nonNull(oldElectricityBattery)) {
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(distanceExchangeOrder.getEId());
            if(Objects.nonNull(electricityCabinet)){
                ElectricityBattery newElectricityBattery = new ElectricityBattery();
                newElectricityBattery.setId(oldElectricityBattery.getId());
                newElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
                newElectricityBattery.setElectricityCabinetId(electricityCabinet.getId());
                newElectricityBattery.setElectricityCabinetName(electricityCabinet.getName());
                newElectricityBattery.setUid(null);
                newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                electricityBatteryService.updateByOrder(newElectricityBattery);
            }

        }

        Integer takeCellNo = null;
        ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(distanceExchangeOrder.getEId(), String.valueOf(distanceExchangeOrder.getTakeCellNo()));
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(distanceExchangeOrder.getEId());

        if (Objects.nonNull(electricityCabinetBox) && Objects.equals(electricityCabinetBox.getStatus(), ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY)) {
            takeCellNo = distanceExchangeOrder.getTakeCellNo();
        } else {
            Pair<Boolean, ElectricityCabinetBox> usableBatteryCellNo = electricityCabinetService.findUsableBatteryCellNo(distanceExchangeOrder.getEId(), null, electricityCabinet.getFullyCharged());
            //没有可用的电池加入操作记录
            if (!usableBatteryCellNo.getLeft()) {
                updateOrder.setStatus(DistanceExchangeOrder.STATUS_EXCEPTION_ORDER);
                distanceExchangeOrderService.update(updateOrder);
                DistanceOrderOperHistory operHistory = DistanceOrderOperHistory.builder()
                        .createTime(System.currentTimeMillis())
                        .orderId(distanceExchangeOrder.getOrderId())
                        .type(DistanceExchangeOrder.TYPE_EXCHANGE)
                        .tenantId(electricityCabinet.getTenantId())
                        .msg("没有可用的满电电池")
                        .build();
                distanceOrderOperHistoryService.insert(operHistory);
                log.error("DISTANCE EXCHANGE ERROR! not found usable battery! sessionId={},", receiverMessage.getSessionId());
                return;
            }

            takeCellNo = Integer.parseInt(usableBatteryCellNo.getRight().getCellNo());
        }

        //发送命令
        // TODO: 2021/11/19 需要类型
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("takeCellNo", takeCellNo);
        dataMap.put("orderId", distanceExchangeOrder.getOrderId());
        dataMap.put("vBatteryType", null);
        dataMap.put("placeBatteryName", distanceExchangeOrderRsp.getPlaceBatteryName());
        dataMap.put("placeCellNo", distanceExchangeOrderRsp.getPlaceCellNo());
        dataMap.put("isInterruptOrder", false);


        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(ElectricityCabinetConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + distanceExchangeOrder.getOrderId())
                .data(dataMap)
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(HardwareCommand.DISTANCE_EXCHANGE_ORDER_TAKE).build();
        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
    }

}

@Data
class DistanceExchangeOrderRsp {
    //订单id
    public String orderId;
    //正确或者错误信息，
    public String msg;
    //订单状态
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

    private Double distance;
}