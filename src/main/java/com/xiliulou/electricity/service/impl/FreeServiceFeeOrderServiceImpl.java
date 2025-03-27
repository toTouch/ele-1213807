package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.dto.CreateFreeServiceFeeOrderDTO;
import com.xiliulou.electricity.dto.IsSupportFreeServiceFeeDTO;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FreeServiceFeeOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.FreeServiceFeeStatusEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.FreeServiceFeeOrderMapper;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FreeServiceFeeOrderService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author : renhang
 * @description FreeServiceFeeOrderServiceImpl
 * @date : 2025-03-27 10:27
 **/
@Service
@Slf4j
public class FreeServiceFeeOrderServiceImpl implements FreeServiceFeeOrderService {

    @Resource
    private FreeServiceFeeOrderMapper freeServiceFeeOrderMapper;

    @Resource
    private EleDepositOrderService eleDepositOrderService;

    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    ApplicationContext applicationContext;

    @Override
    @Slave
    public Integer existsPaySuccessOrder(String freeDepositOrderId, Long uid) {
        return freeServiceFeeOrderMapper.existsPaySuccessOrder(freeDepositOrderId, uid);
    }

    @Override
    public void insertOrder(FreeServiceFeeOrder freeServiceFeeOrder) {
        freeServiceFeeOrderMapper.insert(freeServiceFeeOrder);
    }

    @Override
    public IsSupportFreeServiceFeeDTO isSupportFreeServiceFee(UserInfo userInfo, String depositOrderId) {
        IsSupportFreeServiceFeeDTO dto = new IsSupportFreeServiceFeeDTO().setSupportFreeServiceFee(false);

        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(depositOrderId);
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("isSupportFreeServiceFee Warn! eleDepositOrder is null, uid is {}", userInfo.getUid());
            throw new BizException("ELECTRICITY.0049", "未缴纳押金");
        }

        if (!Objects.equals(eleDepositOrder.getStatus(), EleDepositOrder.STATUS_SUCCESS)) {
            return dto;
        }

        // 如果押金类型不是免押，走正常的支付
        if (Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
            log.warn("isSupportFreeServiceFee WARN! user not free order ,uid is {} ", userInfo.getUid());
            return dto;
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee) || Objects.equals(franchisee.getFreeServiceFeeSwitch(), Franchisee.FREE_SERVICE_FEE_SWITCH_CLOSE)) {
            log.warn("isSupportFreeServiceFee WARN! freeServiceFeeSwitch is close , franchisee is {} ", userInfo.getFranchiseeId());
            return dto;
        }

        // 用户是否已经支付过免押服务费
        Integer existsPaySuccessOrder = applicationContext.getBean(FreeServiceFeeOrderService.class).existsPaySuccessOrder(eleDepositOrder.getOrderId(), userInfo.getUid());
        if (Objects.nonNull(existsPaySuccessOrder)) {
            log.info("isSupportFreeServiceFee Info! current User Payed FreeServiceFee, freeDepositOrderId is {} , uid is {} ", eleDepositOrder.getOrderId(), userInfo.getUid());
            return dto;
        }

        return dto.setSupportFreeServiceFee(true).setFreeServiceFee(franchisee.getFreeServiceFee());
    }

    @Override
    public IsSupportFreeServiceFeeDTO isSupportFreeServiceFeeCar(UserInfo userInfo, String depositOrderId) {
        IsSupportFreeServiceFeeDTO dto = new IsSupportFreeServiceFeeDTO().setSupportFreeServiceFee(false);
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee) || Objects.equals(franchisee.getFreeServiceFeeSwitch(), Franchisee.FREE_SERVICE_FEE_SWITCH_CLOSE)) {
            log.warn("isSupportFreeServiceFeeCar WARN! freeServiceFeeSwitch is close , franchisee is {} ", userInfo.getFranchiseeId());
            return dto;
        }

        // 用户是否已经支付过免押服务费
        Integer existsPaySuccessOrder = applicationContext.getBean(FreeServiceFeeOrderService.class).existsPaySuccessOrder(depositOrderId, userInfo.getUid());
        if (Objects.nonNull(existsPaySuccessOrder)) {
            log.info("isSupportFreeServiceFeeCar Info! current User Payed FreeServiceFee, freeDepositOrderId is {} , uid is {} ", depositOrderId, userInfo.getUid());
            return dto;
        }

        return dto.setSupportFreeServiceFee(true).setFreeServiceFee(franchisee.getFreeServiceFee());
    }


    @Override
    public FreeServiceFeeOrder createFreeServiceFeeOrder(CreateFreeServiceFeeOrderDTO dto) {
        UserInfo userInfo = dto.getUserInfo();
        String freeServiceFeeOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.FREE_SERVICE_FEE, userInfo.getUid());
        return FreeServiceFeeOrder.builder().uid(userInfo.getUid()).orderId(freeServiceFeeOrderId).freeDepositOrderId(dto.getDepositOrderId())
                .payAmount(dto.getFreeServiceFee()).status(dto.getStatus())
                .tenantId(userInfo.getTenantId()).franchiseeId(userInfo.getFranchiseeId())
                .storeId(userInfo.getStoreId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .paymentChannel(StrUtil.isNotBlank(dto.getPaymentChannel()) ? dto.getPaymentChannel() : null)
                .payTime(Objects.nonNull(dto.getPayTime()) ? dto.getPayTime() : null)
                .build();
    }
}
