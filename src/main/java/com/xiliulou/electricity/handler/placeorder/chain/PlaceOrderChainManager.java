package com.xiliulou.electricity.handler.placeorder.chain;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.PlaceOrderConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.UnionPayOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingQueryDetailsEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.handler.placeorder.AbstractPlaceOrderHandler;
import com.xiliulou.electricity.handler.placeorder.context.PlaceOrderContext;
import com.xiliulou.electricity.query.PlaceOrderQuery;
import com.xiliulou.electricity.service.BatteryMemberCardOrderCouponService;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.TradeOrderService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.pay.PayConfigBizService;
import com.xiliulou.pay.base.dto.BasePayOrderCreateDTO;
import com.xiliulou.security.bean.TokenUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/24 13:45
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceOrderChainManager {
    
    private final DepositVerificationHandler depositVerificationHandler;
    
    private final MemberCardVerificationHandler memberCardVerificationHandler;
    
    private final UserInfoService userInfoService;
    
    private final PayConfigBizService payConfigBizService;
    
    private final UserOauthBindService userOauthBindService;
    
    private final BatteryMemberCardService batteryMemberCardService;
    
    private final ElectricityCabinetService electricityCabinetService;
    
    private final EleDepositOrderService eleDepositOrderService;
    
    private final ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    private final BatteryMemberCardOrderCouponService memberCardOrderCouponService;
    
    private final UserCouponService userCouponService;
    
    private final InsuranceOrderService insuranceOrderService;
    
    private final TradeOrderService tradeOrderService;
    
    private final UnionTradeOrderService unionTradeOrderService;
    
    private final ElectricityConfigService electricityConfigService;
    
    
    private HashMap<Integer, AbstractPlaceOrderHandler> firstNodes = new HashMap<>();
    
    @PostConstruct
    public void init() {
        firstNodes.put(PlaceOrderConstant.PLACE_ORDER_DEPOSIT, depositVerificationHandler);
        firstNodes.put(PlaceOrderConstant.PLACE_ORDER_DEPOSIT_AND_MEMBER_CARD, depositVerificationHandler);
        firstNodes.put(PlaceOrderConstant.PLACE_ORDER_MEMBER_CARD, memberCardVerificationHandler);
        firstNodes.put(PlaceOrderConstant.PLACE_ORDER_MEMBER_CARD_AND_INSURANCE, memberCardVerificationHandler);
        firstNodes.put(PlaceOrderConstant.PLACE_ORDER_ALL, depositVerificationHandler);
    }
    
    /**
     * 选择对应业务类型的头结点处理业务
     */
    public R<Object> chooseNodeAndProcess(PlaceOrderContext context) throws Exception {
        R<Object> result = commonVerification(context);
        if (!result.isSuccess()) {
            return result;
        }
        
        Integer placeOrderType = context.getPlaceOrderQuery().getPlaceOrderType();
        AbstractPlaceOrderHandler firstNode = firstNodes.get(placeOrderType);
        if (Objects.isNull(firstNode)) {
            log.error("Place order error! 无相应业务的首节点，PlaceOrderQuery={}", context.getPlaceOrderQuery());
            return R.fail("302002", "业务类型错误，请联系客服");
        }
        
        firstNode.dealWithBusiness(context, result, placeOrderType);
        return result;
    }
    
    /**
     * 公共校验，以及获取相关数据传递给执行链路使用
     *
     * @param context 业务参数
     */
    private R<Object> commonVerification(PlaceOrderContext context) throws Exception {
        TokenUser tokenUser = context.getTokenUser();
        PlaceOrderQuery placeOrderQuery = context.getPlaceOrderQuery();
        Integer tenantId = context.getTenantId();
        
        // 线上与线下两种情况，需要绑定套餐的人的uid是以不同的方式取得的
        Long uid = Objects.equals(placeOrderQuery.getPayType(), PlaceOrderConstant.OFFLINE_PAYMENT) ? placeOrderQuery.getUid() : tokenUser.getUid();
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("BATTERY DEPOSIT WARN! not found user,uid={}", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        context.setUserInfo(userInfo);
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("BATTERY DEPOSIT WARN! user is unUsable,uid={}", uid);
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("BATTERY DEPOSIT WARN! user not auth,uid={}", uid);
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            return R.fail("302003", "运营商配置异常，请联系客服");
        }
        context.setElectricityConfig(electricityConfig);
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(placeOrderQuery.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("BATTERY DEPOSIT WARN!not found batteryMemberCard,uid={},mid={}", uid, placeOrderQuery.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "电池套餐不存在");
        }
        if (!Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())) {
            log.warn("BATTERY DEPOSIT WARN! batteryMemberCard is disable,uid={},mid={}", uid, placeOrderQuery.getMemberCardId());
            return R.fail("100275", "电池套餐不可用");
        }
        
        if (Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),
                batteryMemberCard.getFranchiseeId())) {
            log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals,uid={},mid={}", userInfo.getUid(), batteryMemberCard.getId());
            return R.fail("100349", "用户加盟商与套餐加盟商不一致");
        }
        
        context.setBatteryMemberCard(batteryMemberCard);
        
        // 线上购买才需要支付配置
        if (Objects.equals(placeOrderQuery.getPayType(), PlaceOrderConstant.ONLINE_PAYMENT)) {
            BasePayConfig payParamConfig = payConfigBizService.queryPayParams(placeOrderQuery.getPaymentChannel(), tenantId, batteryMemberCard.getFranchiseeId(),
                    Collections.singleton(ProfitSharingQueryDetailsEnum.PROFIT_SHARING_CONFIG));
            if (Objects.isNull(payParamConfig)) {
                log.warn("BATTERY DEPOSIT WARN!not found pay params,uid={}", uid);
                return R.fail("100307", "未配置支付参数!");
            }
            context.setPayParamConfig(payParamConfig);
            
            UserOauthBind userOauthBind = userOauthBindService.queryByUidAndTenantAndChannel(uid, tenantId, placeOrderQuery.getPaymentChannel());
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.warn("BATTERY DEPOSIT WARN!not found useroauthbind or thirdid is null,uid={}", uid);
                return R.fail("100308", "未找到用户的第三方授权信息!");
            }
            context.setUserOauthBind(userOauthBind);
        }
        
        // 获取扫码柜机
        ElectricityCabinet electricityCabinet = null;
        if (StringUtils.isNotBlank(placeOrderQuery.getProductKey()) && StringUtils.isNotBlank(placeOrderQuery.getDeviceName())) {
            electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(placeOrderQuery.getProductKey(), placeOrderQuery.getDeviceName());
        }
        if (Objects.nonNull(electricityCabinet) && !Objects.equals(electricityCabinet.getFranchiseeId(), NumberConstant.ZERO_L) && Objects.nonNull(
                electricityCabinet.getFranchiseeId()) && !Objects.equals(electricityCabinet.getFranchiseeId(), batteryMemberCard.getFranchiseeId())) {
            log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals electricityCabinet,eid={},mid={}", electricityCabinet.getId(),
                    placeOrderQuery.getMemberCardId());
            return R.fail("100375", "柜机加盟商与套餐加盟商不一致,请删除小程序后重新进入");
        }
        context.setElectricityCabinet(electricityCabinet);
        
        if (Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),
                batteryMemberCard.getFranchiseeId())) {
            log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals,uid={},mid={}", userInfo.getUid(), placeOrderQuery.getMemberCardId());
            return R.fail("100349", "用户加盟商与套餐加盟商不一致");
        }
        
        context.setOrderList(new ArrayList<>());
        context.setOrderTypeList(new ArrayList<>());
        context.setAllPayAmount(new ArrayList<>());
        context.setTotalAmount(BigDecimal.valueOf(0));
        
        return R.ok();
    }
    
    /**
     * 保存订单并调起支付
     */
    public R<Object> saveOrdersAndPay(PlaceOrderContext context) throws Exception {
        // 保存订单
        EleDepositOrder eleDepositOrder = context.getEleDepositOrder();
        if (Objects.nonNull(eleDepositOrder)) {
            eleDepositOrderService.insert(eleDepositOrder);
        }
        
        ElectricityMemberCardOrder electricityMemberCardOrder = context.getElectricityMemberCardOrder();
        if (Objects.nonNull(electricityMemberCardOrder)) {
            electricityMemberCardOrderService.insert(electricityMemberCardOrder);
            
            Set<Integer> userCouponIds = context.getUserCouponIds();
            if (CollectionUtils.isNotEmpty(userCouponIds)) {
                // 保存订单所使用的优惠券
                memberCardOrderCouponService.batchInsert(electricityMemberCardOrderService.buildMemberCardOrderCoupon(electricityMemberCardOrder.getOrderId(), userCouponIds));
                // 修改优惠券状态为核销中
                userCouponService.batchUpdateUserCoupon(
                        electricityMemberCardOrderService.buildUserCouponList(userCouponIds, UserCoupon.STATUS_IS_BEING_VERIFICATION, electricityMemberCardOrder.getOrderId()));
            }
        }
        
        InsuranceOrder insuranceOrder = context.getInsuranceOrder();
        if (Objects.nonNull(insuranceOrder)) {
            insuranceOrderService.insert(insuranceOrder);
        }
        
        UserInfo userInfo = context.getUserInfo();
        BatteryMemberCard batteryMemberCard = context.getBatteryMemberCard();
        BasePayConfig payParamConfig = context.getPayParamConfig();
        List<String> orderList = context.getOrderList();
        List<Integer> orderTypeList = context.getOrderTypeList();
        
        // 0元支付和线下支付
        if (context.getTotalAmount().compareTo(PlaceOrderConstant.AMOUNT_MIN) < 0 || Objects.equals(context.getPlaceOrderQuery().getPayType(),
                PlaceOrderConstant.OFFLINE_PAYMENT)) {
            Triple<Boolean, String, Object> tripleResult = tradeOrderService.handleTotalAmountZero(userInfo, orderList, orderTypeList, null, context);
            if (Boolean.FALSE.equals(tripleResult.getLeft())) {
                return R.fail(tripleResult.getMiddle(), (String) tripleResult.getRight());
            }
            return R.ok();
        }
        
        // 调起支付
        UnionPayOrder unionPayOrder = UnionPayOrder.builder().jsonOrderId(JsonUtil.toJson(orderList)).jsonOrderType(JsonUtil.toJson(orderTypeList))
                .jsonSingleFee(JsonUtil.toJson(context.getAllPayAmount())).payAmount(context.getTotalAmount()).tenantId(batteryMemberCard.getTenantId())
                .attach(UnionTradeOrder.ATTACH_PLACE_ORDER).description("租电租金（含押金）").uid(userInfo.getUid()).build();
        
        // 处理分账交易订单
        tradeOrderService.dealProfitSharingTradeOrderV2(electricityMemberCardOrder, insuranceOrder, payParamConfig, batteryMemberCard, unionPayOrder, userInfo, orderList);
        
        BasePayOrderCreateDTO resultDTO = unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, payParamConfig, context.getUserOauthBind().getThirdId(),
                context.getRequest(), null);
        return R.ok(resultDTO);
    }
}
