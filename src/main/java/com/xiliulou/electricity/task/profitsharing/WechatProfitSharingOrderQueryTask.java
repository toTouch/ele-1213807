/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/30
 */

package com.xiliulou.electricity.task.profitsharing;

import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailUnfreezeStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderStatusEnum;
import com.xiliulou.electricity.exception.BizException;
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
 * @date 2024/8/30 09:07
 */
@Slf4j
@Component
@JobHandler(value = "wechatProfitSharingOrderQueryTask")
public class WechatProfitSharingOrderQueryTask extends AbstractProfitSharingOrderQueryTask<WechatPayParamsDetails> {
    
    
    public static final String WECHAT_STATUS_FINISHED = "FINISHED";
    
    public static final String WECHAT_STATUS_PENDING = "PENDING";
    
    public static final String WECHAT_STATUS_SUCCESS = "SUCCESS";
    
    public static final String WECHAT_STATUS_CLOSED = "CLOSED";
    
    @Override
    protected String getChannel() {
        return ChannelEnum.WECHAT.getCode();
    }
    
    @Override
    protected void queryBuildProfitSharingOrder(WechatPayParamsDetails payParams, DealWithProfitSharingOrderModel orderModel) {
        
        ProfitSharingOrder order = orderModel.getOrder();
        
        List<ProfitSharingOrderDetail> curOrderDetails = orderModel.getCurOrderDetails();
        
        WechatProfitSharingCommonRequest wechatProfitSharingCommonRequest = ElectricityPayParamsConverter.optWechatProfitSharingCommonRequest(payParams);
        WechatProfitSharingQueryOrderRequest queryOrderRequest = new WechatProfitSharingQueryOrderRequest();
        queryOrderRequest.setCommonParam(wechatProfitSharingCommonRequest);
        queryOrderRequest.setTransactionId(order.getThirdTradeOrderNo());
        queryOrderRequest.setOutOrderNo(order.getOrderNo());
        try {
            BaseProfitSharingQueryOrderResp resp = profitSharingServiceAdapter.query(queryOrderRequest);
            if (Objects.isNull(resp)) {
                log.warn("WechatProfitSharingOrderQueryTask.queryBuildProfitSharingOrder WARN! result is null");
                throw new BizException("分账结果查询失败");
            }
            WechatProfitSharingQueryOrderResp queryOrderResp = (WechatProfitSharingQueryOrderResp) resp;
            
            String state = queryOrderResp.getState();
            if (WECHAT_STATUS_FINISHED.equals(state)) {
                // 完成
                order.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_COMPLETE.getCode());
            } else {
                //处理中
                order.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_IN_PROCESS.getCode());
            }
            
            // 根据分账账号将微信分账接受记录分组
            Map<String, ReceiverResp> accountMap = Optional.ofNullable(queryOrderResp.getReceivers()).orElse(Collections.emptyList()).stream()
                    .collect(Collectors.toMap(ReceiverResp::getAccount, Function.identity(), (k1, k2) -> k1));
            
            // 失败订单金额
            BigDecimal failAmount = orderModel.getFailAmount();
            
            for (ProfitSharingOrderDetail orderDetail : curOrderDetails) {
                ReceiverResp receiverResp = accountMap.get(orderDetail.getProfitSharingReceiveAccount());
                if (Objects.isNull(receiverResp)) {
                    // 无分账结果（正常情况不会存在）
                    log.warn("WechatProfitSharingOrderQueryTask.queryBuildProfitSharingOrder WARN! orderDetailId:{}, wechat result is null", orderDetail.getId());
                    continue;
                }
                
                String result = receiverResp.getResult();
                if (WECHAT_STATUS_PENDING.equals(result)) {
                    // 分账处理中
                    orderDetail.setStatus(ProfitSharingOrderDetailStatusEnum.IN_PROCESS.getCode());
                } else if (WECHAT_STATUS_SUCCESS.equals(result)) {
                    // 分账成功
                    orderDetail.setStatus(ProfitSharingOrderDetailStatusEnum.COMPLETE.getCode());
                    orderDetail.setFinishTime(System.currentTimeMillis());
                } else if (WECHAT_STATUS_CLOSED.equals(result)) {
                    // 分账失败
                    orderDetail.setStatus(ProfitSharingOrderDetailStatusEnum.FAIL.getCode());
                    orderDetail.setFailReason(receiverResp.getFailReason());
                    orderDetail.setUnfreezeStatus(ProfitSharingOrderDetailUnfreezeStatusEnum.PENDING.getCode());
                    orderDetail.setFinishTime(System.currentTimeMillis());
                    // 累加失败订单金额
                    orderModel.addFailAmount(orderDetail.getProfitSharingAmount());
                    
                } else {
                    log.warn("WechatProfitSharingOrderQueryTask.queryBuildProfitSharingOrder Unknown state:{} ", result);
                }
            }
            
        } catch (Exception e) {
            log.error("WechatProfitSharingOrderQueryTask.queryBuildProfitSharingOrder Exception:", e);
            throw new BizException("系统异常");
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
