package com.xiliulou.electricity.handler.placeorder.chain;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.constant.PlaceOrderConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.UnionPayOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.handler.placeorder.AbstractPlaceOrderHandler;
import com.xiliulou.electricity.handler.placeorder.context.PlaceOrderContext;
import com.xiliulou.electricity.query.PlaceOrderQuery;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

import static com.xiliulou.electricity.constant.PlaceOrderConstant.PLACE_ORDER_DEPOSIT;

/**
 * @Description 押金订单生成及计算金额节点，同时需要将后续处理需要的数据存入上下文对象context中
 * @Author: SongJP
 * @Date: 2024/10/25 17:05
 */
@Slf4j
@Component
public class DepositPlaceOrderHandler extends AbstractPlaceOrderHandler {
    
    @Resource
    private MemberCardVerificationHandler memberCardVerificationHandler;
    
    @PostConstruct
    public void init() {
        this.nextHandler = memberCardVerificationHandler;
        this.nodePlaceOrderType = PLACE_ORDER_DEPOSIT;
    }
    
    @Override
    public void dealWithBusiness(PlaceOrderContext context, R<Object> result, Integer placeOrderType) {
        UserInfo userInfo = context.getUserInfo();
        BatteryMemberCard batteryMemberCard = context.getBatteryMemberCard();
        ElectricityCabinet electricityCabinet = context.getElectricityCabinet();
        BasePayConfig payParamConfig = context.getPayParamConfig();
        PlaceOrderQuery placeOrderQuery = context.getPlaceOrderQuery();
        
        // 根据支付类型设置押金金额，后台直接缴纳押金时，金额可修改
        Integer payType = placeOrderQuery.getPayType();
        BigDecimal amount = Objects.equals(payType, PlaceOrderConstant.ONLINE_PAYMENT) ? batteryMemberCard.getDeposit() : BigDecimal.valueOf(placeOrderQuery.getDepositAmount());
        
        // 生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder().orderId(depositOrderId).uid(userInfo.getUid()).phone(userInfo.getPhone()).name(userInfo.getName())
                .payAmount(amount).status(EleDepositOrder.STATUS_INIT).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(userInfo.getTenantId()).franchiseeId(batteryMemberCard.getFranchiseeId()).payType(payType)
                .storeId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId()).mid(batteryMemberCard.getId()).modelType(0)
                .paramFranchiseeId(Objects.isNull(payParamConfig) ? null : payParamConfig.getFranchiseeId())
                .wechatMerchantId(Objects.isNull(payParamConfig) ? null : payParamConfig.getThirdPartyMerchantId())
                .paymentChannel(Objects.isNull(payParamConfig) ? null : payParamConfig.getPaymentChannel()).build();
        
        context.setEleDepositOrder(eleDepositOrder);
        
        // 设置套餐订单号、类型、金额等相关数据
        context.getOrderList().add(eleDepositOrder.getOrderId());
        context.getOrderTypeList().add(UnionPayOrder.ORDER_TYPE_DEPOSIT);
        context.getAllPayAmount().add(amount);
        context.setTotalAmount(context.getTotalAmount().add(amount));
        
        fireProcess(context, result, placeOrderType);
    }
}
