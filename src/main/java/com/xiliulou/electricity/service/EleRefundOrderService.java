package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.RefundOrder;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.vo.EleRefundOrderVO;
import com.xiliulou.pay.base.dto.BasePayOrderRefundDTO;
import com.xiliulou.pay.base.exception.PayException;
import com.xiliulou.pay.base.request.BaseOrderRefundCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * 退款订单表(TEleRefundOrder)表服务接口
 *
 * @author makejava
 * @since 2021-02-22 10:21:24
 */
public interface EleRefundOrderService {
    
    
    /**
     * 新增数据
     *
     * @param eleRefundOrder 实例对象
     * @return 实例对象
     */
    EleRefundOrder insert(EleRefundOrder eleRefundOrder);
    
    /**
     * 修改数据
     *
     * @param eleRefundOrder 实例对象
     * @return 实例对象
     */
    Integer update(EleRefundOrder eleRefundOrder);
    
    
    BasePayOrderRefundDTO commonCreateRefundOrderV2(RefundOrder refundOrder, BasePayConfig basePayConfig, HttpServletRequest request) throws PayException;
    
    
    Pair<Boolean, Object> notifyDepositRefundOrder(BaseOrderRefundCallBackResource callBackResource);
    
    Triple<Boolean, String, Object> handleRefundOrder(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid, Integer offlineRefund,
            HttpServletRequest request);
    
    R queryUserDepositPayType(Long uid);
    
    R batteryOffLineRefund(String errMsg, BigDecimal refundAmount, Long uid, Integer refundType, Integer offlineRefund);
    
    R queryList(EleRefundQuery eleRefundQuery);
    
    Integer queryCountByOrderId(String orderId, Integer refundOrderType);
    
    Integer queryIsRefundingCountByOrderId(String orderId);
    
    Integer queryStatusByOrderId(String orderId);
    
    R queryCount(EleRefundQuery eleRefundQuery);
    
    /**
     * 根据押金退款订单号查询用户的UserInfoId
     *
     * @param refundOrderNo
     * @return
     */
    Long queryUserInfoIdByRefundOrderNo(String refundOrderNo);
    
    BigDecimal queryTurnOver(Integer tenantId);
    
    /**
     * 检查押金订单是否退款
     *
     * @param orderId
     * @return
     */
    boolean checkDepositOrderIsRefund(String orderId, Integer refundOrderType);
    
    BigDecimal queryTurnOverByTime(Integer tenantId, Long todayStartTime, Integer refundOrderType, List<Long> franchiseeIds, Integer payType);
    
    BigDecimal queryCarRefundTurnOverByTime(Integer tenantId, Long todayStartTime, Integer refundOrderType, List<Long> franchiseeIds, Integer payType);
    
    Long queryRefundTime(String orderId, Integer refundOrderType);
    
    Triple<Boolean, String, Object> batteryFreeDepositRefund(String errMsg, Long uid);
    
    Triple<Boolean, String, Object> batteryFreeDepositRefundV2(String errMsg, Long uid, BigDecimal refundMoney);
    
    List<EleRefundOrder> selectBatteryFreeDepositRefundingOrder(Integer offset, Integer size);
    
    List<EleRefundOrder> selectCarFreeDepositRefundingOrder(int offset, Integer refundOrderLimit);
    
    Triple<Boolean, String, Object> batteryFreeDepostRefundAudit(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid);
    
    
    Triple<Boolean, String, Object> batteryFreeDepostRefundAuditV2(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid);

    List<EleRefundOrder> selectByOrderId(String orderId);
    
    EleRefundOrder selectLatestRefundDepositOrder(String paymentOrderNo);
    
    Integer existByOrderIdAndStatus(String orderId, List<Integer> statusList);
    
    List<EleRefundOrder> selectByOrderIdNoFilerStatus(String orderId);
    
    EleRefundOrder queryLastByOrderId(String orderId);
    
    Integer existsRefundOrderByUid(Long uid, Long lostUserFirstRebateTime);
    
    Integer updateById(EleRefundOrder eleRefundOrderUpdate);
    
    R listSuperAdminPage(EleRefundQuery eleRefundQuery);
    
    Integer updateRefundAmountById(Long id, BigDecimal refundAmount);
}
