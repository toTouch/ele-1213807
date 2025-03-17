package com.xiliulou.electricity.service.impl.ThirdParty;

import com.xiliulou.electricity.constant.thirdParty.ThirdPartyMqContentConstant;
import com.xiliulou.electricity.enums.thirdParty.ThirdPartyDataTypeEnum;
import com.xiliulou.electricity.event.ThirdPartyEvent;
import com.xiliulou.electricity.event.publish.ThirdPartyPublish;
import com.xiliulou.electricity.service.thirdParty.PushDataToThirdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
    
    @Override
    public void asyncPushExchangeOrder(String traceId, Integer tenantId, String orderId, String orderType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataTypeEnum.PUSH_BATTERY_EXCHANGE_ORDER)
                .addContext(ThirdPartyMqContentConstant.ORDER_ID, orderId).addContext(ThirdPartyMqContentConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushRentOrder(String traceId, Integer tenantId, String orderId, String orderType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataTypeEnum.PUSH_BATTERY_RENT_ORDER)
                .addContext(ThirdPartyMqContentConstant.ORDER_ID, orderId).addContext(ThirdPartyMqContentConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushCabinet(String traceId, Integer tenantId, Long eid, String operateType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataTypeEnum.PUSH_ELE_CABINET).operateType(operateType)
                .addContext(ThirdPartyMqContentConstant.EID, eid).build());
    }
    
    @Override
    public void asyncPushCabinetList(String traceId, Integer tenantId, List<Long> eidList, String operateType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataTypeEnum.PUSH_ELE_CABINET).operateType(operateType)
                .addContext(ThirdPartyMqContentConstant.EID_LIST, eidList).build());
    }
    
    @Override
    public void asyncPushCabinetStatus(String traceId, Integer tenantId, Long eid, Integer delayLevel, String operateType) {
        thirdPartyPublish.publish(
                ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataTypeEnum.PUSH_ELE_CABINET).delayLevel(delayLevel).operateType(operateType)
                        .addContext(ThirdPartyMqContentConstant.EID, eid).build());
    }
    
    @Override
    public void asyncPushUserMemberCardOrder(String traceId, Integer tenantId, Long uid, String mtOrderId, String orderType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataTypeEnum.PUSH_BATTERY_MEMBER_CARD_ORDER)
                .addContext(ThirdPartyMqContentConstant.UID, uid).addContext(ThirdPartyMqContentConstant.ORDER_ID, mtOrderId)
                .addContext(ThirdPartyMqContentConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushStore(String traceId, Integer tenantId, Long storeId, String operateType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataTypeEnum.PUSH_STORE).operateType(operateType)
                .addContext(ThirdPartyMqContentConstant.STORE_ID, storeId).build());
    }
    
    @Override
    public void asyncPushBattery(String traceId, Integer tenantId, String sn, String operateType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataTypeEnum.PUSH_BATTERY).operateType(operateType)
                .addContext(ThirdPartyMqContentConstant.BATTERY_SN, sn).build());
    }
    
    @Override
    public void asyncPushBatteryList(String traceId, Integer tenantId, List<String> snList, String operateType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataTypeEnum.PUSH_BATTERY).operateType(operateType)
                .addContext(ThirdPartyMqContentConstant.BATTERY_SN_LIST, snList).build());
    }
    
    @Override
    public void asyncPushUserInfo(String traceId, Integer tenantId, Long uid, String operateType) {
        thirdPartyPublish.publish(ThirdPartyEvent.builder().traceId(traceId).tenantId(tenantId).type(ThirdPartyDataTypeEnum.PUSH_USER).operateType(operateType)
                .addContext(ThirdPartyMqContentConstant.UID, uid).build());
    }
    
}
