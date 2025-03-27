package com.xiliulou.electricity.handler.placeorder.chain;


import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.FreeServiceFeeStatusEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.handler.placeorder.AbstractPlaceOrderHandler;
import com.xiliulou.electricity.handler.placeorder.context.PlaceOrderContext;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FreeServiceFeeOrderService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.VersionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Objects;

import static com.xiliulou.electricity.enums.PlaceOrderTypeEnum.FREE_SERVICE_FEE;

/**
 * 免押服务费handler
 * 只能加到套餐前面，如果加到套餐后面有可能缴纳押金的时候也会支付
 *
 * @author : renhang
 * @description FreeServiceFeeOrderHandler
 * @date : 2025-03-27 09:42
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class FreeServiceFeeOrderHandler extends AbstractPlaceOrderHandler {

    private final MemberCardVerificationHandler memberCardVerificationHandler;

    private final EleDepositOrderService eleDepositOrderService;

    private final UserBatteryDepositService userBatteryDepositService;

    private final FranchiseeService franchiseeService;

    private final FreeServiceFeeOrderService freeServiceFeeOrderService;

    /**
     * todo 版本号
     */
    public static final String VERSION = "";

    @PostConstruct
    public void init() {
        this.nextHandler = memberCardVerificationHandler;
        this.nodePlaceOrderType = FREE_SERVICE_FEE.getType();
    }

    @Override
    public void dealWithBusiness(PlaceOrderContext context, R<Object> result, Integer placeOrderType) {

        String queryVersion = context.getPlaceOrderQuery().getVersion();
        // 兼容旧版本小程序，如果旧版不需要计算免押服务费
        if (StrUtil.isEmpty(queryVersion) || VersionUtil.compareVersion(queryVersion, VERSION) < 0) {
            log.info("FreeServiceFeeOrderHandler Info! version is {}", queryVersion);
            fireProcess(context, result, placeOrderType);
            return;
        }

        UserInfo userInfo = context.getUserInfo();
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("PLACE ORDER WARN! user not pay deposit,uid={} ", userInfo.getUid());
            throw new BizException("ELECTRICITY.0049", "未缴纳押金");
        }

        // 查询用户押金
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("FreeServiceFeeOrderHandler Warn! userBatteryDeposit is null, uid is {}", userInfo.getUid());
            throw new BizException("ELECTRICITY.0049", "未缴纳押金");
        }

        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("FreeServiceFeeOrderHandler Warn! eleDepositOrder is null, uid is {}", userInfo.getUid());
            throw new BizException("ELECTRICITY.0049", "未缴纳押金");
        }

        if (!Objects.equals(eleDepositOrder.getStatus(), EleDepositOrder.STATUS_SUCCESS)) {
            fireProcess(context, result, placeOrderType);
            return;
        }

        // 如果押金类型不是免押，走正常的支付
        if (Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
            log.warn("FreeServiceFeeOrderHandler WARN! user not free order ,uid is {} ", userInfo.getUid());
            fireProcess(context, result, placeOrderType);
            return;
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee) || Objects.equals(franchisee.getFreeServiceFeeSwitch(), Franchisee.FREE_SERVICE_FEE_SWITCH_CLOSE)) {
            log.warn("FreeServiceFeeOrderHandler WARN! freeServiceFeeSwitch is close , franchisee is {} ", userInfo.getFranchiseeId());
            fireProcess(context, result, placeOrderType);
            return;
        }

        // 用户是否已经支付过免押服务费
        Integer existsPaySuccessOrder = freeServiceFeeOrderService.existsPaySuccessOrder(eleDepositOrder.getOrderId(), userInfo.getUid());
        if (Objects.nonNull(existsPaySuccessOrder)) {
            log.info("FreeServiceFeeOrderHandler Info! current User Payed FreeServiceFee, freeDepositOrderId is {} , uid is {} ", eleDepositOrder.getOrderId(), userInfo.getUid());
            fireProcess(context, result, placeOrderType);
            return;
        }

        BasePayConfig payParamConfig = context.getPayParamConfig();
        // 生成服务免押订单
        String freeServiceFeeOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.FREE_SERVICE_FEE, userInfo.getUid());
        FreeServiceFeeOrder freeServiceFeeOrder = FreeServiceFeeOrder.builder().uid(userInfo.getUid()).orderId(freeServiceFeeOrderId).freeDepositOrderId(eleDepositOrder.getOrderId())
                .uid(userInfo.getUid()).payAmount(franchisee.getFreeServiceFee()).status(FreeServiceFeeStatusEnum.STATUS_UNPAID.getStatus())
                .paymentChannel(payParamConfig.getPaymentChannel())
                .tenantId(TenantContextHolder.getTenantId()).franchiseeId(userInfo.getFranchiseeId()).storeId(eleDepositOrder.getStoreId())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .build();

        context.setFreeServiceFeeOrder(freeServiceFeeOrder);

        context.getOrderList().add(freeServiceFeeOrderId);
        context.getOrderTypeList().add(UnionPayOrder.FREE_SERVICE_FEE);
        context.getAllPayAmount().add(franchisee.getFreeServiceFee());
        context.setTotalAmount(context.getTotalAmount().add(franchisee.getFreeServiceFee()));

        fireProcess(context, result, placeOrderType);
    }
}
