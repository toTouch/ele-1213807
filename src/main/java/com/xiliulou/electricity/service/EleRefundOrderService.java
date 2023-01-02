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

    R handleRefundRentCar(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid, HttpServletRequest request);

    R handleOffLineRefundRentCar(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid, HttpServletRequest request);

    R queryUserDepositPayType(Long uid);

    R batteryOffLineRefund(String errMsg, BigDecimal refundAmount, Long uid, Integer refundType);

    R queryList(EleRefundQuery eleRefundQuery);


    List<EleRefundOrderVO> selectCarRefundPageList(EleRefundQuery eleRefundQuery);

    Integer selectCarRefundPageCount(EleRefundQuery eleRefundQuery);

    Integer queryCountByOrderId(String orderId);

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

    BigDecimal queryTurnOverByTime(Integer tenantId, Long todayStartTime,Integer refundOrderType);

    /**
     * 检查押金订单是否退款
     * @param orderId
     * @return
     */
    boolean checkDepositOrderIsRefund(String orderId);

    BigDecimal queryTurnOverByTime(Integer tenantId, Long todayStartTime,Integer refundOrderType,List<Long> franchiseeIds);
}
