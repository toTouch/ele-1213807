package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.query.installment.InstallmentPayQuery;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 混合支付(UnionTradeOrder)表服务接口
 *
 * @author makejava
 * @since 2022-11-03 13:37:11
 */
public interface TradeOrderService {

    Triple<Boolean, String, Object> integratedPayment(IntegratedPaymentAdd integratedPaymentAdd, HttpServletRequest request);

    Triple<Boolean, String, Object> handleTotalAmountZero(UserInfo userInfo, List<String> orderList, List<Integer> orderTypeList, InstallmentRecord installmentRecord);

    Triple<Boolean, String, Object> payMemberCardAndInsurance(BatteryMemberCardAndInsuranceQuery query, HttpServletRequest request);

    Triple<Boolean, String, Object> payServiceFee( HttpServletRequest request);
    
    /**
     * 用户端购买分期套餐
     * @param query 购买请求参数
     * @param request 请求对象
     * @return 携带二维码连接的返回结果
     */
    R<Object> installmentPayment(InstallmentPayQuery query, HttpServletRequest request);
}
