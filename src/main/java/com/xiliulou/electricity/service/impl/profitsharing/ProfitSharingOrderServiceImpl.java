package com.xiliulou.electricity.service.impl.profitsharing;

import com.xiliulou.core.base.enums.ChannelEnum;
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
import com.xiliulou.electricity.enums.ElectricityPayParamsConfigEnum;
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
import com.xiliulou.pay.base.exception.ProfitSharingException;
import com.xiliulou.pay.profitsharing.ProfitSharingServiceAdapter;
import com.xiliulou.pay.profitsharing.request.wechat.WechatProfitSharingUnfreezeRequest;
import com.xiliulou.pay.profitsharing.response.wechat.ReceiverResp;
import com.xiliulou.pay.profitsharing.response.wechat.WechatProfitSharingUnfreezeResp;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
            return;
        }
        
        ProfitSharingOrder profitSharingOrderUpdate = new ProfitSharingOrder();
        ProfitSharingOrderDetail profitSharingOrderDetailUpdate = new ProfitSharingOrderDetail();
    
        try {
            // 保存解冻分账订单
            ProfitSharingOrder profitSharingOrder = new ProfitSharingOrder();
            // 分账单号
            profitSharingOrder.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.PROFIT_SHARING_ORDER_UNFREEZE, profitSharingTradeMixedOrder.getUid()));
            profitSharingOrder.setBusinessType(ProfitSharingBusinessTypeEnum.UNFREEZE.getCode());
            profitSharingOrder.setAmount(profitSharingTradeMixedOrder.getAmount());
            profitSharingOrder.setThirdTradeOrderNo(profitSharingTradeMixedOrder.getThirdOrderNo());
            profitSharingOrder.setChannel(ChannelEnum.WECHAT.getCode());
            // 如果不是混合支付，则业务订单号等于换电支付订单号
            if (Objects.equals(profitSharingTradeMixedOrder.getWhetherMixedPay(), ProfitSharingTradeOrderConstant.WHETHER_MIXED_PAY_NO)) {
                String orderNo = profitSharingTradeOrderService.queryOrderNoyByThirdOrderNo(profitSharingTradeMixedOrder.getThirdOrderNo());
                profitSharingOrder.setBusinessOrderNo(orderNo);
            }
        
            // 状态已受理
            profitSharingOrder.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_ACCEPT.getCode());
            // 解冻
            profitSharingOrder.setType(ProfitSharingOrderTypeEnum.UNFREEZE.getCode());
            profitSharingOrder.setCreateTime(System.currentTimeMillis());
            profitSharingOrder.setTenantId(profitSharingTradeMixedOrder.getTenantId());
            profitSharingOrder.setFranchiseeId(profitSharingTradeMixedOrder.getFranchiseeId());
            profitSharingOrder.setThirdMerchantId(profitSharingTradeMixedOrder.getThirdMerchantId());
            profitSharingOrder.setCreateTime(System.currentTimeMillis());
            profitSharingOrder.setUpdateTime(System.currentTimeMillis());
            // 分账方类型
            if (Objects.equals(profitSharingTradeMixedOrder.getFranchiseeId(), NumberConstant.ZERO_L)) {
                profitSharingOrder.setOutAccountType(ElectricityPayParamsConfigEnum.DEFAULT_CONFIG.getType());
            } else {
                profitSharingOrder.setOutAccountType(ElectricityPayParamsConfigEnum.FRANCHISEE_CONFIG.getType());
            }
            
            // 保存分账订单
            profitSharingOrderMapper.insert(profitSharingOrder);
        
            // 分账明细
            ProfitSharingOrderDetail profitSharingOrderDetail = new ProfitSharingOrderDetail();
            profitSharingOrderDetail.setThirdTradeOrderNo(profitSharingTradeMixedOrder.getThirdOrderNo());
            profitSharingOrderDetail.setProfitSharingReceiveAccount(profitSharingTradeMixedOrder.getThirdMerchantId());
            profitSharingOrderDetail.setProfitSharingReceiveName(profitSharingTradeMixedOrder.getThirdMerchantId());
            profitSharingOrderDetail.setScale(null);
            profitSharingOrderDetail.setProfitSharingAmount(null);
            profitSharingOrderDetail.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_ACCEPT.getCode());
            profitSharingOrderDetail.setTenantId(profitSharingTradeMixedOrder.getTenantId());
            profitSharingOrderDetail.setType(ProfitSharingOrderTypeEnum.UNFREEZE.getCode());
            profitSharingOrderDetail.setFranchiseeId(profitSharingTradeMixedOrder.getFranchiseeId());
            profitSharingOrderDetail.setOutAccountType(profitSharingOrder.getOutAccountType());
            // 无需解冻
            profitSharingOrderDetail.setUnfreezeStatus(ProfitSharingOrderDetailUnfreezeStatusEnum.DISPENSE_WITH.getCode());
            profitSharingOrderDetail.setCreateTime(System.currentTimeMillis());
            profitSharingOrderDetail.setUpdateTime(System.currentTimeMillis());
            profitSharingOrderDetail.setBusinessType(ProfitSharingBusinessTypeEnum.UNFREEZE.getCode());
            profitSharingOrderDetail.setOrderDetailNo(OrderIdUtil.generateBusinessId(BusinessType.PROFIT_SHARING_ORDER_DETAIL, profitSharingTradeMixedOrder.getUid()));
            profitSharingOrderDetail.setProfitSharingOrderId(profitSharingOrder.getId());
            profitSharingOrderDetail.setChannel(ChannelEnum.WECHAT.getCode());
            
            // 保存分账明细
            profitSharingOrderDetailMapper.insert(profitSharingOrderDetail);
    
            WechatProfitSharingUnfreezeRequest unfreezeRequest = new WechatProfitSharingUnfreezeRequest();
            unfreezeRequest.setCommonParam(ElectricityPayParamsConverter.optWechatProfitSharingCommonRequest(wechatPayParamsDetails));
            unfreezeRequest.setOutOrderNo(profitSharingOrder.getOrderNo());
            unfreezeRequest.setTransactionId(profitSharingTradeMixedOrder.getThirdOrderNo());
            unfreezeRequest.setDescription(ProfitSharingTradeOrderConstant.UNFREEZE_DESC);
            
            // 设置修改信息id
            profitSharingOrderUpdate.setId(profitSharingOrder.getId());
            profitSharingOrderDetailUpdate.setId(profitSharingOrderDetail.getId());
            
            log.info("PROFIT SHARING UNFREEZE INFO!unfreeze start, thirdTradeOrderNo={}", profitSharingTradeMixedOrder.getThirdOrderNo());
    
            // 调用解冻接口
            WechatProfitSharingUnfreezeResp unfreeze = (WechatProfitSharingUnfreezeResp) profitSharingServiceAdapter.unfreeze(unfreezeRequest);
    
            if (Objects.nonNull(unfreeze)) {
                profitSharingOrderUpdate.setThirdOrderNo(unfreeze.getOrderId());
            }
    
            if (Objects.nonNull(unfreeze) && ObjectUtils.isNotEmpty(unfreeze.getReceivers())) {
                List<ReceiverResp> receivers = unfreeze.getReceivers();
        
                // 第三方分账明细单号
                profitSharingOrderDetailUpdate.setThirdOrderDetailNo(receivers.get(0).getDetailId());
            }
        } catch (ProfitSharingException e) {
            profitSharingOrderUpdate.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_COMPLETE.getCode());
            profitSharingOrderDetailUpdate.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_FAIL.getCode());
            
            String failReason = e.getMessage();
            if (StringUtils.isNotEmpty(e.getMessage()) && e.getMessage().length() > 400) {
                failReason = e.getMessage().substring(0, 400);
            }
            profitSharingOrderDetailUpdate.setFailReason(failReason);
            
            log.error("PROFIT SHARING UNFREEZE ERROR!, thirdTradeOrderNo={}", profitSharingTradeMixedOrder.getThirdOrderNo(), e);
            
            throw new ProfitSharingException(e.getMessage());
        } finally {
            profitSharingOrderUpdate.setUpdateTime(System.currentTimeMillis());
            profitSharingOrderDetailUpdate.setUpdateTime(System.currentTimeMillis());
    
            // 修改分账订单的返回信息
            profitSharingOrderMapper.updateUnfreezeResultById(profitSharingOrderUpdate);
            
            // 修改分账订单明细的返回信息
            profitSharingOrderDetailMapper.updateUnfreezeResultById(profitSharingOrderDetailUpdate);
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
