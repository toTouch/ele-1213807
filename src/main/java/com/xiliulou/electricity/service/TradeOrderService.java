package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.*;
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

    Triple<Boolean, String, Object> handleTotalAmountZero(UserInfo userInfo, List<String> orderList, List<Integer> orderTypeList);

    Triple<Boolean, String, Object> payMemberCardAndInsurance(BatteryMemberCardAndInsuranceQuery query, HttpServletRequest request);
}
