package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.query.InsuranceOrderAdd;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.query.IntegratedPaymentAdd;
import com.xiliulou.electricity.query.UnionTradeOrderAdd;

import javax.servlet.http.HttpServletRequest;

/**
 * 混合支付(UnionTradeOrder)表服务接口
 *
 * @author makejava
 * @since 2022-11-03 13:37:11
 */
public interface TradeOrderService {

    R createOrder(UnionTradeOrderAdd unionTradeOrderAdd, HttpServletRequest request);

    R integratedPayment(IntegratedPaymentAdd integratedPaymentAdd,HttpServletRequest request);

}
