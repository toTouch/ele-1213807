package com.xiliulou.electricity.service.impl.ThirdPartyMall;

import com.xiliulou.electricity.constant.thirdPartyMallConstant.MeiTuanRiderMallConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.enums.thirdParthMall.ThirdPartyMallDataType;
import com.xiliulou.electricity.event.ThirdPartyMallEvent;
import com.xiliulou.electricity.event.publish.ThirdPartyMallPublish;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.thirdPartyMall.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.service.thirdPartyMall.MeiTuanRiderMallOrderService;
import com.xiliulou.electricity.service.thirdPartyMall.PushDataToThirdService;
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
    private ThirdPartyMallPublish thirdPartyMallPublish;
    
    @Resource
    private MeiTuanRiderMallOrderService meiTuanRiderMallOrderService;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private MeiTuanRiderMallConfigService meiTuanRiderMallConfigService;
    
    @Override
    public void asyncPushExchangeAndUserAndBatteryToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType, Long uid) {
        Boolean mtOrder = meiTuanRiderMallOrderService.isMtOrder(uid);
        // 判断使用的订单是否美团订单
        if (mtOrder) {
            this.asyncPushExchangeToThird(mallType, traceId, tenantId, orderId, orderType);
            this.asyncPushBatteryToThird(mallType, traceId, tenantId, orderId, orderType);
            this.asyncPushUserToThird(mallType, traceId, tenantId, orderId, orderType);
        }
    }
    
    @Override
    public void asyncPushUserAndBatteryToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType, Long uid) {
        Boolean mtOrder = meiTuanRiderMallOrderService.isMtOrder(uid);
        // 判断使用的订单是否美团订单
        if (mtOrder) {
            this.asyncPushBatteryToThird(mallType, traceId, tenantId, orderId, orderType);
            this.asyncPushUserToThird(mallType, traceId, tenantId, orderId, orderType);
        }
    }
    
    @Override
    public void asyncPushExchangeToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType) {
        thirdPartyMallPublish.publish(ThirdPartyMallEvent.builder().traceId(traceId).tenantId(tenantId).mall(mallType).type(ThirdPartyMallDataType.PUSH_USER_EXCHANGE_RECORD)
                .addContext(MeiTuanRiderMallConstant.ORDER_ID, orderId).addContext(MeiTuanRiderMallConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushUserToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType) {
        thirdPartyMallPublish.publish(ThirdPartyMallEvent.builder().traceId(traceId).tenantId(tenantId).mall(mallType).type(ThirdPartyMallDataType.PUSH_USER_INFO)
                .addContext(MeiTuanRiderMallConstant.ORDER_ID, orderId).addContext(MeiTuanRiderMallConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushBatteryToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType) {
        thirdPartyMallPublish.publish(ThirdPartyMallEvent.builder().traceId(traceId).tenantId(tenantId).mall(mallType).type(ThirdPartyMallDataType.PUSH_USER_BATTERY)
                .addContext(MeiTuanRiderMallConstant.ORDER_ID, orderId).addContext(MeiTuanRiderMallConstant.ORDER_TYPE, orderType).build());
    }
    
    @Override
    public void asyncPushCabinetToThird(Integer mallType, String traceId, Integer tenantId, Long eid) {
        if (isMtCabinet(eid.intValue())) {
            thirdPartyMallPublish.publish(ThirdPartyMallEvent.builder().traceId(traceId).tenantId(tenantId).mall(mallType).type(ThirdPartyMallDataType.PUSH_ELE_CABINET)
                    .addContext(MeiTuanRiderMallConstant.EID, eid).build());
        }
    }
    
    @Override
    public void asyncPushCabinetStatusToThird(Integer mallType, String traceId, Integer tenantId, Long eid, Integer delayLevel) {
        if (isMtCabinet(eid.intValue())) {
            thirdPartyMallPublish.publish(
                    ThirdPartyMallEvent.builder().traceId(traceId).tenantId(tenantId).mall(mallType).type(ThirdPartyMallDataType.PUSH_ELE_CABINET).delayLevel(delayLevel)
                            .addContext(MeiTuanRiderMallConstant.EID, eid).build());
        }
    }
    
    @Override
    public void asyncPushUserMemberCardToThird(Integer mallType, String traceId, Integer tenantId, Long uid, String mtOrderId, Integer orderType) {
        thirdPartyMallPublish.publish(ThirdPartyMallEvent.builder().traceId(traceId).tenantId(tenantId).mall(mallType).type(ThirdPartyMallDataType.PUSH_USER_BATTERY_MEMBER_CARD)
                .addContext(MeiTuanRiderMallConstant.UID, uid).addContext(MeiTuanRiderMallConstant.ORDER_ID, mtOrderId).addContext(MeiTuanRiderMallConstant.ORDER_TYPE, orderType)
                .build());
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
