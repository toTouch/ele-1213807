package com.xiliulou.electricity.service.impl.profitsharing;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.profitsharing.ProfitSharingOrderDetailConstant;
import com.xiliulou.electricity.constant.profitsharing.ProfitSharingTradeOrderConstant;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingBusinessTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailUnfreezeStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderTypeEnum;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingOrderDetailMapper;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingOrderMapper;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingOrderQueryModel;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeOrderService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.pay.base.enums.ChannelEnum;
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
import org.springframework.transaction.annotation.Transactional;

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
    
    @Resource
    private ProfitSharingTradeOrderService profitSharingTradeOrderService;
    
    
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
    @Transactional(rollbackFor = Exception.class)
    public void doUnFreeze(ProfitSharingTradeMixedOrder profitSharingTradeMixedOrder) throws ProfitSharingException {
        WechatPayParamsDetails wechatPayParamsDetails = null;
        try {
            wechatPayParamsDetails = wechatPayParamsBizService.getPreciseCacheByTenantIdAndFranchiseeId(profitSharingTradeMixedOrder.getTenantId(),
                    profitSharingTradeMixedOrder.getFranchiseeId(), null);
        } catch (WechatPayException e) {
            log.warn("PROFIT SHARING UNFREEZE WARN!not found pay params, thirdTradeOrderNo={}", profitSharingTradeMixedOrder.getThirdOrderNo());
        }
    
        if (Objects.isNull(wechatPayParamsDetails)) {
            log.warn("PROFIT SHARING UNFREEZE WARN!not found pay params,thirdTradeOrderNo={}", profitSharingTradeMixedOrder.getThirdOrderNo());
        }
    
        // 调用解冻接口
        try {
            WechatProfitSharingUnfreezeRequest unfreezeRequest = new WechatProfitSharingUnfreezeRequest();
            unfreezeRequest.setCommonParam(ElectricityPayParamsConverter.optWechatProfitSharingCommonRequest(wechatPayParamsDetails));
            unfreezeRequest.setOutOrderNo(OrderIdUtil.generateBusinessId(BusinessType.PROFIT_SHARING_ORDER_UNFREEZE, profitSharingTradeMixedOrder.getUid()));
            unfreezeRequest.setTransactionId(profitSharingTradeMixedOrder.getThirdOrderNo());
            unfreezeRequest.setDescription(ProfitSharingTradeOrderConstant.UNFREEZE_DESC);
            unfreezeRequest.setChannel(ChannelEnum.WECHAT);
        
            log.info("PROFIT SHARING UNFREEZE INFO!unfreeze, thirdTradeOrderNo={}, request={}, ", profitSharingTradeMixedOrder.getThirdOrderNo(), unfreezeRequest);
            
            WechatProfitSharingUnfreezeResp unfreeze = (WechatProfitSharingUnfreezeResp) profitSharingServiceAdapter.unfreeze(unfreezeRequest);
        
            // 保存解冻分账订单
            ProfitSharingOrder profitSharingOrder = new ProfitSharingOrder();
            // 分账单号
            profitSharingOrder.setOrderNo(unfreezeRequest.getOutOrderNo());
            profitSharingOrder.setBusinessType(ProfitSharingBusinessTypeEnum.UNFREEZE.getCode());
            profitSharingOrder.setAmount(profitSharingTradeMixedOrder.getAmount());
            profitSharingOrder.setThirdTradeOrderNo(profitSharingTradeMixedOrder.getThirdOrderNo());
            // 如果不是混合支付，则业务订单号等于换电支付订单号
            if (Objects.equals(profitSharingTradeMixedOrder.getWhetherMixedPay(), ProfitSharingTradeOrderConstant.WHETHER_MIXED_PAY_NO)) {
                String orderNo = profitSharingTradeOrderService.queryOrderNoyByThirdOrderNo(profitSharingTradeMixedOrder.getThirdOrderNo());
                profitSharingOrder.setBusinessOrderNo(orderNo);
            }
        
            if (Objects.nonNull(unfreeze)) {
                profitSharingOrder.setThirdOrderNo(unfreeze.getOrderId());
            }
        
            // 状态已受理
            profitSharingOrder.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_ACCEPT.getCode());
            // 解冻
            profitSharingOrder.setType(ProfitSharingOrderTypeEnum.UNFREEZE.getCode());
            profitSharingOrder.setCreateTime(System.currentTimeMillis());
            profitSharingOrder.setTenantId(profitSharingTradeMixedOrder.getTenantId());
            profitSharingOrder.setFranchiseeId(profitSharingTradeMixedOrder.getFranchiseeId());
            profitSharingOrder.setThirdMerchantId(profitSharingTradeMixedOrder.getThirdMerchantId());
            // 分账方类型
            if (Objects.equals(profitSharingTradeMixedOrder.getFranchiseeId(), NumberConstant.ZERO_L)) {
                profitSharingOrder.setOutAccountType(ProfitSharingOrderDetailConstant.OUT_ACCOUNT_TYPE_DEFAULT);
            } else {
                profitSharingOrder.setOutAccountType(ProfitSharingOrderDetailConstant.OUT_ACCOUNT_TYPE_FRANCHISEE);
            }
    
            profitSharingOrderMapper.insert(profitSharingOrder);
        
            // 分账明细
            ProfitSharingOrderDetail profitSharingOrderDetail = new ProfitSharingOrderDetail();
            profitSharingOrderDetail.setThirdTradeOrderNo(profitSharingTradeMixedOrder.getThirdOrderNo());
            profitSharingOrderDetail.setProfitSharingReceiveAccount(profitSharingTradeMixedOrder.getThirdMerchantId());
            profitSharingOrderDetail.setScale(null);
            profitSharingOrderDetail.setProfitSharingAmount(null);
            profitSharingOrderDetail.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_FAIL.getCode());
            profitSharingOrderDetail.setTenantId(profitSharingTradeMixedOrder.getTenantId());
            profitSharingOrderDetail.setFranchiseeId(profitSharingTradeMixedOrder.getFranchiseeId());
            profitSharingOrderDetail.setOutAccountType(profitSharingOrder.getOutAccountType());
            // 无需解冻
            profitSharingOrderDetail.setUnfreezeStatus(ProfitSharingOrderDetailUnfreezeStatusEnum.DISPENSE_WITH.getCode());
            profitSharingOrderDetail.setCreateTime(System.currentTimeMillis());
            profitSharingOrderDetail.setUpdateTime(System.currentTimeMillis());
            profitSharingOrderDetail.setBusinessType(ProfitSharingBusinessTypeEnum.UNFREEZE.getCode());
            profitSharingOrderDetail.setOrderDetailNo(OrderIdUtil.generateBusinessId(BusinessType.PROFIT_SHARING_ORDER_DETAIL, profitSharingTradeMixedOrder.getUid()));
            profitSharingOrderDetail.setProfitSharingOrderId(profitSharingOrder.getId());
        
            if (Objects.nonNull(unfreeze) && ObjectUtils.isNotEmpty(unfreeze.getReceivers())) {
                List<ReceiverResp> receivers = unfreeze.getReceivers();
            
                if (Objects.nonNull(new BigDecimal(receivers.get(0).getAmount()))) {
                    profitSharingOrderDetail.setProfitSharingAmount(new BigDecimal(receivers.get(0).getAmount()).divide(new BigDecimal(100)));
                }
            
                // 第三方分账明细单号
                profitSharingOrderDetail.setThirdOrderDetailNo(receivers.get(0).getDetailId());
            }
    
            profitSharingOrderDetailMapper.insert(profitSharingOrderDetail);
        
        } catch (ProfitSharingException e) {
            log.error("PROFIT SHARING UNFREEZE ERROR!, thirdTradeOrderNo={}", profitSharingTradeMixedOrder.getThirdOrderNo(), e);
            throw new ProfitSharingException(e.getMessage());
        }
        
    }
    
    @Override
    @Slave
    public List<String> listUnfreezeByThirdOrderNo(List<String> thirdOrderNoList) {
        return profitSharingOrderDetailMapper.selectListUnfreezeByThirdOrderNo(thirdOrderNoList);
    }
    
    @Override
    public int updateUnfreezeOrderById(ProfitSharingOrder profitSharingOrderUpdate) {
        return profitSharingOrderMapper.updateUnfreezeOrderById(profitSharingOrderUpdate);
    }
    
    @Slave
    @Override
    public List<ProfitSharingOrder> queryListByThirdOrderNos(Integer tenantId, List<String> thirdOrderNos) {
        return profitSharingOrderMapper.selectListByThirdOrderNos(tenantId,thirdOrderNos);
    }
    
    @Slave
    @Override
    public List<ProfitSharingOrder> queryByIdGreaterThanAndOtherConditions(ProfitSharingOrderQueryModel profitSharingOrderQueryModel) {
        return profitSharingOrderMapper.selectByIdGreaterThanAndOtherConditions(profitSharingOrderQueryModel);
    }
    
}
