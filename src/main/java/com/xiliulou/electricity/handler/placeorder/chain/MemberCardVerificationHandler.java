package com.xiliulou.electricity.handler.placeorder.chain;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.handler.placeorder.AbstractPlaceOrderHandler;
import com.xiliulou.electricity.handler.placeorder.context.PlaceOrderContext;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.PlaceOrderConstant.PLACE_ORDER_DEPOSIT;
import static com.xiliulou.electricity.constant.PlaceOrderConstant.PLACE_ORDER_MEMBER_CARD;

/**
 * @Description 套餐校验处理节点，同时需要将后续处理需要的数据存入上下文对象context中
 * @Author: SongJP
 * @Date: 2024/10/25 17:12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCardVerificationHandler extends AbstractPlaceOrderHandler {
    
    private final MemberCardPlaceOrderHandler memberCardPlaceOrderHandler;
    
    private final UserInfoGroupDetailService userInfoGroupDetailService;
    
    private final UserBatteryTypeService userBatteryTypeService;
    
    private final MemberCardBatteryTypeService memberCardBatteryTypeService;
    
    private final UserBatteryDepositService userBatteryDepositService;
    
    private final EleRefundOrderService eleRefundOrderService;
    
    private final BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    private final BatteryMemberCardService batteryMemberCardService;
    
    private final UserInfoExtraService userInfoExtraService;
    
    private final UserBatteryMemberCardService userBatteryMemberCardService;
    
    private final ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    
    @PostConstruct
    public void init() {
        this.nextHandler = memberCardPlaceOrderHandler;
        this.nodePlaceOrderType = PLACE_ORDER_MEMBER_CARD;
    }
    
    @Override
    public void dealWithBusiness(PlaceOrderContext context, R<Object> result, Integer placeOrderType) {
        UserInfo userInfo = context.getUserInfo();
        BatteryMemberCard batteryMemberCard = context.getBatteryMemberCard();
        ElectricityConfig electricityConfig = context.getElectricityConfig();
        
        // 查询用户分组供后续校验使用
        List<UserInfoGroupNamesBO> userInfoGroupNamesBos = userInfoGroupDetailService.listGroupByUid(
                UserInfoGroupDetailQuery.builder().uid(userInfo.getUid()).tenantId(TenantContextHolder.getTenantId()).build());
        
        UserBatteryDeposit userBatteryDeposit = null;
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        
        // 续费套餐时，需要校验押金缴纳状态
        if ((placeOrderType & PLACE_ORDER_DEPOSIT) != PLACE_ORDER_DEPOSIT) {
            if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                log.warn("PLACE ORDER WARN! user not pay deposit,uid={} ", userInfo.getUid());
                throw new BizException("ELECTRICITY.0049", "未缴纳押金");
            }
            
            userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryDeposit)) {
                log.warn("PLACE ORDER WARN! not found userBatteryDeposit,uid={}", userInfo.getUid());
                throw new BizException("ELECTRICITY.0001", "用户信息不存在");
            }
            
            // 是否有正在进行中的退押
            Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
            if (refundCount > 0) {
                log.warn("PLACE ORDER WARN! have refunding order,uid={}", userInfo.getUid());
                throw new BizException("ELECTRICITY.0047", "电池押金退款中");
            }
            
            List<BatteryMembercardRefundOrder> batteryMembercardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(userInfo.getUid());
            if (CollectionUtils.isNotEmpty(batteryMembercardRefundOrders)) {
                log.warn("PLACE ORDER WARN! battery membercard refund review,uid={}", userInfo.getUid());
                throw new BizException("100018", "套餐租金退款审核中");
            }
            
            BigDecimal deposit = Objects.equals(userBatteryDeposit.getDepositModifyFlag(), UserBatteryDeposit.DEPOSIT_MODIFY_YES) ? userBatteryDeposit.getBeforeModifyDeposit()
                    : userBatteryDeposit.getBatteryDeposit();
            if (batteryMemberCard.getDeposit().compareTo(deposit) > 0) {
                throw new BizException("100033", "套餐押金金额与缴纳押金不匹配，请刷新重试");
            }
            
            BatteryMemberCard userBindbatteryMemberCard =
                    Objects.isNull(userBatteryMemberCard) ? null : batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            
            Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                    userBindbatteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("PLACE ORDER WARN! user exist battery service fee,uid={},mid={}", userInfo.getUid(), batteryMemberCard.getId());
                throw new BizException("100220", "用户存在电池服务费");
            }
            
            // 灵活续费押金及电池型号校验
            List<String> userBatteryTypes = userBatteryTypeService.selectByUid(userInfo.getUid());
            boolean matchOrNot = memberCardBatteryTypeService.checkBatteryTypeAndDepositWithUser(userBatteryTypes, batteryMemberCard, userBatteryDeposit, electricityConfig, userInfo);
            if (!matchOrNot) {
                log.warn("PLACE ORDER WARN! deposit or batteryTypes not match,uid={},mid={}", userInfo.getUid(), batteryMemberCard.getId());
                throw new BizException("302004", "灵活续费已禁用，请刷新后重新购买");
            }
        }
        
        // 判断套餐用户分组和用户的用户分组是否匹配
        if (CollectionUtils.isNotEmpty(userInfoGroupNamesBos)) {
            if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_SYSTEM)) {
                throw new BizException("100318", "您浏览的套餐已下架，请看看其他的吧");
            }
            
            List<Long> userGroupIds = userInfoGroupNamesBos.stream().map(UserInfoGroupNamesBO::getGroupId).collect(Collectors.toList());
            userGroupIds.retainAll(JsonUtil.fromJsonArray(batteryMemberCard.getUserInfoGroupIds(), Long.class));
            if (CollectionUtils.isEmpty(userGroupIds)) {
                throw new BizException("100318", "您浏览的套餐已下架，请看看其他的吧");
            }
        } else {
            if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_USER)) {
                throw new BizException("100318", "您浏览的套餐已下架，请看看其他的吧");
            }
            
            // 判断套餐租赁状态，用户为老用户，套餐类型为新租，则不支持购买
            if (userInfo.getPayCount() > 0 && BatteryMemberCard.RENT_TYPE_NEW.equals(batteryMemberCard.getRentType())) {
                log.warn("PLACE ORDER WARN! The rent type of current package is a new rental package, uid={}, mid={}", userInfo.getUid(), batteryMemberCard.getId());
                throw new BizException("100376", "已是平台老用户，无法购买新租类型套餐，请刷新页面重试");
            }
        }
        
        Boolean advanceRenewalResult = batteryMemberCardService.checkIsAdvanceRenewal(batteryMemberCard, userBatteryMemberCard);
        if (!advanceRenewalResult) {
            log.warn("PLACE ORDER WARN! not allow advance renewal,uid={}", userInfo.getUid());
            throw new BizException("100439", "您当前有生效中的套餐，无须重复购买，请联系客服后操作");
        }
        
        // 是否限制套餐购买次数
        Triple<Boolean, String, String> limitPurchase = userInfoExtraService.isLimitPurchase(userInfo.getUid(), userInfo.getTenantId());
        if (limitPurchase.getLeft()) {
            log.warn("PLACE ORDER WARN! user limit purchase,uid={}", userInfo.getUid());
            throw new BizException(limitPurchase.getMiddle(), limitPurchase.getRight());
        }
        
        fireProcess(context, result, placeOrderType);
    }
}
