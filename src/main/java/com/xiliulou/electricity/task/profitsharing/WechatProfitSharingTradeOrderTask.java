/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/26
 */

package com.xiliulou.electricity.task.profitsharing;

import com.xiliulou.core.base.enums.ChannelEnum;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailUnfreezeStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderStatusEnum;
import com.xiliulou.electricity.exception.BizException;

import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigReceiverTypeEnum;
import com.xiliulou.pay.base.exception.ProfitSharingException;
import com.xiliulou.pay.profitsharing.request.wechat.WechatProfitSharingCreateOrderRequest;
import com.xiliulou.pay.profitsharing.response.BaseProfitSharingCreateOrderResp;
import com.xiliulou.pay.profitsharing.response.wechat.ReceiverResp;
import com.xiliulou.pay.profitsharing.response.wechat.WechatProfitSharingCreateOrderResp;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
 * @date 2024/8/26 16:33
 */
@Slf4j
@Component
@JobHandler(value = "wechatProfitSharingTradeOrderTask")
public class WechatProfitSharingTradeOrderTask extends AbstractProfitSharingTradeOrderTask<WechatPayParamsDetails> {
    
    
    @Override
    protected String getChannel() {
        return ChannelEnum.WECHAT.getCode();
    }
    
    
    @Override
    protected void order(WechatPayParamsDetails payConfig, List<ProfitSharingCheckModel> profitSharingModels) {
        
        for (int i = 0; i < profitSharingModels.size(); i++) {
            ProfitSharingCheckModel profitSharingCheckModel = profitSharingModels.get(i);
            ProfitSharingOrder profitSharingOrder = profitSharingCheckModel.getProfitSharingOrder();
            
            List<WechatProfitSharingCreateOrderRequest.Receiver> receivers = this.buildReceivers(profitSharingCheckModel.getProfitSharingDetailsCheckModels());
            //
            WechatProfitSharingCreateOrderRequest orderRequest = new WechatProfitSharingCreateOrderRequest();
            orderRequest.setCommonParam(ElectricityPayParamsConverter.optWechatProfitSharingCommonRequest(payConfig));
            orderRequest.setTransactionId(profitSharingOrder.getThirdTradeOrderNo());
            orderRequest.setOutOrderNo(profitSharingOrder.getOrderNo());
            orderRequest.setUnfreezeUnsplit(i == (profitSharingModels.size() - 1));
            orderRequest.setReceivers(receivers);
            
            try {
                log.info("WechatProfitSharingTradeOrderTask.order transactionId:{},outOrderNo:{},unfreezeUnsplit:{},receivers:{}", orderRequest.getTransactionId(),
                        orderRequest.getOutOrderNo(), orderRequest.getUnfreezeUnsplit(), JsonUtil.toJson(receivers));
                
                BaseProfitSharingCreateOrderResp order = profitSharingServiceAdapter.order(orderRequest);
                if (Objects.isNull(order)) {
                    throw new BizException("分账返回异常");
                }
                
                // 成功处理
                WechatProfitSharingCreateOrderResp createOrderResp = (WechatProfitSharingCreateOrderResp) order;
                profitSharingOrder.setThirdOrderNo(createOrderResp.getTransactionId());
                // 接收方返回数据
                Map<String, ReceiverResp> accountReceiverMap = Optional.ofNullable(createOrderResp.getReceivers()).orElse(Collections.emptyList()).stream()
                        .collect(Collectors.toMap(ReceiverResp::getAccount, Function.identity(), (k1, k2) -> k1));
                
                profitSharingCheckModel.getProfitSharingDetailsCheckModels().stream()
                        .filter(v -> accountReceiverMap.containsKey(v.getProfitSharingOrderDetail().getProfitSharingReceiveAccount())).forEach(v -> {
                    // 赋值详情订单号
                    ProfitSharingOrderDetail profitSharingOrderDetail = v.getProfitSharingOrderDetail();
                    profitSharingOrderDetail.setOrderDetailNo(accountReceiverMap.get(profitSharingOrderDetail.getProfitSharingReceiveAccount()).getDetailId());
                    profitSharingOrderDetail.setUpdateTime(System.currentTimeMillis());
                });
                
            } catch (ProfitSharingException e) {
                log.warn("WechatProfitSharingTradeOrderTask.order ProfitSharingException:", e);
                buildError(profitSharingModels, e.getMessage());
            } catch (Exception e) {
                log.warn("WechatProfitSharingTradeOrderTask.order Exception:", e);
                buildError(profitSharingModels, "微信接口调用异常");
            }
            
        }
        
        
    }
    
    
    /**
     * 构建错误
     *
     * @param profitSharingModels
     * @author caobotao.cbt
     * @date 2024/9/4 17:10
     */
    private void buildError(List<ProfitSharingCheckModel> profitSharingModels, String msg) {
        if (msg.length() > 400) {
            msg = msg.substring(0, 400);
        }
        long timeMillis = System.currentTimeMillis();
        
        for (ProfitSharingCheckModel check : profitSharingModels) {
            check.setIsSuccess(false);
            check.getProfitSharingOrder().setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_FAIL.getCode());
            for (ProfitSharingDetailsCheckModel details : check.getProfitSharingDetailsCheckModels()) {
                details.setErrorMsg(msg);
                ProfitSharingOrderDetail profitSharingOrderDetail = details.getProfitSharingOrderDetail();
                profitSharingOrderDetail.setStatus(ProfitSharingOrderDetailStatusEnum.FAIL.getCode());
                profitSharingOrderDetail.setUnfreezeStatus(ProfitSharingOrderDetailUnfreezeStatusEnum.PENDING.getCode());
                profitSharingOrderDetail.setFailReason(details.getErrorMsg());
                profitSharingOrderDetail.setUpdateTime(timeMillis);
                profitSharingOrderDetail.setFinishTime(timeMillis);
            }
        }
    }
    
    /**
     * 接收方数据
     *
     * @param profitSharingDetailsCheckModels
     * @author caobotao.cbt
     * @date 2024/8/29 14:07
     */
    private List<WechatProfitSharingCreateOrderRequest.Receiver> buildReceivers(List<ProfitSharingDetailsCheckModel> profitSharingDetailsCheckModels) {
        return profitSharingDetailsCheckModels.stream().map(detailsCheckModels -> {
            ProfitSharingReceiverConfig receiverConfig = detailsCheckModels.getProfitSharingReceiverConfig();
            ProfitSharingOrderDetail orderDetail = detailsCheckModels.getProfitSharingOrderDetail();
            WechatProfitSharingCreateOrderRequest.Receiver receiver = new WechatProfitSharingCreateOrderRequest.Receiver();
            Integer receiverType = receiverConfig.getReceiverType();
            receiver.setType(ProfitSharingConfigReceiverTypeEnum.CODE_MAP.get(receiverType));
            receiver.setAccount(receiverConfig.getAccount());
            receiver.setName(receiverConfig.getReceiverName());
            receiver.setDescription(receiverConfig.getRemark());
            receiver.setAmount(orderDetail.getProfitSharingAmount().multiply(new BigDecimal(100)).stripTrailingZeros().intValue());
            return receiver;
        }).collect(Collectors.toList());
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
