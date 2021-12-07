package com.xiliulou.electricity.handler;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.retrofilt.api.ApiReturnOrderRetrofitService;
import com.xiliulou.electricity.service.retrofilt.api.RetrofitThirdApiService;
import com.xiliulou.electricity.web.query.ApiReturnOrderCallQuery;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Objects;
import java.util.Optional;


/**
 * @author: eclair
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service
@Slf4j
public class NormalDistanceReturnHandlerIot extends AbstractIotMessageHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    DistanceReturnOrderService returnOrderService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;


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
        DistanceReturnBatteryOrderRsp distanceReturnBatteryOrderRsp = JsonUtil.fromJson(receiverMessage.getOriginContent(), DistanceReturnBatteryOrderRsp.class);
        if (Objects.isNull(distanceReturnBatteryOrderRsp)) {
            log.error("DISTANCE RETURN ERROR! parse rsp error! sessionId={}", receiverMessage.getSessionId());
            return false;
        }

        DistanceReturnOrder distanceReturnOrder = returnOrderService.queryByOrderId(distanceReturnBatteryOrderRsp.getOrderId());
        if (Objects.isNull(distanceReturnOrder)) {
            log.error("DISTANCE RETURN ERROR! not found order ! sessionId={},orderId={}", receiverMessage.getSessionId(), distanceReturnBatteryOrderRsp.getOrderId());
            return false;
        }


        if (distanceReturnBatteryOrderRsp.getOrderSeq() <= distanceReturnOrder.getOrderSeq()) {
            log.error("DISTANCE RETURN ERROR! order's seq high mns's seq ! sessionId={},orderId={},orderSeq={},mnsSeq={}", receiverMessage.getSessionId(), distanceReturnOrder.getOrderId(), distanceReturnOrder.getOrderSeq(), distanceReturnBatteryOrderRsp.getOrderSeq());
            return false;
        }


        if (distanceReturnBatteryOrderRsp.getOrderStatus().equalsIgnoreCase(DistanceRentOrder.STATUS_EXCEPTION_ORDER)) {
            redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + distanceReturnOrder.getEId());
        }

        DistanceReturnOrder updateOrder = new DistanceReturnOrder();
        updateOrder.setId(distanceReturnOrder.getId());
        updateOrder.setBatteryName(distanceReturnBatteryOrderRsp.getBatteryName());
        updateOrder.setUpdateTime(System.currentTimeMillis());
        updateOrder.setOrderSeq(distanceReturnBatteryOrderRsp.getOrderSeq());
        updateOrder.setStatus(distanceReturnBatteryOrderRsp.getOrderStatus());
        updateOrder.setDistance(distanceReturnBatteryOrderRsp.getDistance());
        returnOrderService.update(updateOrder);

        if (updateOrder.getStatus().equalsIgnoreCase(DistanceReturnOrder.STATUS_RETURN_BATTERY_CHECK_SUCCESS)) {
            handleBatteryStatus(receiverMessage, distanceReturnOrder, distanceReturnBatteryOrderRsp);
        }
        return true;
    }

    private void handleBatteryStatus(ReceiverMessage receiverMessage, DistanceReturnOrder distanceReturnOrder, DistanceReturnBatteryOrderRsp distanceReturnBatteryOrderRsp) {
        UserInfo userInfo = userInfoService.queryByUid(distanceReturnOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("DISTANCE RETURN ERROR! not found userInfo !sessionId={},uid={}", receiverMessage.getSessionId(), distanceReturnOrder.getUid());
            return;
        }

        //用户绑新电池
        FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
        franchiseeUserInfo.setUserInfoId(userInfo.getId());
        franchiseeUserInfo.setNowElectricityBatterySn(null);
        franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfo.setServiceStatus(FranchiseeUserInfo.STATUS_IS_DEPOSIT);
        franchiseeUserInfoService.updateByUserInfoId(franchiseeUserInfo);

        ElectricityBattery existsBattery = electricityBatteryService.queryByUid(distanceReturnOrder.getUid());
        if (Objects.nonNull(existsBattery) && !Objects.equals(existsBattery.getSn(), distanceReturnBatteryOrderRsp.getBatteryName())) {
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(existsBattery.getId());
            newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_FREE);
            newElectricityBattery.setUid(null);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBatteryService.updateByOrder(newElectricityBattery);
        }

        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySn(distanceReturnBatteryOrderRsp.getBatteryName());
        if (Objects.nonNull(oldElectricityBattery)) {
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(distanceReturnOrder.getEId());
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

        //删除柜机被锁缓存
        redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + distanceReturnOrder.getEId());


    }

}

@Data
class DistanceReturnBatteryOrderRsp {
    //订单id
    private String orderId;
    //错误消息
    private String msg;
    //订单状态
    private String orderStatus;
    //订单状态序号
    private Double orderSeq;
    //是否需要结束订单
    private Boolean isException;
    //创建时间
    private Long reportTime;

    private String batteryName;

    private Double distance;
}