package com.xiliulou.electricity.handler;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
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

import java.util.Objects;

/**
 * @author: HRP
 * @Date: 2022/03/03 15:22
 * @Description: 离线换电
 */
@Service
@Slf4j
public class NormalOffLineEleExchangeHandlerIot extends AbstractIotMessageHandler {
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    UserService userService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    WechatTemplateNotificationConfig wechatTemplateNotificationConfig;

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
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("OFFLINE EXCHANGE NO sessionId,{}", receiverMessage.getOriginContent());
            return false;
        }

        OfflineEleOrderVo offlineEleOrderVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), OfflineEleOrderVo.class);

        //根据三元组获取柜子信息
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getProductKey());
        if (Objects.isNull(electricityCabinet)) {
            log.error("OFFLINE EXCHANGE ERROR! no product and device ,p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            return false;
        }

        //查找用户
        User user = userService.queryByUserPhone(offlineEleOrderVo.getPhone(), User.TYPE_USER_NORMAL_WX_PRO, electricityCabinet.getTenantId());
        if (Objects.isNull(user)) {
            log.error("OFFLINE EXCHANGE ERROR! not found user! userId:{}", offlineEleOrderVo.getPhone());
            return false;
        }

        UserInfo userInfo = userInfoService.queryByUid(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("OFFLINE EXCHANGE ERROR! userInfo is null! userId:{}", offlineEleOrderVo.getPhone());
            return false;
        }

        //查询用户绑定押金列表
        FranchiseeUserInfo oldFranchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
        if (Objects.isNull(oldFranchiseeUserInfo)) {
            log.error("OFFLINE EXCHANGE ERROR! franchiseeUserInfo is null!userId:{}", user.getUid());
            return false;
        }

        //用户解绑旧电池，绑定新电池
        FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
        franchiseeUserInfo.setId(oldFranchiseeUserInfo.getId());
        franchiseeUserInfo.setNowElectricityBatterySn(offlineEleOrderVo.getNewElectricityBatterySn());
        franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfoService.update(franchiseeUserInfo);

        //更新旧电池为在仓
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySn(offlineEleOrderVo.getOldElectricityBatterySn());
        if (Objects.isNull(oldElectricityBattery)) {
            log.error("OFFLINE EXCHANGE ERROR! electricityBattery is null! BatterySn:{}", offlineEleOrderVo.getOldElectricityBatterySn());
            return false;
        }
        ElectricityBattery InWarehouseElectricityBattery = new ElectricityBattery();
        InWarehouseElectricityBattery.setId(oldElectricityBattery.getId());
        InWarehouseElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
        InWarehouseElectricityBattery.setElectricityCabinetId(electricityCabinet.getId());
        InWarehouseElectricityBattery.setElectricityCabinetName(electricityCabinet.getName());
        InWarehouseElectricityBattery.setUid(null);
        InWarehouseElectricityBattery.setUpdateTime(System.currentTimeMillis());
        InWarehouseElectricityBattery.setBorrowExpireTime(null);
        electricityBatteryService.updateByOrder(InWarehouseElectricityBattery);

        //更新新电池为在用
        ElectricityBattery newElectricityBattery = electricityBatteryService.queryBySn(offlineEleOrderVo.getNewElectricityBatterySn());
        if (Objects.isNull(oldElectricityBattery)) {
            log.error("OFFLINE EXCHANGE ERROR! electricityBattery is null! BatterySn:{}", offlineEleOrderVo.getNewElectricityBatterySn());
            return false;
        }
        ElectricityBattery UsingElectricityBattery = new ElectricityBattery();
        UsingElectricityBattery.setId(newElectricityBattery.getId());
        UsingElectricityBattery.setStatus(ElectricityBattery.LEASE_STATUS);
        UsingElectricityBattery.setElectricityCabinetId(null);
        UsingElectricityBattery.setElectricityCabinetName(null);
        UsingElectricityBattery.setUid(newElectricityBattery.getUid());
        UsingElectricityBattery.setUpdateTime(System.currentTimeMillis());
        UsingElectricityBattery.setBorrowExpireTime(Integer.parseInt(wechatTemplateNotificationConfig.getExpirationTime()) * 3600000 + System.currentTimeMillis());
        electricityBatteryService.updateByOrder(UsingElectricityBattery);

        return true;
    }
}

@Data
class OfflineEleOrderVo {

    /**
     * 换电柜旧仓门号
     */
    private Integer oldCellNo;

    /**
     * 换电柜新仓门号
     */
    private Integer newCellNo;

    /**
     * 旧电池编号
     */
    private String oldElectricityBatterySn;

    /**
     * 新电池编号
     */
    private String newElectricityBatterySn;

    /**
     * 订单状态序列号
     */
    private Double orderSeq;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 用户手机号
     */
    private String phone;


}
