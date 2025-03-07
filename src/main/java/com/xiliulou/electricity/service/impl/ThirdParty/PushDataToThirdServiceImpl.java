package com.xiliulou.electricity.service.impl.ThirdParty;

import com.xiliulou.electricity.constant.thirdParty.ThirdPartyMsgTypeConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.enums.thirdParth.ThirdPartyDataType;
import com.xiliulou.electricity.event.ThirdPartyEvent;
import com.xiliulou.electricity.event.publish.ThirdPartyPublish;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.thirdParty.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.service.thirdParty.MeiTuanRiderMallOrderService;
import com.xiliulou.electricity.service.thirdParty.PushDataToThirdService;
import com.xiliulou.electricity.thirdparty.MeiTuanRiderMallConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 推送数据给第三方
 * @date 2024/10/12 09:18:27
 */
@Slf4j
@Service
public class PushDataToThirdServiceImpl implements PushDataToThirdService {
    
    @Resource
    private ThirdPartyPublish thirdPartyPublish;
    
    @Resource
    private MeiTuanRiderMallOrderService meiTuanRiderMallOrderService;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private MeiTuanRiderMallConfigService meiTuanRiderMallConfigService;
    
    @Override
    public void asyncPushExchangeAndUserAndBatteryToThird(Integer channel, String traceId, Integer tenantId, String orderId, Integer orderType, Long uid) {
        Boolean mtOrder = meiTuanRiderMallOrderService.isMtOrder(uid);
        // 判断使用的订单是否美团订单
        if (mtOrder) {
            this.asyncPushExchangeToThird(channel, traceId, tenantId, orderId, orderType);
            this.asyncPushBatteryToThird(channel, traceId, tenantId, orderId, orderType);
            this.asyncPushUserToThird(channel, traceId, tenantId, orderId, orderType);
        }
    }
    
    @Override
    public void asyncPushUserAndBatteryToThird(Integer channel, String traceId, Integer tenantId, String orderId, Integer orderType, Long uid) {
        Boolean mtOrder = meiTuanRiderMallOrderService.isMtOrder(uid);
        // 判断使用的订单是否美团订单
        if (mtOrder) {
            this.asyncPushBatteryToThird(channel, traceId, tenantId, orderId, orderType);
            this.asyncPushUserToThird(channel, traceId, tenantId, orderId, orderType);
        }
    }
    
    @Override
    public void asyncPushExchangeToThird(Integer channel, String traceId, Integer tenantId, String orderId, Integer orderType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).channel(channel).type(ThirdPartyDataType.PUSH_USER_EXCHANGE_RECORD)
                .addContext(ThirdPartyMsgTypeConstant.ORDER_ID, orderId).addContext(ThirdPartyMsgTypeConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushUserToThird(Integer channel, String traceId, Integer tenantId, String orderId, Integer orderType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).channel(channel).type(ThirdPartyDataType.PUSH_USER_INFO)
                .addContext(ThirdPartyMsgTypeConstant.ORDER_ID, orderId).addContext(ThirdPartyMsgTypeConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushBatteryToThird(Integer channel, String traceId, Integer tenantId, String orderId, Integer orderType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).channel(channel).type(ThirdPartyDataType.PUSH_USER_BATTERY)
                .addContext(ThirdPartyMsgTypeConstant.ORDER_ID, orderId).addContext(ThirdPartyMsgTypeConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushCabinetToThird(Integer channel, String traceId, Integer tenantId, Long eid) {
        if (isMtCabinet(eid.intValue())) {
            thirdPartyPublish.publish(
                    ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).channel(channel).type(ThirdPartyDataType.PUSH_ELE_CABINET).addContext(ThirdPartyMsgTypeConstant.EID, eid)
                            .build());
        }
    }
    
    @Override
    public void asyncPushCabinetStatusToThird(Integer channel, String traceId, Integer tenantId, Long eid, Integer delayLevel) {
        if (isMtCabinet(eid.intValue())) {
            thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).channel(channel).type(ThirdPartyDataType.PUSH_ELE_CABINET).delayLevel(delayLevel)
                    .addContext(ThirdPartyMsgTypeConstant.EID, eid).build());
        }
    }
    
    @Override
    public void asyncPushUserMemberCardToThird(Integer channel, String traceId, Integer tenantId, Long uid, String mtOrderId, Integer orderType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).channel(channel).type(ThirdPartyDataType.PUSH_USER_BATTERY_MEMBER_CARD)
                .addContext(ThirdPartyMsgTypeConstant.UID, uid).addContext(ThirdPartyMsgTypeConstant.ORDER_ID, mtOrderId).addContext(ThirdPartyMsgTypeConstant.ORDER_TYPE, orderType).build());
    }
    
    private Boolean isMtCabinet(Integer eid) {
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(eid);
        if (Objects.isNull(electricityCabinet)) {
            return false;
        }
        
        MeiTuanRiderMallConfig meiTuanRiderMallConfig = meiTuanRiderMallConfigService.checkEnableMeiTuanRiderMall(electricityCabinet.getTenantId());
        if (Objects.isNull(meiTuanRiderMallConfig)) {
            log.warn("The tenant meiTuanConfig switch off, tenantId={}", electricityCabinet.getTenantId());
            return false;
        }
        
        return true;
        
    }
}
