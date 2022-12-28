package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.ElectricityMemberCardOrderQuery;
import com.xiliulou.electricity.query.InsuranceOrderAdd;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.vo.InsuranceOrderVO;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    int updateIsUseByOrderId(InsuranceOrder insuranceOrder);

    R queryInsurance();

    R homeOneQueryInsurance(Integer model,Long franchiseeId);

    void insert(InsuranceOrder insuranceOrder);

    Triple<Boolean, String, Object> handleRentBatteryInsurance(RentCarHybridOrderQuery query, UserInfo userInfo);

    List<InsuranceOrderVO> queryListByStatus(InsuranceOrderQuery insuranceOrderQuery);
}
