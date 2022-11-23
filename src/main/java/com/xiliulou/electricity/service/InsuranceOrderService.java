package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.query.ElectricityMemberCardOrderQuery;
import com.xiliulou.electricity.query.InsuranceOrderAdd;
import com.xiliulou.electricity.query.InsuranceOrderQuery;

import javax.servlet.http.HttpServletRequest;

/**
 * 换电柜保险订单(InsuranceOrder)表服务接口
 *
 * @author makejava
 * @since 2022-11-03 13:37:11
 */
public interface InsuranceOrderService {

    R queryList(InsuranceOrderQuery insuranceOrderQuery);

    R queryCount(InsuranceOrderQuery insuranceOrderQuery);

    R createOrder(InsuranceOrderAdd insuranceOrderAdd, HttpServletRequest request);

    InsuranceOrder queryByOrderId(String orderNo);

    Integer updateOrderStatusById(InsuranceOrder insuranceOrder);

    R queryInsurance(Long franchiseeId);

    void insert(InsuranceOrder insuranceOrder);
}
