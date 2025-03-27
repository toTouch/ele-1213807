package com.xiliulou.electricity.handler.placeorder.chain;


import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.dto.IsSupportFreeServiceFeeDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.FreeServiceFeeStatusEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.handler.placeorder.AbstractPlaceOrderHandler;
import com.xiliulou.electricity.handler.placeorder.context.PlaceOrderContext;
import com.xiliulou.electricity.service.FreeServiceFeeOrderService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
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

    private final UserBatteryDepositService userBatteryDepositService;

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

        // 是否支持免押服务费
        IsSupportFreeServiceFeeDTO supportFreeServiceFee = freeServiceFeeOrderService.isSupportFreeServiceFee(userInfo, userBatteryDeposit.getOrderId());
        if (!supportFreeServiceFee.getSupportFreeServiceFee()) {
            fireProcess(context, result, placeOrderType);
            return;
        }

        BasePayConfig payParamConfig = context.getPayParamConfig();
        // 生成服务免押订单
        String freeServiceFeeOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.FREE_SERVICE_FEE, userInfo.getUid());
        FreeServiceFeeOrder freeServiceFeeOrder = FreeServiceFeeOrder.builder().uid(userInfo.getUid()).orderId(freeServiceFeeOrderId).freeDepositOrderId(userBatteryDeposit.getOrderId())
                .payAmount(supportFreeServiceFee.getFreeServiceFee()).status(FreeServiceFeeStatusEnum.STATUS_UNPAID.getStatus())
                .paymentChannel(payParamConfig.getPaymentChannel()).tenantId(userInfo.getTenantId()).franchiseeId(userInfo.getFranchiseeId())
                .storeId(userInfo.getStoreId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .build();

        context.setFreeServiceFeeOrder(freeServiceFeeOrder);

        context.getOrderList().add(freeServiceFeeOrderId);
        context.getOrderTypeList().add(UnionPayOrder.FREE_SERVICE_FEE);
        context.getAllPayAmount().add(supportFreeServiceFee.getFreeServiceFee());
        context.setTotalAmount(context.getTotalAmount().add(supportFreeServiceFee.getFreeServiceFee()));

        // 如果只是缴纳免押服务费，这个需要中断
        if (Objects.equals(placeOrderType, FREE_SERVICE_FEE.getType())){
            exit();
            return;
        }

        fireProcess(context, result, placeOrderType);
    }
}
