/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/30
 */

package com.xiliulou.electricity.task.profitsharing;

import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailUnfreezeStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderStatusEnum;
import com.xiliulou.pay.profitsharing.request.wechat.WechatProfitSharingCommonRequest;

import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.pay.base.enums.ChannelEnum;
import com.xiliulou.pay.profitsharing.request.wechat.WechatProfitSharingQueryOrderRequest;
import com.xiliulou.pay.profitsharing.response.BaseProfitSharingQueryOrderResp;
import com.xiliulou.pay.profitsharing.response.wechat.ReceiverResp;
import com.xiliulou.pay.profitsharing.response.wechat.WechatProfitSharingQueryOrderResp;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/30 09:07
 */
@Slf4j
@Component
@JobHandler(value = "wechatProfitSharingOrderQueryTask")
public class WechatProfitSharingOrderQueryTask extends AbstractProfitSharingOrderQueryTask<WechatPayParamsDetails> {
    
    
    @Override
    protected String getChannel() {
        return ChannelEnum.WECHAT.getCode();
    }
    
    @Override
    protected Boolean queryBuildProfitSharingOrder(WechatPayParamsDetails payParams, ProfitSharingOrder order, List<ProfitSharingOrderDetail> curOrderDetails) {
        
        WechatProfitSharingCommonRequest wechatProfitSharingCommonRequest = ElectricityPayParamsConverter.optWechatProfitSharingCommonRequest(payParams);
        WechatProfitSharingQueryOrderRequest queryOrderRequest = new WechatProfitSharingQueryOrderRequest();
        queryOrderRequest.setCommonParam(wechatProfitSharingCommonRequest);
        queryOrderRequest.setTransactionId(order.getThirdTradeOrderNo());
        queryOrderRequest.setOutOrderNo(order.getOrderNo());
        queryOrderRequest.setChannel(ChannelEnum.WECHAT);
        try {
            BaseProfitSharingQueryOrderResp resp = profitSharingServiceAdapter.query(queryOrderRequest);
            if (Objects.isNull(resp)) {
                log.warn("WechatProfitSharingOrderQueryTask.queryBuildProfitSharingOrder WARN! result is null");
                return false;
            }
            WechatProfitSharingQueryOrderResp queryOrderResp = (WechatProfitSharingQueryOrderResp) resp;
            
            String state = queryOrderResp.getState();
            if ("FINISHED".equals(state)) {
                order.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_COMPLETE.getCode());
            } else {
                order.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_IN_PROCESS.getCode());
            }
            
            Map<String, ReceiverResp> accountMap = Optional.ofNullable(queryOrderResp.getReceivers()).orElse(Collections.emptyList()).stream()
                    .collect(Collectors.toMap(ReceiverResp::getAccount, Function.identity(), (k1, k2) -> k1));
            
            curOrderDetails.forEach(orderDetail -> {
                ReceiverResp receiverResp = accountMap.get(orderDetail.getProfitSharingReceiveAccount());
                if (Objects.isNull(receiverResp)) {
                    log.warn("WechatProfitSharingOrderQueryTask.queryBuildProfitSharingOrder WARN! orderDetailId:{}, wechat result is null", orderDetail.getId());
                    return;
                }
                String result = receiverResp.getResult();
                if ("PENDING".equals(result)) {
                    // 分账处理中
                    orderDetail.setStatus(ProfitSharingOrderDetailStatusEnum.IN_PROCESS.getCode());
                } else if ("SUCCESS".equals(result)) {
                    orderDetail.setStatus(ProfitSharingOrderDetailStatusEnum.COMPLETE.getCode());
                } else {
                    orderDetail.setStatus(ProfitSharingOrderDetailStatusEnum.FAIL.getCode());
                    orderDetail.setFailReason(receiverResp.getFailReason());
                    orderDetail.setUnfreezeStatus(ProfitSharingOrderDetailUnfreezeStatusEnum.PENDING.getCode());
                }
            });
            return true;
        } catch (Exception e) {
            log.error("WechatProfitSharingOrderQueryTask.queryBuildProfitSharingOrder Exception:", e);
            return false;
        }
    }
    
    
    /**
     * 查询构建支付配置
     *
     * @param tenantFranchiseePayParamMap
     * @param tenantId
     * @param franchiseeIds
     * @author caobotao.cbt
     * @date 2024/8/27 11:06
     */
    @Override
    protected void queryBuildTenantFranchiseePayParamMap(Map<String, WechatPayParamsDetails> tenantFranchiseePayParamMap, Integer tenantId, Set<Long> franchiseeIds) {
        payParamsQuerySupport.queryBuildTenantFranchiseePayParamMap(tenantFranchiseePayParamMap, tenantId, franchiseeIds);
    }
}
