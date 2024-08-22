package com.xiliulou.electricity.service.handler.impl;

import com.xiliulou.electricity.dto.FreeDepositOrderDTO;
import com.xiliulou.electricity.enums.FreeDepositChannelEnum;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.service.handler.CommonFreeDeposit;
import com.xiliulou.electricity.service.handler.FreeDepositService;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: PxzFreeDepositOrderServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-22 15:29
 */
@Service("pxzFreeDepositOrderServiceImpl")
@Slf4j
public class PxzFreeDepositOrderServiceImpl extends CommonFreeDeposit implements FreeDepositService {
    
    @Resource
    PxzDepositService pxzDepositService;
    
    @Override
    public Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request) {
        String orderId = request.getFreeDepositOrderId();
        PxzCommonRsp<String> callPxzRsp = null;
        try {
            callPxzRsp = pxzDepositService.freeDepositOrder(buildFreeDepositOrderPxzRequest(request));
        } catch (Exception e) {
            log.error("Pxz ERROR! freeDepositOrder fail!  orderId={}", orderId, e);
            return Triple.of(false, "100401", "免押调用失败！");
        }
        
        if (Objects.isNull(callPxzRsp)) {
            log.error("Pxz ERROR! freeDepositOrder fail! rsp is null!  orderId={}", orderId);
            return Triple.of(false, "100401", "免押调用失败！");
        }
        
        if (!callPxzRsp.isSuccess()) {
            return Triple.of(false, "100401", callPxzRsp.getRespDesc());
        }
        FreeDepositOrderDTO dto = FreeDepositOrderDTO.builder().channel(FreeDepositChannelEnum.PXZ.getChannel()).data(callPxzRsp.getData()).build();
        return Triple.of(true, null, dto);
    }
}
