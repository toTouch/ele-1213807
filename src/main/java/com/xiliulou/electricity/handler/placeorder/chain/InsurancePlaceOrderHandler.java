package com.xiliulou.electricity.handler.placeorder.chain;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.handler.placeorder.context.PlaceOrderContext;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.UnionPayOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.handler.placeorder.AbstractPlaceOrderHandler;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

import static com.xiliulou.electricity.constant.PlaceOrderConstant.PLACE_ORDER_INSURANCE;

/**
 * @Description 保险订单生成及计算金额节点，同时需要将后续处理需要的数据存入上下文对象context中
 * @Author: SongJP
 * @Date: 2024/10/25 17:14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InsurancePlaceOrderHandler extends AbstractPlaceOrderHandler {
    
    @PostConstruct
    public void init() {
        this.nodePlaceOrderType = PLACE_ORDER_INSURANCE;
    }
    
    @Override
    public void dealWithBusiness(PlaceOrderContext context, R<Object> result, Integer placeOrderType) {
        FranchiseeInsurance franchiseeInsurance = context.getFranchiseeInsurance();
        UserInfo userInfo = context.getUserInfo();
        ElectricityCabinet electricityCabinet = context.getElectricityCabinet();
        BasePayConfig payParamConfig = context.getPayParamConfig();
        
        // 生成保险独立订单
        String insuranceOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_INSURANCE, userInfo.getUid());
        InsuranceOrder insuranceOrder = InsuranceOrder.builder().insuranceId(franchiseeInsurance.getId()).insuranceName(franchiseeInsurance.getName())
                .insuranceType(franchiseeInsurance.getInsuranceType()).orderId(insuranceOrderId).cid(franchiseeInsurance.getCid())
                .franchiseeId(franchiseeInsurance.getFranchiseeId()).isUse(InsuranceOrder.NOT_USE).payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead()).payType(context.getPlaceOrderQuery().getPayType()).phone(userInfo.getPhone()).status(InsuranceOrder.STATUS_INIT)
                .storeId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId()).tenantId(userInfo.getTenantId()).uid(userInfo.getUid())
                .userName(userInfo.getName()).validDays(franchiseeInsurance.getValidDays()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .simpleBatteryType(franchiseeInsurance.getSimpleBatteryType()).paramFranchiseeId(payParamConfig.getFranchiseeId())
                .wechatMerchantId(payParamConfig.getThirdPartyMerchantId()).paymentChannel(payParamConfig.getPaymentChannel()).build();
        
        context.setInsuranceOrder(insuranceOrder);
        
        // 设置套餐订单号、类型、金额等相关数据
        context.getOrderList().add(insuranceOrder.getOrderId());
        context.getOrderTypeList().add(UnionPayOrder.ORDER_TYPE_INSURANCE);
        context.getAllPayAmount().add(insuranceOrder.getPayAmount());
        context.setTotalAmount(context.getTotalAmount().add(insuranceOrder.getPayAmount()));
        
        fireProcess(context, result, placeOrderType);
    }
}
