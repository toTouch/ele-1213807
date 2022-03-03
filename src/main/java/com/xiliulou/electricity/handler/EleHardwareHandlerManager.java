package com.xiliulou.electricity.handler;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.feishu.config.FeishuConfig;
import com.xiliulou.feishu.entity.query.FeishuBotSendMsgQuery;
import com.xiliulou.feishu.entity.query.msg.FeishuMsgPostQuery;
import com.xiliulou.feishu.entity.query.msg.FeishuMsgPostSubQuery;
import com.xiliulou.feishu.entity.query.msg.FeishuMsgPostTextQuery;
import com.xiliulou.feishu.entity.query.msg.FeishuMsgPostTypeQuery;
import com.xiliulou.feishu.entity.rsp.FeishuTokenRsp;
import com.xiliulou.feishu.exception.FeishuException;
import com.xiliulou.feishu.service.FeishuMsgService;
import com.xiliulou.feishu.service.FeishuTokenService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.mns.HardwareHandlerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author: lxc
 * @Date: 2020/12/28 13:26
 * @Description:
 */
@Service
@Slf4j
public class EleHardwareHandlerManager extends HardwareHandlerManager {
    @Autowired
    NormalEleOrderHandlerIot normalEleOrderHandlerIot;
    @Autowired
    NormalEleBatteryHandlerIot normalEleBatteryHandlerIot;
    @Autowired
    NormalEleCellHandlerIot normalEleCellHandlerIot;
    @Autowired
    NormalEleExchangeHandlerIot normalEleExchangeHandlerIot;
    @Autowired
    NormalEleOperateHandlerIot normalEleOperateHandlerIot;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    NormalPowerConsumptionHandlerIot normalPowerConsumptionHandlerIot;
    @Autowired
    RedisService redisService;
    @Autowired
    NormalWarnHandlerIot normalWarnHandlerIot;
    @Autowired
    NormalOtherConfigHandlerIot normalOtherConfigHandlerIot;
    @Autowired
    NormalEleOrderOperateHandlerIot normalEleOrderOperateHandlerIot;

    @Autowired
    NormalApiRentHandlerIot normalApiRentHandlerIot;
    @Autowired
    NormalApiExchangeHandlerIot normalApiExchangeHandlerIot;
    @Autowired
    NormalApiReturnHandlerIot normalApiReturnHandlerIot;
    @Autowired
    IcIdCommandIotHandler icIdCommandIotHandler;
    @Autowired
    TenantService tenantSerivce;
    @Autowired
    FeishuConfig feishuConfig;
    @Autowired
    FeishuMsgService feishuMsgService;
    @Autowired
    FeishuTokenService feishuTokenService;

    ExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("eleHardwareHandlerExecutor", 2, "ELE_HARDWARE_HANDLER_EXECUTOR");

    public Pair<Boolean, String> chooseCommandHandlerProcessSend(HardwareCommandQuery hardwareCommandQuery) {
        if (hardwareCommandQuery.getCommand().contains("cell") || hardwareCommandQuery.getCommand().contains("order")
                || hardwareCommandQuery.getCommand().contains("cupboard")
                || hardwareCommandQuery.getCommand().contains("rent")
                || hardwareCommandQuery.getCommand().contains("return")
                || hardwareCommandQuery.getCommand().equals(HardwareCommand.EXCHANGE_CABINET)
                || hardwareCommandQuery.getCommand().equals(HardwareCommand.ELE_COMMAND_OPERATE)
                || hardwareCommandQuery.getCommand().equals(HardwareCommand.ELE_COMMAND_CELL_CONFIG)
                || hardwareCommandQuery.getCommand().equals(HardwareCommand.ELE_COMMAND_POWER_CONSUMPTION)
                || hardwareCommandQuery.getCommand().equals(HardwareCommand.ELE_COMMAND_OTHER_CONFIG)
                || hardwareCommandQuery.getCommand().equals(HardwareCommand.ELE_COMMAND_BATTERY_SYNC_INFO)
                || hardwareCommandQuery.getCommand().equals(HardwareCommand.ELE_COMMAND_CUPBOARD_RESTART)
                || (hardwareCommandQuery.getCommand().equals(HardwareCommand.ELE_COMMAND_UNLOCK_CABINET))
                || (hardwareCommandQuery.getCommand().equals(HardwareCommand.API_EXCHANGE_ORDER))
                || (hardwareCommandQuery.getCommand().equals(HardwareCommand.ELE_COMMAND_OTHER_CONFIG_READ))
                || (hardwareCommandQuery.getCommand().equals(HardwareCommand.GET_CARD_NUM_ICCID))) {
            return normalEleOrderHandlerIot.handleSendHardwareCommand(hardwareCommandQuery);
        } else {
            log.error("command not support handle,command:{}", hardwareCommandQuery.getCommand());
            return Pair.of(false, "");
        }
    }

    @Override
    public boolean chooseCommandHandlerProcessReceiveMessage(ReceiverMessage receiverMessage) {
        //电柜在线状态
        if (Objects.isNull(receiverMessage.getType())) {
            executorService.execute(() -> {
                if (!StrUtil.isNotEmpty(receiverMessage.getStatus())) {
                    return;
                }
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
                if (Objects.isNull(electricityCabinet)) {
                    log.error("ELE ERROR! no product and device ,p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
                    return;
                }
                //在线状态修改
                ElectricityCabinet newElectricityCabinet = new ElectricityCabinet();
                newElectricityCabinet.setId(electricityCabinet.getId());
                Integer status = 1;
                if (Objects.equals(receiverMessage.getStatus(), "online")) {
                    status = 0;
                }
                newElectricityCabinet.setOnlineStatus(status);
                if (electricityCabinetService.update(newElectricityCabinet) > 0) {
                    redisService.delete(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET + newElectricityCabinet.getId());
                    redisService.delete(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_DEVICE + electricityCabinet.getProductKey() + electricityCabinet.getDeviceName() + electricityCabinet.getTenantId());
                }
                log.error("type is null,{}", receiverMessage.getOriginContent());

                feishuSendMsg(electricityCabinet, receiverMessage.getStatus());
            });

            return false;
        }
        if (receiverMessage.getType().contains("order_operate")) {
            return normalEleOrderOperateHandlerIot.receiveMessageProcess(receiverMessage);
        } else if (Objects.equals(receiverMessage.getType(), HardwareCommand.API_RETURN_ORDER_RSP)) {
            return normalApiReturnHandlerIot.receiveMessageProcess(receiverMessage);
        } else if (Objects.equals(receiverMessage.getType(), HardwareCommand.API_EXCHANGE_ORDER_RSP)) {
            return normalApiExchangeHandlerIot.receiveMessageProcess(receiverMessage);
        } else if (Objects.equals(receiverMessage.getType(), HardwareCommand.API_RENT_ORDER_RSP)) {
            return normalApiRentHandlerIot.receiveMessageProcess(receiverMessage);
        } else if (receiverMessage.getType().contains("order")) {
            return normalEleOrderHandlerIot.receiveMessageProcess(receiverMessage);
        } else if (receiverMessage.getType().contains("operate")) {
            return normalEleOperateHandlerIot.receiveMessageProcess(receiverMessage);
        } else if (receiverMessage.getType().contains("cell")) {
            return normalEleCellHandlerIot.receiveMessageProcess(receiverMessage);
        } else if (receiverMessage.getType().contains("battery")) {
            return normalEleBatteryHandlerIot.receiveMessageProcess(receiverMessage);
        } else if (Objects.equals(receiverMessage.getType(), HardwareCommand.EXCHANGE_CABINET)) {
            return normalEleExchangeHandlerIot.receiveMessageProcess(receiverMessage);
        } else if (Objects.equals(receiverMessage.getType(), HardwareCommand.ELE_COMMAND_POWER_CONSUMPTION_RSP)) {
            return normalPowerConsumptionHandlerIot.receiveMessageProcess(receiverMessage);
        } else if (Objects.equals(receiverMessage.getType(), HardwareCommand.ELE_COMMAND_WARN_MSG_RSP)) {
            return normalWarnHandlerIot.receiveMessageProcess(receiverMessage);
        } else if (Objects.equals(receiverMessage.getType(), HardwareCommand.ELE_COMMAND_OTHER_CONFIG_RSP)) {
            return normalOtherConfigHandlerIot.receiveMessageProcess(receiverMessage);
        } else if (Objects.equals(receiverMessage.getType(), HardwareCommand.ELE_COMMAND_ICCID_GET_RSP)) {
            return icIdCommandIotHandler.receiveMessageProcess(receiverMessage);
        }else {
            log.error("command not support handle,command:{}", receiverMessage.getType());
            return false;
        }
    }

    private void feishuSendMsg(ElectricityCabinet electricityCabinet, String onlineStatus){
        Tenant tenantEntity = tenantSerivce.queryByIdFromCache(electricityCabinet.getTenantId());
        if(Objects.isNull(tenantEntity)){
            log.error("FEI SHU ERROR! tenant is empty error! cid={},tid={}", electricityCabinet.getId(), electricityCabinet.getTenantId());
            return;
        }

        String token = null;
        try{
            token = this.acquireAccessToken();
        }catch (FeishuException e){
            log.error("FEI SHU ERROR! FAILED TO GET TOKEN",e);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

        FeishuMsgPostTextQuery query0 = new FeishuMsgPostTextQuery();
        query0.setText("产品系列：换电柜");

        FeishuMsgPostTextQuery query1 = new FeishuMsgPostTextQuery();
        query1.setText("柜机名称：" + electricityCabinet.getName());

        FeishuMsgPostTextQuery query2 = new FeishuMsgPostTextQuery();
        query2.setText("租户名称：" + tenantEntity.getName());

        FeishuMsgPostTextQuery query3 = new FeishuMsgPostTextQuery();
        query3.setText("当前状态：" +  getOnlineStatus(onlineStatus));

        FeishuMsgPostTextQuery query4 = new FeishuMsgPostTextQuery();
        query4.setText("当前时间：" +  sdf.format(new Date()));

        List<FeishuMsgPostTypeQuery> feishuMsgPostTypeLine0 = Lists.newArrayList(query0);
        List<FeishuMsgPostTypeQuery> feishuMsgPostTypeLine1 = Lists.newArrayList(query1);
        List<FeishuMsgPostTypeQuery> feishuMsgPostTypeLine2 = Lists.newArrayList(query2);
        List<FeishuMsgPostTypeQuery> feishuMsgPostTypeLine3 = Lists.newArrayList(query3);
        List<FeishuMsgPostTypeQuery> feishuMsgPostTypeLine4 = Lists.newArrayList(query4);

        FeishuMsgPostSubQuery feishuMsgPostSubQuery = new FeishuMsgPostSubQuery();
        feishuMsgPostSubQuery.setTitle("设备上下线通知");
        feishuMsgPostSubQuery.setContent(Arrays.asList(feishuMsgPostTypeLine0,
                feishuMsgPostTypeLine1, feishuMsgPostTypeLine2, feishuMsgPostTypeLine3,feishuMsgPostTypeLine4));

        FeishuMsgPostQuery feishuMsgPostQuery = new FeishuMsgPostQuery();
        feishuMsgPostQuery.setZhCn(feishuMsgPostSubQuery);

        FeishuBotSendMsgQuery botSendMsgQuery = new FeishuBotSendMsgQuery();
        botSendMsgQuery.setReceiveIdType(FeishuBotSendMsgQuery.TYPE_CHAT_ID);
        botSendMsgQuery.setMsgType(FeishuBotSendMsgQuery.MSG_POST);
        botSendMsgQuery.setContent(JsonUtil.toJson(feishuMsgPostQuery));

        List<String> receiveIds = feishuConfig.getReceiveIds();
        if(!CollectionUtils.isEmpty(receiveIds)){
            for(String receiveId : receiveIds){
                botSendMsgQuery.setReceiveId(receiveId);
                try {
                    feishuMsgService.sendBotMsg(botSendMsgQuery ,token);
                } catch (FeishuException e) {
                    log.error("FEI SHU ERROR! FEI SHU SEND BOT MSG ERROR!", e);
                }
            }
        }

        return;
    }

    private String acquireAccessToken() throws FeishuException {
        String token = redisService.get(ElectricityCabinetConstant.CACHE_FEISHU_ACCESS_TOKEN);

        if(StringUtils.isBlank(token)){
            FeishuTokenRsp feishuTokenRsp =  feishuTokenService.acquireAccessToken();
            token = feishuTokenRsp.getTenantAccessToken();
            redisService.set(ElectricityCabinetConstant.CACHE_FEISHU_ACCESS_TOKEN,
                    token,1800L, TimeUnit.SECONDS);
        }

        return token;
    }

    private String getOnlineStatus(String status){
        String str = "";
        switch (status){
            case "online":
                str = "上线"; break;
            case "offline":
                str = "下线"; break;
        }
        return str;
    }
}
