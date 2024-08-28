package com.xiliulou.electricity.service.impl.profitsharing;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.profitsharing.ProfitSharingOrderDetailConstant;
import com.xiliulou.electricity.constant.profitsharing.ProfitSharingTradeOrderConstant;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingBusinessTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailUnfreezeStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderTypeEnum;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingOrderDetailMapper;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingOrderMapper;
import com.xiliulou.electricity.mq.model.ProfitSharingTradeOrderRefund;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.pay.base.exception.ProfitSharingException;
import com.xiliulou.pay.profitsharing.ProfitSharingServiceAdapter;
import com.xiliulou.pay.profitsharing.request.wechat.WechatProfitSharingCommonRequest;
import com.xiliulou.pay.profitsharing.request.wechat.WechatProfitSharingUnfreezeRequest;
import com.xiliulou.pay.profitsharing.response.wechat.ReceiverResp;
import com.xiliulou.pay.profitsharing.response.wechat.WechatProfitSharingUnfreezeResp;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 分账订单表(profitSharingOrder)表服务实现类
 *
 * @author maxiaodong
 * @since 2024-08-22 16:58:57
 */
@Service
@Slf4j
public class ProfitSharingOrderServiceImpl implements ProfitSharingOrderService {
    @Resource
    private ProfitSharingOrderMapper profitSharingOrderMapper;
    
    @Resource
    private ProfitSharingOrderDetailMapper profitSharingOrderDetailMapper;
    
    @Resource
    private ProfitSharingServiceAdapter profitSharingServiceAdapter;
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    
    @Override
    @Slave
    public boolean existsUnfreezeByThirdOrderNo(String thirdOrderNo) {
        Integer count = profitSharingOrderMapper.existsUnfreezeByThirdOrderNo(thirdOrderNo);
        if (Objects.nonNull(count)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public int insert(ProfitSharingOrder profitSharingOrder) {
        return profitSharingOrderMapper.insert(profitSharingOrder);
    }
    
    @Override
    public void doUnFreeze(ProfitSharingTradeOrder profitSharingTradeOrder, ProfitSharingTradeOrderRefund profitSharingTradeOrderRefund,
            ProfitSharingTradeMixedOrder profitSharingTradeMixedOrder) {
        WechatPayParamsDetails wechatPayParamsDetails = null;
        try {
            wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(profitSharingTradeOrder.getTenantId(),
                    profitSharingTradeOrder.getFranchiseeId());
        } catch (WechatPayException e) {
            log.warn("PROFIT SHARING ORDE REFUND CONSUMER WARN!not found pay params,refundOrderNo={}", profitSharingTradeOrderRefund.getRefundOrderNo());
        }
    
        if (Objects.isNull(wechatPayParamsDetails)) {
            log.warn("PROFIT SHARING ORDE REFUND CONSUMER WARN!not found pay params,refundOrderNo={}", profitSharingTradeOrderRefund.getRefundOrderNo());
        }
    
        // 调用解冻接口
        try {
            WechatProfitSharingUnfreezeRequest unfreezeRequest = new WechatProfitSharingUnfreezeRequest();
            unfreezeRequest.setCommonParam(new WechatProfitSharingCommonRequest());
            unfreezeRequest.setOutOrderNo(OrderIdUtil.generateBusinessId(BusinessType.PROFIT_SHARING_ORDER_UNFREEZE, profitSharingTradeOrder.getUid()));
            unfreezeRequest.setTransactionId(profitSharingTradeOrder.getThirdOrderNo());
            unfreezeRequest.setDescription(ProfitSharingTradeOrderConstant.UNFREEZE_DESC);
        
            log.info("PROFIT SHARING ORDE REFUND CONSUMER INFO!unfreeze ，refundOrderNo={}, request={}, ", profitSharingTradeOrderRefund.getRefundOrderNo(), unfreezeRequest);
            
            WechatProfitSharingUnfreezeResp unfreeze = (WechatProfitSharingUnfreezeResp) profitSharingServiceAdapter.unfreeze(unfreezeRequest);
        
            // 保存解冻分账订单
            ProfitSharingOrder profitSharingOrder = new ProfitSharingOrder();
            // 分账单号
            profitSharingOrder.setOrderNo(unfreezeRequest.getOutOrderNo());
            profitSharingOrder.setBusinessType(ProfitSharingBusinessTypeEnum.UNFREEZE.getCode());
            profitSharingOrder.setAmount(profitSharingTradeMixedOrder.getAmount());
            profitSharingOrder.setThirdTradeOrderNo(profitSharingTradeOrder.getThirdOrderNo());
            // 如果不是混合支付，则业务订单号等于换电支付订单号
            if (Objects.equals(profitSharingTradeMixedOrder.getWhetherMixedPay(), ProfitSharingTradeOrderConstant.WHETHER_MIXED_PAY_NO)) {
                profitSharingOrder.setBusinessOrderNo(profitSharingTradeOrderRefund.getOrderNo());
            }
        
            if (Objects.nonNull(unfreeze)) {
                profitSharingOrder.setThirdOrderNo(unfreeze.getOrderId());
            }
        
            // 状态已受理
            profitSharingOrder.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_ACCEPT.getCode());
            // 解冻
            profitSharingOrder.setType(ProfitSharingOrderTypeEnum.UNFREEZE.getCode());
            profitSharingOrder.setCreateTime(System.currentTimeMillis());
            profitSharingOrder.setTenantId(profitSharingTradeOrder.getTenantId());
            profitSharingOrder.setFranchiseeId(profitSharingTradeOrder.getFranchiseeId());
            profitSharingOrder.setThirdMerchantId(profitSharingTradeOrder.getThirdMerchantId());
            // 分账方类型
            if (Objects.equals(profitSharingTradeOrder.getFranchiseeId(), NumberConstant.ZERO_L)) {
                profitSharingOrder.setOutAccountType(ProfitSharingOrderDetailConstant.OUT_ACCOUNT_TYPE_DEFAULT);
            } else {
                profitSharingOrder.setOutAccountType(ProfitSharingOrderDetailConstant.OUT_ACCOUNT_TYPE_FRANCHISEE);
            }
    
            profitSharingOrderMapper.insert(profitSharingOrder);
        
            // 分账明细
            ProfitSharingOrderDetail profitSharingOrderDetail = new ProfitSharingOrderDetail();
            profitSharingOrderDetail.setThirdTradeOrderNo(profitSharingTradeOrder.getThirdOrderNo());
            profitSharingOrderDetail.setProfitSharingReceiveAccount(profitSharingTradeOrder.getThirdMerchantId());
            profitSharingOrderDetail.setScale(null);
            profitSharingOrderDetail.setProfitSharingAmount(null);
            profitSharingOrderDetail.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_FAIL.getCode());
            profitSharingOrderDetail.setTenantId(profitSharingTradeOrder.getTenantId());
            profitSharingOrderDetail.setFranchiseeId(profitSharingTradeOrder.getFranchiseeId());
            profitSharingOrderDetail.setOutAccountType(profitSharingOrder.getOutAccountType());
            // 无需解冻
            profitSharingOrderDetail.setUnfreezeStatus(ProfitSharingOrderDetailUnfreezeStatusEnum.DISPENSE_WITH.getCode());
            profitSharingOrderDetail.setCreateTime(System.currentTimeMillis());
            profitSharingOrderDetail.setUpdateTime(System.currentTimeMillis());
            profitSharingOrderDetail.setBusinessType(ProfitSharingBusinessTypeEnum.UNFREEZE.getCode());
            profitSharingOrderDetail.setOrderDetailNo(OrderIdUtil.generateBusinessId(BusinessType.PROFIT_SHARING_ORDER_DETAIL, profitSharingTradeOrder.getUid()));
            profitSharingOrderDetail.setProfitSharingOrderId(profitSharingOrder.getId());
        
            if (Objects.nonNull(unfreeze) && ObjectUtils.isNotEmpty(unfreeze.getReceivers())) {
                List<ReceiverResp> receivers = unfreeze.getReceivers();
            
                if (Objects.nonNull(new BigDecimal(receivers.get(0).getAmount()))) {
                    profitSharingOrderDetail.setProfitSharingAmount(new BigDecimal(receivers.get(0).getAmount()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_DOWN));
                }
            
                // 第三方分账明细单号
                profitSharingOrderDetail.setThirdOrderDetailNo(receivers.get(0).getDetailId());
            }
    
            profitSharingOrderDetailMapper.insert(profitSharingOrderDetail);
        
        } catch (ProfitSharingException e) {
            log.error("PROFIT SHARING ORDER REFUND CONSUMER ERROR!", e);
        }
        
    }
}
