package com.xiliulou.electricity.handler;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityCabinetOrderMapper;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.vo.OperateMsgVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
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
    @Autowired
    FranchiseeBindElectricityBatteryService franchiseeBindElectricityBatteryService;
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
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
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

        //生成订单
        ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
                .orderId(generateOrderId(electricityCabinet.getId(), offlineEleOrderVo.getNewCellNo(), user.getUid()))
                .uid(user.getUid())
                .phone(offlineEleOrderVo.getPhone())
                .electricityCabinetId(electricityCabinet.getId())
                .oldCellNo(Integer.valueOf(offlineEleOrderVo.getOldCellNo()))
                .orderSeq(null)
                .status(offlineEleOrderVo.getStatus())
                .source(OfflineEleOrderVo.ORDER_SOURCE_FOR_OFFLINE)
                .paymentMethod(oldFranchiseeUserInfo.getCardType())
                .createTime(offlineEleOrderVo.getStartTime())
                .updateTime(offlineEleOrderVo.getEndTime())
                .tenantId(electricityCabinet.getTenantId()).build();
        electricityCabinetOrderService.insertOrder(electricityCabinetOrder);

        //操作记录
        List<OperateMsgVo> operateMsgVoList = offlineEleOrderVo.getMsg();
        OffLineElectricityCabinetOrderOperHistory offLineElectricityCabinetOrderOperHistory = OffLineElectricityCabinetOrderOperHistory.builder()
                .orderId(electricityCabinetOrder.getOrderId())
                .type(OfflineEleOrderVo.ORDER_SOURCE_FOR_OFFLINE)
                .tenantId(electricityCabinet.getTenantId())
                .operateMsgVos(operateMsgVoList)
                .build();
        electricityCabinetOrderOperHistoryService.insertOffLineOperateHistory(offLineElectricityCabinetOrderOperHistory);

        if (offlineEleOrderVo.getIsProcessFail()) {
            log.error("OFFLINE EXCHANGE ERROR! exchange exception!orderId:{}", offlineEleOrderVo.getOrderId());
            senMsg(electricityCabinet, offlineEleOrderVo, user);
            return false;
        }

        //根据用户购买的套餐类型进行套餐扣减
        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(oldFranchiseeUserInfo.getCardId());
        if (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
            //扣除月卡
            franchiseeUserInfoService.minCountForOffLineEle(oldFranchiseeUserInfo.getId());
        }

        //用户解绑旧电池，绑定新电池
        FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
        franchiseeUserInfo.setId(oldFranchiseeUserInfo.getId());
        franchiseeUserInfo.setNowElectricityBatterySn(offlineEleOrderVo.getNewElectricityBatterySn());
        franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfoService.update(franchiseeUserInfo);

        //用户绑定电池和还入电池是否一致，不一致绑定的电池更新为游离态
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
        if (Objects.nonNull(electricityBattery)) {
            if (!Objects.equals(electricityBattery.getSn(), offlineEleOrderVo.getOldElectricityBatterySn())) {
                ElectricityBattery newElectricityBattery = new ElectricityBattery();
                newElectricityBattery.setId(electricityBattery.getId());
                newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_FREE);
                newElectricityBattery.setUid(null);
                newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                newElectricityBattery.setElectricityCabinetId(null);
                newElectricityBattery.setElectricityCabinetName(null);
                electricityBatteryService.updateByOrder(newElectricityBattery);
            }
        }

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
        if (Objects.isNull(newElectricityBattery)) {
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

        senMsg(electricityCabinet, offlineEleOrderVo, user);
        return true;
    }

    private void senMsg(ElectricityCabinet electricityCabinet, OfflineEleOrderVo offlineEleOrderVo, User user) {
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("order_id", offlineEleOrderVo.getOrderId());
        dataMap.put("status", offlineEleOrderVo.getStatus());

        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(ElectricityCabinetConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + user.getUid() + "_" + offlineEleOrderVo.getOrderId())
                .data(dataMap)
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(HardwareCommand.OFFLINE_ELE_EXCHANGE_ORDER_MANAGE_SUCCESS).build();
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        if (!sendResult.getLeft()) {
            log.error("OFFLINE EXCHANGE ERROR! send command error! orderId:{}", offlineEleOrderVo.getOrderId());
        }
    }

    private String generateOrderId(Integer id, String cellNo, Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + id +
                cellNo + uid;
    }
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
    protected static final Integer ORDER_SOURCE_FOR_OFFLINE = 3;

}
