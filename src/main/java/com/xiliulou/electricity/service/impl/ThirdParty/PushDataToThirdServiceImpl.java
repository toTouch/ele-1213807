package com.xiliulou.electricity.service.impl.ThirdParty;

import com.xiliulou.electricity.constant.thirdParty.ThirdPartyMsgContentConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.enums.thirdParth.ThirdPartyDataType;
import com.xiliulou.electricity.event.ThirdPartyEvent;
import com.xiliulou.electricity.event.publish.ThirdPartyPublish;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.thirdParty.MeiTuanRiderMallConfigService;
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
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private MeiTuanRiderMallConfigService meiTuanRiderMallConfigService;
    
    @Override
    public void asyncPushExchangeAndUserAndBatteryToThird(String traceId, Integer tenantId, String orderId, Integer orderType, Long uid) {
        this.asyncPushExchangeToThird(traceId, tenantId, orderId, orderType);
        this.asyncPushBatteryToThird(traceId, tenantId, orderId, orderType);
        this.asyncPushUserToThird(traceId, tenantId, orderId, orderType);
    }
    
    @Override
    public void asyncPushUserAndBatteryToThird(String traceId, Integer tenantId, String orderId, Integer orderType, Long uid) {
        this.asyncPushBatteryToThird(traceId, tenantId, orderId, orderType);
        this.asyncPushUserToThird(traceId, tenantId, orderId, orderType);
    }
    
    @Override
    public void asyncPushExchangeToThird(String traceId, Integer tenantId, String orderId, Integer orderType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataType.PUSH_USER_EXCHANGE_RECORD)
                .addContext(ThirdPartyMsgContentConstant.ORDER_ID, orderId).addContext(ThirdPartyMsgContentConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushUserToThird(String traceId, Integer tenantId, String orderId, Integer orderType) {
        thirdPartyPublish.publish(
                ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataType.PUSH_USER_INFO).addContext(ThirdPartyMsgContentConstant.ORDER_ID, orderId)
                        .addContext(ThirdPartyMsgContentConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushBatteryToThird(String traceId, Integer tenantId, String orderId, Integer orderType) {
        thirdPartyPublish.publish(
                ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataType.PUSH_USER_BATTERY).addContext(ThirdPartyMsgContentConstant.ORDER_ID, orderId)
                        .addContext(ThirdPartyMsgContentConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushCabinetToThird(String traceId, Integer tenantId, Long eid) {
        if (isMtCabinet(eid.intValue())) {
            thirdPartyPublish.publish(
                    ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataType.PUSH_ELE_CABINET).addContext(ThirdPartyMsgContentConstant.EID, eid)
                            .build());
        }
    }
    
    @Override
    public void asyncPushCabinetStatusToThird(String traceId, Integer tenantId, Long eid, Integer delayLevel) {
        if (isMtCabinet(eid.intValue())) {
            thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataType.PUSH_ELE_CABINET).delayLevel(delayLevel)
                    .addContext(ThirdPartyMsgContentConstant.EID, eid).build());
        }
    }
    
    @Override
    public void asyncPushUserMemberCardToThird(String traceId, Integer tenantId, Long uid, String mtOrderId, Integer orderType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataType.PUSH_USER_BATTERY_MEMBER_CARD)
                .addContext(ThirdPartyMsgContentConstant.UID, uid).addContext(ThirdPartyMsgContentConstant.ORDER_ID, mtOrderId)
                .addContext(ThirdPartyMsgContentConstant.ORDER_TYPE, orderType).build());
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
