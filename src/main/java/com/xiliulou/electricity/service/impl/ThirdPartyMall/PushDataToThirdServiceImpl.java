package com.xiliulou.electricity.service.impl.ThirdPartyMall;

import com.xiliulou.electricity.constant.thirdPartyMallConstant.MeiTuanRiderMallConstant;
import com.xiliulou.electricity.enums.thirdParthMall.ThirdPartyMallDataType;
import com.xiliulou.electricity.event.ThirdPartyMallEvent;
import com.xiliulou.electricity.event.publish.ThirdPartyMallPublish;
import com.xiliulou.electricity.service.thirdPartyMall.MeiTuanRiderMallOrderService;
import com.xiliulou.electricity.service.thirdPartyMall.PushDataToThirdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HeYafeng
 * @description 推送数据给第三方
 * @date 2024/10/12 09:18:27
 */
@Slf4j
@Service
public class PushDataToThirdServiceImpl implements PushDataToThirdService {
    
    @Resource
    private ThirdPartyMallPublish thirdPartyMallPublish;
    
    @Resource
    private MeiTuanRiderMallOrderService meiTuanRiderMallOrderService;
    
    @Override
    public void asyncPushExchangeAndUserAndBatteryToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType, Long uid) {
        log.info("asyncPushExchangeAndUserAndBatteryToThird params: mallType={}, traceId={}, tenantId={}, orderId={}, orderType={}, uid={}", mallType, traceId, tenantId, orderId,
                orderType, uid);
        
        Boolean mtOrder = meiTuanRiderMallOrderService.isMtOrder(uid, orderId, orderType);
        // 判断使用的订单是否美团订单
        if (mtOrder) {
            this.asyncPushExchangeToThird(mallType, traceId, tenantId, orderId, orderType);
            this.asyncPushBatteryToThird(mallType, traceId, tenantId, orderId, orderType);
            this.asyncPushUserToThird(mallType, traceId, tenantId, orderId, orderType);
        }
    }
    
    @Override
    public void asyncPushUserAndBatteryToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType, Long uid) {
        log.info("asyncPushUserAndBatteryToThird params: mallType={}, traceId={}, tenantId={}, orderId={}, orderType={}, uid={}", mallType, traceId, tenantId, orderId, orderType,
                uid);
        
        Boolean mtOrder = meiTuanRiderMallOrderService.isMtOrder(uid, orderId, orderType);
        // 判断使用的订单是否美团订单
        if (mtOrder) {
            this.asyncPushBatteryToThird(mallType, traceId, tenantId, orderId, orderType);
            this.asyncPushUserToThird(mallType, traceId, tenantId, orderId, orderType);
        }
    }
    
    @Override
    public void asyncPushExchangeToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType) {
        thirdPartyMallPublish.publish(ThirdPartyMallEvent.builder(this).traceId(traceId).tenantId(tenantId).mall(mallType).type(ThirdPartyMallDataType.PUSH_USER_EXCHANGE_RECORD)
                .addContext(MeiTuanRiderMallConstant.ORDER_ID, orderId).addContext(MeiTuanRiderMallConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushUserToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType) {
        thirdPartyMallPublish.publish(ThirdPartyMallEvent.builder(this).traceId(traceId).tenantId(tenantId).mall(mallType).type(ThirdPartyMallDataType.PUSH_USER_INFO)
                .addContext(MeiTuanRiderMallConstant.ORDER_ID, orderId).addContext(MeiTuanRiderMallConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushBatteryToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType) {
        thirdPartyMallPublish.publish(ThirdPartyMallEvent.builder(this).traceId(traceId).tenantId(tenantId).mall(mallType).type(ThirdPartyMallDataType.PUSH_USER_BATTERY)
                .addContext(MeiTuanRiderMallConstant.ORDER_ID, orderId).addContext(MeiTuanRiderMallConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushCabinetToThird(Integer mallType, String traceId, Integer tenantId, Long eid) {
        thirdPartyMallPublish.publish(ThirdPartyMallEvent.builder(this).traceId(traceId).tenantId(tenantId).mall(mallType).type(ThirdPartyMallDataType.PUSH_ELE_CABINET)
                .addContext(MeiTuanRiderMallConstant.EID, eid).build());
    }
    
    @Override
    public void asyncPushCabinetStatusToThird(Integer mallType, String traceId, Integer tenantId, Long eid, Integer delayLevel) {
        thirdPartyMallPublish.publish(
                ThirdPartyMallEvent.builder(this).traceId(traceId).tenantId(tenantId).mall(mallType).type(ThirdPartyMallDataType.PUSH_ELE_CABINET).delayLevel(delayLevel)
                        .addContext(MeiTuanRiderMallConstant.EID, eid).build());
    }
    
    @Override
    public void asyncPushUserMemberCardToThird(Integer mallType, String traceId, Integer tenantId, Long uid, String mtOrderId, Integer orderType) {
        thirdPartyMallPublish.publish(
                ThirdPartyMallEvent.builder(this).traceId(traceId).tenantId(tenantId).mall(mallType).type(ThirdPartyMallDataType.PUSH_USER_BATTERY_MEMBER_CARD)
                        .addContext(MeiTuanRiderMallConstant.UID, uid).addContext(MeiTuanRiderMallConstant.ORDER_ID, mtOrderId)
                        .addContext(MeiTuanRiderMallConstant.ORDER_TYPE, orderType).build());
    }
}
