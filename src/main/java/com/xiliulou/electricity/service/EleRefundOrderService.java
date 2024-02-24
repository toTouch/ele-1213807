package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.RefundOrder;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.vo.EleRefundOrderVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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


    //调起退款
    WechatJsapiRefundResultDTO commonCreateRefundOrder(RefundOrder refundOrder,
                                                       HttpServletRequest request) throws WechatPayException;


    Pair<Boolean, Object> notifyDepositRefundOrder(WechatJsapiRefundOrderCallBackResource callBackResource);

//    R handleRefundOrder(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid, HttpServletRequest request);
    Triple<Boolean, String, Object> handleRefundOrder(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid, HttpServletRequest request);

    R handleOffLineRefundRentCar(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid, HttpServletRequest request);

    R queryUserDepositPayType(Long uid);

    R batteryOffLineRefund(String errMsg, BigDecimal refundAmount, Long uid, Integer refundType);

    R queryList(EleRefundQuery eleRefundQuery);


    List<EleRefundOrderVO> selectCarRefundPageList(EleRefundQuery eleRefundQuery);

    Integer selectCarRefundPageCount(EleRefundQuery eleRefundQuery);

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
     * @param orderId
     * @return
     */
    boolean checkDepositOrderIsRefund(String orderId, Integer refundOrderType);
    
    BigDecimal queryTurnOverByTime(Integer tenantId, Long todayStartTime, Integer refundOrderType,
            List<Long> franchiseeIds, Integer payType);
    
    BigDecimal queryCarRefundTurnOverByTime(Integer tenantId, Long todayStartTime, Integer refundOrderType,
            List<Long> franchiseeIds, Integer payType);
    
    Long queryRefundTime(String orderId, Integer refundOrderType);

    Triple<Boolean,String,Object> batteryFreeDepositRefund(String errMsg, Long uid);

    Triple<Boolean, String, Object> carFreeDepositRefund(String errMsg, Long uid);

    List<EleRefundOrder> selectBatteryFreeDepositRefundingOrder(Integer offset, Integer size);

    List<EleRefundOrder> selectCarFreeDepositRefundingOrder(int offset, Integer refundOrderLimit);

    Triple<Boolean, String, Object> batteryFreeDepostRefundAudit(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid);
    
    Triple<Boolean, String, Object> carRefundDepositReview(Long id, String errMsg, Integer status,
            BigDecimal refundAmount, HttpServletRequest request);
    
    Triple<Boolean, String, Object> carFreeDepostRefundAudit(Long id, String errMsg, Integer status,
            BigDecimal refundAmount);

    List<EleRefundOrder> selectByOrderId(String orderId);

    EleRefundOrder selectLatestRefundDepositOrder(String paymentOrderNo);
    
    Integer existByOrderIdAndStatus(String orderId, List<Integer> statusList);
    
    List<EleRefundOrder> selectByOrderIdNoFilerStatus(String orderId);
    
    Triple<Boolean, String, Object> refund(BigDecimal refundAmount, Long uid, String orderId, HttpServletRequest request);
    
    EleRefundOrder queryLastByUid(Long uid);
}
