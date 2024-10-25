package com.xiliulou.electricity.handler.placeorder.chain;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.handler.placeorder.context.PlaceOrderContext;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.UnionPayOrder;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.handler.placeorder.AbstractPlaceOrderHandler;
import com.xiliulou.electricity.query.PlaceOrderQuery;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

import static com.xiliulou.electricity.constant.PlaceOrderConstant.AMOUNT_MIN;
import static com.xiliulou.electricity.constant.PlaceOrderConstant.PLACE_ORDER_MEMBER_CARD;

/**
 * @Description 套餐订单生成及计算金额节点，同时需要将后续处理需要的数据存入上下文对象context中
 * @Author: SongJP
 * @Date: 2024/10/25 17:13
 */
@Slf4j
@Component
public class MemberCardPlaceOrderHandler extends AbstractPlaceOrderHandler {
    
    @Resource
    private InsuranceVerificationHandler insuranceVerificationHandler;
    
    @Resource
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    public MemberCardPlaceOrderHandler() {
        this.nextHandler = insuranceVerificationHandler;
        this.nodePlaceOrderType = PLACE_ORDER_MEMBER_CARD;
    }
    
    @Override
    public void dealWithBusiness(PlaceOrderContext context, R<Object> result, Integer placeOrderType) {
        UserInfo userInfo = context.getUserInfo();
        BatteryMemberCard batteryMemberCard = context.getBatteryMemberCard();
        ElectricityCabinet electricityCabinet = context.getElectricityCabinet();
        BasePayConfig payParamConfig = context.getPayParamConfig();
        PlaceOrderQuery placeOrderQuery = context.getPlaceOrderQuery();
        
        // 查找计算优惠券
        // 计算优惠后支付金额
        Set<Integer> userCouponIds = electricityMemberCardOrderService.generateUserCouponIds(placeOrderQuery.getUserCouponId(), placeOrderQuery.getUserCouponIds());
        Triple<Boolean, String, Object> calculatePayAmountResult = electricityMemberCardOrderService.calculatePayAmount(batteryMemberCard, userCouponIds);
        if (Boolean.FALSE.equals(calculatePayAmountResult.getLeft())) {
            result = R.fail(calculatePayAmountResult.getMiddle(), (String) calculatePayAmountResult.getRight());
            // 提前退出
            processEntryAndExit(context, result, placeOrderType);
        }
        BigDecimal payAmount = (BigDecimal) calculatePayAmountResult.getRight();
        
        // 支付金额不能为负数
        if (payAmount.compareTo(AMOUNT_MIN) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        Integer payCount = electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard);
        
        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(placeOrderQuery.getMemberCardId());
        electricityMemberCardOrder.setUid(userInfo.getUid());
        electricityMemberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
        electricityMemberCardOrder.setCardName(batteryMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(payAmount);
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(batteryMemberCard.getValidDays());
        electricityMemberCardOrder.setTenantId(batteryMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(batteryMemberCard.getFranchiseeId());
        electricityMemberCardOrder.setPayCount(payCount);
        electricityMemberCardOrder.setSendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null);
        electricityMemberCardOrder.setRefId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getId().longValue() : null);
        electricityMemberCardOrder.setSource(Objects.nonNull(electricityCabinet) ? ElectricityMemberCardOrder.SOURCE_SCAN : ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
        electricityMemberCardOrder.setStoreId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId());
        electricityMemberCardOrder.setCouponIds(batteryMemberCard.getCouponIds());
        electricityMemberCardOrder.setParamFranchiseeId(payParamConfig.getFranchiseeId());
        electricityMemberCardOrder.setWechatMerchantId(payParamConfig.getThirdPartyMerchantId());
        electricityMemberCardOrder.setPaymentChannel(payParamConfig.getPaymentChannel());
        
        context.setElectricityMemberCardOrder(electricityMemberCardOrder);
        context.setUserCouponIds(userCouponIds);
        
        // 设置套餐订单号、类型、金额等相关数据
        context.getOrderList().add(electricityMemberCardOrder.getOrderId());
        context.getOrderTypeList().add(UnionPayOrder.ORDER_TYPE_MEMBER_CARD);
        context.getAllPayAmount().add(electricityMemberCardOrder.getPayAmount());
        context.setTotalAmount(context.getTotalAmount().add(electricityMemberCardOrder.getPayAmount()));
        
        processEntryAndExit(context, result, placeOrderType);
    }
}
